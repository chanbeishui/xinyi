package com.xinyi.rongli.training.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.file.domain.PrivateFile;
import com.xinyi.rongli.file.mapper.PrivateFileMapper;
import com.xinyi.rongli.file.service.FileAuthority;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.qualification.domain.QualificationApplication;
import com.xinyi.rongli.qualification.mapper.QualificationMapper;
import com.xinyi.rongli.training.domain.*;
import com.xinyi.rongli.training.dto.FrozenTrainingCourse;
import com.xinyi.rongli.training.dto.LearningRequests.*;
import com.xinyi.rongli.training.mapper.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;

/** 本人受控学习与考试。目录→会话→档案→申请→进度的父锁串行化计时、次数、阅卷及汇总证明。 */
@Service
public class LearningService {
    private final LearningMapper mapper;
    private final QualificationMapper qualifications;
    private final TrainingMapper courses;
    private final PrivateFileMapper files;
    private final FileAuthority authority;
    private final ViAccountMapper accounts;
    private final BusinessActorResolver actors;
    private final IntegrationCrypto crypto;
    private final TrainingCompletionAuthority completion;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;
    private static final long MAX_HEARTBEAT_MILLIS = 15000;

    public LearningService(
            LearningMapper mapper,
            QualificationMapper qualifications,
            TrainingMapper courses,
            PrivateFileMapper files,
            FileAuthority authority,
            ViAccountMapper accounts,
            BusinessActorResolver actors,
            IntegrationCrypto crypto,
            TrainingCompletionAuthority completion,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.qualifications = qualifications;
        this.courses = courses;
        this.files = files;
        this.authority = authority;
        this.accounts = accounts;
        this.actors = actors;
        this.crypto = crypto;
        this.completion = completion;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long id, long runId) {}

    private record Context(
            DriverProfile profile,
            QualificationApplication application,
            TrainingPlan plan,
            TrainingRun run,
            TrainingPolicyRevision policy,
            List<FrozenTrainingCourse> selected) {}

    public Map<String, Object> get() {
        Actor actor = actors.requireMini();
        return transaction(
                actor,
                "TRAINING_QUERY",
                () -> {
                    Context context = context(actor, null, false);
                    return context == null ? null : trainingView(context);
                });
    }

    public Map<String, Object> unit(long trainingId) {
        Actor actor = actors.requireMini();
        return transaction(
                actor,
                "TRAINING_UNIT_QUERY",
                () -> {
                    Context context = context(actor, null, true);
                    return unitView(context, selected(context, trainingId));
                });
    }

    private Context context(Actor actor, Long requested, boolean required) {
        authority.guard(actor, "biz:driverqualification:query");
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (account.getPhoneVerifiedAt() == null || account.getPhoneCipher() == null)
            throw new ViException(ViErrorCode.VI_PHONE_PROOF_REQUIRED);
        DriverProfile profile = qualifications.lockProfile(actor.accountId());
        if (profile == null) {
            if (required || requested != null) invisible();
            return null;
        }
        QualificationApplication current = qualifications.current(profile.getId());
        if (requested != null) {
            var target = qualifications.lock(requested);
            if (target == null || !Objects.equals(target.getDriverId(), profile.getId()))
                invisible();
            if (current == null || !Objects.equals(current.getId(), requested)) conflict();
        }
        if (current == null) {
            if (required) invisible();
            return null;
        }
        current = qualifications.lock(current.getId());
        TrainingPlan plan = mapper.plan(current.getId(), profile.getTrainingContextRevision());
        if (plan != null && !Objects.equals(plan.getOwnerDeptId(), profile.getOwnerDeptId()))
            state("培训冻结公司与当前档案不一致，请联系管理员核查");
        TrainingRun run =
                plan == null ? null : mapper.run(plan.getId(), current.getMaterialRevision());
        if (run != null
                && !TrainingCompletionAuthority.equal(
                        run.getMaterialHash(), current.getMaterialHash()))
            state("当前材料运行摘要不一致，请重新核查材料");
        TrainingPolicyRevision policy = null;
        List<FrozenTrainingCourse> selected = List.of();
        if (plan != null) {
            policy = mapper.revision(plan.getPolicyRevisionId());
            selected = TrainingCompletionAuthority.courses(plan);
        } else if (profile.getOwnerDeptId() != null) {
            var aggregate = mapper.policy(profile.getOwnerDeptId());
            if (aggregate != null && aggregate.getActiveRevisionId() != null)
                policy = mapper.revision(aggregate.getActiveRevisionId());
            if (policy != null
                    && policy.getPublishedAt() != null
                    && policy.getMaxAttempts() != null
                    && policy.getRetryIntervalSeconds() != null)
                selected = previewCourses(profile.getOwnerDeptId(), policy);
        }
        return new Context(profile, current, plan, run, policy, selected);
    }

