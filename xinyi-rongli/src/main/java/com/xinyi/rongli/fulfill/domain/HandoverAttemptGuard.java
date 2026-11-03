package com.xinyi.rongli.fulfill.domain;

import java.time.LocalDateTime;

/**
 * 跨重发的交接错误次数
 *
 * <p>对应表 vi_handover_attempt_guard，由数据库结构生成，字段与列一一对应。</p>
 */
public class HandoverAttemptGuard
{
    /** 主键。订单+承接+交接类型 */
    private String guardKey;

    /** 累计错误次数 */
    private Integer failedAttempts;

    /** 错3次锁定 */
    private LocalDateTime lockedAt;

    /** 人工复核授权人 */
    private Long unlockedBy;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public String getGuardKey()
    {
        return guardKey;
    }

    public void setGuardKey(String guardKey)
    {
        this.guardKey = guardKey;
    }

    public Integer getFailedAttempts()
    {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts)
    {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockedAt()
    {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt)
    {
        this.lockedAt = lockedAt;
    }

    public Long getUnlockedBy()
    {
        return unlockedBy;
    }

    public void setUnlockedBy(Long unlockedBy)
    {
        this.unlockedBy = unlockedBy;
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
