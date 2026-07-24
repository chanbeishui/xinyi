package com.xinyi.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户管理范围变更请求。
 */
public class SysUserManagementScopeRequest
{
    @NotBlank(message = "管理范围不能为空")
    private String managementScope;

    @NotBlank(message = "变更原因不能为空")
    private String reason;

    public String getManagementScope()
    {
        return managementScope;
    }

    public void setManagementScope(String managementScope)
    {
        this.managementScope = managementScope;
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
