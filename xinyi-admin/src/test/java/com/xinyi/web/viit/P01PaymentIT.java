package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.integration.config.IntegrationKeyProperties;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1",
            "vi.payment.public-origin=https://isolated.invalid",
            "logging.level.com.xinyi.web.viit=INFO"
        })
@ActiveProfiles("druid")
/** 使用临时RSA材料和回环HTTP响应验证官方SDK签验及预支付租约，绝不调用真实收款接口。 */
class P01PaymentIT extends PricingHttpFixture {
    @Autowired IntegrationKeyProperties keys;
    @Autowired com.xinyi.rongli.identity.service.MiniTokenService miniTokens;
    @Autowired com.xinyi.rongli.identity.mapper.ViSessionMapper sessions;
    static final String BASE = "/api/admin/integrations";
    static final AtomicInteger calls = new AtomicInteger();
    static volatile boolean badSignature, slow, timeout;
    static volatile String requestBody, requestFailure;
    static KeyPair merchantKey, providerKey;
    static com.sun.net.httpserver.HttpServer server;

    static {
        try {
            var rsa = KeyPairGenerator.getInstance("RSA");
            rsa.initialize(2048);
            merchantKey = rsa.generateKeyPair();
            providerKey = rsa.generateKeyPair();
            server =
                    com.sun.net.httpserver.HttpServer.create(
                            new java.net.InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext(
                    "/v3/pay/transactions/jsapi",
                    e -> {
                        try {
                            calls.incrementAndGet();
                            requestBody =
                                    new String(
                                            e.getRequestBody().readAllBytes(),
                                            StandardCharsets.UTF_8);
                            String authorization = e.getRequestHeaders().getFirst("Authorization");
                            var attrs = new HashMap<String, String>();
                            var matcher =
                                    java.util.regex.Pattern.compile("(\\w+)=\"([^\"]*)\"")
                                            .matcher(authorization);
                            while (matcher.find()) attrs.put(matcher.group(1), matcher.group(2));
                            var verify = Signature.getInstance("SHA256withRSA");
                            verify.initVerify(merchantKey.getPublic());
                            verify.update(
                                    ("POST\n/v3/pay/transactions/jsapi\n"
                                                    + attrs.get("timestamp")
                                                    + "\n"
                                                    + attrs.get("nonce_str")
                                                    + "\n"
                                                    + requestBody
                                                    + "\n")
                                            .getBytes(StandardCharsets.UTF_8));
                            if (!verify.verify(Base64.getDecoder().decode(attrs.get("signature"))))
                                throw new IllegalStateException("request signature invalid");
                            if (slow) Thread.sleep(1200);
                            if (timeout) Thread.sleep(6500);
                            String body = "{\"prepay_id\":\"isolated_prepay_123\"}",
                                    at = Long.toString(java.time.Instant.now().getEpochSecond()),
                                    nonce = "provider-test-nonce";
                            var sign = Signature.getInstance("SHA256withRSA");
                            sign.initSign(providerKey.getPrivate());
                            sign.update(
                                    (at + "\n" + nonce + "\n" + body + "\n")
                                            .getBytes(StandardCharsets.UTF_8));
                            e.getResponseHeaders().set("Wechatpay-Timestamp", at);
                            e.getResponseHeaders().set("Wechatpay-Nonce", nonce);
                            e.getResponseHeaders().set("Wechatpay-Serial", "PUB_KEY_ID_TEST");
                            e.getResponseHeaders()
                                    .set(
                                            "Wechatpay-Signature",
                                            badSignature
                                                    ? Base64.getEncoder()
                                                            .encodeToString(new byte[256])
                                                    : Base64.getEncoder()
                                                            .encodeToString(sign.sign()));
                            e.getResponseHeaders().set("Content-Type", "application/json");
                            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                            e.sendResponseHeaders(200, bytes.length);
                            e.getResponseBody().write(bytes);
                        } catch (Exception failure) {
                            requestFailure = failure.getClass().getSimpleName();
                            e.sendResponseHeaders(500, -1);
                        } finally {
                            e.close();
                        }
                    });
            server.start();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void paymentProperties(DynamicPropertyRegistry r) {
        r.add(
                "vi.payment.test-endpoint",
                () -> "http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterAll
    static void stop() {
        server.stop(0);
    }

    String root, owner, no;
    JsonNode policy, merchant;
    Map<String, Object> merchantConfig;
    long account, orderId;

    @BeforeEach
    void setup() {
        calls.set(0);
        badSignature = false;
        slow = false;
        timeout = false;
        requestFailure = null;
        for (String t :
                List.of(
                        "vi_payment_order",
                        "vi_order",
                        "vi_integration_test_recipient",
                        "vi_integration_publication",
                        "vi_integration_check",
                        "vi_integration_secret",
                        "vi_integration_revision",
                        "vi_integration_profile")) jdbc.execute("DELETE FROM " + t);
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        keys.setVersions(new HashMap<>(Map.of("p01", Base64.getEncoder().encodeToString(key))));
        keys.setCurrentVersion("p01");
        root = rootToken();
        owner = mini();
        account = miniTokens.resolveAccess(owner).accountId();
        var app =
                pub(
                        save(
                                profile("WECHAT_APP"),
                                Map.of(
                                        "enabled",
                                        true,
                                        "appId",
                                        "wx0000000000000000",
                                        "appName",
                                        "隔离应用"),
                                replace("appSecret", "isolated-secret")));
        merchantConfig =
                new LinkedHashMap<>(
                        Map.of(
                                "merchantName",
                                "隔离商户",
                                "mchid",
                                "1234567890",
                                "merchantCertificateSerial",
                                "AABB",
                                "verificationMode",
                                "WECHATPAY_PUBLIC_KEY",
                                "wechatPayPublicKeyId",
                                "PUB_KEY_ID_TEST",
                                "wechatPayPublicKeyPem",
                                pem("PUBLIC KEY", providerKey.getPublic().getEncoded()),
                                "bindingAcknowledged",
                                true,
                                "appProfile",
                                Map.of(
                                        "profileId",
                                        app.path("profileId").asText(),
                                        "revisionId",
                                        app.path("activeRevisionId").asText())));
        merchant =
                pub(
                        save(
                                profile("WECHAT_PAY"),
                                merchantConfig,
                                Map.of(
                                        "merchantPrivateKey",
                                        Map.of(
                                                "action",
                                                "REPLACE",
                                                "value",
                                                pem(
                                                        "PRIVATE KEY",
                                                        merchantKey.getPrivate().getEncoded())),
                                        "apiV3Key",
                                        Map.of(
                                                "action",
                                                "REPLACE",
                                                "value",
                                                "0123456789abcdef0123456789abcdef"))));
        policy = pub(save(profile("PAYMENT_POLICY"), policyConfig(true), null));
        no = "P01" + UUID.randomUUID().toString().replace("-", "");
        long station = station(SITE_DEPT);
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,"
                    + "fulfillment_status,custody_type,custody_ref_id,pickup_starts_at,pickup_ends_at,"
                    + "pay_expires_at,total_fen,terms_version)"
                    + " VALUES(?,?,?,?,?,'{}','PENDING_PAY','OWNER',?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 2 DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 3"
                    + " DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'test')",
                no,
                account,
                station,
                SITE_DEPT,
                SEQ.incrementAndGet(),
                account);
        orderId = jdbc.queryForObject("SELECT id FROM vi_order WHERE order_no=?", Long.class, no);
        jdbc.update(
                "INSERT INTO"
                    + " vi_payment_order(order_id,out_trade_no,status,total_fen,expires_at,payment_method,"
                    + "receiver_dept_id,policy_revision_id,capacity_resolution) SELECT"
                    + " id,?,'PENDING',total_fen,pay_expires_at,'UNSELECTED',?,?,'HELD' FROM"
                    + " vi_order WHERE id=?",
                UUID.randomUUID().toString().replace("-", ""),
                COMPANY_A,
                policy.path("activeRevisionId").asLong(),
                orderId);
    }

    static String pem(String kind, byte[] bytes) {
        return "-----BEGIN "
                + kind
                + "-----\n"
                + Base64.getEncoder().encodeToString(bytes)
                + "\n-----END "
                + kind
                + "-----";
    }

    Map<String, Object> policyConfig(boolean enabled) {
        var m = new LinkedHashMap<String, Object>();
        m.put("offlineEnabled", !enabled);
        m.put("wechatEnabled", enabled);
        m.put("offlineCollectionMode", "PREPAID_MANUAL_VERIFIED");
        m.put(
                "wechatMerchant",
                Map.of(
                        "profileId",
                        merchant.path("profileId").asText(),
                        "revisionId",
                        merchant.path("activeRevisionId").asText()));
        if (!enabled) {
            m.put("offlineMethods", List.of("CASH"));
            m.put("offlineReceiverName", "隔离公司");
            m.put("offlineInstructions", "线下核实");
            m.put("offlineContactName", "财务");
            m.put("offlineContactPhone", "13900008888");
        }
        return m;
    }

    ResponseEntity<String> ownerCall(
            String method, String suffix, Object body, String token, String key) {
        var h = new HttpHeaders();
        h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        h.setBearerAuth(token);
        h.set("X-Client-Type", "owner");
        if (key != null) h.set("Idempotency-Key", key);
        return http.exchange(
                "http://127.0.0.1:" + port + "/api/app/owner/orders/" + no + suffix,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, h),
                String.class);
    }

    Map<String, Object> intent() {
        var p = data(ownerCall("GET", "/payment-options", null, owner, null));
        return Map.of(
                "expectedVersion",
                p.path("paymentVersion").asLong(),
                "expectedPolicyRevisionId",
                p.path("policyRevisionId").asText());
    }

    ResponseEntity<String> pay(Map<String, Object> body, String key) {
        return ownerCall("POST", "/prepay", body, owner, key);
    }

    String state(String field) {
        return jdbc.queryForObject(
                "SELECT " + field + " FROM vi_payment_order WHERE order_id=?",
                String.class,
                orderId);
    }

    @Test
    void realSdkSignedRequestResponseAndLaunchReplayKeepDeadline() throws Exception {
        var input = intent();
        String key = UUID.randomUUID().toString(), expires = state("expires_at");
        var response = pay(input, key);
        var result = data(response);
        assertNull(requestFailure);
        assertEquals("no-store", response.getHeaders().getFirst("Cache-Control"));
        assertEquals(12345, ViJson.tree(requestBody).path("amount").path("total").asInt());
        assertEquals("1234567890", ViJson.tree(requestBody).path("mchid").asText());
        assertEquals(state("out_trade_no"), ViJson.tree(requestBody).path("out_trade_no").asText());
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(merchantKey.getPublic());
        signature.update(
                ("wx0000000000000000\n"
                                + result.path("timeStamp").asText()
                                + "\n"
                                + result.path("nonceStr").asText()
                                + "\n"
                                + result.path("package").asText()
                                + "\n")
                        .getBytes(StandardCharsets.UTF_8));
        assertTrue(signature.verify(Base64.getDecoder().decode(result.path("paySign").asText())));
        assertEquals(result.path("package"), data(pay(input, key)).path("package"));
        assertEquals(1, calls.get());
        assertEquals(expires, state("expires_at"));
        assertEquals("PENDING", state("status"));
        assertEquals("0", state("paid_fen"));
        assertEquals("READY", state("prepay_state"));
        assertFalse(
                new String(
                                jdbc.queryForObject(
                                        "SELECT prepay_request_cipher FROM vi_payment_order WHERE"
                                                + " order_id=?",
                                        byte[].class,
                                        orderId))
                        .contains("openId"));
        assertFalse(
                jdbc.queryForObject(
                                "SELECT GROUP_CONCAT(result_json) FROM vi_idempotency",
                                String.class)
                        .contains("isolated_prepay"));
    }

    @Test
    void badSignatureIsUnknownAndStableRetryNeverUnlocks() {
        var input = intent();
        String key = UUID.randomUUID().toString();
        badSignature = true;
        assertEquals(503, pay(input, key).getStatusCode().value());
        assertEquals("UNKNOWN", state("status"));
        assertEquals("WECHAT", state("payment_method"));
        String original = requestBody;
        badSignature = false;
        data(pay(input, key));
        assertEquals(original, requestBody);
        assertEquals(2, calls.get());
    }

    @Test
    void expiredReplayAndForeignOwnerDenied() {
        var input = intent();
        String key = UUID.randomUUID().toString();
        data(pay(input, key));
        assertEquals(
                404,
                ownerCall("GET", "/payment-options", null, mini(), null).getStatusCode().value());
        assertEquals(
                404,
                ownerCall("POST", "/prepay", input, mini(), UUID.randomUUID().toString())
                        .getStatusCode()
                        .value());
        jdbc.update(
                "UPDATE vi_payment_order SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE order_id=?",
                orderId);
        assertEquals(409, pay(input, key).getStatusCode().value());
        assertEquals(1, calls.get());
        assertEquals("PENDING", state("status"));
    }

    @Test
    void staleVersionPolicyAndUntrustedAmountsRejected() {
        var input = new LinkedHashMap<>(intent());
        input.put("expectedVersion", 99);
        assertEquals(409, pay(input, UUID.randomUUID().toString()).getStatusCode().value());
        input = new LinkedHashMap<>(intent());
        input.put("expectedPolicyRevisionId", "99999");
        assertEquals(409, pay(input, UUID.randomUUID().toString()).getStatusCode().value());
        input = new LinkedHashMap<>(intent());
        input.put("amount", 1);
        assertEquals(400, pay(input, UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(0, calls.get());
        assertEquals("UNSELECTED", state("payment_method"));
    }

    @Test
    void closingSwitchBlocksNewButPreservesIssuedPrepay() {
        var input = intent();
        String key = UUID.randomUUID().toString();
        data(pay(input, key));
        policy = pub(save(policy, policyConfig(false), null));
        data(pay(input, key));
        assertEquals(1, calls.get());
        assertTrue(
                data(ownerCall("GET", "/payment-options", null, owner, null))
                        .path("options")
                        .get(0)
                        .path("available")
                        .asBoolean());
    }

    @Test
    void unknownCannotRetryAfterSwitchOff() {
        badSignature = true;
        var input = intent();
        String key = UUID.randomUUID().toString();
        assertEquals(503, pay(input, key).getStatusCode().value());
        policy = pub(save(policy, policyConfig(false), null));
        assertEquals(422, pay(input, key).getStatusCode().value());
        assertEquals(1, calls.get());
        assertEquals("UNKNOWN", state("status"));
    }

    @Test
    // 两个请求争用同一预支付租约，渠道调用次数和商户单号必须保持唯一。
    void concurrentRequestsUseSingleLease() throws Exception {
        slow = true;
        var input = intent();
        var pool = Executors.newFixedThreadPool(2);
        try {
            var one = pool.submit(() -> pay(input, UUID.randomUUID().toString()));
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (calls.get() == 0 && System.nanoTime() < until) Thread.sleep(10);
            var second = pay(intent(), UUID.randomUUID().toString());
            assertEquals(409, second.getStatusCode().value());
            data(one.get(10, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void missingRootKeyAndOfflineLockCannotStartProvider() {
        keys.setVersions(Map.of());
        assertEquals(503, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals("UNSELECTED", state("payment_method"));
        jdbc.update(
                "UPDATE vi_payment_order SET"
                        + " payment_method='OFFLINE',method_locked_at=UTC_TIMESTAMP(3) WHERE"
                        + " order_id=?",
                orderId);
        assertEquals(409, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(0, calls.get());
    }

    @Test
    // 渠道请求在途时撤销会话；可保留渠道事实，但不能继续向失效会话返回支付参数。
    void logoutWhileProviderPendingCapturesResultButDoesNotReturnSecrets() throws Exception {
        slow = true;
        var input = intent();
        var pool = Executors.newSingleThreadExecutor();
        try {
            var pending = pool.submit(() -> pay(input, UUID.randomUUID().toString()));
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (calls.get() == 0 && System.nanoTime() < until) Thread.sleep(10);
            jdbc.update(
                    "UPDATE vi_session SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE"
                            + " account_id=?",
                    account);
            var r = pending.get(10, TimeUnit.SECONDS);
            assertEquals(401, r.getStatusCode().value());
            assertFalse(r.getBody().contains("prepay_id"));
            assertEquals("READY", state("prepay_state"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void credentialRotationUsesOriginalIdentityAndChangedMerchantFailsClosed() {
        var input = intent();
        String key = UUID.randomUUID().toString();
        data(pay(input, key));
        merchant =
                pub(
                        save(
                                merchant,
                                merchantConfig,
                                replace("apiV3Key", "abcdef0123456789abcdef0123456789")));
        data(pay(input, key));
        assertEquals(1, calls.get());
        var changed = new LinkedHashMap<>(merchantConfig);
        changed.put("mchid", "9999999999");
        var forbidden = save(merchant, changed, null);
        assertEquals(
                409,
                call(
                                "POST",
                                path(forbidden) + "/publish",
                                publicationBody(forbidden, local(forbidden)),
                                root)
                        .getStatusCode()
                        .value());
        data(pay(input, key));
        assertEquals("1234567890", state("mchid"));
    }

    @Test
    void providerTimeoutPreservesDeadlineAndUnknown() {
        timeout = true;
        String expires = state("expires_at");
        assertEquals(503, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals("UNKNOWN", state("status"));
        assertEquals("WECHAT", state("payment_method"));
        assertEquals(expires, state("expires_at"));
        assertEquals("0", state("paid_fen"));
    }

    @Test
    void successOrCloseRacingProviderIsNeverOverwritten() throws Exception {
        slow = true;
        var input = intent();
        var pool = Executors.newSingleThreadExecutor();
        try {
            var pending = pool.submit(() -> pay(input, UUID.randomUUID().toString()));
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (calls.get() == 0 && System.nanoTime() < until) Thread.sleep(10);
            jdbc.update(
                    "UPDATE vi_payment_order SET"
                        + " status='SUCCESS',paid_fen=total_fen,version=version+1 WHERE order_id=?",
                    orderId);
            assertEquals(409, pending.get(10, TimeUnit.SECONDS).getStatusCode().value());
            assertEquals("SUCCESS", state("status"));
            assertEquals("12345", state("paid_fen"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    // 事件写入失败时最终状态回滚；后续租约重试仍使用原交易身份与截止时间。
    void outboxFailureRollsBackFinalizationAndLeaseRetryIsStable() {
        String trigger = "p01_outbox_fail";
        jdbc.execute(
                "CREATE TRIGGER "
                        + trigger
                        + " BEFORE INSERT ON vi_outbox FOR EACH ROW SIGNAL SQLSTATE '45000' SET"
                        + " MESSAGE_TEXT='isolated p01 rollback'");
        var input = intent();
        String key = UUID.randomUUID().toString();
        try {
            assertEquals(500, pay(input, key).getStatusCode().value());
            assertEquals("REQUESTING", state("prepay_state"));
            assertEquals("UNKNOWN", state("status"));
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_event_log WHERE action='PREPAY_READY'",
                            Integer.class));
        } finally {
            jdbc.execute("DROP TRIGGER " + trigger);
        }
        String trade = state("out_trade_no");
        jdbc.update(
                "UPDATE vi_payment_order SET prepay_lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                        + " 1 SECOND) WHERE order_id=?",
                orderId);
        data(pay(input, key));
        assertEquals(trade, state("out_trade_no"));
        assertEquals(2, calls.get());
    }

    @Test
    void actualHttpEnvelopesMatchPrimaryOpenApi() throws Exception {
        var dir = java.nio.file.Files.createTempDirectory("p01-contract-");
        java.nio.file.Files.writeString(
                dir.resolve("options.json"),
                ownerCall("GET", "/payment-options", null, owner, null).getBody());
        var input = intent();
        var result = pay(input, UUID.randomUUID().toString());
        data(result);
        java.nio.file.Files.writeString(dir.resolve("prepay.json"), result.getBody());
        var repo = java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if (!java.nio.file.Files.exists(repo.resolve("docs"))) repo = repo.getParent();
        try (var in = getClass().getResourceAsStream("/integration/p01_contract.py")) {
            java.nio.file.Files.copy(in, dir.resolve("check.py"));
        }
        var process =
                new ProcessBuilder(
                                System.getenv("VI_PYTHON"),
                                dir.resolve("check.py").toString(),
                                repo.toString(),
                                dir.toString())
                        .redirectErrorStream(true)
                        .redirectOutput(dir.resolve("result.log").toFile())
                        .start();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), "P01 contract private log: " + dir);
    }

    JsonNode profile(String kind) {
        return data(
                call(
                        "POST",
                        BASE + "/profiles",
                        Map.of(
                                "kind",
                                kind,
                                "name",
                                "X01测试配置",
                                "scope",
                                Map.of(
                                        "ownerDeptId",
                                        Long.toString(COMPANY_A),
                                        "environment",
                                        "TEST")),
                        root));
    }

    String path(JsonNode p) {
        return BASE + "/profiles/" + p.path("profileId").asText();
    }

    Map<String, Object> config(boolean enabled) {
        return Map.of(
                "apiVersion",
                "2021-01-11",
                "enabled",
                enabled,
                "secretId",
                "test-id",
                "region",
                "ap-guangzhou",
                "smsSdkAppId",
                "1400000000",
                "signName",
                "测试签名");
    }

    Map<String, Object> draftBody(JsonNode p, Object cfg, Object creds) {
        var b = new LinkedHashMap<String, Object>();
        b.put("kind", p.path("kind").asText());
        b.put("expectedVersion", p.path("version").asLong());
        b.put("config", cfg);
        b.put("reason", "隔离测试保存");
        if (creds != null) b.put("credentials", creds);
        return b;
    }

    JsonNode save(JsonNode p, Object cfg, Object creds) {
        return data(call("PUT", path(p) + "/draft", draftBody(p, cfg, creds), root));
    }

    Object replace(String slot, String value) {
        return Map.of(slot, Map.of("action", "REPLACE", "value", value));
    }

    JsonNode local(JsonNode p) {
        return check(p, p.path("draftRevisionId").asText(), "LOCAL");
    }

    JsonNode check(JsonNode p, String revision, String level) {
        return data(
                call(
                        "POST",
                        path(p) + "/checks",
                        Map.of(
                                "revisionId",
                                revision,
                                "expectedVersion",
                                p.path("version").asLong(),
                                "level",
                                level),
                        root));
    }

    Map<String, Object> publicationBody(JsonNode p, JsonNode check) {
        return new LinkedHashMap<>(
                Map.of(
                        "revisionId",
                        p.path("draftRevisionId").asText(),
                        "expectedVersion",
                        p.path("version").asLong(),
                        "contentHash",
                        p.path("draft").path("contentHash").asText(),
                        "checkId",
                        check.path("checkId").asText(),
                        "reason",
                        "隔离验证发布"));
    }

    JsonNode pub(JsonNode p) {
        data(call("POST", path(p) + "/publish", publicationBody(p, local(p)), root));
        return data(call("GET", path(p), null, root));
    }

    String mini() {
        String open = "x01-" + UUID.randomUUID();
        jdbc.update(
                "INSERT INTO vi_account(app_id,open_id,status)"
                        + " VALUES('wx0000000000000000',?,'ACTIVE')",
                open);
        long account =
                jdbc.queryForObject("SELECT id FROM vi_account WHERE open_id=?", Long.class, open);
        jdbc.update(
                "INSERT INTO vi_account_security(account_id,changed_at) VALUES"
                        + " (?,UTC_TIMESTAMP(3))",
                account);
        var session = new com.xinyi.rongli.identity.domain.ViSession();
        session.setAccountId(account);
        session.setAppId("wx0000000000000000");
        session.setStatus("ACTIVE");
        session.setSessionEpoch(0L);
        session.setContextVersion(0L);
        session.setActiveRole("OWNER");
        session.setAbsoluteExpiresAt(java.time.Instant.now().plusSeconds(3600));
        sessions.insert(session);
        String token = miniTokens.issueForSession(session).accessToken();
        assertEquals(account, miniTokens.resolveAccess(token).accountId());
        return token;
    }
}
