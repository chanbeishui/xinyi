package com.xinyi.rongli.platform.actor;

/**
 * 业务操作人（不可由 HTTP 反序列化构造；只能由服务端从可信会话/上下文构造）。
 * 双主体审计：MINI 用 accountId（vi_account），ADMIN 用 sysUserId（sys_user），两者绝不数值混比。
 * 共用 Service 通过 BusinessActorResolver 取得 Actor，不直接强转 SecurityUtils.getLoginUser（ADR-01）。
 */
public record Actor(
        ActorRealm realm,
        Long accountId,
        Long sysUserId,
        String actingRole,
        Long stationId,
        Long sessionId,
        Long contextVersion) {

    public Actor {
        if (realm == null) throw new IllegalArgumentException("ACTOR_REALM_REQUIRED");
        if (realm == ActorRealm.MINI && accountId == null)
            throw new IllegalArgumentException("MINI_ACTOR_REQUIRES_ACCOUNT_ID");
        if (realm == ActorRealm.ADMIN && sysUserId == null)
            throw new IllegalArgumentException("ADMIN_ACTOR_REQUIRES_SYS_USER_ID");
    }

    public static Actor mini(long accountId, String activeRole, Long stationId,
                             Long sessionId, Long contextVersion, Long linkedSysUserId) {
        return new Actor(ActorRealm.MINI, accountId, linkedSysUserId, activeRole, stationId, sessionId, contextVersion);
    }

    public static Actor admin(long sysUserId, String actingRole) {
        return new Actor(ActorRealm.ADMIN, null, sysUserId, actingRole, null, null, null);
    }

    public static Actor system() {
        return new Actor(ActorRealm.SYSTEM, null, null, "SYSTEM", null, null, null);
    }

    public static Actor callback(String source) {
        return new Actor(ActorRealm.CALLBACK, null, null, source, null, null, null);
    }

    public boolean isMini() { return realm == ActorRealm.MINI; }
    public boolean isAdmin() { return realm == ActorRealm.ADMIN; }

    /**
     * 仅同域身份比较：MINI 比 accountId，ADMIN 比 sysUserId。
     * 跨域“是否同一人”必须经 vi_employee_binding 可信绑定判定（I01/I03），
     * 本方法拒绝跨域数值比较，避免 accountId==sysUserId 的伪等价（ADR-02）。
     */
    public boolean samePrincipal(Actor other) {
        if (other == null || this.realm != other.realm) return false;
        return switch (realm) {
            case MINI -> accountId != null && accountId.equals(other.accountId);
            case ADMIN -> sysUserId != null && sysUserId.equals(other.sysUserId);
            case SYSTEM, CALLBACK -> false;
        };
    }
}
