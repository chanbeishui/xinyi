package com.xinyi.rongli.scope.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_station_grant_set 员工站点授权集合（I06）。uk_station_grant_employee(sys_user_id) 唯一；
 * version 为集合 CAS 版本，含空集合（删空不重置，防 ABA）；不要求 vi_account/OpenID（员工可未微信登录）。
 */
public class ViStationGrantSet extends ViRecord {
    private Long sysUserId;
    private Long ownStationId;
    private Long changedBy;
    private String reason;

    public Long getSysUserId() { return sysUserId; }
    public void setSysUserId(Long v) { this.sysUserId = v; }
    public Long getOwnStationId() { return ownStationId; }
    public void setOwnStationId(Long v) { this.ownStationId = v; }
    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long v) { this.changedBy = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { this.reason = v; }
}