package com.xinyi.rongli.dispatch.domain;

/** 听单与最近设备上下文的内部持久化事实；不直接绑定HTTP，也不输出位置密文。 */
public class DriverLocation {
    private long driverId;
    private byte[] locationCipher;
    private java.time.Instant capturedAt;
    private java.time.Instant receivedAt;
    private Long ownerDeptId;
    private Long companyRevision;
    private Long sessionId;
    private Long contextVersion;
    private Integer accuracyMeter;
    private java.time.Instant validUntil;
    private Long configVersion;
    private long version;

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long value) {
        driverId = value;
    }

    public byte[] getLocationCipher() {
        return locationCipher;
    }

    public void setLocationCipher(byte[] value) {
        locationCipher = value;
    }

    public java.time.Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(java.time.Instant value) {
        capturedAt = value;
    }

    public java.time.Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(java.time.Instant value) {
        receivedAt = value;
    }

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    public Long getCompanyRevision() {
        return companyRevision;
    }

    public void setCompanyRevision(Long value) {
        companyRevision = value;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long value) {
        sessionId = value;
    }

    public Long getContextVersion() {
        return contextVersion;
    }

    public void setContextVersion(Long value) {
        contextVersion = value;
    }

    public Integer getAccuracyMeter() {
        return accuracyMeter;
    }

    public void setAccuracyMeter(Integer value) {
        accuracyMeter = value;
    }

    public java.time.Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(java.time.Instant value) {
        validUntil = value;
    }

    public Long getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(Long value) {
        configVersion = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        version = value;
    }
}
