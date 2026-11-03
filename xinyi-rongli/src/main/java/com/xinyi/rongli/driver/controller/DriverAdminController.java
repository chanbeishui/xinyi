package com.xinyi.rongli.driver.controller;

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
import com.xinyi.rongli.admin.service.AdminService;
import com.xinyi.rongli.owner.service.OwnerAdminService;
import com.xinyi.rongli.driver.service.DriverAdminService;
import com.xinyi.rongli.ledger.service.LedgerAdminService;

/**
 * 管理端 driver 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class DriverAdminController
{
    @Autowired
    private DriverAdminService driverAdminService;
    @Autowired
    private LedgerAdminService ledgerAdminService;

    @GetMapping("/drivers")
    @PreAuthorize("@ss.hasPermi('biz:driver:list')")
    public Map<String, Object> drivers(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return driverAdminService.drivers(keyword, status, pageNum, pageSize);
    }
    @GetMapping("/drivers/{driverId}")
    @PreAuthorize("@ss.hasPermi('biz:driver:query')")
    public Map<String, Object> driver(@PathVariable Long driverId)
    {
        return driverAdminService.driverDetail(driverId);
    }
    @PatchMapping("/drivers/{driverId}")
    @PreAuthorize("@ss.hasPermi('biz:driver:edit')")
    public Map<String, Object> maintainDriver(@PathVariable Long driverId,
            @RequestBody Map<String, Object> body)
    {
        String remark = body == null || body.get("remark") == null ? null
                : String.valueOf(body.get("remark"));
        Boolean enabled = body == null || body.get("newAssignmentEnabled") == null ? null
                : Boolean.valueOf(String.valueOf(body.get("newAssignmentEnabled")));
        return driverAdminService.maintainDriver(driverId, remark, enabled, SecurityUtils.getUserId());
    }
    @PostMapping("/drivers/{driverId}/disable-new-assignments")
    @PreAuthorize("@ss.hasPermi('biz:driver:disablenewassignments')")
    public Map<String, Object> disableNewAssignments(@PathVariable Long driverId,
            @RequestBody Map<String, Object> body)
    {
        boolean disabled = body == null || body.get("disabled") == null
                || Boolean.parseBoolean(String.valueOf(body.get("disabled")));
        String reason = body == null || body.get("reason") == null ? null
                : String.valueOf(body.get("reason"));
        return driverAdminService.disableNewAssignments(driverId, disabled, reason, SecurityUtils.getUserId());
    }
    @GetMapping("/drivers/{driverId}/assignments")
    @PreAuthorize("@ss.hasPermi('biz:assignment:list')")
    public Map<String, Object> driverAssignments(@PathVariable Long driverId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return driverAdminService.driverAssignments(driverId, pageNum, pageSize);
    }
    @GetMapping("/drivers/{driverId}/fee-ledger")
    @PreAuthorize("@ss.hasPermi('biz:driverledger:list')")
    public Map<String, Object> driverFeeLedger(@PathVariable Long driverId,
            @RequestParam(required = false) String status)
    {
        return ledgerAdminService.driverFeeLedger(driverId, status);
    }
    @GetMapping("/driver-qualifications")
    @PreAuthorize("@ss.hasPermi('biz:driverqualification:list')")
    public Map<String, Object> qualifications(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return driverAdminService.qualifications(status, pageNum, pageSize);
    }
    @GetMapping("/driver-qualifications/{applicationId}")
    @PreAuthorize("@ss.hasPermi('biz:driverqualification:query')")
    public Map<String, Object> qualification(@PathVariable Long applicationId)
    {
        return driverAdminService.qualificationDetail(applicationId);
    }
    @PostMapping("/driver-qualifications/{applicationId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:driverqualification:audit')")
    public Map<String, Object> auditQualification(@PathVariable Long applicationId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body == null || body.get("approve") == null
                || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        String opinion = body == null || body.get("opinion") == null ? null
                : String.valueOf(body.get("opinion"));
        return driverAdminService.auditQualification(applicationId, approve, opinion, SecurityUtils.getUserId());
    }
}
