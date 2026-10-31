package com.xinyi.rongli.identity.service;

import com.xinyi.rongli.identity.domain.EmployeeLinkOutcome;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryReadMapper;
import com.xinyi.rongli.identity.mapper.ViEmployeeBindingMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 员工自动关联唯一性裁决（V6.4 §3/§4，单一安全事实源）。须在 GLOBAL_DIRECTORY 保护行锁内调用
 * （锁序之首由调用方持有）；按 DISTINCT sys_user 计数，唯一有效员工且无抢占/禁止重绑才建绑定。
 * 供 I02 绑定、I05 reconcile/后台/框架钩子共用，避免多处复制唯一性检查导致竞态口径不一。
 * 只写 vi_employee_binding；不写 sys_user、不签 ADMIN Token、不覆盖既有不同关联。
 */
@Service
public class EmployeeAssociationService {

    private final EmployeeDirectoryReadMapper directoryReadMapper;
    private final ViEmployeeBindingMapper bindingMapper;

    public EmployeeAssociationService(EmployeeDirectoryReadMapper directoryReadMapper,
                                      ViEmployeeBindingMapper bindingMapper) {
        this.directoryReadMapper = directoryReadMapper;
        this.bindingMapper = bindingMapper;
    }

    /** 目录锁内自动关联：唯一有效员工匹配则建绑定；重号/冲突/已绑/禁用记内部 outcome，不覆盖、不泄细节。 */
    public EmployeeLinkOutcome associate(ViAccount account, String appId, String phone, byte[] lookup, Instant now) {
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
}