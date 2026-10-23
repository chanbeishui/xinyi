package com.xinyi.rongli.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.service.NotifyService;

/**
 * 微信支付/退款通知与登录前公开配置。
 *
 * <p>通知路由在安全链中放行，处理前由服务层按 event_key 去重，重复通知不会重复改单。</p>
 */
@RestController
public class NotifyController
{
    @Autowired
    private NotifyService notifyService;

    @PostMapping("/api/notify/wechat/pay")
    public Map<String, Object> payNotify(@RequestBody(required = false) String body,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial)
    {
        return notifyService.payNotify(body, NotifyService.notifyHeaders(timestamp, nonce, signature, serial));
    }

    @PostMapping("/api/notify/wechat/refund")
    public Map<String, Object> refundNotify(@RequestBody(required = false) String body,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial)
    {
        return notifyService.refundNotify(body, NotifyService.notifyHeaders(timestamp, nonce, signature, serial));
    }

    @GetMapping("/api/public/service-config")
    public Map<String, Object> serviceConfig()
    {
        return notifyService.serviceConfig();
    }

    @RequestMapping("/api/public/channel-health")
    public Map<String, Object> channelHealth()
    {
        return notifyService.channelHealth();
    }
}
