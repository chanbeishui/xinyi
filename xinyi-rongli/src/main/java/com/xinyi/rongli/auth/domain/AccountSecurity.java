package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 小程序账号安全版本
 *
 * <p>对应表 vi_account_security，由数据库结构生成，字段与列一一对应。</p>
 */
public class AccountSecurity
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** vi_account */
    private Long accountId;

    /** 全局退出/安全封禁代际 */
    private Long sessionEpoch;

    /** 角色/能力变化版本，不等于资料version */
    private Long permissionVersion;

    /** AccountStatus: ACTIVE,LOCKED */
    private String securityStatus;

    /** 最近权限/安全变化时间 */
    private LocalDateTime changedAt;

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

    public Long getSessionEpoch()
    {
        return sessionEpoch;
    }

    public void setSessionEpoch(Long sessionEpoch)
    {
        this.sessionEpoch = sessionEpoch;
    }

    public Long getPermissionVersion()
    {
        return permissionVersion;
    }

    public void setPermissionVersion(Long permissionVersion)
    {
        this.permissionVersion = permissionVersion;
    }

    public String getSecurityStatus()
    {
        return securityStatus;
    }

    public void setSecurityStatus(String securityStatus)
    {
        this.securityStatus = securityStatus;
    }

    public LocalDateTime getChangedAt()
    {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt)
    {
        this.changedAt = changedAt;
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
