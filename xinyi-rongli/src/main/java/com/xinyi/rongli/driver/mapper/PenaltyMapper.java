package com.xinyi.rongli.driver.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * driver 域查询。由原 MiscMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface PenaltyMapper
{
    @Insert("""
            INSERT INTO vi_penalty (driver_id, assignment_id, business_date, counted, round_key, penalty_type,
                   status, reason, rule_version, effective_until, created_at, updated_at, version)
            VALUES (#{driverId}, #{assignmentId}, #{businessDate}, 1, #{roundKey}, #{penaltyType},
                   'ACTIVE', #{reason}, #{ruleVersion}, #{effectiveUntil}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE updated_at = UTC_TIMESTAMP(3)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPenalty(Map<String, Object> row);
    @Select("""
            SELECT p.*, o.order_no FROM vi_penalty p
            LEFT JOIN vi_driver_assignment ass ON ass.id = p.assignment_id
            LEFT JOIN vi_order o ON o.id = ass.order_id
            WHERE p.driver_id = #{driverId} ORDER BY p.id DESC LIMIT 100
            """)
    List<Map<String, Object>> listPenaltiesOfDriver(@Param("driverId") Long driverId);
    @Select("SELECT * FROM vi_penalty WHERE id = #{penaltyId}")
    Map<Object, Object> findPenaltyRaw(@Param("penaltyId") Long penaltyId);
    @Select("""
            SELECT p.*, o.order_no FROM vi_penalty p
            LEFT JOIN vi_driver_assignment ass ON ass.id = p.assignment_id
            LEFT JOIN vi_order o ON o.id = ass.order_id
            WHERE p.id = #{penaltyId}
            """)
    Map<String, Object> findPenalty(@Param("penaltyId") Long penaltyId);
    @Select("""
            SELECT p.id, p.driver_id, p.business_date, p.penalty_type, p.status, p.reason, p.created_at,
                   a.nickname AS driver_name
            FROM vi_penalty p
            JOIN vi_driver_profile d ON d.id = p.driver_id
            JOIN vi_account a ON a.id = d.account_id
            ORDER BY p.id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listPenaltiesAdmin(@Param("offset") int offset, @Param("limit") int limit);
    @Update("""
            UPDATE vi_penalty SET status = 'REVOKED', revoked_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{penaltyId} AND status = 'ACTIVE'
            """)
    int revokePenalty(@Param("penaltyId") Long penaltyId);
    @Insert("""
            INSERT INTO vi_penalty_appeal (penalty_id, driver_id, status, reason, evidence_file_ids,
                   submitted_at, created_at, updated_at, version)
            VALUES (#{penaltyId}, #{driverId}, 'SUBMITTED', #{reason}, #{evidenceFileIds}, UTC_TIMESTAMP(3),
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAppeal(Map<String, Object> row);
    @Select("""
            SELECT ap.*, p.reason AS penalty_reason, p.penalty_type FROM vi_penalty_appeal ap
            JOIN vi_penalty p ON p.id = ap.penalty_id
            WHERE ap.driver_id = #{driverId} ORDER BY ap.id DESC LIMIT 50
            """)
    List<Map<String, Object>> listAppealsOfDriver(@Param("driverId") Long driverId);
    @Select("SELECT * FROM vi_penalty_appeal WHERE id = #{appealId}")
    Map<String, Object> findAppeal(@Param("appealId") Long appealId);
    @Select("""
            SELECT ap.*, p.reason AS penalty_reason, a.nickname AS driver_name FROM vi_penalty_appeal ap
            JOIN vi_penalty p ON p.id = ap.penalty_id
            JOIN vi_driver_profile d ON d.id = ap.driver_id
            JOIN vi_account a ON a.id = d.account_id
            ORDER BY ap.id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listAppealsAdmin(@Param("offset") int offset, @Param("limit") int limit);
    @Update("""
            UPDATE vi_penalty_appeal SET status = #{status}, reviewer_user_id = #{reviewer},
                   reviewed_at = UTC_TIMESTAMP(3), public_opinion = #{opinion},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{appealId} AND status = 'SUBMITTED'
            """)
    int auditAppeal(@Param("appealId") Long appealId, @Param("status") String status,
            @Param("reviewer") Long reviewer, @Param("opinion") String opinion);
}
