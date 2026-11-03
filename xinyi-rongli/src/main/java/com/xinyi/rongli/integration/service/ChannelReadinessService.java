package com.xinyi.rongli.integration.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.ConfigStore;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.PaySettings;
import com.xinyi.rongli.common.support.SmsSettings;
import com.xinyi.rongli.common.support.SubscribeSettings;
import com.xinyi.rongli.common.support.WechatMiniClient;

/**
 * 第三方通道凭据体检。
 *
 * <p>真实联调阶段最耗时的不是写代码，而是"到底还差哪一项"。本类把支付、短信、订阅、
 * 小程序凭据、回调地址五块收敛成一份清单：每项给出是否就绪、缺什么、下一步点哪里，
 * 并且不发起任何写操作（回调探测是只读 GET）。</p>
 *
 * <p>判定口径：只做能在服务端本地判定的校验，与真实下单/下发时的校验完全一致，
 * 因此"体检通过"等价于"凭据填对了"，不等于"供应商已受理"。</p>
 */
@Service
public class ChannelReadinessService
{
    private static final Logger log = LoggerFactory.getLogger(ChannelReadinessService.class);

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private ConfigStore store;

    @Autowired
    private WechatMiniClient wechatMiniClient;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    /** mock 开关的落库键：存在时覆盖 application.yml 的 rongli.wechat.mock */
    private static final String KEY_MOCK = "rongli.mini.mock";

