package com.xinyi.rongli.admin.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * admin 域管理端查询。由原 AdminMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface GovernanceMapper
{
    @Insert("""
            INSERT INTO vi_governance_case (case_type, order_id, requester_account_id, requester_user_id, status,
                   payload_cipher, reason, expires_at, created_at, updated_at, version)
            VALUES (#{caseType}, #{orderId}, #{requesterAccountId}, #{requesterUserId}, #{status},
                   #{payloadCipher}, #{reason}, #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGovernance(Map<String, Object> row);
    @Select("""
            <script>
            SELECT g.*, o.order_no, o.total_fen, o.fulfillment_status,
                   p.out_trade_no, p.transaction_id, p.status AS payment_status,
                   p.refund_reserved_fen, p.refunded_fen,
                   r.out_refund_no, r.status AS refund_status
            FROM vi_governance_case g
            LEFT JOIN vi_order o ON o.id = g.order_id
            LEFT JOIN vi_payment_order p ON p.order_id = o.id
            LEFT JOIN vi_refund_order r ON r.id = (SELECT MAX(r2.id) FROM vi_refund_order r2 WHERE r2.payment_id = p.id)
            WHERE g.case_type = #{caseType}
            <if test="status != null and status != ''"> AND g.status = #{status} </if>
            <if test="keyword != null and keyword != ''">
              AND (o.order_no LIKE CONCAT('%', #{keyword}, '%')
                   OR p.out_trade_no LIKE CONCAT('%', #{keyword}, '%')
                   OR r.out_refund_no LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY g.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listGovernance(@Param("caseType") String caseType, @Param("status") String status,
            @Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_governance_case g
            LEFT JOIN vi_order o ON o.id = g.order_id
            LEFT JOIN vi_payment_order p ON p.order_id = o.id
            WHERE g.case_type = #{caseType}
            <if test="status != null and status != ''"> AND g.status = #{status} </if>
            <if test="keyword != null and keyword != ''">
              AND (o.order_no LIKE CONCAT('%', #{keyword}, '%')
                   OR p.out_trade_no LIKE CONCAT('%', #{keyword}, '%')
                   OR EXISTS (SELECT 1 FROM vi_refund_order r
                              WHERE r.payment_id = p.id AND r.out_refund_no LIKE CONCAT('%', #{keyword}, '%')))
            </if>
            </script>
            """)
    long countGovernance(@Param("caseType") String caseType, @Param("status") String status,
            @Param("keyword") String keyword);
    @Select("""
            SELECT g.*, o.order_no, o.total_fen, o.fulfillment_status,
                   p.out_trade_no, p.transaction_id, p.status AS payment_status,
                   p.refund_reserved_fen, p.refunded_fen,
                   r.out_refund_no, r.status AS refund_status
            FROM vi_governance_case g
            LEFT JOIN vi_order o ON o.id = g.order_id
            LEFT JOIN vi_payment_order p ON p.order_id = o.id
            LEFT JOIN vi_refund_order r ON r.id = (SELECT MAX(r2.id) FROM vi_refund_order r2 WHERE r2.payment_id = p.id)
            WHERE g.id = #{id}
            """)
    Map<String, Object> findGovernance(@Param("id") Long id);
    @Select("""
            SELECT * FROM vi_governance_case WHERE case_type = #{caseType} AND requester_account_id = #{accountId}
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listGovernanceOfAccount(@Param("caseType") String caseType,
            @Param("accountId") Long accountId, @Param("offset") int offset, @Param("limit") int limit);
    @Update("""
            UPDATE vi_governance_case SET status = #{status}, reviewer_id = #{reviewerId},
                   reason = COALESCE(#{reason}, reason),
                   payload_cipher = COALESCE(#{payloadCipher}, payload_cipher),
                   expires_at = COALESCE(#{expiresAt}, expires_at),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int updateGovernance(@Param("id") Long id, @Param("status") String status,
            @Param("reviewerId") Long reviewerId, @Param("reason") String reason,
            @Param("payloadCipher") byte[] payloadCipher, @Param("expiresAt") LocalDateTime expiresAt);
    @Update("""
            UPDATE vi_governance_case SET status = #{status}, reviewer_id = #{reviewerId},
                   conclusion = #{conclusion},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int concludeGovernance(@Param("id") Long id, @Param("status") String status,
            @Param("reviewerId") Long reviewerId, @Param("conclusion") String conclusion);
}
