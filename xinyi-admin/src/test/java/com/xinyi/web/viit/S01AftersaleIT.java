package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

/** 售后HTTP/真实会话/资金与订单行锁/私有凭证/事务审计；供应商仅继承隔离支付替身。 */
class S01AftersaleIT extends PaymentHttpFixture {
    static final Path PRIVATE_ROOT = privateRoot();
    @MockitoSpyBean OutboxService outbox;

    static Path privateRoot() {
        try {
            return Files.createTempDirectory("xinyi-s01-private-").toRealPath();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void privateFiles(DynamicPropertyRegistry registry) {
        registry.add("vi.files.root", () -> PRIVATE_ROOT.toString());
    }

    @BeforeEach
    void paidOrder() {
        // 父夹具已确认隔离库保护标记；这里生成售后起点，不把支付服务替换成测试实现。
        jdbc.execute("DELETE FROM vi_aftersale_note");
        jdbc.execute("DELETE FROM vi_aftersale_case");
        jdbc.execute("DELETE FROM vi_file");
        jdbc.update(
                "UPDATE vi_order SET fulfillment_status='PENDING_DISPATCH' WHERE id=?", orderId);
        jdbc.update(
                "UPDATE vi_payment_order SET status='SUCCESS',paid_fen=12345 WHERE order_id=?",
                orderId);
    }

    @AfterAll
    static void removeTestBytes() throws Exception {
        try (var paths = Files.walk(PRIVATE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        }
    }

    Map<String, Object> applyBody(String kind, long amount) {
        return new LinkedHashMap<>(
                Map.of(
                        "kind",
                        kind,
                        "reason",
                        "原始申请说明",
                        "reasonCode",
                        "PLAN_CHANGED",
                        "requestedAmount",
                        amount,
                        "expectedOrderVersion",
                        version(),
                        "voucherFileIds",
                        List.of()));
    }

    long version() {
        return jdbc.queryForObject("SELECT version FROM vi_order WHERE id=?", Long.class, orderId);
    }

    ResponseEntity<String> apply(Map<String, Object> body) {
        return apply(body, UUID.randomUUID().toString());
    }

    ResponseEntity<String> apply(Map<String, Object> body, String key) {
        return ownerCall("POST", "/after-sales", body, owner, key);
    }

    ResponseEntity<String> ownerPath(
            String method, String path, Object body, String token, String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Type", "owner");
        if (key != null) headers.set("Idempotency-Key", key);
        return http.exchange(
                "http://127.0.0.1:" + port + path,
                HttpMethod.valueOf(method),
                new HttpEntity<>(body, headers),
                String.class);
    }

    JsonNode detail(String id) {
        return data(ownerPath("GET", "/api/app/owner/after-sales/" + id, null, owner, null));
    }

    ResponseEntity<String> note(String id, long version, String note, String key) {
        return ownerPath(
                "POST",
                "/api/app/owner/after-sales/" + id + "/notes",
                Map.of("expectedVersion", version, "note", note),
                owner,
                key);
    }

    int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    void status(ResponseEntity<String> response, int expected) {
        assertEquals(expected, response.getStatusCode().value(), response.getBody());
    }

    @Test
    void paidBeforePickupAlwaysWaitsForManualReviewWithoutReservingMoney() {
        JsonNode row = data(apply(applyBody("CANCEL_REFUND", 12345)));
        assertEquals("PENDING_REVIEW", row.path("caseStatus").asText());
        assertEquals("PENDING", row.path("reviewStatus").asText());
        assertEquals("NONE", row.path("refundState").asText());
        assertTrue(row.path("approvedAmount").isNull());
        assertEquals(
                "APPLIED",
                jdbc.queryForObject(
                        "SELECT cancel_status FROM vi_order WHERE id=?", String.class, orderId));
        assertEquals(
                "PENDING_DISPATCH",
                jdbc.queryForObject(
                        "SELECT fulfillment_status FROM vi_order WHERE id=?",
                        String.class,
                        orderId));
        assertEquals("SUCCESS", state("status"));
        assertEquals("0", state("refund_reserved_fen"));
        assertEquals(0, count("vi_refund_order"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_outbox WHERE event_type='AFTERSALE_APPLIED'",
                        Integer.class));
    }

    @Test
    void refundOnlyKeepsCancellationCustodyAndAssignment() {
        long assignment = SEQ.incrementAndGet();
        jdbc.update(
                "UPDATE vi_order SET"
                    + " custody_type='DRIVER',custody_ref_id=88,active_assignment_id=?,fulfillment_status='PICKED_UP'"
                    + " WHERE id=?",
                assignment,
                orderId);
        JsonNode row = data(apply(applyBody("REFUND_ONLY", 2345)));
        assertEquals("DRIVER", row.path("custody").asText());
        assertEquals(
                "NONE",
                jdbc.queryForObject(
                        "SELECT cancel_status FROM vi_order WHERE id=?", String.class, orderId));
        assertEquals(
                assignment,
                jdbc.queryForObject(
                        "SELECT active_assignment_id FROM vi_order WHERE id=?",
                        Long.class,
                        orderId));
        assertEquals("NONE", row.path("refundState").asText());
    }

    @Test
    void pickedUpCancellationDoesNotPretendVehicleReturned() {
        jdbc.update(
                "UPDATE vi_order SET"
                    + " custody_type='STATION',custody_ref_id=11,fulfillment_status='ARRIVED_STATION'"
                    + " WHERE id=?",
                orderId);
        JsonNode row = data(apply(applyBody("CANCEL_REFUND", 1000)));
        assertEquals("STATION", row.path("custody").asText());
        assertEquals("UNDETERMINED", row.path("returnState").asText());
        assertNull(
                jdbc.queryForObject(
                        "SELECT returned_at FROM vi_order WHERE id=?", Object.class, orderId));
    }

    @Test
    void cancelMayRequestZeroButRefundOnlyCannot() {
        status(apply(applyBody("REFUND_ONLY", 0)), 422);
        assertEquals(0, count("vi_aftersale_case"));
        assertEquals(
                0, data(apply(applyBody("CANCEL_REFUND", 0))).path("requestedAmount").asLong());
    }

    @Test
    void availableRefundSubtractsExistingRefundAndReservation() {
        jdbc.update(
                "UPDATE vi_payment_order SET refunded_fen=2000,refund_reserved_fen=1000 WHERE"
                    + " order_id=?",
                orderId);
        status(apply(applyBody("REFUND_ONLY", 9346)), 422);
        assertEquals(0, count("vi_aftersale_case"));
        data(apply(applyBody("REFUND_ONLY", 9345)));
        assertEquals("1000", state("refund_reserved_fen"));
    }

    @Test
    void unpaidAndStaleOrderCannotCreateApplication() {
        Map<String, Object> request = applyBody("CANCEL_REFUND", 1000);
        request.put("expectedOrderVersion", 99);
        status(apply(request), 409);
        jdbc.update(
                "UPDATE vi_payment_order SET paid_fen=0,status='PENDING' WHERE order_id=?",
                orderId);
        status(apply(applyBody("CANCEL_REFUND", 1000)), 409);
        assertEquals(0, count("vi_aftersale_case"));
    }

    @Test
    void duplicateEntryAndRetryReturnOriginalWithoutChangingReason() {
        Map<String, Object> request = applyBody("CANCEL_REFUND", 1000);
        String key = UUID.randomUUID().toString();
        JsonNode original = data(apply(request, key));
        assertEquals(original.path("caseId"), data(apply(request, key)).path("caseId"));
        Map<String, Object> other = applyBody("REFUND_ONLY", 2000);
        other.put("reason", "不能覆盖原说明");
        JsonNode duplicate = data(apply(other));
        assertEquals(original.path("caseId"), duplicate.path("caseId"));
        assertEquals("原始申请说明", duplicate.path("reason").asText());
        status(apply(other, key), 409);
        assertEquals(1, count("vi_aftersale_case"));
    }

    @Test
    void concurrentApplyHasOneApplicationAuditAndEvent() throws Exception {
        Map<String, Object> request = applyBody("CANCEL_REFUND", 1000);
        ExecutorService workers = Executors.newFixedThreadPool(4);
        try {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 4; i++)
                results.add(workers.submit(() -> data(apply(request)).path("caseId").asText()));
            Set<String> ids = new HashSet<>();
            for (Future<String> result : results) ids.add(result.get(30, TimeUnit.SECONDS));
            assertEquals(1, ids.size());
        } finally {
            workers.shutdownNow();
        }
        assertEquals(1, count("vi_aftersale_case"));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='AFTERSALE_APPLY'",
                        Integer.class));
    }

