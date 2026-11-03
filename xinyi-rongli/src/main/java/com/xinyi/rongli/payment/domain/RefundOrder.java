package com.xinyi.rongli.payment.domain;

import java.time.LocalDateTime;

/**
 * 稳定退款单及渠道执行
 *
 * <p>对应表 vi_refund_order，由数据库结构生成，字段与列一一对应。</p>
 */
public class RefundOrder
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 关联唯一统一售后申请 */
    private Long caseId;

    /** 批准决定版本，金额变更必须新版本审核 */
    private Integer reviewRevision;

    /** 支付单 */
    private Long paymentId;

    /** 幂等商户退款号 */
    private String outRefundNo;

    /** 渠道退款ID */
    private String channelRefundId;

    /** 金额分 */
    private Long amountFen;

    /** RefundPaymentMethod: WECHAT,OFFLINE */
    private String paymentMethod;

    /** RefundExecutionMode: WECHAT_API,MANUAL_OFFLINE */
    private String executionMode;

    /** ChannelRefundStatus: CREATED,PROCESSING,SUCCESS… */
    private String status;

    /** 只有SUCCESS存在 */
    private LocalDateTime successAt;

    /** 查单 */
    private LocalDateTime lastQueryAt;

    /** 退避后查单 */
    private LocalDateTime nextQueryAt;

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

    public Long getCaseId()
    {
        return caseId;
    }

    public void setCaseId(Long caseId)
    {
        this.caseId = caseId;
    }

    public Integer getReviewRevision()
    {
        return reviewRevision;
    }

    public void setReviewRevision(Integer reviewRevision)
    {
        this.reviewRevision = reviewRevision;
    }

    public Long getPaymentId()
    {
        return paymentId;
    }

    public void setPaymentId(Long paymentId)
    {
        this.paymentId = paymentId;
    }

    public String getOutRefundNo()
    {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo)
    {
        this.outRefundNo = outRefundNo;
    }

    public String getChannelRefundId()
    {
        return channelRefundId;
    }

    public void setChannelRefundId(String channelRefundId)
    {
        this.channelRefundId = channelRefundId;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
    }

    public String getExecutionMode()
    {
        return executionMode;
    }

    public void setExecutionMode(String executionMode)
    {
        this.executionMode = executionMode;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getSuccessAt()
    {
        return successAt;
    }

    public void setSuccessAt(LocalDateTime successAt)
    {
        this.successAt = successAt;
    }

    public LocalDateTime getLastQueryAt()
    {
        return lastQueryAt;
    }

    public void setLastQueryAt(LocalDateTime lastQueryAt)
    {
        this.lastQueryAt = lastQueryAt;
    }

    public LocalDateTime getNextQueryAt()
    {
        return nextQueryAt;
    }

    public void setNextQueryAt(LocalDateTime nextQueryAt)
    {
        this.nextQueryAt = nextQueryAt;
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
