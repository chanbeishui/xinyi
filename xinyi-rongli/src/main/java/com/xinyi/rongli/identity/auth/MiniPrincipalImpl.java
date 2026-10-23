package com.xinyi.rongli.identity.auth;

import com.xinyi.rongli.platform.actor.ActorRealm;
import com.xinyi.rongli.platform.actor.MiniPrincipal;

/**
 * MiniPrincipal 的不可变实现（E06 预留 seam 由 I01 落地）。由 MINI 令牌过滤器在验证
 * vi_session_token/vi_session 后构造并放入 SecurityContext；绝不可由 HTTP 反序列化构造，
 * 绝不伪装成框架 LoginUser，accountId=1 也不触发框架超管（BusinessActorResolver 按 realm 区分）。
 */
public record MiniPrincipalImpl(
        long accountId,
        Long linkedSysUserId,
        Long sessionId,
        String activeRole,
        Long stationId,
        Long contextVersion) implements MiniPrincipal {

    @Override
    public ActorRealm realm() {
        return ActorRealm.MINI;
    }

    @Override
    public long accountId() {
        return accountId;
    }

    @Override
    public Long linkedSysUserId() {
        return linkedSysUserId;
    }

    @Override
    public Long sessionId() {
        return sessionId;
    }

    @Override
    public String activeRole() {
        return activeRole;
    }

    @Override
    public Long stationId() {
        return stationId;
    }

    @Override
    public Long contextVersion() {
        return contextVersion;
    }
}
