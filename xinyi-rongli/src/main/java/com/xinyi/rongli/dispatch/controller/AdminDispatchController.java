package com.xinyi.rongli.dispatch.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.dispatch.service.DispatchAdminService;
import com.xinyi.rongli.admin.service.OpsAdminService;

/**
 * 管理端工作台与派单调度。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDispatchController
{
    @Autowired
    private DispatchAdminService dispatchAdminService;
    @Autowired
    private OpsAdminService opsAdminService;

    @GetMapping("/dashboard")
    @PreAuthorize("@ss.hasPermi('biz:statistics:list')")
    public Map<String, Object> dashboard()
    {
        return opsAdminService.dashboard();
    }

    @GetMapping("/dispatch/queue")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:list')")
    public Map<String, Object> queue()
    {
        return dispatchAdminService.dispatchQueue();
    }

    @GetMapping("/dispatch/candidates")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:list')")
    public Map<String, Object> candidates(@RequestParam(required = false) String keyword)
    {
        return dispatchAdminService.dispatchCandidates(keyword);
    }

    @PostMapping("/dispatch/manual-invites")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:create')")
    public Map<String, Object> manualInvite(@RequestBody Map<String, Object> body)
    {
        String orderNo = body == null ? null : str(body.get("orderNo"));
        Long driverId = body == null || body.get("driverId") == null ? null
                : Long.valueOf(String.valueOf(body.get("driverId")));
        String reason = body == null ? null : str(body.get("reason"));
        if (orderNo == null || driverId == null)
        {
            throw com.xinyi.rongli.common.support.ApiException.validation("请指定订单与代驾");
        }
        return dispatchAdminService.manualInvite(orderNo, driverId, reason);
    }

    /** 立即派单：跳过「接车前提前量」的等待，按自动派单规则当场发起一轮邀约 */
    @PostMapping("/dispatch/orders/{orderNo}/dispatch-now")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:create')")
    public Map<String, Object> dispatchNow(@org.springframework.web.bind.annotation.PathVariable String orderNo)
    {
        return dispatchAdminService.dispatchNow(orderNo);
    }

    @GetMapping("/dispatch/invites")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:list')")
    public Map<String, Object> invites(@RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return dispatchAdminService.dispatchInvites(orderId, driverId, status, keyword, pageNum, pageSize);
    }

    @GetMapping("/dispatch/config")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:query')")
    public Map<String, Object> config()
    {
        return dispatchAdminService.dispatchConfig();
    }

    @PutMapping("/dispatch/config")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:edit')")
    public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body)
    {
        return dispatchAdminService.saveDispatchConfig(body);
    }

    @PostMapping("/dispatch/plans/preview")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:preview')")
    public Map<String, Object> previewPlan(@RequestBody(required = false) Map<String, Object> body)
    {
        Integer lead = body == null || body.get("leadMinutes") == null ? null
                : Integer.valueOf(String.valueOf(body.get("leadMinutes")));
        return dispatchAdminService.previewDispatchPlan(lead);
    }

    @PostMapping("/dispatch/plans/{taskId}/apply")
    @PreAuthorize("@ss.hasPermi('biz:dispatch:create')")
    public Map<String, Object> applyPlan(@org.springframework.web.bind.annotation.PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        Integer lead = body == null || body.get("leadMinutes") == null ? null
                : Integer.valueOf(String.valueOf(body.get("leadMinutes")));
        return dispatchAdminService.applyDispatchPlan(lead);
    }

    private String str(Object v)
    {
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).trim();
    }
}
