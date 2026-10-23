package com.xinyi.rongli.identity.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_session 小程序独立设备会话。每设备一条；active_role 是当前视角而非授权来源，
 * 每次写操作仍实时校验资格。session_epoch 为签发时代际（来自 vi_account_security），
 * absolute_expires_at 为会话绝对截止（不无限滑动）。表无 updated_at 列。
 */
public class ViSession extends ViRecord {
    private Long accountId;
    private String appId;
    private String status;
    private Long sessionEpoch;
    private Long contextVersion;
    private String activeRole;
    private Long stationId;
    private Long linkedSysUserId;
    private Instant absoluteExpiresAt;
    private Instant revokedAt;
    private String deviceLabel;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public String getAppId() { return appId; }
    public void setAppId(String v) { this.appId = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getSessionEpoch() { return sessionEpoch; }
    public void setSessionEpoch(Long v) { this.sessionEpoch = v; }
    public Long getContextVersion() { return contextVersion; }
    public void setContextVersion(Long v) { this.contextVersion = v; }
    public String getActiveRole() { return activeRole; }
    public void setActiveRole(String v) { this.activeRole = v; }
    public Long getStationId() { return stationId; }
    public void setStationId(Long v) { this.stationId = v; }
    public Long getLinkedSysUserId() { return linkedSysUserId; }
    public void setLinkedSysUserId(Long v) { this.linkedSysUserId = v; }
    public Instant getAbsoluteExpiresAt() { return absoluteExpiresAt; }
    public void setAbsoluteExpiresAt(Instant v) { this.absoluteExpiresAt = v; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant v) { this.revokedAt = v; }
    public String getDeviceLabel() { return deviceLabel; }
    public void setDeviceLabel(String v) { this.deviceLabel = v; }
}
