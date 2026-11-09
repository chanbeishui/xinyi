package com.xinyi.rongli.verification.service;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.qualification.domain.QualificationApplication;
import com.xinyi.rongli.qualification.mapper.QualificationMapper;
import com.xinyi.rongli.training.service.TrainingCompletionAuthority;
import com.xinyi.rongli.verification.domain.*;
import com.xinyi.rongli.verification.mapper.VerificationMapper;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;

/** 设备认证及两项人工核验共用权威证明；加密认证封装、真实父记录、当前公司epoch和材料必须同时匹配。 */
@Service
public class CanonicalVerificationAuthority {
    private final VerificationMapper mapper;
    private final QualificationMapper qualifications;
    private final IntegrationCrypto crypto;
    private final ObjectProvider<ManualVerificationEvidence> manual;
    private final BusinessActorResolver actors;
    private final AuditService audit;
    private final com.xinyi.rongli.file.service.FileAuthority fileAuthority;
    private final com.xinyi.rongli.identity.mapper.ViAccountMapper accounts;
    private final IdempotencyService idem;
    private final OutboxService outbox;

    public CanonicalVerificationAuthority(
            VerificationMapper mapper,
            QualificationMapper qualifications,
            IntegrationCrypto crypto,
            ObjectProvider<ManualVerificationEvidence> manual,
            BusinessActorResolver actors,
            AuditService audit,
            OutboxService outbox,
            com.xinyi.rongli.file.service.FileAuthority fileAuthority,
            com.xinyi.rongli.identity.mapper.ViAccountMapper accounts,
            IdempotencyService idem) {
        this.mapper = mapper;
        this.qualifications = qualifications;
        this.crypto = crypto;
        this.manual = manual;
        this.actors = actors;
        this.audit = audit;
        this.outbox = outbox;
        this.fileAuthority = fileAuthority;
        this.accounts = accounts;
        this.idem = idem;
    }

    public Map<String, VerificationProof> currentProofs(
            DriverProfile profile, QualificationApplication app) {
        Map<String, VerificationProof> result = new LinkedHashMap<>();
        if (profile.getOwnerDeptId() == null || app.getMaterialHash() == null) return result;
        for (var proof :
                mapper.currentProofs(
                        app.getId(),
                        profile.getOwnerDeptId(),
                        profile.getTrainingContextRevision(),
                        app.getMaterialRevision(),
                        app.getMaterialHash())) {
            if ("DEVICE_AUTH".equals(proof.getTaskType())
                    && !Objects.equals(
                            proof.getSourceSessionId(), actors.requireMini().sessionId())) continue;
            if (!result.containsKey(proof.getTaskType())
                    && authenticated(proof)
                    && sourceMatches(proof)) result.put(proof.getTaskType(), proof);
        }
        return result;
    }

    public Map<String, String> currentStatuses(
            DriverProfile profile, QualificationApplication app) {
        Map<String, String> statuses = new HashMap<>();
        currentProofs(profile, app).forEach((type, proof) -> statuses.put(type, proof.getStatus()));
        var session = currentSession(profile, app);
        if (session != null && !Set.of("PASSED", "CLOSED").contains(session.getStatus())) {
            String status = session.getStatus();
            if ("CREATED".equals(status)
                    && session.getExpiresAt() != null
                    && !session.getExpiresAt().isAfter(Instant.now())) status = "EXPIRED";
            if ("PROCESSING".equals(status)
                    && (session.getLeaseUntil() == null
                            || !session.getLeaseUntil().isAfter(Instant.now()))) status = "UNKNOWN";
            statuses.put("DEVICE_AUTH", status);
        }
        return statuses;
    }

    public Long currentSessionId(DriverProfile profile, QualificationApplication app) {
        var row = currentSession(profile, app);
        return row == null ? null : row.getId();
    }

    private DeviceAuthenticationSession currentSession(
            DriverProfile profile, QualificationApplication app) {
        if (profile.getOwnerDeptId() == null || app.getMaterialHash() == null) return null;
        return mapper.current(
                app.getId(),
                profile.getOwnerDeptId(),
                profile.getTrainingContextRevision(),
                app.getMaterialRevision(),
                app.getMaterialHash(),
                actors.requireMini().sessionId(),
                actors.requireMini().contextVersion());
    }

