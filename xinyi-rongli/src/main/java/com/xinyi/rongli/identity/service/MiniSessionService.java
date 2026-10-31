package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.SessionStatus;
import com.xinyi.rongli.identity.domain.SessionTokenType;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViAccountSecurity;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.domain.ViSessionToken;
import com.xinyi.rongli.identity.dto.LogoutResult;
import com.xinyi.rongli.identity.dto.SessionResponse;
import com.xinyi.rongli.identity.dto.SessionView;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.mapper.ViSessionTokenMapper;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 刷新、退出与多设备会话（I03，ADR-01）。MINI 认证以数据库为权威（vi_session/vi_session_token/
 * vi_account_security），**不依赖 Redis**——Redis 故障不影响 MINI 放行判断（负向：Redis 故障不放行）。
 * 刷新单次轮换（generation 递增、parent 链接、原子消费），重用已消费刷新令牌→撤销对应会话族；
 * 会话绝对到期不无限滑动；退出当前会话或递增 session_epoch 撤全部设备。退出只清会话，不动车辆任务/资格（TC-R63-008）。
 */
@Service
public class MiniSessionService {

    private final ViSessionMapper sessionMapper;
    private final ViSessionTokenMapper tokenMapper;
    private final ViAccountMapper accountMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final MiniTokenService tokenService;
    private final IdentityContextService contextService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public MiniSessionService(ViSessionMapper sessionMapper, ViSessionTokenMapper tokenMapper,
                              ViAccountMapper accountMapper, ViAccountSecurityMapper securityMapper,
                              MiniTokenService tokenService, IdentityContextService contextService,
                              AuditService auditService, OutboxService outboxService,
                              PlatformTransactionManager txManager) {
        this.sessionMapper = sessionMapper;
        this.tokenMapper = tokenMapper;
        this.accountMapper = accountMapper;
        this.securityMapper = securityMapper;
        this.tokenService = tokenService;
        this.contextService = contextService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /** 单次刷新轮换。重用已消费刷新→撤会话族；绝对到期/撤销/代际不符→拒绝（重新登录）。 */
    public SessionResponse refresh(String refreshTokenPlain) {
        if (refreshTokenPlain == null || refreshTokenPlain.length() < 32) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "refreshToken 缺失或非法");
        }
        // 撤销须在事务内提交后再向客户端报错：若在事务内抛异常，TransactionTemplate 回滚会抹掉撤销事实。
        RefreshResult result = tx.execute(s -> doRefresh(refreshTokenPlain));
        if (result.reused()) {
            throw new ViException(ViErrorCode.VI_REFRESH_REUSED);
        }
        return result.response();
    }

    /** 事务内结果：reused=true 表示已撤销会话族（事务正常提交），由调用方在事务外抛 VI_REFRESH_REUSED。 */
    private record RefreshResult(boolean reused, SessionResponse response) {
    }

    private RefreshResult doRefresh(String refreshTokenPlain) {
        Instant now = Instant.now();
        ViSessionToken token = tokenMapper.selectByHash(tokenService.hash(refreshTokenPlain),
                SessionTokenType.REFRESH.name());
        if (token == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        token = tokenMapper.selectByIdForUpdate(token.getId());
        // 重用检测：已消费的刷新令牌再次出现 → 撤销整个会话族（含已轮换的新令牌），事务提交后拒绝
        if (token.getConsumedAt() != null) {
            return revokeFamilyForReuse(token, now);
        }
        if (token.getRevokedAt() != null || token.getExpiresAt() == null || !token.getExpiresAt().isAfter(now)) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViSession session = sessionMapper.selectByIdForUpdate(token.getSessionId());
        if (session == null || !SessionStatus.ACTIVE.name().equals(session.getStatus())
                || session.getRevokedAt() != null
                || session.getAbsoluteExpiresAt() == null || !session.getAbsoluteExpiresAt().isAfter(now)) {
            // 会话绝对到期：不无限滑动，须重新微信登录
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViAccountSecurity security = securityMapper.selectByAccountId(session.getAccountId());
        if (security == null || !Objects.equals(security.getSessionEpoch(), session.getSessionEpoch())) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        ViAccount account = accountMapper.selectById(session.getAccountId());
        if (account == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "账号已被限制");
        }
        // 原子消费旧刷新（CAS）：并发仅一条成功；失败者视为重用
        int consumed = tokenMapper.consumeRefresh(token.getId(), now, null, token.getVersion());
        if (consumed != 1) {
            return revokeFamilyForReuse(token, now);
        }
        MiniTokenService.IssuedTokens issued = tokenService.rotateSession(session,
                token.getGeneration() + 1, token.getId());

        Actor actor = Actor.mini(account.getId(), session.getActiveRole(), session.getStationId(),
                session.getId(), session.getContextVersion(), session.getLinkedSysUserId());
        auditService.recordSuccess(actor, "VI_SESSION", session.getId(), "TOKEN_REFRESH", null,
                Map.of("generation", token.getGeneration() + 1), null);
        outboxService.publish("token-refresh:" + token.getId(), "VI_SESSION", session.getId(),
                "TOKEN_REFRESHED", Map.of("generation", token.getGeneration() + 1));

        return new RefreshResult(false, buildSessionResponse(session, account, issued));
    }

    /** 重用刷新令牌：撤销会话全部令牌与当前会话，独立事务记拒绝审计，outbox 通告；返回 reused 由事务外报错。 */
    private RefreshResult revokeFamilyForReuse(ViSessionToken token, Instant now) {
        ViSession session = sessionMapper.selectByIdForUpdate(token.getSessionId());
        tokenMapper.revokeBySession(token.getSessionId(), now);
        if (session != null) {
            sessionMapper.revokeSession(session.getId(), now, session.getVersion());
            Actor actor = Actor.mini(session.getAccountId(), session.getActiveRole(), session.getStationId(),
                    session.getId(), session.getContextVersion(), session.getLinkedSysUserId());
            auditService.recordFailure(actor, "VI_SESSION", session.getId(), "TOKEN_REFRESH_REUSED",
                    null, Map.of("generation", token.getGeneration()), "刷新令牌重用，撤销会话族");
            outboxService.publish("refresh-reused:" + token.getId(), "VI_SESSION", session.getId(),
                    "SESSION_FAMILY_REVOKED", Map.of("reason", "REFRESH_REUSED"));
        }
        return new RefreshResult(true, null);
    }

    /** 当前会话视图（只读，无令牌）；availableRoles/资格/员工关联按权威状态实时重算。 */
    public SessionView currentSession(long accountId, long sessionId) {
        ViSession session = sessionMapper.selectById(sessionId);
        ViAccount account = accountMapper.selectById(accountId);
        if (session == null || account == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        IdentityContext ctx = contextService.compute(accountId);
        return new SessionView(
                ViId.external(session.getId()),
                ViId.external(account.getId()),
                session.getActiveRole(),
                List.copyOf(ctx.availableRoles()),
                session.getContextVersion(),
                account.getPhoneVerifiedAt() != null,
                ctx.permissions(),
                ctx.capabilities(),
                ctx.stationIds(),
                session.getStationId() == null ? null : ViId.external(session.getStationId()),
                ctx.workAccessDisabled(),
                ctx.driverQualification(),
                ctx.employeeLink(),
                account.getVersion());
    }

    /** 退出：allDevices=false 撤当前会话；true 递增 session_epoch 撤全部设备。DB 权威，不依赖 Redis，不动车辆任务。 */
    public LogoutResult logout(long accountId, long sessionId, boolean allDevices) {
        return tx.execute(s -> {
            Instant now = Instant.now();
            if (allDevices) {
                securityMapper.bumpSessionEpoch(accountId, now);
                auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, sessionId, null, null),
                        "VI_ACCOUNT", accountId, "LOGOUT_ALL", null, Map.of("scope", "ALL"), null);
                outboxService.publish("logout-all:" + accountId + ":" + now.toEpochMilli(),
                        "VI_ACCOUNT", accountId, "SESSIONS_REVOKED_ALL", Map.of("scope", "ALL"));
                return LogoutResult.all();
            }
            ViSession session = sessionMapper.selectByIdForUpdate(sessionId);
            if (session != null && session.getAccountId() == accountId) {
                sessionMapper.revokeSession(sessionId, now, session.getVersion());
                tokenMapper.revokeBySession(sessionId, now);
            }
            auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, sessionId, null, null),
                    "VI_SESSION", sessionId, "LOGOUT", null, Map.of("scope", "CURRENT"), null);
            outboxService.publish("logout:" + sessionId, "VI_SESSION", sessionId,
                    "SESSION_REVOKED", Map.of("scope", "CURRENT"));
            return LogoutResult.current();
        });
    }

    private SessionResponse buildSessionResponse(ViSession session, ViAccount account,
                                                 MiniTokenService.IssuedTokens issued) {
        IdentityContext ctx = contextService.compute(account.getId());
        // 旧 STATION 失权（授权被撤）则视角回退 OWNER；activeRole 持久化切换属 I04，此处仅按权威状态校正返回视角
        String effectiveRole = ctx.availableRoles().contains(session.getActiveRole())
                ? session.getActiveRole() : "OWNER";
        String effectiveStation = "STATION".equals(effectiveRole) && session.getStationId() != null
                ? ViId.external(session.getStationId()) : null;
        return new SessionResponse(
                ViId.external(session.getId()),
                ViId.external(account.getId()),
                effectiveRole,
                List.copyOf(ctx.availableRoles()),
                session.getContextVersion(),
                account.getPhoneVerifiedAt() != null,
                ctx.permissions(),
                ctx.capabilities(),
                ctx.stationIds(),
                effectiveStation,
                ctx.workAccessDisabled(),
                ctx.driverQualification(),
                issued.accessToken(),
                issued.expiresIn(),
                issued.refreshToken(),
                issued.refreshExpiresIn(),
                ctx.employeeLink(),
                account.getVersion());
    }
}