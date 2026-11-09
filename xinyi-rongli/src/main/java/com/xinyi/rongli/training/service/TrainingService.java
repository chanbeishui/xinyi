package com.xinyi.rongli.training.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.file.domain.PrivateFile;
import com.xinyi.rongli.file.mapper.PrivateFileMapper;
import com.xinyi.rongli.file.service.PrivateFileService;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.training.domain.TrainingCourse;
import com.xinyi.rongli.training.dto.TrainingRequests;
import com.xinyi.rongli.training.dto.TrainingRequests.*;
import com.xinyi.rongli.training.mapper.TrainingMapper;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

/** 公司课程与题库共用父版本授权；发布只冻结内容，不能创建学员进度、培训通过或代驾资格。 */
@Service
public class TrainingService {
    private final TrainingMapper mapper;
    private final PrivateFileMapper files;
    private final PrivateFileService fileService;
    private final IntegrationAuthority authority;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    public TrainingService(
            TrainingMapper mapper,
            PrivateFileMapper files,
            PrivateFileService fileService,
            IntegrationAuthority authority,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.files = files;
        this.fileService = fileService;
        this.authority = authority;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long trainingId) {}

    public Map<String, Object> list(Query query) {
        return transaction(
                "TRAINING_LIST",
                0,
                () -> {
                    var scope = authority.scope("biz:trainingcourse:list");
                    var rows =
                            mapper.list(
                                    scope, query, (long) (query.pageNum() - 1) * query.pageSize());
                    return Map.of(
                            "code",
                            200,
                            "msg",
                            "操作成功",
                            "rows",
                            rows.stream().map(row -> view(row, false)).toList(),
                            "total",
                            mapper.count(scope, query));
                });
    }

    public Map<String, Object> get(long id) {
        return transaction(
                "TRAINING_QUERY",
                id,
                () -> {
                    TrainingCourse row = accessible(id, "biz:trainingcourse:query", false);
                    audit.recordSuccess(
                            authority.actor(),
                            "VI_TRAINING_COURSE",
                            id,
                            "TRAINING_ANSWERS_READ",
                            null,
                            Map.of("questionCount", mapper.questionCount(id)),
                            null);
                    return view(row, true);
                });
    }

    public Map<String, Object> create(Create input, String key) {
        return transaction("TRAINING_CREATE", 0, () -> createLocked(input, key));
    }

