package com.xinyi.rongli.driver.controller;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.driver.dto.DriverRequests;
import com.xinyi.rongli.driver.service.DriverAdminService;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.refund.dto.RefundRequests;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** 后台协议入口；公司归属、独立功能权限、实时账号及实际在手责任在Service统一执行。 */
@RestController
@RequestMapping("/api/admin/drivers")
public class DriverAdminController {
    private final DriverAdminService service;

    public DriverAdminController(DriverAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.list(DriverRequests.query(query, false)));
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<?> detail(
            @PathVariable String driverId,
            @RequestHeader(value = "X-Client-Type", required = false) String client) {
        client(client);
        return response(ViSuccess.ok(service.get(id(driverId))));
    }

    @PatchMapping("/{driverId}")
    public ResponseEntity<?> maintain(
            @PathVariable String driverId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.maintain(
                                id(driverId),
                                DriverRequests.maintenance(body),
                                RefundRequests.key(key))));
    }

    @GetMapping("/{driverId}/assignments")
    public ResponseEntity<?> assignments(
            @PathVariable String driverId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        return response(service.assignments(id(driverId), DriverRequests.query(query, true)));
    }

    @PostMapping("/{driverId}/disable-new-assignments")
    public ResponseEntity<?> disable(
            @PathVariable String driverId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.switchNew(
                                id(driverId),
                                RefundRequests.action(body),
                                RefundRequests.key(key),
                                false)));
    }

    @PostMapping("/{driverId}/enable-new-assignments")
    public ResponseEntity<?> enable(
            @PathVariable String driverId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(
                ViSuccess.ok(
                        service.switchNew(
                                id(driverId),
                                RefundRequests.action(body),
                                RefundRequests.key(key),
                                true)));
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

    private static ResponseEntity<?> response(Object body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
