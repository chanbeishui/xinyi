package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Q03真实HTTP/MySQL、私有MP4、真实墙钟与一次阅卷；不伪造TRAINING正向通过，不替换业务Service或鉴权。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=q03-isolated-test-phone-key",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class Q03TrainingIT extends TrainingHttpFixture {
    static final String POLICY_QUERY = "biz:trainingpolicy:query";
    static final String POLICY_EDIT = "biz:trainingpolicy:edit";
    static final String POLICY_PUBLISH = "biz:trainingpolicy:publish";
    static final String COURSE_QUERY = "biz:trainingcourse:query";

    @Autowired PlatformTransactionManager transactions;
    @Autowired IntegrationCrypto crypto;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean AuditService audits;
    @MockitoSpyBean RedisCache redis;

    record Lesson(Student person, JsonNode course) {}

    @Test
    void verifiedOwnerWithoutDriverRoleCanLearnItsExplicitCompanyCurriculum() {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode session =
                data(miniCall("GET", "/api/common/session", null, lesson.person().token()));
        assertFalse(session.path("availableRoles").toString().contains("DRIVER"));
        JsonNode overview = trainingView(lesson.person());
        assertEquals(Long.toString(COMPANY_A), overview.path("ownerDeptId").asText());
        assertFalse(overview.path("complete").asBoolean());
        assertEquals(1, overview.path("units").size());
        assertFalse(overview.path("units").get(0).path("initialized").asBoolean());
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        assertFalse(playback.path("runId").asText().isBlank());
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_plan_snapshot"));
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_run"));
        assertEquals(
                0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
        String stationToken = stationSession(lesson.person());
        JsonNode employeeSession =
                data(
                        rawTraining(
                                "GET", "/api/common/session", null, stationToken, null, "station"));
        assertTrue(employeeSession.path("availableRoles").toString().contains("STATION"));
        assertFalse(employeeSession.path("availableRoles").toString().contains("DRIVER"));
        JsonNode employeePlayback =
                data(
                        rawTraining(
                                "POST",
                                playbackPath(lesson.course()),
                                startBody(lesson.person(), lesson.course()),
                                stationToken,
                                key(),
                                "station"));
        assertEquals(playback.path("runId").asText(), employeePlayback.path("runId").asText());
    }

    @Test
    void noApplicationReturnsNullAndReadonlyQueriesNeverCreateTrainingFacts() {
        String owner = newMini(true);
        JsonNode noApplication = data(miniCall("GET", TRAINING, null, owner));
        assertTrue(noApplication.isNull());
        Student person = student(COMPANY_A, false);
        JsonNode missing = trainingView(person);
        assertEquals(0, missing.path("units").size());
        assertTrue(missing.path("blockers").size() > 0);
        assertTrue(missing.path("planId").isNull());
        assertTrue(missing.path("runId").isNull());
        assertTrainingRows(0, 0, 0, 0);
    }

    @Test
    void unverifiedPhoneAndAdminRealmCannotBecomeTheMiniStudent() {
        String unverified = newMini(false);
        requireError(miniCall("GET", TRAINING, null, unverified), 422, "VI_PHONE_PROOF_REQUIRED");
        for (String token : List.of("", rootAdmin)) {
            requireStatus(miniCall("GET", TRAINING, null, token), 401);
            requireStatus(miniCall("POST", TRAINING + "/1/playback", Map.of(), token), 401);
            requireStatus(miniCall("POST", TRAINING + "/submit", Map.of(), token), 401);
        }
        assertTrainingRows(0, 0, 0, 0);
    }

    @Test
    void unassignedOrUnavailableCompanyCannotBeGuessedFromAccountOrStation() {
        String token = newMini(true);
        JsonNode app = data(miniCall("POST", QUALIFICATION, consentBody(), token));
        JsonNode view = data(miniCall("GET", TRAINING, null, token));
        assertTrue(view.path("ownerDeptId").isNull());
        assertEquals(0, view.path("units").size());
        assertTrue(view.path("blockers").toString().contains("COMPANY"));
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_training_plan_snapshot WHERE application_id=?",
                        app.path("applicationId").asLong()));
        Lesson lesson = lesson(1, 80, 3);
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_A);
        JsonNode blocked = trainingView(lesson.person());
        assertFalse(blocked.path("allowedActions").toString().contains("START_TRAINING"));
        assertTrue(blocked.path("blockers").size() > 0);
    }

    @Test
    void companyPolicyGetIsPureReadAndUnconfiguredPolicyHasNoHiddenDefaults() {
        JsonNode first = policy(COMPANY_A);
        JsonNode second = policy(COMPANY_A);
        assertTrue(first.path("policyId").isNull());
        assertTrue(second.path("activeRevision").isNull());
        assertTrue(first.path("draftRevision").isNull());
        assertEquals(0, first.path("version").asLong());
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_policy"));
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_policy_revision"));
    }

    @Test
    void companyPolicyRequiresIndependentFunctionsAndRealCompanyScope() {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        Map<String, Object> draft =
                policyDraftBody(COMPANY_A, List.of(course.path("trainingId").asText()), 3, 0);
        String noQuery = scoped(COMPANY_A, POLICY_EDIT, POLICY_PUBLISH, COURSE_QUERY);
        String noEdit = scoped(COMPANY_A, POLICY_QUERY, POLICY_PUBLISH, COURSE_QUERY);
        requireError(call("GET", POLICIES + "/" + COMPANY_A, null, noQuery), 403, "VI_FORBIDDEN");
        requireError(call("PUT", POLICIES + "/" + COMPANY_A, draft, noEdit), 403, "VI_FORBIDDEN");
        JsonNode saved = data(call("PUT", POLICIES + "/" + COMPANY_A, draft, rootAdmin));
        String noPublish = scoped(COMPANY_A, POLICY_QUERY, POLICY_EDIT, COURSE_QUERY);
        requireError(
                call(
                        "POST",
                        POLICIES + "/" + COMPANY_A + "/publish",
                        policyPublishBody(saved),
                        noPublish),
                403,
                "VI_FORBIDDEN");
        for (String method : List.of("GET", "PUT", "POST")) {
            String foreign =
                    scoped(COMPANY_B, POLICY_QUERY, POLICY_EDIT, POLICY_PUBLISH, COURSE_QUERY);
            requireStatus(
                    call(
                            method,
                            POLICIES + "/" + COMPANY_A + (method.equals("POST") ? "/publish" : ""),
                            method.equals("GET")
                                    ? null
                                    : method.equals("PUT") ? draft : policyPublishBody(saved),
                            foreign),
                    404);
        }
        String empty =
                scopedWithScope(
                        COMPANY_A, "2", POLICY_QUERY, POLICY_EDIT, POLICY_PUBLISH, COURSE_QUERY);
        requireStatus(call("GET", POLICIES + "/" + COMPANY_A, null, empty), 404);
        requireStatus(call("PUT", POLICIES + "/" + COMPANY_A, draft, empty), 404);
    }

    @Test
    void incompletePolicyDraftCanBeSavedButCannotPublishOrEnableAnyLearning() {
        Student person = student(COMPANY_A, false);
        JsonNode saved =
                data(
                        call(
                                "PUT",
                                POLICIES + "/" + COMPANY_A,
                                policyDraftBody(COMPANY_A, List.of(), null, null),
                                rootAdmin));
        JsonNode revision = saved.path("draftRevision");
        assertTrue(revision.path("maxAttempts").isNull());
        assertTrue(revision.path("retryIntervalSeconds").isNull());
        assertTrue(revision.path("publishBlockers").size() >= 3);
        requireError(
                call(
                        "POST",
                        POLICIES + "/" + COMPANY_A + "/publish",
                        policyPublishBody(saved),
                        rootAdmin),
                422,
                "VI_DATA_INVALID");
        assertTrue(trainingView(person).path("policyRevisionId").isNull());
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_run"));
    }

    @Test
    void invalidPolicyCourseSelectionIsRejectedAtomicallyIncludingMixedForeignOrDraftRows() {
        JsonNode a = publishCourse(COMPANY_A, 1, 1, 80);
        JsonNode b = publishCourse(COMPANY_B, 1, 1, 80);
        JsonNode draft =
                data(
                        call(
                                "POST",
                                COURSES,
                                Map.of(
                                        "ownerDeptId",
                                        Long.toString(COMPANY_A),
                                        "title",
                                        "不能列为正式必修"),
                                rootAdmin));
        for (String invalid :
                List.of(
                        b.path("trainingId").asText(),
                        draft.path("trainingId").asText(),
                        Long.toString(Long.MAX_VALUE))) {
            requireStatus(
                    call(
                            "PUT",
                            POLICIES + "/" + COMPANY_A,
                            policyDraftBody(
                                    COMPANY_A,
                                    List.of(a.path("trainingId").asText(), invalid),
                                    3,
                                    0),
                            rootAdmin),
                    invalid.equals(draft.path("trainingId").asText()) ? 422 : 404);
            assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_policy_revision"));
        }
        JsonNode v2 = reviseCourse(a, "B");
        requireStatus(
                call(
                        "PUT",
                        POLICIES + "/" + COMPANY_A,
                        policyDraftBody(
                                COMPANY_A,
                                List.of(
                                        a.path("trainingId").asText(),
                                        v2.path("trainingId").asText()),
                                3,
                                0),
                        rootAdmin),
                422);
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_policy"));
    }

    @Test
    void policyRevisionIsAppendOnlyAndPublishingRequiresItsExactDraftAndCas() {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        JsonNode first =
                data(
                        call(
                                "PUT",
                                POLICIES + "/" + COMPANY_A,
                                policyDraftBody(
                                        COMPANY_A,
                                        List.of(course.path("trainingId").asText()),
                                        2,
                                        0),
                                rootAdmin));
        String original =
                policyRevisionFacts(first.path("draftRevision").path("revisionId").asLong());
        JsonNode second =
                data(
                        call(
                                "PUT",
                                POLICIES + "/" + COMPANY_A,
                                policyDraftBody(
                                        COMPANY_A,
                                        List.of(course.path("trainingId").asText()),
                                        3,
                                        10),
                                rootAdmin));
        assertEquals(
                original,
                policyRevisionFacts(first.path("draftRevision").path("revisionId").asLong()));
        requireError(
                call(
                        "POST",
                        POLICIES + "/" + COMPANY_A + "/publish",
                        policyPublishBody(first),
                        rootAdmin),
                409,
                "VI_VERSION_CONFLICT");
        Map<String, Object> oldTarget = new LinkedHashMap<>(policyPublishBody(second));
        oldTarget.put("draftRevisionId", first.path("draftRevision").path("revisionId").asText());
        requireStatus(
                call("POST", POLICIES + "/" + COMPANY_A + "/publish", oldTarget, rootAdmin), 409);
        JsonNode active =
                data(
                        call(
                                "POST",
                                POLICIES + "/" + COMPANY_A + "/publish",
                                policyPublishBody(second),
                                rootAdmin));
        assertEquals(
                second.path("draftRevision").path("revisionId").asText(),
                active.path("activeRevision").path("revisionId").asText());
        assertEquals(2, rows("SELECT COUNT(*) FROM vi_training_policy_revision"));
    }

    @Test
    void malformedPolicyIdsFieldsNumbersUuidAndInternalScopesAreRejected() {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        Map<String, Object> valid =
                policyDraftBody(COMPANY_A, List.of(course.path("trainingId").asText()), 3, 0);
        for (String field : valid.keySet()) {
            Map<String, Object> body = new LinkedHashMap<>(valid);
            body.remove(field);
            requireStatus(call("PUT", POLICIES + "/" + COMPANY_A, body, rootAdmin), 400);
        }
        for (String field :
                List.of(
                        "params",
                        "ownerDeptId",
                        "activeRevisionId",
                        "gradingMode",
                        "contentHash",
                        "publishedByUserId")) {
            Map<String, Object> body = new LinkedHashMap<>(valid);
            body.put(field, "forged");
            requireStatus(call("PUT", POLICIES + "/" + COMPANY_A, body, rootAdmin), 400);
        }
        requireStatus(callKey("PUT", POLICIES + "/" + COMPANY_A, valid, rootAdmin, null), 400);
        requireStatus(
                callKey("PUT", POLICIES + "/" + COMPANY_A, valid, rootAdmin, "not-uuid"), 400);
        requireStatus(call("GET", POLICIES + "/01", null, rootAdmin), 400);
        requireStatus(
                call("GET", POLICIES + "/" + COMPANY_A + "?params.dataScope=1", null, rootAdmin),
                400);
        for (Map.Entry<String, Object> invalid :
                Map.<String, Object>of("maxAttempts", 101, "retryIntervalSeconds", 2592001)
                        .entrySet()) {
            Map<String, Object> body = new LinkedHashMap<>(valid);
            body.put(invalid.getKey(), invalid.getValue());
            requireError(
                    call("PUT", POLICIES + "/" + COMPANY_A, body, rootAdmin),
                    422,
                    "VI_DATA_INVALID");
        }
    }

    @Test
    void firstRealPlaybackFreezesOnePlanAndRunWhileAllEarlierReadsStayPure() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        for (int i = 0; i < 3; i++) {
            trainingView(lesson.person());
            trainingUnit(lesson.person(), lesson.course());
        }
        assertTrainingRows(0, 0, 0, 0);
        JsonNode first = startPlayback(lesson.person(), lesson.course());
        JsonNode after = trainingView(lesson.person());
        assertEquals(first.path("planId").asText(), after.path("planId").asText());
        assertEquals(first.path("runId").asText(), after.path("runId").asText());
        assertTrainingRows(1, 1, 1, 0);
        JsonNode again = startPlayback(lesson.person(), lesson.course());
        assertEquals(first.path("planId").asText(), again.path("planId").asText());
        assertEquals(first.path("runId").asText(), again.path("runId").asText());
    }

    @Test
    void laterCourseAndPolicyPublicationDoNotReplaceAnAlreadyFrozenCurriculum() {
        Lesson lesson = lesson(1, 80, 3);
        startPlayback(lesson.person(), lesson.course());
        JsonNode frozen = trainingView(lesson.person());
        JsonNode v2 = reviseCourse(lesson.course(), "B");
        publishPolicy(COMPANY_A, List.of(v2), 4, 60);
        JsonNode current = trainingView(lesson.person());
        assertEquals(frozen.path("planId").asText(), current.path("planId").asText());
        assertEquals(
                frozen.path("policyRevisionId").asText(),
                current.path("policyRevisionId").asText());
        assertEquals(
                lesson.course().path("trainingId").asText(),
                current.path("units").get(0).path("trainingId").asText());
        assertEquals(3, current.path("units").get(0).path("maxAttempts").asInt());
        requireStatus(
                miniCall(
                        "GET",
                        TRAINING + "/" + v2.path("trainingId").asText(),
                        null,
                        lesson.person().token()),
                404);
    }

    @Test
    void materialChangeCreatesNewRunUsingTheSameFrozenPlanAndNoOldWatchCredit() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        passCourse(lesson.person(), lesson.course());
        JsonNode old = trainingView(lesson.person());
        JsonNode v2 = reviseCourse(lesson.course(), "B");
        publishPolicy(COMPANY_A, List.of(v2), 5, 10);
        saveMaterials(lesson.person(), Map.of("realName", "真实改版后的申请人"));
        JsonNode reset = trainingView(lesson.person());
        assertEquals(old.path("planId").asText(), reset.path("planId").asText());
        assertTrue(reset.path("runId").isNull());
        assertFalse(reset.path("complete").asBoolean());
        assertEquals(
                lesson.course().path("trainingId").asText(),
                reset.path("units").get(0).path("trainingId").asText());
        assertEquals(0, reset.path("units").get(0).path("verifiedDurationMillis").asLong());
        JsonNode next = startPlayback(lesson.person(), lesson.course());
        assertNotEquals(old.path("runId").asText(), next.path("runId").asText());
        assertEquals(old.path("planId").asText(), next.path("planId").asText());
        assertEquals(2, rows("SELECT COUNT(*) FROM vi_training_run"));
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_run WHERE status='PASSED'"));
    }

    @Test
    void companyRoundTripUsesNewEpochAndCannotReviveOldRunOrProof() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode passed = passCourse(lesson.person(), lesson.course());
        JsonNode original = trainingView(lesson.person());
        long epoch = companyEpoch(lesson.person());
        assignCompany(lesson.person(), COMPANY_B);
        assignCompany(lesson.person(), COMPANY_A);
        assertEquals(epoch + 2, companyEpoch(lesson.person()));
        JsonNode returned = trainingView(lesson.person());
        assertFalse(returned.path("complete").asBoolean());
        assertTrue(returned.path("planId").isNull());
        assertTrue(returned.path("runId").isNull());
        requireStatus(
                miniCall(
                        "GET",
                        TRAINING + "/attempts/" + passed.path("attemptId").asText(),
                        null,
                        lesson.person().token()),
                409);
        JsonNode newPlayback = startPlayback(lesson.person(), lesson.course());
        assertNotEquals(original.path("planId").asText(), newPlayback.path("planId").asText());
        assertNotEquals(original.path("runId").asText(), newPlayback.path("runId").asText());
        assertTrue(
                qualification(lesson.person()).path("checks").toString().contains("UNAVAILABLE"));
    }

    @Test
    void ordinaryRemarkAndAssignmentPauseDoNotDiscardTrainingContext() {
        Lesson lesson = lesson(1, 80, 3);
        startPlayback(lesson.person(), lesson.course());
        JsonNode before = trainingView(lesson.person());
        long epoch = companyEpoch(lesson.person());
        data(
                call(
                        "PATCH",
                        "/api/admin/drivers/" + lesson.person().driverId(),
                        Map.of(
                                "expectedVersion",
                                profileVersion(lesson.person()),
                                "internalRemark",
                                "行政备注不改公司",
                                "reason",
                                "仅更新备注"),
                        rootAdmin));
        data(
                call(
                        "POST",
                        "/api/admin/drivers/"
                                + lesson.person().driverId()
                                + "/disable-new-assignments",
                        Map.of(
                                "expectedVersion",
                                profileVersion(lesson.person()),
                                "reason",
                                "暂停新增承接不影响本人学习"),
                        rootAdmin));
        assertEquals(epoch, companyEpoch(lesson.person()));
        JsonNode after = trainingView(lesson.person());
        assertEquals(before.path("planId").asText(), after.path("planId").asText());
        assertEquals(before.path("runId").asText(), after.path("runId").asText());
    }

    @Test
    void playbackWritesPinApplicationRunMaterialCompanyAndPolicyInsteadOfFollowingCurrentState() {
        Lesson lesson = lesson(1, 80, 3);
        Map<String, Object> original = startBody(lesson.person(), lesson.course());
        saveMaterials(lesson.person(), Map.of("realName", "新材料不能继承旧播放请求"));
        requireStatus(
                miniCall("POST", playbackPath(lesson.course()), original, lesson.person().token()),
                409);
        Map<String, Object> fresh = startBody(lesson.person(), lesson.course());
        assignCompany(lesson.person(), COMPANY_B);
        assignCompany(lesson.person(), COMPANY_A);
        requireStatus(
                miniCall("POST", playbackPath(lesson.course()), fresh, lesson.person().token()),
                409);
        assertTrainingRows(0, 0, 0, 0);
    }

    @Test
    void unknownClientAuthorityAndResultFieldsCannotForgeAnyTrainingTransition() {
        Lesson lesson = lesson(1, 80, 3);
        Map<String, Object> start = startBody(lesson.person(), lesson.course());
        for (String name :
                List.of(
                        "ownerDeptId",
                        "score",
                        "passed",
                        "verifiedDurationMillis",
                        "materialHash",
                        "providerResultId",
                        "params",
                        "visibleDeptIds",
                        "gradingSnapshot")) {
            Map<String, Object> forged = new LinkedHashMap<>(start);
            forged.put(name, "PASSED");
            requireStatus(
                    miniCall(
                            "POST", playbackPath(lesson.course()), forged, lesson.person().token()),
                    400);
        }
        requireStatus(
                miniCallKey(
                        "POST",
                        playbackPath(lesson.course()),
                        start,
                        lesson.person().token(),
                        null),
                400);
        requireStatus(
                miniCallKey(
                        "POST",
                        playbackPath(lesson.course()),
                        start,
                        lesson.person().token(),
                        "not-uuid"),
                400);
        requireStatus(
                miniCall(
                        "GET",
                        TRAINING + "?ownerDeptId=" + COMPANY_B,
                        null,
                        lesson.person().token()),
                400);
        requireStatus(miniCall("GET", TRAINING + "/01", null, lesson.person().token()), 400);
        assertTrainingRows(0, 0, 0, 0);
        for (String field :
                List.of(
                        "expectedApplicationVersion",
                        "expectedCompanyRevision",
                        "expectedMaterialRevision",
                        "expectedVersion")) {
            Map<String, Object> overflow = new LinkedHashMap<>(start);
            overflow.put(field, 9007199254740992L);
            requireError(
                    miniCall(
                            "POST",
                            playbackPath(lesson.course()),
                            overflow,
                            lesson.person().token()),
                    400,
                    "VI_REQUEST_INVALID");
        }
    }

    @Test
    void firstHeartbeatOnlyEstablishesBaselineAndCannotClaimWholeVideo() {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        JsonNode unit = heartbeat(lesson.person(), lesson.course(), playback, 1, 4000, true);
        assertEquals(0, unit.path("verifiedDurationMillis").asLong());
        requireError(
                miniCall(
                        "POST",
                        attemptPath(lesson.course()),
                        attemptBody(lesson.person(), lesson.course()),
                        lesson.person().token()),
                422,
                "VI_TRAINING_NOT_READY");
    }

    @Test
    void actualWallClockAndValidVideoMovementEarnOnlyTheElapsedUniqueDuration() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        long before = System.nanoTime();
        JsonNode unit = watchCourse(lesson.person(), lesson.course());
        long wall = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - before);
        assertEquals(1000, unit.path("verifiedDurationMillis").asLong());
        assertEquals(1, unit.path("verifiedDurationSeconds").asInt());
        assertTrue(unit.path("verifiedDurationMillis").asLong() <= wall);
        assertEquals("READY_FOR_EXAM", unit.path("status").asText());
    }

    @Test
    void immediateSeekWithinActualMediaRangeDoesNotEarnWatchTime() {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        heartbeat(lesson.person(), lesson.course(), playback, 1, 0, true);
        JsonNode fastForward = heartbeat(lesson.person(), lesson.course(), playback, 2, 5000, true);
        assertEquals(0, fastForward.path("verifiedDurationMillis").asLong());
        assertNotEquals("READY_FOR_EXAM", fastForward.path("status").asText());
        assertEquals(
                0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
    }

    @Test
    void repeatedAndRewoundMediaIntervalsCannotEarnTheSameSecondTwice() throws Exception {
        JsonNode course = publishCourse(COMPANY_A, 1, 2, 80);
        publishPolicy(COMPANY_A, List.of(course), 3, 0);
        Student person = student(COMPANY_A, false);
        JsonNode playback = startPlayback(person, course);
        heartbeat(person, course, playback, 1, 0, true);
        Thread.sleep(1150);
        JsonNode first = heartbeat(person, course, playback, 2, 1000, true);
        assertEquals(1000, first.path("verifiedDurationMillis").asLong());
        heartbeat(person, course, playback, 3, 0, true);
        Thread.sleep(1150);
        JsonNode replayed = heartbeat(person, course, playback, 4, 1000, true);
        assertEquals(1000, replayed.path("verifiedDurationMillis").asLong());
        assertNotEquals("READY_FOR_EXAM", replayed.path("status").asText());
    }

    @Test
    void subsecondHeartbeatsPreserveMillisecondsInsteadOfLosingEveryFraction() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        heartbeat(lesson.person(), lesson.course(), playback, 1, 0, true);
        JsonNode unit = null;
        for (int i = 1; i <= 5; i++) {
            Thread.sleep(230);
            unit = heartbeat(lesson.person(), lesson.course(), playback, i + 1, i * 200L, true);
        }
        assertNotNull(unit);
        assertEquals(1000, unit.path("verifiedDurationMillis").asLong());
        assertEquals(1, unit.path("verifiedDurationSeconds").asInt());
    }

    @Test
    void pausedAndResumeBaselineHeartbeatsCannotClaimThePausedGap() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        heartbeat(lesson.person(), lesson.course(), playback, 1, 0, true);
        Thread.sleep(1150);
        JsonNode paused = heartbeat(lesson.person(), lesson.course(), playback, 2, 1000, false);
        assertEquals(0, paused.path("verifiedDurationMillis").asLong());
        Thread.sleep(1150);
        JsonNode resumed = heartbeat(lesson.person(), lesson.course(), playback, 3, 2000, true);
        assertEquals(0, resumed.path("verifiedDurationMillis").asLong());
        // 只老化上次报告边界，不制造学时；离线超过15秒后第一个报告仅重建基线。
        jdbc.update(
                "UPDATE vi_training_playback SET"
                    + " last_reported_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 16 SECOND) WHERE id=?",
                playback.path("playbackSessionId").asLong());
        JsonNode offline = heartbeat(lesson.person(), lesson.course(), playback, 4, 3000, true);
        assertEquals(0, offline.path("verifiedDurationMillis").asLong());
        Thread.sleep(1150);
        JsonNode fresh = heartbeat(lesson.person(), lesson.course(), playback, 5, 4000, true);
        assertEquals(1000, fresh.path("verifiedDurationMillis").asLong());
    }

    @Test
    void expiredPlaybackAndOldSequenceCannotIncreasePersistedDuration() {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        heartbeat(lesson.person(), lesson.course(), playback, 1, 0, true);
        Map<String, Object> duplicate =
                progressBody(lesson.person(), lesson.course(), playback, 1, 1000, true);
        requireStatus(
                miniCall("POST", progressPath(lesson.course()), duplicate, lesson.person().token()),
                409);
        jdbc.update(
                "UPDATE vi_training_playback SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE id=?",
                playback.path("playbackSessionId").asLong());
        requireStatus(
                miniCall(
                        "POST",
                        progressPath(lesson.course()),
                        progressBody(lesson.person(), lesson.course(), playback, 2, 1000, true),
                        lesson.person().token()),
                409);
        assertEquals(0, duration(lesson.person(), lesson.course()));
    }

    @Test
    void learnerMediaUsesRealBytesAndCurrentFrozenMembershipWithoutOpeningAdminFiles() {
        Lesson lesson = lesson(1, 80, 3);
        startPlayback(lesson.person(), lesson.course());
        JsonNode access = learnerAccess(lesson.person(), lesson.course());
        ResponseEntity<byte[]> bytes = readMedia(lesson.person().token(), access);
        requireStatus(bytes, 200);
        assertArrayEquals(mp4, bytes.getBody());
        assertEquals(
                "video/mp4",
                Objects.requireNonNull(bytes.getHeaders().getContentType()).toString());
        Student colleague = student(COMPANY_A, false);
        startPlayback(colleague, lesson.course());
        assertArrayEquals(
                mp4,
                readMedia(colleague.token(), learnerAccess(colleague, lesson.course())).getBody());
        JsonNode notRequired = publishCourse(COMPANY_A, 1, 1, 80);
        requireStatus(
                miniCall(
                        "POST",
                        "/api/common/files/" + notRequired.path("mediaFileId").asText() + "/access",
                        Map.of("purpose", "不能借公司范围读取未列入本人课表的媒体"),
                        lesson.person().token()),
                404);
        Map<String, Object> forbiddenUpload =
                Map.of(
                        "purpose",
                        "TRAINING_MEDIA",
                        "trainingId",
                        lesson.course().path("trainingId").asText(),
                        "fileName",
                        "attack.mp4",
                        "mimeType",
                        "video/mp4",
                        "size",
                        mp4.length);
        requireStatus(
                miniCall("POST", "/api/common/uploads", forbiddenUpload, lesson.person().token()),
                404);
        assignCompany(lesson.person(), COMPANY_B);
        assignCompany(lesson.person(), COMPANY_A);
        requireStatus(readMedia(lesson.person().token(), access), 404);
        startPlayback(lesson.person(), lesson.course());
        // 同一原件在新epoch/run再次合法可读，旧短票仍因MAC上下文不同而失效，不能复活。
        requireStatus(readMedia(lesson.person().token(), access), 403);
        requireStatus(
                readMedia(lesson.person().token(), learnerAccess(lesson.person(), lesson.course())),
                200);
    }

    @Test
    void otherStudentsApplicationPlaybackAndAttemptIdsCannotBeBorrowed() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        Student other = student(COMPANY_A, false);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        JsonNode otherPlayback = startPlayback(other, lesson.course());
        Map<String, Object> forged =
                new LinkedHashMap<>(startBody(lesson.person(), lesson.course()));
        forged.put("applicationId", Long.toString(other.applicationId()));
        requireStatus(
                miniCall("POST", playbackPath(lesson.course()), forged, lesson.person().token()),
                404);
        Map<String, Object> heartbeat =
                new LinkedHashMap<>(
                        progressBody(lesson.person(), lesson.course(), playback, 1, 0, true));
        heartbeat.put("playbackSessionId", otherPlayback.path("playbackSessionId").asText());
        requireStatus(
                miniCall("POST", progressPath(lesson.course()), heartbeat, lesson.person().token()),
                404);
        watchCourse(other, lesson.course());
        JsonNode otherAttempt = beginAttempt(other, lesson.course());
        requireStatus(
                miniCall(
                        "GET",
                        TRAINING + "/attempts/" + otherAttempt.path("attemptId").asText(),
                        null,
                        lesson.person().token()),
                404);
        requireStatus(
                miniCall(
                        "POST",
                        TRAINING + "/submit",
                        answerBody(other, otherAttempt, 1),
                        lesson.person().token()),
                404);
        assertEquals(0, duration(lesson.person(), lesson.course()));
    }

    @Test
    void latestPlaybackAndRealMiniSessionContextPreventParallelDeviceCredit() {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode first = startPlayback(lesson.person(), lesson.course());
        Student device =
                new Student(
                        anotherSession(lesson.person().accountId()),
                        lesson.person().accountId(),
                        lesson.person().driverId(),
                        lesson.person().applicationId());
        JsonNode second = startPlayback(device, lesson.course());
        requireStatus(
                miniCall(
                        "POST",
                        progressPath(lesson.course()),
                        progressBody(lesson.person(), lesson.course(), first, 1, 0, true),
                        lesson.person().token()),
                409);
        Map<String, Object> body = progressBody(device, lesson.course(), second, 1, 0, true);
        requireStatus(
                miniCall("POST", progressPath(lesson.course()), body, lesson.person().token()),
                404);
        long session = miniTokens.resolveAccess(device.token()).sessionId();
        jdbc.update("UPDATE vi_session SET context_version=context_version+1 WHERE id=?", session);
        requireStatus(miniCall("POST", progressPath(lesson.course()), body, device.token()), 401);
        assertEquals(0, duration(lesson.person(), lesson.course()));
    }

    @Test
    void heartbeatIdempotencyAndSequenceReplayDoNotDoubleCountAndDifferentBodyConflicts()
            throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        heartbeat(lesson.person(), lesson.course(), playback, 1, 0, true);
        Thread.sleep(1150);
        Map<String, Object> body =
                progressBody(lesson.person(), lesson.course(), playback, 2, 1000, true);
        String key = key();
        JsonNode first =
                data(
                        miniCallKey(
                                "POST",
                                progressPath(lesson.course()),
                                body,
                                lesson.person().token(),
                                key));
        JsonNode replay =
                data(
                        miniCallKey(
                                "POST",
                                progressPath(lesson.course()),
                                body,
                                lesson.person().token(),
                                key));
        assertEquals(first.path("version").asLong(), replay.path("version").asLong());
        assertEquals(1000, duration(lesson.person(), lesson.course()));
        Map<String, Object> changed = new LinkedHashMap<>(body);
        changed.put("positionMillis", 2000);
        requireError(
                miniCallKey(
                        "POST",
                        progressPath(lesson.course()),
                        changed,
                        lesson.person().token(),
                        key),
                409,
                "VI_IDEMPOTENCY_CONFLICT");
        requireStatus(
                miniCall("POST", progressPath(lesson.course()), body, lesson.person().token()),
                409);
        assertEquals(1000, duration(lesson.person(), lesson.course()));
    }

    @Test
    void successfulPlaybackKeyCannotDriftToTheNewMaterialRun() {
        Lesson lesson = lesson(1, 80, 3);
        Map<String, Object> body = startBody(lesson.person(), lesson.course());
        String key = key();
        JsonNode first =
                data(
                        miniCallKey(
                                "POST",
                                playbackPath(lesson.course()),
                                body,
                                lesson.person().token(),
                                key));
        saveMaterials(lesson.person(), Map.of("realName", "不能将旧成功意图移到这次材料"));
        requireStatus(
                miniCallKey(
                        "POST", playbackPath(lesson.course()), body, lesson.person().token(), key),
                409);
        JsonNode next = startPlayback(lesson.person(), lesson.course());
        assertNotEquals(first.path("runId").asText(), next.path("runId").asText());
        assertEquals(2, rows("SELECT COUNT(*) FROM vi_training_run"));
    }

    @Test
    void wrongVersionsAndPositionsOutsideTheVerifiedMediaAreRejectedWithoutCredit() {
        Lesson lesson = lesson(1, 80, 3);
        JsonNode playback = startPlayback(lesson.person(), lesson.course());
        for (Map.Entry<String, Object> invalid :
                Map.<String, Object>of(
                                "courseVersion",
                                2,
                                "expectedVersion",
                                999,
                                "positionMillis",
                                90000000)
                        .entrySet()) {
            Map<String, Object> body =
                    new LinkedHashMap<>(
                            progressBody(lesson.person(), lesson.course(), playback, 1, 0, true));
            body.put(invalid.getKey(), invalid.getValue());
            requireError(
                    miniCall("POST", progressPath(lesson.course()), body, lesson.person().token()),
                    invalid.getKey().equals("positionMillis") ? 422 : 409,
                    invalid.getKey().equals("positionMillis")
                            ? "VI_DATA_INVALID"
                            : "VI_VERSION_CONFLICT");
        }
        requireStatus(
                miniCall(
                        "POST",
                        progressPath(lesson.course()),
                        progressBody(lesson.person(), lesson.course(), playback, 1, 6000, true),
                        lesson.person().token()),
                422);
        assertEquals(0, duration(lesson.person(), lesson.course()));
    }

    @Test
    void concurrentFirstPlaybackRequestsCannotCreateMultiplePlansOrAddParallelWatchTime()
            throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        Map<String, Object> initial = startBody(lesson.person(), lesson.course());
        List<ResponseEntity<String>> responses =
                race(
                        () ->
                                miniCall(
                                        "POST",
                                        playbackPath(lesson.course()),
                                        initial,
                                        lesson.person().token()),
                        () ->
                                miniCall(
                                        "POST",
                                        playbackPath(lesson.course()),
                                        initial,
                                        lesson.person().token()));
        assertEquals(1, responses.stream().filter(r -> r.getStatusCode().value() == 200).count());
        assertEquals(1, responses.stream().filter(r -> r.getStatusCode().value() == 409).count());
        assertTrainingRows(1, 1, 1, 0);
        assertEquals(0, duration(lesson.person(), lesson.course()));
    }

    @Test
    void examsRequireActualStudyAndConcurrentCreationReturnsTheSameOpenAttemptOnlyOnce()
            throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        startPlayback(lesson.person(), lesson.course());
        requireError(
                miniCall(
                        "POST",
                        attemptPath(lesson.course()),
                        attemptBody(lesson.person(), lesson.course()),
                        lesson.person().token()),
                422,
                "VI_TRAINING_NOT_READY");
        watchCourse(lesson.person(), lesson.course());
        Map<String, Object> body = attemptBody(lesson.person(), lesson.course());
        List<ResponseEntity<String>> responses =
                race(
                        () ->
                                miniCall(
                                        "POST",
                                        attemptPath(lesson.course()),
                                        body,
                                        lesson.person().token()),
                        () ->
                                miniCall(
                                        "POST",
                                        attemptPath(lesson.course()),
                                        body,
                                        lesson.person().token()));
        JsonNode first = data(responses.get(0));
        JsonNode second = data(responses.get(1));
        assertEquals(first.path("attemptId").asText(), second.path("attemptId").asText());
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_attempt"));
        assertEquals(
                1, trainingUnit(lesson.person(), lesson.course()).path("attemptsUsed").asInt());
    }

    @Test
    void halfUpFinalIntegerIsUsedBeforeComparingTheConfiguredPassScore() throws Exception {
        Lesson lesson = lesson(8, 38, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode result =
                submitAnswers(lesson.person(), beginAttempt(lesson.person(), lesson.course()), 3);
        assertEquals(38, result.path("score").asInt());
        assertEquals("PASSED", result.path("status").asText());
        assertTrue(result.path("passed").asBoolean());
    }

    @Test
    void equalQuestionsAreRoundedOnceAtTheEndRatherThanPerQuestion() throws Exception {
        Lesson lesson = lesson(6, 51, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode result =
                submitAnswers(lesson.person(), beginAttempt(lesson.person(), lesson.course()), 3);
        assertEquals(50, result.path("score").asInt());
        assertEquals("FAILED", result.path("status").asText());
        assertFalse(result.path("passed").asBoolean());
        assertEquals(
                0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
    }

    @Test
    void multipleChoiceUsesExactSetsWithoutPartialCreditAndOrderDoesNotMatter() throws Exception {
        List<Map<String, Object>> bank = questionBank(3);
        bank.get(1).put("type", "MULTIPLE");
        bank.get(1).put("correctOptionCodes", List.of("A", "B"));
        bank.get(2).put("type", "TRUE_FALSE");
        bank.get(2)
                .put(
                        "options",
                        List.of(
                                Map.of("code", "A", "text", "对"),
                                Map.of("code", "B", "text", "错")));
        JsonNode course = publishCourse(COMPANY_A, bank, 1, 100);
        publishPolicy(COMPANY_A, List.of(course), 4, 0);
        Student person = student(COMPANY_A, false);
        watchCourse(person, course);
        for (List<String> selected :
                List.of(List.of("A"), List.of("A", "B", "C"), List.of("B", "A"))) {
            JsonNode attempt = beginAttempt(person, course);
            List<Map<String, Object>> answers = answers(attempt, List.of("A"));
            answers.get(1).put("answerCodes", selected);
            JsonNode result =
                    data(
                            miniCall(
                                    "POST",
                                    TRAINING + "/submit",
                                    submissionBody(person, attempt, answers),
                                    person.token()));
            assertEquals(selected.size() == 2 ? 100 : 67, result.path("score").asInt());
            assertEquals(
                    selected.size() == 2 ? "PASSED" : "FAILED", result.path("status").asText());
        }
        assertEquals(3, trainingUnit(person, course).path("attemptsUsed").asInt());
    }

    @Test
    void invalidOrDuplicateQuestionAndOptionInputsRejectTheEntireExamSubmission() throws Exception {
        Lesson lesson = lesson(2, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        for (int invalid = 0; invalid < 4; invalid++) {
            List<Map<String, Object>> answers = answers(attempt, List.of("A"));
            switch (invalid) {
                case 0 -> answers.get(1).put("questionId", answers.get(0).get("questionId"));
                case 1 -> answers.get(1).put("questionId", Long.toString(Long.MAX_VALUE));
                case 2 -> answers.get(0).put("answerCodes", List.of("Z"));
                default -> answers.get(0).put("answerCodes", List.of("A", "A"));
            }
            requireStatus(
                    miniCall(
                            "POST",
                            TRAINING + "/submit",
                            submissionBody(lesson.person(), attempt, answers),
                            lesson.person().token()),
                    422);
            assertNull(attemptField(attempt, "score", Integer.class));
            assertEquals("OPEN", attemptField(attempt, "status", String.class));
        }
    }

    @Test
    void anOpenV1AttemptIsReallyGradedAgainstV1AfterV2AndNewPolicyPublication() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        JsonNode v2 = reviseCourse(lesson.course(), "B");
        publishPolicy(COMPANY_A, List.of(v2), 3, 0);
        JsonNode result = submitAnswers(lesson.person(), attempt, 1);
        assertEquals(100, result.path("score").asInt());
        assertEquals("PASSED", result.path("status").asText());
        assertEquals(
                lesson.course().path("trainingId").asText(), result.path("trainingId").asText());
        assertEquals(1, result.path("courseVersion").asInt());
        assertEquals(
                lesson.course().path("trainingId").asText(),
                trainingView(lesson.person()).path("units").get(0).path("trainingId").asText());
    }

    @Test
    void finalSubmissionIsPermanentAcrossDifferentUuidsAndValidRootKeyRotation() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        Map<String, Object> body = answerBody(lesson.person(), attempt, 1);
        String key = key();
        JsonNode first =
                data(miniCallKey("POST", TRAINING + "/submit", body, lesson.person().token(), key));
        JsonNode retry =
                data(miniCallKey("POST", TRAINING + "/submit", body, lesson.person().token(), key));
        assertEquals(first.path("version").asLong(), retry.path("version").asLong());
        Map<String, String> old = new HashMap<>(integrationKeys.getVersions());
        String version = integrationKeys.getCurrentVersion();
        try {
            byte[] keyBytes = new byte[32];
            new java.security.SecureRandom().nextBytes(keyBytes);
            Map<String, String> rotated = new HashMap<>(old);
            rotated.put("q03-rotated", Base64.getEncoder().encodeToString(keyBytes));
            integrationKeys.setVersions(rotated);
            integrationKeys.setCurrentVersion("q03-rotated");
            JsonNode permanent =
                    data(miniCall("POST", TRAINING + "/submit", body, lesson.person().token()));
            assertEquals(first.path("version").asLong(), permanent.path("version").asLong());
            assertEquals(100, permanent.path("score").asInt());
        } finally {
            integrationKeys.setVersions(old);
            integrationKeys.setCurrentVersion(version);
        }
        Map<String, Object> changed = answerBody(lesson.person(), attempt, 0);
        requireError(
                miniCallKey("POST", TRAINING + "/submit", changed, lesson.person().token(), key),
                409,
                "VI_IDEMPOTENCY_CONFLICT");
        requireStatus(
                miniCall("POST", TRAINING + "/submit", changed, lesson.person().token()), 409);
        assertEquals(
                1,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
        assertEquals(100, attemptField(attempt, "score", Integer.class));
    }

    @Test
    void concurrentDifferentAnswerSubmissionsProduceOneGradeAndOneSummary() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        Map<String, Object> correct = answerBody(lesson.person(), attempt, 1);
        Map<String, Object> wrong = answerBody(lesson.person(), attempt, 0);
        List<ResponseEntity<String>> result =
                race(
                        () ->
                                miniCall(
                                        "POST",
                                        TRAINING + "/submit",
                                        correct,
                                        lesson.person().token()),
                        () ->
                                miniCall(
                                        "POST",
                                        TRAINING + "/submit",
                                        wrong,
                                        lesson.person().token()));
        assertEquals(
                List.of(200, 409),
                result.stream().map(r -> r.getStatusCode().value()).sorted().toList());
        int score = attemptField(attempt, "score", Integer.class);
        assertTrue(score == 0 || score == 100);
        assertEquals(
                attempt.path("version").asLong() + 1, attemptField(attempt, "version", Long.class));
        assertEquals(
                score == 100 ? 1 : 0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
    }

    @Test
    void expiredAttemptsRemainExpiredWithoutScoreAndStillConsumeTheConfiguredAttempt()
            throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        jdbc.update(
                "UPDATE vi_training_attempt SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE id=?",
                attempt.path("attemptId").asLong());
        JsonNode expired =
                data(
                        miniCall(
                                "GET",
                                TRAINING + "/attempts/" + attempt.path("attemptId").asText(),
                                null,
                                lesson.person().token()));
        assertEquals("EXPIRED", expired.path("status").asText());
        assertTrue(expired.path("score").isNull());
        assertTrue(expired.path("passed").isNull());
        requireStatus(
                miniCall(
                        "POST",
                        TRAINING + "/submit",
                        answerBody(lesson.person(), attempt, 1),
                        lesson.person().token()),
                409);
        assertEquals("EXPIRED", attemptField(attempt, "status", String.class));
        assertEquals(
                1, trainingUnit(lesson.person(), lesson.course()).path("attemptsUsed").asInt());
        JsonNode next = beginAttempt(lesson.person(), lesson.course());
        assertEquals(2, next.path("attemptNo").asInt());
        assertEquals(
                0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
    }

    @Test
    void failureAndTimeoutCountAcrossMaterialRunsWithoutResettingTheApplicationQuota()
            throws Exception {
        Lesson lesson = lesson(1, 80, 2);
        watchCourse(lesson.person(), lesson.course());
        JsonNode failed =
                submitAnswers(lesson.person(), beginAttempt(lesson.person(), lesson.course()), 0);
        assertEquals("FAILED", failed.path("status").asText());
        JsonNode second = beginAttempt(lesson.person(), lesson.course());
        jdbc.update(
                "UPDATE vi_training_attempt SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE id=?",
                second.path("attemptId").asLong());
        JsonNode expired =
                data(
                        miniCall(
                                "GET",
                                TRAINING + "/attempts/" + second.path("attemptId").asText(),
                                null,
                                lesson.person().token()));
        assertEquals("EXPIRED", expired.path("status").asText());
        requireError(
                miniCall(
                        "POST",
                        attemptPath(lesson.course()),
                        attemptBody(lesson.person(), lesson.course()),
                        lesson.person().token()),
                409,
                "VI_STATE_CONFLICT");
        saveMaterials(lesson.person(), Map.of("realName", "材料换版也不能重置考试次数"));
        watchCourse(lesson.person(), lesson.course());
        assertEquals(
                2, trainingUnit(lesson.person(), lesson.course()).path("attemptsUsed").asInt());
        requireError(
                miniCall(
                        "POST",
                        attemptPath(lesson.course()),
                        attemptBody(lesson.person(), lesson.course()),
                        lesson.person().token()),
                409,
                "VI_STATE_CONFLICT");
        assertEquals(
                2,
                rows(
                        "SELECT COUNT(*) FROM vi_training_attempt WHERE application_id=?",
                        lesson.person().applicationId()));
    }

    @Test
    void stableCourseKeyCountsOldVersionAttemptsAfterACompanyEpochRoundTrip() throws Exception {
        Lesson lesson = lesson(1, 80, 2);
        watchCourse(lesson.person(), lesson.course());
        submitAnswers(lesson.person(), beginAttempt(lesson.person(), lesson.course()), 0);
        JsonNode v2 = reviseCourse(lesson.course(), "B");
        publishPolicy(COMPANY_A, List.of(v2), 2, 0);
        assignCompany(lesson.person(), COMPANY_B);
        assignCompany(lesson.person(), COMPANY_A);
        JsonNode preview = trainingUnit(lesson.person(), v2);
        assertEquals(1, preview.path("attemptsUsed").asInt());
        watchCourse(lesson.person(), v2);
        JsonNode attempt = beginAttempt(lesson.person(), v2);
        assertEquals(2, attempt.path("attemptNo").asInt());
        JsonNode result = submitAnswers(lesson.person(), attempt, 0);
        assertEquals("PASSED", result.path("status").asText());
        assertEquals(100, result.path("score").asInt());
        assertEquals(
                2,
                rows(
                        "SELECT COUNT(*) FROM vi_training_attempt WHERE application_id=? AND"
                                + " course_key=?",
                        lesson.person().applicationId(),
                        lesson.course().path("courseKey").asText()));
    }

    @Test
    void retryIntervalStartsAtTheRealTerminalTimestampAndIsNotAHiddenDefault() throws Exception {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        publishPolicy(COMPANY_A, List.of(course), 3, 60);
        Student person = student(COMPANY_A, false);
        watchCourse(person, course);
        JsonNode failed = submitAnswers(person, beginAttempt(person, course), 0);
        JsonNode unit = trainingUnit(person, course);
        assertFalse(unit.path("retryAvailableAt").isNull());
        requireError(
                miniCall("POST", attemptPath(course), attemptBody(person, course), person.token()),
                409,
                "VI_STATE_CONFLICT");
        // 只重现已结束考试的历史时刻，不修改学习墙钟、成绩或TRAINING通过事实。
        jdbc.update(
                "UPDATE vi_training_attempt SET submitted_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 61"
                        + " SECOND) WHERE id=?",
                failed.path("attemptId").asLong());
        JsonNode next = beginAttempt(person, course);
        assertEquals(2, next.path("attemptNo").asInt());
    }

    @Test
    void allRealMandatoryCoursesProduceOneCanonicalProofAndQ01CanThenSubmit() throws Exception {
        JsonNode first = publishCourse(COMPANY_A, 1, 1, 80);
        JsonNode second = publishCourse(COMPANY_A, 2, 1, 80);
        publishPolicy(COMPANY_A, List.of(first, second), 3, 0);
        Student person = student(COMPANY_A, true);
        trustedOtherVerifications(person);
        passCourse(person, first);
        assertFalse(trainingView(person).path("complete").asBoolean());
        assertEquals(
                0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
        JsonNode last = passCourse(person, second);
        JsonNode completed = trainingView(person);
        assertTrue(completed.path("complete").asBoolean());
        assertEquals(
                1,
                rows(
                        "SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"
                                + " AND provider='INTERNAL_TRAINING' AND status='PASSED'"));
        assertEquals(
                1,
                rows(
                        "SELECT COUNT(*) FROM vi_training_run WHERE status='PASSED' AND"
                                + " provider_verification_id IS NOT NULL"));
        assertTrue(qualification(person).path("checks").toString().contains("TRAINING"));
        JsonNode submitted =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION + "/submit",
                                qualificationSubmitBody(person),
                                person.token()));
        assertEquals("SUBMITTED", submitted.path("status").asText());
        assertFalse(
                data(miniCall("GET", "/api/common/session", null, person.token()))
                        .path("availableRoles")
                        .toString()
                        .contains("DRIVER"));
        assertFalse(last.toString().contains("correctOptionCodes"));
        assertSafeArtifacts();
    }

    @Test
    void partialCoursesAndStandaloneLegacyPassedRowsCannotBypassQ01TrainingGate() throws Exception {
        JsonNode first = publishCourse(COMPANY_A, 1, 1, 80);
        JsonNode second = publishCourse(COMPANY_A, 1, 1, 80);
        publishPolicy(COMPANY_A, List.of(first, second), 3, 0);
        Student person = student(COMPANY_A, true);
        trustedOtherVerifications(person);
        passCourse(person, first);
        Map<String, Object> material =
                jdbc.queryForMap(
                        "SELECT material_revision,material_hash FROM vi_driver_application WHERE"
                                + " id=?",
                        person.applicationId());
        jdbc.update(
                "INSERT INTO"
                    + " vi_provider_verification(application_id,material_revision,material_hash,provider,task_type,provider_task_id,status,verified_at)"
                    + " VALUES (?,?,?,'INTERNAL_TRAINING','TRAINING',?,'PASSED',UTC_TIMESTAMP(3))",
                person.applicationId(),
                material.get("material_revision"),
                material.get("material_hash"),
                "q03-unlinked-" + key());
        assertFalse(trainingView(person).path("complete").asBoolean());
        requireError(
                miniCall(
                        "POST",
                        QUALIFICATION + "/submit",
                        qualificationSubmitBody(person),
                        person.token()),
                422,
                "VI_TRAINING_NOT_READY");
        assertEquals(
                "DRAFT",
                jdbc.queryForObject(
                        "SELECT status FROM vi_driver_application WHERE id=?",
                        String.class,
                        person.applicationId()));
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_driver_application_submission"));
    }

    @Test
    void correctlyCompletedFrozenPlanStillSatisfiesQ01AfterTheActivePolicyChanges()
            throws Exception {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        publishPolicy(COMPANY_A, List.of(course), 3, 0);
        Student person = student(COMPANY_A, true);
        trustedOtherVerifications(person);
        passCourse(person, course);
        String frozen = trainingView(person).path("policyRevisionId").asText();
        JsonNode v2 = reviseCourse(course, "B");
        JsonNode active = publishPolicy(COMPANY_A, List.of(v2), 2, 30);
        assertNotEquals(frozen, active.path("activeRevision").path("revisionId").asText());
        assertTrue(trainingView(person).path("complete").asBoolean());
        JsonNode submitted =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION + "/submit",
                                qualificationSubmitBody(person),
                                person.token()));
        assertEquals("SUBMITTED", submitted.path("status").asText());
    }

    @Test
    void aNewQualificationApplicationHasFreshCurriculumAndIndependentAttemptCounts()
            throws Exception {
        Lesson lesson = lesson(1, 80, 1);
        JsonNode oldAttempt = passCourse(lesson.person(), lesson.course());
        JsonNode old = trainingView(lesson.person());
        // Q05尚未实现，这里仅设置已审批历史边界以创建真实Q01复审，不把培训当资格审批。
        jdbc.update(
                "UPDATE vi_driver_application SET"
                        + " status='APPROVED',reviewer_user_id=1,reviewed_at=UTC_TIMESTAMP(3) WHERE"
                        + " id=?",
                lesson.person().applicationId());
        jdbc.update(
                "UPDATE vi_driver_profile SET"
                        + " qualification_status='APPROVED',qualification_revision=? WHERE id=?",
                lesson.person().applicationId(),
                lesson.person().driverId());
        JsonNode v2 = reviseCourse(lesson.course(), "B");
        publishPolicy(COMPANY_A, List.of(v2), 2, 0);
        JsonNode nextApp =
                data(miniCall("POST", QUALIFICATION, consentBody(), lesson.person().token()));
        Student next =
                new Student(
                        lesson.person().token(),
                        lesson.person().accountId(),
                        lesson.person().driverId(),
                        nextApp.path("applicationId").asLong());
        assertNotEquals(lesson.person().applicationId(), next.applicationId());
        JsonNode nextView = trainingView(next);
        assertFalse(nextView.path("complete").asBoolean());
        assertEquals(
                v2.path("trainingId").asText(),
                nextView.path("units").get(0).path("trainingId").asText());
        assertEquals(0, nextView.path("units").get(0).path("attemptsUsed").asInt());
        requireStatus(
                miniCall(
                        "GET",
                        TRAINING + "/attempts/" + oldAttempt.path("attemptId").asText(),
                        null,
                        next.token()),
                409);
        JsonNode started = startPlayback(next, v2);
        assertNotEquals(old.path("planId").asText(), started.path("planId").asText());
    }

    @Test
    void tenActualResponsesConformToBothMainAndTaskContractsWithoutAnyAnswersOrInternalSecrets()
            throws Exception {
        Path samples = Files.createTempDirectory("q03-contract-");
        Map<String, ResponseEntity<String>> responses = new LinkedHashMap<>();
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        responses.put("policy-get", call("GET", POLICIES + "/" + COMPANY_A, null, rootAdmin));
        responses.put(
                "policy-save",
                call(
                        "PUT",
                        POLICIES + "/" + COMPANY_A,
                        policyDraftBody(
                                COMPANY_A, List.of(course.path("trainingId").asText()), 3, 0),
                        rootAdmin));
        responses.put(
                "policy-publish",
                call(
                        "POST",
                        POLICIES + "/" + COMPANY_A + "/publish",
                        policyPublishBody(data(responses.get("policy-save"))),
                        rootAdmin));
        Student person = student(COMPANY_A, false);
        responses.put("list", miniCall("GET", TRAINING, null, person.token()));
        responses.put(
                "detail",
                miniCall(
                        "GET",
                        TRAINING + "/" + course.path("trainingId").asText(),
                        null,
                        person.token()));
        responses.put(
                "playback",
                miniCall("POST", playbackPath(course), startBody(person, course), person.token()));
        JsonNode playback = data(responses.get("playback"));
        heartbeat(person, course, playback, 1, 0, true);
        Thread.sleep(1150);
        responses.put(
                "progress",
                miniCall(
                        "POST",
                        progressPath(course),
                        progressBody(person, course, playback, 2, 1000, true),
                        person.token()));
        responses.put(
                "attempt-create",
                miniCall("POST", attemptPath(course), attemptBody(person, course), person.token()));
        JsonNode attempt = data(responses.get("attempt-create"));
        responses.put(
                "attempt-get",
                miniCall(
                        "GET",
                        TRAINING + "/attempts/" + attempt.path("attemptId").asText(),
                        null,
                        person.token()));
        responses.put(
                "submit",
                miniCall(
                        "POST",
                        TRAINING + "/submit",
                        answerBody(person, attempt, 1),
                        person.token()));
        for (var entry : responses.entrySet()) {
            requireStatus(entry.getValue(), 200);
            assertTrue(
                    Objects.requireNonNull(entry.getValue().getHeaders().getCacheControl())
                            .contains("no-store"));
            for (String secret :
                    List.of(
                            "correctOptionCodes",
                            "gradingSnapshot",
                            "grading_snapshot",
                            "submissionMac",
                            "contentHash",
                            "materialHash",
                            "objectKey"))
                assertFalse(
                        entry.getValue().getBody().contains(secret), entry.getKey() + ":" + secret);
            Files.writeString(
                    samples.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
        }
        Path repo = repository();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/q03_contract.py")
                                        .toString(),
                                repo.toString(),
                                samples.toString())
                        .redirectErrorStream(true)
                        .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(process.waitFor(20, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }

    @Test
    void firstPlaybackOutboxFailureRollsBackPlanRunProgressAndIdempotency() {
        Lesson lesson = lesson(1, 80, 3);
        Map<String, Object> body = startBody(lesson.person(), lesson.course());
        int idemBefore = rows("SELECT COUNT(*) FROM vi_idempotency");
        doThrow(new IllegalStateException("隔离播放事件故障"))
                .when(outbox)
                .publish(
                        anyString(),
                        eq("VI_TRAINING_PLAYBACK"),
                        anyLong(),
                        eq("TRAINING_PLAYBACK_STARTED"),
                        any());
        requireStatus(
                miniCall("POST", playbackPath(lesson.course()), body, lesson.person().token()),
                500);
        assertTrainingRows(0, 0, 0, 0);
        assertEquals(idemBefore, rows("SELECT COUNT(*) FROM vi_idempotency"));
    }

    @Test
    void gradingOutboxFailureRollsBackScoreProgressAndCanonicalProofTogether() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        int idemBefore = rows("SELECT COUNT(*) FROM vi_idempotency");
        doThrow(new IllegalStateException("隔离阅卷事件故障"))
                .when(outbox)
                .publish(
                        anyString(),
                        eq("VI_TRAINING_ATTEMPT"),
                        eq(attempt.path("attemptId").asLong()),
                        eq("TRAINING_ATTEMPT_GRADED"),
                        any());
        requireStatus(
                miniCall(
                        "POST",
                        TRAINING + "/submit",
                        answerBody(lesson.person(), attempt, 1),
                        lesson.person().token()),
                500);
        assertEquals("OPEN", attemptField(attempt, "status", String.class));
        assertNull(attemptField(attempt, "score", Integer.class));
        assertNull(attemptField(attempt, "submission_mac", byte[].class));
        assertEquals(
                attempt.path("version").asLong(), attemptField(attempt, "version", Long.class));
        assertEquals(
                "READY_FOR_EXAM",
                trainingUnit(lesson.person(), lesson.course()).path("status").asText());
        assertEquals(
                0,
                rows("SELECT COUNT(*) FROM vi_provider_verification WHERE task_type='TRAINING'"));
        assertEquals(idemBefore, rows("SELECT COUNT(*) FROM vi_idempotency"));
    }

    @Test
    void databaseGradingFailureDoesNotCommitHalfAnExamOrAFalseCompletedRun() throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        jdbc.execute(
                "ALTER TABLE vi_training_attempt ADD CONSTRAINT ck_q03_test_score CHECK (score IS"
                        + " NULL)");
        try {
            requireStatus(
                    miniCall(
                            "POST",
                            TRAINING + "/submit",
                            answerBody(lesson.person(), attempt, 1),
                            lesson.person().token()),
                    500);
            assertEquals("OPEN", attemptField(attempt, "status", String.class));
            assertNull(attemptField(attempt, "score", Integer.class));
            assertEquals(
                    "ACTIVE",
                    jdbc.queryForObject(
                            "SELECT status FROM vi_training_run WHERE id=?",
                            String.class,
                            attempt.path("runId").asLong()));
            assertEquals(
                    0,
                    rows(
                            "SELECT COUNT(*) FROM vi_provider_verification WHERE"
                                    + " task_type='TRAINING'"));
        } finally {
            jdbc.execute("ALTER TABLE vi_training_attempt DROP CHECK ck_q03_test_score");
        }
    }

    @Test
    void missingEncryptionKeyCannotCreateAnUnprotectedGradingSnapshotOrConsumeAnAttempt()
            throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        Map<String, String> original = new HashMap<>(integrationKeys.getVersions());
        try {
            integrationKeys.setVersions(new HashMap<>());
            requireError(
                    miniCall(
                            "POST",
                            attemptPath(lesson.course()),
                            attemptBody(lesson.person(), lesson.course()),
                            lesson.person().token()),
                    503,
                    "VI_SECRET_UNAVAILABLE");
            assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_attempt"));
            assertEquals(
                    0, trainingUnit(lesson.person(), lesson.course()).path("attemptsUsed").asInt());
        } finally {
            integrationKeys.setVersions(original);
        }
        assertEquals(1, beginAttempt(lesson.person(), lesson.course()).path("attemptNo").asInt());
    }

    @Test
    void concurrentPolicyDraftsUseAggregateCasAndKeepExactlyOneAppendedRevision() throws Exception {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        Map<String, Object> first =
                policyDraftBody(COMPANY_A, List.of(course.path("trainingId").asText()), 2, 0);
        Map<String, Object> second = new LinkedHashMap<>(first);
        second.put("maxAttempts", 3);
        List<ResponseEntity<String>> results =
                race(
                        () -> call("PUT", POLICIES + "/" + COMPANY_A, first, rootAdmin),
                        () -> call("PUT", POLICIES + "/" + COMPANY_A, second, rootAdmin));
        assertEquals(
                List.of(200, 409),
                results.stream().map(r -> r.getStatusCode().value()).sorted().toList());
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_policy"));
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_policy_revision"));
        assertEquals(1, policy(COMPANY_A).path("version").asLong());
    }

    @Test
    void policyIdempotencyAndEncryptedReasonCannotBeReplayedAfterCompanyMembershipIsRevoked() {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        String token = scoped(COMPANY_A, POLICY_QUERY, POLICY_EDIT, POLICY_PUBLISH, COURSE_QUERY);
        Map<String, Object> body =
                policyDraftBody(COMPANY_A, List.of(course.path("trainingId").asText()), 3, 0);
        String reason = "Q03私有维护原因_不得进响应与普通审计明文";
        body.put("reason", reason);
        String key = key();
        JsonNode first = data(callKey("PUT", POLICIES + "/" + COMPANY_A, body, token, key));
        JsonNode replay = data(callKey("PUT", POLICIES + "/" + COMPANY_A, body, token, key));
        assertEquals(first.path("version").asLong(), replay.path("version").asLong());
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_policy_revision"));
        long policy = first.path("policyId").asLong();
        JsonNode audit =
                ViJson.tree(
                        jdbc.queryForObject(
                                "SELECT after_summary FROM vi_event_log WHERE"
                                    + " aggregate_type='VI_TRAINING_POLICY' AND aggregate_id=? AND"
                                    + " action='TRAINING_POLICY_DRAFT_SAVED'",
                                String.class,
                                policy));
        byte[] envelope = Base64.getDecoder().decode(audit.path("reasonEnvelope").asText());
        String aad = "Q03:ADMIN:" + staffId(token) + ":TRAINING_POLICY_DRAFT_SAVED:" + policy;
        assertEquals(reason, crypto.unenvelope(envelope, aad));
        assertThrows(RuntimeException.class, () -> crypto.unenvelope(envelope, aad + "0"));
        assertFalse(replay.toString().contains(reason));
        assertNoPlainArtifact(reason);
        Map<String, Object> changed = new LinkedHashMap<>(body);
        changed.put("reason", "相同意图不能变更原因");
        requireError(
                callKey("PUT", POLICIES + "/" + COMPANY_A, changed, token, key),
                409,
                "VI_IDEMPOTENCY_CONFLICT");
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", staffId(token));
        requireStatus(callKey("PUT", POLICIES + "/" + COMPANY_A, body, token, key), 404);
        assertEquals(1, rows("SELECT COUNT(*) FROM vi_training_policy_revision"));
    }

    @Test
    void realMaterialSaveAndExamSubmissionSerializeWithoutProducingAnOldMaterialProof()
            throws Exception {
        Lesson lesson = lesson(1, 80, 3);
        watchCourse(lesson.person(), lesson.course());
        JsonNode attempt = beginAttempt(lesson.person(), lesson.course());
        Map<String, Object> answers = answerBody(lesson.person(), attempt, 1);
        CountDownLatch materialHeld = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        org.mockito.Mockito.doAnswer(
                        invocation -> {
                            materialHeld.countDown();
                            assertTrue(release.await(5, TimeUnit.SECONDS));
                            return invocation.callRealMethod();
                        })
                .when(outbox)
                .publish(
                        anyString(),
                        eq("VI_DRIVER_APPLICATION"),
                        eq(lesson.person().applicationId()),
                        eq("QUALIFICATION_DRAFT_SAVED"),
                        any());
        var pool = Executors.newFixedThreadPool(2);
        try {
            var changed =
                    pool.submit(
                            () ->
                                    saveMaterials(
                                            lesson.person(), Map.of("realName", "持锁提交中的真实新材料")));
            assertTrue(materialHeld.await(3, TimeUnit.SECONDS));
            var grade =
                    pool.submit(
                            () ->
                                    miniCall(
                                            "POST",
                                            TRAINING + "/submit",
                                            answers,
                                            lesson.person().token()));
            assertThrows(
                    java.util.concurrent.TimeoutException.class,
                    () -> grade.get(150, TimeUnit.MILLISECONDS));
            release.countDown();
            changed.get(5, TimeUnit.SECONDS);
            requireStatus(grade.get(5, TimeUnit.SECONDS), 409);
            assertNull(attemptField(attempt, "score", Integer.class));
            assertEquals(
                    0,
                    rows(
                            "SELECT COUNT(*) FROM vi_provider_verification WHERE"
                                    + " task_type='TRAINING'"));
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void oldAdminAuthVersionIsRejectedForPolicyReadAndWriteEvenIfRedisEvictionFails() {
        JsonNode course = publishCourse(COMPANY_A, 1, 1, 80);
        String token = scoped(COMPANY_A, POLICY_QUERY, POLICY_EDIT, POLICY_PUBLISH, COURSE_QUERY);
        Map<String, Object> body =
                policyDraftBody(COMPANY_A, List.of(course.path("trainingId").asText()), 3, 0);
        data(call("GET", POLICIES + "/" + COMPANY_A, null, token));
        long user = staffId(token);
        doThrow(new IllegalStateException("隔离Redis删除故障")).when(redis).deleteObject(anyString());
        jdbc.update("UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?", user);
        requireStatus(call("GET", POLICIES + "/" + COMPANY_A, null, token), 401);
        requireStatus(call("PUT", POLICIES + "/" + COMPANY_A, body, token), 401);
        assertEquals(0, rows("SELECT COUNT(*) FROM vi_training_policy"));
    }

    private Lesson lesson(int questionCount, int passScore, int maximum) {
        JsonNode course = publishCourse(COMPANY_A, questionCount, 1, passScore);
        publishPolicy(COMPANY_A, List.of(course), maximum, 0);
        return new Lesson(student(COMPANY_A, false), course);
    }

    private List<Map<String, Object>> answers(JsonNode attempt, List<String> codes) {
        List<Map<String, Object>> answers = new ArrayList<>();
        for (JsonNode question : attempt.path("questions")) {
            answers.add(
                    new LinkedHashMap<>(
                            Map.of(
                                    "questionId",
                                    question.path("questionId").asText(),
                                    "answerCodes",
                                    codes)));
        }
        return answers;
    }

    private <T> T attemptField(JsonNode attempt, String column, Class<T> type) {
        // 列名全部来自本测试固定字面量，不使用请求或客户端字段拼SQL。
        return jdbc.queryForObject(
                "SELECT " + column + " FROM vi_training_attempt WHERE id=?",
                type,
                attempt.path("attemptId").asLong());
    }

    private JsonNode learnerAccess(Student person, JsonNode course) {
        return data(
                miniCall(
                        "POST",
                        "/api/common/files/" + course.path("mediaFileId").asText() + "/access",
                        Map.of("purpose", "本人当前冻结培训教材"),
                        person.token()));
    }

    private ResponseEntity<byte[]> readMedia(String token, JsonNode access) {
        HttpHeaders headers = trainingHeaders(token, null, "owner");
        headers.set("X-File-Ticket", access.path("requiredHeaders").path("X-File-Ticket").asText());
        return http.exchange(
                "http://127.0.0.1:" + port + access.path("downloadUrl").asText(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class);
    }

    private Map<String, Object> qualificationSubmitBody(Student person) {
        JsonNode current = qualification(person);
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "expectedVersion",
                current.path("version").asLong(),
                "expectedProfileVersion",
                profileVersion(person),
                "reason",
                "全部真实培训完成后提交资料审核");
    }

    private long staffId(String token) {
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId();
    }

    private String stationSession(Student person) {
        // I06身份的明确已关联边界；培训资格仍由同一真实个人申请决定，不能把员工身份当DRIVER。
        long user = staffId(scoped(COMPANY_A, "biz:order:list"));
        long station = station(SITE_DEPT);
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,source,status,phone_lookup,last_phone_verified_at,employee_auth_version)"
                    + " SELECT"
                    + " id,?,UTC_TIMESTAMP(3),app_id,'WECHAT_PHONE_MATCH','ACTIVE',phone_lookup,phone_verified_at,0"
                    + " FROM vi_account WHERE id=?",
                user,
                person.accountId());
        jdbc.update(
                "INSERT INTO vi_station_grant(sys_user_id,station_id,granted_by,reason,enabled)"
                        + " VALUES (?,?,1,'培训身份测试授权',1)",
                user,
                station);
        var session = new com.xinyi.rongli.identity.domain.ViSession();
        session.setAccountId(person.accountId());
        session.setAppId("wx0000000000000000");
        session.setStatus("ACTIVE");
        session.setSessionEpoch(0L);
        session.setContextVersion(0L);
        session.setActiveRole("STATION");
        session.setLinkedSysUserId(user);
        session.setStationId(station);
        session.setAbsoluteExpiresAt(Instant.now().plusSeconds(3600));
        miniSessions.insert(session);
        return miniTokens.issueForSession(session).accessToken();
    }

    private List<ResponseEntity<String>> race(
            java.util.concurrent.Callable<ResponseEntity<String>> first,
            java.util.concurrent.Callable<ResponseEntity<String>> second)
            throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(3, TimeUnit.SECONDS));
                                return first.call();
                            });
            var b =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(3, TimeUnit.SECONDS));
                                return second.call();
                            });
            start.countDown();
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    private void assertSafeArtifacts() {
        for (String secret :
                List.of(
                        "correctOptionCodes",
                        "grading_snapshot_cipher",
                        "110105199001010010",
                        "accessToken",
                        "refreshToken")) {
            assertNoPlainArtifact(secret);
        }
    }

    private void assertNoPlainArtifact(String fragment) {
        for (var row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log")) {
            for (Object value : row.values())
                assertFalse(Objects.toString(value, "").contains(fragment));
        }
        for (String value : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class))
            assertFalse(value.contains(fragment));
        for (String value :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) assertFalse(value.contains(fragment));
    }

    private Path repository() {
        Path repo = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!Files.exists(repo.resolve("docs/vehicle"))) repo = repo.getParent();
        return repo;
    }

    private String playbackPath(JsonNode course) {
        return TRAINING + "/" + course.path("trainingId").asText() + "/playback";
    }

    private String progressPath(JsonNode course) {
        return TRAINING + "/" + course.path("trainingId").asText() + "/progress";
    }

    private String attemptPath(JsonNode course) {
        return TRAINING + "/" + course.path("trainingId").asText() + "/attempts";
    }

    private long duration(Student person, JsonNode course) {
        return trainingUnit(person, course).path("verifiedDurationMillis").asLong();
    }

    private void assertTrainingRows(int plans, int runs, int progress, int attempts) {
        assertEquals(plans, rows("SELECT COUNT(*) FROM vi_training_plan_snapshot"));
        assertEquals(runs, rows("SELECT COUNT(*) FROM vi_training_run"));
        assertEquals(progress, rows("SELECT COUNT(*) FROM vi_training_progress"));
        assertEquals(attempts, rows("SELECT COUNT(*) FROM vi_training_attempt"));
    }

    private String policyRevisionFacts(long revision) {
        return ViJson.write(
                jdbc.queryForMap(
                        "SELECT"
                            + " required_training_ids,max_attempts,retry_interval_seconds,grading_mode,content_hash,created_by_user_id"
                            + " FROM vi_training_policy_revision WHERE id=?",
                        revision));
    }

    private Map<String, Object> consentBody() {
        return Map.of(
                "consentVersion",
                CONSENT_VERSION,
                "consentTextHash",
                sha(CONSENT_TEXT.getBytes(StandardCharsets.UTF_8)),
                "acceptedConsent",
                true);
    }
}
