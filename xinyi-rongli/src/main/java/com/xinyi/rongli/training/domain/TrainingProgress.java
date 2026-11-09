package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingProgress extends ViRecord {
    private Long runId;

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long value) {
        runId = value;
    }

    private Long applicationId;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long value) {
        applicationId = value;
    }

    private Long driverId;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long value) {
        driverId = value;
    }

    private Long trainingId;

    public Long getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(Long value) {
        trainingId = value;
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

    private long verifiedDurationMillis;

    public long getVerifiedDurationMillis() {
        return verifiedDurationMillis;
    }

    public void setVerifiedDurationMillis(long value) {
        verifiedDurationMillis = value;
    }

    private int verifiedDurationSeconds;

    public int getVerifiedDurationSeconds() {
        return verifiedDurationSeconds;
    }

    public void setVerifiedDurationSeconds(int value) {
        verifiedDurationSeconds = value;
    }

    private String verifiedSegments;

    public String getVerifiedSegments() {
        return verifiedSegments;
    }

    public void setVerifiedSegments(String value) {
        verifiedSegments = value;
    }

    private Long activePlaybackId;

    public Long getActivePlaybackId() {
        return activePlaybackId;
    }

    public void setActivePlaybackId(Long value) {
        activePlaybackId = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private int lastPositionSeconds;

    public int getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(int value) {
        lastPositionSeconds = value;
    }
}
