package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryReadMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViEmployeeBindingMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.system.service.EmployeeDirectoryChangeListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 * 框架员工目录变更钩子（I05，V6.4 §4/§6）。在框架受控写事务提交后被调用，于 GLOBAL_DIRECTORY 锁内
 * 重查最新 sys_user 状态/手机号：员工停用/删除或换号→暂停旧绑定工作能力并递增 MINI permission_version（同事务审计/outbox）。
 * 不自动恢复 SUSPENDED（须本人重核/后台处理）；不写 sys_user；实时 fail-closed 校验兜底（IdentityContextService）。
 * 独立事务（提交后回调，无嵌套锁反转）；锁序 guard→account→binding→audit/outbox。
 */
@Service
public class EmployeeDirectoryHookService implements EmployeeDirectoryChangeListener {

    private final ViEmployeeBindingMapper bindingMapper;
    private final ViAccountMapper accountMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final EmployeeDirectoryGuardMapper guardMapper;
    private final EmployeeDirectoryReadMapper directoryReadMapper;
    private final PhoneCrypto crypto;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public EmployeeDirectoryHookService(ViEmployeeBindingMapper bindingMapper, ViAccountMapper accountMapper,
                                        ViAccountSecurityMapper securityMapper, EmployeeDirectoryGuardMapper guardMapper,
                                        EmployeeDirectoryReadMapper directoryReadMapper, PhoneCrypto crypto,
                                        AuditService auditService, OutboxService outboxService,
                                        PlatformTransactionManager txManager) {
        this.bindingMapper = bindingMapper;
        this.accountMapper = accountMapper;
        this.securityMapper = securityMapper;
        this.guardMapper = guardMapper;
        this.directoryReadMapper = directoryReadMapper;
        this.crypto = crypto;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void onDirectoryChanged(Long userId, String changeType) {
        if (userId == null) {
            return;
        }
        tx.execute(s -> {
            guardMapper.ensureGuard();
            guardMapper.lockGuard();
            ViEmployeeBinding binding = bindingMapper.selectActiveBySysUser(userId);
            if (binding == null || !"ACTIVE".equals(binding.getStatus())) {
                return null;
            }
            boolean active = directoryReadMapper.countActiveUserById(userId) > 0;
            String phone = directoryReadMapper.selectUserPhone(userId);
            byte[] currentLookup = phone == null ? null : crypto.lookup(phone);
            boolean phoneMismatch = binding.getPhoneLookup() != null
                    && !Arrays.equals(binding.getPhoneLookup(), currentLookup);
            boolean shouldSuspend = !active || ("PHONE_CHANGED".equals(changeType) && phoneMismatch);
            if (!shouldSuspend) {
                return null;
            }
            ViAccount account = accountMapper.selectByIdForUpdate(binding.getAccountId());
            Instant now = Instant.now();
            String reason = !active ? "EMPLOYEE_INACTIVE" : "PHONE_CHANGED";
            int updated = bindingMapper.suspend(binding.getId(), reason, binding.getVersion());
            if (updated == 1) {
                securityMapper.bumpPermissionVersion(account.getId(), now);
                auditService.recordSuccess(Actor.system(), "VI_EMPLOYEE_BINDING", binding.getId(),
                        "DIRECTORY_CHANGE_SUSPEND", null, Map.of("changeType", changeType, "sysUserId", userId), reason);
                outboxService.publish("directory-suspend:" + binding.getId() + ":" + changeType, "VI_EMPLOYEE_BINDING",
                        binding.getId(), "EMPLOYEE_LINK_SUSPENDED", Map.of("sysUserId", userId, "reason", reason));
            }
            return null;
        });
    }
}