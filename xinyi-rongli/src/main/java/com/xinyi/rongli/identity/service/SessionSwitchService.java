package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.SessionStatus;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViAccountSecurity;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.dto.AccessSessionResponse;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 角色与站点上下文切换（I04，T3.4 专用会话协议）。
 * 只切换服务端实时计算的 availableRoles 内角色；STATION 须有效员工绑定 + 启用站点授权交集（目标站点须在授权集合内）。
 * 原子校验 expectedContextVersion 并递增 context_version、签发绑定新上下文的 ACCESS 令牌；本会话旧上下文令牌随即失效，
 * 其他设备会话不受影响。**切换不释放 assignment、不改变司机任务/车辆保管责任**（TC-R62-024）；撤权回安全视角 OWNER。
 * 不套业务幂等缓存、不存令牌明文；成功审计与 outbox 同事务。
 */
@Service
public class SessionSwitchService {

    private final ViSessionMapper sessionMapper;
    private final ViAccountMapper accountMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final IdentityContextService contextService;
    private final MiniTokenService tokenService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public SessionSwitchService(ViSessionMapper sessionMapper, ViAccountMapper accountMapper,
                                ViAccountSecurityMapper securityMapper, IdentityContextService contextService,
                                MiniTokenService tokenService, AuditService auditService,
                                OutboxService outboxService, PlatformTransactionManager txManager) {
        this.sessionMapper = sessionMapper;
        this.accountMapper = accountMapper;
        this.securityMapper = securityMapper;
        this.contextService = contextService;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /** 切换角色（API_006）：目标须在实时 availableRoles 内；切 STATION 保留仍获授权的当前站点，否则置空待选。 */
    public AccessSessionResponse switchRole(long accountId, long sessionId, String targetRole,
                                            long expectedContextVersion) {
        return tx.execute(s -> doSwitch(accountId, sessionId, targetRole, null, expectedContextVersion, "SWITCH_ROLE"));
    }

    /** 切换站点（API_007）：目标站点须在实时授权集合内，视角置 STATION；0 授权或越权站点拒绝。 */
    public AccessSessionResponse switchStation(long accountId, long sessionId, long stationId,
                                               long expectedContextVersion) {
        return tx.execute(s -> doSwitch(accountId, sessionId, "STATION", stationId, expectedContextVersion, "SWITCH_STATION"));
    }

    private AccessSessionResponse doSwitch(long accountId, long sessionId, String requestedRole, Long requestedStation,
                                           long expectedContextVersion, String action) {
        Instant now = Instant.now();
        ViSession session = sessionMapper.selectByIdForUpdate(sessionId);
        if (session == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (session.getAccountId() != accountId) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "会话不属于当前账号");
        }
        if (!SessionStatus.ACTIVE.name().equals(session.getStatus()) || session.getRevokedAt() != null
                || session.getAbsoluteExpiresAt() == null || !session.getAbsoluteExpiresAt().isAfter(now)) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViAccountSecurity security = securityMapper.selectByAccountId(accountId);
        if (security == null || !Objects.equals(security.getSessionEpoch(), session.getSessionEpoch())) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (session.getContextVersion() != expectedContextVersion) {
            throw contextChanged(sessionId, session.getContextVersion());
        }
        ViAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "账号已被限制");
        }

        IdentityContext ctx = contextService.compute(accountId);
        String oldRole = session.getActiveRole();
        Long oldStation = session.getStationId();
        String newRole;
        Long newStation;
        if (requestedStation != null) {
            // 站点切换：目标须在授权站点集合内（隐含 STATION 可用）；不得切到无权站点
            if (!ctx.stationIds().contains(ViId.external(requestedStation))) {
                throw new ViException(ViErrorCode.VI_FORBIDDEN, "该站点不在当前授权范围内");
            }
            newRole = "STATION";
            newStation = requestedStation;
        } else {
            // 角色切换：目标须在实时可用角色内（未批准无 DRIVER、无员工/站点授权无 STATION）
            if (!ctx.availableRoles().contains(requestedRole)) {
                throw new ViException(ViErrorCode.VI_FORBIDDEN, "目标身份当前不可用");
            }
            newRole = requestedRole;
            if ("STATION".equals(newRole)) {
                // 保留仍获授权的当前站点；已失权则置空（不自动回到无权所属站点，S22），由用户在授权站点中选择
                newStation = oldStation != null && ctx.stationIds().contains(ViId.external(oldStation))
                        ? oldStation : null;
            } else {
                newStation = null;
            }
        }

        int updated = sessionMapper.switchContext(sessionId, newRole, newStation, expectedContextVersion);
        if (updated != 1) {
            throw contextChanged(sessionId, session.getContextVersion());
        }
        long newContextVersion = expectedContextVersion + 1;
        session.setActiveRole(newRole);
        session.setStationId(newStation);
        session.setContextVersion(newContextVersion);
        MiniTokenService.IssuedAccess issued = tokenService.issueAccessForContext(session);

        Actor actor = Actor.mini(accountId, newRole, newStation, sessionId, newContextVersion, ctx.linkedSysUserId());
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("activeRole", oldRole);
        before.put("stationId", oldStation == null ? null : ViId.external(oldStation));
        before.put("contextVersion", expectedContextVersion);
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("activeRole", newRole);
        after.put("stationId", newStation == null ? null : ViId.external(newStation));
        after.put("contextVersion", newContextVersion);
        auditService.recordSuccess(actor, "VI_SESSION", sessionId, action, before, after, null);
        outboxService.publish("session-switch:" + sessionId + ":" + newContextVersion, "VI_SESSION", sessionId,
                "SESSION_CONTEXT_SWITCHED", Map.of("activeRole", newRole, "contextVersion", newContextVersion));

        return new AccessSessionResponse(
                ViId.external(sessionId),
                ViId.external(accountId),
                newRole,
                List.copyOf(ctx.availableRoles()),
                newContextVersion,
                account.getPhoneVerifiedAt() != null,
                ctx.permissions(),
                ctx.capabilities(),
                ctx.stationIds(),
                newStation == null ? null : ViId.external(newStation),
                ctx.workAccessDisabled(),
                ctx.driverQualification(),
                issued.accessToken(),
                issued.expiresIn(),
                ctx.employeeLink(),
                account.getVersion());
    }

    private ViException contextChanged(long sessionId, long currentVersion) {
        return new ViException(ViErrorCode.VI_CONTEXT_CHANGED, "身份或站点已切换，请刷新",
                null,
                List.of(Map.of("resourceType", "VI_SESSION", "resourceId", ViId.external(sessionId),
                        "reasonCode", "CONTEXT_VERSION", "currentVersion", currentVersion)),
                null);
    }
}