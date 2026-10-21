package com.xinyi.rongli.platform.actor;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 从可信 SecurityContext 解析业务 Actor（ADR-01/02）。这是唯一读取认证主体的边界，
 * 共用 Service 只依赖本类，不直接强转 SecurityUtils.getLoginUser，避免把 MiniPrincipal
 * 伪装成框架 LoginUser，也避免以 accountId=1 触发框架超管。
 *
 * 解析顺序：MINI（MiniPrincipal）优先于 ADMIN（框架 LoginUser）；都无则 SYSTEM。
 * MINI 会话/令牌的具体实现由 I01/I03 提供，本类只按只读契约取值。
 */
@Component
public class BusinessActorResolver {

    /** 当前操作人；无认证上下文时返回 SYSTEM（用于定时任务/outbox 中继/回调）。 */
    public Actor currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Actor.system();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof MiniPrincipal mp) {
            return Actor.mini(mp.accountId(), mp.activeRole(), mp.stationId(),
                    mp.sessionId(), mp.contextVersion(), mp.linkedSysUserId());
        }
        if (principal instanceof LoginUser lu && lu.getUserId() != null) {
            return Actor.admin(lu.getUserId(), "ADMIN");
        }
        return Actor.system();
    }

    /** 要求 MINI 域；否则 401（令牌缺失/不适用本入口）。 */
    public Actor requireMini() {
        Actor actor = currentActor();
        if (!actor.isMini()) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED, "需要小程序登录态");
        }
        return actor;
    }

    /** 要求 ADMIN 域；非后台登录 403（已确认无权），无认证 401。 */
    public Actor requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        Actor actor = currentActor();
        if (!actor.isAdmin()) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "需要后台管理登录态");
        }
        return actor;
    }
}
