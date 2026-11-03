package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 第三方核验与培训任务记录
 *
 * <p>对应表 vi_provider_verification，由数据库结构生成，字段与列一一对应。</p>
 */
public class ProviderVerification
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 代驾申请 */
    private Long applicationId;

    /** 供应商或培训服务 */
    private String provider;

    /** VerificationType: REALNAME,FACE,BACKGROUND… */
    private String taskType;

    /** 外部任务标识 */
    private String providerTaskId;

    /** VerificationStatus: CREATED,PROCESSING,PASSED… */
    private String status;

    /** 结果摘要，不泄露生物信息 */
    private String resultDigest;

    /** 可信回调或查单确认时刻 */
    private LocalDateTime verifiedAt;

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

    public Long getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId(Long applicationId)
    {
        this.applicationId = applicationId;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getTaskType()
    {
        return taskType;
    }

    public void setTaskType(String taskType)
    {
        this.taskType = taskType;
    }

    public String getProviderTaskId()
    {
        return providerTaskId;
    }

    public void setProviderTaskId(String providerTaskId)
    {
        this.providerTaskId = providerTaskId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getResultDigest()
    {
        return resultDigest;
    }

    public void setResultDigest(String resultDigest)
    {
        this.resultDigest = resultDigest;
    }

    public LocalDateTime getVerifiedAt()
    {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt)
    {
        this.verifiedAt = verifiedAt;
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
