package com.xinyi.rongli.integration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import com.alibaba.fastjson2.JSON;

/**
 * V6.4 第23章的字段规则。
 *
 * <p>这里只做「不产生副作用」的本地校验：不发短信、不扣款、不访问供应商。
 * 保存草稿允许不完整；发布必须把启用项的必填全部补齐——两者用 {@code forPublish} 区分，
 * 不把发布要求提前压到保存阶段，也不允许用草稿的“能存”当“能发”。</p>
 */
public final class IntegrationRules
{
    private IntegrationRules()
    {
    }

    public static final List<String> PROFILE_KINDS = List.of(
            "PAYMENT_POLICY", "WECHAT_PAY", "TENCENT_SMS", "ALIYUN_SMS", "WECHAT_APP");

    public static final List<String> ENVIRONMENTS = List.of("TEST", "LIVE");

    public static final List<String> SCOPES = List.of("OWNER", "DRIVER", "STATION");

    public static final List<String> PROVIDERS = List.of("TENCENT_SMS", "ALIYUN_SMS", "WECHAT_SUBSCRIBE");

    public static final List<String> SCENES = List.of("LOGIN_OTP", "PHONE_VERIFY", "BOOKING_CONFIRMED",
            "DRIVER_ASSIGNED", "PICKUP_ARRIVED", "INSPECTION_FINISHED", "RETURN_COMPLETED",
            "AFTERSALE_DECIDED", "REFUND_CHANGED", "DRIVER_INVITE", "STATEMENT_READY");

    public static final List<String> VARIABLES = List.of("code", "ttlMinutes", "orderNo", "plateNo",
            "pickupWindow", "stationName", "driverDisplayName", "statusText", "resultText", "amountYuan",
            "statementNo", "serviceDate", "supportPhone");

    public static final List<String> PAGE_KEYS = List.of("orderNo", "statementId", "taskId");

    private static final Pattern MCH_ID = Pattern.compile("^\\d{8,12}$");

    private static final Pattern APP_ID = Pattern.compile("^wx[0-9a-fA-F]{16}$");

    private static final Pattern SERIAL = Pattern.compile("^[0-9A-Fa-f]{16,64}$");

    private static final Pattern KEYWORD = Pattern.compile("^[a-zA-Z]+[0-9]+$");

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private static final Pattern MASK = Pattern.compile("^\\*+$");

    /** 每种配置允许的凭据槽；用户不能自造密钥用途 */
    public static Set<String> slotsOf(String kind)
    {
        return switch (kind)
        {
            case "PAYMENT_POLICY" -> Set.of("offlineBankAccount");
            case "WECHAT_PAY" -> Set.of("merchantPrivateKey", "apiV3Key");
            case "TENCENT_SMS" -> Set.of("secretKey");
            case "ALIYUN_SMS" -> Set.of("accessKeySecret");
            case "WECHAT_APP" -> Set.of("appSecret");
            default -> Set.of();
        };
    }

    public static String credentialKindOf(String slot)
    {
        return switch (slot)
        {
            case "merchantPrivateKey" -> "PRIVATE_KEY";
            case "apiV3Key" -> "API_V3_KEY";
            case "appSecret" -> "APP_SECRET";
            case "offlineBankAccount" -> "BANK_ACCOUNT";
            default -> "CLOUD_SECRET";
        };
    }

    // ------------------------------------------------------------ 通用取值

    public static String str(Map<String, Object> map, String key)
    {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }

