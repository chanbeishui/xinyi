package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.qualification.domain.QualificationSnapshot;
import com.xinyi.rongli.qualification.mapper.QualificationAdminMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/** 独立认证不可变事实；不承担访问授权，不依赖当前MINI或原登录仍在线，调用方必须先验证真实父记录权限。 */
@Service
public class QualificationSnapshotAuthority {
    private final QualificationAdminMapper mapper;
    private final IntegrationCrypto crypto;

    public QualificationSnapshotAuthority(
            QualificationAdminMapper mapper, IntegrationCrypto crypto) {
        this.mapper = mapper;
        this.crypto = crypto;
    }

    public QualificationSnapshot authenticated(long snapshotId) {
        QualificationSnapshot row = mapper.snapshot(snapshotId);
        return authenticated(row) ? row : null;
    }

    public boolean authenticated(QualificationSnapshot row) {
        if (row == null
                || row.getSnapshotSeal() == null
                || row.getApprovedAt() == null
                || row.getExpiresAt() == null
                || row.getReviewDueAt() == null
                || row.getMaterialHash() == null) return false;
        try {
            Map<?, ?> value =
                    ViJson.read(crypto.unenvelope(row.getSnapshotSeal(), aad(row)), Map.class);
            return ViJson.write(value).equals(ViJson.write(facts(row))) && parentMatches(row);
        } catch (ViException failure) {
            // 缺部署密钥属于权威不可读，不能被当作普通旧数据并静默降级。
            if (failure.getErrorCode() == ViErrorCode.VI_SECRET_UNAVAILABLE) throw failure;
            return false;
        } catch (RuntimeException failure) {
            return false;
        }
    }

    private boolean parentMatches(QualificationSnapshot row) {
        var review = mapper.review(row.getReviewId());
        var submission = mapper.submissionById(row.getSubmissionId());
        var context = mapper.context(row.getApplicationId());
        if (review == null
                || submission == null
                || context == null
                || !QualificationSubmissionAuthority.authenticated(submission, crypto)
                || !"APPROVE".equals(review.getDecision())
                || !"DECISION".equals(review.getKind())
                || review.getInternalOpinionCipher() == null
                || review.getReviewedAt() == null
                || review.getApplicationId() != row.getApplicationId()
                || review.getDriverId() != row.getDriverId()
                || review.getOwnerDeptId() != row.getOwnerDeptId()
                || review.getCompanyRevision() != row.getCompanyRevision()
                || !Objects.equals(review.getSubmissionId(), row.getSubmissionId())
                || review.getSubmissionRevision() != submission.getSubmissionRevision()
                || !Objects.equals(submission.getApplicationId(), row.getApplicationId())
                || !Objects.equals(submission.getOwnerDeptId(), row.getOwnerDeptId())
                || !Objects.equals(submission.getCompanyRevision(), row.getCompanyRevision())
                || submission.getMaterialRevision() != row.getMaterialRevision()
                || !java.security.MessageDigest.isEqual(
                        submission.getMaterialHash(), row.getMaterialHash())
                || ((Number) context.get("driver_id")).longValue() != row.getDriverId()
                || ((Number) context.get("account_id")).longValue() != row.getAccountId()
                || row.getApprovedAt().isBefore(review.getReviewedAt())
                || row.getApprovedAt().isAfter(java.time.Instant.now())
                || !row.getReviewDueAt().isAfter(row.getApprovedAt())
                || row.getExpiresAt().isAfter(row.getReviewDueAt())
                || row.getLicenseClass() == null
                || row.getLicenseClass().isBlank()) return false;
        InstantBoundary boundary = new InstantBoundary(row);
        return boundary.valid();
    }

    private record InstantBoundary(QualificationSnapshot row) {
        boolean valid() {
            if (row.getLicenseLongTerm())
                return row.getLicenseValidTo() == null
                        && row.getExpiresAt().equals(row.getReviewDueAt());
            if (row.getLicenseValidTo() == null) return false;
            java.time.Instant licenseEnd =
                    row.getLicenseValidTo()
                            .atZone(java.time.ZoneId.of("Asia/Shanghai"))
                            .toLocalDate()
                            .plusDays(1)
                            .atStartOfDay(java.time.ZoneId.of("Asia/Shanghai"))
                            .toInstant();
            java.time.Instant expected =
                    licenseEnd.isBefore(row.getReviewDueAt()) ? licenseEnd : row.getReviewDueAt();
            return expected.equals(row.getExpiresAt());
        }
    }

    public QualificationSnapshot insertAndSeal(QualificationSnapshot row) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("资格快照必须随业务事务封存");
        mapper.insertSnapshot(row);
        // 先取得数据库主键和毫秒UTC时刻，再一次认证封存；密文不是可被后续重写的工作版本。
        QualificationSnapshot stored = mapper.snapshot(row.getId());
        byte[] envelope = crypto.envelope(ViJson.write(facts(stored)), aad(stored));
        if (mapper.sealSnapshot(stored.getId(), envelope) != 1)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "资格事实封存失败");
        stored.setSnapshotSeal(envelope);
        return stored;
    }

    private static String aad(QualificationSnapshot row) {
        return "Q05:SNAPSHOT:"
                + row.getId()
                + ":DRIVER:"
                + row.getDriverId()
                + ":REVISION:"
                + row.getQualificationRevision();
    }

    private static Map<String, Object> facts(QualificationSnapshot row) {
        Map<String, Object> value = new TreeMap<>();
        value.put("id", row.getId());
        value.put("driverId", row.getDriverId());
        value.put("accountId", row.getAccountId());
        value.put("applicationId", row.getApplicationId());
        value.put("submissionId", row.getSubmissionId());
        value.put("reviewId", row.getReviewId());
        value.put("qualificationRevision", row.getQualificationRevision());
        value.put("ownerDeptId", row.getOwnerDeptId());
        value.put("companyRevision", row.getCompanyRevision());
        value.put("materialRevision", row.getMaterialRevision());
        value.put(
                "materialHash",
                row.getMaterialHash() == null
                        ? null
                        : Base64.getEncoder().encodeToString(row.getMaterialHash()));
        value.put("licenseClass", row.getLicenseClass());
        value.put(
                "licenseValidTo",
                row.getLicenseValidTo() == null ? null : row.getLicenseValidTo().toString());
        value.put("licenseLongTerm", row.getLicenseLongTerm());
        value.put(
                "reviewDueAt",
                row.getReviewDueAt() == null ? null : row.getReviewDueAt().toString());
        value.put("expiresAt", row.getExpiresAt() == null ? null : row.getExpiresAt().toString());
        value.put(
                "approvedAt", row.getApprovedAt() == null ? null : row.getApprovedAt().toString());
        return value;
    }
}
