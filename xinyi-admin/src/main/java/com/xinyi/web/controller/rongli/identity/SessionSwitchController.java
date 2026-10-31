package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.AccessSessionResponse;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.SessionSwitchService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色与站点上下文切换（I04，API_006/API_007，MINI 认证域，T3.4 专用会话协议）。
 * 主体取自可信 MiniPrincipal；SESSION_SWITCH_ROLE/SESSION_SWITCH_STATION 为全角色公共能力。
 * 严格拒绝未知字段（additionalProperties:false）；role 仅限 OWNER/DRIVER/STATION 枚举，目标资格由服务端实时校验，
 * 绝不据客户端 role 授予身份（TC-R63-016）。原子校验 expectedContextVersion，冲突 409 VI_CONTEXT_CHANGED。
 * X-Client-Type 仅审计来源、不作授权。不套业务幂等缓存；新访问令牌仅此处返回一次，no-store。
 */
@RestController
@RequestMapping("/api/common/session")
public class SessionSwitchController {

    private static final Set<String> CLIENT_TYPES = Set.of("owner", "driver", "station", "admin");
    private static final Set<String> ROLES = Set.of("OWNER", "DRIVER", "STATION");

    private final SessionSwitchService switchService;
    private final BusinessActorResolver actorResolver;

    public SessionSwitchController(SessionSwitchService switchService, BusinessActorResolver actorResolver) {
        this.switchService = switchService;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/role")
    public ResponseEntity<ViSuccess<AccessSessionResponse>> switchRole(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        requireClientType(clientType);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, Set.of("role", "expectedContextVersion"));
        String role = asString(body.get("role"));
        if (role == null || !ROLES.contains(role)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "role 须为 OWNER/DRIVER/STATION",
                    List.of(Map.of("field", "role", "reasonCode", "INVALID", "message", "角色取值非法")), null, null);
        }
        long expectedContextVersion = requireContextVersion(body);
        AccessSessionResponse result = switchService.switchRole(actor.accountId(), actor.sessionId(),
                role, expectedContextVersion);
        return noStore(ViSuccess.ok(result));
    }

    @PostMapping("/station")
    public ResponseEntity<ViSuccess<AccessSessionResponse>> switchStation(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        requireClientType(clientType);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, Set.of("stationId", "expectedContextVersion"));
        long stationId = parseStationId(asString(body.get("stationId")));
        long expectedContextVersion = requireContextVersion(body);
        AccessSessionResponse result = switchService.switchStation(actor.accountId(), actor.sessionId(),
                stationId, expectedContextVersion);
        return noStore(ViSuccess.ok(result));
    }

    private long parseStationId(String value) {
        try {
            return ViId.parse(value);
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "stationId 须为合法十进制 ID",
                    List.of(Map.of("field", "stationId", "reasonCode", "INVALID", "message", "站点 ID 格式非法或越界")),
                    null, null);
        }
    }

    private long requireContextVersion(Map<String, Object> body) {
        if (!(body.get("expectedContextVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedContextVersion 缺失或非法",
                    List.of(Map.of("field", "expectedContextVersion", "reasonCode", "INVALID",
                            "message", "上下文版本须为非负整数")), null, null);
        }
        return n.longValue();
    }

    private void requireOnly(Map<String, Object> body, Set<String> allowed) {
        if (body == null || body.isEmpty()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String key : body.keySet()) {
            if (!allowed.contains(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "切换请求含未知字段: " + key,
                        List.of(Map.of("field", key, "reasonCode", "UNKNOWN_FIELD", "message", "不接受该字段")),
                        null, null);
            }
        }
    }

    private void requireClientType(String clientType) {
        if (clientType == null || !CLIENT_TYPES.contains(clientType.trim().toLowerCase())) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "X-Client-Type 缺失或非法");
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}