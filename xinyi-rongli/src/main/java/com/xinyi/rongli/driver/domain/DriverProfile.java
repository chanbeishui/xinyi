package com.xinyi.rongli.driver.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 代驾档案持久化投影，不能作为请求体批量绑定资格和开关。 */
public class DriverProfile extends ViRecord {
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        accountId = value;
    }

    private Long ownerDeptId;

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    private long trainingContextRevision;

    public long getTrainingContextRevision() {
        return trainingContextRevision;
    }

    public void setTrainingContextRevision(long value) {
        trainingContextRevision = value;
    }

    private String qualificationStatus;

    public String getQualificationStatus() {
        return qualificationStatus;
    }

    public void setQualificationStatus(String value) {
        qualificationStatus = value;
    }

    private boolean newAssignmentEnabled;

    public boolean getNewAssignmentEnabled() {
        return newAssignmentEnabled;
    }

    public void setNewAssignmentEnabled(boolean value) {
        newAssignmentEnabled = value;
    }

    private boolean listeningEnabled;

    public boolean getListeningEnabled() {
        return listeningEnabled;
    }

    public void setListeningEnabled(boolean value) {
        listeningEnabled = value;
    }

    private Instant reviewDueAt;

    public Instant getReviewDueAt() {
        return reviewDueAt;
    }

    public void setReviewDueAt(Instant value) {
        reviewDueAt = value;
    }

    private String licenseClass;

    public String getLicenseClass() {
        return licenseClass;
    }

    public void setLicenseClass(String value) {
        licenseClass = value;
    }

    private Instant licenseValidTo;

    public Instant getLicenseValidTo() {
        return licenseValidTo;
    }

    public void setLicenseValidTo(Instant value) {
        licenseValidTo = value;
    }

    private boolean licenseLongTerm;

    public boolean getLicenseLongTerm() {
        return licenseLongTerm;
    }

    public void setLicenseLongTerm(boolean value) {
        licenseLongTerm = value;
    }

    private Long qualificationRevision;

    public Long getQualificationRevision() {
        return qualificationRevision;
    }

    public void setQualificationRevision(Long value) {
        qualificationRevision = value;
    }

    private String internalRemark;

    public String getInternalRemark() {
        return internalRemark;
    }

    public void setInternalRemark(String value) {
        internalRemark = value;
    }

    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        displayName = value;
    }

    private byte[] phoneCipher;

    public byte[] getPhoneCipher() {
        return phoneCipher;
    }

    public void setPhoneCipher(byte[] value) {
        phoneCipher = value;
    }

    private String accountStatus;

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String value) {
        accountStatus = value;
    }

    private String securityStatus;

    public String getSecurityStatus() {
        return securityStatus;
    }

    public void setSecurityStatus(String value) {
        securityStatus = value;
    }

    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String value) {
        companyName = value;
    }

    private Long effectiveSnapshotId;

    public Long getEffectiveSnapshotId() {
        return effectiveSnapshotId;
    }

    public void setEffectiveSnapshotId(Long value) {
        effectiveSnapshotId = value;
    }

    private Long qualificationOwnerDeptId;

    public Long getQualificationOwnerDeptId() {
        return qualificationOwnerDeptId;
    }

    public void setQualificationOwnerDeptId(Long value) {
        qualificationOwnerDeptId = value;
    }

    private Long qualificationCompanyRevision;

    public Long getQualificationCompanyRevision() {
        return qualificationCompanyRevision;
    }

    public void setQualificationCompanyRevision(Long value) {
        qualificationCompanyRevision = value;
    }

    private long listeningRevision;

    public long getListeningRevision() {
        return listeningRevision;
    }

    public void setListeningRevision(long value) {
        listeningRevision = value;
    }

    private Instant listeningUntil;

    public Instant getListeningUntil() {
        return listeningUntil;
    }

    public void setListeningUntil(Instant value) {
        listeningUntil = value;
    }

    private Long listeningCompanyRevision;

    public Long getListeningCompanyRevision() {
        return listeningCompanyRevision;
    }

    public void setListeningCompanyRevision(Long value) {
        listeningCompanyRevision = value;
    }

    private Long listeningConfigVersion;

    public Long getListeningConfigVersion() {
        return listeningConfigVersion;
    }

    public void setListeningConfigVersion(Long value) {
        listeningConfigVersion = value;
    }
}
