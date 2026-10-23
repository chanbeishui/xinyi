package com.xinyi.rongli.identity.domain;

/** 会话状态。REVOKED 为退出/安全撤销，EXPIRED 为超过绝对截止。 */
public enum SessionStatus {
    ACTIVE, REVOKED, EXPIRED
}
