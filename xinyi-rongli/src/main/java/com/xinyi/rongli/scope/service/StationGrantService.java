package com.xinyi.rongli.scope.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.domain.ViEmployeeBinding;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.ViAccountSecurityMapper;
import com.xinyi.rongli.identity.mapper.ViEmployeeBindingMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.scope.domain.ViStationGrantSet;
import com.xinyi.rongli.scope.dto.AuthorizedStation;
import com.xinyi.rongli.scope.dto.StationGrantView;
import com.xinyi.rongli.scope.mapper.ScopeAuthMapper;
import com.xinyi.rongli.scope.mapper.StationGrantMapper;
import com.xinyi.rongli.scope.mapper.StationGrantSetMapper;
import com.xinyi.rongli.scope.mapper.StationScopeReadMapper;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台员工站点预配置与组合权限（I06，V6.4 §5）。直接按 sys_user 配置（不要求 accountId/openid，员工可未微信登录）；
 * grant_set 集合版本含空集合（防 ABA）；PUT 原子更新集合+子授权增减+框架 auth_version+已关联 MINI permission_version+审计+outbox 同事务；
 * 部门数据范围（员工覆盖全部任职、站点 dept 交集，批量越权全拒绝）；零站点可保存撤权，不自动回行政站点；旧会话实时拒绝已撤权操作。
 */
@Service
public class StationGrantService {

