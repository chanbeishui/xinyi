package com.xinyi.rongli.vehicle.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.vehicle.domain.ViVehicle;
import com.xinyi.rongli.vehicle.dto.VehicleAdminView;
import com.xinyi.rongli.vehicle.mapper.VehicleAdminReadMapper;
import com.xinyi.rongli.vehicle.mapper.ViVehicleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 后台车辆管理（A01，B09，ADMIN）。列表/详情/维护/关联订单/关联结果；功能权限服务端校验；
 * 一行=客户-车辆关系（不凭同车牌跨账户合并）；VIN/手机号脱敏；维护仅改本行不追改订单快照；CAS 409 不静默覆盖。
 */
@Service
public class VehicleAdminService {

    private final VehicleAdminReadMapper readMapper;
    private final ViVehicleMapper vehicleMapper;
    private final PhoneCrypto crypto;
    private final BusinessActorResolver actorResolver;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public VehicleAdminService(VehicleAdminReadMapper readMapper, ViVehicleMapper vehicleMapper, PhoneCrypto crypto,
                               BusinessActorResolver actorResolver, AuditService auditService,
                               OutboxService outboxService, PlatformTransactionManager txManager) {
        this.readMapper = readMapper;
        this.vehicleMapper = vehicleMapper;
        this.crypto = crypto;
        this.actorResolver = actorResolver;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public List<VehicleAdminView> list(int pageNum, int pageSize) {
        actorResolver.requireAdmin();
        requirePerm("biz:vehicle:list");
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<VehicleAdminView> out = new ArrayList<>();
        for (Map<String, Object> row : readMapper.selectVehicles(offset, pageSize)) {
            out.add(toView(row, false));
        }
        return out;
    }

    public VehicleAdminView detail(long id) {
        actorResolver.requireAdmin();
        requirePerm("biz:vehicle:query");
        Map<String, Object> row = readMapper.selectVehicleById(id);
        if (row == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return toView(row, true);
    }

    public VehicleAdminView maintenance(long id, long expectedVersion, String brandModel, String typeCode,
                                        String useType, LocalDate registeredOn, String reason) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:vehicle:edit");
        return tx.execute(s -> {
            ViVehicle row = vehicleMapper.selectByIdForUpdate(id);
            if (row == null || row.getDeletedAt() != null) {
                throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
            }
            // PATCH 部分更新：未提供字段保留原值，不置空 NOT NULL 列、不追改订单快照
            String newBrand = brandModel != null ? brandModel : row.getBrandModel();
            String newType = typeCode != null ? typeCode : row.getVehicleTypeCode();
            String newUse = useType != null ? useType : row.getUseType();
            LocalDate newReg = registeredOn != null ? registeredOn : row.getRegisteredOn();
            int updated = vehicleMapper.adminUpdate(id, newBrand, newType, newUse, newReg,
                    reason, expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "车辆已变化，请重新获取");
            }
            auditService.recordSuccess(actor, "VI_VEHICLE", id, "VEHICLE_MAINTENANCE", null,
                    Map.of("reason", reason), null);
            outboxService.publish("vehicle-maintenance:" + id, "VI_VEHICLE", id, "VEHICLE_MAINTAINED", Map.of());
            return toView(readMapper.selectVehicleById(id), true);
        });
    }

    public List<Map<String, Object>> orders(long id) {
        actorResolver.requireAdmin();
        requirePerm("biz:vehicle:list");
        requireVehicle(id);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : readMapper.selectOrdersByVehicle(id)) {
            out.add(Map.of("orderNo", r.get("order_no"), "fulfillmentStatus", r.get("fulfillment_status"),
                    "createdAt", iso(r.get("created_at"))));
        }
        return out;
    }

    public List<Map<String, Object>> results(long id) {
        actorResolver.requireAdmin();
        requirePerm("biz:result:list");
        requireVehicle(id);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : readMapper.selectResultsByVehicle(id)) {
            out.add(Map.of("orderNo", r.get("order_no"), "conclusion", r.get("conclusion"),
                    "inspectedAt", iso(r.get("inspected_at"))));
        }
        return out;
    }

    private void requireVehicle(long id) {
        if (readMapper.selectVehicleById(id) == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
    }

    private VehicleAdminView toView(Map<String, Object> row, boolean withLast) {
        long id = ((Number) row.get("id")).longValue();
        String vin = (String) row.get("vin_last4");
        byte[] cipher = (byte[]) row.get("phone_cipher");
        String phoneMask = cipher == null ? null : PhoneCrypto.mask(crypto.decrypt(cipher));
        String lastResult = null;
        String lastInspectAt = null;
        if (withLast) {
            List<Map<String, Object>> rs = readMapper.selectResultsByVehicle(id);
            if (!rs.isEmpty()) {
                lastResult = (String) rs.get(0).get("conclusion");
                lastInspectAt = iso(rs.get(0).get("inspected_at"));
            }
        }
        Object reg = row.get("registered_on");
        String registeredAt = reg == null ? null : reg.toString();
        return new VehicleAdminView(
                ViId.external(id),
                (String) row.get("plate_no"),
                (String) row.get("vehicle_type_code"),
                null,
                (String) row.get("brand_model"),
                vin == null ? null : "****" + vin,
                registeredAt,
                (String) row.get("use_type"),
                new VehicleAdminView.OwnerRef(ViId.external(((Number) row.get("owner_account_id")).longValue()), phoneMask),
                (String) row.get("verification_status"),
                lastResult,
                lastInspectAt,
                ((Number) row.get("version")).longValue(),
                List.of("VIEW_DETAIL", "CONTACT_OWNER"));
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