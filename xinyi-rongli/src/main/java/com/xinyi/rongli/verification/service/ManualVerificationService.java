package com.xinyi.rongli.verification.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.verification.domain.ManualVerificationRecord;
import com.xinyi.rongli.verification.dto.ManualVerificationRequests.*;
import com.xinyi.rongli.verification.mapper.ManualVerificationMapper;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/** 无附件的人工核验：只记录两个不同有权人员的确认，不伪称第三方报告或自动真实性检测。 */
@Service
public class ManualVerificationService implements ManualVerificationEvidence {
    private final ManualVerificationMapper mapper;
    private final PhoneCrypto phones;
    private final IntegrationAuthority authority;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final ObjectProvider<CanonicalVerificationAuthority> canonical;
    private final TransactionTemplate tx;

    public ManualVerificationService(
            ManualVerificationMapper mapper,
            PhoneCrypto phones,
            IntegrationAuthority authority,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            ObjectProvider<CanonicalVerificationAuthority> canonical,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.phones = phones;
        this.authority = authority;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        this.canonical = canonical;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Page(int code, String msg, List<?> rows, long total) {}

    public record Receipt(long recordId) {}

    public Page list(Query query) {
        return transaction(
                "MANUAL_VERIFICATION_LIST",
                () -> {
                    Set<Long> scope = authority.scope("biz:manualverification:list");
                    return new Page(
                            200,
                            "操作成功",
                            mapper.list(query, scope).stream()
                                    .map(row -> view(row, false))
                                    .toList(),
                            mapper.count(query, scope));
                });
    }

    public Page applications(Query query) {
        return transaction(
                "MANUAL_VERIFICATION_APPLICATIONS",
                () -> {
                    Set<Long> scope = authority.scope("biz:manualverification:query");
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (var item : mapper.applications(query, scope)) {
                        var result = new LinkedHashMap<String, Object>();
                        for (String name : List.of("application_id", "driver_id", "owner_dept_id"))
                            result.put(camel(name), item.get(name).toString());
                        // 昵称可空且不冒充实名；公开候选契约要求字符串，缺值统一为空串。
                        for (String name : List.of("applicant_name", "company_name"))
                            result.put(camel(name), Objects.toString(item.get(name), ""));
                        for (String name :
                                List.of("version", "company_revision", "material_revision"))
                            result.put(camel(name), item.get(name));
                        rows.add(result);
                    }
                    return new Page(200, "操作成功", rows, mapper.applicationCount(query, scope));
                });
    }

    public Map<String, Object> get(long id) {
        return transaction("MANUAL_VERIFICATION_QUERY", () -> view(read(id, "query"), true));
    }

    public Map<String, Object> create(Create input, String key) {
        return transaction(
                "MANUAL_VERIFICATION_CREATE",
                () -> {
                    Actor actor = authority.actor();
                    Map<String, Object> context = context(input.applicationId(), "create");
                    if (number(context, "version") != input.expectedApplicationVersion()
                            || number(context, "company_revision")
                                    != input.expectedCompanyRevision()
                            || number(context, "material_revision")
                                    != input.expectedMaterialRevision()) conflict();
                    denyApplicant(context, actor);
                    Receipt receipt =
                            idem.execute(
                                    IdempotencyService.subjectKey(actor),
                                    "MANUAL_VERIFICATION_CREATE:" + input.applicationId(),
                                    key,
                                    ViJson.write(input),
                                    true,
                                    Receipt.class,
                                    () -> insert(actor, context, input));
                    return view(read(receipt.recordId(), "query"), true);
                });
    }

    private Receipt insert(Actor actor, Map<String, Object> context, Create input) {
        if (mapper.pending(
                        input.applicationId(),
                        input.expectedCompanyRevision(),
                        input.expectedMaterialRevision(),
                        input.type())
                > 0) throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "该项已有待复核记录，请先完成复核");
        var row = new ManualVerificationRecord();
        row.setApplicationId(input.applicationId());
        row.setOwnerDeptId(number(context, "owner_dept_id"));
        row.setCompanyRevision(input.expectedCompanyRevision());
        row.setMaterialRevision(input.expectedMaterialRevision());
        row.setMaterialHash((byte[]) context.get("material_hash"));
        row.setVerificationType(input.type());
        row.setConclusion(input.conclusion());
        row.setSubmittedByUserId(actor.sysUserId());
        row.setApplicantUserIds(ViJson.write(applicantUsers(number(context, "account_id"))));
        mapper.insert(row);
        // 取得真实自增ID后同事务绑定备注AAD，不能把其它人工记录的密文挪来使用。
        row.setNoteCipher(crypto.envelope(ViJson.write(input.note()), noteAad(row, false)));
        if (mapper.setNoteCipher(row) != 1) conflict();
        record(actor, row.getId(), "MANUAL_VERIFICATION_SUBMITTED", 0);
        return new Receipt(row.getId());
    }

