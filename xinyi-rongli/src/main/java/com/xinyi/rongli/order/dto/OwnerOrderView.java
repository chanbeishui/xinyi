package com.xinyi.rongli.order.dto;
import java.util.List;
import com.xinyi.rongli.address.dto.GeoPoint;
import com.xinyi.rongli.booking.dto.QuoteView;
/** 车主视图白名单，不向HTTP序列化持久化实体或加密快照。 */
public record OwnerOrderView(String orderNo,String fulfillmentStatus,String displayStatus,String cancelStatus,String custody,
 Vehicle vehicle,Station station,String slotStartAt,String slotEndAt,String returnedAt,long version,String view,
 String paymentStatus,String refundStatus,String disputeStatus,Address pickupAddress,Address returnAddress,
 Object driver,String dispatchAt,String payExpireAt,String autoConfirmAt,Money money,Mileage mileage,Object afterSale,List<String> allowedActions) {
 public record Vehicle(String vehicleId,String plateNo,String vehicleTypeCode,String vehicleTypeLabel,String brandModel,String vinLast4){}
 public record Station(String stationId,String name,String publicAddress,GeoPoint location,String servicePhone){}
 public record Contact(String displayName,String phoneMask,boolean canRequestContact){}
 public record Address(String address,GeoPoint location,Contact contact){}
 public record Money(List<QuoteView.FeeItem> items,long totalAmount,long paidAmount,long refundedAmount){}
 public record Mileage(String pickupKm,String returnKm,String distanceKm,boolean abnormal,String publicExplanation){}
}
