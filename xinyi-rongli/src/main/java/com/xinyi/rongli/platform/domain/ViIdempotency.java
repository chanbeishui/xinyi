package com.xinyi.rongli.platform.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_idempotency 请求级幂等记录。唯一键 uk_request(subject_key,action_key,request_key)。
 * request_mac 为请求体摘要（HMAC-SHA256，32 字节）；result_json 仅在可缓存动作保存成功结果，
 * 登录/刷新等含明文令牌的动作禁止缓存（cacheResult=false）。
 */
public class ViIdempotency extends ViRecord {
    private String subjectKey;
    private String actionKey;
    private String requestKey;
    private byte[] requestMac;
    private String resultJson;
    private IdempotencyStatus status;
    private Instant expiresAt;

    public String getSubjectKey() { return subjectKey; }
    public void setSubjectKey(String v) { this.subjectKey = v; }
    public String getActionKey() { return actionKey; }
    public void setActionKey(String v) { this.actionKey = v; }
    public String getRequestKey() { return requestKey; }
    public void setRequestKey(String v) { this.requestKey = v; }
    public byte[] getRequestMac() { return requestMac; }
    public void setRequestMac(byte[] v) { this.requestMac = v; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String v) { this.resultJson = v; }
    public IdempotencyStatus getStatus() { return status; }
    public void setStatus(IdempotencyStatus v) { this.status = v; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant v) { this.expiresAt = v; }
}
