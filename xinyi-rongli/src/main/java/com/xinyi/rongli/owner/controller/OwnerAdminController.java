package com.xinyi.rongli.owner.controller;

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

/**
 * 管理端 owner 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class OwnerAdminController
{
    @Autowired
    private OwnerAdminService ownerAdminService;

    @GetMapping("/owners")
    @PreAuthorize("@ss.hasPermi('biz:owner:list')")
    public Map<String, Object> owners(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return ownerAdminService.owners(keyword, pageNum, pageSize);
    }
    @GetMapping("/owners/{ownerId}")
    @PreAuthorize("@ss.hasPermi('biz:owner:query')")
    public Map<String, Object> owner(@PathVariable Long ownerId)
    {
        return ownerAdminService.ownerDetail(ownerId);
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
        return ownerAdminService.maintainOwner(ownerId, flag, remark, SecurityUtils.getUserId());
    }
    @GetMapping("/owners/{ownerId}/vehicles")
    @PreAuthorize("@ss.hasPermi('biz:owner:list')")
    public Map<String, Object> ownerVehicles(@PathVariable Long ownerId)
    {
        return ownerAdminService.ownerVehicles(ownerId);
    }
    @GetMapping("/owners/{ownerId}/orders")
    @PreAuthorize("@ss.hasPermi('biz:owner:list')")
    public Map<String, Object> ownerOrders(@PathVariable Long ownerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return ownerAdminService.ownerOrders(ownerId, pageNum, pageSize);
    }
    @GetMapping("/owners/{ownerId}/after-sales")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:list')")
    public Map<String, Object> ownerAfterSales(@PathVariable Long ownerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return ownerAdminService.ownerAfterSales(ownerId, pageNum, pageSize);
    }
}
