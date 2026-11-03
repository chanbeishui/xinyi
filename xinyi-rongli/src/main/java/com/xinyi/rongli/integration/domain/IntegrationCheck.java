package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 指定配置版本校验记录
 *
 * <p>对应表 vi_integration_check，由数据库结构生成，字段与列一一对应。</p>
 */
public class IntegrationCheck
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** IntegrationTargetType: PROFILE,TEMPLATE */
    private String targetType;

    /** profile/template id */
    private Long targetId;

    /** 被校验版本 */
    private Long revisionId;

    /** 被校验版本内容摘要 */
    private String contentHash;

    /** IntegrationCheckLevel: LOCAL,READ_ONLY… */
    private String checkLevel;

    /** IntegrationCheckStatus: NOT_RUN,PASS,FAIL… */
    private String status;

    /** 脱敏逐字段错误/警告/未验证项 */
    private String issuesJson;

    /** 校验时刻 */
    private LocalDateTime checkedAt;

    /** 校验结果有效期 */
    private LocalDateTime expiresAt;

    /** 校验人 */
    private Long checkedBy;

    /** 仅真实授权资金测试证据才能标记 */
    private Integer realPaymentVerified;

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

    public String getContentHash()
    {
        return contentHash;
    }

    public void setContentHash(String contentHash)
    {
        this.contentHash = contentHash;
    }

    public String getCheckLevel()
    {
        return checkLevel;
    }

    public void setCheckLevel(String checkLevel)
    {
        this.checkLevel = checkLevel;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIssuesJson()
    {
        return issuesJson;
    }

    public void setIssuesJson(String issuesJson)
    {
        this.issuesJson = issuesJson;
    }

    public LocalDateTime getCheckedAt()
    {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt)
    {
        this.checkedAt = checkedAt;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public Long getCheckedBy()
    {
        return checkedBy;
    }

    public void setCheckedBy(Long checkedBy)
    {
        this.checkedBy = checkedBy;
    }

    public Integer getRealPaymentVerified()
    {
        return realPaymentVerified;
    }

    public void setRealPaymentVerified(Integer realPaymentVerified)
    {
        this.realPaymentVerified = realPaymentVerified;
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
