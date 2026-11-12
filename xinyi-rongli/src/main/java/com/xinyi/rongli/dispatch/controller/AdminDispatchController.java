package com.xinyi.rongli.dispatch.controller;

import com.xinyi.rongli.dispatch.dto.InviteRequests;
import com.xinyi.rongli.dispatch.service.*;
import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** ADMIN安全链验证真实登录；功能与数据权限由统一Service在目录守卫事务内显式检查。 使用业务403及拒绝审计，避免入口方法鉴权异常被vi域通用异常映射成500。 */
@RestController
@RequestMapping("/api/admin/dispatch")
public class AdminDispatchController {
    private final DispatchEngineService service;
    private final DispatchPolicyService policy;

    public AdminDispatchController(DispatchEngineService service, DispatchPolicyService policy) {
        this.service = service;
        this.policy = policy;
    }

    @GetMapping("/queue")
    public ResponseEntity<?> queue(@RequestParam Map<String, String> q) {
        return page(service.queue(InviteRequests.query(q, "QUEUE")));
    }

    @GetMapping("/candidates")
    public ResponseEntity<?> candidates(@RequestParam Map<String, String> q) {
        return page(service.candidateList(InviteRequests.query(q, "CANDIDATE")));
    }

    @GetMapping("/invites")
    public ResponseEntity<?> invites(@RequestParam Map<String, String> q) {
        return page(service.adminInvites(InviteRequests.query(q, "ADMIN_INVITE")));
    }

    @PostMapping("/manual-invites")
    public ResponseEntity<?> manual(
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> q,
            @RequestBody Map<String, Object> b) {
        InviteRequests.none(q);
        return ok(service.manual(InviteRequests.manual(b), FileRequests.key(key)));
    }

    @PostMapping("/invites/{inviteId}/revoke")
    public ResponseEntity<?> revoke(
            @PathVariable String inviteId,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> q,
            @RequestBody Map<String, Object> b) {
        InviteRequests.none(q);
        return ok(
                service.revoke(
                        InviteRequests.id(inviteId),
                        InviteRequests.revoke(b),
                        FileRequests.key(key)));
    }

    @GetMapping("/companies/{companyId}/dispatch-policy")
    public ResponseEntity<?> getPolicy(
            @PathVariable String companyId, @RequestParam Map<String, String> q) {
        InviteRequests.none(q);
        return ok(policy.get(InviteRequests.id(companyId)));
    }

    @PutMapping("/companies/{companyId}/dispatch-policy")
    public ResponseEntity<?> putPolicy(
            @PathVariable String companyId,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> q,
            @RequestBody Map<String, Object> b) {
        InviteRequests.none(q);
        return ok(
                policy.put(
                        InviteRequests.id(companyId),
                        InviteRequests.policy(b),
                        FileRequests.key(key)));
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
