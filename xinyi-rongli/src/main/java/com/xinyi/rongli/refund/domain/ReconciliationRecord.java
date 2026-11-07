package com.xinyi.rongli.refund.domain;

import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;

/** 持久化记录，不作为公开请求或响应绑定。 */
public class ReconciliationRecord extends ViRecord {
    private String kind;

    public String getKind() {
        return kind;
    }

    public void setKind(String value) {
        kind = value;
    }

    private Long subjectId;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long value) {
        subjectId = value;
    }

    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long value) {
        orderId = value;
    }

    private Long paymentId;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long value) {
        paymentId = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private String localStatus;

    public String getLocalStatus() {
        return localStatus;
    }

    public void setLocalStatus(String value) {
        localStatus = value;
    }

    private String channelStatus;

    public String getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(String value) {
        channelStatus = value;
    }

    private String reasonCode;

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String value) {
        reasonCode = value;
    }

    private Instant verifiedAt;

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant value) {
        verifiedAt = value;
    }

    private Long resolvedBy;

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Long value) {
        resolvedBy = value;
    }

    private Instant resolvedAt;

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant value) {
        resolvedAt = value;
    }
}
