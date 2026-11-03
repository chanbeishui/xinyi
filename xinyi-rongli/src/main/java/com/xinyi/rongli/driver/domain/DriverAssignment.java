package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 司机承接历史，结束后仍保留
 *
 * <p>对应表 vi_driver_assignment，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverAssignment
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 代驾 */
    private Long driverId;

    /** 接受的邀约 */
    private Long inviteId;

    /** 接受时间 */
    private LocalDateTime acceptedAt;

    /** 解除或归还时结束 */
    private LocalDateTime endedAt;

    /** AssignmentEndReason: RETURNED,CANCELLED… */
    private String endReason;

    /** 生效承接唯一订单 */
    private Long activeOrder;

    /** 生效承接唯一司机 */
    private Long activeDriver;

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

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public Long getInviteId()
    {
        return inviteId;
    }

    public void setInviteId(Long inviteId)
    {
        this.inviteId = inviteId;
    }

    public LocalDateTime getAcceptedAt()
    {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt)
    {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getEndedAt()
    {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt)
    {
        this.endedAt = endedAt;
    }

    public String getEndReason()
    {
        return endReason;
    }

    public void setEndReason(String endReason)
    {
        this.endReason = endReason;
    }

    public Long getActiveOrder()
    {
        return activeOrder;
    }

    public void setActiveOrder(Long activeOrder)
    {
        this.activeOrder = activeOrder;
    }

    public Long getActiveDriver()
    {
        return activeDriver;
    }

    public void setActiveDriver(Long activeDriver)
    {
        this.activeDriver = activeDriver;
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
