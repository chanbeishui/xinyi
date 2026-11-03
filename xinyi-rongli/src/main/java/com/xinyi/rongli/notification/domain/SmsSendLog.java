package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 短信发送与频控日志
 *
 * <p>对应表 vi_sms_send_log，由数据库结构生成，字段与列一一对应。</p>
 */
public class SmsSendLog
{
    /** 主键。 */
    private Long id;

    /** 发起账号；后台代发时为空 */
    private Long accountId;

    /** 手机号 SHA-256 摘要，用于频控计数，不落明文 */
    private byte[] phoneLookup;

    /** 脱敏手机号，便于人工核查 */
    private String phoneMasked;

    /** LOGIN=登录/绑定手机号，TEST=管理端连通性测试 */
    private String scene;

    /** ALIYUN / TENCENT / MOCK */
    private String provider;

    /** template code */
    private String templateCode;

    /** OK=供应商受理，FAIL=调用失败，BLOCKED=频控拦截 */
    private String status;

    /** error code */
    private String errorCode;

    /** error message */
    private String errorMessage;

    /** created at */
    private LocalDateTime createdAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public byte[] getPhoneLookup()
    {
        return phoneLookup;
    }

    public void setPhoneLookup(byte[] phoneLookup)
    {
        this.phoneLookup = phoneLookup;
    }

    public String getPhoneMasked()
    {
        return phoneMasked;
    }

    public void setPhoneMasked(String phoneMasked)
    {
        this.phoneMasked = phoneMasked;
    }

    public String getScene()
    {
        return scene;
    }

    public void setScene(String scene)
    {
        this.scene = scene;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getTemplateCode()
    {
        return templateCode;
    }

    public void setTemplateCode(String templateCode)
    {
        this.templateCode = templateCode;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }
}
