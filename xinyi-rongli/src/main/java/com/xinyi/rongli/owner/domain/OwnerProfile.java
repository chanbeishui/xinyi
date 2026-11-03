package com.xinyi.rongli.owner.domain;

import java.time.LocalDateTime;

/**
 * 车主运营档案扩展，非另一套登录账户
 *
 * <p>对应表 vi_owner_profile，由数据库结构生成，字段与列一一对应。</p>
 */
public class OwnerProfile
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 关联vi_account */
    private Long accountId;

    /** 服务称呼 */
    private String serviceName;

    /** OwnerServiceFlag: NORMAL,VERIFY_AGAIN */
    private String serviceFlag;

    /** 内部备注 */
    private String internalRemark;

    /** 框架维护人 */
    private Long maintainedBy;

    /** UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public String getServiceFlag()
    {
        return serviceFlag;
    }

    public void setServiceFlag(String serviceFlag)
    {
        this.serviceFlag = serviceFlag;
    }

    public String getInternalRemark()
    {
        return internalRemark;
    }

    public void setInternalRemark(String internalRemark)
    {
        this.internalRemark = internalRemark;
    }

    public Long getMaintainedBy()
    {
        return maintainedBy;
    }

    public void setMaintainedBy(Long maintainedBy)
    {
        this.maintainedBy = maintainedBy;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
}
