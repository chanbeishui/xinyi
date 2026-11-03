package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 一次用户触发订阅请求与结果
 *
 * <p>对应表 vi_subscription_prompt，由数据库结构生成，字段与列一一对应。</p>
 */
public class SubscriptionPrompt
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 发起人 */
    private Long accountId;

    /** 所属小程序 */
    private String appId;

    /** 一次性nonce摘要 */
    private String nonceHash;

    /** 模板ID、公开版本及场景 */
    private String templateVersions;

    /** SubscriptionObjectType: ORDER,ASSIGNMENT… */
    private String businessObjectType;

    /** 业务对象 */
    private Long businessObjectId;

    /** SubscriptionPromptStatus: CREATED,REPORTED… */
    private String status;

    /** 短时有效 */
    private LocalDateTime expiresAt;

    /** 回传时刻 */
    private LocalDateTime reportedAt;

    /** 微信前端结果不等于服务端真实剩余额度 */
    private String clientReport;

    /** 回传摘要 */
    private String reportHash;

    /** UTC创建时间 */
    private LocalDateTime createdAt;

    /** UTC更新时间 */
    private LocalDateTime updatedAt;

    /** 显式CAS；不是注解自动生效 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getNonceHash()
    {
        return nonceHash;
    }

    public void setNonceHash(String nonceHash)
    {
        this.nonceHash = nonceHash;
    }

    public String getTemplateVersions()
    {
        return templateVersions;
    }

    public void setTemplateVersions(String templateVersions)
    {
        this.templateVersions = templateVersions;
    }

    public String getBusinessObjectType()
    {
        return businessObjectType;
    }

    public void setBusinessObjectType(String businessObjectType)
    {
        this.businessObjectType = businessObjectType;
    }

    public Long getBusinessObjectId()
    {
        return businessObjectId;
    }

    public void setBusinessObjectId(Long businessObjectId)
    {
        this.businessObjectId = businessObjectId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getReportedAt()
    {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt)
    {
        this.reportedAt = reportedAt;
    }

    public String getClientReport()
    {
        return clientReport;
    }

    public void setClientReport(String clientReport)
    {
        this.clientReport = clientReport;
    }

    public String getReportHash()
    {
        return reportHash;
    }

    public void setReportHash(String reportHash)
    {
        this.reportHash = reportHash;
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
