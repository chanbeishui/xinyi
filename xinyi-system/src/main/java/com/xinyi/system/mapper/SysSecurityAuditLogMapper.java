package com.xinyi.system.mapper;

import com.xinyi.system.domain.SysSecurityAuditLog;

/**
 * 安全审计数据层。
 */
public interface SysSecurityAuditLogMapper
{
    int insert(SysSecurityAuditLog auditLog);
}
