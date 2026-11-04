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
/** 验证付款开关独立、线下选择不等于到账、原交易身份和凭据轮换；供应商请求仅走回环替身。 */
class X02PaymentIT extends PricingHttpFixture {
    @Autowired com.xinyi.rongli.payment.service.PaymentHistoryGateway historical;
    @Autowired com.xinyi.rongli.order.service.PaymentPolicyGateway booking;
    @Autowired org.springframework.transaction.PlatformTransactionManager manager;
    @Autowired IntegrationKeyProperties keys;
    @Autowired com.xinyi.rongli.identity.service.MiniTokenService miniTokens;
    @Autowired com.xinyi.rongli.identity.mapper.ViSessionMapper sessions;
    static final String BASE = "/api/admin/integrations";
    static final AtomicInteger calls = new AtomicInteger();
    static volatile boolean badSignature, slow, timeout;
    static volatile String requestBody, requestFailure;
    static volatile String queryTrade, queryRefund;
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
                    "/v3/",
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
                                    (e.getRequestMethod()
                                                    + "\n"
                                                    + e.getRequestURI()
                                                    + "\n"
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
                            String
                                    body =
                                            e.getRequestMethod().equals("POST")
                                                    ? "{\"prepay_id\":\"isolated_prepay_123\"}"
                                                    : e.getRequestURI()
                                                                    .getPath()
                                                                    .contains("/refund/")
                                                            ? ViJson.write(
                                                                    Map.of(
                                                                            "out_refund_no",
                                                                            queryRefund,
                                                                            "out_trade_no",
                                                                            queryTrade,
                                                                            "status",
                                                                            "SUCCESS"))
                                                            : ViJson.write(
                                                                    Map.of(
                                                                            "appid",
                                                                            "wx0000000000000000",
                                                                            "mchid",
                                                                            "1234567890",
                                                                            "out_trade_no",
                                                                            queryTrade,
                                                                            "trade_state",
                                                                            "SUCCESS",
                                                                            "amount",
                                                                            Map.of(
                                                                                    "total",
                                                                                    12345,
                                                                                    "currency",
                                                                                    "CNY"))),
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
        no = "X02" + UUID.randomUUID().toString().replace("-", "");
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

    Map<String, Object> offlineConfig(boolean wechat, boolean bank) {
        var c = new LinkedHashMap<String, Object>(policyConfig(wechat));
        if (!wechat) c.remove("wechatMerchant");
        c.put("offlineEnabled", true);
        c.put("offlineMethods", bank ? List.of("BANK_TRANSFER") : List.of("CASH"));
        c.put("offlineReceiverName", "企业甲");
        c.put("offlineInstructions", "请付款后联系企业财务登记凭证");
        c.put("offlineContactName", "财务");
        c.put("offlineContactPhone", "13900008888");
        if (bank) {
            c.put("offlineBankName", "测试银行");
            c.put("offlineAccountName", "企业甲");
        }
        return c;
    }

    void enableOffline(boolean wechat, boolean bank) {
        policy =
                pub(
                        save(
                                policy,
                                offlineConfig(wechat, bank),
                                bank ? replace("offlineBankAccount", "6222000011112222") : null));
    }

    ResponseEntity<String> offline(Map<String, Object> body, String key) {
        return ownerCall("POST", "/offline-payment", body, owner, key);
    }

    JsonNode options() {
        return data(ownerCall("GET", "/payment-options", null, owner, null));
    }

    @Test
    void onlyOfflineDoesNotNeedMerchantAndSelectionIsNotPaid() {
        enableOffline(false, false);
        var methods = options().path("options");
        assertEquals(1, methods.size());
        assertEquals("OFFLINE", methods.get(0).path("method").asText());
        var input = intent();
        String key = UUID.randomUUID().toString(), expires = state("expires_at");
        var r = data(offline(input, key));
        assertEquals("OFFLINE", r.path("paymentMethod").asText());
        assertEquals("PENDING", r.path("payStatus").asText());
        assertEquals(0, r.path("paidAmount").asInt());
        assertEquals(r, data(offline(input, key)));
        assertEquals(expires, state("expires_at"));
        assertEquals("HELD", state("capacity_resolution"));
        assertEquals(0, calls.get());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='OFFLINE_PAYMENT_INTENT'",
                        Integer.class));
    }

    @Test
    void onlyWechatAndBothEnabledAreIndependent() {
        assertEquals(1, options().path("options").size());
        assertEquals("WECHAT", options().path("options").get(0).path("method").asText());
        enableOffline(true, false);
        assertEquals(2, options().path("options").size());
        data(offline(intent(), UUID.randomUUID().toString()));
        assertEquals(409, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals("OFFLINE", state("payment_method"));
        assertEquals(0, calls.get());
    }

    @Test
    void bothOffNeedsConfirmationAndStopsBookingNotFree() {
        var c = new LinkedHashMap<>(policyConfig(false));
        c.put("offlineEnabled", false);
        var draft = save(policy, c, null);
        var check = local(draft);
        var body = publicationBody(draft, check);
        assertEquals(
                422, call("POST", path(draft) + "/publish", body, root).getStatusCode().value());
        body.put("acknowledgeNoPaymentMethods", true);
        data(call("POST", path(draft) + "/publish", body, root));
        assertFalse(options().path("newPaymentAllowed").asBoolean());
        assertEquals(0, options().path("options").size());
        long station =
                jdbc.queryForObject(
                        "SELECT station_id FROM vi_order WHERE id=?", Long.class, orderId);
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        new org.springframework.transaction.support.TransactionTemplate(manager)
                                .execute(
                                        tx ->
                                                booking.requireForStation(
                                                        station, "wx0000000000000000")));
        assertEquals(422, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(0, calls.get());
    }

    @Test
    void secretFieldsValidateWithoutProviderTraffic() {
        var c = new LinkedHashMap<>(merchantConfig);
        c.remove("mchid");
        c.remove("wechatPayPublicKeyPem");
        var m = save(profile("WECHAT_PAY"), c, null);
        var checked = local(m);
        assertEquals("FAIL", checked.path("status").asText());
        String issues = checked.path("issues").toString();
        for (String field :
                List.of(
                        "config.mchid",
                        "credentials.merchantPrivateKey",
                        "credentials.apiV3Key",
                        "config.wechatPayPublicKeyPem")) assertTrue(issues.contains(field), field);
        assertEquals(
                422,
                call("POST", path(m) + "/publish", publicationBody(m, checked), root)
                        .getStatusCode()
                        .value());
        m = save(merchant, merchantConfig, replace("apiV3Key", "short-key"));
        assertTrue(local(m).path("issues").toString().contains("APIv3密钥须为32字节"));
        assertEquals(0, calls.get());
    }

    @Test
    void merchantCertificateMustMatchSignatureKey() throws Exception {
        var dir = java.nio.file.Files.createTempDirectory("x02-cert-");
        var proc =
                new ProcessBuilder(
                                "openssl",
                                "req",
                                "-x509",
                                "-newkey",
                                "rsa:2048",
                                "-nodes",
                                "-keyout",
                                dir.resolve("key.pem").toString(),
                                "-out",
                                dir.resolve("cert.pem").toString(),
                                "-days",
                                "2",
                                "-subj",
                                "/CN=isolated-x02")
                        .redirectErrorStream(true)
                        .redirectOutput(dir.resolve("output.log").toFile())
                        .start();
        assertTrue(proc.waitFor(10, TimeUnit.SECONDS));
        assertEquals(0, proc.exitValue());
        String pem = java.nio.file.Files.readString(dir.resolve("cert.pem"));
        var cert = com.wechat.pay.java.core.util.PemUtil.loadX509FromString(pem);
        var cfg = new LinkedHashMap<>(merchantConfig);
        cfg.put("merchantCertificatePem", pem);
        cfg.put("merchantCertificateSerial", cert.getSerialNumber().toString(16));
        var draft = save(merchant, cfg, null);
        assertTrue(local(draft).path("issues").toString().contains("CERTIFICATE_INVALID"));
        assertEquals(0, calls.get());
    }

    @Test
    void appMismatchAndNewPrepayAfterSwitchOffDoNotReachProvider() {
        jdbc.update("UPDATE vi_account SET app_id='wx1111111111111111' WHERE id=?", account);
        assertEquals(422, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        jdbc.update("UPDATE vi_account SET app_id='wx0000000000000000' WHERE id=?", account);
        enableOffline(false, false);
        assertEquals(422, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(0, calls.get());
    }

    @Test
    void bankAccountOnlyForOwnerAfterSelectionAndOriginalRevisionSurvivesChanges() {
        enableOffline(false, true);
        assertFalse(options().toString().contains("6222000011112222"));
        assertFalse(
                data(call("GET", path(policy), null, root))
                        .toString()
                        .contains("6222000011112222"));
        data(offline(intent(), UUID.randomUUID().toString()));
        String original = state("policy_revision_id");
        assertEquals(
                "6222000011112222",
                options().path("options").get(0).path("receiverAccountDisplay").asText());
        policy =
                pub(
                        save(
                                policy,
                                offlineConfig(false, true),
                                replace("offlineBankAccount", "6222999988887777")));
        assertEquals(original, state("policy_revision_id"));
        assertEquals(
                "6222000011112222",
                options().path("options").get(0).path("receiverAccountDisplay").asText());
        assertEquals(
                404,
                ownerCall("GET", "/payment-options", null, mini(), null).getStatusCode().value());
        jdbc.update(
                "UPDATE vi_payment_order SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE order_id=?",
                orderId);
        assertFalse(options().toString().contains("6222000011112222"));
    }

    @Test
    // 同步起跑的线下选择和微信请求只能有一个方式获胜，不允许双通道并存。
    void offlineUnknownWechatRaceHasOneWinner() throws Exception {
        enableOffline(true, false);
        var input = intent();
        var pool = Executors.newFixedThreadPool(2);
        var gate = new CountDownLatch(1);
        try {
            var a =
                    pool.submit(
                            () -> {
                                gate.await();
                                return pay(input, UUID.randomUUID().toString());
                            });
            var b =
                    pool.submit(
                            () -> {
                                gate.await();
                                return offline(input, UUID.randomUUID().toString());
                            });
            gate.countDown();
            var statuses =
                    List.of(
                            a.get(10, TimeUnit.SECONDS).getStatusCode().value(),
                            b.get(10, TimeUnit.SECONDS).getStatusCode().value());
            assertEquals(1, statuses.stream().filter(v -> v == 200).count());
            assertEquals(1, statuses.stream().filter(v -> v == 409).count());
            assertTrue(calls.get() <= 1);
            assertEquals("0", state("paid_fen"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void unknownWechatCannotBecomeOffline() {
        enableOffline(true, false);
        badSignature = true;
        assertEquals(503, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(409, offline(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals("WECHAT", state("payment_method"));
        assertEquals("UNKNOWN", state("status"));
        enableOffline(false, false);
        assertFalse(options().path("newPaymentAllowed").asBoolean());
    }

    @Test
    void offlineRejectsStaleForeignExpiredAndRollsBackOutbox() {
        enableOffline(false, false);
        var input = intent();
        assertEquals(
                404,
                ownerCall("POST", "/offline-payment", input, mini(), UUID.randomUUID().toString())
                        .getStatusCode()
                        .value());
        assertEquals(
                409,
                offline(
                                Map.of(
                                        "expectedVersion",
                                        99,
                                        "expectedPolicyRevisionId",
                                        input.get("expectedPolicyRevisionId")),
                                UUID.randomUUID().toString())
                        .getStatusCode()
                        .value());
        jdbc.execute(
                "CREATE TRIGGER x02_outbox_fail BEFORE INSERT ON vi_outbox FOR EACH ROW SIGNAL"
                        + " SQLSTATE '45000' SET MESSAGE_TEXT='isolated rollback'");
        try {
            assertEquals(500, offline(input, UUID.randomUUID().toString()).getStatusCode().value());
            assertEquals("UNSELECTED", state("payment_method"));
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_event_log WHERE"
                                    + " action='OFFLINE_PAYMENT_INTENT'",
                            Integer.class));
        } finally {
            jdbc.execute("DROP TRIGGER x02_outbox_fail");
        }
        data(offline(input, UUID.randomUUID().toString()));
        jdbc.update(
                "UPDATE vi_payment_order SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                        + " SECOND) WHERE order_id=?",
                orderId);
        assertEquals(409, offline(intent(), UUID.randomUUID().toString()).getStatusCode().value());
    }

    @Test
    void disabledPolicyKeepsOriginalQueryAndValidRotatedSigningKey() throws Exception {
        data(pay(intent(), UUID.randomUUID().toString()));
        queryTrade = state("out_trade_no");
        enableOffline(false, false);
        assertEquals("SUCCESS", historical.queryOrder(orderId).getTradeState().toString());
        var rsa = KeyPairGenerator.getInstance("RSA");
        rsa.initialize(2048);
        merchantKey = rsa.generateKeyPair();
        merchant =
                pub(
                        save(
                                merchant,
                                merchantConfig,
                                replace(
                                        "merchantPrivateKey",
                                        pem(
                                                "PRIVATE KEY",
                                                merchantKey.getPrivate().getEncoded()))));
        assertEquals("SUCCESS", historical.queryOrder(orderId).getTradeState().toString());
        assertEquals("1234567890", state("mchid"));
        assertNull(requestFailure);
        assertEquals("0", state("paid_fen"));
    }

    @Test
    // 新单开关关闭后仍需验签解密原身份通知；测试材料不会从真实商户读取。
    void disabledPolicyStillValidatesAndDecryptsOriginalCallback() throws Exception {
        data(pay(intent(), UUID.randomUUID().toString()));
        enableOffline(false, false);
        String at = Long.toString(java.time.Instant.now().getEpochSecond()),
                nonce = "notify-test-nonce",
                rn = "123456789012",
                aad = "transaction";
        String plain =
                ViJson.write(
                        Map.of(
                                "appid",
                                "wx0000000000000000",
                                "mchid",
                                "1234567890",
                                "out_trade_no",
                                state("out_trade_no"),
                                "trade_state",
                                "SUCCESS",
                                "amount",
                                Map.of("total", 12345, "currency", "CNY")));
        var cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(
                        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "AES"),
                new javax.crypto.spec.GCMParameterSpec(128, rn.getBytes(StandardCharsets.UTF_8)));
        cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        String body =
                ViJson.write(
                        Map.of(
                                "id",
                                "isolated-notify",
                                "create_time",
                                java.time.Instant.now().toString(),
                                "event_type",
                                "TRANSACTION.SUCCESS",
                                "resource_type",
                                "encrypt-resource",
                                "resource",
                                Map.of(
                                        "algorithm",
                                        "AEAD_AES_256_GCM",
                                        "nonce",
                                        rn,
                                        "associated_data",
                                        aad,
                                        "ciphertext",
                                        Base64.getEncoder()
                                                .encodeToString(
                                                        cipher.doFinal(
                                                                plain.getBytes(
                                                                        StandardCharsets
                                                                                .UTF_8))))));
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(providerKey.getPrivate());
        signature.update((at + "\n" + nonce + "\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
        var request =
                new com.wechat.pay.java.core.notification.RequestParam.Builder()
                        .serialNumber("PUB_KEY_ID_TEST")
                        .timestamp(at)
                        .nonce(nonce)
                        .signature(Base64.getEncoder().encodeToString(signature.sign()))
                        .body(body)
                        .build();
        assertEquals(
                "SUCCESS",
                historical.readNotification(orderId, request).getTradeState().toString());
        assertEquals("0", state("paid_fen"));
        merchant =
                pub(
                        save(
                                merchant,
                                merchantConfig,
                                replace("apiV3Key", "abcdef0123456789abcdef0123456789")));
        assertEquals(
                "SUCCESS",
                historical.readNotification(orderId, request).getTradeState().toString());
        var forged =
                new com.wechat.pay.java.core.notification.RequestParam.Builder()
                        .serialNumber("PUB_KEY_ID_TEST")
                        .timestamp(at)
                        .nonce(nonce)
                        .signature(Base64.getEncoder().encodeToString(new byte[256]))
                        .body(body)
                        .build();
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () -> historical.readNotification(orderId, forged));
    }

    @Test
    void refundQueryRetainsMerchantAfterSwitchOff() {
        data(pay(intent(), UUID.randomUUID().toString()));
        queryTrade = state("out_trade_no");
        queryRefund = "X02REFUND" + UUID.randomUUID().toString().replace("-", "");
        long payment =
                jdbc.queryForObject(
                        "SELECT id FROM vi_payment_order WHERE order_id=?", Long.class, orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_refund_order(case_id,review_revision,payment_id,out_refund_no,amount_fen,status,payment_method,execution_mode)"
                    + " VALUES(?,1,?,?,12345,'UNKNOWN','WECHAT','WECHAT_API')",
                SEQ.incrementAndGet(),
                payment,
                queryRefund);
        long refund =
                jdbc.queryForObject(
                        "SELECT id FROM vi_refund_order WHERE out_refund_no=?",
                        Long.class,
                        queryRefund);
        enableOffline(false, false);
        var result = historical.queryRefund(refund);
        assertEquals(queryRefund, result.getOutRefundNo());
        assertEquals(queryTrade, result.getOutTradeNo());
        assertNull(requestFailure);
        assertEquals(
                "UNKNOWN",
                jdbc.queryForObject(
                        "SELECT status FROM vi_refund_order WHERE id=?", String.class, refund));
    }

    @Test
    void merchantReadOnlyCallbacksComeFromDeployment() {
        var p = data(call("GET", path(merchant), null, root));
        assertEquals(
                "https://isolated.invalid/api/notify/wechat/pay",
                p.path("generatedPayNotifyUrl").asText());
        assertEquals(
                "https://isolated.invalid/api/notify/wechat/refund",
                p.path("generatedRefundNotifyUrl").asText());
        assertFalse(p.path("historicalMaintenanceRequired").asBoolean());
        data(pay(intent(), UUID.randomUUID().toString()));
        assertTrue(
                data(call("GET", path(merchant), null, root))
                        .path("historicalMaintenanceRequired")
                        .asBoolean());
    }

    @Test
    void actualOfflineResponseMatchesPrimaryContract() throws Exception {
        enableOffline(false, false);
        var response = offline(intent(), UUID.randomUUID().toString());
        data(response);
        var dir = java.nio.file.Files.createTempDirectory("x02-contract-");
        java.nio.file.Files.writeString(
                dir.resolve("options.json"),
                ownerCall("GET", "/payment-options", null, owner, null).getBody());
        java.nio.file.Files.writeString(dir.resolve("offline.json"), response.getBody());
        var repo = java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath();
        if (!java.nio.file.Files.exists(repo.resolve("docs"))) repo = repo.getParent();
        try (var in = getClass().getResourceAsStream("/integration/x02_contract.py")) {
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
        assertEquals(0, process.exitValue(), "X02 private contract log: " + dir);
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
