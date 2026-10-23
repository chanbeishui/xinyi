package com.xinyi.rongli.identity.domain;

/** 手机号证明来源。仅 WECHAT（本人微信授权核验）可触发新员工自动关联；SMS 不作为关联依据。 */
public enum PhoneProofSource {
    WECHAT, SMS
}
