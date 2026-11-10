package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Q04真实HTTP基础夹具，无@Test。身份、Q01四证件、公司、集成配置均沿真实业务入口；只有微信开放接口位于独立本地HTTP回环，不替换核验Service/鉴权，也不直接制造正向证明。
 */
abstract class VerificationHttpFixture extends TrainingHttpFixture {
    static final String FACE = "/api/app/driver/qualification/face-session";
    static final String FACE_RESULTS = "/api/app/driver/qualification/face-sessions";
    static final String MANUAL = "/api/admin/manual-verifications";
    static final String INTEGRATIONS = "/api/admin/integrations";
    static final String FACE_CONSENT_VERSION = "q04-device-consent-isolated-v1";
    static final String FACE_CONSENT_TEXT = "隔离测试：同意核验当前登录中本设备机主的生物认证签名；此过程不证明身份证实名或真人活体。";
    static final String APP_ID = "wx0000000000000000";
    static final String APP_SECRET = "q04-isolated-wechat-secret-" + UUID.randomUUID();
    static final SoterSignatureLoopback WECHAT = new SoterSignatureLoopback();

    @DynamicPropertySource
    static void verificationTransport(DynamicPropertyRegistry registry) {
        registry.add("vi.verification.wechat-endpoint-test-override", WECHAT::endpoint);
    }

    @BeforeEach
    void verificationBase() {
        WECHAT.reset();
        WECHAT.addApp(APP_ID, APP_SECRET);
        jdbc.execute("DELETE FROM vi_manual_verification");
        for (String table :
                List.of(
                        "vi_integration_test_recipient",
                        "vi_integration_publication",
                        "vi_integration_check",
                        "vi_integration_secret",
                        "vi_integration_revision",
                        "vi_integration_profile")) jdbc.execute("DELETE FROM " + table);
        verificationConsent(FACE_CONSENT_VERSION, FACE_CONSENT_TEXT);
    }

    static {
        // Q04/Q05共用无@Test夹具，不能在首个测试类结束时关掉下一类仍使用的静态协议端点。
        Runtime.getRuntime().addShutdownHook(new Thread(WECHAT::close, "vi-test-wechat-shutdown"));
    }

    void verificationConsent(String version, String text) {
        // config_key无唯一键；精确重建隔离配置，不用无效的ON DUPLICATE KEY假装覆盖。
        for (String name :
                List.of("vi.biz.verification.consent.version", "vi.biz.verification.consent.text"))
            jdbc.update("DELETE FROM sys_config WHERE config_key=?", name);
        jdbc.update(
                "INSERT INTO sys_config(config_name,config_key,config_value,config_type) VALUES"
                        + " ('隔离核验同意版本','vi.biz.verification.consent.version',?,'N'),"
                        + " ('隔离核验同意原文','vi.biz.verification.consent.text',?,'N')",
                version,
                text);
    }

    JsonNode configureWechat(long company) {
        JsonNode profile =
                data(
                        call(
                                "POST",
                                INTEGRATIONS + "/profiles",
                                Map.of(
                                        "kind",
                                        "WECHAT_APP",
                                        "name",
                                        "Q04隔离微信应用配置",
                                        "scope",
                                        Map.of(
                                                "ownerDeptId",
                                                Long.toString(company),
                                                "environment",
                                                "TEST")),
                                rootAdmin));
        return publishWechat(profile, APP_ID, APP_SECRET, true);
    }

    JsonNode publishWechat(JsonNode profile, String appId, String secret, boolean enabled) {
        WECHAT.addApp(appId, secret);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("appId", appId);
        config.put("appName", "Q04隔离设备认证");
        config.put("enabled", enabled);
        JsonNode saved =
                data(
                        call(
                                "PUT",
                                integrationPath(profile) + "/draft",
                                Map.of(
                                        "kind",
                                        "WECHAT_APP",
                                        "expectedVersion",
                                        profile.path("version").asLong(),
                                        "config",
                                        config,
                                        "credentials",
                                        Map.of(
                                                "appSecret",
                                                Map.of("action", "REPLACE", "value", secret)),
                                        "reason",
                                        "保存真实微信应用测试配置"),
                                rootAdmin));
        JsonNode check =
                data(
                        call(
                                "POST",
                                integrationPath(profile) + "/checks",
                                Map.of(
                                        "revisionId",
                                        saved.path("draftRevisionId").asText(),
                                        "expectedVersion",
                                        saved.path("version").asLong(),
                                        "level",
                                        "LOCAL"),
                                rootAdmin));
        assertEquals("PASS", check.path("status").asText(), check.toString());
        data(
                call(
                        "POST",
                        integrationPath(profile) + "/publish",
                        Map.of(
                                "revisionId",
                                saved.path("draftRevisionId").asText(),
                                "expectedVersion",
                                saved.path("version").asLong(),
                                "contentHash",
                                saved.path("draft").path("contentHash").asText(),
                                "checkId",
                                check.path("checkId").asText(),
                                "reason",
                                "发布隔离微信应用配置"),
                        rootAdmin));
        return data(call("GET", integrationPath(profile), null, rootAdmin));
    }

