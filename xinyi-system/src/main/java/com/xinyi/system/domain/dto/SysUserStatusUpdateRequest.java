package com.xinyi.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户状态变更请求。
 */
public class SysUserStatusUpdateRequest
{
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "用户状态不能为空")
    private String status;

    @NotBlank(message = "变更原因不能为空")
    private String reason;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
