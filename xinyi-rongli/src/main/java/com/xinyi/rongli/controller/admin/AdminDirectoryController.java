package com.xinyi.rongli.controller.admin;

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
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.service.AdminService;

/**
 * 管理端基础数据：车主、代驾、车辆、检测点、站点授权、计价规则、资格审核。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDirectoryController
{
    @Autowired
    private AdminService adminService;

    // ------------------------------------------------------------------ 车主

    @GetMapping("/owners")
    @PreAuthorize("@ss.hasPermi('biz:owner:list')")
    public Map<String, Object> owners(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.owners(keyword, pageNum, pageSize);
    }

    @GetMapping("/owners/{ownerId}")
    @PreAuthorize("@ss.hasPermi('biz:owner:query')")
    public Map<String, Object> owner(@PathVariable Long ownerId)
    {
        return adminService.ownerDetail(ownerId);
    }

    @PatchMapping("/owners/{ownerId}")
    @PreAuthorize("@ss.hasPermi('biz:owner:edit')")
    public Map<String, Object> maintainOwner(@PathVariable Long ownerId,
            @RequestBody Map<String, Object> body)
    {
        String flag = body == null || body.get("serviceFlag") == null ? null
                : String.valueOf(body.get("serviceFlag"));
        String remark = body == null || body.get("remark") == null ? null
                : String.valueOf(body.get("remark"));
        return adminService.maintainOwner(ownerId, flag, remark, SecurityUtils.getUserId());
    }

    @GetMapping("/owners/{ownerId}/vehicles")
    @PreAuthorize("@ss.hasPermi('biz:owner:list')")
    public Map<String, Object> ownerVehicles(@PathVariable Long ownerId)
    {
        return adminService.ownerVehicles(ownerId);
    }

    @GetMapping("/owners/{ownerId}/orders")
    @PreAuthorize("@ss.hasPermi('biz:owner:list')")
    public Map<String, Object> ownerOrders(@PathVariable Long ownerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.ownerOrders(ownerId, pageNum, pageSize);
    }

    // ------------------------------------------------------------------ 代驾

    @GetMapping("/drivers")
    @PreAuthorize("@ss.hasPermi('biz:driver:list')")
    public Map<String, Object> drivers(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.drivers(keyword, status, pageNum, pageSize);
    }

    @GetMapping("/drivers/{driverId}")
    @PreAuthorize("@ss.hasPermi('biz:driver:query')")
    public Map<String, Object> driver(@PathVariable Long driverId)
    {
        return adminService.driverDetail(driverId);
    }

    @PatchMapping("/drivers/{driverId}")
    @PreAuthorize("@ss.hasPermi('biz:driver:edit')")
    public Map<String, Object> maintainDriver(@PathVariable Long driverId,
            @RequestBody Map<String, Object> body)
    {
        String remark = body == null || body.get("remark") == null ? null
                : String.valueOf(body.get("remark"));
        Boolean enabled = body == null || body.get("newAssignmentEnabled") == null ? null
                : Boolean.valueOf(String.valueOf(body.get("newAssignmentEnabled")));
        return adminService.maintainDriver(driverId, remark, enabled, SecurityUtils.getUserId());
    }

    @PostMapping("/drivers/{driverId}/disable-new-assignments")
    @PreAuthorize("@ss.hasPermi('biz:driver:disablenewassignments')")
    public Map<String, Object> disableNewAssignments(@PathVariable Long driverId,
            @RequestBody Map<String, Object> body)
    {
        boolean disabled = body == null || body.get("disabled") == null
                || Boolean.parseBoolean(String.valueOf(body.get("disabled")));
        String reason = body == null || body.get("reason") == null ? null
                : String.valueOf(body.get("reason"));
        return adminService.disableNewAssignments(driverId, disabled, reason, SecurityUtils.getUserId());
    }

    @GetMapping("/drivers/{driverId}/assignments")
    @PreAuthorize("@ss.hasPermi('biz:assignment:list')")
    public Map<String, Object> driverAssignments(@PathVariable Long driverId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.driverAssignments(driverId, pageNum, pageSize);
    }

    @GetMapping("/drivers/{driverId}/fee-ledger")
    @PreAuthorize("@ss.hasPermi('biz:driverledger:list')")
    public Map<String, Object> driverFeeLedger(@PathVariable Long driverId,
            @RequestParam(required = false) String status)
    {
        return adminService.driverFeeLedger(driverId, status);
    }

    // ------------------------------------------------------------------ 车辆

    @GetMapping("/vehicles")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:list')")
    public Map<String, Object> vehicles(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vehicleTypeCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.vehicles(keyword, vehicleTypeCode, pageNum, pageSize);
    }

    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:query')")
    public Map<String, Object> vehicle(@PathVariable Long vehicleId)
    {
        return adminService.vehicleDetail(vehicleId);
    }

    @PatchMapping("/vehicles/{vehicleId}")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:edit')")
    public Map<String, Object> maintainVehicle(@PathVariable Long vehicleId,
            @RequestBody Map<String, Object> body)
    {
        String remark = body == null || body.get("remark") == null ? null
                : String.valueOf(body.get("remark"));
        String status = body == null || body.get("verificationStatus") == null ? null
                : String.valueOf(body.get("verificationStatus"));
        return adminService.maintainVehicle(vehicleId, remark, status, SecurityUtils.getUserId());
    }

    @GetMapping("/vehicles/{vehicleId}/orders")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:list')")
    public Map<String, Object> vehicleOrders(@PathVariable Long vehicleId)
    {
        return adminService.vehicleOrders(vehicleId);
    }

    @GetMapping("/vehicles/{vehicleId}/inspection-results")
    @PreAuthorize("@ss.hasPermi('biz:result:list')")
    public Map<String, Object> vehicleResults(@PathVariable Long vehicleId)
    {
        return adminService.vehicleResults(vehicleId);
    }

    // ------------------------------------------------------------------ 检测点

    @GetMapping("/stations")
    @PreAuthorize("@ss.hasPermi('biz:station:list')")
    public Map<String, Object> stations(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return adminService.stations(pageNum, pageSize);
    }

    @PostMapping("/stations")
    @PreAuthorize("@ss.hasPermi('biz:station:create')")
    public Map<String, Object> createStation(@RequestBody DriverDtos.StationUpdateReq req)
    {
        return adminService.createStation(req);
    }

    @PutMapping("/stations/{stationId}")
    @PreAuthorize("@ss.hasPermi('biz:station:edit')")
    public Map<String, Object> updateStation(@PathVariable Long stationId,
            @RequestBody DriverDtos.StationUpdateReq req)
    {
        return adminService.updateStation(stationId, req);
    }

    @GetMapping("/station-grants")
    @PreAuthorize("@ss.hasPermi('biz:stationgrant:list')")
    public Map<String, Object> stationGrants(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize)
    {
        return adminService.stationGrants(pageNum, pageSize);
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
        return adminService.updateStationGrant(employeeUserId, stationId, enabled, reason,
                SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 计价

    @GetMapping("/prices")
    @PreAuthorize("@ss.hasPermi('biz:price:list')")
    public Map<String, Object> prices(@RequestParam(required = false) String status,
            @RequestParam(required = false) String vehicleTypeCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return adminService.prices(status, vehicleTypeCode, pageNum, pageSize);
    }

    @PostMapping("/prices")
    @PreAuthorize("@ss.hasPermi('biz:price:create')")
    public Map<String, Object> createPrice(@RequestBody Map<String, Object> body)
    {
        return adminService.createPrice(body, SecurityUtils.getUserId());
    }

    @PutMapping("/prices/{ruleId}")
    @PreAuthorize("@ss.hasPermi('biz:price:edit')")
    public Map<String, Object> updatePrice(@PathVariable Long ruleId,
            @RequestBody Map<String, Object> body)
    {
        return adminService.updatePrice(ruleId, body);
    }

    @PostMapping("/prices/{ruleId}/submit")
    @PreAuthorize("@ss.hasPermi('biz:price:submit')")
    public Map<String, Object> submitPrice(@PathVariable Long ruleId)
    {
        return adminService.submitPrice(ruleId, SecurityUtils.getUserId());
    }

    @PostMapping("/prices/{ruleId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:price:audit')")
    public Map<String, Object> auditPrice(@PathVariable Long ruleId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body == null || body.get("approve") == null
                || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        String opinion = body == null || body.get("opinion") == null ? null
                : String.valueOf(body.get("opinion"));
        return adminService.auditPrice(ruleId, approve, opinion, SecurityUtils.getUserId());
    }

    @PostMapping("/prices/{ruleId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:price:publish')")
    public Map<String, Object> publishPrice(@PathVariable Long ruleId)
    {
        return adminService.publishPrice(ruleId, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 资格审核

    @GetMapping("/driver-qualifications")
    @PreAuthorize("@ss.hasPermi('biz:driverqualification:list')")
    public Map<String, Object> qualifications(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.qualifications(status, pageNum, pageSize);
    }

    @GetMapping("/driver-qualifications/{applicationId}")
    @PreAuthorize("@ss.hasPermi('biz:driverqualification:query')")
    public Map<String, Object> qualification(@PathVariable Long applicationId)
    {
        return adminService.qualificationDetail(applicationId);
    }

    @PostMapping("/driver-qualifications/{applicationId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:driverqualification:audit')")
    public Map<String, Object> auditQualification(@PathVariable Long applicationId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body == null || body.get("approve") == null
                || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        String opinion = body == null || body.get("opinion") == null ? null
                : String.valueOf(body.get("opinion"));
        return adminService.auditQualification(applicationId, approve, opinion, SecurityUtils.getUserId());
    }
}
