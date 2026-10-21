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
 * 管理端聚合查询：结果更正、审计流水、outbox 补偿、治理台账、处罚申诉、培训课程与统计。
 *
 * <p>与 {@link MiscMapper} 的分工：MiscMapper 提供业务写入原语（申请、登记、审核），
 * 本 Mapper 提供带筛选与分页的管理端读取及少量运维动作。</p>
 */
@Mapper
public interface AdminMapper
{
    // ------------------------------------------------------------------ 计数（配合 MiscMapper 的列表）

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_dispute d WHERE 1 = 1
            <if test="status != null and status != ''"> AND d.status = #{status} </if>
            </script>
            """)
    long countDisputes(@Param("status") String status);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_service_exception e WHERE 1 = 1
            <if test="status != null and status != ''"> AND e.status = #{status} </if>
            </script>
            """)
    long countExceptions(@Param("status") String status);

    @Select("SELECT * FROM vi_dispute WHERE order_id = #{orderId} ORDER BY id DESC")
    List<Map<String, Object>> listDisputesOfOrder(@Param("orderId") Long orderId);

    @Select("""
            SELECT d.*, o.order_no, o.fulfillment_status FROM vi_dispute d
            JOIN vi_order o ON o.id = d.order_id WHERE d.id = #{disputeId}
            """)
    Map<String, Object> findDisputeDetail(@Param("disputeId") Long disputeId);

    @Select("""
            SELECT e.*, o.order_no, o.fulfillment_status FROM vi_service_exception e
            JOIN vi_order o ON o.id = e.order_id WHERE e.id = #{exceptionId}
            """)
    Map<String, Object> findExceptionDetail(@Param("exceptionId") Long exceptionId);

    // ------------------------------------------------------------------ 结果更正

    @Select("""
            <script>
            SELECT c.*, o.order_no FROM vi_result_correction c JOIN vi_order o ON o.id = c.order_id
            WHERE 1 = 1
            <if test="status != null and status != ''"> AND c.status = #{status} </if>
            ORDER BY c.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listCorrections(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_result_correction c WHERE 1 = 1
            <if test="status != null and status != ''"> AND c.status = #{status} </if>
            </script>
            """)
    long countCorrections(@Param("status") String status);

    @Select("""
            SELECT c.*, o.order_no FROM vi_result_correction c JOIN vi_order o ON o.id = c.order_id
            WHERE c.id = #{correctionId}
            """)
    Map<String, Object> findCorrection(@Param("correctionId") Long correctionId);

    @Update("""
            UPDATE vi_result_correction SET status = #{status}, reviewer_id = #{reviewerId},
                   opinion = #{opinion}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{correctionId} AND status = 'APPLIED'
            """)
    int auditCorrection(@Param("correctionId") Long correctionId, @Param("status") String status,
            @Param("reviewerId") Long reviewerId, @Param("opinion") String opinion);

    @Select("SELECT * FROM vi_result_correction WHERE order_id = #{orderId} ORDER BY id DESC")
    List<Map<String, Object>> listCorrectionsOfOrder(@Param("orderId") Long orderId);

    // ------------------------------------------------------------------ 审计事件

    @Select("""
            <script>
            SELECT e.*, o.order_no FROM vi_event_log e LEFT JOIN vi_order o ON o.id = e.order_id
            WHERE 1 = 1
            <if test="orderId != null"> AND e.order_id = #{orderId} </if>
            <if test="aggregateType != null and aggregateType != ''"> AND e.aggregate_type = #{aggregateType} </if>
            <if test="action != null and action != ''"> AND e.action = #{action} </if>
            ORDER BY e.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listEvents(@Param("orderId") Long orderId,
            @Param("aggregateType") String aggregateType, @Param("action") String action,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_event_log e WHERE 1 = 1
            <if test="orderId != null"> AND e.order_id = #{orderId} </if>
            <if test="aggregateType != null and aggregateType != ''"> AND e.aggregate_type = #{aggregateType} </if>
            <if test="action != null and action != ''"> AND e.action = #{action} </if>
            </script>
            """)
    long countEvents(@Param("orderId") Long orderId, @Param("aggregateType") String aggregateType,
            @Param("action") String action);

    // ------------------------------------------------------------------ outbox 补偿队列

    @Select("""
            SELECT * FROM vi_outbox WHERE status IN ('READY','PROCESSING','FAILED')
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            """)
    List<Map<String, Object>> listOutbox(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM vi_outbox WHERE status IN ('READY','PROCESSING','FAILED')")
    long countOutbox();

    @Select("SELECT * FROM vi_outbox WHERE id = #{id}")
    Map<String, Object> findOutbox(@Param("id") Long id);

    /** 受控重试：重置为 READY 并把下次尝试拉到当下 */
    @Update("""
            UPDATE vi_outbox SET status = 'READY', next_attempt_at = UTC_TIMESTAMP(3),
                   lease_until = NULL, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status <> 'DONE'
            """)
    int retryOutbox(@Param("id") Long id);

    // ------------------------------------------------------------------ 治理台账（隐私 / 导出 / 对账）

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
            SELECT g.*, o.order_no FROM vi_governance_case g LEFT JOIN vi_order o ON o.id = g.order_id
            WHERE g.case_type = #{caseType}
            <if test="status != null and status != ''"> AND g.status = #{status} </if>
            ORDER BY g.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listGovernance(@Param("caseType") String caseType, @Param("status") String status,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_governance_case g WHERE g.case_type = #{caseType}
            <if test="status != null and status != ''"> AND g.status = #{status} </if>
            </script>
            """)
    long countGovernance(@Param("caseType") String caseType, @Param("status") String status);

    @Select("""
            SELECT g.*, o.order_no FROM vi_governance_case g LEFT JOIN vi_order o ON o.id = g.order_id
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

    // ------------------------------------------------------------------ 处罚与申诉

    @Select("""
            <script>
            SELECT p.*, a.nickname AS driver_name FROM vi_penalty p
            JOIN vi_driver_profile d ON d.id = p.driver_id
            JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="driverId != null"> AND p.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND p.status = #{status} </if>
            ORDER BY p.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listPenalties(@Param("driverId") Long driverId, @Param("status") String status,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_penalty p WHERE 1 = 1
            <if test="driverId != null"> AND p.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND p.status = #{status} </if>
            </script>
            """)
    long countPenalties(@Param("driverId") Long driverId, @Param("status") String status);

    @Select("""
            <script>
            SELECT ap.*, p.reason AS penalty_reason, p.penalty_type, a.nickname AS driver_name
            FROM vi_penalty_appeal ap
            JOIN vi_penalty p ON p.id = ap.penalty_id
            JOIN vi_driver_profile d ON d.id = ap.driver_id
            JOIN vi_account a ON a.id = d.account_id
            WHERE 1 = 1
            <if test="driverId != null"> AND ap.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND ap.status = #{status} </if>
            ORDER BY ap.id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listAppeals(@Param("driverId") Long driverId, @Param("status") String status,
            @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_penalty_appeal ap WHERE 1 = 1
            <if test="driverId != null"> AND ap.driver_id = #{driverId} </if>
            <if test="status != null and status != ''"> AND ap.status = #{status} </if>
            </script>
            """)
    long countAppeals(@Param("driverId") Long driverId, @Param("status") String status);

    @Select("""
            SELECT ap.*, p.reason AS penalty_reason, p.penalty_type, a.nickname AS driver_name
            FROM vi_penalty_appeal ap
            JOIN vi_penalty p ON p.id = ap.penalty_id
            JOIN vi_driver_profile d ON d.id = ap.driver_id
            JOIN vi_account a ON a.id = d.account_id
            WHERE ap.id = #{appealId}
            """)
    Map<String, Object> findAppealDetail(@Param("appealId") Long appealId);

    // ------------------------------------------------------------------ 培训课程管理

    @Select("""
            <script>
            SELECT * FROM vi_training_course WHERE 1 = 1
            <if test="status != null and status != ''"> AND status = #{status} </if>
            ORDER BY id DESC LIMIT #{offset}, #{limit}
            </script>
            """)
    List<Map<String, Object>> listCourses(@Param("status") String status, @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1) FROM vi_training_course WHERE 1 = 1
            <if test="status != null and status != ''"> AND status = #{status} </if>
            </script>
            """)
    long countCourses(@Param("status") String status);

    @Insert("""
            INSERT INTO vi_training_course (course_key, course_version, title, description, media_file_id,
                   required_duration_seconds, pass_score, attempt_time_limit_seconds, status,
                   created_at, updated_at, version)
            VALUES (#{courseKey}, #{courseVersion}, #{title}, #{description}, #{mediaFileId},
                   #{requiredDurationSeconds}, #{passScore}, #{attemptTimeLimitSeconds}, 'DRAFT',
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCourse(Map<String, Object> row);

    @Select("SELECT COALESCE(MAX(course_version), 0) FROM vi_training_course WHERE course_key = #{courseKey}")
    int maxCourseVersion(@Param("courseKey") String courseKey);

    @Update("""
            UPDATE vi_training_course SET title = #{title}, description = #{description},
                   media_file_id = #{mediaFileId}, required_duration_seconds = #{requiredDurationSeconds},
                   pass_score = #{passScore}, attempt_time_limit_seconds = #{attemptTimeLimitSeconds},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id} AND status = 'DRAFT'
            """)
    int updateCourse(Map<String, Object> row);

    @Update("""
            UPDATE vi_training_course SET status = 'PUBLISHED', published_at = UTC_TIMESTAMP(3),
                   published_by_user_id = #{userId}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{trainingId} AND status = 'DRAFT'
            """)
    int publishCourse(@Param("trainingId") Long trainingId, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO vi_training_question (training_id, question_code, question_type, prompt, options_json,
                   correct_option_codes, sort_no, created_at, updated_at, version)
            VALUES (#{trainingId}, #{questionCode}, #{questionType}, #{prompt}, #{optionsJson},
                   #{correctOptionCodes}, #{sortNo}, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertQuestion(Map<String, Object> row);

    @Delete("DELETE FROM vi_training_question WHERE training_id = #{trainingId}")
    int deleteQuestions(@Param("trainingId") Long trainingId);

    @Select("""
            SELECT * FROM vi_training_progress WHERE driver_id = #{driverId} AND training_id = #{trainingId}
            """)
    Map<String, Object> findDriverProgress(@Param("driverId") Long driverId,
            @Param("trainingId") Long trainingId);

    @Select("""
            SELECT * FROM vi_training_progress WHERE application_id = #{applicationId} ORDER BY id DESC
            """)
    List<Map<String, Object>> listProgressOfApplication(@Param("applicationId") Long applicationId);

    @Insert("""
            INSERT INTO vi_training_playback (progress_id, session_id, started_at, expires_at, last_reported_at,
                   last_sequence, created_at, updated_at, version)
            VALUES (#{progressId}, #{sessionId}, UTC_TIMESTAMP(3), #{expiresAt}, UTC_TIMESTAMP(3), 0,
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPlayback(Map<String, Object> row);

    @Select("SELECT * FROM vi_training_playback WHERE id = #{id}")
    Map<String, Object> findPlayback(@Param("id") Long id);

    @Update("""
            UPDATE vi_training_playback SET last_reported_at = UTC_TIMESTAMP(3), last_sequence = #{sequence},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int touchPlayback(@Param("id") Long id, @Param("sequence") int sequence);

    @Update("""
            UPDATE vi_training_progress SET verified_duration_seconds = #{seconds},
                   last_position_seconds = #{position}, status = #{status},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{progressId}
            """)
    int setProgress(@Param("progressId") Long progressId, @Param("seconds") int seconds,
            @Param("position") int position, @Param("status") String status);

    @Update("""
            UPDATE vi_training_attempt SET status = #{status}, answer_snapshot = #{answers},
                   score = #{score}, active_attempt = NULL, submitted_at = UTC_TIMESTAMP(3),
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{attemptId} AND status = 'OPEN'
            """)
    int closeAttempt(@Param("attemptId") Long attemptId, @Param("status") String status,
            @Param("answers") String answers, @Param("score") int score);

    @Select("SELECT * FROM vi_face_session WHERE verification_id = #{verificationId}")
    Map<String, Object> findFaceSession(@Param("verificationId") String verificationId);

    @Insert("""
            INSERT INTO vi_face_session (application_id, verification_id, account_id, status, launch_mode,
                   launch_payload_cipher, consent_version, expires_at, result_available, created_at, updated_at, version)
            VALUES (#{applicationId}, #{verificationId}, #{accountId}, 'CREATED', #{launchMode},
                   #{launchPayloadCipher}, #{consentVersion}, #{expiresAt}, 0,
                   UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 0)
            """)
    int insertFaceSession(Map<String, Object> row);

    @Update("""
            UPDATE vi_face_session SET result_available = #{available}, status = #{status},
                   failure_code = #{failureCode}, updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int updateFaceResult(@Param("id") Long id, @Param("available") boolean available,
            @Param("status") String status, @Param("failureCode") String failureCode);

    // ------------------------------------------------------------------ 业务配置（复用框架 sys_config）

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

    // ------------------------------------------------------------------ 统计

    @Select("""
            SELECT
              (SELECT COUNT(1) FROM vi_order
                 WHERE fulfillment_status NOT IN ('COMPLETED','CANCELLED','CLOSED')) AS active_orders,
              (SELECT COUNT(1) FROM vi_order
                 WHERE DATE(CONVERT_TZ(created_at,'+00:00','+08:00')) = #{bizDate}) AS orders_today,
              (SELECT COUNT(1) FROM vi_aftersale_case WHERE case_status = 'PENDING_REVIEW') AS aftersale_pending,
              (SELECT COUNT(1) FROM vi_dispute WHERE status IN ('OPEN','PROCESSING')) AS dispute_open,
              (SELECT COUNT(1) FROM vi_service_exception WHERE status IN ('OPEN','PROCESSING')) AS exception_open,
              (SELECT COUNT(1) FROM vi_result_correction WHERE status = 'APPLIED') AS correction_pending,
              (SELECT COUNT(1) FROM vi_penalty_appeal WHERE status = 'SUBMITTED') AS appeal_pending,
              (SELECT COUNT(1) FROM vi_driver_application WHERE status = 'SUBMITTED') AS qualification_pending
            """)
    Map<String, Object> statisticsOverview(@Param("bizDate") String bizDate);
}
