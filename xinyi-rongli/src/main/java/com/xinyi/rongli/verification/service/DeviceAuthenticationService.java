package com.xinyi.rongli.verification.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.file.service.FileAuthority;
import com.xinyi.rongli.identity.mapper.*;
import com.xinyi.rongli.integration.service.*;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.qualification.domain.QualificationApplication;
import com.xinyi.rongli.qualification.mapper.QualificationMapper;
import com.xinyi.rongli.training.service.TrainingCompletionAuthority;
import com.xinyi.rongli.verification.domain.DeviceAuthenticationSession;
import com.xinyi.rongli.verification.dto.VerificationRequests;
import com.xinyi.rongli.verification.mapper.VerificationMapper;
import com.xinyi.rongli.verification.provider.VerificationProvider;
import com.xinyi.rongli.verification.provider.VerificationProvider.*;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

/** SOTER本机生物认证，不能证明实名/身份证或人脸活体；挑战与本次实际MINI登录会话绑定。 */
@Service
public class DeviceAuthenticationService {
    public static final String AUTH_CONTENT = "验证本机生物特征以继续代驾资格申请";
    private static final ObjectMapper STRICT_JSON =
            new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private final VerificationMapper mapper;
    private final QualificationMapper qualifications;
    private final ViAccountMapper accounts;
    private final FileAuthority authority;
    private final IntegrationAuthority admin;
    private final EmployeeDirectoryGuardMapper directory;
    private final BusinessActorResolver actors;
    private final IntegrationCrypto crypto;
    private final ObjectProvider<VerificationProvider> providers;
    private final CanonicalVerificationAuthority canonical;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;
    private final long leaseSeconds;
    private final SecureRandom random = new SecureRandom();

