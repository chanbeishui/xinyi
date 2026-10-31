package com.xinyi.rongli.identity.dto;

/**
 * 手机号验证结果（PhoneBindResult）。手机号验证与员工关联结果分开：员工冲突不推翻已成功的手机号验证。
 * 无令牌；requiresSessionRefresh=true 时客户端先以 REFRESH 凭据轮换会话再读关联/切角色，不得重放 phoneCode。
 * verificationStatus: PROCESSING/VERIFIED/UNKNOWN/FAILED。
 */
public record PhoneBindResult(
        String verificationRequestId,
        String verificationStatus,
        boolean phoneBound,
        String phoneMask,
        long accountVersion,
        EmployeeLinkPublic employeeLink,
        boolean requiresSessionRefresh) {
}
