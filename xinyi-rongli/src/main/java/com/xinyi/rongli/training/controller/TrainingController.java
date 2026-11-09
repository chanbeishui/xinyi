package com.xinyi.rongli.training.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.refund.dto.RefundRequests;
import com.xinyi.rongli.training.dto.TrainingRequests;
import com.xinyi.rongli.training.service.TrainingService;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** 后台课程协议入口；真实ADMIN、公司范围、版本与题库答案投影在统一Service校验。 */
@RestController
@RequestMapping("/api/admin/training-courses")
public class TrainingController {
    private final TrainingService service;

    public TrainingController(TrainingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.list(TrainingRequests.query(query)));
    }

    @GetMapping("/{trainingId}")
    public ResponseEntity<?> get(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        if (!query.isEmpty()) invalid();
        return response(ViSuccess.ok(service.get(TrainingRequests.id(trainingId))));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(service.create(TrainingRequests.create(body), FileRequests.key(key))));
    }

    @PutMapping("/{trainingId}")
    public ResponseEntity<?> update(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.update(
                                TrainingRequests.id(trainingId),
                                TrainingRequests.update(body),
                                FileRequests.key(key))));
    }

    @PostMapping("/{trainingId}/publish")
    public ResponseEntity<?> publish(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.publish(
                                TrainingRequests.id(trainingId),
                                RefundRequests.action(body),
                                FileRequests.key(key))));
    }

    private static void client(String client) {
        if (!"admin".equals(client)) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
