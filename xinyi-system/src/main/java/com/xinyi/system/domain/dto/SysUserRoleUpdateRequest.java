package com.xinyi.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户角色更新请求。
 */
public class SysUserRoleUpdateRequest
{
    private Long[] roleIds;

    @NotBlank(message = "变更原因不能为空")
    private String reason;

    public Long[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
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
