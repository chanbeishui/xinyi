package com.xinyi.rongli.training.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.integration.service.*;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.qualification.mapper.QualificationMapper;
import com.xinyi.rongli.training.domain.*;
import com.xinyi.rongli.training.dto.LearningRequests.*;
import com.xinyi.rongli.training.mapper.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Supplier;

/** 公司明确发布学习政策。聚合CAS只移动指针，旧修订与已冻结申请集合不被后续发布覆盖。 */
@Service
public class TrainingPolicyService {
    public static final String GRADING_MODE = "EQUAL_HALF_UP_EXACT";
    private final LearningMapper mapper;
    private final TrainingMapper courses;
    private final QualificationMapper qualification;
    private final IntegrationAuthority authority;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    public TrainingPolicyService(
            LearningMapper mapper,
            TrainingMapper courses,
            QualificationMapper qualification,
            IntegrationAuthority authority,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.courses = courses;
        this.qualification = qualification;
        this.authority = authority;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long policyId) {}

    public Map<String, Object> get(long owner) {
        return transaction(
                owner,
                "TRAINING_POLICY_QUERY",
                () -> {
                    requireCompany(owner, "biz:trainingpolicy:query");
                    return view(owner, mapper.policy(owner));
                });
    }

    public Map<String, Object> save(long owner, PolicyUpdate input, String key) {
        return transaction(owner, "TRAINING_POLICY_EDIT", () -> saveLocked(owner, input, key));
    }

    private Map<String, Object> saveLocked(long owner, PolicyUpdate input, String key) {
        requireCompany(owner, "biz:trainingpolicy:query");
        requireCompany(owner, "biz:trainingpolicy:edit");
        var policy = mapper.policy(owner);
        Actor actor = authority.actor();
        idem.execute(
                IdempotencyService.subjectKey(actor),
                "TRAINING_POLICY_EDIT:" + owner,
                key,
                ViJson.write(input),
                true,
                Receipt.class,
                () -> saveRevision(owner, policy, input, actor));
        return view(owner, mapper.policy(owner));
    }

    private Receipt saveRevision(
            long owner, TrainingPolicy policy, PolicyUpdate input, Actor actor) {
        if ((policy == null ? 0 : policy.getVersion()) != input.expectedVersion()) conflict();
        requireCourses(owner, input.requiredTrainingIds(), true);
        if (policy == null) {
            policy = new TrainingPolicy();
            policy.setOwnerDeptId(owner);
            mapper.insertPolicy(policy);
        }
        var revision = new TrainingPolicyRevision();
        revision.setPolicyId(policy.getId());
        revision.setRevisionNo(mapper.maxPolicyRevision(policy.getId()) + 1);
        revision.setRequiredTrainingIds(
                ViJson.write(input.requiredTrainingIds().stream().map(Object::toString).toList()));
        revision.setMaxAttempts(input.maxAttempts());
        revision.setRetryIntervalSeconds(input.retryIntervalSeconds());
        revision.setGradingMode(GRADING_MODE);
        revision.setCreatedByUserId(actor.sysUserId());
        revision.setContentHash(
                HexFormat.of()
                        .parseHex(
                                crypto.seal(ViJson.write(input), "Q03:POLICY:" + owner)
                                        .fingerprint));
        mapper.insertRevision(revision);
        policy.setDraftRevisionId(revision.getId());
        if (mapper.pointDraft(policy) != 1) conflict();
        policy.setVersion(policy.getVersion() + 1);
        record(actor, policy, "TRAINING_POLICY_DRAFT_SAVED", input.reason());
        return new Receipt(policy.getId());
    }

    public Map<String, Object> publish(long owner, PolicyPublish input, String key) {
        return transaction(
                owner, "TRAINING_POLICY_PUBLISH", () -> publishLocked(owner, input, key));
    }

