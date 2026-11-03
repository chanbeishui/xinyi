package com.xinyi.rongli.aftersale.domain;

import java.time.LocalDateTime;

/**
 * 取消与退款的唯一申请与处理记录
 *
 * <p>对应表 vi_aftersale_case，由数据库结构生成，字段与列一一对应。</p>
 */
public class AftersaleCase
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 对外售后号 */
    private String caseNo;

    /** 订单 */
    private Long orderId;

    /** 支付记录 */
    private Long paymentId;

    /** CANCEL_REFUND/REFUND_ONLY */
    private String kind;

    /** AfterSaleStatus: PENDING_REVIEW,EXECUTING… */
    private String caseStatus;

    /** AfterSaleReviewStatus: PENDING,APPROVED,REJECTED */
    private String reviewStatus;

    /** 审核修订号 */
    private Integer reviewRevision;

    /** 申请金额 */
    private Long requestedFen;

    /** 批准金额，未审为NULL */
    private Long approvedFen;

    /** UNDETERMINED/NOT_REQUIRED/WAIT_RETURN/RETURNED */
    private String returnState;

    /** NONE/NOT_REQUIRED/WAIT_RETURN/READY/PROCESSING/S… */
    private String refundState;

    /** ApplicantType: ACCOUNT,SYS_USER */
    private String applicantType;

    /** 实际申请身份 */
    private Long applicantId;

    /** 原始申请说明 */
    private String reason;

    /** 关联证据文件ID */
    private String evidenceIds;

    /** 业务及资金均已办结 */
    private LocalDateTime resolvedAt;

    /** 一个未办结申请 */
    private Long activeOrder;

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

    public String getCaseNo()
    {
        return caseNo;
    }

    public void setCaseNo(String caseNo)
    {
        this.caseNo = caseNo;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getPaymentId()
    {
        return paymentId;
    }

    public void setPaymentId(Long paymentId)
    {
        this.paymentId = paymentId;
    }

    public String getKind()
    {
        return kind;
    }

    public void setKind(String kind)
    {
        this.kind = kind;
    }

    public String getCaseStatus()
    {
        return caseStatus;
    }

    public void setCaseStatus(String caseStatus)
    {
        this.caseStatus = caseStatus;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
    }

    public Integer getReviewRevision()
    {
        return reviewRevision;
    }

    public void setReviewRevision(Integer reviewRevision)
    {
        this.reviewRevision = reviewRevision;
    }

    public Long getRequestedFen()
    {
        return requestedFen;
    }

    public void setRequestedFen(Long requestedFen)
    {
        this.requestedFen = requestedFen;
    }

    public Long getApprovedFen()
    {
        return approvedFen;
    }

    public void setApprovedFen(Long approvedFen)
    {
        this.approvedFen = approvedFen;
    }

    public String getReturnState()
    {
        return returnState;
    }

    public void setReturnState(String returnState)
    {
        this.returnState = returnState;
    }

    public String getRefundState()
    {
        return refundState;
    }

    public void setRefundState(String refundState)
    {
        this.refundState = refundState;
    }

    public String getApplicantType()
    {
        return applicantType;
    }

    public void setApplicantType(String applicantType)
    {
        this.applicantType = applicantType;
    }

    public Long getApplicantId()
    {
        return applicantId;
    }

    public void setApplicantId(Long applicantId)
    {
        this.applicantId = applicantId;
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

    public LocalDateTime getResolvedAt()
    {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt)
    {
        this.resolvedAt = resolvedAt;
    }

    public Long getActiveOrder()
    {
        return activeOrder;
    }

    public void setActiveOrder(Long activeOrder)
    {
        this.activeOrder = activeOrder;
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