    /**
     * 生成体检报告。
     *
     * @param probeCallback true 时额外探测支付回调地址是否公网可达（会发起一次外部请求）
     */
    public Map<String, Object> report(boolean probeCallback)
    {
        List<Map<String, Object>> items = new ArrayList<>();
        List<String> blockers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        PaySettings pay = PaySettings.load(config, crypto);
        SmsSettings sms = SmsSettings.load(config, crypto);
        SubscribeSettings subscribe = SubscribeSettings.load(config);
        boolean mock = wechatMiniClient.isMock();
        String miniAppId = wechatMiniClient.effectiveAppId();
        String miniSecret = wechatMiniClient.effectiveAppSecret();

        // ---------------------------------------------------------------- 支付
        List<String> payMissing = new ArrayList<>(pay.missingFields());
        String urlProblem = PaySettings.urlProblem(pay.notifyUrl());
        if (!pay.realChannel())
        {
            payMissing.add("支付通道（当前 MOCK，需切到 WECHAT）");
        }
        if ("SANDBOX".equalsIgnoreCase(pay.mode()))
        {
            payMissing.add("配置模式（当前 SANDBOX，只能验签不能真实收款）");
        }
        boolean payReady = pay.realChannel() && payMissing.isEmpty();
        items.add(item("payment", "微信支付", payReady, payMissing,
                payReady ? "凭据齐全，可发起真实 JSAPI 下单与退款"
                        : "到「微信支付配置」补齐：" + String.join("、", payMissing),
                Map.of("channel", text(pay.channel()), "mode", text(pay.mode()),
                        "mchId", maskMid(pay.mchId()), "appId", text(pay.appId()),
                        "verifyNotify", pay.verifyNotify(),
                        "platformCertConfigured", pay.publicKey() != null,
                        "notifyUrl", text(pay.notifyUrl()),
                        "refundNotifyUrl", text(pay.effectiveRefundNotifyUrl()))));
        if (!payReady)
        {
            blockers.add("微信支付：" + (payMissing.isEmpty() ? "未切换到真实通道" : String.join("、", payMissing)));
        }
        if (urlProblem != null && pay.realChannel())
        {
            blockers.add("支付回调地址不可用：" + urlProblem);
        }

        // ---------------------------------------------------------------- 小程序凭据
        List<String> miniMissing = new ArrayList<>();
        if (miniAppId == null || !PaySettings.APP_ID.matcher(miniAppId.trim()).matches())
        {
            miniMissing.add("小程序 AppID（wx 开头 18 位）");
        }
        if (miniSecret == null || miniSecret.isBlank())
        {
            miniMissing.add("小程序 AppSecret");
        }
        boolean miniReady = miniMissing.isEmpty() && !mock;
        items.add(item("mini", "小程序凭据（登录/手机号/订阅）", miniReady, miniMissing,
                miniHint(mock, miniMissing),
                Map.of("mock", mock, "mockSource", mockSource(), "appId", text(miniAppId),
                        "secretConfigured", miniSecret != null && !miniSecret.isBlank())));
        if (mock)
        {
            blockers.add("服务端处于联调模式（rongli.mini.mock=true）：微信登录、手机号授权、订阅消息全部走模拟");
        }
        else if (!miniMissing.isEmpty())
        {
            blockers.add("小程序凭据：" + String.join("、", miniMissing));
        }

        // 支付用 AppID 与小程序 AppID 必须一致，否则下单会报 openid 与 appid 不匹配
        if (pay.realChannel() && !miniAppId.isBlank() && pay.appId() != null && !pay.appId().isBlank()
                && !pay.appId().trim().equals(miniAppId.trim()))
        {
            warnings.add("支付配置的 AppID（" + pay.appId() + "）与小程序 AppID（" + miniAppId
                    + "）不一致：微信支付下单会因 openid 与 appid 不匹配失败");
        }

        // ---------------------------------------------------------------- 短信
        List<String> smsMissing = new ArrayList<>();
        if (!sms.enabled())
        {
            smsMissing.add("真实下发开关（当前未启用，验证码只回显不发送）");
        }
        smsMissing.addAll(sms.missingFields());
        boolean smsReady = sms.enabled() && smsMissing.isEmpty();
        items.add(item("sms", "短信通道", smsReady, smsMissing,
                smsReady ? "凭据齐全，可在「短信通道配置」页发一条测试短信"
                        : "到「短信通道配置」补齐：" + String.join("、", smsMissing),
                Map.of("provider", text(sms.provider()), "enabled", sms.enabled(),
                        "signName", text(sms.signName()),
                        "templateCode", text(sms.templateCode()))));
        if (!smsReady)
        {
            blockers.add("短信：" + String.join("、", smsMissing));
        }

        // ---------------------------------------------------------------- 订阅消息
        List<String> subMissing = new ArrayList<>(subscribe.problems());
        if (!subscribe.enabled())
        {
            subMissing.add("订阅消息总开关（当前关闭，只发站内信）");
        }
        if (mock)
        {
            subMissing.add("服务端联调模式（订阅消息不会真实下发）");
        }
        boolean subReady = subscribe.enabled() && subMissing.isEmpty();
        items.add(item("subscribe", "订阅消息", subReady, subMissing,
                subReady ? "模板与凭据齐全，业务事件会真实下发"
                        : "到「订阅消息配置」补齐：" + String.join("、", subMissing),
                Map.of("enabled", subscribe.enabled(), "scenes", subscribe.configuredScenes(),
                        "page", subscribe.effectivePage(), "state", subscribe.effectiveState())));
        if (!subReady)
        {
            blockers.add("订阅消息：" + String.join("、", subMissing));
        }
        if ("developer".equalsIgnoreCase(subscribe.effectiveState())
                || "trial".equalsIgnoreCase(subscribe.effectiveState()))
        {
            warnings.add("订阅消息运行环境为 " + subscribe.effectiveState() + "，正式用户收不到；上线前改 formal");
        }

        // ---------------------------------------------------------------- 回调地址
        Map<String, Object> callbackDetail = new LinkedHashMap<>();
        callbackDetail.put("payNotifyUrl", text(pay.notifyUrl()));
        callbackDetail.put("refundNotifyUrl", text(pay.effectiveRefundNotifyUrl()));
        List<String> callbackMissing = new ArrayList<>();
        if (urlProblem != null)
        {
            callbackMissing.add(urlProblem);
        }
        if (probeCallback && urlProblem == null)
        {
            String probeResult = probe(pay.notifyUrl());
            callbackDetail.put("probe", probeResult);
            if (probeResult != null && probeResult.startsWith("不可达"))
            {
                callbackMissing.add(probeResult);
            }
        }
        boolean callbackReady = callbackMissing.isEmpty();
        items.add(item("callback", "支付回调可达性", callbackReady, callbackMissing,
                callbackReady ? "回调地址格式合法" + (probeCallback ? "且公网可达" : "；可加 probe=true 做一次真实探测")
                        : "回调必须是公网 https 域名并放行 /api/notify/wechat/**",
                callbackDetail));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ready", blockers.isEmpty());
        out.put("blockers", blockers);
        out.put("warnings", warnings);
        out.put("items", items);
        out.put("mock", mock);
        out.put("mockSource", mockSource());
        out.put("hint", blockers.isEmpty()
                ? "三通道凭据均已就绪；建议再各做一次真实测试（短信发码、支付 1 分钱、订阅白名单推送）"
                : "仍有 " + blockers.size() + " 项阻塞真实联调；按 items 里的 action 逐项补齐即可");
        return out;
    }

