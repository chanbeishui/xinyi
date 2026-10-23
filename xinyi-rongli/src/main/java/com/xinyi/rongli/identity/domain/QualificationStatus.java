package com.xinyi.rongli.identity.domain;

/** 代驾资格状态。仅 APPROVED 且未撤销才开通 DRIVER 角色；申请/审核中不增加 DRIVER。 */
public enum QualificationStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED, EXPIRED, SUSPENDED
}
