package com.xinyi.rongli.payment.domain;

import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;

public class PaymentTransaction extends ViRecord {
    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long value) {
        orderId = value;
    }

    private String outTradeNo;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String value) {
        outTradeNo = value;
    }

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    private long totalFen;

    public long getTotalFen() {
        return totalFen;
    }

    public void setTotalFen(long value) {
        totalFen = value;
    }

    private long paidFen;

    public long getPaidFen() {
        return paidFen;
    }

    public void setPaidFen(long value) {
        paidFen = value;
    }

    private Instant expiresAt;

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant value) {
        expiresAt = value;
    }

    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String value) {
        paymentMethod = value;
    }

    private Long receiverDeptId;

    public Long getReceiverDeptId() {
        return receiverDeptId;
    }

    public void setReceiverDeptId(Long value) {
        receiverDeptId = value;
    }

    private Long policyRevisionId;

    public Long getPolicyRevisionId() {
        return policyRevisionId;
    }

    public void setPolicyRevisionId(Long value) {
        policyRevisionId = value;
    }

    private Long merchantProfileId;

    public Long getMerchantProfileId() {
        return merchantProfileId;
    }

    public void setMerchantProfileId(Long value) {
        merchantProfileId = value;
    }

    private Long merchantRevisionId;

    public Long getMerchantRevisionId() {
        return merchantRevisionId;
    }

    public void setMerchantRevisionId(Long value) {
        merchantRevisionId = value;
    }

    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String value) {
        appId = value;
    }

    private String mchid;

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String value) {
        mchid = value;
    }

    private Instant methodLockedAt;

    public Instant getMethodLockedAt() {
        return methodLockedAt;
    }

    public void setMethodLockedAt(Instant value) {
        methodLockedAt = value;
    }

    private String capacityResolution;

    public String getCapacityResolution() {
        return capacityResolution;
    }

    public void setCapacityResolution(String value) {
        capacityResolution = value;
    }

    private String prepayState;

    public String getPrepayState() {
        return prepayState;
    }

    public void setPrepayState(String value) {
        prepayState = value;
    }

    private byte[] prepayRequestCipher;

    public byte[] getPrepayRequestCipher() {
        return prepayRequestCipher;
    }

    public void setPrepayRequestCipher(byte[] value) {
        prepayRequestCipher = value;
    }

    private byte[] prepayResultCipher;

    public byte[] getPrepayResultCipher() {
        return prepayResultCipher;
    }

    public void setPrepayResultCipher(byte[] value) {
        prepayResultCipher = value;
    }

    private String prepayLeaseToken;

    public String getPrepayLeaseToken() {
        return prepayLeaseToken;
    }

    public void setPrepayLeaseToken(String value) {
        prepayLeaseToken = value;
    }

    private Instant prepayLeaseUntil;

    public Instant getPrepayLeaseUntil() {
        return prepayLeaseUntil;
    }

    public void setPrepayLeaseUntil(Instant value) {
        prepayLeaseUntil = value;
    }

    private Instant closeRequestedAt, nextQueryAt, paidAt;
    private String transactionId;

    public Instant getCloseRequestedAt() {
        return closeRequestedAt;
    }

    public void setCloseRequestedAt(Instant v) {
        closeRequestedAt = v;
    }

    public Instant getNextQueryAt() {
        return nextQueryAt;
    }

    public void setNextQueryAt(Instant v) {
        nextQueryAt = v;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant v) {
        paidAt = v;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String v) {
        transactionId = v;
    }

    @Override
    public String toString() {
        return "PaymentTransaction[id=" + getId() + "]";
    }
}
