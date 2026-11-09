package com.xinyi.rongli.verification.service;

import com.xinyi.rongli.platform.actor.Actor;

import java.time.Instant;

/** 人工域提供已落库的双人复核事实；不得根据HTTP请求临时拼出通过，根服务实现该接口。 */
public interface ManualVerificationEvidence {
    record Evidence(
            long reviewId,
            long caseId,
            long applicationId,
            long ownerDeptId,
            long companyRevision,
            long materialRevision,
            byte[] materialHash,
            String type,
            long submittedByUserId,
            long reviewedByUserId,
            String decision,
            Instant reviewedAt,
            Long proofId) {
        @Override
        public String toString() {
            return "ManualVerificationEvidence[REDACTED]";
        }
    }

    /** 真实复核记录不存在或尚无终态时返回null；先检查root人工表，不接受客户端passed。 */
    Evidence approvedEvidence(long reviewId);

    /** 人工域沿其真实复核功能/公司范围再次检查，供共用publisher内部写入使用。 */
    void requireReviewAuthority(Actor actor, long authoritativeOwnerDeptId);
}