    String integrationPath(JsonNode profile) {
        return INTEGRATIONS + "/profiles/" + profile.path("profileId").asText();
    }

    Map<String, Object> deviceBody(Student person) {
        JsonNode application = qualification(person);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("applicationId", Long.toString(person.applicationId()));
        body.put("expectedApplicationVersion", application.path("version").asLong());
        body.put("expectedCompanyRevision", companyEpoch(person));
        body.put("expectedMaterialRevision", application.path("materialRevision").asLong());
        body.put("consentVersion", FACE_CONSENT_VERSION);
        body.put("consentTextHash", sha(FACE_CONSENT_TEXT.getBytes(StandardCharsets.UTF_8)));
        body.put("acceptedConsent", true);
        return body;
    }

    ResponseEntity<String> createDevice(Student person, Map<String, Object> body, String key) {
        return miniCallKey("POST", FACE, body, person.token(), key);
    }

    JsonNode createDevice(Student person) {
        return data(createDevice(person, deviceBody(person), key()));
    }

    ResponseEntity<String> getDevice(Student person, String id) {
        return miniCall("GET", FACE_RESULTS + "/" + id, null, person.token());
    }

    String openId(Student person) {
        return jdbc.queryForObject(
                "SELECT open_id FROM vi_account WHERE id=?", String.class, person.accountId());
    }

    Map<String, Object> signedDeviceBody(Student person, JsonNode session) {
        String openId = openId(person);
        WECHAT.bindOpenId(APP_ID, openId);
        String result = WECHAT.resultJson(session.path("challenge").asText());
        return Map.of(
                "expectedVersion",
                session.path("version").asLong(),
                "resultJSON",
                result,
                "resultJSONSignature",
                WECHAT.sign(openId, result));
    }

    ResponseEntity<String> confirmDevice(
            Student person, JsonNode session, Map<String, Object> body, String key) {
        return miniCallKey(
                "POST",
                FACE_RESULTS + "/" + session.path("verificationSessionId").asText() + "/confirm",
                body,
                person.token(),
                key);
    }

    JsonNode passDevice(Student person) {
        JsonNode session = createDevice(person);
        assertEquals("CREATED", session.path("status").asText());
        JsonNode result =
                data(confirmDevice(person, session, signedDeviceBody(person, session), key()));
        assertEquals("PASSED", result.path("status").asText());
        assertTrue(result.path("resultAvailable").asBoolean());
        assertNull(WECHAT.failure.get(), "本地微信端点须通过应用、OpenID与原文签名独立校验");
        return result;
    }

    Map<String, Object> manualBody(Student person, String type, String conclusion, String note) {
        JsonNode application = qualification(person);
        return Map.of(
                "applicationId",
                Long.toString(person.applicationId()),
                "expectedApplicationVersion",
                application.path("version").asLong(),
                "expectedCompanyRevision",
                companyEpoch(person),
                "expectedMaterialRevision",
                application.path("materialRevision").asLong(),
                "type",
                type,
                "conclusion",
                conclusion,
                "note",
                note);
    }

    JsonNode submitManual(
            Student person, String type, String conclusion, String note, String operator) {
        return data(call("POST", MANUAL, manualBody(person, type, conclusion, note), operator));
    }

    Map<String, Object> manualReviewBody(JsonNode row, String decision, String note) {
        return Map.of(
                "expectedVersion",
                row.path("version").asLong(),
                "decision",
                decision,
                "note",
                note);
    }

    JsonNode reviewManual(JsonNode row, String decision, String note, String reviewer) {
        return data(
                call(
                        "POST",
                        MANUAL + "/" + row.path("recordId").asText() + "/review",
                        manualReviewBody(row, decision, note),
                        reviewer));
    }

    JsonNode confirmManual(Student person, String type, String operator, String reviewer) {
        JsonNode row = submitManual(person, type, "PASSED", "", operator);
        assertEquals("PENDING_REVIEW", row.path("status").asText());
        JsonNode result = reviewManual(row, "CONFIRM", "", reviewer);
        assertEquals("CONFIRMED", result.path("status").asText());
        return result;
    }
}
