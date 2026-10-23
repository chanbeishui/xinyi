package com.xinyi.rongli.identity.dto;

/**
 * vi 域统一错误信封（ApiError）。code 等于真实 HTTP 状态；errorCode 为稳定枚举；
 * requestId 关联日志；retryable/retryAfterSeconds 指导客户端重试；data 为限定详情白名单。
 * required 字段齐全（含 retryAfterSeconds:null），故用默认（含 null）序列化。
 */
public record ApiError(
        int code,
        String errorCode,
        String msg,
        String requestId,
        boolean retryable,
        Integer retryAfterSeconds,
        ApiErrorDetails data) {
}
