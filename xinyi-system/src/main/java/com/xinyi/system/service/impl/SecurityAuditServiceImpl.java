package com.xinyi.system.service.impl;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson2.JSON;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.ip.IpUtils;
import com.xinyi.system.domain.SysSecurityAuditLog;
import com.xinyi.system.mapper.SysSecurityAuditLogMapper;
import com.xinyi.system.service.ISecurityAuditService;
import org.springframework.stereotype.Service;

/**
 * 安全审计写服务。失败审计使用独立事务，避免随业务回滚丢失。
 */
@Service
public class SecurityAuditServiceImpl implements ISecurityAuditService
{
    private final SysSecurityAuditLogMapper auditMapper;

    public SecurityAuditServiceImpl(SysSecurityAuditLogMapper auditMapper)
    {
        this.auditMapper = auditMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordSuccess(String eventType, Long targetUserId, String reason,
            Object beforeValue, Object afterValue, Long versionBefore, Long versionAfter)
    {
        SysSecurityAuditLog log = build(eventType, targetUserId, reason, beforeValue, afterValue);
        log.setAuthVersionBefore(versionBefore);
        log.setAuthVersionAfter(versionAfter);
        log.setResult("SUCCESS");
        auditMapper.insert(log);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String eventType, Long targetUserId, String reason,
            Object beforeValue, Object afterValue, String failureCode, String failureMessage)
    {
        SysSecurityAuditLog log = build(eventType, targetUserId, reason, beforeValue, afterValue);
        log.setResult("FAILURE");
        log.setFailureCode(limit(failureCode, 64));
        log.setFailureMessage(limit(failureMessage, 1000));
        auditMapper.insert(log);
    }

    private SysSecurityAuditLog build(String eventType, Long targetUserId, String reason,
            Object beforeValue, Object afterValue)
    {
        SysSecurityAuditLog log = new SysSecurityAuditLog();
        log.setEventType(limit(eventType, 64));
        log.setTargetUserId(targetUserId);
        log.setReason(limit(reason, 500));
        log.setBeforeJson(safeJson(beforeValue));
        log.setAfterJson(safeJson(afterValue));

        LoginUser actor = currentLoginUser();
        if (actor != null)
        {
            log.setActorUserId(actor.getUserId());
            log.setActorUserName(limit(actor.getUsername(), 64));
        }
        ServletRequestAttributes attributes = requestAttributes();
        if (attributes != null)
        {
            String requestId = attributes.getRequest().getHeader("X-Request-Id");
            log.setRequestId(limit(StringUtils.defaultIfEmpty(requestId,
                    UUID.randomUUID().toString()), 64));
            log.setClientIp(limit(IpUtils.getIpAddr(attributes.getRequest()), 128));
        }
        else
        {
            log.setRequestId(UUID.randomUUID().toString());
            log.setClientIp("INTERNAL");
        }
        return log;
    }

    private LoginUser currentLoginUser()
    {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser)
        {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    private ServletRequestAttributes requestAttributes()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes ? (ServletRequestAttributes) attributes : null;
    }

    private String safeJson(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String json = JSON.toJSONString(value);
        return limit(json, 16000);
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
