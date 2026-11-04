package com.xinyi.web.viit;

import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.payment.service.PaymentLifecycleService;
import com.xinyi.rongli.address.service.AddressCrypto;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import java.util.*;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class P02PaymentIT extends PaymentHttpFixture {
    @Autowired PaymentLifecycleService lifecycle;
    @Autowired AddressCrypto addressCrypto;
    private static final String DATA_KEY = UUID.randomUUID().toString() + UUID.randomUUID();

    @org.springframework.test.context.DynamicPropertySource
    static void dataKey(org.springframework.test.context.DynamicPropertyRegistry r) {
        r.add("vi.crypto.data-key", () -> DATA_KEY);
    }

    @BeforeEach
    void orderFixtures() {
        queryTrade = state("out_trade_no");
        long station =
                jdbc.queryForObject(
                        "SELECT station_id FROM vi_order WHERE id=?", Long.class, orderId);
        long quote =
                jdbc.queryForObject(
                        "SELECT quote_id FROM vi_order WHERE id=?", Long.class, orderId);
        var address =
                Map.of(
                        "address",
                        "隔离测试地址",
                        "location",
                        Map.of("lng", 110, "lat", 21, "coordinateSystem", "GCJ02"));
        byte[] cipher = addressCrypto.encrypt(ViJson.write(address));
        jdbc.update(
                "INSERT INTO"
                    + " vi_quote(id,owner_account_id,station_id,vehicle_snapshot,address_snapshot_cipher,pickup_slot_id,rule_id,route_snapshot,amount_fen,items_json,expires_at)"
                    + " VALUES(?,?,?,'{}',?,1,1,'{}',12345,'{\"items\":[]}',DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 HOUR))",
                quote,
                account,
                station,
                cipher);
        jdbc.update(
                "UPDATE vi_order SET"
                    + " vehicle_snapshot='{\"plateNo\":\"粤A12345\",\"vehicleTypeCode\":\"C04_CAR\",\"vehicleTypeLabel\":\"测试车型\"}'"
                    + " WHERE id=?",
                orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_order_snapshot(order_id,address_cipher,contact_cipher,station_snapshot,agreement_snapshot,accepted_at)"
                    + " VALUES(?,?,?,?, '{}',UTC_TIMESTAMP(3))",
                orderId,
                cipher,
                addressCrypto.encrypt(
                        ViJson.write(Map.of("displayName", "车主", "phone", "13900008888"))),
                ViJson.write(
                        Map.of(
                                "stationId",
                                Long.toString(station),
                                "name",
                                "测试站",
                                "publicAddress",
                                "地址",
                                "location",
                                Map.of("lng", 110, "lat", 21, "coordinateSystem", "GCJ02"))));
        for (String type : List.of("PICKUP", "STATION_RECEIVE")) {
            long slot = SEQ.incrementAndGet();
            jdbc.update(
                    "INSERT INTO"
                        + " vi_quota_slot(id,quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota)"
                        + " VALUES(?,?,?, ?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 2"
                        + " DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 3 DAY),5,1)",
                    slot,
                    type,
                    "station:" + station,
                    station);
            jdbc.update(
                    "INSERT INTO vi_quota_reservation(order_id,slot_id,status,quantity,expires_at)"
                        + " SELECT ?,?,'HELD',1,expires_at FROM vi_payment_order WHERE order_id=?",
                    orderId,
                    slot,
                    orderId);
        }
    }

    void startPay() {
        data(pay(intent(), UUID.randomUUID().toString()));
    }

    void expire() {
        jdbc.update(
                "UPDATE vi_payment_order SET expires_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1"
                    + " SECOND),next_query_at=NULL WHERE order_id=?",
                orderId);
    }

    void retryDue() {
        jdbc.update("UPDATE vi_payment_order SET next_query_at=NULL WHERE order_id=?", orderId);
    }

    int quota(String state) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM vi_quota_reservation WHERE order_id=? AND status=?",
                Integer.class,
                orderId,
                state);
    }

    Map<String, Object> fact() {
        return new LinkedHashMap<>(
                Map.of(
                        "appid",
                        "wx0000000000000000",
                        "mchid",
                        "1234567890",
                        "out_trade_no",
                        queryTrade,
                        "transaction_id",
                        "TX_P02_" + queryTrade,
                        "trade_state",
                        "SUCCESS",
                        "success_time",
                        Instant.now().toString(),
                        "amount",
                        Map.of("total", 12345, "currency", "CNY")));
    }

    String envelope(Map<String, Object> fact, String id, String type) throws Exception {
        String nonce = "123456789012", aad = "transaction";
        var cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(
                        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "AES"),
                new javax.crypto.spec.GCMParameterSpec(
                        128, nonce.getBytes(StandardCharsets.UTF_8)));
        cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        return ViJson.write(
                Map.of(
                        "id",
                        id,
                        "create_time",
                        Instant.now().toString(),
                        "event_type",
                        type,
                        "resource_type",
                        "encrypt-resource",
                        "resource",
                        Map.of(
                                "algorithm",
                                "AEAD_AES_256_GCM",
                                "nonce",
                                nonce,
                                "associated_data",
                                aad,
                                "ciphertext",
                                Base64.getEncoder()
                                        .encodeToString(
                                                cipher.doFinal(
                                                        ViJson.write(fact)
                                                                .getBytes(
                                                                        StandardCharsets
                                                                                .UTF_8))))));
    }

    ResponseEntity<String> notify(String kind, String body, long timestamp, boolean forged)
            throws Exception {
        String at = Long.toString(timestamp), nonce = "p02-notify";
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(providerKey.getPrivate());
        signature.update((at + "\n" + nonce + "\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
        var h = new HttpHeaders();
        h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        h.set("Wechatpay-Serial", "PUB_KEY_ID_TEST");
        h.set("Wechatpay-Timestamp", at);
        h.set("Wechatpay-Nonce", nonce);
        h.set(
                "Wechatpay-Signature",
                Base64.getEncoder().encodeToString(forged ? new byte[256] : signature.sign()));
        return http.exchange(
                "http://127.0.0.1:" + port + "/api/notify/wechat/" + kind,
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class);
    }

    ResponseEntity<String> notify(Map<String, Object> f, String id) throws Exception {
        return notify(
                "pay",
                envelope(f, id, "TRANSACTION.SUCCESS"),
                Instant.now().getEpochSecond(),
                false);
    }

    ResponseEntity<String> payment() {
        return ownerCall("GET", "/payment", null, owner, null);
    }

    ResponseEntity<String> close(long version, String key) {
        return ownerCall(
                "POST",
                "/close-unpaid",
                Map.of("expectedVersion", version, "reason", "客户取消预约"),
                owner,
                key);
    }

    @Test
    void deadlineAndClientSuccessCannotPay() {
        String deadline = state("expires_at");
        startPay();
        assertEquals(deadline, state("expires_at"));
        assertEquals("PENDING", data(payment()).path("payStatus").asText());
        assertEquals(2, quota("HELD"));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_order WHERE id=? AND"
                            + " fulfillment_status='SCHEDULED'",
                        Integer.class,
                        orderId));
        assertEquals(
                400,
                ownerCall(
                                "POST",
                                "/close-unpaid",
                                Map.of("expectedVersion", 0, "reason", "测试", "paid", true),
                                owner,
                                UUID.randomUUID().toString())
                        .getStatusCode()
                        .value());
    }

    @Test
    void validCallbackConfirmsOnceAndSchedules() throws Exception {
        startPay();
        var f = fact();
        String body = envelope(f, "event-one", "TRANSACTION.SUCCESS");
        assertEquals(
                204,
                notify("pay", body, Instant.now().getEpochSecond(), false).getStatusCode().value());
        assertEquals(
                204,
                notify("pay", body, Instant.now().getEpochSecond(), false).getStatusCode().value());
        assertEquals(204, notify(f, "event-two").getStatusCode().value());
        assertEquals("SUCCESS", state("status"));
        assertEquals("12345", state("paid_fen"));
        assertEquals(2, quota("CONFIRMED"));
        assertEquals(
                2, jdbc.queryForObject("SELECT COUNT(*) FROM vi_channel_event", Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='PAYMENT_CONFIRMED'",
                        Integer.class));
        assertEquals(
                "SCHEDULED",
                jdbc.queryForObject(
                        "SELECT fulfillment_status FROM vi_order WHERE id=?",
                        String.class,
                        orderId));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_dispatch_invite WHERE order_id=?",
                        Integer.class,
                        orderId));
    }

    @Test
    void badAmountMerchantAppAndSignatureFailClosed() throws Exception {
        startPay();
        for (String field : List.of("mchid", "appid", "amount")) {
            var f = fact();
            f.put(
                    field,
                    field.equals("amount") ? Map.of("total", 12344, "currency", "CNY") : "wrong");
            assertEquals(400, notify(f, UUID.randomUUID().toString()).getStatusCode().value());
        }
        String body = envelope(fact(), "forged", "TRANSACTION.SUCCESS");
        assertEquals(
                400,
                notify("pay", body, Instant.now().getEpochSecond(), true).getStatusCode().value());
        assertEquals("0", state("paid_fen"));
        assertEquals(2, quota("HELD"));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_channel_event", Integer.class));
        assertEquals(
                4,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE"
                            + " action='PAYMENT_CALLBACK_REJECTED'",
                        Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='PAYMENT_AMOUNT_MISMATCH'",
                        Integer.class));
    }

    @Test
    void replayWindowAndEventCollisionRejected() throws Exception {
        startPay();
        String b = envelope(fact(), "same-event", "TRANSACTION.SUCCESS");
        assertEquals(
                400,
                notify("pay", b, Instant.now().getEpochSecond() - 301, false)
                        .getStatusCode()
                        .value());
        assertEquals(
                400,
                notify("pay", b, Instant.now().getEpochSecond() + 301, false)
                        .getStatusCode()
                        .value());
        assertEquals(
                204,
                notify("pay", b, Instant.now().getEpochSecond(), false).getStatusCode().value());
        var changed = fact();
        changed.put("transaction_id", "OTHER_TRANSACTION");
        assertEquals(400, notify(changed, "same-event").getStatusCode().value());
        assertEquals(2, quota("CONFIRMED"));
    }

    @Test
    void timeoutClosesOnlyAfterChannelConfirmation() {
        startPay();
        expire();
        lifecycle.runDue();
        assertEquals("CLOSED", state("status"));
        assertEquals(2, quota("RELEASED"));
        lifecycle.runDue();
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='PAYMENT_CLOSED'",
                        Integer.class));
        assertEquals(409, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
    }

    @Test
    void channelFailureRetainsQuotaAndRetrySurvives() {
        startPay();
        expire();
        queryFails = true;
        lifecycle.runDue();
        assertEquals("UNKNOWN", state("status"));
        assertEquals(2, quota("HELD"));
        assertNotNull(state("close_requested_at"));
        assertNotNull(state("next_query_at"));
        queryFails = false;
        retryDue();
        lifecycle.runDue();
        assertEquals("CLOSED", state("status"));
        assertEquals(2, quota("RELEASED"));
    }

    @Test
    void providerPaidDuringCloseWins() {
        startPay();
        expire();
        closeRaces = true;
        lifecycle.runDue();
        assertEquals("SUCCESS", state("status"));
        assertEquals(2, quota("CONFIRMED"));
        assertEquals(0, quota("RELEASED"));
    }

    @Test
    void callbackAndCloserRaceOnlyOneQuotaOutcome() throws Exception {
        startPay();
        expire();
        queryState = "SUCCESS";
        var f = fact();
        var pool = Executors.newFixedThreadPool(2);
        var gate = new CountDownLatch(1);
        try {
            var a =
                    pool.submit(
                            () -> {
                                gate.await();
                                return notify(f, "race").getStatusCode().value();
                            });
            var b =
                    pool.submit(
                            () -> {
                                gate.await();
                                return lifecycle.runDue();
                            });
            gate.countDown();
            assertEquals(204, a.get(15, TimeUnit.SECONDS));
            b.get(15, TimeUnit.SECONDS);
            assertEquals("SUCCESS", state("status"));
            assertEquals(2, quota("CONFIRMED"));
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_outbox WHERE event_type='PAYMENT_CONFIRMED'",
                            Integer.class));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void lateSuccessDoesNotReopenReleasedOrder() throws Exception {
        startPay();
        expire();
        lifecycle.runDue();
        assertEquals(204, notify(fact(), "late").getStatusCode().value());
        assertEquals("SUCCESS", state("status"));
        assertEquals("MANUAL_CONFLICT", state("capacity_resolution"));
        assertEquals(2, quota("RELEASED"));
        assertEquals(
                "CLOSED",
                jdbc.queryForObject(
                        "SELECT fulfillment_status FROM vi_order WHERE id=?",
                        String.class,
                        orderId));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE"
                            + " event_type='PAYMENT_LATE_SUCCESS_CONFLICT'",
                        Integer.class));
        assertTrue(data(payment()).path("queryRequired").asBoolean());
    }

    @Test
    void persistenceRollbackNeverAcknowledges() throws Exception {
        startPay();
        jdbc.execute(
                "CREATE TRIGGER p02_fail_event BEFORE INSERT ON vi_channel_event FOR EACH ROW"
                    + " SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='isolated rollback'");
        try {
            assertEquals(500, notify(fact(), "rollback").getStatusCode().value());
            assertEquals("0", state("paid_fen"));
            assertEquals(2, quota("HELD"));
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_outbox WHERE event_type='PAYMENT_CONFIRMED'",
                            Integer.class));
        } finally {
            jdbc.execute("DROP TRIGGER p02_fail_event");
        }
        assertEquals(204, notify(fact(), "retry").getStatusCode().value());
    }

    @Test
    void closeOwnerScopeVersionIdempotencyAndInFlightLease() {
        assertEquals(404, ownerCall("GET", "/payment", null, mini(), null).getStatusCode().value());
        assertEquals(
                404,
                ownerCall(
                                "POST",
                                "/close-unpaid",
                                Map.of("expectedVersion", 0, "reason", "测试"),
                                mini(),
                                UUID.randomUUID().toString())
                        .getStatusCode()
                        .value());
        assertEquals(409, close(99, UUID.randomUUID().toString()).getStatusCode().value());
        String key = UUID.randomUUID().toString();
        assertEquals("CLOSED", data(close(0, key)).path("fulfillmentStatus").asText());
        assertEquals("CLOSED", data(close(0, key)).path("fulfillmentStatus").asText());
        assertEquals(2, quota("RELEASED"));
    }

    @Test
    void offlineSelectionExpiresWithoutCallingWechat() {
        enableOffline(false, false);
        data(offline(intent(), UUID.randomUUID().toString()));
        expire();
        lifecycle.runDue();
        assertEquals("CLOSED", state("status"));
        assertEquals(2, quota("RELEASED"));
        assertEquals(0, calls.get());
    }

    @Test
    void activePrepayLeaseBlocksClosingAndNewPayment() {
        startPay();
        jdbc.update(
                "UPDATE vi_payment_order SET"
                    + " prepay_state='REQUESTING',prepay_lease_until=DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 MINUTE) WHERE order_id=?",
                orderId);
        assertEquals(
                "PENDING_PAY",
                data(close(0, UUID.randomUUID().toString())).path("fulfillmentStatus").asText());
        assertEquals(2, quota("HELD"));
        assertEquals(409, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
        jdbc.update(
                "UPDATE vi_payment_order SET prepay_lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 SECOND) WHERE order_id=?",
                orderId);
        lifecycle.runDue();
        assertEquals("CLOSED", state("status"));
    }

    @Test
    void queryRepairsMissingCallbackAndRespectsSwitchOff() {
        startPay();
        enableOffline(false, false);
        queryState = "SUCCESS";
        assertEquals("SUCCESS", data(payment()).path("payStatus").asText());
        assertEquals(2, quota("CONFIRMED"));
        assertNull(requestFailure);
    }

    @Test
    void refundNotificationUpdatesReservedBalanceExactlyOnce() throws Exception {
        startPay();
        assertEquals(204, notify(fact(), "paid-refund").getStatusCode().value());
        jdbc.update(
                "UPDATE vi_payment_order SET refund_reserved_fen=100 WHERE order_id=?", orderId);
        long payment =
                jdbc.queryForObject(
                        "SELECT id FROM vi_payment_order WHERE order_id=?", Long.class, orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_refund_order(case_id,review_revision,payment_id,out_refund_no,amount_fen,status,payment_method,execution_mode)"
                    + " VALUES(1,1,?,'REF_P02',100,'PROCESSING','WECHAT','WECHAT_API')",
                payment);
        var f = new LinkedHashMap<>(fact());
        f.remove("appid");
        f.remove("trade_state");
        f.put("refund_status", "SUCCESS");
        f.put("refund_id", "CHANNEL_REFUND");
        f.put("out_refund_no", "REF_P02");
        f.put("amount", Map.of("total", 12345, "refund", 100, "currency", "CNY"));
        String b = envelope(f, "refund-one", "REFUND.SUCCESS");
        assertEquals(
                204,
                notify("refund", b, Instant.now().getEpochSecond(), false).getStatusCode().value());
        assertEquals(
                204,
                notify("refund", b, Instant.now().getEpochSecond(), false).getStatusCode().value());
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals("100", state("refunded_fen"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='REFUND_SUCCESS'",
                        Integer.class));
    }

    @Test
    void unauthenticatedMalformedCallbackUsesWechatProtocol() {
        var h = new HttpHeaders();
        h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        var r =
                http.exchange(
                        "http://127.0.0.1:" + port + "/api/notify/wechat/pay",
                        HttpMethod.POST,
                        new HttpEntity<>("{}", h),
                        String.class);
        assertEquals(400, r.getStatusCode().value());
        assertEquals("FAIL", ViJson.tree(r.getBody()).path("code").asText());
        assertFalse(ViJson.tree(r.getBody()).has("data"));
    }

    @Test
    void failedQueryCloseReturnsPendingWithoutFalseSuccess() {
        startPay();
        queryFails = true;
        var response = data(close(0, UUID.randomUUID().toString()));
        assertEquals("PENDING_PAY", response.path("fulfillmentStatus").asText());
        assertEquals("UNKNOWN", response.path("paymentStatus").asText());
        assertEquals(2, quota("HELD"));
        assertEquals(409, pay(intent(), UUID.randomUUID().toString()).getStatusCode().value());
    }

    @Test
    void revokedSessionCannotCloseOrRead() {
        jdbc.update(
                "UPDATE vi_account_security SET session_epoch=session_epoch+1 WHERE account_id=?",
                account);
        assertEquals(401, payment().getStatusCode().value());
        assertEquals(401, close(0, UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(2, quota("HELD"));
        assertEquals("0", state("paid_fen"));
    }

    @Test
    void actualResponsesMatchPrimaryContract() throws Exception {
        var result = payment();
        assertEquals("no-store", result.getHeaders().getFirst("Cache-Control"));
        var closed = close(0, UUID.randomUUID().toString());
        assertEquals(200, closed.getStatusCode().value());
        var dir = java.nio.file.Files.createTempDirectory("p02-contract-");
        java.nio.file.Files.writeString(dir.resolve("payment.json"), result.getBody());
        java.nio.file.Files.writeString(dir.resolve("close.json"), closed.getBody());
        var repo = java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!java.nio.file.Files.exists(repo.resolve("pom.xml"))
                || !java.nio.file.Files.exists(repo.resolve("docs/vehicle")))
            repo = repo.getParent();
        var p =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/p02_contract.py")
                                        .toString(),
                                repo.toString(),
                                dir.toString())
                        .redirectErrorStream(true)
                        .start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(p.waitFor(15, TimeUnit.SECONDS));
        assertEquals(0, p.exitValue(), output);
    }
}
