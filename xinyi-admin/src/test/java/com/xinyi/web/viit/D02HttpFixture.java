package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.dispatch.service.DispatchEngineJob;
import com.xinyi.rongli.jobs.service.DispatchDeadlineJob;
import com.xinyi.rongli.platform.json.ViJson;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** D02 无 @Test 夹具：真实资格提交/审批、D01、B03 报价建单和官方 SDK 签名支付。 Q03/Q04 仍明确为正式父事实消费者边界；只替换腾讯路线与微信支付的外部传输。 */
abstract class D02HttpFixture extends DispatchHttpFixture {
    static final String INVITES = "/api/app/driver/invites";
    static final String CURRENT = "/api/app/driver/assignments/current";
    static final String ADMIN_DISPATCH = "/api/admin/dispatch";
    static final String DISPATCH_LIST = "biz:dispatch:list";
    static final String DISPATCH_CREATE = "biz:dispatch:create";
    static final String DISPATCH_REVOKE = "biz:dispatch:revoke";
    static final String POLICY_QUERY = "biz:dispatchpolicy:query";
    static final String POLICY_EDIT = "biz:dispatchpolicy:edit";
    private static final com.sun.net.httpserver.HttpServer ROUTES = routes();

    @Autowired DispatchEngineJob engine;
    @Autowired DispatchDeadlineJob dispatchDeadline;

    JsonNode paymentPolicy;
    long dispatchStation;
    long pickupSlot;
    Instant pickupStarts;
    final List<PaidOrder> createdOrders = new ArrayList<>();

    record PaidOrder(
            String no,
            long id,
            String ownerToken,
            long accountId,
            long paymentId,
            long stationId) {}

    @DynamicPropertySource
    static void d02Transports(DynamicPropertyRegistry properties) {
        properties.add("vi.maps.tencent.key", () -> "d02-isolated-route-key");
        properties.add(
                "vi.maps.tencent.endpoint",
                () -> "http://127.0.0.1:" + ROUTES.getAddress().getPort() + "/route");
        properties.add(
                "vi.payment.test-endpoint",
                () -> "http://127.0.0.1:" + PaymentHttpFixture.server.getAddress().getPort());
    }

    private static com.sun.net.httpserver.HttpServer routes() {
        try {
            var server =
                    com.sun.net.httpserver.HttpServer.create(
                            new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext(
                    "/route",
                    exchange -> {
                        byte[] response =
                                "{\"status\":0,\"result\":{\"routes\":[{\"distance\":12345,\"duration\":20}]}}"
                                        .getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(200, response.length);
                        exchange.getResponseBody().write(response);
                        exchange.close();
                    });
            server.start();
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> server.stop(0), "d02-route-shutdown"));
            return server;
        } catch (Exception failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    @BeforeEach
    void dispatchOrdersBase() {
        // 父夹具已验证隔离保护标记；每条正向订单随后经真实报价、建单和支付，不直接造 SUCCESS。
        for (String table :
                List.of(
                        "vi_channel_event",
                        "vi_refund_order",
                        "vi_quota_reservation",
                        "vi_order_snapshot",
                        "vi_payment_order",
                        "vi_order",
                        "vi_quote",
                        "vi_quota_slot",
                        "vi_dispatch_batch",
                        "vi_dispatch_policy",
                        "vi_dispatch_cursor")) {
            jdbc.execute("DELETE FROM " + table);
        }
        createdOrders.clear();
        pickupStarts =
                LocalDate.now(SHANGHAI).plusDays(2).atTime(10, 0).atZone(SHANGHAI).toInstant();
        controlledNow.set(pickupStarts.minusSeconds(1800));
        // 候选使用真实 ACTIVE 会话。延长测试会话边界仅为受控未来时钟场景，不伪造 actor 或绕过 token 校验。
        extendTestSession(driver);
        configure(COMPANY_A, "23:59");
        PaymentHttpFixture.queryState = "NOTPAY";
        PaymentHttpFixture.queryFails = false;
        PaymentHttpFixture.closeRaces = false;
        PaymentHttpFixture.badSignature = false;
        PaymentHttpFixture.slow = false;
        PaymentHttpFixture.timeout = false;
        PaymentHttpFixture.requestFailure = null;
        PaymentHttpFixture.calls.set(0);
        configurePayment();
        configureBooking();
        dispatchStation = supplyStation(SITE_DEPT);
        pickupSlot = supplySlots(dispatchStation);
        Map<String, Object> price = body(COMPANY_A, "TEST");
        price.put("name", "D02签名支付真实金额");
        price.put("inspectionFee", 12345);
        price.put("driverBaseFee", 0);
        price.put("distanceUnitFee", 0);
        JsonNode rule = create(price, rootAdmin);
        rule = submit(rule, rootAdmin);
        rule = approve(rule, scoped(COMPANY_A, "biz:price:audit"));
        publish(rule, rootAdmin);
    }

    void extendTestSession(Student person) {
        jdbc.update(
                "UPDATE vi_session SET absolute_expires_at=? WHERE account_id=? AND"
                        + " status='ACTIVE'",
                utc(now().plus(Duration.ofDays(7))),
                person.accountId());
    }

    void configureBooking() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("vi.biz.dispatch.lead.minutes", "30");
        values.put("vi.biz.booking.enabled", "true");
        values.put("vi.biz.terms.version", "d02-terms");
        values.put("vi.biz.terms.text", "隔离派单测试协议");
        values.put("vi.biz.privacy.version", "d02-privacy");
        values.put("vi.biz.privacy.text", "隔离派单测试隐私");
        values.put("vi.biz.booking.handover.seconds", "600");
        values.put("vi.biz.booking.route-buffer.seconds", "300");
        values.put("vi.biz.booking.min-lead.seconds", "60");
        values.put("vi.biz.booking.quote-ttl.seconds", "300");
        values.put("vi.config.version", "1");
        values.forEach(this::bookingConfig);
    }

