package com.xinyi.rongli.identity.domain;

/**
 * 员工关联公开状态（粗粒度）。小程序只收到本人关联结果，重号/冲突不透露其他员工或账号。
 * NEEDS_ADMIN 表示手机号核验成功但无唯一员工匹配/重号/冲突，保持车主能力。
 */
public enum EmployeeLinkStatus {
    UNLINKED, LINKED, NEEDS_ADMIN, SUSPENDED, REVOKED
}
