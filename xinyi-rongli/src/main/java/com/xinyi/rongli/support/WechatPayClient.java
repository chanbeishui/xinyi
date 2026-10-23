package com.xinyi.rongli.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * 微信支付 APIv3 客户端（纯 JDK 实现，不引入第三方 SDK）。
 *
 * <p>覆盖 JSAPI 下单、小程序调起参数签名、退款申请、回调验签与报文解密。
 * 所有方法都在"配置不完整"时抛出 {@link ApiException}，由统一异常处理器转成 503，
 * 前端据此提示"支付通道未配置"，避免把配置缺失伪装成业务错误。</p>
 *
 * <p>签名串严格遵循官方文档：请求为
 * {@code METHOD\nURL\nTIMESTAMP\nNONCE\nBODY\n}，调起参数为
 * {@code appId\nTIME\nNONCE\npackage\n}，回调验签为 {@code TIMESTAMP\nNONCE\nBODY\n}。</p>
 */
@Component
public class WechatPayClient
{
    private static final Logger log = LoggerFactory.getLogger(WechatPayClient.class);

    private static final String BASE = "https://api.mch.weixin.qq.com";

    private static final String PATH_JSAPI = "/v3/pay/transactions/jsapi";

    private static final String PATH_REFUND = "/v3/refund/domestic/refunds";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final SecureRandom random = new SecureRandom();

    /** JSAPI 下单结果 */
    public record Prepay(String prepayId, String appId, String timeStamp, String nonceStr, String packageValue,
            String paySign)
    {
        public Map<String, Object> toView()
        {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("timeStamp", timeStamp);
            view.put("nonceStr", nonceStr);
            view.put("package", packageValue);
            view.put("signType", "RSA");
            view.put("paySign", paySign);
            return view;
        }
    }

