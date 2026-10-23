package com.xinyi.rongli.identity.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_account 微信统一基础账户。AppID/OpenID 由服务端可信换取，绝不接收客户端指定；
 * 新号只有 OWNER 资格，角色是账户扩展资格而非客户端可写高权身份（ADR-01）。
 * phone_cipher 为密文、phone_lookup 为带密钥摘要（非裸哈希），均不由客户端写入。
 */
public class ViAccount extends ViRecord {
    private String appId;
    private String openId;
    private String unionId;
    private byte[] phoneCipher;
    private byte[] phoneLookup;
    private String nickname;
    private String status;
    private String privacyVersion;
    private Long avatarFileId;
    private Instant phoneVerifiedAt;
    private String phoneSource;
    private Integer employeeAutoLinkBlocked;

    public String getAppId() { return appId; }
    public void setAppId(String v) { this.appId = v; }
    public String getOpenId() { return openId; }
    public void setOpenId(String v) { this.openId = v; }
    public String getUnionId() { return unionId; }
    public void setUnionId(String v) { this.unionId = v; }
    public byte[] getPhoneCipher() { return phoneCipher; }
    public void setPhoneCipher(byte[] v) { this.phoneCipher = v; }
    public byte[] getPhoneLookup() { return phoneLookup; }
    public void setPhoneLookup(byte[] v) { this.phoneLookup = v; }
    public String getNickname() { return nickname; }
    public void setNickname(String v) { this.nickname = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getPrivacyVersion() { return privacyVersion; }
    public void setPrivacyVersion(String v) { this.privacyVersion = v; }
    public Long getAvatarFileId() { return avatarFileId; }
    public void setAvatarFileId(Long v) { this.avatarFileId = v; }
    public Instant getPhoneVerifiedAt() { return phoneVerifiedAt; }
    public void setPhoneVerifiedAt(Instant v) { this.phoneVerifiedAt = v; }
    public String getPhoneSource() { return phoneSource; }
    public void setPhoneSource(String v) { this.phoneSource = v; }
    public Integer getEmployeeAutoLinkBlocked() { return employeeAutoLinkBlocked; }
    public void setEmployeeAutoLinkBlocked(Integer v) { this.employeeAutoLinkBlocked = v; }
}
