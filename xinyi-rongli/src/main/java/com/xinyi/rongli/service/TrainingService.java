package com.xinyi.rongli.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 代驾资格培训与考试。
 *
 * <p>学时只认服务器可核算的有效时长：每次上报最多采信"距上次上报的真实流逝时间"，
 * 快进或并行刷时长不会增加已核验学时。考试题目在开考时快照，正确选项不下发。</p>
 */
@Service
public class TrainingService
{
    private static final int SESSION_MINUTES = 60;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private Crypto crypto;

    @Autowired
    private EventService eventService;

    @Autowired
    private BizConfig config;

    // ------------------------------------------------------------------ 代驾侧

    public Map<String, Object> myTraining()
    {
        Row driver = requireDriver();
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        Long applicationId = application.isEmpty() ? null : application.lng("id");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : miscMapper.listPublishedCourses())
        {
            Row course = Row.of(c);
            Row progress = applicationId == null ? Row.empty()
                    : Row.of(miscMapper.findProgress(applicationId, course.lng("id")));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("trainingId", String.valueOf(course.lng("id")));
            item.put("courseKey", course.str("course_key"));
            item.put("courseVersion", course.intg("course_version"));
            item.put("title", course.str("title"));
            item.put("description", course.str("description"));
            item.put("requiredDurationSeconds", course.intg("required_duration_seconds", 0));
            item.put("passScore", course.intg("pass_score", 80));
            item.put("verifiedSeconds", progress.isEmpty() ? 0 : progress.intg("verified_duration_seconds", 0));
            item.put("progressStatus", progress.isEmpty() ? "NOT_STARTED" : progress.str("status"));
            item.put("canExam", !progress.isEmpty()
                    && ("READY_FOR_EXAM".equals(progress.str("status")) || "PASSED".equals(progress.str("status"))));
            rows.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationId", applicationId == null ? null : String.valueOf(applicationId));
        result.put("qualificationStatus", driver.str("qualification_status"));
        result.put("total", rows.size());
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> courseDetail(Long trainingId)
    {
        Row driver = requireDriver();
        Row course = requireCourse(trainingId);
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        Long applicationId = application.isEmpty() ? null : application.lng("id");
        Row progress = applicationId == null ? Row.empty()
                : Row.of(miscMapper.findProgress(applicationId, trainingId));

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("trainingId", String.valueOf(trainingId));
        view.put("courseKey", course.str("course_key"));
        view.put("courseVersion", course.intg("course_version"));
        view.put("title", course.str("title"));
        view.put("description", course.str("description"));
        view.put("mediaFileId", course.lng("media_file_id") == null ? null
                : String.valueOf(course.lng("media_file_id")));
        view.put("requiredDurationSeconds", course.intg("required_duration_seconds", 0));
        view.put("passScore", course.intg("pass_score", 80));
        view.put("attemptTimeLimitSeconds", course.intg("attempt_time_limit_seconds", 1800));
        view.put("verifiedSeconds", progress.isEmpty() ? 0 : progress.intg("verified_duration_seconds", 0));
        view.put("progressStatus", progress.isEmpty() ? "NOT_STARTED" : progress.str("status"));
        view.put("lastPositionSeconds", progress.isEmpty() ? 0 : progress.intg("last_position_seconds", 0));
        view.put("questionCount", miscMapper.listQuestions(trainingId).size());
        view.put("canExam", !progress.isEmpty()
                && ("READY_FOR_EXAM".equals(progress.str("status")) || "PASSED".equals(progress.str("status"))));
        return view;
    }

    /** 开始受控学习会话：签发一次性会话，服务端据此核算有效学时 */
    @Transactional
    public Map<String, Object> startPlayback(Long trainingId)
    {
        Row driver = requireDriver();
        requireCourse(trainingId);
        Long applicationId = requireApplicationId(driver);
        Map<String, Object> progressRow = new LinkedHashMap<>();
        progressRow.put("applicationId", applicationId);
        progressRow.put("driverId", driver.lng("id"));
        progressRow.put("trainingId", trainingId);
        miscMapper.ensureProgress(progressRow);
        Row progress = Row.of(miscMapper.findProgress(applicationId, trainingId));

        String sessionId = crypto.randomToken().substring(0, 32);
        // 同一进度只保留一个有效会话，避免多端并行刷时长
        adminMapper.expirePlaybacks(progress.lng("id"));
        Map<String, Object> playback = new LinkedHashMap<>();
        playback.put("progressId", progress.lng("id"));
        playback.put("sessionId", sessionId);
        playback.put("expiresAt", ViTime.now().plusMinutes(SESSION_MINUTES));
        adminMapper.insertPlayback(playback);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("sessionId", sessionId);
        view.put("playbackId", String.valueOf(playback.get("id")));
        view.put("expiresAt", ViTime.iso((LocalDateTime) playback.get("expiresAt")));
        view.put("resumeAtSeconds", progress.intg("last_position_seconds", 0));
        view.put("hint", "请保持前台播放，离开页面或快进不计入有效学时");
        view.put("heartbeatSeconds", config.trainingHeartbeatMaxGapSeconds());
        return view;
    }

    /**
     * 上报学习进度（同时作为播放心跳）。
     *
     * <p>有效学时由服务端按墙钟核算，客户端上报值只作上限参考：
     * <ol>
     *   <li>会话必须由本人在本课程签发且未过期，否则拒绝；</li>
     *   <li>采信时长 = min(客户端上报, 相邻心跳真实间隔, 心跳阈值)；间隔越长，
     *       超出阈值的部分视为"人不在场"，不计学时；</li>
     *   <li>序号必须递增，乱序/重放一律按 0 秒处理，不阻断幂等重试；</li>
     *   <li>播放位置不得超过上次位置 + 本次采信时长 + 容差，防快进刷时长。</li>
     * </ol>
     */
    @Transactional
    public Map<String, Object> reportProgress(Long trainingId, DriverDtos.ProgressReq req)
    {
        Row driver = requireDriver();
        Row course = requireCourse(trainingId);
        if (req == null || req.sessionId == null || req.sessionId.isBlank())
        {
            throw ApiException.validation("缺少学习会话");
        }
        Long applicationId = requireApplicationId(driver);
        Row progress = Row.of(miscMapper.findProgress(applicationId, trainingId));
        if (progress.isEmpty())
        {
            throw ApiException.stateConflict("请先开始学习会话");
        }
        Row playback = Row.of(adminMapper.findPlaybackBySession(progress.lng("id"), req.sessionId.trim()));
        if (playback.isEmpty())
        {
            throw ApiException.stateConflict("学习会话无效，请重新开始学习");
        }
        LocalDateTime expiresAt = playback.time("expires_at");
        if (expiresAt == null || expiresAt.isBefore(ViTime.now()))
        {
            throw ApiException.stateConflict("学习会话已过期，请重新开始学习");
        }

        int reported = req.deltaSeconds == null ? 0 : Math.max(0, req.deltaSeconds);
        if (reported > 600)
        {
            throw ApiException.validation("单次上报时长不能超过 600 秒");
        }

        int lastPosition = playback.intg("last_position_seconds", 0);
        int lastSequence = playback.intg("last_sequence", 0);
        int sequence = req.sequence == null ? lastSequence + 1 : req.sequence;
        LocalDateTime lastReportAt = playback.time("last_reported_at");
        int maxGap = config.trainingHeartbeatMaxGapSeconds();

        // 乱序或重放：幂等返回当前状态，不累加学时
        if (sequence <= lastSequence)
        {
            return progressView(trainingId, Row.of(miscMapper.findProgress(applicationId, trainingId)),
                    course.intg("required_duration_seconds", 0), 0, "DUPLICATE", lastPosition);
        }

        int allowance = reported;
        if (lastReportAt == null)
        {
            allowance = Math.min(reported, maxGap);
        }
        else
        {
            long gap = Math.max(0, Duration.between(lastReportAt, ViTime.now()).getSeconds());
            // 允许 2 秒网络/调度误差，避免首次心跳因取整被判为 0
            allowance = (int) Math.min(reported, gap + 2);
            if (gap > maxGap)
            {
                // 超时心跳：只认阈值内的在线时长
                allowance = Math.min(allowance, maxGap);
                eventService.record("TRAINING", trainingId, "HEARTBEAT_GAP", null, null,
                        Map.of("gapSeconds", String.valueOf(gap), "capSeconds", String.valueOf(maxGap)),
                        "相邻心跳间隔超阈值，超出部分不计学时");
            }
        }
        allowance = Math.max(0, allowance);

        int position = req.positionSeconds == null ? lastPosition : Math.max(0, req.positionSeconds);
        int positionCeiling = lastPosition + allowance + 3;
        if (position > positionCeiling)
        {
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "播放位置跳变，疑似快进，本次不计入学时");
        }
        position = Math.max(position, lastPosition);

        adminMapper.touchPlayback(playback.lng("id"), sequence, position);
        int required = course.intg("required_duration_seconds", 0);
        miscMapper.addProgress(progress.lng("id"), allowance, position, required);
        Row after = Row.of(miscMapper.findProgress(applicationId, trainingId));
        if ("READY_FOR_EXAM".equals(after.str("status")) && !"READY_FOR_EXAM".equals(progress.str("status")))
        {
            eventService.record("TRAINING", trainingId, "TRAINING_COMPLETED", null, null,
                    Map.of("verifiedSeconds", String.valueOf(after.intg("verified_duration_seconds", 0))), null);
        }
        return progressView(trainingId, after, required, allowance, "ACCEPTED", position);
    }

