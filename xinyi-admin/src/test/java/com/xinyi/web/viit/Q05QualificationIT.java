package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.driver.service.DriverNewAssignmentPolicy;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.qualification.service.QualificationExpiryJob;
import com.xinyi.rongli.qualification.service.QualificationSnapshotAuthority;
import com.xinyi.rongli.qualification.service.QualificationSubmissionAuthority;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

/** Q05真实HTTP/MySQL审核、认证快照及真实到期任务；不替换鉴权/审核/资格Service。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.crypto.data-key=q05-isolated-phone-key",
            "vi.outbox-relay-enabled=false",
            "vi.jobs.qualification-enabled=true",
            "vi.jobs.qualification-delay-ms=3600000",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class Q05QualificationIT extends QualificationAuditHttpFixture {
    @Autowired IntegrationCrypto crypto;
    @Autowired QualificationSubmissionAuthority submissions;
    @Autowired QualificationSnapshotAuthority snapshots;
    @Autowired QualificationExpiryJob expiryJob;
    @Autowired DriverNewAssignmentPolicy newAssignments;
    @Autowired DriverMapper driverMapper;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;

    @Test
    void adminAndMiniRealmsCannotBeReplacedByClientHeaders() {
        Submitted item = submitted();
        for (String path : List.of(AUDITS, auditPath(item))) {
            requireStatus(rawTraining("GET", path, null, null, key(), "admin"), 401);
            requireStatus(
                    rawTraining("GET", path, null, item.person().token(), key(), "admin"), 401);
        }
        requireStatus(
                rawTraining(
                        "POST",
                        auditPath(item) + "/audit",
                        approvalBody(adminDetail(item)),
                        item.person().token(),
                        key(),
                        "admin"),
                401);
        requireStatus(rawTraining("GET", AUDITS, null, rootAdmin, key(), "owner"), 400);
        assertEquals(0, reviewCount());
    }

    @Test
    void listAndDetailRespectTheRealFrozenCompanyAndAccurateTotal() {
        Submitted a = submitted(COMPANY_A), b = submitted(COMPANY_B);
        String staff = scoped(COMPANY_A, QLIST, QQUERY);
        JsonNode list = list(AUDITS, staff);
        assertEquals(1, list.path("rows").size());
        assertEquals(1, list.path("total").asInt());
        assertEquals(
                Long.toString(a.person().applicationId()),
                list.path("rows").get(0).path("applicationId").asText());
        requireStatus(call("GET", auditPath(b), null, staff), 404);
        assertEquals(1, list(AUDITS + "?ownerDeptId=" + COMPANY_A, staff).path("total").asInt());
        assertEquals(0, list(AUDITS + "?ownerDeptId=" + COMPANY_B, staff).path("total").asInt());
    }

    @Test
    void functionPermissionsCannotBeInferredFromCompanyMembership() {
        Submitted item = submitted();
        String listed = scoped(COMPANY_A, QLIST), queried = scoped(COMPANY_A, QQUERY);
        requireStatus(call("GET", auditPath(item), null, listed), 403);
        requireStatus(call("GET", AUDITS, null, queried), 403);
        requireStatus(audit(item, approvalBody(adminDetail(item)), queried), 403);
        requireStatus(audit(item, approvalBody(adminDetail(item)), scoped(COMPANY_A, QAUDIT)), 403);
        assertEquals(0, reviewCount());
    }

    @Test
    void emptyDepartmentScopeFailsClosedForReadsAndAudit() {
        Submitted item = submitted();
        String staff = scoped(COMPANY_A, QLIST, QQUERY, QAUDIT);
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", administratorId(staff));
        JsonNode list = list(AUDITS, staff);
        assertEquals(0, list.path("rows").size());
        assertEquals(0, list.path("total").asInt());
        requireStatus(call("GET", auditPath(item), null, staff), 404);
        requireStatus(audit(item, approvalBody(adminDetail(item)), staff), 404);
        assertEquals(0, reviewCount());
    }

    @Test
    void multiDepartmentAuditorMustCoverOriginalAndCurrentCompanyWithoutGlobalScope() {
        Submitted item = submitted();
        String staff = scoped(COMPANY_A, QLIST, QQUERY, QAUDIT);
        jdbc.update(
                "INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,?)",
                administratorId(staff),
                COMPANY_B);
        assignCompany(item.person(), COMPANY_B);
        JsonNode detail = data(call("GET", auditPath(item), null, staff));
        assertEquals(Long.toString(COMPANY_A), detail.path("ownerDeptId").asText());
        assertEquals(Long.toString(COMPANY_B), detail.path("currentOwnerDeptId").asText());
        JsonNode rejected =
                data(
                        audit(
                                item,
                                auditBody(detail, "REJECT", "转公司后重新申请", List.of("realName"), null),
                                staff));
        assertEquals("REJECTED", rejected.path("status").asText());
        assertEquals(1, reviewCount());
    }

    @Test
    void summaryAndDetailNeverExposeRawIdentityInternalCipherOrDeviceSecrets() {
        Submitted item = submitted();
        JsonNode summary = list(AUDITS, rootAdmin).path("rows").get(0), detail = adminDetail(item);
        assertFalse(summary.has("draft"));
        assertFalse(summary.has("checks"));
        for (JsonNode view : List.of(summary, detail)) {
            String text = view.toString();
            for (String secret :
                    List.of(
                            "110105199001010010",
                            "submissionSeal",
                            "snapshotSeal",
                            "internalOpinionCipher",
                            "openid",
                            "resultJSONSignature",
                            "challenge")) assertFalse(text.contains(secret), secret);
        }
        assertTrue(detail.path("draft").path("idCardNoProvided").asBoolean());
        assertEquals(4, detail.path("checks").size());
        assertTrue(detail.path("hasTrustedSubmission").asBoolean());
        assertTrue(detail.path("reviews").isEmpty());
    }

    @Test
    void queryFiltersSortingAndPaginationHaveNoAuthorityBypass() {
        Submitted a = submitted(), b = submitted();
        JsonNode byId = list(AUDITS + "?keyword=" + a.person().applicationId(), rootAdmin);
        assertEquals(1, byId.path("total").asInt());
        assertEquals(
                Long.toString(a.person().applicationId()),
                byId.path("rows").get(0).path("applicationId").asText());
        JsonNode sorted = list(AUDITS + "?sort=idDesc&pageSize=1&pageNum=1", rootAdmin);
        assertEquals(2, sorted.path("total").asInt());
        assertEquals(1, sorted.path("rows").size());
        assertEquals(
                Long.toString(b.person().applicationId()),
                sorted.path("rows").get(0).path("applicationId").asText());
        assertEquals(0, list(AUDITS + "?status=APPROVED", rootAdmin).path("total").asInt());
        assertEquals(0, reviewCount());
    }

    @Test
    void internalQueriesInvalidIdsAndUnboundedPaginationAreRejected() {
        Submitted item = submitted();
        for (String query :
                List.of(
                        "params.dataScope=1=1",
                        "searchDeptIds=1",
                        "visibleDeptIds=1",
                        "sort=sql",
                        "pageNum=0",
                        "pageSize=101",
                        "status=PASSED",
                        "ownerDeptId=01"))
            requireStatus(call("GET", AUDITS + "?" + query, null, rootAdmin), 400);
        requireStatus(call("GET", auditPath(item) + "?fields=all", null, rootAdmin), 400);
        requireStatus(call("GET", AUDITS + "/01", null, rootAdmin), 400);
        requireStatus(call("GET", AUDITS + "/999999999999999999", null, rootAdmin), 404);
    }

    @Test
    void nineKeysAreExactAndAllInternalUpdatesAreForbidden() {
        Submitted item = submitted();
        Map<String, Object> body = approvalBody(adminDetail(item));
        assertEquals(9, body.size());
        for (String field :
                List.of(
                        "status",
                        "driverId",
                        "ownerDeptId",
                        "licenseClass",
                        "newAssignmentEnabled",
                        "listeningEnabled",
                        "providerResultIds",
                        "submissionSeal",
                        "params")) {
            Map<String, Object> bad = new LinkedHashMap<>(body);
            bad.put(field, "forged");
            requireStatus(audit(item, bad, rootAdmin), 400);
        }
        for (String field : body.keySet()) {
            Map<String, Object> missing = new LinkedHashMap<>(body);
            missing.remove(field);
            requireStatus(audit(item, missing, rootAdmin), 400);
        }
        assertEquals(0, reviewCount());
    }

    @Test
    void typedVersionsAndUuidMustBeCanonicalAndSafe() {
        Submitted item = submitted();
        Map<String, Object> base = approvalBody(adminDetail(item));
        for (String field :
                List.of(
                        "expectedVersion",
                        "expectedProfileVersion",
                        "expectedSubmissionRevision",
                        "expectedCompanyRevision")) {
            for (Object invalid : List.of("1", -1, 1.5, 9007199254740992L)) {
                Map<String, Object> bad = new LinkedHashMap<>(base);
                bad.put(field, invalid);
                requireStatus(audit(item, bad, rootAdmin), 400);
            }
        }
        requireStatus(auditKey(item, base, rootAdmin, "invalid-key"), 400);
        requireStatus(
                rawTraining("POST", auditPath(item) + "/audit", base, rootAdmin, null, "admin"),
                400);
        assertEquals(0, reviewCount());
    }

    @Test
    void approvalMustSpecifyFutureUtcDeadlineAndCannotInventRejectFields() {
        Submitted item = submitted();
        Map<String, Object> base = approvalBody(adminDetail(item));
        for (Object due :
                Arrays.asList(
                        null,
                        Instant.now().minusSeconds(1).truncatedTo(ChronoUnit.MILLIS).toString())) {
            Map<String, Object> bad = new LinkedHashMap<>(base);
            bad.put("reviewDueAt", due);
            requireError(audit(item, bad, rootAdmin), 422, "VI_DATA_INVALID");
        }
        Map<String, Object> offset = new LinkedHashMap<>(base);
        offset.put("reviewDueAt", "2099-12-31T12:00:00+08:00");
        requireStatus(audit(item, offset, rootAdmin), 400);
        for (String invalid :
                List.of(
                        "+10000-01-01T00:00:00Z",
                        "2099-12-31T12:00:00.0001Z",
                        "9999-12-31T23:59:59.9999Z")) {
            Map<String, Object> bad = new LinkedHashMap<>(base);
            bad.put("reviewDueAt", invalid);
            requireStatus(audit(item, bad, rootAdmin), 400);
        }
        Map<String, Object> rejected = new LinkedHashMap<>(base);
        rejected.put("rejectFields", List.of("realName"));
        requireStatus(audit(item, rejected, rootAdmin), 422);
        assertEquals(0, reviewCount());
    }

    @Test
    void rejectionNeedsPublicOpinionAndExactUniqueMaterialFields() {
        Submitted item = submitted();
        JsonNode detail = adminDetail(item);
        for (List<String> fields :
                List.of(
                        List.<String>of(),
                        List.of("providerResultIds"),
                        List.of("realName", "realName"))) {
            Map<String, Object> bad = auditBody(detail, "REJECT", "请修正资料", fields, null);
            requireStatus(audit(item, bad, rootAdmin), fields.isEmpty() ? 422 : 400);
        }
        for (String opinion : Arrays.asList(null, ""))
            requireStatus(
                    audit(
                            item,
                            auditBody(detail, "REJECT", opinion, List.of("realName"), null),
                            rootAdmin),
                    422);
        assertEquals(0, reviewCount());
    }

    @Test
    void eachIndependentCasConflictKeepsOriginalSubmittedRecord() {
        Submitted item = submitted();
        Map<String, Object> base = approvalBody(adminDetail(item));
        for (String field :
                List.of(
                        "expectedVersion",
                        "expectedProfileVersion",
                        "expectedSubmissionRevision",
                        "expectedCompanyRevision")) {
            Map<String, Object> bad = new LinkedHashMap<>(base);
            bad.put(field, 999);
            requireError(audit(item, bad, rootAdmin), 409, "VI_VERSION_CONFLICT");
        }
        assertEquals("SUBMITTED", adminDetail(item).path("status").asText());
        assertEquals(0, reviewCount());
        assertEquals(0, snapshotCount());
    }

    @Test
    void approvalCreatesAuthenticatedEffectiveSnapshotWithoutSwitchingOrEnablingListening() {
        Submitted item = submitted();
        long before = permissionVersion(item.person());
        JsonNode result = approve(item);
        assertEquals("APPROVED", result.path("status").asText());
        assertEquals(1, result.path("qualificationRevision").asLong());
        assertEquals(1, reviewCount());
        assertEquals(1, snapshotCount());
        assertNotNull(snapshots.authenticated(currentSnapshotId(item)));
        assertNotNull(submissions.authenticated(item.person().applicationId(), 1));
        assertEquals(before + 1, permissionVersion(item.person()));
        Map<String, Object> row = profile(item);
        assertEquals("APPROVED", row.get("qualification_status"));
        assertEquals(0, ((Number) row.get("new_assignment_enabled")).intValue());
        assertEquals(0, ((Number) row.get("listening_enabled")).intValue());
        JsonNode session =
                data(miniCall("GET", "/api/common/session", null, item.person().token()));
        assertEquals("OWNER", session.path("activeRole").asText());
        assertTrue(session.path("availableRoles").toString().contains("DRIVER"));
        assertEquals(0, WECHAT.verifyCalls.get());
    }

    @Test
    void completeRealDeviceManualTrainingSubmissionAuditAndExplicitRoleSwitchWorkTogether()
            throws Exception {
        Submitted item = fullyExecutedSubmission();
        assertEquals(1, WECHAT.verifyCalls.get());
        JsonNode result = approve(item);
        assertEquals("APPROVED", result.path("status").asText());
        JsonNode owner = data(miniCall("GET", "/api/common/session", null, item.person().token()));
        assertEquals("OWNER", owner.path("activeRole").asText());
        assertTrue(owner.path("availableRoles").toString().contains("DRIVER"));
        JsonNode switched =
                data(
                        miniCall(
                                "POST",
                                "/api/common/session/role",
                                Map.of(
                                        "role",
                                        "DRIVER",
                                        "expectedContextVersion",
                                        owner.path("contextVersion").asLong()),
                                item.person().token()));
        assertEquals("DRIVER", switched.path("activeRole").asText());
        assertFalse(switched.path("token").asText().isBlank());
        assertEquals(
                item.person().accountId(),
                miniTokens.resolveAccess(switched.path("token").asText()).accountId());
        assertEquals(
                4,
                ViJson.tree(
                                jdbc.queryForObject(
                                        "SELECT provider_result_ids FROM"
                                                + " vi_driver_application_submission WHERE"
                                                + " application_id=?",
                                        String.class,
                                        item.person().applicationId()))
                        .size());
        assertEquals(1, snapshotCount());
        assertNull(WECHAT.failure.get());
    }

    @Test
    void submittedProofRemainsAuditableAfterOriginalMiniLogoutOrAnotherLogin() {
        Submitted item = submitted();
        long session = miniTokens.resolveAccess(item.person().token()).sessionId();
        data(
                miniCall(
                        "POST",
                        "/api/common/session/logout",
                        Map.of("allDevices", false),
                        item.person().token()));
        String later = anotherSession(item.person().accountId());
        assertNotEquals(session, miniTokens.resolveAccess(later).sessionId());
        JsonNode pending = data(miniCall("GET", QUALIFICATION, null, later));
        assertEquals("SUBMITTED", pending.path("status").asText());
        for (JsonNode check : pending.path("checks"))
            assertEquals("PASSED", check.path("status").asText(), "提交后只展示原冻结事实，不要求MINI旧session在线");
        JsonNode result = approve(item);
        assertEquals("APPROVED", result.path("status").asText());
        assertEquals(
                "REVOKED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_session WHERE id=?", String.class, session));
        assertTrue(
                data(miniCall("GET", "/api/common/session", null, later))
                        .path("availableRoles")
                        .toString()
                        .contains("DRIVER"));
        JsonNode approved = data(miniCall("GET", QUALIFICATION, null, later));
        assertEquals("APPROVED", approved.path("status").asText());
        for (JsonNode check : approved.path("checks"))
            assertEquals("PASSED", check.path("status").asText());
    }

    @Test
    void expiredOriginalDeviceChallengeDoesNotInvalidateAlreadyFrozenSuccessfulProof() {
        Submitted item = submitted();
        jdbc.update(
                "UPDATE vi_face_session SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 SECOND)"
                        + " WHERE application_id=?",
                item.person().applicationId());
        assertEquals("APPROVED", approve(item).path("status").asText());
        assertEquals(1, snapshotCount());
    }

    @Test
    void missingLegacySubmissionSealCannotBeApprovedButCanBeExplicitlyRejected() {
        Submitted item = submitted();
        jdbc.update(
                "UPDATE vi_driver_application_submission SET submission_seal=NULL WHERE"
                        + " application_id=?",
                item.person().applicationId());
        JsonNode detail = adminDetail(item);
        assertFalse(detail.path("hasTrustedSubmission").asBoolean());
        requireStatus(audit(item, approvalBody(detail), rootAdmin), 409);
        assertEquals(0, snapshotCount());
        JsonNode rejected =
                data(
                        audit(
                                item,
                                auditBody(
                                        adminDetail(item),
                                        "REJECT",
                                        "原提交缺少可信来源，请重新提交",
                                        List.of("realName"),
                                        null),
                                rootAdmin));
        assertEquals("REJECTED", rejected.path("status").asText());
        assertEquals(1, reviewCount());
    }

    @Test
    void changingAnySealedSubmissionFactFailsClosedWithoutAnApproval() {
        Submitted item = submitted();
        Map<String, Object> input = approvalBody(adminDetail(item));
        jdbc.update(
                "UPDATE vi_driver_application_submission SET consent_text='被替换的正文' WHERE"
                        + " application_id=?",
                item.person().applicationId());
        requireError(audit(item, input, rootAdmin), 409, "VI_STATE_CONFLICT");
        assertEquals(0, reviewCount());
        assertEquals(0, snapshotCount());
    }

    @Test
    void frozenProofSourceTamperingCannotBeReplacedByAnIndependentPassedRow() {
        Submitted item = submitted();
        jdbc.update(
                "UPDATE vi_manual_verification SET status='RETURNED' WHERE application_id=? AND"
                        + " verification_type='BACKGROUND'",
                item.person().applicationId());
        Map<String, Object> material =
                jdbc.queryForMap(
                        "SELECT material_revision,material_hash FROM vi_driver_application WHERE"
                                + " id=?",
                        item.person().applicationId());
        jdbc.update(
                "INSERT INTO"
                    + " vi_provider_verification(application_id,material_revision,material_hash,provider,task_type,provider_task_id,status,verified_at)"
                    + " VALUES (?,?,?,'UNTRUSTED','BACKGROUND',?,'PASSED',UTC_TIMESTAMP(3))",
                item.person().applicationId(),
                material.get("material_revision"),
                material.get("material_hash"),
                key());
        requireStatus(audit(item, approvalBody(adminDetail(item)), rootAdmin), 409);
        assertEquals(0, snapshotCount());
    }

    @Test
    void ordinaryAdministrativeRemarkChangesCasButDoesNotInvalidateSubmittedMaterials() {
        Submitted item = submitted();
        JsonNode stale = adminDetail(item);
        data(
                call(
                        "PATCH",
                        "/api/admin/drivers/" + item.person().driverId(),
                        Map.of(
                                "expectedVersion",
                                profileVersion(item.person()),
                                "reason",
                                "补充运营备注",
                                "internalRemark",
                                "不改变材料及公司"),
                        rootAdmin));
        requireError(audit(item, approvalBody(stale), rootAdmin), 409, "VI_VERSION_CONFLICT");
        assertEquals("APPROVED", approve(item).path("status").asText());
        assertEquals(1, snapshotCount());
    }

    @Test
    void companyTransferAndRoundTripPreventApprovalOfTheOldFrozenCompanyEpoch() {
        Submitted item = submitted();
        assignCompany(item.person(), COMPANY_B);
        requireStatus(audit(item, approvalBody(adminDetail(item)), rootAdmin), 409);
        assignCompany(item.person(), COMPANY_A);
        requireStatus(audit(item, approvalBody(adminDetail(item)), rootAdmin), 409);
        assertEquals(0, snapshotCount());
        assertEquals("SUBMITTED", adminDetail(item).path("status").asText());
    }

    @Test
    void transferredSubmissionReadBelongsToOriginalCompanyAndRejectionNeedsBothScopes() {
        Submitted item = submitted();
        String old = scoped(COMPANY_A, QLIST, QQUERY, QAUDIT),
                current = scoped(COMPANY_B, QLIST, QQUERY, QAUDIT);
        assignCompany(item.person(), COMPANY_B);
        JsonNode original = data(call("GET", auditPath(item), null, old));
        assertEquals(Long.toString(COMPANY_A), original.path("ownerDeptId").asText());
        assertTrue(original.path("currentOwnerDeptId").isNull(), "原公司有历史读权但不能泄露无权当前公司字段");
        assertEquals(
                Long.toString(COMPANY_B), adminDetail(item).path("currentOwnerDeptId").asText());
        requireStatus(call("GET", auditPath(item), null, current), 404);
        Map<String, Object> reject =
                auditBody(adminDetail(item), "REJECT", "公司已转移，请按当前公司重提", List.of("realName"), null);
        requireStatus(audit(item, reject, old), 404);
        requireStatus(audit(item, reject, current), 404);
        assertEquals("REJECTED", data(audit(item, reject, rootAdmin)).path("status").asText());

        // 同申请补正后按当前公司重新提交，B的申请读权不能顺带继承A审核事实的读权。
        String firstReview = adminDetail(item).path("reviews").get(0).path("reviewId").asText();
        saveMaterials(item.person(), Map.of("realName", "按当前公司重新提交的申请人"));
        Submitted resubmitted = freezeWithUpstreamBoundary(item.person());
        assertEquals(2, resubmitted.application().path("submissionRevision").asLong());
        JsonNode newCompany = data(call("GET", auditPath(resubmitted), null, current));
        assertEquals(Long.toString(COMPANY_B), newCompany.path("ownerDeptId").asText());
        assertTrue(newCompany.path("reviews").isEmpty(), newCompany.toString());
        assertTrue(newCompany.path("publicReviewOpinion").isNull());
        assertEquals(
                List.of(), ViJson.read(newCompany.path("rejectFields").toString(), List.class));
        requireStatus(call("GET", auditPath(resubmitted), null, old), 404);
        jdbc.update(
                "INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,?)",
                administratorId(current),
                COMPANY_A);
        JsonNode both = data(call("GET", auditPath(resubmitted), null, current));
        assertEquals(1, both.path("reviews").size());
        assertEquals(firstReview, both.path("reviews").get(0).path("reviewId").asText());
        assertEquals("公司已转移，请按当前公司重提", both.path("reviews").get(0).path("publicOpinion").asText());
        assertEquals(1, reviewCount(), "按范围过滤只影响读取，不删除历史审核事实");
    }

    @Test
    void applicantHistoricEmployeeBindingCannotBeUsedToApproveTheirOwnSubmission() {
        Student person = student(COMPANY_A, true);
        String self = scoped(COMPANY_A, QQUERY, QAUDIT);
        bindApplicant(person, administratorId(self));
        Submitted item = freezeWithUpstreamBoundary(person);
        jdbc.update(
                "UPDATE vi_employee_binding SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE"
                        + " account_id=?",
                person.accountId());
        requireError(
                audit(item, approvalBody(adminDetail(item)), self), 403, "VI_SELF_REVIEW_DENIED");
        assertEquals(0, reviewCount());
        assertEquals(0, snapshotCount());
        // 拒绝事实由独立事务保存；主体必须是真实ADMIN，不能把MINI账号ID混入审核人。
        Map<String, Object> denied =
                jdbc.queryForMap(
                        "SELECT actor_realm,actor_user_id,actor_account_id,aggregate_type,reason"
                                + " FROM vi_event_log WHERE actor_user_id=? AND"
                                + " action='QUALIFICATION_AUDIT_DENIED'",
                        administratorId(self));
        assertEquals("ADMIN", denied.get("actor_realm"));
        assertEquals(administratorId(self), ((Number) denied.get("actor_user_id")).longValue());
        assertNull(denied.get("actor_account_id"));
        assertEquals("VI_DRIVER_APPLICATION", denied.get("aggregate_type"));
        assertEquals("VI_SELF_REVIEW_DENIED", denied.get("reason"));
    }

    @Test
    void trustedPhoneMatchIsFrozenAtSubmitAndCannotBeErasedByChangingAdminPhone() {
        Student person = student(COMPANY_A, true);
        String self = scoped(COMPANY_A, QQUERY, QAUDIT);
        long uid = administratorId(self);
        jdbc.update("UPDATE sys_user SET phonenumber='13800139505' WHERE user_id=?", uid);
        jdbc.update(
                "UPDATE vi_account SET phone_lookup=?,phone_verified_at=UTC_TIMESTAMP(3) WHERE"
                        + " id=?",
                phoneCrypto.lookup("13800139505"),
                person.accountId());
        Submitted item = freezeWithUpstreamBoundary(person);
        jdbc.update("UPDATE sys_user SET phonenumber='13800139506' WHERE user_id=?", uid);
        requireError(
                audit(item, approvalBody(adminDetail(item)), self), 403, "VI_SELF_REVIEW_DENIED");
        assertEquals(
                0,
                rows(
                        "SELECT COUNT(*) FROM vi_employee_binding WHERE account_id=?",
                        person.accountId()));
        assertEquals(0, reviewCount());
    }

    @Test
    void newlyVerifiedPhoneMappingAtAuditAlsoPreventsApplicantSelfApproval() {
        Submitted item = submitted();
        String self = scoped(COMPANY_A, QQUERY, QAUDIT);
        jdbc.update(
                "UPDATE sys_user SET phonenumber='13800139507' WHERE user_id=?",
                administratorId(self));
        jdbc.update(
                "UPDATE vi_account SET phone_lookup=?,phone_verified_at=UTC_TIMESTAMP(3) WHERE"
                        + " id=?",
                phoneCrypto.lookup("13800139507"),
                item.person().accountId());
        requireError(
                audit(item, approvalBody(adminDetail(item)), self), 403, "VI_SELF_REVIEW_DENIED");
        assertEquals(0, snapshotCount());
    }

    @Test
    void manualReviewerMayPerformFinalAuditWhenTheyAreNotTheApplicant() throws Exception {
        Submitted item = fullyExecutedSubmission();
        assertEquals(
                2,
                rows(
                        "SELECT COUNT(*) FROM vi_manual_verification WHERE application_id=? AND"
                                + " reviewed_by_user_id=? AND status='CONFIRMED'",
                        item.person().applicationId(),
                        administratorId(completedManualReviewer)));
        JsonNode approved =
                data(audit(item, approvalBody(adminDetail(item)), completedManualReviewer));
        assertEquals("APPROVED", approved.path("status").asText());
        assertEquals(1, snapshotCount());
    }

    @Test
    void rejectStoresOnlyPublicOpinionInMiniAndKeepsIndependentReviewHistory() {
        Submitted item = submitted();
        Map<String, Object> body =
                auditBody(adminDetail(item), "REJECT", "请重新核对本人姓名", List.of("realName"), null);
        body.put("opinion", "不应公开的内部审核详细意见");
        JsonNode rejected = data(audit(item, body, rootAdmin));
        assertEquals("REJECTED", rejected.path("status").asText());
        JsonNode mine = qualification(item.person());
        assertEquals("请重新核对本人姓名", mine.path("publicReviewOpinion").asText());
        assertEquals(
                List.of("realName"), ViJson.read(mine.path("rejectFields").toString(), List.class));
        assertFalse(mine.toString().contains("不应公开的内部审核详细意见"));
        assertFalse(adminDetail(item).toString().contains("不应公开的内部审核详细意见"));
        assertPrivateTextNotInArtifacts("不应公开的内部审核详细意见");
        assertEquals(0, snapshotCount());
        assertEquals(1, reviewCount());
    }

    @Test
    void rejectedSubmissionAllowsOnlyNamedFieldsAndResubmitAppendsNewDecision() {
        Submitted item = submitted();
        data(
                audit(
                        item,
                        auditBody(adminDetail(item), "REJECT", "姓名需补正", List.of("realName"), null),
                        rootAdmin));
        JsonNode rejected = qualification(item.person());
        Map<String, Object> forbidden = new LinkedHashMap<>();
        forbidden.put("applicationId", Long.toString(item.person().applicationId()));
        forbidden.put("expectedVersion", rejected.path("version").asLong());
        forbidden.put("licenseClass", "B2");
        requireStatus(miniCall("PUT", QUALIFICATION, forbidden, item.person().token()), 409);
        saveMaterials(item.person(), Map.of("realName", "补正后的申请人"));
        assertEquals("REJECTED", qualification(item.person()).path("status").asText());
        Submitted again = freezeWithUpstreamBoundary(item.person());
        assertEquals(2, again.application().path("submissionRevision").asLong());
        approve(again);
        assertEquals(2, reviewCount());
        assertEquals(
                2,
                rows(
                        "SELECT COUNT(*) FROM vi_driver_application_submission WHERE"
                                + " application_id=?",
                        item.person().applicationId()));
    }

    @Test
    void terminalDecisionCannotBeReplacedByAnotherUuidOrOppositeDecision() {
        Submitted item = submitted();
        approve(item);
        JsonNode done = adminDetail(item);
        requireError(
                audit(
                        item,
                        auditBody(done, "REJECT", "不能覆盖批准", List.of("realName"), null),
                        rootAdmin),
                409,
                "VI_STATE_CONFLICT");
        assertEquals("APPROVED", adminDetail(item).path("status").asText());
        assertEquals(1, reviewCount());
        assertEquals(1, snapshotCount());
    }

    @Test
    void sameIdempotencyKeyReturnsOneDecisionButAnotherBodyConflicts() {
        Submitted item = submitted();
        Map<String, Object> body = approvalBody(adminDetail(item));
        String key = key();
        JsonNode first = data(auditKey(item, body, rootAdmin, key));
        JsonNode replay = data(auditKey(item, body, rootAdmin, key));
        assertEquals(
                first.path("effectiveSnapshotId").asText(),
                replay.path("effectiveSnapshotId").asText());
        Map<String, Object> other = new LinkedHashMap<>(body);
        other.put("opinion", "不同内部意见");
        requireError(auditKey(item, other, rootAdmin, key), 409, "VI_IDEMPOTENCY_CONFLICT");
        assertEquals(1, reviewCount());
        assertEquals(1, snapshotCount());
    }

    @Test
    void concurrentAuditorsCommitOneReviewOneSnapshotAndOnePermissionChange() throws Exception {
        Submitted item = submitted();
        Map<String, Object> body = approvalBody(adminDetail(item));
        String one = scoped(COMPANY_A, QQUERY, QAUDIT), two = scoped(COMPANY_A, QQUERY, QAUDIT);
        long before = permissionVersion(item.person());
        List<ResponseEntity<String>> result =
                race(() -> audit(item, body, one), () -> audit(item, body, two));
        assertEquals(
                List.of(200, 409),
                result.stream().map(r -> r.getStatusCode().value()).sorted().toList());
        assertEquals(1, reviewCount());
        assertEquals(1, snapshotCount());
        assertEquals(before + 1, permissionVersion(item.person()));
        assertEquals(
                1,
                rows("SELECT COUNT(*) FROM vi_outbox WHERE event_type='QUALIFICATION_APPROVED'"));
    }

    @Test
    void auditOutboxFailureRollsBackReviewSnapshotProfileAndPermissionVersion() {
        Submitted item = submitted();
        long permission = permissionVersion(item.person());
        doThrow(new IllegalStateException("隔离审核outbox故障"))
                .when(outbox)
                .publish(
                        startsWith("QUALIFICATION_APPROVED:"),
                        eq("VI_DRIVER_APPLICATION"),
                        anyLong(),
                        eq("QUALIFICATION_APPROVED"),
                        any());
        requireStatus(audit(item, approvalBody(adminDetail(item)), rootAdmin), 500);
        assertEquals(0, reviewCount());
        assertEquals(0, snapshotCount());
        assertEquals("SUBMITTED", adminDetail(item).path("status").asText());
        assertEquals(permission, permissionVersion(item.person()));
        assertNull(profile(item).get("effective_snapshot_id"));
    }

    @Test
    void expiryTimerPublicationFailureAlsoRollsBackApprovalAndPermissionVersion() {
        Submitted item = submitted();
        long permission = permissionVersion(item.person());
        doThrow(new IllegalStateException("隔离到期任务发布故障"))
                .when(outbox)
                .publishAt(
                        startsWith("QUALIFICATION_EXPIRY:"),
                        eq("VI_DRIVER_PROFILE"),
                        anyLong(),
                        eq("DRIVER_QUALIFICATION_EXPIRES"),
                        any(),
                        any(Instant.class));
        requireStatus(audit(item, approvalBody(adminDetail(item)), rootAdmin), 500);
        assertEquals(0, reviewCount());
        assertEquals(0, snapshotCount());
        assertEquals(permission, permissionVersion(item.person()));
        assertEquals("SUBMITTED", adminDetail(item).path("status").asText());
    }

    @Test
    void revokedAdminCannotAuditOrReplayEvenWhenRedisEvictionFails() {
        Submitted item = submitted();
        String user = scoped(COMPANY_A, QQUERY, QAUDIT);
        Map<String, Object> body = approvalBody(adminDetail(item));
        doThrow(new IllegalStateException("隔离Redis故障")).when(redis).deleteObject(anyString());
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                administratorId(user));
        requireStatus(audit(item, body, user), 401);
        assertEquals(0, reviewCount());
        assertEquals(0, snapshotCount());
    }

    @Test
    void replayAfterPermissionsAreRevokedCannotReturnThePreviousSensitiveDetail() {
        Submitted item = submitted();
        String user = scoped(COMPANY_A, QQUERY, QAUDIT);
        String key = key();
        Map<String, Object> body = approvalBody(adminDetail(item));
        data(auditKey(item, body, user, key));
        jdbc.update("DELETE FROM sys_user_dept WHERE user_id=?", administratorId(user));
        requireStatus(auditKey(item, body, user, key), 404);
        assertEquals(1, reviewCount());
    }

    @Test
    void approvalCopiesOnlyFrozenLicenseAndUsesMinimumExplicitDeadline() {
        Submitted item = submitted();
        Instant due = Instant.now().plus(Duration.ofDays(3)).truncatedTo(ChronoUnit.MILLIS);
        JsonNode result =
                data(
                        audit(
                                item,
                                auditBody(adminDetail(item), "APPROVE", null, List.of(), due),
                                rootAdmin));
        assertEquals("APPROVED", result.path("status").asText());
        var authenticated = snapshots.authenticated(currentSnapshotId(item));
        assertEquals("C1", authenticated.getLicenseClass());
        assertEquals(due, authenticated.getReviewDueAt());
        assertEquals(due, authenticated.getExpiresAt());
        assertFalse(authenticated.getLicenseLongTerm());
        assertEquals(1, authenticated.getQualificationRevision());
    }

    @Test
    void renewalRejectionPreservesApprovedSnapshotFlagsAndExistingVehicleResponsibility() {
        Submitted original = submitted();
        approve(original);
        long snapshot = currentSnapshotId(original), order = heldVehicle(original);
        jdbc.update(
                "UPDATE vi_driver_profile SET listening_enabled=1 WHERE id=?",
                original.person().driverId());
        Submitted renewal = renewal(original);
        data(
                audit(
                        renewal,
                        auditBody(
                                adminDetail(renewal),
                                "REJECT",
                                "请补正复审姓名",
                                List.of("realName"),
                                null),
                        rootAdmin));
        assertEquals(snapshot, currentSnapshotId(original));
        assertEquals("APPROVED", profile(original).get("qualification_status"));
        assertEquals(1, ((Number) profile(original).get("listening_enabled")).intValue());
        assertVehicleHeld(order, original);
        assertEquals(1, snapshotCount());
    }

    @Test
    void renewalApprovalCreatesNextRevisionWithoutReenablingAdministrativePause() {
        Submitted original = submitted();
        approve(original);
        jdbc.update(
                "UPDATE vi_driver_profile SET listening_enabled=1 WHERE id=?",
                original.person().driverId());
        Submitted renewal = renewal(original);
        data(
                call(
                        "POST",
                        "/api/admin/drivers/"
                                + original.person().driverId()
                                + "/disable-new-assignments",
                        Map.of(
                                "expectedVersion",
                                profileVersion(original.person()),
                                "reason",
                                "复审期间保持行政暂停"),
                        rootAdmin));
        JsonNode approved = approve(renewal);
        assertEquals(2, approved.path("qualificationRevision").asLong());
        assertEquals(2, snapshotCount());
        assertEquals(0, ((Number) profile(original).get("new_assignment_enabled")).intValue());
        assertEquals(1, ((Number) profile(original).get("listening_enabled")).intValue());
        assertEquals("APPROVED", profile(original).get("qualification_status"));
    }

    @Test
    void pastReviewDeadlineBlocksNewAssignmentsBeforeAnyJobRunsAndKeepsDriverHistoryRole() {
        Submitted item = submitted();
        approve(item);
        var snapshot = snapshots.authenticated(currentSnapshotId(item));
        controlledNow.set(snapshot.getExpiresAt());
        assertEquals(
                "REVIEW_EXPIRED_OR_UNKNOWN",
                newAssignments.qualificationBlock(
                        driverMapper.find(item.person().driverId()), controlledNow.get()));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        newAssignments.requireQualification(
                                driverMapper.find(item.person().driverId())));
        assertEquals(
                "APPROVED", profile(item).get("qualification_status"), "定时任务尚未运行，不靠job及时性保证禁新单");
        JsonNode session =
                data(miniCall("GET", "/api/common/session", null, item.person().token()));
        assertTrue(session.path("availableRoles").toString().contains("DRIVER"), "曾批准视角保留历史与在手责任");
    }

    @Test
    void expiryJobPreservesApprovedHistoryVehicleCustodyAndAdministrativePreferences() {
        Submitted item = submitted();
        approve(item);
        data(
                call(
                        "POST",
                        "/api/admin/drivers/"
                                + item.person().driverId()
                                + "/enable-new-assignments",
                        Map.of(
                                "expectedVersion",
                                profileVersion(item.person()),
                                "reason",
                                "明确开放行政新承接"),
                        rootAdmin));
        long order = heldVehicle(item), permission = permissionVersion(item.person());
        long snapshot = currentSnapshotId(item);
        jdbc.update(
                "UPDATE vi_driver_profile SET listening_enabled=1 WHERE id=?",
                item.person().driverId());
        controlledNow.set(snapshots.authenticated(snapshot).getExpiresAt().plusMillis(1));
        expiryJob.runOnce();
        assertEquals("EXPIRED", profile(item).get("qualification_status"));
        assertEquals(snapshot, currentSnapshotId(item));
        assertEquals(permission + 1, permissionVersion(item.person()));
        assertEquals("APPROVED", adminDetail(item).path("status").asText());
        assertEquals(1, ((Number) profile(item).get("listening_enabled")).intValue());
        assertEquals(
                1,
                ((Number) profile(item).get("new_assignment_enabled")).intValue(),
                "到期不偷偷改行政开关，实时资格规则禁新单");
        assertVehicleHeld(order, item);
        assertNotNull(snapshots.authenticated(snapshot));
    }

    @Test
    void repeatedAndConcurrentExpiryWorkersRecordOneExpiryEventOnly() throws Exception {
        Submitted item = submitted();
        approve(item);
        long before = permissionVersion(item.person());
        controlledNow.set(
                snapshots.authenticated(currentSnapshotId(item)).getExpiresAt().plusSeconds(1));
        var pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> a = pool.submit(expiryJob::runOnce),
                    b = pool.submit(expiryJob::runOnce);
            a.get(15, TimeUnit.SECONDS);
            b.get(15, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        expiryJob.runOnce();
        assertEquals("EXPIRED", profile(item).get("qualification_status"));
        assertEquals(before + 1, permissionVersion(item.person()));
        assertEquals(
                1,
                rows(
                        "SELECT COUNT(*) FROM vi_event_log WHERE aggregate_type='VI_DRIVER_PROFILE'"
                                + " AND aggregate_id=? AND action='DRIVER_QUALIFICATION_EXPIRED'",
                        item.person().driverId()));
    }

    @Test
    void oldExpiryTimerCannotExpireANewerApprovedRenewalSnapshot() {
        Submitted first = submitted();
        Instant oldDue = Instant.now().plus(Duration.ofDays(2)).truncatedTo(ChronoUnit.MILLIS);
        data(
                audit(
                        first,
                        auditBody(adminDetail(first), "APPROVE", null, List.of(), oldDue),
                        rootAdmin));
        long oldSnapshot = currentSnapshotId(first);
        Submitted second = renewal(first);
        Instant nextDue = oldDue.plus(Duration.ofDays(4));
        data(
                audit(
                        second,
                        auditBody(adminDetail(second), "APPROVE", null, List.of(), nextDue),
                        rootAdmin));
        long current = currentSnapshotId(second), permission = permissionVersion(second.person());
        controlledNow.set(oldDue.plusSeconds(1));
        expiryJob.runOnce();
        assertNotEquals(oldSnapshot, current);
        assertEquals(current, currentSnapshotId(second));
        assertEquals("APPROVED", profile(second).get("qualification_status"));
        assertEquals(permission, permissionVersion(second.person()));
    }

    @Test
    void missingExpiryTimerIsRepairedFromAuthenticatedSnapshotExactlyOnce() {
        Submitted item = submitted();
        approve(item);
        long snapshot = currentSnapshotId(item);
        jdbc.update("DELETE FROM vi_outbox WHERE event_key=?", "QUALIFICATION_EXPIRY:" + snapshot);
        expiryJob.runOnce();
        expiryJob.runOnce();
        assertEquals(
                1,
                rows(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_key=?",
                        "QUALIFICATION_EXPIRY:" + snapshot));
        assertEquals("APPROVED", profile(item).get("qualification_status"));
    }

    @Test
    void legacyRejectionRepairAddsOnlyPublicCorrectionStructureWithoutApprovingOrOpeningFiles() {
        Student person = student(COMPANY_A, true);
        jdbc.update(
                "UPDATE vi_driver_application SET"
                    + " status='REJECTED',reject_fields=NULL,public_review_opinion=NULL WHERE id=?",
                person.applicationId());
        Submitted item = new Submitted(person, qualification(person));
        JsonNode detail = adminDetail(item);
        assertTrue(detail.path("legacyRejectionRepairable").asBoolean());
        JsonNode repaired =
                data(
                        audit(
                                item,
                                auditBody(
                                        detail,
                                        "REPAIR_REJECTION",
                                        "历史驳回明确只补姓名",
                                        List.of("realName"),
                                        null),
                                rootAdmin));
        assertEquals("REJECTED", repaired.path("status").asText());
        assertEquals(0, snapshotCount());
        assertEquals(1, reviewCount());
        assertEquals(
                List.of("realName"),
                ViJson.read(qualification(person).path("editableFields").toString(), List.class));
        requireStatus(
                call(
                        "POST",
                        "/api/common/files/" + documentId(item) + "/access",
                        Map.of("purpose", "查看历史未提交证件"),
                        rootAdmin),
                404);
    }

    @Test
    void legacyRepairCannotBeUsedToChangeModernRejectionOrAnUnsubmittedDraft() {
        Submitted modern = submitted();
        data(
                audit(
                        modern,
                        auditBody(
                                adminDetail(modern), "REJECT", "请补正姓名", List.of("realName"), null),
                        rootAdmin));
        requireStatus(
                audit(
                        modern,
                        auditBody(
                                adminDetail(modern),
                                "REPAIR_REJECTION",
                                "尝试覆盖既有审核",
                                List.of("licenseClass"),
                                null),
                        rootAdmin),
                409);
        Student draft = student(COMPANY_A, true);
        Submitted unfinished = new Submitted(draft, qualification(draft));
        requireStatus(
                audit(
                        unfinished,
                        auditBody(
                                adminDetail(unfinished),
                                "REPAIR_REJECTION",
                                "不能修复草稿",
                                List.of("realName"),
                                null),
                        rootAdmin),
                409);
        assertEquals(1, reviewCount());
    }

    @Test
    void shanghaiLicenseExpiryIncludesTodayAndStopsAtNextLocalMidnight() {
        Student person = student(COMPANY_A, true);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        saveMaterials(person, Map.of("validTo", today.toString()));
        Submitted item = freezeWithUpstreamBoundary(person);
        approve(item);
        Instant boundary = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant();
        assertEquals(boundary, snapshots.authenticated(currentSnapshotId(item)).getExpiresAt());
        controlledNow.set(boundary.minusMillis(1));
        assertNull(
                newAssignments.qualificationBlock(
                        driverMapper.find(person.driverId()), controlledNow.get()));
        controlledNow.set(boundary);
        assertEquals(
                "LICENSE_EXPIRED_OR_UNKNOWN",
                newAssignments.qualificationBlock(
                        driverMapper.find(person.driverId()), controlledNow.get()));
        assertEquals("APPROVED", profile(item).get("qualification_status"));
    }

    @Test
    void longTermLicenseStillHasAnExplicitReviewDeadlineAndCanExpire() {
        Student person = student(COMPANY_A, true);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("longTermValid", true);
        values.put("validTo", null);
        saveMaterials(person, values);
        Submitted item = freezeWithUpstreamBoundary(person);
        approve(item);
        var effective = snapshots.authenticated(currentSnapshotId(item));
        assertTrue(effective.getLicenseLongTerm());
        assertNull(effective.getLicenseValidTo());
        assertEquals(effective.getReviewDueAt(), effective.getExpiresAt());
        controlledNow.set(effective.getExpiresAt());
        assertEquals(
                "REVIEW_EXPIRED_OR_UNKNOWN",
                newAssignments.qualificationBlock(
                        driverMapper.find(person.driverId()), controlledNow.get()));
    }

    @Test
    void authorizedAdminReadsExactlyTheFrozenFourPrivateImagesAndEncryptedAccessReason() {
        Submitted item = submitted();
        String viewer = scoped(COMPANY_A, QQUERY, QSENSITIVE, FQUERY, FACCESS);
        Map<?, ?> docs =
                ViJson.read(
                        jdbc.queryForObject(
                                "SELECT evidence_file_ids FROM vi_driver_application_submission"
                                        + " WHERE application_id=?",
                                String.class,
                                item.person().applicationId()),
                        Map.class);
        for (Object value : docs.values()) {
            long id = Long.parseLong(value.toString());
            requireStatus(call("GET", "/api/common/files/" + id, null, viewer), 200);
            String reason = "核对已提交资格证件原件";
            JsonNode access =
                    data(
                            call(
                                    "POST",
                                    "/api/common/files/" + id + "/access",
                                    Map.of("purpose", reason),
                                    viewer));
            long ttl =
                    Duration.between(
                                    Instant.now(), Instant.parse(access.path("expiresAt").asText()))
                            .getSeconds();
            assertTrue(ttl >= 55 && ttl <= 60, "资格图片沿F01 READ60秒，不套用培训视频300秒");
            ResponseEntity<byte[]> download =
                    downloadQualification(
                            access,
                            viewer,
                            access.path("requiredHeaders").path("X-File-Ticket").asText());
            requireStatus(download, 200);
            assertArrayEquals(png, download.getBody());
            JsonNode summary =
                    ViJson.tree(
                            jdbc.queryForObject(
                                    "SELECT after_summary FROM vi_event_log WHERE"
                                            + " aggregate_type='FILE' AND aggregate_id=? AND"
                                            + " action='FILE_ACCESS' ORDER BY id DESC LIMIT 1",
                                    String.class,
                                    id));
            String aad =
                    "Q05:FILE_ACCESS:"
                            + id
                            + ":USER:"
                            + administratorId(viewer)
                            + ":REQUEST:"
                            + access.path("requestId").asText();
            assertEquals(
                    reason,
                    crypto.unenvelope(
                            Base64.getDecoder()
                                    .decode(summary.path("accessReasonEnvelope").asText()),
                            aad));
            assertPrivateTextNotInArtifacts(reason);
            assertFalse(access.toString().contains(reason));
        }
    }

    @Test
    void qualificationFileReadRequiresIndependentSensitiveAndFileScopes() {
        Submitted item = submitted();
        long file = documentId(item);
        String noSensitive = scoped(COMPANY_A, QQUERY, FQUERY, FACCESS);
        String noQualification = scoped(COMPANY_A, QSENSITIVE, FQUERY, FACCESS);
        String otherCompany = scoped(COMPANY_B, QQUERY, QSENSITIVE, FQUERY, FACCESS);
        requireStatus(call("GET", "/api/common/files/" + file, null, noSensitive), 403);
        requireStatus(call("GET", "/api/common/files/" + file, null, noQualification), 403);
        requireStatus(call("GET", "/api/common/files/" + file, null, otherCompany), 404);
        requireStatus(
                call(
                        "POST",
                        "/api/common/files/" + file + "/access",
                        Map.of("purpose", "查看"),
                        scoped(COMPANY_A, QQUERY, QSENSITIVE, FQUERY)),
                403);
        requireStatus(
                call("POST", "/api/common/files/" + file + "/access", Map.of(), rootAdmin), 400);
    }

    @Test
    void sameApplicationUnselectedReadyImageAndUnsubmittedDraftAreNotOpenedToAdmins() {
        Student person = student(COMPANY_A, true);
        long unused =
                uploadPrivate(
                        Map.of(
                                "purpose",
                                "ID_CARD",
                                "applicationId",
                                Long.toString(person.applicationId())),
                        png,
                        "image/png",
                        "未选择的证件.png",
                        person.token(),
                        "owner");
        Submitted item = freezeWithUpstreamBoundary(person);
        requireStatus(call("GET", "/api/common/files/" + unused, null, rootAdmin), 404);
        requireStatus(
                call(
                        "POST",
                        "/api/common/files/" + unused + "/access",
                        Map.of("purpose", "不能读未选图片"),
                        rootAdmin),
                404);
        Student draft = student(COMPANY_A, true);
        long selected = qualification(draft).path("draft").path("idCardFrontFileId").asLong();
        requireStatus(call("GET", "/api/common/files/" + selected, null, rootAdmin), 404);
        assertEquals("SUBMITTED", adminDetail(item).path("status").asText());
    }

    @Test
    void qualificationReadTicketRejectsTamperingDifferentAdminAndRevokedAuthentication() {
        Submitted item = submitted();
        String viewer = scoped(COMPANY_A, QQUERY, QSENSITIVE, FQUERY, FACCESS);
        long frozenFileId = documentId(item);
        JsonNode access =
                data(
                        call(
                                "POST",
                                "/api/common/files/" + documentId(item) + "/access",
                                Map.of("purpose", "核对证件"),
                                viewer));
        String ticket = access.path("requiredHeaders").path("X-File-Ticket").asText();
        requireStatus(downloadQualification(access, viewer, "0.forged"), 403);
        requireStatus(downloadQualification(access, rootAdmin, ticket), 403);
        data(
                audit(
                        item,
                        auditBody(adminDetail(item), "REJECT", "补正姓名", List.of("realName"), null),
                        rootAdmin));
        saveMaterials(item.person(), Map.of("realName", "同图新提交申请人"));
        Submitted next = freezeWithUpstreamBoundary(item.person());
        assertEquals(2, next.application().path("submissionRevision").asLong());
        assertEquals(frozenFileId, documentId(next), "四图未换，必须靠提交修订与seal上下文使旧票失效");
        requireStatus(downloadQualification(access, viewer, ticket), 403);
        JsonNode fresh =
                data(
                        call(
                                "POST",
                                "/api/common/files/" + documentId(next) + "/access",
                                Map.of("purpose", "复核补正后同图"),
                                viewer));
        requireStatus(
                downloadQualification(
                        fresh,
                        viewer,
                        fresh.path("requiredHeaders").path("X-File-Ticket").asText()),
                200);
        jdbc.update(
                "UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?",
                administratorId(viewer));
        requireStatus(
                downloadQualification(
                        fresh,
                        viewer,
                        fresh.path("requiredHeaders").path("X-File-Ticket").asText()),
                401);
    }

    @Test
    void internalOpinionSealBindsItsReviewAndNeverAppearsInAnyProjection() {
        Submitted item = submitted();
        Map<String, Object> body = approvalBody(adminDetail(item));
        String opinion = "内部审核核对记录不可公开";
        body.put("opinion", opinion);
        approveWith(item, body);
        Map<String, Object> review =
                jdbc.queryForMap(
                        "SELECT * FROM vi_qualification_review WHERE application_id=?",
                        item.person().applicationId());
        String aad =
                "Q05:REVIEW:"
                        + review.get("id")
                        + ":APPLICATION:"
                        + item.person().applicationId()
                        + ":REVISION:1:KIND:DECISION";
        byte[] cipher = (byte[]) review.get("internal_opinion_cipher");
        assertEquals(opinion, crypto.unenvelope(cipher, aad));
        assertThrows(RuntimeException.class, () -> crypto.unenvelope(cipher, aad + ":OTHER"));
        assertFalse(adminDetail(item).toString().contains(opinion));
        assertFalse(qualification(item.person()).toString().contains(opinion));
        assertPrivateTextNotInArtifacts(opinion);
    }

    @Test
    void unknownDeviceStatusIsARealQualificationResponseAndCannotBeSubmitted() throws Exception {
        configureWechat(COMPANY_A);
        Student person = student(COMPANY_A, true);
        JsonNode session = createDevice(person);
        WECHAT.mode = SoterSignatureLoopback.Mode.HTTP_503;
        JsonNode result =
                data(confirmDevice(person, session, signedDeviceBody(person, session), key()));
        assertEquals("UNKNOWN", result.path("status").asText());
        ResponseEntity<String> current = miniCall("GET", QUALIFICATION, null, person.token());
        JsonNode checks = data(current).path("checks");
        assertEquals(
                "UNKNOWN",
                java.util.stream.StreamSupport.stream(checks.spliterator(), false)
                        .filter(x -> "DEVICE_AUTH".equals(x.path("type").asText()))
                        .findFirst()
                        .orElseThrow()
                        .path("status")
                        .asText());
        requireStatus(
                miniCall(
                        "POST",
                        QUALIFICATION + "/submit",
                        submitQualificationBody(person),
                        person.token()),
                422);
        validateResponses(Map.of("qualification-unknown", current), "unknown");
    }

    @Test
    void threeActualAdminResponsesMatchBothPrimaryAndTaskContracts() throws Exception {
        Submitted item = submitted();
        Map<String, ResponseEntity<String>> samples = new LinkedHashMap<>();
        samples.put("list", call("GET", AUDITS, null, rootAdmin));
        samples.put("detail", call("GET", auditPath(item), null, rootAdmin));
        samples.put("audit", audit(item, approvalBody(adminDetail(item)), rootAdmin));
        for (var response : samples.values()) {
            requireStatus(response, 200);
            assertTrue(response.getHeaders().getCacheControl().contains("no-store"));
        }
        validateResponses(samples, "admin");
    }

    private void approveWith(Submitted item, Map<String, Object> body) {
        assertEquals("APPROVED", data(audit(item, body, rootAdmin)).path("status").asText());
    }

    private Map<String, Object> profile(Submitted item) {
        return jdbc.queryForMap(
                "SELECT * FROM vi_driver_profile WHERE id=?", item.person().driverId());
    }

    private int reviewCount() {
        return rows("SELECT COUNT(*) FROM vi_qualification_review");
    }

    private int snapshotCount() {
        return rows("SELECT COUNT(*) FROM vi_qualification_snapshot");
    }

    private JsonNode list(String path, String token) {
        ResponseEntity<String> response = call("GET", path, null, token);
        requireStatus(response, 200);
        return ViJson.tree(response.getBody());
    }

    private void bindApplicant(Student person, long user) {
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,phone_lookup,last_phone_verified_at,employee_auth_version)"
                    + " SELECT id,?,UTC_TIMESTAMP(3),app_id,phone_lookup,phone_verified_at,0 FROM"
                    + " vi_account WHERE id=?",
                user,
                person.accountId());
    }

    private long documentId(Submitted item) {
        return qualification(item.person()).path("draft").path("idCardFrontFileId").asLong();
    }

    private ResponseEntity<byte[]> downloadQualification(
            JsonNode grant, String token, String ticket) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", "admin");
        headers.set("X-File-Ticket", ticket);
        return http.exchange(
                "http://127.0.0.1:" + port + grant.path("downloadUrl").asText(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class);
    }

    private void assertVehicleHeld(long order, Submitted item) {
        Map<String, Object> row =
                jdbc.queryForMap(
                        "SELECT custody_type,custody_ref_id,active_assignment_id FROM vi_order"
                                + " WHERE id=?",
                        order);
        assertEquals("DRIVER", row.get("custody_type"));
        assertEquals(item.person().driverId(), ((Number) row.get("custody_ref_id")).longValue());
        assertEquals(
                1,
                rows(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=? AND"
                                + " holder_id=?",
                        item.person().driverId(),
                        row.get("active_assignment_id")));
        assertEquals(
                1,
                rows(
                        "SELECT COUNT(*) FROM vi_driver_assignment WHERE id=? AND ended_at IS NULL",
                        row.get("active_assignment_id")));
    }

    private List<ResponseEntity<String>> race(
            Callable<ResponseEntity<String>> first, Callable<ResponseEntity<String>> second)
            throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<ResponseEntity<String>> a =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(5, TimeUnit.SECONDS));
                                return first.call();
                            });
            Future<ResponseEntity<String>> b =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(5, TimeUnit.SECONDS));
                                return second.call();
                            });
            start.countDown();
            return List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    private void validateResponses(Map<String, ResponseEntity<String>> samples, String mode)
            throws Exception {
        Path directory = Files.createTempDirectory("q05-contract-");
        for (var entry : samples.entrySet()) {
            requireStatus(entry.getValue(), 200);
            Files.writeString(
                    directory.resolve(entry.getKey() + ".json"), entry.getValue().getBody());
        }
        Path repo = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!Files.exists(repo.resolve("docs/vehicle"))) repo = repo.getParent();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/q05_contract.py")
                                        .toString(),
                                repo.toString(),
                                directory.toString(),
                                mode)
                        .redirectErrorStream(true)
                        .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(process.waitFor(20, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }
}