    private Map<String, Object> publishLocked(long owner, PolicyPublish input, String key) {
        requireCompany(owner, "biz:trainingpolicy:query");
        requireCompany(owner, "biz:trainingpolicy:publish");
        var policy = mapper.policy(owner);
        Actor actor = authority.actor();
        idem.execute(
                IdempotencyService.subjectKey(actor),
                "TRAINING_POLICY_PUBLISH:" + owner,
                key,
                ViJson.write(input),
                true,
                Receipt.class,
                () -> publishRevision(owner, policy, input, actor));
        return view(owner, mapper.policy(owner));
    }

    private Receipt publishRevision(
            long owner, TrainingPolicy policy, PolicyPublish input, Actor actor) {
        if (policy == null
                || policy.getVersion() != input.expectedVersion()
                || !Objects.equals(policy.getDraftRevisionId(), input.draftRevisionId()))
            conflict();
        var revision = mapper.revision(input.draftRevisionId());
        if (revision == null
                || !Objects.equals(revision.getPolicyId(), policy.getId())
                || revision.getPublishedAt() != null) conflict();
        List<Long> selected = ids(revision);
        requireCourses(owner, selected, true);
        var blockers = blockers(owner, revision);
        if (!blockers.isEmpty())
            throw new ViException(ViErrorCode.VI_DATA_INVALID, blockers.get(0).get("message"));
        if (mapper.publishRevision(revision.getId(), actor.sysUserId()) != 1) conflict();
        policy.setActiveRevisionId(revision.getId());
        if (mapper.pointActive(policy) != 1) conflict();
        policy.setVersion(policy.getVersion() + 1);
        record(actor, policy, "TRAINING_POLICY_PUBLISHED", input.reason());
        return new Receipt(policy.getId());
    }

