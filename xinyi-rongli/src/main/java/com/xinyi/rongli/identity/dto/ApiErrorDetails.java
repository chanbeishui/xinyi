package com.xinyi.rongli.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 错误详情固定白名单（additionalProperties:false，全部可选）；无明文 Token/凭证/交接码/SQL。
 * 空详情以 NON_NULL 渲染为 {}，契合契约示例。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorDetails(
        List<FieldViolation> fieldErrors,
        List<ConflictItem> conflicts,
        List<String> missingPhotoTypes,
        Integer remainingAttempts,
        Long currentVersion) {

    public static ApiErrorDetails empty() {
        return new ApiErrorDetails(null, null, null, null, null);
    }

    public static ApiErrorDetails ofFields(List<FieldViolation> fieldErrors) {
        return new ApiErrorDetails(fieldErrors, null, null, null, null);
    }
}
