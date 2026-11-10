package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.qualification.domain.*;
import com.xinyi.rongli.qualification.dto.*;
import com.xinyi.rongli.qualification.mapper.*;
import com.xinyi.rongli.training.service.TrainingCompletionAuthority;
import com.xinyi.rongli.verification.service.CanonicalVerificationAuthority;

import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.util.*;
import java.util.function.Supplier;

/** 最终资格审批只消费已认证提交；审批、能力版本、审计和到期计划共同提交，不执行任何外部核验。 */
@Service
public class QualificationAdminService {
    private static final String LIST = "biz:driverqualification:list";
    private static final String QUERY = "biz:driverqualification:query";
    private static final String AUDIT = "biz:driverqualification:audit";
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final List<String> CHECKS =
            List.of("DEVICE_AUTH", "BACKGROUND", "LICENSE", "TRAINING");
    private static final List<String> FILES =
            List.of(
                    "idCardFrontFileId",
                    "idCardBackFileId",
                    "licenseMainFileId",
                    "licenseSubFileId");
    private final IntegrationAuthority authority;
    private final QualificationAdminMapper mapper;
    private final QualificationMapper qualifications;
    private final QualificationSubmissionAuthority submissions;
    private final QualificationSnapshotAuthority snapshots;
    private final QualificationApplicantAuthority applicants;
    private final CanonicalVerificationAuthority verifications;
    private final TrainingCompletionAuthority training;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final QualificationLifecyclePort lifecycle;
    private final TransactionTemplate tx;

