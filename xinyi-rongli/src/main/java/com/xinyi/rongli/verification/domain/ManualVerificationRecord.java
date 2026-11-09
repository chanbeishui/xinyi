package com.xinyi.rongli.verification.domain;

import com.xinyi.rongli.core.ViRecord;

/** 人工经办及独立复核的单次事实；HTTP只绑定专用DTO，不能覆盖公司、操作者或最终证明。 */
public class ManualVerificationRecord extends ViRecord {
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

    private String verificationType;

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String value) {
        verificationType = value;
    }

    private String conclusion;

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String value) {
        conclusion = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private Long submittedByUserId;

    public Long getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(Long value) {
        submittedByUserId = value;
    }

    private Long reviewedByUserId;

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(Long value) {
        reviewedByUserId = value;
    }

    private java.time.Instant submittedAt;

    public java.time.Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(java.time.Instant value) {
        submittedAt = value;
    }

    private java.time.Instant reviewedAt;

    public java.time.Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(java.time.Instant value) {
        reviewedAt = value;
    }

    private byte[] noteCipher;

    public byte[] getNoteCipher() {
        return noteCipher;
    }

    public void setNoteCipher(byte[] value) {
        noteCipher = value;
    }

    private byte[] reviewNoteCipher;

    public byte[] getReviewNoteCipher() {
        return reviewNoteCipher;
    }

    public void setReviewNoteCipher(byte[] value) {
        reviewNoteCipher = value;
    }

    private Long proofId;

    public Long getProofId() {
        return proofId;
    }

    public void setProofId(Long value) {
        proofId = value;
    }

    private Long driverId;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long value) {
        driverId = value;
    }

    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        accountId = value;
    }

    private String applicantName;

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String value) {
        applicantName = value;
    }

    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String value) {
        companyName = value;
    }

    private String submittedByName;

    public String getSubmittedByName() {
        return submittedByName;
    }

    public void setSubmittedByName(String value) {
        submittedByName = value;
    }

    private String reviewedByName;

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String value) {
        reviewedByName = value;
    }

    private String applicantUserIds;

    public String getApplicantUserIds() {
        return applicantUserIds;
    }

    public void setApplicantUserIds(String value) {
        applicantUserIds = value;
    }
}
