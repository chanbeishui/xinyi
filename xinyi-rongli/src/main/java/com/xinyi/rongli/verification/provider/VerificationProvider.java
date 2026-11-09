package com.xinyi.rongli.verification.provider;

import java.time.Instant;

/** 微信SOTER可信验签端口；不创建外部核身任务，不表示实名、人脸活体或身份证真实性。 */
public interface VerificationProvider {
    record Configuration(
            long ownerDeptId,
            long profileId,
            long revisionId,
            String provider,
            String product,
            String appId,
            String identityDigest) {}

    record VerifyCommand(String openId, String resultJSON, String resultJSONSignature) {
        @Override
        public String toString() {
            return "SoterVerifyCommand[REDACTED]";
        }
    }

    enum ResultStatus {
        PASSED,
        FAILED,
        UNKNOWN
    }

    /** 仅官方is_ok布尔true可PASSED；不扩展或猜测is_root等未定义微信返回字段。 */
    record VerifyResult(
            ResultStatus status, Instant verifiedAt, String failureCode, String resultDigest) {}

    Configuration configuration(long authoritativeOwnerDeptId);

    VerifyResult verify(Configuration frozen, VerifyCommand command);
}
