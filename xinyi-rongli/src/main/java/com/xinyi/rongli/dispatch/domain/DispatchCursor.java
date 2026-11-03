package com.xinyi.rongli.dispatch.domain;

import java.time.LocalDateTime;

/**
 * 全局轮转游标与版本
 *
 * <p>对应表 vi_dispatch_cursor，由数据库结构生成，字段与列一一对应。</p>
 */
public class DispatchCursor
{
    /** 主键。全局或已确认调度域主键 */
    private String scopeKey;

    /** 上次派发指针 */
    private Long lastDriverId;

    /** 轮次定义须P01评审 */
    private Long roundNo;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public String getScopeKey()
    {
        return scopeKey;
    }

    public void setScopeKey(String scopeKey)
    {
        this.scopeKey = scopeKey;
    }

    public Long getLastDriverId()
    {
        return lastDriverId;
    }

    public void setLastDriverId(Long lastDriverId)
    {
        this.lastDriverId = lastDriverId;
    }

    public Long getRoundNo()
    {
        return roundNo;
    }

    public void setRoundNo(Long roundNo)
    {
        this.roundNo = roundNo;
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
