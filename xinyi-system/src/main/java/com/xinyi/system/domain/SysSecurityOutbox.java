package com.xinyi.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 安全事件outbox。
 */
public class SysSecurityOutbox implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long eventId;
    private Long userId;
    private Long targetAuthVersion;
    private String eventType;
    private String status;
    private Integer retryCount;
    private Date nextRetryTime;
    private String lastError;
    private Date createTime;
    private Date completeTime;

    public Long getEventId()
    {
        return eventId;
    }

    public void setEventId(Long eventId)
    {
        this.eventId = eventId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getTargetAuthVersion()
    {
        return targetAuthVersion;
    }

    public void setTargetAuthVersion(Long targetAuthVersion)
    {
        this.targetAuthVersion = targetAuthVersion;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount)
    {
        this.retryCount = retryCount;
    }

    public Date getNextRetryTime()
    {
        return nextRetryTime;
    }

    public void setNextRetryTime(Date nextRetryTime)
    {
        this.nextRetryTime = nextRetryTime;
    }

    public String getLastError()
    {
        return lastError;
    }

    public void setLastError(String lastError)
    {
        this.lastError = lastError;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getCompleteTime()
    {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime)
    {
        this.completeTime = completeTime;
    }
}
