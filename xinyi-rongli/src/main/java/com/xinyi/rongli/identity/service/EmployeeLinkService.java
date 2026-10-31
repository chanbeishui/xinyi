package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.domain.EmployeeLinkOutcome;
import com.xinyi.rongli.identity.domain.SessionStatus;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import com.xinyi.rongli.identity.domain.ViPhoneVerificationReceipt;
import com.xinyi.rongli.identity.domain.ViSession;
import com.xinyi.rongli.identity.dto.EmployeeLinkPublic;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViEmployeeBindingMapper;
import com.xinyi.rongli.identity.mapper.ViPhoneVerificationReceiptMapper;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 本人员工关联读取与 reconcile（I05，U02）。只返回粗粒度公开投影（无 sysUserId/名单/完整手机号/OpenID）。
 * reconcile 仅重试本账号最近 10 分钟内已确认的 WECHAT 回执（不接收任意手机号/目标员工、不重复消费 phoneCode），
 * 在 GLOBAL_DIRECTORY 锁内复用统一唯一性裁决；到期必须重新手机号授权（VI_PHONE_PROOF_REQUIRED）。
 */
@Service
public class EmployeeLinkService {

    private static final long RECONCILE_WINDOW_SECONDS = 600L;

    private final ViAccountMapper accountMapper;
    private final ViSessionMapper sessionMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final ViEmployeeBindingMapper bindingMapper;
    private final ViPhoneVerificationReceiptMapper receiptMapper;
    private final EmployeeDirectoryGuardMapper guardMapper;
    private final EmployeeAssociationService associationService;
    private final PhoneCrypto crypto;
    private final IdentityContextService contextService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public EmployeeLinkService(ViAccountMapper accountMapper, ViSessionMapper sessionMapper,
                               ViAccountSecurityMapper securityMapper, ViEmployeeBindingMapper bindingMapper,
                               ViPhoneVerificationReceiptMapper receiptMapper, EmployeeDirectoryGuardMapper guardMapper,
                               EmployeeAssociationService associationService, PhoneCrypto crypto,
                               IdentityContextService contextService, AuditService auditService,
                               OutboxService outboxService, PlatformTransactionManager txManager) {
        this.accountMapper = accountMapper;
        this.sessionMapper = sessionMapper;
        this.securityMapper = securityMapper;
        this.bindingMapper = bindingMapper;
        this.receiptMapper = receiptMapper;
        this.guardMapper = guardMapper;
        this.associationService = associationService;
        this.crypto = crypto;
        this.contextService = contextService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /** 本人关联公开投影：REVOKED 单独投影（LINK_REVOKED），其余按权威绑定/资格实时计算。 */
    public EmployeeLinkPublic readPublic(long accountId) {
        ViEmployeeBinding latest = bindingMapper.selectLatestByAccount(accountId);
        if (latest != null && "REVOKED".equals(latest.getStatus())) {
            boolean canReverify = latest.getAutoRebindBlocked() == null || latest.getAutoRebindBlocked() == 0;
            return new EmployeeLinkPublic("REVOKED", false, 0, "LINK_REVOKED", canReverify);
        }
        return contextService.compute(accountId).employeeLink();
    }

    /** 重试最近核验的自动关联（不重复消费 phoneCode）；窗口外要求重新授权。 */
    public EmployeeLinkPublic reconcile(long accountId, long sessionId, long expectedContextVersion) {
        return tx.execute(s -> {
            guardMapper.ensureGuard();
            guardMapper.lockGuard();
            ViSession session = sessionMapper.selectByIdForUpdate(sessionId);
            if (session == null || session.getAccountId() != accountId) {
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
            }
            Instant now = Instant.now();
            if (!SessionStatus.ACTIVE.name().equals(session.getStatus()) || session.getRevokedAt() != null
                    || session.getAbsoluteExpiresAt() == null || !session.getAbsoluteExpiresAt().isAfter(now)) {
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
            }
            var security = securityMapper.selectByAccountId(accountId);
            if (security == null || !Objects.equals(security.getSessionEpoch(), session.getSessionEpoch())) {
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
            }
            if (session.getContextVersion() != expectedContextVersion) {
                throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED, "身份或站点已切换，请刷新",
                        null, List.of(Map.of("resourceType", "VI_SESSION", "resourceId", Long.toString(sessionId),
                                "reasonCode", "CONTEXT_VERSION", "currentVersion", session.getContextVersion())), null);
            }
            ViAccount account = accountMapper.selectByIdForUpdate(accountId);
            if (account == null) {
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
            }
            ViPhoneVerificationReceipt receipt = receiptMapper.selectLatestVerifiedByAccount(accountId);
            if (receipt == null || receipt.getVerifiedAt() == null
                    || receipt.getVerifiedAt().isBefore(now.minusSeconds(RECONCILE_WINDOW_SECONDS))) {
                throw new ViException(ViErrorCode.VI_PHONE_PROOF_REQUIRED, "请重新进行微信手机号授权");
            }
            String phone = crypto.decrypt(receipt.getPhoneCipher());
            EmployeeLinkOutcome outcome = associationService.associate(account, account.getAppId(),
                    phone, receipt.getPhoneLookup(), now);
            receiptMapper.updateOutcome(receipt.getId(), outcome.name(), receipt.getVersion());
            boolean linked = outcome == EmployeeLinkOutcome.LINKED || outcome == EmployeeLinkOutcome.EXISTING_LINK;
            if (linked) {
                securityMapper.bumpPermissionVersion(accountId, now);
            }
            Actor actor = Actor.mini(accountId, session.getActiveRole(), session.getStationId(), sessionId,
                    session.getContextVersion(), null);
            auditService.recordSuccess(actor, "VI_ACCOUNT", accountId, "EMPLOYEE_LINK_RECONCILE", null,
                    Map.of("employeeLinkOutcome", outcome.name()), null);
            outboxService.publish("employee-reconcile:" + accountId + ":" + receipt.getId(), "VI_ACCOUNT", accountId,
                    "EMPLOYEE_LINK_RECONCILED", Map.of("employeeLinkOutcome", outcome.name()));
            return readPublic(accountId);
        });
    }
}