package com.xinyi.rongli.dispatch.domain;

/** 派单内部持久事实；HTTP仅使用专用白名单投影，不批量绑定实体。 */
public class DispatchInvite {
    private Long id;
    private Long orderId;
    private Long driverId;
    private String status;
    private String source;
    private String roundKey;
    private java.time.Instant issuedAt;
    private java.time.Instant expiresAt;
    private java.time.Instant respondedAt;
    private Long batchId;
    private Long serviceCompanyId;
    private Long companyRevision;
    private Integer distanceMeter;
    private Long driverSessionId;
    private Long driverContextVersion;
    private Long driverLocationVersion;
    private Long driverPresenceVersion;
    private Long issuedByUserId;
    private byte[] reasonCipher;
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long value) {
        orderId = value;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long value) {
        driverId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String value) {
        source = value;
    }

    public String getRoundKey() {
        return roundKey;
    }

    public void setRoundKey(String value) {
        roundKey = value;
    }

    public java.time.Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(java.time.Instant value) {
        issuedAt = value;
    }

    public java.time.Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(java.time.Instant value) {
        expiresAt = value;
    }

    public java.time.Instant getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(java.time.Instant value) {
        respondedAt = value;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long value) {
        batchId = value;
    }

    public Long getServiceCompanyId() {
        return serviceCompanyId;
    }

    public void setServiceCompanyId(Long value) {
        serviceCompanyId = value;
    }

    public Long getCompanyRevision() {
        return companyRevision;
    }

    public void setCompanyRevision(Long value) {
        companyRevision = value;
    }

    public Integer getDistanceMeter() {
        return distanceMeter;
    }

    public void setDistanceMeter(Integer value) {
        distanceMeter = value;
    }

    public Long getDriverSessionId() {
        return driverSessionId;
    }

    public void setDriverSessionId(Long value) {
        driverSessionId = value;
    }

    public Long getDriverContextVersion() {
        return driverContextVersion;
    }

    public void setDriverContextVersion(Long value) {
        driverContextVersion = value;
    }

    public Long getDriverLocationVersion() {
        return driverLocationVersion;
    }

    public void setDriverLocationVersion(Long value) {
        driverLocationVersion = value;
    }

    public Long getDriverPresenceVersion() {
        return driverPresenceVersion;
    }

    public void setDriverPresenceVersion(Long value) {
        driverPresenceVersion = value;
    }

    public Long getIssuedByUserId() {
        return issuedByUserId;
    }

    public void setIssuedByUserId(Long value) {
        issuedByUserId = value;
    }

    public byte[] getReasonCipher() {
        return reasonCipher;
    }

    public void setReasonCipher(byte[] value) {
        reasonCipher = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        version = value;
    }
}
