package com.xinyi.rongli.capacity.domain;

import com.xinyi.rongli.core.ViRecord;
import java.time.Instant;

/** 每个站点独立的接车或接收时段额度，业务占用数不由后台请求覆盖。 */
public class ViQuotaSlot extends ViRecord {
    private Long stationId;
    private String quotaType;
    private Instant startsAt;
    private Instant endsAt;
    private int totalQuota;
    private int heldQuota;
    private int confirmedQuota;
    private int consumedQuota;
    private boolean enabled;
    public Long getStationId() { return stationId; }
    public void setStationId(Long v) { stationId = v; }
    public String getQuotaType() { return quotaType; }
    public void setQuotaType(String v) { quotaType = v; }
    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant v) { startsAt = v; }
    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant v) { endsAt = v; }
    public int getTotalQuota() { return totalQuota; }
    public void setTotalQuota(int v) { totalQuota = v; }
    public int getHeldQuota() { return heldQuota; }
    public void setHeldQuota(int v) { heldQuota = v; }
    public int getConfirmedQuota() { return confirmedQuota; }
    public void setConfirmedQuota(int v) { confirmedQuota = v; }
    public int getConsumedQuota() { return consumedQuota; }
    public void setConsumedQuota(int v) { consumedQuota = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }
}
