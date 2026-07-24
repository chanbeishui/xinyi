package com.xinyi.system.domain;

import java.io.Serializable;

/**
 * 用户认证权威状态。
 */
public class SysUserAuthState implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long authVersion;
    private String status;
    private String delFlag;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getAuthVersion()
    {
        return authVersion;
    }

    public void setAuthVersion(Long authVersion)
    {
        this.authVersion = authVersion;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
