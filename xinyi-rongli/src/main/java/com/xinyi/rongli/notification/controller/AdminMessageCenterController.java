package com.xinyi.rongli.notification.controller;

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

/**
 * 管理端 notification 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminMessageCenterController
{
    @Autowired
    private MessageService messageService;
    @Autowired
    private OpsAdminService opsAdminService;

    @GetMapping("/notification-templates")
    @PreAuthorize("@ss.hasPermi('biz:notificationtemplate:list')")
    public Map<String, Object> templates()
    {
        return opsAdminService.notificationTemplates();
    }
    @PutMapping("/notification-templates/{templateId}")
    @PreAuthorize("@ss.hasPermi('biz:notificationtemplate:edit')")
    public Map<String, Object> saveTemplate(@PathVariable String templateId,
            @RequestBody Map<String, Object> body)
    {
        String content = body == null || body.get("content") == null ? null
                : String.valueOf(body.get("content"));
        return opsAdminService.saveNotificationTemplate(templateId, content, SecurityUtils.getUserId());
    }
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
}