    private void requireCompany(long owner, String permission) {
        Set<Long> scope = authority.scope(permission);
        if (scope != null && !scope.contains(owner)) invisible();
        var company = qualification.company(owner);
        if (company == null
                || !"0".equals(company.get("status"))
                || !"0".equals(company.get("del_flag")))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "公司部门不存在或已停用");
    }

    private void requireCourses(long owner, List<Long> ids, boolean scoped) {
        Set<Long> queryScope =
                scoped && !ids.isEmpty() ? authority.scope("biz:trainingcourse:query") : null;
        if (scoped && !ids.isEmpty() && queryScope != null && !queryScope.contains(owner))
            invisible();
        Set<String> families = new HashSet<>();
        for (long id : ids) {
            TrainingCourse course = courses.find(id);
            if (course == null || !Objects.equals(course.getOwnerDeptId(), owner)) invisible();
            if (!validPublished(course))
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "必修课程须为可信已发布确切版本");
            if (!families.add(course.getCourseKey()))
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "同一课程族只能选择一个已发布版本");
        }
    }

    public static boolean validPublished(TrainingCourse course) {
        return course != null
                && "PUBLISHED".equals(course.getStatus())
                && course.getOwnerDeptId() != null
                && course.getContentHash() != null
                && course.getCreatedByUserId() != null
                && course.getPublishedAt() != null
                && course.getPublishedByUserId() != null
                && course.getMediaFileId() != null
                && course.getRequiredDurationSeconds() != null
                && course.getPassScore() != null
                && course.getAttemptTimeLimitSeconds() != null;
    }

    public static List<Long> ids(TrainingPolicyRevision revision) {
        List<?> raw = ViJson.read(revision.getRequiredTrainingIds(), List.class);
        return raw.stream().map(value -> Long.parseLong(value.toString())).toList();
    }

    private List<Map<String, String>> blockers(long owner, TrainingPolicyRevision revision) {
        var result = new ArrayList<Map<String, String>>();
        if (ids(revision).isEmpty())
            result.add(blocker("requiredTrainingIds", "REQUIRED", "请明确选择至少一门必修课程"));
        if (revision.getMaxAttempts() == null)
            result.add(blocker("maxAttempts", "REQUIRED", "请明确配置含首次的累计考试上限"));
        if (revision.getRetryIntervalSeconds() == null)
            result.add(blocker("retryIntervalSeconds", "REQUIRED", "请明确配置重试间隔，允许显式填写0"));
        Set<String> families = new HashSet<>();
        for (long id : ids(revision)) {
            var course = courses.find(id);
            if (!validPublished(course)
                    || !Objects.equals(course.getOwnerDeptId(), owner)
                    || !families.add(course.getCourseKey()))
                result.add(
                        blocker(
                                "requiredTrainingIds",
                                "COURSE_UNAVAILABLE",
                                "选定课程版本不可用或同族重复，请维护草稿"));
        }
        return result;
    }

    private boolean can(long owner, String permission) {
        if (!SecurityUtils.hasPermi(permission)) return false;
        var scope = authority.scope(permission);
        return scope == null || scope.contains(owner);
    }

    private Map<String, Object> view(long owner, TrainingPolicy policy) {
        var company = qualification.company(owner);
        var draft =
                policy == null || policy.getDraftRevisionId() == null
                        ? null
                        : mapper.revision(policy.getDraftRevisionId());
        var active =
                policy == null || policy.getActiveRevisionId() == null
                        ? null
                        : mapper.revision(policy.getActiveRevisionId());
        List<String> actions = new ArrayList<>();
        actions.add("VIEW_TRAINING_POLICY");
        if (can(owner, "biz:trainingpolicy:edit")) actions.add("EDIT_TRAINING_POLICY");
        if (draft != null
                && can(owner, "biz:trainingpolicy:publish")
                && can(owner, "biz:trainingcourse:query")
                && blockers(owner, draft).isEmpty()) actions.add("PUBLISH_TRAINING_POLICY");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("policyId", policy == null ? null : policy.getId().toString());
        result.put("ownerDeptId", Long.toString(owner));
        result.put("companyName", company.get("dept_name"));
        result.put("version", policy == null ? 0 : policy.getVersion());
        result.put("draftRevision", revisionView(owner, draft));
        result.put("activeRevision", revisionView(owner, active));
        result.put("allowedActions", actions);
        return result;
    }

    private Map<String, Object> revisionView(long owner, TrainingPolicyRevision revision) {
        if (revision == null) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revisionId", revision.getId().toString());
        result.put("revisionNo", revision.getRevisionNo());
        result.put("status", revision.getPublishedAt() == null ? "DRAFT" : "PUBLISHED");
        result.put("requiredTrainingIds", ids(revision).stream().map(Object::toString).toList());
        result.put("maxAttempts", revision.getMaxAttempts());
        result.put("retryIntervalSeconds", revision.getRetryIntervalSeconds());
        result.put("gradingMode", revision.getGradingMode());
        result.put(
                "publishedAt",
                revision.getPublishedAt() == null ? null : revision.getPublishedAt().toString());
        result.put("publishBlockers", blockers(owner, revision));
        return result;
    }

    private void record(Actor actor, TrainingPolicy policy, String action, String reason) {
        String aad = "Q03:ADMIN:" + actor.sysUserId() + ":" + action + ":" + policy.getId();
        Map<String, Object> summary =
                Map.of(
                        "version",
                        policy.getVersion(),
                        "ownerDeptId",
                        policy.getOwnerDeptId().toString(),
                        "reasonEnvelope",
                        Base64.getEncoder().encodeToString(crypto.envelope(reason, aad)));
        audit.recordSuccess(
                actor, "VI_TRAINING_POLICY", policy.getId(), action, null, summary, "维护公司培训政策");
        outbox.publish(
                action + ":" + policy.getId() + ":" + policy.getVersion(),
                "VI_TRAINING_POLICY",
                policy.getId(),
                action,
                Map.of("policyId", policy.getId().toString(), "version", policy.getVersion()));
    }

    private <T> T transaction(long owner, String action, Supplier<T> work) {
        Actor actor = authority.actor();
        try {
            return tx.execute(
                    status -> {
                        authority.lockAuthority();
                        return work.get();
                    });
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_TRAINING_POLICY",
                    owner,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException failure
                            ? failure.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }

    public static Map<String, String> blocker(String field, String code, String message) {
        return Map.of("field", field, "reasonCode", code, "message", message);
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }
}
