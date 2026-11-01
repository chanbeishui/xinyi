package com.xinyi.rongli.booking.dto;
import java.util.List;
public record QuoteView(String quoteId,String expiresAt,String stationId,String stationName,String slotId,long routeDistanceMeter,String routeBasis,List<FeeItem> items,long totalAmount,int priceRuleVersion,String termsVersion,long quotaPlanVersion) {
 public record FeeItem(String feeCode,String name,long amount,String calculation){}
}
