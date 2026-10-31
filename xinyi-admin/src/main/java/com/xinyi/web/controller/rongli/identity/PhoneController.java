package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.identity.dto.Ack;
import com.xinyi.rongli.identity.dto.PhoneBindResult;
import com.xinyi.rongli.identity.dto.ProfileResponse;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.PhoneBindService;
import com.xinyi.rongli.identity.service.SmsService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 手机号验证回执与短信兜底（I02，MINI 认证域）。
 * phone/bind 严格只接受 {phoneCode,expectedAccountVersion,verificationRequestId}，混入裸 phone/sysUserId/openId
 * 等一律 400（TC-R62-008），绝不用于自动绑定；身份取自可信 MiniPrincipal。短信兜底未配置即拒绝、不触发员工关联。
 * 身份响应 no-store；verificationRequestId 专用回执去重，不进通用幂等缓存、不缓存令牌。
 */
@RestController
@RequestMapping("/api/common/phone")
public class PhoneController {

    private static final Set<String> BIND_FIELDS = Set.of("phoneCode", "expectedAccountVersion", "verificationRequestId");
    private static final Set<String> CLIENT_TYPES = Set.of("owner", "driver", "station", "admin");

    private final PhoneBindService phoneBindService;
    private final SmsService smsService;
    private final BusinessActorResolver actorResolver;

    public PhoneController(PhoneBindService phoneBindService, SmsService smsService,
                           BusinessActorResolver actorResolver) {
        this.phoneBindService = phoneBindService;
        this.smsService = smsService;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/bind")
    public ResponseEntity<ViSuccess<PhoneBindResult>> bind(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        requireClientType(clientType);
        Actor actor = actorResolver.requireMini();
        validateBindBody(body);
        String phoneCode = (String) body.get("phoneCode");
        long expectedVersion = ((Number) body.get("expectedAccountVersion")).longValue();
        String requestId = (String) body.get("verificationRequestId");
        PhoneBindResult result = phoneBindService.bind(actor.accountId(), actor.sessionId(),
                phoneCode, expectedVersion, requestId);
        return noStore(ViSuccess.ok(result));
    }

    @GetMapping("/verifications/{verificationRequestId}")
    public ResponseEntity<ViSuccess<PhoneBindResult>> verification(
            @PathVariable("verificationRequestId") String verificationRequestId,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        requireClientType(clientType);
        Actor actor = actorResolver.requireMini();
        requireUuid(verificationRequestId);
        return noStore(ViSuccess.ok(phoneBindService.queryReceipt(actor.accountId(), verificationRequestId)));
    }

    @PostMapping("/sms/send")
    public ResponseEntity<ViSuccess<Ack>> smsSend(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        requireClientType(clientType);
        requireIdempotencyKey(idempotencyKey);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, Set.of("phone", "scene"));
        smsService.send(actor.accountId(), asString(body.get("phone")), asString(body.get("scene")));
        return noStore(ViSuccess.ok(Ack.ok()));
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<ViSuccess<ProfileResponse>> smsVerify(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        requireClientType(clientType);
        requireIdempotencyKey(idempotencyKey);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, Set.of("challengeId", "code"));
        smsService.verify(actor.accountId(), asString(body.get("challengeId")), asString(body.get("code")));
        // 短信核验不建立员工关联；未配置时上面已抛 VI_CAPABILITY_NOT_CONFIGURED，不会到达此处
        return noStore(ViSuccess.ok((ProfileResponse) null));
    }

    private void validateBindBody(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String key : body.keySet()) {
            if (!BIND_FIELDS.contains(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "手机号绑定不接受该字段",
                        java.util.List.of(Map.of("field", key, "reasonCode", "UNKNOWN_FIELD",
                                "message", "不接受裸手机号/sysUserId/openId/role 等身份字段")), null, null);
            }
        }
        String phoneCode = asString(body.get("phoneCode"));
        if (phoneCode == null || phoneCode.isBlank() || phoneCode.length() > 512) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "phoneCode 缺失或非法");
        }
        if (!(body.get("expectedAccountVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedAccountVersion 缺失或非法");
        }
        requireUuid(asString(body.get("verificationRequestId")));
    }

    private void requireOnly(Map<String, Object> body, Set<String> allowed) {
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String key : body.keySet()) {
            if (!allowed.contains(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求含未知字段: " + key);
            }
        }
    }

    private void requireUuid(String v) {
        if (v == null || v.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "verificationRequestId 缺失");
        }
        try {
            UUID.fromString(v);
        } catch (IllegalArgumentException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "verificationRequestId 须为 UUID");
        }
    }

    private void requireClientType(String clientType) {
        if (clientType == null || !CLIENT_TYPES.contains(clientType.trim().toLowerCase())) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "X-Client-Type 缺失或非法");
        }
    }

    private void requireIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
