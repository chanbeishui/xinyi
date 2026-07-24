package com.xinyi.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限敏感操作安全审计。
 */
public class SysSecurityAuditLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long auditId;
    private String eventType;
    private Long actorUserId;
    private String actorUserName;
    private Long targetUserId;
    private String requestId;
    private String clientIp;
    private String reason;
    private String beforeJson;
    private String afterJson;
    private Long authVersionBefore;
    private Long authVersionAfter;
    private String result;
    private String failureCode;
    private String failureMessage;
    private Date createTime;

    public Long getAuditId()
    {
        return auditId;
    }

    public void setAuditId(Long auditId)
    {
        this.auditId = auditId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public Long getActorUserId()
    {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId)
    {
        this.actorUserId = actorUserId;
    }

    public String getActorUserName()
    {
        return actorUserName;
    }

    public void setActorUserName(String actorUserName)
    {
        this.actorUserName = actorUserName;
    }

    public Long getTargetUserId()
    {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId)
    {
        this.targetUserId = targetUserId;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getClientIp()
    {
        return clientIp;
    }

    public void setClientIp(String clientIp)
    {
        this.clientIp = clientIp;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getBeforeJson()
    {
        return beforeJson;
    }

    public void setBeforeJson(String beforeJson)
    {
        this.beforeJson = beforeJson;
    }

    public String getAfterJson()
    {
        return afterJson;
    }

    public void setAfterJson(String afterJson)
    {
        this.afterJson = afterJson;
    }

    public Long getAuthVersionBefore()
    {
        return authVersionBefore;
    }

    public void setAuthVersionBefore(Long authVersionBefore)
    {
        this.authVersionBefore = authVersionBefore;
    }

    public Long getAuthVersionAfter()
    {
        return authVersionAfter;
    }

    public void setAuthVersionAfter(Long authVersionAfter)
    {
        this.authVersionAfter = authVersionAfter;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getFailureCode()
    {
        return failureCode;
    }

    public void setFailureCode(String failureCode)
    {
        this.failureCode = failureCode;
    }

    public String getFailureMessage()
    {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage)
    {
        this.failureMessage = failureMessage;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
