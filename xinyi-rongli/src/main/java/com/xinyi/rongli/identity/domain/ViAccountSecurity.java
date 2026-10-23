package com.xinyi.rongli.identity.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_account_security 小程序账号安全版本（新域权限线性化门禁）。
 * session_epoch 控制全局退出/封禁代际；permission_version 控制角色/能力变化，
 * 与资料 version 分开。sys_user 停用不自动注销车主（ADR-01/02）。
 * 表无 updated_at 列（不可变行不更新内容），changed_at 须显式写入。
 */
public class ViAccountSecurity extends ViRecord {
    private Long accountId;
    private Long sessionEpoch;
    private Long permissionVersion;
    private String securityStatus;
    private Instant changedAt;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public Long getSessionEpoch() { return sessionEpoch; }
    public void setSessionEpoch(Long v) { this.sessionEpoch = v; }
    public Long getPermissionVersion() { return permissionVersion; }
    public void setPermissionVersion(Long v) { this.permissionVersion = v; }
    public String getSecurityStatus() { return securityStatus; }
    public void setSecurityStatus(String v) { this.securityStatus = v; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant v) { this.changedAt = v; }
}
