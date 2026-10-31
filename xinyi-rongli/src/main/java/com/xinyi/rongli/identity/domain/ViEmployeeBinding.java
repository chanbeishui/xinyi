package com.xinyi.rongli.identity.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_employee_binding 微信账户与框架员工受控绑定。自动关联 source=WECHAT_PHONE_MATCH 且 bound_by=NULL
 * （ck_employee_link_source_actor）。active_account/active_user 为生成列（不入实体、不写）。
 * uk_active_account/uk_active_user(app_id,active_user) 保证同账号/同应用员工至多一条未撤销关联；
 * SUSPENDED 保留唯一占用。employee_auth_version 为建立时框架授权版本快照，每次工作操作再查实时状态。
 */
public class ViEmployeeBinding extends ViRecord {
    private Long accountId;
    private Long sysUserId;
    private Long boundBy;
    private Instant boundAt;
    private Instant revokedAt;
    private Long permissionVersion;
    private String appId;
    private String source;
    private String status;
    private byte[] phoneLookup;
    private Instant lastPhoneVerifiedAt;
    private Long employeeAuthVersion;
    private Integer autoRebindBlocked;
    private String suspendedReason;
    private Long revokedBy;
    private String revokedReason;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public Long getSysUserId() { return sysUserId; }
    public void setSysUserId(Long v) { this.sysUserId = v; }
    public Long getBoundBy() { return boundBy; }
    public void setBoundBy(Long v) { this.boundBy = v; }
    public Instant getBoundAt() { return boundAt; }
    public void setBoundAt(Instant v) { this.boundAt = v; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant v) { this.revokedAt = v; }
    public Long getPermissionVersion() { return permissionVersion; }
    public void setPermissionVersion(Long v) { this.permissionVersion = v; }
    public String getAppId() { return appId; }
    public void setAppId(String v) { this.appId = v; }
    public String getSource() { return source; }
    public void setSource(String v) { this.source = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public byte[] getPhoneLookup() { return phoneLookup; }
    public void setPhoneLookup(byte[] v) { this.phoneLookup = v; }
    public Instant getLastPhoneVerifiedAt() { return lastPhoneVerifiedAt; }
    public void setLastPhoneVerifiedAt(Instant v) { this.lastPhoneVerifiedAt = v; }
    public Long getEmployeeAuthVersion() { return employeeAuthVersion; }
    public void setEmployeeAuthVersion(Long v) { this.employeeAuthVersion = v; }
    public Integer getAutoRebindBlocked() { return autoRebindBlocked; }
    public void setAutoRebindBlocked(Integer v) { this.autoRebindBlocked = v; }
    public String getSuspendedReason() { return suspendedReason; }
    public void setSuspendedReason(String v) { this.suspendedReason = v; }
    public Long getRevokedBy() { return revokedBy; }
    public void setRevokedBy(Long v) { this.revokedBy = v; }
    public String getRevokedReason() { return revokedReason; }
    public void setRevokedReason(String v) { this.revokedReason = v; }
}
