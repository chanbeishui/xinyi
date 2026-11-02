package com.xinyi.rongli.order.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class ViPaymentOrder extends ViRecord {
 private Long orderId;
 public Long getOrderId(){return orderId;}
 public void setOrderId(Long v){orderId=v;}
 private String outTradeNo;
 public String getOutTradeNo(){return outTradeNo;}
 public void setOutTradeNo(String v){outTradeNo=v;}
 private String status;
 public String getStatus(){return status;}
 public void setStatus(String v){status=v;}
 private long totalFen;
 public long getTotalFen(){return totalFen;}
 public void setTotalFen(long v){totalFen=v;}
 private long paidFen;
 public long getPaidFen(){return paidFen;}
 public void setPaidFen(long v){paidFen=v;}
 private long refundedFen;
 public long getRefundedFen(){return refundedFen;}
 public void setRefundedFen(long v){refundedFen=v;}
 private Instant expiresAt;
 public Instant getExpiresAt(){return expiresAt;}
 public void setExpiresAt(Instant v){expiresAt=v;}
 private String paymentMethod;
 public String getPaymentMethod(){return paymentMethod;}
 public void setPaymentMethod(String v){paymentMethod=v;}
 private Long receiverDeptId;
 public Long getReceiverDeptId(){return receiverDeptId;}
 public void setReceiverDeptId(Long v){receiverDeptId=v;}
 private Long policyRevisionId;
 public Long getPolicyRevisionId(){return policyRevisionId;}
 public void setPolicyRevisionId(Long v){policyRevisionId=v;}
 private String capacityResolution;
 public String getCapacityResolution(){return capacityResolution;}
 public void setCapacityResolution(String v){capacityResolution=v;}
}
