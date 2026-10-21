package com.xinyi.rongli.platform.domain;

import com.xinyi.rongli.core.ViRecord;

/**
 * vi_event_log 不可变业务事实流水（append-only，不物理删除、不 UPDATE 业务字段）。
 * 双主体审计：actor_realm 区分 MINI/ADMIN/SYSTEM/CALLBACK；MINI 写 actor_account_id，
 * ADMIN 写 actor_user_id，两者绝不数值混比（ADR-02）。before/after_summary 为脱敏 JSON，
 * 禁止明文 token/credential/launchUrl。成功审计同事务，失败审计独立持久化。
 */
public class ViEventLog extends ViRecord {
    private Long orderId;
    private String aggregateType;
    private Long aggregateId;
    private String action;
    private Long actorAccountId;
    private Long actorUserId;
    private String actingRole;
    private Long stationId;
    private String beforeSummary;
    private String afterSummary;
    private String reason;
    private String actorRealm;
    private Long sessionId;
    private Long contextVersion;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long v) { this.orderId = v; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String v) { this.aggregateType = v; }
    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long v) { this.aggregateId = v; }
    public String getAction() { return action; }
    public void setAction(String v) { this.action = v; }
    public Long getActorAccountId() { return actorAccountId; }
    public void setActorAccountId(Long v) { this.actorAccountId = v; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long v) { this.actorUserId = v; }
    public String getActingRole() { return actingRole; }
    public void setActingRole(String v) { this.actingRole = v; }
    public Long getStationId() { return stationId; }
    public void setStationId(Long v) { this.stationId = v; }
    public String getBeforeSummary() { return beforeSummary; }
    public void setBeforeSummary(String v) { this.beforeSummary = v; }
    public String getAfterSummary() { return afterSummary; }
    public void setAfterSummary(String v) { this.afterSummary = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { this.reason = v; }
    public String getActorRealm() { return actorRealm; }
    public void setActorRealm(String v) { this.actorRealm = v; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public Long getContextVersion() { return contextVersion; }
    public void setContextVersion(Long v) { this.contextVersion = v; }
}
