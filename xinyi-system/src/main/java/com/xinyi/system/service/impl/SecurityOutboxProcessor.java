package com.xinyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.common.security.SessionInvalidator;
import com.xinyi.system.domain.SysSecurityOutbox;
import com.xinyi.system.mapper.SysSecurityOutboxMapper;
import com.xinyi.system.service.ISecurityAuditService;

/**
 * 重试提交后会话清理。权限拒绝仍以数据库 auth_version 为最终依据。
 */
@Component
public class SecurityOutboxProcessor
{
    private static final Logger log = LoggerFactory.getLogger(SecurityOutboxProcessor.class);
    private static final int BATCH_SIZE = 100;

    private final SysSecurityOutboxMapper outboxMapper;
    private final ObjectProvider<SessionInvalidator> invalidatorProvider;
    private final ISecurityAuditService auditService;

    public SecurityOutboxProcessor(SysSecurityOutboxMapper outboxMapper,
            ObjectProvider<SessionInvalidator> invalidatorProvider,
            ISecurityAuditService auditService)
    {
        this.outboxMapper = outboxMapper;
        this.invalidatorProvider = invalidatorProvider;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "${security.outbox.fixed-delay:5000}")
    @Transactional
    public void process()
    {
        SessionInvalidator invalidator = invalidatorProvider.getIfAvailable();
        if (invalidator == null)
        {
            return;
        }
        List<SysSecurityOutbox> events = outboxMapper.selectPending(new Date(), BATCH_SIZE);
        for (SysSecurityOutbox event : events)
        {
            try
            {
                invalidator.forceLogoutByUserId(event.getUserId());
                outboxMapper.markDone(event.getEventId());
                auditService.recordSuccess("SESSION_FORCE_LOGOUT", event.getUserId(),
                        "security outbox event " + event.getEventId(), null, null,
                        event.getTargetAuthVersion(), event.getTargetAuthVersion());
            }
            catch (RuntimeException ex)
            {
                int retryCount = event.getRetryCount() + 1;
                long delaySeconds = Math.min(3600L, 1L << Math.min(retryCount, 12));
                outboxMapper.markRetry(event.getEventId(), retryCount,
                        new Date(System.currentTimeMillis() + delaySeconds * 1000L),
                        limit(ex.getMessage(), 1000));
                auditService.recordFailure("SESSION_FORCE_LOGOUT", event.getUserId(),
                        "security outbox retry " + retryCount, null, null,
                        "REDIS_FORCE_LOGOUT_FAILED", ex.getMessage());
                log.warn("安全outbox事件[{}]执行失败，第{}次重试: {}",
                        event.getEventId(), retryCount, ex.getMessage());
            }
        }
    }

    private String limit(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
