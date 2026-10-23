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
import com.xinyi.rongli.support.WechatPayClient;

/**
 * 微信支付配置读写与校验（G01）。
 *
 * <p>必填项与格式规则集中在 {@link PaySettings#missingFields()}，本类只负责
 * "读出来 → 合并提交 → 校验 → 加密落库 → 清缓存"，因此配置页与真实下单
 * 使用同一套判定，不会出现"页面说已配好、下单仍报缺参数"。</p>
 *
 * <p>敏感项（APIv3 密钥、商户私钥、平台证书）在库内加密保存，接口只返回
 * 是否已配置与末 4 位掩码；提交 {@code __UNCHANGED__} 表示保持原值。</p>
 */
@Service
public class PayConfigService
{
    /** 前端用它表示"这一项不修改"，避免用户看不到原值时必须重填 */
    private static final String UNCHANGED = "__UNCHANGED__";

    @Autowired
    private ConfigStore store;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private WechatPayClient payClient;

    // ------------------------------------------------------------------ 读

    public Map<String, Object> view()
    {
        PaySettings s = PaySettings.load(config, crypto);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("channel", s.channel());
        view.put("mode", s.mode());
        view.put("appId", s.appId());
        view.put("mchId", s.mchId());
        view.put("serialNo", s.serialNo());
        view.put("apiV3KeyConfigured", !blank(s.apiV3Key()));
        view.put("apiV3KeyMasked", mask(s.apiV3Key()));
        view.put("privateKeyConfigured", !blank(s.privateKeyPem()));
        view.put("privateKeyMasked", mask(s.privateKeyPem()));
        view.put("platformSerialNo", s.platformSerialNo());
        view.put("platformCertConfigured", !blank(s.platformCertPem()));
        view.put("platformCertMasked", mask(s.platformCertPem()));
        view.put("notifyUrl", s.notifyUrl());
        view.put("refundNotifyUrl", s.refundNotifyUrl());
        view.put("effectiveRefundNotifyUrl", s.effectiveRefundNotifyUrl());
        view.put("description", s.description());
        view.put("verifyNotify", s.verifyNotify());

        List<String> missing = s.missingFields();
        view.put("missing", missing);
        view.put("ready", s.ready());
        view.put("status", statusOf(s, missing));
        view.put("statusText", statusTextOf(s, missing));
        view.put("fields", fieldMeta());
        view.put("hint", hintOf(s, missing));
        return view;
    }

