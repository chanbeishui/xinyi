package com.xinyi.rongli.identity.domain;

/** 手机号核验状态机。PROCESSING 处理中（租约内不重复消费）；UNKNOWN 外部结果不明（查回执/重新授权，不假成功）。 */
public enum PhoneVerificationStatus {
    PROCESSING, VERIFIED, UNKNOWN, FAILED
}