    public QualificationAdminService(
            IntegrationAuthority authority,
            QualificationAdminMapper mapper,
            QualificationMapper qualifications,
            QualificationSubmissionAuthority submissions,
            QualificationSnapshotAuthority snapshots,
            QualificationApplicantAuthority applicants,
            CanonicalVerificationAuthority verifications,
            TrainingCompletionAuthority training,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            QualificationLifecyclePort lifecycle,
            PlatformTransactionManager manager) {
        this.authority = authority;
        this.mapper = mapper;
        this.qualifications = qualifications;
        this.submissions = submissions;
        this.snapshots = snapshots;
        this.applicants = applicants;
        this.verifications = verifications;
        this.training = training;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        this.lifecycle = lifecycle;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    private record Context(
            Map<String, Object> meta,
            DriverProfile profile,
            QualificationApplication application,
            QualificationSubmission submission) {}

    public record Receipt(long reviewId) {}

    public Map<String, Object> list(QualificationAdminRequests.Query query) {
        return transaction(
                "QUALIFICATION_LIST",
                () -> {
                    Set<Long> scope = authority.scope(LIST);
                    List<Map<String, Object>> rows = new ArrayList<>();
                    if (scope == null || !scope.isEmpty()) {
                        for (Long id :
                                mapper.list(
                                        scope,
                                        query,
                                        (long) (query.pageNum() - 1) * query.pageSize()))
                            rows.add(view(context(id, scope), false));
                    }
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("code", 200);
                    result.put("msg", "操作成功");
                    result.put("rows", rows);
                    result.put(
                            "total",
                            scope != null && scope.isEmpty() ? 0L : mapper.count(scope, query));
                    return result;
                });
    }

    public Map<String, Object> get(long id) {
        return transaction(
                "QUALIFICATION_QUERY", () -> view(context(id, authority.scope(QUERY)), true));
    }

    public Map<String, Object> audit(
            long id, QualificationAdminRequests.Audit request, String key) {
        return transaction("QUALIFICATION_AUDIT", () -> auditLocked(id, request, key));
    }

    private Map<String, Object> auditLocked(
            long id, QualificationAdminRequests.Audit request, String key) {
        Context context = context(id, authority.scope(QUERY));
        requireWriteScope(context);
        denySelf(context);
        // 重放仍先鉴权及同人检查，但版本和状态仅在首次执行supplier中检查，避免原意图被后续状态误判。
        idem.execute(
                IdempotencyService.subjectKey(authority.actor()),
                "QUALIFICATION_AUDIT:" + id,
                key,
                auditIntent(request),
                true,
                Receipt.class,
                () -> decide(context, request));
        return view(context(id, authority.scope(QUERY)), true);
    }

    /** 内部JSON工具保持原隔离配置；明确把时刻转为UTC文本，不为一个DTO改变全局序列化约定。 */
    private static String auditIntent(QualificationAdminRequests.Audit request) {
        Map<String, Object> intent = new TreeMap<>();
        intent.put("expectedVersion", request.expectedVersion());
        intent.put("expectedProfileVersion", request.expectedProfileVersion());
        intent.put("expectedSubmissionRevision", request.expectedSubmissionRevision());
        intent.put("expectedCompanyRevision", request.expectedCompanyRevision());
        intent.put("decision", request.decision());
        intent.put("opinion", request.opinion());
        intent.put("publicOpinion", request.publicOpinion());
        intent.put("rejectFields", request.rejectFields());
        intent.put(
                "reviewDueAt",
                request.reviewDueAt() == null ? null : request.reviewDueAt().toString());
        return ViJson.write(intent);
    }

    private Context context(long id, Set<Long> scope) {
        Map<String, Object> meta = mapper.context(id);
        if (meta == null || !visible(nullableNumber(meta, "owner_dept_id"), scope)) invisible();
        long account = number(meta, "account_id");
        mapper.lockAccount(account);
        DriverProfile profile = qualifications.lockProfile(account);
        QualificationApplication application = qualifications.lock(id);
        if (profile == null
                || application == null
                || !Objects.equals(application.getDriverId(), profile.getId())) invisible();
        QualificationSubmission submission =
                submissions.authenticated(id, application.getSubmissionRevision());
        return new Context(meta, profile, application, submission);
    }

    private Receipt decide(Context context, QualificationAdminRequests.Audit request) {
        var profile = context.profile();
        var application = context.application();
        if (application.getVersion() != request.expectedVersion()
                || profile.getVersion() != request.expectedProfileVersion()
                || application.getSubmissionRevision() != request.expectedSubmissionRevision()
                || profile.getTrainingContextRevision() != request.expectedCompanyRevision())
            versionConflict();
        boolean repair = "REPAIR_REJECTION".equals(request.decision());
        if (repair ? !repairable(context) : !"SUBMITTED".equals(application.getStatus()))
            state("当前申请状态不允许本次审核");
        if ("APPROVE".equals(request.decision())) requireApproval(context, request.reviewDueAt());
        var review = createReview(context, request);
        QualificationSnapshot snapshot = null;
        if ("APPROVE".equals(request.decision()))
            snapshot = approve(context, review, request.reviewDueAt());
        else if (!repair) qualifications.updateInitialStatus(profile.getId(), "REJECTED");
        if (mapper.decideApplication(application.getId(), application.getVersion(), review) != 1)
            versionConflict();
        long revision =
                snapshot == null
                        ? Objects.requireNonNullElse(profile.getQualificationRevision(), 0L)
                        : snapshot.getQualificationRevision();
        lifecycle.decisionRecorded(
                profile.getAccountId(),
                profile.getId(),
                review.getId(),
                snapshot == null ? null : snapshot.getId(),
                revision,
                snapshot == null ? null : snapshot.getExpiresAt());
        String action =
                repair
                        ? "QUALIFICATION_REJECTION_REPAIRED"
                        : "APPROVE".equals(request.decision())
                                ? "QUALIFICATION_APPROVED"
                                : "QUALIFICATION_REJECTED";
        Map<String, Object> summary =
                Map.of(
                        "reviewId",
                        review.getId().toString(),
                        "applicationId",
                        application.getId().toString(),
                        "submissionRevision",
                        application.getSubmissionRevision());
        audit.recordSuccess(
                authority.actor(),
                "VI_DRIVER_APPLICATION",
                application.getId(),
                action,
                null,
                summary,
                null);
        outbox.publish(
                action + ":" + review.getId(),
                "VI_DRIVER_APPLICATION",
                application.getId(),
                action,
                summary);
        return new Receipt(review.getId());
    }

    private QualificationReview createReview(
            Context context, QualificationAdminRequests.Audit request) {
        var row = new QualificationReview();
        var application = context.application();
        Long owner = nullableNumber(context.meta(), "owner_dept_id");
        if (owner == null) state("历史申请必须先明确真实公司才能补正或审核");
        row.setApplicationId(application.getId());
        row.setDriverId(context.profile().getId());
        row.setOwnerDeptId(owner);
        row.setCompanyRevision(context.profile().getTrainingContextRevision());
        row.setSubmissionId(context.submission() == null ? null : context.submission().getId());
        row.setSubmissionRevision(application.getSubmissionRevision());
        row.setKind(
                "REPAIR_REJECTION".equals(request.decision()) ? "REPAIR_REJECTION" : "DECISION");
        row.setDecision(request.decision());
        row.setReviewerUserId(authority.actor().sysUserId());
        row.setPublicOpinion(request.publicOpinion());
        row.setRejectFields(ViJson.write(request.rejectFields()));
        row.setApplicationVersion(application.getVersion());
        row.setProfileVersion(context.profile().getVersion());
        mapper.insertReview(row);
        String aad =
                "Q05:REVIEW:"
                        + row.getId()
                        + ":APPLICATION:"
                        + application.getId()
                        + ":REVISION:"
                        + row.getSubmissionRevision()
                        + ":KIND:"
                        + row.getKind();
        if (mapper.sealReviewOpinion(row.getId(), crypto.envelope(request.opinion(), aad)) != 1)
            state("审核意见封存失败");
        return mapper.review(row.getId());
    }

    private QualificationSnapshot approve(
            Context context, QualificationReview review, Instant due) {
        var profile = context.profile();
        var submission = context.submission();
        Map<String, Object> payload = payload(context);
        var snapshot = new QualificationSnapshot();
        snapshot.setDriverId(profile.getId());
        snapshot.setAccountId(profile.getAccountId());
        snapshot.setApplicationId(context.application().getId());
        snapshot.setSubmissionId(submission.getId());
        snapshot.setReviewId(review.getId());
        snapshot.setQualificationRevision(
                Objects.requireNonNullElse(profile.getQualificationRevision(), 0L) + 1);
        snapshot.setOwnerDeptId(profile.getOwnerDeptId());
        snapshot.setCompanyRevision(profile.getTrainingContextRevision());
        snapshot.setMaterialRevision(submission.getMaterialRevision());
        snapshot.setMaterialHash(submission.getMaterialHash());
        snapshot.setLicenseClass(payload.get("licenseClass").toString());
        boolean longTerm = Boolean.TRUE.equals(payload.get("longTermValid"));
        snapshot.setLicenseLongTerm(longTerm);
        LocalDate validTo = longTerm ? null : LocalDate.parse(payload.get("validTo").toString());
        snapshot.setLicenseValidTo(
                validTo == null ? null : validTo.atStartOfDay(SHANGHAI).toInstant());
        snapshot.setReviewDueAt(due);
        Instant licenseEnd =
                validTo == null ? null : validTo.plusDays(1).atStartOfDay(SHANGHAI).toInstant();
        snapshot.setExpiresAt(licenseEnd != null && licenseEnd.isBefore(due) ? licenseEnd : due);
        snapshot = snapshots.insertAndSeal(snapshot);
        if (mapper.approveProfile(profile.getId(), profile.getVersion(), snapshot) != 1)
            versionConflict();
        return snapshot;
    }

    private void requireApproval(Context context, Instant due) {
        List<Map<String, String>> blockers = blockers(context);
        if (!blockers.isEmpty()) state(blockers.get(0).get("message"));
        if (due == null || !due.isAfter(Instant.now())) data("复审截止必须是明确的未来UTC时刻");
        var account = mapper.lockAccount(context.profile().getAccountId());
        if (account == null
                || !"ACTIVE".equals(account.get("status"))
                || !"ACTIVE".equals(account.get("security_status"))) state("申请账号当前不可用");
        authority.department(context.profile().getOwnerDeptId(), authority.scope(AUDIT));
        verifyFiles(context);
    }

    private List<Map<String, String>> blockers(Context context) {
        List<Map<String, String>> result = new ArrayList<>();
        var submission = context.submission();
        if (submission == null) {
            result.add(
                    blocker("submissionRevision", "SUBMISSION_UNTRUSTED", "缺少可信提交快照，请驳回补正后重新提交"));
            return result;
        }
        if (!Objects.equals(context.profile().getOwnerDeptId(), submission.getOwnerDeptId())
                || context.profile().getTrainingContextRevision()
                        != submission.getCompanyRevision())
            result.add(blocker("companyRevision", "COMPANY_CHANGED", "归属公司或公司版本已变化，不能批准旧提交"));
        if (context.application().getMaterialRevision() != submission.getMaterialRevision()
                || !TrainingCompletionAuthority.equal(
                        context.application().getMaterialHash(), submission.getMaterialHash()))
            result.add(blocker("materialRevision", "MATERIAL_CHANGED", "当前申请与原提交材料不一致"));
        Map<String, Object> payload = payload(context);
        try {
            for (String field :
                    List.of("realName", "idCardNo", "licenseClass", "firstIssueDate", "validFrom"))
                if (payload.get(field) == null || payload.get(field).toString().isBlank())
                    result.add(blocker(field, "REQUIRED", "原提交缺少必要资料"));
            if (!Boolean.TRUE.equals(payload.get("longTermValid"))
                    && (payload.get("validTo") == null
                            || LocalDate.parse(payload.get("validTo").toString())
                                    .isBefore(LocalDate.now(SHANGHAI))))
                result.add(blocker("validTo", "LICENSE_EXPIRED", "提交的驾驶证已到期或期限不明确"));
        } catch (RuntimeException failure) {
            result.add(blocker("validTo", "MATERIAL_INVALID", "原提交证件日期不可读取"));
        }
        if (verifications.frozenProofs(submission).size() != 3)
            result.add(blocker("checks", "VERIFICATION_UNTRUSTED", "提交时的设备或双人人工证明不可认证"));
        if (training.frozenProof(submission) == null)
            result.add(blocker("TRAINING", "TRAINING_UNTRUSTED", "缺少提交时已完成的冻结必修课程事实"));
        return result;
    }

    private void verifyFiles(Context context) {
        Map<String, Object> evidence =
                ViJson.read(context.submission().getEvidenceFileIds(), Map.class);
        Map<Long, String> ordered = new TreeMap<>();
        for (String field : FILES) {
            Object value = evidence.get(field);
            if (value == null) data("提交缺少四类必要证件文件");
            long id = QualificationAdminRequests.id(value.toString());
            if (ordered.put(id, field) != null) data("四证件槽位必须使用不同文件");
        }
        for (var entry : ordered.entrySet()) {
            var file = mapper.lockEvidence(entry.getKey());
            String purpose = entry.getValue().startsWith("idCard") ? "ID_CARD" : "DRIVER_LICENSE";
            if (file == null
                    || !Objects.equals(
                            nullableNumber(file, "application_id"), context.application().getId())
                    || !Objects.equals(
                            nullableNumber(file, "uploader_account_id"),
                            context.profile().getAccountId())
                    || !purpose.equals(file.get("purpose"))
                    || !"READY".equals(file.get("status"))) data("冻结证件文件尚未就绪或关联不正确");
        }
    }

    private Map<String, Object> payload(Context context) {
        if (context.submission() == null) return Map.of();
        return ViJson.read(
                crypto.unenvelope(
                        context.submission().getSensitivePayloadCipher(),
                        "Q01:ACCOUNT:"
                                + context.profile().getAccountId()
                                + ":APPLICATION:"
                                + context.application().getId()
                                + ":PAYLOAD"),
                Map.class);
    }

    private void requireWriteScope(Context context) {
        for (String permission : List.of(QUERY, AUDIT)) {
            Set<Long> scope = authority.scope(permission);
            if (!visible(nullableNumber(context.meta(), "owner_dept_id"), scope)
                    || !visible(context.profile().getOwnerDeptId(), scope)) invisible();
        }
    }

    private void denySelf(Context context) {
        if (self(context)) throw new ViException(ViErrorCode.VI_SELF_REVIEW_DENIED, "申请人不能审核本人的资格");
    }

    private boolean self(Context context) {
        long reviewer = authority.actor().sysUserId();
        if (applicants.users(context.profile().getAccountId()).contains(reviewer)) return true;
        if (context.submission() != null) {
            List<?> frozen = ViJson.read(context.submission().getApplicantUserIds(), List.class);
            return frozen.stream().anyMatch(id -> Long.toString(reviewer).equals(id.toString()));
        }
        return false;
    }

    private boolean repairable(Context context) {
        return "REJECTED".equals(context.application().getStatus())
                && rejected(context.application()).isEmpty()
                && mapper.normalReviewCount(context.application().getId()) == 0;
    }

    private Map<String, Object> view(Context context, boolean detail) {
        var application = context.application();
        var profile = context.profile();
        boolean currentVisible = canSeeCurrent(context);
        boolean self = self(context);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationId", application.getId().toString());
        result.put("driverId", profile.getId().toString());
        result.put("applicantName", Objects.toString(context.meta().get("applicant_name"), ""));
        result.put("ownerDeptId", external(nullableNumber(context.meta(), "owner_dept_id")));
        result.put("companyName", context.meta().get("company_name"));
        result.put(
                "currentOwnerDeptId", currentVisible ? external(profile.getOwnerDeptId()) : null);
        result.put(
                "currentCompanyName",
                currentVisible ? context.meta().get("current_company_name") : null);
        result.put("companyRevision", currentVisible ? profile.getTrainingContextRevision() : null);
        result.put("status", application.getStatus());
        result.put(
                "effectiveQualificationStatus",
                currentVisible ? profile.getQualificationStatus() : null);
        result.put("profileVersion", currentVisible ? profile.getVersion() : null);
        result.put("version", application.getVersion());
        result.put("submissionRevision", application.getSubmissionRevision());
        result.put("materialRevision", application.getMaterialRevision());
        result.put("submittedAt", time(application.getSubmittedAt()));
        result.put("reviewDueAt", currentVisible ? time(profile.getReviewDueAt()) : null);
        result.put(
                "qualificationRevision",
                currentVisible ? profile.getQualificationRevision() : null);
        result.put("selfReviewDenied", self);
        result.put("hasTrustedSubmission", context.submission() != null);
        result.put("legacyRejectionRepairable", repairable(context));
        List<String> actions = new ArrayList<>();
        if (canQuery(context)) actions.add("VIEW_QUALIFICATION");
        if (!self && canWrite(context)) {
            if ("SUBMITTED".equals(application.getStatus())) actions.add("AUDIT_QUALIFICATION");
            if (repairable(context)) actions.add("REPAIR_QUALIFICATION_REJECTION");
        }
        result.put("allowedActions", actions);
        if (detail) addDetail(result, context, currentVisible);
        return result;
    }

    private void addDetail(Map<String, Object> result, Context context, boolean currentVisible) {
        var submission = context.submission();
        Map<String, Object> draft = submission == null ? null : draft(context);
        result.put("draft", draft);
        result.put("items", submission == null ? List.of() : FILES);
        result.put("rejectFields", rejected(context.application()));
        result.put("publicReviewOpinion", context.application().getPublicReviewOpinion());
        Map<String, ?> verified =
                submission == null ? Map.of() : verifications.frozenProofs(submission);
        boolean trained = submission != null && training.frozenProof(submission) != null;
        result.put(
                "checks",
                CHECKS.stream()
                        .map(
                                type ->
                                        Map.of(
                                                "type",
                                                type,
                                                "status",
                                                "TRAINING".equals(type)
                                                        ? trained ? "PASSED" : "UNAVAILABLE"
                                                        : verified.containsKey(type)
                                                                ? "PASSED"
                                                                : "UNAVAILABLE"))
                        .toList());
        result.put("reviewBlockers", blockers(context));
        result.put("submissionId", submission == null ? null : submission.getId().toString());
        Long pointer = nullableNumber(context.meta(), "effective_snapshot_id");
        result.put("effectiveSnapshotId", currentVisible ? external(pointer) : null);
        result.put("effectiveQualification", currentVisible ? effective(context, pointer) : null);
        result.put(
                "reviews",
                mapper.reviews(context.application().getId()).stream()
                        // 同申请跨公司补正重提，不把原公司的审核历史读取权转给新公司。
                        .filter(review -> visible(review.getOwnerDeptId(), authority.scope(QUERY)))
                        .map(this::reviewView)
                        .toList());
    }

    private Map<String, Object> draft(Context context) {
        Map<String, Object> payload = payload(context);
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : QualificationRequests.MATERIAL_FIELDS)
            if (!"idCardNo".equals(field)) result.put(field, payload.get(field));
        String id = Objects.toString(payload.get("idCardNo"), "");
        result.put("consentVersion", context.submission().getConsentVersion());
        result.put(
                "consentTextHash",
                com.xinyi.rongli.config.service.QualificationConsentSnapshot.hash(
                        context.submission().getConsentText()));
        result.put("consentAcceptedAt", time(context.submission().getConsentAcceptedAt()));
        result.put("idCardNoProvided", !id.isEmpty());
        result.put(
                "idCardNoMask",
                id.length() == 18 ? id.substring(0, 3) + "***********" + id.substring(14) : null);
        return result;
    }

