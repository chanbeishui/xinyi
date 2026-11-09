package com.xinyi.rongli.training.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.training.dto.LearningRequests;
import com.xinyi.rongli.training.service.TrainingPolicyService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 公司显式政策入口；功能权、原始公司范围、CAS与发布目标由统一Service实时核验。 */
@RestController
@RequestMapping("/api/admin/training-policies/{ownerDeptId}")
public class TrainingPolicyController {
    private final TrainingPolicyService service;

    public TrainingPolicyController(TrainingPolicyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> get(
            @PathVariable String ownerDeptId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.get(LearningRequests.id(ownerDeptId)));
    }

    @PutMapping
    public ResponseEntity<?> save(
            @PathVariable String ownerDeptId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.save(
                        LearningRequests.id(ownerDeptId),
                        LearningRequests.policy(body),
                        FileRequests.key(key)));
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish(
            @PathVariable String ownerDeptId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.publish(
                        LearningRequests.id(ownerDeptId),
                        LearningRequests.publish(body),
                        FileRequests.key(key)));
    }

    private static void client(String value) {
        if (!"admin".equals(value)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(value));
    }
}
