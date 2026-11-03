package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 第三方测试收件人白名单
 *
 * <p>对应表 vi_integration_test_recipient，由数据库结构生成，字段与列一一对应。</p>
 */
public class IntegrationTestRecipient
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 白名单名称 */
    private String name;

    /** IntegrationRecipientKind: PHONE,MINI_ACCOUNT */
    private String kind;

    /** 归属组织 */
    private Long ownerDeptId;

    /** IntegrationEnvironment: TEST,LIVE */
    private String environment;

    /** 仅PHONE */
    private byte[] phoneCipher;

    /** 仅MINI_ACCOUNT */
    private Long accountId;

    /** 小程序白名单同应用 */
    private Long appProfileId;

    /** 启用 */
    private Integer enabled;

    /** 登记人 */
    private Long createdBy;

    /** 登记原因 */
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public byte[] getPhoneCipher()
    {
        return phoneCipher;
    }

    public void setPhoneCipher(byte[] phoneCipher)
    {
        this.phoneCipher = phoneCipher;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public Long getAppProfileId()
    {
        return appProfileId;
    }

    public void setAppProfileId(Long appProfileId)
    {
        this.appProfileId = appProfileId;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
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
