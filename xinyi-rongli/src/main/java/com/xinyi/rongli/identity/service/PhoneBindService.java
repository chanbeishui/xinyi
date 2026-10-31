package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.domain.EmployeeLinkOutcome;
import com.xinyi.rongli.identity.domain.PhoneVerificationStatus;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import com.xinyi.rongli.identity.domain.ViPhoneVerificationReceipt;
import com.xinyi.rongli.identity.dto.EmployeeLinkPublic;
import com.xinyi.rongli.identity.dto.PhoneBindResult;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryReadMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViEmployeeBindingMapper;
import com.xinyi.rongli.identity.mapper.ViPhoneVerificationReceiptMapper;
import com.xinyi.rongli.identity.provider.IdentityProvider;
import com.xinyi.rongli.identity.provider.PhoneExchangeResult;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 手机号验证回执与员工自动关联（I02，V6.4 §2/§3/§4）。
 * 分离 wx.login code 与 phoneCode；专用 verificationRequestId 回执去重（不进通用幂等缓存、不缓存令牌）；
 * 一次性 phoneCode 只换取一次，处理中不重复消费，结果不明查回执或重新授权（UNKNOWN 不假成功）。
 * 手机号核验成功与员工匹配结果分开：重号/冲突/目录不可用不推翻手机号验证、不取消 OWNER。
 * 自动关联在 GLOBAL_DIRECTORY 保护行锁下串行（锁序：目录guard→sys_user→account→binding→audit/outbox），
 * 微信外部换取绝不在锁/事务内。SMS 不触发员工关联（本服务只接受 WECHAT 证明）。
 */
@Service
public class PhoneBindService {

    private static final long RECEIPT_WINDOW_SECONDS = 600L;
    private static final long LEASE_SECONDS = 30L;
    private static final Set<String> NEEDS_ADMIN_OUTCOMES = Set.of(
            EmployeeLinkOutcome.AMBIGUOUS_PHONE.name(), EmployeeLinkOutcome.USER_ALREADY_LINKED.name(),
            EmployeeLinkOutcome.ACCOUNT_ALREADY_LINKED.name(), EmployeeLinkOutcome.REBIND_BLOCKED.name(),
            EmployeeLinkOutcome.EMPLOYEE_UNAVAILABLE.name(), EmployeeLinkOutcome.DIRECTORY_UNAVAILABLE.name());
    private static final Set<String> RETRYABLE_OUTCOMES = Set.of(
            EmployeeLinkOutcome.NOT_ATTEMPTED.name(), EmployeeLinkOutcome.DIRECTORY_UNAVAILABLE.name());