    /** 字段元数据：让配置页的必填标记、格式说明与实际校验完全一致 */
    public List<Map<String, Object>> fieldMeta()
    {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(field("appId", "小程序 AppID", true, "text", "wx1234567890abcdef",
                "微信公众平台 → 开发管理 → 开发设置", "^wx[0-9a-fA-F]{16}$"));
        list.add(field("mchId", "商户号", true, "text", "1900000109",
                "微信支付商户平台 → 账户中心 → 商户信息", "^\\d{8,12}$"));
        list.add(field("apiV3Key", "APIv3 密钥", true, "secret", "32 位字母数字",
                "商户平台 → 账户中心 → API安全 → 设置 APIv3 密钥", "^[A-Za-z0-9]{32}$"));
        list.add(field("serialNo", "商户证书序列号", true, "text", "以十六进制表示的序列号",
                "商户平台 → API安全 → 申请 API 证书", "^[0-9A-Fa-f]{16,64}$"));
        list.add(field("privateKey", "商户私钥", true, "textarea", "apiclient_key.pem 的全部内容",
                "申请证书后下载的 apiclient_key.pem，含 BEGIN/END 行", null));
        list.add(field("platformSerialNo", "平台证书序列号", false, "text", "选填，用于回调验签",
                "商户平台 → API安全 → 微信支付公钥/平台证书", "^[0-9A-Fa-f]{16,64}$"));
        list.add(field("platformCert", "平台证书", false, "textarea", "选填，PEM 内容",
                "配置后才能强制校验回调来源；开启验签时必填", null));
        list.add(field("notifyUrl", "支付回调地址", true, "text", "https://your-domain/api/common/pay/notify",
                "必须公网可达的 https 地址，微信服务器会 POST 到此地址", null));
        list.add(field("refundNotifyUrl", "退款回调地址", false, "text", "留空时自动取支付回调地址并替换末段为 /refund",
                "退款结果异步通知地址", null));
        list.add(field("description", "商品描述", true, "text", "机动车检测代驾预约",
                "用户在支付弹窗看到的商品说明，1~127 字符", null));
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
        // 先在内存里合并出"保存后"的取值，校验通过才落库，避免半套配置写进库
        String channel = text(body.get("channel"), store.get("rongli.payment.channel", "MOCK"));
        String mode = text(body.get("mode"), store.get("rongli.pay.mode", "NORMAL"));
        if (!List.of("MOCK", "WECHAT").contains(channel.toUpperCase()))
        {
            throw ApiException.validation("支付通道只能是 MOCK 或 WECHAT");
        }
        if (!List.of("NORMAL", "SANDBOX").contains(mode.toUpperCase()))
        {
            throw ApiException.validation("配置模式只能是 NORMAL 或 SANDBOX");
        }
        channel = channel.toUpperCase();
        mode = mode.toUpperCase();

        String appId = text(body.get("appId"), store.get("rongli.pay.appId", ""));
        String mchId = text(body.get("mchId"), store.get("rongli.pay.mchId", ""));
        String serialNo = text(body.get("serialNo"), store.get("rongli.pay.serialNo", ""));
        String platformSerialNo = text(body.get("platformSerialNo"),
                store.get("rongli.pay.platformSerialNo", ""));
        String notifyUrl = text(body.get("notifyUrl"), store.get("rongli.pay.notifyUrl", ""));
        String refundNotifyUrl = text(body.get("refundNotifyUrl"), store.get("rongli.pay.refundNotifyUrl", ""));
        String description = text(body.get("description"), store.get("rongli.pay.description", "机动车检测代驾预约"));
        boolean verifyNotify = body.get("verifyNotify") == null
                ? config.getBool("rongli.pay.verifyNotify", true)
                : Boolean.TRUE.equals(body.get("verifyNotify")) || "true".equals(String.valueOf(body.get("verifyNotify")));

        String apiV3Key = resolveSecret(body.get("apiV3Key"), "rongli.pay.apiV3Key");
        String privateKey = resolveSecret(body.get("privateKey"), "rongli.pay.privateKey");
        String platformCert = resolveSecret(body.get("platformCert"), "rongli.pay.platformCert");

        PaySettings candidate = new PaySettings(channel, mode, appId, mchId, apiV3Key, serialNo, privateKey,
                platformSerialNo, platformCert, notifyUrl, refundNotifyUrl, description, verifyNotify);
        validate(candidate);

        store.put("rongli.payment.channel", channel);
        store.put("rongli.pay.mode", mode);
        store.put("rongli.pay.appId", appId.trim());
        store.put("rongli.pay.mchId", mchId.trim());
        store.put("rongli.pay.serialNo", serialNo.trim());
        store.put("rongli.pay.platformSerialNo", platformSerialNo.trim());
        store.put("rongli.pay.notifyUrl", notifyUrl.trim());
        store.put("rongli.pay.refundNotifyUrl", refundNotifyUrl.trim());
        store.put("rongli.pay.description", description.trim());
        store.put("rongli.pay.verifyNotify", String.valueOf(verifyNotify));
        store.putSecret("rongli.pay.apiV3Key", apiV3Key);
        store.putSecret("rongli.pay.privateKey", privateKey);
        store.putSecret("rongli.pay.platformCert", platformCert);

        Map<String, Object> result = new LinkedHashMap<>(view());
        result.put("saved", true);
        result.put("message", "已保存并即时生效");
        return result;
    }

