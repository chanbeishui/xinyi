package com.xinyi.rongli.payment.mapper;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.integration.domain.IntegrationProfile;
import org.apache.ibatis.annotations.Param;
public interface PaymentMapper {
 ViOrder lockOwnedOrder(@Param("orderNo")String orderNo,@Param("owner")long owner);
 ViOrder lockOrder(long id);
 PaymentTransaction lockPayment(long orderId);
 IntegrationProfile policyProfile(long revisionId);
 int freeze(PaymentTransaction row);
 int claim(@Param("id")long id,@Param("version")long version,@Param("token")String token);
 int finish(@Param("id")long id,@Param("version")long version,@Param("token")String token,@Param("cipher")byte[] cipher);
 String now();
}
