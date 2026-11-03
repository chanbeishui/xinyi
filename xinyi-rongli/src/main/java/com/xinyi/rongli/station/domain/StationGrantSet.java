package com.xinyi.rongli.station.domain;

import java.time.LocalDateTime;

/**
 * 员工站点授权集合版本
 *
 * <p>对应表 vi_station_grant_set，由数据库结构生成，字段与列一一对应。</p>
 */
public class StationGrantSet
{
    /** 主键。数据库生成内部Long，API字符串 */
    private Long id;

    /** 目标框架员工；可无微信账号 */
    private Long sysUserId;

    /** 仅行政展示，不自动授权 */
    private Long ownStationId;

    /** 后台修改人 */
    private Long changedBy;

    /** 本次集合调整原因 */
    private String reason;

    /** UTC创建时间 */
    private LocalDateTime createdAt;

    /** 更新显式UTC_TIMESTAMP(3) */
    private LocalDateTime updatedAt;

    /** 受控CAS版本 */
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

    public Long getOwnStationId()
    {
        return ownStationId;
    }

    public void setOwnStationId(Long ownStationId)
    {
        this.ownStationId = ownStationId;
    }

    public Long getChangedBy()
    {
        return changedBy;
    }

    public void setChangedBy(Long changedBy)
    {
        this.changedBy = changedBy;
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
