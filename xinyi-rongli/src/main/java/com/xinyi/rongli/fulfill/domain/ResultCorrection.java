package com.xinyi.rongli.fulfill.domain;

import java.time.LocalDateTime;

/**
 * 检测结果更正审批
 *
 * <p>对应表 vi_result_correction，由数据库结构生成，字段与列一一对应。</p>
 */
public class ResultCorrection
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 原版本 */
    private Long baseResultId;

    /** 拟更正内容 */
    private String replacementJson;

    /** CorrectionStatus: APPLIED,APPROVED,REJECTED */
    private String status;

    /** 原因 */
    private String reason;

    /** 申请人 */
    private Long applicantId;

    /** 审核人 */
    private Long reviewerId;

    /** 意见 */
    private String opinion;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getBaseResultId()
    {
        return baseResultId;
    }

    public void setBaseResultId(Long baseResultId)
    {
        this.baseResultId = baseResultId;
    }

    public String getReplacementJson()
    {
        return replacementJson;
    }

    public void setReplacementJson(String replacementJson)
    {
        this.replacementJson = replacementJson;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public Long getApplicantId()
    {
        return applicantId;
    }

    public void setApplicantId(Long applicantId)
    {
        this.applicantId = applicantId;
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
