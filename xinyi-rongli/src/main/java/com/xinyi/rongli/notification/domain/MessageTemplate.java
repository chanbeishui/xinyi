package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 外部短信与订阅模板档案
 *
 * <p>对应表 vi_message_template，由数据库结构生成，字段与列一一对应。</p>
 */
public class MessageTemplate
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 本地模板名称 */
    private String name;

    /** ExternalMessageProvider: TENCENT_SMS,ALIYUN_SMS… */
    private String provider;

    /** 授权组织 */
    private Long ownerDeptId;

    /** IntegrationEnvironment: TEST,LIVE */
    private String environment;

    /** NotificationScene（11 个稳定业务场景） */
    private String scene;

    /** NotificationAudience: OWNER,DRIVER,STATION */
    private String audience;

    /** 当前已发布模板 */
    private Long activeRevisionId;

    /** 最新草稿 */
    private Long draftRevisionId;

    /** 模板版本分配计数 */
    private Long revisionSeq;

    /** 创建人 */
    private Long createdBy;

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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
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
