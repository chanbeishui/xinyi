package com.xinyi.rongli.payment.controller;

import com.xinyi.rongli.payment.service.PaymentLifecycleService;
import com.xinyi.rongli.platform.error.*;
import com.wechat.pay.java.core.notification.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.*;

/** 微信原始协议入口；仅在业务事务提交后返回无响应体204。 */
@RestController
@RequestMapping("/api/notify/wechat")
public class PaymentNotificationController {
    private final PaymentLifecycleService service;

    public PaymentNotificationController(PaymentLifecycleService service) {
        this.service = service;
    }

    @PostMapping({"/pay", "/refund"})
    public ResponseEntity<?> receive(HttpServletRequest request) {
        try {
            byte[] bytes = request.getInputStream().readNBytes(65537);
            if (bytes.length == 0 || bytes.length > 65536)
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
            String body =
                    StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            var input =
                    new RequestParam.Builder()
                            .serialNumber(header(request, "Wechatpay-Serial"))
                            .timestamp(header(request, "Wechatpay-Timestamp"))
                            .nonce(header(request, "Wechatpay-Nonce"))
                            .signature(header(request, "Wechatpay-Signature"))
                            .body(body)
                            .build();
            // 该调用完成数据库事务提交后才能 ACK；异常返回失败，让微信重试通知。
            service.notification(
                    request.getRequestURI().endsWith("/refund") ? "refund" : "pay", body, input);
            return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
        } catch (ViException e) {
            return failure(e.getErrorCode() == ViErrorCode.VI_REQUEST_INVALID ? 400 : 500);
        } catch (java.nio.charset.CharacterCodingException e) {
            return failure(400);
        } catch (Exception e) {
            return failure(500);
        }
    }

    // 重复签名头可能造成代理与应用解释不一致，必须恰好一个非空值。
    private static String header(HttpServletRequest r, String name) {
        var values = Collections.list(r.getHeaders(name));
        if (values.size() != 1 || values.get(0).isBlank() || values.get(0).length() > 2048)
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        return values.get(0);
    }

    private static ResponseEntity<?> failure(int status) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("code", "FAIL", "message", status == 400 ? "通知校验失败" : "通知未持久化，请重试"));
    }
}
