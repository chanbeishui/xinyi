package com.xinyi.rongli.file.controller;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.file.service.PrivateFileService;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** 共用文件协议入口；X-Client-Type 只选择认证域，主体仍由对应认证链校验。 */
@RestController
public class PrivateFileController {
    private final PrivateFileService service;
    private final BusinessActorResolver actors;

    public PrivateFileController(PrivateFileService service, BusinessActorResolver actors) {
        this.service = service;
        this.actors = actors;
    }

    @PostMapping("/api/common/uploads")
    public ResponseEntity<?> create(
            @RequestHeader("X-Client-Type") String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(service.create(FileRequests.upload(body), FileRequests.key(key)));
    }

    @PostMapping("/api/common/uploads/{id}/complete")
    public ResponseEntity<?> complete(
            @RequestHeader("X-Client-Type") String client,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body) {
        client(client);
        if (body != null && !body.isEmpty()) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        return response(service.complete(id(id)));
    }

    @GetMapping("/api/common/files/{id}")
    public ResponseEntity<?> get(
            @RequestHeader("X-Client-Type") String client, @PathVariable String id) {
        client(client);
        return response(service.get(id(id)));
    }

    @PostMapping("/api/common/files/{id}/access")
    public ResponseEntity<?> access(
            @RequestHeader("X-Client-Type") String client,
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        client(client);
        return response(service.access(id(id), FileRequests.access(body)));
    }

    @PutMapping("/vi-private-transfer/uploads/{id}")
    public ResponseEntity<?> upload(
            @RequestHeader("X-Client-Type") String client,
            @RequestHeader(value = "X-File-Ticket", required = false) String ticket,
            @PathVariable String id,
            HttpServletRequest request)
            throws IOException {
        client(client);
        service.upload(id(id), ticket, request.getInputStream());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @GetMapping("/vi-private-transfer/files/{id}")
    public ResponseEntity<?> download(
            @RequestHeader("X-Client-Type") String client,
            @RequestHeader(value = "X-File-Ticket", required = false) String ticket,
            @PathVariable String id,
            jakarta.servlet.http.HttpServletResponse response) {
        client(client);
        if (service.streamMedia(id(id), ticket, response)) return null;
        var download = service.download(id(id), ticket);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evidence\"")
                .contentType(MediaType.parseMediaType(download.mime()))
                .body(download.bytes());
    }

    private void client(String client) {
        var actor = actors.currentActor();
        if (actor.isAdmin()
                ? !"admin".equals(client)
                : !actor.isMini() || !actor.actingRole().equalsIgnoreCase(client))
            throw new ViException(ViErrorCode.VI_TOKEN_REALM_MISMATCH);
    }

    private static long id(String value) {
        try {
            return ViId.parse(value);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static ResponseEntity<?> response(Object data) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(data));
    }
}
