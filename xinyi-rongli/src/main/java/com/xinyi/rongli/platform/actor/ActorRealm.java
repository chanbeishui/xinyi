package com.xinyi.rongli.platform.actor;

/**
 * 认证域/操作来源，对应 vi_event_log.actor_realm 的 CHECK 约束（MINI/ADMIN/SYSTEM/CALLBACK）。
 * 不可用数值 ID 混淆两个域（ADR-02）。
 */
public enum ActorRealm {
    MINI, ADMIN, SYSTEM, CALLBACK;

    public static ActorRealm of(String value) {
        if (value == null) return null;
        return valueOf(value.trim().toUpperCase());
    }
}
