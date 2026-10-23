package com.xinyi.rongli.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONObject;

/**
 * 微信小程序服务端接口客户端。
 *
 * <p>当前只承担两件事：刷新接口调用凭证、用 {@code getPhoneNumber} 返回的 code 换取手机号。
 * 联调模式（{@code rongli.wechat.mock=true}）不访问微信，直接把 phoneCode 当手机号使用，
 * 便于在开发者工具与本机验证整条绑定链路。</p>
 */
@Component
public class WechatMiniClient
{
    private static final Logger log = LoggerFactory.getLogger(WechatMiniClient.class);

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    private static final String PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    private static final String SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Value("${rongli.wechat.mock:true}")
    private boolean mock;

    @Value("${rongli.wechat.appId:}")
    private String defaultAppId;

    @Value("${rongli.wechat.appSecret:}")
    private String appSecret;

    @Autowired
    private BizConfig bizConfig;

    @Autowired
    private Crypto crypto;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final Map<String, CachedToken> tokens = new ConcurrentHashMap<>();

    private record CachedToken(String value, Instant expireAt)
    {
    }

    public boolean isMock()
    {
        return mock;
    }

    /**
     * 生效的小程序 appId：优先取管理端「订阅消息/小程序凭据」里填的值，
     * 为空时回退到 application.yml，保证老部署不改配置也能跑。
     */
    public String effectiveAppId()
    {
        String fromConfig = bizConfig == null ? "" : bizConfig.get("rongli.mini.appId", "");
        return fromConfig.isBlank() ? defaultAppId : fromConfig.trim();
    }

    /** 生效的 appSecret：配置页保存的是 ENC: 密文，这里统一还原 */
    public String effectiveAppSecret()
    {
        String fromConfig = bizConfig == null ? "" : PaySettings.secret(bizConfig, crypto, "rongli.mini.appSecret");
        return fromConfig == null || fromConfig.isBlank() ? appSecret : fromConfig.trim();
    }

    /** 对外暴露接口调用凭证，供订阅消息复用，避免两处各自维护一份令牌缓存 */
    public String accessTokenOf(String appIdHint)
    {
        String appId = appIdHint == null || appIdHint.isBlank() ? effectiveAppId() : appIdHint.trim();
        return accessToken(appId, effectiveAppSecret());
    }

    /**
     * 用手机号授权 code 换真实手机号。
     *
     * @param appId     小程序 appId，为空时用配置的默认值
     * @param phoneCode 组件 {@code getPhoneNumber} 回调里的 code
     * @return 11 位手机号；code 为空时返回 null
     */
    public String resolvePhone(String appId, String phoneCode)
    {
        if (phoneCode == null || phoneCode.isBlank())
        {
            return null;
        }
        String code = phoneCode.trim();
        if (mock)
        {
            // 联调模式没有真实 code：约定直接传 11 位手机号，避免开发者工具无法验证绑定链路
            if (!code.matches("\\d{11}"))
            {
                throw ApiException.validation("联调模式下请在手机号授权位置填写 11 位手机号");
            }
            return code;
        }
        String secret = effectiveAppSecret();
        if (secret == null || secret.isBlank())
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "未配置小程序 AppSecret，无法校验手机号");
        }
        String actualAppId = appId == null || appId.isBlank() ? effectiveAppId() : appId;
        JSONObject resp = requestPhone(actualAppId, code);
        int errcode = resp.getIntValue("errcode", 0);
        if (errcode == 40001 || errcode == 42001)
        {
            // 令牌失效：清缓存后重试一次
            tokens.remove(actualAppId);
            resp = requestPhone(actualAppId, code);
            errcode = resp.getIntValue("errcode", 0);
        }
        if (errcode != 0)
        {
            log.warn("微信手机号解析失败 appId={} errcode={} errmsg={}", actualAppId, errcode,
                    resp.getString("errmsg"));
            throw ApiException.validation("手机号授权已失效，请重新授权");
        }
        JSONObject info = resp.getJSONObject("phone_info");
        String phone = info == null ? null : info.getString("purePhoneNumber");
        if (phone == null || !phone.matches("\\d{11}"))
        {
            throw ApiException.validation("未能获取到有效手机号，请重新授权");
        }
        return phone;
    }

    /**
     * 用 wx.login 的 code 换 openId。
     *
     * <p>联调模式把 code 当稳定标识（加 dev_ 前缀），避免真机每次登录都换 code 导致重复注册；
     * 真实模式调官方 {@code sns/jscode2session}。</p>
     */
    public WechatSession resolveOpenId(String appId, String code)
    {
        if (code == null || code.isBlank())
        {
            throw ApiException.validation("缺少微信登录 code");
        }
        if (mock)
        {
            String openId = code.startsWith("dev_") ? code : "dev_" + code;
            return new WechatSession(openId, null);
        }
        String secret = effectiveAppSecret();
        if (secret == null || secret.isBlank())
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "未配置小程序 AppSecret，无法完成微信登录");
        }
        String actualAppId = appId == null || appId.isBlank() ? effectiveAppId() : appId;
        String url = SESSION_URL + "?appid=" + actualAppId + "&secret=" + secret
                + "&js_code=" + code.trim() + "&grant_type=authorization_code";
        JSONObject resp = getJson(url);
        int errcode = resp.getIntValue("errcode", 0);
        if (errcode != 0)
        {
            log.warn("微信登录换取 openId 失败 appId={} errcode={} errmsg={}", actualAppId, errcode,
                    resp.getString("errmsg"));
            throw ApiException.unauthorized("微信登录凭证无效或已过期，请重新登录");
        }
        String openId = resp.getString("openid");
        if (openId == null || openId.isBlank())
        {
            throw ApiException.unauthorized("未能获取到微信身份，请重新登录");
        }
        return new WechatSession(openId, resp.getString("unionid"));
    }

    /** 微信身份解析结果 */
    public record WechatSession(String openId, String unionId)
    {
    }

    private JSONObject requestPhone(String appId, String code)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        String url = PHONE_URL + "?access_token=" + accessToken(appId, effectiveAppSecret());
        return postJson(url, JSONObject.toJSONString(body));
    }

    private String accessToken(String appId, String secret)
    {
        CachedToken cached = tokens.get(appId);
        if (cached != null && cached.expireAt().isAfter(Instant.now()))
        {
            return cached.value();
        }
        String url = TOKEN_URL + "?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        JSONObject resp = getJson(url);
        String token = resp.getString("access_token");
        if (token == null || token.isBlank())
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "获取微信接口令牌失败 errcode=" + resp.getIntValue("errcode", -1));
        }
        long expiresIn = Math.max(60, resp.getLongValue("expires_in", 7200L) - 300);
        tokens.put(appId, new CachedToken(token, Instant.now().plusSeconds(expiresIn)));
        return token;
    }

    private JSONObject getJson(String url)
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return JSONObject.parseObject(resp.body());
        }
        catch (Exception e)
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "调用微信接口异常：" + e.getMessage());
        }
    }

    private JSONObject postJson(String url, String body)
    {
        try
        {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return JSONObject.parseObject(resp.body());
        }
        catch (Exception e)
        {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, Codes.Error.CHANNEL_UNAVAILABLE,
                    "调用微信接口异常：" + e.getMessage());
        }
    }
}
