package com.xinyi.rongli.verification.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.training.dto.LearningRequests;
import com.xinyi.rongli.verification.dto.VerificationRequests;
import com.xinyi.rongli.verification.service.DeviceAuthenticationService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 设备认证任务只可授权查看与显式封存；不把封存当作生物认证或人工核验通过。 */
@RestController
@RequestMapping("/api/admin/verification-sessions")
public class VerificationSessionAdminController {
    private final DeviceAuthenticationService service;

    public VerificationSessionAdminController(DeviceAuthenticationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.adminList(VerificationRequests.query(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @PathVariable String id,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.adminGet(LearningRequests.id(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(
            @PathVariable String id,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.close(
                        LearningRequests.id(id),
                        VerificationRequests.close(body),
                        FileRequests.key(key)));
    }

    private static void client(String value) {
        if (!"admin".equals(value)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(value));
    }
}
