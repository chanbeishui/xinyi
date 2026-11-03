package com.xinyi.rongli.auth.domain;

import java.time.LocalDateTime;

/**
 * 访问与刷新令牌摘要
 *
 * <p>对应表 vi_session_token，由数据库结构生成，字段与列一一对应。</p>
 */
public class SessionToken
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** vi_session */
    private Long sessionId;

    /** 安全随机令牌的摘要；不存明文 */
    private byte[] tokenHash;

    /** SessionTokenType: ACCESS,REFRESH */
    private String tokenType;

    /** 刷新令牌代际 */
    private Long generation;

    /** ACCESS绑定上下文 */
    private Long contextVersion;

    /** 刷新祖先关系 */
    private Long parentTokenId;

    /** 轮换后的令牌引用 */
    private Long replacedByTokenId;

    /** 到期 */
    private LocalDateTime expiresAt;

    /** 刷新原子消费时间 */
    private LocalDateTime consumedAt;

    /** 撤销时间 */
    private LocalDateTime revokedAt;

    /** UTC */
    private LocalDateTime createdAt;

    /** 并发控制；不可变行不更新内容 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public byte[] getTokenHash()
    {
        return tokenHash;
    }

    public void setTokenHash(byte[] tokenHash)
    {
        this.tokenHash = tokenHash;
    }

    public String getTokenType()
    {
        return tokenType;
    }

    public void setTokenType(String tokenType)
    {
        this.tokenType = tokenType;
    }

    public Long getGeneration()
    {
        return generation;
    }

    public void setGeneration(Long generation)
    {
        this.generation = generation;
    }

    public Long getContextVersion()
    {
        return contextVersion;
    }

    public void setContextVersion(Long contextVersion)
    {
        this.contextVersion = contextVersion;
    }

    public Long getParentTokenId()
    {
        return parentTokenId;
    }

    public void setParentTokenId(Long parentTokenId)
    {
        this.parentTokenId = parentTokenId;
    }

    public Long getReplacedByTokenId()
    {
        return replacedByTokenId;
    }

    public void setReplacedByTokenId(Long replacedByTokenId)
    {
        this.replacedByTokenId = replacedByTokenId;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getConsumedAt()
    {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt)
    {
        this.consumedAt = consumedAt;
    }

    public LocalDateTime getRevokedAt()
    {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt)
    {
        this.revokedAt = revokedAt;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
}
