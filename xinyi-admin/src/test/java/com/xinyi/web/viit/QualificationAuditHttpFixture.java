package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Q05无@Test基础夹具。大部分审批场景明确消费Q03/Q04正式上游完成关系，仍由真实Q01提交封装；
 * 至少一条完整正向另走实际微信HTTP协议、人工双人、MP4学习及阅卷，不能把边界夹具叫做供应商验收。
 */
abstract class QualificationAuditHttpFixture extends VerificationHttpFixture {
    static final String AUDITS = "/api/admin/driver-qualifications";
    static final String QLIST = "biz:driverqualification:list";
    static final String QQUERY = "biz:driverqualification:query";
    static final String QAUDIT = "biz:driverqualification:audit";
    static final String QSENSITIVE = "biz:driverqualification:sensitive";
    static final String FQUERY = "biz:files:query";
    static final String FACCESS = "biz:files:access";

    @MockitoBean(name = "viDeadlineClock")
    Clock deadlineClock;

    String completedManualReviewer;
    final AtomicReference<Instant> controlledNow = new AtomicReference<>();

    record Submitted(Student person, JsonNode application) {}

    @BeforeEach
    void qualificationAuditBase() {
        controlledNow.set(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        when(deadlineClock.instant()).thenAnswer(unused -> controlledNow.get());
        when(deadlineClock.getZone()).thenReturn(ZoneOffset.UTC);
        when(deadlineClock.withZone(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(call -> Clock.fixed(controlledNow.get(), call.getArgument(0)));
        // 仍由最上层先检查隔离库名称及保护标记；这里只清理本任务新增的消费者事实。
        jdbc.execute("DELETE FROM vi_qualification_snapshot");
        jdbc.execute("DELETE FROM vi_qualification_review");
    }

    Submitted submitted() {
        return submitted(COMPANY_A);
    }

    Submitted submitted(long company) {
        Student person = student(company, true);
        return freezeWithUpstreamBoundary(person);
    }

    Submitted freezeWithUpstreamBoundary(Student person) {
        // 这里只消费正式父记录/认证封装；不假装本用例已调用微信或真实学习。Q01提交本身始终为真实API。
        trustedOtherVerifications(person);
        TrainingCompletedBoundaryFixture.seed(
                jdbc, boundaryCrypto, boundaryIdempotency, person.applicationId());
        JsonNode result =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION + "/submit",
                                submitQualificationBody(person),
                                person.token()));
        assertEquals("SUBMITTED", result.path("status").asText());
        return new Submitted(person, result);
    }

    Submitted fullyExecutedSubmission() throws Exception {
        configureWechat(COMPANY_A);
        Student person = student(COMPANY_A, true);
        passDevice(person);
        String reviewer =
                scoped(
                        COMPANY_A,
                        "biz:manualverification:query",
                        "biz:manualverification:review",
                        QQUERY,
                        QAUDIT);
        completedManualReviewer = reviewer;
        confirmManual(person, "BACKGROUND", rootAdmin, reviewer);
        confirmManual(person, "LICENSE", rootAdmin, reviewer);
        JsonNode course = publishCourse(COMPANY_A, 2, 1, 80);
        publishPolicy(COMPANY_A, List.of(course), 3, 0);
        passCourse(person, course);
        JsonNode result =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION + "/submit",
                                submitQualificationBody(person),
                                person.token()));
        assertEquals("SUBMITTED", result.path("status").asText());
        return new Submitted(person, result);
    }

    Map<String, Object> submitQualificationBody(Student person) {
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "expectedVersion",
                qualification(person).path("version").asLong(),
                "expectedProfileVersion",
                profileVersion(person),
                "reason",
                "完整资料和可信上游后提交资格审核");
    }

    String auditPath(Submitted item) {
        return AUDITS + "/" + item.person().applicationId();
    }

    JsonNode adminDetail(Submitted item) {
        return data(call("GET", auditPath(item), null, rootAdmin));
    }

    Map<String, Object> auditBody(
            JsonNode detail,
            String decision,
            String publicOpinion,
            List<String> rejected,
            Instant due) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("expectedVersion", detail.path("version").asLong());
        body.put("expectedProfileVersion", detail.path("profileVersion").asLong());
        body.put("expectedSubmissionRevision", detail.path("submissionRevision").asLong());
        body.put("expectedCompanyRevision", detail.path("companyRevision").asLong());
        body.put("decision", decision);
        body.put("opinion", "授权审核人的内部意见");
        body.put("publicOpinion", publicOpinion);
        body.put("rejectFields", rejected);
        body.put("reviewDueAt", due == null ? null : due.toString());
        return body;
    }

    Map<String, Object> approvalBody(JsonNode detail) {
        // 这是用例明确给定的未来时间，不是产品默认复审周期。
        return auditBody(
                detail,
                "APPROVE",
                "资格资料审核通过",
                List.of(),
                Instant.now().plus(Duration.ofDays(30)).truncatedTo(ChronoUnit.MILLIS));
    }

    ResponseEntity<String> audit(Submitted item, Map<String, Object> body, String user) {
        return call("POST", auditPath(item) + "/audit", body, user);
    }

    ResponseEntity<String> auditKey(
            Submitted item, Map<String, Object> body, String user, String key) {
        return callKey("POST", auditPath(item) + "/audit", body, user, key);
    }

    JsonNode approve(Submitted item) {
        return data(audit(item, approvalBody(adminDetail(item)), rootAdmin));
    }

    Submitted renewal(Submitted prior) {
        Student old = prior.person();
        JsonNode created =
                data(
                        miniCall(
                                "POST",
                                QUALIFICATION,
                                Map.of(
                                        "consentVersion",
                                        CONSENT_VERSION,
                                        "consentTextHash",
                                        sha(
                                                CONSENT_TEXT.getBytes(
                                                        java.nio.charset.StandardCharsets.UTF_8)),
                                        "acceptedConsent",
                                        true),
                                old.token()));
        Student person =
                new Student(
                        old.token(),
                        old.accountId(),
                        old.driverId(),
                        created.path("applicationId").asLong());
        completeMaterials(person);
        for (JsonNode check : qualification(person).path("checks")) {
            if ("DEVICE_AUTH".equals(check.path("type").asText()))
                assertNotEquals("PASSED", check.path("status").asText(), "新复审申请仍必须当前材料的新设备证明");
        }
        return freezeWithUpstreamBoundary(person);
    }

    void completeMaterials(Student person) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("realName", "复审隔离申请人");
        values.put("idCardNo", "110105199001010010");
        values.put("licenseClass", "C1");
        values.put("firstIssueDate", "2010-01-01");
        values.put("validFrom", "2020-01-01");
        values.put("validTo", "2099-12-31");
        values.put("longTermValid", false);
        for (String slot :
                List.of(
                        "idCardFrontFileId",
                        "idCardBackFileId",
                        "licenseMainFileId",
                        "licenseSubFileId")) {
            values.put(
                    slot,
                    Long.toString(
                            uploadPrivate(
                                    Map.of(
                                            "purpose",
                                            slot.startsWith("idCard")
                                                    ? "ID_CARD"
                                                    : "DRIVER_LICENSE",
                                            "applicationId",
                                            Long.toString(person.applicationId())),
                                    png,
                                    "image/png",
                                    "复审证件.png",
                                    person.token(),
                                    "owner")));
        }
        saveMaterials(person, values);
    }

    long heldVehicle(Submitted item) {
        Student person = item.person();
        long order = SEQ.incrementAndGet(),
                assignment = SEQ.incrementAndGet(),
                invite = SEQ.incrementAndGet();
        // 明确既有持车责任的领域夹具，不冒称本用例已执行H系列实际交接；Q05必须原样保留这些事实。
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(id,order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,active_assignment_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version)"
                    + " VALUES"
                    + " (?,?,?,?,?,?,'{}','PICKED_UP','DRIVER',?,?,UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 HOUR),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'Q05-test')",
                order,
                "Q05-ORDER-" + order,
                person.accountId(),
                SEQ.incrementAndGet(),
                SITE_DEPT,
                SEQ.incrementAndGet(),
                person.driverId(),
                assignment);
        jdbc.update(
                "INSERT INTO vi_driver_assignment(id,order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES (?,?,?,?,UTC_TIMESTAMP(3))",
                assignment,
                order,
                person.driverId(),
                invite);
        jdbc.update(
                "INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES"
                        + " (?,'ASSIGNMENT',?,?)",
                person.driverId(),
                assignment,
                order);
        return order;
    }

    long currentSnapshotId(Submitted item) {
        return jdbc.queryForObject(
                "SELECT effective_snapshot_id FROM vi_driver_profile WHERE id=?",
                Long.class,
                item.person().driverId());
    }

    Map<String, Object> snapshot(Submitted item) {
        return jdbc.queryForMap(
                "SELECT * FROM vi_qualification_snapshot WHERE id=?", currentSnapshotId(item));
    }

    long permissionVersion(Student person) {
        return jdbc.queryForObject(
                "SELECT permission_version FROM vi_account_security WHERE account_id=?",
                Long.class,
                person.accountId());
    }

    long administratorId(String token) {
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId();
    }

    void assertPrivateTextNotInArtifacts(String value) {
        for (Map<String, Object> row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log"))
            for (Object cell : row.values())
                assertFalse(Objects.toString(cell, "").contains(value));
        for (String payload : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class))
            assertFalse(payload.contains(value));
        for (String result :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) assertFalse(result.contains(value));
    }
}