    /**
     * 切换服务端联调模式。
     *
     * <p>写入 {@code rongli.mini.mock} 覆盖 yml：真实联调开始前关掉，遇到凭据问题时再打开，
     * 不需要重启服务。关闭前会先校验小程序凭据，避免把登录链路切成 503。</p>
     */
    public Map<String, Object> setMock(boolean mock)
    {
        if (!mock)
        {
            String appId = wechatMiniClient.effectiveAppId();
            String secret = wechatMiniClient.effectiveAppSecret();
            List<String> missing = new ArrayList<>();
            if (appId == null || !PaySettings.APP_ID.matcher(appId.trim()).matches())
            {
                missing.add("小程序 AppID");
            }
            if (secret == null || secret.isBlank())
            {
                missing.add("小程序 AppSecret");
            }
            if (!missing.isEmpty())
            {
                throw com.xinyi.rongli.common.support.ApiException.validation(
                        "关闭联调模式前必须先配置：" + String.join("、", missing)
                                + "；否则微信登录与手机号授权会直接返回 503");
            }
        }
        store.put(KEY_MOCK, String.valueOf(mock));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mock", mock);
        out.put("saved", true);
        out.put("source", "sys_config:" + KEY_MOCK);
        out.put("message", mock ? "已切回联调模式：微信登录/手机号/订阅消息不再访问微信"
                : "已关闭联调模式：下一次微信登录、手机号授权、订阅消息将走真实接口");
        out.put("nextStep", mock ? "需要真实下发前再关闭一次" : "到「订阅消息配置」页点连通性自检，确认 AppID 与 AppSecret 配对");
        return out;
    }

    // ------------------------------------------------------------------ 内部

    /** null 安全：避免响应里出现字符串 "null" 让前端以为填了值 */
    private String text(String value)
    {
        return value == null ? "" : value;
    }

    private String miniHint(boolean mock, List<String> missing)
    {
        if (mock && !missing.isEmpty())
        {
            return "服务端仍在联调模式，且缺少：" + String.join("、", missing) + "；两项都要处理才算就绪";
        }
        if (mock)
        {
            return "凭据已填，但服务端仍是联调模式，真实微信接口不会被调用";
        }
        if (!missing.isEmpty())
        {
            return "到「订阅消息配置」补齐：" + String.join("、", missing);
        }
        return "凭据已配置，可在「订阅消息」页做连通性自检（会真实换一次 access_token）";
    }

    private String mockSource()
    {
        String fromConfig = config.get(KEY_MOCK, "");
        if ("true".equalsIgnoreCase(fromConfig) || "false".equalsIgnoreCase(fromConfig))
        {
            return "sys_config:" + KEY_MOCK;
        }
        return "application.yml:rongli.wechat.mock";
    }

    /**
     * 探测回调地址是否公网可达。
     *
     * <p>微信只发 POST，这里用 GET 探活：只要拿得到任何 HTTP 状态码（含 401/405）就说明
     * DNS、证书、网关都通；连不上则说明地址写错或没做公网映射。</p>
     */
    private String probe(String url)
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url.trim()))
                    .timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return "可达（HTTP " + resp.statusCode() + "）";
        }
        catch (Exception e)
        {
            log.warn("支付回调探测失败 url={} err={}", url, e.toString());
            return "不可达：" + e.getClass().getSimpleName()
                    + "（检查域名解析、https 证书、网关是否放行 /api/notify/wechat/**）";
        }
    }

    private Map<String, Object> item(String key, String label, boolean ready, List<String> missing, String action,
            Map<String, Object> detail)    {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("key", key);
        out.put("label", label);
        out.put("ready", ready);
        out.put("missing", missing);
        out.put("action", action);
        out.put("detail", detail);
        return out;
    }

    private String maskMid(String value)
    {
        if (value == null || value.isBlank())
        {
            return "";
        }
        String text = value.trim();
        return text.length() <= 4 ? text : text.substring(0, 2) + "****" + text.substring(text.length() - 2);
    }
}
