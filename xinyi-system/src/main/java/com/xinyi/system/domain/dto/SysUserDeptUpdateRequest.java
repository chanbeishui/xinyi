package com.xinyi.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 用户任职部门更新请求。
 */
public class SysUserDeptUpdateRequest
{
    @NotNull(message = "主部门不能为空")
    private Long primaryDeptId;

    @NotEmpty(message = "任职部门不能为空")
    private Long[] deptIds;

    @NotBlank(message = "变更原因不能为空")
    private String reason;

    public Long getPrimaryDeptId()
    {
        return primaryDeptId;
    }

    public void setPrimaryDeptId(Long primaryDeptId)
    {
        this.primaryDeptId = primaryDeptId;
    }

    public Long[] getDeptIds()
    {
        return deptIds;
    }

    public void setDeptIds(Long[] deptIds)
    {
        this.deptIds = deptIds;
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
