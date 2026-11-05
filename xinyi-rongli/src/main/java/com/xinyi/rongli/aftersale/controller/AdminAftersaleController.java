package com.xinyi.rongli.aftersale.controller;

import com.xinyi.rongli.aftersale.dto.AftersaleRequests;
import com.xinyi.rongli.aftersale.dto.AdminAftersaleRequests;
import com.xinyi.rongli.aftersale.service.AdminAftersaleService;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** 后台售后协议适配；服务层统一执行原订单部门、历史收款主体、实时账号和独立审核校验。 */
@RestController
@RequestMapping("/api/admin")
public class AdminAftersaleController {
    private final AdminAftersaleService service;

    public AdminAftersaleController(AdminAftersaleService service) {
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
        return response(service.list(orderNo(orderNo), null, AftersaleRequests.query(query)));
    }

    @GetMapping("/owners/{ownerId}/after-sales")
    public ResponseEntity<?> ownerList(
            @PathVariable String ownerId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.list(null, id(ownerId), AftersaleRequests.query(query)));
    }

    @GetMapping("/after-sales")
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.list(null, null, AftersaleRequests.query(query)));
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

    @PostMapping("/after-sales/{caseId}/review-preview")
    public ResponseEntity<?> preview(
            @PathVariable String caseId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.preview(
                                id(caseId),
                                AdminAftersaleRequests.preview(body),
                                AftersaleRequests.key(key))));
    }

    @PostMapping("/after-sales/{caseId}/review")
    public ResponseEntity<?> review(
            @PathVariable String caseId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.review(
                                id(caseId),
                                AdminAftersaleRequests.review(body),
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
        if (!"admin".equals(value)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value);
    }
}