    private final ViAccountMapper accountMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final ViPhoneVerificationReceiptMapper receiptMapper;
    private final ViEmployeeBindingMapper bindingMapper;
    private final EmployeeDirectoryGuardMapper guardMapper;
    private final EmployeeDirectoryReadMapper directoryReadMapper;
    private final IdentityProvider identityProvider;
    private final PhoneCrypto crypto;
    private final IdentityContextService contextService;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public PhoneBindService(ViAccountMapper accountMapper, ViAccountSecurityMapper securityMapper,
                            ViPhoneVerificationReceiptMapper receiptMapper, ViEmployeeBindingMapper bindingMapper,
                            EmployeeDirectoryGuardMapper guardMapper, EmployeeDirectoryReadMapper directoryReadMapper,
                            IdentityProvider identityProvider, PhoneCrypto crypto,
                            IdentityContextService contextService, AuditService auditService,
                            OutboxService outboxService, PlatformTransactionManager txManager) {
        this.accountMapper = accountMapper;
        this.securityMapper = securityMapper;
        this.receiptMapper = receiptMapper;
        this.bindingMapper = bindingMapper;
        this.guardMapper = guardMapper;
        this.directoryReadMapper = directoryReadMapper;
        this.identityProvider = identityProvider;
        this.crypto = crypto;
        this.contextService = contextService;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    private record Registration(ViPhoneVerificationReceipt receipt, boolean proceed) {
    }

    /** 手机号绑定主流程。principal 提供可信 accountId/sessionId（绝不从请求体接收身份）。 */
    public PhoneBindResult bind(long accountId, Long sessionId, String phoneCode,
                                long expectedAccountVersion, String requestId) {
        ViAccount account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        byte[] codeHash = sha256(phoneCode);
        // 幂等重试优先：同 requestId 已登记则直接返回既有回执（不重复消费、不受版本守卫阻断），
        // 响应丢失后按原 requestId 安全恢复（V6.4 §2）。版本守卫仅针对首次新请求。
        ViPhoneVerificationReceipt prior = receiptMapper.selectByRequest(accountId, requestId);
        if (prior != null) {
            if (!Arrays.equals(prior.getCodeHash(), codeHash)) {
                throw new ViException(ViErrorCode.VI_IDEMPOTENCY_CONFLICT, "相同核验编号对应了不同凭据");
            }
            return buildResult(prior, accountId, false);
        }
        if (account.getVersion() != expectedAccountVersion) {
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        }
        Registration reg = tx.execute(s -> register(accountId, sessionId, account.getAppId(), requestId, codeHash));
        if (!reg.proceed()) {
            // 并发同 requestId：唯一键裁决，返回既有回执，不重复消费外部凭据
            return buildResult(reg.receipt(), accountId, false);
        }
        // 外部换取在短事务/锁之外
        PhoneExchangeResult ext = identityProvider.exchangePhone(phoneCode);
        return tx.execute(s -> finalize(reg.receipt().getId(), accountId, account.getAppId(), ext));
    }

    private Registration register(long accountId, Long sessionId, String appId, String requestId, byte[] codeHash) {
        ViPhoneVerificationReceipt existing = receiptMapper.selectByRequest(accountId, requestId);
        if (existing != null) {
            if (!Arrays.equals(existing.getCodeHash(), codeHash)) {
                throw new ViException(ViErrorCode.VI_IDEMPOTENCY_CONFLICT, "相同核验编号对应了不同凭据");
            }
            return new Registration(existing, false);
        }
        ViPhoneVerificationReceipt byCode = receiptMapper.selectByCode(appId, codeHash);
        if (byCode != null && byCode.getAccountId() != accountId) {
            throw new ViException(ViErrorCode.VI_PHONE_PROOF_REUSED);
        }
        Instant now = Instant.now();
        ViPhoneVerificationReceipt r = new ViPhoneVerificationReceipt();
        r.setRequestId(requestId);
        r.setAppId(appId);
        r.setAccountId(accountId);
        r.setSessionId(sessionId);
        r.setCodeHash(codeHash);
        r.setStatus(PhoneVerificationStatus.PROCESSING.name());
        r.setExpiresAt(now.plusSeconds(RECEIPT_WINDOW_SECONDS));
        r.setEmployeeLinkOutcome(EmployeeLinkOutcome.NOT_ATTEMPTED.name());
        r.setLeaseUntil(now.plusSeconds(LEASE_SECONDS));
        try {
            receiptMapper.insert(r);
        } catch (DuplicateKeyException e) {
            // 并发同 requestId：唯一键裁决，重查既有回执，不重复消费
            ViPhoneVerificationReceipt again = receiptMapper.selectByRequest(accountId, requestId);
            if (again == null) {
                throw new ViException(ViErrorCode.VI_PHONE_PROOF_REUSED);
            }
            return new Registration(again, false);
        }
        return new Registration(r, true);
    }

    private PhoneBindResult finalize(long receiptId, long accountId, String appId, PhoneExchangeResult ext) {
        // 锁序之首：目录保护行（低频互斥，串行化所有自动关联）
        guardMapper.ensureGuard();
        guardMapper.lockGuard();
        ViPhoneVerificationReceipt receipt = receiptMapper.selectByIdForUpdate(receiptId);
        ViAccount account = accountMapper.selectByIdForUpdate(accountId);
        Instant now = Instant.now();

        if (ext.status() == PhoneVerificationStatus.VERIFIED) {
            String phone = ext.phone();
            byte[] lookup = crypto.lookup(phone);
            byte[] cipher = crypto.encrypt(phone);
            EmployeeLinkOutcome outcome = associate(account, appId, phone, lookup, now);
            receiptMapper.markVerified(receiptId, lookup, cipher, now, outcome.name(), now, receipt.getVersion());
            accountMapper.updatePhone(accountId, lookup, cipher, now, "WECHAT", account.getVersion());
            boolean linked = outcome == EmployeeLinkOutcome.LINKED || outcome == EmployeeLinkOutcome.EXISTING_LINK;
            if (linked) {
                securityMapper.bumpPermissionVersion(accountId, now);
            }
            Actor actor = Actor.mini(accountId, "OWNER", null, receipt.getSessionId(), null, null);
            auditService.recordSuccess(actor, "VI_ACCOUNT", accountId, "PHONE_VERIFIED", null,
                    Map.of("verificationStatus", "VERIFIED", "employeeLinkOutcome", outcome.name()), null);
            outboxService.publish("phone-verified:" + receiptId, "VI_ACCOUNT", accountId,
                    "PHONE_VERIFIED", Map.of("employeeLinkOutcome", outcome.name()));
            ViPhoneVerificationReceipt updated = receiptMapper.selectByRequest(accountId, receipt.getRequestId());
            return buildResult(updated, accountId, linked);
        }
        // FAILED / UNKNOWN：保留处理中/未知或失败，不开通 STATION，不假成功
        receiptMapper.markTerminal(receiptId, ext.status().name(),
                EmployeeLinkOutcome.NOT_ATTEMPTED.name(), now, receipt.getVersion());
        ViPhoneVerificationReceipt updated = receiptMapper.selectByRequest(accountId, receipt.getRequestId());
        return buildResult(updated, accountId, false);
    }

    /** 目录锁内自动关联：唯一有效员工匹配则建绑定；重号/冲突/已绑/禁用记内部 outcome，不覆盖、不泄细节。 */
    private EmployeeLinkOutcome associate(ViAccount account, String appId, String phone, byte[] lookup, Instant now) {
        if (account.getEmployeeAutoLinkBlocked() != null && account.getEmployeeAutoLinkBlocked() == 1) {
            return EmployeeLinkOutcome.REBIND_BLOCKED;
        }
        String normalized = PhoneCrypto.normalize(phone);
        List<Long> userIds = directoryReadMapper.selectActiveUserIdsByPhone(normalized);
        ViEmployeeBinding existing = bindingMapper.selectActiveByAccount(account.getId());
        if (existing != null) {
            if (userIds.size() == 1 && userIds.get(0).equals(existing.getSysUserId())) {
                return EmployeeLinkOutcome.EXISTING_LINK;
            }
            return EmployeeLinkOutcome.ACCOUNT_ALREADY_LINKED;
        }
        if (userIds.isEmpty()) {
            return EmployeeLinkOutcome.NO_MATCH;
        }
        if (userIds.size() > 1) {
            return EmployeeLinkOutcome.AMBIGUOUS_PHONE;
        }
        long sysUserId = userIds.get(0);
        if (bindingMapper.countActiveByAppUser(appId, sysUserId) > 0) {
            return EmployeeLinkOutcome.USER_ALREADY_LINKED;
        }
        Long authVersion = directoryReadMapper.selectUserAuthVersion(sysUserId);
        if (authVersion == null) {
            return EmployeeLinkOutcome.EMPLOYEE_UNAVAILABLE;
        }
        ViEmployeeBinding b = new ViEmployeeBinding();
        b.setAccountId(account.getId());
        b.setSysUserId(sysUserId);
        b.setBoundBy(null);
        b.setBoundAt(now);
        b.setPermissionVersion(0L);
        b.setAppId(appId);
        b.setSource("WECHAT_PHONE_MATCH");
        b.setStatus("ACTIVE");
        b.setPhoneLookup(lookup);
        b.setLastPhoneVerifiedAt(now);
        b.setEmployeeAuthVersion(authVersion);
        b.setAutoRebindBlocked(0);
        bindingMapper.insert(b);
        return EmployeeLinkOutcome.LINKED;
    }

    /** 查询本人手机号授权处理结果；VERIFIED 且关联可重试时安全重试关联（不重复消费外部凭据）。 */
    public PhoneBindResult queryReceipt(long accountId, String requestId) {
        ViPhoneVerificationReceipt receipt = receiptMapper.selectByRequest(accountId, requestId);
        if (receipt == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        if (PhoneVerificationStatus.VERIFIED.name().equals(receipt.getStatus())
                && RETRYABLE_OUTCOMES.contains(receipt.getEmployeeLinkOutcome())) {
            final long rid = receipt.getId();
            tx.execute(s -> {
                guardMapper.ensureGuard();
                guardMapper.lockGuard();
                ViPhoneVerificationReceipt r = receiptMapper.selectByIdForUpdate(rid);
                ViAccount account = accountMapper.selectByIdForUpdate(accountId);
                String phone = crypto.decrypt(r.getPhoneCipher());
                EmployeeLinkOutcome outcome = associate(account, account.getAppId(), phone, r.getPhoneLookup(), Instant.now());
                receiptMapper.updateOutcome(r.getId(), outcome.name(), r.getVersion());
                if (outcome == EmployeeLinkOutcome.LINKED || outcome == EmployeeLinkOutcome.EXISTING_LINK) {
                    securityMapper.bumpPermissionVersion(accountId, Instant.now());
                }
                return null;
            });
            receipt = receiptMapper.selectByRequest(accountId, requestId);
        }
        return buildResult(receipt, accountId, false);
    }

    private PhoneBindResult buildResult(ViPhoneVerificationReceipt receipt, long accountId, boolean requiresRefresh) {
        ViAccount account = accountMapper.selectById(accountId);
        boolean phoneBound = PhoneVerificationStatus.VERIFIED.name().equals(receipt.getStatus());
        String phoneMask = null;
        if (phoneBound && receipt.getPhoneCipher() != null) {
            phoneMask = PhoneCrypto.mask(crypto.decrypt(receipt.getPhoneCipher()));
        }
        EmployeeLinkPublic link = employeeLinkForOutcome(receipt.getEmployeeLinkOutcome(), accountId);
        return new PhoneBindResult(receipt.getRequestId(), receipt.getStatus(), phoneBound, phoneMask,
                account.getVersion(), link, requiresRefresh);
    }

    private EmployeeLinkPublic employeeLinkForOutcome(String outcome, long accountId) {
        if (EmployeeLinkOutcome.LINKED.name().equals(outcome) || EmployeeLinkOutcome.EXISTING_LINK.name().equals(outcome)) {
            return contextService.compute(accountId).employeeLink();
        }
        if (NEEDS_ADMIN_OUTCOMES.contains(outcome)) {
            return new EmployeeLinkPublic("NEEDS_ADMIN", false, 0, "CONTACT_ADMIN", true);
        }
        if (EmployeeLinkOutcome.PHONE_CHANGED.name().equals(outcome)) {
            return new EmployeeLinkPublic("NEEDS_ADMIN", false, 0, "PHONE_REVERIFY_REQUIRED", true);
        }
        // NO_MATCH / NOT_ATTEMPTED：按绑定权威态（无绑定即 UNLINKED）
        return contextService.compute(accountId).employeeLink();
    }

    private static byte[] sha256(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "凭据摘要计算失败", e);
        }
    }
}