    private boolean authenticated(VerificationProof proof) {
        if (proof.getCanonicalResultCipher() == null
                || proof.getVerifiedAt() == null
                || proof.getVerifiedAt().isAfter(Instant.now())
                || !Set.of("PASSED", "FAILED").contains(proof.getStatus())) return false;
        try {
            Map<?, ?> decoded =
                    ViJson.read(
                            crypto.unenvelope(proof.getCanonicalResultCipher(), aad(proof)),
                            Map.class);
            return ViJson.write(decoded).equals(ViJson.write(facts(proof)));
        } catch (ViException error) {
            // 根密钥缺失属于权威不可读，不能悄悄用历史provider名称兜底。
            if (error.getErrorCode() == ViErrorCode.VI_SECRET_UNAVAILABLE) throw error;
            return false;
        } catch (RuntimeException error) {
            return false;
        }
    }

    private boolean sourceMatches(VerificationProof proof) {
        if ("DEVICE_SESSION".equals(proof.getSourceKind())) {
            var session = mapper.find(proof.getSourceRecordId());
            if (session == null
                    || !"DEVICE_AUTH".equals(proof.getTaskType())
                    || !Objects.equals(session.getApplicationId(), proof.getApplicationId())
                    || !Objects.equals(session.getOwnerDeptId(), proof.getOwnerDeptId())
                    || !Objects.equals(session.getCompanyRevision(), proof.getCompanyRevision())
                    || !Objects.equals(session.getMaterialRevision(), proof.getMaterialRevision())
                    || !TrainingCompletionAuthority.equal(
                            session.getMaterialHash(), proof.getMaterialHash())
                    || !Objects.equals(session.getStatus(), proof.getStatus())
                    || !session.getResultAvailable()
                    || !Objects.equals(session.getProvider(), "WECHAT_SOTER")
                    || !Objects.equals(session.getSessionId(), proof.getSourceSessionId())
                    || session.getVerifiedAt() == null
                    || !session.getVerifiedAt().equals(proof.getVerifiedAt())) return false;
            // 已完成本机确认允许同一登录会话切身份，不能跨新登录/设备会话复用。
            return Objects.equals(session.getVerificationId(), proof.getId())
                    && Objects.equals(session.getSessionId(), actors.requireMini().sessionId());
        }
        if (!"MANUAL_REVIEW".equals(proof.getSourceKind())
                || !Set.of("BACKGROUND", "LICENSE").contains(proof.getTaskType())) return false;
        var bridge = manual.getIfAvailable();
        if (bridge == null) return false;
        var evidence = bridge.approvedEvidence(proof.getSourceRecordId());
        return evidence != null
                && Objects.equals(evidence.proofId(), proof.getId())
                && matches(proof, evidence);
    }

    private boolean matches(VerificationProof proof, ManualVerificationEvidence.Evidence evidence) {
        return evidence.reviewId() == proof.getSourceRecordId()
                && evidence.applicationId() == proof.getApplicationId()
                && evidence.ownerDeptId() == proof.getOwnerDeptId()
                && evidence.companyRevision() == proof.getCompanyRevision()
                && evidence.materialRevision() == proof.getMaterialRevision()
                && TrainingCompletionAuthority.equal(
                        evidence.materialHash(), proof.getMaterialHash())
                && evidence.submittedByUserId() != evidence.reviewedByUserId()
                && evidence.type().equals(proof.getTaskType())
                && evidence.decision().equals(proof.getStatus())
                && evidence.reviewedAt() != null
                && evidence.reviewedAt().equals(proof.getVerifiedAt());
    }

