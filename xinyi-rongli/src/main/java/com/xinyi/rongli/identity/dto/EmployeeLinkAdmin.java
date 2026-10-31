package com.xinyi.rongli.identity.dto;

/**
 * 后台员工微信关联投影（B36，EmployeeLinkAdmin）。部门数据范围校验后返回；
 * phoneMask/openIdMask 仅脱敏识别摘要，不返回完整手机号/微信标识；不是新的员工账号管理。
 */
public record EmployeeLinkAdmin(
        String bindingId,
        String accountId,
        String appId,
        String sysUserId,
        String employeeName,
        String phoneMask,
        String openIdMask,
        String status,
        String source,
        String boundAt,
        String lastPhoneVerifiedAt,
        boolean autoRebindBlocked,
        long version) {
}