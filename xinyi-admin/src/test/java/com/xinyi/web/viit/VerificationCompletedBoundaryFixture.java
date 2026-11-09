package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;

import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.IdempotencyService;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 仅旧Q01/Q03消费者的明确Q04上游完成边界。建立设备原登录会话及人工双人parent-link，不执行微信/人工流程。
 * Q04VerificationIT禁止使用本类，其正向必须经真实微信HTTP协议回环及双人API产生证明。
 */
final class VerificationCompletedBoundaryFixture {
    private VerificationCompletedBoundaryFixture() {}

    static void seed(
            JdbcTemplate jdbc,
            IntegrationCrypto crypto,
            IdempotencyService idempotency,
            long applicationId,
            long sessionId) {
        assertEquals("xinyi_vi_it_e07", jdbc.queryForObject("SELECT DATABASE()", String.class));
        assertEquals(
                "XINYI_VEHICLE_ISOLATED",
                jdbc.queryForObject(
                        "SELECT kind FROM test_environment_guard WHERE id=1", String.class));
        Map<String, Object> app =
                jdbc.queryForMap(
                        "SELECT"
                            + " app.material_revision,app.material_hash,p.owner_dept_id,p.training_context_revision,p.account_id,account.app_id,account.open_id"
                            + " FROM vi_driver_application app JOIN vi_driver_profile p ON"
                            + " p.id=app.driver_id JOIN vi_account account ON"
                            + " account.id=p.account_id WHERE app.id=?",
                        applicationId);
        if (app.get("owner_dept_id") == null) return;
        long company = ((Number) app.get("owner_dept_id")).longValue();
        long epoch = ((Number) app.get("training_context_revision")).longValue();
        long material = ((Number) app.get("material_revision")).longValue();
        long account = ((Number) app.get("account_id")).longValue();
        byte[] hash = (byte[]) app.get("material_hash");
        assertNotNull(hash);
        Map<String, Object> login =
                jdbc.queryForMap(
                        "SELECT account_id,context_version FROM vi_session WHERE id=?", sessionId);
        assertEquals(account, ((Number) login.get("account_id")).longValue());
        jdbc.update(
                "DELETE FROM vi_provider_verification WHERE application_id=? AND task_type IN"
                        + " ('DEVICE_AUTH','REALNAME','FACE','BACKGROUND','LICENSE')",
                applicationId);
        jdbc.update("DELETE FROM vi_face_session WHERE application_id=?", applicationId);
        jdbc.update("DELETE FROM vi_manual_verification WHERE application_id=?", applicationId);
        Instant at = Instant.now().minusSeconds(1).truncatedTo(ChronoUnit.MILLIS);
        // JDBC连接采用GMT+8，而DATETIME列存UTC；直接传Timestamp会平移8小时，必须按生产handler写UTC本地值。
        LocalDateTime time = LocalDateTime.ofInstant(at, ZoneOffset.UTC);
        String request = UUID.randomUUID().toString();
        byte[] marker = idempotency.mac("explicit upstream consumer boundary:" + request);
        jdbc.update(
                "INSERT INTO"
                    + " vi_face_session(application_id,owner_dept_id,company_revision,account_id,material_revision,material_hash,"
                    + "status,launch_mode,consent_version,expires_at,result_available,session_id,context_version,app_id,open_id_mac,request_no,"
                    + "request_mac,provider,product,provider_identity_digest,config_snapshot,submission_mac,submitted_at,verified_at,result_digest)"
                    + " VALUES"
                    + " (?,?,?,?,?,?,'PASSED','WX_SOTER_DEVICE_AUTH','q04-explicit-consumer-boundary',DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 HOUR),1,?,?,?,?,?,?,'WECHAT_SOTER','WX_SOTER_DEVICE_AUTH',?,'{}',?,?,?,?)",
                applicationId,
                company,
                epoch,
                account,
                material,
                hash,
                sessionId,
                login.get("context_version"),
                app.get("app_id"),
                idempotency.mac(app.get("open_id").toString()),
                request,
                marker,
                HexFormat.of().formatHex(marker),
                marker,
                time,
                time,
                "q04-explicit-consumer-boundary");
        long device =
                jdbc.queryForObject(
                        "SELECT id FROM vi_face_session WHERE request_no=?", Long.class, request);
        long deviceProof =
                proof(
                        jdbc,
                        crypto,
                        applicationId,
                        company,
                        epoch,
                        material,
                        hash,
                        "DEVICE_AUTH",
                        "WECHAT_SOTER",
                        "SOTER:" + device,
                        "DEVICE_SESSION",
                        device,
                        sessionId,
                        at,
                        "q04-explicit-consumer-boundary");
        jdbc.update("UPDATE vi_face_session SET verification_id=? WHERE id=?", deviceProof, device);
        for (String type : List.of("BACKGROUND", "LICENSE")) {
            // 真实形状的双人上游事实只供消费者关联检查，不声称本用例调用过人工API或最终资格审批。
            jdbc.update(
                    "INSERT INTO"
                        + " vi_manual_verification(application_id,owner_dept_id,company_revision,material_revision,material_hash,"
                        + "verification_type,conclusion,status,submitted_by_user_id,reviewed_by_user_id,submitted_at,reviewed_at,applicant_user_ids)"
                        + " VALUES (?,?,?,?,?,?,'PASSED','CONFIRMED',1,2,?,?,'[]')",
                    applicationId,
                    company,
                    epoch,
                    material,
                    hash,
                    type,
                    time,
                    time);
            long review =
                    jdbc.queryForObject(
                            "SELECT id FROM vi_manual_verification WHERE application_id=? AND"
                                    + " verification_type=? ORDER BY id DESC LIMIT 1",
                            Long.class,
                            applicationId,
                            type);
            String aad =
                    "Q04:MANUAL:"
                            + review
                            + ":"
                            + applicationId
                            + ":"
                            + company
                            + ":"
                            + epoch
                            + ":"
                            + material
                            + ":"
                            + type
                            + ":1:SUBMIT";
            jdbc.update(
                    "UPDATE vi_manual_verification SET note_cipher=? WHERE id=?",
                    crypto.envelope(ViJson.write(""), aad),
                    review);
            long manualProof =
                    proof(
                            jdbc,
                            crypto,
                            applicationId,
                            company,
                            epoch,
                            material,
                            hash,
                            type,
                            "MANUAL_TWO_PERSON",
                            "MANUAL:" + review + ":" + type,
                            "MANUAL_REVIEW",
                            review,
                            null,
                            at,
                            null);
            jdbc.update(
                    "UPDATE vi_manual_verification SET proof_id=? WHERE id=?", manualProof, review);
        }
    }

