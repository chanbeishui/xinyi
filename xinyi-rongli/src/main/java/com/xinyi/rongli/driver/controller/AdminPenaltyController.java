package com.xinyi.rongli.driver.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.notification.service.MessageService;
import com.xinyi.rongli.driver.service.PenaltyService;
import com.xinyi.rongli.driver.service.TrainingService;
import com.xinyi.rongli.aftersale.service.AfterSaleAdminService;
import com.xinyi.rongli.fulfill.service.FulfillAdminService;

/**
 * 管理端 driver 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminPenaltyController
{
    @Autowired
    private PenaltyService penaltyService;

    @GetMapping("/penalties")
    @PreAuthorize("@ss.hasPermi('biz:penalty:list')")
    public Map<String, Object> penalties(@RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return penaltyService.adminPenalties(driverId, status, pageNum, pageSize);
    }
    @GetMapping("/penalties/{penaltyId}")
    @PreAuthorize("@ss.hasPermi('biz:penalty:query')")
    public Map<String, Object> penalty(@PathVariable Long penaltyId)
    {
        return penaltyService.adminPenalty(penaltyId);
    }
    @GetMapping("/penalty-appeals")
    @PreAuthorize("@ss.hasPermi('biz:penaltyappeal:list')")
    public Map<String, Object> appeals(@RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return penaltyService.adminAppeals(driverId, status, pageNum, pageSize);
    }
    @GetMapping("/penalty-appeals/{appealId}")
    @PreAuthorize("@ss.hasPermi('biz:penaltyappeal:query')")
    public Map<String, Object> appeal(@PathVariable Long appealId)
    {
        return penaltyService.adminAppeal(appealId);
    }
    @PostMapping("/penalty-appeals/{appealId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:penaltyappeal:audit')")
    public Map<String, Object> auditAppeal(@PathVariable Long appealId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body == null || body.get("approve") == null
                || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        String opinion = body == null || body.get("opinion") == null ? null
                : String.valueOf(body.get("opinion"));
        return penaltyService.auditAppeal(appealId, approve, opinion, SecurityUtils.getUserId());
    }
}
