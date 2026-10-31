package com.xinyi.rongli.identity.domain;

/**
 * 员工自动关联内部结果（不直接下发小程序，仅投影为粗粒度 EmployeeLinkPublic）。
 * 手机号核验成功与员工匹配结果分开：NO_MATCH/AMBIGUOUS_PHONE 等不推翻已成功的手机号验证、不取消 OWNER。
 */
public enum EmployeeLinkOutcome {
    NOT_ATTEMPTED, NO_MATCH, LINKED, EXISTING_LINK, AMBIGUOUS_PHONE, EMPLOYEE_UNAVAILABLE,
    USER_ALREADY_LINKED, ACCOUNT_ALREADY_LINKED, REBIND_BLOCKED, PHONE_CHANGED, DIRECTORY_UNAVAILABLE
}
