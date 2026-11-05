package com.xinyi.rongli.aftersale.domain;

import com.xinyi.rongli.core.ViRecord;

/** 持久化记录，禁止作为请求体或公开响应绑定。 */
public class AftersaleCase extends ViRecord {
    private String publicOpinion;

    public String getPublicOpinion() {
        return publicOpinion;
    }

    public void setPublicOpinion(String value) {
        publicOpinion = value;
    }

    private String caseNo;

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String value) {
        caseNo = value;
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

    private String kind;

    public String getKind() {
        return kind;
    }

    public void setKind(String value) {
        kind = value;
    }

    private String caseStatus;

    public String getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(String value) {
        caseStatus = value;
    }

    private String reviewStatus;

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String value) {
        reviewStatus = value;
    }

    private int reviewRevision;

    public int getReviewRevision() {
        return reviewRevision;
    }

    public void setReviewRevision(int value) {
        reviewRevision = value;
    }

    private long requestedFen;

    public long getRequestedFen() {
        return requestedFen;
    }

    public void setRequestedFen(long value) {
        requestedFen = value;
    }

    private Long approvedFen;

    public Long getApprovedFen() {
        return approvedFen;
    }

    public void setApprovedFen(Long value) {
        approvedFen = value;
    }

    private String returnState;

    public String getReturnState() {
        return returnState;
    }

    public void setReturnState(String value) {
        returnState = value;
    }

    private String refundState;

    public String getRefundState() {
        return refundState;
    }

    public void setRefundState(String value) {
        refundState = value;
    }

    private String applicantType;

    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String value) {
        applicantType = value;
    }

    private Long applicantId;

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long value) {
        applicantId = value;
    }

    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String value) {
        reason = value;
    }

    private String reasonCode;

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String value) {
        reasonCode = value;
    }

    private String evidenceIds;

    public String getEvidenceIds() {
        return evidenceIds;
    }

    public void setEvidenceIds(String value) {
        evidenceIds = value;
    }
}
