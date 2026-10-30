package com.xinyi.rongli.controller.admin;

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
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.service.AdminService;
import com.xinyi.rongli.service.AfterSaleService;

/**
 * 管理端订单与售后：与检测员工端复用同一套履约口径，只是去掉了站点归属限制。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminOrderController
{
    @Autowired
    private AdminService adminService;

    @Autowired
    private AfterSaleService afterSaleService;

    // ------------------------------------------------------------------ 订单

    @GetMapping("/orders")
    @PreAuthorize("@ss.hasPermi('biz:order:list')")
    public Map<String, Object> orders(@RequestParam(required = false) String status,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.orders(status, group, keyword, stationId,
                com.xinyi.rongli.support.ViTime.parseFlexible(from),
                com.xinyi.rongli.support.ViTime.parseFlexible(to),
                pageNum, pageSize);
    }

    @GetMapping("/orders/{orderNo}")
    @PreAuthorize("@ss.hasPermi('biz:order:query')")
    public Map<String, Object> order(@PathVariable String orderNo)
    {
        return adminService.orderDetail(orderNo);
    }

    @GetMapping("/orders/{orderNo}/license")
    @PreAuthorize("@ss.hasPermi('biz:license:list')")
    public Map<String, Object> license(@PathVariable String orderNo)
    {
        return adminService.license(orderNo);
    }

    @PutMapping("/orders/{orderNo}/license")
    @PreAuthorize("@ss.hasPermi('biz:license:edit')")
    public Map<String, Object> saveLicense(@PathVariable String orderNo,
            @RequestBody DriverDtos.LicenseReviewReq req)
    {
        return adminService.saveLicense(orderNo, req, SecurityUtils.getUserId());
    }

    @GetMapping("/orders/{orderNo}/results")
    @PreAuthorize("@ss.hasPermi('biz:result:list')")
    public Map<String, Object> results(@PathVariable String orderNo)
    {
        return adminService.results(orderNo);
    }

    @PostMapping("/orders/{orderNo}/results")
    @PreAuthorize("@ss.hasPermi('biz:result:create')")
    public Map<String, Object> submitResult(@PathVariable String orderNo,
            @RequestBody DriverDtos.ResultReq req)
    {
        return adminService.submitResult(orderNo, req);
    }

    @PostMapping("/orders/{orderNo}/result-corrections")
    @PreAuthorize("@ss.hasPermi('biz:resultcorrection:create')")
    public Map<String, Object> correction(@PathVariable String orderNo,
            @RequestBody DriverDtos.CorrectionReq req)
    {
        return adminService.applyCorrection(orderNo, req);
    }

    @PostMapping("/orders/{orderNo}/pickup-notification")
    @PreAuthorize("@ss.hasPermi('biz:order:create')")
    public Map<String, Object> pickupNotification(@PathVariable String orderNo)
    {
        return adminService.resendPickupNotification(orderNo);
    }

    @PostMapping("/orders/{orderNo}/exceptions")
    @PreAuthorize("@ss.hasPermi('biz:order:create')")
    public Map<String, Object> exception(@PathVariable String orderNo,
            @RequestBody DriverDtos.ExceptionReq req)
    {
        return adminService.reportException(orderNo, req);
    }

    @GetMapping("/capacity")
    @PreAuthorize("@ss.hasPermi('biz:capacity:list')")
    public Map<String, Object> capacity(@RequestParam Long stationId)
    {
        return adminService.capacity(stationId);
    }

    @PutMapping("/capacity/{slotId}")
    @PreAuthorize("@ss.hasPermi('biz:capacity:edit')")
    public Map<String, Object> updateCapacity(@PathVariable Long slotId,
            @RequestBody DriverDtos.CapacityChangeReq req)
    {
        return adminService.updateCapacity(slotId, req);
    }

    // ------------------------------------------------------------------ 售后

    @PostMapping("/orders/{orderNo}/after-sales")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:create')")
    public Map<String, Object> createAfterSale(@PathVariable String orderNo,
            @RequestBody MiniDtos.AfterSaleApply req)
    {
        return afterSaleService.createByAdmin(orderNo, req, SecurityUtils.getUserId());
    }

    @GetMapping("/orders/{orderNo}/after-sales")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:list')")
    public Map<String, Object> afterSalesOfOrder(@PathVariable String orderNo)
    {
        return afterSaleService.listOfOrder(orderNo, true);
    }

    @GetMapping("/after-sales")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:list')")
    public Map<String, Object> afterSales(@RequestParam(required = false) String status,
            @RequestParam(required = false) String kind,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return afterSaleService.adminCases(status, kind, pageNum, pageSize);
    }

    @GetMapping("/after-sales/{caseId}")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:query')")
    public Map<String, Object> afterSale(@PathVariable Long caseId)
    {
        return afterSaleService.detail(caseId, true);
    }

    @PostMapping("/after-sales/{caseId}/notes")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:create')")
    public Map<String, Object> note(@PathVariable Long caseId, @RequestBody MiniDtos.AfterSaleNote req)
    {
        return afterSaleService.addNote(caseId, req == null ? null : req.content, true);
    }

    @PostMapping("/after-sales/{caseId}/review-preview")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:reviewpreview')")
    public Map<String, Object> reviewPreview(@PathVariable Long caseId)
    {
        return afterSaleService.reviewPreview(caseId);
    }

    @PostMapping("/after-sales/{caseId}/review")
    @PreAuthorize("@ss.hasPermi('biz:aftersale:review')")
    public Map<String, Object> review(@PathVariable Long caseId,
            @RequestBody MiniDtos.AfterSaleReviewReq req)
    {
        boolean approve = req != null && Boolean.TRUE.equals(req.approve);
        return afterSaleService.review(caseId, approve, req == null ? null : req.approvedFen,
                req == null ? null : req.returnRequired, req == null ? null : req.opinion,
                SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 退款运维

    @GetMapping("/refunds/{refundNo}")
    @PreAuthorize("@ss.hasPermi('biz:refund:query')")
    public Map<String, Object> refund(@PathVariable String refundNo)
    {
        return afterSaleService.refundDetail(refundNo);
    }

    @PostMapping("/refunds/{refundNo}/query")
    @PreAuthorize("@ss.hasPermi('biz:refund:query')")
    public Map<String, Object> queryRefund(@PathVariable String refundNo)
    {
        return afterSaleService.queryRefund(refundNo);
    }

    @PostMapping("/refunds/{refundNo}/retry")
    @PreAuthorize("@ss.hasPermi('biz:refund:retry')")
    public Map<String, Object> retryRefund(@PathVariable String refundNo)
    {
        return afterSaleService.retryRefund(refundNo);
    }
}
