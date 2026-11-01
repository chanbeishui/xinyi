package com.xinyi.rongli.station.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.station.domain.ViStation;
import com.xinyi.rongli.station.dto.GeoPoint;
import com.xinyi.rongli.station.dto.PublicStationView;
import com.xinyi.rongli.station.dto.StationInput;
import com.xinyi.rongli.station.dto.StationView;
import com.xinyi.rongli.station.mapper.ViStationMapper;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 检测点、营业与服务区域（C02，S16/B17）。后台与员工共用 Service：
 * ADMIN 经 /api/admin/stations（数据范围按 dept_id），员工经 /api/app/station/info（仅本人切换站点）。
 * 联系电话密文存储、对外脱敏；资质文件ID引用 vi_file；版本必填 CAS；
 * 迁部门存在在手订单受控拒绝（VI_STATE_CONFLICT），不静默改归属。
 */
@Service
public class StationService {

    private final ViStationMapper stationMapper;
    private final PhoneCrypto crypto;
    private final BusinessActorResolver actorResolver;
    private final IDeptScopeService deptScope;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public StationService(ViStationMapper stationMapper, PhoneCrypto crypto, BusinessActorResolver actorResolver,
                          IDeptScopeService deptScope, AuditService auditService, OutboxService outboxService,
                          PlatformTransactionManager txManager) {
        this.stationMapper = stationMapper;
        this.crypto = crypto;
        this.actorResolver = actorResolver;
        this.deptScope = deptScope;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    // ---------- MINI 公开营业列表（API_011） ----------

    public List<PublicStationView> publicList(int pageNum, int pageSize) {
        actorResolver.requireMini();
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<PublicStationView> out = new ArrayList<>();
        for (ViStation s : stationMapper.selectList(null, true, offset, pageSize)) {
            out.add(toPublicView(s));
        }
        return out;
    }

    public long publicCount() {
        actorResolver.requireMini();
        return stationMapper.countList(null, true);
    }

    // ---------- MINI 员工本站点（API_095/096） ----------

    public StationView getOwnStation() {
        Actor actor = actorResolver.requireMini();
        long id = requireStationContext(actor);
        ViStation s = stationMapper.selectById(id);
        if (s == null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return toView(s);
    }

    public StationView updateOwnStation(StationInput in) {
        Actor actor = actorResolver.requireMini();
        long id = requireStationContext(actor);
        validate(in, true);
        return tx.execute(st -> {
            ViStation row = stationMapper.selectByIdForUpdate(id);
            if (row == null) {
                throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
            }
            ViStation merged = merge(row, in, false);
            if (stationMapper.update(merged, in.expectedVersion()) != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "站点已变化，请重新获取");
            }
            auditService.recordSuccess(actor, "VI_STATION", id, "STATION_UPDATE", null, Map.of("scope", "SELF"), null);
            outboxService.publish("station-update:" + id, "VI_STATION", id, "STATION_UPDATED", Map.of());
            return toView(stationMapper.selectById(id));
        });
    }

    // ---------- ADMIN 站点管理（API_128/129/130） ----------

    public List<StationView> adminList(int pageNum, int pageSize) {
        actorResolver.requireAdmin();
        requirePerm("biz:station:list");
        Set<Long> deptIds = scope("biz:station:list");
        if (emptyScope(deptIds)) {
            return List.of();
        }
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<StationView> out = new ArrayList<>();
        for (ViStation s : stationMapper.selectList(deptIds, false, offset, pageSize)) {
            out.add(toView(s));
        }
        return out;
    }

    public long adminCount() {
        actorResolver.requireAdmin();
        requirePerm("biz:station:list");
        Set<Long> deptIds = scope("biz:station:list");
        if (emptyScope(deptIds)) {
            return 0;
        }
        return stationMapper.countList(deptIds, false);
    }

    public StationView create(StationInput in) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:station:create");
        validate(in, false);
        long deptId = resolveCreateDept(in);
        ViStation s = new ViStation();
        s.setName(in.name().trim());
        s.setDeptId(deptId);
        s.setAddress(in.address().trim());
        s.setLng(in.location().lng());
        s.setLat(in.location().lat());
        s.setServiceRadiusMeter(in.serviceRadiusMeter() == null ? 0 : in.serviceRadiusMeter());
        s.setSupportedVehicleCodes(ViJson.write(in.supportedVehicleTypeCodes()));
        s.setBusinessHours(ViJson.write(in.businessHours() == null ? "" : in.businessHours()));
        s.setEnabled(Boolean.TRUE.equals(in.enabled()) ? 1 : 0);
        s.setContactPhoneCipher(in.contactPhone() == null || in.contactPhone().isBlank() ? null : crypto.encrypt(in.contactPhone()));
        s.setQualificationFileIds(in.qualificationFileIds() == null ? null : ViJson.write(in.qualificationFileIds()));
        return tx.execute(st -> {
            stationMapper.insert(s);
            auditService.recordSuccess(actor, "VI_STATION", s.getId(), "STATION_CREATE", null, Map.of("deptId", deptId), null);
            outboxService.publish("station-create:" + s.getId(), "VI_STATION", s.getId(), "STATION_CREATED", Map.of());
            return toView(stationMapper.selectById(s.getId()));
        });
    }