    private void bookingConfig(String name, String value) {
        // sys_config 无 config_key 唯一约束，显式替换隔离规则，不能用无效 upsert 留下旧条目。
        jdbc.update("DELETE FROM sys_config WHERE config_key=?", name);
        jdbc.update(
                "INSERT INTO sys_config(config_name,config_key,config_value,config_type)"
                        + " VALUES(?,?,?,'N')",
                name,
                name,
                value);
    }

    long supplyStation(long dept) {
        long station = station(dept);
        jdbc.update(
                "UPDATE vi_station SET lng=112.123456,lat=22.234567,service_radius_meter=1000000"
                        + " WHERE id=?",
                station);
        return station;
    }

    long supplySlots(long station) {
        for (String type : List.of("PICKUP", "STATION_RECEIVE")) {
            jdbc.update(
                    "INSERT INTO"
                        + " vi_quota_slot(quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota,confirmed_quota,consumed_quota,enabled)"
                        + " VALUES(?,?,?,?,?,100,0,0,0,1)",
                    type,
                    "station:" + station,
                    station,
                    utc(pickupStarts),
                    utc(pickupStarts.plusSeconds(type.equals("PICKUP") ? 3600 : 7200)));
        }
        return jdbc.queryForObject(
                "SELECT id FROM vi_quota_slot WHERE station_id=? AND quota_type='PICKUP'",
                Long.class,
                station);
    }

