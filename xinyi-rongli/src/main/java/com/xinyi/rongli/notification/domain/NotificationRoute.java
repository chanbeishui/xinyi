package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 业务事件到唯一主通道的路由
 *
 * <p>对应表 vi_notification_route，由数据库结构生成，字段与列一一对应。</p>
 */
public class NotificationRoute
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 归属组织 */
    private Long ownerDeptId;

    /** IntegrationEnvironment: TEST,LIVE */
    private String environment;

    /** NotificationScene */
    private String scene;

    /** NotificationAudience: OWNER,DRIVER,STATION */
    private String audience;

    /** 主通道模板 */
    private Long primaryTemplateId;

    /** 只允许明确未发送时按规则用备用 */
    private Long fallbackTemplateId;

    /** MessageFallbackPolicy: NONE… */
    private String fallbackPolicy;

    /** 路由开关 */
    private Integer enabled;

    /** 最后操作人 */
    private Long updatedBy;

    /** 变更原因 */
    private String reason;

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

    public Long getOwnerDeptId()
    {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long ownerDeptId)
    {
        this.ownerDeptId = ownerDeptId;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    public String getScene()
    {
        return scene;
    }

    public void setScene(String scene)
    {
        this.scene = scene;
    }

    public String getAudience()
    {
        return audience;
    }

    public void setAudience(String audience)
    {
        this.audience = audience;
    }

    public Long getPrimaryTemplateId()
    {
        return primaryTemplateId;
    }

    public void setPrimaryTemplateId(Long primaryTemplateId)
    {
        this.primaryTemplateId = primaryTemplateId;
    }

    public Long getFallbackTemplateId()
    {
        return fallbackTemplateId;
    }

    public void setFallbackTemplateId(Long fallbackTemplateId)
    {
        this.fallbackTemplateId = fallbackTemplateId;
    }

    public String getFallbackPolicy()
    {
        return fallbackPolicy;
    }

    public void setFallbackPolicy(String fallbackPolicy)
    {
        this.fallbackPolicy = fallbackPolicy;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
    }

    public Long getUpdatedBy()
    {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy)
    {
        this.updatedBy = updatedBy;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
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