    private Map<String, Object> effective(Context context, Long id) {
        if (id == null) return null;
        var snapshot = snapshots.authenticated(id);
        if (snapshot == null
                || snapshot.getDriverId() != context.profile().getId()
                || snapshot.getAccountId() != context.profile().getAccountId()) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("snapshotId", snapshot.getId().toString());
        result.put("qualificationRevision", snapshot.getQualificationRevision());
        result.put("ownerDeptId", Long.toString(snapshot.getOwnerDeptId()));
        result.put("companyRevision", snapshot.getCompanyRevision());
        result.put("licenseClass", snapshot.getLicenseClass());
        result.put(
                "licenseValidTo",
                snapshot.getLicenseValidTo() == null
                        ? null
                        : snapshot.getLicenseValidTo().atZone(SHANGHAI).toLocalDate().toString());
        result.put("licenseLongTerm", snapshot.getLicenseLongTerm());
        result.put("reviewDueAt", time(snapshot.getReviewDueAt()));
        result.put("expiresAt", time(snapshot.getExpiresAt()));
        result.put("approvedAt", time(snapshot.getApprovedAt()));
        result.put("status", context.profile().getQualificationStatus());
        String status =
                !snapshot.getExpiresAt().isAfter(Instant.now())
                        ? "EXPIRED"
                        : snapshot.getOwnerDeptId()
                                                != Objects.requireNonNullElse(
                                                        context.profile().getOwnerDeptId(), -1L)
                                        || snapshot.getCompanyRevision()
                                                != context.profile().getTrainingContextRevision()
                                ? "SUSPENDED"
                                : context.profile().getQualificationStatus();
        result.put("effectiveStatus", status);
        return result;
    }

