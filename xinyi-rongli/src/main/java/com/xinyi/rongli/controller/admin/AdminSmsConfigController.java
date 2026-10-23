package com.xinyi.rongli.controller.admin;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.annotation.Log;
import com.xinyi.common.enums.BusinessType;
import com.xinyi.rongli.service.SmsConfigService;

/**
 * 短信通道配置（G02）。
 *
 * <p>保存即生效。测试接口会用已保存的配置真实发一条短信，同样计入频控与日志。</p>
 */
@RestController
@RequestMapping("/api/admin/config/sms")
public class AdminSmsConfigController
{
    @Autowired
    private SmsConfigService smsConfigService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('biz:config:sms')")
    public Map<String, Object> view()
    {
        return smsConfigService.view();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('biz:config:sms')")
    @Log(title = "短信通道配置", businessType = BusinessType.UPDATE)
    public Map<String, Object> save(@RequestBody Map<String, Object> body)
    {
        return smsConfigService.save(body);
    }

    @PostMapping("/test")
    @PreAuthorize("@ss.hasPermi('biz:config:sms')")
    @Log(title = "短信连通性测试", businessType = BusinessType.OTHER)
    public Map<String, Object> test(@RequestBody(required = false) Map<String, Object> body)
    {
        Object phone = body == null ? null : body.get("phone");
        return smsConfigService.test(phone == null ? null : String.valueOf(phone).trim());
    }
}
