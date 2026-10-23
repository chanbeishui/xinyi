package com.xinyi.rongli.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.integration.IntegrationPaymentService;

/**
 * 车主端支付方式（O10）。
 *
 * <p>支付方式由后台已发布的支付策略决定，前端只做展示；服务端每次都重新读取权威版本，
 * 不信任客户端传来的“开通状态”。选择线下只记录意向，订单仍为待支付。</p>
 */
@RestController
@RequestMapping("/api/app/owner/orders/{orderNo}")
public class PaymentOptionController
{
    @Autowired
    private IntegrationPaymentService paymentService;

    @GetMapping("/payment-options")
    public Map<String, Object> paymentOptions(@PathVariable String orderNo)
    {
        return paymentService.paymentOptions(orderNo);
    }

    @PostMapping("/offline-payment")
    public Map<String, Object> chooseOffline(@PathVariable String orderNo, @RequestBody(required = false) Map<String, Object> body)
    {
        return paymentService.chooseOffline(orderNo, body == null ? Map.of() : body);
    }
}
