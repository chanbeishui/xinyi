package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 不可变外部模板版本
 *
 * <p>对应表 vi_message_template_revision，由数据库结构生成，字段与列一一对应。</p>
 */
public class MessageTemplateRevision
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 所属模板档案 */
    private Long templateId;

    /** 版本号 */
    private Long revisionNo;

    /** 引用的通道配置 */
    private Long profileId;

    /** 引用的通道配置版本 */
    private Long profileRevisionId;

    /** 供应商审核通过的模板ID；草稿可空，发布时强制 */
    private String vendorTemplateId;

    /** 白名单变量、实际key/type/length；腾讯顺序/阿里命名 */
    private String parameterBindings;

    /** 签名/内容快照/小程序page/env/lang等非秘密 */
    private String optionsJson;

    /** 发送开关 */
    private Integer enabled;

    /** TemplateApprovalStatus: UNKNOWN,APPROVED… */
    private String approvalStatus;

    /** 内容摘要 */
    private String contentHash;

    /** 保存人 */
    private Long createdBy;

    /** 保存原因 */
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

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getRevisionNo()
    {
        return revisionNo;
    }

    public void setRevisionNo(Long revisionNo)
    {
        this.revisionNo = revisionNo;
    }

    public Long getProfileId()
    {
        return profileId;
    }

    public void setProfileId(Long profileId)
    {
        this.profileId = profileId;
    }

    public Long getProfileRevisionId()
    {
        return profileRevisionId;
    }

    public void setProfileRevisionId(Long profileRevisionId)
    {
        this.profileRevisionId = profileRevisionId;
    }

    public String getVendorTemplateId()
    {
        return vendorTemplateId;
    }

    public void setVendorTemplateId(String vendorTemplateId)
    {
        this.vendorTemplateId = vendorTemplateId;
    }

    public String getParameterBindings()
    {
        return parameterBindings;
    }

    public void setParameterBindings(String parameterBindings)
    {
        this.parameterBindings = parameterBindings;
    }

    public String getOptionsJson()
    {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson)
    {
        this.optionsJson = optionsJson;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
    }

    public String getApprovalStatus()
    {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus)
    {
        this.approvalStatus = approvalStatus;
    }

    public String getContentHash()
    {
        return contentHash;
    }

    public void setContentHash(String contentHash)
    {
        this.contentHash = contentHash;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
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
