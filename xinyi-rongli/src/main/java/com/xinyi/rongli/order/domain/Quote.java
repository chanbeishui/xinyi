package com.xinyi.rongli.order.domain;

import java.time.LocalDateTime;

/**
 * 短期报价快照
 *
 * <p>对应表 vi_quote，由数据库结构生成，字段与列一一对应。</p>
 */
public class Quote
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 报价人 */
    private Long ownerAccountId;

    /** 匹配站点 */
    private Long stationId;

    /** 车牌、车型编码与名称快照 */
    private String vehicleSnapshot;

    /** 加密接车地址快照 */
    private byte[] addressSnapshotCipher;

    /** 上门时段 */
    private Long pickupSlotId;

    /** 内部接收排期 */
    private Long receiveSlotId;

    /** 计价版本ID */
    private Long ruleId;

    /** 供应商路线距离、方向、时效 */
    private String routeSnapshot;

    /** 总费用分 */
    private Long amountFen;

    /** 逐项明细及计算依据 */
    private String itemsJson;

    /** 报价失效时间 */
    private LocalDateTime expiresAt;

    /** 已用于建单的订单 */
    private Long usedOrderId;

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

    public Long getOwnerAccountId()
    {
        return ownerAccountId;
    }

    public void setOwnerAccountId(Long ownerAccountId)
    {
        this.ownerAccountId = ownerAccountId;
    }

    public Long getStationId()
    {
        return stationId;
    }

    public void setStationId(Long stationId)
    {
        this.stationId = stationId;
    }

    public String getVehicleSnapshot()
    {
        return vehicleSnapshot;
    }

    public void setVehicleSnapshot(String vehicleSnapshot)
    {
        this.vehicleSnapshot = vehicleSnapshot;
    }

    public byte[] getAddressSnapshotCipher()
    {
        return addressSnapshotCipher;
    }

    public void setAddressSnapshotCipher(byte[] addressSnapshotCipher)
    {
        this.addressSnapshotCipher = addressSnapshotCipher;
    }

    public Long getPickupSlotId()
    {
        return pickupSlotId;
    }

    public void setPickupSlotId(Long pickupSlotId)
    {
        this.pickupSlotId = pickupSlotId;
    }

    public Long getReceiveSlotId()
    {
        return receiveSlotId;
    }

    public void setReceiveSlotId(Long receiveSlotId)
    {
        this.receiveSlotId = receiveSlotId;
    }

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public String getRouteSnapshot()
    {
        return routeSnapshot;
    }

    public void setRouteSnapshot(String routeSnapshot)
    {
        this.routeSnapshot = routeSnapshot;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public String getItemsJson()
    {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson)
    {
        this.itemsJson = itemsJson;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public Long getUsedOrderId()
    {
        return usedOrderId;
    }

    public void setUsedOrderId(Long usedOrderId)
    {
        this.usedOrderId = usedOrderId;
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