    private List<FrozenTrainingCourse> previewCourses(long owner, TrainingPolicyRevision policy) {
        List<FrozenTrainingCourse> result = new ArrayList<>();
        for (long id : TrainingPolicyService.ids(policy)) {
            TrainingCourse course = courses.find(id);
            if (!TrainingPolicyService.validPublished(course)
                    || !Objects.equals(course.getOwnerDeptId(), owner)) return List.of();
            PrivateFile media = files.find(course.getMediaFileId());
            if (media == null
                    || !"READY".equals(media.getStatus())
                    || !"TRAINING_MEDIA".equals(media.getPurpose())
                    || media.getMediaDurationMillis() == null
                    || media.getMediaDurationMillis() < course.getRequiredDurationSeconds() * 1000L)
                return List.of();
            result.add(
                    new FrozenTrainingCourse(
                            id,
                            course.getCourseKey(),
                            course.getCourseVersion(),
                            course.getTitle(),
                            course.getDescription(),
                            media.getId(),
                            media.getMediaDurationMillis(),
                            HexFormat.of().formatHex(course.getContentHash()),
                            course.getRequiredDurationSeconds(),
                            course.getPassScore(),
                            course.getAttemptTimeLimitSeconds()));
        }
        return List.copyOf(result);
    }

    private List<Map<String, String>> blockers(Context context) {
        List<Map<String, String>> blocked = new ArrayList<>();
        var profile = context.profile();
        var app = context.application();
        if (profile.getOwnerDeptId() == null)
            blocked.add(blocker("ownerDeptId", "COMPANY_UNASSIGNED", "请联系管理员明确归属公司"));
        else {
            var company = qualifications.company(profile.getOwnerDeptId());
            if (company == null
                    || !"0".equals(company.get("status"))
                    || !"0".equals(company.get("del_flag")))
                blocked.add(blocker("ownerDeptId", "COMPANY_UNAVAILABLE", "归属公司当前不可用"));
        }
        if (!Set.of("DRAFT", "REJECTED").contains(app.getStatus()))
            blocked.add(blocker("applicationId", "APPLICATION_FROZEN", "当前申请已冻结，不能新增学习或考试"));
        if (app.getMaterialHash() == null)
            blocked.add(blocker("materialRevision", "MATERIAL_UNAVAILABLE", "请先保存本人申请材料"));
        if (context.policy() == null
                || context.policy().getPublishedAt() == null
                || context.selected().isEmpty())
            blocked.add(blocker("policyRevisionId", "POLICY_UNAVAILABLE", "公司尚未发布完整必修课程及考试政策"));
        return blocked;
    }

    private void requireReady(Context context) {
        var blocked = blockers(context);
        if (!blocked.isEmpty())
            throw new ViException(ViErrorCode.VI_TRAINING_NOT_READY, blocked.get(0).get("message"));
    }

