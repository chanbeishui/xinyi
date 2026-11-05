package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

/** 真实后台HTTP/权限/资金与业务锁；渠道只继承支付夹具的隔离替身，不替换审核服务。 */
@TestPropertySource(
        properties = {"vi.jobs.dispatch-enabled=true", "vi.jobs.dispatch-delay-ms=3600000"})
class S02AftersaleIT extends PaymentHttpFixture {
    static final Path PRIVATE_ROOT = privateRoot();
    static final String AFTER = "/api/admin/after-sales";
    static final String[] REVIEW_PERMISSIONS = {
        "biz:aftersale:list",
        "biz:aftersale:query",
        "biz:aftersale:note",
        "biz:aftersale:create",
        "biz:aftersale:reviewpreview",
        "biz:aftersale:review",
        "biz:refund:query",
        "biz:refund:approve",
        "biz:uploads:create",
        "biz:uploads:complete",
        "biz:files:query",
        "biz:files:access"
    };
    @MockitoSpyBean OutboxService outbox;
    @MockitoSpyBean RedisCache redis;
    @Autowired com.xinyi.rongli.jobs.service.DispatchDeadlineJob dispatchJob;
    @Autowired com.xinyi.rongli.identity.service.PhoneCrypto phoneCrypto;
    @Autowired com.xinyi.rongli.address.service.AddressCrypto addressCrypto;
    String reviewer;

