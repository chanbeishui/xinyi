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
import com.xinyi.rongli.service.PayConfigService;

/**
 * 微信支付配置（G01）。
 *
 * <p>保存即生效：写入后会清理配置缓存，下一次下单直接使用新参数，无需重启。
 * 敏感项在接口上只返回"是否已配置 + 末 4 位"，不回传明文。</p>
 */
@RestController
@RequestMapping("/api/admin/config/payment")
public class AdminPayConfigController
{
    @Autowired
    private PayConfigService payConfigService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('biz:config:payment')")
    public Map<String, Object> view()
    {
        return payConfigService.view();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('biz:config:payment')")
    @Log(title = "微信支付配置", businessType = BusinessType.UPDATE)
    public Map<String, Object> save(@RequestBody Map<String, Object> body)
    {
        return payConfigService.save(body);
    }

    /** 本地签名链路自检：不发起网络请求，用于确认私钥/证书/回调地址可用 */
    @PostMapping("/self-check")
    @PreAuthorize("@ss.hasPermi('biz:config:payment')")
    public Map<String, Object> selfCheck()
    {
        return payConfigService.selfCheck();
    }
}
