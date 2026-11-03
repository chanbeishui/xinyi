package com.xinyi.rongli.driver.mapper;

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
 * driver 域管理端查询。由原 AdminMapper 按功能域拆分而来，SQL 原样迁移。
 */
@Mapper
public interface AdminDriverMapper
{
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
            UPDATE vi_training_playback SET expires_at = UTC_TIMESTAMP(3), updated_at = UTC_TIMESTAMP(3),
                   version = version + 1
            WHERE progress_id = #{progressId} AND expires_at > UTC_TIMESTAMP(3)
            """)
    int expirePlaybacks(@Param("progressId") Long progressId);
    @Select("""
            SELECT * FROM vi_training_playback
            WHERE progress_id = #{progressId} AND session_id = #{sessionId}
            ORDER BY id DESC LIMIT 1
            """)
    Map<String, Object> findPlaybackBySession(@Param("progressId") Long progressId,
            @Param("sessionId") String sessionId);
    @Update("""
            UPDATE vi_training_playback SET last_reported_at = UTC_TIMESTAMP(3), last_sequence = #{sequence},
                   last_position_seconds = #{position},
                   updated_at = UTC_TIMESTAMP(3), version = version + 1
            WHERE id = #{id}
            """)
    int touchPlayback(@Param("id") Long id, @Param("sequence") int sequence,
            @Param("position") int position);
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
}
