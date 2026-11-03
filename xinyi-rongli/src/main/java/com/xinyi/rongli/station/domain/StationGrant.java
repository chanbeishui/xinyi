package com.xinyi.rongli.station.domain;

import java.time.LocalDateTime;

/**
 * 员工跨检测点操作范围
 *
 * <p>对应表 vi_station_grant，由数据库结构生成，字段与列一一对应。</p>
 */
public class StationGrant
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 复用框架人员 */
    private Long sysUserId;

    /** 获授权站点 */
    private Long stationId;

    /** 功能权限仍单独验证 */
    private Integer enabled;

    /** 授权人 */
    private Long grantedBy;

    /** 授权变更说明 */
    private String reason;

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

    public Long getSysUserId()
    {
        return sysUserId;
    }

    public void setSysUserId(Long sysUserId)
    {
        this.sysUserId = sysUserId;
    }

    public Long getStationId()
    {
        return stationId;
    }

    public void setStationId(Long stationId)
    {
        this.stationId = stationId;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
    }

    public Long getGrantedBy()
    {
        return grantedBy;
    }

    public void setGrantedBy(Long grantedBy)
    {
        this.grantedBy = grantedBy;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
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
