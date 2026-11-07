package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.refund.service.RefundExecutionJob;
import com.xinyi.rongli.refund.service.RefundExecutionService;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

/** S02真实审批→官方SDK签验回环→资金事务；供应商替身不替换审核、退款、权限或幂等业务服务。 */
@org.springframework.context.annotation.Import(RefundHttpFixture.EndpointIsolation.class)
class P04RefundIT extends RefundHttpFixture {
    static final String REFUNDS = "/api/admin/refunds/", RECON = "/api/admin/reconciliation";
    static final String[] FINANCE = {
        "biz:refund:query",
        "biz:refund:retry",
        "biz:paymentreconciliation:list",
        "biz:paymentreconciliation:query",
        "biz:paymentreconciliation:resolve"
    };
    static final String[] REVIEW = {
        "biz:aftersale:query", "biz:aftersale:review", "biz:refund:approve", "biz:refund:query"
    };
    @Autowired RefundExecutionJob job;
    @Autowired org.springframework.core.env.Environment environment;
    @Autowired RefundExecutionService execution;
    @Autowired com.xinyi.rongli.integration.service.IntegrationCrypto reasonCrypto;
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;
    String finance, reviewer;
    JsonNode application;
    long refundId;

    @BeforeEach
    void paidFixture() {
        // 父夹具先检查隔离库名称/保护标记；只清理测试库生成的业务起点，不触达用户业务库。
        for (String table :
                List.of(
                        "vi_payment_reconciliation",
                        "vi_aftersale_participant_user",
                        "vi_aftersale_note",
                        "vi_aftersale_review",
                        "vi_aftersale_case",
                        "vi_driver_occupancy",
                        "vi_driver_assignment",
                        "vi_dispatch_invite")) jdbc.execute("DELETE FROM " + table);
        resetChannel();
        assertEquals(
                "http://127.0.0.1:" + REFUND_SERVER.getAddress().getPort(),
                environment.getProperty("vi.payment.test-endpoint"),
                "退款回环端口必须由当前夹具唯一提供，不能落到旧支付替身");
        expectedTrade = state("out_trade_no");
        jdbc.update(
                "UPDATE vi_order SET fulfillment_status='PENDING_DISPATCH' WHERE id=?", orderId);
        jdbc.update(
                "UPDATE vi_payment_order SET"
                    + " status='SUCCESS',paid_fen=12345,transaction_id=?,payment_method='WECHAT',merchant_profile_id=?,merchant_revision_id=?,app_id='wx0000000000000000',mchid='1234567890',method_locked_at=UTC_TIMESTAMP(3)"
                    + " WHERE order_id=?",
                "TX_P04_" + expectedTrade,
                merchant.path("profileId").asLong(),
                merchant.path("activeRevisionId").asLong(),
                orderId);
        finance = scoped(COMPANY_A, FINANCE);
        reviewer = scopedWithScope(COMPANY_A, "4", REVIEW);
    }

    JsonNode approved(String kind, long amount) {
        Map<String, Object> input =
                Map.of(
                        "kind",
                        kind,
                        "reason",
                        "退款执行集成申请",
                        "requestedAmount",
                        amount,
                        "expectedOrderVersion",
                        orderVersion());
        application =
                data(ownerCall("POST", "/after-sales", input, owner, UUID.randomUUID().toString()));
        application =
                data(
                        call(
                                "POST",
                                "/api/admin/after-sales/"
                                        + application.path("caseId").asText()
                                        + "/review",
                                Map.of(
                                        "expectedVersion",
                                        application.path("version").asLong(),
                                        "observedOrderVersion",
                                        orderVersion(),
                                        "decision",
                                        "APPROVE",
                                        "approvedAmount",
                                        amount,
                                        "opinion",
                                        "隔离审批核实",
                                        "publicOpinion",
                                        "已批准申请"),
                                reviewer));
        expectedAmount = amount;
        if (amount > 0) {
            expectedRefund = application.path("refundNo").asText();
            refundId =
                    jdbc.queryForObject(
                            "SELECT id FROM vi_refund_order WHERE out_refund_no=?",
                            Long.class,
                            expectedRefund);
        }
        return application;
    }

    long orderVersion() {
        return jdbc.queryForObject("SELECT version FROM vi_order WHERE id=?", Long.class, orderId);
    }

    String orderState(String field) {
        return jdbc.queryForObject(
                "SELECT " + field + " FROM vi_order WHERE id=?", String.class, orderId);
    }

    String refundState(String field) {
        return jdbc.queryForObject(
                "SELECT " + field + " FROM vi_refund_order WHERE id=?", String.class, refundId);
    }

    int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    JsonNode detail() {
        return data(call("GET", REFUNDS + expectedRefund, null, finance));
    }

    Map<String, Object> action() {
        return new LinkedHashMap<>(
                Map.of("expectedVersion", detail().path("version").asLong(), "reason", "核实原渠道退款"));
    }

    ResponseEntity<String> query() {
        return call("POST", REFUNDS + expectedRefund + "/query", action(), finance);
    }

    ResponseEntity<String> retry() {
        return call("POST", REFUNDS + expectedRefund + "/retry", action(), finance);
    }

