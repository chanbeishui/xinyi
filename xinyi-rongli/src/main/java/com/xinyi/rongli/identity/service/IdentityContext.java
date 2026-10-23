package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.dto.EmployeeLinkPublic;

import java.util.List;
import java.util.Set;

/**
 * 登录/档案计算出的身份上下文投影。availableRoles 由服务端按权威状态计算（绝不来自客户端）；
 * capabilities/permissions 仅为 UI 提示，写操作各自实时重算。stationIds 为十进制字符串。
 */
public record IdentityContext(
        Set<String> availableRoles,
        String driverQualification,
        EmployeeLinkPublic employeeLink,
        List<String> capabilities,
        List<String> permissions,
        List<String> stationIds,
        boolean workAccessDisabled,
        Long linkedSysUserId) {
}
