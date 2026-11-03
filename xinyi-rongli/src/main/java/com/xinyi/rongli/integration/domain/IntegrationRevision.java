package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 不可变第三方配置版本
 *
 * <p>对应表 vi_integration_revision，由数据库结构生成，字段与列一一对应。</p>
 */
public class IntegrationRevision
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 配置档案 */
    private Long profileId;

    /** 版本号，由 revision_seq 分配 */
    private Long revisionNo;

    /** 经kind专用DTO验证的非秘密配置 */
    private String configJson;

    /** 本配置凭据槽位到secret id的内部映射，不下发 */
    private String secretRefsJson;

    /** 规范化内容与秘密引用版本摘要 */
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

    public Long getProfileId()
    {
        return profileId;
    }

    public void setProfileId(Long profileId)
    {
        this.profileId = profileId;
    }

    public Long getRevisionNo()
    {
        return revisionNo;
    }

    public void setRevisionNo(Long revisionNo)
    {
        this.revisionNo = revisionNo;
    }

    public String getConfigJson()
    {
        return configJson;
    }

    public void setConfigJson(String configJson)
    {
        this.configJson = configJson;
    }

    public String getSecretRefsJson()
    {
        return secretRefsJson;
    }

    public void setSecretRefsJson(String secretRefsJson)
    {
        this.secretRefsJson = secretRefsJson;
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
