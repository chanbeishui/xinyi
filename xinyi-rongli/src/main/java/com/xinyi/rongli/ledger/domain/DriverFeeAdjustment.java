package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 独立审核的费用调整
 *
 * <p>对应表 vi_driver_fee_adjustment，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverFeeAdjustment
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 源费用记录 */
    private Long sourceEntryId;

    /** 源代驾 */
    private Long driverId;

    /** 源主体 */
    private Long payorEntityId;

    /** 非0有符号调整 */
    private Long amountFen;

    /** 业务依据 */
    private String reason;

    /** 必填凭证引用 */
    private String evidenceIds;

    /** AdjustmentStatus: PENDING,APPROVED,REJECTED */
    private String status;

    /** 经办人 */
    private Long createdBy;

    /** 独立审核人 */
    private Long reviewerId;

    /** 意见 */
    private String opinion;

    /** 审批后唯一派生费用条目 */
    private Long generatedLedgerId;

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

    public Long getSourceEntryId()
    {
        return sourceEntryId;
    }

    public void setSourceEntryId(Long sourceEntryId)
    {
        this.sourceEntryId = sourceEntryId;
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

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getEvidenceIds()
    {
        return evidenceIds;
    }

    public void setEvidenceIds(String evidenceIds)
    {
        this.evidenceIds = evidenceIds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
    }

    public Long getReviewerId()
    {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId)
    {
        this.reviewerId = reviewerId;
    }

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }

    public Long getGeneratedLedgerId()
    {
        return generatedLedgerId;
    }

    public void setGeneratedLedgerId(Long generatedLedgerId)
    {
        this.generatedLedgerId = generatedLedgerId;
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
