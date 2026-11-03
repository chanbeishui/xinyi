package com.xinyi.rongli.payment.domain;

import java.time.LocalDateTime;

/**
 * 客户线下收退款证明与核实
 *
 * <p>对应表 vi_offline_payment_record，由数据库结构生成，字段与列一一对应。</p>
 */
public class OfflinePaymentRecord
{
    /** 主键。内部自增Long；API十进制字符串 */
    private Long id;

    /** 支付单 */
    private Long paymentId;

    /** 仅REFUND有值，必须来自统一审核已批准执行单 */
    private Long refundId;

    /** OfflineProofDirection: COLLECTION,REFUND */
    private String direction;

    /** 原支付收款主体快照 */
    private Long ownerDeptId;

    /** 本期完整金额；不支持混合/客户拆单付款 */
    private Long amountFen;

    /** 真实线下发生时刻 */
    private LocalDateTime paidAt;

    /** OfflineProofMethod: CASH,BANK_TRANSFER */
    private String method;

    /** 流水/收据编号密文 */
    private byte[] externalReferenceCipher;

    /** 主体+方向+方式+流水永久去重摘要 */
    private String referenceHmac;

    /** 私有文件，服务端核查归属与可用性 */
    private String voucherFileIds;

    /** OfflineProofStatus: SUBMITTED,VERIFIED,REJECTED */
    private String status;

    /** 登记人 */
    private Long registeredBy;

    /** 登记时刻 */
    private LocalDateTime registeredAt;

    /** 独立核实人 */
    private Long verifiedBy;

    /** 核实时刻 */
    private LocalDateTime verifiedAt;

    /** 登记说明 */
    private String registerNote;

    /** 核实/驳回原因 */
    private String verifyReason;

    /** 当前有效收款证明唯一占位 */
    private Long activePayment;

    /** 当前有效退款证明唯一占位 */
    private Long activeRefund;

    /** UTC创建时间 */
    private LocalDateTime createdAt;

    /** UTC更新时间 */
    private LocalDateTime updatedAt;

    /** 显式CAS；不是注解自动生效 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getPaymentId()
    {
        return paymentId;
    }

    public void setPaymentId(Long paymentId)
    {
        this.paymentId = paymentId;
    }

    public Long getRefundId()
    {
        return refundId;
    }

    public void setRefundId(Long refundId)
    {
        this.refundId = refundId;
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    public Long getOwnerDeptId()
    {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long ownerDeptId)
    {
        this.ownerDeptId = ownerDeptId;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public LocalDateTime getPaidAt()
    {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt)
    {
        this.paidAt = paidAt;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public byte[] getExternalReferenceCipher()
    {
        return externalReferenceCipher;
    }

    public void setExternalReferenceCipher(byte[] externalReferenceCipher)
    {
        this.externalReferenceCipher = externalReferenceCipher;
    }

    public String getReferenceHmac()
    {
        return referenceHmac;
    }

    public void setReferenceHmac(String referenceHmac)
    {
        this.referenceHmac = referenceHmac;
    }

    public String getVoucherFileIds()
    {
        return voucherFileIds;
    }

    public void setVoucherFileIds(String voucherFileIds)
    {
        this.voucherFileIds = voucherFileIds;
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

    public LocalDateTime getRegisteredAt()
    {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt)
    {
        this.registeredAt = registeredAt;
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

    public String getRegisterNote()
    {
        return registerNote;
    }

    public void setRegisterNote(String registerNote)
    {
        this.registerNote = registerNote;
    }

    public String getVerifyReason()
    {
        return verifyReason;
    }

    public void setVerifyReason(String verifyReason)
    {
        this.verifyReason = verifyReason;
    }

    public Long getActivePayment()
    {
        return activePayment;
    }

    public void setActivePayment(Long activePayment)
    {
        this.activePayment = activePayment;
    }

    public Long getActiveRefund()
    {
        return activeRefund;
    }

    public void setActiveRefund(Long activeRefund)
    {
        this.activeRefund = activeRefund;
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
