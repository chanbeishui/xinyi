package com.xinyi.rongli.qualification.domain;

/** 资格审批内部持久化事实；不直接绑定HTTP输入，不把密文或内部意见序列化到响应。 */
public class QualificationSubmission {
    private Long id;
    private Long applicationId;
    private long submissionRevision;
    private Long ownerDeptId;
    private long profileVersion;
    private Long companyRevision;
    private Long sourceSessionId;
    private String applicantUserIds;
    private long materialRevision;
    private byte[] materialHash;
    private byte[] sensitivePayloadCipher;
    private String evidenceFileIds;
    private String providerResultIds;
    private String consentVersion;
    private String consentText;
    private java.time.Instant consentAcceptedAt;
    private byte[] submitReasonCipher;
    private java.time.Instant submittedAt;
    private byte[] submissionSeal;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long value) {
        applicationId = value;
    }

    public long getSubmissionRevision() {
        return submissionRevision;
    }

    public void setSubmissionRevision(long value) {
        submissionRevision = value;
    }

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    public long getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(long value) {
        profileVersion = value;
    }

    public Long getCompanyRevision() {
        return companyRevision;
    }

    public void setCompanyRevision(Long value) {
        companyRevision = value;
    }

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long value) {
        sourceSessionId = value;
    }

    public String getApplicantUserIds() {
        return applicantUserIds;
    }

    public void setApplicantUserIds(String value) {
        applicantUserIds = value;
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

    public byte[] getSensitivePayloadCipher() {
        return sensitivePayloadCipher;
    }

    public void setSensitivePayloadCipher(byte[] value) {
        sensitivePayloadCipher = value;
    }

    public String getEvidenceFileIds() {
        return evidenceFileIds;
    }

    public void setEvidenceFileIds(String value) {
        evidenceFileIds = value;
    }

    public String getProviderResultIds() {
        return providerResultIds;
    }

    public void setProviderResultIds(String value) {
        providerResultIds = value;
    }

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String value) {
        consentVersion = value;
    }

    public String getConsentText() {
        return consentText;
    }

    public void setConsentText(String value) {
        consentText = value;
    }

    public java.time.Instant getConsentAcceptedAt() {
        return consentAcceptedAt;
    }

    public void setConsentAcceptedAt(java.time.Instant value) {
        consentAcceptedAt = value;
    }

    public byte[] getSubmitReasonCipher() {
        return submitReasonCipher;
    }

    public void setSubmitReasonCipher(byte[] value) {
        submitReasonCipher = value;
    }

    public java.time.Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(java.time.Instant value) {
        submittedAt = value;
    }

    public byte[] getSubmissionSeal() {
        return submissionSeal;
    }

    public void setSubmissionSeal(byte[] value) {
        submissionSeal = value;
    }
}
