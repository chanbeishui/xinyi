package com.xinyi.rongli.ledger.controller;

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
import com.xinyi.rongli.station.service.StationAdminService;
import com.xinyi.rongli.ledger.service.LedgerAdminService;

/**
 * 管理端 ledger 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminPriceController
{
    @Autowired
    private LedgerAdminService ledgerAdminService;

    @GetMapping("/prices")
    @PreAuthorize("@ss.hasPermi('biz:price:list')")
    public Map<String, Object> prices(@RequestParam(required = false) String status,
            @RequestParam(required = false) String vehicleTypeCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return ledgerAdminService.prices(status, vehicleTypeCode, pageNum, pageSize);
    }
    @PostMapping("/prices")
    @PreAuthorize("@ss.hasPermi('biz:price:create')")
    public Map<String, Object> createPrice(@RequestBody Map<String, Object> body)
    {
        return ledgerAdminService.createPrice(body, SecurityUtils.getUserId());
    }
    @PutMapping("/prices/{ruleId}")
    @PreAuthorize("@ss.hasPermi('biz:price:edit')")
    public Map<String, Object> updatePrice(@PathVariable Long ruleId,
            @RequestBody Map<String, Object> body)
    {
        return ledgerAdminService.updatePrice(ruleId, body);
    }
    @PostMapping("/prices/{ruleId}/submit")
    @PreAuthorize("@ss.hasPermi('biz:price:submit')")
    public Map<String, Object> submitPrice(@PathVariable Long ruleId)
    {
        return ledgerAdminService.submitPrice(ruleId, SecurityUtils.getUserId());
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
        return ledgerAdminService.auditPrice(ruleId, approve, opinion, SecurityUtils.getUserId());
    }
    @PostMapping("/prices/{ruleId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:price:publish')")
    public Map<String, Object> publishPrice(@PathVariable Long ruleId)
    {
        return ledgerAdminService.publishPrice(ruleId, SecurityUtils.getUserId());
    }
}
