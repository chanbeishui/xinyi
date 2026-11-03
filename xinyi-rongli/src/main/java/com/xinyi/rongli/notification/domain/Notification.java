package com.xinyi.rongli.notification.domain;

import java.time.LocalDateTime;

/**
 * 站内消息与下发记录
 *
 * <p>对应表 vi_notification，由数据库结构生成，字段与列一一对应。</p>
 */
public class Notification
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 接收人 */
    private Long accountId;

    /** 业务视角 */
    private String role;

    /** 订单 */
    private Long orderId;

    /** 场景 */
    private String scene;

    /** 触发事件 */
    private Long outboxId;

    /** NotificationSendStatus: PENDING,ACCEPTED,FAILED… */
    private String sendStatus;

    /** INBOX=仅站内；SUBSCRIBE=已投订阅消息；SUBSCRIBE_FAILED=订阅失败 */
    private String channel;

    /** 脱敏消息 */
    private String content;

    /** 站内显式已读 */
    private LocalDateTime readAt;

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

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getScene()
    {
        return scene;
    }

    public void setScene(String scene)
    {
        this.scene = scene;
    }

    public Long getOutboxId()
    {
        return outboxId;
    }

    public void setOutboxId(Long outboxId)
    {
        this.outboxId = outboxId;
    }

    public String getSendStatus()
    {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus)
    {
        this.sendStatus = sendStatus;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public LocalDateTime getReadAt()
    {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt)
    {
        this.readAt = readAt;
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
