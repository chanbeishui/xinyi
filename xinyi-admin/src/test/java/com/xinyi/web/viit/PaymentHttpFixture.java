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
abstract class PaymentHttpFixture extends PricingHttpFixture {
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
    static volatile String queryTrade, queryRefund, queryState = "NOTPAY";
    static volatile boolean queryFails, closeRaces;
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
                            boolean close = e.getRequestURI().getPath().endsWith("/close");
                            if (close) {
                                queryState = closeRaces ? "SUCCESS" : "CLOSED";
                            }
                            if (queryFails && e.getRequestMethod().equals("GET")) {
                                e.sendResponseHeaders(503, -1);
                                return;
                            }
                            String
                                    body =
                                            close
                                                    ? ""
                                                    : e.getRequestMethod().equals("POST")
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
                                                                                    queryState,
                                                                                    "transaction_id",
                                                                                    "TX_P02_"
                                                                                            + queryTrade,
                                                                                    "success_time",
                                                                                    java.time
                                                                                            .Instant
                                                                                            .now()
                                                                                            .toString(),
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
                            e.sendResponseHeaders(close ? 204 : 200, close ? -1 : bytes.length);
                            if (!close) e.getResponseBody().write(bytes);
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

    String root, owner, no;
    JsonNode policy, merchant;
    Map<String, Object> merchantConfig;
    long account, orderId;

    @BeforeEach
    void setup() {
        queryState = "NOTPAY";
        queryFails = false;
        closeRaces = false;
        calls.set(0);
        badSignature = false;
        slow = false;
        timeout = false;
        requestFailure = null;
        for (String t :
                List.of(
                        "vi_channel_event",
                        "vi_refund_order",
                        "vi_quota_reservation",
                        "vi_order_snapshot",
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
                "INSERT INTO vi_order(order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version) VALUES(?,?,?,?,?,'{}','PENDING_PAY','OWNER',?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 2 DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 3 DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE),12345,'test')",
                no,
                account,
                station,
                SITE_DEPT,
                SEQ.incrementAndGet(),
                account);
        orderId = jdbc.queryForObject("SELECT id FROM vi_order WHERE order_no=?", Long.class, no);
        jdbc.update(
                "INSERT INTO vi_payment_order(order_id,out_trade_no,status,total_fen,expires_at,payment_method,receiver_dept_id,policy_revision_id,capacity_resolution) SELECT id,?,'PENDING',total_fen,pay_expires_at,'UNSELECTED',?,?,'HELD' FROM vi_order WHERE id=?",
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
                "INSERT INTO vi_account(app_id,open_id,status) VALUES('wx0000000000000000',?,'ACTIVE')",
                open);
        long account =
                jdbc.queryForObject("SELECT id FROM vi_account WHERE open_id=?", Long.class, open);
        jdbc.update(
                "INSERT INTO vi_account_security(account_id,changed_at) VALUES (?,UTC_TIMESTAMP(3))",
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
