package com.xinyi.rongli.fulfill.domain;

import java.time.LocalDateTime;

/**
 * 一次实物交接事务凭证
 *
 * <p>对应表 vi_handover，由数据库结构生成，字段与列一一对应。</p>
 */
public class Handover
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 当前承接 */
    private Long assignmentId;

    /** HandoverType: OWNER_TO_DRIVER,DRIVER_TO_STATION… */
    private String handoverType;

    /** 持码基础账号 */
    private Long holderAccountId;

    /** 核验基础账号 */
    private Long verifierAccountId;

    /** 含订单/类型/代次随机盐的服务端带密钥MAC；4位码不能只裸哈希 */
    private byte[] codeMac;

    /** 仅持码方短期读取所需的加密值，使用后销毁可读密文 */
    private byte[] codeCipher;

    /** 码代次 */
    private Integer generationNo;

    /** HandoverStatus: PENDING,CONFIRMED,LOCKED,EXPIRED */
    private String status;

    /** 时效 */
    private LocalDateTime expiresAt;

    /** 实物交接成功 */
    private LocalDateTime confirmedAt;

    /** 持码方确认已交付时刻 */
    private LocalDateTime holderConfirmedAt;

    /** 核验方确认已接收时刻 */
    private LocalDateTime verifierConfirmedAt;

    /** 已经封存证据组 */
    private Long evidenceGroupId;

    /** 唯一活跃交接：PENDING/PARTIAL 期间非空 */
    private String activeKey;

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

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getAssignmentId()
    {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId)
    {
        this.assignmentId = assignmentId;
    }

    public String getHandoverType()
    {
        return handoverType;
    }

    public void setHandoverType(String handoverType)
    {
        this.handoverType = handoverType;
    }

    public Long getHolderAccountId()
    {
        return holderAccountId;
    }

    public void setHolderAccountId(Long holderAccountId)
    {
        this.holderAccountId = holderAccountId;
    }

    public Long getVerifierAccountId()
    {
        return verifierAccountId;
    }

    public void setVerifierAccountId(Long verifierAccountId)
    {
        this.verifierAccountId = verifierAccountId;
    }

    public byte[] getCodeMac()
    {
        return codeMac;
    }

    public void setCodeMac(byte[] codeMac)
    {
        this.codeMac = codeMac;
    }

    public byte[] getCodeCipher()
    {
        return codeCipher;
    }

    public void setCodeCipher(byte[] codeCipher)
    {
        this.codeCipher = codeCipher;
    }

    public Integer getGenerationNo()
    {
        return generationNo;
    }

    public void setGenerationNo(Integer generationNo)
    {
        this.generationNo = generationNo;
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

    public LocalDateTime getConfirmedAt()
    {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt)
    {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getHolderConfirmedAt()
    {
        return holderConfirmedAt;
    }

    public void setHolderConfirmedAt(LocalDateTime holderConfirmedAt)
    {
        this.holderConfirmedAt = holderConfirmedAt;
    }

    public LocalDateTime getVerifierConfirmedAt()
    {
        return verifierConfirmedAt;
    }

    public void setVerifierConfirmedAt(LocalDateTime verifierConfirmedAt)
    {
        this.verifierConfirmedAt = verifierConfirmedAt;
    }

    public Long getEvidenceGroupId()
    {
        return evidenceGroupId;
    }

    public void setEvidenceGroupId(Long evidenceGroupId)
    {
        this.evidenceGroupId = evidenceGroupId;
    }

    public String getActiveKey()
    {
        return activeKey;
    }

    public void setActiveKey(String activeKey)
    {
        this.activeKey = activeKey;
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
