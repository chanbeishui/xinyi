package com.xinyi.rongli.payment.domain;

import java.time.LocalDateTime;

/**
 * 支付交易与可退资金
 *
 * <p>对应表 vi_payment_order，由数据库结构生成，字段与列一一对应。</p>
 */
public class PaymentOrder
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 稳定微信商户单号 */
    private String outTradeNo;

    /** 渠道交易号 */
    private String transactionId;

    /** PaymentStatus: PENDING,SUCCESS,CLOSED,UNKNOWN */
    private String status;

    /** PaymentMethod: UNSELECTED,WECHAT,OFFLINE */
    private String paymentMethod;

    /** 原始收款主体部门；回填后收紧为NOT NULL */
    private Long receiverDeptId;

    /** 创建/选择时采用的配置版 */
    private Long policyRevisionId;

    /** 仅微信 */
    private Long merchantProfileId;

    /** 原资金身份快照版本 */
    private Long merchantRevisionId;

    /** 下单小程序AppID快照 */
    private String appId;

    /** 商户号快照 */
    private String mchid;

    /** 首次有效预下单/线下意图时写入 */
    private LocalDateTime methodLockedAt;

    /** PaymentCapacityResolution: HELD,RELEASED… */
    private String capacityResolution;

    /** 应付 */
    private Long totalFen;

    /** 实付 */
    private Long paidFen;

    /** 处理中退款金额预留 */
    private Long refundReservedFen;

    /** 已成功退款 */
    private Long refundedFen;

    /** 支付成功 */
    private LocalDateTime paidAt;

    /** 对应30分钟时点 */
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

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOutTradeNo()
    {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo)
    {
        this.outTradeNo = outTradeNo;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
    }

    public Long getReceiverDeptId()
    {
        return receiverDeptId;
    }

    public void setReceiverDeptId(Long receiverDeptId)
    {
        this.receiverDeptId = receiverDeptId;
    }

    public Long getPolicyRevisionId()
    {
        return policyRevisionId;
    }

    public void setPolicyRevisionId(Long policyRevisionId)
    {
        this.policyRevisionId = policyRevisionId;
    }

    public Long getMerchantProfileId()
    {
        return merchantProfileId;
    }

    public void setMerchantProfileId(Long merchantProfileId)
    {
        this.merchantProfileId = merchantProfileId;
    }

    public Long getMerchantRevisionId()
    {
        return merchantRevisionId;
    }

    public void setMerchantRevisionId(Long merchantRevisionId)
    {
        this.merchantRevisionId = merchantRevisionId;
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getMchid()
    {
        return mchid;
    }

    public void setMchid(String mchid)
    {
        this.mchid = mchid;
    }

    public LocalDateTime getMethodLockedAt()
    {
        return methodLockedAt;
    }

    public void setMethodLockedAt(LocalDateTime methodLockedAt)
    {
        this.methodLockedAt = methodLockedAt;
    }

    public String getCapacityResolution()
    {
        return capacityResolution;
    }

    public void setCapacityResolution(String capacityResolution)
    {
        this.capacityResolution = capacityResolution;
    }

    public Long getTotalFen()
    {
        return totalFen;
    }

    public void setTotalFen(Long totalFen)
    {
        this.totalFen = totalFen;
    }

    public Long getPaidFen()
    {
        return paidFen;
    }

    public void setPaidFen(Long paidFen)
    {
        this.paidFen = paidFen;
    }

    public Long getRefundReservedFen()
    {
        return refundReservedFen;
    }

    public void setRefundReservedFen(Long refundReservedFen)
    {
        this.refundReservedFen = refundReservedFen;
    }

    public Long getRefundedFen()
    {
        return refundedFen;
    }

    public void setRefundedFen(Long refundedFen)
    {
        this.refundedFen = refundedFen;
    }

    public LocalDateTime getPaidAt()
    {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt)
    {
        this.paidAt = paidAt;
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
