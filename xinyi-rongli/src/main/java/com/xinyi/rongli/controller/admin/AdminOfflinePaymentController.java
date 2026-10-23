package com.xinyi.rongli.controller.admin;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.annotation.Log;
import com.xinyi.common.enums.BusinessType;
import com.xinyi.rongli.integration.IntegrationPaymentService;

/**
 * 客户线下收退款凭证（B52）。
 *
 * <p>登记与核实分属两个权限：可登记的人不一定能核实，核实人也不能是自己登记的那笔。
 * 这里处理的是客户资金，与第19章“付款给代驾”的凭证是相反方向，不能共表冲销。</p>
 */
@RestController
@RequestMapping("/api/admin/offline-payments")
public class AdminOfflinePaymentController
{
    @Autowired
    private IntegrationPaymentService paymentService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('biz:offlinepayment:list')")
    public Map<String, Object> list(@RequestParam(required = false) String ownerDeptId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String direction,
            @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        return paymentService.listProofs(ownerDeptId, status, direction, pageNum, pageSize);
    }

    @GetMapping("/{proofId}")
    @PreAuthorize("@ss.hasPermi('biz:offlinepayment:list')")
    public Map<String, Object> detail(@PathVariable Long proofId)
    {
        return paymentService.proofDetail(proofId);
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('biz:offlinepayment:register')")
    @Log(title = "登记客户线下收退款", businessType = BusinessType.INSERT)
    public Map<String, Object> register(@RequestBody Map<String, Object> body)
    {
        return paymentService.registerProof(body);
    }

    @PostMapping("/{proofId}/verify")
    @PreAuthorize("@ss.hasPermi('biz:offlinepayment:verify')")
    @Log(title = "核实客户线下资金", businessType = BusinessType.UPDATE)
    public Map<String, Object> verify(@PathVariable Long proofId, @RequestBody Map<String, Object> body)
    {
        return paymentService.verifyProof(proofId, body);
    }
}
