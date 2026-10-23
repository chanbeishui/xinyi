package com.xinyi.rongli.integration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.xinyi.rongli.mapper.IntegrationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 订阅消息授权事件链（PRD 23.6）。
 *
 * <p>客户端回传的 accept 只是“用户点了允许”的报告，不等于服务端获得永久发送许可。
 * 因此这里只记录本次 prompt 的结果并绑定 nonce，真正发送仍要看业务事件、接收人和微信返回；
 * 用户拒绝或没有额度也不阻断下单、支付、交接和检测结果。</p>
 */
@Service
public class IntegrationSubscriptionService
{
    private static final long PROMPT_TTL_MINUTES = 10L;

    @Autowired
    private IntegrationMapper mapper;

    @Autowired
    private IntegrationConfigService configService;

    @Autowired
    private IntegrationMessageService messageService;

    @Autowired
    private Crypto crypto;

    @Transactional
    public Map<String, Object> createPrompt(Map<String, Object> body)
    {
        Long accountId = MiniContext.require().accountId;
        String objectType = IntegrationRules.str(body, "businessObjectType");
        if (objectType == null || !List.of("ORDER", "ASSIGNMENT", "STATEMENT", "DRIVER_APPLICATION").contains(objectType))
        {
            throw ApiException.validation("businessObjectType 不在允许范围内");
        }
        Long objectId = IntegrationConfigService.longOrNull(body.get("businessObjectId"));
        if (objectId == null)
        {
            throw ApiException.validation("请指定业务对象");
        }
        Long deptId = 100L;
        if ("ORDER".equals(objectType))
        {
            Row order = Row.of(mapper.findOrderById(objectId));
            if (order.isEmpty() || !accountId.equals(order.lng("ownerAccountId")))
            {
                throw ApiException.forbidden("只能为自己的订单请求订阅提醒");
            }
            if (order.lng("deptId") != null)
            {
                deptId = order.lng("deptId");
            }
        }
        Row appProfile = configService.activeProfile("WECHAT_APP", deptId);
        Row appRevision = configService.activeRevision(appProfile);
        String appId = appRevision.isEmpty() ? MiniContext.require().appId
                : IntegrationRules.str(configService.configOf(appRevision), "appId");
        if (appId == null)
        {
            throw ApiException.unprocessable("VI_CONFIG_INVALID", "小程序公共凭据尚未发布，无法申请订阅授权");
        }

        List<String> scenes = new ArrayList<>();
        for (Object scene : IntegrationRules.list(body, "scenes"))
        {
            String value = String.valueOf(scene);
            if (!IntegrationRules.SCENES.contains(value))
            {
                throw ApiException.validation("未知的通知场景：" + value);
            }
            scenes.add(value);
        }
        if (scenes.isEmpty())
        {
            throw ApiException.validation("请指定要申请的订阅场景");
        }
        List<Map<String, Object>> available = messageService.publishedSubscribeTemplates(deptId,
                configService.runtimeEnvironment());
        List<Map<String, Object>> selected = new ArrayList<>();
        for (Map<String, Object> item : available)
        {
            if (scenes.contains(String.valueOf(item.get("scene"))) && selected.size() < 3)
            {
                selected.add(item);
            }
        }
        if (selected.isEmpty())
        {
            throw ApiException.unprocessable("VI_CONFIG_TEMPLATE_NOT_PUBLISHED",
                    "当前没有可用的订阅模板；订阅不是业务前置条件，业务可继续");
        }
        if (mapper.countRecentPrompts(accountId) >= 3)
        {
            throw ApiException.conflict("VI_SUBSCRIPTION_RATE_LIMITED", "请求过于频繁，请稍后再试");
        }

        String nonce = crypto.randomToken();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("accountId", accountId);
        row.put("appId", appId);
        row.put("nonceHash", Crypto.hex(Crypto.sha256(nonce)));
        row.put("templateVersions", Json.write(selected));
        row.put("businessObjectType", objectType);
        row.put("businessObjectId", objectId);
        row.put("ttlMinutes", PROMPT_TTL_MINUTES);
        mapper.insertPrompt(row);
        Long promptId = Row.of(row).lng("id");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("promptId", String.valueOf(promptId));
        out.put("appId", appId);
        out.put("nonce", nonce);
        out.put("expiresAt", ViTime.iso(ViTime.now().plusMinutes(PROMPT_TTL_MINUTES)));
        out.put("templates", selected);
        out.put("permissionRequiredForBusiness", false);
        out.put("note", "用户拒绝订阅不影响登录、下单、支付、归还与检测结果保存，站内消息仍会送达");
        return out;
    }

    @Transactional
    public Map<String, Object> reportPrompt(Long promptId, Map<String, Object> body)
    {
        Long accountId = MiniContext.require().accountId;
        Row prompt = Row.of(mapper.findPrompt(promptId));
        if (prompt.isEmpty() || !accountId.equals(prompt.lng("accountId")))
        {
            throw ApiException.notFound("订阅请求不存在");
        }
        String nonce = IntegrationRules.str(body, "nonce");
        if (nonce == null || !Crypto.hex(Crypto.sha256(nonce)).equals(prompt.str("nonceHash")))
        {
            throw ApiException.forbidden("nonce 不匹配，拒绝伪造订阅结果");
        }
        if (!"CREATED".equals(prompt.str("status")))
        {
            throw ApiException.stateConflict("该订阅请求已处理或已过期，不能重复上报");
        }
        if (prompt.time("expiresAt") != null && prompt.time("expiresAt").isBefore(ViTime.now()))
        {
            mapper.expirePrompts();
            throw ApiException.stateConflict("订阅请求已过期，请重新发起");
        }
        List<Object> results = IntegrationRules.list(body, "results");
        if (results.isEmpty() || results.size() > 3)
        {
            throw ApiException.validation("results 需包含 1~3 条本次授权结果");
        }
        Set<String> allowed = new LinkedHashSet<>();
        for (Object item : JSON.parseArray(prompt.str("templateVersions")))
        {
            if (item instanceof Map<?, ?> map && map.get("vendorTemplateId") != null)
            {
                allowed.add(String.valueOf(map.get("vendorTemplateId")));
            }
        }
        Set<String> accepted = new LinkedHashSet<>();
        for (Object item : results)
        {
            if (!(item instanceof Map<?, ?> map))
            {
                throw ApiException.validation("results 结构不正确");
            }
            String vendorTemplateId = String.valueOf(map.get("vendorTemplateId"));
            String result = String.valueOf(map.get("result"));
            if (!allowed.contains(vendorTemplateId))
            {
                throw ApiException.validation("模板不在本次请求范围内：" + vendorTemplateId);
            }
            if (!List.of("accept", "reject", "ban", "filter").contains(result))
            {
                throw ApiException.validation("未知的授权结果：" + result);
            }
            if ("accept".equals(result))
            {
                accepted.add(vendorTemplateId);
            }
        }
        String report = Json.write(Map.of("results", results));
        if (mapper.reportPrompt(promptId, report, Crypto.hex(Crypto.sha256(report))) == 0)
        {
            throw ApiException.stateConflict("该订阅请求已被处理");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("promptId", String.valueOf(promptId));
        out.put("reported", true);
        out.put("acceptedTemplateIds", new ArrayList<>(accepted));
        out.put("sendEligibility", "VENDOR_DECIDES");
        out.put("businessContinues", true);
        out.put("note", "已记录本次授权意向；实际发送仍由微信按剩余次数与模板状态决定，拒绝不影响业务");
        return out;
    }
}
