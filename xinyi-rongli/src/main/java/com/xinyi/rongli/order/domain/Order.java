package com.xinyi.rongli.order.domain;

import java.time.LocalDateTime;

/**
 * 订单主履约与实际保管
 *
 * <p>对应表 vi_order，由数据库结构生成，字段与列一一对应。</p>
 */
public class Order
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 对外业务单号 */
    private String orderNo;

    /** 下单账户 */
    private Long ownerAccountId;

    /** 常用车辆档案 */
    private Long vehicleId;

    /** 业务权威检测点归属 */
    private Long stationId;

    /** 框架数据归属快照 */
    private Long deptId;

    /** 报价及价格快照 */
    private Long quoteId;

    /** 历史车辆快照 */
    private String vehicleSnapshot;

    /** FulfillmentStatus: PENDING_PAY,SCHEDULED… */
    private String fulfillmentStatus;

    /** CancelStatus: NONE,APPLIED,REJECTED… */
    private String cancelStatus;

    /** CustodyType: OWNER,DRIVER,STATION */
    private String custodyType;

    /** 实际保管方ID，须与类型联查 */
    private Long custodyRefId;

    /** 当前承接指针，已释放时为空 */
    private Long activeAssignmentId;

    /** 对外约定上门时间 */
    private LocalDateTime pickupStartsAt;

    /** 对外时段结束 */
    private LocalDateTime pickupEndsAt;

    /** 创建后30分钟 */
    private LocalDateTime payExpiresAt;

    /** 预约前N分钟 */
    private LocalDateTime dispatchAt;

    /** 派单参数快照 */
    private Long dispatchConfigVersion;

    /** 实际归还成功时间 */
    private LocalDateTime returnedAt;

    /** 归还后24小时服务确认截止 */
    private LocalDateTime autoConfirmAt;

    /** 业务服务确认完成 */
    private LocalDateTime completedAt;

    /** 取消正式办结 */
    private LocalDateTime cancelledAt;

    /** 应付分 */
    private Long totalFen;

    /** 服务协议版本 */
    private String termsVersion;

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

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getOwnerAccountId()
    {
        return ownerAccountId;
    }

    public void setOwnerAccountId(Long ownerAccountId)
    {
        this.ownerAccountId = ownerAccountId;
    }

    public Long getVehicleId()
    {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId)
    {
        this.vehicleId = vehicleId;
    }

    public Long getStationId()
    {
        return stationId;
    }

    public void setStationId(Long stationId)
    {
        this.stationId = stationId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getQuoteId()
    {
        return quoteId;
    }

    public void setQuoteId(Long quoteId)
    {
        this.quoteId = quoteId;
    }

    public String getVehicleSnapshot()
    {
        return vehicleSnapshot;
    }

    public void setVehicleSnapshot(String vehicleSnapshot)
    {
        this.vehicleSnapshot = vehicleSnapshot;
    }

    public String getFulfillmentStatus()
    {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(String fulfillmentStatus)
    {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public String getCancelStatus()
    {
        return cancelStatus;
    }

    public void setCancelStatus(String cancelStatus)
    {
        this.cancelStatus = cancelStatus;
    }

    public String getCustodyType()
    {
        return custodyType;
    }

    public void setCustodyType(String custodyType)
    {
        this.custodyType = custodyType;
    }

    public Long getCustodyRefId()
    {
        return custodyRefId;
    }

    public void setCustodyRefId(Long custodyRefId)
    {
        this.custodyRefId = custodyRefId;
    }

    public Long getActiveAssignmentId()
    {
        return activeAssignmentId;
    }

    public void setActiveAssignmentId(Long activeAssignmentId)
    {
        this.activeAssignmentId = activeAssignmentId;
    }

    public LocalDateTime getPickupStartsAt()
    {
        return pickupStartsAt;
    }

    public void setPickupStartsAt(LocalDateTime pickupStartsAt)
    {
        this.pickupStartsAt = pickupStartsAt;
    }

    public LocalDateTime getPickupEndsAt()
    {
        return pickupEndsAt;
    }

    public void setPickupEndsAt(LocalDateTime pickupEndsAt)
    {
        this.pickupEndsAt = pickupEndsAt;
    }

    public LocalDateTime getPayExpiresAt()
    {
        return payExpiresAt;
    }

    public void setPayExpiresAt(LocalDateTime payExpiresAt)
    {
        this.payExpiresAt = payExpiresAt;
    }

    public LocalDateTime getDispatchAt()
    {
        return dispatchAt;
    }

    public void setDispatchAt(LocalDateTime dispatchAt)
    {
        this.dispatchAt = dispatchAt;
    }

    public Long getDispatchConfigVersion()
    {
        return dispatchConfigVersion;
    }

    public void setDispatchConfigVersion(Long dispatchConfigVersion)
    {
        this.dispatchConfigVersion = dispatchConfigVersion;
    }

    public LocalDateTime getReturnedAt()
    {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt)
    {
        this.returnedAt = returnedAt;
    }

    public LocalDateTime getAutoConfirmAt()
    {
        return autoConfirmAt;
    }

    public void setAutoConfirmAt(LocalDateTime autoConfirmAt)
    {
        this.autoConfirmAt = autoConfirmAt;
    }

    public LocalDateTime getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt)
    {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt()
    {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt)
    {
        this.cancelledAt = cancelledAt;
    }

    public Long getTotalFen()
    {
        return totalFen;
    }

    public void setTotalFen(Long totalFen)
    {
        this.totalFen = totalFen;
    }

    public String getTermsVersion()
    {
        return termsVersion;
    }

    public void setTermsVersion(String termsVersion)
    {
        this.termsVersion = termsVersion;
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
