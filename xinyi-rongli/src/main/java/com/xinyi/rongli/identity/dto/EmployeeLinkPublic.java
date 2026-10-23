package com.xinyi.rongli.identity.dto;

/**
 * 员工关联公开投影（EmployeeLinkPublic）。仅当前小程序账号的粗粒度结果：
 * 不含 sysUserId、员工名单、其他手机号、完整 OpenID 或后台权限（ADR-04 / V6.4 身份架构 §3）。
 */
public record EmployeeLinkPublic(
        String status,
        boolean workAccessEnabled,
        int authorizedStationCount,
        String messageKey,
        boolean canReverifyPhone) {

    /** 未关联任何员工（手机号未验证或无匹配）。 */
    public static EmployeeLinkPublic unlinked() {
        return new EmployeeLinkPublic("UNLINKED", false, 0, "NO_EMPLOYEE_MATCH", true);
    }
}
