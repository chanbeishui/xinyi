package com.xinyi.rongli.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * 微信订阅消息参数快照（G03）。
 *
 * <p>模板 ID 由运营在微信公众平台「订阅消息」里申请后填入；任一场景留空表示
 * 该场景只发站内信，不影响其它场景。字段映射把模板占位符（thing1/time2…）
 * 映射到内部取值来源，避免改模板就要改代码。</p>
 */
public record SubscribeSettings(
        boolean enabled,
        String dispatchInvite,
        String assignmentAccepted,
        String resultReady,
        String refundDone,
        String page,
        String miniprogramState,
        String fieldsJson)
{
    /** 微信模板 ID：字母数字下划线连字符，长度 10~64 */
    public static final Pattern TEMPLATE_ID = Pattern.compile("^[A-Za-z0-9_-]{10,64}$");

    /** 模板字段名：thing1 / time2 / character_string3 / amount4 等 */
    public static final Pattern FIELD_KEY = Pattern.compile("^[a-z_]+\\d{1,2}$");

    /** 允许的取值来源 */
    public static final List<String> FIELD_SOURCES = List.of("content", "now", "scene");

    public static final List<String> STATES = List.of("developer", "trial", "formal");

    /** 场景 -> 模板 ID */
    public String templateOf(String scene)
    {
        if (scene == null)
        {
            return null;
        }
        String id = switch (scene)
        {
            case "DISPATCH_INVITE" -> dispatchInvite;
            case "ASSIGNMENT_ACCEPTED" -> assignmentAccepted;
            case "RESULT_READY" -> resultReady;
            case "REFUND_DONE" -> refundDone;
            default -> null;
        };
        return id == null || id.isBlank() ? null : id.trim();
    }

    /** 已配置模板的场景清单 */
    public List<String> configuredScenes()
    {
        List<String> scenes = new ArrayList<>();
        for (String scene : List.of("DISPATCH_INVITE", "ASSIGNMENT_ACCEPTED", "RESULT_READY", "REFUND_DONE"))
        {
            if (templateOf(scene) != null)
            {
                scenes.add(scene);
            }
        }
        return scenes;
    }

    /**
     * 格式类问题：填了但填错。这类必须拦在保存前，否则错误值会一直躺在 sys_config 里，
     * 直到某次真实下发才暴露。
     */
    public List<String> formatProblems()
    {
        List<String> problems = new ArrayList<>();
        checkTemplate("派单邀约", dispatchInvite, problems);
        checkTemplate("接单成功", assignmentAccepted, problems);
        checkTemplate("结果可查看", resultReady, problems);
        checkTemplate("退款完成", refundDone, problems);
        if (page != null && !page.isBlank()
                && (page.trim().startsWith("/") || page.trim().contains(" ")))
        {
            problems.add("跳转页面不要以 / 开头，也不要含空格，例如 pages/order/detail");
        }
        if (miniprogramState != null && !miniprogramState.isBlank()
                && !STATES.contains(miniprogramState.trim()))
        {
            problems.add("运行环境只能是 developer / trial / formal");
        }
        try
        {
            JSONObject fields = JSON.parseObject(fieldsJson == null || fieldsJson.isBlank()
                    ? "{}" : fieldsJson);
            for (String key : fields.keySet())
            {
                if (!FIELD_KEY.matcher(key).matches())
                {
                    problems.add("字段名 " + key + " 不合法，应形如 thing1 / time2");
                }
                String source = fields.getString(key);
                if (source == null || !FIELD_SOURCES.contains(source))
                {
                    problems.add("字段 " + key + " 的取值来源必须是 content / now / scene");
                }
            }
        }
        catch (Exception e)
        {
            problems.add("字段映射不是合法 JSON");
        }
        return problems;
    }

    /**
     * 配置问题清单（含"还没填"与"填错了"两类）。启用订阅消息时至少要有一个模板，
     * 否则整条链路只是"多一次无意义的站内信写入"，运营会以为已经推送到微信。
     */
    public List<String> problems()
    {
        List<String> problems = new ArrayList<>();
        if (!enabled)
        {
            return problems;
        }
        if (configuredScenes().isEmpty())
        {
            problems.add("至少配置一个场景的模板 ID");
        }
        if (page == null || page.isBlank())
        {
            problems.add("跳转页面不能为空");
        }
        if (fieldsJson == null || fieldsJson.isBlank() || "{}".equals(fieldsJson.trim()))
        {
            problems.add("字段映射不能为空，至少 {thing1: content}");
        }
        for (String problem : formatProblems())
        {
            if (!problems.contains(problem))
            {
                problems.add(problem);
            }
        }
        return problems;
    }

    public boolean ready()
    {
        return enabled && problems().isEmpty();
    }

    /** 字段映射解析结果，解析失败时退回默认 */
    public Map<String, String> fieldMapping()
    {
        Map<String, String> mapping = new LinkedHashMap<>();
        try
        {
            JSONObject fields = JSON.parseObject(fieldsJson == null || fieldsJson.isBlank()
                    ? "{\"thing1\":\"content\",\"time2\":\"now\"}" : fieldsJson);
            for (String key : fields.keySet())
            {
                mapping.put(key, fields.getString(key));
            }
        }
        catch (Exception e)
        {
            mapping.put("thing1", "content");
            mapping.put("time2", "now");
        }
        return mapping;
    }

    public String effectivePage()
    {
        return page == null || page.isBlank() ? "pages/index/index" : page.trim();
    }

    public String effectiveState()
    {
        return miniprogramState == null || miniprogramState.isBlank() ? "formal" : miniprogramState.trim();
    }

    private void checkTemplate(String label, String value, List<String> problems)
    {
        if (value == null || value.isBlank())
        {
            return;
        }
        if (!TEMPLATE_ID.matcher(value.trim()).matches())
        {
            problems.add(label + " 模板 ID 格式不正确（字母数字下划线连字符，10~64 位）");
        }
    }

    public static SubscribeSettings load(BizConfig cfg)
    {
        return new SubscribeSettings(
                cfg.getBool("rongli.subscribe.enabled", true),
                cfg.get("rongli.subscribe.template.dispatchInvite", ""),
                cfg.get("rongli.subscribe.template.assignmentAccepted", ""),
                cfg.get("rongli.subscribe.template.resultReady", ""),
                cfg.get("rongli.subscribe.template.refundDone", ""),
                cfg.get("rongli.subscribe.page", "pages/index/index"),
                cfg.get("rongli.subscribe.miniprogramState", "formal"),
                cfg.get("rongli.subscribe.fields", "{\"thing1\":\"content\",\"time2\":\"now\"}"));
    }
}
