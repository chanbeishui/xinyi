package com.xinyi.system.service;

/**
 * 权限敏感操作的不可变安全审计入口。
 */
public interface ISecurityAuditService
{
    void recordSuccess(String eventType, Long targetUserId, String reason,
            Object beforeValue, Object afterValue, Long versionBefore, Long versionAfter);

    void recordFailure(String eventType, Long targetUserId, String reason,
            Object beforeValue, Object afterValue, String failureCode, String failureMessage);
}
