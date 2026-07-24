package com.xinyi.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户任职部门关系。
 */
public class SysUserDept implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long deptId;
    private String createBy;
    private Date createTime;

    public SysUserDept()
    {
    }

    public SysUserDept(Long userId, Long deptId, String createBy)
    {
        this.userId = userId;
        this.deptId = deptId;
        this.createBy = createBy;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
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
