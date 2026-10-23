package com.xinyi.rongli.identity.dto;

import java.util.List;

/**
 * 当前统一档案（Profile，API_005_get）。phoneMask 为脱敏手机号；realName 为服务称呼，
 * 不代表实名核验已通过；employeeLink 为粗粒度公开投影。ID 十进制字符串。
 */
public record ProfileResponse(
        String accountId,
        String nickname,
        String phoneMask,
        String driverQualification,
        List<String> availableRoles,
        List<String> permissions,
        long version,
        String realName,
        String avatarFileId,
        String defaultAddressId,
        EmployeeLinkPublic employeeLink) {
}
