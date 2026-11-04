package com.xinyi.rongli.payment.controller;

import com.xinyi.rongli.payment.service.PaymentLifecycleService;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;

@RestController
@RequestMapping("/api/app/owner/orders")
public class PaymentLifecycleController {
    private final PaymentLifecycleService service;

    public PaymentLifecycleController(PaymentLifecycleService service) {
        this.service = service;
    }

    @GetMapping("/{no}/payment")
    public ResponseEntity<?> read(
            @PathVariable String no,
            @RequestHeader(value = "X-Client-Type", required = false) String client) {
        check(no, client);
        return result(service.read(no));
    }

    @PostMapping("/{no}/close-unpaid")
    public ResponseEntity<?> close(
            @PathVariable String no,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        check(no, client);
        long version;
        String reason;
        try {
            if (!body.keySet().equals(Set.of("expectedVersion", "reason"))
                    || !(body.get("expectedVersion") instanceof Number)
                    || !(body.get("reason") instanceof String))
                throw new IllegalArgumentException();
            version = Long.parseLong(body.get("expectedVersion").toString());
            reason = ((String) body.get("reason")).trim();
            if (version < 0
                    || reason.isEmpty()
                    || reason.length() > 500
                    || key == null
                    || !UUID.fromString(key).toString().equalsIgnoreCase(key))
                throw new IllegalArgumentException();
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
        return result(service.close(no, version, reason, key));
    }

    private static void check(String no, String client) {
        if (!"owner".equals(client) || !no.matches("[A-Za-z0-9_-]{1,64}"))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> result(Object data) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(data));
    }
}
