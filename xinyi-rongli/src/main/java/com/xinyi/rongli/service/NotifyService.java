package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.mapper.PayMapper;
import com.xinyi.rongli.mapper.StationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 微信支付/退款通知与登录前公开配置。
 *
 * <p>通知一律"先落去重表再处理业务"：同一 event_key 只处理一次；
 * 处理过程幂等，重复通知返回成功即可，不重复改单。</p>
 */
@Service
public class NotifyService
{
    private static final Logger log = LoggerFactory.getLogger(NotifyService.class);

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AfterSaleService afterSaleService;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private PayConfigService payConfigService;

    @Autowired
    private com.xinyi.rongli.support.WechatPayClient wechatPayClient;

    /** 微信回调请求头，键统一小写 */
    public static Map<String, String> notifyHeaders(String timestamp, String nonce, String signature, String serial)
    {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("wechatpay-timestamp", timestamp);
        headers.put("wechatpay-nonce", nonce);
        headers.put("wechatpay-signature", signature);
        headers.put("wechatpay-serial", serial);
        return headers;
    }

    /**
     * 回调报文可信化：真实通道下必须通过平台证书验签并按 APIv3 密钥解密；
     * 联调通道（MOCK）允许直接提交明文，便于本地构造回调验证幂等与状态推进。
     */
    private JSONObject resolvePayload(String body, Map<String, String> headers)
    {
        if (config.mockPayment())
        {
            return parse(body);
        }
        var settings = payConfigService.settings();
        String timestamp = header(headers, "wechatpay-timestamp");
        String nonce = header(headers, "wechatpay-nonce");
        String signature = header(headers, "wechatpay-signature");
        String serial = header(headers, "wechatpay-serial");
        if (timestamp == null || nonce == null || signature == null)
        {
            throw ApiException.forbidden("缺少微信支付回调签名头，已拒绝处理");
        }
        return wechatPayClient.verifyAndDecrypt(settings, timestamp, nonce, signature, serial, body);
    }

    private String header(Map<String, String> headers, String name)
    {
        if (headers == null)
        {
            return null;
        }
        for (Map.Entry<String, String> e : headers.entrySet())
        {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name))
            {
                return e.getValue();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ 支付通知

    @Transactional
    public Map<String, Object> payNotify(String body, Map<String, String> headers)
    {
        JSONObject json = resolvePayload(body, headers);
        String outTradeNo = first(json, "out_trade_no", "outTradeNo");
        String transactionId = first(json, "transaction_id", "transactionId");
        String tradeState = first(json, "trade_state", "tradeState", "result_code");
        String eventId = first(json, "event_id", "id");
        if (outTradeNo == null || outTradeNo.isBlank())
        {
            throw ApiException.validation("通知缺少商户订单号");
        }
        if (!dedupe("WECHAT_PAY", eventId == null ? outTradeNo + ":" + tradeState : eventId, outTradeNo,
                "PAY", json.toJSONString()))
        {
            return ack(true, "重复通知已忽略");
        }
        Row payment = Row.of(payMapper.findByTradeNo(outTradeNo));
        if (payment.isEmpty())
        {
            log.warn("支付通知找不到本地支付单：{}", outTradeNo);
            return ack(true, "本地无对应支付单，已记录");
        }
        boolean success = "SUCCESS".equalsIgnoreCase(tradeState) || "0".equals(tradeState)
                || "SUCCESS".equalsIgnoreCase(first(json, "refund_status"));
        if (success)
        {
            bookingService.applyPaymentSuccess(payment, transactionId == null ? outTradeNo : transactionId);
            return ack(true, "支付成功已处理");
        }
        String local = payment.str("status");
        if (Codes.Payment.PENDING.equals(local))
        {
            payMapper.markClosed(payment.lng("id"));
        }
        return ack(true, "支付未成功，支付单已关闭");
    }

    // ------------------------------------------------------------------ 退款通知

    @Transactional
    public Map<String, Object> refundNotify(String body, Map<String, String> headers)
    {
        JSONObject json = resolvePayload(body, headers);
        String outRefundNo = first(json, "out_refund_no", "outRefundNo");
        String refundId = first(json, "refund_id", "refundId");
        String status = first(json, "refund_status", "refundStatus", "status");
        String eventId = first(json, "event_id", "id");
        if (outRefundNo == null || outRefundNo.isBlank())
        {
            throw ApiException.validation("通知缺少商户退款单号");
        }
        if (!dedupe("WECHAT_REFUND", eventId == null ? outRefundNo + ":" + status : eventId, outRefundNo,
                "REFUND", json.toJSONString()))
        {
            return ack(true, "重复通知已忽略");
        }
        boolean success = "SUCCESS".equalsIgnoreCase(status);
        boolean failed = "ABNORMAL".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)
                || "FAILED".equalsIgnoreCase(status);
        if (success || failed)
        {
            afterSaleService.applyRefundResult(outRefundNo, success, refundId);
        }
        return ack(true, success ? "退款成功已处理" : (failed ? "退款失败已登记" : "退款处理中"));
    }

