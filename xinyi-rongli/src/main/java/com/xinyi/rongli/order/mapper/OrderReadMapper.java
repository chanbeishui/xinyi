package com.xinyi.rongli.order.mapper;
import com.xinyi.rongli.order.domain.*;
import com.xinyi.rongli.order.dto.OrderQuery;
import com.xinyi.rongli.order.service.OrderReadScope;
import org.apache.ibatis.annotations.Param;
import java.util.*;
public interface OrderReadMapper {
 List<ViOrder> list(@Param("scope")OrderReadScope scope,@Param("query")OrderQuery query);
 long count(@Param("scope")OrderReadScope scope,@Param("query")OrderQuery query);
 ViOrder detail(@Param("scope")OrderReadScope scope,@Param("orderNo")String orderNo);
 Long driverId(long accountId);
 OrderAssignment ownAssignment(@Param("orderId")long orderId,@Param("driverId")long driverId);
 OrderAssignment activeAssignment(@Param("orderId")long orderId,@Param("assignmentId")Long assignmentId);
 Map<String,Object> driverPublic(long driverId);
 Map<String,Object> afterSale(long orderId);
 String dispute(long orderId);String license(long orderId);String result(long orderId);
 List<Map<String,Object>> mileage(long orderId);
 String stationSnapshot(long orderId);
 ViOrderSnapshot contactCipher(long orderId);
 List<Map<String,Object>> timeline(@Param("orderId")long orderId,@Param("actions")Set<String> actions,@Param("offset")int offset,@Param("limit")int limit);
 long timelineCount(@Param("orderId")long orderId,@Param("actions")Set<String> actions);
}
