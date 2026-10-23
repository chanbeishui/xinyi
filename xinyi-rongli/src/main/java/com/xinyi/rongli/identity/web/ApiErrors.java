package com.xinyi.rongli.identity.web;

import com.xinyi.rongli.identity.dto.ApiError;
import com.xinyi.rongli.identity.dto.ApiErrorDetails;
import com.xinyi.rongli.identity.dto.ConflictItem;
import com.xinyi.rongli.identity.dto.FieldViolation;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

import java.util.List;
import java.util.Map;

/** ViException/ViErrorCode → ApiError 信封的转换（真实 HTTP 状态 + 稳定 errorCode + 限定详情白名单）。 */
final class ApiErrors {

    private ApiErrors() {
    }

    static ApiError from(ViException ex, String requestId) {
        ViErrorCode ec = ex.getErrorCode();
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : ec.defaultMsg();
        return new ApiError(ec.httpStatus(), ec.name(), msg, requestId, ec.retryable(),
                ex.getRetryAfterSeconds(), details(ex));
    }

    static ApiError of(ViErrorCode ec, String msg, String requestId) {
        return new ApiError(ec.httpStatus(), ec.name(),
                msg != null && !msg.isBlank() ? msg : ec.defaultMsg(),
                requestId, ec.retryable(), null, ApiErrorDetails.empty());
    }

    private static ApiErrorDetails details(ViException ex) {
        List<Map<String, String>> fe = ex.getFieldErrors();
        List<Map<String, Object>> cf = ex.getConflicts();
        boolean noFe = fe == null || fe.isEmpty();
        boolean noCf = cf == null || cf.isEmpty();
        if (noFe && noCf) {
            return ApiErrorDetails.empty();
        }
        List<FieldViolation> fieldViolations = noFe ? null : fe.stream()
                .map(m -> new FieldViolation(m.get("field"), m.get("reasonCode"), m.get("message")))
                .toList();
        List<ConflictItem> conflicts = noCf ? null : cf.stream()
                .map(m -> new ConflictItem(str(m, "resourceType"), str(m, "resourceId"),
                        str(m, "reasonCode"), lng(m, "currentVersion")))
                .toList();
        return new ApiErrorDetails(fieldViolations, conflicts, null, null, null);
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : String.valueOf(v);
    }

    private static Long lng(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v instanceof Number n ? n.longValue() : null;
    }
}
