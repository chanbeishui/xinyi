package com.xinyi.rongli.integration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.IntegrationMapper;
import com.xinyi.rongli.service.SubscribeService;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.SmsClient;
import com.xinyi.rongli.support.SmsSettings;

/**
 * 业务事件的消息下发（V6.4 第六章：路由 → 模板 → 投递）。
 *
 * <p>设计约束，逐条对应资料包要求：</p>
 * <ul>
 *   <li><b>不阻断业务</b>：任何异常都在这里被吞掉并记日志，下单、派单、完单不会因为
 *       短信通道挂了而回滚。站内信是另一条独立链路，不受影响。</li>
 *   <li><b>未配置不瞎发</b>：没有路由、路由停用、模板未发布、通道未启用、
 *       账号没有手机号——一律记 SKIPPED 并留下原因码，绝不退回某个默认通道。</li>
 *   <li><b>只发一次</b>：靠 {@code uk_delivery_event(event_key, recipient_key, scene)}
 *       在数据库层去重，重复触发插入 0 行即跳过。</li>
 *   <li><b>ACCEPTED ≠ DELIVERED</b>：供应商受理只写 ACCEPTED，到达要等回执。</li>
 * </ul>
 */
@Service
public class NotificationDispatchService
{
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private static final String STATUS_PENDING = "PENDING";

    private static final String STATUS_ACCEPTED = "ACCEPTED";

    private static final String STATUS_FAILED = "FAILED";

    private static final String STATUS_SKIPPED = "SKIPPED";

    private static final String STATUS_UNKNOWN = "UNKNOWN";

    @Autowired
    private IntegrationMapper mapper;

    @Autowired
    private IntegrationConfigService configService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private Crypto crypto;

    /**
     * 业务事件入口。
     *
     * @param scene       模板场景，取值见 {@link IntegrationRules#SCENES}
     * @param audience    接收身份：OWNER / DRIVER / STATION
     * @param accountId   接收账号
     * @param bizId       业务主键（订单 id 等），参与去重；为空时按账号去重
     * @param ownerDeptId 归属组织，用于路由优先级；可为空表示只用全局路由
     * @param variables   模板变量，键为 {@link IntegrationRules#VARIABLES}
     */
    public void dispatch(String scene, String audience, Long accountId, Long bizId, Long ownerDeptId,
            Map<String, String> variables)
    {
        if (scene == null || accountId == null)
        {
            return;
        }
        try
        {
            doDispatch(scene, audience, accountId, bizId, ownerDeptId, variablesOf(bizId, variables));
        }
        catch (Exception e)
        {
            // 通知失败绝不冒泡到业务事务
            log.warn("消息下发异常 scene={} account={}：{}", scene, accountId, e.getMessage());
        }
    }

    /**
     * 组装模板变量。
     *
     * <p>全部变量键先放默认值、再用真实值覆盖，这样「模板绑了一个本轮没有数据的
     * 展示型变量」不会让整条通知失败（比如没有司机时 driverDisplayName 留空）；
     * 而模板绑了根本不认识的变量属于配置错误，仍然要判失败让后台看见。</p>
     */
    private Map<String, String> variablesOf(Long bizId, Map<String, String> extra)
    {
        Map<String, String> vars = new LinkedHashMap<>();
        for (String key : IntegrationRules.VARIABLES)
        {
            vars.put(key, "");
        }
        vars.put("driverDisplayName", "代驾师傅");
        vars.put("statusText", "");
        vars.put("resultText", "");

        if (bizId != null)
        {
            Row order = Row.of(mapper.findOrderForNotify(bizId));
            if (order.isNotEmpty())
            {
                vars.put("orderNo", nvl(order.str("orderNo")));
                vars.put("stationName", nvl(order.str("stationName")));
                vars.put("pickupWindow", window(order));
                JSONObject snapshot = Json.read(order.str("vehicleSnapshot"));
                if (snapshot != null && snapshot.getString("plateNo") != null)
                {
                    vars.put("plateNo", snapshot.getString("plateNo"));
                }
                vars.put("statusText", statusText(order.str("fulfillmentStatus")));
            }
        }
        if (extra != null)
        {
            extra.forEach((k, v) -> {
                if (k != null && v != null)
                {
                    vars.put(k, v);
                }
            });
        }
        return vars;
    }