    private Map<String, Object> reviewView(QualificationReview review) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reviewId", review.getId().toString());
        result.put("decision", review.getDecision());
        result.put("submissionRevision", review.getSubmissionRevision());
        result.put("publicOpinion", review.getPublicOpinion());
        result.put("rejectFields", ViJson.read(review.getRejectFields(), List.class));
        result.put("reviewedAt", time(review.getReviewedAt()));
        result.put("reviewerUserId", Long.toString(review.getReviewerUserId()));
        return result;
    }

    private boolean canSeeCurrent(Context context) {
        try {
            return visible(context.profile().getOwnerDeptId(), authority.scope(QUERY));
        } catch (ViException failure) {
            return false;
        }
    }

    private boolean canQuery(Context context) {
        try {
            return visible(nullableNumber(context.meta(), "owner_dept_id"), authority.scope(QUERY));
        } catch (ViException failure) {
            return false;
        }
    }

    private boolean canWrite(Context context) {
        try {
            requireWriteScope(context);
            return true;
        } catch (ViException failure) {
            return false;
        }
    }

    private static List<String> rejected(QualificationApplication application) {
        try {
            if (application.getRejectFields() == null) return List.of();
            List<?> fields = ViJson.read(application.getRejectFields(), List.class);
            if (!fields.stream().allMatch(QualificationRequests.MATERIAL_FIELDS::contains))
                return List.of();
            return fields.stream().map(String::valueOf).sorted().toList();
        } catch (RuntimeException failure) {
            return List.of();
        }
    }

    private <T> T transaction(String action, Supplier<T> work) {
        Actor actor = authority.actor();
        try {
            return tx.execute(
                    status -> {
                        authority.lockAuthority();
                        return work.get();
                    });
        } catch (RuntimeException failure) {
            audit.recordFailure(
                    actor,
                    "VI_DRIVER_APPLICATION",
                    0,
                    action + "_DENIED",
                    null,
                    null,
                    failure instanceof ViException error
                            ? error.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw failure;
        }
    }

    private static boolean visible(Long owner, Set<Long> scope) {
        return scope == null || owner != null && scope.contains(owner);
    }

    private static Long nullableNumber(Map<String, Object> row, String key) {
        return row.get(key) == null ? null : ((Number) row.get(key)).longValue();
    }

    private static long number(Map<String, Object> row, String key) {
        Long value = nullableNumber(row, key);
        return value == null ? Long.MIN_VALUE : value;
    }

    private static String external(Long value) {
        return value == null ? null : value.toString();
    }

    private static String time(Instant value) {
        return value == null ? null : value.toString();
    }

    private static Map<String, String> blocker(String field, String code, String message) {
        return Map.of("field", field, "reasonCode", code, "message", message);
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    private static void state(String message) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, message);
    }

    private static void data(String message) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, message);
    }

    private static void versionConflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }
}
