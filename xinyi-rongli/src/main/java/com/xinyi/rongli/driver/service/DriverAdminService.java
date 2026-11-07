package com.xinyi.rongli.driver.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.dto.DriverRequests.*;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.integration.service.*;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.refund.dto.RefundRequests.Action;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 代驾档案按后台明确指定的公司归属；资料查询、跨公司转移、行政启停及历史订单权限各自校验。 */
@Service
public class DriverAdminService {
    public record Receipt(long driverId) {}

    private final DriverMapper mapper;
    private final IntegrationAuthority authority;
    private final DriverNewAssignmentPolicy policy;
    private final PhoneCrypto phones;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    public DriverAdminService(
            DriverMapper mapper,
            IntegrationAuthority authority,
            DriverNewAssignmentPolicy policy,
            PhoneCrypto phones,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.authority = authority;
        this.policy = policy;
        this.phones = phones;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public Map<String, Object> list(Query query) {
        Actor actor = authority.actor();
        return audited(actor, 0, "DRIVER_LIST", () -> tx.execute(status -> readList(query)));
    }

    private Map<String, Object> readList(Query query) {
        authority.lockAuthority();
        Set<Long> scope = authority.scope("biz:driver:list");
        byte[] phone = null;
        Query safe = query;
        if (query.keyword() != null && query.keyword().matches("1[0-9]{10}")) {
            phone = phones.lookup(query.keyword());
            safe =
                    new Query(
                            query.pageNum(),
                            query.pageSize(),
                            null,
                            query.status(),
                            query.sort(),
                            query.ownerDeptId());
        }
        Search search = new Search(safe, phone);
        var rows = mapper.list(scope, search, (long) (query.pageNum() - 1) * query.pageSize());
        return Map.of(
                "code",
                200,
                "msg",
                "操作成功",
                "rows",
                rows.stream().map(this::view).toList(),
                "total",
                mapper.count(scope, search));
    }

    public Map<String, Object> get(long id) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "DRIVER_QUERY",
                () ->
                        tx.execute(
                                status -> {
                                    authority.lockAuthority();
                                    return view(record(id, "biz:driver:query", false));
                                }));
    }

    public Map<String, Object> assignments(long id, Query query) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "DRIVER_ASSIGNMENTS",
                () -> tx.execute(status -> readAssignments(id, query)));
    }

    private Map<String, Object> readAssignments(long id, Query query) {
        authority.lockAuthority();
        DriverProfile row = record(id, "biz:driver:query", false);
        Set<Long> scope = authority.scope("biz:assignment:list");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (var item :
                mapper.assignments(
                        id, scope, query, (long) (query.pageNum() - 1) * query.pageSize())) {
            Map<String, Object> projection = new LinkedHashMap<>();
            projection.put("assignmentId", item.get("id").toString());
            projection.put("driverId", Long.toString(id));
            projection.put("orderNo", item.get("order_no"));
            projection.put("acceptedAt", item.get("accepted_iso"));
            projection.put("endedAt", item.get("ended_iso"));
            projection.put("endReason", item.get("end_reason"));
            projection.put("custody", item.get("custody_type"));
            projection.put("released", item.get("ended_iso") != null);
            rows.add(projection);
        }
        return Map.of(
                "code",
                200,
                "msg",
                "操作成功",
                "rows",
                rows,
                "total",
                mapper.assignmentCount(id, scope, query));
    }

    public Map<String, Object> maintain(long id, Maintenance request, String key) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "DRIVER_EDIT",
                () -> tx.execute(status -> recordMaintenance(id, request, key, actor)));
    }

    private Map<String, Object> recordMaintenance(
            long id, Maintenance request, String key, Actor actor) {
        authority.lockAuthority();
        DriverProfile row = record(id, "biz:driver:edit", true);
        Receipt receipt =
                idem.execute(
                        "ADMIN:" + actor.sysUserId(),
                        "DRIVER_EDIT:" + id,
                        key,
                        ViJson.write(request),
                        true,
                        Receipt.class,
                        () -> applyMaintenance(row, request, actor));
        DriverProfile current = record(receipt.driverId(), "biz:driver:edit", false);
        return view(current);
    }

    /** 原归属和目标归属分别验证；NULL必须显式全局认领，跨公司转移不能搬走尚有责任的司机。 */
    private Receipt applyMaintenance(DriverProfile row, Maintenance request, Actor actor) {
        if (row.getVersion() != request.expectedVersion()) conflict();
        Long original = row.getOwnerDeptId();
        boolean transferred =
                request.ownerPresent() && !Objects.equals(original, request.ownerDeptId());
        if (request.ownerPresent()) {
            Set<Long> scope = authority.scope("biz:driver:assigncompany");
            requireOwner(row, scope);
            if (scope != null && !scope.contains(request.ownerDeptId())) forbidden();
            Set<Long> editScope = authority.scope("biz:driver:edit");
            if (editScope != null && !editScope.contains(request.ownerDeptId())) forbidden();
            requireCompany(request.ownerDeptId());
            if (transferred && mapper.busy(row.getId()) > 0)
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "存在任务占用、待响应邀约或未结束承接，不能转移归属公司");
        }
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("ownerDeptId", external(original));
        before.put("newAssignmentEnabled", row.getNewAssignmentEnabled());
        Map<String, Object> sensitive = new LinkedHashMap<>();
        sensitive.put("reason", request.reason());
        sensitive.put("beforeRemark", row.getInternalRemark());
        sensitive.put(
                "afterRemark",
                request.remarkPresent() ? request.internalRemark() : row.getInternalRemark());
        if (request.ownerPresent()) row.setOwnerDeptId(request.ownerDeptId());
        if (request.remarkPresent()) row.setInternalRemark(request.internalRemark());
        if (mapper.maintain(row, transferred, request.remarkPresent()) != 1) conflict();
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("ownerDeptId", external(row.getOwnerDeptId()));
        after.put("newAssignmentEnabled", transferred ? false : row.getNewAssignmentEnabled());
        after.put(
                "changeEnvelope",
                envelope(actor, "DRIVER_EDIT", row.getId(), ViJson.write(sensitive)));
        audit.recordSuccess(
                actor,
                "VI_DRIVER_PROFILE",
                row.getId(),
                "DRIVER_UPDATED",
                before,
                after,
                "维护说明和备注变更已加密留存");
        outbox.publish(
                "DRIVER_UPDATED:" + row.getId() + ":" + (row.getVersion() + 1),
                "VI_DRIVER_PROFILE",
                row.getId(),
                "DRIVER_UPDATED",
                Map.of("driverId", row.getId().toString(), "version", row.getVersion() + 1));
        return new Receipt(row.getId());
    }

    public Map<String, Object> switchNew(long id, Action request, String key, boolean enabled) {
        Actor actor = authority.actor();
        String action = enabled ? "DRIVER_ENABLE_NEW" : "DRIVER_DISABLE_NEW";
        return audited(
                actor,
                id,
                action,
                () -> tx.execute(status -> recordSwitch(id, request, key, enabled, actor)));
    }

    private Map<String, Object> recordSwitch(
            long id, Action request, String key, boolean enabled, Actor actor) {
        authority.lockAuthority();
        String permission =
                enabled ? "biz:driver:enablenewassignments" : "biz:driver:disablenewassignments";
        DriverProfile row = record(id, permission, true);
        Receipt receipt =
                idem.execute(
                        "ADMIN:" + actor.sysUserId(),
                        (enabled ? "DRIVER_ENABLE_NEW:" : "DRIVER_DISABLE_NEW:") + id,
                        key,
                        ViJson.write(request),
                        true,
                        Receipt.class,
                        () -> applySwitch(row, request, enabled, actor));
        return availability(record(receipt.driverId(), permission, false));
    }

    /** 只改行政开关：不改听单偏好/资格/会话，也不撤销邀约、解除承接或释放持车占用。 */
    private Receipt applySwitch(DriverProfile row, Action request, boolean enabled, Actor actor) {
        if (row.getVersion() != request.expectedVersion()) conflict();
        if (enabled) {
            requireCompany(row.getOwnerDeptId());
            policy.requireQualification(row);
        }
        if (mapper.switchNew(row.getId(), row.getVersion(), enabled) != 1) conflict();
        String action =
                enabled ? "DRIVER_NEW_ASSIGNMENTS_ENABLED" : "DRIVER_NEW_ASSIGNMENTS_DISABLED";
        audit.recordSuccess(
                actor,
                "VI_DRIVER_PROFILE",
                row.getId(),
                action,
                Map.of("newAssignmentEnabled", row.getNewAssignmentEnabled()),
                Map.of(
                        "newAssignmentEnabled",
                        enabled,
                        "reasonEnvelope",
                        envelope(actor, action, row.getId(), request.reason())),
                "操作说明已加密留存");
        outbox.publish(
                action + ":" + row.getId() + ":" + (row.getVersion() + 1),
                "VI_DRIVER_PROFILE",
                row.getId(),
                action,
                Map.of(
                        "driverId",
                        row.getId().toString(),
                        "enabled",
                        enabled,
                        "version",
                        row.getVersion() + 1));
        return new Receipt(row.getId());
    }

    private DriverProfile record(long id, String permission, boolean lock) {
        Set<Long> scope = authority.scope(permission);
        DriverProfile row = lock ? mapper.lock(id) : mapper.find(id);
        if (row == null) invisible();
        requireOwner(row, scope);
        return row;
    }

    private void requireOwner(DriverProfile row, Set<Long> scope) {
        if (scope != null
                && (row.getOwnerDeptId() == null || !scope.contains(row.getOwnerDeptId())))
            invisible();
    }

    private void requireCompany(Long id) {
        if (!companyActive(id))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "请明确选择有效的归属公司部门");
    }

    private boolean companyActive(Long id) {
        if (id == null) return false;
        var company = mapper.company(id);
        return company != null
                && "0".equals(company.get("status"))
                && "0".equals(company.get("del_flag"));
    }

    private boolean can(DriverProfile row, String permission) {
        if (!SecurityUtils.hasPermi(permission)) return false;
        Set<Long> scope = authority.scope(permission);
        return scope == null
                || row.getOwnerDeptId() != null && scope.contains(row.getOwnerDeptId());
    }

    /** 司机档案归公司，承接记录归原订单；历史权限范围只过滤订单部门，不能要求它再次覆盖公司根节点。 */
    private boolean canHistory(DriverProfile row) {
        if (!can(row, "biz:driver:query") || !SecurityUtils.hasPermi("biz:assignment:list"))
            return false;
        Set<Long> scope = authority.scope("biz:assignment:list");
        return scope == null || !scope.isEmpty();
    }

    private Map<String, Object> view(DriverProfile row) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("driverId", row.getId().toString());
        result.put("accountId", row.getAccountId().toString());
        result.put("name", row.getDisplayName());
        result.put("phoneMask", phoneMask(row));
        result.put("ownerDeptId", external(row.getOwnerDeptId()));
        result.put("companyName", row.getCompanyName());
        result.put("ownershipUnassigned", row.getOwnerDeptId() == null);
        result.put("canAssignCompany", can(row, "biz:driver:assigncompany"));
        result.put("qualificationStatus", row.getQualificationStatus());
        result.put("qualificationRevision", row.getQualificationRevision());
        result.put("newAssignmentEnabled", row.getNewAssignmentEnabled());
        result.put("listening", row.getListeningEnabled());
        result.put("licenseClass", row.getLicenseClass());
        result.put("licenseValidTo", date(row.getLicenseValidTo()));
        result.put("licenseLongTerm", row.getLicenseLongTerm());
        result.put("reviewDueAt", date(row.getReviewDueAt()));
        result.put("reviewDueAtInstant", instant(row.getReviewDueAt()));
        result.put("score", null);
        result.put("internalRemark", can(row, "biz:driver:query") ? row.getInternalRemark() : null);
        boolean busy = mapper.busy(row.getId()) > 0;
        result.put("occupied", busy);
        result.put("assignmentId", null);
        if (canHistory(row)) {
            Set<Long> scope = authority.scope("biz:assignment:list");
            result.put("completedAssignmentCount", mapper.completedCount(row.getId(), scope));
            result.put("assignmentId", external(mapper.visibleAssignment(row.getId(), scope)));
        }
        result.put("version", row.getVersion());
        result.put("availability", availability(row));
        result.put("allowedActions", actions(row));
        return result;
    }

    private List<String> actions(DriverProfile row) {
        List<String> actions = new ArrayList<>();
        if (can(row, "biz:driver:query")) actions.add("VIEW_DRIVER");
        if (can(row, "biz:driver:edit")) actions.add("EDIT_DRIVER");
        if (can(row, "biz:driver:edit")
                && can(row, "biz:driver:assigncompany")
                && mapper.busy(row.getId()) == 0) actions.add("ASSIGN_DRIVER_COMPANY");
        if (row.getNewAssignmentEnabled() && can(row, "biz:driver:disablenewassignments"))
            actions.add("DISABLE_NEW_ASSIGNMENTS");
        if (!row.getNewAssignmentEnabled()
                && can(row, "biz:driver:enablenewassignments")
                && companyActive(row.getOwnerDeptId())
                && policy.qualificationBlock(row, Instant.now()) == null)
            actions.add("ENABLE_NEW_ASSIGNMENTS");
        if (canHistory(row)) actions.add("VIEW_ASSIGNMENTS");
        return actions;
    }

    private Map<String, Object> availability(DriverProfile row) {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean busy = mapper.busy(row.getId()) > 0;
        var occupancy = mapper.occupancy(row.getId());
        result.put("newAssignmentEnabled", row.getNewAssignmentEnabled());
        result.put("listeningEnabled", row.getListeningEnabled());
        result.put("qualificationValid", policy.qualificationBlock(row, Instant.now()) == null);
        result.put("occupationType", occupancy == null ? "NONE" : occupancy.get("holder_type"));
        result.put("holderId", null);
        if (occupancy != null
                && canHistory(row)
                && "ASSIGNMENT".equals(occupancy.get("holder_type"))) {
            Long visible =
                    mapper.visibleAssignment(row.getId(), authority.scope("biz:assignment:list"));
            if (visible != null && visible.toString().equals(occupancy.get("holder_id").toString()))
                result.put("holderId", visible.toString());
        }
        result.put("availableForNewOrder", false);
        result.put(
                "reason",
                policy.availabilityReason(row, companyActive(row.getOwnerDeptId()), busy));
        result.put("version", row.getVersion());
        return result;
    }

    private String phoneMask(DriverProfile row) {
        if (row.getPhoneCipher() == null) return null;
        String phone = phones.decrypt(row.getPhoneCipher());
        return phone == null
                ? null
                : phone.length() >= 7
                        ? phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4)
                        : "****";
    }

    private String envelope(Actor actor, String action, long id, String value) {
        return Base64.getEncoder()
                .encodeToString(
                        crypto.envelope(
                                value, "A04:ADMIN:" + actor.sysUserId() + ":" + action + ":" + id));
    }

    private static String date(Instant value) {
        return value == null ? null : value.atZone(BUSINESS_ZONE).toLocalDate().toString();
    }

    private static String instant(Instant value) {
        return value == null ? null : value.toString();
    }

    private static String external(Long id) {
        return id == null ? null : id.toString();
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    private static void forbidden() {
        throw new ViException(ViErrorCode.VI_FORBIDDEN);
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private <T> T audited(Actor actor, long id, String action, Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_DRIVER_PROFILE",
                    id,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException failure
                            ? failure.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }
}
