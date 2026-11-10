package com.xinyi.rongli.qualification.domain;

/** 资格审批内部持久化事实；不直接绑定HTTP输入，不把密文或内部意见序列化到响应。 */
public class QualificationSnapshot {
    private Long id;
    private long driverId;
    private long accountId;
    private long applicationId;
    private long submissionId;
    private long reviewId;
    private long qualificationRevision;
    private long ownerDeptId;
    private long companyRevision;
    private long materialRevision;
    private byte[] materialHash;
    private String licenseClass;
    private java.time.Instant licenseValidTo;
    private boolean licenseLongTerm;
    private java.time.Instant reviewDueAt;
    private java.time.Instant expiresAt;
    private java.time.Instant approvedAt;
    private byte[] snapshotSeal;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long value) {
        driverId = value;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long value) {
        accountId = value;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long value) {
        applicationId = value;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long value) {
        submissionId = value;
    }

    public long getReviewId() {
        return reviewId;
    }

    public void setReviewId(long value) {
        reviewId = value;
    }

    public long getQualificationRevision() {
        return qualificationRevision;
    }

    public void setQualificationRevision(long value) {
        qualificationRevision = value;
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

    public long getMaterialRevision() {
        return materialRevision;
    }

    public void setMaterialRevision(long value) {
        materialRevision = value;
    }

    public byte[] getMaterialHash() {
        return materialHash;
    }

    public void setMaterialHash(byte[] value) {
        materialHash = value;
    }

    public String getLicenseClass() {
        return licenseClass;
    }

    public void setLicenseClass(String value) {
        licenseClass = value;
    }

    public java.time.Instant getLicenseValidTo() {
        return licenseValidTo;
    }

    public void setLicenseValidTo(java.time.Instant value) {
        licenseValidTo = value;
    }

    public boolean getLicenseLongTerm() {
        return licenseLongTerm;
    }

    public void setLicenseLongTerm(boolean value) {
        licenseLongTerm = value;
    }

    public java.time.Instant getReviewDueAt() {
        return reviewDueAt;
    }

    public void setReviewDueAt(java.time.Instant value) {
        reviewDueAt = value;
    }

    public java.time.Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(java.time.Instant value) {
        expiresAt = value;
    }

    public java.time.Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(java.time.Instant value) {
        approvedAt = value;
    }

    public byte[] getSnapshotSeal() {
        return snapshotSeal;
    }

    public void setSnapshotSeal(byte[] value) {
        snapshotSeal = value;
    }
}
