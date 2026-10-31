package com.xinyi.rongli.identity.provider;

import com.xinyi.rongli.identity.domain.PhoneVerificationStatus;

/**
 * 微信手机号换取结果。VERIFIED 携带可信号码；FAILED 凭据无效/被拒；UNKNOWN 外部结果不明
 * （超时/网络不确定），调用方须保留处理中/未知并查回执或重新授权，绝不把 UNKNOWN 当成功。
 * phone 仅在 VERIFIED 时非空；不含 session_key 等凭据。
 */
public record PhoneExchangeResult(PhoneVerificationStatus status, String phone) {

    public static PhoneExchangeResult verified(String phone) {
        return new PhoneExchangeResult(PhoneVerificationStatus.VERIFIED, phone);
    }

    public static PhoneExchangeResult failed() {
        return new PhoneExchangeResult(PhoneVerificationStatus.FAILED, null);
    }

    public static PhoneExchangeResult unknown() {
        return new PhoneExchangeResult(PhoneVerificationStatus.UNKNOWN, null);
    }
}