    /** 保存前的校验：逐项给可执行提示，而不是笼统的"参数错误" */
    private void validate(PaySettings s)
    {
        if (!s.ready() && s.realChannel())
        {
            List<String> missing = s.missingFields();
            throw ApiException.validation("微信支付配置不完整：" + String.join("；", missing));
        }
        if (!blank(s.notifyUrl()))
        {
            String problem = PaySettings.urlProblem(s.notifyUrl());
            if (problem != null)
            {
                throw ApiException.validation("支付回调地址" + problem);
            }
        }
        if (!blank(s.refundNotifyUrl()))
        {
            String problem = PaySettings.urlProblem(s.refundNotifyUrl());
            if (problem != null)
            {
                throw ApiException.validation("退款回调地址" + problem);
            }
        }
        if (!blank(s.platformSerialNo()) && !PaySettings.SERIAL_NO.matcher(s.platformSerialNo().trim()).matches())
        {
            throw ApiException.validation("平台证书序列号必须是十六进制");
        }
        if (!blank(s.apiV3Key()) && !PaySettings.API_V3_KEY.matcher(s.apiV3Key().trim()).matches())
        {
            throw ApiException.validation("APIv3 密钥必须是 32 位字母数字");
        }
        // 必填与否取决于通道，但"填了就必须填对"与通道无关：
        // 否则联调期可以把明显错误的值存进去，切到真实通道时才炸在支付那一刻。
        if (!blank(s.appId()) && !PaySettings.APP_ID.matcher(s.appId().trim()).matches())
        {
            throw ApiException.validation("小程序 AppID 格式不正确（wx 开头 18 位）");
        }
        if (!blank(s.mchId()) && !PaySettings.MCH_ID.matcher(s.mchId().trim()).matches())
        {
            throw ApiException.validation("商户号格式不正确（8~12 位数字）");
        }
        if (!blank(s.serialNo()) && !PaySettings.SERIAL_NO.matcher(s.serialNo().trim()).matches())
        {
            throw ApiException.validation("商户证书序列号必须是十六进制");
        }
        // 只有真实通道才走回调验签；联调通道（MOCK）由服务端自行构造明文通知，
        // 此时强制要求平台证书会让联调环境连商品描述都存不下去。
        if (s.verifyNotify() && s.realChannel() && blank(s.platformCertPem()))
        {
            throw ApiException.validation("已开启回调验签，必须填写微信支付平台证书；若在联调环境可先关闭验签");
        }
    }

    /** 敏感项合并：__UNCHANGED__ 或未提交 → 保持原值；空串 → 清空 */
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

    // ------------------------------------------------------------------ 自检

    /** 本地签名链路自检：不发起网络请求，验证私钥可用与各项格式 */
    public Map<String, Object> selfCheck()
    {
        PaySettings s = PaySettings.load(config, crypto);
        Map<String, Object> result = new LinkedHashMap<>(payClient.selfCheck(s));
        result.put("channel", s.channel());
        result.put("mode", s.mode());
        result.put("status", statusOf(s, s.missingFields()));
        // 回调和私钥属于配置类问题，这里额外把"能否真实收款"的结论说清楚
        boolean canCharge = s.ready() && "NORMAL".equalsIgnoreCase(s.mode());
        result.put("canCharge", canCharge);
        if (!canCharge)
        {
            result.put("reason", s.realChannel()
                    ? ("配置模式为 " + s.mode() + "，不会发起真实下单")
                    : "当前为联调支付通道（MOCK），未启用真实收款");
        }
        return result;
    }

    /** 供下单链路使用的配置快照 */
    public PaySettings settings()
    {
        return PaySettings.load(config, crypto);
    }

    // ------------------------------------------------------------------ 工具

    private String statusOf(PaySettings s, List<String> missing)
    {
        if (!s.realChannel())
        {
            return "MOCK";
        }
        if (s.privateKeyPem() != null && !s.privateKeyPem().isBlank() && s.privateKey() == null)
        {
            return "INVALID";
        }
        return missing.isEmpty() ? "READY" : "INCOMPLETE";
    }

    private String statusTextOf(PaySettings s, List<String> missing)
    {
        return switch (statusOf(s, missing))
        {
            case "MOCK" -> "联调通道：仅内部验证，不产生真实收款";
            case "READY" -> "已配置完成，可以发起真实支付";
            case "INVALID" -> "商户私钥无法解析，请重新粘贴 apiclient_key.pem 内容";
            default -> "还缺少 " + missing.size() + " 项必填配置";
        };
    }

    private String hintOf(PaySettings s, List<String> missing)
    {
        if (!s.realChannel())
        {
            return "当前为联调支付通道。切换为 WECHAT 并补全商户参数后，小程序下单将直接调起微信支付。";
        }
        if (!missing.isEmpty())
        {
            return "以下必填项缺失或格式不正确：" + String.join("、", missing);
        }
        if ("SANDBOX".equalsIgnoreCase(s.mode()))
        {
            return "配置已齐全，但当前为沙箱模式，只用于验证签名链路，不会真实下单。";
        }
        return "配置已齐全。保存后立即生效，无需重启服务。";
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
        String text = String.valueOf(submitted).trim();
        return text.isEmpty() ? "" : text;
    }

    /** 只露末 4 位，长度不足时整体打码 */
    private String mask(String value)
    {
        if (blank(value))
        {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4)
        {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }
}
