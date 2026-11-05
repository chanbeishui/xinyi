package com.xinyi.rongli.aftersale.controller;

import com.xinyi.rongli.aftersale.dto.AftersaleRequests;
import com.xinyi.rongli.aftersale.service.AftersaleService;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** 车主售后入口。JSON白名单只解析公开输入，真实归属、金额和会话校验由统一服务执行。 */
@RestController
@RequestMapping("/api/app/owner")
public class AftersaleController {
    private final AftersaleService service;

    public AftersaleController(AftersaleService service) {
        this.service = service;
    }

    @PostMapping("/orders/{orderNo}/after-sales")
    public ResponseEntity<?> apply(
            @PathVariable String orderNo,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.apply(
                                orderNo(orderNo),
                                AftersaleRequests.apply(body),
                                AftersaleRequests.key(key))));
    }

    @GetMapping("/orders/{orderNo}/after-sales")
    public ResponseEntity<?> orderList(
            @PathVariable String orderNo,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.list(orderNo(orderNo), AftersaleRequests.query(query)));
    }

    @GetMapping("/after-sales")
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.list(null, AftersaleRequests.query(query)));
    }

    @GetMapping("/after-sales/{caseId}")
    public ResponseEntity<?> detail(
            @PathVariable String caseId,
            @RequestHeader(value = "X-Client-Type", required = false) String client) {
        client(client);
        return response(ViSuccess.ok(service.get(id(caseId))));
    }

    @PostMapping("/after-sales/{caseId}/notes")
    public ResponseEntity<?> note(
            @PathVariable String caseId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.note(
                                id(caseId),
                                AftersaleRequests.note(body),
                                AftersaleRequests.key(key))));
    }

    private static String orderNo(String value) {
        if (!value.matches("[A-Za-z0-9_-]{1,64}"))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        return value;
    }

    private static long id(String value) {
        try {
            return ViId.parse(value);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static void client(String value) {
        if (!"owner".equals(value)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value);
    }
}
