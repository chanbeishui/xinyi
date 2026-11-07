package com.xinyi.rongli.qualification.domain;

import com.xinyi.rongli.core.ViRecord;

/** 资格申请工作版本；与已生效的司机档案分离，不作为HTTP输入绑定。 */
public class QualificationApplication extends ViRecord {
    private Long driverId;
    private Long previousApplicationId;
    private Long ownerDeptId;
    private String status;
    private byte[] sensitivePayloadCipher;
    private String evidenceFileIds;
    private String providerResultIds;
    private long materialRevision;
    private byte[] materialHash;
    private long submissionRevision;
    private String consentVersion;
    private String consentText;
    private java.time.Instant consentAcceptedAt;
    private String rejectFields;
    private String publicReviewOpinion;
    private java.time.Instant submittedAt;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long value) {
        driverId = value;
    }

    public Long getPreviousApplicationId() {
        return previousApplicationId;
    }

    public void setPreviousApplicationId(Long value) {
        previousApplicationId = value;
    }

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
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

    public long getSubmissionRevision() {
        return submissionRevision;
    }

    public void setSubmissionRevision(long value) {
        submissionRevision = value;
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

    public String getRejectFields() {
        return rejectFields;
    }

    public void setRejectFields(String value) {
        rejectFields = value;
    }

    public String getPublicReviewOpinion() {
        return publicReviewOpinion;
    }

    public void setPublicReviewOpinion(String value) {
        publicReviewOpinion = value;
    }

    public java.time.Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(java.time.Instant value) {
        submittedAt = value;
    }
}
