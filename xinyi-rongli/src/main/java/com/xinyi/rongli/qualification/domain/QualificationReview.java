package com.xinyi.rongli.qualification.domain;

/** 资格审批内部持久化事实；不直接绑定HTTP输入，不把密文或内部意见序列化到响应。 */
public class QualificationReview {
    private Long id;
    private long applicationId;
    private long driverId;
    private long ownerDeptId;
    private long companyRevision;
    private Long submissionId;
    private long submissionRevision;
    private String kind;
    private String decision;
    private long reviewerUserId;
    private java.time.Instant reviewedAt;
    private byte[] internalOpinionCipher;
    private String publicOpinion;
    private String rejectFields;
    private long applicationVersion;
    private long profileVersion;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long value) {
        applicationId = value;
    }

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

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long value) {
        submissionId = value;
    }

    public long getSubmissionRevision() {
        return submissionRevision;
    }

    public void setSubmissionRevision(long value) {
        submissionRevision = value;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String value) {
        kind = value;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String value) {
        decision = value;
    }

    public long getReviewerUserId() {
        return reviewerUserId;
    }

    public void setReviewerUserId(long value) {
        reviewerUserId = value;
    }

    public java.time.Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(java.time.Instant value) {
        reviewedAt = value;
    }

    public byte[] getInternalOpinionCipher() {
        return internalOpinionCipher;
    }

    public void setInternalOpinionCipher(byte[] value) {
        internalOpinionCipher = value;
    }

    public String getPublicOpinion() {
        return publicOpinion;
    }

    public void setPublicOpinion(String value) {
        publicOpinion = value;
    }

    public String getRejectFields() {
        return rejectFields;
    }

    public void setRejectFields(String value) {
        rejectFields = value;
    }

    public long getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(long value) {
        applicationVersion = value;
    }

    public long getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(long value) {
        profileVersion = value;
    }
}
