package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 普通取消处罚申诉记录
 *
 * <p>对应表 vi_penalty_appeal，由数据库结构生成，字段与列一一对应。</p>
 */
public class PenaltyAppeal
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 处罚主键 */
    private Long penaltyId;

    /** 本人司机档案 */
    private Long driverId;

    /** AppealRecordStatus: SUBMITTED,APPROVED,REJECTED */
    private String status;

    /** 本人说明 */
    private String reason;

    /** 通过文件权限检查的证据引用 */
    private String evidenceFileIds;

    /** UTC提交时刻 */
    private LocalDateTime submittedAt;

    /** 独立后台审核人 */
    private Long reviewerUserId;

    /** UTC审核时刻 */
    private LocalDateTime reviewedAt;

    /** 对本人公开结论 */
    private String publicOpinion;

    /** 同一处罚只允许一项待审申诉 */
    private Long activePenalty;

    /** 创建时刻UTC */
    private LocalDateTime createdAt;

    /** 更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3) */
    private LocalDateTime updatedAt;

    /** 手写CAS版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getPenaltyId()
    {
        return penaltyId;
    }

    public void setPenaltyId(Long penaltyId)
    {
        this.penaltyId = penaltyId;
    }

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
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

    public String getEvidenceFileIds()
    {
        return evidenceFileIds;
    }

    public void setEvidenceFileIds(String evidenceFileIds)
    {
        this.evidenceFileIds = evidenceFileIds;
    }

    public LocalDateTime getSubmittedAt()
    {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt)
    {
        this.submittedAt = submittedAt;
    }

    public Long getReviewerUserId()
    {
        return reviewerUserId;
    }

    public void setReviewerUserId(Long reviewerUserId)
    {
        this.reviewerUserId = reviewerUserId;
    }

    public LocalDateTime getReviewedAt()
    {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt)
    {
        this.reviewedAt = reviewedAt;
    }

    public String getPublicOpinion()
    {
        return publicOpinion;
    }

    public void setPublicOpinion(String publicOpinion)
    {
        this.publicOpinion = publicOpinion;
    }

    public Long getActivePenalty()
    {
        return activePenalty;
    }

    public void setActivePenalty(Long activePenalty)
    {
        this.activePenalty = activePenalty;
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
