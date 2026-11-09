package com.xinyi.rongli.verification.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.verification.dto.ManualVerificationRequests;
import com.xinyi.rongli.verification.service.ManualVerificationService;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 不接收凭证或供应商通过声明；公司、操作人和最终证明由统一Service数据库判定。 */
@RestController
@RequestMapping("/api/admin/manual-verifications")
public class ManualVerificationController {
    private final ManualVerificationService service;

    public ManualVerificationController(ManualVerificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        admin(client);
        return response(service.list(ManualVerificationRequests.query(query, false)));
    }

    @GetMapping("/applications")
    public ResponseEntity<?> applications(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        admin(client);
        return response(service.applications(ManualVerificationRequests.query(query, true)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @PathVariable String id,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        admin(client);
        noQuery(query);
        return response(ViSuccess.ok(service.get(ManualVerificationRequests.id(id))));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        admin(client);
        noQuery(query);
        return response(
                ViSuccess.ok(
                        service.create(
                                ManualVerificationRequests.create(body), FileRequests.key(key))));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<?> review(
            @PathVariable String id,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        admin(client);
        noQuery(query);
        return response(
                ViSuccess.ok(
                        service.review(
                                ManualVerificationRequests.id(id),
                                ManualVerificationRequests.review(body),
                                FileRequests.key(key))));
    }

    private static void admin(String client) {
        if (!"admin".equals(client)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void noQuery(Map<String, String> query) {
        if (!query.isEmpty()) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value);
    }
}
