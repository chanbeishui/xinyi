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
import com.xinyi.common.annotation.Log;
import com.xinyi.common.enums.BusinessType;
import com.xinyi.rongli.integration.IntegrationConfigService;
import com.xinyi.rongli.integration.IntegrationMessageService;

/**
 * 第三方服务接入配置（V6.4 第23章 / T3.4）。
 *
 * <p>B46 支付方式、B47 微信商户、B48 短信通道、B49 短信模板、B50 订阅模板、B51 发布与诊断、
 * B53 小程序凭据的后台接口。所有写操作都要求“保存草稿 → 校验 → 发布”，
 * 密钥只写不读，接口与日志都不回显明文。</p>
 */
@RestController
@RequestMapping("/api/admin/integrations")
public class AdminIntegrationController
{
    @Autowired
    private IntegrationConfigService configService;

    @Autowired
    private IntegrationMessageService messageService;

    // ------------------------------------------------------------ 配置档案

    @GetMapping("/profiles")
    @PreAuthorize("@ss.hasPermi('biz:integration:list')")
    public Map<String, Object> profiles(@RequestParam(required = false) String kind,
            @RequestParam(required = false) String ownerDeptId,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize)
    {
        return configService.list(kind, ownerDeptId, environment, pageNum, pageSize);
    }

    @PostMapping("/profiles")
    @PreAuthorize("@ss.hasPermi('biz:integration:edit')")
    @Log(title = "第三方配置档案", businessType = BusinessType.INSERT)
    public Map<String, Object> createProfile(@RequestBody Map<String, Object> body)
    {
        return configService.create(body);
    }

    @GetMapping("/profiles/{profileId}")
    @PreAuthorize("@ss.hasPermi('biz:integration:list')")
    public Map<String, Object> profile(@PathVariable Long profileId)
    {
        return configService.view(profileId);
    }

    @PutMapping("/profiles/{profileId}/draft")
    @PreAuthorize("@ss.hasPermi('biz:integration:edit')")
    @Log(title = "第三方配置草稿", businessType = BusinessType.UPDATE)
    public Map<String, Object> saveDraft(@PathVariable Long profileId, @RequestBody Map<String, Object> body)
    {
        return configService.saveDraft(profileId, body);
    }

    @PostMapping("/profiles/{profileId}/checks")
    @PreAuthorize("@ss.hasPermi('biz:integration:check')")
    public Map<String, Object> check(@PathVariable Long profileId, @RequestBody Map<String, Object> body)
    {
        return configService.check(profileId, body);
    }

    @PostMapping("/profiles/{profileId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:integration:publish')")
    @Log(title = "第三方配置发布", businessType = BusinessType.UPDATE)
    public Map<String, Object> publish(@PathVariable Long profileId, @RequestBody Map<String, Object> body)
    {
        return configService.publish(profileId, body);
    }

    @PostMapping("/profiles/{profileId}/rollback")
    @PreAuthorize("@ss.hasPermi('biz:integration:publish')")
    @Log(title = "第三方配置回滚", businessType = BusinessType.UPDATE)
    public Map<String, Object> rollback(@PathVariable Long profileId, @RequestBody Map<String, Object> body)
    {
        return configService.rollback(profileId, body);
    }

    @GetMapping("/profiles/{profileId}/revisions")
    @PreAuthorize("@ss.hasPermi('biz:integration:list')")
    public Map<String, Object> revisions(@PathVariable Long profileId,
            @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        return configService.revisions(profileId, pageNum, pageSize);
    }

    @GetMapping("/profiles/{profileId}/publications")
    @PreAuthorize("@ss.hasPermi('biz:integration:list')")
    public Map<String, Object> publications(@PathVariable Long profileId,
            @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        return configService.publications(profileId, pageNum, pageSize);
    }

    // ------------------------------------------------------------ 测试收件人

    @GetMapping("/test-recipients")
    @PreAuthorize("@ss.hasPermi('biz:integration:check')")
    public Map<String, Object> testRecipients(@RequestParam(required = false) String ownerDeptId,
            @RequestParam(required = false) String environment)
    {
        return configService.testRecipients(ownerDeptId, environment);
    }

    @PostMapping("/test-recipients")
    @PreAuthorize("@ss.hasPermi('biz:integration:check')")
    @Log(title = "登记测试收件人", businessType = BusinessType.INSERT)
    public Map<String, Object> addTestRecipient(@RequestBody Map<String, Object> body)
    {
        return configService.addTestRecipient(body);
    }

    @PostMapping("/test-recipients/{recipientId}/revoke")
    @PreAuthorize("@ss.hasPermi('biz:integration:check')")
    @Log(title = "撤销测试收件人", businessType = BusinessType.UPDATE)
    public Map<String, Object> revokeTestRecipient(@PathVariable Long recipientId)
    {
        return configService.revokeTestRecipient(recipientId);
    }

