package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 微信账户与框架员工受控绑定
 *
 * <p>对应表 vi_employee_binding，由数据库结构生成，字段与列一一对应。</p>
 */
public class EmployeeBinding
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 关联vi_account */
    private Long accountId;

    /** 引用现有sys_user，不重建员工 */
    private Long sysUserId;

    /** 自动关联为NULL，操作者为系统；管理员后续操作单独审计，绝不填假管理员 */
    private Long boundBy;

    /** 绑定时间 */
    private LocalDateTime boundAt;

    /** 解除时间 */
    private LocalDateTime revokedAt;

    /** 生效账户唯一 */
    private Long activeAccount;

    /** 生效员工唯一 */
    private Long activeUser;

    /** 员工关联权限版本；撤权事务更新 */
    private Long permissionVersion;

    /** 关联应用命名空间 */
    private String appId;

    /** EmployeeLinkSource: WECHAT_PHONE_MATCH… */
    private String source;

    /** EmployeeBindingLifecycle: ACTIVE,SUSPENDED… */
    private String status;

    /** 关联时一致的规范化手机号HMAC；不是可还原裸手机号 */
    private byte[] phoneLookup;

    /** 最近本人微信手机号核验时间 */
    private LocalDateTime lastPhoneVerifiedAt;

    /** 建立关联时的框架授权版本快照；每次工作请求仍检查当前员工状态和权限 */
    private Long employeeAuthVersion;

    /** 管理员撤销后拦截同应用同员工新自动关联；明确allow-rematch才解除 */
    private Integer autoRebindBlocked;

    /** 手机号变更、账号停用等失效原因；不泄漏其他账号 */
    private String suspendedReason;

    /** 后台受控撤销操作者 */
    private Long revokedBy;

    /** 解除依据 */
    private String revokedReason;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
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

    public Long getSysUserId()
    {
        return sysUserId;
    }

    public void setSysUserId(Long sysUserId)
    {
        this.sysUserId = sysUserId;
    }

    public Long getBoundBy()
    {
        return boundBy;
    }

    public void setBoundBy(Long boundBy)
    {
        this.boundBy = boundBy;
    }

    public LocalDateTime getBoundAt()
    {
        return boundAt;
    }

    public void setBoundAt(LocalDateTime boundAt)
    {
        this.boundAt = boundAt;
    }

    public LocalDateTime getRevokedAt()
    {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt)
    {
        this.revokedAt = revokedAt;
    }

    public Long getActiveAccount()
    {
        return activeAccount;
    }

    public void setActiveAccount(Long activeAccount)
    {
        this.activeAccount = activeAccount;
    }

    public Long getActiveUser()
    {
        return activeUser;
    }

    public void setActiveUser(Long activeUser)
    {
        this.activeUser = activeUser;
    }

    public Long getPermissionVersion()
    {
        return permissionVersion;
    }

    public void setPermissionVersion(Long permissionVersion)
    {
        this.permissionVersion = permissionVersion;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public byte[] getPhoneLookup()
    {
        return phoneLookup;
    }

    public void setPhoneLookup(byte[] phoneLookup)
    {
        this.phoneLookup = phoneLookup;
    }

    public LocalDateTime getLastPhoneVerifiedAt()
    {
        return lastPhoneVerifiedAt;
    }

    public void setLastPhoneVerifiedAt(LocalDateTime lastPhoneVerifiedAt)
    {
        this.lastPhoneVerifiedAt = lastPhoneVerifiedAt;
    }

    public Long getEmployeeAuthVersion()
    {
        return employeeAuthVersion;
    }

    public void setEmployeeAuthVersion(Long employeeAuthVersion)
    {
        this.employeeAuthVersion = employeeAuthVersion;
    }

    public Integer getAutoRebindBlocked()
    {
        return autoRebindBlocked;
    }

    public void setAutoRebindBlocked(Integer autoRebindBlocked)
    {
        this.autoRebindBlocked = autoRebindBlocked;
    }

    public String getSuspendedReason()
    {
        return suspendedReason;
    }

    public void setSuspendedReason(String suspendedReason)
    {
        this.suspendedReason = suspendedReason;
    }

    public Long getRevokedBy()
    {
        return revokedBy;
    }

    public void setRevokedBy(Long revokedBy)
    {
        this.revokedBy = revokedBy;
    }

    public String getRevokedReason()
    {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason)
    {
        this.revokedReason = revokedReason;
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
