package com.xinyi.rongli.vehicle.service;

import com.xinyi.rongli.common.service.EventService;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.station.service.StationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Transactional
    public Map<String, Object> maintainVehicle(Long vehicleId, String remark, String verificationStatus, Long userId)
    {
        Row v = Row.of(vehicleMapper.findById(vehicleId));
        if (v.isEmpty())
        {
            throw ApiException.notFound("车辆不存在");
        }
        if (verificationStatus != null
                && !List.of("PENDING", "VERIFIED", "REJECTED").contains(verificationStatus.toUpperCase()))
        {
            throw ApiException.validation("核验状态只能是 PENDING / VERIFIED / REJECTED");
        }
        vehicleMapper.adminMaintain(vehicleId, remark == null ? v.str("internal_remark") : remark,
                verificationStatus == null ? v.str("verification_status") : verificationStatus.toUpperCase());
        eventService.record("VEHICLE", vehicleId, "VEHICLE_MAINTAINED", null,
                Map.of("verificationStatus", String.valueOf(v.str("verification_status"))),
                Map.of("verificationStatus", String.valueOf(verificationStatus)), remark);
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
