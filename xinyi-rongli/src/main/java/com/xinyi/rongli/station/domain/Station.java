package com.xinyi.rongli.station.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检测点与可服务设置
 *
 * <p>对应表 vi_station，由数据库结构生成，字段与列一一对应。</p>
 */
public class Station
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 名称 */
    private String name;

    /** 框架部门归属 */
    private Long deptId;

    /** 检测点公开地址 */
    private String address;

    /** GCJ02经度 */
    private BigDecimal lng;

    /** GCJ02纬度 */
    private BigDecimal lat;

    /** 服务半径 */
    private Integer serviceRadiusMeter;

    /** 引用现有vehicle_type数据字典 */
    private String supportedVehicleCodes;

    /** 营业日和接收边界 */
    private String businessHours;

    /** 配置完成后启用 */
    private Integer enabled;

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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public BigDecimal getLng()
    {
        return lng;
    }

    public void setLng(BigDecimal lng)
    {
        this.lng = lng;
    }

    public BigDecimal getLat()
    {
        return lat;
    }

    public void setLat(BigDecimal lat)
    {
        this.lat = lat;
    }

    public Integer getServiceRadiusMeter()
    {
        return serviceRadiusMeter;
    }

    public void setServiceRadiusMeter(Integer serviceRadiusMeter)
    {
        this.serviceRadiusMeter = serviceRadiusMeter;
    }

    public String getSupportedVehicleCodes()
    {
        return supportedVehicleCodes;
    }

    public void setSupportedVehicleCodes(String supportedVehicleCodes)
    {
        this.supportedVehicleCodes = supportedVehicleCodes;
    }

    public String getBusinessHours()
    {
        return businessHours;
    }

    public void setBusinessHours(String businessHours)
    {
        this.businessHours = businessHours;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
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