    private static String window(Row order)
    {
        java.time.LocalDateTime start = order.time("pickupStartsAt");
        java.time.LocalDateTime end = order.time("pickupEndsAt");
        if (start == null)
        {
            return "";
        }
        String from = start.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        if (end == null)
        {
            return from;
        }
        return from + "-" + end.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static String statusText(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case "PENDING_DISPATCH" -> "待派单";
            case "ACCEPTED" -> "代驾已接单";
            case "GOING_PICKUP" -> "代驾前往接车";
            case "ARRIVED_PICKUP" -> "已到达接车点";
            case "PICKED_UP" -> "已接车";
            case "ARRIVED_STATION" -> "已到达检测点";
            case "INSPECTING" -> "检测中";
            case "INSPECTED" -> "检测完成";
            case "CAR_TAKEN" -> "车辆送回中";
            case "WAIT_CONFIRM" -> "待确认服务";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }

    private static String nvl(String value)
    {
        return value == null ? "" : value;
    }

    private void doDispatch(String scene, String audience, Long accountId, Long bizId, Long ownerDeptId,
            Map<String, String> variables)
    {
        // 路由按组织隔离（vi_notification_route.owner_dept_id NOT NULL），
        // 调用方不传时用业务单据自己的归属组织，不跨组织拿别人的模板。
        if (ownerDeptId == null && bizId != null)
        {
            Row order = Row.of(mapper.findOrderForNotify(bizId));
            ownerDeptId = order.lng("deptId");
        }
        String env = configService.runtimeEnvironment();
        Row route = Row.of(mapper.findApplicableRoute(ownerDeptId, env, scene, audience));
        if (route.isEmpty())
        {
            log.debug("场景 {} 未配置路由，仅保留站内信", scene);
            return;
        }
        if (!route.bool("enabled"))
        {
            log.debug("场景 {} 路由已停用，仅保留站内信", scene);
            return;
        }

        Row template = publishedTemplate(route.lng("primaryTemplateId"), env);
        if (template.isEmpty())
        {
            template = publishedTemplate(route.lng("fallbackTemplateId"), env);
        }
        if (template.isEmpty())
        {
            log.debug("场景 {} 没有已发布的模板，仅保留站内信", scene);
            return;
        }

        Row revision = Row.of(mapper.findTemplateRevision(template.lng("activeRevisionId")));
        if (revision.isEmpty() || !revision.bool("enabled"))
        {
            log.debug("场景 {} 模板当前版本未启用，仅保留站内信", scene);
            return;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("eventKey", scene + ":" + (bizId == null ? "-" : bizId));
        row.put("recipientKey", "ACCOUNT:" + accountId);
        row.put("ownerDeptId", ownerDeptId == null ? route.lng("ownerDeptId") : ownerDeptId);
        row.put("environment", env);
        row.put("routeId", route.lng("id"));
        row.put("scene", scene);
        row.put("provider", template.str("provider"));
        row.put("templateId", template.lng("id"));
        row.put("templateRevisionId", revision.lng("id"));
        row.put("profileId", revision.lng("profileId"));
        row.put("profileRevisionId", revision.lng("profileRevisionId"));
        row.put("status", STATUS_PENDING);
        if (mapper.insertDelivery(row) == 0)
        {
            // 唯一键冲突：同一事件已下发过，直接跳过
            return;
        }
        Long deliveryId = Row.of(row).lng("id");
        if (deliveryId == null)
        {
            return;
        }

        String provider = template.str("provider");
        try
        {
            if ("WECHAT_SUBSCRIBE".equals(provider))
            {
                sendSubscribe(deliveryId, accountId, revision, variables);
            }
            else
            {
                sendSms(deliveryId, accountId, revision, provider, variables);
            }
        }
        catch (Exception e)
        {
            // 投递行已经落库，绝不能让它永远停在 PENDING——后台需要看到明确的失败原因
            mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_INTERNAL_ERROR", e.getMessage());
            throw e;
        }
    }

    // ------------------------------------------------------------------ 短信

    private void sendSms(Long deliveryId, Long accountId, Row revision, String provider,
            Map<String, String> variables)
    {
        // 手机号密文单独取：findAccount 是通用查询，不携带敏感字段
        byte[] cipher = accountMapper.findPhoneCipher(accountId);
        if (cipher == null || cipher.length == 0)
        {
            mark(deliveryId, STATUS_SKIPPED, null, null, "VI_NOTIF_NO_PHONE", "接收账号未绑定手机号");
            return;
        }

        Row profile = Row.of(mapper.findProfile(revision.lng("profileId")));
        if (profile.isEmpty() || !"ACTIVE".equals(profile.str("lifecycle")))
        {
            mark(deliveryId, STATUS_SKIPPED, null, null, "VI_NOTIF_NO_PROFILE", "短信通道没有生效中的配置档案");
            return;
        }
        Map<String, Object> cfg = configService.configOf(revision);
        if (!IntegrationRules.bool(cfg, "enabled"))
        {
            mark(deliveryId, STATUS_SKIPPED, null, null, "VI_NOTIF_CHANNEL_DISABLED", "短信通道未启用");
            return;
        }

        String vendorTemplateId = revision.str("vendorTemplateId");
        if (vendorTemplateId == null || vendorTemplateId.isBlank())
        {
            mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_NO_VENDOR_TEMPLATE", "模板未登记供应商模板ID");
            return;
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (Map<String, Object> binding : bindingsOf(revision))
        {
            String key = str(binding.get("parameterKey"));
            String variable = str(binding.get("variable"));
            if (key == null || variable == null || !variables.containsKey(variable))
            {
                mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_VARIABLE_MISSING",
                        "模板绑定了未知变量 " + (variable == null ? "?" : variable) + "，已放弃下发");
                return;
            }
            params.put(key, variables.get(variable));
        }
        if (params.isEmpty())
        {
            mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_NO_PARAMETERS", "模板没有参数映射，下发内容不完整");
            return;
        }

        Map<String, String> secrets = configService.runtimeSecrets(profile);
        boolean aliyun = "ALIYUN_SMS".equals(provider);
        SmsSettings settings = new SmsSettings(true,
                aliyun ? SmsSettings.ALIYUN : SmsSettings.TENCENT,
                IntegrationRules.str(cfg, "signName"),
                vendorTemplateId,
                IntegrationRules.str(cfg, "accessKeyId"),
                secrets.get("accessKeySecret"),
                IntegrationRules.str(cfg, "secretId"),
                secrets.get("secretKey"),
                IntegrationRules.str(cfg, "smsSdkAppId"),
                IntegrationRules.str(cfg, "region"),
                0, 0, 0);

        String phone;
        try
        {
            phone = crypto.decrypt(cipher);
        }
        catch (Exception e)
        {
            mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_PHONE_UNDECRYPTABLE", "手机号密文无法解密");
            return;
        }

        try
        {
            SmsClient.Result result = smsClient.sendTemplate(settings, phone, vendorTemplateId, params);
            if (result.ok())
            {
                mark(deliveryId, STATUS_ACCEPTED, result.requestId(), null, null, null);
            }
            else
            {
                mark(deliveryId, STATUS_FAILED, null, null, safe(result.errorCode()), safe(result.errorMessage()));
            }
        }
        catch (IllegalStateException e)
        {
            // 网络层异常：请求可能已到达供应商，按资料包规则记 UNKNOWN，只查回执不重发
            mark(deliveryId, STATUS_UNKNOWN, null, null, "VI_NOTIF_NETWORK_UNKNOWN",
                    "调用供应商超时或网络中断，结果未知；请查询回执，不要直接重发");
        }
    }

    // ------------------------------------------------------------------ 订阅消息

    private void sendSubscribe(Long deliveryId, Long accountId, Row revision, Map<String, String> variables)
    {
        Row account = Row.of(accountMapper.findAccount(accountId));
        if (account.isEmpty() || !"ACTIVE".equals(account.str("status")))
        {
            mark(deliveryId, STATUS_SKIPPED, null, null, "VI_NOTIF_ACCOUNT_INACTIVE", "接收账号不存在或已停用");
            return;
        }
        String openId = account.str("openId");
        String appId = account.str("appId");

        Map<String, Object> data = new LinkedHashMap<>();
        for (Map<String, Object> binding : bindingsOf(revision))
        {
            String keyword = str(binding.get("keyword"));
            String variable = str(binding.get("variable"));
            if (keyword == null || variable == null || !variables.containsKey(variable))
            {
                mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_VARIABLE_MISSING",
                        "模板绑定了未知变量 " + (variable == null ? "?" : variable) + "，已放弃下发");
                return;
            }
            data.put(keyword, Map.of("value", variables.get(variable)));
        }
        if (data.isEmpty())
        {
            mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_NO_PARAMETERS", "模板没有字段映射，下发内容不完整");
            return;
        }

        JSONObject options = Json.read(revision.str("optionsJson"));
        String page = options == null ? null : options.getString("page");
        try
        {
            SubscribeService.Result result = subscribeService.sendPublished(appId, openId,
                    revision.str("vendorTemplateId"), page, data);
            if (result.delivered())
            {
                mark(deliveryId, STATUS_ACCEPTED, result.reason(), null, null, null);
            }
            else if (result.reason() != null && result.reason().contains("未订阅"))
            {
                // 用户没授权这次推送：终态，不算失败，也不再重试
                mark(deliveryId, STATUS_SKIPPED, null, null, "VI_NOTIF_USER_UNSUBSCRIBED", result.reason());
            }
            else
            {
                mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_SUBSCRIBE_FAILED", safe(result.reason()));
            }
        }
        catch (IllegalStateException e)
        {
            mark(deliveryId, STATUS_FAILED, null, null, "VI_NOTIF_SUBSCRIBE_FAILED", safe(e.getMessage()));
        }
    }

    // ------------------------------------------------------------------ 工具

    private Row publishedTemplate(Long templateId, String env)
    {
        if (templateId == null)
        {
            return Row.empty();
        }
        Row template = Row.of(mapper.findTemplate(templateId));
        if (template.isEmpty() || !env.equals(template.str("environment")))
        {
            // 环境不一致的模板不参与下发：TEST 模板绝不能打到 LIVE 用户
            return Row.empty();
        }
        if (template.lng("activeRevisionId") == null)
        {
            return Row.empty();
        }
        return template;
    }

    private void mark(Long deliveryId, String status, String requestId, String bizId, String code, String message)
    {
        mapper.markDeliveryResult(deliveryId, status, requestId, bizId, code,
                message == null ? null : (message.length() > 500 ? message.substring(0, 500) : message));
        if (STATUS_FAILED.equals(status) || STATUS_UNKNOWN.equals(status))
        {
            log.warn("消息下发未成功 delivery={} status={} code={} msg={}", deliveryId, status, code, message);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> bindingsOf(Row revision)
    {
        Object raw = revision.get("parameterBindings");
        if (raw == null)
        {
            return List.of();
        }
        try
        {
            return (List<Map<String, Object>>) (List<?>) JSON.parseArray(String.valueOf(raw), Map.class);
        }
        catch (Exception e)
        {
            return List.of();
        }
    }

    private static String str(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private static String safe(String value)
    {
        return value == null ? "UNKNOWN" : value;
    }
}
