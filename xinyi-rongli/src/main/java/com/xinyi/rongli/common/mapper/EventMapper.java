package com.xinyi.rongli.common.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * common 域查询。由原 MiscMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface EventMapper
{
    @Insert("""
            INSERT INTO vi_event_log (order_id, aggregate_type, aggregate_id, action, actor_account_id,
                   actor_user_id, acting_role, station_id, before_summary, after_summary, reason,
                   actor_realm, session_id, context_version, created_at, updated_at, version)
            VALUES (#{orderId}, #{aggregateType}, #{aggregateId}, #{action}, #{actorAccountId},
                   #{actorUserId}, #{actingRole}, #{stationId}, #{before}, #{after}, #{reason},
                   #{realm}, #{sessionId}, #{contextVersion}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertEvent(Map<String, Object> row);
    @Select("""
            <script>
            SELECT e.*, o.order_no FROM vi_event_log e LEFT JOIN vi_order o ON o.id = e.order_id
            WHERE 1 = 1
            <if test="orderId != null"> AND e.order_id = #{orderId} </if>
            <if test="action != null and action != ''"> AND e.action = #{action} </if>
            ORDER BY e.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listEvents(@Param("orderId") Long orderId, @Param("action") String action,
            @Param("offset") int offset, @Param("limit") int limit);
    @Select("""
            SELECT e.id, e.action, e.actor_realm, e.acting_role, e.before_summary, e.after_summary,
                   e.reason, e.created_at, e.actor_account_id, e.actor_user_id
            FROM vi_event_log e WHERE e.order_id = #{orderId} ORDER BY e.id
            """)
    List<Map<String, Object>> timelineEvents(@Param("orderId") Long orderId);
    @Insert("""
            INSERT INTO vi_outbox (event_key, aggregate_type, aggregate_id, event_type, payload, status,
                   attempts, next_attempt_at, created_at, updated_at, version)
            VALUES (#{eventKey}, #{aggregateType}, #{aggregateId}, #{eventType}, #{payload}, 'READY',
                   0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE updated_at = UTC_TIMESTAMP(3)
            """)
    int insertOutbox(Map<String, Object> row);
    @Select("""
            SELECT * FROM vi_outbox WHERE status = 'READY' AND next_attempt_at <= UTC_TIMESTAMP(3)
            ORDER BY id LIMIT #{limit}
            """)
    List<Map<String, Object>> listDueOutbox(@Param("limit") int limit);
    @Update("""
            UPDATE vi_outbox SET status = 'DONE', updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int markOutboxDone(@Param("id") Long id);
    @Update("""
            UPDATE vi_outbox SET status = IF(attempts + 1 >= 5, 'FAILED', 'READY'), attempts = attempts + 1,
                   next_attempt_at = DATE_ADD(UTC_TIMESTAMP(3), INTERVAL POW(2, attempts) MINUTE),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int markOutboxRetry(@Param("id") Long id);
    @Select("""
            <script>
            SELECT * FROM vi_outbox
            <where>
              <if test="status != null and status != ''"> AND status = #{status} </if>
            </where>
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listOutbox(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);
    @Select("""
            SELECT * FROM vi_idempotency
            WHERE subject_key = #{subjectKey} AND action_key = #{actionKey} AND request_key = #{requestKey}
            """)
    Map<String, Object> findIdempotency(@Param("subjectKey") String subjectKey, @Param("actionKey") String actionKey,
            @Param("requestKey") String requestKey);
    @Insert("""
            INSERT INTO vi_idempotency (subject_key, action_key, request_key, request_mac, status, expires_at,
                   created_at, updated_at, version)
            VALUES (#{subjectKey}, #{actionKey}, #{requestKey}, #{requestMac}, 'PROCESSING',
                   DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 24 HOUR), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE updated_at = UTC_TIMESTAMP(3)
            """)
    int insertIdempotency(Map<String, Object> row);
    @Update("""
            UPDATE vi_idempotency SET status = 'DONE', result_json = #{resultJson},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE subject_key = #{subjectKey} AND action_key = #{actionKey} AND request_key = #{requestKey}
            """)
    int finishIdempotency(@Param("subjectKey") String subjectKey, @Param("actionKey") String actionKey,
            @Param("requestKey") String requestKey, @Param("resultJson") String resultJson);
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{key} LIMIT 1")
    String findConfigValue(@Param("key") String key);
}
