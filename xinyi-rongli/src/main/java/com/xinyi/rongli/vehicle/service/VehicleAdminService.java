package com.xinyi.rongli.vehicle.service;

import com.xinyi.rongli.common.service.EventService;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.station.service.StationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.auth.mapper.AccountMapper;
import com.xinyi.rongli.aftersale.mapper.AfterSaleMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.ledger.mapper.LedgerMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.order.mapper.PriceMapper;
import com.xinyi.rongli.station.mapper.StationMapper;
import com.xinyi.rongli.vehicle.mapper.VehicleMapper;
import com.xinyi.rongli.vehicle.dto.VehicleDtos;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.DeptScope;
import com.xinyi.rongli.common.support.Codes;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.ViTime;

/**
 * 管理端 vehicle 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class VehicleAdminService
{
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventService eventService;
    @Autowired
    private DeptScope deptScope;

    public Map<String, Object> vehicles(String keyword, String vehicleTypeCode, String verificationStatus,
            int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        List<Long> deptIds = deptScope.visibleDeptIds();
        view.put("total", vehicleMapper.adminCount(keyword, vehicleTypeCode, verificationStatus, deptIds));
        view.put("rows", camelRows(
                vehicleMapper.adminList(keyword, vehicleTypeCode, verificationStatus, deptIds, offset, limit)));
        return view;
    }
    public Map<String, Object> vehicleDetail(Long vehicleId)
    {
        Row v = Row.of(vehicleMapper.findById(vehicleId));
        if (v.isEmpty() || v.get("deleted_at") != null)
        {
            throw ApiException.notFound("车辆不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>(camelRow(v.map()));
        view.put("orders", camelRows(vehicleMapper.ordersByVehicle(vehicleId)));
        view.put("inspectionResults", camelRows(vehicleMapper.resultsByVehicle(vehicleId)));
        return view;
    }
    /**
     * 维护车辆档案。字段为 null 表示本次不改；每个字段都写进审计的前后值。
     *
     * <p>两条必须同时满足的约束：改车牌要同步 active_plate（唯一索引建在它上面），
     * 改计价车型要落在 vehicle_type 字典内——否则订单核价会拿到不存在的车型。</p>
     */
    @Transactional
    public Map<String, Object> updateVehicle(Long vehicleId, VehicleDtos.VehicleUpdateReq req, Long userId)
    {
        Row v = Row.of(vehicleMapper.findById(vehicleId));
        if (v.isEmpty() || v.get("deleted_at") != null)
        {
            throw ApiException.notFound("车辆不存在");
        }
        // dept_id 为空表示全公司共享，任何持有编辑权限的账号都可维护；非空时按任职部门校验
        if (v.get("dept_id") != null)
        {
            deptScope.requireAssigned(v.lng("dept_id"), "无权维护该车辆");
        }
        if (req == null || req.reason == null || req.reason.isBlank())
        {
            throw ApiException.validation("请填写变更原因，本次修改会记入审计");
        }
        String reason = req.reason.trim();

        String plate = req.plateNo == null || req.plateNo.isBlank() ? v.str("plate_no")
                : normalizePlate(req.plateNo);
        if (!plate.equals(v.str("plate_no"))
                && vehicleMapper.countByPlate(v.lng("owner_account_id"), plate) > 0)
        {
            throw ApiException.stateConflict("该车主名下已存在相同车牌");
        }
        String typeCode = req.vehicleTypeCode == null || req.vehicleTypeCode.isBlank()
                ? v.str("vehicle_type_code") : req.vehicleTypeCode.trim();
        if (!typeCode.equals(v.str("vehicle_type_code"))
                && vehicleMapper.countDictValue("vehicle_type", typeCode) == 0)
        {
            throw ApiException.validation("计价车型不在车型字典中");
        }
        LocalDate registeredOn = req.registeredOn == null || req.registeredOn.isBlank()
                ? v.date("registered_on") : parseDate(req.registeredOn);
        String brandModel = req.brandModel == null ? v.str("brand_model") : req.brandModel.trim();
        String useType = req.useType == null || req.useType.isBlank() ? v.str("use_type") : req.useType.trim();
        String remark = req.internalRemark == null ? v.str("internal_remark") : req.internalRemark.trim();

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", vehicleId);
        row.put("plateNo", plate);
        row.put("vehicleTypeCode", typeCode);
        row.put("brandModel", brandModel);
        row.put("registeredOn", registeredOn);
        row.put("useType", useType);
        row.put("internalRemark", remark);
        row.put("isDefault", req.isDefault);
        vehicleMapper.adminUpdate(row);

        Long ownerId = v.lng("owner_account_id");
        boolean defaultAfter = req.isDefault == null ? v.bool("is_default") : req.isDefault;
        if (Boolean.TRUE.equals(req.isDefault))
        {
            vehicleMapper.clearDefault(ownerId);
            vehicleMapper.markDefault(vehicleId, ownerId);
        }

        Map<String, Object> before = new LinkedHashMap<>();
        before.put("plateNo", v.str("plate_no"));
        before.put("vehicleTypeCode", v.str("vehicle_type_code"));
        before.put("brandModel", v.str("brand_model"));
        before.put("registeredOn", v.date("registered_on") == null ? null : v.date("registered_on").toString());
        before.put("useType", v.str("use_type"));
        before.put("internalRemark", v.str("internal_remark"));
        before.put("isDefault", v.bool("is_default"));

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("plateNo", plate);
        after.put("vehicleTypeCode", typeCode);
        after.put("brandModel", brandModel);
        after.put("registeredOn", registeredOn == null ? null : registeredOn.toString());
        after.put("useType", useType);
        after.put("internalRemark", remark);
        after.put("isDefault", defaultAfter);

        // 只把真正变化的字段写进审计，避免每次保存都刷一整屏一样的值
        Map<String, Object> changedBefore = new LinkedHashMap<>();
        Map<String, Object> changedAfter = new LinkedHashMap<>();
        for (String key : before.keySet())
        {
            if (!Objects.equals(before.get(key), after.get(key)))
            {
                changedBefore.put(key, before.get(key));
                changedAfter.put(key, after.get(key));
            }
        }
        eventService.record("VEHICLE", vehicleId, "VEHICLE_UPDATED", null,
                changedBefore.isEmpty() ? null : changedBefore,
                changedAfter.isEmpty() ? null : changedAfter, reason);
        return vehicleDetail(vehicleId);
    }
    public Map<String, Object> vehicleOrders(Long vehicleId)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        List<Map<String, Object>> rows = vehicleMapper.ordersByVehicle(vehicleId);
        rows.forEach(this::enrichOrderRow);
        view.put("rows", camelRows(rows));
        return view;
    }
    public Map<String, Object> vehicleResults(Long vehicleId)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", camelRows(vehicleMapper.resultsByVehicle(vehicleId)));
        return view;
    }
    private String camelKey(String key)
    {
        if (key == null || key.indexOf('_') < 0)
        {
            return key;
        }
        StringBuilder sb = new StringBuilder(key.length());
        boolean upper = false;
        for (char c : key.toCharArray())
        {
            if (c == '_')
            {
                upper = true;
                continue;
            }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }
    /** 车牌归一化：去空格、转大写，并校验「省份简称 + 字母 + 5~6 位」 */
    private String normalizePlate(String plateNo)
    {
        String plate = plateNo.trim().replace(" ", "").toUpperCase();
        if (!plate.matches("^[\\u4e00-\\u9fa5][A-Z][A-Z0-9]{5,6}$"))
        {
            throw ApiException.validation("车牌号格式不正确，例如：粤K12345");
        }
        return plate;
    }

    private LocalDate parseDate(String text)
    {
        try
        {
            return LocalDate.parse(text.length() > 10 ? text.substring(0, 10) : text);
        }
        catch (RuntimeException e)
        {
            throw ApiException.validation("日期格式应为 yyyy-MM-dd");
        }
    }

    private Map<String, Object> camelRow(Map<String, Object> row)
    {
        Map<String, Object> out = new LinkedHashMap<>();
        if (row == null)
        {
            return out;
        }
        for (Map.Entry<String, Object> entry : row.entrySet())
        {
            out.put(camelKey(entry.getKey()), cellValue(entry.getValue()));
        }
        return out;
    }
    private List<Map<String, Object>> camelRows(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        if (rows == null)
        {
            return out;
        }
        for (Map<String, Object> row : rows)
        {
            out.add(camelRow(row));
        }
        return out;
    }
    private Object cellValue(Object value)
    {
        if (value instanceof LocalDateTime time)
        {
            return ViTime.iso(time);
        }
        if (value instanceof java.time.LocalDate date)
        {
            return date.toString();
        }
        return value;
    }
    private void enrichOrderRow(Map<String, Object> row)
    {
        Object starts = row.get("pickup_starts_at");
        Object ends = row.get("pickup_ends_at");
        if (starts instanceof LocalDateTime s && ends instanceof LocalDateTime e)
        {
            row.put("pickup_window_label", ViTime.windowLabel(s, e));
        }
        Object status = row.get("fulfillment_status");
        if (status != null)
        {
            row.put("fulfillment_status_label", bookingService.statusLabel(String.valueOf(status)));
        }
        Object fen = row.get("total_fen");
        if (fen instanceof Number n)
        {
            row.put("total_yuan", BigDecimal.valueOf(n.longValue()).movePointLeft(2)
                    .setScale(2, RoundingMode.HALF_UP));
        }
    }
}
