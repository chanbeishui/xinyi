package com.xinyi.rongli.booking.domain;
public class ViQuote {
 private Long id;
 public Long getId(){return id;}
 public void setId(Long value){id=value;}
 private Long ownerAccountId;
 public Long getOwnerAccountId(){return ownerAccountId;}
 public void setOwnerAccountId(Long value){ownerAccountId=value;}
 private Long stationId;
 public Long getStationId(){return stationId;}
 public void setStationId(Long value){stationId=value;}
 private String vehicleSnapshot;
 public String getVehicleSnapshot(){return vehicleSnapshot;}
 public void setVehicleSnapshot(String value){vehicleSnapshot=value;}
 private byte[] addressSnapshotCipher;
 public byte[] getAddressSnapshotCipher(){return addressSnapshotCipher;}
 public void setAddressSnapshotCipher(byte[] value){addressSnapshotCipher=value;}
 private Long pickupSlotId;
 public Long getPickupSlotId(){return pickupSlotId;}
 public void setPickupSlotId(Long value){pickupSlotId=value;}
 private Long receiveSlotId;
 public Long getReceiveSlotId(){return receiveSlotId;}
 public void setReceiveSlotId(Long value){receiveSlotId=value;}
 private Long ruleId;
 public Long getRuleId(){return ruleId;}
 public void setRuleId(Long value){ruleId=value;}
 private String routeSnapshot;
 public String getRouteSnapshot(){return routeSnapshot;}
 public void setRouteSnapshot(String value){routeSnapshot=value;}
 private Long amountFen;
 public Long getAmountFen(){return amountFen;}
 public void setAmountFen(Long value){amountFen=value;}
 private String itemsJson;
 public String getItemsJson(){return itemsJson;}
 public void setItemsJson(String value){itemsJson=value;}
 private java.time.Instant expiresAt;
 public java.time.Instant getExpiresAt(){return expiresAt;}
 public void setExpiresAt(java.time.Instant value){expiresAt=value;}
 private Long usedOrderId;
 public Long getUsedOrderId(){return usedOrderId;}
 public void setUsedOrderId(Long value){usedOrderId=value;}
 private Long version;
 public Long getVersion(){return version;}
 public void setVersion(Long value){version=value;}
}