    public static boolean bool(Map<String, Object> map, String key)
    {
        Object v = map == null ? null : map.get(key);
        if (v instanceof Boolean b)
        {
            return b;
        }
        return v != null && ("true".equalsIgnoreCase(String.valueOf(v)) || "1".equals(String.valueOf(v)));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj(Map<String, Object> map, String key)
    {
        Object v = map == null ? null : map.get(key);
        return v instanceof Map ? (Map<String, Object>) v : Map.of();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> list(Map<String, Object> map, String key)
    {
        Object v = map == null ? null : map.get(key);
        return v instanceof List ? (List<Object>) v : List.of();
    }

    // ------------------------------------------------------------ 校验

    /**
     * @param secrets 该 profile 当前 ACTIVE 的凭据槽集合（不含值，只有槽名）
     */
    public static List<Map<String, Object>> validate(String kind, Map<String, Object> config,
            Set<String> secrets, boolean forPublish)
    {
        List<Map<String, Object>> issues = new ArrayList<>();
        switch (kind)
        {
            case "PAYMENT_POLICY" -> paymentPolicy(config, issues, forPublish);
            case "WECHAT_PAY" -> wechatPay(config, secrets, issues, forPublish);
            case "TENCENT_SMS" -> tencentSms(config, secrets, issues, forPublish);
            case "ALIYUN_SMS" -> aliyunSms(config, secrets, issues, forPublish);
            case "WECHAT_APP" -> wechatApp(config, secrets, issues, forPublish);
            default -> error(issues, "kind", "VI_CONFIG_INVALID", "未知的配置类型：" + kind);
        }
        return issues;
    }

    private static void paymentPolicy(Map<String, Object> c, List<Map<String, Object>> issues, boolean publish)
    {
        if (!c.containsKey("offlineEnabled"))
        {
            error(issues, "offlineEnabled", "VI_CONFIG_INVALID", "请明确线下支付开关（即使关闭也要提交 false）");
        }
        if (!c.containsKey("wechatEnabled"))
        {
            error(issues, "wechatEnabled", "VI_CONFIG_INVALID", "请明确微信支付开关（即使关闭也要提交 false）");
        }
        boolean offline = bool(c, "offlineEnabled");
        boolean wechat = bool(c, "wechatEnabled");
        if (!offline && !wechat)
        {
            warn(issues, "offlineEnabled", "VI_CONFIG_NO_PAYMENT_METHOD",
                    "两种支付方式都关闭：将暂停新付费预约（历史订单与退款仍可查），发布需显式确认");
        }
        if (!offline)
        {
            return;
        }
        require(c, "offlineCollectionMode", 40, issues, "线下收款模式不能为空");
        String mode = str(c, "offlineCollectionMode");
        if (mode != null && !"PREPAID_MANUAL_VERIFIED".equals(mode))
        {
            error(issues, "offlineCollectionMode", "VI_CONFIG_INVALID",
                    "本版只支持 PREPAID_MANUAL_VERIFIED（先收款、后台登记、独立核实），不支持服务后结算");
        }
        require(c, "offlineInstructions", 1000, issues, "开启线下支付必须填写收款说明");
        require(c, "offlineContactName", 80, issues, "开启线下支付必须填写联系人");
        String phone = require(c, "offlineContactPhone", 30, issues, "开启线下支付必须填写联系电话");
        if (phone != null && !PHONE.matcher(phone).matches() && !phone.matches("^0\\d{2,3}-?\\d{7,8}$"))
        {
            error(issues, "offlineContactPhone", "VI_CONFIG_INVALID", "联系电话格式不正确，请填写企业对外服务电话");
        }
        List<Object> methods = list(c, "offlineMethods");
        if (methods.isEmpty())
        {
            error(issues, "offlineMethods", "VI_CONFIG_INVALID", "开启线下支付时至少选择一种收款方式（CASH/BANK_TRANSFER）");
        }
        for (Object m : methods)
        {
            if (!List.of("CASH", "BANK_TRANSFER").contains(String.valueOf(m)))
            {
                error(issues, "offlineMethods", "VI_CONFIG_INVALID", "不支持的线下收款方式：" + m);
            }
        }
        if (methods.contains("BANK_TRANSFER"))
        {
            require(c, "offlineAccountName", 200, issues, "启用转账收款必须填写对公户名");
            require(c, "offlineBankName", 100, issues, "启用转账收款必须填写开户行");
            if (publish && str(c, "offlineAccountName") != null)
            {
                // 账号与户名必须成对；账号本身只存密文，这里只校验“是否已配置”
                info(issues, "offlineBankAccount", "VI_CONFIG_SECRET_CHECK",
                        "对公账号以密文保存，发布前请确认已登记（页面上显示为已配置/未配置）");
            }
        }
    }

    private static void wechatPay(Map<String, Object> c, Set<String> secrets, List<Map<String, Object>> issues,
            boolean publish)
    {
        require(c, "merchantName", 200, issues, "请填写商户名称，用于后台核对与公开收款说明");
        String mchid = require(c, "mchid", 32, issues, "请填写微信支付商户号");
        if (mchid != null && !MCH_ID.matcher(mchid).matches())
        {
            error(issues, "mchid", "VI_CONFIG_INVALID", "商户号应为 8~12 位数字；不要把商户号当数值处理");
        }
        Map<String, Object> appRef = obj(c, "appProfile");
        if (str(appRef, "profileId") == null || str(appRef, "revisionId") == null)
        {
            error(issues, "appProfile", "VI_CONFIG_DEPENDENCY_MISSING",
                    "请选择已发布的小程序公共凭据（B53），支付 AppID 必须与实际登录小程序一致");
        }
        String serial = require(c, "merchantCertificateSerial", 128, issues, "请填写商户 API 证书序列号");
        if (serial != null && !SERIAL.matcher(serial).matches())
        {
            error(issues, "merchantCertificateSerial", "VI_CONFIG_INVALID", "证书序列号应为 16~64 位十六进制");
        }
        String mode = require(c, "verificationMode", 40, issues, "请选择验签方式");
        if (mode != null && !List.of("WECHATPAY_PUBLIC_KEY", "PLATFORM_CERT").contains(mode))
        {
            error(issues, "verificationMode", "VI_CONFIG_INVALID", "验签方式只能是 WECHATPAY_PUBLIC_KEY 或 PLATFORM_CERT");
        }
        if ("WECHATPAY_PUBLIC_KEY".equals(mode))
        {
            require(c, "wechatPayPublicKeyId", 128, issues, "公钥模式必须填写微信支付公钥ID");
            require(c, "wechatPayPublicKeyPem", 16384, issues, "公钥模式必须上传微信支付公钥 PEM");
        }
        if ("PLATFORM_CERT".equals(mode) && list(c, "platformCertificates").isEmpty())
        {
            error(issues, "platformCertificates", "VI_CONFIG_INVALID",
                    "平台证书模式必须提交有效平台证书；新商户默认使用公钥模式，不强制下载平台证书");
        }
        if (!bool(c, "bindingAcknowledged"))
        {
            error(issues, "bindingAcknowledged", "VI_CONFIG_INVALID",
                    "请在微信商户平台确认该 AppID 与商户号已绑定，并勾选确认依据");
        }
        if (secrets == null || !secrets.contains("merchantPrivateKey"))
        {
            error(issues, "credentials.merchantPrivateKey", "VI_CONFIG_SECRET_MISSING",
                    "缺少商户签名私钥，请在本次提交中选择替换并上传 PEM");
        }
        if (secrets == null || !secrets.contains("apiV3Key"))
        {
            error(issues, "credentials.apiV3Key", "VI_CONFIG_SECRET_MISSING",
                    "缺少 APIv3 密钥（32 字节），不能使用 AppSecret 代替");
        }
        if (publish)
        {
            info(issues, "paymentNotifyUrl", "VI_CONFIG_UNVERIFIED",
                    "本地校验只能证明格式与密钥可解析；真实收款、退款与小程序绑定仍需 L01 实际商户联调");
        }
    }

    private static void tencentSms(Map<String, Object> c, Set<String> secrets, List<Map<String, Object>> issues,
            boolean publish)
    {
        String apiVersion = str(c, "apiVersion");
        if (!"2021-01-11".equals(apiVersion))
        {
            error(issues, "apiVersion", "VI_CONFIG_INVALID", "腾讯云短信接口版本固定为 2021-01-11，不接受任意版本");
        }
        require(c, "region", 64, issues, "请填写腾讯云 Region");
        require(c, "smsSdkAppId", 32, issues, "请填写短信 SdkAppId");
        require(c, "signName", 100, issues, "请填写短信签名内容（不是签名ID）");
        require(c, "secretId", 128, issues, "请填写腾讯云 SecretId");
        boolean enabled = bool(c, "enabled");
        if (enabled && (secrets == null || !secrets.contains("secretKey")))
        {
            error(issues, "credentials.secretKey", "VI_CONFIG_SECRET_MISSING", "启用腾讯云短信前必须登记 SecretKey");
        }
        if (enabled && publish)
        {
            info(issues, "enabled", "VI_CONFIG_UNVERIFIED",
                    "启用只代表可尝试下发；真实到达率与回执需在测试白名单验证，ACCEPTED 不等于 DELIVERED");
        }
    }

    private static void aliyunSms(Map<String, Object> c, Set<String> secrets, List<Map<String, Object>> issues,
            boolean publish)
    {
        String apiVersion = str(c, "apiVersion");
        if (!"2017-05-25".equals(apiVersion))
        {
            error(issues, "apiVersion", "VI_CONFIG_INVALID", "阿里云短信接口版本固定为 2017-05-25，不接受任意版本");
        }
        require(c, "region", 64, issues, "请填写阿里云 RegionId（默认 cn-hangzhou）");
        require(c, "signName", 100, issues, "请填写短信签名内容（不是签名ID）");
        require(c, "accessKeyId", 128, issues, "请填写阿里云 AccessKeyId");
        boolean enabled = bool(c, "enabled");
        if (enabled && (secrets == null || !secrets.contains("accessKeySecret")))
        {
            error(issues, "credentials.accessKeySecret", "VI_CONFIG_SECRET_MISSING",
                    "启用阿里云短信前必须登记 AccessKeySecret");
        }
        if (enabled && publish)
        {
            info(issues, "enabled", "VI_CONFIG_UNVERIFIED",
                    "阿里云 SendSms 没有供应商幂等保证；超时记 UNKNOWN 查回执，不自动跨通道重发");
        }
    }

    private static void wechatApp(Map<String, Object> c, Set<String> secrets, List<Map<String, Object>> issues,
            boolean publish)
    {
        String appId = require(c, "appId", 64, issues, "请填写小程序 AppID");
        if (appId != null && !APP_ID.matcher(appId).matches())
        {
            error(issues, "appId", "VI_CONFIG_INVALID", "小程序 AppID 应为 wx + 16 位十六进制");
        }
        require(c, "appName", 100, issues, "请填写小程序名称");
        if (!c.containsKey("enabled"))
        {
            error(issues, "enabled", "VI_CONFIG_INVALID", "请明确该应用凭据是否启用");
        }
        if (bool(c, "enabled") && (secrets == null || !secrets.contains("appSecret")))
        {
            error(issues, "credentials.appSecret", "VI_CONFIG_SECRET_MISSING",
                    "启用小程序凭据前必须登记 AppSecret（仅服务端使用，不用于支付 APIv3）");
        }
    }

    // ------------------------------------------------------------ 模板校验

    public static List<Map<String, Object>> validateSmsTemplate(String provider, Map<String, Object> draft,
            boolean forPublish)
    {
        List<Map<String, Object>> issues = new ArrayList<>();
        if (draft == null)
        {
            error(issues, "provider", "VI_CONFIG_INVALID", "模板不能为空");
            return issues;
        }
        if (str(draft, "profileId") == null || str(draft, "revisionId") == null)
        {
            error(issues, "profile", "VI_CONFIG_DEPENDENCY_MISSING", "请选择已发布的短信通道配置");
        }
        String vendorTemplateId = str(draft, "vendorTemplateId");
        if (forPublish && (vendorTemplateId == null || vendorTemplateId.isBlank()))
        {
            error(issues, "vendorTemplateId", "VI_CONFIG_INVALID",
                    "发布前必须填写供应商审核通过的模板ID/TemplateCode；本地保存不能替代供应商审批");
        }
        if (!bool(draft, "enabled"))
        {
            return issues;
        }
        List<Object> params = list(draft, "parameters");
        Set<String> keys = new LinkedHashSet<>();
        for (int i = 0; i < params.size(); i++)
        {
            if (!(params.get(i) instanceof Map))
            {
                error(issues, "parameters[" + i + "]", "VI_CONFIG_INVALID", "参数映射格式不正确");
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> p = (Map<String, Object>) params.get(i);
            String key = str(p, "parameterKey");
            String variable = str(p, "variable");
            if (key == null || key.isBlank())
            {
                error(issues, "parameters[" + i + "].parameterKey", "VI_CONFIG_INVALID",
                        "第 " + (i + 1) + " 个参数缺少供应商参数键");
            }
            else if (!keys.add(key))
            {
                error(issues, "parameters[" + i + "].parameterKey", "VI_CONFIG_INVALID", "参数键重复：" + key);
            }
            if (variable == null || !VARIABLES.contains(variable))
            {
                error(issues, "parameters[" + i + "].variable", "VI_CONFIG_INVALID",
                        "第 " + (i + 1) + " 个参数来源变量不在白名单内");
            }
            if ("TENCENT_SMS".equals(provider) && key != null && !key.matches("^[1-9]\\d{0,2}$"))
            {
                error(issues, "parameters[" + i + "].parameterKey", "VI_CONFIG_INVALID",
                        "腾讯云按位置顺序渲染，参数键应为 1、2、3…");
            }
        }
        if (forPublish && params.isEmpty())
        {
            error(issues, "parameters", "VI_CONFIG_INVALID", "启用中的模板至少需要一个参数映射，否则下发内容不完整");
        }
        return issues;
    }

    public static List<Map<String, Object>> validateSubscribeTemplate(Map<String, Object> draft, boolean forPublish)
    {
        List<Map<String, Object>> issues = new ArrayList<>();
        if (str(draft, "profileId") == null || str(draft, "revisionId") == null)
        {
            error(issues, "profile", "VI_CONFIG_DEPENDENCY_MISSING", "请选择已发布的小程序公共凭据");
        }
        String vendorTemplateId = str(draft, "vendorTemplateId");
        if (forPublish && (vendorTemplateId == null || vendorTemplateId.isBlank()))
        {
            error(issues, "vendorTemplateId", "VI_CONFIG_INVALID", "发布前必须填写该小程序真实订阅模板ID");
        }
        String pageRoute = str(draft, "pageRoute");
        if (forPublish && (pageRoute == null || pageRoute.isBlank()))
        {
            error(issues, "pageRoute", "VI_CONFIG_INVALID", "本项目要求发布时填写跳转页面（从已发布小程序路由白名单选择）");
        }
        if (pageRoute != null && (pageRoute.startsWith("http") || pageRoute.contains("://")))
        {
            error(issues, "pageRoute", "VI_CONFIG_INVALID", "跳转页面只能是小程序内部路由，不能填外部URL");
        }
        for (Object k : list(draft, "pageParameterKeys"))
        {
            if (!PAGE_KEYS.contains(String.valueOf(k)))
            {
                error(issues, "pageParameterKeys", "VI_CONFIG_INVALID",
                        "页面参数只允许 orderNo / statementId / taskId，禁止手机号、密钥等参数");
            }
        }
        String state = str(draft, "miniProgramState");
        if (state != null && !List.of("formal", "trial", "developer").contains(state))
        {
            error(issues, "miniProgramState", "VI_CONFIG_INVALID", "miniprogram_state 只能是 formal / trial / developer");
        }
        if ("LONG".equals(str(draft, "subscriptionType")) && str(draft, "longTermQualificationEvidenceId") == null)
        {
            error(issues, "subscriptionType", "VI_CONFIG_INVALID", "长期订阅必须提供真实资格核验证据，默认一次性订阅");
        }
        List<Object> params = list(draft, "parameters");
        Set<String> keys = new LinkedHashSet<>();
        for (int i = 0; i < params.size(); i++)
        {
            if (!(params.get(i) instanceof Map))
            {
                error(issues, "parameters[" + i + "]", "VI_CONFIG_INVALID", "参数映射格式不正确");
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> p = (Map<String, Object>) params.get(i);
            String keyword = str(p, "keyword");
            if (keyword == null || !KEYWORD.matcher(keyword).matches())
            {
                error(issues, "parameters[" + i + "].keyword", "VI_CONFIG_INVALID",
                        "字段键必须是实际模板 key（如 thing1 / time2 / amount3），不能假设都是 thing1–thing5");
            }
            else if (!keys.add(keyword))
            {
                error(issues, "parameters[" + i + "].keyword", "VI_CONFIG_INVALID", "模板字段重复：" + keyword);
            }
            String variable = str(p, "variable");
            if (variable == null || !VARIABLES.contains(variable))
            {
                error(issues, "parameters[" + i + "].variable", "VI_CONFIG_INVALID",
                        "缺少字段来源：" + (keyword == null ? keyword : keyword) + " 未映射到白名单变量");
            }
        }
        return issues;
    }

    // ------------------------------------------------------------ 摘要与脱敏

    /** 规范化摘要：键排序，避免同一内容因 JSON 顺序不同产生不同 hash */
    public static String contentHash(Object config, Map<String, Object> secretRefs)
    {
        Map<String, Object> canonical = new TreeMap<>();
        canonical.put("config", canonicalize(config));
        canonical.put("secrets", new TreeMap<>(secretRefs == null ? Map.of() : secretRefs));
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(JSON.toJSONString(canonical).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : out)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private static Object canonicalize(Object value)
    {
        if (value instanceof Map<?, ?> map)
        {
            Map<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> e : map.entrySet())
            {
                sorted.put(String.valueOf(e.getKey()), canonicalize(e.getValue()));
            }
            return sorted;
        }
        if (value instanceof List<?> list)
        {
            List<Object> out = new ArrayList<>();
            for (Object item : list)
            {
                out.add(canonicalize(item));
            }
            return out;
        }
        return value;
    }

    public static String maskPhone(String phone)
    {
        if (phone == null || phone.length() < 7)
        {
            return "—";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskReference(String ref)
    {
        if (ref == null || ref.isBlank())
        {
            return "—";
        }
        if (ref.length() <= 4)
        {
            return "****";
        }
        return "****" + ref.substring(ref.length() - 4);
    }

    public static boolean maskedValue(String value)
    {
        return value != null && MASK.matcher(value.trim()).matches();
    }

    // ------------------------------------------------------------ issue 构造

    private static String require(Map<String, Object> c, String field, int max, List<Map<String, Object>> issues,
            String message)
    {
        String v = str(c, field);
        if (v == null || v.isBlank())
        {
            error(issues, field, "VI_CONFIG_INVALID", message);
            return null;
        }
        if (v.length() > max)
        {
            error(issues, field, "VI_CONFIG_INVALID", message + "（长度不能超过 " + max + " 字）");
            return null;
        }
        return v;
    }

    public static Map<String, Object> issue(String field, String code, String message, String severity,
            String recovery)
    {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("field", field);
        issue.put("code", code);
        issue.put("message", message);
        issue.put("severity", severity);
        issue.put("recovery", recovery);
        return issue;
    }

    public static void error(List<Map<String, Object>> issues, String field, String code, String message)
    {
        issues.add(issue(field, code, message, "ERROR", recoveryOf(field)));
    }

    public static void warn(List<Map<String, Object>> issues, String field, String code, String message)
    {
        issues.add(issue(field, code, message, "WARNING", recoveryOf(field)));
    }

    public static void info(List<Map<String, Object>> issues, String field, String code, String message)
    {
        issues.add(issue(field, code, message, "WARNING", recoveryOf(field)));
    }

    private static String recoveryOf(String field)
    {
        if (field == null)
        {
            return "按提示补齐后重新校验";
        }
        if (field.startsWith("credentials."))
        {
            return "在对应配置页重新输入凭据（输入框留空表示保持原值），再执行校验";
        }
        return "修改该字段后重新保存草稿并校验；旧校验结果会失效";
    }

    public static boolean hasError(List<Map<String, Object>> issues)
    {
        return issues.stream().anyMatch(i -> "ERROR".equals(i.get("severity")));
    }
}