    public long publishManualProof(Actor actor, long reviewId) {
        requireTransaction();
        var current = actors.requireAdmin();
        if (!actor.isAdmin() || !Objects.equals(actor.sysUserId(), current.sysUserId()))
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        var bridge = manual.getIfAvailable();
        if (bridge == null) throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED);
        var evidence = bridge.approvedEvidence(reviewId);
        if (evidence == null
                || evidence.reviewedByUserId() != actor.sysUserId()
                || evidence.submittedByUserId() == evidence.reviewedByUserId()
                || !Set.of("BACKGROUND", "LICENSE").contains(evidence.type())
                || !Set.of("PASSED", "FAILED").contains(evidence.decision())
                || evidence.reviewedAt() == null
                || evidence.reviewedAt().isAfter(Instant.now())) state("缺少真实双人复核终态");
        bridge.requireReviewAuthority(actor, evidence.ownerDeptId());
        var app = qualifications.lock(evidence.applicationId());
        if (app == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        // 调用者已经持目录→档案→申请锁；这里重新核对原始材料，不能异步补签旧公司证明。
        var profile = qualifications.lockProfile(findAccount(app));
        var latestApplication = profile == null ? null : qualifications.current(profile.getId());
        if (profile == null
                || latestApplication == null
                || !Objects.equals(latestApplication.getId(), app.getId())
                || !Set.of("DRAFT", "REJECTED").contains(app.getStatus())
                || !Objects.equals(profile.getId(), app.getDriverId())
                || !Objects.equals(profile.getOwnerDeptId(), evidence.ownerDeptId())
                || profile.getTrainingContextRevision() != evidence.companyRevision()
                || app.getMaterialRevision() != evidence.materialRevision()
                || !TrainingCompletionAuthority.equal(
                        app.getMaterialHash(), evidence.materialHash())) state("复核材料或归属已变化");
        var existing = mapper.sourceProof("MANUAL_REVIEW", reviewId, evidence.type());
        if (existing != null) {
            if (!authenticated(existing) || !matches(existing, evidence)) state("复核证明与原始记录不一致");
            return existing.getId();
        }
        var proof = new VerificationProof();
        proof.setApplicationId(evidence.applicationId());
        proof.setOwnerDeptId(evidence.ownerDeptId());
        proof.setCompanyRevision(evidence.companyRevision());
        proof.setMaterialRevision(evidence.materialRevision());
        proof.setMaterialHash(evidence.materialHash());
        proof.setTaskType(evidence.type());
        proof.setProvider("MANUAL_TWO_PERSON");
        proof.setProviderTaskId("MANUAL:" + reviewId + ":" + evidence.type());
        proof.setSourceKind("MANUAL_REVIEW");
        proof.setSourceRecordId(reviewId);
        proof.setStatus(evidence.decision());
        proof.setVerifiedAt(evidence.reviewedAt());
        return publish(actor, proof);
    }

