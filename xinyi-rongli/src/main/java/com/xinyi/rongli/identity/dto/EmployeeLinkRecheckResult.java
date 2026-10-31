package com.xinyi.rongli.identity.dto;

/**
 * 允许重新核验结果（B36，EmployeeLinkRecheckResult）。仅取消自动重匹配阻止标记；
 * 不直接恢复 SUSPENDED/REVOKED、不授角色；必须本人新的微信手机号授权后通过全部唯一性检查。
 */
public record EmployeeLinkRecheckResult(EmployeeLinkAdmin binding, boolean requiresNewPhoneAuthorization) {
}