package com.xinyi.rongli.identity.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.ViAccount;
import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import com.xinyi.rongli.identity.dto.EmployeeLinkAdmin;
import com.xinyi.rongli.identity.dto.EmployeeLinkRecheckResult;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.EmployeeLinkAdminMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViEmployeeBindingMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台员工微信关联管理（I05，B36，ADMIN 域）。功能权限 + 部门数据范围（列表任职相交、详情/写覆盖全部任职）；
 * 目录 GLOBAL_DIRECTORY 锁序（guard→sys_user→account→binding→audit/outbox）；暂停/撤销/允许重核与 MINI
 * permission_version、审计、outbox 同事务。不直接覆盖不同关联、不签 ADMIN Token 给 MINI、不直接恢复 SUSPENDED/REVOKED。
 */
@Service
public class EmployeeLinkAdminService {

    private final EmployeeLinkAdminMapper adminMapper;
    private final ViEmployeeBindingMapper bindingMapper;
    private final ViAccountMapper accountMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final EmployeeDirectoryGuardMapper guardMapper;
    private final IDeptScopeService deptScope;
    private final BusinessActorResolver actorResolver;
    private final PhoneCrypto crypto;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public EmployeeLinkAdminService(EmployeeLinkAdminMapper adminMapper, ViEmployeeBindingMapper bindingMapper,
                                    ViAccountMapper accountMapper, ViAccountSecurityMapper securityMapper,
                                    EmployeeDirectoryGuardMapper guardMapper, IDeptScopeService deptScope,
                                    BusinessActorResolver actorResolver, PhoneCrypto crypto,
                                    AuditService auditService, OutboxService outboxService,
                                    PlatformTransactionManager txManager) {
        this.adminMapper = adminMapper;
        this.bindingMapper = bindingMapper;
        this.accountMapper = accountMapper;
        this.securityMapper = securityMapper;
        this.guardMapper = guardMapper;
        this.deptScope = deptScope;
        this.actorResolver = actorResolver;
        this.crypto = crypto;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public List<EmployeeLinkAdmin> list() {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:employeebinding:list");
        DeptScopeResult scope = deptScope.resolve(SecurityUtils.getLoginUser(), "biz:employeebinding:list");
        Set<Long> deptIds = scope.isAllData() ? null : scope.getDeptIds();
        List<EmployeeLinkAdmin> out = new ArrayList<>();
        for (Map<String, Object> row : adminMapper.selectBindings(deptIds)) {
            out.add(toDto(row));
        }
        return out;
    }

    public EmployeeLinkAdmin detail(long bindingId) {
        actorResolver.requireAdmin();
        requirePerm("biz:employeebinding:query");
        Map<String, Object> row = adminMapper.selectAdminById(bindingId);
        if (row == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        checkFullAccess(num(row.get("sys_user_id")));
        return toDto(row);
    }

    public EmployeeLinkAdmin suspend(long bindingId, long expectedVersion, String reason) {
        return mutate(bindingId, expectedVersion, reason, "SUSPEND");
    }

    public EmployeeLinkAdmin revoke(long bindingId, long expectedVersion, String reason) {
        return mutate(bindingId, expectedVersion, reason, "REVOKE");
    }

    public EmployeeLinkRecheckResult allowRematch(long bindingId, long expectedVersion, String reason) {
        EmployeeLinkAdmin binding = mutate(bindingId, expectedVersion, reason, "ALLOW_REMATCH");
        return new EmployeeLinkRecheckResult(binding, true);
    }

    private EmployeeLinkAdmin mutate(long bindingId, long expectedVersion, String reason, String action) {
        Actor actor = actorResolver.requireAdmin();
        String perm = switch (action) {
            case "SUSPEND" -> "biz:employeebinding:suspend";
            case "REVOKE" -> "biz:employeebinding:revoke";
            default -> "biz:employeebinding:rematch";
        };
        requirePerm(perm);
        ViEmployeeBinding pre = bindingMapper.selectById(bindingId);
        if (pre == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        checkFullAccess(pre.getSysUserId());
        return tx.execute(s -> {
            guardMapper.ensureGuard();
            guardMapper.lockGuard();
            ViEmployeeBinding b = bindingMapper.selectByIdForUpdate(bindingId);
            if (b == null) {
                throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
            }
            ViAccount account = accountMapper.selectByIdForUpdate(b.getAccountId());
            Instant now = Instant.now();
            int updated;
            switch (action) {
                case "SUSPEND" -> updated = bindingMapper.suspend(bindingId, reason, expectedVersion);
                case "REVOKE" -> {
                    updated = bindingMapper.revoke(bindingId, actor.sysUserId(), reason, now, expectedVersion);
                    if (updated == 1) {
                        accountMapper.updateAutoLinkBlocked(account.getId(), 1, account.getVersion());
                    }
                }
                default -> {
                    updated = bindingMapper.allowRematch(bindingId, expectedVersion);
                    if (updated == 1) {
                        accountMapper.updateAutoLinkBlocked(account.getId(), 0, account.getVersion());
                    }
                }
            }
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "关联已变化，请刷新后重试");
            }
            // 暂停/撤销/允许重核都改变 MINI 工作能力或重绑资格 → 递增权限版本（同事务）
            securityMapper.bumpPermissionVersion(account.getId(), now);
            auditService.recordSuccess(actor, "VI_EMPLOYEE_BINDING", bindingId, "EMPLOYEE_LINK_" + action,
                    null, Map.of("status", action, "sysUserId", b.getSysUserId()), reason);
            outboxService.publish("employee-link-" + action.toLowerCase() + ":" + bindingId, "VI_EMPLOYEE_BINDING",
                    bindingId, "EMPLOYEE_LINK_" + action, Map.of("sysUserId", b.getSysUserId()));
            return toDto(adminMapper.selectAdminById(bindingId));
        });
    }

    private void requirePerm(String perm) {
        LoginUser lu = SecurityUtils.getLoginUser();
        if (lu == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (!SecurityUtils.isAdmin() && (lu.getPermissions() == null || !lu.getPermissions().contains(perm))) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "没有执行此操作的权限");
        }
    }

