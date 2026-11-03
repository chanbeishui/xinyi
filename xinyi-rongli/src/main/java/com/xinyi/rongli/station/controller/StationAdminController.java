package com.xinyi.rongli.station.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.admin.service.AdminService;
import com.xinyi.rongli.owner.service.OwnerAdminService;
import com.xinyi.rongli.driver.service.DriverAdminService;
import com.xinyi.rongli.ledger.service.LedgerAdminService;
import com.xinyi.rongli.vehicle.service.VehicleAdminService;
import com.xinyi.rongli.station.service.StationAdminService;

/**
 * 管理端 station 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class StationAdminController
{
    @Autowired
    private StationAdminService stationAdminService;

    @GetMapping("/stations")
    @PreAuthorize("@ss.hasPermi('biz:station:list')")
    public Map<String, Object> stations(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return stationAdminService.stations(pageNum, pageSize);
    }
    @PostMapping("/stations")
    @PreAuthorize("@ss.hasPermi('biz:station:create')")
    public Map<String, Object> createStation(@RequestBody DriverDtos.StationUpdateReq req)
    {
        return stationAdminService.createStation(req);
    }
    @PutMapping("/stations/{stationId}")
    @PreAuthorize("@ss.hasPermi('biz:station:edit')")
    public Map<String, Object> updateStation(@PathVariable Long stationId,
            @RequestBody DriverDtos.StationUpdateReq req)
    {
        return stationAdminService.updateStation(stationId, req);
    }
    @GetMapping("/station-grants")
    @PreAuthorize("@ss.hasPermi('biz:stationgrant:list')")
    public Map<String, Object> stationGrants(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize)
    {
        return stationAdminService.stationGrants(pageNum, pageSize);
    }
    @GetMapping("/employee-candidates")
    @PreAuthorize("@ss.hasPermi('biz:stationgrant:list')")
    public java.util.List<Map<String, Object>> employeeCandidates(
            @RequestParam(required = false) String keyword)
    {
        return stationAdminService.employeeCandidates(keyword);
    }
    @PutMapping("/station-grants/{employeeUserId}")
    @PreAuthorize("@ss.hasPermi('biz:stationgrant:edit')")
    public Map<String, Object> updateStationGrant(@PathVariable Long employeeUserId,
            @RequestBody Map<String, Object> body)
    {
        Long stationId = body == null || body.get("stationId") == null ? null
                : Long.valueOf(String.valueOf(body.get("stationId")));
        boolean enabled = body == null || body.get("enabled") == null
                || Boolean.parseBoolean(String.valueOf(body.get("enabled")));
        String reason = body == null || body.get("reason") == null ? null
                : String.valueOf(body.get("reason"));
        return stationAdminService.updateStationGrant(employeeUserId, stationId, enabled, reason,
                SecurityUtils.getUserId());
    }
}
