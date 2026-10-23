package com.xinyi.rongli.support;

import java.util.ArrayList;
import java.util.List;

/**
 * 短信通道参数快照（G02）。
 *
 * <p>与 {@link PaySettings} 同样每次调用重新加载，后台保存后下一次发送立即使用新配置。
 * 两家供应商的凭据字段不同，本类把差异收敛在 {@link #missingFields()} 与
 * {@link #provider()} 上，发送实现只关心"用哪家 + 参数是什么"。</p>
 */
public record SmsSettings(
        boolean enabled,
        String provider,
        String signName,
        String templateCode,
        String accessKeyId,
        String accessKeySecret,
        String secretId,
        String secretKey,
        String sdkAppId,
        String region,
        int codeExpireSeconds,
        int resendSeconds,
        int dailyLimit)
{
    public static final String ALIYUN = "ALIYUN";

    public static final String TENCENT = "TENCENT";

    public boolean aliyun()
    {
        return ALIYUN.equalsIgnoreCase(provider);
    }

    public boolean tencent()
    {
        return TENCENT.equalsIgnoreCase(provider);
    }

    /** 真实下发的必填项清单；未启用真实通道时返回空（联调模式允许缺参数） */
    public List<String> missingFields()
    {
        List<String> missing = new ArrayList<>();
        if (!enabled)
        {
            return missing;
        }
        if (!aliyun() && !tencent())
        {
            missing.add("短信供应商（ALIYUN 或 TENCENT）");
            return missing;
        }
        if (blank(signName) || signName.trim().length() < 2 || signName.trim().length() > 20)
        {
            missing.add("短信签名（2~20 字符）");
        }
        if (blank(templateCode))
        {
            missing.add("短信模板编号");
        }
        if (aliyun())
        {
            if (blank(accessKeyId))
            {
                missing.add("阿里云 AccessKeyId");
            }
            if (blank(accessKeySecret))
            {
                missing.add("阿里云 AccessKeySecret");
            }
        }
        else
        {
            if (blank(secretId))
            {
                missing.add("腾讯云 SecretId");
            }
            if (blank(secretKey))
            {
                missing.add("腾讯云 SecretKey");
            }
            if (blank(sdkAppId) || !sdkAppId.trim().matches("^\\d{6,20}$"))
            {
                missing.add("腾讯云 SdkAppId（数字）");
            }
        }
        return missing;
    }

    /** 真实通道是否可用 */
    public boolean ready()
    {
        return enabled && missingFields().isEmpty();
    }

    public String effectiveRegion()
    {
        return blank(region) ? "ap-guangzhou" : region.trim();
    }

    public int effectiveExpire()
    {
        return Math.min(Math.max(codeExpireSeconds, 60), 1800);
    }

    public int effectiveResend()
    {
        return Math.min(Math.max(resendSeconds, 10), 600);
    }

    public int effectiveDailyLimit()
    {
        return Math.min(Math.max(dailyLimit, 1), 50);
    }

    /**
     * 从配置中心加载。敏感项（两家供应商的密钥）支持 {@code ENC:} 密文存储，
     * 明文体也能读，便于早期手工写入的配置平滑过渡。
     */
    public static SmsSettings load(BizConfig cfg, Crypto crypto)
    {
        return new SmsSettings(
                cfg.getBool("rongli.sms.enabled", false),
                cfg.get("rongli.sms.provider", ALIYUN),
                cfg.get("rongli.sms.signName", ""),
                cfg.get("rongli.sms.templateCode", ""),
                PaySettings.secret(cfg, crypto, "rongli.sms.accessKeyId"),
                PaySettings.secret(cfg, crypto, "rongli.sms.accessKeySecret"),
                PaySettings.secret(cfg, crypto, "rongli.sms.secretId"),
                PaySettings.secret(cfg, crypto, "rongli.sms.secretKey"),
                cfg.get("rongli.sms.sdkAppId", ""),
                cfg.get("rongli.sms.region", "ap-guangzhou"),
                cfg.getInt("rongli.sms.codeExpireSeconds", 300),
                cfg.getInt("rongli.sms.resendSeconds", 60),
                cfg.getInt("rongli.sms.dailyLimit", 10));
    }

    private static boolean blank(String v)
    {
        return v == null || v.isBlank();
    }
}
