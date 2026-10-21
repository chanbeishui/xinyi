package com.xinyi.rongli.mapper;

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
 * 派单邀约、司机承接与唯一占用。
 */
@Mapper
public interface DispatchMapper
{
    // ------------------------------------------------------------------ 邀约

    @Insert("""
            INSERT INTO vi_dispatch_invite (order_id, driver_id, status, source, round_key, issued_at, expires_at,
                   created_at, updated_at, version)
            VALUES (#{orderId}, #{driverId}, 'WAIT_RESPONSE', #{source}, #{roundKey}, UTC_TIMESTAMP(3),
                   #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertInvite(Map<String, Object> row);

    @Select("SELECT * FROM vi_dispatch_invite WHERE id = #{inviteId}")
    Map<String, Object> findInvite(@Param("inviteId") Long inviteId);

    @Select("""
            SELECT i.*, o.order_no, o.pickup_starts_at, o.pickup_ends_at, o.vehicle_snapshot, o.total_fen,
                   o.station_id, s.name AS station_name
            FROM vi_dispatch_invite i
            JOIN vi_order o ON o.id = i.order_id
            LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE i.driver_id = #{driverId} AND i.status = 'WAIT_RESPONSE' AND i.expires_at > UTC_TIMESTAMP(3)
            ORDER BY i.issued_at DESC LIMIT 20
            """)
    List<Map<String, Object>> listActiveInvites(@Param("driverId") Long driverId);

    @Select("""
            SELECT i.*, o.order_no, o.pickup_starts_at, o.pickup_ends_at, o.vehicle_snapshot, o.total_fen,
                   o.station_id, s.name AS station_name, o.custody_type
            FROM vi_dispatch_invite i
            JOIN vi_order o ON o.id = i.order_id
            LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE i.id = #{inviteId} AND i.driver_id = #{driverId}
            """)
    Map<String, Object> findInviteForDriver(@Param("inviteId") Long inviteId, @Param("driverId") Long driverId);

    @Update("""
            UPDATE vi_dispatch_invite SET status = #{to}, responded_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{inviteId} AND status = 'WAIT_RESPONSE'
            """)
    int respondInvite(@Param("inviteId") Long inviteId, @Param("to") String to);

    @Update("""
            UPDATE vi_dispatch_invite SET status = 'EXPIRED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE status = 'WAIT_RESPONSE' AND expires_at <= UTC_TIMESTAMP(3)
            """)
    int expireDueInvites();

    @Update("""
            UPDATE vi_dispatch_invite SET status = 'REVOKED', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE order_id = #{orderId} AND status = 'WAIT_RESPONSE'
            """)
    int revokeInvitesOfOrder(@Param("orderId") Long orderId);

    @Select("""
            <script>
            SELECT i.id, i.order_id, i.driver_id, i.status, i.source, i.issued_at, i.expires_at, i.responded_at,
                   o.order_no, a.nickname AS driver_name
            FROM vi_dispatch_invite i
            JOIN vi_order o ON o.id = i.order_id
            LEFT JOIN vi_driver_profile d ON d.id = i.driver_id
            LEFT JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="orderId != null"> AND i.order_id = #{orderId} </if>
            <if test="driverId != null"> AND i.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND i.status = #{status} </if>
            ORDER BY i.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminListInvites(@Param("orderId") Long orderId, @Param("driverId") Long driverId,
            @Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    // ------------------------------------------------------------------ 承接

    @Insert("""
            INSERT INTO vi_driver_assignment (order_id, driver_id, invite_id, accepted_at, created_at, updated_at, version)
            VALUES (#{orderId}, #{driverId}, #{inviteId}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAssignment(Map<String, Object> row);

    @Select("SELECT * FROM vi_driver_assignment WHERE order_id = #{orderId} AND ended_at IS NULL")
    Map<String, Object> findActiveAssignmentOfOrder(@Param("orderId") Long orderId);

    @Select("SELECT * FROM vi_driver_assignment WHERE driver_id = #{driverId} AND ended_at IS NULL")
    Map<String, Object> findActiveAssignmentOfDriver(@Param("driverId") Long driverId);

    @Select("SELECT * FROM vi_driver_assignment WHERE id = #{assignmentId}")
    Map<String, Object> findAssignment(@Param("assignmentId") Long assignmentId);

    @Update("""
            UPDATE vi_driver_assignment SET ended_at = UTC_TIMESTAMP(3), end_reason = #{reason},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{assignmentId} AND ended_at IS NULL
            """)
    int endAssignment(@Param("assignmentId") Long assignmentId, @Param("reason") String reason);

    @Select("""
            <script>
            SELECT ass.id, ass.order_id, ass.driver_id, ass.accepted_at, ass.ended_at, ass.end_reason,
                   o.order_no, o.fulfillment_status, o.pickup_starts_at, o.returned_at, o.total_fen,
                   o.vehicle_snapshot
            FROM vi_driver_assignment ass JOIN vi_order o ON o.id = ass.order_id
            WHERE ass.driver_id = #{driverId}
            <if test="status == 'ACTIVE'"> AND ass.ended_at IS NULL </if>
            <if test="status == 'ENDED'"> AND ass.ended_at IS NOT NULL </if>
            ORDER BY ass.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listAssignmentsOfDriver(@Param("driverId") Long driverId,
            @Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_driver_assignment ass WHERE ass.driver_id = #{driverId}
            <if test="status == 'ACTIVE'"> AND ass.ended_at IS NULL </if>
            <if test="status == 'ENDED'"> AND ass.ended_at IS NOT NULL </if>
            </script>
            """)
    long countAssignmentsOfDriver(@Param("driverId") Long driverId, @Param("status") String status);

    // ------------------------------------------------------------------ 唯一占用

    @Insert("""
            INSERT INTO vi_driver_occupancy (driver_id, holder_type, holder_id, order_id, expires_at, created_at, updated_at, version)
            VALUES (#{driverId}, #{holderType}, #{holderId}, #{orderId}, #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int acquireOccupancy(@Param("driverId") Long driverId, @Param("holderType") String holderType,
            @Param("holderId") Long holderId, @Param("orderId") Long orderId,
            @Param("expiresAt") LocalDateTime expiresAt);

    /** 必须精确匹配 holder 才能释放，避免旧事件误释放新任务 */
    @Delete("""
            DELETE FROM vi_driver_occupancy WHERE driver_id = #{driverId} AND holder_id = #{holderId}
            """)
    int releaseOccupancy(@Param("driverId") Long driverId, @Param("holderId") Long holderId);

    @Select("SELECT * FROM vi_driver_occupancy WHERE driver_id = #{driverId}")
    Map<String, Object> findOccupancy(@Param("driverId") Long driverId);

    @Delete("""
            DELETE FROM vi_driver_occupancy WHERE holder_type = 'INVITE' AND expires_at IS NOT NULL
              AND expires_at <= UTC_TIMESTAMP(3)
            """)
    int releaseExpiredInviteOccupancy();

    @Select("""
            SELECT d.id AS driver_id, d.account_id, d.license_class, d.listening_enabled,
                   d.qualification_status, a.nickname, l.captured_at, l.location_cipher
            FROM vi_driver_profile d
            JOIN vi_account a ON a.id = d.account_id
            LEFT JOIN vi_driver_location l ON l.driver_id = d.id
            LEFT JOIN vi_driver_occupancy oc ON oc.driver_id = d.id
            WHERE d.qualification_status = 'APPROVED' AND d.new_assignment_enabled = 1
              AND d.listening_enabled = 1 AND a.status = 'ACTIVE' AND oc.driver_id IS NULL
            ORDER BY d.id
            """)
    List<Map<String, Object>> listEligibleDrivers();

    @Select("""
            SELECT d.id AS driver_id, a.nickname, d.qualification_status, d.listening_enabled,
                   d.new_assignment_enabled, oc.holder_type, oc.order_id, l.captured_at
            FROM vi_driver_profile d
            JOIN vi_account a ON a.id = d.account_id
            LEFT JOIN vi_driver_occupancy oc ON oc.driver_id = d.id
            LEFT JOIN vi_driver_location l ON l.driver_id = d.id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''"> AND a.nickname LIKE CONCAT('%', #{keyword}, '%') </if>
            ORDER BY d.id LIMIT 200
            """)
    List<Map<String, Object>> candidates(@Param("keyword") String keyword);

    @Select("""
            SELECT ass.id AS assignment_id, ass.order_id, ass.accepted_at, o.order_no, o.fulfillment_status,
                   o.pickup_starts_at, o.vehicle_snapshot, o.station_id, s.name AS station_name
            FROM vi_driver_assignment ass
            JOIN vi_order o ON o.id = ass.order_id
            LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE ass.ended_at IS NULL
            ORDER BY ass.accepted_at LIMIT 200
            """)
    List<Map<String, Object>> listActiveAssignments();
}