    void configurePayment() {
        JsonNode app = configureWechat(COMPANY_A);
        Map<String, Object> merchantConfig = new LinkedHashMap<>();
        merchantConfig.put("merchantName", "D02隔离商户");
        merchantConfig.put("mchid", "1234567890");
        merchantConfig.put("merchantCertificateSerial", "AABB");
        merchantConfig.put("verificationMode", "WECHATPAY_PUBLIC_KEY");
        merchantConfig.put("wechatPayPublicKeyId", "PUB_KEY_ID_TEST");
        merchantConfig.put(
                "wechatPayPublicKeyPem",
                PaymentHttpFixture.pem(
                        "PUBLIC KEY", PaymentHttpFixture.providerKey.getPublic().getEncoded()));
        merchantConfig.put("bindingAcknowledged", true);
        merchantConfig.put("appProfile", integrationReference(app));
        JsonNode merchant =
                publishIntegration(
                        "WECHAT_PAY",
                        merchantConfig,
                        Map.of(
                                "merchantPrivateKey",
                                Map.of(
                                        "action",
                                        "REPLACE",
                                        "value",
                                        PaymentHttpFixture.pem(
                                                "PRIVATE KEY",
                                                PaymentHttpFixture.merchantKey
                                                        .getPrivate()
                                                        .getEncoded())),
                                "apiV3Key",
                                Map.of(
                                        "action",
                                        "REPLACE",
                                        "value",
                                        "0123456789abcdef0123456789abcdef")));
        paymentPolicy =
                publishIntegration(
                        "PAYMENT_POLICY",
                        Map.of(
                                "offlineEnabled",
                                false,
                                "wechatEnabled",
                                true,
                                "offlineCollectionMode",
                                "PREPAID_MANUAL_VERIFIED",
                                "wechatMerchant",
                                integrationReference(merchant)),
                        null);
    }

    Map<String, Object> integrationReference(JsonNode profile) {
        return Map.of(
                "profileId",
                profile.path("profileId").asText(),
                "revisionId",
                profile.path("activeRevisionId").asText());
    }

    JsonNode publishIntegration(String kind, Object config, Object credentials) {
        JsonNode profile =
                data(
                        call(
                                "POST",
                                INTEGRATIONS + "/profiles",
                                Map.of(
                                        "kind",
                                        kind,
                                        "name",
                                        "D02隔离支付配置",
                                        "scope",
                                        Map.of(
                                                "ownerDeptId",
                                                Long.toString(COMPANY_A),
                                                "environment",
                                                "TEST")),
                                rootAdmin));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("kind", kind);
        body.put("expectedVersion", profile.path("version").asLong());
        body.put("config", config);
        body.put("reason", "真实支付协议替身隔离配置");
        if (credentials != null) body.put("credentials", credentials);
        JsonNode draft = data(call("PUT", integrationPath(profile) + "/draft", body, rootAdmin));
        JsonNode check =
                data(
                        call(
                                "POST",
                                integrationPath(profile) + "/checks",
                                Map.of(
                                        "revisionId",
                                        draft.path("draftRevisionId").asText(),
                                        "expectedVersion",
                                        draft.path("version").asLong(),
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
                                draft.path("draftRevisionId").asText(),
                                "expectedVersion",
                                draft.path("version").asLong(),
                                "contentHash",
                                draft.path("draft").path("contentHash").asText(),
                                "checkId",
                                check.path("checkId").asText(),
                                "reason",
                                "独立验证后发布隔离支付配置"),
                        rootAdmin));
        return data(call("GET", integrationPath(profile), null, rootAdmin));
    }

    String dispatchPolicyPath(long company) {
        return CONFIGS + company + "/dispatch-policy";
    }

    JsonNode dispatchPolicy(long company) {
        return data(call("GET", dispatchPolicyPath(company), null, rootAdmin));
    }

    Map<String, Object> dispatchPolicyBody(long version) {
        return new LinkedHashMap<>(
                Map.of(
                        "expectedVersion",
                        version,
                        "inviteResponseSeconds",
                        60,
                        "radiusMeter",
                        5000,
                        "expandedRadiusMeter",
                        8000,
                        "overallDeadlineSeconds",
                        300,
                        "roundPolicy",
                        "COMPANY_CURSOR",
                        "distanceMode",
                        "GCJ02_GREAT_CIRCLE",
                        "licenseCompatibility",
                        List.of(
                                Map.of(
                                        "vehicleTypeCode",
                                        "C04_CAR",
                                        "licenseClasses",
                                        List.of("C1"))),
                        "reason",
                        "D02用例显式公司规则，不是运营默认"));
    }

    JsonNode configureDispatch() {
        return data(
                call(
                        "PUT",
                        dispatchPolicyPath(COMPANY_A),
                        dispatchPolicyBody(dispatchPolicy(COMPANY_A).path("version").asLong()),
                        rootAdmin));
    }

    PaidOrder paidOrder() {
        return paidOrder(dispatchStation, pickupSlot);
    }

