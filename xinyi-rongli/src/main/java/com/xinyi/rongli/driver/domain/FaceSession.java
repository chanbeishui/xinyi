package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 本人实名活体启动与可信结果会话
 *
 * <p>对应表 vi_face_session，由数据库结构生成，字段与列一一对应。</p>
 */
public class FaceSession
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 资格申请 */
    private Long applicationId;

    /** 供应商核验记录 */
    private Long verificationId;

    /** 本人基础账户 */
    private Long accountId;

    /** FaceSessionStatus: CREATED,PROCESSING,PASSED… */
    private String status;

    /** FaceLaunchMode: WEBVIEW_URL */
    private String launchMode;

    /** 加密短期URL；失效或结束销毁 */
    private byte[] launchPayloadCipher;

    /** 此次明确同意版本 */
    private String consentVersion;

    /** 供应商与本地较短有效期 */
    private LocalDateTime expiresAt;

    /** 可信查询后赋值 */
    private Integer resultAvailable;

    /** 安全映射错误，无原始生物数据 */
    private String failureCode;

    /** 创建时刻UTC */
    private LocalDateTime createdAt;

    /** 更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3) */
    private LocalDateTime updatedAt;

    /** 手写CAS版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId(Long applicationId)
    {
        this.applicationId = applicationId;
    }

    public Long getVerificationId()
    {
        return verificationId;
    }

    public void setVerificationId(Long verificationId)
    {
        this.verificationId = verificationId;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getLaunchMode()
    {
        return launchMode;
    }

    public void setLaunchMode(String launchMode)
    {
        this.launchMode = launchMode;
    }

    public byte[] getLaunchPayloadCipher()
    {
        return launchPayloadCipher;
    }

    public void setLaunchPayloadCipher(byte[] launchPayloadCipher)
    {
        this.launchPayloadCipher = launchPayloadCipher;
    }

    public String getConsentVersion()
    {
        return consentVersion;
    }

    public void setConsentVersion(String consentVersion)
    {
        this.consentVersion = consentVersion;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public Integer getResultAvailable()
    {
        return resultAvailable;
    }

    public void setResultAvailable(Integer resultAvailable)
    {
        this.resultAvailable = resultAvailable;
    }

    public String getFailureCode()
    {
        return failureCode;
    }

    public void setFailureCode(String failureCode)
    {
        this.failureCode = failureCode;
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
