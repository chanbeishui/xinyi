package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.qualification.domain.QualificationSubmission;
import com.xinyi.rongli.qualification.mapper.QualificationAdminMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/** 独立认证不可变事实；不承担访问授权，不依赖当前MINI或原登录仍在线，调用方必须先验证真实父记录权限。 */
@Service
public class QualificationSubmissionAuthority {
    private final QualificationAdminMapper mapper;
    private final IntegrationCrypto crypto;

    public QualificationSubmissionAuthority(
            QualificationAdminMapper mapper, IntegrationCrypto crypto) {
        this.mapper = mapper;
        this.crypto = crypto;
    }

    public QualificationSubmission authenticated(long applicationId, long submissionRevision) {
        QualificationSubmission row = mapper.submission(applicationId, submissionRevision);
        return authenticated(row) ? row : null;
    }

    public boolean authenticated(QualificationSubmission row) {
        return authenticated(row, crypto);
    }

    static boolean authenticated(QualificationSubmission row, IntegrationCrypto crypto) {
        if (row == null
                || row.getSubmissionSeal() == null
                || row.getCompanyRevision() == null
                || row.getSourceSessionId() == null
                || row.getApplicantUserIds() == null
                || row.getSubmittedAt() == null
                || row.getMaterialHash() == null
                || row.getSensitivePayloadCipher() == null) return false;
        try {
            if (row.getId() == null
                    || row.getApplicationId() == null
                    || row.getOwnerDeptId() == null
                    || row.getSubmissionRevision() <= 0
                    || row.getCompanyRevision() < 0
                    || row.getSourceSessionId() <= 0
                    || row.getMaterialHash().length != 32
                    || row.getSubmittedAt().isAfter(java.time.Instant.now())
                    || row.getConsentVersion() == null
                    || row.getConsentText() == null
                    || row.getConsentAcceptedAt() == null
                    || row.getSubmitReasonCipher() == null) return false;
            List<?> users = ViJson.read(row.getApplicantUserIds(), List.class);
            for (Object user : users) com.xinyi.rongli.core.ViId.parse(user.toString());
            Map<?, ?> value =
                    ViJson.read(crypto.unenvelope(row.getSubmissionSeal(), aad(row)), Map.class);
            return ViJson.write(value).equals(ViJson.write(facts(row)));
        } catch (ViException failure) {
            // 缺部署密钥属于权威不可读，不能被当作普通旧数据并静默降级。
            if (failure.getErrorCode() == ViErrorCode.VI_SECRET_UNAVAILABLE) throw failure;
            return false;
        } catch (RuntimeException failure) {
            return false;
        }
    }

    public QualificationSubmission insertAndSeal(QualificationSubmission row) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("资格快照必须随业务事务封存");
        mapper.insertSubmission(row);
        // 先取得数据库主键和毫秒UTC时刻，再一次认证封存；密文不是可被后续重写的工作版本。
        QualificationSubmission stored = mapper.submissionById(row.getId());
        byte[] envelope = crypto.envelope(ViJson.write(facts(stored)), aad(stored));
        if (mapper.sealSubmission(stored.getId(), envelope) != 1)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "资格事实封存失败");
        stored.setSubmissionSeal(envelope);
        return stored;
    }

    private static String aad(QualificationSubmission row) {
        return "Q05:SUBMISSION:"
                + row.getId()
                + ":APPLICATION:"
                + row.getApplicationId()
                + ":REVISION:"
                + row.getSubmissionRevision();
    }

    private static Map<String, Object> facts(QualificationSubmission row) {
        Map<String, Object> value = new TreeMap<>();
        value.put("id", row.getId());
        value.put("applicationId", row.getApplicationId());
        value.put("submissionRevision", row.getSubmissionRevision());
        value.put("ownerDeptId", row.getOwnerDeptId());
        value.put("profileVersion", row.getProfileVersion());
        value.put("companyRevision", row.getCompanyRevision());
        value.put("sourceSessionId", row.getSourceSessionId());
        value.put("applicantUserIds", row.getApplicantUserIds());
        value.put("materialRevision", row.getMaterialRevision());
        value.put(
                "materialHash",
                row.getMaterialHash() == null
                        ? null
                        : Base64.getEncoder().encodeToString(row.getMaterialHash()));
        value.put(
                "sensitivePayloadCipher",
                row.getSensitivePayloadCipher() == null
                        ? null
                        : Base64.getEncoder().encodeToString(row.getSensitivePayloadCipher()));
        value.put("evidenceFileIds", row.getEvidenceFileIds());
        value.put("providerResultIds", row.getProviderResultIds());
        value.put("consentVersion", row.getConsentVersion());
        value.put("consentText", row.getConsentText());
        value.put(
                "consentAcceptedAt",
                row.getConsentAcceptedAt() == null ? null : row.getConsentAcceptedAt().toString());
        value.put(
                "submitReasonCipher",
                row.getSubmitReasonCipher() == null
                        ? null
                        : Base64.getEncoder().encodeToString(row.getSubmitReasonCipher()));
        value.put(
                "submittedAt",
                row.getSubmittedAt() == null ? null : row.getSubmittedAt().toString());
        return value;
    }
}