    // ------------------------------------------------------------ 模板

    @GetMapping("/templates")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:list')")
    public Map<String, Object> templates(@RequestParam(required = false) String provider,
            @RequestParam(required = false) String scene, @RequestParam(required = false) String ownerDeptId,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        return messageService.listTemplates(provider, scene, ownerDeptId, environment, pageNum, pageSize);
    }

    @PostMapping("/templates")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:edit')")
    @Log(title = "新建消息模板", businessType = BusinessType.INSERT)
    public Map<String, Object> createTemplate(@RequestBody Map<String, Object> body)
    {
        return messageService.createTemplate(body);
    }

    @GetMapping("/templates/{templateId}")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:list')")
    public Map<String, Object> template(@PathVariable Long templateId)
    {
        return messageService.viewTemplate(templateId);
    }

    @PutMapping("/templates/{templateId}/draft")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:edit')")
    @Log(title = "模板草稿", businessType = BusinessType.UPDATE)
    public Map<String, Object> saveTemplateDraft(@PathVariable Long templateId, @RequestBody Map<String, Object> body)
    {
        return messageService.saveTemplateDraft(templateId, body);
    }

    @PostMapping("/templates/{templateId}/checks")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:edit')")
    public Map<String, Object> checkTemplate(@PathVariable Long templateId, @RequestBody Map<String, Object> body)
    {
        return messageService.checkTemplate(templateId, body);
    }

    @PostMapping("/templates/{templateId}/preview")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:list')")
    public Map<String, Object> previewTemplate(@PathVariable Long templateId, @RequestBody Map<String, Object> body)
    {
        return messageService.previewTemplate(templateId, body);
    }

    @PostMapping("/templates/{templateId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:edit')")
    @Log(title = "模板发布", businessType = BusinessType.UPDATE)
    public Map<String, Object> publishTemplate(@PathVariable Long templateId, @RequestBody Map<String, Object> body)
    {
        return messageService.publishTemplate(templateId, body);
    }

    @GetMapping("/templates/{templateId}/publications")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:list')")
    public Map<String, Object> templatePublications(@PathVariable Long templateId,
            @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        return messageService.templatePublications(templateId, pageNum, pageSize);
    }

    @PostMapping("/templates/{templateId}/test")
    @PreAuthorize("@ss.hasPermi('biz:messagetemplate:test')")
    @Log(title = "模板真实测试发送", businessType = BusinessType.OTHER)
    public Map<String, Object> testTemplate(@PathVariable Long templateId, @RequestBody Map<String, Object> body)
    {
        return messageService.testTemplate(templateId, body);
    }

    // ------------------------------------------------------------ 场景路由

    @GetMapping("/routes")
    @PreAuthorize("@ss.hasPermi('biz:messageroute:edit')")
    public Map<String, Object> routes(@RequestParam(required = false) String ownerDeptId,
            @RequestParam(required = false) String environment)
    {
        return messageService.listRoutes(ownerDeptId, environment);
    }

    @PostMapping("/routes")
    @PreAuthorize("@ss.hasPermi('biz:messageroute:edit')")
    @Log(title = "新建通知路由", businessType = BusinessType.INSERT)
    public Map<String, Object> createRoute(@RequestBody Map<String, Object> body)
    {
        return messageService.createRoute(body);
    }

    @PutMapping("/routes/{routeId}")
    @PreAuthorize("@ss.hasPermi('biz:messageroute:edit')")
    @Log(title = "保存通知路由", businessType = BusinessType.UPDATE)
    public Map<String, Object> updateRoute(@PathVariable Long routeId, @RequestBody Map<String, Object> body)
    {
        return messageService.updateRoute(routeId, body);
    }

    // ------------------------------------------------------------ 发送诊断

    @GetMapping("/deliveries")
    @PreAuthorize("@ss.hasPermi('biz:delivery:list')")
    public Map<String, Object> deliveries(@RequestParam(required = false) String status,
            @RequestParam(required = false) String provider, @RequestParam(required = false) String scene,
            @RequestParam(required = false) String ownerDeptId,
            @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        return messageService.listDeliveries(status, provider, scene, ownerDeptId, pageNum, pageSize);
    }

    @PostMapping("/deliveries/{deliveryId}/query")
    @PreAuthorize("@ss.hasPermi('biz:delivery:operate')")
    public Map<String, Object> queryReceipt(@PathVariable Long deliveryId, @RequestBody Map<String, Object> body)
    {
        return messageService.queryReceipt(deliveryId, body);
    }

    @PostMapping("/deliveries/{deliveryId}/retry")
    @PreAuthorize("@ss.hasPermi('biz:delivery:operate')")
    @Log(title = "发送重试", businessType = BusinessType.UPDATE)
    public Map<String, Object> retryDelivery(@PathVariable Long deliveryId, @RequestBody Map<String, Object> body)
    {
        return messageService.retryDelivery(deliveryId, body);
    }
}
