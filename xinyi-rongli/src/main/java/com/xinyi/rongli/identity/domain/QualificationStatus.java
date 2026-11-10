package com.xinyi.rongli.identity.domain;

/** 代驾资格状态。首次批准才开通DRIVER视角；到期/暂停保留历史及在手入口，新单实时单独核验。 */
public enum QualificationStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED, EXPIRED, SUSPENDED
}
