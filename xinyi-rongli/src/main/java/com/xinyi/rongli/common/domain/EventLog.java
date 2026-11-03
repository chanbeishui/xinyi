package com.xinyi.rongli.common.domain;

import java.time.LocalDateTime;

/**
 * 不可变业务事实流水
 *
 * <p>对应表 vi_event_log，由数据库结构生成，字段与列一一对应。</p>
 */
public class EventLog
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 可关联订单 */
    private Long orderId;

    /** 聚合类型 */
    private String aggregateType;

    /** 聚合ID */
    private Long aggregateId;

    /** 事件或动作 */
    private String action;

    /** 小程序账户 */
    private Long actorAccountId;

    /** 框架人员 */
    private Long actorUserId;

    /** 视角 */
    private String actingRole;

    /** 操作上下文 */
    private Long stationId;

    /** 脱敏前值 */
    private String beforeSummary;

    /** 脱敏后值 */
    private String afterSummary;

    /** 原因 */
    private String reason;

    /** MINI/ADMIN/SYSTEM/CALLBACK；不可混淆数值ID */
    private String actorRealm;

    /** 可信小程序会话，可空 */
    private Long sessionId;

    /** 业务动作的可信上下文版本 */
    private Long contextVersion;

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

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
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

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Long getActorAccountId()
    {
        return actorAccountId;
    }

    public void setActorAccountId(Long actorAccountId)
    {
        this.actorAccountId = actorAccountId;
    }

    public Long getActorUserId()
    {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId)
    {
        this.actorUserId = actorUserId;
    }

    public String getActingRole()
    {
        return actingRole;
    }

    public void setActingRole(String actingRole)
    {
        this.actingRole = actingRole;
    }

    public Long getStationId()
    {
        return stationId;
    }

    public void setStationId(Long stationId)
    {
        this.stationId = stationId;
    }

    public String getBeforeSummary()
    {
        return beforeSummary;
    }

    public void setBeforeSummary(String beforeSummary)
    {
        this.beforeSummary = beforeSummary;
    }

    public String getAfterSummary()
    {
        return afterSummary;
    }

    public void setAfterSummary(String afterSummary)
    {
        this.afterSummary = afterSummary;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getActorRealm()
    {
        return actorRealm;
    }

    public void setActorRealm(String actorRealm)
    {
        this.actorRealm = actorRealm;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public Long getContextVersion()
    {
        return contextVersion;
    }

    public void setContextVersion(Long contextVersion)
    {
        this.contextVersion = contextVersion;
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
