package com.xinyi.rongli.training.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.training.dto.LearningRequests;
import com.xinyi.rongli.training.service.LearningService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 本人学习不要求已具备DRIVER角色；显式申请与运行目标由Service对真实MINI会话校验。 */
@RestController
@RequestMapping("/api/app/driver/qualification/training")
public class LearningController {
    private final LearningService service;
    private final BusinessActorResolver actors;

    public LearningController(LearningService service, BusinessActorResolver actors) {
        this.service = service;
        this.actors = actors;
    }

    @GetMapping
    public ResponseEntity<?> get(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.get());
    }

    @GetMapping("/{trainingId}")
    public ResponseEntity<?> unit(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.unit(LearningRequests.id(trainingId)));
    }

    @PostMapping("/{trainingId}/playback")
    public ResponseEntity<?> playback(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.playback(
                        LearningRequests.id(trainingId),
                        LearningRequests.start(body),
                        FileRequests.key(key)));
    }

    @PostMapping("/{trainingId}/progress")
    public ResponseEntity<?> progress(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.progress(
                        LearningRequests.id(trainingId),
                        LearningRequests.progress(body),
                        FileRequests.key(key)));
    }

    @PostMapping("/{trainingId}/attempts")
    public ResponseEntity<?> createAttempt(
            @PathVariable String trainingId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(
                service.createAttempt(
                        LearningRequests.id(trainingId),
                        LearningRequests.attempt(body),
                        FileRequests.key(key)));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<?> attempt(
            @PathVariable String attemptId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.attempt(LearningRequests.id(attemptId)));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body,
            @RequestParam Map<String, String> query) {
        client(client);
        LearningRequests.noQuery(query);
        return response(service.submit(LearningRequests.submit(body), FileRequests.key(key)));
    }

    private void client(String client) {
        if (!actors.requireMini().actingRole().toLowerCase(Locale.ROOT).equals(client))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(value));
    }
}
