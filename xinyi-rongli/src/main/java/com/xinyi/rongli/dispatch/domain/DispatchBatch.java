package com.xinyi.rongli.dispatch.domain;

/** 派单内部持久事实；HTTP仅使用专用白名单投影，不批量绑定实体。 */
public class DispatchBatch {
    private Long id;
    private Long orderId;
    private int runNo;
    private Long serviceCompanyId;
    private String source;
    private String status;
    private long policyVersion;
    private String policySnapshot;
    private java.time.Instant startedAt;
    private java.time.Instant deadlineAt;
    private int radiusStage;
    private String reasonCode;
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

    public int getRunNo() {
        return runNo;
    }

    public void setRunNo(int value) {
        runNo = value;
    }

    public Long getServiceCompanyId() {
        return serviceCompanyId;
    }

    public void setServiceCompanyId(Long value) {
        serviceCompanyId = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String value) {
        source = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public long getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(long value) {
        policyVersion = value;
    }

    public String getPolicySnapshot() {
        return policySnapshot;
    }

    public void setPolicySnapshot(String value) {
        policySnapshot = value;
    }

    public java.time.Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(java.time.Instant value) {
        startedAt = value;
    }

    public java.time.Instant getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(java.time.Instant value) {
        deadlineAt = value;
    }

    public int getRadiusStage() {
        return radiusStage;
    }

    public void setRadiusStage(int value) {
        radiusStage = value;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String value) {
        reasonCode = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        version = value;
    }
}
