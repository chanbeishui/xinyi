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
 * 订单主履约、额度占用凭证与派单游标。
 */
@Mapper
public interface OrderMapper
{
    @Insert("""
            INSERT INTO vi_order (order_no, owner_account_id, vehicle_id, station_id, dept_id, quote_id,
                   vehicle_snapshot, fulfillment_status, cancel_status, custody_type, custody_ref_id,
                   pickup_starts_at, pickup_ends_at, pay_expires_at, dispatch_at, dispatch_config_version,
                   total_fen, terms_version, created_at, updated_at, version)
            VALUES (#{orderNo}, #{ownerAccountId}, #{vehicleId}, #{stationId}, #{deptId}, #{quoteId},
                   #{vehicleSnapshot}, 'PENDING_PAY', 'NONE', 'OWNER', #{ownerAccountId},
                   #{pickupStartsAt}, #{pickupEndsAt}, #{payExpiresAt}, #{dispatchAt}, #{dispatchConfigVersion},
                   #{totalFen}, #{termsVersion}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Map<String, Object> row);

    @Select("SELECT * FROM vi_order WHERE id = #{id}")
    Map<String, Object> findById(@Param("id") Long id);

    @Select("SELECT * FROM vi_order WHERE order_no = #{orderNo}")
    Map<String, Object> findByNo(@Param("orderNo") String orderNo);

    @Select("""
            SELECT o.*, s.name AS station_name, s.address AS station_address, s.lng AS station_lng, s.lat AS station_lat
            FROM vi_order o LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE o.id = #{id}
            """)
    Map<String, Object> findDetailById(@Param("id") Long id);

    @Select("""
            SELECT o.*, s.name AS station_name FROM vi_order o LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE o.order_no = #{orderNo}
            """)
    Map<String, Object> findDetailByNo(@Param("orderNo") String orderNo);

    @Select("""
            <script>
            SELECT o.id, o.order_no, o.fulfillment_status, o.cancel_status, o.custody_type,
                   o.pickup_starts_at, o.pickup_ends_at, o.total_fen, o.created_at, o.returned_at, o.completed_at,
                   o.vehicle_snapshot, s.name AS station_name, o.station_id
            FROM vi_order o LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE o.owner_account_id = #{accountId}
            <if test="status != null and status != ''"> AND o.fulfillment_status = #{status} </if>
            ORDER BY o.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> ownerList(@Param("accountId") Long accountId, @Param("status") String status,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_order o WHERE o.owner_account_id = #{accountId}
            <if test="status != null and status != ''"> AND o.fulfillment_status = #{status} </if>
            </script>
            """)
    long ownerCount(@Param("accountId") Long accountId, @Param("status") String status);

    /** 状态条件更新：必须带期望版本与期望当前状态，保证业务动作只在前置成立时生效 */
    @Update("""
            UPDATE vi_order SET fulfillment_status = #{to}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId} AND fulfillment_status = #{from} AND version = #{version}
            """)
    int casStatus(@Param("orderId") Long orderId, @Param("from") String from, @Param("to") String to,
            @Param("version") long version);

    @Update("""
            UPDATE vi_order SET fulfillment_status = #{to}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId} AND fulfillment_status = #{from}
            """)
    int casStatusByState(@Param("orderId") Long orderId, @Param("from") String from, @Param("to") String to);

    @Update("""
            UPDATE vi_order SET cancel_status = #{cancelStatus}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int updateCancelStatus(@Param("orderId") Long orderId, @Param("cancelStatus") String cancelStatus);

    @Update("""
            UPDATE vi_order SET custody_type = #{custodyType}, custody_ref_id = #{custodyRefId},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int updateCustody(@Param("orderId") Long orderId, @Param("custodyType") String custodyType,
            @Param("custodyRefId") Long custodyRefId);

    @Update("""
            UPDATE vi_order SET active_assignment_id = #{assignmentId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int updateActiveAssignment(@Param("orderId") Long orderId, @Param("assignmentId") Long assignmentId);

    @Update("""
            UPDATE vi_order SET returned_at = UTC_TIMESTAMP(3), auto_confirm_at = #{autoConfirmAt},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int markReturned(@Param("orderId") Long orderId, @Param("autoConfirmAt") LocalDateTime autoConfirmAt);

    @Update("""
            UPDATE vi_order SET completed_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int markCompleted(@Param("orderId") Long orderId);

    @Update("""
            UPDATE vi_order SET cancelled_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int markCancelled(@Param("orderId") Long orderId);

    @Update("""
            UPDATE vi_order SET dispatch_at = #{dispatchAt}, dispatch_config_version = #{configVersion},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{orderId}
            """)
    int updateDispatchAt(@Param("orderId") Long orderId, @Param("dispatchAt") LocalDateTime dispatchAt,
            @Param("configVersion") long configVersion);

    // ------------------------------------------------------------------ 定时任务扫描

    @Select("""
            SELECT id, order_no FROM vi_order
            WHERE fulfillment_status = 'PENDING_PAY' AND pay_expires_at <= UTC_TIMESTAMP(3)
            ORDER BY pay_expires_at LIMIT #{limit}
            """)
    List<Map<String, Object>> listPayExpired(@Param("limit") int limit);

    @Select("""
            SELECT id, order_no FROM vi_order
            WHERE fulfillment_status = 'SCHEDULED' AND dispatch_at IS NOT NULL AND dispatch_at <= UTC_TIMESTAMP(3)
            ORDER BY dispatch_at LIMIT #{limit}
            """)
    List<Map<String, Object>> listDispatchDue(@Param("limit") int limit);

    @Select("""
            SELECT id, order_no FROM vi_order
            WHERE fulfillment_status = 'WAIT_CONFIRM' AND auto_confirm_at IS NOT NULL AND auto_confirm_at <= UTC_TIMESTAMP(3)
            ORDER BY auto_confirm_at LIMIT #{limit}
            """)
    List<Map<String, Object>> listAutoConfirmDue(@Param("limit") int limit);

    @Select("""
            SELECT id, order_no FROM vi_order
            WHERE fulfillment_status = 'DISPATCHING' AND cancel_status = 'NONE'
              AND EXISTS (SELECT 1 FROM vi_dispatch_invite i WHERE i.order_id = vi_order.id AND i.status = 'WAIT_RESPONSE')
            LIMIT #{limit}
            """)
    List<Map<String, Object>> listDispatchingInvoices(@Param("limit") int limit);

    @Select("""
            SELECT id, order_no, fulfillment_status, pickup_starts_at, dispatch_at, dispatch_config_version
            FROM vi_order
            WHERE fulfillment_status IN ('SCHEDULED','PENDING_DISPATCH')
              AND pickup_starts_at IS NOT NULL AND pickup_starts_at > UTC_TIMESTAMP(3)
            ORDER BY pickup_starts_at ASC LIMIT #{limit}
            """)
    List<Map<String, Object>> listPlanCandidates(@Param("limit") int limit);

    // ------------------------------------------------------------------ 管理端

    @Select("""
            <script>
            SELECT o.id, o.order_no, o.fulfillment_status, o.cancel_status, o.custody_type, o.total_fen,
                   o.pickup_starts_at, o.pickup_ends_at, o.dispatch_at, o.created_at, o.returned_at,
                   o.vehicle_snapshot, o.station_id, s.name AS station_name, a.nickname AS owner_name,
                   o.active_assignment_id
            FROM vi_order o
            LEFT JOIN vi_station s ON s.id = o.station_id
            LEFT JOIN vi_account a ON a.id = o.owner_account_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND o.fulfillment_status = #{status} </if>
            <if test="stationId != null"> AND o.station_id = #{stationId} </if>
            <if test="keyword != null and keyword != ''">
              AND (o.order_no LIKE CONCAT('%', #{keyword}, '%')
                   OR JSON_UNQUOTE(JSON_EXTRACT(o.vehicle_snapshot, '$.plateNo')) LIKE CONCAT('%', #{keyword}, '%')
                   OR a.nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="from != null"> AND o.created_at &gt;= #{from} </if>
            <if test="to != null"> AND o.created_at &lt; #{to} </if>
            ORDER BY o.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminList(@Param("status") String status, @Param("stationId") Long stationId,
            @Param("keyword") String keyword, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_order o LEFT JOIN vi_account a ON a.id = o.owner_account_id WHERE 1 = 1
            <if test="status != null and status != ''"> AND o.fulfillment_status = #{status} </if>
            <if test="stationId != null"> AND o.station_id = #{stationId} </if>
            <if test="keyword != null and keyword != ''">
              AND (o.order_no LIKE CONCAT('%', #{keyword}, '%')
                   OR JSON_UNQUOTE(JSON_EXTRACT(o.vehicle_snapshot, '$.plateNo')) LIKE CONCAT('%', #{keyword}, '%')
                   OR a.nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="from != null"> AND o.created_at &gt;= #{from} </if>
            <if test="to != null"> AND o.created_at &lt; #{to} </if>
            </script>
            """)
    long adminCount(@Param("status") String status, @Param("stationId") Long stationId,
            @Param("keyword") String keyword, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Select("""
            SELECT o.id, o.order_no, o.fulfillment_status, o.cancel_status, o.custody_type, o.total_fen,
                   o.pickup_starts_at, o.pickup_ends_at, o.dispatch_at, o.returned_at, o.auto_confirm_at,
                   o.vehicle_snapshot, o.station_id, s.name AS station_name, o.active_assignment_id
            FROM vi_order o LEFT JOIN vi_station s ON s.id = o.station_id
            WHERE o.fulfillment_status IN ('PENDING_DISPATCH','MANUAL_DISPATCH','DISPATCHING')
            ORDER BY o.dispatch_at ASC, o.id ASC LIMIT #{limit}
            """)
    List<Map<String, Object>> dispatchQueue(@Param("limit") int limit);

    @Select("""
            <script>
            SELECT
              SUM(fulfillment_status = 'PENDING_PAY') AS pending_pay,
              SUM(fulfillment_status = 'SCHEDULED') AS scheduled,
              SUM(fulfillment_status IN ('PENDING_DISPATCH','DISPATCHING','MANUAL_DISPATCH')) AS dispatching,
              SUM(fulfillment_status IN ('ACCEPTED','GOING_PICKUP','ARRIVED_PICKUP')) AS on_the_way,
              SUM(fulfillment_status IN ('PICKED_UP','ARRIVED_STATION','INSPECTING')) AS in_service,
              SUM(fulfillment_status = 'INSPECTED') AS inspected,
              SUM(fulfillment_status = 'CAR_TAKEN') AS returning,
              SUM(fulfillment_status = 'WAIT_CONFIRM') AS wait_confirm,
              SUM(fulfillment_status = 'COMPLETED') AS completed,
              SUM(fulfillment_status = 'CANCELLED') AS cancelled,
              COUNT(*) AS total,
              COALESCE(SUM(CASE WHEN fulfillment_status NOT IN ('CANCELLED','CLOSED') THEN total_fen ELSE 0 END), 0) AS gmv_fen
            FROM vi_order
            </script>
            """)
    Map<String, Object> dashboard();

    @Select("""
            SELECT COUNT(1) FROM vi_order WHERE cancel_status = 'APPLIED'
            """)
    long countCancelApplied();

    @Select("""
            SELECT COUNT(1) FROM vi_order WHERE fulfillment_status = 'MANUAL_DISPATCH'
            """)
    long countManualDispatch();

    @Select("""
            SELECT COUNT(1) FROM vi_order WHERE cancel_status = 'APPROVED_WAIT_RETURN'
            """)
    long countWaitReturn();

    @Select("""
            SELECT DATE_FORMAT(CONVERT_TZ(created_at, '+00:00', '+08:00'), '%Y-%m-%d') AS day,
                   COUNT(*) AS orders, COALESCE(SUM(total_fen), 0) AS amount_fen
            FROM vi_order WHERE created_at >= #{from}
            GROUP BY day ORDER BY day
            """)
    List<Map<String, Object>> dailyStats(@Param("from") LocalDateTime from);

    // ------------------------------------------------------------------ 额度凭证

    @Insert("""
            INSERT INTO vi_quota_reservation (order_id, slot_id, status, quantity, expires_at, created_at, updated_at, version)
            VALUES (#{orderId}, #{slotId}, 'HELD', #{quantity}, #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertReservation(Map<String, Object> row);

    @Select("SELECT * FROM vi_quota_reservation WHERE order_id = #{orderId} ORDER BY id")
    List<Map<String, Object>> listReservations(@Param("orderId") Long orderId);

    @Update("""
            UPDATE vi_quota_reservation SET status = #{to}, released_at = IF(#{to} = 'RELEASED', UTC_TIMESTAMP(3), released_at),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = #{from}
            """)
    int casReservation(@Param("id") Long id, @Param("from") String from, @Param("to") String to);

    // ------------------------------------------------------------------ 派单游标

    @Select("SELECT * FROM vi_dispatch_cursor WHERE scope_key = #{scopeKey}")
    Map<String, Object> findCursor(@Param("scopeKey") String scopeKey);

    @Insert("""
            INSERT INTO vi_dispatch_cursor (scope_key, last_driver_id, round_no, created_at, updated_at, version)
            VALUES (#{scopeKey}, #{lastDriverId}, 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE last_driver_id = VALUES(last_driver_id), updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            """)
    int saveCursor(@Param("scopeKey") String scopeKey, @Param("lastDriverId") Long lastDriverId);

    @Update("""
            UPDATE vi_dispatch_cursor SET round_no = round_no + 1, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE scope_key = #{scopeKey}
            """)
    int bumpRound(@Param("scopeKey") String scopeKey);
}
