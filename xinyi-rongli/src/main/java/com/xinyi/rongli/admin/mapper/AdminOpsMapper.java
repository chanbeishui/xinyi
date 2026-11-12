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
public interface AdminOpsMapper
{
    @Select("""
            <script>
            SELECT
              (SELECT COUNT(1) FROM vi_order o
                 WHERE o.fulfillment_status NOT IN ('COMPLETED','CANCELLED','CLOSED')
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS active_orders,
              (SELECT COUNT(1) FROM vi_order o
                 WHERE DATE(CONVERT_TZ(o.created_at,'+00:00','+08:00')) = #{bizDate}
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS orders_today,
              (SELECT COUNT(1) FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id
                 WHERE c.case_status = 'PENDING_REVIEW'
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS aftersale_pending,
              (SELECT COUNT(1) FROM vi_dispute d JOIN vi_order o ON o.id = d.order_id
                 WHERE d.status IN ('OPEN','PROCESSING')
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS dispute_open,
              (SELECT COUNT(1) FROM vi_service_exception e JOIN vi_order o ON o.id = e.order_id
                 WHERE e.status IN ('OPEN','PROCESSING')
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS exception_open,
              (SELECT COUNT(1) FROM vi_result_correction r JOIN vi_order o ON o.id = r.order_id
                 WHERE r.status = 'APPLIED'
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS correction_pending,
              (SELECT COUNT(1) FROM vi_penalty_appeal WHERE status = 'SUBMITTED') AS appeal_pending,
              (SELECT COUNT(1) FROM vi_driver_application WHERE status = 'SUBMITTED') AS qualification_pending,
              (SELECT COUNT(1) FROM vi_aftersale_case c JOIN vi_order o ON o.id = c.order_id
                 WHERE c.case_status = 'EXECUTING' AND c.return_state = 'WAIT_RETURN'
                 <if test="deptIds != null and deptIds.size() > 0">
                   AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
                 </if>) AS cancel_return_waiting,
              (SELECT COUNT(1) FROM vi_driver_statement WHERE status IN ('INTERNAL_REVIEW','SENT','DISPUTED')) AS statement_pending
            </script>
            """)
    Map<String, Object> statisticsOverview(@Param("bizDate") String bizDate, @Param("deptIds") List<Long> deptIds);
    @Select("""
            <script>
            SELECT COUNT(1) AS sample_orders,
                   COALESCE(SUM(fulfillment_status = 'WAIT_CONFIRM'), 0) AS returned_assignments,
                   COALESCE(SUM(fulfillment_status = 'PENDING_PAY'), 0) AS pending_pay_orders
            FROM vi_order
            <where>
              <if test="deptIds != null and deptIds.size() > 0">
                dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
              </if>
            </where>
            </script>
            """)
    Map<String, Object> statisticsOrderKpi(@Param("deptIds") List<Long> deptIds);
    @Select("""
            <script>
            SELECT COALESCE(SUM(p.paid_fen), 0) AS paid_fen,
                   COALESCE(COUNT(1), 0) AS paid_orders,
                   COALESCE(SUM(p.refunded_fen), 0) AS refunded_fen
            FROM vi_payment_order p
            <if test="deptIds != null and deptIds.size() > 0">
              JOIN vi_order o ON o.id = p.order_id
            </if>
            WHERE p.status = 'SUCCESS'
            <if test="deptIds != null and deptIds.size() > 0">
              AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
            </if>
            </script>
            """)
    Map<String, Object> statisticsPaymentKpi(@Param("deptIds") List<Long> deptIds);
    @Select("""
            <script>
            SELECT s.id AS station_id, s.name AS station_name,
                   COUNT(o.id) AS orders,
                   COALESCE(SUM(CASE WHEN p.status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS paid,
                   COALESCE(SUM(CASE WHEN o.id IS NOT NULL AND (p.status IS NULL OR p.status &lt;&gt; 'SUCCESS') THEN 1 ELSE 0 END), 0) AS unpaid,
                   COALESCE(SUM(CASE WHEN o.custody_type IN ('DRIVER','STATION')
                                      AND o.fulfillment_status NOT IN ('COMPLETED','CANCELLED','CLOSED')
                                     THEN 1 ELSE 0 END), 0) AS in_hand,
                   COALESCE(SUM(CASE WHEN o.returned_at IS NOT NULL THEN 1 ELSE 0 END), 0) AS returned
            FROM vi_station s
            LEFT JOIN vi_order o ON o.station_id = s.id
            <if test="deptIds != null and deptIds.size() > 0">
              AND o.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
            </if>
            LEFT JOIN vi_payment_order p ON p.order_id = o.id
            <where>
              <if test="deptIds != null and deptIds.size() > 0">
                s.dept_id IN <foreach collection="deptIds" item="scopeDeptId" open="(" separator="," close=")">#{scopeDeptId}</foreach>
              </if>
            </where>
            GROUP BY s.id, s.name
            ORDER BY s.id
            </script>
            """)
    List<Map<String, Object>> statisticsByStation(@Param("deptIds") List<Long> deptIds);
    @Select("""
            <script>
            SELECT e.*, o.order_no FROM vi_event_log e LEFT JOIN vi_order o ON o.id = e.order_id
            WHERE 1 = 1
            <if test="orderId != null"> AND e.order_id = #{orderId} </if>
            <if test="aggregateType != null and aggregateType != ''"> AND e.aggregate_type = #{aggregateType} </if>
            <if test="action != null and action != ''"> AND e.action = #{action} </if>
            <if test="actorRealm != null and actorRealm != ''"> AND e.actor_realm = #{actorRealm} </if>
            <if test="result != null and result != ''"> AND e.result = #{result} </if>
            ORDER BY e.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listEvents(@Param("orderId") Long orderId,
            @Param("aggregateType") String aggregateType, @Param("action") String action,
            @Param("actorRealm") String actorRealm, @Param("result") String result,
            @Param("offset") int offset, @Param("limit") int limit);
    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_event_log e WHERE 1 = 1
            <if test="orderId != null"> AND e.order_id = #{orderId} </if>
            <if test="aggregateType != null and aggregateType != ''"> AND e.aggregate_type = #{aggregateType} </if>
            <if test="action != null and action != ''"> AND e.action = #{action} </if>
            <if test="actorRealm != null and actorRealm != ''"> AND e.actor_realm = #{actorRealm} </if>
            <if test="result != null and result != ''"> AND e.result = #{result} </if>
            </script>
            """)
    long countEvents(@Param("orderId") Long orderId, @Param("aggregateType") String aggregateType,
            @Param("action") String action, @Param("actorRealm") String actorRealm,
            @Param("result") String result);
    @Select("""
            SELECT * FROM vi_outbox WHERE status IN ('READY','PROCESSING','FAILED')
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listOutbox(@Param("offset") int offset, @Param("limit") int limit);
    @Select("SELECT COUNT(1) FROM vi_outbox WHERE status IN ('READY','PROCESSING','FAILED')")
    long countOutbox();
    @Select("SELECT * FROM vi_outbox WHERE id = #{id}")
    Map<String, Object> findOutbox(@Param("id") Long id);
    @Update("""
            UPDATE vi_outbox SET status = 'READY', next_attempt_at = UTC_TIMESTAMP(3),
                   lease_until = NULL, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status <> 'DONE'
            """)
    int retryOutbox(@Param("id") Long id);
    @Select("""
            SELECT config_id, config_key, config_name, config_value, remark
            FROM sys_config WHERE config_key LIKE 'rongli.%' ORDER BY config_key
            """)
    List<Map<String, Object>> listRongliConfigs();
    @Select("SELECT config_id, config_key, config_name, config_value FROM sys_config WHERE config_key = #{key} LIMIT 1")
    Map<String, Object> findConfigByKey(@Param("key") String key);
    @Update("""
            UPDATE sys_config SET config_value = #{value}, update_time = NOW() WHERE config_key = #{key}
            """)
    int updateConfigValue(@Param("key") String key, @Param("value") String value);
}
