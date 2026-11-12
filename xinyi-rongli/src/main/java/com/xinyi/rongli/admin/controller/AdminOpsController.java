package com.xinyi.rongli.admin.controller;

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
import com.xinyi.rongli.admin.service.OpsAdminService;
import com.xinyi.rongli.dispatch.service.DispatchAdminService;
import com.xinyi.rongli.admin.service.OpsAdminService;

/**
 * 管理端 admin 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminOpsController
{
    @Autowired
    private DispatchAdminService dispatchAdminService;
    @Autowired
    private OpsAdminService opsAdminService;

    @GetMapping("/exports")
    @PreAuthorize("@ss.hasPermi('biz:export:list')")
    public Map<String, Object> exports(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return opsAdminService.exports(status, pageNum, pageSize);
    }
    @PostMapping("/orders/export")
    @PreAuthorize("@ss.hasPermi('biz:order:export')")
    public Map<String, Object> requestExport(@RequestBody Map<String, Object> body)
    {
        return opsAdminService.requestExport(body, SecurityUtils.getUserId());
    }
    @PostMapping("/exports/{taskId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:export:audit')")
    public Map<String, Object> auditExport(@PathVariable Long taskId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body == null || body.get("approve") == null
                || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        String opinion = body == null || body.get("opinion") == null ? null
                : String.valueOf(body.get("opinion"));
        return opsAdminService.auditExport(taskId, approve, opinion, SecurityUtils.getUserId());
    }
    @GetMapping("/exports/{taskId}")
    @PreAuthorize("@ss.hasPermi('biz:export:query')")
    public Map<String, Object> exportTask(@PathVariable Long taskId)
    {
        return opsAdminService.exportTask(taskId);
    }
    @GetMapping("/audit-events")
    @PreAuthorize("@ss.hasPermi('biz:audit:list')")
    public Map<String, Object> auditEvents(@RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String aggregateType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actorRealm,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return opsAdminService.auditEvents(orderId, aggregateType, action, actorRealm, result,
                pageNum, pageSize);
    }
    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('biz:statistics:list')")
    public Map<String, Object> statistics(@RequestParam(defaultValue = "14") int days,
            @RequestParam(required = false) String range)
    {
        return opsAdminService.statistics(days, range);
    }
    @GetMapping("/jobs")
    @PreAuthorize("@ss.hasPermi('biz:job:list')")
    public Map<String, Object> jobs(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return opsAdminService.jobs(pageNum, pageSize);
    }
    @PostMapping("/jobs/{taskId}/retry")
    @PreAuthorize("@ss.hasPermi('biz:job:retry')")
    public Map<String, Object> retryJob(@PathVariable Long taskId)
    {
        return opsAdminService.retryJob(taskId);
    }
    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('biz:config:query')")
    public Map<String, Object> config()
    {
        return opsAdminService.businessConfig();
    }
    @PutMapping("/config")
    @PreAuthorize("@ss.hasPermi('biz:config:edit')")
    public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body)
    {
        return dispatchAdminService.saveDispatchConfig(body);
    }
}
