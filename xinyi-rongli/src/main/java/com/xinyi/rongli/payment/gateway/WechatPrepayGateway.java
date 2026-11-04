package com.xinyi.rongli.payment.gateway;

import com.xinyi.rongli.payment.dto.PrepaySnapshot;
import com.xinyi.rongli.platform.error.*;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** 官方SDK签名与验签均保留；仅明确隔离测试可将网络传输指向回环协议替身。 */
@Component
public class WechatPrepayGateway {
    private final String origin;
    private final String endpoint;
    private final boolean live;
    private final OkHttpClient transport;

    public WechatPrepayGateway(
            @Value("${vi.environment:local}") String environment,
            @Value("${vi.payment.test-endpoint:}") String endpoint,
            @Value("${vi.payment.public-origin:}") String origin) {
        this.origin = origin;
        this.endpoint = endpoint;
        live = Set.of("prod", "production", "live").contains(environment);
        if (!endpoint.isBlank()) {
            var u = URI.create(endpoint);
            if (!Set.of("test", "integration").contains(environment)
                    || !"http".equals(u.getScheme())
                    || !"127.0.0.1".equals(u.getHost())
                    || u.getPort() < 1
                    || u.getUserInfo() != null
                    || u.getRawQuery() != null
                    || u.getFragment() != null
                    || !(u.getPath().isEmpty() || "/".equals(u.getPath())))
                throw new IllegalArgumentException("支付替身仅允许隔离测试回环端点");
        }
        transport = transport();
    }

    /** 不自动重试或跟随重定向，保留渠道未知结果，由支付服务按原商户单处理重试。 */
    private OkHttpClient transport() {
        var client =
                new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(6, TimeUnit.SECONDS)
                        .writeTimeout(6, TimeUnit.SECONDS)
                        .callTimeout(8, TimeUnit.SECONDS)
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .retryOnConnectionFailure(false);
        if (!endpoint.isBlank())
            client.addInterceptor(
                    chain -> {
                        var req = chain.request();
                        return chain.proceed(
                                req.newBuilder()
                                        .url(
                                                endpoint.replaceAll("/+$", "")
                                                        + req.url().encodedPath()
                                                        + (req.url().encodedQuery() == null
                                                                ? ""
                                                                : "?" + req.url().encodedQuery()))
                                        .build());
                    });
        // SDK响应头容器按大小写查找；HTTP头按协议应大小写不敏感，统一名称但不修改签名值。
        client.addInterceptor(
                chain -> {
                    var response = chain.proceed(chain.request());
                    var normalized = response.newBuilder();
                    for (String name :
                            List.of(
                                    "Wechatpay-Timestamp",
                                    "Wechatpay-Nonce",
                                    "Wechatpay-Serial",
                                    "Wechatpay-Signature",
                                    "Request-ID")) {
                        var values = response.headers(name);
                        if (values.size() > 1) {
                            response.close();
                            throw new java.io.IOException("重复支付响应头");
                        }
                        if (values.size() == 1) normalized.header(name, values.get(0));
                    }
                    return normalized.build();
                });
        return client.build();
    }

    private void networkAvailable() {
        if (!live && endpoint.isBlank())
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "当前环境未配置支付测试网关");
    }

    public void available() {
        networkAvailable();
        notifyUrl();
    }

    public com.wechat.pay.java.core.http.HttpClient client(Config config) {
        networkAvailable();
        return new DefaultHttpClientBuilder()
                .config(config)
                .okHttpClient(transport)
                .disableRetryOnConnectionFailure()
                .build();
    }

    public String notifyUrl() {
        try {
            var u = URI.create(origin);
            if (!"https".equals(u.getScheme())
                    || u.getHost() == null
                    || u.getUserInfo() != null
                    || u.getRawQuery() != null
                    || u.getFragment() != null
                    || !(u.getPath().isEmpty() || "/".equals(u.getPath())))
                throw new IllegalArgumentException();
            return origin.replaceAll("/+$", "") + "/api/notify/wechat/pay";
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "支付回调部署域名未正确配置");
        }
    }

    public String prepay(Config config, PrepaySnapshot snapshot) {
        available();
        try {
            var http = client(config);
            var sdk = new JsapiService.Builder().httpClient(http).build();
            var r = new PrepayRequest();
            r.setAppid(snapshot.appId());
            r.setMchid(snapshot.mchid());
            r.setDescription("机动车检测预约");
            r.setOutTradeNo(snapshot.outTradeNo());
            r.setTimeExpire(snapshot.expiresAt());
            r.setNotifyUrl(snapshot.notifyUrl());
            var amount = new Amount();
            amount.setCurrency("CNY");
            amount.setTotal(Math.toIntExact(snapshot.totalFen()));
            r.setAmount(amount);
            var payer = new Payer();
            payer.setOpenid(snapshot.openId());
            r.setPayer(payer);
            String id = sdk.prepay(r).getPrepayId();
            if (id == null || !id.matches("[A-Za-z0-9_-]{1,128}"))
                throw new IllegalArgumentException();
            return id;
        } catch (Exception e) {
            throw new ViException(
                    ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "支付结果暂未确定，请刷新订单后重试；不会延长付款期限");
        }
    }

    public Map<String, Object> launch(
            Config config, String appId, String prepayId, String expires) {
        String at = Long.toString(Instant.now().getEpochSecond()),
                nonce = UUID.randomUUID().toString().replace("-", ""),
                pkg = "prepay_id=" + prepayId;
        String signature =
                config.createSigner()
                        .sign(appId + "\n" + at + "\n" + nonce + "\n" + pkg + "\n")
                        .getSign();
        return Map.of(
                "timeStamp",
                at,
                "nonceStr",
                nonce,
                "package",
                pkg,
                "signType",
                "RSA",
                "paySign",
                signature,
                "payExpireAt",
                expires);
    }
}