    private final StationGrantSetMapper setMapper;
    private final StationGrantMapper grantMapper;
    private final StationScopeReadMapper readMapper;
    private final ScopeAuthMapper authMapper;
    private final EmployeeDirectoryGuardMapper guardMapper;
    private final ViEmployeeBindingMapper bindingMapper;
    private final ViAccountSecurityMapper securityMapper;
    private final IDeptScopeService deptScope;
    private final BusinessActorResolver actorResolver;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public StationGrantService(StationGrantSetMapper setMapper, StationGrantMapper grantMapper,
                               StationScopeReadMapper readMapper, ScopeAuthMapper authMapper,
                               EmployeeDirectoryGuardMapper guardMapper, ViEmployeeBindingMapper bindingMapper,
                               ViAccountSecurityMapper securityMapper, IDeptScopeService deptScope,
                               BusinessActorResolver actorResolver, AuditService auditService,
                               OutboxService outboxService, PlatformTransactionManager txManager) {
        this.setMapper = setMapper;
        this.grantMapper = grantMapper;
        this.readMapper = readMapper;
        this.authMapper = authMapper;
        this.guardMapper = guardMapper;
        this.bindingMapper = bindingMapper;
        this.securityMapper = securityMapper;
        this.deptScope = deptScope;
        this.actorResolver = actorResolver;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public StationGrantView getView(long sysUserId) {
        ViStationGrantSet set = setMapper.selectBySysUser(sysUserId);
        List<Long> enabled = grantMapper.selectEnabledStationIds(sysUserId);
        List<String> ids = new ArrayList<>();
        for (Long id : enabled) {
            ids.add(ViId.external(id));
        }
        return new StationGrantView(
                ViId.external(sysUserId),
                set == null || set.getOwnStationId() == null ? null : ViId.external(set.getOwnStationId()),
                ids,
                set == null ? 0L : set.getVersion(),
                wechatLinkStatus(sysUserId),
                set == null || set.getUpdatedAt() == null ? null : set.getUpdatedAt().toString());
    }

    public List<StationGrantView> list(int pageNum, int pageSize) {
        actorResolver.requireAdmin();
        requirePerm("biz:stationgrant:list");
        DeptScopeResult scope = deptScope.resolve(SecurityUtils.getLoginUser(), "biz:stationgrant:list");
        Set<Long> deptIds = scope.isAllData() ? null : scope.getDeptIds();
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<StationGrantView> out = new ArrayList<>();
        for (Long uid : readMapper.selectEmployeeIds(deptIds, offset, pageSize)) {
            out.add(getView(uid));
        }
        return out;
    }

    public StationGrantView put(long sysUserId, Long ownStationId, List<Long> authorizedStationIds,
                                long expectedVersion, String reason) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:stationgrant:edit");
        checkFullAccess(sysUserId);
        Set<Long> desired = new HashSet<>(authorizedStationIds);
        validateStations(desired, ownStationId);
        return tx.execute(s -> {
            guardMapper.ensureGuard();
            guardMapper.lockGuard();
            ViStationGrantSet set = setMapper.selectBySysUserForUpdate(sysUserId);
            long setId;
            long baseVersion;
            if (set == null) {
                if (expectedVersion != 0) {
                    throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "集合已变化，请刷新后重试");
                }
                ViStationGrantSet fresh = new ViStationGrantSet();
                fresh.setSysUserId(sysUserId);
                fresh.setOwnStationId(ownStationId);
                fresh.setChangedBy(actor.sysUserId());
                fresh.setReason(reason);
                setMapper.insert(fresh);
                setId = fresh.getId();
                baseVersion = 0;
            } else {
                setId = set.getId();
                baseVersion = set.getVersion();
                if (baseVersion != expectedVersion) {
                    throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "集合已变化，请刷新后重试");
                }
            }
            int updated = setMapper.casUpdate(setId, ownStationId, actor.sysUserId(), reason, baseVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "集合已变化，请刷新后重试");
            }
            reconcileGrants(sysUserId, desired, actor.sysUserId(), reason);
            authMapper.bumpAuthVersion(sysUserId);
            ViEmployeeBinding binding = bindingMapper.selectActiveBySysUser(sysUserId);
            if (binding != null) {
                securityMapper.bumpPermissionVersion(binding.getAccountId(), Instant.now());
            }
            Map<String, Object> after = new LinkedHashMap<>();
            after.put("ownStationId", ownStationId == null ? null : ViId.external(ownStationId));
            after.put("authorizedStationIds", authorizedStationIds);
            after.put("version", baseVersion + 1);
            auditService.recordSuccess(actor, "VI_STATION_GRANT_SET", sysUserId, "STATION_GRANT_UPDATE",
                    null, after, reason);
            outboxService.publish("station-grant:" + sysUserId + ":" + (baseVersion + 1), "VI_STATION_GRANT_SET",
                    sysUserId, "STATION_GRANTS_CHANGED", Map.of("version", baseVersion + 1));
            return getView(sysUserId);
        });
    }

    /** API_094：本人员工获授权站点（MINI）。无绑定返回空；仅启用站点。 */
    public List<AuthorizedStation> authorizedStations(long accountId) {
        ViEmployeeBinding binding = bindingMapper.selectActiveByAccount(accountId);
        if (binding == null) {
            return List.of();
        }
        List<Long> ids = grantMapper.selectEnabledStationIds(binding.getSysUserId());
        if (ids.isEmpty()) {
            return List.of();
        }
        List<AuthorizedStation> out = new ArrayList<>();
        for (Map<String, Object> row : readMapper.selectStationsByIds(ids)) {
            out.add(toStation(row));
        }
        return out;
    }

    private void reconcileGrants(long sysUserId, Set<Long> desired, long actorId, String reason) {
        Map<Long, Integer> existing = new HashMap<>();
        for (Map<String, Object> row : grantMapper.selectBySysUser(sysUserId)) {
            existing.put(((Number) row.get("station_id")).longValue(), ((Number) row.get("enabled")).intValue());
        }
        for (Map.Entry<Long, Integer> e : existing.entrySet()) {
            boolean want = desired.contains(e.getKey());
            int wantEnabled = want ? 1 : 0;
            if (e.getValue() != wantEnabled) {
                grantMapper.setEnabled(sysUserId, e.getKey(), wantEnabled, actorId, reason);
            }
        }
        for (Long id : desired) {
            if (!existing.containsKey(id)) {
                grantMapper.insert(sysUserId, id, actorId, reason);
            }
        }
    }

    private void validateStations(Set<Long> desired, Long ownStationId) {
        Set<Long> all = new HashSet<>(desired);
        if (ownStationId != null) {
            all.add(ownStationId);
        }
        if (all.isEmpty()) {
            return;
        }
        List<Map<String, Object>> rows = readMapper.selectExistingStations(all);
        if (rows.size() != all.size()) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "存在不存在的站点");
        }
        DeptScopeResult scope = deptScope.resolve(SecurityUtils.getLoginUser(), "biz:stationgrant:edit");
        for (Map<String, Object> row : rows) {
            if (((Number) row.get("enabled")).intValue() != 1) {
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "存在已停用站点");
            }
            Object dept = row.get("dept_id");
            if (!scope.isAllData() && dept != null && !scope.getDeptIds().contains(((Number) dept).longValue())) {
                throw new ViException(ViErrorCode.VI_FORBIDDEN, "批量包含越权站点，整体拒绝");
            }
        }
    }

    private String wechatLinkStatus(long sysUserId) {
        ViEmployeeBinding latest = bindingMapper.selectLatestBySysUser(sysUserId);
        if (latest == null) {
            return "NOT_LOGGED_IN_OR_UNLINKED";
        }
        return switch (latest.getStatus()) {
            case "ACTIVE" -> "LINKED";
            case "SUSPENDED" -> "SUSPENDED";
            default -> "REVOKED";
        };
    }

    private AuthorizedStation toStation(Map<String, Object> row) {
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("lng", row.get("lng"));
        location.put("lat", row.get("lat"));
        location.put("coordinateSystem", "GCJ02");
        List<String> codes = parseCodes(row.get("supported_vehicle_codes"));
        Object bh = row.get("business_hours");
        String businessHours = bh == null ? null : (bh instanceof String str ? str : ViJson.write(bh));
        return new AuthorizedStation(
                ViId.external(((Number) row.get("id")).longValue()),
                (String) row.get("name"),
                (String) row.get("address"),
                location,
                null,
                ((Number) row.get("enabled")).intValue() == 1,
                codes,
                businessHours,
                row.get("service_radius_meter") == null ? 0 : ((Number) row.get("service_radius_meter")).intValue(),
                ((Number) row.get("version")).longValue());
    }

    @SuppressWarnings("unchecked")
    private List<String> parseCodes(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof List<?> list) {
            return (List<String>) list;
        }
        return ViJson.read(String.valueOf(raw), List.class);
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
}