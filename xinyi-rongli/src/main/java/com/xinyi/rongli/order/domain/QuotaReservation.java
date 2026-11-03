package com.xinyi.rongli.order.domain;

import java.time.LocalDateTime;

/**
 * 每单额度占用凭证
 *
 * <p>对应表 vi_quota_reservation，由数据库结构生成，字段与列一一对应。</p>
 */
public class QuotaReservation
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 关联订单 */
    private Long orderId;

    /** 关联quota_slot */
    private Long slotId;

    /** ReservationStatus: HELD,CONFIRMED,CONSUMED… */
    private String status;

    /** 数量 */
    private Integer quantity;

    /** 未付锁额截止 */
    private LocalDateTime expiresAt;

    /** 释放时间 */
    private LocalDateTime releasedAt;

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

    public Long getSlotId()
    {
        return slotId;
    }

    public void setSlotId(Long slotId)
    {
        this.slotId = slotId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getReleasedAt()
    {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt)
    {
        this.releasedAt = releasedAt;
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
