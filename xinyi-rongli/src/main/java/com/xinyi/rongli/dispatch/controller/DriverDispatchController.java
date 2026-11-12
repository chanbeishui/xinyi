package com.xinyi.rongli.dispatch.controller;

import com.xinyi.rongli.dispatch.dto.DispatchRequests;
import com.xinyi.rongli.dispatch.service.DriverDispatchService;
import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 三个原业务接口及独立交互心跳；GET不写可达，客户端不提交司机/公司等授权主体。 */
@RestController
@RequestMapping("/api/app/driver")
public class DriverDispatchController {
    private final DriverDispatchService service;

    public DriverDispatchController(DriverDispatchService service) {
        this.service = service;
    }

    @GetMapping("/availability")
    public ResponseEntity<?> availability(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        check(client, query);
        return response(service.availability());
    }

    @PatchMapping("/listening")
    public ResponseEntity<?> listening(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        check(client, query);
        return response(service.listening(DispatchRequests.listening(body), FileRequests.key(key)));
    }

    @PostMapping("/location")
    public ResponseEntity<?> location(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        check(client, query);
        return response(service.location(DispatchRequests.location(body), FileRequests.key(key)));
    }

    @PostMapping("/presence")
    public ResponseEntity<?> presence(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        check(client, query);
        return response(service.presence(DispatchRequests.presence(body), FileRequests.key(key)));
    }

    private static void check(String client, Map<String, String> query) {
        if (!"driver".equals(client) || !query.isEmpty())
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object data) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(data));
    }
}
