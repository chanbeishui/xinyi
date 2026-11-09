package com.xinyi.web.viit;

import static org.junit.jupiter.api.Assertions.*;

import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.training.dto.FrozenTrainingCourse;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * 仅给旧Q01消费端测试建立“Q03已经完成”的明确上游边界事实，不是学习或媒体验收。 Q03TrainingIT绝不使用本类：该套件必须从真实上传、课表、墙钟与阅卷产生TRAINING证明。
 * 这里连同政策、冻结计划、材料运行和已通过考试建立完整关系，不能再靠独立PASSED行放行。
 */
final class TrainingCompletedBoundaryFixture {
    private TrainingCompletedBoundaryFixture() {}

    static void seed(
            JdbcTemplate jdbc,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            long application) {
        assertEquals("xinyi_vi_it_e07", jdbc.queryForObject("SELECT DATABASE()", String.class));
        assertEquals(
                "XINYI_VEHICLE_ISOLATED",
                jdbc.queryForObject(
                        "SELECT kind FROM test_environment_guard WHERE id=1", String.class));
        Map<String, Object> context =
                jdbc.queryForMap(
                        "SELECT"
                            + " app.driver_id,app.material_revision,app.material_hash,profile.owner_dept_id,profile.training_context_revision"
                            + " FROM vi_driver_application app JOIN vi_driver_profile profile ON"
                            + " profile.id=app.driver_id WHERE app.id=?",
                        application);
        // 没有权威公司不能伪造可用培训证明；Q01对应测试须精确核对COMPANY_UNASSIGNED阻断。
        if (context.get("owner_dept_id") == null) return;
        long company = ((Number) context.get("owner_dept_id")).longValue();
        long driver = ((Number) context.get("driver_id")).longValue();
        long revision = ((Number) context.get("material_revision")).longValue();
        byte[] material = (byte[]) context.get("material_hash");
        long epoch = ((Number) context.get("training_context_revision")).longValue();
        assertNotNull(material);
        // 仅重建当前消费端边界夹具，不触碰其它申请的历史事实。
        jdbc.update(
                "DELETE FROM vi_training_playback WHERE progress_id IN (SELECT id FROM"
                        + " vi_training_progress WHERE application_id=?)",
                application);
        jdbc.update("DELETE FROM vi_training_attempt WHERE application_id=?", application);
        jdbc.update("DELETE FROM vi_training_progress WHERE application_id=?", application);
        jdbc.update("DELETE FROM vi_training_run WHERE application_id=?", application);
        jdbc.update("DELETE FROM vi_training_plan_snapshot WHERE application_id=?", application);

        long course = next(),
                media = next(),
                question = next(),
                policyRevision = next(),
                plan = next(),
                run = next(),
                progress = next(),
                attempt = next();
        String courseKey = "Q01_BOUNDARY_" + course;
        byte[] hash =
                HexFormat.of()
                        .parseHex(
                                crypto.seal(
                                                "Q01 upstream boundary course",
                                                "Q01:TRAINING_BOUNDARY:" + course)
                                        .fingerprint);
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_course(id,owner_dept_id,course_key,course_version,title,description,media_file_id,required_duration_seconds,pass_score,attempt_time_limit_seconds,status,created_by_user_id,updated_by_user_id,content_hash,published_by_user_id,published_at)"
                    + " VALUES"
                    + " (?,?,?,1,'Q01上游培训完成边界','不代表本测试执行过真实学习或媒体核验',?,1,80,60,'PUBLISHED',1,1,?,1,DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 3 SECOND))",
                course,
                company,
                courseKey,
                media,
                hash);
        // 消费端只使用可信上游元数据；此处不创建或声称存在可播放的视频原件。
        jdbc.update(
                "INSERT INTO"
                    + " vi_file(id,uploader_user_id,training_id,purpose,object_key,status,mime_type,server_size,server_sha256,media_duration_millis,media_width,media_height,media_video_codec)"
                    + " VALUES (?,1,?,'TRAINING_MEDIA',?,'READY','video/mp4',1,?,1000,1,1,'avc1')",
                media,
                course,
                UUID.randomUUID().toString().replace("-", ""),
                hash);
        List<Map<String, String>> options =
                List.of(Map.of("code", "A", "text", "边界选项甲"), Map.of("code", "B", "text", "边界选项乙"));
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_question(id,training_id,question_code,question_type,prompt,options_json,correct_option_codes,sort_no)"
                    + " VALUES (?,?,'Q1','SINGLE','Q01可信上游题卷边界',?,'[\"A\"]',1)",
                question,
                course,
                ViJson.write(options));

        List<Long> policies =
                jdbc.queryForList(
                        "SELECT id FROM vi_training_policy WHERE owner_dept_id=?",
                        Long.class,
                        company);
        long policy;
        if (policies.isEmpty()) {
            policy = next();
            jdbc.update(
                    "INSERT INTO vi_training_policy(id,owner_dept_id) VALUES (?,?)",
                    policy,
                    company);
        } else policy = policies.get(0);
        int policyNo =
                jdbc.queryForObject(
                        "SELECT COALESCE(MAX(revision_no),0)+1 FROM vi_training_policy_revision"
                                + " WHERE policy_id=?",
                        Integer.class,
                        policy);
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_policy_revision(id,policy_id,revision_no,required_training_ids,max_attempts,retry_interval_seconds,grading_mode,content_hash,created_by_user_id,published_by_user_id,published_at)"
                    + " VALUES (?,?,?,?"
                    + " ,3,0,'EQUAL_HALF_UP_EXACT',?,1,1,DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 3"
                    + " SECOND))",
                policyRevision,
                policy,
                policyNo,
                ViJson.write(List.of(Long.toString(course))),
                hash);
        jdbc.update(
                "UPDATE vi_training_policy SET"
                    + " active_revision_id=?,draft_revision_id=NULL,version=version+1,updated_at=UTC_TIMESTAMP(3)"
                    + " WHERE id=?",
                policyRevision,
                policy);
        FrozenTrainingCourse frozen =
                new FrozenTrainingCourse(
                        course,
                        courseKey,
                        1,
                        "Q01上游培训完成边界",
                        null,
                        media,
                        1000,
                        HexFormat.of().formatHex(hash),
                        1,
                        80,
                        60);
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_plan_snapshot(id,application_id,owner_dept_id,company_revision,policy_revision_id,course_snapshot,max_attempts,retry_interval_seconds,grading_mode)"
                    + " VALUES (?,?,?,?,?,?,3,0,'EQUAL_HALF_UP_EXACT')",
                plan,
                application,
                company,
                epoch,
                policyRevision,
                ViJson.write(List.of(frozen)));
        jdbc.update(
                "INSERT INTO"
                    + " vi_provider_verification(application_id,material_revision,material_hash,provider,task_type,provider_task_id,status,result_digest,verified_at)"
                    + " VALUES"
                    + " (?,?,?,'INTERNAL_TRAINING','TRAINING',?,'PASSED','q01-consumer-upstream-boundary',DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 1 SECOND))",
                application,
                revision,
                material,
                "Q03:RUN:" + run);
        // 与真实Q03共享同一证明表时必须使用数据库主键，不能用SEQ撞上生产AUTO_INCREMENT。
        long proof =
                jdbc.queryForObject(
                        "SELECT id FROM vi_provider_verification WHERE application_id=? AND"
                            + " provider='INTERNAL_TRAINING' AND task_type='TRAINING' AND"
                            + " provider_task_id=?",
                        Long.class,
                        application,
                        "Q03:RUN:" + run);
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_run(id,plan_snapshot_id,application_id,material_revision,material_hash,status,provider_verification_id,completed_at)"
                    + " VALUES (?,?,?,?,?,'PASSED',?,DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 SECOND))",
                run,
                plan,
                application,
                revision,
                material,
                proof);
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_progress(id,application_id,run_id,material_revision,material_hash,driver_id,training_id,verified_duration_seconds,verified_duration_millis,verified_segments,status)"
                    + " VALUES (?,?,?,?,?,?,?,1,1000,'[[0,1000]]','PASSED')",
                progress,
                application,
                run,
                revision,
                material,
                driver,
                course);
        String qid = Long.toString(question);
        List<Map<String, Object>> publicQuestions =
                List.of(
                        Map.of(
                                "questionId",
                                qid,
                                "type",
                                "SINGLE",
                                "prompt",
                                "Q01可信上游题卷边界",
                                "options",
                                options));
        List<Map<String, Object>> answers =
                List.of(Map.of("questionId", qid, "answerCodes", List.of("A")));
        Map<String, Object> grading =
                Map.of(
                        "correct",
                        Map.of(qid, List.of("A")),
                        "passScore",
                        80,
                        "gradingMode",
                        "EQUAL_HALF_UP_EXACT",
                        "courseVersion",
                        1,
                        "contentHash",
                        HexFormat.of().formatHex(hash));
        String aad =
                "Q03:APPLICATION:"
                        + application
                        + ":RUN:"
                        + run
                        + ":ATTEMPT:"
                        + attempt
                        + ":GRADING";
        jdbc.update(
                "INSERT INTO"
                    + " vi_training_attempt(id,application_id,run_id,material_revision,material_hash,driver_id,training_id,course_key,attempt_no,status,question_snapshot,grading_snapshot_cipher,answer_snapshot,submission_mac,score,started_at,expires_at,submitted_at)"
                    + " VALUES"
                    + " (?,?,?,?,?,?,?,?,1,'PASSED',?,?,?,?,100,DATE_SUB(UTC_TIMESTAMP(3),INTERVAL"
                    + " 2 SECOND),DATE_ADD(UTC_TIMESTAMP(3),INTERVAL 58"
                    + " SECOND),DATE_SUB(UTC_TIMESTAMP(3),INTERVAL 1 SECOND))",
                attempt,
                application,
                run,
                revision,
                material,
                driver,
                course,
                courseKey,
                ViJson.write(publicQuestions),
                crypto.envelope(ViJson.write(grading), aad),
                ViJson.write(answers),
                idem.mac(ViJson.write(answers)));
    }

    private static long next() {
        return PricingHttpFixture.SEQ.incrementAndGet();
    }
}
