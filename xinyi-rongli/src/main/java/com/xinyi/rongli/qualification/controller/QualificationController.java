package com.xinyi.rongli.qualification.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.qualification.dto.QualificationRequests;
import com.xinyi.rongli.qualification.service.QualificationService;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 本人资格入口不要求DRIVER；主体与当前视角取真实MINI会话，客户端不能指定申请人或公司。 */
@RestController
@RequestMapping("/api/app/driver/qualification")
public class QualificationController {
    private final QualificationService service;
    private final BusinessActorResolver actors;

    public QualificationController(QualificationService service, BusinessActorResolver actors) {
        this.service = service;
        this.actors = actors;
    }

    @GetMapping
    public ResponseEntity<?> get(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        // 本人单例查询不接受任何筛选或归属声明，避免内部参数被忽略后误导为已生效。
        if (!query.isEmpty()) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        return response(service.get());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(service.create(QualificationRequests.create(body), FileRequests.key(key)));
    }

    @PutMapping
    public ResponseEntity<?> save(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(service.save(QualificationRequests.draft(body), FileRequests.key(key)));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(service.submit(QualificationRequests.submit(body), FileRequests.key(key)));
    }

    private void client(String value) {
        var actor = actors.requireMini();
        if (!actor.actingRole().toLowerCase(java.util.Locale.ROOT).equals(value))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object data) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(data));
    }
}
