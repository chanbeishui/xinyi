package com.xinyi.rongli.support;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * 短信发送客户端（G02）：支持阿里云与腾讯云两种签名体系，纯 JDK 实现。
 *
 * <p>阿里云走 RPC 风格 HMAC-SHA1 签名（SignatureVersion 1.0），
 * 腾讯云走 TC3-HMAC-SHA256 签名。两家都以供应商返回码判定成功，
 * 不使用 HTTP 200 作为成功依据（供应商业务失败也常返回 200）。</p>
 *
 * <p>本类不抛业务异常，统一返回 {@link Result}，由调用方决定是提示用户
 * 还是降级；只有网络层异常才抛 {@link IllegalStateException}。</p>
 */
@Component
public class SmsClient
{
    private static final Logger log = LoggerFactory.getLogger(SmsClient.class);

    private static final String ALIYUN_ENDPOINT = "https://dysmsapi.aliyuncs.com/";

    private static final String TENCENT_ENDPOINT = "https://sms.tencentcloudapi.com/";

    private static final DateTimeFormatter ALIYUN_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter TC_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final SecureRandom random = new SecureRandom();

    /** 发送结果：ok=false 时 errorCode/errorMessage 必须可用于排查 */
    public record Result(boolean ok, String provider, String requestId, String errorCode, String errorMessage)
    {
        public static Result ok(String provider, String requestId)
        {
            return new Result(true, provider, requestId, null, null);
        }

        public static Result fail(String provider, String code, String message)
        {
            return new Result(false, provider, null, code, message);
        }
    }

    /** 按供应商分发（验证码场景：单一 code 参数） */
    public Result send(SmsSettings settings, String phone, String code)
    {
        return sendTemplate(settings, phone, settings.templateCode(), Map.of("code", code));
    }

    /**
     * 按供应商分发（业务模板场景：任意参数集合）。
     *
     * <p>与验证码发送的差别只在参数形态：阿里云按参数名下发 JSON，
     * 腾讯云按 {@code TemplateParamSet} 的数组顺序对应模板里的 {1}{2}{3}，
     * 所以调用方必须保证 {@code params} 的迭代顺序与模板占位顺序一致。</p>
     */
    public Result sendTemplate(SmsSettings settings, String phone, String templateCode, Map<String, String> params)
    {
        if (settings.aliyun())
        {
            return sendAliyun(settings, phone, templateCode, params);
        }
        if (settings.tencent())
        {
            return sendTencent(settings, phone, templateCode, params);
        }
        return Result.fail(settings.provider(), "PROVIDER_UNKNOWN", "不支持的短信供应商");
    }

    // ------------------------------------------------------------------ 阿里云

