package com.xinyi.rongli.integration.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xinyi.rongli.common.support.ConfigStore;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;

/**
 * 生效投影：把集成档案「已发布版本」的值写进 {@code sys_config}。
 *
 * <p><b>为什么需要这一层。</b>第三方参数有两套存储：集成档案（{@code vi_integration_profile}，
 * 带草稿/校验/发布/回滚/审计）与框架 {@code sys_config}。支付签名、短信下发、小程序登录这些
 * 运行时路径一直是直接读 {@code sys_config} 的（{@code PaySettings} / {@code SmsSettings} /
 * {@code WechatMiniClient}）。在没有投影之前，两套各写各的，管理员必须在两个页面把同一组
 * 商户号、私钥、AK/SK 各填一遍，填漏一处就出现「配了不生效」的假故障。</p>
 *
 * <p><b>现在的约定（单一写入者）。</b>人工只在集成档案页面编辑；<b>发布</b>与<b>回滚</b>是
 * 唯一会把生效值写进 {@code sys_config} 的地方。{@code sys_config} 中的对应键降级为
 * 「当前生效快照」，不再有页面直接编辑它们，因此不会出现两份互相覆盖。</p>
 *
 * <p><b>不投影什么。</b>业务语义参数（派单时限、支付超时、短信频控、订阅跳转页）不属于第三方
 * 凭据，继续由业务参数维护；{@code rongli.payment.channel} 也不在此处切换，
 * 避免一次发布就把联调环境推到真实收款。</p>
 */
@Component
public class RuntimeProjection
{
    private static final Logger log = LoggerFactory.getLogger(RuntimeProjection.class);

    @Autowired
    private IntegrationMapper mapper;

    @Autowired
    private ConfigStore store;

    @Autowired
    private Crypto crypto;

