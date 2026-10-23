package com.xinyi.rongli.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 支付单、渠道退款单与回调去重。
 */
@Mapper
public interface PayMapper
{
    @Insert("""
            INSERT INTO vi_payment_order (order_id, out_trade_no, status, total_fen, paid_fen,
                   refund_reserved_fen, refunded_fen, expires_at, payment_method, receiver_dept_id,
                   capacity_resolution, created_at, updated_at, version)
            VALUES (#{orderId}, #{outTradeNo}, 'PENDING', #{totalFen}, 0, 0, 0, #{expiresAt}, #{paymentMethod},
                   #{receiverDeptId}, #{capacityResolution}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE total_fen = VALUES(total_fen), expires_at = VALUES(expires_at),
                   updated_at = UTC_TIMESTAMP(3)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPayment(Map<String, Object> row);

    @Select("SELECT * FROM vi_payment_order WHERE order_id = #{orderId}")
    Map<String, Object> findByOrder(@Param("orderId") Long orderId);

    @Select("SELECT * FROM vi_payment_order WHERE id = #{paymentId}")
    Map<String, Object> findById(@Param("paymentId") Long paymentId);

    @Select("SELECT * FROM vi_payment_order WHERE out_trade_no = #{outTradeNo}")
    Map<String, Object> findByTradeNo(@Param("outTradeNo") String outTradeNo);

    /** 支付成功：只有 PENDING 才能转 SUCCESS，回调重放不会重复记账 */
    @Update("""
            UPDATE vi_payment_order SET status = 'SUCCESS', paid_fen = total_fen, transaction_id = #{transactionId},
                   paid_at = #{paidAt}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{paymentId} AND status = 'PENDING'
            """)
    int markPaid(@Param("paymentId") Long paymentId, @Param("transactionId") String transactionId,
            @Param("paidAt") LocalDateTime paidAt);

    @Update("""
            UPDATE vi_payment_order SET status = 'CLOSED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{paymentId} AND status = 'PENDING'
            """)
    int markClosed(@Param("paymentId") Long paymentId);

    @Update("""
            UPDATE vi_payment_order SET status = 'UNKNOWN', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{paymentId} AND status = 'PENDING'
            """)
    int markUnknown(@Param("paymentId") Long paymentId);

    /** 批准退款时预留金额：不得超过 实付 - 已退 - 已预留 */
    @Update("""
            UPDATE vi_payment_order SET refund_reserved_fen = refund_reserved_fen + #{amount},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{paymentId} AND paid_fen - refunded_fen - refund_reserved_fen >= #{amount}
            """)
    int reserveRefund(@Param("paymentId") Long paymentId, @Param("amount") long amount);

    /** 渠道退款成功：预留转已退 */
    @Update("""
            UPDATE vi_payment_order SET refund_reserved_fen = refund_reserved_fen - #{amount},
                   refunded_fen = refunded_fen + #{amount}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{paymentId} AND refund_reserved_fen >= #{amount}
            """)
    int settleRefund(@Param("paymentId") Long paymentId, @Param("amount") long amount);

    /** 渠道退款失败/关闭：释放预留，不改变已退 */
    @Update("""
            UPDATE vi_payment_order SET refund_reserved_fen = refund_reserved_fen - #{amount},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{paymentId} AND refund_reserved_fen >= #{amount}
            """)
    int releaseRefundReserve(@Param("paymentId") Long paymentId, @Param("amount") long amount);

    @Select("""
            SELECT p.*, o.order_no FROM vi_payment_order p JOIN vi_order o ON o.id = p.order_id
            WHERE o.order_no = #{orderNo}
            """)
    Map<String, Object> findByOrderNo(@Param("orderNo") String orderNo);

    // ------------------------------------------------------------------ 退款单

    @Insert("""
            INSERT INTO vi_refund_order (case_id, review_revision, payment_id, out_refund_no, amount_fen, status,
                   created_at, updated_at, version)
            VALUES (#{caseId}, #{reviewRevision}, #{paymentId}, #{outRefundNo}, #{amountFen}, #{status},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRefund(Map<String, Object> row);

    @Select("SELECT * FROM vi_refund_order WHERE case_id = #{caseId} ORDER BY id DESC LIMIT 1")
    Map<String, Object> findLatestRefundOfCase(@Param("caseId") Long caseId);

    @Select("SELECT * FROM vi_refund_order WHERE out_refund_no = #{outRefundNo}")
    Map<String, Object> findByRefundNo(@Param("outRefundNo") String outRefundNo);

    @Select("SELECT * FROM vi_refund_order WHERE id = #{id}")
    Map<String, Object> findRefundById(@Param("id") Long id);

    @Update("""
            UPDATE vi_refund_order SET status = #{status}, channel_refund_id = COALESCE(#{channelRefundId}, channel_refund_id),
                   success_at = IF(#{status} = 'SUCCESS', UTC_TIMESTAMP(3), success_at),
                   last_query_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{refundId}
            """)
    int updateRefundStatus(@Param("refundId") Long refundId, @Param("status") String status,
            @Param("channelRefundId") String channelRefundId);

    @Select("""
            <script>
            SELECT r.*, o.order_no, c.case_no FROM vi_refund_order r
            JOIN vi_aftersale_case c ON c.id = r.case_id
            JOIN vi_order o ON o.id = c.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND r.status = #{status} </if>
            ORDER BY r.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listRefunds(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    // ------------------------------------------------------------------ 回调去重

    @Insert("""
            INSERT INTO vi_channel_event (provider, event_key, trade_no, event_type, verified_digest,
                   processed_at, created_at, updated_at, version)
            VALUES (#{provider}, #{eventKey}, #{tradeNo}, #{eventType}, #{digest}, UTC_TIMESTAMP(3),
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertChannelEvent(@Param("provider") String provider, @Param("eventKey") String eventKey,
            @Param("tradeNo") String tradeNo, @Param("eventType") String eventType,
            @Param("digest") byte[] digest);
}
