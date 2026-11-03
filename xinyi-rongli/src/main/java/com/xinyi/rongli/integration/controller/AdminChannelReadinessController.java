package com.xinyi.rongli.integration.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.annotation.Log;
import com.xinyi.common.enums.BusinessType;
import com.xinyi.rongli.integration.service.ChannelReadinessService;

/**
 * 第三方通道凭据体检（支付 / 短信 / 订阅 / 小程序 / 回调）。
 *
 * <p>真实联调前后的"还差什么"入口：只读、可重复调用，不产生费用、不消耗订阅次数。</p>
 */
@RestController
@RequestMapping("/api/admin/config/readiness")
public class AdminChannelReadinessController
{
    @Autowired
    private ChannelReadinessService readinessService;

    /**
     * @param probe 传 true 时额外探测支付回调地址是否公网可达（会发起一次外部 GET）
     */
    @GetMapping
    @PreAuthorize("@ss.hasAnyPermi('biz:config:payment,biz:config:sms,biz:config:subscribe')")
    public Map<String, Object> report(@RequestParam(value = "probe", required = false, defaultValue = "false")
            boolean probe)
    {
        return readinessService.report(probe);
    }

    /** 切换服务端联调模式（写 rongli.mini.mock，保存即生效，无需重启） */
    @PutMapping("/mock")
    @PreAuthorize("@ss.hasAnyPermi('biz:config:payment,biz:config:sms,biz:config:subscribe')")
    @Log(title = "第三方联调模式", businessType = BusinessType.UPDATE)
    public Map<String, Object> setMock(@RequestBody Map<String, Object> body)
    {
        Object value = body == null ? null : body.get("mock");
        boolean mock = value == null || Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
        return readinessService.setMock(mock);
    }
}