    static Path privateRoot() {
        try {
            return Files.createTempDirectory("xinyi-s02-private-").toRealPath();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void privateFiles(DynamicPropertyRegistry properties) {
        properties.add("vi.files.root", () -> PRIVATE_ROOT.toString());
        properties.add("vi.crypto.data-key", () -> "s02-isolated-address-snapshot-key");
    }

    @BeforeEach
    void paidStart() {
        // 父夹具已检查隔离库名称及保护标记；这里只建立测试原订单的可信收款起点。
        for (String table :
                List.of(
                        "vi_aftersale_participant_user",
                        "vi_aftersale_note",
                        "vi_aftersale_review",
                        "vi_aftersale_case",
                        "vi_file",
                        "vi_driver_occupancy",
                        "vi_driver_assignment",
                        "vi_dispatch_invite",
                        "vi_quota_reservation",
                        "vi_quota_slot")) jdbc.execute("DELETE FROM " + table);
        jdbc.update(
                "UPDATE vi_order SET fulfillment_status='PENDING_DISPATCH' WHERE id=?", orderId);
        jdbc.update(
                "UPDATE vi_payment_order SET"
                    + " status='SUCCESS',paid_fen=12345,payment_method='WECHAT',merchant_profile_id=?,merchant_revision_id=?,app_id='wx0000000000000000',mchid='1234567890',method_locked_at=UTC_TIMESTAMP(3)"
                    + " WHERE order_id=?",
                merchant.path("profileId").asLong(),
                merchant.path("activeRevisionId").asLong(),
                orderId);
        reviewer = scopedWithScope(COMPANY_A, "4", REVIEW_PERMISSIONS);
    }

    @AfterAll
    static void removeTestBytes() throws Exception {
        try (var paths = Files.walk(PRIVATE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        }
    }

    long orderVersion() {
        return jdbc.queryForObject("SELECT version FROM vi_order WHERE id=?", Long.class, orderId);
    }

    String orderState(String field) {
        return jdbc.queryForObject(
                "SELECT " + field + " FROM vi_order WHERE id=?", String.class, orderId);
    }

    Map<String, Object> applyBody(String kind, long amount) {
        return new LinkedHashMap<>(
                Map.of(
                        "kind",
                        kind,
                        "reason",
                        "售后原申请",
                        "reasonCode",
                        "PLAN_CHANGED",
                        "requestedAmount",
                        amount,
                        "expectedOrderVersion",
                        orderVersion(),
                        "voucherFileIds",
                        List.of()));
    }

    JsonNode ownerApply(String kind, long amount) {
        return data(
                ownerCall(
                        "POST",
                        "/after-sales",
                        applyBody(kind, amount),
                        owner,
                        UUID.randomUUID().toString()));
    }

    JsonNode adminApply(String kind, long amount, String admin) {
        return data(
                call(
                        "POST",
                        "/api/admin/orders/" + no + "/after-sales",
                        applyBody(kind, amount),
                        admin));
    }

    String path(JsonNode row, String suffix) {
        return AFTER + "/" + row.path("caseId").asText() + suffix;
    }

    JsonNode adminDetail(JsonNode row, String admin) {
        return data(call("GET", path(row, ""), null, admin));
    }

    Map<String, Object> previewBody(JsonNode row, long amount) {
        return new LinkedHashMap<>(
                Map.of("expectedVersion", row.path("version").asLong(), "approvedAmount", amount));
    }

    JsonNode preview(JsonNode row, long amount, String admin) {
        return data(call("POST", path(row, "/review-preview"), previewBody(row, amount), admin));
    }

    Map<String, Object> decision(JsonNode row, long amount, String decision) {
        return new LinkedHashMap<>(
                Map.of(
                        "expectedVersion",
                        row.path("version").asLong(),
                        "observedOrderVersion",
                        orderVersion(),
                        "decision",
                        decision,
                        "approvedAmount",
                        amount,
                        "opinion",
                        "仅运营可见的核查意见",
                        "publicOpinion",
                        "已核实本次申请"));
    }

    ResponseEntity<String> review(JsonNode row, long amount, String decision, String admin) {
        return call("POST", path(row, "/review"), decision(row, amount, decision), admin);
    }

    int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    void status(ResponseEntity<String> response, int status) {
        assertEquals(status, response.getStatusCode().value(), response.getBody());
    }

    String userId(String token) {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return tokens.getLoginUser(request).getUserId().toString();
    }

    ResponseEntity<String> ownerPath(String method, String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Type", "owner");
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers),
                String.class);
    }

    JsonNode ownerDetail(JsonNode row) {
        return data(
                ownerPath(
                        "GET",
                        "/api/app/owner/after-sales/" + row.path("caseId").asText(),
                        null,
                        owner));
    }

    @Test
    void previewReadsRealtimeBalanceAndCustodyWithoutReservationOrDecision() {
        JsonNode row = ownerApply("CANCEL_REFUND", 5000);
        jdbc.update(
                "UPDATE vi_payment_order SET refunded_fen=1000,refund_reserved_fen=2000 WHERE"
                        + " order_id=?",
                orderId);
        JsonNode result = preview(row, 5000, reviewer);
        assertEquals(12345, result.path("paidAmount").asLong());
        assertEquals(1000, result.path("refundedAmount").asLong());
        assertEquals(2000, result.path("reservedAmount").asLong());
        assertEquals(9345, result.path("availableAmount").asLong());
        assertFalse(result.path("returnRequired").asBoolean());
        assertEquals("OWNER", result.path("custody").asText());
        assertEquals("2000", state("refund_reserved_fen"));
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals(0, count("vi_refund_order"));
        assertTrue(adminDetail(row, reviewer).path("approvedAmount").isNull());
    }

    @Test
    void cancelBeforePickupHasOneDecisionReservationStableRefundAndNoChannelCall() {
        JsonNode row = ownerApply("CANCEL_REFUND", 5000);
        int beforeCalls = calls.get();
        JsonNode result = data(review(row, 5000, "APPROVE", reviewer));
        assertEquals("APPROVED", result.path("reviewStatus").asText());
        assertEquals(5000, result.path("approvedAmount").asLong());
        assertEquals("CANCELLED", orderState("fulfillment_status"));
        assertEquals("COMPLETED", orderState("cancel_status"));
        assertEquals("5000", state("refund_reserved_fen"));
        assertEquals("0", state("refunded_fen"));
        assertEquals(1, count("vi_aftersale_review"));
        assertEquals(1, count("vi_refund_order"));
        assertEquals(beforeCalls, calls.get(), "审核只登记执行意图，不能在事务内直接访问渠道");
        assertEquals(
                "WECHAT",
                jdbc.queryForObject("SELECT payment_method FROM vi_refund_order", String.class));
        assertEquals(
                "WECHAT_API",
                jdbc.queryForObject("SELECT execution_mode FROM vi_refund_order", String.class));
        assertEquals(
                result.path("refundNo").asText(),
                jdbc.queryForObject("SELECT out_refund_no FROM vi_refund_order", String.class));
    }

    @Test
    void zeroCancellationCompletesWithoutRefundOrderOrReservation() {
        JsonNode row = ownerApply("CANCEL_REFUND", 0);
        JsonNode result = data(review(row, 0, "APPROVE", reviewer));
        assertEquals("CLOSED", result.path("caseStatus").asText());
        assertEquals("NOT_REQUIRED", result.path("refundState").asText());
        assertEquals("COMPLETED", orderState("cancel_status"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(0, count("vi_refund_order"));
    }

    @Test
    void refundOnlyDoesNotChangeFulfillmentCustodyOrAssignment() {
        long driver = assignment(true);
        String assignment = orderState("active_assignment_id");
        JsonNode row = ownerApply("REFUND_ONLY", 3000);
        JsonNode result = data(review(row, 3000, "APPROVE", reviewer));
        assertEquals("PICKED_UP", orderState("fulfillment_status"));
        assertEquals("NONE", orderState("cancel_status"));
        assertEquals("DRIVER", orderState("custody_type"));
        assertEquals(assignment, orderState("active_assignment_id"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        Integer.class,
                        driver));
        assertEquals("NOT_REQUIRED", result.path("returnState").asText());
        assertEquals("3000", state("refund_reserved_fen"));
    }

    @Test
    void pickedUpCancellationReservesButWaitsForRealReturnAndKeepsDriverAndCapacity() {
        long driver = assignment(true);
        long quota = quota("CONFIRMED");
        JsonNode row = ownerApply("CANCEL_REFUND", 5000);
        assertTrue(preview(row, 5000, reviewer).path("returnRequired").asBoolean());
        JsonNode result = data(review(row, 5000, "APPROVE", reviewer));
        assertEquals("APPROVED_WAIT_RETURN", orderState("cancel_status"));
        assertEquals("PICKED_UP", orderState("fulfillment_status"));
        assertEquals("WAIT_RETURN", result.path("returnState").asText());
        assertEquals("WAIT_RETURN", result.path("refundState").asText());
        assertEquals("5000", state("refund_reserved_fen"));
        assertNotNull(orderState("active_assignment_id"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        Integer.class,
                        driver));
        assertEquals(
                "CONFIRMED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_quota_reservation WHERE slot_id=?",
                        String.class,
                        quota));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT confirmed_quota FROM vi_quota_slot WHERE id=?",
                        Integer.class,
                        quota));
    }

    @Test
    void zeroPickedUpCancellationAlsoWaitsForActualReturn() {
        assignment(true);
        JsonNode row = ownerApply("CANCEL_REFUND", 0);
        JsonNode result = data(review(row, 0, "APPROVE", reviewer));
        assertEquals("APPROVED_WAIT_RETURN", orderState("cancel_status"));
        assertEquals("WAIT_RETURN", result.path("returnState").asText());
        assertEquals("EXECUTING", result.path("caseStatus").asText());
        assertNotNull(orderState("active_assignment_id"));
        assertEquals(0, count("vi_refund_order"));
    }

