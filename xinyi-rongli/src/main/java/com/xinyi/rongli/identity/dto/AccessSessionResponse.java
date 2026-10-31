package com.xinyi.rongli.identity.dto;

import java.util.List;

/**
 * 角色/站点切换返回（AccessSession，I04）。含新访问令牌（绑定切换后的上下文版本）与有效期，
 * 不含刷新令牌（刷新凭据仍为会话级，不随切换轮换）。本会话旧上下文令牌随即失效；其他设备不受影响。
 * ID 一律十进制字符串；明文令牌仅此一处返回，no-store、不写日志/幂等缓存。
 */
public record AccessSessionResponse(
        String sessionId,
        String accountId,
        String activeRole,
        List<String> availableRoles,
        long contextVersion,
        boolean phoneBound,
        List<String> permissions,
        List<String> capabilities,
        List<String> stationIds,
        String stationId,
        boolean workAccessDisabled,
        String driverQualification,
        String token,
        long expiresIn,
        EmployeeLinkPublic employeeLink,
        long accountVersion) {
}