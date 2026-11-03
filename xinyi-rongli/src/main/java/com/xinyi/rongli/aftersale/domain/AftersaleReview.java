package com.xinyi.rongli.aftersale.domain;

import java.time.LocalDateTime;

/**
 * 统一审核的追加式决定凭证
 *
 * <p>对应表 vi_aftersale_review，由数据库结构生成，字段与列一一对应。</p>
 */
public class AftersaleReview
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 申请 */
    private Long caseId;

    /** 唯一决定版本 */
    private Integer revision;

    /** APPROVE/REJECT */
    private String decision;

    /** 本决定金额 */
    private Long approvedFen;

    /** 审批时重新读取订单版本 */
    private Long observedOrderVersion;

    /** 实际保管方快照 */
    private String custodyAtReview;

    /** 由服务端产生 */
    private Integer returnRequired;

    /** 不能与申请人为同一真实身份 */
    private Long reviewerUserId;

    /** 审核意见 */
    private String opinion;

    /** UTC */
    private LocalDateTime reviewedAt;

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

    public Long getCaseId()
    {
        return caseId;
    }

    public void setCaseId(Long caseId)
    {
        this.caseId = caseId;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public String getDecision()
    {
        return decision;
    }

    public void setDecision(String decision)
    {
        this.decision = decision;
    }

    public Long getApprovedFen()
    {
        return approvedFen;
    }

    public void setApprovedFen(Long approvedFen)
    {
        this.approvedFen = approvedFen;
    }

    public Long getObservedOrderVersion()
    {
        return observedOrderVersion;
    }

    public void setObservedOrderVersion(Long observedOrderVersion)
    {
        this.observedOrderVersion = observedOrderVersion;
    }

    public String getCustodyAtReview()
    {
        return custodyAtReview;
    }

    public void setCustodyAtReview(String custodyAtReview)
    {
        this.custodyAtReview = custodyAtReview;
    }

    public Integer getReturnRequired()
    {
        return returnRequired;
    }

    public void setReturnRequired(Integer returnRequired)
    {
        this.returnRequired = returnRequired;
    }

    public Long getReviewerUserId()
    {
        return reviewerUserId;
    }

    public void setReviewerUserId(Long reviewerUserId)
    {
        this.reviewerUserId = reviewerUserId;
    }

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }

    public LocalDateTime getReviewedAt()
    {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt)
    {
        this.reviewedAt = reviewedAt;
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
