package com.xinyi.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.xinyi.system.mapper.SysUserDeptMapper;
import com.xinyi.system.service.ISecurityAuditService;

/**
 * 每日检查主部门、DEPT账号和任职关系完整性。
 */
@Component
public class UserDeptIntegrityPatrol
{
    private static final Logger log = LoggerFactory.getLogger(UserDeptIntegrityPatrol.class);

    private final SysUserDeptMapper userDeptMapper;
    private final ISecurityAuditService auditService;

    public UserDeptIntegrityPatrol(SysUserDeptMapper userDeptMapper,
            ISecurityAuditService auditService)
    {
        this.userDeptMapper = userDeptMapper;
        this.auditService = auditService;
    }

    @Scheduled(cron = "${security.user-dept-integrity-cron:0 30 2 * * ?}")
    public void inspect()
    {
        int issueCount = userDeptMapper.countIntegrityIssues();
        if (issueCount > 0)
        {
            log.error("用户任职部门完整性巡检发现{}条异常，必须人工处理", issueCount);
            auditService.recordFailure("USER_DEPT_INTEGRITY_PATROL", null,
                    "每日完整性巡检", null, null, "INTEGRITY_ISSUES",
                    "发现异常数量: " + issueCount);
        }
    }
}