    @Test
    void rejectionKeepsPhysicalFactsWithoutAddingReservation() {
        long driver = assignment(true);
        JsonNode row = ownerApply("CANCEL_REFUND", 4000);
        JsonNode result = data(review(row, 0, "REJECT", reviewer));
        assertEquals("REJECTED", result.path("caseStatus").asText());
        assertEquals("NONE", orderState("cancel_status"));
        assertEquals("PICKED_UP", orderState("fulfillment_status"));
        assertEquals("DRIVER", orderState("custody_type"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        Integer.class,
                        driver));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(0, count("vi_refund_order"));
        assertEquals(
                "REJECT",
                jdbc.queryForObject(
                        "SELECT decision FROM vi_aftersale_review WHERE case_id=?",
                        String.class,
                        row.path("caseId").asLong()));
        assertEquals("已核实本次申请", ownerDetail(row).path("publicOpinion").asText());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='AFTERSALE_REVIEW'",
                        Integer.class));
    }

    @Test
    void publicOpinionIsSeparateFromInternalOpinion() {
        completeOrderSnapshots();
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        data(review(row, 1000, "APPROVE", reviewer));
        JsonNode ownerView = ownerDetail(row);
        assertEquals("已核实本次申请", ownerView.path("publicOpinion").asText());
        assertFalse(ownerView.toString().contains("仅运营可见的核查意见"));
        assertFalse(ownerView.has("opinion"));
        assertEquals(
                "仅运营可见的核查意见",
                adminDetail(row, reviewer).path("reviews").get(0).path("opinion").asText());
        JsonNode order = data(ownerCall("GET", "", null, owner, null));
        assertEquals("已核实本次申请", order.path("afterSale").path("publicOpinion").asText());
        assertFalse(order.toString().contains("仅运营可见的核查意见"));
    }

    @Test
    void staleApplicationVersionReturnsConflictAndKeepsOriginalPending() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        Map<String, Object> body = decision(row, 1000, "APPROVE");
        body.put("expectedVersion", 99);
        status(call("POST", path(row, "/review"), body, reviewer), 409);
        assertEquals("PENDING", adminDetail(row, reviewer).path("reviewStatus").asText());
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals("0", state("refund_reserved_fen"));
    }

    @Test
    void custodyChangedAfterPreviewRequiresNewOrderVersion() {
        JsonNode row = ownerApply("CANCEL_REFUND", 1000);
        Map<String, Object> body = decision(row, 1000, "APPROVE");
        assignment(true);
        jdbc.update("UPDATE vi_order SET version=version+1 WHERE id=?", orderId);
        status(call("POST", path(row, "/review"), body, reviewer), 409);
        assertEquals("PENDING", adminDetail(row, reviewer).path("reviewStatus").asText());
        assertNotNull(orderState("active_assignment_id"));
        assertEquals("0", state("refund_reserved_fen"));
    }

    @Test
    void availableAmountIsRecheckedInsideReviewTransaction() {
        JsonNode row = ownerApply("REFUND_ONLY", 9000);
        preview(row, 9000, reviewer);
        jdbc.update(
                "UPDATE vi_payment_order SET refunded_fen=2000,refund_reserved_fen=2000 WHERE"
                        + " order_id=?",
                orderId);
        status(review(row, 9000, "APPROVE", reviewer), 422);
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals("2000", state("refund_reserved_fen"));
        data(review(row, 8345, "APPROVE", reviewer));
        assertEquals("10345", state("refund_reserved_fen"));
    }

    @Test
    void refundOnlyZeroRejectNonzeroAndDecimalAmountsAreInvalid() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        status(review(row, 0, "APPROVE", reviewer), 422);
        status(review(row, 1, "REJECT", reviewer), 400);
        var decimal = decision(row, 1000, "APPROVE");
        decimal.put("approvedAmount", 1.5);
        status(call("POST", path(row, "/review"), decimal, reviewer), 400);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void concurrentReviewsCreateOneDecisionOneReservationOneRefund() throws Exception {
        JsonNode row = ownerApply("REFUND_ONLY", 3000);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var start = new CountDownLatch(1);
            Callable<Integer> action =
                    () -> {
                        start.await();
                        return review(row, 3000, "APPROVE", reviewer).getStatusCode().value();
                    };
            Future<Integer> a = pool.submit(action), b = pool.submit(action);
            start.countDown();
            assertEquals(
                    Set.of(200, 409),
                    Set.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS)));
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, count("vi_aftersale_review"));
        assertEquals(1, count("vi_refund_order"));
        assertEquals("3000", state("refund_reserved_fen"));
    }

    @Test
    void sameIdempotencyRetryKeepsOriginalRefundAndDifferentRequestConflicts() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        Map<String, Object> body = decision(row, 1000, "APPROVE");
        String key = UUID.randomUUID().toString();
        JsonNode original = data(callKey("POST", path(row, "/review"), body, reviewer, key));
        JsonNode retry = data(callKey("POST", path(row, "/review"), body, reviewer, key));
        assertEquals(original.path("refundNo"), retry.path("refundNo"));
        body.put("approvedAmount", 999);
        status(callKey("POST", path(row, "/review"), body, reviewer, key), 409);
        assertEquals(1, count("vi_aftersale_review"));
        assertEquals("1000", state("refund_reserved_fen"));
    }

    @Test
    void functionalReviewAndFinancialPermissionAreBothRequired() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String reviewOnly =
                scopedWithScope(
                        COMPANY_A, "1", "biz:aftersale:review", "biz:aftersale:reviewpreview");
        String financeOnly =
                scopedWithScope(COMPANY_A, "1", "biz:refund:approve", "biz:refund:query");
        status(review(row, 1000, "APPROVE", reviewOnly), 403);
        status(review(row, 1000, "APPROVE", financeOnly), 403);
        status(call("POST", path(row, "/review-preview"), previewBody(row, 1000), reviewOnly), 403);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void orderDepartmentAndOriginalReceiverDepartmentMustBothBeCovered() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String onlyOrder = scoped(SITE_DEPT, REVIEW_PERMISSIONS);
        String onlyReceiver = scoped(COMPANY_A, REVIEW_PERMISSIONS);
        status(review(row, 1000, "APPROVE", onlyOrder), 404);
        status(review(row, 1000, "APPROVE", onlyReceiver), 404);
        status(call("POST", path(row, "/review-preview"), previewBody(row, 1000), onlyOrder), 404);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void emptyScopeFailsClosedForListDetailAndReview() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String empty = scopedWithScope(COMPANY_B, "5", REVIEW_PERMISSIONS);
        assertEquals(
                0, ViJson.tree(call("GET", AFTER, null, empty).getBody()).path("total").asInt());
        status(call("GET", path(row, ""), null, empty), 404);
        status(review(row, 1000, "APPROVE", empty), 404);
    }

    @Test
    void filtersAndOwnerOrderListsRespectRealScope() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        JsonNode all =
                ViJson.tree(
                        call("GET", AFTER + "?pageSize=1&kind=REFUND_ONLY", null, reviewer)
                                .getBody());
        assertEquals(1, all.path("total").asInt());
        assertEquals(row.path("caseId"), all.path("rows").get(0).path("caseId"));
        assertEquals(
                1,
                ViJson.tree(
                                call(
                                                "GET",
                                                "/api/admin/orders/" + no + "/after-sales",
                                                null,
                                                reviewer)
                                        .getBody())
                        .path("total")
                        .asInt());
        assertEquals(
                1,
                ViJson.tree(
                                call(
                                                "GET",
                                                "/api/admin/owners/" + account + "/after-sales",
                                                null,
                                                reviewer)
                                        .getBody())
                        .path("total")
                        .asInt());
        String outsider = scoped(COMPANY_B, REVIEW_PERMISSIONS);
        assertEquals(
                0,
                ViJson.tree(
                                call(
                                                "GET",
                                                "/api/admin/owners/" + account + "/after-sales",
                                                null,
                                                outsider)
                                        .getBody())
                        .path("total")
                        .asInt());
    }

    @Test
    void adminCreatedApplicationCannotBeSelfReviewed() {
        JsonNode row = adminApply("REFUND_ONLY", 1000, root);
        status(review(row, 1000, "APPROVE", root), 403);
        data(review(row, 1000, "APPROVE", reviewer));
    }

    @Test
    void miniApplicantTrustedEmployeeBindingCannotSelfReview() {
        bind(account, Long.parseLong(userId(reviewer)));
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        status(review(row, 1000, "APPROVE", reviewer), 403);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void adminNoteContributorCannotReviewOrOverwriteOriginalReason() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        JsonNode noted =
                data(
                        call(
                                "POST",
                                path(row, "/notes"),
                                Map.of(
                                        "expectedVersion",
                                        row.path("version").asLong(),
                                        "note",
                                        "运营补充证据"),
                                reviewer));
        assertEquals("售后原申请", noted.path("reason").asText());
        status(review(noted, 1000, "APPROVE", reviewer), 403);
        data(review(noted, 1000, "APPROVE", root));
    }

    @Test
    void originalPaymentMethodControlsRefundEvenWhenCurrentSettingsAreDisabled() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        // 当前配置可变，原资金身份不可变；审核不应重新按当前支付开关决定是否允许历史退款。
        jdbc.update(
                "UPDATE vi_integration_profile SET active_revision_id=NULL WHERE"
                        + " kind='PAYMENT_POLICY'");
        data(review(row, 1000, "APPROVE", reviewer));
        assertEquals(
                "WECHAT",
                jdbc.queryForObject("SELECT payment_method FROM vi_refund_order", String.class));
        assertEquals(
                "WECHAT_API",
                jdbc.queryForObject("SELECT execution_mode FROM vi_refund_order", String.class));
    }

    @Test
    void offlineRefundUsesManualEvidenceModeWithoutSendingWechat() {
        jdbc.update(
                "UPDATE vi_payment_order SET"
                    + " payment_method='OFFLINE',merchant_profile_id=NULL,merchant_revision_id=NULL,app_id=NULL,mchid=NULL"
                    + " WHERE order_id=?",
                orderId);
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        int before = calls.get();
        data(review(row, 1000, "APPROVE", reviewer));
        assertEquals(
                "OFFLINE",
                jdbc.queryForObject("SELECT payment_method FROM vi_refund_order", String.class));
        assertEquals(
                "MANUAL_OFFLINE",
                jdbc.queryForObject("SELECT execution_mode FROM vi_refund_order", String.class));
        assertEquals(before, calls.get());
        assertEquals("0", state("refunded_fen"));
    }

    @Test
    void strictReviewInputRejectsForgedAuthorityAndMissingPublicOpinion() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        for (String field :
                List.of("deptId", "receiverDeptId", "reviewerUserId", "returnRequired", "params")) {
            var input = decision(row, 1000, "APPROVE");
            input.put(field, 1);
            status(call("POST", path(row, "/review"), input, reviewer), 400);
        }
        var missing = decision(row, 1000, "APPROVE");
        missing.remove("publicOpinion");
        status(call("POST", path(row, "/review"), missing, reviewer), 400);
        status(call("GET", AFTER + "?visibleDeptIds=1", null, reviewer), 400);
        status(call("GET", AFTER + "/9223372036854775807", null, reviewer), 404);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void oldAuthorizationVersionIsDeniedEvenWhenRedisEvictionFails() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        long reviewerId = Long.parseLong(userId(reviewer));
        doThrow(new IllegalStateException("隔离模拟缓存删除失败")).when(redis).deleteObject(anyString());
        jdbc.update("UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?", reviewerId);
        status(review(row, 1000, "APPROVE", reviewer), 401);
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals("0", state("refund_reserved_fen"));
    }

    @Test
    void outboxFailureRollsBackDecisionRefundReservationAndCancellation() {
        JsonNode row = ownerApply("CANCEL_REFUND", 1000);
        doThrow(new IllegalStateException("隔离审核outbox失败"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), anyString(), any());
        status(review(row, 1000, "APPROVE", reviewer), 500);
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals(0, count("vi_refund_order"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals("APPLIED", orderState("cancel_status"));
        assertEquals("PENDING", adminDetail(row, reviewer).path("reviewStatus").asText());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='AFTERSALE_REVIEW'",
                        Integer.class));
    }

    @Test
    void cancellationBeforePickupReleasesOnlyOldAssignmentAndUnusedQuota() {
        long driver = assignment(false);
        long confirmed = quota("CONFIRMED");
        String oldAssignment = orderState("active_assignment_id");
        JsonNode row = ownerApply("CANCEL_REFUND", 1000);
        data(review(row, 1000, "APPROVE", reviewer));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        Integer.class,
                        driver));
        assertEquals(
                "CANCELLED",
                jdbc.queryForObject(
                        "SELECT end_reason FROM vi_driver_assignment WHERE driver_id=?",
                        String.class,
                        driver));
        assertNull(orderState("active_assignment_id"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='ASSIGNMENT_ENDED'",
                        Integer.class));
        String payload =
                jdbc.queryForObject(
                        "SELECT payload FROM vi_outbox WHERE event_type='ASSIGNMENT_ENDED'",
                        String.class);
        assertEquals(oldAssignment, ViJson.tree(payload).path("assignmentId").asText());
        assertEquals(
                "RELEASED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_quota_reservation WHERE slot_id=?",
                        String.class,
                        confirmed));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT confirmed_quota FROM vi_quota_slot WHERE id=?",
                        Integer.class,
                        confirmed));
    }

    @Test
    void cancellationRevokesOnlyCorrespondingWaitingInviteAndOccupancy() {
        long invite = SEQ.incrementAndGet(), driver = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_dispatch_invite(id,order_id,driver_id,status,source,issued_at,expires_at)"
                    + " VALUES"
                    + " (?,?,?,'WAIT_RESPONSE','AUTO',UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 5 MINUTE))",
                invite,
                orderId,
                driver);
        jdbc.update(
                "INSERT INTO"
                    + " vi_driver_occupancy(driver_id,holder_type,holder_id,order_id,expires_at)"
                    + " VALUES (?,'INVITE',?,?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 5 MINUTE))",
                driver,
                invite,
                orderId);
        JsonNode row = ownerApply("CANCEL_REFUND", 0);
        data(review(row, 0, "APPROVE", reviewer));
        assertEquals(
                "REVOKED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_dispatch_invite WHERE id=?", String.class, invite));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_driver_occupancy WHERE driver_id=?",
                        Integer.class,
                        driver));
    }

    @Test
    void returnedBeforeReviewDoesNotWaitAgainOrReleaseDriversNewTask() {
        long driver = assignment(true);
        long consumed = quota("CONSUMED");
        JsonNode row = ownerApply("CANCEL_REFUND", 1000);
        long oldAssignment = Long.parseLong(orderState("active_assignment_id"));
        jdbc.update(
                "UPDATE vi_driver_assignment SET ended_at=UTC_TIMESTAMP(3),end_reason='RETURNED'"
                        + " WHERE id=?",
                oldAssignment);
        jdbc.update("DELETE FROM vi_driver_occupancy WHERE driver_id=?", driver);
        jdbc.update(
                "UPDATE vi_order SET"
                    + " custody_type='OWNER',custody_ref_id=owner_account_id,active_assignment_id=NULL,returned_at=UTC_TIMESTAMP(3),fulfillment_status='WAIT_CONFIRM',version=version+1"
                    + " WHERE id=?",
                orderId);
        long nextOrder = SEQ.incrementAndGet(), nextAssignment = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_order(id,order_no,owner_account_id,station_id,dept_id,quote_id,vehicle_snapshot,fulfillment_status,custody_type,custody_ref_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version,active_assignment_id)"
                    + " SELECT"
                    + " ?,?,owner_account_id,station_id,dept_id,?,vehicle_snapshot,'ACCEPTED','OWNER',owner_account_id,pickup_starts_at,pickup_ends_at,pay_expires_at,total_fen,terms_version,?"
                    + " FROM vi_order WHERE id=?",
                nextOrder,
                "S02_NEW_" + UUID.randomUUID(),
                SEQ.incrementAndGet(),
                nextAssignment,
                orderId);
        jdbc.update(
                "INSERT INTO vi_driver_assignment(id,order_id,driver_id,invite_id,accepted_at)"
                        + " VALUES (?,?,?,?,UTC_TIMESTAMP(3))",
                nextAssignment,
                nextOrder,
                driver,
                SEQ.incrementAndGet());
        jdbc.update(
                "INSERT INTO vi_driver_occupancy(driver_id,holder_type,holder_id,order_id) VALUES"
                        + " (?,'ASSIGNMENT',?,?)",
                driver,
                nextAssignment,
                nextOrder);
        JsonNode result = data(review(row, 1000, "APPROVE", reviewer));
        assertNotEquals("WAIT_RETURN", result.path("returnState").asText());
        assertNotEquals("WAIT_RETURN", result.path("refundState").asText());
        assertEquals("COMPLETED", orderState("cancel_status"));
        assertEquals(
                nextOrder,
                jdbc.queryForObject(
                        "SELECT order_id FROM vi_driver_occupancy WHERE driver_id=?",
                        Long.class,
                        driver));
        assertNull(
                jdbc.queryForObject(
                        "SELECT ended_at FROM vi_driver_assignment WHERE id=?",
                        Object.class,
                        nextAssignment));
        assertEquals(
                "CONSUMED",
                jdbc.queryForObject(
                        "SELECT status FROM vi_quota_reservation WHERE slot_id=?",
                        String.class,
                        consumed));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT consumed_quota FROM vi_quota_slot WHERE id=?",
                        Integer.class,
                        consumed));
    }

    @Test
    void createPermissionDoesNotGrantNotePermission() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String createOnly = scopedWithScope(COMPANY_A, "1", "biz:aftersale:create");
        status(
                call(
                        "POST",
                        path(row, "/notes"),
                        Map.of("expectedVersion", row.path("version").asLong(), "note", "无补充权限"),
                        createOnly),
                403);
        assertEquals(0, count("vi_aftersale_note"));
    }

    @Test
    void adminVoucherOnlyBecomesOwnerReadableAfterExplicitPublicAssociation() throws Exception {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String file = uploadAdmin("DISPUTE", no, reviewer, true).path("fileId").asText();
        String access = "/api/common/files/" + file + "/access";
        status(ownerPath("POST", access, Map.of("purpose", "查看附件", "orderNo", no), owner), 404);
        JsonNode noted =
                data(
                        call(
                                "POST",
                                path(row, "/notes"),
                                Map.of(
                                        "expectedVersion",
                                        row.path("version").asLong(),
                                        "note",
                                        "向车主补充的说明",
                                        "voucherFileIds",
                                        List.of(file)),
                                reviewer));
        assertEquals(file, noted.path("notes").get(0).path("voucherFileIds").get(0).asText());
        JsonNode grant =
                data(ownerPath("POST", access, Map.of("purpose", "查看补充材料", "orderNo", no), owner));
        assertEquals("APPROVED", grant.path("status").asText());
        status(ownerPath("POST", access, Map.of("purpose", "猜测他人附件", "orderNo", no), mini()), 404);
    }

    @Test
    void adminInvalidVoucherGroupDoesNotAppendPartiallyOrContributeToReview() throws Exception {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String good = uploadAdmin("DISPUTE", no, reviewer, true).path("fileId").asText();
        String wrong = uploadAdmin("VEHICLE_PHOTO", no, reviewer, true).path("fileId").asText();
        status(
                call(
                        "POST",
                        path(row, "/notes"),
                        Map.of(
                                "expectedVersion",
                                row.path("version").asLong(),
                                "note",
                                "整组应拒绝",
                                "voucherFileIds",
                                List.of(good, wrong)),
                        reviewer),
                404);
        assertEquals(0, count("vi_aftersale_note"));
        assertEquals(row.path("version"), adminDetail(row, reviewer).path("version"));
        data(review(row, 1000, "APPROVE", reviewer));
    }

    @Test
    void participantSeparationSurvivesBindingRevocation() {
        long employee = Long.parseLong(userId(reviewer));
        bind(account, employee);
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        jdbc.update(
                "UPDATE vi_employee_binding SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE"
                        + " account_id=?",
                account);
        status(review(row, 1000, "APPROVE", reviewer), 403);
        assertEquals(0, count("vi_aftersale_review"));
    }

    JsonNode uploadAdmin(String purpose, String orderNo, String admin, boolean finish)
            throws Exception {
        var image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        var output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", output));
        byte[] bytes = output.toByteArray();
        JsonNode ticket =
                data(
                        call(
                                "POST",
                                "/api/common/uploads",
                                Map.of(
                                        "purpose",
                                        purpose,
                                        "orderNo",
                                        orderNo,
                                        "fileName",
                                        "后台证据.png",
                                        "mimeType",
                                        "image/png",
                                        "size",
                                        bytes.length),
                                admin));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(admin);
        headers.set("X-Client-Type", "admin");
        headers.set("X-File-Ticket", ticket.path("requiredHeaders").path("X-File-Ticket").asText());
        headers.setContentType(MediaType.IMAGE_PNG);
        status(
                http.exchange(
                        "http://127.0.0.1:" + port + ticket.path("uploadUrl").asText(),
                        HttpMethod.PUT,
                        new HttpEntity<>(bytes, headers),
                        String.class),
                204);
        if (finish)
            data(
                    call(
                            "POST",
                            "/api/common/uploads/" + ticket.path("uploadId").asText() + "/complete",
                            null,
                            admin));
        return ticket;
    }

    @Test
    void orderQueryPermissionDoesNotExposeUncoveredFinancialBalances() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        String onlyOrder = scoped(SITE_DEPT, "biz:aftersale:query", "biz:aftersale:list");
        JsonNode detail = adminDetail(row, onlyOrder);
        assertEquals(row.path("caseId"), detail.path("caseId"));
        for (String field :
                List.of(
                        "paidAmount",
                        "refundedAmount",
                        "reservedAmount",
                        "availableAmount",
                        "receiverDeptId"))
            assertFalse(detail.has(field) && !detail.path(field).isNull(), field);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void miniNoteContributionFreezesTrustedEmployeeSeparation() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        bind(account, Long.parseLong(userId(reviewer)));
        JsonNode noted =
                data(
                        ownerPath(
                                "POST",
                                "/api/app/owner/after-sales/"
                                        + row.path("caseId").asText()
                                        + "/notes",
                                Map.of(
                                        "expectedVersion",
                                        row.path("version").asLong(),
                                        "note",
                                        "本人追加材料"),
                                owner));
        jdbc.update(
                "UPDATE vi_employee_binding SET status='REVOKED',revoked_at=UTC_TIMESTAMP(3) WHERE"
                        + " account_id=?",
                account);
        status(review(noted, 1000, "APPROVE", reviewer), 403);
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void databaseWriteFailureRollsBackAllDecisionAndFundChanges() {
        JsonNode row = ownerApply("CANCEL_REFUND", 1000);
        // 只对受保护隔离库增加临时约束，以真实数据库拒绝代替 Service mock；finally 恢复结构。
        jdbc.execute(
                "ALTER TABLE vi_aftersale_review ADD CONSTRAINT ck_s02_it_failure CHECK"
                        + " (approved_fen<>1000)");
        try {
            status(review(row, 1000, "APPROVE", reviewer), 500);
            assertEquals(0, count("vi_aftersale_review"));
            assertEquals(0, count("vi_refund_order"));
            assertEquals("0", state("refund_reserved_fen"));
            assertEquals("APPLIED", orderState("cancel_status"));
            assertEquals("PENDING", adminDetail(row, reviewer).path("reviewStatus").asText());
            assertEquals(
                    0,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM vi_event_log WHERE action='AFTERSALE_REVIEW'",
                            Integer.class));
        } finally {
            jdbc.execute("ALTER TABLE vi_aftersale_review DROP CHECK ck_s02_it_failure");
        }
    }

    @Test
    void approvedRefundOnlyReservationDoesNotBlockScheduledServiceDispatch() {
        scheduledPrerequisites();
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        data(review(row, 1000, "APPROVE", reviewer));
        assertEquals("SCHEDULED", orderState("fulfillment_status"));
        dispatchJob.runOnce();
        assertEquals("PENDING_DISPATCH", orderState("fulfillment_status"));
        assertEquals("1000", state("refund_reserved_fen"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='DISPATCH_REQUESTED' AND"
                                + " aggregate_id=?",
                        Integer.class,
                        orderId));
    }

    @Test
    void successfulRefundOnlyStillAllowsScheduledServiceDispatch() {
        scheduledPrerequisites();
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        data(review(row, 1000, "APPROVE", reviewer));
        // P04尚未实现：仅建立其成功后应持久化的可信资金事实，验证调度不会将仅退款当取消。
        jdbc.update(
                "UPDATE vi_refund_order SET status='SUCCESS',success_at=UTC_TIMESTAMP(3) WHERE"
                        + " case_id=?",
                row.path("caseId").asLong());
        jdbc.update(
                "UPDATE vi_payment_order SET refund_reserved_fen=0,refunded_fen=1000 WHERE"
                        + " order_id=?",
                orderId);
        jdbc.update(
                "UPDATE vi_aftersale_case SET refund_state='SUCCESS',case_status='CLOSED' WHERE"
                        + " id=?",
                row.path("caseId").asLong());
        dispatchJob.runOnce();
        assertEquals("PENDING_DISPATCH", orderState("fulfillment_status"));
        assertEquals("1000", state("refunded_fen"));
    }

    @Test
    void unexplainedRefundBalanceStillBlocksAutomaticDispatch() {
        scheduledPrerequisites();
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        data(review(row, 1000, "APPROVE", reviewer));
        jdbc.update(
                "UPDATE vi_payment_order SET refund_reserved_fen=1001 WHERE order_id=?", orderId);
        dispatchJob.runOnce();
        assertEquals("MANUAL_DISPATCH", orderState("fulfillment_status"));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='DISPATCH_REQUESTED' AND"
                                + " aggregate_id=?",
                        Integer.class,
                        orderId));
    }

    @Test
    void trustedVerifiedPhoneMatchBlocksSelfReviewWithoutEmployeeBinding() {
        String phone = "13800138012";
        jdbc.update(
                "UPDATE sys_user SET phonenumber=? WHERE user_id=?",
                phone,
                Long.parseLong(userId(reviewer)));
        jdbc.update(
                "UPDATE vi_account SET"
                        + " phone_lookup=?,phone_verified_at=UTC_TIMESTAMP(3),phone_source='WECHAT'"
                        + " WHERE id=?",
                phoneCrypto.lookup(phone),
                account);
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_employee_binding WHERE account_id=?",
                        Integer.class,
                        account));
        status(review(row, 1000, "APPROVE", reviewer), 403);
        // 贡献时的可信同人关系已冻结，之后改手机号不能消除独立审核约束。
        jdbc.update(
                "UPDATE sys_user SET phonenumber='13800138013' WHERE user_id=?",
                Long.parseLong(userId(reviewer)));
        status(review(row, 1000, "APPROVE", reviewer), 403);
        assertEquals(0, count("vi_aftersale_review"));
    }

    void scheduledPrerequisites() {
        jdbc.update(
                "UPDATE vi_order SET"
                    + " fulfillment_status='SCHEDULED',dispatch_at=DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 30 SECOND),dispatch_config_version=7 WHERE id=?",
                orderId);
        quota("CONFIRMED");
        long station =
                jdbc.queryForObject(
                        "SELECT station_id FROM vi_order WHERE id=?", Long.class, orderId);
        long receiveSlot = SEQ.incrementAndGet();
        jdbc.update(
                "INSERT INTO"
                    + " vi_quota_slot(id,quota_type,scope_key,station_id,starts_at,ends_at,total_quota,confirmed_quota)"
                    + " VALUES (?,'STATION_RECEIVE',?,?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 2"
                    + " DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 3 DAY),5,1)",
                receiveSlot,
                "station:" + station,
                station);
        jdbc.update(
                "INSERT INTO vi_quota_reservation(order_id,slot_id,status,quantity,expires_at)"
                    + " VALUES (?,?,'CONFIRMED',1,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE))",
                orderId,
                receiveSlot);
    }

    void completeOrderSnapshots() {
        // 支付夹具只建资金订单；读取B04完整投影须补齐当时已经冻结的真实格式快照，而非放宽生产检查。
        long station =
                jdbc.queryForObject(
                        "SELECT station_id FROM vi_order WHERE id=?", Long.class, orderId);
        long quote =
                jdbc.queryForObject(
                        "SELECT quote_id FROM vi_order WHERE id=?", Long.class, orderId);
        Map<String, Object> location = Map.of("lng", 110, "lat", 21, "coordinateSystem", "GCJ02");
        String vehicle =
                ViJson.write(
                        Map.of(
                                "plateNo",
                                "粤G12345",
                                "vehicleTypeCode",
                                "C04_CAR",
                                "vehicleTypeLabel",
                                "小型汽车"));
        String address = ViJson.write(Map.of("address", "隔离测试接车地址", "location", location));
        String contact = ViJson.write(Map.of("displayName", "测试车主", "phone", "13800138021"));
        String stationSnapshot =
                ViJson.write(
                        Map.of(
                                "stationId",
                                Long.toString(station),
                                "name",
                                "隔离检测站",
                                "publicAddress",
                                "测试站点地址",
                                "location",
                                location,
                                "servicePhone",
                                "13800138022"));
        String fees =
                ViJson.write(
                        Map.of(
                                "items",
                                List.of(
                                        Map.of(
                                                "feeCode",
                                                "INSPECTION",
                                                "name",
                                                "检测费",
                                                "amount",
                                                12345,
                                                "calculation",
                                                "测试建单快照"))));
        jdbc.update("UPDATE vi_order SET vehicle_snapshot=? WHERE id=?", vehicle, orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_quote(id,owner_account_id,station_id,vehicle_snapshot,address_snapshot_cipher,pickup_slot_id,rule_id,route_snapshot,amount_fen,items_json,expires_at,used_order_id)"
                    + " VALUES (?,?,?,?,?,1,1,'{}',12345,?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30"
                    + " MINUTE),?)",
                quote,
                account,
                station,
                vehicle,
                addressCrypto.encrypt(address),
                fees,
                orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_order_snapshot(order_id,address_cipher,contact_cipher,station_snapshot,agreement_snapshot,accepted_at)"
                    + " VALUES (?,?,?,?,?,UTC_TIMESTAMP(3))",
                orderId,
                addressCrypto.encrypt(address),
                addressCrypto.encrypt(contact),
                stationSnapshot,
                ViJson.write(
                        Map.of(
                                "termsVersion",
                                "test",
                                "privacyVersion",
                                "test",
                                "accepted",
                                true)));
    }

    @Test
    void rejectRefundOnlyCanPreviewZeroWithoutApprovalOrReservation() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        var input = previewBody(row, 0);
        input.put("decision", "REJECT");
        JsonNode result = data(call("POST", path(row, "/review-preview"), input, reviewer));
        assertFalse(result.path("returnRequired").asBoolean());
        assertEquals(12345, result.path("availableAmount").asLong());
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals(0, count("vi_refund_order"));
        assertEquals("PENDING", adminDetail(row, reviewer).path("reviewStatus").asText());
        input.put("decision", "APPROVE");
        status(call("POST", path(row, "/review-preview"), input, reviewer), 422);
        input.remove("decision");
        status(call("POST", path(row, "/review-preview"), input, reviewer), 422);
    }

    @Test
    void rejectPickedCancellationPreviewDoesNotInventReturnRequirement() {
        assignment(true);
        JsonNode row = ownerApply("CANCEL_REFUND", 1000);
        var input = previewBody(row, 0);
        input.put("decision", "REJECT");
        JsonNode result = data(call("POST", path(row, "/review-preview"), input, reviewer));
        assertFalse(result.path("returnRequired").asBoolean());
        assertEquals("DRIVER", result.path("custody").asText());
        assertEquals("DRIVER", orderState("custody_type"));
        assertEquals("PICKED_UP", orderState("fulfillment_status"));
        assertEquals("APPLIED", orderState("cancel_status"));
        assertNotNull(orderState("active_assignment_id"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(0, count("vi_aftersale_review"));
    }

    @Test
    void previewIdempotencyFreezesInputButRecomputesCurrentFactsWithoutBusinessWrites() {
        JsonNode row = ownerApply("REFUND_ONLY", 1000);
        Map<String, Object> body = previewBody(row, 1000);
        String key = UUID.randomUUID().toString();
        status(callKey("POST", path(row, "/review-preview"), body, reviewer, null), 400);
        JsonNode first = data(callKey("POST", path(row, "/review-preview"), body, reviewer, key));
        jdbc.update("UPDATE vi_order SET version=version+1 WHERE id=?", orderId);
        JsonNode latest = data(callKey("POST", path(row, "/review-preview"), body, reviewer, key));
        assertEquals(first.path("orderVersion").asLong() + 1, latest.path("orderVersion").asLong());
        assertEquals(first.path("caseVersion"), latest.path("caseVersion"));
        assertEquals(12345, latest.path("availableAmount").asLong());
        body.put("approvedAmount", 999);
        status(callKey("POST", path(row, "/review-preview"), body, reviewer, key), 409);
        String cached =
                jdbc.queryForObject(
                        "SELECT result_json FROM vi_idempotency WHERE request_key=? AND"
                            + " action_key=?",
                        String.class,
                        key,
                        "AFTERSALE_REVIEW_PREVIEW:" + row.path("caseId").asText());
        assertFalse(cached.contains("availableAmount"));
        assertFalse(cached.contains("orderVersion"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(0, count("vi_aftersale_review"));
        assertEquals(0, count("vi_refund_order"));
        assertEquals("PENDING", adminDetail(row, reviewer).path("reviewStatus").asText());
    }

    long assignment(boolean pickedUp) {
        long driver = SEQ.incrementAndGet(),
                invite = SEQ.incrementAndGet(),
                assignment = SEQ.incrementAndGet();
        String driverOwner = mini();
        long driverAccount = miniTokens.resolveAccess(driverOwner).accountId();
        jdbc.update(
                "INSERT INTO vi_driver_profile(id,account_id,qualification_status) VALUES"
                        + " (?,?,'APPROVED')",
                driver,
                driverAccount);
        jdbc.update(
                "INSERT INTO"
                    + " vi_dispatch_invite(id,order_id,driver_id,status,source,issued_at,expires_at,responded_at)"
                    + " VALUES"
                    + " (?,?,?,'ACCEPTED','AUTO',UTC_TIMESTAMP(3),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL"
                    + " 5 MINUTE),UTC_TIMESTAMP(3))",
                invite,
                orderId,
                driver);
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
                    + " active_assignment_id=?,fulfillment_status=?,custody_type=?,custody_ref_id=?"
                    + " WHERE id=?",
                assignment,
                pickedUp ? "PICKED_UP" : "ACCEPTED",
                pickedUp ? "DRIVER" : "OWNER",
                pickedUp ? driver : account,
                orderId);
        return driver;
    }

    long quota(String state) {
        long slot = SEQ.incrementAndGet();
        int held = "HELD".equals(state) ? 1 : 0,
                confirmed = "CONFIRMED".equals(state) ? 1 : 0,
                consumed = "CONSUMED".equals(state) ? 1 : 0;
        long station =
                jdbc.queryForObject(
                        "SELECT station_id FROM vi_order WHERE id=?", Long.class, orderId);
        jdbc.update(
                "INSERT INTO"
                    + " vi_quota_slot(id,quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota,confirmed_quota,consumed_quota)"
                    + " VALUES (?,'PICKUP',?,?,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 2"
                    + " DAY),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 3 DAY),5,?,?,?)",
                slot,
                "station:" + station,
                station,
                held,
                confirmed,
                consumed);
        jdbc.update(
                "INSERT INTO vi_quota_reservation(order_id,slot_id,status,quantity,expires_at)"
                        + " VALUES (?,?,?,1,DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 30 MINUTE))",
                orderId,
                slot,
                state);
        return slot;
    }

    void bind(long accountId, long employeeId) {
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,phone_lookup,last_phone_verified_at,employee_auth_version)"
                    + " VALUES (?,?,UTC_TIMESTAMP(3),'wx0000000000000000',?,UTC_TIMESTAMP(3),0)",
                accountId,
                employeeId,
                new byte[32]);
    }
}
