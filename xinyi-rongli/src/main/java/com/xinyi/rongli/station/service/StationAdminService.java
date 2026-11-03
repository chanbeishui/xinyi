package com.xinyi.rongli.station.service;

import com.xinyi.rongli.common.service.EventService;
import com.xinyi.rongli.common.support.ViBool;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.station.domain.Station;
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
        Station row = new Station();
        row.setName(req.name.trim());
        row.setAddress(req.address);
        row.setLng(req.lng);
        row.setLat(req.lat);
        row.setServiceRadiusMeter(req.serviceRadiusMeter == null ? 8000 : req.serviceRadiusMeter);
        row.setSupportedVehicleCodes(req.supportedVehicleCodes == null
                ? "[\"SMALL_CAR\"]" : req.supportedVehicleCodes);
        row.setBusinessHours(req.businessHours == null ? "[]" : req.businessHours);
        row.setEnabled(req.enabled == null || req.enabled ? 1 : 0);
        stationMapper.insert(row);
        Long id = row.getId();
        eventService.record("STATION", id, "STATION_CREATED", null, null, Map.of("name", req.name), null);
        return stationInfo(id);
    }
    @Transactional
    public Map<String, Object> updateStation(Long stationId, com.xinyi.rongli.driver.dto.DriverDtos.StationUpdateReq req)
    {
        Station existing = stationMapper.find(stationId);
        if (existing == null)
        {
            throw ApiException.notFound("检测点不存在");
        }
        if (req == null)
        {
            throw ApiException.validation("缺少站点信息");
        }
        // 就地修改实体，事件快照需要先留存改前的值
        boolean beforeEnabled = ViBool.of(existing.getEnabled());
        if (req.name != null)
        {
            existing.setName(req.name);
        }
        if (req.address != null)
        {
            existing.setAddress(req.address);
        }
        if (req.lng != null)
        {
            existing.setLng(req.lng);
        }
        if (req.lat != null)
        {
            existing.setLat(req.lat);
        }
        if (req.serviceRadiusMeter != null)
        {
            existing.setServiceRadiusMeter(req.serviceRadiusMeter);
        }
        if (req.supportedVehicleCodes != null)
        {
            existing.setSupportedVehicleCodes(req.supportedVehicleCodes);
        }
        if (req.businessHours != null)
        {
            existing.setBusinessHours(req.businessHours);
        }
        if (req.enabled != null)
        {
            existing.setEnabled(ViBool.toInt(req.enabled));
        }
        stationMapper.update(existing);
        eventService.record("STATION", stationId, "STATION_UPDATED", null,
                Map.of("enabled", String.valueOf(beforeEnabled)),
                Map.of("enabled", String.valueOf(ViBool.of(existing.getEnabled()))), null);
        return stationInfo(stationId);
    }
    public Map<String, Object> stationInfo(Long stationId)
    {
        Station s = stationMapper.find(stationId);
        if (s == null)
        {
            throw ApiException.notFound("检测点不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("stationId", String.valueOf(stationId));
        view.put("name", s.getName());
        view.put("deptId", s.getDeptId());
        view.put("address", s.getAddress());
        view.put("lng", s.getLng());
        view.put("lat", s.getLat());
        view.put("serviceRadiusMeter", s.getServiceRadiusMeter());
        view.put("supportedVehicleCodes", s.getSupportedVehicleCodes());
        view.put("businessHours", s.getBusinessHours());
        view.put("enabled", ViBool.of(s.getEnabled()));
        view.put("version", s.getVersion());
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
        Station station = stationMapper.find(stationId);
        if (station == null)
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
