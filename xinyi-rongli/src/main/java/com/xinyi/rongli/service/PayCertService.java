package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.ConfigStore;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.PaySettings;
import com.xinyi.rongli.support.WechatPayClient;

/**
 * 微信支付平台证书维护。
 *
 * <p>回调验签依赖平台证书，而证书只能从商户平台手工下载且会轮换。手工粘贴有两个高频故障：
 * 粘错（验签失败、订单永远停在待支付）和忘记轮换（到期后所有回调被拒）。因此提供一个
 * "用现有商户凭据自动拉取并落库"的能力，把 APIv3 密钥的价值用足。</p>
 *
 * <p>只写 {@code rongli.pay.platformCert} 与 {@code rongli.pay.platformSerialNo} 两项，
 * 不动商户号/私钥/回调地址，避免误覆盖其它已核对过的配置。</p>
 */
@Service
public class PayCertService
{
    private static final Logger log = LoggerFactory.getLogger(PayCertService.class);

    private static final String KEY_PLATFORM_CERT = "rongli.pay.platformCert";

    private static final String KEY_PLATFORM_SERIAL = "rongli.pay.platformSerialNo";

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private ConfigStore store;

    @Autowired
    private WechatPayClient wechatPayClient;

    /**
     * 拉取平台证书。
     *
     * @param persist true 时把最新证书写回配置，回调验签立即使用新证书
     */
    public Map<String, Object> refresh(boolean persist)
    {
        PaySettings settings = PaySettings.load(config, crypto);
        List<String> missing = wechatPayClient.credentialMissing(settings);
        if (!missing.isEmpty())
        {
            throw ApiException.validation("拉取平台证书前请先补全：" + String.join("、", missing));
        }
        List<Map<String, Object>> certificates = wechatPayClient.fetchPlatformCertificates(settings);
        if (certificates.isEmpty())
        {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    com.xinyi.rongli.support.Codes.Error.CHANNEL_UNAVAILABLE,
                    "微信支付未返回任何平台证书，请确认商户号与 APIv3 密钥属于同一商户");
        }
        Map<String, Object> latest = certificates.get(0);
        String latestSerial = String.valueOf(latest.get("serialNo"));
        String latestPem = String.valueOf(latest.get("certificate"));

        boolean persisted = false;
        if (persist)
        {
            store.putSecret(KEY_PLATFORM_CERT, latestPem);
            store.put(KEY_PLATFORM_SERIAL, latestSerial);
            persisted = true;
            log.info("微信支付平台证书已更新 serialNo={} expireTime={}", latestSerial, latest.get("expireTime"));
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : certificates)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serialNo", item.get("serialNo"));
            row.put("effectiveTime", item.get("effectiveTime"));
            row.put("expireTime", item.get("expireTime"));
            row.put("algorithm", item.get("algorithm"));
            row.put("certificateBytes", String.valueOf(item.get("certificate")).length());
            row.put("selected", latestSerial.equals(item.get("serialNo")));
            rows.add(row);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fetched", rows.size());
        out.put("certificates", rows);
        out.put("selectedSerialNo", latestSerial);
        out.put("persisted", persisted);
        out.put(
                "message",
                persisted
                        ? "已写入平台证书与序列号，回调验签立即生效（无需重启）"
                        : "仅拉取未落库；如需启用验签，请以 persist=true 重新调用");
        out.put("nextStep", persisted
                ? "到支付配置页做一次「自检」，确认 platformCertParsed=true 且 verifyNotify 已开启"
                : "调用本接口时带 {\"persist\": true}");
        return out;
    }

    /** 只读预览：不落库，用于确认当前商户号能否取到证书 */
    public Map<String, Object> preview()
    {
        return refresh(false);
    }
}
