package com.xinyi.rongli.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.SubscribeSettings;
import com.xinyi.rongli.support.ViTime;
import com.xinyi.rongli.support.WechatMiniClient;

/**
 * 微信订阅消息（N01）。
 *
 * <p>订阅消息属于"尽力而为"的外部副作用：业务事务只写 outbox，本类由
 * {@link OutboxConsumer} 异步调用；失败抛异常交由 outbox 退避重试，
 * 5 次后进入死信，不影响订单主链路。</p>
 *
 * <p>模板 ID、跳转页面、字段映射、运行环境全部来自
 * {@link SubscribeSettings}（即管理端「订阅消息」配置页），保存后下一次投递即生效。
 * 联调环境（{@code rongli.wechat.mock=true}）不调用微信接口，但走完整链路并留下
 * 可观测记录，保证切换到真实凭据后行为一致。</p>
 */
@Service
public class SubscribeService
{
    private static final Logger log = LoggerFactory.getLogger(SubscribeService.class);

    private static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    @Autowired
    private WechatMiniClient wechatMiniClient;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private BizConfig config;

    private final java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(3)).build();

    /** 投递结果，供 outbox 与后台观测使用 */
    public record Result(boolean delivered, String channel, String reason) {}

    /**
     * 处理一条 SUBSCRIBE_PUSH 事件。
     *
     * @param payload outbox 载荷，需含 accountId / scene / content
     * @throws RuntimeException 真实通道失败时抛出，触发 outbox 退避重试
     */
    public Result deliver(JSONObject payload)
    {
        Long accountId = payload.getLong("accountId");
        String scene = payload.getString("scene");
        String content = payload.getString("content");
        Long notificationId = payload.getLong("notificationId");
        if (accountId == null || scene == null)
        {
            return new Result(false, "INBOX", "载荷缺少接收人或场景，已跳过");
        }
        SubscribeSettings settings = SubscribeSettings.load(config);
        if (!settings.enabled())
        {
            return new Result(false, "INBOX", "订阅消息总开关关闭");
        }
        String templateId = settings.templateOf(scene);
        if (templateId == null)
        {
            return new Result(false, "INBOX", "场景 " + scene + " 未配置模板，只发站内信");
        }
        Row account = Row.of(accountMapper.findAccount(accountId));
        if (account.isEmpty() || !"ACTIVE".equals(account.str("status")))
        {
            return new Result(false, "INBOX", "接收账号不存在或已停用");
        }
        String appId = account.str("app_id") == null || account.str("app_id").isBlank()
                ? wechatMiniClient.effectiveAppId() : account.str("app_id");
        String openId = account.str("open_id");

        if (wechatMiniClient.isMock())
        {
            log.info("订阅消息[MOCK] scene={} template={} openId={} content={}", scene, templateId, openId, content);
            markChannel(notificationId, "SUBSCRIBE");
            return new Result(true, "SUBSCRIBE", "联调通道：已模拟下发");
        }
        if (openId == null || openId.isBlank())
        {
            return new Result(false, "INBOX", "账号没有 openId，无法下发订阅消息");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("touser", openId);
        body.put("template_id", templateId);
        body.put("page", settings.effectivePage());
        body.put("miniprogram_state", settings.effectiveState());
        body.put("lang", "zh_CN");
        body.put("data", buildData(settings, scene, content));

        // 令牌统一由小程序客户端管理（含缓存与失效重取），避免两处各自维护过期时间
        String token = wechatMiniClient.accessTokenOf(appId);
        JSONObject resp = postJson(SEND_URL + "?access_token=" + token, JSON.toJSONString(body));
        int errcode = resp.getIntValue("errcode", -1);
        if (errcode != 0)
        {
            // 43101 = 用户未订阅/已取消订阅，属终态：不重试，避免无效退避
            if (errcode == 43101)
            {
                markChannel(notificationId, "SUBSCRIBE_FAILED");
                log.info("用户未订阅该模板 scene={} openId={}", scene, openId);
                return new Result(false, "SUBSCRIBE_FAILED", "用户未订阅，转站内信");
            }
            throw new IllegalStateException("订阅消息下发失败 errcode=" + errcode + " errmsg=" + resp.getString("errmsg"));
        }
        markChannel(notificationId, "SUBSCRIBE");
        return new Result(true, "SUBSCRIBE", "已下发");
    }

    private void markChannel(Long notificationId, String channel)
    {
        if (notificationId == null)
        {
            return;
        }
        miscMapper.markNotificationChannel(notificationId, channel);
    }

    /**
     * 按「已发布的模板档案」下发一条订阅消息（V6.4 业务事件通道）。
     *
     * <p>与 {@link #deliver(JSONObject)} 的区别：模板 ID、页面、字段值全部来自
     * {@code vi_message_template} 的生效版本，不再读 {@code rongli.subscribe.*}
     * 这套静态配置，因此后台换模板无需重启或改库。</p>
     *
     * <p>43101（用户未订阅/已取消）属终态：返回 delivered=false 且不抛异常，
     * 由调用方记为 SKIPPED，不做无意义的退避重试。</p>
     */
    public Result sendPublished(String appId, String openId, String vendorTemplateId, String page,
            Map<String, Object> data)
    {
        if (vendorTemplateId == null || vendorTemplateId.isBlank())
        {
            return new Result(false, "SUBSCRIBE_FAILED", "模板未登记供应商模板ID");
        }
        if (openId == null || openId.isBlank())
        {
            return new Result(false, "SUBSCRIBE_FAILED", "账号没有 openId，无法下发订阅消息");
        }
        if (wechatMiniClient.isMock())
        {
            log.info("订阅消息[MOCK] template={} openId={} data={}", vendorTemplateId, openId, data);
            return new Result(true, "SUBSCRIBE", "联调通道：已模拟下发");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("touser", openId);
        body.put("template_id", vendorTemplateId);
        if (page != null && !page.isBlank())
        {
            body.put("page", page);
        }
        body.put("miniprogram_state", SubscribeSettings.load(config).effectiveState());
        body.put("lang", "zh_CN");
        body.put("data", data);

        String token = wechatMiniClient.accessTokenOf(appId);
        JSONObject resp = postJson(SEND_URL + "?access_token=" + token, JSON.toJSONString(body));
        int errcode = resp.getIntValue("errcode", -1);
        if (errcode == 0)
        {
            return new Result(true, "SUBSCRIBE", resp.getString("msgid"));
        }
        if (errcode == 43101)
        {
            log.info("用户未订阅该模板 template={} openId={}", vendorTemplateId, openId);
            return new Result(false, "SUBSCRIBE_FAILED", "用户未订阅该模板");
        }
        throw new IllegalStateException("订阅消息下发失败 errcode=" + errcode
                + " errmsg=" + resp.getString("errmsg"));
    }

    /**
     * 模板字段：默认 thing1=摘要、time2=当前时间。
     *
     * <p>取值来源由配置页的字段映射决定，新增模板只需改配置，无需改代码。</p>
     */
    private Map<String, Object> buildData(SubscribeSettings settings, String scene, String content)
    {
        String summary = content == null ? "" : content;
        if (summary.length() > 20)
        {
            summary = summary.substring(0, 20);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : settings.fieldMapping().entrySet())
        {
            String value = switch (e.getValue() == null ? "" : e.getValue())
            {
                case "content" -> summary;
                case "now" -> ViTime.iso(ViTime.now());
                case "scene" -> scene;
                default -> "";
            };
            data.put(e.getKey(), Map.of("value", value));
        }
        return data;
    }

    private JSONObject postJson(String url, String body)
    {
        try
        {
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body, java.nio.charset.StandardCharsets.UTF_8))
                    .build();
            java.net.http.HttpResponse<String> resp = http.send(req, java.net.http.HttpResponse.BodyHandlers.ofString(
                    java.nio.charset.StandardCharsets.UTF_8));
            return JSON.parseObject(resp.body());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("调用微信接口异常：" + e.getMessage(), e);
        }
    }
}