    public StationView adminUpdate(long id, StationInput in) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:station:edit");
        Set<Long> deptIds = scope("biz:station:edit");
        requireVisible(id, deptIds);
        validate(in, true);
        return tx.execute(st -> {
            ViStation row = stationMapper.selectByIdForUpdate(id);
            if (row == null) {
                throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
            }
            // 迁部门：目标部门须在本人数据范围内，且无在手订单（受控拒绝，不静默改归属）
            if (in.deptId() != null && in.deptId() != row.getDeptId().longValue()) {
                deptScope.checkDeptAccess(List.of(in.deptId()));
                if (stationMapper.countActiveOrders(id) > 0) {
                    throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "站点存在在手订单，不能迁部门");
                }
            }
            ViStation merged = merge(row, in, true);
            if (stationMapper.update(merged, in.expectedVersion()) != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "站点已变化，请重新获取");
            }
            auditService.recordSuccess(actor, "VI_STATION", id, "STATION_UPDATE", null, Map.of("scope", "ADMIN"), null);
            outboxService.publish("station-update:" + id, "VI_STATION", id, "STATION_UPDATED", Map.of());
            return toView(stationMapper.selectById(id));
        });
    }

    // ---------- 内部 ----------

    private long requireStationContext(Actor actor) {
        if (!"STATION".equals(actor.actingRole()) || actor.stationId() == null) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "需切换到站点工作身份");
        }
        return actor.stationId();
    }

    private long resolveCreateDept(StationInput in) {
        if (in.deptId() != null) {
            deptScope.checkDeptAccess(List.of(in.deptId()));
            return in.deptId();
        }
        LoginUser lu = SecurityUtils.getLoginUser();
        Long deptId = lu == null || lu.getUser() == null ? null : lu.getUser().getDeptId();
        if (deptId == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "无法确定站点权威部门归属");
        }
        return deptId;
    }

    /** 合并更新：必填字段覆盖，可选字段提供才改；allowDeptMove 控制是否接受 deptId（员工端恒 false）。 */
    private ViStation merge(ViStation row, StationInput in, boolean allowDeptMove) {
        row.setName(in.name().trim());
        row.setAddress(in.address().trim());
        row.setLng(in.location().lng());
        row.setLat(in.location().lat());
        row.setSupportedVehicleCodes(ViJson.write(in.supportedVehicleTypeCodes()));
        if (in.businessHours() != null) {
            row.setBusinessHours(ViJson.write(in.businessHours()));
        }
        if (in.serviceRadiusMeter() != null) {
            row.setServiceRadiusMeter(in.serviceRadiusMeter());
        }
        if (in.enabled() != null) {
            row.setEnabled(in.enabled() ? 1 : 0);
        }
        if (in.contactPhone() != null && !in.contactPhone().isBlank()) {
            row.setContactPhoneCipher(crypto.encrypt(in.contactPhone()));
        }
        if (in.qualificationFileIds() != null) {
            row.setQualificationFileIds(ViJson.write(in.qualificationFileIds()));
        }
        if (allowDeptMove && in.deptId() != null) {
            row.setDeptId(in.deptId());
        }
        return row;
    }

    private void validate(StationInput in, boolean isUpdate) {
        if (in.name() == null || in.name().isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "name 必填");
        }
        if (in.address() == null || in.address().isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "address 必填");
        }
        GeoPoint loc = in.location();
        if (loc == null || loc.lng() == null || loc.lat() == null || !"GCJ02".equals(loc.coordinateSystem())) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "location 必填且坐标系须为 GCJ02");
        }
        if (loc.lng().compareTo(BigDecimal.valueOf(-180)) < 0 || loc.lng().compareTo(BigDecimal.valueOf(180)) > 0
                || loc.lat().compareTo(BigDecimal.valueOf(-90)) < 0 || loc.lat().compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "经纬度超出范围");
        }
        if (in.supportedVehicleTypeCodes() == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "supportedVehicleTypeCodes 必填");
        }
        if (in.serviceRadiusMeter() != null && in.serviceRadiusMeter() < 0) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "serviceRadiusMeter 不能为负");
        }
        if (isUpdate && (in.expectedVersion() == null || in.expectedVersion() < 0)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 必填且非负");
        }
    }

    private void requireVisible(long id, Set<Long> deptIds) {
        if (emptyScope(deptIds)) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        ViStation s = stationMapper.selectById(id);
        if (s == null || (deptIds != null && !deptIds.contains(s.getDeptId()))) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
    }

    private Set<Long> scope(String perm) {
        DeptScopeResult r = deptScope.resolve(SecurityUtils.getLoginUser(), perm);
        return r.isAllData() ? null : r.getDeptIds();
    }

    private static boolean emptyScope(Set<Long> deptIds) {
        return deptIds != null && deptIds.isEmpty();
    }

    private StationView toView(ViStation s) {
        return new StationView(ViId.external(s.getId()), s.getName(), s.getAddress(),
                GeoPoint.gcj02(s.getLng(), s.getLat()), phoneMask(s.getContactPhoneCipher()),
                s.getEnabled() != null && s.getEnabled() == 1, parseList(s.getSupportedVehicleCodes()),
                parseStr(s.getBusinessHours()), s.getServiceRadiusMeter() == null ? 0 : s.getServiceRadiusMeter(),
                s.getVersion() == null ? 0 : s.getVersion());
    }

    private PublicStationView toPublicView(ViStation s) {
        return new PublicStationView(ViId.external(s.getId()), s.getName(), s.getAddress(),
                GeoPoint.gcj02(s.getLng(), s.getLat()), phoneMask(s.getContactPhoneCipher()),
                parseList(s.getSupportedVehicleCodes()), parseStr(s.getBusinessHours()),
                s.getServiceRadiusMeter() == null ? 0 : s.getServiceRadiusMeter());
    }

    private String phoneMask(byte[] cipher) {
        return cipher == null ? null : PhoneCrypto.mask(crypto.decrypt(cipher));
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        com.fasterxml.jackson.databind.JsonNode n = ViJson.tree(json);
        if (!n.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        n.forEach(e -> out.add(e.asText()));
        return out;
    }

    private String parseStr(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        com.fasterxml.jackson.databind.JsonNode n = ViJson.tree(json);
        return n.isTextual() ? n.asText() : n.toString();
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
}