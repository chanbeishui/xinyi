package com.xinyi.rongli.payment.domain;

import java.time.LocalDateTime;

/**
 * 永久渠道回调去重
 *
 * <p>对应表 vi_channel_event，由数据库结构生成，字段与列一一对应。</p>
 */
public class ChannelEvent
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 微信支付等 */
    private String provider;

    /** 通知ID/交易业务唯一标识 */
    private String eventKey;

    /** 关联支付或退款号 */
    private String tradeNo;

    /** 事件类型 */
    private String eventType;

    /** 已验签原报文摘要 */
    private byte[] verifiedDigest;

    /** 已持久化时间 */
    private LocalDateTime processedAt;

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

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getEventKey()
    {
        return eventKey;
    }

    public void setEventKey(String eventKey)
    {
        this.eventKey = eventKey;
    }

    public String getTradeNo()
    {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo)
    {
        this.tradeNo = tradeNo;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public byte[] getVerifiedDigest()
    {
        return verifiedDigest;
    }

    public void setVerifiedDigest(byte[] verifiedDigest)
    {
        this.verifiedDigest = verifiedDigest;
    }

    public LocalDateTime getProcessedAt()
    {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt)
    {
        this.processedAt = processedAt;
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
