package com.xinyi.rongli.refund.domain;

import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;

/** 持久化记录，不作为公开请求或响应绑定。 */
public class RefundRecord extends ViRecord {
    private Long caseId;

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long value) {
        caseId = value;
    }

    private int reviewRevision;

    public int getReviewRevision() {
        return reviewRevision;
    }

    public void setReviewRevision(int value) {
        reviewRevision = value;
    }

    private Long paymentId;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long value) {
        paymentId = value;
    }

    private String outRefundNo;

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String value) {
        outRefundNo = value;
    }

    private String channelRefundId;

    public String getChannelRefundId() {
        return channelRefundId;
    }

    public void setChannelRefundId(String value) {
        channelRefundId = value;
    }

    private long amountFen;

    public long getAmountFen() {
        return amountFen;
    }

    public void setAmountFen(long value) {
        amountFen = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private Instant successAt;

    public Instant getSuccessAt() {
        return successAt;
    }

    public void setSuccessAt(Instant value) {
        successAt = value;
    }

    private Instant lastQueryAt;

    public Instant getLastQueryAt() {
        return lastQueryAt;
    }

    public void setLastQueryAt(Instant value) {
        lastQueryAt = value;
    }

    private Instant nextQueryAt;

    public Instant getNextQueryAt() {
        return nextQueryAt;
    }

    public void setNextQueryAt(Instant value) {
        nextQueryAt = value;
    }

    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String value) {
        paymentMethod = value;
    }

    private String executionMode;

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String value) {
        executionMode = value;
    }

    private Instant executionStartedAt;

    public Instant getExecutionStartedAt() {
        return executionStartedAt;
    }

    public void setExecutionStartedAt(Instant value) {
        executionStartedAt = value;
    }

    private String executionLeaseToken;

    public String getExecutionLeaseToken() {
        return executionLeaseToken;
    }

    public void setExecutionLeaseToken(String value) {
        executionLeaseToken = value;
    }

    private Instant executionLeaseUntil;

    public Instant getExecutionLeaseUntil() {
        return executionLeaseUntil;
    }

    public void setExecutionLeaseUntil(Instant value) {
        executionLeaseUntil = value;
    }

    private int executionAttempts;

    public int getExecutionAttempts() {
        return executionAttempts;
    }

    public void setExecutionAttempts(int value) {
        executionAttempts = value;
    }

    private String lastErrorCode;

    public String getLastErrorCode() {
        return lastErrorCode;
    }

    public void setLastErrorCode(String value) {
        lastErrorCode = value;
    }

    private String lastVerifiedStatus;

    public String getLastVerifiedStatus() {
        return lastVerifiedStatus;
    }

    public void setLastVerifiedStatus(String value) {
        lastVerifiedStatus = value;
    }

    private Instant lastVerifiedAt;

    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public void setLastVerifiedAt(Instant value) {
        lastVerifiedAt = value;
    }
}
