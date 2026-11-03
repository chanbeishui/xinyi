package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 第三方配置档案与生效指针
 *
 * <p>对应表 vi_integration_profile，由数据库结构生成，字段与列一一对应。</p>
 */
public class IntegrationProfile
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** IntegrationKind: PAYMENT_POLICY,WECHAT_PAY… */
    private String kind;

    /** 真实管理/收款主体，复用框架部门归属 */
    private Long ownerDeptId;

    /** IntegrationEnvironment: TEST,LIVE */
    private String environment;

    /** PAYMENT_POLICY与WECHAT_APP用DEFAULT；其他由服务端生成 */
    private String configKey;

    /** 配置名称 1-60字，这里留到100 */
    private String displayName;

    /** IntegrationLifecycle: DRAFT,ACTIVE,RETIRED */
    private String lifecycle;

    /** 当前发布版本 */
    private Long activeRevisionId;

    /** 最新草稿版本 */
    private Long draftRevisionId;

    /** 分配版本号的CAS计数 */
    private Long revisionSeq;

    /** 配置创建人 */
    private Long createdBy;

    /** 最近发布人 */
    private Long lastPublishedBy;

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

    public String getKind()
    {
        return kind;
    }

    public void setKind(String kind)
    {
        this.kind = kind;
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

    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getLifecycle()
    {
        return lifecycle;
    }

    public void setLifecycle(String lifecycle)
    {
        this.lifecycle = lifecycle;
    }

    public Long getActiveRevisionId()
    {
        return activeRevisionId;
    }

    public void setActiveRevisionId(Long activeRevisionId)
    {
        this.activeRevisionId = activeRevisionId;
    }

    public Long getDraftRevisionId()
    {
        return draftRevisionId;
    }

    public void setDraftRevisionId(Long draftRevisionId)
    {
        this.draftRevisionId = draftRevisionId;
    }

    public Long getRevisionSeq()
    {
        return revisionSeq;
    }

    public void setRevisionSeq(Long revisionSeq)
    {
        this.revisionSeq = revisionSeq;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
    }

    public Long getLastPublishedBy()
    {
        return lastPublishedBy;
    }

    public void setLastPublishedBy(Long lastPublishedBy)
    {
        this.lastPublishedBy = lastPublishedBy;
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
