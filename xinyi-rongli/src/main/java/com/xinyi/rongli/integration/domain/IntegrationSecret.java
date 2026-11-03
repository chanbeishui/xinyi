package com.xinyi.rongli.integration.domain;

import java.time.LocalDateTime;

/**
 * 安全凭据密文与受控生命周期
 *
 * <p>对应表 vi_integration_secret，由数据库结构生成，字段与列一一对应。</p>
 */
public class IntegrationSecret
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 配置档案 */
    private Long profileId;

    /** 固定允许的凭据槽，禁止用户自造密钥用途 */
    private String slotName;

    /** IntegrationCredentialKind: PRIVATE_KEY… */
    private String credentialKind;

    /** AEAD密文，绝非base64明文 */
    private byte[] ciphertext;

    /** 每密文独立随机nonce */
    private byte[] nonce;

    /** 主密钥版本在服务端安全配置 */
    private String keyVersion;

    /** 绑定profile/slot/env的不可更改上下文 */
    private String aadContext;

    /** 安全标识不可逆；不显示明文末几位 */
    private String fingerprint;

    /** IntegrationSecretLifecycle: ACTIVE,RETIRED… */
    private String lifecycle;

    /** 证书类凭据到期 */
    private LocalDateTime expiresAt;

    /** 退役时刻 */
    private LocalDateTime retiredAt;

    /** 写入人 */
    private Long createdBy;

    /** 受控撤销人 */
    private Long retiredBy;

    /** 撤销原因 */
    private String retireReason;

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

    public String getSlotName()
    {
        return slotName;
    }

    public void setSlotName(String slotName)
    {
        this.slotName = slotName;
    }

    public String getCredentialKind()
    {
        return credentialKind;
    }

    public void setCredentialKind(String credentialKind)
    {
        this.credentialKind = credentialKind;
    }

    public byte[] getCiphertext()
    {
        return ciphertext;
    }

    public void setCiphertext(byte[] ciphertext)
    {
        this.ciphertext = ciphertext;
    }

    public byte[] getNonce()
    {
        return nonce;
    }

    public void setNonce(byte[] nonce)
    {
        this.nonce = nonce;
    }

    public String getKeyVersion()
    {
        return keyVersion;
    }

    public void setKeyVersion(String keyVersion)
    {
        this.keyVersion = keyVersion;
    }

    public String getAadContext()
    {
        return aadContext;
    }

    public void setAadContext(String aadContext)
    {
        this.aadContext = aadContext;
    }

    public String getFingerprint()
    {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint)
    {
        this.fingerprint = fingerprint;
    }

    public String getLifecycle()
    {
        return lifecycle;
    }

    public void setLifecycle(String lifecycle)
    {
        this.lifecycle = lifecycle;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRetiredAt()
    {
        return retiredAt;
    }

    public void setRetiredAt(LocalDateTime retiredAt)
    {
        this.retiredAt = retiredAt;
    }

    public Long getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy)
    {
        this.createdBy = createdBy;
    }

    public Long getRetiredBy()
    {
        return retiredBy;
    }

    public void setRetiredBy(Long retiredBy)
    {
        this.retiredBy = retiredBy;
    }

    public String getRetireReason()
    {
        return retireReason;
    }

    public void setRetireReason(String retireReason)
    {
        this.retireReason = retireReason;
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
