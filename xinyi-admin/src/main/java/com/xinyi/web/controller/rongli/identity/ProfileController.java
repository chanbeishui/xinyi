package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.identity.dto.ProfileResponse;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.ProfileService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 当前统一档案（API_005_get，MINI 认证）。主体来自可信 MiniPrincipal（经 BusinessActorResolver），
 * 绝不接收客户端伪造的 accountId/role。X-Client-Type 仅作审计来源、不作授权，须为合法视角字面量。
 * availableRoles/资格/员工关联由服务端按权威状态实时计算。响应 no-store。
 */
@RestController
@RequestMapping("/api/common")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
    private static final Set<String> CLIENT_TYPES = Set.of("owner", "driver", "station", "admin");

    private final ProfileService profileService;
    private final BusinessActorResolver actorResolver;

    public ProfileController(ProfileService profileService, BusinessActorResolver actorResolver) {
        this.profileService = profileService;
        this.actorResolver = actorResolver;
    }

    @GetMapping("/profile")
    public ResponseEntity<ViSuccess<ProfileResponse>> profile(
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        if (clientType == null || !CLIENT_TYPES.contains(clientType.trim().toLowerCase())) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "X-Client-Type 缺失或非法");
        }
        Actor actor = actorResolver.requireMini();
        // X-Client-Type 仅审计，不参与授权；真实视角以服务端 activeRole 为准
        log.debug("profile read by mini account, clientType={}", clientType);
        ProfileResponse profile = profileService.getProfile(actor.accountId());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ViSuccess.ok(profile));
    }
}
