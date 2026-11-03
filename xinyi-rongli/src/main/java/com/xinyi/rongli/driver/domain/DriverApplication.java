package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 代驾资格申请与复审版本
 *
 * <p>对应表 vi_driver_application，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverApplication
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 关联代驾档案 */
    private Long driverId;

    /** QualificationStatus: DRAFT,SUBMITTED,APPROVED… */
    private String status;

    /** 证件信息加密，不存明文JSON */
    private byte[] sensitivePayloadCipher;

    /** 证件文件关联ID */
    private String evidenceFileIds;

    /** 可信核验和培训记录ID */
    private String providerResultIds;

    /** 完整提交时间 */
    private LocalDateTime submittedAt;

    /** 审核人 */
    private Long reviewerUserId;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    /** 审批意见 */
    private String reviewOpinion;

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

    public byte[] getSensitivePayloadCipher()
    {
        return sensitivePayloadCipher;
    }

    public void setSensitivePayloadCipher(byte[] sensitivePayloadCipher)
    {
        this.sensitivePayloadCipher = sensitivePayloadCipher;
    }

    public String getEvidenceFileIds()
    {
        return evidenceFileIds;
    }

    public void setEvidenceFileIds(String evidenceFileIds)
    {
        this.evidenceFileIds = evidenceFileIds;
    }

    public String getProviderResultIds()
    {
        return providerResultIds;
    }

    public void setProviderResultIds(String providerResultIds)
    {
        this.providerResultIds = providerResultIds;
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

    public String getReviewOpinion()
    {
        return reviewOpinion;
    }

    public void setReviewOpinion(String reviewOpinion)
    {
        this.reviewOpinion = reviewOpinion;
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
