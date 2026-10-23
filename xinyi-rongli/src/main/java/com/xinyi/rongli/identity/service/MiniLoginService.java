package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.AccountStatus;
import com.xinyi.rongli.identity.domain.MiniRole;
import com.xinyi.rongli.identity.domain.SessionStatus;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViAccountSecurity;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.config.ViIdentityProperties;
import com.xinyi.rongli.identity.dto.SessionResponse;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViSessionMapper;
import com.xinyi.rongli.identity.provider.IdentityProvider;
import com.xinyi.rongli.identity.provider.WeChatIdentity;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一微信登录（API_001_post）。以服务端可信换取的 AppID/OpenID 唯一创建或恢复 vi_account；
 * 新账号只有 OWNER，新会话默认 activeRole=OWNER；绝不接收客户端 role/openid/sysUserId 作为身份依据；
 * 恢复老账号真实资格（读权威状态）而不重置代驾档案、不创建重复 sys_user、不签发 ADMIN 令牌。
 *
 * 微信外部换取在数据库事务之外（绝不置于锁/事务内）；账号 get-or-create 依赖 uk_app_open 唯一键，
 * 保证同 openid 并发只产生一个账号。成功审计与业务 outbox 同事务（复用 E06）。
 * 明文令牌仅在返回的 SessionResponse 中出现一次，绝不写审计/日志/outbox（SecretGuard 约束）。
 */
@Service
public class MiniLoginService {

    private final IdentityProvider identityProvider;
    private final ViAccountMapper accountMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final ViSessionMapper sessionMapper;
    private final IdentityContextService contextService;
    private final MiniTokenService tokenService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final ViIdentityProperties props;
    private final TransactionTemplate tx;

    public MiniLoginService(IdentityProvider identityProvider, ViAccountMapper accountMapper,
                            ViAccountSecurityMapper securityMapper, ViSessionMapper sessionMapper,
                            IdentityContextService contextService, MiniTokenService tokenService,
                            AuditService auditService, OutboxService outboxService,
                            ViIdentityProperties props, PlatformTransactionManager txManager) {
        this.identityProvider = identityProvider;
        this.accountMapper = accountMapper;
        this.securityMapper = securityMapper;
        this.sessionMapper = sessionMapper;
        this.contextService = contextService;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.props = props;
        this.tx = new TransactionTemplate(txManager);
        // READ_COMMITTED：并发同 openid 登录时，输家线程的 insertOrGetId 会阻塞至赢家提交，
        // 之后的账号/安全行读取需看到赢家已提交结果（REPEATABLE_READ 旧快照会漏读并撞唯一键）。
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public SessionResponse login(String code, String deviceLabel) {
        // 1) 外部换取可信身份（事务外；SIMULATOR/WECHAT 由 vi.providers.mode 选择）
        WeChatIdentity identity = identityProvider.exchangeCode(code);
        // 2) 账号/安全/会话/令牌/审计/outbox 在同一数据库事务内完成
        return tx.execute(status -> establishSession(identity, deviceLabel));
    }

    private SessionResponse establishSession(WeChatIdentity identity, String deviceLabel) {
        Instant now = Instant.now();
        ViAccount account = accountMapper.selectByAppOpen(identity.appId(), identity.openId());
        boolean created = false;
        if (account == null) {
            ViAccount fresh = new ViAccount();
            fresh.setAppId(identity.appId());
            fresh.setOpenId(identity.openId());
            fresh.setUnionId(identity.unionId());
            fresh.setStatus(AccountStatus.ACTIVE.name());
            accountMapper.insertOrGetId(fresh);
            // 锁定读：并发输家线程的一致性快照早于赢家提交，普通读看不到既有行；FOR UPDATE 读最新已提交
            account = accountMapper.selectByIdForUpdate(fresh.getId());
            created = true;
        }
        if (account == null) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "账号创建后无法读取");
        }
        if (!AccountStatus.ACTIVE.name().equals(account.getStatus())) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "账号已被限制登录");
        }

        ViAccountSecurity security = securityMapper.selectByAccountId(account.getId());
        if (security == null) {
            security = new ViAccountSecurity();
            security.setAccountId(account.getId());
            security.setSessionEpoch(0L);
            security.setPermissionVersion(0L);
            security.setSecurityStatus(AccountStatus.ACTIVE.name());
            security.setChangedAt(now);
            securityMapper.insert(security);
        }

        IdentityContext ctx = contextService.compute(account.getId());

        ViSession session = new ViSession();
        session.setAccountId(account.getId());
        session.setAppId(identity.appId());
        session.setStatus(SessionStatus.ACTIVE.name());
        session.setSessionEpoch(security.getSessionEpoch());
        session.setContextVersion(1L);
        session.setActiveRole(MiniRole.OWNER.name());
        session.setStationId(null);
        session.setLinkedSysUserId(ctx.linkedSysUserId());
        session.setAbsoluteExpiresAt(now.plusSeconds(props.getAbsoluteTtlSeconds()));
        session.setDeviceLabel(truncate(deviceLabel, 120));
        sessionMapper.insert(session);

        MiniTokenService.IssuedTokens tokens = tokenService.issueForSession(session);

        Actor actor = Actor.mini(account.getId(), MiniRole.OWNER.name(), null,
                session.getId(), session.getContextVersion(), ctx.linkedSysUserId());
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("activeRole", MiniRole.OWNER.name());
        after.put("availableRoles", new ArrayList<>(ctx.availableRoles()));
        after.put("newAccount", created);
        // after 不含任何令牌/凭据字段名，SecretGuard 校验通过
        auditService.recordSuccess(actor, "VI_ACCOUNT", account.getId(), "LOGIN", null, after, null);
        outboxService.publish("mini-login:" + session.getId(), "VI_ACCOUNT", account.getId(),
                "MINI_SESSION_CREATED", Map.of("activeRole", MiniRole.OWNER.name(),
                        "availableRoles", new ArrayList<>(ctx.availableRoles())));

        return new SessionResponse(
                ViId.external(session.getId()),
                ViId.external(account.getId()),
                MiniRole.OWNER.name(),
                List.copyOf(ctx.availableRoles()),
                session.getContextVersion(),
                account.getPhoneVerifiedAt() != null,
                ctx.permissions(),
                ctx.capabilities(),
                ctx.stationIds(),
                null,
                ctx.workAccessDisabled(),
                ctx.driverQualification(),
                tokens.accessToken(),
                tokens.expiresIn(),
                tokens.refreshToken(),
                tokens.refreshExpiresIn(),
                ctx.employeeLink(),
                account.getVersion());
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
