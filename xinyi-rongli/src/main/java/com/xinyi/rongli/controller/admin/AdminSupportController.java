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
import com.xinyi.rongli.service.AdminService;
import com.xinyi.rongli.service.MessageService;
import com.xinyi.rongli.service.PenaltyService;
import com.xinyi.rongli.service.TrainingService;

/**
 * 管理端治理与运维：争议、异常、结果更正、对账、导出、审计、统计、配置、补偿、隐私、处罚、培训。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminSupportController
{
    @Autowired
    private AdminService adminService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PenaltyService penaltyService;

    @Autowired
    private TrainingService trainingService;

    // ------------------------------------------------------------------ 争议

    @GetMapping("/disputes")
    @PreAuthorize("@ss.hasPermi('biz:dispute:list')")
    public Map<String, Object> disputes(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.disputes(status, pageNum, pageSize);
    }

    @GetMapping("/disputes/{disputeId}")
    @PreAuthorize("@ss.hasPermi('biz:dispute:query')")
    public Map<String, Object> dispute(@PathVariable Long disputeId)
    {
        return adminService.disputeDetail(disputeId);
    }

    @PostMapping("/disputes/{disputeId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:dispute:resolve')")
    public Map<String, Object> resolveDispute(@PathVariable Long disputeId,
            @RequestBody Map<String, Object> body)
    {
        String conclusion = body == null || body.get("conclusion") == null ? null
                : String.valueOf(body.get("conclusion"));
        return adminService.resolveDispute(disputeId, conclusion, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 异常

    @GetMapping("/exceptions")
    @PreAuthorize("@ss.hasPermi('biz:exception:list')")
    public Map<String, Object> exceptions(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.exceptions(status, pageNum, pageSize);
    }

    @PostMapping("/exceptions/{exceptionId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:exception:resolve')")
    public Map<String, Object> resolveException(@PathVariable Long exceptionId,
            @RequestBody Map<String, Object> body)
    {
        String resolution = body == null || body.get("resolution") == null ? null
                : String.valueOf(body.get("resolution"));
        return adminService.resolveException(exceptionId, resolution, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 结果更正

    @GetMapping("/result-corrections")
    @PreAuthorize("@ss.hasPermi('biz:resultcorrection:list')")
    public Map<String, Object> corrections(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.corrections(status, pageNum, pageSize);
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
        return adminService.auditCorrection(correctionId, approve, opinion, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 对账异常

    @GetMapping("/reconciliation")
    @PreAuthorize("@ss.hasPermi('biz:paymentreconciliation:list')")
    public Map<String, Object> reconciliation(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.reconciliation(status, pageNum, pageSize);
    }

    @PostMapping("/reconciliation/{caseId}/query")
    @PreAuthorize("@ss.hasPermi('biz:paymentreconciliation:query')")
    public Map<String, Object> queryReconciliation(@PathVariable Long caseId)
    {
        return adminService.queryReconciliation(caseId);
    }

    @PostMapping("/reconciliation/{caseId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:paymentreconciliation:resolve')")
    public Map<String, Object> resolveReconciliation(@PathVariable Long caseId,
            @RequestBody Map<String, Object> body)
    {
        String conclusion = body == null || body.get("conclusion") == null ? null
                : String.valueOf(body.get("conclusion"));
        return adminService.resolveReconciliation(caseId, conclusion, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 导出

    @GetMapping("/exports")
    @PreAuthorize("@ss.hasPermi('biz:export:list')")
    public Map<String, Object> exports(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.exports(status, pageNum, pageSize);
    }

    @PostMapping("/orders/export")
    @PreAuthorize("@ss.hasPermi('biz:order:export')")
    public Map<String, Object> requestExport(@RequestBody Map<String, Object> body)
    {
        return adminService.requestExport(body, SecurityUtils.getUserId());
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
        return adminService.auditExport(taskId, approve, opinion, SecurityUtils.getUserId());
    }

    @GetMapping("/exports/{taskId}")
    @PreAuthorize("@ss.hasPermi('biz:export:query')")
    public Map<String, Object> exportTask(@PathVariable Long taskId)
    {
        return adminService.exportTask(taskId);
    }

    // ------------------------------------------------------------------ 审计与统计

    @GetMapping("/audit-events")
    @PreAuthorize("@ss.hasPermi('biz:audit:list')")
    public Map<String, Object> auditEvents(@RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String aggregateType,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return adminService.auditEvents(orderId, aggregateType, action, pageNum, pageSize);
    }

    @GetMapping("/statistics")
    @PreAuthorize("@ss.hasPermi('biz:statistics:list')")
    public Map<String, Object> statistics(@RequestParam(defaultValue = "14") int days)
    {
        return adminService.statistics(days);
    }

    // ------------------------------------------------------------------ 通知模板

    @GetMapping("/notification-templates")
    @PreAuthorize("@ss.hasPermi('biz:notificationtemplate:list')")
    public Map<String, Object> templates()
    {
        return adminService.notificationTemplates();
    }

    @PutMapping("/notification-templates/{templateId}")
    @PreAuthorize("@ss.hasPermi('biz:notificationtemplate:edit')")
    public Map<String, Object> saveTemplate(@PathVariable String templateId,
            @RequestBody Map<String, Object> body)
    {
        String content = body == null || body.get("content") == null ? null
                : String.valueOf(body.get("content"));
        return adminService.saveNotificationTemplate(templateId, content, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 补偿任务

    @GetMapping("/jobs")
    @PreAuthorize("@ss.hasPermi('biz:job:list')")
    public Map<String, Object> jobs(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return adminService.jobs(pageNum, pageSize);
    }

    @PostMapping("/jobs/{taskId}/retry")
    @PreAuthorize("@ss.hasPermi('biz:job:retry')")
    public Map<String, Object> retryJob(@PathVariable Long taskId)
    {
        return adminService.retryJob(taskId);
    }

    // ------------------------------------------------------------------ 隐私

    @GetMapping("/privacy-cases")
    @PreAuthorize("@ss.hasPermi('biz:privacycase:list')")
    public Map<String, Object> privacyCases(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return messageService.adminPrivacyCases(status, pageNum, pageSize);
    }

    @PostMapping("/privacy-cases/{caseId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:privacycase:resolve')")
    public Map<String, Object> resolvePrivacy(@PathVariable Long caseId,
            @RequestBody Map<String, Object> body)
    {
        String status = body == null || body.get("status") == null ? null
                : String.valueOf(body.get("status"));
        String conclusion = body == null || body.get("conclusion") == null ? null
                : String.valueOf(body.get("conclusion"));
        return messageService.resolvePrivacyCase(caseId, status, conclusion, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 配置

    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermi('biz:config:query')")
    public Map<String, Object> config()
    {
        return adminService.businessConfig();
    }

    @PutMapping("/config")
    @PreAuthorize("@ss.hasPermi('biz:config:edit')")
    public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body)
    {
        return adminService.saveDispatchConfig(body);
    }

    // ------------------------------------------------------------------ 员工绑定

    @GetMapping("/employee-binding-requests")
    @PreAuthorize("@ss.hasPermi('biz:employeebinding:list')")
    public Map<String, Object> bindingRequests(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return adminService.bindingRequests(pageNum, pageSize);
    }

    @PostMapping("/employee-binding-credentials")
    @PreAuthorize("@ss.hasPermi('biz:employeebinding:issue')")
    public Map<String, Object> issueCredential(@RequestBody Map<String, Object> body)
    {
        Long requestId = body == null || body.get("requestId") == null ? null
                : Long.valueOf(String.valueOf(body.get("requestId")));
        Long employeeUserId = body == null || body.get("employeeUserId") == null ? null
                : Long.valueOf(String.valueOf(body.get("employeeUserId")));
        if (requestId == null || employeeUserId == null)
        {
            throw com.xinyi.rongli.support.ApiException.validation("请指定绑定申请与员工账号");
        }
        return adminService.issueBindingCredential(requestId, employeeUserId, SecurityUtils.getUserId());
    }

    @PostMapping("/employee-binding-credentials/{credentialId}/revoke")
    @PreAuthorize("@ss.hasPermi('biz:employeebinding:revoke')")
    public Map<String, Object> revokeCredential(@PathVariable Long credentialId)
    {
        return adminService.revokeBindingCredential(credentialId, SecurityUtils.getUserId());
    }

    // ------------------------------------------------------------------ 处罚与申诉

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

    // ------------------------------------------------------------------ 培训课程

    @GetMapping("/training-courses")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:list')")
    public Map<String, Object> courses(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return trainingService.adminCourses(status, pageNum, pageSize);
    }

    @PostMapping("/training-courses")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:create')")
    public Map<String, Object> createCourse(@RequestBody DriverDtos.TrainingCourseReq req)
    {
        return trainingService.saveCourse(null, req, SecurityUtils.getUserId());
    }

    @GetMapping("/training-courses/{trainingId}")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:query')")
    public Map<String, Object> course(@PathVariable Long trainingId)
    {
        return trainingService.adminCourse(trainingId);
    }

    @PutMapping("/training-courses/{trainingId}")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:edit')")
    public Map<String, Object> updateCourse(@PathVariable Long trainingId,
            @RequestBody DriverDtos.TrainingCourseReq req)
    {
        return trainingService.saveCourse(trainingId, req, SecurityUtils.getUserId());
    }

    @PostMapping("/training-courses/{trainingId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:publish')")
    public Map<String, Object> publishCourse(@PathVariable Long trainingId)
    {
        return trainingService.publishCourse(trainingId, SecurityUtils.getUserId());
    }
}