    private long findAccount(QualificationApplication app) {
        // 小型内部只读映射，不能按当前管理员或客户端账号猜申请人。
        Long account = mapper.applicationAccount(app.getId());
        if (account == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return account;
    }

    public void publishDeviceProof(Actor actor, DeviceAuthenticationSession session) {
        requireTransaction();
        Actor trusted = actors.requireMini();
        if (!actor.isMini()
                || !Objects.equals(trusted.accountId(), actor.accountId())
                || !Objects.equals(trusted.sessionId(), actor.sessionId()))
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        fileAuthority.guard(actor, "biz:driverqualification:query");
        var profile = qualifications.lockProfile(actor.accountId());
        var application = qualifications.lock(session.getApplicationId());
        var latest = profile == null ? null : qualifications.current(profile.getId());
        var account = accounts.selectByIdForUpdate(actor.accountId());
        if (profile == null
                || application == null
                || latest == null
                || !Objects.equals(latest.getId(), application.getId())
                || !Objects.equals(application.getDriverId(), profile.getId())
                || !Objects.equals(profile.getOwnerDeptId(), session.getOwnerDeptId())
                || profile.getTrainingContextRevision() != session.getCompanyRevision()
                || application.getMaterialRevision() != session.getMaterialRevision()
                || !TrainingCompletionAuthority.equal(
                        application.getMaterialHash(), session.getMaterialHash())
                || !Objects.equals(actor.sessionId(), session.getSessionId())
                || !Objects.equals(actor.contextVersion(), session.getContextVersion())
                || account.getPhoneVerifiedAt() == null
                || account.getPhoneCipher() == null
                || !Set.of("DRAFT", "REJECTED").contains(application.getStatus())
                || !Objects.equals(account.getAppId(), session.getAppId())
                || account.getOpenId() == null
                || !TrainingCompletionAuthority.equal(
                        idem.mac(account.getOpenId()), session.getOpenIdMac()))
            throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
        if (!Set.of("PASSED", "FAILED").contains(session.getStatus())
                || session.getVerifiedAt() == null
                || !"WECHAT_SOTER".equals(session.getProvider())
                || session.getSubmissionMac() == null
                || session.getSessionId() == null
                || !session.getResultAvailable()) state("没有正式可信设备验签结果");
        var proof = mapper.sourceProof("DEVICE_SESSION", session.getId(), "DEVICE_AUTH");
        if (proof == null) {
            proof = new VerificationProof();
            proof.setApplicationId(session.getApplicationId());
            proof.setOwnerDeptId(session.getOwnerDeptId());
            proof.setCompanyRevision(session.getCompanyRevision());
            proof.setMaterialRevision(session.getMaterialRevision());
            proof.setMaterialHash(session.getMaterialHash());
            proof.setTaskType("DEVICE_AUTH");
            proof.setProvider("WECHAT_SOTER");
            proof.setProviderTaskId("SOTER:" + session.getId());
            proof.setStatus(session.getStatus());
            proof.setVerifiedAt(session.getVerifiedAt());
            proof.setResultDigest(session.getResultDigest());
            proof.setSourceKind("DEVICE_SESSION");
            proof.setSourceRecordId(session.getId());
            proof.setSourceSessionId(session.getSessionId());
            publish(actor, proof);
        } else if (!authenticated(proof)
                || !Objects.equals(proof.getStatus(), session.getStatus())
                || !Objects.equals(proof.getSourceSessionId(), session.getSessionId()))
            state("既有设备认证证明不可认证");
        session.setVerificationId(proof.getId());
    }

    private long publish(Actor actor, VerificationProof proof) {
        mapper.insertProof(proof);
        byte[] cipher = crypto.envelope(ViJson.write(facts(proof)), aad(proof));
        if (mapper.sealProof(proof.getId(), cipher) != 1) state("证明封装失败");
        audit.recordSuccess(
                actor,
                "VI_PROVIDER_VERIFICATION",
                proof.getId(),
                "VERIFICATION_PROOF_PUBLISHED",
                null,
                Map.of(
                        "type",
                        proof.getTaskType(),
                        "status",
                        proof.getStatus(),
                        "sourceId",
                        proof.getSourceRecordId().toString()),
                null);
        outbox.publish(
                "VERIFICATION_PROOF_PUBLISHED:" + proof.getId(),
                "VI_PROVIDER_VERIFICATION",
                proof.getId(),
                "VERIFICATION_PROOF_PUBLISHED",
                Map.of(
                        "proofId",
                        proof.getId().toString(),
                        "applicationId",
                        proof.getApplicationId().toString(),
                        "type",
                        proof.getTaskType()));
        return proof.getId();
    }

    private static Map<String, Object> facts(VerificationProof proof) {
        Map<String, Object> value = new TreeMap<>();
        value.put("proofId", proof.getId().toString());
        value.put("applicationId", proof.getApplicationId().toString());
        value.put("ownerDeptId", proof.getOwnerDeptId().toString());
        value.put("companyRevision", proof.getCompanyRevision());
        value.put("materialRevision", proof.getMaterialRevision());
        value.put("materialHash", HexFormat.of().formatHex(proof.getMaterialHash()));
        value.put("type", proof.getTaskType());
        value.put("status", proof.getStatus());
        value.put("provider", proof.getProvider());
        value.put("providerTaskId", proof.getProviderTaskId());
        value.put("sourceKind", proof.getSourceKind());
        value.put("sourceRecordId", proof.getSourceRecordId().toString());
        value.put("verifiedAt", proof.getVerifiedAt().toString());
        value.put("resultDigest", proof.getResultDigest());
        value.put(
                "sourceSessionId",
                proof.getSourceSessionId() == null ? null : proof.getSourceSessionId().toString());
        return value;
    }

    private static String aad(VerificationProof proof) {
        return "Q04:PROOF:"
                + proof.getId()
                + ":APPLICATION:"
                + proof.getApplicationId()
                + ":TYPE:"
                + proof.getTaskType()
                + ":SOURCE:"
                + proof.getSourceKind()
                + ":"
                + proof.getSourceRecordId();
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("核验证明必须随原始复核事务发布");
    }

    private static void state(String message) {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, message);
    }
}
