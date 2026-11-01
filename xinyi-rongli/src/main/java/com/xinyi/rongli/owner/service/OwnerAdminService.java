package com.xinyi.rongli.owner.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.owner.domain.ViOwnerProfile;
import com.xinyi.rongli.owner.dto.OwnerView;
import com.xinyi.rongli.owner.mapper.OwnerAdminReadMapper;
import com.xinyi.rongli.owner.mapper.ViOwnerProfileMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车主档案管理（A03，B27，ADMIN）。列表/详情/维护/关联车辆/关联订单；功能权限+部门数据范围服务层校验。
 * 数据范围按订单权威 dept_id：跨公司历史不因一条订单暴露；默认脱敏；维护仅改运营字段（服务称呼/标记/备注），
 * 不直接改微信标识/实名/已验证手机，不因标记封禁下单；CAS 409 不静默覆盖。
 */
@Service
public class OwnerAdminService {

    private static final Set<String> SERVICE_FLAGS = Set.of("NORMAL", "VERIFY_AGAIN");

    private final OwnerAdminReadMapper readMapper;
    private final ViOwnerProfileMapper profileMapper;
    private final PhoneCrypto crypto;
    private final BusinessActorResolver actorResolver;
    private final IDeptScopeService deptScope;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public OwnerAdminService(OwnerAdminReadMapper readMapper, ViOwnerProfileMapper profileMapper, PhoneCrypto crypto,
                             BusinessActorResolver actorResolver, IDeptScopeService deptScope,
                             AuditService auditService, OutboxService outboxService, PlatformTransactionManager txManager) {
        this.readMapper = readMapper;
        this.profileMapper = profileMapper;
        this.crypto = crypto;
        this.actorResolver = actorResolver;
        this.deptScope = deptScope;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public List<OwnerView> list(int pageNum, int pageSize, String keyword, String status) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:list");
        Set<Long> deptIds = scope("biz:owner:list");
        if (emptyScope(deptIds)) {
            return List.of();
        }
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<OwnerView> out = new ArrayList<>();
        for (Map<String, Object> row : readMapper.selectOwners(deptIds, keyword, status, offset, pageSize)) {
            out.add(toView(row));
        }
        return out;
    }

