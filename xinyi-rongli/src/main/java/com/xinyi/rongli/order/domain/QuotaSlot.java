package com.xinyi.rongli.order.domain;

import java.time.LocalDateTime;

/**
 * 上门服务与站点接收额度
 *
 * <p>对应表 vi_quota_slot，由数据库结构生成，字段与列一一对应。</p>
 */
public class QuotaSlot
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** QuotaType: PICKUP,STATION_RECEIVE */
    private String quotaType;

    /** 区域标识或站点标识 */
    private String scopeKey;

    /** 接收额度所属站点 */
    private Long stationId;

    /** 时间区间起 */
    private LocalDateTime startsAt;

    /** 时间区间止 */
    private LocalDateTime endsAt;

    /** 可提供数量 */
    private Integer totalQuota;

    /** 未付预占数量 */
    private Integer heldQuota;

    /** 已付预约尚未消耗 */
    private Integer confirmedQuota;

    /** 已经真实消耗 */
    private Integer consumedQuota;

    /** 停约开关 */
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

    public String getQuotaType()
    {
        return quotaType;
    }

    public void setQuotaType(String quotaType)
    {
        this.quotaType = quotaType;
    }

    public String getScopeKey()
    {
        return scopeKey;
    }

    public void setScopeKey(String scopeKey)
    {
        this.scopeKey = scopeKey;
    }

    public Long getStationId()
    {
        return stationId;
    }

    public void setStationId(Long stationId)
    {
        this.stationId = stationId;
    }

    public LocalDateTime getStartsAt()
    {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt)
    {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt()
    {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt)
    {
        this.endsAt = endsAt;
    }

    public Integer getTotalQuota()
    {
        return totalQuota;
    }

    public void setTotalQuota(Integer totalQuota)
    {
        this.totalQuota = totalQuota;
    }

    public Integer getHeldQuota()
    {
        return heldQuota;
    }

    public void setHeldQuota(Integer heldQuota)
    {
        this.heldQuota = heldQuota;
    }

    public Integer getConfirmedQuota()
    {
        return confirmedQuota;
    }

    public void setConfirmedQuota(Integer confirmedQuota)
    {
        this.confirmedQuota = confirmedQuota;
    }

    public Integer getConsumedQuota()
    {
        return consumedQuota;
    }

    public void setConsumedQuota(Integer consumedQuota)
    {
        this.consumedQuota = consumedQuota;
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
