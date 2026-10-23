package com.xinyi.rongli.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 事件流水、outbox、站内消息、幂等、争议、异常、评价、处罚与培训。
 */
@Mapper
public interface MiscMapper
{
    // ------------------------------------------------------------------ 事件与消息

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

    @Insert("""
            INSERT INTO vi_notification (account_id, role, order_id, scene, outbox_id, send_status, content,
                   created_at, updated_at, version)
            VALUES (#{accountId}, #{role}, #{orderId}, #{scene}, #{outboxId}, 'ACCEPTED', #{content},
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertNotification(Map<String, Object> row);

    @Update("""
            UPDATE vi_notification SET channel = #{channel}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int markNotificationChannel(@Param("id") Long id, @Param("channel") String channel);

    /** 直接读库取配置值，绕过框架缓存；订阅推送需"改完立刻生效" */
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{key} LIMIT 1")
    String findConfigValue(@Param("key") String key);

    @Select("""
            SELECT * FROM vi_notification WHERE account_id = #{accountId} AND role = #{role}
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listNotifications(@Param("accountId") Long accountId, @Param("role") String role,
            @Param("offset") int offset, @Param("limit") int limit);

    @Update("""
            UPDATE vi_notification SET read_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{messageId} AND account_id = #{accountId}
            """)
    int markNotificationRead(@Param("messageId") Long messageId, @Param("accountId") Long accountId);

    @Select("SELECT COUNT(1) FROM vi_notification WHERE account_id = #{accountId} AND read_at IS NULL")
    long countUnread(@Param("accountId") Long accountId);

    // ------------------------------------------------------------------ 幂等

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

    // ------------------------------------------------------------------ 争议与异常

    @Insert("""
            INSERT INTO vi_dispute (order_id, type, status, description, file_ids, created_at, updated_at, version)
            VALUES (#{orderId}, #{type}, 'OPEN', #{description}, #{fileIds}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDispute(Map<String, Object> row);

    @Select("SELECT * FROM vi_dispute WHERE order_id = #{orderId} ORDER BY id DESC LIMIT 1")
    Map<String, Object> findLatestDispute(@Param("orderId") Long orderId);

    @Select("SELECT * FROM vi_dispute WHERE id = #{id}")
    Map<String, Object> findDispute(@Param("id") Long id);

    @Select("""
            <script>
            SELECT d.*, o.order_no FROM vi_dispute d JOIN vi_order o ON o.id = d.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND d.status = #{status} </if>
            ORDER BY d.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listDisputes(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Update("""
            UPDATE vi_dispute SET status = 'RESOLVED', conclusion = #{conclusion}, handler_id = #{handler},
                   resolved_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status <> 'RESOLVED'
            """)
    int resolveDispute(@Param("id") Long id, @Param("conclusion") String conclusion, @Param("handler") Long handler);

    @Insert("""
            INSERT INTO vi_service_exception (order_id, type, status, description, file_ids, created_at, updated_at, version)
            VALUES (#{orderId}, #{type}, 'OPEN', #{description}, #{fileIds}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertException(Map<String, Object> row);

    @Select("""
            <script>
            SELECT e.*, o.order_no FROM vi_service_exception e JOIN vi_order o ON o.id = e.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND e.status = #{status} </if>
            ORDER BY e.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listExceptions(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT * FROM vi_service_exception WHERE id = #{id}")
    Map<String, Object> findException(@Param("id") Long id);

    @Update("""
            UPDATE vi_service_exception SET status = 'RESOLVED', resolution = #{resolution}, handler_id = #{handler},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status <> 'RESOLVED'
            """)
    int resolveException(@Param("id") Long id, @Param("resolution") String resolution, @Param("handler") Long handler);

    // ------------------------------------------------------------------ 评价

    @Insert("""
            INSERT INTO vi_review (order_id, owner_account_id, score, tags, content, created_at, updated_at, version)
            VALUES (#{orderId}, #{accountId}, #{score}, #{tags}, #{content}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE score = VALUES(score), tags = VALUES(tags), content = VALUES(content),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            """)
    int insertReview(Map<String, Object> row);

    @Select("SELECT * FROM vi_review WHERE order_id = #{orderId}")
    Map<String, Object> findReview(@Param("orderId") Long orderId);

    // ------------------------------------------------------------------ 处罚与申诉

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

    // ------------------------------------------------------------------ 隐私/导出

    @Insert("""
            INSERT INTO vi_governance_case (case_type, order_id, requester_account_id, requester_user_id, status,
                   payload_cipher, reason, created_at, updated_at, version)
            VALUES (#{caseType}, #{orderId}, #{requesterAccountId}, #{requesterUserId}, 'APPLIED',
                   #{payload}, #{reason}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertGovernanceCase(Map<String, Object> row);

    @Select("""
            <script>
            SELECT * FROM vi_governance_case
            <where>
              <if test="caseType != null and caseType != ''"> AND case_type = #{caseType} </if>
              <if test="status != null and status != ''"> AND status = #{status} </if>
            </where>
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listGovernanceCases(@Param("caseType") String caseType,
            @Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            SELECT * FROM vi_governance_case
            WHERE requester_account_id = #{accountId}
            ORDER BY id DESC LIMIT 50
            """)
    List<Map<String, Object>> listMyGovernanceCases(@Param("accountId") Long accountId);

    @Update("""
            UPDATE vi_governance_case SET status = #{status}, reviewer_id = #{reviewer}, reason = #{reason},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{caseId}
            """)
    int resolveGovernanceCase(@Param("caseId") Long caseId, @Param("status") String status,
            @Param("reviewer") Long reviewer, @Param("reason") String reason);

    // ------------------------------------------------------------------ 培训

    @Select("""
            SELECT id, course_key, course_version, title, description, required_duration_seconds,
                   pass_score, attempt_time_limit_seconds, published_at
            FROM vi_training_course WHERE status = 'PUBLISHED' ORDER BY id DESC
            """)
    List<Map<String, Object>> listPublishedCourses();

    @Select("SELECT * FROM vi_training_course WHERE id = #{trainingId}")
    Map<String, Object> findCourse(@Param("trainingId") Long trainingId);

    @Select("""
            SELECT id, question_code, question_type, prompt, options_json, sort_no
            FROM vi_training_question WHERE training_id = #{trainingId} ORDER BY sort_no
            """)
    List<Map<String, Object>> listQuestions(@Param("trainingId") Long trainingId);

    @Select("""
            SELECT * FROM vi_training_progress WHERE application_id = #{applicationId} AND training_id = #{trainingId}
            """)
    Map<String, Object> findProgress(@Param("applicationId") Long applicationId, @Param("trainingId") Long trainingId);

    @Insert("""
            INSERT INTO vi_training_progress (application_id, driver_id, training_id, verified_duration_seconds,
                   status, last_position_seconds, created_at, updated_at, version)
            VALUES (#{applicationId}, #{driverId}, #{trainingId}, 0, 'IN_PROGRESS', 0,
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            ON DUPLICATE KEY UPDATE updated_at = UTC_TIMESTAMP(3)
            """)
    int ensureProgress(Map<String, Object> row);

    @Update("""
            UPDATE vi_training_progress SET verified_duration_seconds = verified_duration_seconds + #{seconds},
                   last_position_seconds = #{position}, status = IF(verified_duration_seconds + #{seconds} >= #{required}, 'READY_FOR_EXAM', 'IN_PROGRESS'),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{progressId}
            """)
    int addProgress(@Param("progressId") Long progressId, @Param("seconds") int seconds,
            @Param("position") int position, @Param("required") int required);

    @Insert("""
            INSERT INTO vi_training_attempt (application_id, driver_id, training_id, attempt_no, status,
                   question_snapshot, started_at, expires_at, created_at, updated_at, version)
            VALUES (#{applicationId}, #{driverId}, #{trainingId}, #{attemptNo}, 'OPEN', #{questionSnapshot},
                   UTC_TIMESTAMP(3), #{expiresAt}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAttempt(Map<String, Object> row);

    @Select("SELECT * FROM vi_training_attempt WHERE id = #{attemptId}")
    Map<String, Object> findAttempt(@Param("attemptId") Long attemptId);

    @Select("SELECT * FROM vi_training_attempt WHERE application_id = #{applicationId} AND training_id = #{trainingId} AND status = 'OPEN'")
    Map<String, Object> findOpenAttempt(@Param("applicationId") Long applicationId,
            @Param("trainingId") Long trainingId);

    @Select("SELECT COALESCE(MAX(attempt_no), 0) FROM vi_training_attempt WHERE application_id = #{applicationId} AND training_id = #{trainingId}")
    int maxAttemptNo(@Param("applicationId") Long applicationId, @Param("trainingId") Long trainingId);

    @Update("""
            UPDATE vi_training_attempt SET status = #{status}, answer_snapshot = #{answers}, score = #{score},
                   submitted_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{attemptId} AND status = 'OPEN'
            """)
    int submitAttempt(@Param("attemptId") Long attemptId, @Param("status") String status,
            @Param("answers") String answers, @Param("score") int score);
}
