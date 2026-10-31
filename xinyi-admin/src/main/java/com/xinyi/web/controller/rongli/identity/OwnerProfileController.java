package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.identity.dto.ProfileResponse;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.ProfilePatchService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 修改选填档案（API_012_patch，OWNER 本人）。CAS expectedVersion；仅 activeRole=OWNER 的本人账号；
 * 只接受 nickname/realName/avatarFileId/defaultAddressId/expectedVersion，至少 2 个字段（minProperties:2）。
 * 小程序经 X-HTTP-Method-Override:PATCH（E06 过滤器）以 POST 表达，PC 直接 PATCH。响应 no-store。
 */
@RestController
@RequestMapping("/api/app/owner")
public class OwnerProfileController {

    private static final Set<String> FIELDS =
            Set.of("nickname", "realName", "avatarFileId", "defaultAddressId", "expectedVersion");
    private static final Set<String> CLIENT_TYPES = Set.of("owner", "driver", "station", "admin");

    private final ProfilePatchService profilePatchService;
    private final BusinessActorResolver actorResolver;

    public OwnerProfileController(ProfilePatchService profilePatchService, BusinessActorResolver actorResolver) {
        this.profilePatchService = profilePatchService;
        this.actorResolver = actorResolver;
    }

    @PatchMapping("/profile")
    public ResponseEntity<ViSuccess<ProfileResponse>> patch(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (clientType == null || !CLIENT_TYPES.contains(clientType.trim().toLowerCase())) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "X-Client-Type 缺失或非法");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
        Actor actor = actorResolver.requireMini();
        if (!"OWNER".equals(actor.actingRole())) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "仅车主视角可修改本人档案");
        }
        validate(body);
        ProfileResponse profile = profilePatchService.patch(actor.accountId(), body);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(profile));
    }

    private void validate(Map<String, Object> body) {
        if (body == null || body.size() < 2) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "至少提供 expectedVersion 与一个修改字段");
        }
        for (String key : body.keySet()) {
            if (!FIELDS.contains(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "档案修改不接受该字段: " + key);
            }
        }
        if (!(body.get("expectedVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 缺失或非法");
        }
    }
}