    private static long proof(
            JdbcTemplate jdbc,
            IntegrationCrypto crypto,
            long app,
            long company,
            long epoch,
            long revision,
            byte[] material,
            String type,
            String provider,
            String task,
            String source,
            long parent,
            Long session,
            Instant at,
            String digest) {
        jdbc.update(
                "INSERT INTO"
                    + " vi_provider_verification(application_id,owner_dept_id,company_revision,material_revision,material_hash,provider,task_type,provider_task_id,status,result_digest,verified_at,source_kind,source_record_id,source_session_id)"
                    + " VALUES (?,?,?,?,?,?,?,?,'PASSED',?,?,?,?,?)",
                app,
                company,
                epoch,
                revision,
                material,
                provider,
                type,
                task,
                digest,
                LocalDateTime.ofInstant(at, ZoneOffset.UTC),
                source,
                parent,
                session);
        long id =
                jdbc.queryForObject(
                        "SELECT id FROM vi_provider_verification WHERE source_kind=? AND"
                                + " source_record_id=? AND task_type=?",
                        Long.class,
                        source,
                        parent,
                        type);
        Map<String, Object> facts = new TreeMap<>();
        facts.put("proofId", Long.toString(id));
        facts.put("applicationId", Long.toString(app));
        facts.put("ownerDeptId", Long.toString(company));
        facts.put("companyRevision", epoch);
        facts.put("materialRevision", revision);
        facts.put("materialHash", HexFormat.of().formatHex(material));
        facts.put("type", type);
        facts.put("status", "PASSED");
        facts.put("provider", provider);
        facts.put("providerTaskId", task);
        facts.put("sourceKind", source);
        facts.put("sourceRecordId", Long.toString(parent));
        facts.put("sourceSessionId", session == null ? null : Long.toString(session));
        facts.put("verifiedAt", at.toString());
        facts.put("resultDigest", digest);
        String aad =
                "Q04:PROOF:"
                        + id
                        + ":APPLICATION:"
                        + app
                        + ":TYPE:"
                        + type
                        + ":SOURCE:"
                        + source
                        + ":"
                        + parent;
        jdbc.update(
                "UPDATE vi_provider_verification SET canonical_result_cipher=? WHERE id=?",
                crypto.envelope(ViJson.write(facts), aad),
                id);
        return id;
    }
}
