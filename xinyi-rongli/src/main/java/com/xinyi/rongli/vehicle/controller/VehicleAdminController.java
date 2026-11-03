package com.xinyi.rongli.vehicle.controller;

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
import com.xinyi.rongli.owner.service.OwnerAdminService;
import com.xinyi.rongli.driver.service.DriverAdminService;
import com.xinyi.rongli.ledger.service.LedgerAdminService;
import com.xinyi.rongli.vehicle.service.VehicleAdminService;

/**
 * 管理端 vehicle 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class VehicleAdminController
{
    @Autowired
    private VehicleAdminService vehicleAdminService;

    @GetMapping("/vehicles")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:list')")
    public Map<String, Object> vehicles(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vehicleTypeCode,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return vehicleAdminService.vehicles(keyword, vehicleTypeCode, verificationStatus, pageNum, pageSize);
    }
    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:query')")
    public Map<String, Object> vehicle(@PathVariable Long vehicleId)
    {
        return vehicleAdminService.vehicleDetail(vehicleId);
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
        return vehicleAdminService.maintainVehicle(vehicleId, remark, status, SecurityUtils.getUserId());
    }
    @GetMapping("/vehicles/{vehicleId}/orders")
    @PreAuthorize("@ss.hasPermi('biz:vehicle:list')")
    public Map<String, Object> vehicleOrders(@PathVariable Long vehicleId)
    {
        return vehicleAdminService.vehicleOrders(vehicleId);
    }
    @GetMapping("/vehicles/{vehicleId}/inspection-results")
    @PreAuthorize("@ss.hasPermi('biz:result:list')")
    public Map<String, Object> vehicleResults(@PathVariable Long vehicleId)
    {
        return vehicleAdminService.vehicleResults(vehicleId);
    }
}
