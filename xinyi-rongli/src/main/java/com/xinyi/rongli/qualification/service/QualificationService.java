package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.config.mapper.ConfigMapper;
import com.xinyi.rongli.config.service.QualificationConsentSnapshot;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.file.service.FileAuthority;
import com.xinyi.rongli.file.service.PrivateFileService;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.qualification.domain.QualificationApplication;
import com.xinyi.rongli.qualification.dto.QualificationRequests;
import com.xinyi.rongli.qualification.mapper.QualificationMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;

/** 本人资格材料与生效司机资格分离；所有写入以真实账号、父档案和材料版本为依据。 */
@Service
public class QualificationService {
    public static final String CONSENT_VERSION = "vi.biz.qualification.consent.version";
    public static final String CONSENT_TEXT = "vi.biz.qualification.consent.text";
    private static final Set<String> ACTIVE = Set.of("DRAFT", "SUBMITTED", "REJECTED");
    private static final List<String> CHECK_TYPES =
            List.of("DEVICE_AUTH", "BACKGROUND", "LICENSE", "TRAINING");
    private static final List<String> FILE_FIELDS =
            List.of(
                    "idCardFrontFileId",
                    "idCardBackFileId",
                    "licenseMainFileId",
                    "licenseSubFileId");
    private final BusinessActorResolver actors;
    private final FileAuthority authority;
    private final ViAccountMapper accounts;
    private final QualificationMapper mapper;
    private final ConfigMapper configs;
    private final com.xinyi.rongli.training.service.TrainingCompletionAuthority trainingCompletion;
    private final com.xinyi.rongli.verification.service.CanonicalVerificationAuthority
            verifications;
    private final PrivateFileService files;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    @org.springframework.beans.factory.annotation.Value("${vi.environment:local}")
    private String environment;

    public QualificationService(
            BusinessActorResolver actors,
            FileAuthority authority,
            ViAccountMapper accounts,
            QualificationMapper mapper,
            ConfigMapper configs,
            com.xinyi.rongli.training.service.TrainingCompletionAuthority trainingCompletion,
            com.xinyi.rongli.verification.service.CanonicalVerificationAuthority verifications,
            PrivateFileService files,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.actors = actors;
        this.authority = authority;
        this.accounts = accounts;
        this.mapper = mapper;
        this.configs = configs;
        this.trainingCompletion = trainingCompletion;
        this.verifications = verifications;
        this.files = files;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long applicationId) {}

    public Map<String, Object> get() {
        Actor actor = actors.requireMini();
        return transactional(actor, "QUALIFICATION_QUERY", () -> readApplication(actor));
    }

    private Map<String, Object> readApplication(Actor actor) {
        guard(actor);
        DriverProfile profile = mapper.lockProfile(actor.accountId());
        if (profile == null) return null;
        QualificationApplication application = mapper.current(profile.getId());
        return application == null ? null : view(actor, profile, mapper.lock(application.getId()));
    }

    public Map<String, Object> create(QualificationRequests.Create request, String key) {
        Actor actor = actors.requireMini();
        return transactional(
                actor, "QUALIFICATION_CREATE", () -> createLocked(actor, request, key));
    }