    PaidOrder paidOrder(long station, long slot) {
        PaidOrder result = unpaidOrder(station, slot);
        JsonNode options = data(ownerOrder(result, "GET", "/payment-options", null));
        data(
                ownerOrder(
                        result,
                        "POST",
                        "/prepay",
                        Map.of(
                                "expectedVersion",
                                options.path("paymentVersion").asLong(),
                                "expectedPolicyRevisionId",
                                options.path("policyRevisionId").asText())));
        PaymentHttpFixture.queryTrade =
                jdbc.queryForObject(
                        "SELECT out_trade_no FROM vi_payment_order WHERE order_id=?",
                        String.class,
                        result.id());
        PaymentHttpFixture.queryState = "SUCCESS";
        data(ownerOrder(result, "GET", "/payment", null));
        assertEquals("SUCCESS", paymentField(result, "status"));
        assertEquals(
                12345,
                jdbc.queryForObject(
                        "SELECT paid_fen FROM vi_payment_order WHERE order_id=?",
                        Integer.class,
                        result.id()));
        assertNull(PaymentHttpFixture.requestFailure, "支付必须通过真实 SDK 请求签名校验");
        return result;
    }

    PaidOrder unpaidOrder(long station, long slot) {
        String owner = newMini(true);
        long account = miniTokens.resolveAccess(owner).accountId();
        JsonNode created = data(createOrderAttempt(station, slot, owner));
        String no = created.path("orderNo").asText();
        long id = jdbc.queryForObject("SELECT id FROM vi_order WHERE order_no=?", Long.class, no);
        long payment =
                jdbc.queryForObject(
                        "SELECT id FROM vi_payment_order WHERE order_id=?", Long.class, id);
        PaidOrder result = new PaidOrder(no, id, owner, account, payment, station);
        createdOrders.add(result);
        return result;
    }

    ResponseEntity<String> createOrderAttempt(long station, long slot, String owner) {
        JsonNode quote =
                data(
                        miniCall(
                                "POST",
                                "/api/app/owner/orders/quote",
                                Map.of(
                                        "slotId",
                                        Long.toString(slot),
                                        "stationId",
                                        Long.toString(station),
                                        "inspectionTypeCode",
                                        "C04_ANNUAL",
                                        "vehicleDraft",
                                        Map.of("plateNo", "粤A12345", "vehicleTypeCode", "C04_CAR"),
                                        "addressDraft",
                                        Map.of(
                                                "address",
                                                "D02隔离接车地址，不应出现在邀约",
                                                "location",
                                                Map.of(
                                                        "lng",
                                                        112.123456,
                                                        "lat",
                                                        22.234567,
                                                        "coordinateSystem",
                                                        "GCJ02"))),
                                owner));
        return miniCall(
                "POST",
                "/api/app/owner/orders",
                Map.of(
                        "quoteId",
                        quote.path("quoteId").asText(),
                        "termsVersion",
                        "d02-terms",
                        "privacyVersion",
                        "d02-privacy",
                        "acceptedTerms",
                        true),
                owner);
    }

    ResponseEntity<String> ownerOrder(PaidOrder order, String method, String suffix, Object body) {
        return miniCall(
                method, "/api/app/owner/orders/" + order.no() + suffix, body, order.ownerToken());
    }

    Student additionalDriver(long company) {
        Submitted extra = submitted(company);
        approve(extra);
        data(
                call(
                        "POST",
                        "/api/admin/drivers/"
                                + extra.person().driverId()
                                + "/enable-new-assignments",
                        Map.of(
                                "expectedVersion",
                                profileVersion(extra.person()),
                                "reason",
                                "D02隔离测试显式启用第二真实代驾"),
                        rootAdmin));
        Student active = switchToDriver(extra.person());
        extendTestSession(active);
        return active;
    }

    void readyDriver() {
        listening(true);
        locate(now());
        contact("FOREGROUND", now());
        assertTrue(availability().path("availableForNewOrder").asBoolean());
    }

    void readyDriver(Student person) {
        Student saved = driver;
        try {
            driver = person;
            readyDriver();
        } finally {
            driver = saved;
        }
    }