    /**
     * 阿里云 SendSms。
     *
     * <p>签名串为 {@code GET&%2F&} + 按参数名排序并两次编码的查询串，
     * 因此 TemplateParam 里的 JSON 必须与其它参数一起参与排序和编码，
     * 否则会得到 SignatureDoesNotMatch。</p>
     */
    private Result sendAliyun(SmsSettings settings, String phone, String templateCode, Map<String, String> params)
    {
        Map<String, String> vars = new TreeMap<>();
        params.forEach((k, v) -> vars.put(k, v == null ? "" : v));

        Map<String, String> q = new TreeMap<>();
        q.put("AccessKeyId", settings.accessKeyId());
        q.put("Action", "SendSms");
        q.put("Format", "JSON");
        q.put("PhoneNumbers", phone);
        q.put("RegionId", "cn-hangzhou");
        q.put("SignName", settings.signName());
        q.put("SignatureMethod", "HMAC-SHA1");
        q.put("SignatureNonce", randomNonce());
        q.put("SignatureVersion", "1.0");
        q.put("TemplateCode", templateCode);
        q.put("TemplateParam", JSON.toJSONString(vars));
        q.put("Timestamp", ALIYUN_TS.format(Instant.now()));
        q.put("Version", "2017-05-25");

        StringBuilder canonical = new StringBuilder();
        for (Map.Entry<String, String> e : q.entrySet())
        {
            if (canonical.length() > 0)
            {
                canonical.append('&');
            }
            canonical.append(percentEncode(e.getKey())).append('=').append(percentEncode(e.getValue()));
        }
        String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(canonical.toString());
        String signature = hmacBase64(settings.accessKeySecret() + "&", stringToSign);

        StringBuilder url = new StringBuilder(ALIYUN_ENDPOINT).append('?');
        for (Map.Entry<String, String> e : q.entrySet())
        {
            url.append(percentEncode(e.getKey())).append('=').append(percentEncode(e.getValue())).append('&');
        }
        url.append("Signature=").append(percentEncode(signature));

        try
        {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url.toString()))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JSONObject json = JSON.parseObject(resp.body());
            String code2 = json.getString("Code");
            String requestId = json.getString("RequestId");
            if ("OK".equalsIgnoreCase(code2))
            {
                return Result.ok(SmsSettings.ALIYUN, requestId);
            }
            return Result.fail(SmsSettings.ALIYUN, code2, json.getString("Message"));
        }
        catch (Exception e)
        {
            log.warn("阿里云短信调用异常：{}", e.getMessage());
            throw new IllegalStateException("阿里云短信调用异常：" + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ 腾讯云

    /**
     * 腾讯云 SendSms（TC3-HMAC-SHA256）。
     *
     * <p>签名密钥由 SecretKey 逐级派生，日期取 UTC 当天；
     * 手机号必须带 +86 区号，否则返回 InvalidParameterValue.PhoneNumber。</p>
     */
    private Result sendTencent(SmsSettings settings, String phone, String templateCode, Map<String, String> params)
    {
        long timestamp = Instant.now().getEpochSecond();
        String date = TC_DATE.format(Instant.ofEpochSecond(timestamp));
        String payload = buildTencentPayload(settings, phone, templateCode, params);
        String hashedPayload = Crypto.hex(Crypto.sha256(payload));

        String canonicalRequest = "POST\n/\n\n"
                + "content-type:application/json; charset=utf-8\n"
                + "host:sms.tencentcloudapi.com\n"
                + "\n"
                + "content-type;host\n"
                + hashedPayload;
        String credentialScope = date + "/sms/tc3_request";
        String stringToSign = "TC3-HMAC-SHA256\n" + timestamp + "\n" + credentialScope + "\n"
                + Crypto.hex(Crypto.sha256(canonicalRequest));

        byte[] secretDate = hmac(("TC3" + settings.secretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac(secretDate, "sms");
        byte[] secretSigning = hmac(secretService, "tc3_request");
        String signature = Crypto.hex(hmac(secretSigning, stringToSign));

        String authorization = "TC3-HMAC-SHA256 Credential=" + settings.secretId() + "/" + credentialScope
                + ", SignedHeaders=content-type;host, Signature=" + signature;

        try
        {
            HttpRequest req = HttpRequest.newBuilder(URI.create(TENCENT_ENDPOINT))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Host", "sms.tencentcloudapi.com")
                    .header("X-TC-Action", "SendSms")
                    .header("X-TC-Version", "2021-01-11")
                    .header("X-TC-Timestamp", String.valueOf(timestamp))
                    .header("X-TC-Region", settings.effectiveRegion())
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JSONObject json = JSON.parseObject(resp.body());
            JSONObject response = json.getJSONObject("Response");
            if (response == null)
            {
                return Result.fail(SmsSettings.TENCENT, "BAD_RESPONSE", resp.body());
            }
            if (response.get("Error") instanceof JSONObject err)
            {
                return Result.fail(SmsSettings.TENCENT, err.getString("Code"), err.getString("Message"));
            }
            JSONArray statuses = response.getJSONArray("SendStatusSet");
            String requestId = response.getString("RequestId");
            if (statuses != null && !statuses.isEmpty())
            {
                JSONObject first = statuses.getJSONObject(0);
                String code2 = first.getString("Code");
                if ("Ok".equalsIgnoreCase(code2))
                {
                    return Result.ok(SmsSettings.TENCENT, requestId);
                }
                return Result.fail(SmsSettings.TENCENT, code2, first.getString("Message"));
            }
            return Result.ok(SmsSettings.TENCENT, requestId);
        }
        catch (Exception e)
        {
            log.warn("腾讯云短信调用异常：{}", e.getMessage());
            throw new IllegalStateException("腾讯云短信调用异常：" + e.getMessage(), e);
        }
    }

    private String buildTencentPayload(SmsSettings settings, String phone, String templateCode,
            Map<String, String> params)
    {
        String e164 = phone.startsWith("+") ? phone : "+86" + phone;
        // 腾讯云按数组顺序填充模板里的 {1}{2}{3}，顺序必须与参数绑定一致
        String[] values = params.values().stream().map(v -> v == null ? "" : v).toArray(String[]::new);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("PhoneNumberSet", new String[] { e164 });
        body.put("SmsSdkAppId", settings.sdkAppId());
        body.put("SignName", settings.signName());
        body.put("TemplateId", templateCode);
        body.put("TemplateParamSet", values);
        return JSON.toJSONString(body);
    }

    // ------------------------------------------------------------------ 签名工具

    /** 阿里云要求的编码：先标准 URL 编码，再把 + * %7E 换成 RFC3986 形式 */
    private static String percentEncode(String value)
    {
        if (value == null)
        {
            return "";
        }
        try
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        }
        catch (Exception e)
        {
            return value;
        }
    }

    private static String hmacBase64(String key, String message)
    {
        return Base64.getEncoder().encodeToString(hmac(key.getBytes(StandardCharsets.UTF_8), message));
    }

    private static byte[] hmac(byte[] key, String message)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("HMAC-SHA256 不可用", e);
        }
    }

    private String randomNonce()
    {
        byte[] buf = new byte[16];
        random.nextBytes(buf);
        StringBuilder sb = new StringBuilder();
        for (byte b : buf)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
