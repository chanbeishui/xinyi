package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingPlan extends ViRecord {
    private Long applicationId;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long value) {
        applicationId = value;
    }

    private Long ownerDeptId;

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    private long companyRevision;

    public long getCompanyRevision() {
        return companyRevision;
    }

    public void setCompanyRevision(long value) {
        companyRevision = value;
    }

    private Long policyRevisionId;

    public Long getPolicyRevisionId() {
        return policyRevisionId;
    }

    public void setPolicyRevisionId(Long value) {
        policyRevisionId = value;
    }

    private String courseSnapshot;

    public String getCourseSnapshot() {
        return courseSnapshot;
    }

    public void setCourseSnapshot(String value) {
        courseSnapshot = value;
    }

    private int maxAttempts;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int value) {
        maxAttempts = value;
    }

    private int retryIntervalSeconds;

    public int getRetryIntervalSeconds() {
        return retryIntervalSeconds;
    }

    public void setRetryIntervalSeconds(int value) {
        retryIntervalSeconds = value;
    }

    private String gradingMode;

    public String getGradingMode() {
        return gradingMode;
    }

    public void setGradingMode(String value) {
        gradingMode = value;
    }
}
