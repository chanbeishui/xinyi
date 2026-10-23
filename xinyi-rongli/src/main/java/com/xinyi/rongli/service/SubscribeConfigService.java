package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.ConfigStore;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.PaySettings;
import com.xinyi.rongli.support.SubscribeSettings;
import com.xinyi.rongli.support.WechatMiniClient;

/**
 * 订阅消息配置读写与校验（G03）。
 *
 * <p>除模板 ID 外，本页同时承载小程序凭据（AppID/AppSecret）：订阅消息下发与
 * 手机号授权登录用的是同一套凭据，放在一处配置可避免两边不一致导致的
 * "登录正常但推送 40001"。</p>
 */
@Service
public class SubscribeConfigService
{
    private static final String UNCHANGED = "__UNCHANGED__";

    @Autowired
    private ConfigStore store;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private WechatMiniClient wechatMiniClient;

    /** 场景与中文名，供前端渲染四个模板输入项 */
    private static final Map<String, String> SCENES = new LinkedHashMap<>();

    static
    {
        SCENES.put("DISPATCH_INVITE", "派单邀约");
        SCENES.put("ASSIGNMENT_ACCEPTED", "接单成功");
        SCENES.put("RESULT_READY", "检测结果可查看");
        SCENES.put("REFUND_DONE", "退款完成");
    }

    // ------------------------------------------------------------------ 读

    public Map<String, Object> view()
    {
        SubscribeSettings s = SubscribeSettings.load(config);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("enabled", s.enabled());
        view.put("dispatchInvite", s.dispatchInvite());
        view.put("assignmentAccepted", s.assignmentAccepted());
        view.put("resultReady", s.resultReady());
        view.put("refundDone", s.refundDone());
        view.put("page", s.effectivePage());
        view.put("miniprogramState", s.effectiveState());
        view.put("fields", s.fieldsJson());
        view.put("scenes", sceneList(s));

        view.put("miniAppId", wechatMiniClient.effectiveAppId());
        view.put("miniAppSecretConfigured", !blank(wechatMiniClient.effectiveAppSecret()));
        view.put("miniAppSecretMasked", mask(wechatMiniClient.effectiveAppSecret()));
        view.put("mockWechat", wechatMiniClient.isMock());

        List<String> problems = s.problems();
        if (s.enabled() && blank(wechatMiniClient.effectiveAppId()))
        {
            problems.add("小程序 AppID（订阅消息与登录共用）");
        }
        if (s.enabled() && blank(wechatMiniClient.effectiveAppSecret()))
        {
            problems.add("小程序 AppSecret（订阅消息与登录共用）");
        }
        view.put("problems", problems);
        view.put("ready", s.enabled() && problems.isEmpty());
        view.put("status", s.enabled() ? (problems.isEmpty() ? "READY" : "INCOMPLETE") : "OFF");
        view.put("statusText", !s.enabled()
                ? "订阅消息已关闭：所有通知只发站内信"
                : (problems.isEmpty() ? "已配置完成，通知会同时下发站内信与订阅消息"
                        : "还缺少 " + problems.size() + " 项配置"));
        view.put("hint", hintOf(s, problems));
        view.put("fieldMeta", fieldMeta());
        return view;
    }

