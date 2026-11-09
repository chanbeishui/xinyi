package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingPolicyRevision extends ViRecord {
    private Long policyId;

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long value) {
        policyId = value;
    }

    private int revisionNo;

    public int getRevisionNo() {
        return revisionNo;
    }

    public void setRevisionNo(int value) {
        revisionNo = value;
    }

    private String requiredTrainingIds;

    public String getRequiredTrainingIds() {
        return requiredTrainingIds;
    }

    public void setRequiredTrainingIds(String value) {
        requiredTrainingIds = value;
    }

    private Integer maxAttempts;

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer value) {
        maxAttempts = value;
    }

    private Integer retryIntervalSeconds;

    public Integer getRetryIntervalSeconds() {
        return retryIntervalSeconds;
    }

    public void setRetryIntervalSeconds(Integer value) {
        retryIntervalSeconds = value;
    }

    private String gradingMode;

    public String getGradingMode() {
        return gradingMode;
    }

    public void setGradingMode(String value) {
        gradingMode = value;
    }

    private byte[] contentHash;

    public byte[] getContentHash() {
        return contentHash;
    }

    public void setContentHash(byte[] value) {
        contentHash = value;
    }

    private Long createdByUserId;

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long value) {
        createdByUserId = value;
    }

    private Long publishedByUserId;

    public Long getPublishedByUserId() {
        return publishedByUserId;
    }

    public void setPublishedByUserId(Long value) {
        publishedByUserId = value;
    }

    private Instant publishedAt;

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant value) {
        publishedAt = value;
    }
}
