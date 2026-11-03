package com.xinyi.rongli.aftersale.controller;

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
import com.xinyi.rongli.admin.service.AdminService;
import com.xinyi.rongli.notification.service.MessageService;
import com.xinyi.rongli.driver.service.PenaltyService;
import com.xinyi.rongli.driver.service.TrainingService;
import com.xinyi.rongli.aftersale.service.AfterSaleAdminService;
import com.xinyi.rongli.fulfill.service.FulfillAdminService;

/**
 * 管理端 aftersale 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAfterSaleController
{
    @Autowired
    private AfterSaleAdminService afterSaleAdminService;
    @Autowired
    private FulfillAdminService fulfillAdminService;

    @GetMapping("/disputes")
    @PreAuthorize("@ss.hasPermi('biz:dispute:list')")
    public Map<String, Object> disputes(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return afterSaleAdminService.disputes(status, pageNum, pageSize);
    }
    @GetMapping("/disputes/{disputeId}")
    @PreAuthorize("@ss.hasPermi('biz:dispute:query')")
    public Map<String, Object> dispute(@PathVariable Long disputeId)
    {
        return afterSaleAdminService.disputeDetail(disputeId);
    }
    @PostMapping("/disputes/{disputeId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:dispute:resolve')")
    public Map<String, Object> resolveDispute(@PathVariable Long disputeId,
            @RequestBody Map<String, Object> body)
    {
        String conclusion = body == null || body.get("conclusion") == null ? null
                : String.valueOf(body.get("conclusion"));
        return afterSaleAdminService.resolveDispute(disputeId, conclusion, SecurityUtils.getUserId());
    }
    @GetMapping("/exceptions")
    @PreAuthorize("@ss.hasPermi('biz:exception:list')")
    public Map<String, Object> exceptions(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return afterSaleAdminService.exceptions(status, pageNum, pageSize);
    }
    @PostMapping("/exceptions/{exceptionId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:exception:resolve')")
    public Map<String, Object> resolveException(@PathVariable Long exceptionId,
            @RequestBody Map<String, Object> body)
    {
        String resolution = body == null || body.get("resolution") == null ? null
                : String.valueOf(body.get("resolution"));
        return afterSaleAdminService.resolveException(exceptionId, resolution, SecurityUtils.getUserId());
    }
    @GetMapping("/result-corrections")
    @PreAuthorize("@ss.hasPermi('biz:resultcorrection:list')")
    public Map<String, Object> corrections(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return fulfillAdminService.corrections(status, pageNum, pageSize);
    }
    @PostMapping("/result-corrections/{correctionId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:resultcorrection:audit')")
    public Map<String, Object> auditCorrection(@PathVariable Long correctionId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body == null || body.get("approve") == null
                || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        String opinion = body == null || body.get("opinion") == null ? null
                : String.valueOf(body.get("opinion"));
        return fulfillAdminService.auditCorrection(correctionId, approve, opinion, SecurityUtils.getUserId());
    }
}
