package com.xinyi.rongli.owner.domain;

import java.time.Instant;

/** 车主运营档案扩展（vi_owner_profile），非另一套登录账户；1:1 关联 vi_account。微信标识/已验证手机不由运营字段覆盖。 */
public class ViOwnerProfile {
    private Long id;
    private Long accountId;
    private String serviceName;
    private String serviceFlag;
    private String internalRemark;
    private Long maintainedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String v) { this.serviceName = v; }
    public String getServiceFlag() { return serviceFlag; }
    public void setServiceFlag(String v) { this.serviceFlag = v; }
    public String getInternalRemark() { return internalRemark; }
    public void setInternalRemark(String v) { this.internalRemark = v; }
    public Long getMaintainedBy() { return maintainedBy; }
    public void setMaintainedBy(Long v) { this.maintainedBy = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }
}