    private Map<String, Object> ack(boolean success, String message)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", success ? "SUCCESS" : "FAIL");
        result.put("message", message);
        return result;
    }

    private JSONObject parse(String body)
    {
        if (body == null || body.isBlank())
        {
            throw ApiException.validation("通知内容为空");
        }
        try
        {
            return JSON.parseObject(body);
        }
        catch (Exception e)
        {
            throw ApiException.validation("通知报文不是合法 JSON");
        }
    }

    private String first(JSONObject json, String... keys)
    {
        for (String k : keys)
        {
            Object v = json.get(k);
            if (v != null && !String.valueOf(v).isBlank())
            {
                return String.valueOf(v);
            }
        }
        Object resource = json.get("resource");
        if (resource instanceof JSONObject ro)
        {
            for (String k : keys)
            {
                Object v = ro.get(k);
                if (v != null && !String.valueOf(v).isBlank())
                {
                    return String.valueOf(v);
                }
            }
        }
        return null;
    }

    /** 通知去重：插入成功表示首次，重复键表示已处理过 */
    private boolean dedupe(String provider, String eventKey, String tradeNo, String type, String raw)
    {
        try
        {
            payMapper.insertChannelEvent(provider, eventKey, tradeNo, type, crypto.sha256(raw));
            return true;
        }
        catch (DuplicateKeyException e)
        {
            return false;
        }
    }

    // ------------------------------------------------------------------ 公开配置

    /** 登录前可见信息：只暴露服务能力与已发布协议版本，不含任何内部标识 */
    public Map<String, Object> serviceConfig()
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("serviceName", "机动车检测预约");
        view.put("termsVersion", config.termsVersion());
        view.put("paymentChannel", config.mockPayment() ? "MOCK" : "WECHAT");
        view.put("mockPayment", config.mockPayment());
        view.put("dispatchLeadMinutes", config.dispatchLeadMinutes());
        view.put("payExpireMinutes", config.payExpireMinutes());
        view.put("autoConfirmHours", config.autoConfirmHours());
        view.put("inviteExpireSeconds", config.inviteExpireSeconds());
        view.put("supportHint", "如遇车辆交接或检测问题，可在订单内发起售后，我们会统一审核取消与退款");
        List<Map<String, Object>> stations = new ArrayList<>();
        for (Map<String, Object> s : stationMapper.listEnabled())
        {
            Row st = Row.of(s);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stationId", String.valueOf(st.lng("id")));
            item.put("name", st.str("name"));
            item.put("address", st.str("address"));
            item.put("businessHours", st.str("business_hours"));
            stations.add(item);
        }
        view.put("stations", stations);
        view.put("serverTime", ViTime.iso(ViTime.now()));
        return view;
    }

    /** 便于前端判断支付通道是否可用于真实收款 */
    public Map<String, Object> channelHealth()
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("channel", config.mockPayment() ? "MOCK" : "WECHAT");
        view.put("realPaymentEnabled", !config.mockPayment());
        view.put("hint", config.mockPayment()
                ? "当前为联调通道，仅用于内部验证" : "真实通道已启用");
        return view;
    }
}
