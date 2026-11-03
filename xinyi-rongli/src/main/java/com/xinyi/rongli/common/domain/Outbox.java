package com.xinyi.rongli.common.domain;

import java.time.LocalDateTime;

/**
 * 可靠异步事务消息
 *
 * <p>对应表 vi_outbox，由数据库结构生成，字段与列一一对应。</p>
 */
public class Outbox
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 事件唯一键 */
    private String eventKey;

    /** 所属对象 */
    private String aggregateType;

    /** 对象ID */
    private Long aggregateId;

    /** 通知/支付查单/补偿等 */
    private String eventType;

    /** 最小必要载荷，不塞敏感原文 */
    private String payload;

    /** OutboxStatus: READY,PROCESSING,DONE,FAILED */
    private String status;

    /** 重试次数 */
    private Integer attempts;

    /** 退避时间 */
    private LocalDateTime nextAttemptAt;

    /** 工作者租约 */
    private LocalDateTime leaseUntil;

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

    public String getEventKey()
    {
        return eventKey;
    }

    public void setEventKey(String eventKey)
    {
        this.eventKey = eventKey;
    }

    public String getAggregateType()
    {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType)
    {
        this.aggregateType = aggregateType;
    }

    public Long getAggregateId()
    {
        return aggregateId;
    }

    public void setAggregateId(Long aggregateId)
    {
        this.aggregateId = aggregateId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getAttempts()
    {
        return attempts;
    }

    public void setAttempts(Integer attempts)
    {
        this.attempts = attempts;
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
