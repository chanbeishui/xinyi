package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingExamAttempt extends ViRecord {
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

    private String courseKey;

    public String getCourseKey() {
        return courseKey;
    }

    public void setCourseKey(String value) {
        courseKey = value;
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

    private int attemptNo;

    public int getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(int value) {
        attemptNo = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private String questionSnapshot;

    public String getQuestionSnapshot() {
        return questionSnapshot;
    }

    public void setQuestionSnapshot(String value) {
        questionSnapshot = value;
    }

    private String answerSnapshot;

    public String getAnswerSnapshot() {
        return answerSnapshot;
    }

    public void setAnswerSnapshot(String value) {
        answerSnapshot = value;
    }

    private byte[] gradingSnapshotCipher;

    public byte[] getGradingSnapshotCipher() {
        return gradingSnapshotCipher;
    }

    public void setGradingSnapshotCipher(byte[] value) {
        gradingSnapshotCipher = value;
    }

    private byte[] submissionMac;

    public byte[] getSubmissionMac() {
        return submissionMac;
    }

    public void setSubmissionMac(byte[] value) {
        submissionMac = value;
    }

    private Integer score;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer value) {
        score = value;
    }

    private Instant startedAt;

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant value) {
        startedAt = value;
    }

    private Instant expiresAt;

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant value) {
        expiresAt = value;
    }

    private Instant submittedAt;

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant value) {
        submittedAt = value;
    }
}
