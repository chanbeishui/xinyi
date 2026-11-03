package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 代驾报酬规则独立版本
 *
 * <p>对应表 vi_driver_fee_rule，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverFeeRule
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 承担应付的主体 */
    private Long payorEntityId;

    /** 稳定规则标识 */
    private String ruleCode;

    /** 版本 */
    private Integer revision;

    /** 币种 */
    private String currency;

    /** 正常归还/取消返还/接车前解除 */
    private String scenario;

    /** 车型/代驾/服务范围条件 */
    private String scopeJson;

    /** 固定费、含路线米、计费单位、单价及进位 */
    private String pricingJson;

    /** DriverFeeRuleStatus: DRAFT,PENDING_REVIEW… */
    private String status;

    /** 生效起点UTC */
    private LocalDateTime effectiveFrom;

    /** 不含截止 */
    private LocalDateTime effectiveTo;

    /** 规则编辑人 */
    private Long createdBy;

    /** 独立审核人 */
    private Long approvedBy;

    /** 发布确认时间 */
    private LocalDateTime approvedAt;

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

    public Long getPayorEntityId()
    {
        return payorEntityId;
    }

    public void setPayorEntityId(Long payorEntityId)
    {
        this.payorEntityId = payorEntityId;
    }

    public String getRuleCode()
    {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode)
    {
        this.ruleCode = ruleCode;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getScenario()
    {
        return scenario;
    }

    public void setScenario(String scenario)
    {
        this.scenario = scenario;
    }

    public String getScopeJson()
    {
        return scopeJson;
    }

    public void setScopeJson(String scopeJson)
    {
        this.scopeJson = scopeJson;
    }

    public String getPricingJson()
    {
        return pricingJson;
    }

    public void setPricingJson(String pricingJson)
    {
        this.pricingJson = pricingJson;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getEffectiveFrom()
    {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom)
    {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo()
    {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo)
    {
        this.effectiveTo = effectiveTo;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
    }

    public Long getApprovedBy()
    {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy)
    {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt()
    {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt)
    {
        this.approvedAt = approvedAt;
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
