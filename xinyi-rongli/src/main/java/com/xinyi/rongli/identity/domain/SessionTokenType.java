package com.xinyi.rongli.identity.domain;

/** 会话令牌类型。ACCESS 短时效绑定上下文；REFRESH 单次使用原子轮换。 */
public enum SessionTokenType {
    ACCESS, REFRESH
}
