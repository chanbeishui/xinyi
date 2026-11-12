package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.dispatch.mapper.DispatchListeningMapper;
import com.xinyi.rongli.dispatch.service.ListeningExpiryJob;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** D01真实HTTP/MySQL听单、公司下班截止与最近定位；不替换业务服务或鉴权。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=d01-isolated-phone-key",
            "vi.outbox-relay-enabled=false",
            "vi.jobs.listening-enabled=true",
            "vi.jobs.listening-delay-ms=3600000",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class D01DispatchIT extends DispatchHttpFixture {
    @Autowired IntegrationCrypto dispatchCrypto;
    @Autowired DispatchListeningMapper dispatchMapper;
    @Autowired ListeningExpiryJob listeningJob;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;

    @Test
    void missingMiniAuthenticationReturnsReal401ForAllFourRoutes() {
        for (String path : List.of(AVAILABILITY, LISTENING, LOCATION, PRESENCE)) {
            requireStatus(rawTraining(method(path), path, Map.of(), null, key(), "driver"), 401);
        }
    }

    @Test
    void administratorTokensCannotEnterOwnDriverApiEvenWithDriverHeaders() {
        for (String path : List.of(AVAILABILITY, LISTENING, LOCATION, PRESENCE)) {
            requireStatus(
                    rawTraining(method(path), path, Map.of(), rootAdmin, key(), "driver"), 401);
        }
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_location"));
    }

    @Test
    void ownerRoleAndClientSubmittedIdentityCannotGrantDriverAuthority() {
        configured();
        String owner = anotherSession(driver.accountId());
        requireStatus(rawTraining("GET", AVAILABILITY, null, owner, key(), "owner"), 400);
        requireStatus(rawTraining("GET", AVAILABILITY, null, owner, key(), "driver"), 403);
        Map<String, Object> fake = listeningBody(true);
        fake.put("driverId", Long.toString(driver.driverId()));
        requireStatus(driverCall("PATCH", LISTENING, fake), 400);
        for (String field : List.of("ownerDeptId", "accountId", "params", "visibleDeptIds")) {
            fake = locationBody(now());
            fake.put(field, "1");
            requireStatus(driverCall("POST", LOCATION, fake), 400);
        }
    }

    @Test
    void missingConfigurationIsExplicitAndReadsCannotInventDefaultsOrWriteFreshness() {
        JsonNode config = data(call("GET", configPath(COMPANY_A), null, rootAdmin));
        assertFalse(config.path("configured").asBoolean());
        assertEquals(0, config.path("version").asLong());
        for (String field :
                List.of(
                        "closingTime",
                        "locationMaxAgeSeconds",
                        "reachabilityMaxAgeSeconds",
                        "maxAccuracyMeter",
                        "locationReportIntervalSeconds")) {
            assertTrue(config.path(field).isNull(), field);
        }
        JsonNode view = availability();
        assertFalse(view.path("configurationReady").asBoolean());
        assertFalse(view.path("availableForNewOrder").asBoolean());
        assertTrue(view.path("qualificationValid").asBoolean());
        assertEquals("MISSING", view.path("locationStatus").asText());
        assertFalse(view.path("clientReachable").asBoolean());
        Map<String, Object> opening = listeningBody(true);
        opening.put("expectedConfigVersion", config.path("version").asLong());
        requireError(driverCall("PATCH", LISTENING, opening), 422, "VI_CAPABILITY_NOT_CONFIGURED");
        requireError(
                driverCall("POST", LOCATION, locationBody(now())),
                422,
                "VI_CAPABILITY_NOT_CONFIGURED");
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_listening_config"));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_location"));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_presence"));
        assertFalse(listening(false).path("listeningEnabled").asBoolean());
    }

    @Test
    void companyConfigurationNeedsIndependentQueryAndEditPermissions() {
        String read = scoped(COMPANY_A, CONFIG_QUERY), edit = scoped(COMPANY_A, CONFIG_EDIT);
        data(call("GET", configPath(COMPANY_A), null, read));
        requireStatus(call("PUT", configPath(COMPANY_A), configBody(0, "18:00"), read), 403);
        requireStatus(call("GET", configPath(COMPANY_A), null, edit), 403);
        requireStatus(call("PUT", configPath(COMPANY_A), configBody(0, "18:00"), edit), 403);
        requireStatus(
                rawTraining("GET", configPath(COMPANY_A), null, driver.token(), key(), "admin"),
                401);
    }

    @Test
    void companyConfigurationScopeUsesRealSelectedCompanyAndEmptyScopeFailsClosed() {
        String a = scoped(COMPANY_A, CONFIG_QUERY, CONFIG_EDIT);
        requireStatus(call("GET", configPath(COMPANY_B), null, a), 404);
        requireStatus(call("PUT", configPath(COMPANY_B), configBody(0, "18:00"), a), 404);
        data(call("PUT", configPath(COMPANY_A), configBody(0, "18:00"), a));
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", administratorId(a));
        requireStatus(call("GET", configPath(COMPANY_A), null, a), 404);
        requireStatus(call("PUT", configPath(COMPANY_A), configBody(1, "17:00"), a), 404);
        assertEquals("18:00", companyField("closing_time"));
        List<Map<String, Object>> denied =
                jdbc.queryForList(
                        "SELECT"
                            + " action,actor_realm,actor_user_id,actor_account_id,aggregate_type,reason"
                            + " FROM vi_event_log WHERE action IN"
                            + " ('COMPANY_LISTENING_CONFIG_QUERY_DENIED','COMPANY_LISTENING_CONFIG_CHANGED_DENIED')"
                            + " AND actor_user_id=?",
                        administratorId(a));
        assertEquals(4, denied.size(), "跨公司与空范围的GET/PUT拒绝均须真实留痕");
        for (String action :
                List.of(
                        "COMPANY_LISTENING_CONFIG_QUERY_DENIED",
                        "COMPANY_LISTENING_CONFIG_CHANGED_DENIED")) {
            assertEquals(
                    2,
                    denied.stream().filter(record -> action.equals(record.get("action"))).count(),
                    action);
        }
        for (Map<String, Object> record : denied) {
            assertEquals("ADMIN", record.get("actor_realm"));
            assertEquals(administratorId(a), ((Number) record.get("actor_user_id")).longValue());
            assertNull(record.get("actor_account_id"));
            assertEquals("VI_DRIVER_LISTENING_CONFIG", record.get("aggregate_type"));
            assertEquals("VI_RESOURCE_NOT_VISIBLE", record.get("reason"));
        }
    }

    @Test
    void nonexistentDisabledAndMalformedCompanyTargetsAreNotSilentlyConfigured() {
        requireStatus(call("GET", configPath(9223372036854775807L), null, rootAdmin), 422);
        jdbc.update("UPDATE sys_dept SET status='1' WHERE dept_id=?", COMPANY_B);
        requireStatus(call("PUT", configPath(COMPANY_B), configBody(0, "18:00"), rootAdmin), 422);
        for (String id : List.of("01", "-1", "9223372036854775808")) {
            requireStatus(call("GET", CONFIGS + id + "/listening-config", null, rootAdmin), 400);
        }
        requireStatus(
                call("GET", configPath(COMPANY_A) + "?params.dataScope=1", null, rootAdmin), 400);
        requireStatus(driverCall("GET", AVAILABILITY + "?driverId=1", null), 400);
    }

    @Test
    void configurationWriteRequiresExactKeysAndCannotWriteInternalOwnership() {
        Map<String, Object> good = configBody(0, "18:00");
        for (String field : good.keySet()) {
            Map<String, Object> missing = new LinkedHashMap<>(good);
            missing.remove(field);
            requireStatus(call("PUT", configPath(COMPANY_A), missing, rootAdmin), 400);
        }
        for (String field : List.of("ownerDeptId", "updatedByUserId", "params", "version")) {
            Map<String, Object> forged = new LinkedHashMap<>(good);
            forged.put(field, "1");
            requireStatus(call("PUT", configPath(COMPANY_A), forged, rootAdmin), 400);
        }
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_listening_config"));
    }

    @Test
    void configurationHasNoAmbiguousTimeOrImplicitOperatingParameterBounds() {
        for (String value : List.of("6:00", "24:00", "18:60", "18:00:00", " 18:00")) {
            Map<String, Object> body = configBody(0, value);
            requireStatus(call("PUT", configPath(COMPANY_A), body, rootAdmin), 422);
        }
        for (Map.Entry<String, Object> bad :
                Map.<String, Object>of(
                                "locationMaxAgeSeconds",
                                30,
                                "reachabilityMaxAgeSeconds",
                                30,
                                "maxAccuracyMeter",
                                0,
                                "locationReportIntervalSeconds",
                                29)
                        .entrySet()) {
            Map<String, Object> body = configBody(0, "18:00");
            body.put(bad.getKey(), bad.getValue());
            requireStatus(call("PUT", configPath(COMPANY_A), body, rootAdmin), 422);
        }
        Map<String, Object> body = configBody(0, "18:00");
        body.put("locationReportIntervalSeconds", 90);
        requireStatus(call("PUT", configPath(COMPANY_A), body, rootAdmin), 422);
    }

    @Test
    void configurationCasUuidAndPermissionRevocationApplyAlsoToReplays() {
        String staff = scoped(COMPANY_A, CONFIG_QUERY, CONFIG_EDIT);
        String uuid = key();
        Map<String, Object> body = configBody(0, "18:00");
        JsonNode first = data(callKey("PUT", configPath(COMPANY_A), body, staff, uuid));
        assertEquals(1, first.path("version").asLong());
        data(callKey("PUT", configPath(COMPANY_A), body, staff, uuid));
        Map<String, Object> other = new LinkedHashMap<>(body);
        other.put("closingTime", "19:00");
        requireError(
                callKey("PUT", configPath(COMPANY_A), other, staff, uuid),
                409,
                "VI_IDEMPOTENCY_CONFLICT");
        requireError(call("PUT", configPath(COMPANY_A), body, staff), 409, "VI_VERSION_CONFLICT");
        requireStatus(
                callKey("PUT", configPath(COMPANY_A), configBody(1, "17:00"), staff, "bad"), 400);
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", administratorId(staff));
        requireStatus(callKey("PUT", configPath(COMPANY_A), body, staff, uuid), 404);
        assertEquals(1, eventCount("COMPANY_LISTENING_CONFIG_CHANGED"));
    }

    @Test
    void concurrentCompanyEditsCommitOnlyOneRevision() throws Exception {
        configured();
        List<ResponseEntity<String>> results =
                race(
                        () -> call("PUT", configPath(COMPANY_A), configBody(1, "17:00"), rootAdmin),
                        () ->
                                call(
                                        "PUT",
                                        configPath(COMPANY_A),
                                        configBody(1, "19:00"),
                                        rootAdmin));
        assertEquals(List.of(200, 409), statuses(results));
        assertEquals(2L, ((Number) companyField("version")).longValue());
    }

    @Test
    void listeningRequestBindsSeparateVersionCompanyConfigAndActualSession() {
        configured();
        Map<String, Object> original = listeningBody(true);
        assertEquals(7, original.size());
        for (String field : original.keySet()) {
            Map<String, Object> missing = new LinkedHashMap<>(original);
            missing.remove(field);
            requireStatus(driverCall("PATCH", LISTENING, missing), 400);
        }
        for (String field :
                List.of(
                        "expectedVersion",
                        "expectedCompanyRevision",
                        "expectedContextVersion",
                        "expectedConfigVersion")) {
            Map<String, Object> stale = new LinkedHashMap<>(original);
            stale.put(field, 999);
            requireStatus(driverCall("PATCH", LISTENING, stale), 409);
        }
        Map<String, Object> wrongSession = new LinkedHashMap<>(original);
        wrongSession.put("expectedSessionId", "9223372036854775807");
        requireStatus(driverCall("PATCH", LISTENING, wrongSession), 409);
        assertFalse(availability().path("listeningEnabled").asBoolean());
    }

    @Test
    void listeningTypesAndUuidCannotBeReplacedByTruthyOrUnsafeValues() {
        configured();
        for (Object invalid : List.of("true", 1, List.of(true))) {
            Map<String, Object> body = listeningBody(true);
            body.put("enabled", invalid);
            requireStatus(driverCall("PATCH", LISTENING, body), 400);
        }
        for (Object invalid : List.of(-1, 1.5, 9007199254740992L, "0")) {
            Map<String, Object> body = listeningBody(true);
            body.put("expectedVersion", invalid);
            requireStatus(driverCall("PATCH", LISTENING, body), 400);
        }
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true), "not-a-uuid"), 400);
    }

    @Test
    void manualOpeningFreezesTodayShanghaiDeadlineWithoutGrantingLocationOrReachability()
            throws Exception {
        configured();
        Map<String, Object> sameVersion = listeningBody(true);
        List<ResponseEntity<String>> replies =
                race(
                        () -> driverCall("PATCH", LISTENING, sameVersion),
                        () -> driverCall("PATCH", LISTENING, sameVersion));
        assertEquals(List.of(200, 409), statuses(replies), "两个明确开启动作不能一起消费同一听单CAS");
        JsonNode opened = availability();
        assertTrue(opened.path("listeningEnabled").asBoolean());
        assertTrue(opened.path("listeningIntentEnabled").asBoolean());
        assertEquals(businessTime(18, 0), Instant.parse(opened.path("listeningUntil").asText()));
        assertEquals(1, opened.path("listeningVersion").asLong());
        assertEquals(opened.path("listeningVersion"), opened.path("version"));
        assertFalse(opened.path("locationFresh").asBoolean());
        assertFalse(opened.path("clientReachable").asBoolean());
        assertFalse(opened.path("availableForNewOrder").asBoolean());
        assertEquals(1, eventCount("DRIVER_LISTENING_CHANGED"));
    }

    @Test
    void completeFreshFactsProduceOnlyABaseCandidateAndManualOffRemainsIndependent() {
        JsonNode ready = readyCandidate();
        assertTrue(ready.path("availableForNewOrder").asBoolean(), ready.toString());
        assertEquals("BASE_CANDIDATE_ONLY", ready.path("eligibilityScope").asText());
        assertEquals("BASE_READY", ready.path("reason").asText());
        JsonNode stopped = listening(false);
        assertFalse(stopped.path("listeningEnabled").asBoolean());
        assertFalse(stopped.path("availableForNewOrder").asBoolean());
        assertTrue(stopped.path("locationFresh").asBoolean());
        assertTrue(stopped.path("clientReachable").asBoolean());
    }

    @Test
    void backgroundObservationKeepsListeningIntentAndDoesNotClaimPermanentReachability() {
        readyCandidate();
        controlledNow.set(now().plusSeconds(1));
        contact("BACKGROUND", now());
        JsonNode background = availability();
        assertTrue(background.path("listeningEnabled").asBoolean());
        assertEquals("BACKGROUND", background.path("clientState").asText());
        assertTrue(background.path("clientReachable").asBoolean());
        assertTrue(background.path("availableForNewOrder").asBoolean());
        controlledNow.set(now().plusSeconds(90));
        JsonNode later = availability();
        assertTrue(later.path("listeningIntentEnabled").asBoolean());
        assertTrue(later.path("listeningEnabled").asBoolean());
        assertFalse(later.path("clientReachable").asBoolean());
        assertFalse(later.path("availableForNewOrder").asBoolean());
    }

    @Test
    void exactDeadlineDisablesListeningBeforeThePersistentWorkerRuns() {
        configured();
        listening(true);
        controlledNow.set(businessTime(18, 0).minusMillis(1));
        assertTrue(availability().path("listeningEnabled").asBoolean());
        controlledNow.set(now().plusMillis(1));
        JsonNode expired = availability();
        assertFalse(expired.path("listeningEnabled").asBoolean());
        assertTrue(expired.path("listeningIntentEnabled").asBoolean(), "读取不伪装定时任务已经写回");
        assertEquals(1, ((Number) persistedProfile().get("listening_enabled")).intValue());
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true)), 409);
        assertFalse(expired.path("availableForNewOrder").asBoolean());
    }

    @Test
    void nextBusinessDayRequiresANewExplicitManualOpening() {
        configured();
        listening(true);
        Instant yesterday = businessTime(18, 0);
        controlledNow.set(businessTime(9, 0).plus(Duration.ofDays(1)));
        assertFalse(availability().path("listeningEnabled").asBoolean());
        assertEquals(yesterday, Instant.parse(availability().path("listeningUntil").asText()));
        listeningJob.runOnce();
        assertFalse(availability().path("listeningEnabled").asBoolean());
        JsonNode opened = listening(true);
        assertTrue(opened.path("listeningEnabled").asBoolean());
        assertEquals(
                yesterday.plus(Duration.ofDays(1)),
                Instant.parse(opened.path("listeningUntil").asText()));
    }

    @Test
    void oldUuidReplayNeverReopensTomorrowOrExtendsTheOriginalDeadline() {
        configured();
        Map<String, Object> body = listeningBody(true);
        String uuid = key();
        JsonNode opened = data(driverCall("PATCH", LISTENING, body, uuid));
        String deadline = opened.path("listeningUntil").asText();
        Map<String, Object> anotherIntent = new LinkedHashMap<>(body);
        anotherIntent.put("enabled", false);
        requireError(
                driverCall("PATCH", LISTENING, anotherIntent, uuid),
                409,
                "VI_IDEMPOTENCY_CONFLICT");
        assertTrue(availability().path("listeningEnabled").asBoolean());
        controlledNow.set(now().plusSeconds(10));
        data(driverCall("PATCH", LISTENING, body, uuid));
        assertEquals(deadline, availability().path("listeningUntil").asText());
        controlledNow.set(businessTime(9, 0).plus(Duration.ofDays(1)));
        JsonNode replay = data(driverCall("PATCH", LISTENING, body, uuid));
        assertFalse(replay.path("listeningEnabled").asBoolean());
        assertEquals(deadline, replay.path("listeningUntil").asText());
        assertEquals(1, eventCount("DRIVER_LISTENING_CHANGED"));
    }

    @Test
    void anUndeliveredOpeningCannotMoveToTomorrowEvenWhenAllVersionsStillMatch() {
        configured();
        Map<String, Object> delayed = listeningBody(true);
        String firstDate = delayed.get("expectedBusinessDate").toString();
        controlledNow.set(businessTime(9, 0).plus(Duration.ofDays(1)));
        assertEquals(
                delayed.get("expectedVersion"), availability().path("listeningVersion").asLong());
        assertNotEquals(firstDate, availability().path("businessDate").asText());
        // 这是从未到达服务器的旧动作，不能靠没有幂等记录或CAS仍相等就偷偷在新一天开启。
        requireStatus(driverCall("PATCH", LISTENING, delayed), 409);
        assertFalse(availability().path("listeningIntentEnabled").asBoolean());
        assertEquals(0, eventCount("DRIVER_LISTENING_CHANGED"));
        assertTrue(listening(true).path("listeningEnabled").asBoolean());
        Map<String, Object> safeClose = listeningBody(false);
        safeClose.put("expectedBusinessDate", firstDate);
        assertFalse(
                data(driverCall("PATCH", LISTENING, safeClose))
                        .path("listeningEnabled")
                        .asBoolean());
    }

    @Test
    void extendingCompanyClosingTimeCannotExtendAnAlreadyOpenedDay() {
        configured();
        JsonNode opened = listening(true);
        configure(COMPANY_A, "19:00");
        assertEquals(opened.path("listeningUntil"), availability().path("listeningUntil"));
        controlledNow.set(businessTime(18, 0));
        assertFalse(availability().path("listeningEnabled").asBoolean());
        assertEquals(Instant.parse(opened.path("listeningUntil").asText()), databaseDeadline());
    }

    @Test
    void earlierCompanyClosingTimePersistentlyTightensExistingDeadlines() {
        configured();
        listening(true);
        Instant observed = now();
        locate(observed);
        contact("BACKGROUND", observed);
        Map<String, Object> tighter = configBody(1, "17:00");
        tighter.put("locationMaxAgeSeconds", 60);
        tighter.put("reachabilityMaxAgeSeconds", 45);
        JsonNode changed = data(call("PUT", configPath(COMPANY_A), tighter, rootAdmin));
        assertEquals(2, changed.path("version").asLong());
        assertEquals(businessTime(17, 0), databaseDeadline());
        JsonNode shortened = availability();
        assertEquals("FRESH", shortened.path("locationStatus").asText(), "收紧位置须重新绑定AAD而不是破坏原密文认证");
        assertEquals(2, shortened.path("locationVersion").asLong());
        assertEquals(2, shortened.path("presenceVersion").asLong());
        assertEquals(
                observed.plusSeconds(60),
                Instant.parse(shortened.path("locationExpiresAt").asText()));
        assertEquals(
                observed.plusSeconds(45),
                Instant.parse(shortened.path("reachabilityExpiresAt").asText()));

        // 随后放宽只作用于以后新报告/新开启；不能复活已经收紧的旧点、交互或当日意愿期限。
        configure(COMPANY_A, "18:00");
        JsonNode relaxed = availability();
        assertEquals(shortened.path("locationExpiresAt"), relaxed.path("locationExpiresAt"));
        assertEquals(
                shortened.path("reachabilityExpiresAt"), relaxed.path("reachabilityExpiresAt"));
        assertEquals(businessTime(17, 0), databaseDeadline());
        controlledNow.set(observed.plusSeconds(61));
        assertFalse(availability().path("locationFresh").asBoolean());
        assertFalse(availability().path("clientReachable").asBoolean());
        assertTrue(availability().path("listeningEnabled").asBoolean());
        controlledNow.set(businessTime(17, 0));
        assertFalse(availability().path("listeningEnabled").asBoolean());
        listeningJob.runOnce();
        assertEquals(0, ((Number) persistedProfile().get("listening_enabled")).intValue());
    }

    @Test
    void shanghaiDateCanDifferFromUtcAndMidnightIsNotUnlimitedListening() {
        configured();
        controlledNow.set(businessTime(1, 0));
        assertNotEquals(
                now().atZone(ZoneOffset.UTC).toLocalDate(), now().atZone(SHANGHAI).toLocalDate());
        assertEquals(
                businessTime(18, 0),
                Instant.parse(listening(true).path("listeningUntil").asText()));
        listening(false);
        configure(COMPANY_A, "00:00");
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true)), 409);
    }

    @Test
    void administrativePauseCannotBeBypassedButNeverPreventsManualOff() {
        configured();
        listening(true);
        data(
                call(
                        "POST",
                        "/api/admin/drivers/" + driver.driverId() + "/disable-new-assignments",
                        Map.of("expectedVersion", driverVersion(), "reason", "明确暂停新承接"),
                        rootAdmin));
        JsonNode paused = availability();
        assertFalse(paused.path("newAssignmentEnabled").asBoolean());
        assertTrue(paused.path("listeningEnabled").asBoolean());
        assertFalse(paused.path("availableForNewOrder").asBoolean());
        listening(false);
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true)), 409);
    }

    @Test
    void expiredQualificationKeepsHistoricalRoleAndAllowsClosingWithoutRestoringQualification() {
        configured();
        listening(true);
        controlledNow.set(
                Instant.parse(
                                adminDetail(qualified)
                                        .path("effectiveQualification")
                                        .path("expiresAt")
                                        .asText())
                        .plusMillis(1));
        JsonNode invalid = availability();
        assertFalse(invalid.path("qualificationValid").asBoolean());
        assertFalse(invalid.path("availableForNewOrder").asBoolean());
        assertFalse(listening(false).path("listeningEnabled").asBoolean());
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true)), 409);
        assertEquals("APPROVED", persistedProfile().get("qualification_status"), "实时门禁不伪称审批历史被改写");
    }

    @Test
    void busyDriverMayListenButClosingNeverAbandonsAnExistingHeldVehicle() {
        configured();
        long order = heldVehicle(qualified);
        Map<String, Object> before = heldFacts(order);
        JsonNode busy = listening(true);
        assertTrue(busy.path("listeningEnabled").asBoolean());
        assertTrue(busy.path("occupied").asBoolean());
        assertEquals("ASSIGNMENT", busy.path("occupationType").asText());
        assertFalse(busy.path("availableForNewOrder").asBoolean());
        listening(false);
        assertEquals(before, heldFacts(order));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        driver.driverId()));
    }

    @Test
    void persistentDeadlineClosurePreservesAssignmentOccupationAndAdministrativePreference() {
        configured();
        long order = heldVehicle(qualified);
        Map<String, Object> before = heldFacts(order);
        listening(true);
        controlledNow.set(businessTime(18, 0));
        listeningJob.runOnce();
        assertEquals(0, ((Number) persistedProfile().get("listening_enabled")).intValue());
        assertEquals(1, ((Number) persistedProfile().get("new_assignment_enabled")).intValue());
        assertEquals(before, heldFacts(order));
        assertEquals(1, eventCount("DRIVER_LISTENING_EXPIRED"));
    }

    @Test
    void legacyEnabledWithoutDeadlineFailsClosedAndWorkerDoesNotInventTodaysWindow() {
        jdbc.update(
                "UPDATE vi_driver_profile SET"
                    + " listening_enabled=1,listening_until=NULL,listening_company_revision=NULL"
                    + " WHERE id=?",
                driver.driverId());
        assertFalse(availability().path("listeningEnabled").asBoolean());
        listeningJob.runOnce();
        assertEquals(0, ((Number) persistedProfile().get("listening_enabled")).intValue());
        assertNull(persistedProfile().get("listening_until"));
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_listening_config"));
    }

    @Test
    void repeatedDeadlineWorkersRecordOnlyOneClosure() {
        configured();
        listening(true);
        controlledNow.set(businessTime(18, 0));
        listeningJob.runOnce();
        listeningJob.runOnce();
        assertEquals(1, eventCount("DRIVER_LISTENING_EXPIRED"));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='DRIVER_LISTENING_EXPIRED'"
                                + " AND aggregate_id=?",
                        driver.driverId()));
    }

    @Test
    void anOldDayTimerCannotCloseANewerExplicitOpening() {
        configured();
        listening(true);
        controlledNow.set(businessTime(9, 0).plus(Duration.ofDays(1)));
        JsonNode tomorrow = listening(true);
        listeningJob.runOnce();
        assertTrue(availability().path("listeningEnabled").asBoolean());
        assertEquals(tomorrow.path("listeningVersion"), availability().path("listeningVersion"));
        assertEquals(tomorrow.path("listeningUntil"), availability().path("listeningUntil"));
    }

    @Test
    void missingPersistentTimerIsRepairedWithoutChangingTheFrozenDeadline() {
        configured();
        JsonNode opened = listening(true);
        jdbc.update(
                "DELETE FROM vi_outbox WHERE event_type='DRIVER_LISTENING_EXPIRES' AND"
                        + " aggregate_id=?",
                driver.driverId());
        listeningJob.runOnce();
        listeningJob.runOnce();
        assertEquals(1, eventCount("DRIVER_LISTENING_EXPIRES"));
        assertEquals(opened.path("listeningUntil"), availability().path("listeningUntil"));
    }

    @Test
    void listeningOutboxFailureRollsBackPreferenceVersionAndSuccessAudit() {
        configured();
        long revision = availability().path("listeningVersion").asLong();
        failEvent("DRIVER_LISTENING_CHANGED");
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true)), 500);
        assertFalse(availability().path("listeningIntentEnabled").asBoolean());
        assertEquals(revision, availability().path("listeningVersion").asLong());
        assertEquals(0, eventCount("DRIVER_LISTENING_CHANGED"));
        assertEquals(
                0,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE"
                                + " action='DRIVER_LISTENING_CHANGED'"));
    }

    @Test
    void timerPublicationFailureCannotCommitAnOpeningWithoutItsClosurePlan() {
        configured();
        doThrow(new IllegalStateException("隔离截止任务发布失败"))
                .when(outbox)
                .publishAt(
                        anyString(),
                        anyString(),
                        anyLong(),
                        eq("DRIVER_LISTENING_EXPIRES"),
                        any(),
                        any());
        requireStatus(driverCall("PATCH", LISTENING, listeningBody(true)), 500);
        assertFalse(availability().path("listeningIntentEnabled").asBoolean());
        assertEquals(0, eventCount("DRIVER_LISTENING_CHANGED"));
    }

    @Test
    void configurationEventFailureRollsBackTheWholeImmediateChange() {
        configured();
        listening(true);
        Instant deadline = databaseDeadline();
        failEvent("COMPANY_LISTENING_CONFIG_CHANGED");
        requireStatus(call("PUT", configPath(COMPANY_A), configBody(1, "17:00"), rootAdmin), 500);
        assertEquals("18:00", companyField("closing_time"));
        assertEquals(deadline, databaseDeadline());
        assertEquals(1, ((Number) companyField("version")).intValue());
    }

    @Test
    void locationIsOnlyEncryptedLatestPointAndDoesNotExposeCoordinatesInViewsOrArtifacts() {
        configured();
        JsonNode accepted = locate(now());
        assertTrue(accepted.path("accepted").asBoolean());
        assertEquals(1, accepted.path("version").asLong());
        Map<String, Object> point = locationRow();
        assertEquals(driver.driverId(), ((Number) point.get("driver_id")).longValue());
        assertEquals(COMPANY_A, ((Number) point.get("owner_dept_id")).longValue());
        assertNotNull(point.get("location_cipher"));
        var bound = dispatchMapper.location(driver.driverId());
        String aad =
                "D01:LOCATION:"
                        + bound.getDriverId()
                        + ":SESSION:"
                        + bound.getSessionId()
                        + ":CONTEXT:"
                        + bound.getContextVersion()
                        + ":COMPANY:"
                        + bound.getOwnerDeptId()
                        + ":EPOCH:"
                        + bound.getCompanyRevision()
                        + ":VERSION:"
                        + bound.getVersion()
                        + ":CONFIG:"
                        + bound.getConfigVersion()
                        + ":CAPTURED:"
                        + bound.getCapturedAt()
                        + ":RECEIVED:"
                        + bound.getReceivedAt()
                        + ":UNTIL:"
                        + bound.getValidUntil()
                        + ":ACCURACY:"
                        + bound.getAccuracyMeter();
        JsonNode coordinates =
                ViJson.tree(dispatchCrypto.unenvelope(bound.getLocationCipher(), aad));
        assertEquals(112.123456, coordinates.path("lng").asDouble());
        assertEquals(22.234567, coordinates.path("lat").asDouble());
        assertThrows(
                RuntimeException.class,
                () -> dispatchCrypto.unenvelope(bound.getLocationCipher(), aad + ":OTHER"));
        assertFalse(
                new String(
                                (byte[]) point.get("location_cipher"),
                                java.nio.charset.StandardCharsets.UTF_8)
                        .contains("112.123456"));
        JsonNode view = availability();
        assertTrue(view.path("locationFresh").asBoolean());
        assertEquals("FRESH", view.path("locationStatus").asText());
        assertFalse(view.toString().contains("112.123456"));
        assertFalse(view.toString().contains("22.234567"));
        assertPrivateTextNotInArtifacts("112.123456");
        assertPrivateTextNotInArtifacts("22.234567");
        controlledNow.set(now().plusSeconds(1));
        locate(now());
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_driver_location WHERE driver_id=?",
                        driver.driverId()));
    }

    @Test
    void locationRejectsWrongCoordinatesAccuracyTypesAndCoordinateSystems() {
        configured();
        for (Map.Entry<String, Object> bad :
                Map.<String, Object>of("lng", 181, "lat", -91, "accuracyMeter", -1).entrySet()) {
            Map<String, Object> body = locationBody(now());
            body.put(bad.getKey(), bad.getValue());
            requireStatus(driverCall("POST", LOCATION, body), 422);
        }
        Map<String, Object> wrong = locationBody(now());
        wrong.put("coordinateSystem", "WGS84");
        requireStatus(driverCall("POST", LOCATION, wrong), 400);
        wrong = locationBody(now());
        wrong.put("lng", "112.123456");
        requireStatus(driverCall("POST", LOCATION, wrong), 400);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_location"));
    }

    @Test
    void observationTimesAreStrictUtcMillisWithExplicitFiveSecondAndDayBounds() {
        configured();
        for (String value :
                List.of(
                        "2026-11-12T10:00:00+08:00",
                        "2026-11-12T10:00:00.1234Z",
                        "+10000-01-01T00:00:00Z")) {
            Map<String, Object> body = locationBody(now());
            body.put("capturedAt", value);
            requireStatus(driverCall("POST", LOCATION, body), 400);
        }
        requireStatus(driverCall("POST", LOCATION, locationBody(now().plusMillis(5001))), 422);
        requireStatus(
                driverCall(
                        "POST",
                        PRESENCE,
                        presenceBody("FOREGROUND", now().minus(Duration.ofDays(1)).minusMillis(1))),
                422);
        locate(now().plusSeconds(5));
        assertTrue(availability().path("locationFresh").asBoolean());
    }

    @Test
    void delayedOldPointCanBeRecordedButCannotBecomeFreshAtItsReceiptTime() {
        configured();
        locate(now().minusSeconds(121));
        JsonNode old = availability();
        assertEquals("STALE", old.path("locationStatus").asText());
        assertFalse(old.path("locationFresh").asBoolean());
        assertEquals(now().minusSeconds(1), Instant.parse(old.path("locationExpiresAt").asText()));
        assertEquals(now(), Instant.parse(old.path("locationReceivedAt").asText()));
        assertFalse(old.path("availableForNewOrder").asBoolean());
    }

    @Test
    void allowedFutureSkewCannotExtendFreshnessBeyondServerReceiptPlusTtl() {
        configured();
        locate(now().plusSeconds(5));
        JsonNode point = availability();
        assertEquals(
                now().plusSeconds(120), Instant.parse(point.path("locationExpiresAt").asText()));
        controlledNow.set(now().plusSeconds(120));
        assertFalse(availability().path("locationFresh").asBoolean());
    }

    @Test
    void missingOrPoorAccuracyIsSeparateFromFreshTimestampAndBlocksBaseCandidate() {
        configured();
        listening(true);
        contact("FOREGROUND", now());
        Map<String, Object> unknown = locationBody(now());
        unknown.put("accuracyMeter", null);
        data(driverCall("POST", LOCATION, unknown));
        JsonNode view = availability();
        assertTrue(view.path("locationFresh").asBoolean());
        assertFalse(view.path("locationAccuracyValid").asBoolean());
        assertEquals("UNKNOWN_ACCURACY", view.path("locationStatus").asText());
        assertFalse(view.path("availableForNewOrder").asBoolean());
        controlledNow.set(now().plusSeconds(1));
        Map<String, Object> poor = locationBody(now());
        poor.put("accuracyMeter", 101);
        data(driverCall("POST", LOCATION, poor));
        assertEquals("INACCURATE", availability().path("locationStatus").asText());
        assertFalse(availability().path("availableForNewOrder").asBoolean());
    }

    @Test
    void presenceAndLocationHaveSeparateDeadlinesAndNeitherCanEnableListening() {
        configured();
        locate(now());
        contact("BACKGROUND", now());
        JsonNode point = availability();
        assertFalse(point.path("listeningEnabled").asBoolean());
        assertTrue(point.path("locationFresh").asBoolean());
        assertTrue(point.path("clientReachable").asBoolean());
        controlledNow.set(now().plusSeconds(90));
        assertTrue(availability().path("locationFresh").asBoolean());
        assertFalse(availability().path("clientReachable").asBoolean());
    }

    @Test
    void pollingAvailabilityDoesNotRefreshReachabilityLocationOrTheirVersions() {
        readyCandidate();
        Map<String, Object> point = locationRow(), presence = presenceRow();
        controlledNow.set(now().plusSeconds(90));
        for (int index = 0; index < 3; index++) availability();
        assertDatabaseFact(point, locationRow());
        assertEquals(presence, presenceRow());
        assertFalse(availability().path("clientReachable").asBoolean());
    }

    @Test
    void locationUuidReplayDoesNotExtendFreshnessAndChangedBodyConflicts() {
        configured();
        Map<String, Object> body = locationBody(now());
        String uuid = key();
        data(driverCall("POST", LOCATION, body, uuid));
        Map<String, Object> persisted = locationRow();
        controlledNow.set(now().plusSeconds(121));
        data(driverCall("POST", LOCATION, body, uuid));
        assertDatabaseFact(persisted, locationRow());
        assertFalse(availability().path("locationFresh").asBoolean());
        Map<String, Object> changed = new LinkedHashMap<>(body);
        changed.put("lat", 23.234567);
        requireError(driverCall("POST", LOCATION, changed, uuid), 409, "VI_IDEMPOTENCY_CONFLICT");
        assertEquals(1, eventCount("DRIVER_LOCATION_REPORTED"));
    }

    @Test
    void presenceUuidReplayDoesNotRenewContactAndAnotherBodyConflicts() {
        configured();
        Map<String, Object> body = presenceBody("FOREGROUND", now());
        String uuid = key();
        data(driverCall("POST", PRESENCE, body, uuid));
        Map<String, Object> persisted = presenceRow();
        controlledNow.set(now().plusSeconds(91));
        data(driverCall("POST", PRESENCE, body, uuid));
        assertEquals(persisted, presenceRow());
        assertFalse(availability().path("clientReachable").asBoolean());
        Map<String, Object> changed = new LinkedHashMap<>(body);
        changed.put("clientState", "BACKGROUND");
        requireError(driverCall("POST", PRESENCE, changed, uuid), 409, "VI_IDEMPOTENCY_CONFLICT");
        assertEquals(1, eventCount("DRIVER_PRESENCE_REPORTED"));
    }

    @Test
    void oldAndEqualObservationTimesWithNewKeysCannotReplaceNewerFacts() {
        configured();
        locate(now());
        contact("FOREGROUND", now());
        for (Instant old : List.of(now(), now().minusSeconds(1))) {
            requireError(driverCall("POST", LOCATION, locationBody(old)), 409, "VI_STATE_CONFLICT");
            requireError(
                    driverCall("POST", PRESENCE, presenceBody("BACKGROUND", old)),
                    409,
                    "VI_STATE_CONFLICT");
        }
        assertEquals(1, availability().path("locationVersion").asLong());
        assertEquals(1, availability().path("presenceVersion").asLong());
    }

    @Test
    void theThreeCasCountersAreIndependentAndStaleLocationDoesNotAffectListening() {
        configured();
        Map<String, Object> opening = listeningBody(true);
        long profile = availability().path("profileVersion").asLong();
        locate(now());
        contact("FOREGROUND", now());
        data(driverCall("PATCH", LISTENING, opening));
        JsonNode view = availability();
        assertEquals(1, view.path("listeningVersion").asLong());
        assertEquals(1, view.path("locationVersion").asLong());
        assertEquals(1, view.path("presenceVersion").asLong());
        Map<String, Object> stale = locationBody(now().plusSeconds(1));
        stale.put("expectedVersion", 0);
        requireError(driverCall("POST", LOCATION, stale), 409, "VI_VERSION_CONFLICT");
        assertTrue(availability().path("listeningEnabled").asBoolean());
        assertTrue(availability().path("profileVersion").asLong() >= profile);
    }

    @Test
    void aNewSessionCannotReuseAnotherSessionsLocationOrPresence() {
        readyCandidate();
        Student later =
                switchToDriver(
                        new Student(
                                anotherSession(driver.accountId()),
                                driver.accountId(),
                                driver.driverId(),
                                driver.applicationId()));
        driver = later;
        JsonNode current = availability();
        assertTrue(current.path("listeningEnabled").asBoolean(), "听单是本人当日意愿，不因新登录自动重开或延长");
        assertEquals("CONTEXT_CHANGED", current.path("locationStatus").asText());
        assertEquals("CONTEXT_CHANGED", current.path("reachabilityStatus").asText());
        assertFalse(current.path("availableForNewOrder").asBoolean());
        controlledNow.set(now().plusSeconds(1));
        locate(now());
        contact("FOREGROUND", now());
        assertTrue(availability().path("availableForNewOrder").asBoolean());
    }

    @Test
    void staleSessionContextAndCompanyTargetsCannotBeWrittenOrReplayedIntoANewContext() {
        configured();
        Map<String, Object> old = presenceBody("FOREGROUND", now());
        String uuid = key();
        data(driverCall("POST", PRESENCE, old, uuid));
        driver =
                switchToDriver(
                        new Student(
                                anotherSession(driver.accountId()),
                                driver.accountId(),
                                driver.driverId(),
                                driver.applicationId()));
        requireStatus(driverCall("POST", PRESENCE, old, uuid), 409);
        Map<String, Object> wrongCompany = locationBody(now());
        wrongCompany.put("expectedCompanyRevision", 999);
        requireStatus(driverCall("POST", LOCATION, wrongCompany), 409);
        Map<String, Object> wrongContext = locationBody(now());
        wrongContext.put("expectedContextVersion", 999);
        requireStatus(driverCall("POST", LOCATION, wrongContext), 409);
    }

    @Test
    void companyRoundTripCannotReviveOldListeningOrObservationFacts() {
        readyCandidate();
        assignCompany(driver, COMPANY_B);
        assignCompany(driver, COMPANY_A);
        JsonNode view = availability();
        assertFalse(view.path("listeningEnabled").asBoolean());
        assertEquals("CONTEXT_CHANGED", view.path("locationStatus").asText());
        assertEquals("CONTEXT_CHANGED", view.path("reachabilityStatus").asText());
        assertFalse(view.path("availableForNewOrder").asBoolean());
    }

    @Test
    void locationOutboxFailureRollsBackEncryptedPointAndItsVersion() {
        configured();
        failEvent("DRIVER_LOCATION_REPORTED");
        requireStatus(driverCall("POST", LOCATION, locationBody(now())), 500);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_location"));
        assertEquals(0, availability().path("locationVersion").asLong());
        assertEquals(0, eventCount("DRIVER_LOCATION_REPORTED"));
    }

    @Test
    void presenceOutboxFailureCannotLeaveReachabilityAlive() {
        configured();
        failEvent("DRIVER_PRESENCE_REPORTED");
        requireStatus(driverCall("POST", PRESENCE, presenceBody("BACKGROUND", now())), 500);
        assertEquals(0, countRows("SELECT COUNT(*) FROM vi_driver_presence"));
        assertFalse(availability().path("clientReachable").asBoolean());
        assertEquals(0, eventCount("DRIVER_PRESENCE_REPORTED"));
    }

    @Test
    void revokedAdminAndMiniSessionRemainRevokedWhenRedisEvictionFails() {
        configured();
        String staff = scoped(COMPANY_A, CONFIG_QUERY, CONFIG_EDIT);
        doThrow(new IllegalStateException("隔离Redis撤销失败")).when(redis).deleteObject(anyString());
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                administratorId(staff));
        requireStatus(call("PUT", configPath(COMPANY_A), configBody(1, "17:00"), staff), 401);
        jdbc.update(
                "UPDATE vi_session SET status='REVOKED' WHERE id=?",
                miniTokens.resolveAccess(driver.token()).sessionId());
        requireStatus(driverCall("GET", AVAILABILITY, null), 401);
        assertEquals("18:00", companyField("closing_time"));
    }

    @Test
    void successfulAndRejectedOperationsKeepRealAuditWithoutCoordinateOrReasonPlaintext() {
        String note = "公司真实操作原因仅限加密留存D01";
        Map<String, Object> body = configBody(0, "18:00");
        body.put("reason", note);
        data(call("PUT", configPath(COMPANY_A), body, rootAdmin));
        String summary =
                jdbc.queryForObject(
                        "SELECT after_summary FROM vi_event_log WHERE"
                                + " action='COMPANY_LISTENING_CONFIG_CHANGED'",
                        String.class);
        byte[] encryptedReason =
                Base64.getDecoder().decode(ViJson.tree(summary).path("reasonEnvelope").asText());
        String aad = "D01:COMPANY:" + COMPANY_A + ":CONFIG:1:ADMIN:" + administratorId(rootAdmin);
        assertEquals(note, dispatchCrypto.unenvelope(encryptedReason, aad));
        assertThrows(
                RuntimeException.class,
                () -> dispatchCrypto.unenvelope(encryptedReason, aad + ":OTHER"));
        listening(true);
        locate(now());
        contact("FOREGROUND", now());
        requireStatus(driverCall("POST", LOCATION, locationBody(now())), 409);
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='DRIVER_LOCATION_REPORTED'"
                                + " AND actor_realm='MINI' AND actor_account_id=?",
                        driver.accountId()));
        assertEquals(
                1,
                countRows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE"
                            + " action='DRIVER_LOCATION_REPORTED_DENIED' AND actor_realm='MINI' AND"
                            + " actor_account_id=?",
                        driver.accountId()));
        assertPrivateTextNotInArtifacts(note);
        assertPrivateTextNotInArtifacts("112.123456");
    }

    @Test
    void concurrentLocationWritesWithTheSameCasHaveOneWinner() throws Exception {
        configured();
        Map<String, Object> one = locationBody(now()), two = new LinkedHashMap<>(one);
        two.put("lat", 23.234567);
        List<ResponseEntity<String>> replies =
                race(
                        () -> driverCall("POST", LOCATION, one),
                        () -> driverCall("POST", LOCATION, two));
        assertEquals(List.of(200, 409), statuses(replies));
        assertEquals(1, availability().path("locationVersion").asLong());
        assertEquals(1, countRows("SELECT COUNT(*) FROM vi_driver_location"));
    }

    @Test
    void activeUnexpiredPenaltyIsIndependentOfListeningLocationAndExpiredPenaltyHistory() {
        readyCandidate();
        long id = penalty(now().plusSeconds(3600), "ACTIVE");
        JsonNode blocked = availability();
        assertTrue(blocked.path("listeningEnabled").asBoolean());
        assertTrue(blocked.path("locationFresh").asBoolean());
        assertFalse(blocked.path("availableForNewOrder").asBoolean());
        assertTrue(blocked.path("activePenaltyIds").toString().contains(Long.toString(id)));
        jdbc.update("UPDATE vi_penalty SET status='REVOKED' WHERE id=?", id);
        assertTrue(availability().path("activePenaltyIds").isEmpty());
        assertTrue(availability().path("availableForNewOrder").asBoolean());
    }

    @Test
    void sixActualResponsesMatchPrimaryAndD01PacketContracts() throws Exception {
        // 仅建立归还历史消费者边界，不冒称本例已执行H系列实物交接；累计不向后台开放历史明细。
        assignmentHistory(driver.driverId(), COMPANY_A, "RETURNED", true);
        assignmentHistory(driver.driverId(), COMPANY_B, "RETURNED", true);
        assignmentHistory(driver.driverId(), COMPANY_A, "CANCELLED", true);
        assignmentHistory(student(COMPANY_B, false).driverId(), COMPANY_B, "RETURNED", true);
        assignmentHistory(driver.driverId(), COMPANY_A, "RETURNED", false);
        JsonNode before = data(call("GET", configPath(COMPANY_A), null, rootAdmin));
        ResponseEntity<String> configured =
                call(
                        "PUT",
                        configPath(COMPANY_A),
                        configBody(before.path("version").asLong(), "18:00"),
                        rootAdmin);
        data(configured);
        ResponseEntity<String> opened = driverCall("PATCH", LISTENING, listeningBody(true));
        data(opened);
        ResponseEntity<String> point = driverCall("POST", LOCATION, locationBody(now()));
        data(point);
        ResponseEntity<String> contact =
                driverCall("POST", PRESENCE, presenceBody("FOREGROUND", now()));
        data(contact);
        ResponseEntity<String> available = driverCall("GET", AVAILABILITY, null);
        assertEquals(2, data(available).path("completedAssignmentCount").asLong());
        ResponseEntity<String> config = call("GET", configPath(COMPANY_A), null, rootAdmin);
        assertEquals("no-store", available.getHeaders().getCacheControl());
        assertEquals("no-store", config.getHeaders().getCacheControl());
        Path directory = Files.createTempDirectory("d01-real-contract-");
        try {
            for (var entry :
                    Map.of(
                                    "availability",
                                    available,
                                    "listening",
                                    opened,
                                    "location",
                                    point,
                                    "presence",
                                    contact,
                                    "config-get",
                                    config,
                                    "config-put",
                                    configured)
                            .entrySet()) {
                Files.writeString(
                        directory.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
            }
            Path repo = Path.of("").toAbsolutePath();
            while (!Files.exists(repo.resolve("docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml")))
                repo = repo.getParent();
            String python = System.getenv().getOrDefault("VI_PYTHON", "python3");
            Process process =
                    new ProcessBuilder(
                                    python,
                                    repo.resolve(
                                                    "xinyi-admin/src/test/resources/integration/d01_contract.py")
                                            .toString(),
                                    repo.toString(),
                                    directory.toString())
                            .redirectErrorStream(true)
                            .start();
            String output =
                    new String(
                            process.getInputStream().readAllBytes(),
                            java.nio.charset.StandardCharsets.UTF_8);
            assertEquals(0, process.waitFor(), output);
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                    Files.deleteIfExists(path);
            }
        }
    }

    private String method(String path) {
        return path.equals(AVAILABILITY) ? "GET" : path.equals(LISTENING) ? "PATCH" : "POST";
    }

    private int eventCount(String type) {
        return countRows("SELECT COUNT(*) FROM vi_outbox WHERE event_type=?", type);
    }

    private Object companyField(String column) {
        if (!Set.of("closing_time", "version").contains(column))
            throw new IllegalArgumentException();
        return jdbc.queryForObject(
                "SELECT " + column + " FROM vi_driver_listening_config WHERE owner_dept_id=?",
                Object.class,
                COMPANY_A);
    }

    private Map<String, Object> locationRow() {
        return jdbc.queryForMap(
                "SELECT * FROM vi_driver_location WHERE driver_id=?", driver.driverId());
    }

    private Map<String, Object> presenceRow() {
        return jdbc.queryForMap(
                "SELECT * FROM vi_driver_presence WHERE driver_id=?", driver.driverId());
    }

    private Map<String, Object> heldFacts(long order) {
        return jdbc.queryForMap(
                "SELECT"
                    + " o.fulfillment_status,o.custody_type,o.custody_ref_id,o.active_assignment_id,a.id"
                    + " assignment_id,a.ended_at,c.holder_type,c.holder_id FROM vi_order o JOIN"
                    + " vi_driver_assignment a ON a.id=o.active_assignment_id JOIN"
                    + " vi_driver_occupancy c ON c.order_id=o.id WHERE o.id=?",
                order);
    }

    private void failEvent(String type) {
        doThrow(new IllegalStateException("隔离D01 outbox故障"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), eq(type), any());
    }

    private Instant databaseDeadline() {
        // DATETIME存的是UTC墙钟；直接取列文本，避免测试JDBC连接/宿主默认时区再次偏移8小时。
        return Instant.parse(
                jdbc.queryForObject(
                        "SELECT DATE_FORMAT(listening_until,'%Y-%m-%dT%H:%i:%s.%fZ') FROM"
                                + " vi_driver_profile WHERE id=?",
                        String.class, driver.driverId()));
    }

    private void assertDatabaseFact(Map<String, Object> before, Map<String, Object> after) {
        assertEquals(before.keySet(), after.keySet());
        for (String field : before.keySet()) {
            Object value = before.get(field);
            if (value instanceof byte[] bytes)
                assertArrayEquals(bytes, (byte[]) after.get(field), field);
            else assertEquals(value, after.get(field), field);
        }
    }

    private long penalty(Instant until, String state) {
        long assignment = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_penalty(driver_id,assignment_id,business_date,penalty_type,status,reason,rule_version,effective_until)"
                    + " VALUES (?,?,?,'STOP_TODAY',?,'明确历史处罚消费者事实','D01-test',?)",
                driver.driverId(),
                assignment,
                now().atZone(SHANGHAI).toLocalDate(),
                state,
                LocalDateTime.ofInstant(until, ZoneOffset.UTC));
        return jdbc.queryForObject(
                "SELECT id FROM vi_penalty WHERE assignment_id=?", Long.class, assignment);
    }

    private void assignmentHistory(long driverId, long company, String endReason, boolean ended) {
        String orderNo = "D01-HISTORY-" + key();
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version)"
                    + " VALUES"
                    + " (?,?,?,?,?,'{}',?,'OWNER',?,UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 HOUR),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'D01-history')",
                orderNo,
                driver.accountId(),
                SEQ.incrementAndGet(),
                company,
                SEQ.incrementAndGet(),
                ended ? "COMPLETED" : "WAIT_CONFIRM",
                driver.accountId());
        long order =
                jdbc.queryForObject(
                        "SELECT id FROM vi_order WHERE order_no=?", Long.class, orderNo);
        jdbc.update(
                "INSERT INTO"
                    + " vi_driver_assignment(order_id,driver_id,invite_id,accepted_at,ended_at,end_reason)"
                    + " VALUES (?,?,?,UTC_TIMESTAMP(3),CASE WHEN ? THEN UTC_TIMESTAMP(3) ELSE NULL"
                    + " END,?)",
                order,
                driverId,
                SEQ.incrementAndGet(),
                ended,
                endReason);
    }

    private List<Integer> statuses(List<ResponseEntity<String>> values) {
        return values.stream().map(r -> r.getStatusCode().value()).sorted().toList();
    }

    private List<ResponseEntity<String>> race(
            Callable<ResponseEntity<String>> first, Callable<ResponseEntity<String>> second)
            throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            List<Future<ResponseEntity<String>>> futures = new ArrayList<>();
            for (Callable<ResponseEntity<String>> work : List.of(first, second))
                futures.add(
                        pool.submit(
                                () -> {
                                    ready.countDown();
                                    assertTrue(start.await(10, TimeUnit.SECONDS));
                                    return work.call();
                                }));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return List.of(
                    futures.get(0).get(30, TimeUnit.SECONDS),
                    futures.get(1).get(30, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }
}
