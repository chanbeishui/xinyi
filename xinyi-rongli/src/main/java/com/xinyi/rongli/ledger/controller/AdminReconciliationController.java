package com.xinyi.rongli.ledger.controller;

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
import com.xinyi.rongli.admin.service.OpsAdminService;
import com.xinyi.rongli.dispatch.service.DispatchAdminService;
import com.xinyi.rongli.admin.service.OpsAdminService;
import com.xinyi.rongli.ledger.service.LedgerAdminService;

/**
 * 管理端 ledger 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminReconciliationController
{
    @Autowired
    private LedgerAdminService ledgerAdminService;

    @GetMapping("/reconciliation")
    @PreAuthorize("@ss.hasPermi('biz:paymentreconciliation:list')")
    public Map<String, Object> reconciliation(@RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return ledgerAdminService.reconciliation(status, keyword, pageNum, pageSize);
    }
    @PostMapping("/reconciliation/{caseId}/query")
    @PreAuthorize("@ss.hasPermi('biz:paymentreconciliation:query')")
    public Map<String, Object> queryReconciliation(@PathVariable Long caseId)
    {
        return ledgerAdminService.queryReconciliation(caseId);
    }
    @PostMapping("/reconciliation/{caseId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:paymentreconciliation:resolve')")
    public Map<String, Object> resolveReconciliation(@PathVariable Long caseId,
            @RequestBody Map<String, Object> body)
    {
        String conclusion = body == null || body.get("conclusion") == null ? null
                : String.valueOf(body.get("conclusion"));
        return ledgerAdminService.resolveReconciliation(caseId, conclusion, SecurityUtils.getUserId());
    }
}
