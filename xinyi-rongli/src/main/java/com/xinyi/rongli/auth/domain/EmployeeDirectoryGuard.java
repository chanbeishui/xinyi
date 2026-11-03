package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 员工手机号匹配与目录变更串行防护
 *
 * <p>对应表 vi_employee_directory_guard，由数据库结构生成，字段与列一一对应。</p>
 */
public class EmployeeDirectoryGuard
{
    /** 主键。数据库生成内部Long，API字符串 */
    private Long id;

    /** 首期唯一GLOBAL_DIRECTORY；同库轻量互斥，不是用户权限 */
    private String guardKey;

    /** UTC创建时间 */
    private LocalDateTime createdAt;

    /** 更新显式UTC_TIMESTAMP(3) */
    private LocalDateTime updatedAt;

    /** 受控CAS版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getGuardKey()
    {
        return guardKey;
    }

    public void setGuardKey(String guardKey)
    {
        this.guardKey = guardKey;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt)
    {
        this.updatedAt = updatedAt;
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
