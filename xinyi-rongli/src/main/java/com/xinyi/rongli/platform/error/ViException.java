package com.xinyi.rongli.platform.error;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 普通业务异常：携带稳定 errorCode 与 HTTP 真实状态。RuntimeException 触发事务回滚，
 * 保证“失败不写成功流水/outbox”（ADR-05）。由后续 web 层 @RestControllerAdvice 渲染为
 * V6 ApiError 信封；本类不直接写响应，避免与框架 sys 的“恒 200 + body.code”混淆。
 */
public class ViException extends RuntimeException {

    private final ViErrorCode errorCode;
    private final List<Map<String, String>> fieldErrors;
    private final List<Map<String, Object>> conflicts;
    private final Integer retryAfterSeconds;

    public ViException(ViErrorCode errorCode) {
        this(errorCode, errorCode.defaultMsg(), null, null, null);
    }

    public ViException(ViErrorCode errorCode, String msg) {
        this(errorCode, msg, null, null, null);
    }

    public ViException(ViErrorCode errorCode, String msg, Throwable cause) {
        super(msg, cause);
        this.errorCode = errorCode;
        this.fieldErrors = Collections.emptyList();
        this.conflicts = Collections.emptyList();
        this.retryAfterSeconds = null;
    }

    public ViException(ViErrorCode errorCode, String msg,
                       List<Map<String, String>> fieldErrors,
                       List<Map<String, Object>> conflicts,
                       Integer retryAfterSeconds) {
        super(msg);
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors == null ? Collections.emptyList() : List.copyOf(fieldErrors);
        this.conflicts = conflicts == null ? Collections.emptyList() : List.copyOf(conflicts);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public ViErrorCode getErrorCode() { return errorCode; }
    public int getHttpStatus() { return errorCode.httpStatus(); }
    public boolean isRetryable() { return errorCode.retryable(); }
    public List<Map<String, String>> getFieldErrors() { return fieldErrors; }
    public List<Map<String, Object>> getConflicts() { return conflicts; }
    public Integer getRetryAfterSeconds() { return retryAfterSeconds; }

    /** 429/503 必须给 Retry-After（ADR-04）。 */
    public static ViException rateLimited(int retryAfterSeconds) {
        return new ViException(ViErrorCode.VI_RATE_LIMITED, ViErrorCode.VI_RATE_LIMITED.defaultMsg(),
                null, null, Math.max(0, retryAfterSeconds));
    }

    public static ViException authorityUnavailable() {
        return new ViException(ViErrorCode.VI_AUTHORITY_UNAVAILABLE, ViErrorCode.VI_AUTHORITY_UNAVAILABLE.defaultMsg(),
                null, null, 5);
    }
}
