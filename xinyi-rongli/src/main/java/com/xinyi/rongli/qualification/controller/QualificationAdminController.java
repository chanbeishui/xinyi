package com.xinyi.rongli.qualification.controller;

import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.qualification.dto.QualificationAdminRequests;
import com.xinyi.rongli.qualification.service.QualificationAdminService;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 后台审批只适配严格请求；真实公司、当前管理员及全部版本由统一Service重新核验。 */
@RestController
@RequestMapping("/api/admin/driver-qualifications")
public class QualificationAdminController {
    private final QualificationAdminService service;

    public QualificationAdminController(QualificationAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        admin(client);
        return response(service.list(QualificationAdminRequests.query(query)));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<?> get(
            @PathVariable String applicationId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        admin(client);
        noQuery(query);
        return response(ViSuccess.ok(service.get(QualificationAdminRequests.id(applicationId))));
    }

    @PostMapping("/{applicationId}/audit")
    public ResponseEntity<?> audit(
            @PathVariable String applicationId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        admin(client);
        noQuery(query);
        return response(
                ViSuccess.ok(
                        service.audit(
                                QualificationAdminRequests.id(applicationId),
                                QualificationAdminRequests.audit(body),
                                FileRequests.key(key))));
    }

    private static void admin(String client) {
        if (!"admin".equals(client)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void noQuery(Map<String, String> query) {
        if (!query.isEmpty()) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object value) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value);
    }
}
