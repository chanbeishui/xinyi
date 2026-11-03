package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 外部已付款证据登记与核实，不发起转账
 *
 * <p>对应表 vi_driver_external_payment，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverExternalPayment
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 已确认对账单 */
    private Long statementId;

    /** 收款代驾 */
    private Long driverId;

    /** 付款主体 */
    private Long payorEntityId;

    /** 正数已发生付款 */
    private Long amountFen;

    /** 外部流水密文 */
    private byte[] externalReferenceCipher;

    /** 带密钥查询摘要 */
    private byte[] referenceLookup;

    /** 外部付款时间 */
    private LocalDateTime paidAt;

    /** 凭证 */
    private String evidenceIds;

    /** ExternalPaymentStatus: PENDING,VERIFIED… */
    private String status;

    /** 登记人 */
    private Long registeredBy;

    /** 不可同一真实身份 */
    private Long verifiedBy;

    /** 核对时间 */
    private LocalDateTime verifiedAt;

    /** 核对说明/冲正原因 */
    private String reason;

    /** 有错通过冲正记录处理 */
    private Long reversalOf;

    /** 关联的已确认内容版本 */
    private Integer statementRevision;

    /** UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getStatementId()
    {
        return statementId;
    }

    public void setStatementId(Long statementId)
    {
        this.statementId = statementId;
    }

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public Long getPayorEntityId()
    {
        return payorEntityId;
    }

    public void setPayorEntityId(Long payorEntityId)
    {
        this.payorEntityId = payorEntityId;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public byte[] getExternalReferenceCipher()
    {
        return externalReferenceCipher;
    }

    public void setExternalReferenceCipher(byte[] externalReferenceCipher)
    {
        this.externalReferenceCipher = externalReferenceCipher;
    }

    public byte[] getReferenceLookup()
    {
        return referenceLookup;
    }

    public void setReferenceLookup(byte[] referenceLookup)
    {
        this.referenceLookup = referenceLookup;
    }

    public LocalDateTime getPaidAt()
    {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt)
    {
        this.paidAt = paidAt;
    }

    public String getEvidenceIds()
    {
        return evidenceIds;
    }

    public void setEvidenceIds(String evidenceIds)
    {
        this.evidenceIds = evidenceIds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getRegisteredBy()
    {
        return registeredBy;
    }

    public void setRegisteredBy(Long registeredBy)
    {
        this.registeredBy = registeredBy;
    }

    public Long getVerifiedBy()
    {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy)
    {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt()
    {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt)
    {
        this.verifiedAt = verifiedAt;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public Long getReversalOf()
    {
        return reversalOf;
    }

    public void setReversalOf(Long reversalOf)
    {
        this.reversalOf = reversalOf;
    }

    public Integer getStatementRevision()
    {
        return statementRevision;
    }

    public void setStatementRevision(Integer statementRevision)
    {
        this.statementRevision = statementRevision;
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
