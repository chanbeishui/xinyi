package com.xinyi.rongli.refund.controller;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.refund.dto.RefundRequests;
import com.xinyi.rongli.refund.service.*;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** 后台资金入口只做协议适配，收款主体与双域会话权限均由Service从原支付记录校验。 */
@RestController
@RequestMapping("/api/admin")
public class RefundController {
    private final RefundExecutionService refunds;
    private final PaymentReconciliationService reconciliation;

    public RefundController(
            RefundExecutionService refunds, PaymentReconciliationService reconciliation) {
        this.refunds = refunds;
        this.reconciliation = reconciliation;
    }

    @GetMapping("/refunds/{refundNo}")
    public ResponseEntity<?> get(
            @PathVariable String refundNo,
            @RequestHeader(value = "X-Client-Type", required = false) String client) {
        client(client);
        return response(ViSuccess.ok(refunds.get(no(refundNo))));
    }

    @PostMapping("/refunds/{refundNo}/query")
    public ResponseEntity<?> query(
            @PathVariable String refundNo,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        refunds.action(
                                no(refundNo),
                                RefundRequests.action(body),
                                RefundRequests.key(key),
                                false)));
    }

    @PostMapping("/refunds/{refundNo}/retry")
    public ResponseEntity<?> retry(
            @PathVariable String refundNo,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        refunds.action(
                                no(refundNo),
                                RefundRequests.action(body),
                                RefundRequests.key(key),
                                true)));
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(reconciliation.list(RefundRequests.query(query)));
    }

    @PostMapping("/reconciliation/{caseId}/query")
    public ResponseEntity<?> reconQuery(
            @PathVariable String caseId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        reconciliation.query(
                                id(caseId), RefundRequests.action(body), RefundRequests.key(key))));
    }

    @PostMapping("/reconciliation/{caseId}/resolve")
    public ResponseEntity<?> resolve(
            @PathVariable String caseId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        reconciliation.resolve(
                                id(caseId), RefundRequests.action(body), RefundRequests.key(key))));
    }

    private static long id(String value) {
        try {
            return ViId.parse(value);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String no(String value) {
        if (!value.matches("[A-Za-z0-9_-]{1,64}"))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        return value;
    }

    private static void client(String value) {
        if (!"admin".equals(value)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
