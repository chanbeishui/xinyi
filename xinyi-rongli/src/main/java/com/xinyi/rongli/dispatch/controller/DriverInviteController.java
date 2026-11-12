package com.xinyi.rongli.dispatch.controller;

import com.xinyi.rongli.dispatch.dto.InviteRequests;
import com.xinyi.rongli.dispatch.service.DispatchEngineService;
import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 本人邀约仅收稳定目标与观察版本；坐标、候选会话或公司范围不由客户端提交。 */
@RestController
@RequestMapping("/api/app/driver")
public class DriverInviteController {
    private final DispatchEngineService service;

    public DriverInviteController(DispatchEngineService service) {
        this.service = service;
    }

    @GetMapping("/invites")
    public ResponseEntity<?> list(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        var page = service.driverInvites(InviteRequests.query(query, "MINI_INVITE"));
        return page(page);
    }

    @GetMapping("/invites/{inviteId}")
    public ResponseEntity<?> detail(
            @PathVariable String inviteId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        InviteRequests.none(query);
        return ok(service.driverInvite(InviteRequests.id(inviteId)));
    }

    @PostMapping("/invites/{inviteId}/response")
    public ResponseEntity<?> respond(
            @PathVariable String inviteId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        client(client);
        InviteRequests.none(query);
        return ok(
                service.respond(
                        InviteRequests.id(inviteId),
                        InviteRequests.response(body),
                        FileRequests.key(key)));
    }

    @GetMapping("/assignments/current")
    public ResponseEntity<?> current(
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        client(client);
        InviteRequests.none(query);
        return ok(service.currentAssignment());
    }

    private static void client(String value) {
        if (!"driver".equals(value)) throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> ok(Object result) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(result));
    }

    private static ResponseEntity<?> page(DispatchEngineService.Page page) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(
                        Map.of(
                                "code",
                                200,
                                "msg",
                                "操作成功",
                                "rows",
                                page.rows(),
                                "total",
                                page.total()));
    }
}
