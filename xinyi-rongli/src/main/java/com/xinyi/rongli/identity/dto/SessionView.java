package com.xinyi.rongli.identity.dto;

import java.util.List;

/**
 * 当前会话视图（SessionView，GET /api/common/session）。不含令牌（只读投影，非轮换）；
 * availableRoles/资格/员工关联由服务端按权威状态实时重算。身份响应 no-store。
 */
public record SessionView(
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
        EmployeeLinkPublic employeeLink,
        long accountVersion) {
}
