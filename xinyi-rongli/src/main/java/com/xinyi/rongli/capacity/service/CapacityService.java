package com.xinyi.rongli.capacity.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.capacity.domain.ViQuotaSlot;
import com.xinyi.rongli.capacity.dto.*;
import com.xinyi.rongli.capacity.mapper.ViQuotaSlotMapper;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.scope.service.EmployeeStationAccessService;
import com.xinyi.rongli.station.domain.ViStation;
import com.xinyi.rongli.station.mapper.ViStationMapper;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 每站双额度：接车区间不代表到站时间；调整只改总量/停约，不改历史占用。 */
@Service
public class CapacityService {
    private final ViQuotaSlotMapper slots;
    private final ViStationMapper stations;
    private final BusinessActorResolver actors;
    private final EmployeeStationAccessService employeeAccess;
    private final IDeptScopeService deptScope;
    private final EmployeeDirectoryGuardMapper directory;
    private final IdempotencyService idempotency;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    public CapacityService(ViQuotaSlotMapper slots, ViStationMapper stations, BusinessActorResolver actors,
                           EmployeeStationAccessService employeeAccess, IDeptScopeService deptScope,
                           EmployeeDirectoryGuardMapper directory, IdempotencyService idempotency,
                           AuditService audit, OutboxService outbox, PlatformTransactionManager manager) {
        this.slots = slots;
        this.stations = stations;
        this.actors = actors;
        this.employeeAccess = employeeAccess;
        this.deptScope = deptScope;
        this.directory = directory;
        this.idempotency = idempotency;
        this.audit = audit;
        this.outbox = outbox;
        this.tx = new TransactionTemplate(manager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Page(List<CapacityView> rows, long total) { }

    public Page list(CapacityQuery query, boolean mini) {
        if (query.quotaType() != null) requireType(query.quotaType());
        if (query.proposedTotal() != null && (query.proposedTotal() < 0 || query.slotId() == null)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "影响预览须指定时段及非负目标额度");
        }
        Set<Long> deptIds = null;
        Long stationId = null;
        if (mini) {
            stationId = employeeAccess.require("biz:capacity:list");
            if (query.stationId() != null && !stationId.equals(query.stationId())) {
                throw new ViException(ViErrorCode.VI_FORBIDDEN);
            }
        } else {
            actors.requireAdmin();
            deptIds = adminScope("biz:capacity:list");
        }
        int offset = Math.toIntExact(Math.min(Integer.MAX_VALUE, (long) (query.pageNum() - 1) * query.pageSize()));
        List<CapacityView> rows = slots.selectList(query, deptIds, stationId, offset).stream()
                .map(row -> view(row, query.proposedTotal())).toList();
        if (query.slotId() != null && rows.isEmpty()) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return new Page(rows, slots.countList(query, deptIds, stationId));
    }

    public CapacityView create(CapacityCreate input, String key) {
        Actor actor = actors.requireAdmin();
        reason(input.reason());
        requireType(input.quotaType());
        long stationId = id(input.stationId());
        Instant start = utc(input.startAt()), end = utc(input.endAt());
        if (!start.isBefore(end) || input.total() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "仅能创建未来有效区间及非负额度");
        }
        try {
            return tx.execute(status -> {
                directory.ensureGuard();
                directory.lockGuard();
                ViStation station = stations.selectByIdForUpdate(stationId);
                requireAdminStation(station, "biz:capacity:edit");
                return idempotency.execute(IdempotencyService.subjectKey(actor), "POST /api/admin/capacity", key,
                        ViJson.write(input), true, CapacityView.class, () -> {
                    if (!start.isAfter(Instant.now())) throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "时段已经开始");
                    if (station.getEnabled() != 1) {
                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "停用站点不能新增供给时段");
                    }
                    ViQuotaSlot row = new ViQuotaSlot();
                    row.setStationId(stationId);
                    row.setQuotaType(input.quotaType());
                    row.setStartsAt(start);
                    row.setEndsAt(end);
                    row.setTotalQuota(input.total());
                    if (slots.countOverlap(row) != 0) {
                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "本站点同类时段重叠，请调整原时段");
                    }
                    slots.insert(row);
                    audit.recordSuccess(actor, "VI_QUOTA_SLOT", row.getId(), "CAPACITY_CREATE", null,
                            Map.of("stationId", stationId, "quotaType", input.quotaType(), "total", input.total()), input.reason());
                    outbox.publish("capacity-create:" + row.getId(), "VI_QUOTA_SLOT", row.getId(), "CAPACITY_CREATED", Map.of());
                    return view(slots.selectById(row.getId()), null);
                });
            });
        } catch (ViException e) {
            rejected(actor, 0L, e);
            throw e;
        }
    }

    public CapacityView change(long id, CapacityChange input, String key, boolean mini) {
        Actor actor = mini ? actors.requireMini() : actors.requireAdmin();
        reason(input.reason());
        if (input.total() < 0 || input.expectedVersion() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
        try {
            return tx.execute(status -> {
                directory.ensureGuard();
                directory.lockGuard();
                ViQuotaSlot initial = slots.selectById(id);
                if (initial == null || initial.getStationId() == null) {
                    throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                }
                // 固定目录→站点→额度行锁序，创建和调容不会与迁部门/站点撤权交叉绕过。
                ViStation station = stations.selectByIdForUpdate(initial.getStationId());
                if (mini) {
                    if (employeeAccess.require("biz:capacity:edit") != initial.getStationId().longValue()) {
                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                    }
                } else {
                    requireAdminStation(station, "biz:capacity:edit");
                }
                ViQuotaSlot row = slots.lockById(id);
                if (row == null || !row.getStationId().equals(initial.getStationId())) {
                    throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
                }
                String action = (mini ? "PUT /api/app/station/capacity/" : "PUT /api/admin/capacity/") + id;
                return idempotency.execute(IdempotencyService.subjectKey(actor), action, key, ViJson.write(input), true,
                        CapacityView.class, () -> {
                    if (row.getVersion() != input.expectedVersion()) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
                    if (!row.getStartsAt().isAfter(Instant.now())) {
                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "已经开始的时段不能调整");
                    }
                    if (used(row) > input.total()) {
                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "总额度不能小于预占、正式预约及已消耗之和");
                    }
                    if (slots.change(id, input.total(), input.enabled(), input.expectedVersion()) != 1) {
                        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "额度或时间已变化，请重新预览");
                    }
                    audit.recordSuccess(actor, "VI_QUOTA_SLOT", id, "CAPACITY_CHANGE",
                            Map.of("total", row.getTotalQuota(), "enabled", row.isEnabled()),
                            Map.of("total", input.total(), "enabled", input.enabled() == null ? row.isEnabled() : input.enabled()), input.reason());
                    outbox.publish("capacity-change:" + id + ":" + (input.expectedVersion() + 1),
                            "VI_QUOTA_SLOT", id, "CAPACITY_CHANGED", Map.of());
                    return view(slots.selectById(id), null);
                });
            });
        } catch (ViException e) {
            rejected(actor, id, e);
            throw e;
        }
    }

    private void requireAdminStation(ViStation station, String permission) {
        Set<Long> ids = adminScope(permission);
        if (station == null || (ids != null && !ids.contains(station.getDeptId()))) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
    }

    private Set<Long> adminScope(String permission) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null || (!SecurityUtils.isAdmin() && (user.getPermissions() == null || !user.getPermissions().contains(permission)))) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        }
        DeptScopeResult scope = deptScope.resolve(user, permission);
        return scope.isAllData() ? null : scope.getDeptIds();
    }

    private void rejected(Actor actor, long id, ViException error) {
        audit.recordFailure(actor, "VI_QUOTA_SLOT", id, "CAPACITY_REJECTED", null,
                Map.of("errorCode", error.getErrorCode().name()), "容量操作拒绝，未修改预约或占用");
    }

    private static long used(ViQuotaSlot row) {
        return (long) row.getHeldQuota() + row.getConfirmedQuota() + row.getConsumedQuota();
    }

    private CapacityView view(ViQuotaSlot row, Integer proposed) {
        int available = row.isEnabled() ? Math.toIntExact(row.getTotalQuota() - used(row)) : 0;
        return new CapacityView(ViId.external(row.getId()), ViId.external(row.getStationId()), row.getQuotaType(),
                row.getStartsAt().toString(), row.getEndsAt().toString(), row.getTotalQuota(), row.getHeldQuota(),
                row.getConfirmedQuota(), row.getConsumedQuota(), available, row.isEnabled(), row.getVersion(), proposed,
                proposed == null ? null : proposed >= used(row) && row.getStartsAt().isAfter(Instant.now()));
    }

    private static void reason(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请填写500字以内的变更原因");
        }
    }

    private static void requireType(String type) {
        if (!Set.of("PICKUP", "STATION_RECEIVE").contains(type == null ? "" : type)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "额度类型须为接车或站点接收");
        }
    }

    private static long id(String text) {
        try { return ViId.parse(text); }
        catch (RuntimeException e) { throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "站点ID无效"); }
    }

    private static Instant utc(String text) {
        try {
            if (text == null || !text.endsWith("Z")) throw new IllegalArgumentException();
            Instant value = Instant.parse(text);
            if (value.getNano() % 1_000_000 != 0
                    || value.isAfter(Instant.parse("9999-12-31T23:59:59.999Z"))
                    || value.isBefore(Instant.parse("1000-01-01T00:00:00Z"))) throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "时间须为UTC且最多毫秒精度");
        }
    }
}