    void status(ResponseEntity<String> result, int expected) {
        assertEquals(expected, result.getStatusCode().value(), result.getBody());
    }

    void due() {
        jdbc.update(
                "UPDATE vi_refund_order SET"
                    + " next_query_at=NULL,execution_lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 SECOND) WHERE id=?",
                refundId);
    }

    void run() {
        execution.execute(refundId);
    }

    void pendingFunds() {
        assertEquals(Long.toString(expectedAmount), state("refund_reserved_fen"));
        assertEquals("0", state("refunded_fen"));
        assertNotEquals("SUCCESS", refundState("status"));
    }

    String userId(String token) {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId().toString();
    }

    @Test
    void approvalOutboxAndWorkerSendOfficialSignedRequestThenQuerySettlesOnce() {
        approved("REFUND_ONLY", 1000);
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE"
                                + " event_type='REFUND_EXECUTION_REQUESTED'",
                        Integer.class));
        job.runOnce();
        assertEquals(1, refundPosts.get());
        assertTrue(paymentQueries.get() >= 1);
        assertNull(providerFailure);
        assertEquals("PROCESSING", refundState("status"));
        pendingFunds();
        JsonNode request = ViJson.tree(postedBodies.element());
        assertEquals(expectedRefund, request.path("out_refund_no").asText());
        if (request.hasNonNull("out_trade_no"))
            assertEquals(expectedTrade, request.path("out_trade_no").asText());
        else assertEquals("TX_P04_" + expectedTrade, request.path("transaction_id").asText());
        assertEquals(1000, request.path("amount").path("refund").asLong());
        assertEquals(12345, request.path("amount").path("total").asLong());
        assertEquals(
                "https://isolated.invalid/api/notify/wechat/refund",
                request.path("notify_url").asText());
        due();
        data(query());
        assertEquals("SUCCESS", refundState("status"));
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(1, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
        assertEquals("PENDING_DISPATCH", orderState("fulfillment_status"));
        assertEquals("NONE", orderState("cancel_status"));
    }

    @Test
    void trustedCallbackAndQueryConcurrentResultsOnlyMoveMoneyOnce() throws Exception {
        approved("REFUND_ONLY", 1000);
        run();
        due();
        String notification =
                encryptedEnvelope(refundFact("SUCCESS"), "p04-success-a", "REFUND.SUCCESS");
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            var start = new CountDownLatch(1);
            Future<Integer> a =
                    pool.submit(
                            () -> {
                                start.await();
                                return refundNotifyBody(notification, false)
                                        .getStatusCode()
                                        .value();
                            });
            Future<Integer> b =
                    pool.submit(
                            () -> {
                                start.await();
                                return refundNotifyBody(notification, false)
                                        .getStatusCode()
                                        .value();
                            });
            Future<ResponseEntity<String>> c =
                    pool.submit(
                            () -> {
                                start.await();
                                return query();
                            });
            start.countDown();
            assertEquals(204, a.get(20, TimeUnit.SECONDS));
            assertEquals(204, b.get(20, TimeUnit.SECONDS));
            assertTrue(
                    Set.of(200, 409).contains(c.get(20, TimeUnit.SECONDS).getStatusCode().value()));
        } finally {
            pool.shutdownNow();
        }
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='REFUND_SUCCESS'",
                        Integer.class));
        assertEquals(1, count("vi_refund_order"));
    }

    @Test
    void processingResponseNeverPretendsMoneyHasBeenRefunded() {
        approved("REFUND_ONLY", 1000);
        refundState = "PROCESSING";
        run();
        due();
        data(query());
        assertEquals("PROCESSING", refundState("status"));
        pendingFunds();
        assertNull(refundState("success_at"));
        assertEquals(1, refundPosts.get());
    }

    @Test
    void lostPostResponseKeepsOriginalNumberAndReservationThenQueriesFirst() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        assertEquals(1, refundPosts.get());
        pendingFunds();
        lostResponse = false;
        due();
        job.runOnce();
        assertTrue(refundQueries.get() >= 1);
        assertEquals(1, refundPosts.get());
        assertEquals("SUCCESS", refundState("status"));
        assertEquals(expectedRefund, detail().path("refundNo").asText());
    }

    @Test
    void invalidResponseSignatureCannotSettleApprovedMoney() {
        approved("REFUND_ONLY", 1000);
        refundBadSignature = true;
        postState = "SUCCESS";
        run();
        pendingFunds();
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='REFUND_SUCCESS'",
                        Integer.class));
    }

    @Test
    void responseAmountMismatchCannotSettleOrReleaseReservation() {
        approved("REFUND_ONLY", 1000);
        wrongAmount = true;
        postState = "SUCCESS";
        run();
        pendingFunds();
    }

    @Test
    void responseTotalOrTradeMismatchCannotSettle() {
        approved("REFUND_ONLY", 1000);
        wrongTotal = true;
        postState = "SUCCESS";
        run();
        pendingFunds();
        wrongTotal = false;
        wrongTrade = true;
        due();
        data(query());
        pendingFunds();
    }

    @Test
    void responseRefundNumberMismatchCannotSettle() {
        approved("REFUND_ONLY", 1000);
        wrongNo = true;
        postState = "SUCCESS";
        run();
        pendingFunds();
    }

    @Test
    void establishedChannelRefundIdCannotBeReplacedByAnotherTrustedResult() {
        approved("REFUND_ONLY", 1000);
        run();
        assertEquals("50300000000000000000000000001", refundState("channel_refund_id"));
        wrongChannel = true;
        due();
        data(query());
        pendingFunds();
        assertEquals("50300000000000000000000000001", refundState("channel_refund_id"));
    }

    @Test
    void forgedCallbackAndMismatchedAmountDoNotConsumeReservation() throws Exception {
        approved("REFUND_ONLY", 1000);
        assertNotEquals(
                204, refundNotify(refundFact("SUCCESS"), "forged", true).getStatusCode().value());
        wrongAmount = true;
        assertNotEquals(
                204,
                refundNotify(refundFact("SUCCESS"), "wrong-amount", false).getStatusCode().value());
        pendingFunds();
    }

    @Test
    void callbackTerminalSuccessIsNeverDowngradedByLateProcessingResponse() throws Exception {
        approved("REFUND_ONLY", 1000);
        holdResponse = true;
        holdRefundOnly = true;
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<?> request = pool.submit(this::run);
        try {
            assertTrue(requestStarted.await(3, TimeUnit.SECONDS));
            status(refundNotify(refundFact("SUCCESS"), "race-success", false), 204);
            assertEquals("SUCCESS", refundState("status"));
            releaseResponse.countDown();
            request.get(15, TimeUnit.SECONDS);
        } finally {
            releaseResponse.countDown();
            pool.shutdownNow();
        }
        assertEquals("SUCCESS", refundState("status"));
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
    }

    @Test
    void leaseExpiredAfterCrashQueriesBeforeAnyNewApplication() {
        approved("REFUND_ONLY", 1000);
        jdbc.update(
                "UPDATE vi_refund_order SET execution_started_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 2 MINUTE),execution_lease_token=?,execution_lease_until=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 SECOND),execution_attempts=1 WHERE id=?",
                UUID.randomUUID().toString(),
                refundId);
        job.runOnce();
        assertEquals(0, refundPosts.get());
        assertEquals(1, refundQueries.get());
        assertEquals("SUCCESS", refundState("status"));
    }

    @Test
    void signedNotFoundAllowsOnlySameRefundNumberAndFrozenAmountRetry() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        lostResponse = false;
        queryMissing = true;
        due();
        data(retry());
        assertEquals(2, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
        List<String> requests = new ArrayList<>(postedBodies);
        assertEquals(ViJson.tree(requests.get(0)), ViJson.tree(requests.get(1)));
        pendingFunds();
    }

    @Test
    void unsignedNotFoundDoesNotAuthorizeAnotherRefundPost() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        lostResponse = false;
        queryMissing = true;
        missingUnsigned = true;
        due();
        data(retry());
        assertEquals(1, refundPosts.get());
        pendingFunds();
    }

    @Test
    void badSignatureNotFoundDoesNotAuthorizeAnotherRefundPost() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        lostResponse = false;
        queryMissing = true;
        missingBadSignature = true;
        due();
        data(retry());
        assertEquals(1, refundPosts.get());
        pendingFunds();
    }

    @Test
    void closedRefundCannotBeBlindlyRetriedWithSameOrNewNumber() {
        approved("REFUND_ONLY", 1000);
        postState = "CLOSED";
        run();
        assertEquals("CLOSED", refundState("status"));
        int posted = refundPosts.get();
        status(retry(), 409);
        assertEquals(posted, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
        assertEquals("0", state("refunded_fen"));
    }

    @Test
    void waitingForReturnNeverCallsChannelOrReleasesDriverOnForgedSuccess() throws Exception {
        long driver = pickedUp();
        approved("CANCEL_REFUND", 1000);
        job.runOnce();
        assertEquals(0, refundPosts.get());
        assertEquals(0, refundQueries.get());
        assertEquals(0, paymentQueries.get());
        assertEquals("WAIT_RETURN", application.path("returnState").asText());
        assertNotEquals(
                204,
                refundNotify(refundFact("SUCCESS"), "premature-return-forged", true)
                        .getStatusCode()
                        .value());
        pendingFunds();
        // 未主动执行不等于可以抹去渠道真实已退事实；真实提前成功只落资金，不能伪造还车。
        status(refundNotify(refundFact("SUCCESS"), "premature-return-real", false), 204);
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals("APPROVED_WAIT_RETURN", orderState("cancel_status"));
        assertEquals("PICKED_UP", orderState("fulfillment_status"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        Integer.class,
                        driver));
    }

    @Test
    void zeroCancellationHasNoChannelRefundToExecute() {
        approved("CANCEL_REFUND", 0);
        job.runOnce();
        assertEquals(0, count("vi_refund_order"));
        assertEquals(0, refundPosts.get());
        assertEquals(0, paymentQueries.get());
    }

    @Test
    void offlineRemainsPendingManualEvidenceWithoutWechatCalls() {
        jdbc.update(
                "UPDATE vi_payment_order SET"
                    + " payment_method='OFFLINE',merchant_profile_id=NULL,merchant_revision_id=NULL,app_id=NULL,mchid=NULL"
                    + " WHERE order_id=?",
                orderId);
        approved("REFUND_ONLY", 1000);
        job.runOnce();
        JsonNode refund = data(query());
        assertEquals("OFFLINE", refund.path("paymentMethod").asText());
        assertEquals("MANUAL_OFFLINE", refund.path("executionMode").asText());
        assertEquals(0, refundPosts.get());
        assertEquals(0, refundQueries.get());
        assertEquals(0, paymentQueries.get());
        pendingFunds();
    }

    @Test
    void historicalMerchantStillExecutesWhenCurrentPaymentPolicyIsOff() {
        approved("REFUND_ONLY", 1000);
        enableOffline(false, false);
        run();
        assertEquals(1, refundPosts.get());
        assertNull(providerFailure);
        assertEquals("PROCESSING", refundState("status"));
    }

    @Test
    void originalReceiverCompanyControlsRefundAccessInsteadOfOrderStationDepartment() {
        approved("REFUND_ONLY", 1000);
        String onlyOrderDepartment = scoped(SITE_DEPT, FINANCE);
        status(call("GET", REFUNDS + expectedRefund, null, onlyOrderDepartment), 404);
        status(
                call("POST", REFUNDS + expectedRefund + "/query", action(), onlyOrderDepartment),
                404);
        assertEquals(expectedRefund, detail().path("refundNo").asText());
        assertEquals(0, refundPosts.get());
    }

    @Test
    void functionalityAndEmptyDepartmentScopeCannotBeForged() {
        approved("REFUND_ONLY", 1000);
        String empty = scopedWithScope(COMPANY_B, "5", FINANCE);
        String noFunction = scoped(COMPANY_A, "biz:aftersale:query");
        status(call("GET", REFUNDS + expectedRefund, null, empty), 404);
        status(call("GET", REFUNDS + expectedRefund, null, noFunction), 403);
        status(call("GET", REFUNDS + "UNKNOWN_REFUND", null, finance), 404);
        var input = action();
        input.put("receiverDeptId", COMPANY_A);
        status(call("POST", REFUNDS + expectedRefund + "/retry", input, finance), 400);
        assertEquals(0, refundPosts.get());
    }

    @Test
    void staleVersionMissingUuidAndInternalQueryFieldsAreRejected() {
        approved("REFUND_ONLY", 1000);
        var body = action();
        body.put("expectedVersion", 999);
        status(call("POST", REFUNDS + expectedRefund + "/retry", body, finance), 409);
        status(callKey("POST", REFUNDS + expectedRefund + "/query", action(), finance, null), 400);
        body = action();
        body.put("params", Map.of("dataScope", "1=1"));
        status(call("POST", REFUNDS + expectedRefund + "/query", body, finance), 400);
        status(call("GET", RECON + "?visibleDeptIds=1", null, finance), 400);
        assertEquals(0, refundPosts.get());
    }

    @Test
    void oldAuthVersionRemainsDeniedWhenRedisEvictionFails() {
        approved("REFUND_ONLY", 1000);
        long user = Long.parseLong(userId(finance));
        doThrow(new IllegalStateException("隔离Redis删除失败")).when(redis).deleteObject(anyString());
        jdbc.update("UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?", user);
        status(
                call(
                        "POST",
                        REFUNDS + expectedRefund + "/retry",
                        Map.of("expectedVersion", 0, "reason", "旧会话"),
                        finance),
                401);
        assertEquals(0, refundPosts.get());
        pendingFunds();
    }

    @Test
    void blockedExternalIoDoesNotHoldDatabaseAuthorityLockAndRevocationStopsNewPost()
            throws Exception {
        approved("REFUND_ONLY", 1000);
        long user = Long.parseLong(userId(finance));
        holdResponse = true;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        var input = action();
        Future<ResponseEntity<String>> request =
                pool.submit(
                        () -> call("POST", REFUNDS + expectedRefund + "/retry", input, finance));
        try {
            assertTrue(requestStarted.await(3, TimeUnit.SECONDS));
            Future<Integer> revoke =
                    pool.submit(
                            () ->
                                    jdbc.update(
                                            "UPDATE sys_user SET auth_version=auth_version+1 WHERE"
                                                    + " user_id=?",
                                            user));
            assertEquals(1, revoke.get(2, TimeUnit.SECONDS));
            releaseResponse.countDown();
            assertNotEquals(200, request.get(15, TimeUnit.SECONDS).getStatusCode().value());
        } finally {
            releaseResponse.countDown();
            pool.shutdownNow();
        }
        assertEquals(0, refundPosts.get());
        pendingFunds();
    }

    @Test
    void callbackMerchantMismatchCannotSettleOriginalMerchantFunds() throws Exception {
        approved("REFUND_ONLY", 1000);
        wrongMerchant = true;
        assertNotEquals(
                204,
                refundNotify(refundFact("SUCCESS"), "merchant-mismatch", false)
                        .getStatusCode()
                        .value());
        pendingFunds();
    }

    @Test
    void fundsEndpointsRejectMissingLoginMiniTokenAndForgedRealmWithRealHttp401() {
        approved("REFUND_ONLY", 1000);
        for (String path : List.of(REFUNDS + expectedRefund, RECON)) {
            for (String token : List.of("", owner)) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Client-Type", "admin");
                if (!token.isEmpty()) headers.setBearerAuth(token);
                status(
                        http.exchange(
                                "http://127.0.0.1:" + port + path,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                String.class),
                        401);
            }
            HttpHeaders forged = new HttpHeaders();
            forged.setBearerAuth(owner);
            forged.set("X-Client-Type", "owner");
            status(
                    http.exchange(
                            "http://127.0.0.1:" + port + path,
                            HttpMethod.GET,
                            new HttpEntity<>(forged),
                            String.class),
                    401);
        }
        assertEquals(0, refundPosts.get());
    }

    @Test
    void concurrentWorkersOnlyClaimOneRefundPost() throws Exception {
        approved("REFUND_ONLY", 1000);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var start = new CountDownLatch(1);
            Callable<Void> call =
                    () -> {
                        start.await();
                        job.runOnce();
                        return null;
                    };
            Future<Void> a = pool.submit(call), b = pool.submit(call);
            start.countDown();
            a.get(20, TimeUnit.SECONDS);
            b.get(20, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
        pendingFunds();
    }

    @Test
    void trustedOriginalPaymentStateRefundAlsoAllowsRemainingApprovedRefund() {
        approved("REFUND_ONLY", 1000);
        paymentState = "REFUND";
        run();
        assertEquals(1, refundPosts.get());
        assertEquals("PROCESSING", refundState("status"));
        pendingFunds();
    }

    @Test
    void refundUnknownCreatesOnePermanentReconciliationCaseAndRepeatedQueryDoesNotDuplicate() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        JsonNode row = reconciliation();
        assertEquals("REFUND", row.path("type").asText());
        lostResponse = false;
        queryError = true;
        due();
        data(query());
        JsonNode repeated = reconciliation();
        assertEquals(row.path("caseId"), repeated.path("caseId"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_payment_reconciliation WHERE kind='REFUND' AND"
                                + " subject_id=?",
                        Integer.class,
                        refundId));
        pendingFunds();
    }

    @Test
    void reasonAloneCannotResolveUnknownOrMismatchedFinancialFact() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        JsonNode row = reconciliation();
        lostResponse = false;
        queryError = true;
        ResponseEntity<String> result =
                call(
                        "POST",
                        RECON + "/" + row.path("caseId").asText() + "/resolve",
                        Map.of(
                                "expectedVersion",
                                row.path("version").asLong(),
                                "reason",
                                "人工描述不是可信已退证据"),
                        finance);
        assertNotEquals(200, result.getStatusCode().value());
        assertNotEquals("RESOLVED", reconciliation().path("status").asText());
        pendingFunds();
    }

    @Test
    void matchingTrustedRequeryThenExplicitResolveClosesOriginalReconciliationCase() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        JsonNode row = reconciliation();
        lostResponse = false;
        due();
        data(
                call(
                        "POST",
                        RECON + "/" + row.path("caseId").asText() + "/query",
                        Map.of(
                                "expectedVersion",
                                row.path("version").asLong(),
                                "reason",
                                "重查原商户原退款号"),
                        finance));
        JsonNode observed = reconciliation();
        JsonNode resolved =
                data(
                        call(
                                "POST",
                                RECON + "/" + observed.path("caseId").asText() + "/resolve",
                                Map.of(
                                        "expectedVersion",
                                        observed.path("version").asLong(),
                                        "reason",
                                        "可信资金事实与本地一致"),
                                finance));
        assertEquals("RESOLVED", resolved.path("status").asText());
        assertEquals(row.path("caseId"), resolved.path("caseId"));
        assertEquals("1000", state("refunded_fen"));
        assertEquals(1, refundPosts.get());
    }

    @Test
    void reconciliationPermissionDepartmentAndVersionAreEnforced() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        JsonNode row = reconciliation();
        String outsider = scoped(SITE_DEPT, FINANCE);
        JsonNode hidden = ViJson.tree(call("GET", RECON, null, outsider).getBody());
        assertEquals(0, hidden.path("total").asLong());
        status(
                call(
                        "POST",
                        RECON + "/" + row.path("caseId").asText() + "/query",
                        Map.of("expectedVersion", row.path("version").asLong(), "reason", "越权"),
                        outsider),
                404);
        status(
                call(
                        "POST",
                        RECON + "/" + row.path("caseId").asText() + "/resolve",
                        Map.of("expectedVersion", 999, "reason", "旧版"),
                        finance),
                409);
        status(
                call(
                        "POST",
                        RECON + "/9223372036854775807/query",
                        Map.of("expectedVersion", 0, "reason", "猜测编号"),
                        finance),
                404);
    }

    @Test
    void paymentUnknownAlsoPersistsTypedCaseWithoutPretendingRefundCaseExists() {
        jdbc.update(
                "UPDATE vi_payment_order SET status='UNKNOWN',paid_fen=0,transaction_id=NULL WHERE"
                        + " order_id=?",
                orderId);
        paymentState = "USERPAYING";
        data(ownerCall("GET", "/payment", null, owner, null));
        JsonNode row = reconciliation();
        assertEquals("PAYMENT", row.path("type").asText());
        assertEquals(0, count("vi_refund_order"));
        assertTrue(paymentQueries.get() >= 1);
        assertEquals("UNKNOWN", state("status"));
        assertNotEquals("RESOLVED", row.path("status").asText());
    }

    @Test
    void outboxFailureAfterTrustedSuccessRollsBackFundSettlementThenQueriesSameNumber() {
        approved("REFUND_ONLY", 1000);
        postState = "SUCCESS";
        doThrow(new IllegalStateException("隔离退款outbox失败"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), eq("REFUND_SUCCESS"), any());
        run();
        pendingFunds();
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='REFUND_SUCCESS'",
                        Integer.class));
        org.mockito.Mockito.reset(outbox);
        due();
        data(query());
        assertEquals("1000", state("refunded_fen"));
        assertEquals(1, refundPosts.get());
    }

    @Test
    void databaseFailureRollsBackTrustedCallbackAndIdenticalRetryCanSucceed() throws Exception {
        approved("REFUND_ONLY", 1000);
        run();
        jdbc.execute(
                "ALTER TABLE vi_payment_order ADD CONSTRAINT ck_p04_it_balance CHECK"
                        + " (refunded_fen=0)");
        try {
            assertNotEquals(
                    204,
                    refundNotify(refundFact("SUCCESS"), "callback-db-retry", false)
                            .getStatusCode()
                            .value());
            pendingFunds();
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_outbox WHERE event_type='REFUND_SUCCESS'",
                            Integer.class));
        } finally {
            jdbc.execute("ALTER TABLE vi_payment_order DROP CHECK ck_p04_it_balance");
        }
        status(refundNotify(refundFact("SUCCESS"), "callback-db-retry", false), 204);
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
    }

    @Test
    void currentValidSigningRotationPreservesOriginalMerchantAndRefundIdentity() throws Exception {
        approved("REFUND_ONLY", 1000);
        String originalRevision = state("merchant_revision_id");
        var generator = java.security.KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var rotated = generator.generateKeyPair();
        merchantConfig.put("merchantCertificateSerial", "AACC");
        merchant =
                pub(
                        save(
                                merchant,
                                merchantConfig,
                                replace(
                                        "merchantPrivateKey",
                                        pem("PRIVATE KEY", rotated.getPrivate().getEncoded()))));
        requestPublicKey = rotated.getPublic();
        enableOffline(false, false);
        run();
        assertEquals(1, refundPosts.get());
        assertNull(providerFailure);
        assertTrue(signedSerials.contains("AACC"));
        assertEquals("1234567890", state("mchid"));
        assertEquals(originalRevision, state("merchant_revision_id"));
        assertEquals(expectedRefund, detail().path("refundNo").asText());
    }

    @Test
    void requestIdempotencyNeverChangesOriginalAmountOrRepeatsSuccessfulSettlement() {
        approved("REFUND_ONLY", 1000);
        run();
        due();
        Map<String, Object> input = action();
        String key = UUID.randomUUID().toString();
        JsonNode first =
                data(callKey("POST", REFUNDS + expectedRefund + "/query", input, finance, key));
        int queryCount = refundQueries.get();
        JsonNode repeated =
                data(callKey("POST", REFUNDS + expectedRefund + "/query", input, finance, key));
        assertEquals(first.path("refundNo"), repeated.path("refundNo"));
        assertEquals(queryCount, refundQueries.get());
        input.put("reason", "同键不同内容");
        status(callKey("POST", REFUNDS + expectedRefund + "/query", input, finance, key), 409);
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(1, refundPosts.get());
    }

    @Test
    void alteredRefundCannotExecuteWithoutMatchingOriginalApprovedAmount() {
        approved("REFUND_ONLY", 1000);
        jdbc.update("UPDATE vi_refund_order SET amount_fen=1001 WHERE id=?", refundId);
        job.runOnce();
        assertEquals(0, refundPosts.get());
        assertEquals(0, paymentQueries.get());
        assertEquals("1000", state("refund_reserved_fen"));
        assertEquals("0", state("refunded_fen"));
        assertNotEquals("SUCCESS", refundState("status"));
    }

    @Test
    void callerCannotOverrideRefundAmountCaseOrChannelFromRetryBody() {
        approved("REFUND_ONLY", 1000);
        for (String field :
                List.of(
                        "amount",
                        "caseId",
                        "paymentId",
                        "merchantRevisionId",
                        "executionMode",
                        "outRefundNo")) {
            var input = action();
            input.put(field, "forged");
            status(call("POST", REFUNDS + expectedRefund + "/retry", input, finance), 400);
        }
        assertEquals(0, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
        pendingFunds();
    }

    @Test
    void sixActualHttpResponsesConformToPrimaryContract() throws Exception {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        var dir = java.nio.file.Files.createTempDirectory("p04-contract-");
        java.nio.file.Files.writeString(
                dir.resolve("refund.json"),
                call("GET", REFUNDS + expectedRefund, null, finance).getBody());
        JsonNode row = reconciliation();
        lostResponse = false;
        due();
        ResponseEntity<String> queried = query();
        status(queried, 200);
        java.nio.file.Files.writeString(dir.resolve("query.json"), queried.getBody());
        ResponseEntity<String> retried = retry();
        status(retried, 200);
        java.nio.file.Files.writeString(dir.resolve("retry.json"), retried.getBody());
        java.nio.file.Files.writeString(
                dir.resolve("reconciliation.json"), call("GET", RECON, null, finance).getBody());
        JsonNode current = reconciliation();
        ResponseEntity<String> rechecked =
                call(
                        "POST",
                        RECON + "/" + current.path("caseId").asText() + "/query",
                        Map.of(
                                "expectedVersion",
                                current.path("version").asLong(),
                                "reason",
                                "契约采集重查"),
                        finance);
        status(rechecked, 200);
        java.nio.file.Files.writeString(dir.resolve("recheck.json"), rechecked.getBody());
        current = reconciliation();
        ResponseEntity<String> resolved =
                call(
                        "POST",
                        RECON + "/" + current.path("caseId").asText() + "/resolve",
                        Map.of(
                                "expectedVersion",
                                current.path("version").asLong(),
                                "reason",
                                "可信事实一致后核实"),
                        finance);
        status(resolved, 200);
        java.nio.file.Files.writeString(dir.resolve("resolve.json"), resolved.getBody());
        java.nio.file.Path repo =
                java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (!java.nio.file.Files.exists(repo.resolve("docs/vehicle"))) repo = repo.getParent();
        Process process =
                new ProcessBuilder(
                                System.getenv().getOrDefault("VI_PYTHON", "python3"),
                                repo.resolve(
                                                "xinyi-admin/src/test/resources/integration/p04_contract.py")
                                        .toString(),
                                repo.toString(),
                                dir.toString())
                        .redirectErrorStream(true)
                        .start();
        String output =
                new String(
                        process.getInputStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(process.waitFor(15, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), output);
    }

    @Test
    void operatorReasonIsAuthenticatedEncryptedAndNeverAppearsInPublicResponsesOrPlainAudit() {
        approved("REFUND_ONLY", 1000);
        run();
        due();
        String reason = "仅授权审计查看的重查依据 Bearer p04-test-only-sensitive-note";
        var body = action();
        body.put("reason", reason);
        data(call("POST", REFUNDS + expectedRefund + "/query", body, finance));
        long user = Long.parseLong(userId(finance));
        String audit =
                jdbc.queryForObject(
                        "SELECT after_summary FROM vi_event_log WHERE"
                                + " action='REFUND_QUERY_ACCEPTED' AND aggregate_id=? AND"
                                + " actor_user_id=?",
                        String.class,
                        refundId,
                        user);
        byte[] envelope =
                Base64.getDecoder().decode(ViJson.tree(audit).path("reasonEnvelope").asText());
        String aad = "P04:ADMIN:" + user + ":REFUND_QUERY:" + refundId;
        assertEquals(reason, reasonCrypto.unenvelope(envelope, aad));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        reasonCrypto.unenvelope(
                                envelope, "P04:ADMIN:" + user + ":REFUND_RETRY:" + refundId));
        assertThrows(
                com.xinyi.rongli.platform.error.ViException.class,
                () ->
                        reasonCrypto.unenvelope(
                                envelope, "P04:ADMIN:" + user + ":REFUND_QUERY:" + (refundId + 1)));
        for (Map<String, Object> row :
                jdbc.queryForList("SELECT before_summary,after_summary,reason FROM vi_event_log"))
            for (Object value : row.values())
                assertFalse(Objects.toString(value, "").contains("p04-test-only-sensitive-note"));
        assertFalse(detail().toString().contains("reasonEnvelope"));
        assertFalse(detail().toString().contains("p04-test-only-sensitive-note"));
        for (String payload : jdbc.queryForList("SELECT payload FROM vi_outbox", String.class))
            assertFalse(payload.contains("p04-test-only-sensitive-note"));
        for (String cached :
                jdbc.queryForList(
                        "SELECT result_json FROM vi_idempotency WHERE result_json IS NOT NULL",
                        String.class)) assertFalse(cached.contains("p04-test-only-sensitive-note"));
    }

    @Test
    void manualOperationReasonsAreRecordedOncePerSuccessfulIdempotentIntent() {
        approved("REFUND_ONLY", 1000);
        lostResponse = true;
        run();
        lostResponse = false;
        due();
        var queryBody = action();
        queryBody.put("reason", "原号重查的操作依据");
        String queryKey = UUID.randomUUID().toString();
        data(callKey("POST", REFUNDS + expectedRefund + "/query", queryBody, finance, queryKey));
        data(callKey("POST", REFUNDS + expectedRefund + "/query", queryBody, finance, queryKey));
        var retryBody = action();
        retryBody.put("reason", "已成功退款的本地复核依据");
        String retryKey = UUID.randomUUID().toString();
        data(callKey("POST", REFUNDS + expectedRefund + "/retry", retryBody, finance, retryKey));
        data(callKey("POST", REFUNDS + expectedRefund + "/retry", retryBody, finance, retryKey));
        JsonNode reconcile = reconciliation();
        long caseId = reconcile.path("caseId").asLong();
        var recheckBody =
                Map.of(
                        "expectedVersion",
                        reconcile.path("version").asLong(),
                        "reason",
                        "以渠道事实核对账本");
        String recheckKey = UUID.randomUUID().toString();
        data(callKey("POST", RECON + "/" + caseId + "/query", recheckBody, finance, recheckKey));
        data(callKey("POST", RECON + "/" + caseId + "/query", recheckBody, finance, recheckKey));
        reconcile = reconciliation();
        var resolveBody =
                Map.of(
                        "expectedVersion",
                        reconcile.path("version").asLong(),
                        "reason",
                        "资金与可信终态一致");
        String resolveKey = UUID.randomUUID().toString();
        data(callKey("POST", RECON + "/" + caseId + "/resolve", resolveBody, finance, resolveKey));
        data(callKey("POST", RECON + "/" + caseId + "/resolve", resolveBody, finance, resolveKey));
        assertReasonAudit("REFUND_QUERY_ACCEPTED", "REFUND_QUERY", refundId, "原号重查的操作依据");
        assertReasonAudit("REFUND_RETRY_ACCEPTED", "REFUND_RETRY", refundId, "已成功退款的本地复核依据");
        assertReasonAudit(
                "RECONCILIATION_QUERY_INTENT", "RECONCILIATION_QUERY", caseId, "以渠道事实核对账本");
        assertReasonAudit("RECONCILIATION_RESOLVED", "RECONCILIATION_RESOLVE", caseId, "资金与可信终态一致");
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(1, refundPosts.get());
    }

    void assertReasonAudit(String action, String operation, long id, String reason) {
        long user = Long.parseLong(userId(finance));
        List<String> summaries =
                jdbc.queryForList(
                        "SELECT after_summary FROM vi_event_log WHERE action=? AND aggregate_id=?"
                                + " AND actor_user_id=?",
                        String.class,
                        action,
                        id,
                        user);
        assertEquals(1, summaries.size(), action);
        byte[] envelope =
                Base64.getDecoder()
                        .decode(ViJson.tree(summaries.get(0)).path("reasonEnvelope").asText());
        assertEquals(
                reason,
                reasonCrypto.unenvelope(
                        envelope, "P04:ADMIN:" + user + ":" + operation + ":" + id));
    }

    @Test
    void abnormalRefundPreservesReservationAndRetryOnlyQueriesOriginalNumber() {
        approved("REFUND_ONLY", 1000);
        postState = "ABNORMAL";
        refundState = "ABNORMAL";
        run();
        assertEquals("ABNORMAL", refundState("status"));
        pendingFunds();
        JsonNode anomaly = reconciliation();
        assertEquals("REFUND", anomaly.path("type").asText());
        due();
        JsonNode retried = data(retry());
        assertEquals("ABNORMAL", retried.path("status").asText());
        assertEquals(expectedRefund, retried.path("refundNo").asText());
        assertEquals(1, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
        pendingFunds();
        anomaly = reconciliation();
        assertNotEquals(
                200,
                call(
                                "POST",
                                RECON + "/" + anomaly.path("caseId").asText() + "/resolve",
                                Map.of(
                                        "expectedVersion",
                                        anomaly.path("version").asLong(),
                                        "reason",
                                        "文字不能代替异常退款的可信终态"),
                                finance)
                        .getStatusCode()
                        .value());
        refundState = "SUCCESS";
        due();
        data(query());
        assertEquals("SUCCESS", refundState("status"));
        assertEquals("1000", state("refunded_fen"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(1, refundPosts.get());
        assertEquals(1, count("vi_refund_order"));
    }

    JsonNode reconciliation() {
        ResponseEntity<String> result = call("GET", RECON + "?pageSize=100", null, finance);
        status(result, 200);
        JsonNode rows = ViJson.tree(result.getBody()).path("rows");
        for (JsonNode row : rows) if (no.equals(row.path("orderNo").asText())) return row;
        fail("原订单资金异常未持久化或未按权限返回");
        return null;
    }

    long pickedUp() {
        long driver = SEQ.incrementAndGet(),
                assignment = SEQ.incrementAndGet(),
                invite = SEQ.incrementAndGet();
        long driverAccount = miniTokens.resolveAccess(mini()).accountId();
        jdbc.update(
                "INSERT INTO vi_driver_profile(id,account_id,qualification_status) VALUES"
                        + " (?,?,'APPROVED')",
                driver,
                driverAccount);
        jdbc.update(
                "INSERT INTO vi_driver_assignment(id,order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES (?,?,?,?,UTC_TIMESTAMP(3))",
                assignment,
                orderId,
                driver,
                invite);
        jdbc.update(
                "INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES"
                        + " (?,'ASSIGNMENT',?,?)",
                driver,
                assignment,
                orderId);
        jdbc.update(
                "UPDATE vi_order SET"
                    + " custody_type='DRIVER',custody_ref_id=?,active_assignment_id=?,fulfillment_status='PICKED_UP'"
                    + " WHERE id=?",
                driver,
                assignment,
                orderId);
        return driver;
    }
}
