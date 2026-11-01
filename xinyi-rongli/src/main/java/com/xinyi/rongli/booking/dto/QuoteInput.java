package com.xinyi.rongli.booking.dto;
import com.xinyi.rongli.address.dto.GeoPoint;
public record QuoteInput(String slotId,String stationId,String inspectionTypeCode,String vehicleId,VehicleDraft vehicleDraft,String addressId,AddressDraft addressDraft) {
 public record VehicleDraft(String plateNo,String vehicleTypeCode,String brandModel,String useType,String vinLast4,Boolean isDefault){}
 public record AddressDraft(String address,GeoPoint location,String label,Boolean isDefault){}
}
