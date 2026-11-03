package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 配置发布与回滚审计
 *
 * <p>对应表 vi_integration_publication，由数据库结构生成，字段与列一一对应。</p>
 */
public class IntegrationPublication
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** IntegrationTargetType: PROFILE,TEMPLATE */
    private String targetType;

    /** profile/template id */
    private Long targetId;

    /** 本次生效版本 */
    private Long revisionId;

    /** 旧生效版本 */
    private Long previousRevisionId;

    /** 发布依据的校验记录 */
    private Long checkId;

    /** IntegrationPublishAction: PUBLISH,ROLLBACK */
    private String action;

    /** 发布人 */
    private Long publishedBy;

    /** 发布时刻 */
    private LocalDateTime publishedAt;

    /** 发布/回滚原因 */
    private String reason;

    /** 发布时依赖版本快照 */
    private String dependencyVersions;

    /** 发布事务后的根版本 */
    private Long targetVersion;

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

    public String getTargetType()
    {
        return targetType;
    }

    public void setTargetType(String targetType)
    {
        this.targetType = targetType;
    }

    public Long getTargetId()
    {
        return targetId;
    }

    public void setTargetId(Long targetId)
    {
        this.targetId = targetId;
    }

    public Long getRevisionId()
    {
        return revisionId;
    }

    public void setRevisionId(Long revisionId)
    {
        this.revisionId = revisionId;
    }

    public Long getPreviousRevisionId()
    {
        return previousRevisionId;
    }

    public void setPreviousRevisionId(Long previousRevisionId)
    {
        this.previousRevisionId = previousRevisionId;
    }

    public Long getCheckId()
    {
        return checkId;
    }

    public void setCheckId(Long checkId)
    {
        this.checkId = checkId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Long getPublishedBy()
    {
        return publishedBy;
    }

    public void setPublishedBy(Long publishedBy)
    {
        this.publishedBy = publishedBy;
    }

    public LocalDateTime getPublishedAt()
    {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt)
    {
        this.publishedAt = publishedAt;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getDependencyVersions()
    {
        return dependencyVersions;
    }

    public void setDependencyVersions(String dependencyVersions)
    {
        this.dependencyVersions = dependencyVersions;
    }

    public Long getTargetVersion()
    {
        return targetVersion;
    }

    public void setTargetVersion(Long targetVersion)
    {
        this.targetVersion = targetVersion;
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
