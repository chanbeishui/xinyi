package com.xinyi.rongli.identity.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_phone_verification_receipt 手机号授权处理与重试回执（CONTROLLED_LIFECYCLE）。
 * 只存凭据摘要 code_hash（绝不存原始 phoneCode）；uk_phone_request(account_id,request_id) 去重、
 * uk_phone_code(app_id,code_hash) 防跨账号重放。status 状态机 PROCESSING→VERIFIED/UNKNOWN/FAILED；
 * 供应商调用在短事务/锁外，成功后原子保存手机号事实；employee_link_outcome 为内部原因不直接下发。
 * lease_until 为处理租约（处理中不盲目重复消费凭据）；expires_at 为关联重试窗口（默认10分钟技术值）。
 */
public class ViPhoneVerificationReceipt extends ViRecord {
    private String requestId;
    private String appId;
    private Long accountId;
    private Long sessionId;
    private byte[] codeHash;
    private String status;
    private byte[] phoneLookup;
    private byte[] phoneCipher;
    private Instant verifiedAt;
    private Instant expiresAt;
    private String employeeLinkOutcome;
    private Instant leaseUntil;
    private Instant finishedAt;

    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public String getAppId() { return appId; }
    public void setAppId(String v) { this.appId = v; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long v) { this.accountId = v; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public byte[] getCodeHash() { return codeHash; }
    public void setCodeHash(byte[] v) { this.codeHash = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public byte[] getPhoneLookup() { return phoneLookup; }
    public void setPhoneLookup(byte[] v) { this.phoneLookup = v; }
    public byte[] getPhoneCipher() { return phoneCipher; }
    public void setPhoneCipher(byte[] v) { this.phoneCipher = v; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant v) { this.verifiedAt = v; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant v) { this.expiresAt = v; }
    public String getEmployeeLinkOutcome() { return employeeLinkOutcome; }
    public void setEmployeeLinkOutcome(String v) { this.employeeLinkOutcome = v; }
    public Instant getLeaseUntil() { return leaseUntil; }
    public void setLeaseUntil(Instant v) { this.leaseUntil = v; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant v) { this.finishedAt = v; }
}
