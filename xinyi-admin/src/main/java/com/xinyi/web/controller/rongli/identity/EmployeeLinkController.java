package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.identity.dto.EmployeeLinkPublic;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.EmployeeLinkService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 本人员工关联状态与 reconcile（I05，U02，MINI 认证域）。
 * 只返回粗粒度公开投影；reconcile 仅重试本账号最近已确认 WECHAT 回执，不接收任意手机号/目标员工。
 * 身份取自可信 MiniPrincipal；EMPLOYEE_LINK_READ/EMPLOYEE_LINK_RECONCILE 为公共能力。响应 no-store。
 */
@RestController
@RequestMapping("/api/common/employee-link")
public class EmployeeLinkController {

    private final EmployeeLinkService linkService;
    private final BusinessActorResolver actorResolver;

    public EmployeeLinkController(EmployeeLinkService linkService, BusinessActorResolver actorResolver) {
        this.linkService = linkService;
        this.actorResolver = actorResolver;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<EmployeeLinkPublic>> read() {
        Actor actor = actorResolver.requireMini();
        return noStore(ViSuccess.ok(linkService.readPublic(actor.accountId())));
    }

    @PostMapping("/reconcile")
    public ResponseEntity<ViSuccess<EmployeeLinkPublic>> reconcile(@RequestBody Map<String, Object> body) {
        Actor actor = actorResolver.requireMini();
        if (body == null || !body.keySet().equals(Set.of("expectedContextVersion"))) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "reconcile 只接受 expectedContextVersion");
        }
        if (!(body.get("expectedContextVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedContextVersion 缺失或非法");
        }
        return noStore(ViSuccess.ok(linkService.reconcile(actor.accountId(), actor.sessionId(), n.longValue())));
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}