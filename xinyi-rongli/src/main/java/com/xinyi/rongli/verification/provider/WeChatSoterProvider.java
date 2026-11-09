package com.xinyi.rongli.verification.provider;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinyi.rongli.platform.json.ViJson;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 微信SOTER服务端可信验签；只确认设备已录入的生物认证，不声称实名、人证比对或活体检测。 */
@Component
public class WeChatSoterProvider implements VerificationProvider {
    private static final ObjectMapper STRICT_RESPONSE =
            new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final PublishedSoterSettings settings;
    private final String endpoint;
    private final OkHttpClient http =
            new OkHttpClient.Builder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(10))
                    .callTimeout(Duration.ofSeconds(15))
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .retryOnConnectionFailure(false)
                    .build();
    private final Map<String, Token> tokens = new ConcurrentHashMap<>();

    private record Token(String value, Instant until) {
        @Override
        public String toString() {
            return "Token[REDACTED]";
        }
    }

    public WeChatSoterProvider(
            PublishedSoterSettings settings,
            @Value("${vi.environment:production}") String environment,
            @Value("${vi.verification.wechat-endpoint-test-override:}") String override) {
        this.settings = settings;
        endpoint = transport(environment, override);
    }

    @Override
    public Configuration configuration(long owner) {
        return settings.current(owner);
    }

    @Override
    public VerifyResult verify(Configuration frozen, VerifyCommand command) {
        // 即使Token尚在进程内缓存，也重读原发布与秘密有效性，撤销凭据不能靠缓存继续验签。
        return settings.secret(
                frozen,
                secret -> {
                    try {
                        String token = token(frozen, secret);
                        HttpUrl url =
                                HttpUrl.get(endpoint + "/cgi-bin/soter/verify_signature")
                                        .newBuilder()
                                        .addQueryParameter("access_token", token)
                                        .build();
                        var result =
                                call(
                                        url,
                                        Map.of(
                                                "openid",
                                                command.openId(),
                                                "json_string",
                                                command.resultJSON(),
                                                "json_signature",
                                                command.resultJSONSignature()));
                        if (result.has("errcode")
                                && (!result.path("errcode").isIntegralNumber()
                                        || result.path("errcode").longValue() != 0)) {
                            long code = result.path("errcode").asLong();
                            if (code == 40001 || code == 40014 || code == 42001)
                                tokens.remove(cacheKey(frozen));
                            return unknown();
                        }
                        if (!result.path("is_ok").isBoolean()) return unknown();
                        boolean passed = result.get("is_ok").booleanValue();
                        Instant now = Instant.now();
                        // 只摘要无个人信息的确认事实。原JSON/设备标识/签名只在受控会话密文中保留。
                        String digest =
                                PublishedSoterSettings.digest(
                                        frozen.identityDigest() + ":" + passed + ":" + now);
                        return new VerifyResult(
                                passed ? ResultStatus.PASSED : ResultStatus.FAILED,
                                now,
                                passed ? null : "DEVICE_SIGNATURE_REJECTED",
                                digest);
                    } catch (IOException | RuntimeException uncertain) {
                        return unknown();
                    }
                });
    }

    /** 使用微信稳定凭据接口普通模式，重复获取不强刷旧token；缓存从不进入DB、响应或审计。 */
    private synchronized String token(Configuration config, byte[] secret) throws IOException {
        String key = cacheKey(config);
        Token cached = tokens.get(key);
        if (cached != null && cached.until().isAfter(Instant.now())) return cached.value();
        var result =
                call(
                        HttpUrl.get(endpoint + "/cgi-bin/stable_token"),
                        Map.of(
                                "grant_type",
                                "client_credential",
                                "appid",
                                config.appId(),
                                "secret",
                                new String(secret, StandardCharsets.UTF_8),
                                "force_refresh",
                                false));
        if (result.has("errcode")
                && (!result.path("errcode").isIntegralNumber()
                        || result.path("errcode").longValue() != 0))
            throw new IOException("WECHAT_TOKEN_UNAVAILABLE");
        String value = result.path("access_token").asText();
        if (!result.path("access_token").isTextual()
                || !result.path("expires_in").isIntegralNumber()
                || value.isBlank()
                || value.length() > 2048) throw new IOException("WECHAT_TOKEN_INVALID");
        long expires = result.path("expires_in").longValue();
        if (expires <= 0 || expires > 86400) throw new IOException("WECHAT_TOKEN_EXPIRY");
        if (tokens.size() > 1000)
            tokens.entrySet().removeIf(entry -> !entry.getValue().until().isAfter(Instant.now()));
        tokens.put(key, new Token(value, Instant.now().plusSeconds(Math.max(1, expires - 30))));
        return value;
    }

    private JsonNode call(HttpUrl url, Map<String, Object> body) throws IOException {
        var request =
                new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(ViJson.write(body), JSON))
                        .build();
        try (var response = http.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null)
                throw new IOException("WECHAT_STATUS");
            byte[] bytes = response.body().byteStream().readNBytes(65537);
            try {
                if (bytes.length > 65536) throw new IOException("WECHAT_SIZE");
                // 不能接受重复is_ok/errcode或尾随第二份JSON；可信端点的畸形回复也只能UNKNOWN。
                try (JsonParser parser = STRICT_RESPONSE.getFactory().createParser(bytes)) {
                    JsonNode result = STRICT_RESPONSE.readTree(parser);
                    if (result == null || !result.isObject() || parser.nextToken() != null)
                        throw new IOException("WECHAT_SHAPE");
                    return result;
                }
            } finally {
                Arrays.fill(bytes, (byte) 0);
            }
        }
    }

    private static String cacheKey(Configuration config) {
        return config.profileId() + ":" + config.revisionId() + ":" + config.identityDigest();
    }

    private static VerifyResult unknown() {
        return new VerifyResult(ResultStatus.UNKNOWN, null, "DEVICE_VERIFY_UNKNOWN", null);
    }

    private static String transport(String environment, String override) {
        if (override == null || override.isBlank()) return "https://api.weixin.qq.com";
        URI uri = URI.create(override);
        if (!"integration".equals(environment)
                || !"http".equals(uri.getScheme())
                || !("127.0.0.1".equals(uri.getHost()) || "localhost".equals(uri.getHost()))
                || uri.getPort() < 1
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null
                || !(uri.getPath().isEmpty() || "/".equals(uri.getPath())))
            throw new IllegalStateException("微信验签测试运输地址仅限明确integration回环环境");
        return override.endsWith("/") ? override.substring(0, override.length() - 1) : override;
    }
}
