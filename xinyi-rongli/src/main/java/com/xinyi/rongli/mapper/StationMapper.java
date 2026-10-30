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
 * 检测点、员工站点授权与容量额度。
 */
@Mapper
public interface StationMapper
{
    @Select("""
            SELECT id, name, address, lng, lat, service_radius_meter, supported_vehicle_codes, business_hours, enabled
            FROM vi_station WHERE enabled = 1 ORDER BY id
            """)
    List<Map<String, Object>> listEnabled();

    @Select("SELECT * FROM vi_station WHERE id = #{id}")
    Map<String, Object> find(@Param("id") Long id);

    @Select("SELECT * FROM vi_station WHERE id = #{id} AND enabled = 1")
    Map<String, Object> findEnabled(@Param("id") Long id);

    @Select("""
            <script>
            SELECT s.*, d.dept_name,
                   (SELECT COUNT(1) FROM vi_order o WHERE o.station_id = s.id AND o.fulfillment_status IN
                      ('ARRIVED_STATION','INSPECTING','INSPECTED')) AS active_order_count
            FROM vi_station s LEFT JOIN sys_dept d ON d.dept_id = s.dept_id
            ORDER BY s.id LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> adminList(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM vi_station")
    long adminCount();

    @Insert("""
            INSERT INTO vi_station (name, dept_id, address, lng, lat, service_radius_meter,
                   supported_vehicle_codes, business_hours, enabled, created_at, updated_at, version)
            VALUES (#{name}, #{deptId}, #{address}, #{lng}, #{lat}, #{serviceRadiusMeter},
                   #{supportedVehicleCodes}, #{businessHours}, #{enabled}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Map<String, Object> row);

    @Update("""
            UPDATE vi_station SET name = #{name}, dept_id = #{deptId}, address = #{address}, lng = #{lng},
                   lat = #{lat}, service_radius_meter = #{serviceRadiusMeter},
                   supported_vehicle_codes = #{supportedVehicleCodes}, business_hours = #{businessHours},
                   enabled = #{enabled}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int update(Map<String, Object> row);

    // ------------------------------------------------------------------ 员工站点授权

    @Select("""
            SELECT g.*, s.name AS station_name FROM vi_station_grant g
            JOIN vi_station s ON s.id = g.station_id
            WHERE g.sys_user_id = #{sysUserId} AND g.enabled = 1 ORDER BY g.station_id
            """)
    List<Map<String, Object>> grantsOfUser(@Param("sysUserId") Long sysUserId);

    @Select("""
            SELECT COUNT(1) FROM vi_station_grant
            WHERE sys_user_id = #{sysUserId} AND station_id = #{stationId} AND enabled = 1
            """)
    int hasGrant(@Param("sysUserId") Long sysUserId, @Param("stationId") Long stationId);

    @Insert("""
            INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason, created_at, updated_at, version)
            VALUES (#{sysUserId}, #{stationId}, #{enabled}, #{grantedBy}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE enabled = VALUES(enabled), granted_by = VALUES(granted_by),
                   reason = VALUES(reason), updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    int upsertGrant(@Param("sysUserId") Long sysUserId, @Param("stationId") Long stationId,
            @Param("enabled") boolean enabled, @Param("grantedBy") Long grantedBy, @Param("reason") String reason);

    @Select("""
            SELECT g.sys_user_id, u.user_name, u.nick_name, g.station_id, s.name AS station_name,
                   g.enabled, g.reason, g.updated_at, d.dept_id, d.dept_name,
                   (SELECT COUNT(1) FROM vi_employee_binding b
                     WHERE b.sys_user_id = g.sys_user_id AND b.revoked_at IS NULL) AS binding_count
            FROM vi_station_grant g
            LEFT JOIN sys_user u ON u.user_id = g.sys_user_id
            LEFT JOIN sys_dept d ON d.dept_id = u.dept_id
            LEFT JOIN vi_station s ON s.id = g.station_id
            ORDER BY g.updated_at DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listGrants(@Param("offset") int offset, @Param("limit") int limit);

    // ------------------------------------------------------------------ 容量

    @Select("""
            <script>
            SELECT * FROM vi_quota_slot
            WHERE quota_type = #{quotaType}
            <if test="scopeKey != null and scopeKey != ''"> AND scope_key = #{scopeKey} </if>
            <if test="from != null"> AND ends_at &gt; #{from} </if>
            <if test="to != null"> AND starts_at &lt; #{to} </if>
            ORDER BY starts_at LIMIT 500
            </script>
            """)
    List<Map<String, Object>> listSlots(@Param("quotaType") String quotaType, @Param("scopeKey") String scopeKey,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Select("SELECT * FROM vi_quota_slot WHERE id = #{id}")
    Map<String, Object> findSlot(@Param("id") Long id);

    @Select("""
            SELECT * FROM vi_quota_slot
            WHERE quota_type = #{quotaType} AND scope_key = #{scopeKey} AND starts_at = #{startsAt} AND ends_at = #{endsAt}
            """)
    Map<String, Object> findSlotExact(@Param("quotaType") String quotaType, @Param("scopeKey") String scopeKey,
            @Param("startsAt") LocalDateTime startsAt, @Param("endsAt") LocalDateTime endsAt);

    @Insert("""
            INSERT INTO vi_quota_slot (quota_type, scope_key, station_id, starts_at, ends_at, total_quota,
                   held_quota, confirmed_quota, consumed_quota, enabled, created_at, updated_at, version)
            VALUES (#{quotaType}, #{scopeKey}, #{stationId}, #{startsAt}, #{endsAt}, #{totalQuota},
                   0, 0, 0, 1, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE updated_at = UTC_TIMESTAMP(3)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSlot(Map<String, Object> row);

    /** 原子预占：容量不足时受影响行数为 0 */
    @Update("""
            UPDATE vi_quota_slot SET held_quota = held_quota + #{qty}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{slotId} AND enabled = 1
              AND held_quota + confirmed_quota + consumed_quota + #{qty} <= total_quota
            """)
    int holdQuota(@Param("slotId") Long slotId, @Param("qty") int qty);

    /** 支付成功：预占转正式占用，不重复扣减 */
    @Update("""
            UPDATE vi_quota_slot SET held_quota = held_quota - #{qty}, confirmed_quota = confirmed_quota + #{qty},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{slotId} AND held_quota >= #{qty}
            """)
    int confirmQuota(@Param("slotId") Long slotId, @Param("qty") int qty);

    /** 站点实际接收：正式占用转已消耗 */
    @Update("""
            UPDATE vi_quota_slot SET confirmed_quota = confirmed_quota - #{qty}, consumed_quota = consumed_quota + #{qty},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{slotId} AND confirmed_quota >= #{qty}
            """)
    int consumeQuota(@Param("slotId") Long slotId, @Param("qty") int qty);

    @Update("""
            UPDATE vi_quota_slot SET held_quota = held_quota - #{qty}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{slotId} AND held_quota >= #{qty}
            """)
    int releaseHeldQuota(@Param("slotId") Long slotId, @Param("qty") int qty);

    @Update("""
            UPDATE vi_quota_slot SET confirmed_quota = confirmed_quota - #{qty}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{slotId} AND confirmed_quota >= #{qty}
            """)
    int releaseConfirmedQuota(@Param("slotId") Long slotId, @Param("qty") int qty);

    @Update("""
            UPDATE vi_quota_slot SET total_quota = #{totalQuota}, enabled = #{enabled},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{slotId} AND (held_quota + confirmed_quota + consumed_quota) <= #{totalQuota}
            """)
    int updateSlotCapacity(@Param("slotId") Long slotId, @Param("totalQuota") int totalQuota,
            @Param("enabled") boolean enabled);
}
