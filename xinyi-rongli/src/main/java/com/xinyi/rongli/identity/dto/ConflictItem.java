package com.xinyi.rongli.identity.dto;

/** 冲突项（409）。仅返回调用者已获权对象，禁止泄露其他成员。resourceId 十进制字符串。 */
public record ConflictItem(String resourceType, String resourceId, String reasonCode, Long currentVersion) {
}
