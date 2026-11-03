package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 按真实承接形成的不可覆盖费用台账
 *
 * <p>对应表 vi_driver_fee_ledger，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverFeeLedger
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 代驾 */
    private Long driverId;

    /** 付款责任主体 */
    private Long payorEntityId;

    /** 币种 */
    private String currency;

    /** 具体承接，不仅订单 */
    private Long assignmentId;

    /** 客户订单 */
    private Long orderId;

    /** 归还/解除/调整永久去重键 */
    private String sourceEventKey;

    /** NORMAL_RETURN/CANCEL_RETURN/CANCEL_BEFORE_PICKUP… */
    private String entryKind;

    /** 实际任务结束UTC；非客户服务确认时间 */
    private LocalDateTime serviceEndedAt;

    /** LedgerPricingStatus: PENDING_PRICING,READY */
    private String pricingStatus;

    /** 匹配规则版本ID */
    private Long feeRuleId;

    /** 冻结规则 */
    private String ruleSnapshot;

    /** 未知为NULL；调整可负 */
    private Long amountFen;

    /** 基础/路线/调整明细 */
    private String feeBreakdown;

    /** 调整源条目 */
    private Long sourceEntryId;

    /** 人工核价复核人 */
    private Long verifiedBy;

    /** 确认结转生成的核价/差额已直接纳入本单，不能再入未来账单 */
    private Long includedStatementId;

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

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public Long getPayorEntityId()
    {
        return payorEntityId;
    }

    public void setPayorEntityId(Long payorEntityId)
    {
        this.payorEntityId = payorEntityId;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public Long getAssignmentId()
    {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId)
    {
        this.assignmentId = assignmentId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getSourceEventKey()
    {
        return sourceEventKey;
    }

    public void setSourceEventKey(String sourceEventKey)
    {
        this.sourceEventKey = sourceEventKey;
    }

    public String getEntryKind()
    {
        return entryKind;
    }

    public void setEntryKind(String entryKind)
    {
        this.entryKind = entryKind;
    }

    public LocalDateTime getServiceEndedAt()
    {
        return serviceEndedAt;
    }

    public void setServiceEndedAt(LocalDateTime serviceEndedAt)
    {
        this.serviceEndedAt = serviceEndedAt;
    }

    public String getPricingStatus()
    {
        return pricingStatus;
    }

    public void setPricingStatus(String pricingStatus)
    {
        this.pricingStatus = pricingStatus;
    }

    public Long getFeeRuleId()
    {
        return feeRuleId;
    }

    public void setFeeRuleId(Long feeRuleId)
    {
        this.feeRuleId = feeRuleId;
    }

    public String getRuleSnapshot()
    {
        return ruleSnapshot;
    }

    public void setRuleSnapshot(String ruleSnapshot)
    {
        this.ruleSnapshot = ruleSnapshot;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public String getFeeBreakdown()
    {
        return feeBreakdown;
    }

    public void setFeeBreakdown(String feeBreakdown)
    {
        this.feeBreakdown = feeBreakdown;
    }

    public Long getSourceEntryId()
    {
        return sourceEntryId;
    }

    public void setSourceEntryId(Long sourceEntryId)
    {
        this.sourceEntryId = sourceEntryId;
    }

    public Long getVerifiedBy()
    {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy)
    {
        this.verifiedBy = verifiedBy;
    }

    public Long getIncludedStatementId()
    {
        return includedStatementId;
    }

    public void setIncludedStatementId(Long includedStatementId)
    {
        this.includedStatementId = includedStatementId;
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
