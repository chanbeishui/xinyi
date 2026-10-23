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
import com.xinyi.rongli.service.SubscribeConfigService;

/**
 * 订阅消息配置（G03）。
 *
 * <p>保存即生效。自检接口会真实换取 access_token，用于证明 AppID 与 AppSecret 配对。</p>
 */
@RestController
@RequestMapping("/api/admin/config/subscribe")
public class AdminSubscribeConfigController
{
    @Autowired
    private SubscribeConfigService subscribeConfigService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('biz:config:subscribe')")
    public Map<String, Object> view()
    {
        return subscribeConfigService.view();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('biz:config:subscribe')")
    @Log(title = "订阅消息配置", businessType = BusinessType.UPDATE)
    public Map<String, Object> save(@RequestBody Map<String, Object> body)
    {
        return subscribeConfigService.save(body);
    }

    @PostMapping("/self-check")
    @PreAuthorize("@ss.hasPermi('biz:config:subscribe')")
    public Map<String, Object> selfCheck()
    {
        return subscribeConfigService.selfCheck();
    }
}
