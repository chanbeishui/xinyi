package com.xinyi.rongli.identity.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_session_token 访问与刷新令牌摘要。token_hash 为安全随机令牌的摘要（BINARY(32)，
 * 唯一键 uk_token_hash），绝不存明文。generation 为刷新代际，consumed_at 用于刷新原子
 * 消费（重复旧刷新令牌拒绝并撤销会话族，见 I03）。表无 updated_at 列。
 */
public class ViSessionToken extends ViRecord {
    private Long sessionId;
    private byte[] tokenHash;
    private String tokenType;
    private Long generation;
    private Long contextVersion;
    private Long parentTokenId;
    private Long replacedByTokenId;
    private Instant expiresAt;
    private Instant consumedAt;
    private Instant revokedAt;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long v) { this.sessionId = v; }
    public byte[] getTokenHash() { return tokenHash; }
    public void setTokenHash(byte[] v) { this.tokenHash = v; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String v) { this.tokenType = v; }
    public Long getGeneration() { return generation; }
    public void setGeneration(Long v) { this.generation = v; }
    public Long getContextVersion() { return contextVersion; }
    public void setContextVersion(Long v) { this.contextVersion = v; }
    public Long getParentTokenId() { return parentTokenId; }
    public void setParentTokenId(Long v) { this.parentTokenId = v; }
    public Long getReplacedByTokenId() { return replacedByTokenId; }
    public void setReplacedByTokenId(Long v) { this.replacedByTokenId = v; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant v) { this.expiresAt = v; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant v) { this.consumedAt = v; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant v) { this.revokedAt = v; }
}