    public Map<String, Object> review(long id, Review input, String key) {
        return transaction(
                "MANUAL_VERIFICATION_REVIEW",
                () -> {
                    Actor actor = authority.actor();
                    var initial = read(id, "review");
                    var context = context(initial.getApplicationId(), "review");
                    current(initial, context);
                    denyApplicant(context, actor);
                    var row = mapper.lock(id);
                    if (Objects.equals(row.getSubmittedByUserId(), actor.sysUserId())
                            || frozenApplicant(row, actor.sysUserId()))
                        throw new ViException(ViErrorCode.VI_SELF_REVIEW_DENIED);
                    Receipt receipt =
                            idem.execute(
                                    IdempotencyService.subjectKey(actor),
                                    "MANUAL_VERIFICATION_REVIEW:" + id,
                                    key,
                                    ViJson.write(input),
                                    true,
                                    Receipt.class,
                                    () -> finishReview(actor, row, input));
                    return view(read(receipt.recordId(), "query"), true);
                });
    }

    private Receipt finishReview(Actor actor, ManualVerificationRecord row, Review input) {
        if (row.getVersion() != input.expectedVersion()) conflict();
        if (!"PENDING_REVIEW".equals(row.getStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "该人工核验已处理，不能覆盖原结论");
        row.setReviewedByUserId(actor.sysUserId());
        row.setStatus("CONFIRM".equals(input.decision()) ? "CONFIRMED" : "RETURNED");
        row.setReviewNoteCipher(crypto.envelope(ViJson.write(input.note()), noteAad(row, true)));
        if (mapper.review(row) != 1) conflict();
        if ("CONFIRMED".equals(row.getStatus())) {
            // 先写真实双人终态，再由统一发布器重读、认证并建证明，同事务链接；任一步失败全部回滚。
            long proof = canonical.getObject().publishManualProof(actor, row.getId());
            if (mapper.linkProof(row.getId(), proof) != 1) conflict();
        }
        record(actor, row.getId(), "MANUAL_VERIFICATION_REVIEWED", row.getVersion() + 1);
        return new Receipt(row.getId());
    }

    private ManualVerificationRecord read(long id, String action) {
        var row = mapper.find(id);
        Set<Long> scope = authority.scope("biz:manualverification:" + action);
        if (row == null || scope != null && !scope.contains(row.getOwnerDeptId())) invisible();
        // 写响应包含备注详情，因此独立query范围也必须覆盖，不以写权限冒充读取授权。
        if (!"query".equals(action)) requireScope(row.getOwnerDeptId(), "query");
        return row;
    }

    private Map<String, Object> context(long application, String action) {
        var context = mapper.context(application);
        if (context == null || context.get("owner_dept_id") == null) invisible();
        long owner = number(context, "owner_dept_id");
        requireScope(owner, action);
        requireScope(owner, "query");
        if (number(context, "current_application_id") != application
                || !Set.of("DRAFT", "REJECTED").contains(context.get("status"))
                || context.get("material_hash") == null) conflict();
        return context;
    }

    private void requireScope(long owner, String action) {
        Set<Long> scope = authority.scope("biz:manualverification:" + action);
        if (scope != null && !scope.contains(owner)) invisible();
        authority.department(owner, scope);
    }

    private static void current(ManualVerificationRecord row, Map<String, Object> context) {
        if (!Objects.equals(row.getOwnerDeptId(), number(context, "owner_dept_id"))
                || row.getCompanyRevision() != number(context, "company_revision")
                || row.getMaterialRevision() != number(context, "material_revision")
                || !java.security.MessageDigest.isEqual(
                        row.getMaterialHash(), (byte[]) context.get("material_hash"))) conflict();
    }

    private void denyApplicant(Map<String, Object> context, Actor actor) {
        // 跨域同人通过真实员工关联识别，含历史关联，不能拿accountId与sysUserId数值混比。
        if (applicantUsers(number(context, "account_id")).contains(actor.sysUserId()))
            throw new ViException(ViErrorCode.VI_SELF_REVIEW_DENIED, "申请人不能经办或复核本人的资格核验");
    }

    private Set<Long> applicantUsers(long account) {
        Set<Long> result = new HashSet<>(mapper.boundUsers(account));
        var phoneRow = mapper.verifiedPhoneLookup(account);
        byte[] lookup = phoneRow == null ? null : (byte[]) phoneRow.get("phone_lookup");
        if (lookup != null)
            for (var user : mapper.usersWithPhone()) {
                String phone = Objects.toString(user.get("phonenumber"), "");
                if (java.security.MessageDigest.isEqual(lookup, phones.lookup(phone)))
                    result.add(((Number) user.get("user_id")).longValue());
            }
        return result;
    }

    private static boolean frozenApplicant(ManualVerificationRecord row, long user) {
        if (row.getApplicantUserIds() == null) return true;
        return ViJson.read(row.getApplicantUserIds(), List.class).stream()
                .anyMatch(item -> Long.toString(user).equals(item.toString()));
    }

    @Override
    public Evidence approvedEvidence(long reviewId) {
        var row = mapper.find(reviewId);
        if (row == null
                || !"CONFIRMED".equals(row.getStatus())
                || row.getReviewedByUserId() == null
                || row.getReviewedAt() == null) return null;
        return new Evidence(
                row.getId(),
                row.getId(),
                row.getApplicationId(),
                row.getOwnerDeptId(),
                row.getCompanyRevision(),
                row.getMaterialRevision(),
                row.getMaterialHash(),
                row.getVerificationType(),
                row.getSubmittedByUserId(),
                row.getReviewedByUserId(),
                row.getConclusion(),
                row.getReviewedAt(),
                row.getProofId());
    }

    @Override
    public void requireReviewAuthority(Actor actor, long owner) {
        // 共用证明发布器也可能由内部入口调用；独立核验数据库auth_version，不能只信调用者缓存。
        authority.lockAuthority();
        if (!Objects.equals(authority.actor().sysUserId(), actor.sysUserId()))
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        requireScope(owner, "review");
        requireScope(owner, "query");
    }

    private Map<String, Object> view(ManualVerificationRecord row, boolean notes) {
        var value = new LinkedHashMap<String, Object>();
        value.put("recordId", row.getId().toString());
        value.put("applicationId", row.getApplicationId().toString());
        value.put("driverId", row.getDriverId().toString());
        value.put("applicantName", Objects.toString(row.getApplicantName(), ""));
        value.put("ownerDeptId", row.getOwnerDeptId().toString());
        value.put("companyName", Objects.toString(row.getCompanyName(), ""));
        value.put("companyRevision", row.getCompanyRevision());
        value.put("materialRevision", row.getMaterialRevision());
        value.put("type", row.getVerificationType());
        value.put("conclusion", row.getConclusion());
        value.put("status", row.getStatus());
        value.put("submittedByUserId", row.getSubmittedByUserId().toString());
        value.put("submittedByName", Objects.toString(row.getSubmittedByName(), ""));
        value.put("submittedAt", time(row.getSubmittedAt()));
        value.put(
                "reviewedByUserId",
                row.getReviewedByUserId() == null ? null : row.getReviewedByUserId().toString());
        value.put("reviewedByName", row.getReviewedByName());
        value.put("reviewedAt", time(row.getReviewedAt()));
        value.put(
                "note",
                notes
                        ? ViJson.read(
                                crypto.unenvelope(row.getNoteCipher(), noteAad(row, false)),
                                String.class)
                        : null);
        value.put(
                "reviewNote",
                notes && row.getReviewNoteCipher() != null
                        ? ViJson.read(
                                crypto.unenvelope(row.getReviewNoteCipher(), noteAad(row, true)),
                                String.class)
                        : null);
        value.put("version", row.getVersion());
        value.put("allowedActions", actions(row));
        return value;
    }

    private List<String> actions(ManualVerificationRecord row) {
        var result = new ArrayList<String>();
        if (allowed(row.getOwnerDeptId(), "query")) result.add("VIEW_MANUAL_VERIFICATION");
        if (!"PENDING_REVIEW".equals(row.getStatus())
                || !allowed(row.getOwnerDeptId(), "query")
                || !allowed(row.getOwnerDeptId(), "review")
                || Objects.equals(row.getSubmittedByUserId(), authority.actor().sysUserId())
                || frozenApplicant(row, authority.actor().sysUserId())) return result;
        var context = mapper.context(row.getApplicationId());
        if (context != null
                && number(context, "owner_dept_id") == row.getOwnerDeptId()
                && number(context, "company_revision") == row.getCompanyRevision()
                && number(context, "material_revision") == row.getMaterialRevision()
                && Set.of("DRAFT", "REJECTED").contains(context.get("status"))
                && !applicantUsers(row.getAccountId()).contains(authority.actor().sysUserId()))
            result.add("REVIEW_MANUAL_VERIFICATION");
        return result;
    }

    private boolean allowed(long owner, String action) {
        String permission = "biz:manualverification:" + action;
        if (!SecurityUtils.hasPermi(permission)) return false;
        Set<Long> scope = authority.scope(permission);
        return scope == null || scope.contains(owner);
    }

    private static String noteAad(ManualVerificationRecord row, boolean review) {
        return "Q04:MANUAL:"
                + row.getId()
                + ":"
                + row.getApplicationId()
                + ":"
                + row.getOwnerDeptId()
                + ":"
                + row.getCompanyRevision()
                + ":"
                + row.getMaterialRevision()
                + ":"
                + row.getVerificationType()
                + ":"
                + row.getSubmittedByUserId()
                + (review ? ":REVIEW:" + row.getId() + ":" + row.getReviewedByUserId() : ":SUBMIT");
    }

    private void record(Actor actor, long id, String action, long version) {
        var summary = Map.of("recordId", Long.toString(id), "version", version);
        audit.recordSuccess(actor, "VI_MANUAL_VERIFICATION", id, action, null, summary, null);
        outbox.publish(action + ":" + id, "VI_MANUAL_VERIFICATION", id, action, summary);
    }

    private <T> T transaction(String action, Supplier<T> work) {
        Actor actor = authority.actor();
        try {
            return tx.execute(
                    status -> {
                        // 与MINI、公司迁移及证明发布共享目录→档案→申请→核验记录锁顺序。
                        authority.lockAuthority();
                        return work.get();
                    });
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_MANUAL_VERIFICATION",
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

    private static long number(Map<String, Object> value, String key) {
        return value.get(key) == null ? Long.MIN_VALUE : ((Number) value.get(key)).longValue();
    }

    private static String time(Instant value) {
        return value == null ? null : value.toString();
    }

    private static String camel(String value) {
        var parts = value.split("_");
        var result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++)
            result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        return result.toString();
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }
}
