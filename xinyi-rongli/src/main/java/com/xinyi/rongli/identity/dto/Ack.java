package com.xinyi.rongli.identity.dto;

/** 通用确认回执（Ack）。accepted 必填；requestId 不透明字符串；version 客户端最后读取版本。 */
public record Ack(boolean accepted, String requestId, Long version) {
    public static Ack ok() {
        return new Ack(true, null, null);
    }
}
