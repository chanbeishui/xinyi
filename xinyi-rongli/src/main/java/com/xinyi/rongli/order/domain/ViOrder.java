package com.xinyi.rongli.order.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class ViOrder extends ViRecord {
 private String orderNo;
 public String getOrderNo(){return orderNo;}
 public void setOrderNo(String v){orderNo=v;}
 private Long ownerAccountId;
 public Long getOwnerAccountId(){return ownerAccountId;}
 public void setOwnerAccountId(Long v){ownerAccountId=v;}
 private Long vehicleId;
 public Long getVehicleId(){return vehicleId;}
 public void setVehicleId(Long v){vehicleId=v;}
 private Long stationId;
 public Long getStationId(){return stationId;}
 public void setStationId(Long v){stationId=v;}
 private Long deptId;
 public Long getDeptId(){return deptId;}
 public void setDeptId(Long v){deptId=v;}
 private Long quoteId;
 public Long getQuoteId(){return quoteId;}
 public void setQuoteId(Long v){quoteId=v;}
 private String vehicleSnapshot;
 public String getVehicleSnapshot(){return vehicleSnapshot;}
 public void setVehicleSnapshot(String v){vehicleSnapshot=v;}
 private String fulfillmentStatus;
 public String getFulfillmentStatus(){return fulfillmentStatus;}
 public void setFulfillmentStatus(String v){fulfillmentStatus=v;}
 private String cancelStatus;
 public String getCancelStatus(){return cancelStatus;}
 public void setCancelStatus(String v){cancelStatus=v;}
 private String custodyType;
 public String getCustodyType(){return custodyType;}
 public void setCustodyType(String v){custodyType=v;}
 private Long custodyRefId;
 public Long getCustodyRefId(){return custodyRefId;}
 public void setCustodyRefId(Long v){custodyRefId=v;}
 private Instant pickupStartsAt;
 public Instant getPickupStartsAt(){return pickupStartsAt;}
 public void setPickupStartsAt(Instant v){pickupStartsAt=v;}
 private Instant pickupEndsAt;
 public Instant getPickupEndsAt(){return pickupEndsAt;}
 public void setPickupEndsAt(Instant v){pickupEndsAt=v;}
 private Instant payExpiresAt;
 public Instant getPayExpiresAt(){return payExpiresAt;}
 public void setPayExpiresAt(Instant v){payExpiresAt=v;}
 private long totalFen;
 public long getTotalFen(){return totalFen;}
 public void setTotalFen(long v){totalFen=v;}
 private String termsVersion;
 public String getTermsVersion(){return termsVersion;}
 public void setTermsVersion(String v){termsVersion=v;}
}
