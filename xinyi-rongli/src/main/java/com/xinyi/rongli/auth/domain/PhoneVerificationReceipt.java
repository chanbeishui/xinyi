package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 手机号授权处理与重试回执
 *
 * <p>对应表 vi_phone_verification_receipt，由数据库结构生成，字段与列一一对应。</p>
 */
public class PhoneVerificationReceipt
{
    /** 主键。数据库生成内部Long，API字符串 */
    private Long id;

    /** 客户端一次流程UUID，绑定当前账号 */
    private String requestId;

    /** 服务端应用标识 */
    private String appId;

    /** 已认证MINI账号；不从请求接收 */
    private Long accountId;

    /** 发起的基础会话 */
    private Long sessionId;

    /** 微信phoneCode摘要，防跨账号重放；绝不保存原始code */
    private byte[] codeHash;

    /** PhoneVerificationStatus: PROCESSING,VERIFIED… */
    private String status;

    /** 成功后可信手机号HMAC */
    private byte[] phoneLookup;

    /** 成功结果恢复所需加密号码；敏感回执到期按留存规则清理，审计不记录原文 */
    private byte[] phoneCipher;

    /** 微信已成功核验的UTC时间 */
    private LocalDateTime verifiedAt;

    /** 关联重试窗口截至时间；默认10分钟是技术值，不是微信code有效期 */
    private LocalDateTime expiresAt;

    /** EmployeeLinkOutcome；内部原因不直接下发小程序 */
    private String employeeLinkOutcome;

    /** 处理租约；处理中未知不盲目重复消费供应商凭据 */
    private LocalDateTime leaseUntil;

    /** 最终处理时间 */
    private LocalDateTime finishedAt;

    /** UTC创建时间 */
    private LocalDateTime createdAt;

    /** 更新显式UTC_TIMESTAMP(3) */
    private LocalDateTime updatedAt;

    /** 受控CAS版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public byte[] getCodeHash()
    {
        return codeHash;
    }

    public void setCodeHash(byte[] codeHash)
    {
        this.codeHash = codeHash;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public byte[] getPhoneLookup()
    {
        return phoneLookup;
    }

    public void setPhoneLookup(byte[] phoneLookup)
    {
        this.phoneLookup = phoneLookup;
    }

    public byte[] getPhoneCipher()
    {
        return phoneCipher;
    }

    public void setPhoneCipher(byte[] phoneCipher)
    {
        this.phoneCipher = phoneCipher;
    }

    public LocalDateTime getVerifiedAt()
    {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt)
    {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public String getEmployeeLinkOutcome()
    {
        return employeeLinkOutcome;
    }

    public void setEmployeeLinkOutcome(String employeeLinkOutcome)
    {
        this.employeeLinkOutcome = employeeLinkOutcome;
    }

    public LocalDateTime getLeaseUntil()
    {
        return leaseUntil;
    }

    public void setLeaseUntil(LocalDateTime leaseUntil)
    {
        this.leaseUntil = leaseUntil;
    }

    public LocalDateTime getFinishedAt()
    {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt)
    {
        this.finishedAt = finishedAt;
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