    private Map<String, Object> progressView(Long trainingId, Row progress, int required, int accepted,
            String result, int position)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("trainingId", String.valueOf(trainingId));
        view.put("verifiedSeconds", progress.intg("verified_duration_seconds", 0));
        view.put("requiredDurationSeconds", required);
        view.put("progressStatus", progress.str("status"));
        view.put("acceptedSeconds", accepted);
        view.put("lastPositionSeconds", position);
        view.put("heartbeatResult", result);
        view.put("canExam", "READY_FOR_EXAM".equals(progress.str("status")));
        return view;
    }

    /** 建立考试尝试：开考即快照题目，正确选项不下发 */
    @Transactional
    public Map<String, Object> createAttempt(Long trainingId)
    {
        Row driver = requireDriver();
        Row course = requireCourse(trainingId);
        Long applicationId = requireApplicationId(driver);
        Row progress = Row.of(miscMapper.findProgress(applicationId, trainingId));
        if (progress.isEmpty() || (!"READY_FOR_EXAM".equals(progress.str("status"))
                && !"PASSED".equals(progress.str("status"))))
        {
            throw ApiException.stateConflict("有效学时未达标，暂不能参加考试");
        }
        Map<String, Object> open = miscMapper.findOpenAttempt(applicationId, trainingId);
        if (open != null)
        {
            return attemptView(Row.of(open), course);
        }
        List<Map<String, Object>> questions = miscMapper.listQuestions(trainingId);
        if (questions.isEmpty())
        {
            throw ApiException.stateConflict("该课程尚未配置考试题目");
        }
        JSONArray snapshot = new JSONArray();
        for (Map<String, Object> q : questions)
        {
            Row question = Row.of(q);
            JSONObject item = new JSONObject();
            item.put("questionCode", question.str("question_code"));
            item.put("questionType", question.str("question_type"));
            item.put("prompt", question.str("prompt"));
            item.put("options", question.json("options_json") == null ? new JSONArray()
                    : JSON.parseArray(question.json("options_json")));
            snapshot.add(item);
        }
        int attemptNo = miscMapper.maxAttemptNo(applicationId, trainingId) + 1;
        int limit = course.intg("attempt_time_limit_seconds", 1800);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("applicationId", applicationId);
        row.put("driverId", driver.lng("id"));
        row.put("trainingId", trainingId);
        row.put("attemptNo", attemptNo);
        row.put("questionSnapshot", snapshot.toJSONString());
        row.put("expiresAt", ViTime.now().plusSeconds(limit));
        miscMapper.insertAttempt(row);
        Long attemptId = ((Number) row.get("id")).longValue();
        return attemptView(Row.of(miscMapper.findAttempt(attemptId)), course);
    }

    public Map<String, Object> attemptDetail(Long attemptId)
    {
        Row driver = requireDriver();
        Row attempt = Row.of(miscMapper.findAttempt(attemptId));
        if (attempt.isEmpty() || !Objects.equals(driver.lng("id"), attempt.lng("driver_id")))
        {
            throw ApiException.notFound("考试尝试不存在");
        }
        Row course = requireCourse(attempt.lng("training_id"));
        return attemptView(attempt, course);
    }

    /** 提交考试答案：服务端判卷，重复提交只认第一次 */
    @Transactional
    public Map<String, Object> submitAttempt(DriverDtos.AttemptSubmitReq req)
    {
        Row driver = requireDriver();
        if (req == null || req.attemptId == null)
        {
            throw ApiException.validation("缺少考试尝试");
        }
        Row attempt = Row.of(miscMapper.findAttempt(req.attemptId));
        if (attempt.isEmpty() || !Objects.equals(driver.lng("id"), attempt.lng("driver_id")))
        {
            throw ApiException.notFound("考试尝试不存在");
        }
        if (!"OPEN".equals(attempt.str("status")))
        {
            throw ApiException.stateConflict("该次考试已提交");
        }
        if (attempt.time("expires_at") != null && attempt.time("expires_at").isBefore(ViTime.now()))
        {
            adminMapper.closeAttempt(req.attemptId, "EXPIRED", null, 0);
            throw ApiException.stateConflict("本次考试已超时，请重新开考");
        }
        Long trainingId = attempt.lng("training_id");
        Row course = requireCourse(trainingId);
        Map<String, String> answers = new LinkedHashMap<>();
        if (req.answers != null)
        {
            for (Map.Entry<String, String> e : req.answers.entrySet())
            {
                answers.put(e.getKey(), e.getValue() == null ? "" : e.getValue().trim().toUpperCase());
            }
        }
        List<Map<String, Object>> questions = miscMapper.listQuestions(trainingId);
        int correct = 0;
        JSONArray detail = new JSONArray();
        for (Map<String, Object> q : questions)
        {
            Row question = Row.of(q);
            String code = question.str("question_code");
            String expected = question.str("correct_option_codes", "");
            String got = answers.getOrDefault(code, "");
            boolean ok = normalize(expected).equals(got);
            if (ok)
            {
                correct++;
            }
            JSONObject item = new JSONObject();
            item.put("questionCode", code);
            item.put("answer", got);
            item.put("correct", ok);
            if (!ok)
            {
                item.put("expected", normalize(expected));
            }
            detail.add(item);
        }
        int score = questions.isEmpty() ? 0 : Math.round(100f * correct / questions.size());
        int passScore = course.intg("pass_score", 80);
        String status = score >= passScore ? "PASSED" : "FAILED";
        if (adminMapper.closeAttempt(req.attemptId, status, JSON.toJSONString(answers), score) == 0)
        {
            throw ApiException.stateConflict("该次考试已提交，请刷新");
        }
        if ("PASSED".equals(status))
        {
            Row progress = Row.of(miscMapper.findProgress(attempt.lng("application_id"), trainingId));
            if (!progress.isEmpty())
            {
                adminMapper.setProgress(progress.lng("id"), progress.intg("verified_duration_seconds", 0),
                        progress.intg("last_position_seconds", 0), "PASSED");
            }
            eventService.record("TRAINING", trainingId, "EXAM_PASSED", null, null,
                    Map.of("score", String.valueOf(score)), null);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("attemptId", String.valueOf(req.attemptId));
        view.put("score", score);
        view.put("passScore", passScore);
        view.put("status", status);
        view.put("statusLabel", "PASSED".equals(status) ? "通过" : "未通过");
        view.put("correctCount", correct);
        view.put("totalCount", questions.size());
        view.put("details", detail);
        return view;
    }

    private String normalize(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : raw.toUpperCase().toCharArray())
        {
            if (Character.isLetterOrDigit(c))
            {
                sb.append(c);
            }
        }
        char[] arr = sb.toString().toCharArray();
        java.util.Arrays.sort(arr);
        return new String(arr);
    }

    private Map<String, Object> attemptView(Row attempt, Row course)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("attemptId", String.valueOf(attempt.lng("id")));
        view.put("trainingId", String.valueOf(attempt.lng("training_id")));
        view.put("attemptNo", attempt.intg("attempt_no", 1));
        view.put("status", attempt.str("status"));
        view.put("expiresAt", ViTime.iso(attempt.time("expires_at")));
        view.put("submittedAt", ViTime.iso(attempt.time("submitted_at")));
        view.put("score", attempt.intg("score", 0));
        view.put("passScore", course.intg("pass_score", 80));
        if ("OPEN".equals(attempt.str("status")))
        {
            view.put("questions", attempt.json("question_snapshot") == null ? new JSONArray()
                    : JSON.parseArray(attempt.json("question_snapshot")));
            view.put("hint", "本次考试限时 " + (course.intg("attempt_time_limit_seconds", 1800) / 60) + " 分钟，提交后不可修改");
        }
        return view;
    }

    // ------------------------------------------------------------------ 管理端课程

    public Map<String, Object> adminCourses(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : adminMapper.listCourses(status, offset, limit))
        {
            rows.add(courseView(Row.of(c)));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", adminMapper.countCourses(status));
        result.put("rows", rows);
        return result;
    }

    @Transactional
    public Map<String, Object> saveCourse(Long trainingId, DriverDtos.TrainingCourseReq req, Long userId)
    {
        if (req == null || req.title == null || req.title.isBlank())
        {
            throw ApiException.validation("请填写课程标题");
        }
        if (req.requiredDurationSeconds == null || req.requiredDurationSeconds <= 0)
        {
            throw ApiException.validation("请填写要求学时");
        }
        if (req.passScore != null && (req.passScore < 0 || req.passScore > 100))
        {
            throw ApiException.validation("及格分必须在 0-100 之间");
        }
        if (trainingId == null)
        {
            String key = req.courseKey == null || req.courseKey.isBlank()
                    ? "COURSE_" + crypto.randomDigits(6) : req.courseKey.trim().toUpperCase();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseKey", key);
            row.put("courseVersion", adminMapper.maxCourseVersion(key) + 1);
            row.put("title", req.title.trim());
            row.put("description", req.description);
            row.put("mediaFileId", req.mediaFileId);
            row.put("requiredDurationSeconds", req.requiredDurationSeconds);
            row.put("passScore", req.passScore == null ? 80 : req.passScore);
            row.put("attemptTimeLimitSeconds", req.attemptTimeLimitSeconds == null ? 1800 : req.attemptTimeLimitSeconds);
            adminMapper.insertCourse(row);
            Long id = ((Number) row.get("id")).longValue();
            if (req.questions != null)
            {
                saveQuestions(id, req.questions);
            }
            return courseView(Row.of(miscMapper.findCourse(id)));
        }
        Row existing = Row.of(miscMapper.findCourse(trainingId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("培训课程不存在");
        }
        if (!"DRAFT".equals(existing.str("status")))
        {
            throw ApiException.stateConflict("已发布课程不可修改，请另建新版本");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", trainingId);
        row.put("title", req.title.trim());
        row.put("description", req.description);
        row.put("mediaFileId", req.mediaFileId == null ? existing.lng("media_file_id") : req.mediaFileId);
        row.put("requiredDurationSeconds", req.requiredDurationSeconds);
        row.put("passScore", req.passScore == null ? existing.intg("pass_score", 80) : req.passScore);
        row.put("attemptTimeLimitSeconds", req.attemptTimeLimitSeconds == null
                ? existing.intg("attempt_time_limit_seconds", 1800) : req.attemptTimeLimitSeconds);
        adminMapper.updateCourse(row);
        if (req.questions != null)
        {
            saveQuestions(trainingId, req.questions);
        }
        return courseView(Row.of(miscMapper.findCourse(trainingId)));
    }

    private void saveQuestions(Long trainingId, List<DriverDtos.TrainingQuestionReq> questions)
    {
        // 草稿课程按提交内容整体替换题库：既支持删题，也避免重复保存撞唯一键
        adminMapper.deleteQuestions(trainingId);
        int sort = 1;
        for (DriverDtos.TrainingQuestionReq q : questions)
        {
            if (q == null || q.prompt == null || q.prompt.isBlank() || q.correctOptionCodes == null)
            {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("trainingId", trainingId);
            row.put("questionCode", q.questionCode == null || q.questionCode.isBlank()
                    ? "Q" + sort : q.questionCode.trim().toUpperCase());
            row.put("questionType", q.questionType == null ? "SINGLE" : q.questionType.toUpperCase());
            row.put("prompt", q.prompt.trim());
            row.put("optionsJson", normalizeOptions(q.options));
            row.put("correctOptionCodes", q.correctOptionCodes.trim().toUpperCase());
            row.put("sortNo", q.sortNo == null ? sort : q.sortNo);
            adminMapper.insertQuestion(row);
            sort++;
        }
    }

    /**
     * 选项统一存为 [{code:"A",text:"..."}]：小程序端按 code 作答、后端按 code 阅卷，
     * 若直接存字符串数组，客户端取不到 code 会导致选项无法作答。
     */
    private String normalizeOptions(List<String> options)
    {
        JSONArray array = new JSONArray();
        if (options != null)
        {
            char code = 'A';
            for (String text : options)
            {
                if (text == null || text.isBlank())
                {
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("code", String.valueOf(code));
                item.put("text", text.trim());
                array.add(item);
                code++;
            }
        }
        return array.toJSONString();
    }

    @Transactional
    public Map<String, Object> publishCourse(Long trainingId, Long userId)
    {
        Row course = Row.of(miscMapper.findCourse(trainingId));
        if (course.isEmpty())
        {
            throw ApiException.notFound("培训课程不存在");
        }
        if (!"DRAFT".equals(course.str("status")))
        {
            throw ApiException.stateConflict("该课程已发布");
        }
        if (miscMapper.listQuestions(trainingId).isEmpty())
        {
            throw ApiException.stateConflict("课程尚未配置考试题目，不能发布");
        }
        adminMapper.publishCourse(trainingId, userId);
        eventService.record("TRAINING", trainingId, "COURSE_PUBLISHED", null, null,
                Map.of("courseKey", course.str("course_key"),
                        "version", String.valueOf(course.intg("course_version"))), null);
        return courseView(Row.of(miscMapper.findCourse(trainingId)));
    }

    public Map<String, Object> adminCourse(Long trainingId)
    {
        Row course = Row.of(miscMapper.findCourse(trainingId));
        if (course.isEmpty())
        {
            throw ApiException.notFound("培训课程不存在");
        }
        Map<String, Object> view = courseView(course);
        List<Map<String, Object>> questions = new ArrayList<>();
        for (Map<String, Object> q : miscMapper.listQuestions(trainingId))
        {
            questions.add(questionView(Row.of(q)));
        }
        view.put("questions", questions);
        return view;
    }

    private Map<String, Object> courseView(Row c)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("trainingId", String.valueOf(c.lng("id")));
        view.put("courseKey", c.str("course_key"));
        view.put("courseVersion", c.intg("course_version"));
        view.put("title", c.str("title"));
        view.put("description", c.str("description"));
        view.put("mediaFileId", c.lng("media_file_id") == null ? null : String.valueOf(c.lng("media_file_id")));
        view.put("requiredDurationSeconds", c.intg("required_duration_seconds", 0));
        view.put("passScore", c.intg("pass_score", 80));
        view.put("attemptTimeLimitSeconds", c.intg("attempt_time_limit_seconds", 1800));
        view.put("status", c.str("status"));
        view.put("statusLabel", switch (c.str("status", ""))
        {
            case "DRAFT" -> "草稿";
            case "PUBLISHED" -> "已发布";
            case "DISABLED" -> "已停用";
            default -> c.str("status");
        });
        view.put("publishedAt", ViTime.iso(c.time("published_at")));
        return view;
    }

    private Map<String, Object> questionView(Row q)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionCode", q.str("question_code"));
        item.put("questionType", q.str("question_type"));
        item.put("prompt", q.str("prompt"));
        item.put("options", q.json("options_json") == null ? new JSONArray() : JSON.parseArray(q.json("options_json")));
        item.put("correctOptionCodes", q.str("correct_option_codes"));
        item.put("sortNo", q.intg("sort_no", 0));
        return item;
    }

    private Row requireCourse(Long trainingId)
    {
        Row course = Row.of(miscMapper.findCourse(trainingId));
        if (course.isEmpty())
        {
            throw ApiException.notFound("培训课程不存在");
        }
        return course;
    }

    private Long requireApplicationId(Row driver)
    {
        Row application = Row.of(accountMapper.findLatestApplication(driver.lng("id")));
        if (application.isEmpty())
        {
            throw ApiException.stateConflict("请先提交代驾资格申请，再参加培训");
        }
        return application.lng("id");
    }

    private Row requireDriver()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("当前账号还不是代驾员");
        }
        return driver;
    }
}