    public DeviceAuthenticationService(
            VerificationMapper mapper,
            QualificationMapper qualifications,
            ViAccountMapper accounts,
            FileAuthority authority,
            IntegrationAuthority admin,
            EmployeeDirectoryGuardMapper directory,
            BusinessActorResolver actors,
            IntegrationCrypto crypto,
            ObjectProvider<VerificationProvider> providers,
            CanonicalVerificationAuthority canonical,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager,
            @Value("${vi.verification.operation-lease-seconds:30}") long leaseSeconds) {
        this.mapper = mapper;
        this.qualifications = qualifications;
        this.accounts = accounts;
        this.authority = authority;
        this.admin = admin;
        this.directory = directory;
        this.actors = actors;
        this.crypto = crypto;
        this.providers = providers;
        this.canonical = canonical;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        this.leaseSeconds = Math.max(1, Math.min(120, leaseSeconds));
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    private record Context(DriverProfile profile, QualificationApplication application) {}

    private record Lease(
            long id,
            long version,
            String leaseToken,
            Configuration configuration,
            VerifyCommand command) {}

    public record Receipt(long id) {}

    public Map<String, Object> create(VerificationRequests.Create input, String requestNo) {
        Actor actor = actors.requireMini();
        return audited(
                actor,
                0,
                "DEVICE_AUTH_CREATE",
                () -> tx.execute(status -> createChallenge(actor, input, requestNo)));
    }

    private Map<String, Object> createChallenge(
            Actor actor, VerificationRequests.Create input, String requestNo) {
        Context context = current(actor, input.applicationId());
        byte[] mac = idem.mac(ViJson.write(input));
        var prior = mapper.byRequest(requestNo);
        if (prior != null) {
            if (!Objects.equals(prior.getAccountId(), actor.accountId())) invisible();
            if (!TrainingCompletionAuthority.equal(prior.getRequestMac(), mac))
                throw new ViException(ViErrorCode.VI_IDEMPOTENCY_CONFLICT);
            requireContext(prior, context, actor, true);
            return view(prior, true, actor);
        }
        if (context.application().getVersion() != input.expectedApplicationVersion()
                || context.profile().getTrainingContextRevision() != input.expectedCompanyRevision()
                || context.application().getMaterialRevision() != input.expectedMaterialRevision())
            conflict();
        if (!Set.of("DRAFT", "REJECTED").contains(context.application().getStatus()))
            state("当前资格申请已冻结，不可新建设备认证挑战");
        if ("PASSED"
                .equals(
                        canonical
                                .currentStatuses(context.profile(), context.application())
                                .get("DEVICE_AUTH"))) state("本次登录会话已完成当前资料的本机确认，无需重复认证");
        var active =
                mapper.active(input.applicationId(), actor.sessionId(), actor.contextVersion());
        if (active != null) {
            boolean sameMaterial =
                    Objects.equals(active.getOwnerDeptId(), context.profile().getOwnerDeptId())
                            && Objects.equals(
                                    active.getCompanyRevision(),
                                    context.profile().getTrainingContextRevision())
                            && Objects.equals(
                                    active.getMaterialRevision(),
                                    context.application().getMaterialRevision())
                            && TrainingCompletionAuthority.equal(
                                    active.getMaterialHash(),
                                    context.application().getMaterialHash());
            if (!sameMaterial) closeStale(actor, active);
            else if ("CREATED".equals(active.getStatus())
                    && !active.getExpiresAt().isAfter(Instant.now()))
                expireChallenge(actor, active);
            else state("已有本机认证挑战或待确认结果，请读取原会话，不要重复创建");
        }
        var consent = VerificationConsentSnapshot.read(mapper, true);
        if (!consent.published())
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "本机设备认证同意内容尚未发布");
        if (!Objects.equals(consent.version(), input.consentVersion())
                || !Objects.equals(consent.textHash(), input.consentTextHash()))
            throw new ViException(ViErrorCode.VI_CONFIG_CHANGED, "设备认证同意内容已变化，请重新阅读并明确同意");
        Configuration config = provider().configuration(context.profile().getOwnerDeptId());
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (config.ownerDeptId() != context.profile().getOwnerDeptId()
                || !"WECHAT_SOTER".equals(config.provider())
                || !"WX_SOTER_DEVICE_AUTH".equals(config.product())
                || !Objects.equals(config.appId(), account.getAppId())
                || config.identityDigest() == null
                || account.getOpenId() == null)
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "本机认证微信配置与实际登录小程序不一致");
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var row = new DeviceAuthenticationSession();
        row.setApplicationId(input.applicationId());
        row.setAccountId(actor.accountId());
        row.setOwnerDeptId(context.profile().getOwnerDeptId());
        row.setCompanyRevision(context.profile().getTrainingContextRevision());
        row.setMaterialRevision(context.application().getMaterialRevision());
        row.setMaterialHash(context.application().getMaterialHash());
        row.setSessionId(actor.sessionId());
        row.setContextVersion(actor.contextVersion());
        row.setAppId(account.getAppId());
        row.setOpenIdMac(idem.mac(account.getOpenId()));
        row.setRequestNo(requestNo);
        row.setRequestMac(mac);
        row.setCreatedAt(now);
        row.setExpiresAt(now.plusSeconds(300));
        row.setStatus("CREATED");
        row.setLaunchMode("WX_SOTER_DEVICE_AUTH");
        row.setConsentVersion(consent.version());
        row.setConsentTextHash(consent.textHash());
        row.setConsentText(consent.text());
        row.setConsentAcceptedAt(now);
        row.setConfigProfileId(config.profileId());
        row.setConfigRevisionId(config.revisionId());
        row.setProvider(config.provider());
        row.setProduct(config.product());
        row.setProviderIdentityDigest(config.identityDigest());
        row.setConfigSnapshot(ViJson.write(config));
        mapper.insertSession(row);
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
        row.setChallengeCipher(crypto.envelope(challenge, aad(row, "CHALLENGE")));
        row.setChallengeMac(idem.mac(challenge));
        save(row);
        record(actor, row, "DEVICE_AUTH_CHALLENGE_CREATED", Map.of());
        return view(row, true, actor);
    }

    /** GET只读本地状态，不持锁做外网查询，也不把本地页面完成当作可信认证成功。 */
    public Map<String, Object> get(long id) {
        Actor actor = actors.requireMini();
        return audited(
                actor,
                id,
                "DEVICE_AUTH_QUERY",
                () -> tx.execute(status -> view(owned(actor, id, false), true, actor)));
    }

    public Map<String, Object> confirm(long id, VerificationRequests.Confirm input, String key) {
        Actor actor = actors.requireMini();
        return audited(
                actor,
                id,
                "DEVICE_AUTH_CONFIRM",
                () -> {
                    Lease lease = tx.execute(status -> claim(actor, id, input, key));
                    if (lease != null) {
                        VerifyResult result;
                        try {
                            result = provider().verify(lease.configuration(), lease.command());
                        } catch (RuntimeException error) {
                            result =
                                    new VerifyResult(
                                            ResultStatus.UNKNOWN,
                                            null,
                                            "VERIFY_RESULT_UNKNOWN",
                                            null);
                        }
                        VerifyResult fact = result;
                        tx.executeWithoutResult(status -> complete(actor, lease, fact));
                    }
                    return tx.execute(status -> view(owned(actor, id, false), true, actor));
                });
    }

    private Lease claim(Actor actor, long id, VerificationRequests.Confirm input, String key) {
        DeviceAuthenticationSession row = owned(actor, id, true);
        Lease[] claimed = new Lease[1];
        idem.execute(
                IdempotencyService.subjectKey(actor),
                "DEVICE_AUTH_CONFIRM:" + id,
                key,
                ViJson.write(input),
                true,
                Receipt.class,
                () -> {
                    claimed[0] = freezeAndLease(actor, row, input);
                    return new Receipt(row.getId());
                });
        return claimed[0];
    }

    private Lease freezeAndLease(
            Actor actor, DeviceAuthenticationSession row, VerificationRequests.Confirm input) {
        if (row.getSubmissionMac() != null && !input.recover()) {
            byte[] received =
                    idem.mac(
                            ViJson.write(
                                    Map.of(
                                            "resultJSON",
                                            input.resultJSON(),
                                            "resultJSONSignature",
                                            input.resultJSONSignature())));
            if (!TrainingCompletionAuthority.equal(received, row.getSubmissionMac()))
                throw new ViException(
                        ViErrorCode.VI_IDEMPOTENCY_CONFLICT, "本次挑战已冻结原始认证结果，不能更改签名或原文");
            if (Set.of("PASSED", "FAILED").contains(row.getStatus())) return null;
            state("原始认证结果已接收，请重读状态并使用原文恢复确认");
        }
        if (row.getVersion() != input.expectedVersion()) conflict();
        if (row.getLeaseUntil() != null && row.getLeaseUntil().isAfter(Instant.now()))
            state("微信验签仍在处理中，请稍后读取状态");
        if (input.recover()) {
            if (row.getSubmissionCipher() == null
                    || row.getSubmissionMac() == null
                    || !Set.of("UNKNOWN", "PROCESSING").contains(row.getStatus()))
                state("没有可恢复的原始已接收认证结果");
        } else {
            if (!"CREATED".equals(row.getStatus()) || !row.getExpiresAt().isAfter(Instant.now()))
                state("本机认证挑战已过期或结束，请重新建立挑战");
            validateChallenge(row, input.resultJSON());
            row.setSubmissionMac(
                    idem.mac(
                            ViJson.write(
                                    Map.of(
                                            "resultJSON",
                                            input.resultJSON(),
                                            "resultJSONSignature",
                                            input.resultJSONSignature()))));
            row.setSubmissionCipher(
                    crypto.envelope(
                            ViJson.write(
                                    Map.of(
                                            "resultJSON",
                                            input.resultJSON(),
                                            "resultJSONSignature",
                                            input.resultJSONSignature())),
                            aad(row, "SUBMISSION")));
            row.setSubmittedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        }
        Map<?, ?> original =
                ViJson.read(
                        crypto.unenvelope(row.getSubmissionCipher(), aad(row, "SUBMISSION")),
                        Map.class);
        String raw = original.get("resultJSON").toString(),
                signature = original.get("resultJSONSignature").toString();
        validateChallenge(row, raw);
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (!Objects.equals(account.getAppId(), row.getAppId())
                || !TrainingCompletionAuthority.equal(
                        idem.mac(account.getOpenId()), row.getOpenIdMac()))
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
        row.setStatus("PROCESSING");
        row.setFailureCode(null);
        row.setExecutionStartedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        row.setLeaseToken(UUID.randomUUID().toString());
        row.setLeaseUntil(Instant.now().plusSeconds(leaseSeconds));
        save(row);
        record(
                actor,
                row,
                input.recover() ? "DEVICE_AUTH_RECOVERY_INTENT" : "DEVICE_AUTH_VERIFY_INTENT",
                Map.of());
        return new Lease(
                row.getId(),
                row.getVersion(),
                row.getLeaseToken(),
                ViJson.read(row.getConfigSnapshot(), Configuration.class),
                new VerifyCommand(account.getOpenId(), raw, signature));
    }

    private void validateChallenge(DeviceAuthenticationSession row, String raw) {
        try (JsonParser parser = STRICT_JSON.getFactory().createParser(raw)) {
            JsonNode data = STRICT_JSON.readTree(parser);
            if (data == null
                    || !data.isObject()
                    || !data.path("raw").isTextual()
                    || parser.nextToken() != null)
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "认证原文缺少实际challenge或JSON结构无效");
            String expected = crypto.unenvelope(row.getChallengeCipher(), aad(row, "CHALLENGE"));
            if (!MessageDigest.isEqual(
                            expected.getBytes(StandardCharsets.UTF_8),
                            data.path("raw").asText().getBytes(StandardCharsets.UTF_8))
                    || !TrainingCompletionAuthority.equal(
                            idem.mac(expected), row.getChallengeMac()))
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "认证结果不对应本次设备挑战");
            // 仅核官方定义的raw；不臆造is_root、CPU标识或设备身份字段作为通过条件。
        } catch (ViException error) {
            throw error;
        } catch (Exception error) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "认证原文JSON无效或存在重复字段");
        }
    }

    private void complete(Actor actor, Lease lease, VerifyResult result) {
        directory.ensureGuard();
        directory.lockGuard();
        var snapshot = mapper.find(lease.id());
        if (snapshot == null) return;
        // 先重新鉴权并锁真实档案/申请，再锁设备认证记录。IO期间换材料、撤会话或切视角都不能先发布旧证明。
        Context current = current(actor, snapshot.getApplicationId());
        var row = mapper.lock(lease.id());
        if (row == null) return;
        requireContext(row, current, actor, true);
        if (row == null
                || row.getVersion() != lease.version()
                || !Objects.equals(row.getLeaseToken(), lease.leaseToken())
                || !"PROCESSING".equals(row.getStatus())) return;
        row.setLeaseToken(null);
        row.setLeaseUntil(null);
        if (result == null || result.status() == ResultStatus.UNKNOWN) {
            row.setStatus("UNKNOWN");
            row.setFailureCode("VERIFY_RESULT_UNKNOWN");
        } else {
            Instant verified = result.verifiedAt();
            if (verified == null
                    || verified.isAfter(Instant.now())
                    || row.getSubmittedAt() == null
                    || verified.isBefore(row.getSubmittedAt())) {
                row.setStatus("UNKNOWN");
                row.setFailureCode("UNTRUSTED_VERIFICATION_TIME");
            } else {
                row.setStatus(result.status() == ResultStatus.PASSED ? "PASSED" : "FAILED");
                row.setResultAvailable(true);
                row.setVerifiedAt(verified.truncatedTo(ChronoUnit.MILLIS));
                row.setResultDigest(digest(result.resultDigest()));
                row.setFailureCode(
                        "FAILED".equals(row.getStatus())
                                ? safe(result.failureCode(), "DEVICE_SIGNATURE_REJECTED")
                                : null);
                canonical.publishDeviceProof(actor, row);
                row.setSubmissionCipher(null);
                row.setChallengeCipher(null);
            }
        }
        save(row);
        record(
                actor,
                row,
                "DEVICE_AUTH_VERIFY_RESULT",
                Map.of("resultAvailable", row.getResultAvailable()));
    }

    public Map<String, Object> adminList(VerificationRequests.Query query) {
        Actor actor = admin.actor();
        return audited(
                actor,
                0,
                "DEVICE_AUTH_ADMIN_LIST",
                () ->
                        tx.execute(
                                status -> {
                                    admin.lockAuthority();
                                    var scope = admin.scope("biz:verification:list");
                                    return Map.of(
                                            "code",
                                            200,
                                            "msg",
                                            "操作成功",
                                            "rows",
                                            mapper
                                                    .list(
                                                            scope,
                                                            query,
                                                            (long) (query.pageNum() - 1)
                                                                    * query.pageSize())
                                                    .stream()
                                                    .map(row -> view(row, false, actor))
                                                    .toList(),
                                            "total",
                                            mapper.count(scope, query));
                                }));
    }

    public Map<String, Object> adminGet(long id) {
        Actor actor = admin.actor();
        return audited(
                actor,
                id,
                "DEVICE_AUTH_ADMIN_QUERY",
                () ->
                        tx.execute(
                                status ->
                                        view(
                                                adminRecord(id, "biz:verification:query"),
                                                false,
                                                actor)));
    }

    public Map<String, Object> close(long id, VerificationRequests.Close input, String key) {
        Actor actor = admin.actor();
        return audited(
                actor,
                id,
                "DEVICE_AUTH_CLOSE",
                () ->
                        tx.execute(
                                status -> {
                                    var row = adminRecord(id, "biz:verification:close");
                                    idem.execute(
                                            IdempotencyService.subjectKey(actor),
                                            "DEVICE_AUTH_CLOSE:" + id,
                                            key,
                                            ViJson.write(input),
                                            true,
                                            Receipt.class,
                                            () -> {
                                                if (row.getVersion() != input.expectedVersion())
                                                    conflict();
                                                if (row.getLeaseUntil() != null
                                                        && row.getLeaseUntil()
                                                                .isAfter(Instant.now()))
                                                    state("验签请求仍在处理中");
                                                if (!Set.of(
                                                                "CREATED",
                                                                "UNKNOWN",
                                                                "PROCESSING",
                                                                "EXPIRED")
                                                        .contains(row.getStatus()))
                                                    state("设备认证已结束，不能改写终态");
                                                row.setStatus("CLOSED");
                                                row.setChallengeCipher(null);
                                                row.setSubmissionCipher(null);
                                                row.setLeaseToken(null);
                                                row.setLeaseUntil(null);
                                                save(row);
                                                String reason =
                                                        Base64.getEncoder()
                                                                .encodeToString(
                                                                        crypto.envelope(
                                                                                input.reason(),
                                                                                "Q04:ADMIN:"
                                                                                        + actor
                                                                                                .sysUserId()
                                                                                        + ":DEVICE_CLOSE:"
                                                                                        + id));
                                                record(
                                                        actor,
                                                        row,
                                                        "DEVICE_AUTH_CLOSED",
                                                        Map.of("reasonEnvelope", reason));
                                                return new Receipt(id);
                                            });
                                    return view(mapper.find(id), false, actor);
                                }));
    }

    private DeviceAuthenticationSession adminRecord(long id, String permission) {
        admin.lockAuthority();
        var scope = admin.scope(permission);
        var row = mapper.lock(id);
        if (row == null
                || row.getRequestNo() == null
                || !"WECHAT_SOTER".equals(row.getProvider())
                || !"WX_SOTER_DEVICE_AUTH".equals(row.getProduct())
                || scope != null
                        && (row.getOwnerDeptId() == null || !scope.contains(row.getOwnerDeptId())))
            invisible();
        return row;
    }

    private Context current(Actor actor, long application) {
        authority.guard(actor, "biz:driverqualification:query");
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (account.getPhoneVerifiedAt() == null || account.getPhoneCipher() == null)
            throw new ViException(ViErrorCode.VI_PHONE_PROOF_REQUIRED);
        var profile = qualifications.lockProfile(actor.accountId());
        var app = qualifications.lock(application);
        if (profile == null || app == null || !Objects.equals(app.getDriverId(), profile.getId()))
            invisible();
        var latest = qualifications.current(profile.getId());
        if (latest == null || !Objects.equals(latest.getId(), app.getId())) conflict();
        if (profile.getOwnerDeptId() == null || app.getMaterialHash() == null)
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "请先明确归属公司并保存申请草稿");
        var company = qualifications.company(profile.getOwnerDeptId());
        if (company == null
                || !"0".equals(company.get("status"))
                || !"0".equals(company.get("del_flag"))) state("归属公司不可用");
        return new Context(profile, app);
    }

    private DeviceAuthenticationSession owned(Actor actor, long id, boolean confirm) {
        authority.guard(actor, "biz:driverqualification:query");
        var row = mapper.find(id);
        if (row == null || !Objects.equals(row.getAccountId(), actor.accountId())) invisible();
        Context context = current(actor, row.getApplicationId());
        row = mapper.lock(id);
        requireContext(row, context, actor, confirm);
        return row;
    }

    private void requireContext(
            DeviceAuthenticationSession row, Context context, Actor actor, boolean strictContext) {
        if (!Objects.equals(row.getOwnerDeptId(), context.profile().getOwnerDeptId())
                || !Objects.equals(
                        row.getCompanyRevision(), context.profile().getTrainingContextRevision())
                || !Objects.equals(
                        row.getMaterialRevision(), context.application().getMaterialRevision())
                || !TrainingCompletionAuthority.equal(
                        row.getMaterialHash(), context.application().getMaterialHash())
                || !Objects.equals(row.getSessionId(), actor.sessionId())) conflict();
        if ((strictContext
                        || !Set.of("PASSED", "FAILED", "CLOSED", "EXPIRED")
                                .contains(row.getStatus()))
                && !Objects.equals(row.getContextVersion(), actor.contextVersion()))
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (!Objects.equals(row.getAppId(), account.getAppId())
                || account.getOpenId() == null
                || !TrainingCompletionAuthority.equal(
                        row.getOpenIdMac(), idem.mac(account.getOpenId())))
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
    }

    private Map<String, Object> view(DeviceAuthenticationSession row, boolean mini, Actor actor) {
        String status = effectiveStatus(row);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("verificationSessionId", row.getId().toString());
        value.put("applicationId", row.getApplicationId().toString());
        value.put("requestNo", row.getRequestNo());
        value.put("status", status);
        value.put("launchMode", "WX_SOTER_DEVICE_AUTH");
        value.put("launchUrl", null);
        value.put("authContent", AUTH_CONTENT);
        boolean live =
                mini
                        && "CREATED".equals(status)
                        && row.getChallengeCipher() != null
                        && Objects.equals(row.getContextVersion(), actor.contextVersion());
        value.put(
                "challenge",
                live ? crypto.unenvelope(row.getChallengeCipher(), aad(row, "CHALLENGE")) : null);
        value.put("expiresAt", row.getExpiresAt().toString());
        value.put("resultAvailable", row.getResultAvailable());
        value.put("failureCode", row.getFailureCode());
        value.put("version", row.getVersion());
        value.put("materialRevision", row.getMaterialRevision());
        value.put("companyRevision", row.getCompanyRevision());
        List<String> actions = new ArrayList<>();
        if (mini) {
            actions.add("QUERY_DEVICE_AUTH");
            if (live) {
                actions.add("START_DEVICE_AUTH");
                actions.add("CONFIRM_DEVICE_AUTH");
            }
            if ("UNKNOWN".equals(status)
                    && row.getSubmissionCipher() != null
                    && Objects.equals(row.getContextVersion(), actor.contextVersion()))
                actions.add("RECOVER_DEVICE_AUTH");
        } else {
            value.put(
                    "ownerDeptId",
                    row.getOwnerDeptId() == null ? null : row.getOwnerDeptId().toString());
            var company =
                    row.getOwnerDeptId() == null
                            ? null
                            : qualifications.company(row.getOwnerDeptId());
            value.put("companyName", company == null ? null : company.get("dept_name"));
            value.put("provider", row.getProvider());
            value.put("createdAt", external(row.getCreatedAt()));
            value.put("verifiedAt", external(row.getVerifiedAt()));
            if (canAdmin(row, "biz:verification:query")) actions.add("VIEW_DEVICE_AUTH");
            if (canAdmin(row, "biz:verification:close")
                    && Set.of("CREATED", "UNKNOWN", "EXPIRED").contains(status)
                    && (row.getLeaseUntil() == null || !row.getLeaseUntil().isAfter(Instant.now())))
                actions.add("CLOSE_DEVICE_AUTH");
        }
        value.put("allowedActions", actions);
        return value;
    }

    private boolean canAdmin(DeviceAuthenticationSession row, String permission) {
        if (!com.xinyi.common.utils.SecurityUtils.hasPermi(permission)) return false;
        var scope = admin.scope(permission);
        return scope == null
                || row.getOwnerDeptId() != null && scope.contains(row.getOwnerDeptId());
    }

    private static String effectiveStatus(DeviceAuthenticationSession row) {
        if ("CREATED".equals(row.getStatus()) && !row.getExpiresAt().isAfter(Instant.now()))
            return "EXPIRED";
        if ("PROCESSING".equals(row.getStatus())
                && (row.getLeaseUntil() == null || !row.getLeaseUntil().isAfter(Instant.now())))
            return "UNKNOWN";
        return row.getStatus();
    }

    private void expireChallenge(Actor actor, DeviceAuthenticationSession row) {
        row.setStatus("EXPIRED");
        row.setChallengeCipher(null);
        save(row);
        record(actor, row, "DEVICE_AUTH_CHALLENGE_EXPIRED", Map.of());
    }

    private void closeStale(Actor actor, DeviceAuthenticationSession row) {
        row.setStatus("CLOSED");
        row.setChallengeCipher(null);
        row.setSubmissionCipher(null);
        row.setLeaseToken(null);
        row.setLeaseUntil(null);
        save(row);
        record(actor, row, "DEVICE_AUTH_CONTEXT_CLOSED", Map.of());
    }

    private void save(DeviceAuthenticationSession row) {
        if (mapper.saveSession(row) != 1) conflict();
        row.setVersion(row.getVersion() + 1);
    }

    private VerificationProvider provider() {
        var provider = providers.getIfAvailable();
        if (provider == null) throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED);
        return provider;
    }

    private static String aad(DeviceAuthenticationSession row, String purpose) {
        return "Q04:DEVICE:"
                + row.getId()
                + ":APPLICATION:"
                + row.getApplicationId()
                + ":SESSION:"
                + row.getSessionId()
                + ":COMPANY:"
                + row.getCompanyRevision()
                + ":MATERIAL:"
                + row.getMaterialRevision()
                + ":"
                + purpose;
    }

    private void record(
            Actor actor,
            DeviceAuthenticationSession row,
            String action,
            Map<String, Object> extra) {
        Map<String, Object> summary = new LinkedHashMap<>(extra);
        summary.put("status", row.getStatus());
        summary.put("version", row.getVersion());
        audit.recordSuccess(
                actor, "VI_DEVICE_AUTH_SESSION", row.getId(), action, null, summary, null);
        outbox.publish(
                action + ":" + row.getId() + ":" + row.getVersion(),
                "VI_DEVICE_AUTH_SESSION",
                row.getId(),
                action,
                Map.of(
                        "verificationSessionId",
                        row.getId().toString(),
                        "applicationId",
                        row.getApplicationId().toString(),
                        "version",
                        row.getVersion()));
    }

    private <T> T audited(Actor actor, long id, String action, Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_DEVICE_AUTH_SESSION",
                    id,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException failure
                            ? failure.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }

    private static String safe(String value, String fallback) {
        return value != null && value.matches("[A-Z0-9_]{1,64}") ? value : fallback;
    }

    private static String digest(String value) {
        return value != null && value.matches("[A-Fa-f0-9]{64}")
                ? value.toLowerCase(Locale.ROOT)
                : null;
    }

    private static String external(Instant value) {
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
