package com.xinyi.rongli.aftersale.domain;

import java.time.LocalDateTime;

/**
 * 隐私、导出、敏感访问与对账审批台账
 *
 * <p>对应表 vi_governance_case，由数据库结构生成，字段与列一一对应。</p>
 */
public class GovernanceCase
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** GovernanceType: PRIVACY,EXPORT,SENSITIVE_ACCESS… */
    private String caseType;

    /** 可关联订单 */
    private Long orderId;

    /** 申请客户 */
    private Long requesterAccountId;

    /** 申请后台人员 */
    private Long requesterUserId;

    /** GovernanceStatus: APPLIED,APPROVED,CHECKING… */
    private String status;

    /** 必要申请与结果加密 */
    private byte[] payloadCipher;

    /** 申请/处置依据 */
    private String reason;

    /** 人工处置结论，与 reason（问题类型/申请依据）分开保存 */
    private String conclusion;

    /** 审批人 */
    private Long reviewerId;

    /** 授权或下载失效 */
    private LocalDateTime expiresAt;

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

    public String getCaseType()
    {
        return caseType;
    }

    public void setCaseType(String caseType)
    {
        this.caseType = caseType;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getRequesterAccountId()
    {
        return requesterAccountId;
    }

    public void setRequesterAccountId(Long requesterAccountId)
    {
        this.requesterAccountId = requesterAccountId;
    }

    public Long getRequesterUserId()
    {
        return requesterUserId;
    }

    public void setRequesterUserId(Long requesterUserId)
    {
        this.requesterUserId = requesterUserId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public byte[] getPayloadCipher()
    {
        return payloadCipher;
    }

    public void setPayloadCipher(byte[] payloadCipher)
    {
        this.payloadCipher = payloadCipher;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getConclusion()
    {
        return conclusion;
    }

    public void setConclusion(String conclusion)
    {
        this.conclusion = conclusion;
    }

    public Long getReviewerId()
    {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId)
    {
        this.reviewerId = reviewerId;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
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
