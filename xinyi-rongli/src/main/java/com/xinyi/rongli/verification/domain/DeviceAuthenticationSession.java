package com.xinyi.rongli.verification.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 本机设备生物认证挑战，不代表实名或人脸活体；旧物理表名vi_face_session仅作迁移兼容。 */
public class DeviceAuthenticationSession extends ViRecord {
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

    private Long companyRevision;

    public Long getCompanyRevision() {
        return companyRevision;
    }

    public void setCompanyRevision(Long value) {
        companyRevision = value;
    }

    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long value) {
        accountId = value;
    }

    private Long materialRevision;

    public Long getMaterialRevision() {
        return materialRevision;
    }

    public void setMaterialRevision(Long value) {
        materialRevision = value;
    }

    private byte[] materialHash;

    public byte[] getMaterialHash() {
        return materialHash;
    }

    public void setMaterialHash(byte[] value) {
        materialHash = value;
    }

    private Long verificationId;

    public Long getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(Long value) {
        verificationId = value;
    }

    private Long sessionId;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long value) {
        sessionId = value;
    }

    private Long contextVersion;

    public Long getContextVersion() {
        return contextVersion;
    }

    public void setContextVersion(Long value) {
        contextVersion = value;
    }

    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String value) {
        appId = value;
    }

    private byte[] openIdMac;

    public byte[] getOpenIdMac() {
        return openIdMac;
    }

    public void setOpenIdMac(byte[] value) {
        openIdMac = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private String launchMode;

    public String getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(String value) {
        launchMode = value;
    }

    private String consentVersion;

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String value) {
        consentVersion = value;
    }

    private String consentTextHash;

    public String getConsentTextHash() {
        return consentTextHash;
    }

    public void setConsentTextHash(String value) {
        consentTextHash = value;
    }

    private String consentText;

    public String getConsentText() {
        return consentText;
    }

    public void setConsentText(String value) {
        consentText = value;
    }

    private Instant consentAcceptedAt;

    public Instant getConsentAcceptedAt() {
        return consentAcceptedAt;
    }

    public void setConsentAcceptedAt(Instant value) {
        consentAcceptedAt = value;
    }

    private Instant expiresAt;

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant value) {
        expiresAt = value;
    }

    private boolean resultAvailable;

    public boolean getResultAvailable() {
        return resultAvailable;
    }

    public void setResultAvailable(boolean value) {
        resultAvailable = value;
    }

    private String failureCode;

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String value) {
        failureCode = value;
    }

    private String requestNo;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String value) {
        requestNo = value;
    }

    private byte[] requestMac;

    public byte[] getRequestMac() {
        return requestMac;
    }

    public void setRequestMac(byte[] value) {
        requestMac = value;
    }

    private Long configProfileId;

    public Long getConfigProfileId() {
        return configProfileId;
    }

    public void setConfigProfileId(Long value) {
        configProfileId = value;
    }

    private Long configRevisionId;

    public Long getConfigRevisionId() {
        return configRevisionId;
    }

    public void setConfigRevisionId(Long value) {
        configRevisionId = value;
    }

    private String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        provider = value;
    }

    private String product;

    public String getProduct() {
        return product;
    }

    public void setProduct(String value) {
        product = value;
    }

    private String providerIdentityDigest;

    public String getProviderIdentityDigest() {
        return providerIdentityDigest;
    }

    public void setProviderIdentityDigest(String value) {
        providerIdentityDigest = value;
    }

    private String configSnapshot;

    public String getConfigSnapshot() {
        return configSnapshot;
    }

    public void setConfigSnapshot(String value) {
        configSnapshot = value;
    }

    private byte[] challengeCipher;

    public byte[] getChallengeCipher() {
        return challengeCipher;
    }

    public void setChallengeCipher(byte[] value) {
        challengeCipher = value;
    }

    private byte[] challengeMac;

    public byte[] getChallengeMac() {
        return challengeMac;
    }

    public void setChallengeMac(byte[] value) {
        challengeMac = value;
    }

    private byte[] submissionCipher;

    public byte[] getSubmissionCipher() {
        return submissionCipher;
    }

    public void setSubmissionCipher(byte[] value) {
        submissionCipher = value;
    }

    private byte[] submissionMac;

    public byte[] getSubmissionMac() {
        return submissionMac;
    }

    public void setSubmissionMac(byte[] value) {
        submissionMac = value;
    }

    private Instant submittedAt;

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant value) {
        submittedAt = value;
    }

    private Instant executionStartedAt;

    public Instant getExecutionStartedAt() {
        return executionStartedAt;
    }

    public void setExecutionStartedAt(Instant value) {
        executionStartedAt = value;
    }

    private String leaseToken;

    public String getLeaseToken() {
        return leaseToken;
    }

    public void setLeaseToken(String value) {
        leaseToken = value;
    }

    private Instant leaseUntil;

    public Instant getLeaseUntil() {
        return leaseUntil;
    }

    public void setLeaseUntil(Instant value) {
        leaseUntil = value;
    }

    private Instant verifiedAt;

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant value) {
        verifiedAt = value;
    }

    private String resultDigest;

    public String getResultDigest() {
        return resultDigest;
    }

    public void setResultDigest(String value) {
        resultDigest = value;
    }
}