    private Map<String, Object> createLocked(
            Actor actor, QualificationRequests.Create request, String key) {
        guard(actor);
        DriverProfile profile = mapper.lockProfile(actor.accountId());
        if (profile == null) {
            DriverProfile newProfile = new DriverProfile();
            newProfile.setAccountId(actor.accountId());
            mapper.insertProfile(newProfile);
            profile = mapper.lockProfile(actor.accountId());
        }
        DriverProfile currentProfile = profile;
        Receipt result =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "QUALIFICATION_CREATE",
                        key,
                        ViJson.write(request),
                        true,
                        Receipt.class,
                        () -> createApplication(actor, currentProfile, request));
        return view(actor, profile, owned(result.applicationId(), profile));
    }

    private Receipt createApplication(
            Actor actor, DriverProfile profile, QualificationRequests.Create request) {
        QualificationApplication previous = mapper.current(profile.getId());
        if (previous != null && ACTIVE.contains(previous.getStatus())) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "已有活动申请，请继续原申请");
        }
        QualificationApplication application = new QualificationApplication();
        application.setDriverId(profile.getId());
        application.setPreviousApplicationId(previous == null ? null : previous.getId());
        application.setStatus("DRAFT");
        acceptConsent(application, request.consentVersion(), request.consentTextHash());
        mapper.insert(application);
        Map<String, Object> payload = new TreeMap<>();
        payload.put("longTermValid", false);
        application.setSensitivePayloadCipher(
                crypto.envelope(ViJson.write(payload), aad(actor, application)));
        application.setEvidenceFileIds("{}");
        application.setMaterialHash(materialHash(actor, application, payload));
        if (mapper.save(application) != 1) conflict();
        application.setVersion(application.getVersion() + 1);
        record(actor, application, "QUALIFICATION_CREATED");
        return new Receipt(application.getId());
    }

    public Map<String, Object> save(QualificationRequests.Draft request, String key) {
        Actor actor = actors.requireMini();
        return transactional(actor, "QUALIFICATION_EDIT", () -> saveLocked(actor, request, key));
    }

    private Map<String, Object> saveLocked(
            Actor actor, QualificationRequests.Draft request, String key) {
        Context context = currentTarget(actor, request.applicationId());
        QualificationApplication application = context.application();
        Receipt result =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "QUALIFICATION_EDIT:" + application.getId(),
                        key,
                        ViJson.write(request),
                        true,
                        Receipt.class,
                        () -> saveApplication(actor, application, request));
        return view(actor, context.profile(), owned(result.applicationId(), context.profile()));
    }

    private Receipt saveApplication(
            Actor actor,
            QualificationApplication application,
            QualificationRequests.Draft request) {
        if (application.getVersion() != request.expectedVersion()) conflict();
        requireEditable(application, request.values().keySet());
        Map<String, Object> before = payload(actor, application);
        Map<String, Object> merged = new TreeMap<>(before);
        for (String field : QualificationRequests.MATERIAL_FIELDS) {
            if (!request.values().containsKey(field)) continue;
            Object value = request.values().get(field);
            if (value == null) merged.remove(field);
            else merged.put(field, value);
        }
        validateMaterials(merged);
        files.validateQualificationFiles(actor, application.getId(), merged);
        // 摘要绑定规范化明文材料而非随机密文。只有实际材料变化才使旧核验/学习依据失效。
        if (!before.equals(merged)) {
            application.setMaterialRevision(application.getMaterialRevision() + 1);
            application.setMaterialHash(materialHash(actor, application, merged));
        }
        if (request.values().containsKey("consentVersion")) {
            acceptConsent(
                    application,
                    (String) request.values().get("consentVersion"),
                    (String) request.values().get("consentTextHash"));
        }
        application.setSensitivePayloadCipher(
                crypto.envelope(ViJson.write(merged), aad(actor, application)));
        application.setEvidenceFileIds(ViJson.write(evidence(merged)));
        if (mapper.save(application) != 1) conflict();
        application.setVersion(application.getVersion() + 1);
        record(actor, application, "QUALIFICATION_DRAFT_SAVED");
        return new Receipt(application.getId());
    }

    public Map<String, Object> submit(QualificationRequests.Submit request, String key) {
        Actor actor = actors.requireMini();
        return transactional(
                actor, "QUALIFICATION_SUBMIT", () -> submitLocked(actor, request, key));
    }

    private Map<String, Object> submitLocked(
            Actor actor, QualificationRequests.Submit request, String key) {
        Context context = currentTarget(actor, request.applicationId());
        QualificationApplication application = context.application();
        Receipt result =
                idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "QUALIFICATION_SUBMIT:" + application.getId(),
                        key,
                        ViJson.write(request),
                        true,
                        Receipt.class,
                        () -> submitApplication(actor, context.profile(), application, request));
        return view(
                actor,
                mapper.lockProfile(actor.accountId()),
                owned(result.applicationId(), context.profile()));
    }

    private record Context(DriverProfile profile, QualificationApplication application) {}

    /** 单例路径仍明确绑定请求目标；旧本人申请409，他人/不存在404，不能让未知重试漂移到新复审。 */
    private Context currentTarget(Actor actor, long requestedId) {
        guard(actor);
        DriverProfile profile = requireProfile(actor);
        QualificationApplication requested = owned(requestedId, profile);
        QualificationApplication current = requireCurrent(profile);
        if (!Objects.equals(current.getId(), requested.getId())) conflict();
        return new Context(profile, current);
    }

    private Receipt submitApplication(
            Actor actor,
            DriverProfile profile,
            QualificationApplication application,
            QualificationRequests.Submit request) {
        if (application.getVersion() != request.expectedVersion()
                || profile.getVersion() != request.expectedProfileVersion()) conflict();
        requireEditable(application, Set.of());
        requireCurrentConsent(application);
        Map<String, Object> materials = payload(actor, application);
        validateMaterials(materials);
        files.validateQualificationFiles(actor, application.getId(), materials);
        List<Map<String, String>> blocked = blockers(profile, application, materials);
        if (!blocked.isEmpty()) {
            Map<String, String> first = blocked.get(0);
            boolean training = "TRAINING".equals(first.get("field"));
            throw new ViException(
                    training ? ViErrorCode.VI_TRAINING_NOT_READY : ViErrorCode.VI_DATA_INVALID,
                    first.get("message"));
        }
        application.setOwnerDeptId(profile.getOwnerDeptId());
        // 只接收当前材料和可信核验时间的服务端记录；不从请求读取任何passed、proofId或hash。
        List<String> proofIds =
                verifications.currentProofs(profile, application).values().stream()
                        .filter(proof -> "PASSED".equals(proof.getStatus()))
                        .map(proof -> proof.getId().toString())
                        .toList();
        // 汇总培训只能冻结当前公司epoch/材料运行认可的唯一证明，不能顺带采入旧独立PASSED行。
        proofIds = new ArrayList<>(proofIds);
        Long trainingProofId = trainingCompletion.currentProof(profile, application);
        if (trainingProofId == null) throw new ViException(ViErrorCode.VI_TRAINING_NOT_READY);
        proofIds.add(trainingProofId.toString());
        application.setProviderResultIds(ViJson.write(proofIds));
        byte[] reason =
                crypto.envelope(
                        request.reason(),
                        "Q01:ACCOUNT:"
                                + actor.accountId()
                                + ":APPLICATION:"
                                + application.getId()
                                + ":SUBMIT:"
                                + (application.getSubmissionRevision() + 1));
        if (mapper.submit(application, profile.getVersion()) != 1) conflict();
        mapper.insertSubmission(application, profile.getVersion(), reason);
        mapper.updateInitialStatus(profile.getId(), "SUBMITTED");
        application.setVersion(application.getVersion() + 1);
        record(actor, application, "QUALIFICATION_SUBMITTED");
        return new Receipt(application.getId());
    }

    private void guard(Actor actor) {
        // 与上传及A04共用目录守卫；锁序为目录→账号/会话→档案→申请→有序文件，撤权和提交不能穿透。
        authority.guard(actor, "biz:driverqualification:query");
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (account.getPhoneVerifiedAt() == null || account.getPhoneCipher() == null) {
            throw new ViException(ViErrorCode.VI_PHONE_PROOF_REQUIRED);
        }
    }

    private DriverProfile requireProfile(Actor actor) {
        DriverProfile profile = mapper.lockProfile(actor.accountId());
        if (profile == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return profile;
    }

    private QualificationApplication requireCurrent(DriverProfile profile) {
        QualificationApplication current = mapper.current(profile.getId());
        if (current == null) throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "请先建立本人资格草稿");
        return owned(current.getId(), profile);
    }

    private QualificationApplication owned(long id, DriverProfile profile) {
        QualificationApplication application = mapper.lock(id);
        if (application == null || !Objects.equals(application.getDriverId(), profile.getId())) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return application;
    }

    private void requireCurrentConsent(QualificationApplication application) {
        QualificationConsentSnapshot consent = QualificationConsentSnapshot.read(configs, true);
        if (!consent.published()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "资格申请同意内容尚未发布");
        }
        if (!Objects.equals(consent.version(), application.getConsentVersion())
                || !Objects.equals(consent.text(), application.getConsentText())
                || application.getConsentAcceptedAt() == null) {
            throw new ViException(ViErrorCode.VI_CONFIG_CHANGED, "资格申请同意内容已变化，请重新阅读并明确同意");
        }
    }

    private void acceptConsent(
            QualificationApplication application, String version, String textHash) {
        // 两键同一SELECT并按key顺序加锁；同版本改正文也必须重新展示并取得明确同意。
        QualificationConsentSnapshot consent = QualificationConsentSnapshot.read(configs, true);
        if (!consent.published()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "资格申请同意内容尚未发布");
        }
        if (!Objects.equals(consent.version(), version)
                || !Objects.equals(consent.textHash(), textHash)) {
            throw new ViException(ViErrorCode.VI_CONFIG_CHANGED, "资格申请同意版本或正文已变化，请重新阅读并明确同意");
        }
        application.setConsentVersion(consent.version());
        application.setConsentText(consent.text());
        application.setConsentAcceptedAt(Instant.now());
    }

    private void requireEditable(QualificationApplication application, Set<String> fields) {
        if (!Set.of("DRAFT", "REJECTED").contains(application.getStatus())) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "当前申请已冻结，不可直接修改材料");
        }
        if ("REJECTED".equals(application.getStatus())) {
            List<String> permitted = rejectFields(application);
            if (permitted == null || permitted.isEmpty()) {
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "历史驳回未记录可补正字段，请联系管理员补齐审核记录");
            }
            Set<String> requested = new HashSet<>(fields);
            requested.removeAll(Set.of("consentVersion", "consentTextHash", "acceptedConsent"));
            if (!permitted.containsAll(requested)) {
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "仅能补正明确驳回的材料项");
            }
        }
    }

    private static void validateMaterials(Map<String, Object> payload) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        for (String field : List.of("firstIssueDate", "validFrom")) {
            if (payload.get(field) != null
                    && LocalDate.parse(payload.get(field).toString()).isAfter(today)) {
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "初领日期及有效起日不能晚于今天");
            }
        }
        if (payload.get("validFrom") != null
                && payload.get("validTo") != null
                && LocalDate.parse(payload.get("validTo").toString())
                        .isBefore(LocalDate.parse(payload.get("validFrom").toString()))) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "驾驶证有效结束日不能早于有效起日");
        }
        if (payload.get("firstIssueDate") != null
                && payload.get("validFrom") != null
                && LocalDate.parse(payload.get("firstIssueDate").toString())
                        .isAfter(LocalDate.parse(payload.get("validFrom").toString()))) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "初领日期不能晚于当前有效起日");
        }
        if (Boolean.TRUE.equals(payload.get("longTermValid")) && payload.get("validTo") != null) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "长期有效不能同时填写有限到期日");
        }
        List<Object> ids = FILE_FIELDS.stream().map(payload::get).filter(Objects::nonNull).toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "四个证件槽位必须使用不同文件");
        }
    }

    private Map<String, Object> payload(Actor actor, QualificationApplication application) {
        if (application.getSensitivePayloadCipher() == null) return new TreeMap<>();
        return new TreeMap<>(
                ViJson.read(
                        crypto.unenvelope(
                                application.getSensitivePayloadCipher(), aad(actor, application)),
                        Map.class));
    }

    private byte[] materialHash(
            Actor actor, QualificationApplication application, Map<String, Object> payload) {
        // 复用带密钥指纹，避免低熵身份证资料被离线枚举；随机nonce不进入材料摘要。
        String fingerprint =
                crypto.seal(
                                ViJson.write(new TreeMap<>(payload)),
                                "Q01:MATERIAL:" + actor.accountId() + ":" + application.getId())
                        .fingerprint;
        return HexFormat.of().parseHex(fingerprint);
    }

    private static String aad(Actor actor, QualificationApplication application) {
        return "Q01:ACCOUNT:"
                + actor.accountId()
                + ":APPLICATION:"
                + application.getId()
                + ":PAYLOAD";
    }

    private static Map<String, Object> evidence(Map<String, Object> payload) {
        Map<String, Object> result = new TreeMap<>();
        for (String field : FILE_FIELDS)
            if (payload.get(field) != null) result.put(field, payload.get(field));
        return result;
    }

    private List<String> rejectFields(QualificationApplication application) {
        if (application.getRejectFields() == null) return null;
        try {
            List<?> parsed = ViJson.read(application.getRejectFields(), List.class);
            if (!parsed.stream()
                    .allMatch(
                            field ->
                                    field instanceof String
                                            && QualificationRequests.MATERIAL_FIELDS.contains(
                                                    field))) return null;
            return parsed.stream().map(Object::toString).distinct().sorted().toList();
        } catch (RuntimeException error) {
            return null;
        }
    }

    private Map<String, Object> view(
            Actor actor, DriverProfile profile, QualificationApplication application) {
        Map<String, Object> payload = payload(actor, application);
        Map<String, Object> draft = new LinkedHashMap<>();
        for (String field : QualificationRequests.MATERIAL_FIELDS) {
            if (!"idCardNo".equals(field)) draft.put(field, payload.get(field));
        }
        draft.put("longTermValid", Boolean.TRUE.equals(payload.get("longTermValid")));
        String number = (String) payload.get("idCardNo");
        draft.put(
                "idCardNoMask",
                number == null
                        ? null
                        : number.substring(0, 3) + "***********" + number.substring(14));
        draft.put("idCardNoProvided", number != null);
        draft.put("consentVersion", application.getConsentVersion());
        draft.put(
                "consentTextHash", QualificationConsentSnapshot.hash(application.getConsentText()));
        draft.put("consentAcceptedAt", external(application.getConsentAcceptedAt()));
        List<String> rejected = rejectFields(application);
        List<String> editable =
                "DRAFT".equals(application.getStatus())
                        ? QualificationRequests.MATERIAL_FIELDS.stream().sorted().toList()
                        : "REJECTED".equals(application.getStatus()) && rejected != null
                                ? rejected
                                : List.of();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationId", application.getId().toString());
        result.put("driverId", profile.getId().toString());
        result.put("profileVersion", profile.getVersion());
        result.put("companyRevision", profile.getTrainingContextRevision());
        Long verificationSessionId = verifications.currentSessionId(profile, application);
        result.put(
                "verificationSessionId",
                verificationSessionId == null ? null : verificationSessionId.toString());
        result.put("status", application.getStatus());
        result.put("effectiveQualificationStatus", profile.getQualificationStatus());
        result.put(
                "ownerDeptId",
                profile.getOwnerDeptId() == null ? null : profile.getOwnerDeptId().toString());
        result.put("companyName", profile.getCompanyName());
        result.put("ownershipUnassigned", profile.getOwnerDeptId() == null);
        result.put("draft", draft);
        result.put("materialRevision", application.getMaterialRevision());
        result.put(
                "materialHash",
                application.getMaterialHash() == null
                        ? null
                        : HexFormat.of().formatHex(application.getMaterialHash()));
        result.put("submissionRevision", application.getSubmissionRevision());
        result.put("editableFields", editable);
        result.put("rejectFields", rejected);
        result.put("publicReviewOpinion", application.getPublicReviewOpinion());
        result.put(
                "rejectReasons",
                application.getPublicReviewOpinion() == null
                        ? List.of()
                        : List.of(application.getPublicReviewOpinion()));
        result.put(
                "items", FILE_FIELDS.stream().filter(field -> payload.get(field) != null).toList());
        result.put("checks", checks(profile, application));
        result.put("submitBlockers", blockers(profile, application, payload));
        List<String> actions = new ArrayList<>();
        if (!editable.isEmpty()) {
            actions.add("EDIT_QUALIFICATION");
            if (blockers(profile, application, payload).isEmpty())
                actions.add("SUBMIT_QUALIFICATION");
        } else if (Set.of("APPROVED", "EXPIRED", "SUSPENDED").contains(application.getStatus())) {
            actions.add("CREATE_QUALIFICATION_REVISION");
        }
        result.put("allowedActions", actions);
        result.put("reviewDueAt", external(profile.getReviewDueAt()));
        // 行政开关、听单意愿和资格均不能替代尚待D01的最终新单准入。
        result.put("canAcceptOrder", false);
        result.put("version", application.getVersion());
        return result;
    }

    private List<Map<String, String>> checks(
            DriverProfile profile, QualificationApplication application) {
        Map<String, String> states =
                new HashMap<>(verifications.currentStatuses(profile, application));
        if (trainingCompletion.currentProof(profile, application) != null)
            states.put("TRAINING", "PASSED");
        List<Map<String, String>> result = new ArrayList<>();
        for (String type : CHECK_TYPES)
            result.add(Map.of("type", type, "status", states.getOrDefault(type, "UNAVAILABLE")));
        return result;
    }

    private List<Map<String, String>> blockers(
            DriverProfile profile,
            QualificationApplication application,
            Map<String, Object> payload) {
        List<Map<String, String>> result = new ArrayList<>();
        if (profile.getOwnerDeptId() == null)
            result.add(blocker("ownerDeptId", "COMPANY_UNASSIGNED", "请联系管理员明确归属公司"));
        else {
            var company = mapper.company(profile.getOwnerDeptId());
            if (company == null
                    || !"0".equals(company.get("status"))
                    || !"0".equals(company.get("del_flag")))
                result.add(blocker("ownerDeptId", "COMPANY_UNAVAILABLE", "归属公司当前不可用，请联系管理员"));
        }
        for (String field :
                List.of(
                        "realName",
                        "idCardNo",
                        "idCardFrontFileId",
                        "idCardBackFileId",
                        "licenseMainFileId",
                        "licenseSubFileId",
                        "licenseClass",
                        "firstIssueDate",
                        "validFrom")) {
            if (payload.get(field) == null) result.add(blocker(field, "REQUIRED", "请补齐本项资料"));
        }
        if (!Boolean.TRUE.equals(payload.get("longTermValid")) && payload.get("validTo") == null)
            result.add(blocker("validTo", "REQUIRED", "请填写到期日或明确长期有效"));
        if (payload.get("validTo") != null
                && LocalDate.parse(payload.get("validTo").toString())
                        .isBefore(LocalDate.now(ZoneId.of("Asia/Shanghai"))))
            result.add(blocker("validTo", "LICENSE_EXPIRED", "证件已到期，请更新材料"));
        if ("REJECTED".equals(application.getStatus())
                && (rejectFields(application) == null || rejectFields(application).isEmpty()))
            result.add(blocker("rejectFields", "REJECT_FIELDS_UNAVAILABLE", "历史驳回缺少可补正项目，请联系管理员"));
        QualificationConsentSnapshot consent = QualificationConsentSnapshot.read(configs, false);
        if (!Objects.equals(application.getConsentVersion(), consent.version())
                || !Objects.equals(application.getConsentText(), consent.text())
                || application.getConsentAcceptedAt() == null)
            result.add(blocker("consentVersion", "CONSENT_CHANGED", "请重新阅读并明确同意当前资格申请说明"));
        for (Map<String, String> check : checks(profile, application)) {
            if (!"PASSED".equals(check.get("status"))) {
                result.add(
                        blocker(
                                check.get("type"),
                                "CHECK_NOT_PASSED",
                                "请先完成" + check.get("type") + "真实核验或培训；未接入能力不能视为通过"));
            }
        }
        return result;
    }

    private static Map<String, String> blocker(String field, String code, String message) {
        return Map.of("field", field, "reasonCode", code, "message", message);
    }

    private void record(Actor actor, QualificationApplication application, String action) {
        Map<String, Object> summary =
                Map.of(
                        "materialRevision",
                        application.getMaterialRevision(),
                        "version",
                        application.getVersion());
        audit.recordSuccess(
                actor, "VI_DRIVER_APPLICATION", application.getId(), action, null, summary, null);
        outbox.publish(
                action + ":" + application.getId() + ":" + application.getVersion(),
                "VI_DRIVER_APPLICATION",
                application.getId(),
                action,
                Map.of(
                        "applicationId",
                        application.getId().toString(),
                        "version",
                        application.getVersion()));
    }

    private <T> T transactional(Actor actor, String action, Supplier<T> work) {
        // 权限检查、幂等、材料写入、成功审计与outbox共用一个事务；拒绝审计在回滚后独立保存。
        return audited(actor, action, () -> tx.execute(status -> work.get()));
    }

    private <T> T audited(Actor actor, String action, Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_DRIVER_APPLICATION",
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

    private static String external(Instant value) {
        return value == null ? null : value.toString();
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }
}
