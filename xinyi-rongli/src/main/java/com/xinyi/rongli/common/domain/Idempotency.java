package com.xinyi.rongli.common.domain;

import java.time.LocalDateTime;

/**
 * 请求级幂等
 *
 * <p>对应表 vi_idempotency，由数据库结构生成，字段与列一一对应。</p>
 */
public class Idempotency
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 统一主体命名空间 */
    private String subjectKey;

    /** HTTP动作+资源 */
    private String actionKey;

    /** UUID幂等头 */
    private String requestKey;

    /** 请求摘要 */
    private byte[] requestMac;

    /** 成功结果或可重放结果 */
    private String resultJson;

    /** IdempotencyStatus: PROCESSING,DONE */
    private String status;

    /** 至少24h，请求级保存时限 */
    private LocalDateTime expiresAt;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getSubjectKey()
    {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey)
    {
        this.subjectKey = subjectKey;
    }

    public String getActionKey()
    {
        return actionKey;
    }

    public void setActionKey(String actionKey)
    {
        this.actionKey = actionKey;
    }

    public String getRequestKey()
    {
        return requestKey;
    }

    public void setRequestKey(String requestKey)
    {
        this.requestKey = requestKey;
    }

    public byte[] getRequestMac()
    {
        return requestMac;
    }

    public void setRequestMac(byte[] requestMac)
    {
        this.requestMac = requestMac;
    }

    public String getResultJson()
    {
        return resultJson;
    }

    public void setResultJson(String resultJson)
    {
        this.resultJson = resultJson;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt)
    {
        this.updatedAt = updatedAt;
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