    @Test
    void notesAppendWithoutOverwritingOriginalOrApprovedAmount() {
        JsonNode row = data(apply(applyBody("REFUND_ONLY", 1000)));
        String id = row.path("caseId").asText();
        String key = UUID.randomUUID().toString();
        JsonNode changed = data(note(id, row.path("version").asLong(), "第一条补充", key));
        data(note(id, row.path("version").asLong(), "第一条补充", key));
        assertEquals(1, changed.path("notes").size());
        assertEquals("原始申请说明", changed.path("reason").asText());
        assertTrue(changed.path("approvedAmount").isNull());
        assertEquals("第一条补充", changed.path("notes").get(0).path("note").asText());
        assertEquals(1, count("vi_aftersale_note"));
        status(note(id, 0, "旧版本", UUID.randomUUID().toString()), 409);
    }

    @Test
    void concurrentNotesOneVersionWinsAndClosedCaseRejectsNotes() throws Exception {
        String id = data(apply(applyBody("REFUND_ONLY", 1000))).path("caseId").asText();
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> a =
                    workers.submit(
                            () ->
                                    note(id, 0, "并发甲", UUID.randomUUID().toString())
                                            .getStatusCode()
                                            .value());
            Future<Integer> b =
                    workers.submit(
                            () ->
                                    note(id, 0, "并发乙", UUID.randomUUID().toString())
                                            .getStatusCode()
                                            .value());
            assertEquals(
                    Set.of(200, 409),
                    Set.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS)));
        } finally {
            workers.shutdownNow();
        }
        jdbc.update(
                "UPDATE vi_aftersale_case SET case_status='CLOSED' WHERE id=?", Long.parseLong(id));
        status(note(id, 1, "办结后不能补充", UUID.randomUUID().toString()), 409);
        assertEquals(1, count("vi_aftersale_note"));
    }

    @Test
    void anotherOwnerCannotReadWriteOrListPrivateApplication() {
        String id = data(apply(applyBody("REFUND_ONLY", 1000))).path("caseId").asText();
        String stranger = mini();
        status(ownerPath("GET", "/api/app/owner/after-sales/" + id, null, stranger, null), 404);
        status(
                ownerPath(
                        "POST",
                        "/api/app/owner/after-sales/" + id + "/notes",
                        Map.of("expectedVersion", 0, "note", "越权"),
                        stranger,
                        UUID.randomUUID().toString()),
                404);
        status(
                ownerCall(
                        "POST",
                        "/after-sales",
                        applyBody("REFUND_ONLY", 1000),
                        stranger,
                        UUID.randomUUID().toString()),
                404);
        JsonNode list =
                ViJson.tree(
                        ownerPath("GET", "/api/app/owner/after-sales", null, stranger, null)
                                .getBody());
        assertEquals(0, list.path("total").asLong());
        assertFalse(list.toString().contains("原始申请说明"));
    }

    @Test
    void strictInputAndQueryDoNotAcceptAuthorityOrSql() {
        for (String field : List.of("deptId", "applicantId", "approvedAmount", "params")) {
            Map<String, Object> request = applyBody("REFUND_ONLY", 1000);
            request.put(field, 1);
            status(apply(request), 400);
        }
        Map<String, Object> decimal = applyBody("REFUND_ONLY", 1000);
        decimal.put("requestedAmount", 1.5);
        status(apply(decimal), 400);
        status(
                ownerPath("GET", "/api/app/owner/after-sales?sort=id%20DESC", null, owner, null),
                400);
        status(
                ownerPath("GET", "/api/app/owner/after-sales?visibleDeptIds=1", null, owner, null),
                400);
    }

    @Test
    void listPagesUseOwnerScopeAndShowOnlyPublicState() {
        String id = data(apply(applyBody("REFUND_ONLY", 1000))).path("caseId").asText();
        JsonNode list =
                ViJson.tree(
                        ownerCall("GET", "/after-sales?pageSize=1", null, owner, null).getBody());
        assertEquals(200, list.path("code").asInt());
        assertEquals(1, list.path("total").asLong());
        assertEquals(id, list.path("rows").get(0).path("caseId").asText());
        JsonNode empty =
                ViJson.tree(
                        ownerPath(
                                        "GET",
                                        "/api/app/owner/after-sales?status=CLOSED",
                                        null,
                                        owner,
                                        null)
                                .getBody());
        assertEquals(0, empty.path("total").asLong());
        assertTrue(detail(id).path("publicOpinion").isNull());
        assertFalse(detail(id).has("applicantId"));
    }

    @Test
    void revokedSessionCannotAppend() {
        String id = data(apply(applyBody("REFUND_ONLY", 1000))).path("caseId").asText();
        long session = miniTokens.resolveAccess(owner).sessionId();
        jdbc.update(
                "UPDATE vi_session SET revoked_at=UTC_TIMESTAMP(3),status='REVOKED' WHERE id=?",
                session);
        status(note(id, 0, "旧令牌", UUID.randomUUID().toString()), 401);
        assertEquals(0, count("vi_aftersale_note"));
    }

    @Test
    void outboxFailureRollsBackApplicationCancellationAndSuccessAudit() {
        doThrow(new IllegalStateException("隔离注入outbox失败"))
                .when(outbox)
                .publish(anyString(), anyString(), anyLong(), eq("AFTERSALE_APPLIED"), any());
        status(apply(applyBody("CANCEL_REFUND", 1000)), 500);
        assertEquals(0, count("vi_aftersale_case"));
        assertEquals(
                "NONE",
                jdbc.queryForObject(
                        "SELECT cancel_status FROM vi_order WHERE id=?", String.class, orderId));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='AFTERSALE_APPLY'",
                        Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_event_log WHERE action='AFTERSALE_APPLY_DENIED'",
                        Integer.class));
    }

    JsonNode upload(String purpose, String order, String token, boolean complete) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", output));
        byte[] bytes = output.toByteArray();
        JsonNode ticket =
                data(
                        ownerPath(
                                "POST",
                                "/api/common/uploads",
                                Map.of(
                                        "purpose",
                                        purpose,
                                        "orderNo",
                                        order,
                                        "fileName",
                                        "售后.png",
                                        "mimeType",
                                        "image/png",
                                        "size",
                                        bytes.length),
                                token,
                                UUID.randomUUID().toString()));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("X-Client-Type", "owner");
        headers.set("X-File-Ticket", ticket.path("requiredHeaders").path("X-File-Ticket").asText());
        headers.setContentType(MediaType.IMAGE_PNG);
        var sent =
                http.exchange(
                        "http://127.0.0.1:" + port + ticket.path("uploadUrl").asText(),
                        HttpMethod.PUT,
                        new HttpEntity<>(bytes, headers),
                        String.class);
        status(sent, 204);
        if (complete)
            data(
                    ownerPath(
                            "POST",
                            "/api/common/uploads/" + ticket.path("fileId").asText() + "/complete",
                            null,
                            token,
                            null));
        return ticket;
    }

    @Test
    void realPrivateVoucherLinksAndRemainsReadable() throws Exception {
        String file = upload("DISPUTE", no, owner, true).path("fileId").asText();
        Map<String, Object> request = applyBody("REFUND_ONLY", 1000);
        request.put("voucherFileIds", List.of(file));
        JsonNode row = data(apply(request));
        assertEquals(file, row.path("voucherFileIds").get(0).asText());
        JsonNode access =
                data(
                        ownerPath(
                                "POST",
                                "/api/common/files/" + file + "/access",
                                Map.of("purpose", "查看售后申请凭证", "orderNo", no),
                                owner,
                                null));
        assertEquals("APPROVED", access.path("status").asText());
    }

    @Test
    void unavailableForeignOrWrongPurposeVoucherFailsWholeApplication() throws Exception {
        String unavailable = upload("DISPUTE", no, owner, false).path("fileId").asText();
        String ready = upload("DISPUTE", no, owner, true).path("fileId").asText();
        Map<String, Object> request = applyBody("CANCEL_REFUND", 1000);
        request.put("voucherFileIds", List.of(ready, unavailable));
        status(apply(request), 409);
        assertEquals(0, count("vi_aftersale_case"));
        String wrongPurpose = upload("VEHICLE_PHOTO", no, owner, true).path("fileId").asText();
        request.put("voucherFileIds", List.of(wrongPurpose));
        status(apply(request), 404);
        request.put("voucherFileIds", List.of("9223372036854775807"));
        status(apply(request), 404);
        assertEquals(
                "NONE",
                jdbc.queryForObject(
                        "SELECT cancel_status FROM vi_order WHERE id=?", String.class, orderId));
    }
}