    private FrozenTrainingCourse selected(Context context, long training) {
        return context.selected().stream()
                .filter(course -> course.trainingId() == training)
                .findFirst()
                .orElseThrow(() -> new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE));
    }

    private Context requireRun(Actor actor, long application, long requestedRun) {
        Context context = context(actor, application, true);
        if (context.run() == null || context.run().getId() != requestedRun) conflict();
        requireReady(context);
        return context;
    }

    public Map<String, Object> playback(long training, Start input, String key) {
        Actor actor = actors.requireMini();
        return transaction(
                actor, "TRAINING_PLAYBACK", () -> playbackLocked(actor, training, input, key));
    }

    private Map<String, Object> playbackLocked(
            Actor actor, long training, Start input, String key) {
        Context context = context(actor, input.applicationId(), true);
        requireReady(context);
        requireStartEpoch(context, input);
        selected(context, training);
        Receipt receipt =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "TRAINING_PLAYBACK:" + input.applicationId() + ":" + training,
                        key,
                        ViJson.write(input),
                        true,
                        Receipt.class,
                        () -> startPlayback(actor, context, training, input));
        Context latest = context(actor, input.applicationId(), true);
        if (latest.run() == null || latest.run().getId() != receipt.runId()) conflict();
        return playbackView(latest, mapper.playback(receipt.id()), selected(latest, training));
    }

    private void requireStartEpoch(Context context, Start input) {
        if (context.profile().getTrainingContextRevision() != input.expectedCompanyRevision()
                || context.application().getMaterialRevision() != input.expectedMaterialRevision()
                || context.policy() == null
                || context.policy().getId() != input.expectedPolicyRevisionId()) conflict();
    }

    private Receipt startPlayback(Actor actor, Context context, long training, Start input) {
        if (context.application().getVersion() != input.expectedApplicationVersion()
                || !Objects.equals(
                        context.plan() == null ? null : context.plan().getId(), input.planId())
                || !Objects.equals(
                        context.run() == null ? null : context.run().getId(), input.runId()))
            conflict();
        FrozenTrainingCourse course = selected(context, training);
        if (course.courseVersion() != input.courseVersion()) conflict();
        Context initialized = initialize(context);
        TrainingProgress progress = mapper.progress(initialized.run().getId(), training);
        if (progress == null) {
            if (input.expectedVersion() != 0) conflict();
            progress = new TrainingProgress();
            progress.setApplicationId(context.application().getId());
            progress.setRunId(initialized.run().getId());
            progress.setDriverId(context.profile().getId());
            progress.setTrainingId(training);
            progress.setMaterialRevision(context.application().getMaterialRevision());
            progress.setMaterialHash(context.application().getMaterialHash());
            progress.setVerifiedSegments("[]");
            progress.setStatus("NOT_STARTED");
            mapper.insertProgress(progress);
        }
        if (progress.getVersion() != input.expectedVersion()) conflict();
        var playback = new TrainingPlaybackSession();
        playback.setRunId(initialized.run().getId());
        playback.setProgressId(progress.getId());
        playback.setMaterialRevision(context.application().getMaterialRevision());
        playback.setMaterialHash(context.application().getMaterialHash());
        playback.setSessionId(actor.sessionId());
        playback.setContextVersion(actor.contextVersion());
        playback.setStartedAt(Instant.now());
        playback.setExpiresAt(playback.getStartedAt().plusSeconds(1800));
        mapper.insertPlayback(playback);
        // 只允许最新播放会话计时；另一设备启动后旧会话不能与它并发累加。
        progress.setActivePlaybackId(playback.getId());
        if (!"PASSED".equals(progress.getStatus())) progress.setStatus("IN_PROGRESS");
        saveProgress(progress);
        record(
                actor,
                "VI_TRAINING_PLAYBACK",
                playback.getId(),
                "TRAINING_PLAYBACK_STARTED",
                Map.of("runId", initialized.run().getId().toString()));
        return new Receipt(playback.getId(), initialized.run().getId());
    }

    private Context initialize(Context context) {
        TrainingPlan plan = context.plan();
        if (plan == null) {
            // 首次真实学习才冻结；GET预览、后台发布新政策均不替申请人暗中启动学习。
            plan = new TrainingPlan();
            plan.setApplicationId(context.application().getId());
            plan.setOwnerDeptId(context.profile().getOwnerDeptId());
            plan.setCompanyRevision(context.profile().getTrainingContextRevision());
            plan.setPolicyRevisionId(context.policy().getId());
            plan.setCourseSnapshot(ViJson.write(context.selected()));
            plan.setMaxAttempts(context.policy().getMaxAttempts());
            plan.setRetryIntervalSeconds(context.policy().getRetryIntervalSeconds());
            plan.setGradingMode(TrainingPolicyService.GRADING_MODE);
            mapper.insertPlan(plan);
        }
        TrainingRun run = context.run();
        if (run == null) {
            // 材料变更使用同epoch的原冻结课程集合，新建运行；次数仍跨run按application+courseKey累计。
            run = new TrainingRun();
            run.setPlanSnapshotId(plan.getId());
            run.setApplicationId(context.application().getId());
            run.setMaterialRevision(context.application().getMaterialRevision());
            run.setMaterialHash(context.application().getMaterialHash());
            run.setStatus("ACTIVE");
            mapper.insertRun(run);
        }
        return new Context(
                context.profile(),
                context.application(),
                plan,
                run,
                context.policy(),
                context.selected());
    }

    public Map<String, Object> progress(long training, Progress input, String key) {
        Actor actor = actors.requireMini();
        return transaction(
                actor,
                "TRAINING_PROGRESS",
                () -> {
                    Context context = requireRun(actor, input.applicationId(), input.runId());
                    FrozenTrainingCourse course = selected(context, training);
                    if (course.courseVersion() != input.courseVersion()) conflict();
                    idem.execute(
                            IdempotencyService.subjectKey(actor),
                            "TRAINING_PROGRESS:" + input.playbackSessionId(),
                            key,
                            ViJson.write(input),
                            true,
                            Receipt.class,
                            () -> reportProgress(actor, context, course, input));
                    return unitView(context, course);
                });
    }

    private Receipt reportProgress(
            Actor actor, Context context, FrozenTrainingCourse course, Progress input) {
        TrainingProgress progress = mapper.progress(context.run().getId(), course.trainingId());
        TrainingPlaybackSession playback = mapper.playback(input.playbackSessionId());
        if (playback == null
                || progress == null
                || !Objects.equals(playback.getSessionId(), actor.sessionId())) invisible();
        if (!Objects.equals(playback.getRunId(), context.run().getId())
                || !Objects.equals(playback.getProgressId(), progress.getId())
                || !Objects.equals(playback.getContextVersion(), actor.contextVersion())
                || !Objects.equals(progress.getActivePlaybackId(), playback.getId())
                || !playback.getExpiresAt().isAfter(Instant.now())) conflict();
        if (progress.getVersion() != input.expectedVersion()
                || input.sequence() <= playback.getLastSequence()) conflict();
        if (input.positionMillis() > course.mediaDurationMillis())
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "播放位置超过真实视频时长");
        Instant now = Instant.now();
        long credit = creditMillis(playback, input, now);
        List<List<Long>> segments = segments(progress.getVerifiedSegments());
        if (credit > 0)
            segments = merge(segments, input.positionMillis() - credit, input.positionMillis());
        long verified = segments.stream().mapToLong(range -> range.get(1) - range.get(0)).sum();
        progress.setVerifiedDurationMillis(verified);
        progress.setVerifiedDurationSeconds((int) (verified / 1000));
        progress.setVerifiedSegments(ViJson.write(segments));
        progress.setLastPositionSeconds((int) (input.positionMillis() / 1000));
        if (!"PASSED".equals(progress.getStatus()))
            progress.setStatus(
                    verified >= course.requiredDurationSeconds() * 1000L
                            ? "READY_FOR_EXAM"
                            : "IN_PROGRESS");
        playback.setLastReportedAt(now);
        playback.setLastPositionMillis(input.positionMillis());
        playback.setLastPlaying(input.playing());
        playback.setLastSequence(input.sequence());
        if (mapper.savePlayback(playback) != 1) conflict();
        saveProgress(progress);
        record(
                actor,
                "VI_TRAINING_PROGRESS",
                progress.getId(),
                "TRAINING_PROGRESS_REPORTED",
                Map.of(
                        "version",
                        progress.getVersion(),
                        "verifiedDurationMillis",
                        verified,
                        "sequence",
                        input.sequence()));
        return new Receipt(progress.getId(), context.run().getId());
    }

    private static long creditMillis(
            TrainingPlaybackSession playback, Progress input, Instant now) {
        if (playback.getLastReportedAt() == null
                || playback.getLastPositionMillis() == null
                || !playback.getLastPlaying()
                || !input.playing()) return 0;
        long elapsed = Math.max(0, Duration.between(playback.getLastReportedAt(), now).toMillis());
        long moved = input.positionMillis() - playback.getLastPositionMillis();
        // 超过心跳窗口整段不计时，恢复报告只建立新基线，不能把离线尾段补成学时。
        // 窗口内允许轻微播放计时偏差，但有效区间仍不能超过真实墙钟时长。
        if (elapsed > MAX_HEARTBEAT_MILLIS || moved <= 0 || moved > elapsed + 1000) return 0;
        return Math.min(moved, Math.min(elapsed, MAX_HEARTBEAT_MILLIS));
    }

    private static List<List<Long>> segments(String json) {
        if (json == null) return new ArrayList<>();
        List<?> raw = ViJson.read(json, List.class);
        List<List<Long>> ranges = new ArrayList<>();
        for (Object item : raw) {
            List<?> pair = (List<?>) item;
            ranges.add(
                    List.of(
                            ((Number) pair.get(0)).longValue(),
                            ((Number) pair.get(1)).longValue()));
        }
        return ranges;
    }

    private static List<List<Long>> merge(List<List<Long>> previous, long start, long end) {
        var ranges = new ArrayList<>(previous);
        ranges.add(List.of(start, end));
        ranges.sort(Comparator.comparingLong(range -> range.get(0)));
        List<List<Long>> merged = new ArrayList<>();
        for (var range : ranges) {
            if (merged.isEmpty() || range.get(0) > merged.get(merged.size() - 1).get(1))
                merged.add(range);
            else {
                var last = merged.remove(merged.size() - 1);
                merged.add(List.of(last.get(0), Math.max(last.get(1), range.get(1))));
            }
        }
        // 报告频率受服务端限制；极端碎片化拒绝继续膨胀JSON，不合并未实际观看的空隙伪造时长。
        if (merged.size() > 10000) state("学习位置片段过多，请联系管理员核查");
        return merged;
    }

    public Map<String, Object> createAttempt(long training, AttemptCreate input, String key) {
        Actor actor = actors.requireMini();
        // 超时是已发生事实；先独立提交其EXPIRED与审计，后续次数/冷却拒绝不能回滚这个事实。
        transaction(
                actor,
                "TRAINING_ATTEMPT_EXPIRY",
                () -> {
                    Context context = requireRun(actor, input.applicationId(), input.runId());
                    var course = selected(context, training);
                    var open = mapper.openAttempt(input.applicationId(), course.courseKey());
                    if (open != null
                            && (!open.getExpiresAt().isAfter(Instant.now())
                                    || !Objects.equals(open.getRunId(), context.run().getId())))
                        expire(actor, open);
                    return null;
                });
        return transaction(
                actor,
                "TRAINING_ATTEMPT_CREATE",
                () -> createAttemptLocked(actor, training, input, key));
    }

    private Map<String, Object> createAttemptLocked(
            Actor actor, long training, AttemptCreate input, String key) {
        Context context = requireRun(actor, input.applicationId(), input.runId());
        FrozenTrainingCourse course = selected(context, training);
        if (course.courseVersion() != input.courseVersion()) conflict();
        Receipt result =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "TRAINING_ATTEMPT_CREATE:" + input.applicationId() + ":" + training,
                        key,
                        ViJson.write(input),
                        true,
                        Receipt.class,
                        () -> newAttempt(actor, context, course, input));
        if (result.runId() != context.run().getId()) conflict();
        return attemptView(mapper.attempt(result.id()), course);
    }

    private Receipt newAttempt(
            Actor actor, Context context, FrozenTrainingCourse course, AttemptCreate input) {
        var open = mapper.openAttempt(input.applicationId(), course.courseKey());
        if (open != null
                && Objects.equals(open.getRunId(), context.run().getId())
                && open.getExpiresAt().isAfter(Instant.now()))
            return new Receipt(open.getId(), context.run().getId());
        TrainingProgress progress = mapper.progress(context.run().getId(), course.trainingId());
        if (progress == null
                || progress.getVerifiedDurationMillis() < course.requiredDurationSeconds() * 1000L)
            throw new ViException(ViErrorCode.VI_TRAINING_NOT_READY);
        if (progress.getVersion() != input.expectedVersion()) conflict();
        if ("PASSED".equals(progress.getStatus())) state("当前材料该课程已通过，无需重复考试");
        if (mapper.attemptCount(input.applicationId(), course.courseKey())
                >= context.plan().getMaxAttempts()) state("本申请该课程累计考试次数已达公司上限");
        Instant retryAt = retryAt(context, course);
        if (retryAt != null && retryAt.isAfter(Instant.now())) state("尚未到公司规定的再次考试时间");
        List<Map<String, Object>> publicQuestions = new ArrayList<>();
        Map<String, Object> correct = new TreeMap<>();
        snapshotQuestions(context, course, publicQuestions, correct);
        var attempt = new TrainingExamAttempt();
        attempt.setApplicationId(input.applicationId());
        attempt.setRunId(context.run().getId());
        attempt.setDriverId(context.profile().getId());
        attempt.setTrainingId(course.trainingId());
        attempt.setCourseKey(course.courseKey());
        attempt.setMaterialRevision(context.application().getMaterialRevision());
        attempt.setMaterialHash(context.application().getMaterialHash());
        attempt.setAttemptNo(mapper.maxAttemptNo(input.applicationId(), course.courseKey()) + 1);
        attempt.setStatus("OPEN");
        attempt.setQuestionSnapshot(ViJson.write(publicQuestions));
        attempt.setStartedAt(Instant.now());
        attempt.setExpiresAt(attempt.getStartedAt().plusSeconds(course.attemptTimeLimitSeconds()));
        mapper.insertAttempt(attempt);
        Map<String, Object> grading =
                Map.of(
                        "correct",
                        correct,
                        "passScore",
                        course.passScore(),
                        "gradingMode",
                        TrainingPolicyService.GRADING_MODE,
                        "courseVersion",
                        course.courseVersion(),
                        "contentHash",
                        course.contentHash());
        attempt.setGradingSnapshotCipher(
                crypto.envelope(ViJson.write(grading), gradingAad(attempt)));
        if (mapper.saveAttempt(attempt) != 1) conflict();
        attempt.setVersion(attempt.getVersion() + 1);
        record(
                actor,
                "VI_TRAINING_ATTEMPT",
                attempt.getId(),
                "TRAINING_ATTEMPT_CREATED",
                Map.of(
                        "runId",
                        context.run().getId().toString(),
                        "attemptNo",
                        attempt.getAttemptNo()));
        return new Receipt(attempt.getId(), context.run().getId());
    }

    private void snapshotQuestions(
            Context context,
            FrozenTrainingCourse frozen,
            List<Map<String, Object>> visible,
            Map<String, Object> correct) {
        TrainingCourse source = courses.find(frozen.trainingId());
        if (!TrainingPolicyService.validPublished(source)
                || !Objects.equals(source.getOwnerDeptId(), context.profile().getOwnerDeptId())
                || !Objects.equals(
                        HexFormat.of().formatHex(source.getContentHash()), frozen.contentHash()))
            state("冻结课程内容记录不一致，请联系管理员核查");
        for (Map<String, Object> row : courses.questions(frozen.trainingId())) {
            String id = row.get("id").toString();
            List<?> options = ViJson.read(row.get("options_json").toString(), List.class);
            List<?> answers = ViJson.read(row.get("correct_option_codes").toString(), List.class);
            visible.add(
                    Map.of(
                            "questionId",
                            id,
                            "type",
                            row.get("question_type"),
                            "prompt",
                            row.get("prompt"),
                            "options",
                            options));
            correct.put(id, answers.stream().map(Object::toString).sorted().toList());
        }
        if (visible.isEmpty()) state("冻结课程缺少正式题库，不能生成考试");
    }

    public Map<String, Object> attempt(long id) {
        Actor actor = actors.requireMini();
        return transaction(
                actor,
                "TRAINING_ATTEMPT_QUERY",
                () -> {
                    Context context = context(actor, null, true);
                    TrainingExamAttempt attempt = ownedAttempt(context, id);
                    if ("OPEN".equals(attempt.getStatus())
                            && !attempt.getExpiresAt().isAfter(Instant.now()))
                        expire(actor, attempt);
                    return attemptView(attempt, selected(context, attempt.getTrainingId()));
                });
    }

    private TrainingExamAttempt ownedAttempt(Context context, long id) {
        TrainingExamAttempt attempt = mapper.attempt(id);
        if (attempt == null || !Objects.equals(attempt.getDriverId(), context.profile().getId()))
            invisible();
        if (!Objects.equals(attempt.getApplicationId(), context.application().getId())
                || context.run() == null
                || !Objects.equals(attempt.getRunId(), context.run().getId())) conflict();
        return attempt;
    }

    public Map<String, Object> submit(Submit input, String key) {
        Actor actor = actors.requireMini();
        return transaction(
                actor,
                "TRAINING_ATTEMPT_SUBMIT",
                () -> {
                    Context context = requireRun(actor, input.applicationId(), input.runId());
                    TrainingExamAttempt attempt = ownedAttempt(context, input.attemptId());
                    FrozenTrainingCourse course = selected(context, attempt.getTrainingId());
                    Receipt receipt =
                            idem.execute(
                                    IdempotencyService.subjectKey(actor),
                                    "TRAINING_ATTEMPT_SUBMIT:" + attempt.getId(),
                                    key,
                                    ViJson.write(input),
                                    true,
                                    Receipt.class,
                                    () -> grade(actor, context, course, attempt, input));
                    return attemptView(mapper.attempt(receipt.id()), course);
                });
    }

    private Receipt grade(
            Actor actor,
            Context context,
            FrozenTrainingCourse course,
            TrainingExamAttempt attempt,
            Submit input) {
        byte[] answersMac = idem.mac(ViJson.write(input.answers()));
        if (!"OPEN".equals(attempt.getStatus())) {
            if (TrainingCompletionAuthority.equal(attempt.getSubmissionMac(), answersMac))
                return new Receipt(attempt.getId(), context.run().getId());
            state("该考试已结束，不能更换作答或再次阅卷");
        }
        if (attempt.getVersion() != input.expectedVersion()) conflict();
        if (!attempt.getExpiresAt().isAfter(Instant.now())) {
            expire(actor, attempt);
            return new Receipt(attempt.getId(), context.run().getId());
        }
        Map<String, Object> grading =
                ViJson.read(
                        crypto.unenvelope(attempt.getGradingSnapshotCipher(), gradingAad(attempt)),
                        Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> correct = (Map<String, Object>) grading.get("correct");
        validateAnswerTargets(attempt, input.answers(), correct.keySet());
        int right = 0;
        for (Answer answer : input.answers()) {
            List<String> expected =
                    ((List<?>) correct.get(answer.questionId()))
                            .stream().map(Object::toString).sorted().toList();
            if (expected.equals(answer.answerCodes())) right++;
        }
        // 每题等分；只在最终总分四舍五入到整数，多选漏/错/多选整题0分，不先逐题舍入。
        int score =
                BigDecimal.valueOf(right)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(correct.size()), 0, RoundingMode.HALF_UP)
                        .intValueExact();
        int passScore = ((Number) grading.get("passScore")).intValue();
        attempt.setScore(score);
        attempt.setStatus(score >= passScore ? "PASSED" : "FAILED");
        attempt.setAnswerSnapshot(ViJson.write(input.answers()));
        attempt.setSubmissionMac(answersMac);
        attempt.setSubmittedAt(Instant.now());
        if (mapper.saveAttempt(attempt) != 1) conflict();
        attempt.setVersion(attempt.getVersion() + 1);
        TrainingProgress progress = mapper.progress(context.run().getId(), course.trainingId());
        progress.setStatus(score >= passScore ? "PASSED" : "FAILED");
        saveProgress(progress);
        record(
                actor,
                "VI_TRAINING_ATTEMPT",
                attempt.getId(),
                "TRAINING_ATTEMPT_GRADED",
                Map.of(
                        "score",
                        score,
                        "status",
                        attempt.getStatus(),
                        "version",
                        attempt.getVersion()));
        completeIfPassed(actor, context);
        return new Receipt(attempt.getId(), context.run().getId());
    }

    private void validateAnswerTargets(
            TrainingExamAttempt attempt, List<Answer> answers, Set<String> expected) {
        if (!answers.stream()
                .map(Answer::questionId)
                .collect(java.util.stream.Collectors.toSet())
                .equals(expected))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "须逐题提交本次固定题卷，不能增加或遗漏题目");
        List<?> publicQuestions = ViJson.read(attempt.getQuestionSnapshot(), List.class);
        Map<String, Set<String>> options = new HashMap<>();
        for (Object item : publicQuestions) {
            Map<?, ?> q = (Map<?, ?>) item;
            Set<String> values = new HashSet<>();
            for (Object option : (List<?>) q.get("options"))
                values.add(((Map<?, ?>) option).get("code").toString());
            options.put(q.get("questionId").toString(), values);
        }
        for (Answer answer : answers)
            if (!options.get(answer.questionId()).containsAll(answer.answerCodes()))
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "作答包含本次题卷不存在的选项");
    }

    private void expire(Actor actor, TrainingExamAttempt attempt) {
        if (!"OPEN".equals(attempt.getStatus())) return;
        attempt.setStatus("EXPIRED");
        attempt.setSubmittedAt(Instant.now());
        if (mapper.saveAttempt(attempt) != 1) conflict();
        attempt.setVersion(attempt.getVersion() + 1);
        record(
                actor,
                "VI_TRAINING_ATTEMPT",
                attempt.getId(),
                "TRAINING_ATTEMPT_EXPIRED",
                Map.of("version", attempt.getVersion()));
    }

    private void completeIfPassed(Actor actor, Context context) {
        if ("PASSED".equals(context.run().getStatus())
                || !completion.allPassed(context.plan(), context.run())) return;
        Map<String, Object> proof = new HashMap<>();
        proof.put("applicationId", context.application().getId());
        proof.put("materialRevision", context.application().getMaterialRevision());
        proof.put("materialHash", context.application().getMaterialHash());
        proof.put("requestNo", "Q03:RUN:" + context.run().getId());
        mapper.insertTrainingProof(proof);
        long proofId = ((Number) proof.get("id")).longValue();
        if (mapper.completeRun(context.run().getId(), context.run().getVersion(), proofId) != 1)
            conflict();
        record(
                actor,
                "VI_TRAINING_RUN",
                context.run().getId(),
                "TRAINING_REQUIRED_COMPLETED",
                Map.of(
                        "applicationId",
                        context.application().getId().toString(),
                        "proofId",
                        Long.toString(proofId)));
    }

    private Instant retryAt(Context context, FrozenTrainingCourse course) {
        TrainingExamAttempt latest =
                mapper.latestAttempt(context.application().getId(), course.courseKey());
        if (latest == null || !Set.of("FAILED", "EXPIRED").contains(latest.getStatus()))
            return null;
        // 超时以法定expiresAt为间隔起点；主动失效旧材料OPEN使用本次确认结束时刻。
        Instant ended = latest.getSubmittedAt();
        if ("EXPIRED".equals(latest.getStatus())
                && latest.getExpiresAt() != null
                && (ended == null || latest.getExpiresAt().isBefore(ended)))
            ended = latest.getExpiresAt();
        return ended == null
                ? null
                : ended.plusSeconds(
                        context.plan() == null
                                ? context.policy().getRetryIntervalSeconds()
                                : context.plan().getRetryIntervalSeconds());
    }

    private Map<String, Object> trainingView(Context context) {
        Map<String, Object> view = new LinkedHashMap<>();
        var profile = context.profile();
        var app = context.application();
        view.put("applicationId", app.getId().toString());
        view.put("version", app.getVersion());
        view.put("profileVersion", profile.getVersion());
        view.put("ownerDeptId", external(profile.getOwnerDeptId()));
        view.put("companyName", profile.getCompanyName());
        view.put("companyRevision", profile.getTrainingContextRevision());
        view.put("materialRevision", app.getMaterialRevision());
        view.put(
                "policyRevisionId",
                context.policy() == null ? null : context.policy().getId().toString());
        view.put("planId", context.plan() == null ? null : context.plan().getId().toString());
        view.put("runId", context.run() == null ? null : context.run().getId().toString());
        view.put(
                "units",
                context.selected().stream().map(course -> unitView(context, course)).toList());
        view.put("complete", completion.currentProof(profile, app) != null);
        var blocked = blockers(context);
        view.put("blockers", blocked);
        view.put("allowedActions", blocked.isEmpty() ? List.of("START_TRAINING") : List.of());
        return view;
    }

    private Map<String, Object> unitView(Context context, FrozenTrainingCourse course) {
        TrainingProgress progress =
                context.run() == null
                        ? null
                        : mapper.progress(context.run().getId(), course.trainingId());
        TrainingExamAttempt latest =
                context.run() == null
                        ? null
                        : mapper.runLatestAttempt(context.run().getId(), course.trainingId());
        int used = mapper.attemptCount(context.application().getId(), course.courseKey());
        int max =
                context.plan() == null
                        ? context.policy().getMaxAttempts()
                        : context.plan().getMaxAttempts();
        Instant retry = retryAt(context, course);
        List<String> actions = new ArrayList<>();
        if (blockers(context).isEmpty()) {
            actions.add("START_PLAYBACK");
            if (progress != null && progress.getActivePlaybackId() != null)
                actions.add("REPORT_TRAINING_PROGRESS");
            if (progress != null
                    && progress.getVerifiedDurationMillis()
                            >= course.requiredDurationSeconds() * 1000L
                    && !"PASSED".equals(progress.getStatus())
                    && used < max
                    && (retry == null || !retry.isAfter(Instant.now())))
                actions.add("CREATE_TRAINING_ATTEMPT");
        }
        if (latest != null) actions.add("VIEW_TRAINING_ATTEMPT");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trainingId", Long.toString(course.trainingId()));
        result.put("courseKey", course.courseKey());
        result.put("courseVersion", course.courseVersion());
        result.put("title", course.title());
        result.put("description", course.description());
        result.put("mediaFileId", Long.toString(course.mediaFileId()));
        result.put("mediaDurationMillis", course.mediaDurationMillis());
        result.put("requiredDurationSeconds", course.requiredDurationSeconds());
        result.put(
                "verifiedDurationSeconds",
                progress == null ? 0 : progress.getVerifiedDurationSeconds());
        result.put(
                "verifiedDurationMillis",
                progress == null ? 0 : progress.getVerifiedDurationMillis());
        result.put("status", progress == null ? "NOT_STARTED" : progress.getStatus());
        result.put("latestAttemptId", latest == null ? null : latest.getId().toString());
        result.put("version", progress == null ? 0 : progress.getVersion());
        result.put("initialized", progress != null);
        result.put("attemptsUsed", used);
        result.put("maxAttempts", max);
        result.put("retryAvailableAt", retry == null ? null : retry.toString());
        result.put("allowedActions", actions);
        return result;
    }

    private Map<String, Object> playbackView(
            Context context, TrainingPlaybackSession playback, FrozenTrainingCourse course) {
        if (playback == null || !Objects.equals(playback.getRunId(), context.run().getId()))
            conflict();
        var progress = mapper.progress(context.run().getId(), course.trainingId());
        return Map.ofEntries(
                Map.entry("playbackSessionId", playback.getId().toString()),
                Map.entry("applicationId", context.application().getId().toString()),
                Map.entry("planId", context.plan().getId().toString()),
                Map.entry("runId", context.run().getId().toString()),
                Map.entry("trainingId", Long.toString(course.trainingId())),
                Map.entry("courseVersion", course.courseVersion()),
                Map.entry("serverStartedAt", playback.getStartedAt().toString()),
                Map.entry("expiresAt", playback.getExpiresAt().toString()),
                Map.entry("version", playback.getVersion()),
                Map.entry("progressVersion", progress.getVersion()),
                Map.entry("heartbeatMaxGapSeconds", 15));
    }

    private Map<String, Object> attemptView(
            TrainingExamAttempt attempt, FrozenTrainingCourse course) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("attemptId", attempt.getId().toString());
        result.put("applicationId", attempt.getApplicationId().toString());
        result.put("runId", attempt.getRunId().toString());
        result.put("trainingId", attempt.getTrainingId().toString());
        result.put("courseVersion", course.courseVersion());
        result.put("status", attempt.getStatus());
        result.put("questions", ViJson.read(attempt.getQuestionSnapshot(), List.class));
        result.put("startedAt", attempt.getStartedAt().toString());
        result.put("expiresAt", attempt.getExpiresAt().toString());
        result.put(
                "submittedAt",
                attempt.getSubmittedAt() == null ? null : attempt.getSubmittedAt().toString());
        result.put("score", attempt.getScore());
        // 超时未阅卷，与已阅卷不通过分开；不能用false伪装已有成绩。
        Boolean passed = null;
        if ("PASSED".equals(attempt.getStatus())) passed = Boolean.TRUE;
        else if ("FAILED".equals(attempt.getStatus())) passed = Boolean.FALSE;
        result.put("passed", passed);
        result.put("version", attempt.getVersion());
        result.put("attemptNo", attempt.getAttemptNo());
        result.put("passScore", course.passScore());
        result.put(
                "allowedActions",
                "OPEN".equals(attempt.getStatus()) && attempt.getExpiresAt().isAfter(Instant.now())
                        ? List.of("SUBMIT_TRAINING_ATTEMPT")
                        : List.of());
        return result;
    }

    private void saveProgress(TrainingProgress progress) {
        if (mapper.saveProgress(progress) != 1) conflict();
        progress.setVersion(progress.getVersion() + 1);
    }

    private static String gradingAad(TrainingExamAttempt attempt) {
        return "Q03:APPLICATION:"
                + attempt.getApplicationId()
                + ":RUN:"
                + attempt.getRunId()
                + ":ATTEMPT:"
                + attempt.getId()
                + ":GRADING";
    }

    private void record(
            Actor actor, String type, long id, String action, Map<String, Object> summary) {
        audit.recordSuccess(actor, type, id, action, null, summary, null);
        // 每次事实拥有独立版本或事件主键；重复UUID/重复作答不会再次进入此路径。
        String event = action + ":" + id + ":" + UUID.randomUUID();
        outbox.publish(event, type, id, action, summary);
    }

    private <T> T transaction(Actor actor, String action, Supplier<T> work) {
        try {
            return tx.execute(status -> work.get());
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_TRAINING",
                    0,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException failure
                            ? failure.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }

    private static Map<String, String> blocker(String field, String code, String message) {
        return TrainingPolicyService.blocker(field, code, message);
    }

    private static String external(Long value) {
        return value == null ? null : value.toString();
    }

    private static void state(String message) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, message);
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }
}
