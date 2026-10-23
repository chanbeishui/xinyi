package com.xinyi.rongli.identity.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.identity.config.ViIdentityProperties;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * SIMULATOR 身份 provider：调用隔离测试模拟器 POST {base-url}/wechat/exchange 换取身份。
 * 仅当 vi.providers.mode=SIMULATOR 时装配（生产默认 WECHAT，绝不缺省走模拟器）。
 * 这是显式声明的测试替身：不代表真实微信 SDK 可用，真实渠道在 L01 门禁另行验证。
 * 绝不记录 code/secret/响应体；外部调用不在数据库锁内。
 */
@Component
@ConditionalOnProperty(name = "vi.providers.mode", havingValue = "SIMULATOR")
public class SimulatorIdentityProvider implements IdentityProvider {

    private final ViIdentityProperties props;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public SimulatorIdentityProvider(ViIdentityProperties props) {
        this.props = props;
    }

    @Override
    public WeChatIdentity exchangeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少微信登录 code");
        }
        String base = props.getProvidersBaseUrl();
        if (base.isBlank()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "SIMULATOR 未配置 vi.providers.base-url");
        }
        String body = ViJson.write(Map.of("code", code));
        HttpRequest req = HttpRequest.newBuilder(URI.create(base.replaceAll("/+$", "") + "/wechat/exchange"))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("X-Sim-Secret", props.getProvidersSecret())
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp;
        try {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "身份模拟器不可达", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "身份换取被中断", e);
        }
        int sc = resp.statusCode();
        if (sc == 403) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "身份模拟器密钥不匹配");
        }
        if (sc == 422 || sc == 409) {
            // code 无效/过期/已消费：无法认证为有效登录，按 401 处理，不泄露模拟器内部细节
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED, "登录凭证无效或已失效，请重新登录");
        }
        if (sc != 200) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "身份模拟器返回异常状态");
        }
        JsonNode node = ViJson.tree(resp.body());
        String appId = text(node, "appId");
        String openId = text(node, "openId");
        if (appId == null || openId == null) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "身份模拟器响应缺少 appId/openId");
        }
        return new WeChatIdentity(appId, openId, text(node, "unionId"));
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
