package com.xinyi.rongli.support;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 微信支付商户参数快照。
 *
 * <p>每次下单/退款/回调都重新从 {@link BizConfig} 构造，因此后台保存后下一次请求立即生效，
 * 不存在"改完要重启"或缓存窗口。敏感项（APIv3 密钥、商户私钥、平台证书）在库内以
 * {@code ENC:} 前缀的 AES-GCM 密文保存，本类负责在读取时还原为明文供签名使用。</p>
 */
public record PaySettings(
        String channel,
        String mode,
        String appId,
        String mchId,
        String apiV3Key,
        String serialNo,
        String privateKeyPem,
        String platformSerialNo,
        String platformCertPem,
        String notifyUrl,
        String refundNotifyUrl,
        String description,
        boolean verifyNotify)
{
    /** 微信小程序 AppID：wx + 16 位十六进制 */
    public static final Pattern APP_ID = Pattern.compile("^wx[0-9a-fA-F]{16}$");

    /** 商户号：8~12 位数字（历史商户号为 8 位，新商户号为 10 位） */
    public static final Pattern MCH_ID = Pattern.compile("^\\d{8,12}$");

    /** APIv3 密钥：固定 32 位字母数字 */
    public static final Pattern API_V3_KEY = Pattern.compile("^[A-Za-z0-9]{32}$");

    /** 证书序列号：十六进制，微信当前为 40 位，留出扩展区间 */
    public static final Pattern SERIAL_NO = Pattern.compile("^[0-9A-Fa-f]{16,64}$");

    public static final Pattern HTTPS_URL = Pattern.compile("^https://[^\\s]+$");

    /** 小程序调起支付的 appId 与商户号必须同主体，这里只校验格式，主体一致性由微信侧返回 */
    public boolean realChannel()
    {
        return "WECHAT".equalsIgnoreCase(channel);
    }

    /** 真实收款是否可用：通道为 WECHAT 且必填项齐全且私钥可解析 */
    public boolean ready()
    {
        return realChannel() && missingFields().isEmpty();
    }

    /**
     * 必填项缺失或格式非法清单。返回空表示可用于真实收款。
     *
     * <p>只做本地可判定的校验，不发起网络请求，因此可以在保存前与启动自检中反复调用。</p>
     */
    public List<String> missingFields()
    {
        List<String> missing = new ArrayList<>();
        if (!realChannel())
        {
            return missing;
        }
        if (blank(appId) || !APP_ID.matcher(appId.trim()).matches())
        {
            missing.add("小程序 AppID（wx 开头 18 位）");
        }
        if (blank(mchId) || !MCH_ID.matcher(mchId.trim()).matches())
        {
            missing.add("商户号（8~12 位数字）");
        }
        if (blank(apiV3Key) || !API_V3_KEY.matcher(apiV3Key.trim()).matches())
        {
            missing.add("APIv3 密钥（32 位字母数字）");
        }
        if (blank(serialNo) || !SERIAL_NO.matcher(serialNo.trim()).matches())
        {
            missing.add("商户证书序列号（十六进制）");
        }
        if (blank(privateKeyPem))
        {
            missing.add("商户私钥");
        }
        else if (privateKey() == null)
        {
            missing.add("商户私钥（内容不是合法的 PKCS#8 PEM）");
        }
        if (blank(notifyUrl) || !HTTPS_URL.matcher(notifyUrl.trim()).matches())
        {
            missing.add("支付回调地址（https）");
        }
        if (!blank(refundNotifyUrl) && !HTTPS_URL.matcher(refundNotifyUrl.trim()).matches())
        {
            missing.add("退款回调地址（https）");
        }
        if (!blank(platformCertPem) && publicKey() == null)
        {
            missing.add("微信支付平台证书（内容不是合法的 X.509 PEM）");
        }
        if (verifyNotify && publicKey() == null)
        {
            missing.add("微信支付平台证书（已开启回调验签，必须提供）");
        }
        if (blank(description) || description.trim().length() > 127)
        {
            missing.add("商品描述（1~127 字符）");
        }
        return missing;
    }

    /** 退款回调未单独配置时，沿用支付回调地址并把路径末段换成 /refund */
    public String effectiveRefundNotifyUrl()
    {
        if (!blank(refundNotifyUrl))
        {
            return refundNotifyUrl.trim();
        }
        if (blank(notifyUrl))
        {
            return null;
        }
        String base = notifyUrl.trim();
        int idx = base.lastIndexOf('/');
        return idx > "https://".length() ? base.substring(0, idx) + "/refund" : base + "/refund";
    }

    public String effectiveDescription()
    {
        return blank(description) ? "机动车检测代驾预约" : description.trim();
    }

    /** 解析商户私钥；内容非法返回 null，由调用方转成可读提示 */
    public PrivateKey privateKey()
    {
        return parsePrivateKey(privateKeyPem);
    }

    /** 解析微信支付平台证书公钥；未配置或非法返回 null */
    public PublicKey publicKey()
    {
        return parseCertificate(platformCertPem);
    }

    public static PrivateKey parsePrivateKey(String pem)
    {
        if (blank(pem))
        {
            return null;
        }
        try
        {
            String body = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(body);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static PublicKey parseCertificate(String pem)
    {
        if (blank(pem))
        {
            return null;
        }
        try
        {
            String normalized = pem.contains("-----BEGIN CERTIFICATE-----") ? pem
                    : "-----BEGIN CERTIFICATE-----\n" + pem.replaceAll("\\s", "") + "\n-----END CERTIFICATE-----\n";
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(
                    new java.io.ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8)));
            return cert.getPublicKey();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 用商户私钥做 SHA256withRSA 签名，输出 Base64；私钥缺失时返回 null */
    public String sign(String message)
    {
        PrivateKey key = privateKey();
        if (key == null)
        {
            return null;
        }
        try
        {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(key);
            signer.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signer.sign());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 用微信支付平台证书验签；证书缺失时返回 false，调用方据此决定是否放行 */
    public boolean verify(String message, String signatureBase64)
    {
        PublicKey key = publicKey();
        if (key == null || blank(signatureBase64))
        {
            return false;
        }
        try
        {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signatureBase64));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /** 回调地址必须是公网可达的 https，且不能指向本机 */
    public static String urlProblem(String url)
    {
        if (blank(url))
        {
            return "不能为空";
        }
        String text = url.trim();
        if (!HTTPS_URL.matcher(text).matches())
        {
            return "必须是 https 开头的完整地址";
        }
        try
        {
            String host = URI.create(text).getHost();
            if (host == null || host.isBlank())
            {
                return "缺少主机名";
            }
            if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "0.0.0.0".equals(host))
            {
                return "不能使用本机地址，微信服务器无法访问";
            }
        }
        catch (Exception e)
        {
            return "地址格式不合法";
        }
        return null;
    }

    private static boolean blank(String v)
    {
        return v == null || v.isBlank();
    }

    /**
     * 从配置中心加载。敏感项（APIv3 密钥、商户私钥、平台证书）支持 {@code ENC:} 密文，
     * 明文体（早期手工写入 sys_config 的值）也能正常读取，便于平滑迁移。
     */
    public static PaySettings load(BizConfig cfg, Crypto crypto)
    {
        return new PaySettings(
                cfg.get("rongli.payment.channel", "MOCK"),
                cfg.get("rongli.pay.mode", "NORMAL"),
                cfg.get("rongli.pay.appId", ""),
                cfg.get("rongli.pay.mchId", ""),
                secret(cfg, crypto, "rongli.pay.apiV3Key"),
                cfg.get("rongli.pay.serialNo", ""),
                secret(cfg, crypto, "rongli.pay.privateKey"),
                cfg.get("rongli.pay.platformSerialNo", ""),
                secret(cfg, crypto, "rongli.pay.platformCert"),
                cfg.get("rongli.pay.notifyUrl", ""),
                cfg.get("rongli.pay.refundNotifyUrl", ""),
                cfg.get("rongli.pay.description", "机动车检测代驾预约"),
                cfg.getBool("rongli.pay.verifyNotify", true));
    }

    /** 读取可能被加密的配置项：ENC: 前缀走解密，其余原样返回 */
    public static String secret(BizConfig cfg, Crypto crypto, String key)
    {
        String raw = cfg.get(key, "");
        if (raw.isEmpty() || !raw.startsWith("ENC:"))
        {
            return raw;
        }
        String plain = crypto.decrypt(Base64.getDecoder().decode(raw.substring(4)));
        return plain == null ? "" : plain;
    }
}
