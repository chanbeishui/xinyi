package com.xinyi.rongli.station.service;

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
 * 管理端 station 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class StationAdminService
{
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private StationService stationService;
    @Autowired
    private EventService eventService;

    public Map<String, Object> capacity(Long stationId)
    {
        return stationService.adminCapacity(stationId);
    }
    @Transactional
    public Map<String, Object> updateCapacity(Long slotId, com.xinyi.rongli.driver.dto.DriverDtos.CapacityChangeReq req)
    {
        return stationService.adminUpdateCapacity(slotId, req);
    }
    public Map<String, Object> stations(int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", stationMapper.adminCount());
        view.put("rows", stationMapper.adminList(offset, limit));
        return view;
    }
    @Transactional
    public Map<String, Object> createStation(com.xinyi.rongli.driver.dto.DriverDtos.StationUpdateReq req)
    {
        if (req == null || req.name == null || req.name.isBlank())
        {
            throw ApiException.validation("请填写检测点名称");
        }
        if (req.lng == null || req.lat == null)
        {
            throw ApiException.validation("请填写检测点经纬度");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", req.name.trim());
        row.put("deptId", null);
        row.put("address", req.address);
        row.put("lng", req.lng);
        row.put("lat", req.lat);
        row.put("serviceRadiusMeter", req.serviceRadiusMeter == null ? 8000 : req.serviceRadiusMeter);
        row.put("supportedVehicleCodes", req.supportedVehicleCodes == null
                ? "[\"SMALL_CAR\"]" : req.supportedVehicleCodes);
        row.put("businessHours", req.businessHours == null ? "[]" : req.businessHours);
        row.put("enabled", req.enabled == null || req.enabled);
        stationMapper.insert(row);
        Long id = ((Number) row.get("id")).longValue();
        eventService.record("STATION", id, "STATION_CREATED", null, null, Map.of("name", req.name), null);
        return stationInfo(id);
    }
    @Transactional
    public Map<String, Object> updateStation(Long stationId, com.xinyi.rongli.driver.dto.DriverDtos.StationUpdateReq req)
    {
        Row existing = Row.of(stationMapper.find(stationId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        if (req == null)
        {
            throw ApiException.validation("缺少站点信息");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", stationId);
        row.put("name", req.name == null ? existing.str("name") : req.name);
        row.put("deptId", existing.lng("dept_id"));
        row.put("address", req.address == null ? existing.str("address") : req.address);
        row.put("lng", req.lng == null ? existing.dec("lng") : req.lng);
        row.put("lat", req.lat == null ? existing.dec("lat") : req.lat);
        row.put("serviceRadiusMeter", req.serviceRadiusMeter == null
                ? existing.intg("service_radius_meter") : req.serviceRadiusMeter);
        row.put("supportedVehicleCodes", req.supportedVehicleCodes == null
                ? existing.json("supported_vehicle_codes") : req.supportedVehicleCodes);
        row.put("businessHours", req.businessHours == null ? existing.json("business_hours") : req.businessHours);
        row.put("enabled", req.enabled == null ? existing.bool("enabled") : req.enabled);
        stationMapper.update(row);
        eventService.record("STATION", stationId, "STATION_UPDATED", null,
                Map.of("enabled", String.valueOf(existing.bool("enabled"))),
                Map.of("enabled", String.valueOf(row.get("enabled"))), null);
        return stationInfo(stationId);
    }
    public Map<String, Object> stationInfo(Long stationId)
    {
        Row s = Row.of(stationMapper.find(stationId));
        if (s.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("stationId", String.valueOf(stationId));
        view.put("name", s.str("name"));
        view.put("deptId", s.lng("dept_id"));
        view.put("address", s.str("address"));
        view.put("lng", s.dec("lng"));
        view.put("lat", s.dec("lat"));
        view.put("serviceRadiusMeter", s.intg("service_radius_meter"));
        view.put("supportedVehicleCodes", s.json("supported_vehicle_codes"));
        view.put("businessHours", s.json("business_hours"));
        view.put("enabled", s.bool("enabled"));
        view.put("version", s.lng("version"));
        return view;
    }
    public Map<String, Object> stationGrants(int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 200);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", stationMapper.listGrants(offset, limit));
        return view;
    }
    public List<Map<String, Object>> employeeCandidates(String keyword)
    {
        String kw = keyword == null ? null : keyword.trim();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> raw : accountMapper.searchEmployeeCandidates(kw))
        {
            Row r = Row.of(raw);
            String phone = r.str("phonenumber");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", String.valueOf(r.lng("user_id")));
            item.put("userName", r.str("user_name"));
            item.put("nickName", r.str("nick_name"));
            item.put("phoneMasked", maskPhone(phone));
            item.put("deptName", r.str("dept_name"));
            item.put("grantedStationCount", r.intg("granted_station_count", 0));
            out.add(item);
        }
        return out;
    }
    @Transactional
    public Map<String, Object> updateStationGrant(Long employeeUserId, Long stationId, boolean enabled,
            String reason, Long grantedBy)
    {
        if (employeeUserId == null || stationId == null)
        {
            throw ApiException.validation("缺少员工或站点");
        }
        Row user = Row.of(accountMapper.findSysUser(employeeUserId));
        if (user.isEmpty())
        {
            throw ApiException.notFound("员工不存在");
        }
        Row station = Row.of(stationMapper.find(stationId));
        if (station.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        stationMapper.upsertGrant(employeeUserId, stationId, enabled, grantedBy, reason);
        if (!enabled)
        {
            // 撤销授权后立即让该员工旧会话失效，避免继续用旧站点上下文操作
            accountMapper.revokeBySysUser(employeeUserId);
        }
        eventService.record("STATION_GRANT", stationId, enabled ? "GRANT_ENABLED" : "GRANT_DISABLED", null,
                null, Map.of("sysUserId", String.valueOf(employeeUserId)), reason);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("sysUserId", String.valueOf(employeeUserId));
        view.put("stationId", String.valueOf(stationId));
        view.put("enabled", enabled);
        view.put("hint", enabled ? "已授权，该员工切换站点后可见" : "已撤销授权，该员工旧会话已失效");
        return view;
    }
    private String maskPhone(String phone)
    {
        if (phone == null || phone.length() < 7)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
