package com.xinyi.rongli.identity.dto;

import java.util.List;

/**
 * 登录/刷新返回的会话视图（Session）。ID 一律十进制字符串；activeRole/availableRoles/
 * driverQualification 为枚举字面量字符串。token/refreshToken 为不透明明文令牌，仅此一处返回，
 * 不写日志/幂等缓存（SecretGuard 约束）。新会话默认 OWNER；恢复已审核身份但不重新授予。
 */
public record SessionResponse(
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
        String refreshToken,
        long refreshExpiresIn,
        EmployeeLinkPublic employeeLink,
        long accountVersion) {
}