    /**
     * 按档案类型投影。未知类型直接忽略，不影响发布结果。
     *
     * @param profile  档案行（需要 id / kind / environment）
     * @param revision 已发布或已回滚到的版本行
     */
    public void project(Row profile, Row revision)
    {
        if (profile == null || profile.isEmpty() || revision == null || revision.isEmpty())
        {
            return;
        }
        String kind = profile.str("kind");
        if (kind == null)
        {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> config = revision.str("configJson") == null ? Map.of()
                : com.alibaba.fastjson2.JSON.parseObject(revision.str("configJson"), Map.class);
        Map<String, String> secrets = secrets(profile, revision);
        try
        {
            switch (kind)
            {
                case "WECHAT_APP" -> projectWechatApp(config, secrets);
                case "WECHAT_PAY" -> projectWechatPay(config, secrets);
                case "TENCENT_SMS" -> projectSms("TENCENT", config, secrets);
                case "ALIYUN_SMS" -> projectSms("ALIYUN", config, secrets);
                default -> {
                    // PAYMENT_POLICY 由运行时直接读档案，不需要投影
                }
            }
            log.info("集成配置已投影到运行时配置 kind={} profile={} revision={}", kind,
                    profile.lng("id"), revision.lng("id"));
        }
        catch (RuntimeException e)
        {
            // 投影失败不能把已经成功的发布回滚掉：档案已是权威值，下次发布或重试会补齐
            log.error("集成配置投影失败 kind={} profile={}：{}", kind, profile.lng("id"), e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- 各类型投影

    private void projectWechatApp(Map<String, Object> config, Map<String, String> secrets)
    {
        String appId = IntegrationRules.str(config, "appId");
        if (notBlank(appId))
        {
            store.put("rongli.mini.appId", appId);
            store.put("rongli.pay.appId", appId);
        }
        String appSecret = secrets.get("appSecret");
        if (notBlank(appSecret))
        {
            store.putSecret("rongli.mini.appSecret", appSecret);
        }
    }

    /**
     * 商户参数投影。AppID 取自档案里引用的小程序公共凭据，保证「支付用的 AppID」
     * 与「登录用的小程序」是同一个，避免 OpenID 跨应用不可用。
     */
    private void projectWechatPay(Map<String, Object> config, Map<String, String> secrets)
    {
        String mchId = IntegrationRules.str(config, "mchid");
        if (notBlank(mchId))
        {
            store.put("rongli.pay.mchId", mchId);
        }
        String serial = IntegrationRules.str(config, "merchantCertificateSerial");
        if (notBlank(serial))
        {
            store.put("rongli.pay.serialNo", serial);
        }

        String appId = appIdOf(config);
        if (notBlank(appId))
        {
            store.put("rongli.pay.appId", appId);
        }

        String privateKey = secrets.get("merchantPrivateKey");
        if (notBlank(privateKey))
        {
            store.putSecret("rongli.pay.privateKey", privateKey);
        }
        String apiV3Key = secrets.get("apiV3Key");
        if (notBlank(apiV3Key))
        {
            store.putSecret("rongli.pay.apiV3Key", apiV3Key);
        }

        // 验签材料：公钥模式写微信支付公钥，平台证书模式写平台证书；两者都由 PaySettings 解析
        String verificationMode = IntegrationRules.str(config, "verificationMode");
        if ("WECHATPAY_PUBLIC_KEY".equals(verificationMode))
        {
            String publicKeyPem = IntegrationRules.str(config, "wechatPayPublicKeyPem");
            if (notBlank(publicKeyPem))
            {
                store.putSecret("rongli.pay.platformCert", publicKeyPem);
                store.put("rongli.pay.platformSerialNo",
                        nullToEmpty(IntegrationRules.str(config, "wechatPayPublicKeyId")));
            }
        }
        else if ("PLATFORM_CERT".equals(verificationMode))
        {
            // 平台证书模式：档案显式带了证书就投影第一条；留空表示由「通道自检」的拉取动作
            // 写入运行时快照，此时不能覆盖，否则一次发布就把刚拉到的证书清空。
            projectPlatformCertificate(config);
        }

        store.put("rongli.pay.verifyNotify", "true");

        // 回调地址：档案填了才覆盖，留空表示沿用现有值，避免发布把线上回调地址清空
        String notifyUrl = IntegrationRules.str(config, "paymentNotifyUrl");
        if (notBlank(notifyUrl))
        {
            store.put("rongli.pay.notifyUrl", notifyUrl.trim());
        }
        String refundNotifyUrl = IntegrationRules.str(config, "refundNotifyUrl");
        if (notBlank(refundNotifyUrl))
        {
            store.put("rongli.pay.refundNotifyUrl", refundNotifyUrl.trim());
        }
    }

    /**
     * 平台证书模式的证书投影。
     *
     * <p>档案里的 {@code platformCertificates} 是列表，元素形如
     * <code>{"serialNo":"...","certificate":"-----BEGIN CERTIFICATE-----..."}</code>，
     * 也兼容直接给 PEM 字符串。取第一条（微信下发的通常就是当前有效证书）。</p>
     *
     * <p>列表为空时什么都不做：平台证书由商户凭据向微信拉取，属于运行时事实而不是配置声明，
     * 此时保留「通道自检」写入的现值，避免一次发布把刚拉到的证书清空。</p>
     */
    @SuppressWarnings("unchecked")
    private void projectPlatformCertificate(Map<String, Object> config)
    {
        java.util.List<Object> certificates = IntegrationRules.list(config, "platformCertificates");
        if (certificates.isEmpty())
        {
            return;
        }
        Object first = certificates.get(0);
        String pem = null;
        String serial = null;
        if (first instanceof Map<?, ?> map)
        {
            Map<String, Object> item = (Map<String, Object>) map;
            pem = IntegrationRules.str(item, "certificate");
            serial = IntegrationRules.str(item, "serialNo");
        }
        else if (first != null)
        {
            pem = String.valueOf(first);
        }
        if (!notBlank(pem))
        {
            return;
        }
        store.putSecret("rongli.pay.platformCert", pem);
        if (notBlank(serial))
        {
            store.put("rongli.pay.platformSerialNo", serial);
        }
    }

    /**
     * 短信通道投影。
     *
     * <p>档案模型把腾讯云/阿里云拆成两种 kind，而 {@code SmsSettings} 只有一个 {@code provider}
     * 字段。这里的规则是「启用哪个就投影哪个」：后发布的启用档案覆盖前者，等价于老页面的
     * 供应商标单选。停用的档案只有在当前 provider 就是自己时才把 {@code enabled} 置 false，
     * 否则会把另一家的启用状态误关掉。</p>
     */
    private void projectSms(String provider, Map<String, Object> config, Map<String, String> secrets)
    {
        boolean enabled = IntegrationRules.bool(config, "enabled");
        String currentProvider = store.get("rongli.sms.provider", "ALIYUN");
        if (!enabled)
        {
            if (provider.equalsIgnoreCase(nullToEmpty(currentProvider)))
            {
                store.put("rongli.sms.enabled", "false");
            }
            return;
        }
        store.put("rongli.sms.enabled", "true");
        store.put("rongli.sms.provider", provider);

        String signName = IntegrationRules.str(config, "signName");
        if (notBlank(signName))
        {
            store.put("rongli.sms.signName", signName);
        }
        String region = IntegrationRules.str(config, "region");
        if (notBlank(region))
        {
            store.put("rongli.sms.region", region);
        }
        if ("TENCENT".equals(provider))
        {
            String secretId = IntegrationRules.str(config, "secretId");
            if (notBlank(secretId))
            {
                store.put("rongli.sms.secretId", secretId);
            }
            String sdkAppId = IntegrationRules.str(config, "smsSdkAppId");
            if (notBlank(sdkAppId))
            {
                store.put("rongli.sms.sdkAppId", sdkAppId);
            }
            String secretKey = secrets.get("secretKey");
            if (notBlank(secretKey))
            {
                store.putSecret("rongli.sms.secretKey", secretKey);
            }
        }
        else
        {
            String accessKeyId = IntegrationRules.str(config, "accessKeyId");
            if (notBlank(accessKeyId))
            {
                store.put("rongli.sms.accessKeyId", accessKeyId);
            }
            String accessKeySecret = secrets.get("accessKeySecret");
            if (notBlank(accessKeySecret))
            {
                store.putSecret("rongli.sms.accessKeySecret", accessKeySecret);
            }
        }
    }

    // ---------------------------------------------------------------- 辅助

    /** 档案里引用的 WECHAT_APP 版本的 appId；引用缺失返回 null，调用方保持原值 */
    @SuppressWarnings("unchecked")
    private String appIdOf(Map<String, Object> config)
    {
        Map<String, Object> ref = IntegrationRules.obj(config, "appProfile");
        Long revisionId = longOf(ref.get("revisionId"));
        if (revisionId == null)
        {
            return null;
        }
        Row revision = Row.of(mapper.findRevision(revisionId));
        if (revision.isEmpty() || revision.str("configJson") == null)
        {
            return null;
        }
        Map<String, Object> appConfig = com.alibaba.fastjson2.JSON.parseObject(revision.str("configJson"), Map.class);
        return IntegrationRules.str(appConfig, "appId");
    }

    /** 解封该版本引用到的凭据；单个槽失败只跳过该槽 */
    private Map<String, String> secrets(Row profile, Row revision)
    {
        Map<String, String> out = new LinkedHashMap<>();
        Set<String> slots = Json.read(revision.str("secretRefsJson")).keySet();
        for (String slot : slots)
        {
            Row secret = Row.of(mapper.findActiveSecret(profile.lng("id"), slot));
            if (secret.isEmpty() || secret.get("ciphertext") == null)
            {
                continue;
            }
            String plain = crypto.decrypt((byte[]) secret.get("ciphertext"));
            if (plain != null)
            {
                out.put(slot, plain);
            }
        }
        return out;
    }

    private static boolean notBlank(String v)
    {
        return v != null && !v.isBlank();
    }

    private static String nullToEmpty(String v)
    {
        return v == null ? "" : v;
    }

    private static Long longOf(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value).trim());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
