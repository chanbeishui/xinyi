package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.mapper.SmsMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.ConfigStore;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.PaySettings;
import com.xinyi.rongli.support.SmsSettings;
import com.xinyi.rongli.support.ViTime;

/**
 * 短信通道配置读写与校验（G02）。
 *
 * <p>必填项随供应商变化：阿里云要 AccessKey 对，腾讯云要 SecretId/SecretKey/SdkAppId。
 * 校验规则集中在 {@link SmsSettings#missingFields()}，配置页与真实发送共用同一判定。</p>
 */
@Service
public class SmsConfigService
{
    private static final String UNCHANGED = "__UNCHANGED__";

    @Autowired
    private ConfigStore store;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SmsMapper smsMapper;

    // ------------------------------------------------------------------ 读

    public Map<String, Object> view()
    {
        SmsSettings s = SmsSettings.load(config, crypto);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("enabled", s.enabled());
        view.put("provider", s.provider());
        view.put("signName", s.signName());
        view.put("templateCode", s.templateCode());
        view.put("accessKeyId", s.accessKeyId());
        view.put("accessKeySecretConfigured", !blank(s.accessKeySecret()));
        view.put("accessKeySecretMasked", mask(s.accessKeySecret()));
        view.put("secretId", s.secretId());
        view.put("secretKeyConfigured", !blank(s.secretKey()));
        view.put("secretKeyMasked", mask(s.secretKey()));
        view.put("sdkAppId", s.sdkAppId());
        view.put("region", s.effectiveRegion());
        view.put("codeExpireSeconds", s.effectiveExpire());
        view.put("resendSeconds", s.effectiveResend());
        view.put("dailyLimit", s.effectiveDailyLimit());

        List<String> missing = s.missingFields();
        view.put("missing", missing);
        view.put("ready", s.ready());
        view.put("status", statusOf(s, missing));
        view.put("statusText", statusTextOf(s, missing));
        view.put("hint", hintOf(s, missing));
        view.put("fields", fieldMeta(s.provider()));
        view.put("recentLogs", recentLogs());
        return view;
    }

    /** 字段元数据按供应商切换可见性，避免让运维填另一家的凭据 */
    public List<Map<String, Object>> fieldMeta(String provider)
    {
        boolean aliyun = !SmsSettings.TENCENT.equalsIgnoreCase(provider);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(field("provider", "短信供应商", true, "select", "ALIYUN 或 TENCENT", "决定下面需要填哪一组凭据", null));
        list.add(field("signName", "短信签名", true, "text", "例如：新意出行",
                "必须是供应商后台已审核通过的签名，2~20 字符", null));
        list.add(field("templateCode", "模板编号", true, "text",
                aliyun ? "例如 SMS_123456789" : "例如 1234567",
                "验证码模板，模板变量必须恰好一个，值为验证码", null));
        if (aliyun)
        {
            list.add(field("accessKeyId", "AccessKeyId", true, "text", "LTAI...",
                    "阿里云 RAM 用户 AccessKey，建议只授予短信发送权限", null));
            list.add(field("accessKeySecret", "AccessKeySecret", true, "secret", "粘贴后不可再查看",
                    "阿里云 RAM 用户密钥，保存后加密存储", null));
        }
        else
        {
            list.add(field("secretId", "SecretId", true, "text", "AKID...", "腾讯云 API 密钥 SecretId", null));
            list.add(field("secretKey", "SecretKey", true, "secret", "粘贴后不可再查看",
                    "腾讯云 API 密钥 SecretKey，保存后加密存储", null));
            list.add(field("sdkAppId", "SdkAppId", true, "text", "例如 1400000000",
                    "短信应用 ID，腾讯云控制台 → 短信 → 应用管理", "^\\d{6,20}$"));
            list.add(field("region", "地域", false, "text", "ap-guangzhou", "一般保持默认", null));
        }
        list.add(field("codeExpireSeconds", "验证码有效期（秒）", true, "number", "300", "60~1800", null));
        list.add(field("resendSeconds", "重发间隔（秒）", true, "number", "60", "10~600", null));
        list.add(field("dailyLimit", "单号日上限（条）", true, "number", "10", "1~50", null));
        return list;
    }

