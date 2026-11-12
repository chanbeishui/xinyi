package com.xinyi.rongli.dispatch.domain;

/** 听单与最近设备上下文的内部持久化事实；不直接绑定HTTP，也不输出位置密文。 */
public class DriverPresence {
    private long driverId;
    private long ownerDeptId;
    private long companyRevision;
    private long sessionId;
    private long contextVersion;
    private String clientState;
    private java.time.Instant observedAt;
    private java.time.Instant receivedAt;
    private java.time.Instant validUntil;
    private long configVersion;
    private long version;

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long value) {
        driverId = value;
    }

    public long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(long value) {
        ownerDeptId = value;
    }

    public long getCompanyRevision() {
        return companyRevision;
    }

    public void setCompanyRevision(long value) {
        companyRevision = value;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long value) {
        sessionId = value;
    }

    public long getContextVersion() {
        return contextVersion;
    }

    public void setContextVersion(long value) {
        contextVersion = value;
    }

    public String getClientState() {
        return clientState;
    }

    public void setClientState(String value) {
        clientState = value;
    }

    public java.time.Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(java.time.Instant value) {
        observedAt = value;
    }

    public java.time.Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(java.time.Instant value) {
        receivedAt = value;
    }

    public java.time.Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(java.time.Instant value) {
        validUntil = value;
    }

    public long getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(long value) {
        configVersion = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        version = value;
    }
}
