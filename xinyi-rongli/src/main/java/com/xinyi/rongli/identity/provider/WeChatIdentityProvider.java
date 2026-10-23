package com.xinyi.rongli.identity.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 真实微信身份 provider（生产默认；vi.providers.mode 缺省即 WECHAT，绝不缺省走 SIMULATOR）。
 * 调用 jscode2session 以临时 code 换取 openid/unionid。appId/appSecret 来自部署环境/KMS
 * （vi.providers.wechat.*），绝不硬编码、绝不进 Git/日志。
 *
 * 真实渠道联调属 L01 发布门禁：缺凭据时 fail-closed 抛 VI_CAPABILITY_NOT_CONFIGURED，
 * 不以模拟器替代、不宣称真实微信认证已通过。
 */
@Component
@ConditionalOnProperty(name = "vi.providers.mode", havingValue = "WECHAT", matchIfMissing = true)
public class WeChatIdentityProvider implements IdentityProvider {

    private static final String JSCODE2SESSION =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private final String appId;
    private final String appSecret;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public WeChatIdentityProvider(
            @Value("${vi.providers.wechat.app-id:}") String appId,
            @Value("${vi.providers.wechat.app-secret:}") String appSecret) {
        this.appId = appId == null ? "" : appId.trim();
        this.appSecret = appSecret == null ? "" : appSecret;
    }

    @Override
    public WeChatIdentity exchangeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少微信登录 code");
        }
        if (appId.isBlank() || appSecret.isBlank()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED,
                    "微信真实渠道未配置凭据（vi.providers.wechat.app-id/app-secret）；属 L01 发布门禁");
        }
        String url = String.format(JSCODE2SESSION,
                enc(appId), enc(appSecret), enc(code));
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8)).GET().build();
        HttpResponse<String> resp;
        try {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "微信身份服务不可达", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "微信身份换取被中断", e);
        }
        if (resp.statusCode() != 200) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "微信身份服务返回异常状态");
        }
        JsonNode node = ViJson.tree(resp.body());
        JsonNode errcode = node.get("errcode");
        if (errcode != null && errcode.asInt(0) != 0) {
            // 40029 invalid code / 45011 rate limit 等：无效或过期 code 按 401，其余按上游不可用
            int ec = errcode.asInt();
            if (ec == 40029 || ec == 40163 || ec == 41008) {
                throw new ViException(ViErrorCode.VI_AUTH_REQUIRED, "登录凭证无效或已失效，请重新登录");
            }
            if (ec == 45011) {
                throw ViException.rateLimited(10);
            }
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "微信身份换取失败");
        }
        String openId = text(node, "openid");
        if (openId == null) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "微信响应缺少 openid");
        }
        // appId 以服务端配置为准，不信任响应回显
        return new WeChatIdentity(appId, openId, text(node, "unionid"));
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