    public long count(String keyword, String status) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:list");
        Set<Long> deptIds = scope("biz:owner:list");
        if (emptyScope(deptIds)) {
            return 0;
        }
        return readMapper.countOwners(deptIds, keyword, status);
    }

    public OwnerView detail(long id) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:query");
        Set<Long> deptIds = scope("biz:owner:query");
        requireVisible(id, deptIds);
        return toView(readMapper.selectOwnerById(id, deptIds));
    }

    public OwnerView maintenance(long id, long expectedVersion, String serviceName, String serviceFlag,
                                 String internalRemark, String reason) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:owner:edit");
        Set<Long> deptIds = scope("biz:owner:edit");
        requireVisible(id, deptIds);
        if (serviceFlag != null && !SERVICE_FLAGS.contains(serviceFlag)) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "serviceFlag 非法");
        }
        return tx.execute(s -> {
            ViOwnerProfile row = profileMapper.selectByIdForUpdate(id);
            if (row == null) {
                throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
            }
            // PATCH 部分更新：未提供字段保留原值；仅运营字段，不触碰手机/核验/微信标识
            String newName = serviceName != null ? serviceName : row.getServiceName();
            String newFlag = serviceFlag != null ? serviceFlag : row.getServiceFlag();
            String newRemark = internalRemark != null ? internalRemark : row.getInternalRemark();
            int updated = profileMapper.adminUpdate(id, newName, newFlag, newRemark, actor.sysUserId(), expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "档案已变化，请重新获取");
            }
            auditService.recordSuccess(actor, "VI_OWNER", id, "OWNER_MAINTENANCE", null,
                    Map.of("reason", reason), null);
            outboxService.publish("owner-maintenance:" + id, "VI_OWNER", id, "OWNER_MAINTAINED", Map.of());
            return toView(readMapper.selectOwnerById(id, deptIds));
        });
    }

    public List<Map<String, Object>> vehicles(long id, int pageNum, int pageSize) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:list");
        Set<Long> deptIds = scope("biz:owner:list");
        long accountId = requireVisible(id, deptIds);
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : readMapper.selectVehiclesByOwner(accountId, offset, pageSize)) {
            out.add(vehicleView(r));
        }
        return out;
    }

    public long countVehicles(long id) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:list");
        Set<Long> deptIds = scope("biz:owner:list");
        long accountId = requireVisible(id, deptIds);
        return readMapper.countVehiclesByOwner(accountId);
    }

    public List<Map<String, Object>> orders(long id, int pageNum, int pageSize) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:list");
        Set<Long> deptIds = scope("biz:owner:list");
        long accountId = requireVisible(id, deptIds);
        if (emptyScope(deptIds)) {
            return List.of();
        }
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : readMapper.selectOrdersByOwner(accountId, deptIds, offset, pageSize)) {
            out.add(orderSummary(r));
        }
        return out;
    }

    public long countOrders(long id) {
        actorResolver.requireAdmin();
        requirePerm("biz:owner:list");
        Set<Long> deptIds = scope("biz:owner:list");
        long accountId = requireVisible(id, deptIds);
        if (emptyScope(deptIds)) {
            return 0;
        }
        return readMapper.countOrdersByOwner(accountId, deptIds);
    }

    /** 可见性：全量或档案存在且本人在可见部门有订单；返回 account_id 供关联查询。不可见统一 404 不泄露存在。 */
    private long requireVisible(long id, Set<Long> deptIds) {
        if (emptyScope(deptIds) || readMapper.countVisible(id, deptIds) == 0) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        Map<String, Object> row = readMapper.selectOwnerById(id, deptIds);
        if (row == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return ((Number) row.get("account_id")).longValue();
    }

    private Set<Long> scope(String perm) {
        DeptScopeResult r = deptScope.resolve(SecurityUtils.getLoginUser(), perm);
        return r.isAllData() ? null : r.getDeptIds();
    }

    private static boolean emptyScope(Set<Long> deptIds) {
        return deptIds != null && deptIds.isEmpty();
    }

    private OwnerView toView(Map<String, Object> row) {
        byte[] cipher = (byte[]) row.get("phone_cipher");
        String phoneMask = cipher == null ? null : PhoneCrypto.mask(crypto.decrypt(cipher));
        boolean phoneVerified = row.get("phone_verified_at") != null;
        return new OwnerView(
                ViId.external(((Number) row.get("id")).longValue()),
                (String) row.get("nickname"),
                (String) row.get("service_name"),
                phoneMask,
                phoneVerified,
                ((Number) row.get("vehicle_count")).longValue(),
                ((Number) row.get("order_count")).longValue(),
                iso(row.get("last_service_at")),
                (String) row.get("service_flag"),
                (String) row.get("internal_remark"),
                ((Number) row.get("version")).longValue(),
                List.of("VIEW_DETAIL", "CONTACT_OWNER"));
    }

    private Map<String, Object> vehicleView(Map<String, Object> r) {
        String vin = (String) r.get("vin_last4");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("vehicleId", ViId.external(((Number) r.get("id")).longValue()));
        m.put("plateNo", r.get("plate_no"));
        m.put("vehicleTypeCode", r.get("vehicle_type_code"));
        m.put("vehicleTypeLabel", null);
        m.put("brandModel", r.get("brand_model"));
        m.put("vinMask", vin == null ? null : "****" + vin);
        m.put("verificationStatus", r.get("verification_status"));
        m.put("version", ((Number) r.get("version")).longValue());
        m.put("allowedActions", List.of("VIEW_DETAIL", "CONTACT_OWNER"));
        return m;
    }

    private Map<String, Object> orderSummary(Map<String, Object> r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("view", "ADMIN");
        m.put("orderNo", r.get("order_no"));
        String fs = (String) r.get("fulfillment_status");
        m.put("fulfillmentStatus", fs);
        m.put("displayStatus", displayStatus(fs));
        m.put("cancelStatus", r.get("cancel_status"));
        m.put("custody", r.get("custody_type"));
        m.put("vehicle", orderVehicle((String) r.get("vehicle_snapshot")));
        m.put("station", orderStation(r));
        m.put("slotStartAt", iso(r.get("pickup_starts_at")));
        m.put("slotEndAt", iso(r.get("pickup_ends_at")));
        m.put("returnedAt", iso(r.get("returned_at")));
        m.put("version", ((Number) r.get("version")).longValue());
        m.put("allowedActions", List.of("VIEW_DETAIL", "CONTACT_OWNER"));
        Object pay = r.get("payment_status");
        m.put("paymentStatus", pay == null ? "UNKNOWN" : pay);
        return m;
    }

    private Map<String, Object> orderVehicle(String snapshotJson) {
        Map<String, Object> snap = snapshotJson == null ? Map.of() : ViJson.read(snapshotJson, Map.class);
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("vehicleId", str(snap.get("vehicleId")));
        v.put("plateNo", str(snap.get("plateNo")));
        v.put("vehicleTypeCode", str(snap.get("vehicleTypeCode")));
        v.put("vehicleTypeLabel", snap.get("vehicleTypeLabel") == null ? "" : str(snap.get("vehicleTypeLabel")));
        v.put("brandModel", snap.get("brandModel"));
        v.put("vinLast4", snap.get("vinLast4"));
        return v;
    }

    private Map<String, Object> orderStation(Map<String, Object> r) {
        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("lng", r.get("lng"));
        loc.put("lat", r.get("lat"));
        loc.put("coordinateSystem", "GCJ02");
        Map<String, Object> st = new LinkedHashMap<>();
        st.put("stationId", ViId.external(((Number) r.get("station_id")).longValue()));
        st.put("name", r.get("station_name"));
        st.put("publicAddress", r.get("station_address"));
        st.put("location", loc);
        st.put("servicePhone", null);
        return st;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String displayStatus(String fs) {
        if (fs == null) {
            return "UNKNOWN";
        }
        return switch (fs) {
            case "PENDING_PAY" -> "待支付";
            case "SCHEDULED" -> "已预约";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "CLOSED" -> "已关闭";
            default -> fs;
        };
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