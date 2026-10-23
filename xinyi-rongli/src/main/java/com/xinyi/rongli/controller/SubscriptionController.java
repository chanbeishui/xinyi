package com.xinyi.rongli.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.integration.IntegrationSubscriptionService;

/**
 * 订阅消息授权（小程序侧）。
 *
 * <p>先取本次可请求的已发布模板，再由客户端在用户点击时调起订阅面板，最后回传本次结果。
 * 服务端只把它当作一次性意向记录，不使用“客户端说同意了”作为发送许可。</p>
 */
@RestController
@RequestMapping("/api/common/subscriptions/prompts")
public class SubscriptionController
{
    @Autowired
    private IntegrationSubscriptionService subscriptionService;

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body)
    {
        return subscriptionService.createPrompt(body);
    }

    @PostMapping("/{promptId}/report")
    public Map<String, Object> report(@PathVariable Long promptId, @RequestBody Map<String, Object> body)
    {
        return subscriptionService.reportPrompt(promptId, body);
    }
}
