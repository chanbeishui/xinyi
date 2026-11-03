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
public interface TrainingMapper
{
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
