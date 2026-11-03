package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 代驾最近前台定位
 *
 * <p>对应表 vi_driver_location，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverLocation
{
    /** 主键。主键 */
    private Long driverId;

    /** 加密位置 */
    private byte[] locationCipher;

    /** 设备采集时间 */
    private LocalDateTime capturedAt;

    /** 服务器接收时间 */
    private LocalDateTime receivedAt;

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

    public byte[] getLocationCipher()
    {
        return locationCipher;
    }

    public void setLocationCipher(byte[] locationCipher)
    {
        this.locationCipher = locationCipher;
    }

    public LocalDateTime getCapturedAt()
    {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt)
    {
        this.capturedAt = capturedAt;
    }

    public LocalDateTime getReceivedAt()
    {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt)
    {
        this.receivedAt = receivedAt;
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
