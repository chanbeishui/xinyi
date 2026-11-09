package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingRun extends ViRecord {
    private Long planSnapshotId;

    public Long getPlanSnapshotId() {
        return planSnapshotId;
    }

    public void setPlanSnapshotId(Long value) {
        planSnapshotId = value;
    }

    private Long applicationId;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long value) {
        applicationId = value;
    }

    private long materialRevision;

    public long getMaterialRevision() {
        return materialRevision;
    }

    public void setMaterialRevision(long value) {
        materialRevision = value;
    }

    private byte[] materialHash;

    public byte[] getMaterialHash() {
        return materialHash;
    }

    public void setMaterialHash(byte[] value) {
        materialHash = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private Long providerVerificationId;

    public Long getProviderVerificationId() {
        return providerVerificationId;
    }

    public void setProviderVerificationId(Long value) {
        providerVerificationId = value;
    }

    private Instant completedAt;

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant value) {
        completedAt = value;
    }
}
