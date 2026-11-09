package com.xinyi.rongli.verification.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.training.dto.LearningRequests;
import com.xinyi.rongli.verification.dto.VerificationRequests;
import com.xinyi.rongli.verification.service.DeviceAuthenticationService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 旧face-session路径兼容为本机认证挑战；不提供实名、人脸活体或客户端passed入口。 */
@RestController
@RequestMapping("/api/app/driver/qualification")
public class DeviceAuthenticationController {
    private final DeviceAuthenticationService service;
    private final BusinessActorResolver actors;

    public DeviceAuthenticationController(
            DeviceAuthenticationService service, BusinessActorResolver actors) {
        this.service = service;
        this.actors = actors;
    }

    @PostMapping("/face-session")
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.create(VerificationRequests.create(body), FileRequests.key(key)));
    }

    @GetMapping("/face-sessions/{verificationSessionId}")
    public ResponseEntity<?> get(
            @PathVariable String verificationSessionId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.get(LearningRequests.id(verificationSessionId)));
    }

    @PostMapping("/face-sessions/{verificationSessionId}/confirm")
    public ResponseEntity<?> confirm(
            @PathVariable String verificationSessionId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.confirm(
                        LearningRequests.id(verificationSessionId),
                        VerificationRequests.confirm(body),
                        FileRequests.key(key)));
    }

    private void client(String value) {
        if (!actors.requireMini().actingRole().toLowerCase(Locale.ROOT).equals(value))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(value));
    }
}