    private void checkFullAccess(long sysUserId) {
        try {
            deptScope.checkUserFullAccess(sysUserId);
        } catch (ServiceException e) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, e.getMessage());
        }
    }

    private EmployeeLinkAdmin toDto(Map<String, Object> row) {
        byte[] cipher = (byte[]) row.get("phone_cipher");
        String phoneMask = cipher == null ? null : PhoneCrypto.mask(crypto.decrypt(cipher));
        return new EmployeeLinkAdmin(
                ViId.external(num(row.get("id"))),
                ViId.external(num(row.get("account_id"))),
                (String) row.get("app_id"),
                ViId.external(num(row.get("sys_user_id"))),
                (String) row.get("nick_name"),
                phoneMask,
                maskOpenId((String) row.get("open_id")),
                (String) row.get("status"),
                (String) row.get("source"),
                iso(row.get("bound_at")),
                iso(row.get("last_phone_verified_at")),
                num(row.get("auto_rebind_blocked")) == 1,
                num(row.get("version")));
    }

    private static String maskOpenId(String openId) {
        if (openId == null || openId.length() < 8) {
            return "****";
        }
        return openId.substring(0, 4) + "****" + openId.substring(openId.length() - 4);
    }

    private static long num(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    private static String iso(Object v) {
        Instant i = null;
        if (v instanceof Instant inst) {
            i = inst;
        } else if (v instanceof Timestamp t) {
            i = t.toInstant();
        } else if (v instanceof LocalDateTime ldt) {
            i = ldt.toInstant(ZoneOffset.UTC);
        }
        return i == null ? null : i.toString();
    }
}