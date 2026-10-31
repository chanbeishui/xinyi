package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.identity.dto.LogoutResult;
import com.xinyi.rongli.identity.dto.SessionResponse;
import com.xinyi.rongli.identity.dto.SessionView;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.MiniSessionService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 刷新、当前会话与退出（I03，ADR-01）。
 * token/refresh 为 PUBLIC（realm REFRESH）：只接受有效 REFRESH 摘要，不接受后台 JWT/ACCESS；严格拒绝未知字段；
 * 单次轮换、重用撤会话族、绝对到期不滑动；明文令牌仅此处返回一次，no-store、不进幂等缓存。
 * session/logout 为 MINI：主体取自可信 MiniPrincipal，SESSION_READ/SESSION_LOGOUT 为全角色公共能力；
 * 退出只清会话，不取消订单、不解除持车任务（TC-R63-008）。logout 需 Idempotency-Key（业务动作幂等，不缓存凭据）。
 */
@RestController
@RequestMapping("/api/common")
public class SessionController {

    private final MiniSessionService sessionService;
    private final BusinessActorResolver actorResolver;

    public SessionController(MiniSessionService sessionService, BusinessActorResolver actorResolver) {
        this.sessionService = sessionService;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ViSuccess<SessionResponse>> refresh(@RequestBody Map<String, Object> body) {
        String refreshToken = requireOnlyRefreshToken(body);
        SessionResponse session = sessionService.refresh(refreshToken);
        return noStore(ViSuccess.ok(session));
    }

    @GetMapping("/session")
    public ResponseEntity<ViSuccess<SessionView>> session() {
        Actor actor = actorResolver.requireMini();
        SessionView view = sessionService.currentSession(actor.accountId(), actor.sessionId());
        return noStore(ViSuccess.ok(view));
    }

    @PostMapping("/session/logout")
    public ResponseEntity<ViSuccess<LogoutResult>> logout(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().length() < 16) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "Idempotency-Key 缺失或过短");
        }
        Actor actor = actorResolver.requireMini();
        boolean allDevices = parseAllDevices(body);
        LogoutResult result = sessionService.logout(actor.accountId(), actor.sessionId(), allDevices);
        return noStore(ViSuccess.ok(result));
    }

    private String requireOnlyRefreshToken(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String key : body.keySet()) {
            if (!"refreshToken".equals(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "刷新请求只接受 refreshToken 字段",
                        List.of(Map.of("field", key, "reasonCode", "UNKNOWN_FIELD",
                                "message", "不接受 ACCESS/后台 JWT 或其他身份字段")), null, null);
            }
        }
        Object token = body.get("refreshToken");
        if (!(token instanceof String s) || s.length() < 32) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "refreshToken 缺失或非法",
                    List.of(Map.of("field", "refreshToken", "reasonCode", "INVALID",
                            "message", "refreshToken 必填且长度不少于 32")), null, null);
        }
        return s;
    }

    private boolean parseAllDevices(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        for (String key : body.keySet()) {
            if (!"allDevices".equals(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "退出请求只接受 allDevices 字段",
                        List.of(Map.of("field", key, "reasonCode", "UNKNOWN_FIELD",
                                "message", "不接受其他字段")), null, null);
            }
        }
        Object v = body.get("allDevices");
        if (v == null) {
            return false;
        }
        if (!(v instanceof Boolean b)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "allDevices 须为布尔值");
        }
        return b;
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}