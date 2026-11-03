package com.xinyi.rongli.dispatch.domain;

import java.time.LocalDateTime;

/**
 * 司机唯一资源占用，不受订单待确认影响
 *
 * <p>对应表 vi_driver_occupancy，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverOccupancy
{
    /** 主键。主键，唯一占用 */
    private Long driverId;

    /** OccupancyHolderType: INVITE,ASSIGNMENT */
    private String holderType;

    /** 被占用邀约或承接ID */
    private Long holderId;

    /** 对应订单 */
    private Long orderId;

    /** 仅邀约有自动失效 */
    private LocalDateTime expiresAt;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public String getHolderType()
    {
        return holderType;
    }

    public void setHolderType(String holderType)
    {
        this.holderType = holderType;
    }

    public Long getHolderId()
    {
        return holderId;
    }

    public void setHolderId(Long holderId)
    {
        this.holderId = holderId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
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
