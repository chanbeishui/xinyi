package com.xinyi.rongli.dispatch.domain;

/** 听单与最近设备上下文的内部持久化事实；不直接绑定HTTP，也不输出位置密文。 */
public class ListeningCompanyConfig {
    private Long ownerDeptId;
    private String closingTime;
    private int locationMaxAgeSeconds;
    private int reachabilityMaxAgeSeconds;
    private int maxAccuracyMeter;
    private int locationReportIntervalSeconds;
    private long updatedByUserId;
    private long version;

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String value) {
        closingTime = value;
    }

    public int getLocationMaxAgeSeconds() {
        return locationMaxAgeSeconds;
    }

    public void setLocationMaxAgeSeconds(int value) {
        locationMaxAgeSeconds = value;
    }

    public int getReachabilityMaxAgeSeconds() {
        return reachabilityMaxAgeSeconds;
    }

    public void setReachabilityMaxAgeSeconds(int value) {
        reachabilityMaxAgeSeconds = value;
    }

    public int getMaxAccuracyMeter() {
        return maxAccuracyMeter;
    }

    public void setMaxAccuracyMeter(int value) {
        maxAccuracyMeter = value;
    }

    public int getLocationReportIntervalSeconds() {
        return locationReportIntervalSeconds;
    }

    public void setLocationReportIntervalSeconds(int value) {
        locationReportIntervalSeconds = value;
    }

    public long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(long value) {
        updatedByUserId = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        version = value;
    }
}