    int dispatchNow() {
        dispatchDeadline.runOnce();
        return engine.runOnce();
    }

    JsonNode invite(PaidOrder order) {
        JsonNode rows =
                page(
                        call(
                                "GET",
                                ADMIN_DISPATCH + "/invites?orderNo=" + order.no(),
                                null,
                                rootAdmin));
        assertEquals(1, rows.path("total").asInt(), rows.toString());
        return rows.path("rows").get(0);
    }

    JsonNode ownInvite(JsonNode admin) {
        return data(driverCall("GET", INVITES + "/" + admin.path("inviteId").asText(), null));
    }

    Map<String, Object> responseBody(JsonNode invite, String decision) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("decision", decision);
        body.put("expectedVersion", invite.path("version").asLong());
        body.put("expectedOrderVersion", invite.path("orderVersion").asLong());
        body.put("expectedSessionId", invite.path("sessionId").asText());
        body.put("expectedContextVersion", invite.path("contextVersion").asLong());
        body.put("reason", decision.equals("REJECT") ? "D02本人明确拒绝当前邀约" : null);
        return body;
    }

    ResponseEntity<String> respond(JsonNode invite, String decision) {
        return respond(invite, responseBody(invite, decision), key());
    }

    ResponseEntity<String> respond(JsonNode invite, Object body, String idempotency) {
        return driverCall(
                "POST",
                INVITES + "/" + invite.path("inviteId").asText() + "/response",
                body,
                idempotency);
    }

    Map<String, Object> manualBody(PaidOrder order, Student target) {
        return new LinkedHashMap<>(
                Map.of(
                        "orderNo",
                        order.no(),
                        "driverId",
                        Long.toString(target.driverId()),
                        "expectedOrderVersion",
                        orderVersion(order),
                        "expectedDriverVersion",
                        profileVersion(target),
                        "reason",
                        "D02后台明确选择司机发出待本人接受邀约"));
    }

    JsonNode manualInvite(PaidOrder order, Student target) {
        return data(
                call(
                        "POST",
                        ADMIN_DISPATCH + "/manual-invites",
                        manualBody(order, target),
                        rootAdmin));
    }

    Map<String, Object> revokeBody(JsonNode invite) {
        return new LinkedHashMap<>(
                Map.of(
                        "expectedVersion",
                        invite.path("version").asLong(),
                        "expectedOrderVersion",
                        invite.path("orderVersion").asLong(),
                        "reason",
                        "D02后台撤销尚未接受的原邀约"));
    }

    String revokePath(JsonNode invite) {
        return ADMIN_DISPATCH + "/invites/" + invite.path("inviteId").asText() + "/revoke";
    }

    long orderVersion(PaidOrder order) {
        return jdbc.queryForObject(
                "SELECT version FROM vi_order WHERE id=?", Long.class, order.id());
    }

    String orderField(PaidOrder order, String field) {
        return jdbc.queryForObject(
                "SELECT " + field + " FROM vi_order WHERE id=?", String.class, order.id());
    }

    String paymentField(PaidOrder order, String field) {
        return jdbc.queryForObject(
                "SELECT " + field + " FROM vi_payment_order WHERE order_id=?",
                String.class,
                order.id());
    }

    JsonNode candidates(PaidOrder order) {
        return page(
                call("GET", ADMIN_DISPATCH + "/candidates?orderNo=" + order.no(), null, rootAdmin));
    }

    long sessionId(Student person) {
        return miniTokens.resolveAccess(person.token()).sessionId();
    }

    JsonNode page(ResponseEntity<String> response) {
        requireStatus(response, 200);
        JsonNode body = ViJson.tree(response.getBody());
        assertTrue(body.has("rows"), body.toString());
        assertTrue(body.has("total"), body.toString());
        return body;
    }

    static LocalDateTime utc(Instant time) {
        return LocalDateTime.ofInstant(time.truncatedTo(ChronoUnit.MILLIS), ZoneOffset.UTC);
    }

    static Instant instant(JsonNode value) {
        return Instant.parse(value.asText());
    }
}