    private List<Map<String, Object>> recentLogs()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : smsMapper.listRecent(20))
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(row.get("id")));
            item.put("phoneMasked", row.get("phone_masked"));
            item.put("scene", row.get("scene"));
            item.put("provider", row.get("provider"));
            item.put("status", row.get("status"));
            item.put("errorCode", row.get("error_code"));
            item.put("errorMessage", row.get("error_message"));
            Object created = row.get("created_at");
            item.put("createdAt", created instanceof java.time.LocalDateTime dt ? ViTime.iso(dt) : String.valueOf(created));
            rows.add(item);
        }
        return rows;
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
                ? config.getBool("rongli.sms.enabled", false)
                : Boolean.TRUE.equals(body.get("enabled")) || "true".equals(String.valueOf(body.get("enabled")));
        String provider = text(body.get("provider"), store.get("rongli.sms.provider", SmsSettings.ALIYUN))
                .toUpperCase();
        if (!SmsSettings.ALIYUN.equals(provider) && !SmsSettings.TENCENT.equals(provider))
        {
            throw ApiException.validation("短信供应商只能是 ALIYUN 或 TENCENT");
        }
        String signName = text(body.get("signName"), store.get("rongli.sms.signName", ""));
        String templateCode = text(body.get("templateCode"), store.get("rongli.sms.templateCode", ""));
        String accessKeyId = text(body.get("accessKeyId"), store.get("rongli.sms.accessKeyId", ""));
        String secretId = text(body.get("secretId"), store.get("rongli.sms.secretId", ""));
        String sdkAppId = text(body.get("sdkAppId"), store.get("rongli.sms.sdkAppId", ""));
        String region = text(body.get("region"), store.get("rongli.sms.region", "ap-guangzhou"));
        int expire = intOf(body.get("codeExpireSeconds"), config.getInt("rongli.sms.codeExpireSeconds", 300));
        int resend = intOf(body.get("resendSeconds"), config.getInt("rongli.sms.resendSeconds", 60));
        int daily = intOf(body.get("dailyLimit"), config.getInt("rongli.sms.dailyLimit", 10));

        String accessKeySecret = resolveSecret(body.get("accessKeySecret"), "rongli.sms.accessKeySecret");
        String secretKey = resolveSecret(body.get("secretKey"), "rongli.sms.secretKey");

        SmsSettings candidate = new SmsSettings(enabled, provider, signName, templateCode, accessKeyId,
                accessKeySecret, secretId, secretKey, sdkAppId, region, expire, resend, daily);
        List<String> missing = candidate.missingFields();
        if (!missing.isEmpty())
        {
            throw ApiException.validation("短信配置不完整：" + String.join("；", missing));
        }
        if (expire < 60 || expire > 1800)
        {
            throw ApiException.validation("验证码有效期需在 60~1800 秒之间");
        }
        if (resend < 10 || resend > 600)
        {
            throw ApiException.validation("重发间隔需在 10~600 秒之间");
        }
        if (daily < 1 || daily > 50)
        {
            throw ApiException.validation("单号日上限需在 1~50 条之间");
        }

        store.put("rongli.sms.enabled", String.valueOf(enabled));
        store.put("rongli.sms.provider", provider);
        store.put("rongli.sms.signName", signName.trim());
        store.put("rongli.sms.templateCode", templateCode.trim());
        store.put("rongli.sms.accessKeyId", accessKeyId.trim());
        store.put("rongli.sms.secretId", secretId.trim());
        store.put("rongli.sms.sdkAppId", sdkAppId.trim());
        store.put("rongli.sms.region", region.trim());
        store.put("rongli.sms.codeExpireSeconds", String.valueOf(expire));
        store.put("rongli.sms.resendSeconds", String.valueOf(resend));
        store.put("rongli.sms.dailyLimit", String.valueOf(daily));
        store.putSecret("rongli.sms.accessKeySecret", accessKeySecret);
        store.putSecret("rongli.sms.secretKey", secretKey);

        Map<String, Object> result = new LinkedHashMap<>(view());
        result.put("saved", true);
        result.put("message", enabled ? "已保存，下一次发送即生效" : "已保存，当前为联调模式（验证码回显）");
        return result;
    }

    /**
     * 连通性测试：用已保存的配置真实发送一条短信。
     *
     * <p>测试短信同样计入频控与日志，避免被当成绕过限制的口子。</p>
     */
    public Map<String, Object> test(String phone)
    {
        if (phone == null || !phone.matches("\\d{11}"))
        {
            throw ApiException.validation("请输入 11 位手机号");
        }
        SmsSettings s = SmsSettings.load(config, crypto);
        if (!s.enabled())
        {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("tested", false);
            view.put("reason", "短信通道未启用，未发起真实发送");
            view.put("hint", "先打开「启用真实下发」并保存，再点测试");
            return view;
        }
        if (!s.ready())
        {
            throw ApiException.validation("短信配置不完整：" + String.join("；", s.missingFields()));
        }
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        Map<String, Object> sent = smsService.sendCode(null, phone, code, SmsService.SCENE_TEST);
        Map<String, Object> view = new LinkedHashMap<>(sent);
        view.put("tested", true);
        view.put("phoneMasked", SmsService.maskPhone(phone));
        view.put("hint", "已提交供应商，请确认是否收到；未收到请检查签名与模板审核状态");
        return view;
    }

    // ------------------------------------------------------------------ 工具

    private String resolveSecret(Object submitted, String key)
    {
        if (submitted == null)
        {
            return PaySettings.secret(config, crypto, key);
        }
        String text = String.valueOf(submitted);
        if (UNCHANGED.equals(text))
        {
            return PaySettings.secret(config, crypto, key);
        }
        return text.trim();
    }

    private String statusOf(SmsSettings s, List<String> missing)
    {
        if (!s.enabled())
        {
            return "MOCK";
        }
        return missing.isEmpty() ? "READY" : "INCOMPLETE";
    }

    private String statusTextOf(SmsSettings s, List<String> missing)
    {
        return switch (statusOf(s, missing))
        {
            case "MOCK" -> "联调模式：验证码直接回显，不发送真实短信";
            case "READY" -> "已配置完成，验证码将通过 " + s.provider() + " 真实下发";
            default -> "还缺少 " + missing.size() + " 项必填配置";
        };
    }

    private String hintOf(SmsSettings s, List<String> missing)
    {
        if (!s.enabled())
        {
            return "当前未启用真实短信。小程序端仍可用微信手机号一键授权登录，短信仅作为兜底。";
        }
        if (!missing.isEmpty())
        {
            return "以下必填项缺失或格式不正确：" + String.join("、", missing);
        }
        return "配置已齐全，保存后立即生效。建议先用下方「发送测试短信」确认签名与模板可用。";
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

    private int intOf(Object value, int fallback)
    {
        if (value == null || String.valueOf(value).isBlank())
        {
            return fallback;
        }
        try
        {
            return Integer.parseInt(String.valueOf(value).trim());
        }
        catch (NumberFormatException e)
        {
            throw ApiException.validation("必须是整数：" + value);
        }
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