    /**
     * JSAPI 下单并生成小程序 {@code wx.requestPayment} 所需参数。
     *
     * @param openId 支付人 openId，必须是该 appId 下的 openId
     */
    public Prepay jsapiPrepay(PaySettings settings, String outTradeNo, String description, long amountFen, String openId)
    {
        requireReady(settings);
        if (openId == null || openId.isBlank())
        {
            throw ApiException.validation("缺少支付人 openId，无法发起微信支付");
        }
        JSONObject body = new JSONObject();
        body.put("appid", settings.appId().trim());
        body.put("mchid", settings.mchId().trim());
        body.put("description", description);
        body.put("out_trade_no", outTradeNo);
        body.put("notify_url", settings.notifyUrl().trim());
        JSONObject amount = new JSONObject();
        amount.put("total", amountFen);
        amount.put("currency", "CNY");
        body.put("amount", amount);
        JSONObject payer = new JSONObject();
        payer.put("openid", openId);
        body.put("payer", payer);

        JSONObject resp = request(settings, "POST", PATH_JSAPI, body.toJSONString());
        String prepayId = resp.getString("prepay_id");
        if (prepayId == null || prepayId.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_GATEWAY, Codes.Error.CHANNEL_UNAVAILABLE,
                    "微信支付下单未返回 prepay_id");
        }
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = randomNonce();
        String packageValue = "prepay_id=" + prepayId;
        String message = settings.appId().trim() + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageValue + "\n";
        String paySign = settings.sign(message);
        if (paySign == null)
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "商户私钥无法签名，请检查微信支付配置");
        }
        return new Prepay(prepayId, settings.appId().trim(), timeStamp, nonceStr, packageValue, paySign);
    }

    /**
     * 申请退款。金额单位为分，必须与订单总额同时上报，微信侧据此校验。
     *
     * @return 微信返回的 refund_id 与状态
     */
    public Map<String, Object> refund(PaySettings settings, String outTradeNo, String outRefundNo, String reason,
            long refundFen, long totalFen)
    {
        requireReady(settings);
        JSONObject body = new JSONObject();
        body.put("out_trade_no", outTradeNo);
        body.put("out_refund_no", outRefundNo);
        if (reason != null && !reason.isBlank())
        {
            body.put("reason", reason.length() > 80 ? reason.substring(0, 80) : reason);
        }
        String notifyUrl = settings.effectiveRefundNotifyUrl();
        if (notifyUrl != null)
        {
            body.put("notify_url", notifyUrl);
        }
        JSONObject amount = new JSONObject();
        amount.put("refund", refundFen);
        amount.put("total", totalFen);
        amount.put("currency", "CNY");
        body.put("amount", amount);

        JSONObject resp = request(settings, "POST", PATH_REFUND, body.toJSONString());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("refundId", resp.getString("refund_id"));
        view.put("outRefundNo", resp.getString("out_refund_no"));
        view.put("status", resp.getString("status"));
        view.put("channel", "WECHAT");
        return view;
    }

    /**
     * 主动查询退款状态。
     *
     * <p>用于"回调丢失"与人工核对场景：以渠道返回为准，而不是本地推测。</p>
     */
    public Map<String, Object> queryRefund(PaySettings settings, String outRefundNo)
    {
        requireReady(settings);
        String path = "/v3/refund/domestic/refunds/"
                + java.net.URLEncoder.encode(outRefundNo, StandardCharsets.UTF_8);
        JSONObject resp = request(settings, "GET", path, "");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("refundId", resp.getString("refund_id"));
        view.put("outRefundNo", resp.getString("out_refund_no"));
        view.put("status", resp.getString("status"));
        view.put("channel", "WECHAT");
        Object amount = resp.get("amount");
        if (amount instanceof JSONObject am)
        {
            view.put("refundFen", am.getLong("refund"));
            view.put("totalFen", am.getLong("total"));
        }
        return view;
    }

    /**
     * 回调验签与报文解密。
     *
     * <p>验签用平台证书公钥；未配置平台证书时按配置决定放行（仅联调）或拒绝。
     * 解密用 APIv3 密钥做 AES-256-GCM，associated_data 参与认证。</p>
     *
     * @return 解密后的业务报文；验签失败或解密失败抛异常
     */
    public JSONObject verifyAndDecrypt(PaySettings settings, String timestamp, String nonce, String signature,
            String serial, String body)
    {
        if (settings.verifyNotify())
        {
            if (settings.platformSerialNo() != null && !settings.platformSerialNo().isBlank()
                    && serial != null && !serial.equalsIgnoreCase(settings.platformSerialNo().trim()))
            {
                throw ApiException.forbidden("支付回调证书序列号与已配置的平台证书不一致，已拒绝处理");
            }
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            if (!settings.verify(message, signature))
            {
                throw ApiException.forbidden("支付回调验签失败，请检查平台证书是否与商户号匹配");
            }
        }
        else
        {
            log.warn("支付回调未验签（rongli.pay.verifyNotify=false），仅允许在联调环境使用");
        }

        JSONObject envelope = JSON.parseObject(body);
        Object resource = envelope.get("resource");
        if (!(resource instanceof JSONObject ro))
        {
            // 部分联调报文直接给明文业务字段，此时原样返回
            return envelope;
        }
        String cipherText = ro.getString("ciphertext");
        if (cipherText == null || cipherText.isBlank())
        {
            return ro;
        }
        String plain = decryptResource(settings.apiV3Key(), ro.getString("associated_data"), ro.getString("nonce"),
                cipherText);
        return JSON.parseObject(plain);
    }

    /** AES-256-GCM 解密回调资源；apiV3Key 必须是 32 位 */
    public String decryptResource(String apiV3Key, String associatedData, String nonce, String cipherText)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8)));
            if (associatedData != null && !associatedData.isBlank())
            {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(plain, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, Codes.Error.SIGNATURE_INVALID,
                    "支付回调报文解密失败，请核对 APIv3 密钥");
        }
    }

    /**
     * 本地签名自检：用当前配置对一段固定报文签名再自验，验证私钥可用。
     *
     * <p>不发起网络请求，因此可在配置页保存后立即返回结果，用于排除"私钥粘贴错/格式不对"。</p>
     */
    public Map<String, Object> selfCheck(PaySettings settings)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        String sample = "SELF_CHECK\n/v3/pay/transactions/jsapi\n" + Instant.now().getEpochSecond() + "\nnonce\n{}\n";
        String signature = settings.sign(sample);
        result.put("privateKeyParsed", settings.privateKey() != null);
        result.put("signatureProduced", signature != null);
        result.put("platformCertParsed", settings.publicKey() != null);
        result.put("apiV3KeyLength", settings.apiV3Key() == null ? 0 : settings.apiV3Key().trim().length());
        result.put("notifyUrlProblem", PaySettings.urlProblem(settings.notifyUrl()));
        result.put("refundNotifyUrl", settings.effectiveRefundNotifyUrl());
        result.put("ready", settings.ready());
        result.put("missing", settings.missingFields());
        boolean ok = settings.privateKey() != null && signature != null;
        result.put("passed", ok);
        result.put("hint", ok
                ? "本地签名链路正常；真实下单还需商户号与小程序 AppID 同主体"
                : "商户私钥不可用，请粘贴 apiclient_key.pem 的完整内容（含 BEGIN/END 行）");
        return result;
    }

    // ------------------------------------------------------------------ 内部

    private void requireReady(PaySettings settings)
    {
        if (!settings.realChannel())
        {
            throw ApiException.stateConflict("当前为联调支付通道，请先在微信支付配置页切换为真实通道");
        }
        if ("SANDBOX".equalsIgnoreCase(settings.mode()))
        {
            throw ApiException.stateConflict("当前为沙箱配置模式，仅用于验证签名链路，不能发起真实支付");
        }
        var missing = settings.missingFields();
        if (!missing.isEmpty())
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "微信支付配置不完整，缺少：" + String.join("、", missing));
        }
    }

    private JSONObject request(PaySettings settings, String method, String path, String body)
    {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = randomNonce();
        String message = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";
        String signature = settings.sign(message);
        if (signature == null)
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "商户私钥不可用，无法签名请求");
        }
        String authorization = "WECHATPAY2-SHA256-RSA2048 mchid=\"" + settings.mchId().trim()
                + "\",nonce_str=\"" + nonce + "\",signature=\"" + signature
                + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + settings.serialNo().trim() + "\"";
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", authorization)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("User-Agent", "xinyi-rongli/1.0");
        builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        HttpResponse<String> resp;
        try
        {
            resp = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            throw new ApiException(HttpStatus.BAD_GATEWAY, Codes.Error.CHANNEL_UNAVAILABLE,
                    "调用微信支付接口失败：" + e.getMessage());
        }
        String text = resp.body() == null ? "" : resp.body();
        if (resp.statusCode() / 100 != 2)
        {
            String code = null;
            String detail = text;
            try
            {
                JSONObject err = JSON.parseObject(text);
                code = err.getString("code");
                detail = err.getString("message");
            }
            catch (Exception ignored)
            {
                // 非 JSON 错误体，保留原始文本
            }
            log.warn("微信支付接口返回 {} code={} message={}", resp.statusCode(), code, detail);
            throw new ApiException(HttpStatus.BAD_GATEWAY, Codes.Error.CHANNEL_UNAVAILABLE,
                    "微信支付返回失败：" + (detail == null ? ("HTTP " + resp.statusCode()) : detail));
        }
        try
        {
            return JSON.parseObject(text);
        }
        catch (Exception e)
        {
            throw new ApiException(HttpStatus.BAD_GATEWAY, Codes.Error.CHANNEL_UNAVAILABLE,
                    "微信支付返回内容不是合法 JSON");
        }
    }

    private String randomNonce()
    {
        byte[] buf = new byte[16];
        random.nextBytes(buf);
        return new String(Base64.getEncoder().encode(buf), StandardCharsets.US_ASCII)
                .replaceAll("[^A-Za-z0-9]", "");
    }
}
