package com.xinyi.rongli.identity.dto;

/** 字段级校验违规（422）。message 为安全人类文案，前端逻辑按 reasonCode。 */
public record FieldViolation(String field, String reasonCode, String message) {
}
