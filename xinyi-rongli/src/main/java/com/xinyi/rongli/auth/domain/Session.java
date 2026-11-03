package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 小程序独立设备会话
 *
 * <p>对应表 vi_session，由数据库结构生成，字段与列一一对应。</p>
 */
public class Session
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 基础账号 */
    private Long accountId;

    /** 微信应用 */
    private String appId;

    /** SessionStatus: ACTIVE,REVOKED,EXPIRED */
    private String status;

    /** 签发时代际 */
    private Long sessionEpoch;

    /** 当前设备角色/站点切换版本 */
    private Long contextVersion;

    /** OWNER/DRIVER/STATION；不是授权来源 */
    private String activeRole;

    /** 只存获授权当前站点上下文 */
    private Long stationId;

    /** 可信受控绑定快照；每次工作操作再查实时状态 */
    private Long linkedSysUserId;

    /** 会话绝对截止，不无限滑动 */
    private LocalDateTime absoluteExpiresAt;

    /** 退出或安全撤销 */
    private LocalDateTime revokedAt;

    /** 最小必要设备标识摘要，不作为授权凭据 */
    private String deviceLabel;

    /** UTC */
    private LocalDateTime createdAt;

    /** 并发控制；不可变行不更新内容 */
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

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getSessionEpoch()
    {
        return sessionEpoch;
    }

    public void setSessionEpoch(Long sessionEpoch)
    {
        this.sessionEpoch = sessionEpoch;
    }

    public Long getContextVersion()
    {
        return contextVersion;
    }

    public void setContextVersion(Long contextVersion)
    {
        this.contextVersion = contextVersion;
    }

    public String getActiveRole()
    {
        return activeRole;
    }

    public void setActiveRole(String activeRole)
    {
        this.activeRole = activeRole;
    }

    public Long getStationId()
    {
        return stationId;
    }

    public void setStationId(Long stationId)
    {
        this.stationId = stationId;
    }

    public Long getLinkedSysUserId()
    {
        return linkedSysUserId;
    }

    public void setLinkedSysUserId(Long linkedSysUserId)
    {
        this.linkedSysUserId = linkedSysUserId;
    }

    public LocalDateTime getAbsoluteExpiresAt()
    {
        return absoluteExpiresAt;
    }

    public void setAbsoluteExpiresAt(LocalDateTime absoluteExpiresAt)
    {
        this.absoluteExpiresAt = absoluteExpiresAt;
    }

    public LocalDateTime getRevokedAt()
    {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt)
    {
        this.revokedAt = revokedAt;
    }

    public String getDeviceLabel()
    {
        return deviceLabel;
    }

    public void setDeviceLabel(String deviceLabel)
    {
        this.deviceLabel = deviceLabel;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
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
