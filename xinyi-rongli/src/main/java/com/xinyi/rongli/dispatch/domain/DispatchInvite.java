package com.xinyi.rongli.dispatch.domain;

import java.time.LocalDateTime;

/**
 * 单人派单邀约
 *
 * <p>对应表 vi_dispatch_invite，由数据库结构生成，字段与列一一对应。</p>
 */
public class DispatchInvite
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 候选代驾 */
    private Long driverId;

    /** InviteStatus: WAIT_RESPONSE,ACCEPTED,REJECTED… */
    private String status;

    /** AUTO/MANUAL */
    private String source;

    /** 派单轮次 */
    private String roundKey;

    /** 扩圈轮次，第 1 轮为初始半径 */
    private Integer roundNo;

    /** 邀约时刻 */
    private LocalDateTime issuedAt;

    /** 绝对失效时间 */
    private LocalDateTime expiresAt;

    /** 响应 */
    private LocalDateTime respondedAt;

    /** 同单最多一个待响应邀约 */
    private Long activeOrder;

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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getRoundKey()
    {
        return roundKey;
    }

    public void setRoundKey(String roundKey)
    {
        this.roundKey = roundKey;
    }

    public Integer getRoundNo()
    {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo)
    {
        this.roundNo = roundNo;
    }

    public LocalDateTime getIssuedAt()
    {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt)
    {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRespondedAt()
    {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt)
    {
        this.respondedAt = respondedAt;
    }

    public Long getActiveOrder()
    {
        return activeOrder;
    }

    public void setActiveOrder(Long activeOrder)
    {
        this.activeOrder = activeOrder;
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
