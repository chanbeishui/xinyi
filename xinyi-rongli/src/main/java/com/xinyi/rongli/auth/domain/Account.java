package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 微信统一基础账户
 *
 * <p>对应表 vi_account，由数据库结构生成，字段与列一一对应。</p>
 */
public class Account
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 微信应用标识 */
    private String appId;

    /** 同一小程序身份 */
    private String openId;

    /** 仅供应商可信返回时记录 */
    private String unionId;

    /** 手机号密文 */
    private byte[] phoneCipher;

    /** 带密钥摘要，非裸哈希 */
    private byte[] phoneLookup;

    /** 服务端最后一次可信手机号验证UTC时刻 */
    private LocalDateTime phoneVerifiedAt;

    /** PhoneProofSource: WECHAT,SMS；仅WECHAT可触发新员工关联 */
    private String phoneSource;

    /** 管理员解除后阻止该账号自动换绑；仅后台明确恢复，可不影响车主 */
    private Integer employeeAutoLinkBlocked;

    /** 选填昵称 */
    private String nickname;

    /** AccountStatus: ACTIVE,LOCKED */
    private String status;

    /** 最近同意版本 */
    private String privacyVersion;

    /** 本人已验证头像引用，不直接存URL */
    private Long avatarFileId;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getOpenId()
    {
        return openId;
    }

    public void setOpenId(String openId)
    {
        this.openId = openId;
    }

    public String getUnionId()
    {
        return unionId;
    }

    public void setUnionId(String unionId)
    {
        this.unionId = unionId;
    }

    public byte[] getPhoneCipher()
    {
        return phoneCipher;
    }

    public void setPhoneCipher(byte[] phoneCipher)
    {
        this.phoneCipher = phoneCipher;
    }

    public byte[] getPhoneLookup()
    {
        return phoneLookup;
    }

    public void setPhoneLookup(byte[] phoneLookup)
    {
        this.phoneLookup = phoneLookup;
    }

    public LocalDateTime getPhoneVerifiedAt()
    {
        return phoneVerifiedAt;
    }

    public void setPhoneVerifiedAt(LocalDateTime phoneVerifiedAt)
    {
        this.phoneVerifiedAt = phoneVerifiedAt;
    }

    public String getPhoneSource()
    {
        return phoneSource;
    }

    public void setPhoneSource(String phoneSource)
    {
        this.phoneSource = phoneSource;
    }

    public Integer getEmployeeAutoLinkBlocked()
    {
        return employeeAutoLinkBlocked;
    }

    public void setEmployeeAutoLinkBlocked(Integer employeeAutoLinkBlocked)
    {
        this.employeeAutoLinkBlocked = employeeAutoLinkBlocked;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPrivacyVersion()
    {
        return privacyVersion;
    }

    public void setPrivacyVersion(String privacyVersion)
    {
        this.privacyVersion = privacyVersion;
    }

    public Long getAvatarFileId()
    {
        return avatarFileId;
    }

    public void setAvatarFileId(Long avatarFileId)
    {
        this.avatarFileId = avatarFileId;
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
