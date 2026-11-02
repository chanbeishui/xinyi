package com.xinyi.rongli.order.mapper;
import com.xinyi.rongli.order.domain.*;
import com.xinyi.rongli.booking.domain.ViQuote;
import org.apache.ibatis.annotations.Param;
import java.time.Instant;
public interface OrderMapper {
 com.xinyi.rongli.identity.domain.ViAccountSecurity lockSecurity(long accountId);
 ViQuote lockQuote(long id);
 int consumeQuote(@Param("id")long id,@Param("orderId")long orderId,@Param("version")long version);
 int hold(@Param("slotId")long slotId,@Param("version")long version);
 void reserve(@Param("orderId")long orderId,@Param("slotId")long slotId,@Param("expires")Instant expires);
 void insert(ViOrder row);void insertSnapshot(ViOrderSnapshot row);void insertPayment(ViPaymentOrder row);
 ViOrder find(long id);ViOrderSnapshot snapshot(long orderId);ViPaymentOrder payment(long orderId);
 String now();
}