    private List<Map<String, Object>> sceneList(SubscribeSettings s)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String> e : SCENES.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("scene", e.getKey());
            item.put("label", e.getValue());
            item.put("templateId", s.templateOf(e.getKey()));
            item.put("configured", s.templateOf(e.getKey()) != null);
            list.add(item);
        }
        return list;
    }

    /** 字段元数据：与后端校验一一对应 */
    public List<Map<String, Object>> fieldMeta()
    {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(field("dispatchInvite", "派单邀约模板 ID", false, "text", "选填",
                "代驾收到新订单邀约时推送；留空则该场景只发站内信", null));
        list.add(field("assignmentAccepted", "接单成功模板 ID", false, "text", "选填",
                "司机接单后通知车主", null));
        list.add(field("resultReady", "结果可查看模板 ID", false, "text", "选填",
                "检测结果登记后通知车主", null));
        list.add(field("refundDone", "退款完成模板 ID", false, "text", "选填",
                "退款到账后通知车主", null));
        list.add(field("page", "点击跳转页面", true, "text", "pages/index/index",
                "不要以 / 开头；页面路径必须已在小程序内注册", null));
        list.add(field("miniprogramState", "运行环境", true, "select", "formal",
                "developer=开发版，trial=体验版，formal=正式版", null));
        list.add(field("fields", "字段映射", true, "textarea", "{\"thing1\":\"content\",\"time2\":\"now\"}",
                "JSON：模板字段名 -> 取值来源（content/now/scene）", null));
        list.add(field("miniAppId", "小程序 AppID", true, "text", "wx1234567890abcdef",
                "与登录共用；留空时沿用服务端配置", "^wx[0-9a-fA-F]{16}$"));
        list.add(field("miniAppSecret", "小程序 AppSecret", true, "secret", "粘贴后不可再查看",
                "与登录共用；已配置时提交 __UNCHANGED__ 保持原值", null));
        return list;
    }

    // ------------------------------------------------------------------ 写

    @Transactional
    public Map<String, Object> save(Map<String, Object> body)
    {
        if (body == null || body.isEmpty())
        {
            throw ApiException.validation("没有需要保存的配置项");
        }
        boolean enabled = body.get("enabled") == null
                ? config.getBool("rongli.subscribe.enabled", true)
                : Boolean.TRUE.equals(body.get("enabled")) || "true".equals(String.valueOf(body.get("enabled")));
        String dispatchInvite = text(body.get("dispatchInvite"), store.get("rongli.subscribe.template.dispatchInvite", ""));
        String assignmentAccepted = text(body.get("assignmentAccepted"),
                store.get("rongli.subscribe.template.assignmentAccepted", ""));
        String resultReady = text(body.get("resultReady"), store.get("rongli.subscribe.template.resultReady", ""));
        String refundDone = text(body.get("refundDone"), store.get("rongli.subscribe.template.refundDone", ""));
        String page = text(body.get("page"), store.get("rongli.subscribe.page", "pages/index/index"));
        String state = text(body.get("miniprogramState"), store.get("rongli.subscribe.miniprogramState", "formal"));
        String fields = text(body.get("fields"),
                store.get("rongli.subscribe.fields", "{\"thing1\":\"content\",\"time2\":\"now\"}"));
        String miniAppId = text(body.get("miniAppId"), wechatMiniClient.effectiveAppId());

        SubscribeSettings candidate = new SubscribeSettings(enabled, dispatchInvite, assignmentAccepted,
                resultReady, refundDone, page, state, fields);
        String miniSecret = resolveMiniSecret(body.get("miniAppSecret"));
        // 只有"填错了"才拒绝保存；"还没填"允许保存，由页面按 problems 提示还缺什么。
        // 否则运营先关开关、再想打开时会被必填项永久卡死。
        List<String> blockers = new ArrayList<>(enabled ? candidate.formatProblems() : List.of());
        if (enabled && !blank(miniAppId) && !PaySettings.APP_ID.matcher(miniAppId.trim()).matches())
        {
            blockers.add("小程序 AppID 格式不正确（wx 开头 18 位）");
        }
        if (!blockers.isEmpty())
        {
            throw ApiException.validation("订阅消息配置有误：" + String.join("；", blockers));
        }

        store.put("rongli.subscribe.enabled", String.valueOf(enabled));
        store.put("rongli.subscribe.template.dispatchInvite", dispatchInvite.trim());
        store.put("rongli.subscribe.template.assignmentAccepted", assignmentAccepted.trim());
        store.put("rongli.subscribe.template.resultReady", resultReady.trim());
        store.put("rongli.subscribe.template.refundDone", refundDone.trim());
        store.put("rongli.subscribe.page", page.trim());
        store.put("rongli.subscribe.miniprogramState", state.trim());
        store.put("rongli.subscribe.fields", fields.trim());
        store.put("rongli.mini.appId", miniAppId.trim());
        store.putSecret("rongli.mini.appSecret", miniSecret);

        Map<String, Object> result = new LinkedHashMap<>(view());
        result.put("saved", true);
        if (!enabled)
        {
            result.put("message", "已保存，订阅消息已关闭，仅发站内信");
        }
        else if (Boolean.TRUE.equals(result.get("ready")))
        {
            result.put("message", "已保存，下一条通知即按新配置下发");
        }
        else
        {
            Object problems = result.get("problems");
            result.put("message", "已保存。还差：" + (problems instanceof List<?> l && !l.isEmpty()
                    ? String.join("；", l.stream().map(String::valueOf).toList())
                    : "小程序凭据"));
        }
        return result;
    }

    /**
     * 连通性自检：用当前 AppID/AppSecret 真实换取 access_token。
     *
     * <p>这是唯一能证明"凭据配对且未过期"的本地手段，因此值得发一次外部请求；
     * 联调模式下跳过，避免无凭据时报错干扰配置流程。</p>
     */
    public Map<String, Object> selfCheck()
    {
        SubscribeSettings s = SubscribeSettings.load(config);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mockWechat", wechatMiniClient.isMock());
        result.put("appId", wechatMiniClient.effectiveAppId());
        result.put("secretConfigured", !blank(wechatMiniClient.effectiveAppSecret()));
        result.put("configuredScenes", s.configuredScenes());
        result.put("problems", s.problems());
        if (wechatMiniClient.isMock())
        {
            result.put("passed", s.problems().isEmpty());
            result.put("hint", "当前为联调模式（rongli.wechat.mock=true），未校验微信凭据；上线前请设为 false");
            return result;
        }
        try
        {
            // 能拿到令牌即证明 appId 与 appSecret 匹配
            String token = wechatMiniClient.accessTokenOf(null);
            result.put("passed", token != null && !token.isBlank());
            result.put("hint", "凭据有效，可以正常换取接口调用凭证");
        }
        catch (RuntimeException e)
        {
            result.put("passed", false);
            result.put("error", e.getMessage());
            result.put("hint", "换取接口调用凭证失败，请检查 AppID 与 AppSecret 是否属于同一个小程序");
        }
        return result;
    }

    // ------------------------------------------------------------------ 工具

    private String resolveMiniSecret(Object submitted)
    {
        if (submitted == null || UNCHANGED.equals(String.valueOf(submitted)))
        {
            return wechatMiniClient.effectiveAppSecret();
        }
        return String.valueOf(submitted).trim();
    }

    private String hintOf(SubscribeSettings s, List<String> problems)
    {
        if (!s.enabled())
        {
            return "订阅消息已关闭。开启后需至少配置一个场景的模板 ID，并填写小程序凭据。";
        }
        if (!problems.isEmpty())
        {
            return "以下配置缺失或格式不正确：" + String.join("、", problems);
        }
        if (wechatMiniClient.isMock())
        {
            return "配置已齐全，但服务端仍在联调模式（rongli.wechat.mock=true），不会真实下发。";
        }
        return "配置已齐全，保存后立即生效。建议先做一次连通性自检确认凭据可用。";
    }

    private Map<String, Object> field(String key, String label, boolean required, String type, String placeholder,
            String help, String pattern)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("required", required);
        item.put("type", type);
        item.put("placeholder", placeholder);
        item.put("help", help);
        item.put("pattern", pattern);
        return item;
    }

    private static boolean blank(String v)
    {
        return v == null || v.isBlank();
    }

    private String text(Object submitted, String fallback)
    {
        if (submitted == null)
        {
            return fallback == null ? "" : fallback;
        }
        return String.valueOf(submitted).trim();
    }

    private String mask(String value)
    {
        if (blank(value))
        {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 4 ? "****" : "****" + trimmed.substring(trimmed.length() - 4);
    }
}