    private Map<String, Object> createLocked(Create input, String key) {
        Actor actor = authority.actor();
        var scope = authority.scope("biz:trainingcourse:create");
        TrainingCourse source = null;
        if (input.sourceTrainingId() != null) {
            source = accessible(input.sourceTrainingId(), "biz:trainingcourse:query", true);
            requireOwner(source, scope);
        } else company(input.ownerDeptId(), scope);
        TrainingCourse original = source;
        Receipt receipt =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "TRAINING_CREATE",
                        key,
                        ViJson.write(input),
                        true,
                        Receipt.class,
                        () -> createCourse(actor, input, original));
        TrainingCourse row = accessible(receipt.trainingId(), "biz:trainingcourse:create", false);
        return view(row, can(row, "biz:trainingcourse:query"));
    }

    private Receipt createCourse(Actor actor, Create input, TrainingCourse source) {
        TrainingCourse row = new TrainingCourse();
        List<Question> questions = List.of();
        if (source == null) {
            row.setOwnerDeptId(input.ownerDeptId());
            row.setTitle(input.title());
            row.setCourseKey(UUID.randomUUID().toString());
            row.setCourseVersion(1);
        } else {
            if (!"PUBLISHED".equals(source.getStatus())) state("仅能从已发布确切版本派生草稿");
            if (source.getOwnerDeptId() == null
                    || source.getCreatedByUserId() == null
                    || source.getContentHash() == null
                    || source.getPublishedAt() == null
                    || source.getPublishedByUserId() == null) state("历史来源缺少可信公司或发布记录，请人工核查");
            if (!Objects.equals(source.getVersion(), input.expectedSourceVersion())) conflict();
            company(source.getOwnerDeptId(), authority.scope("biz:trainingcourse:create"));
            TrainingCourse family = mapper.lockFamily(source.getCourseKey());
            if (family == null || !Objects.equals(family.getOwnerDeptId(), source.getOwnerDeptId()))
                state("课程族归属不一致，请人工核查");
            if (mapper.draft(source.getCourseKey()) != null) state("本课程族已有未发布草稿，请继续原草稿");
            verifyMedia(actor, source);
            row.setOwnerDeptId(source.getOwnerDeptId());
            row.setTitle(source.getTitle());
            row.setDescription(source.getDescription());
            row.setCourseKey(source.getCourseKey());
            row.setCourseVersion(Math.addExact(mapper.maxVersion(source.getCourseKey()), 1));
            row.setSourceTrainingId(source.getId());
            row.setInheritedMediaFileId(source.getMediaFileId());
            row.setMediaFileId(source.getMediaFileId());
            row.setRequiredDurationSeconds(source.getRequiredDurationSeconds());
            row.setPassScore(source.getPassScore());
            row.setAttemptTimeLimitSeconds(source.getAttemptTimeLimitSeconds());
            questions = questions(source.getId());
        }
        row.setStatus("DRAFT");
        row.setCreatedByUserId(actor.sysUserId());
        row.setUpdatedByUserId(actor.sysUserId());
        row.setContentHash(hash(row, questions));
        mapper.insert(row);
        insertQuestions(row.getId(), questions);
        record(actor, row, "TRAINING_COURSE_CREATED", null);
        return new Receipt(row.getId());
    }

    public Map<String, Object> update(long id, Update input, String key) {
        return transaction("TRAINING_EDIT", id, () -> updateLocked(id, input, key));
    }

    private Map<String, Object> updateLocked(long id, Update input, String key) {
        Actor actor = authority.actor();
        TrainingCourse row = accessible(id, "biz:trainingcourse:edit", true);
        Receipt receipt =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "TRAINING_EDIT:" + id,
                        key,
                        ViJson.write(input),
                        true,
                        Receipt.class,
                        () -> updateCourse(actor, row, input));
        TrainingCourse result = accessible(receipt.trainingId(), "biz:trainingcourse:edit", false);
        return view(result, can(result, "biz:trainingcourse:query"));
    }

    private Receipt updateCourse(Actor actor, TrainingCourse row, Update input) {
        if (row.getVersion() != input.expectedVersion()) conflict();
        requireDraft(row);
        company(row.getOwnerDeptId(), authority.scope("biz:trainingcourse:edit"));
        row.setTitle(input.title());
        row.setDescription(input.description());
        row.setMediaFileId(input.mediaFileId());
        row.setRequiredDurationSeconds(input.requiredDurationSeconds());
        row.setPassScore(input.passScore());
        row.setAttemptTimeLimitSeconds(input.attemptTimeLimitSeconds());
        row.setUpdatedByUserId(actor.sysUserId());
        if (row.getMediaFileId() != null) verifyMedia(actor, row);
        row.setContentHash(hash(row, input.questions()));
        if (mapper.save(row) != 1) conflict();
        // 只替换已持父锁且CAS成功的DRAFT题库；老PUBLISHED课程与任何考试快照都不在此更新。
        mapper.clearQuestions(row.getId());
        insertQuestions(row.getId(), input.questions());
        row.setVersion(row.getVersion() + 1);
        record(actor, row, "TRAINING_COURSE_UPDATED", input.reason());
        return new Receipt(row.getId());
    }

    public Map<String, Object> publish(
            long id, com.xinyi.rongli.refund.dto.RefundRequests.Action input, String key) {
        return transaction("TRAINING_PUBLISH", id, () -> publishLocked(id, input, key));
    }

    private Map<String, Object> publishLocked(
            long id, com.xinyi.rongli.refund.dto.RefundRequests.Action input, String key) {
        Actor actor = authority.actor();
        TrainingCourse row = accessible(id, "biz:trainingcourse:publish", true);
        Receipt result =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "TRAINING_PUBLISH:" + id,
                        key,
                        ViJson.write(input),
                        true,
                        Receipt.class,
                        () -> publishCourse(actor, row, input));
        TrainingCourse published =
                accessible(result.trainingId(), "biz:trainingcourse:publish", false);
        return view(published, can(published, "biz:trainingcourse:query"));
    }

    private Receipt publishCourse(
            Actor actor,
            TrainingCourse row,
            com.xinyi.rongli.refund.dto.RefundRequests.Action input) {
        if (row.getVersion() != input.expectedVersion()) conflict();
        requireDraft(row);
        company(row.getOwnerDeptId(), authority.scope("biz:trainingcourse:publish"));
        PrivateFile media = verifyMedia(actor, row);
        var blocked = blockers(row, media);
        if (!blocked.isEmpty())
            throw new ViException(ViErrorCode.VI_DATA_INVALID, blocked.get(0).get("message"));
        row.setContentHash(hash(row, questions(row.getId())));
        if (mapper.publish(row.getId(), row.getVersion(), actor.sysUserId(), row.getContentHash())
                != 1) conflict();
        row.setVersion(row.getVersion() + 1);
        record(actor, row, "TRAINING_COURSE_PUBLISHED", input.reason());
        return new Receipt(row.getId());
    }

    private TrainingCourse accessible(long id, String permission, boolean lock) {
        var scope = authority.scope(permission);
        TrainingCourse row = lock ? mapper.lock(id) : mapper.find(id);
        if (row == null) invisible();
        requireOwner(row, scope);
        return row;
    }

    private void requireOwner(TrainingCourse row, Set<Long> scope) {
        if (scope != null
                && (row.getOwnerDeptId() == null || !scope.contains(row.getOwnerDeptId())))
            invisible();
    }

    private void company(Long id, Set<Long> scope) {
        if (id == null) state("历史课程没有可信公司归属，请人工核查");
        if (scope != null && !scope.contains(id)) throw new ViException(ViErrorCode.VI_FORBIDDEN);
        authority.department(id, scope);
    }

    private void requireDraft(TrainingCourse row) {
        if (!"DRAFT".equals(row.getStatus())) state("已发布或停用课程只读，请从已发布版本派生新草稿");
        if (row.getOwnerDeptId() == null || row.getCreatedByUserId() == null)
            state("历史课程缺少可信归属或创建记录，请人工核查");
    }

    private PrivateFile verifyMedia(Actor actor, TrainingCourse row) {
        if (row.getMediaFileId() == null) return null;
        PrivateFile snapshot = files.find(row.getMediaFileId());
        if (!related(row, snapshot)) invisible();
        return fileService.validateTrainingMedia(
                actor,
                row.getOwnerDeptId(),
                row.getMediaFileId(),
                Set.of(snapshot.getTrainingId()));
    }

    private boolean related(TrainingCourse row, PrivateFile file) {
        if (file == null
                || file.getTrainingId() == null
                || !"TRAINING_MEDIA".equals(file.getPurpose())) return false;
        TrainingCourse cursor = row;
        Set<Long> visited = new HashSet<>();
        // 仅允许服务端从选定来源冻结的精确fileId沿同公司同族链只读继承，不开放同族任意附件。
        while (cursor != null && visited.add(cursor.getId()) && visited.size() <= 1000) {
            if (Objects.equals(cursor.getId(), file.getTrainingId())) return true;
            if (!Objects.equals(cursor.getInheritedMediaFileId(), file.getId())
                    || cursor.getSourceTrainingId() == null) return false;
            TrainingCourse source = mapper.find(cursor.getSourceTrainingId());
            if (source == null
                    || !"PUBLISHED".equals(source.getStatus())
                    || !Objects.equals(source.getCourseKey(), row.getCourseKey())
                    || !Objects.equals(source.getOwnerDeptId(), row.getOwnerDeptId())
                    || !Objects.equals(source.getMediaFileId(), file.getId())) return false;
            cursor = source;
        }
        return false;
    }

    private List<Question> questions(long id) {
        List<Question> result = new ArrayList<>();
        for (var row : mapper.questions(id)) {
            List<Option> options = new ArrayList<>();
            for (Object item : ViJson.read(row.get("options_json").toString(), List.class)) {
                Map<?, ?> option = (Map<?, ?>) item;
                options.add(
                        new Option(option.get("code").toString(), option.get("text").toString()));
            }
            List<String> answers =
                    ViJson.read(row.get("correct_option_codes").toString(), List.class);
            result.add(
                    new Question(
                            row.get("question_code").toString(),
                            row.get("question_type").toString(),
                            row.get("prompt").toString(),
                            options,
                            answers));
        }
        return result;
    }

    private void insertQuestions(long id, List<Question> questions) {
        int sort = 0;
        for (Question question : questions)
            mapper.insertQuestion(
                    id,
                    question,
                    ViJson.write(question.options()),
                    ViJson.write(question.correctOptionCodes()),
                    ++sort);
    }

    private byte[] hash(TrainingCourse row, List<Question> questions) {
        Map<String, Object> values = new TreeMap<>();
        values.put("company", row.getOwnerDeptId());
        values.put("courseKey", row.getCourseKey());
        values.put("courseVersion", row.getCourseVersion());
        values.put("title", row.getTitle());
        values.put("description", row.getDescription());
        values.put("mediaFileId", row.getMediaFileId());
        values.put("requiredDurationSeconds", row.getRequiredDurationSeconds());
        values.put("passScore", row.getPassScore());
        values.put("attemptTimeLimitSeconds", row.getAttemptTimeLimitSeconds());
        values.put("questions", questions);
        return HexFormat.of()
                .parseHex(
                        crypto.seal(
                                        ViJson.write(values),
                                        "Q02:COMPANY:"
                                                + row.getOwnerDeptId()
                                                + ":COURSE:"
                                                + row.getCourseKey()
                                                + ":VERSION:"
                                                + row.getCourseVersion())
                                .fingerprint);
    }

    private boolean can(TrainingCourse row, String permission) {
        if (!SecurityUtils.hasPermi(permission)) return false;
        var scope = authority.scope(permission);
        return scope == null
                || row.getOwnerDeptId() != null && scope.contains(row.getOwnerDeptId());
    }

    private Map<String, Object> view(TrainingCourse row, boolean detail) {
        PrivateFile media = row.getMediaFileId() == null ? null : files.find(row.getMediaFileId());
        if (!related(row, media) || !can(row, "biz:files:query")) media = null;
        var blocked = blockers(row, media);
        List<String> actions = new ArrayList<>();
        if (can(row, "biz:trainingcourse:query")) actions.add("VIEW_TRAINING_COURSE");
        if ("DRAFT".equals(row.getStatus())
                && row.getOwnerDeptId() != null
                && row.getCreatedByUserId() != null) {
            if (can(row, "biz:trainingcourse:edit")) actions.add("EDIT_TRAINING_COURSE");
            if (can(row, "biz:trainingcourse:publish") && blocked.isEmpty())
                actions.add("PUBLISH_TRAINING_COURSE");
        }
        if ("PUBLISHED".equals(row.getStatus())
                && row.getOwnerDeptId() != null
                && row.getCreatedByUserId() != null
                && row.getContentHash() != null
                && row.getPublishedAt() != null
                && row.getPublishedByUserId() != null
                && blocked.isEmpty()
                && can(row, "biz:trainingcourse:create")
                && can(row, "biz:trainingcourse:query")
                && can(row, "biz:files:query")
                && mapper.draft(row.getCourseKey()) == null)
            actions.add("CREATE_TRAINING_REVISION");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trainingId", row.getId().toString());
        result.put("ownerDeptId", external(row.getOwnerDeptId()));
        result.put("companyName", row.getCompanyName());
        result.put("courseKey", row.getCourseKey());
        result.put("courseVersion", row.getCourseVersion());
        result.put("sourceTrainingId", external(row.getSourceTrainingId()));
        result.put("title", row.getTitle());
        result.put("description", row.getDescription());
        result.put("mediaFileId", external(row.getMediaFileId()));
        result.put("requiredDurationSeconds", row.getRequiredDurationSeconds());
        result.put("passScore", row.getPassScore());
        result.put("attemptTimeLimitSeconds", row.getAttemptTimeLimitSeconds());
        result.put("status", row.getStatus());
        result.put("version", row.getVersion());
        result.put(
                "contentHash",
                row.getContentHash() == null
                        ? null
                        : HexFormat.of().formatHex(row.getContentHash()));
        result.put(
                "publishedAt",
                row.getPublishedAt() == null ? null : row.getPublishedAt().toString());
        result.put("questionCount", mapper.questionCount(row.getId()));
        result.put("allowedActions", actions);
        result.put("publishBlockers", blocked);
        if (detail) {
            result.put("questions", questions(row.getId()));
            result.put("media", media == null ? null : mediaView(row, media));
        }
        return result;
    }

    private Map<String, Object> mediaView(TrainingCourse row, PrivateFile media) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileId", media.getId().toString());
        result.put("status", media.getStatus());
        result.put("mimeType", media.getMimeType());
        result.put("size", media.getServerSize());
        result.put("durationMillis", media.getMediaDurationMillis());
        result.put("width", media.getMediaWidth());
        result.put("height", media.getMediaHeight());
        result.put("videoCodec", media.getMediaVideoCodec());
        result.put("audioCodec", media.getMediaAudioCodec());
        result.put("inherited", !Objects.equals(row.getId(), media.getTrainingId()));
        return result;
    }

    private List<Map<String, String>> blockers(TrainingCourse row, PrivateFile media) {
        List<Map<String, String>> result = new ArrayList<>();
        if (row.getOwnerDeptId() == null || row.getCreatedByUserId() == null)
            result.add(block("ownerDeptId", "LEGACY_AUTHORITY_UNKNOWN", "历史课程归属或创建记录需人工核查"));
        else {
            var company = mapper.company(row.getOwnerDeptId());
            if (company == null
                    || !"0".equals(company.get("status"))
                    || !"0".equals(company.get("del_flag")))
                result.add(block("ownerDeptId", "COMPANY_UNAVAILABLE", "公司部门不存在、已停用或删除"));
        }
        if (row.getMediaFileId() == null)
            result.add(block("mediaFileId", "MEDIA_REQUIRED", "请上传真实私有MP4教材"));
        else if (media == null)
            result.add(block("mediaFileId", "MEDIA_UNAVAILABLE", "媒体不存在、未获权或不属于本草稿及可信继承来源"));
        else if (!"READY".equals(media.getStatus()) || media.getMediaDurationMillis() == null)
            result.add(block("mediaFileId", "MEDIA_NOT_READY", "教材尚未完成真实视频核验"));
        if (row.getRequiredDurationSeconds() == null)
            result.add(block("requiredDurationSeconds", "REQUIRED", "请核定必学时长"));
        else if (media != null
                && media.getMediaDurationMillis() != null
                && row.getRequiredDurationSeconds() * 1000L > media.getMediaDurationMillis())
            result.add(
                    block("requiredDurationSeconds", "EXCEEDS_MEDIA_DURATION", "必学时长不能超过实际视频时长"));
        if (row.getPassScore() == null) result.add(block("passScore", "REQUIRED", "请核定通过分数"));
        if (row.getAttemptTimeLimitSeconds() == null)
            result.add(block("attemptTimeLimitSeconds", "REQUIRED", "请核定考试时限"));
        if (mapper.questionCount(row.getId()) == 0)
            result.add(block("questions", "QUESTIONS_REQUIRED", "发布至少需要一题完整题库"));
        return result;
    }

    private static Map<String, String> block(String field, String code, String message) {
        return Map.of("field", field, "reasonCode", code, "message", message);
    }

    private <T> T transaction(String action, long id, Supplier<T> operation) {
        Actor actor = authority.actor();
        try {
            return tx.execute(
                    status -> {
                        authority.lockAuthority();
                        return operation.get();
                    });
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_TRAINING_COURSE",
                    id,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException known
                            ? known.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }

    private void record(Actor actor, TrainingCourse row, String action, String reason) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("courseVersion", row.getCourseVersion());
        summary.put("version", row.getVersion());
        summary.put("ownerDeptId", external(row.getOwnerDeptId()));
        summary.put("sourceTrainingId", external(row.getSourceTrainingId()));
        if (reason != null)
            summary.put(
                    "reasonEnvelope",
                    Base64.getEncoder()
                            .encodeToString(
                                    crypto.envelope(
                                            reason,
                                            "Q02:ADMIN:"
                                                    + actor.sysUserId()
                                                    + ":TRAINING:"
                                                    + row.getId()
                                                    + ":"
                                                    + action)));
        audit.recordSuccess(
                actor,
                "VI_TRAINING_COURSE",
                row.getId(),
                action,
                null,
                summary,
                reason == null ? "创建公司培训草稿" : "维护原因已加密留存");
        outbox.publish(
                action + ":" + row.getId() + ":" + row.getVersion(),
                "VI_TRAINING_COURSE",
                row.getId(),
                action,
                Map.of(
                        "trainingId",
                        row.getId().toString(),
                        "courseVersion",
                        row.getCourseVersion(),
                        "version",
                        row.getVersion()));
    }

    private static String external(Long value) {
        return value == null ? null : value.toString();
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    private static void state(String message) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, message);
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }
}
