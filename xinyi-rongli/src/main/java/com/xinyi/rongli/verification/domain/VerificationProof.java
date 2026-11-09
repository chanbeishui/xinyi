package com.xinyi.rongli.verification.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 当前上下文的核验事实投影；canonical密文及真实父记录共同证明来源，不以provider名称当权限。 */
public class VerificationProof extends ViRecord {
    private Long applicationId;
    private Long ownerDeptId;
    private long companyRevision;
    private long materialRevision;
    private byte[] materialHash;
    private String provider;
    private String taskType;
    private String providerTaskId;
    private String status;
    private String resultDigest;
    private Instant verifiedAt;
    private String sourceKind;
    private Long sourceRecordId;
    private Long sourceSessionId;

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long value) {
        sourceSessionId = value;
    }

    private byte[] canonicalResultCipher;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long value) {
        applicationId = value;
    }

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        provider = value;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String value) {
        taskType = value;
    }

    public String getProviderTaskId() {
        return providerTaskId;
    }

    public void setProviderTaskId(String value) {
        providerTaskId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String getResultDigest() {
        return resultDigest;
    }

    public void setResultDigest(String value) {
        resultDigest = value;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant value) {
        verifiedAt = value;
    }

    public String getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(String value) {
        sourceKind = value;
    }

    public Long getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(Long value) {
        sourceRecordId = value;
    }

    public byte[] getCanonicalResultCipher() {
        return canonicalResultCipher;
    }

    public void setCanonicalResultCipher(byte[] value) {
        canonicalResultCipher = value;
    }
}
