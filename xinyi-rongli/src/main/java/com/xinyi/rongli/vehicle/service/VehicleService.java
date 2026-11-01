package com.xinyi.rongli.vehicle.service;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.vehicle.domain.ViVehicle;
import com.xinyi.rongli.vehicle.dto.VehicleView;
import com.xinyi.rongli.vehicle.mapper.ViVehicleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 车主车辆（A01，MINI）。本人 CRUD/常用/软删；归属本人（伪造 owner/猜车牌越权 404 不泄露）；
 * 同车主同车牌活跃唯一（冲突 409，不泄露其他车主）；软删释放车牌可重建；生成列不入 DTO；CAS；历史快照不追改。
 */
@Service
public class VehicleService {

    private final ViVehicleMapper vehicleMapper;
    private final ViAccountMapper accountMapper;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public VehicleService(ViVehicleMapper vehicleMapper, ViAccountMapper accountMapper,
                          AuditService auditService, OutboxService outboxService, PlatformTransactionManager txManager) {
        this.vehicleMapper = vehicleMapper;
        this.accountMapper = accountMapper;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public List<VehicleView> list(long accountId) {
        List<VehicleView> out = new ArrayList<>();
        for (ViVehicle v : vehicleMapper.selectByOwner(accountId)) {
            out.add(toView(v));
        }
        return out;
    }

    public VehicleView create(long accountId, String plateNo, String typeCode, String brandModel,
                              String useType, String vinLast4, boolean isDefault) {
        String plate = normalizePlate(plateNo);
        validate(plate, typeCode);
        return tx.execute(s -> {
            lockOwner(accountId);
            if (vehicleMapper.selectActiveByOwnerPlate(accountId, plate) != null) {
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "该车牌已在你的车辆中");
            }
            ViVehicle v = new ViVehicle();
            v.setOwnerAccountId(accountId);
            v.setPlateNo(plate);
            v.setVehicleTypeCode(typeCode);
            v.setBrandModel(brandModel);
            v.setUseType(useType);
            v.setVinLast4(vinLast4);
            v.setVerificationStatus("PENDING");
            v.setIsDefault(isDefault ? 1 : 0);
            vehicleMapper.insert(v);
            if (isDefault) {
                vehicleMapper.clearDefault(accountId, v.getId());
            }
            audit(accountId, v.getId(), "VEHICLE_CREATE");
            return toView(vehicleMapper.selectById(v.getId()));
        });
    }

    public VehicleView update(long accountId, long id, String plateNo, String typeCode, String brandModel,
                              String useType, String vinLast4, Boolean isDefault, long expectedVersion) {
        String plate = normalizePlate(plateNo);
        validate(plate, typeCode);
        return tx.execute(s -> {
            lockOwner(accountId);
            ViVehicle row = owned(accountId, id);
            ViVehicle conflict = vehicleMapper.selectActiveByOwnerPlate(accountId, plate);
            if (conflict != null && conflict.getId() != id) {
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "该车牌已在你的车辆中");
            }
            int newDefault = isDefault == null ? row.getIsDefault() : (isDefault ? 1 : 0);
            if (newDefault == 1) {
                vehicleMapper.clearDefault(accountId, id);
            }
            int updated = vehicleMapper.update(id, accountId, plate, typeCode, brandModel, useType, vinLast4,
                    newDefault, expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "车辆已变化，请刷新后重试");
            }
            audit(accountId, id, "VEHICLE_UPDATE");
            return toView(vehicleMapper.selectById(id));
        });
    }

    public void delete(long accountId, long id, long expectedVersion, String reason) {
        tx.execute(s -> {
            lockOwner(accountId);
            owned(accountId, id);
            int updated = vehicleMapper.softDelete(id, accountId, Instant.now(), expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "车辆已变化，请刷新后重试");
            }
            auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, null, null, null),
                    "VI_VEHICLE", id, "VEHICLE_DELETE", null, Map.of("reason", reason), null);
            outboxService.publish("vehicle-delete:" + id, "VI_VEHICLE", id, "VEHICLE_DELETED", Map.of());
            return null;
        });
    }

    public VehicleView setDefault(long accountId, long id, long expectedVersion, String reason) {
        return tx.execute(s -> {
            lockOwner(accountId);
            owned(accountId, id);
            vehicleMapper.clearDefault(accountId, id);
            int updated = vehicleMapper.setDefaultFlag(id, accountId, 1, expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "车辆已变化，请刷新后重试");
            }
            audit(accountId, id, "VEHICLE_SET_DEFAULT");
            return toView(vehicleMapper.selectById(id));
        });
    }

    private void lockOwner(long accountId) {
        if (accountMapper.selectByIdForUpdate(accountId) == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
    }

    private ViVehicle owned(long accountId, long id) {
        ViVehicle row = vehicleMapper.selectByIdForUpdate(id);
        if (row == null || row.getOwnerAccountId() != accountId || row.getDeletedAt() != null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return row;
    }

    private String normalizePlate(String plate) {
        return plate == null ? "" : plate.trim().toUpperCase();
    }

    private void validate(String plate, String typeCode) {
        if (plate.isBlank() || plate.length() > 20) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "车牌必填且不超过 20 字符");
        }
        if (typeCode == null || typeCode.isBlank()) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "车型必填");
        }
    }

    private void audit(long accountId, long id, String action) {
        auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, null, null, null),
                "VI_VEHICLE", id, action, null, Map.of(), null);
        outboxService.publish("vehicle:" + action.toLowerCase() + ":" + id, "VI_VEHICLE", id, action, Map.of());
    }

    private VehicleView toView(ViVehicle v) {
        return new VehicleView(ViId.external(v.getId()), v.getPlateNo(), v.getVehicleTypeCode(),
                null, v.getBrandModel(), v.getVersion(), v.getIsDefault() != null && v.getIsDefault() == 1);
    }
}