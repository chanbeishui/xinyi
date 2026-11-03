package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 外部发送执行与回执
 *
 * <p>对应表 vi_notification_delivery，由数据库结构生成，字段与列一一对应。</p>
 */
public class NotificationDelivery
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 关联已有站内消息/OTP任务 */
    private Long notificationId;

    /** 逻辑事件业务键 */
    private String eventKey;

    /** accountId或脱敏HMAC，禁止裸号 */
    private String recipientKey;

    /** 必要收件手机号/OpenID密文 */
    private byte[] recipientCipher;

    /** 归属组织 */
    private Long ownerDeptId;

    /** IntegrationEnvironment: TEST,LIVE */
    private String environment;

    /** 使用的路由 */
    private Long routeId;

    /** NotificationScene */
    private String scene;

    /** ExternalMessageProvider */
    private String provider;

    /** 模板档案 */
    private Long templateId;

    /** 冻结的模板版本 */
    private Long templateRevisionId;

    /** 通道配置 */
    private Long profileId;

    /** 冻结的通道版本 */
    private Long profileRevisionId;

    /** 消息内容加密引用，不含明文验证码 */
    private String payloadCipherRefs;

    /** ExternalDeliveryStatus: PENDING,SENDING… */
    private String status;

    /** 供应商请求ID */
    private String vendorRequestId;

    /** 供应商标识（回执查询用） */
    private String vendorBizId;

    /** 脱敏错误码 */
    private String safeErrorCode;

    /** 脱敏错误信息 */
    private String safeErrorMessage;

    /** 尝试次数 */
    private Integer attemptCount;

    /** 下次受控重试时刻 */
    private LocalDateTime nextAttemptAt;

    /** worker租约 */
    private LocalDateTime leaseUntil;

    /** 供应商受理时刻 */
    private LocalDateTime acceptedAt;

    /** 供应商送达时刻 */
    private LocalDateTime deliveredAt;

    /** 业务事件有效期 */
    private LocalDateTime businessExpiresAt;

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

    public Long getNotificationId()
    {
        return notificationId;
    }

    public void setNotificationId(Long notificationId)
    {
        this.notificationId = notificationId;
    }

    public String getEventKey()
    {
        return eventKey;
    }

    public void setEventKey(String eventKey)
    {
        this.eventKey = eventKey;
    }

    public String getRecipientKey()
    {
        return recipientKey;
    }

    public void setRecipientKey(String recipientKey)
    {
        this.recipientKey = recipientKey;
    }

    public byte[] getRecipientCipher()
    {
        return recipientCipher;
    }

    public void setRecipientCipher(byte[] recipientCipher)
    {
        this.recipientCipher = recipientCipher;
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

    public Long getRouteId()
    {
        return routeId;
    }

    public void setRouteId(Long routeId)
    {
        this.routeId = routeId;
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

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateRevisionId()
    {
        return templateRevisionId;
    }

    public void setTemplateRevisionId(Long templateRevisionId)
    {
        this.templateRevisionId = templateRevisionId;
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

    public String getPayloadCipherRefs()
    {
        return payloadCipherRefs;
    }

    public void setPayloadCipherRefs(String payloadCipherRefs)
    {
        this.payloadCipherRefs = payloadCipherRefs;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVendorRequestId()
    {
        return vendorRequestId;
    }

    public void setVendorRequestId(String vendorRequestId)
    {
        this.vendorRequestId = vendorRequestId;
    }

    public String getVendorBizId()
    {
        return vendorBizId;
    }

    public void setVendorBizId(String vendorBizId)
    {
        this.vendorBizId = vendorBizId;
    }

    public String getSafeErrorCode()
    {
        return safeErrorCode;
    }

    public void setSafeErrorCode(String safeErrorCode)
    {
        this.safeErrorCode = safeErrorCode;
    }

    public String getSafeErrorMessage()
    {
        return safeErrorMessage;
    }

    public void setSafeErrorMessage(String safeErrorMessage)
    {
        this.safeErrorMessage = safeErrorMessage;
    }

    public Integer getAttemptCount()
    {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount)
    {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getNextAttemptAt()
    {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(LocalDateTime nextAttemptAt)
    {
        this.nextAttemptAt = nextAttemptAt;
    }

    public LocalDateTime getLeaseUntil()
    {
        return leaseUntil;
    }

    public void setLeaseUntil(LocalDateTime leaseUntil)
    {
        this.leaseUntil = leaseUntil;
    }

    public LocalDateTime getAcceptedAt()
    {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt)
    {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getDeliveredAt()
    {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt)
    {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getBusinessExpiresAt()
    {
        return businessExpiresAt;
    }

    public void setBusinessExpiresAt(LocalDateTime businessExpiresAt)
    {
        this.businessExpiresAt = businessExpiresAt;
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
