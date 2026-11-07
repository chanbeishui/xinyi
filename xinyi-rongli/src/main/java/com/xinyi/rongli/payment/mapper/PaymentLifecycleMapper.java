package com.xinyi.rongli.payment.mapper;

import com.xinyi.rongli.payment.domain.PaymentTransaction;
import org.apache.ibatis.annotations.Param;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface PaymentLifecycleMapper {
    List<PaymentTransaction> callbackRoutes();

    Long orderByTrade(String trade);

    List<Long> due();

    int claimQuery(long id);

    int requestClose(long id);

    int unknown(long id);

    int pending(long id);

    int paid(
            @Param("id") long id,
            @Param("transactionId") String transactionId,
            @Param("paidAt") Instant paidAt,
            @Param("conflict") boolean conflict);

    int closed(long id);

    int orderState(@Param("id") long id, @Param("state") String state);

    List<Map<String, Object>> reservations(long orderId);

    int confirmSlot(@Param("slotId") long slotId, @Param("quantity") int quantity);

    int releaseSlot(@Param("slotId") long slotId, @Param("quantity") int quantity);

    int reserveState(@Param("id") long id, @Param("state") String state);

    Map<String, Object> event(@Param("provider") String provider, @Param("key") String key);

    int eventInsert(
            @Param("provider") String provider,
            @Param("key") String key,
            @Param("trade") String trade,
            @Param("type") String type,
            @Param("digest") byte[] digest);

    Map<String, Object> refundByNo(String no);

    Map<String, Object> lockRefund(long id);

    Map<String, Object> refundSnapshot(long id);

    int refundSuccess(
            @Param("id") long id, @Param("channelId") String channelId, @Param("at") Instant at);

    int refundState(@Param("id") long id, @Param("state") String state);

    int refundBalance(
            @Param("paymentId") long paymentId,
            @Param("amount") long amount,
            @Param("success") boolean success);
}
