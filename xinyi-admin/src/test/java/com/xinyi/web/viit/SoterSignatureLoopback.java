package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.xinyi.rongli.platform.json.ViJson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 微信 SOTER 服务端协议的本地 HTTP 回环。测试密钥只模拟设备签名边界，不模拟真实微信或真实设备认证； 生产 Service、认证、材料与公司上下文、证明入账仍经过真实业务代码和数据库。
 */
final class SoterSignatureLoopback implements AutoCloseable {
    enum Mode {
        VERIFY_SIGNATURE,
        FALSE,
        ERROR,
        ERROR_WITH_TRUE,
        HTTP_503,
        DROP_RESPONSE,
        MALFORMED_JSON,
        MISSING_RESULT,
        STRING_TRUE,
        OVERSIZED_RESPONSE
    }

    record Call(String path, JsonNode body) {}

    private static final PSSParameterSpec PSS =
            new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 20, 1);

    private final HttpServer server;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Set<String>> appSecrets = new ConcurrentHashMap<>();
    private final Map<String, String> tokenApps = new ConcurrentHashMap<>();
    private final Map<String, String> openIdApps = new ConcurrentHashMap<>();
    private final Map<String, KeyPair> deviceKeys = new ConcurrentHashMap<>();
    private final List<Call> calls = new CopyOnWriteArrayList<>();
    final AtomicInteger tokenCalls = new AtomicInteger();
    final AtomicInteger verifyCalls = new AtomicInteger();
    final AtomicReference<Throwable> failure = new AtomicReference<>();
    volatile Mode mode = Mode.VERIFY_SIGNATURE;
    volatile CountDownLatch requestArrived;
    volatile CountDownLatch releaseResponse;

    SoterSignatureLoopback() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", this::handle);
            server.setExecutor(executor);
            server.start();
        } catch (IOException error) {
            executor.shutdownNow();
            throw new IllegalStateException(error);
        }
    }

    String endpoint() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    void addApp(String appId, String appSecret) {
        appSecrets.computeIfAbsent(appId, unused -> ConcurrentHashMap.newKeySet()).add(appSecret);
    }

    void addToken(String token, String appId) {
        tokenApps.put(token, appId);
    }

    void bindOpenId(String appId, String openId) {
        openIdApps.put(openId, appId);
    }

    List<Call> calls() {
        return List.copyOf(calls);
    }

    void reset() {
        calls.clear();
        tokenCalls.set(0);
        verifyCalls.set(0);
        failure.set(null);
        mode = Mode.VERIFY_SIGNATURE;
        requestArrived = null;
        releaseResponse = null;
    }

    String resultJson(String challenge) {
        return ViJson.write(
                Map.of(
                        "raw",
                        challenge,
                        "counter",
                        1,
                        "uid",
                        "q04-isolated-device-user",
                        "cpu_id",
                        "q04-isolated-device"));
    }

    String sign(String openId, String resultJson) {
        try {
            KeyPair pair = deviceKeys.computeIfAbsent(openId, unused -> newDeviceKey());
            Signature signer = Signature.getInstance("RSASSA-PSS");
            signer.setParameter(PSS);
            signer.initSign(pair.getPrivate());
            signer.update(resultJson.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static KeyPair newDeviceKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            JsonNode body = null;
            if ("POST".equals(exchange.getRequestMethod())) {
                body =
                        ViJson.tree(
                                new String(
                                        exchange.getRequestBody().readAllBytes(),
                                        StandardCharsets.UTF_8));
            }
            calls.add(new Call(path, body));
            switch (path) {
                case "/cgi-bin/token", "/cgi-bin/stable_token" -> token(exchange, path, body);
                case "/cgi-bin/soter/verify_signature" -> verify(exchange, body);
                default -> throw new AssertionError("Unexpected WeChat API path: " + path);
            }
        } catch (Throwable error) {
            failure.compareAndSet(null, error);
            respond(
                    exchange,
                    500,
                    "{\"errcode\":-1,\"errmsg\":\"isolated fixture rejected request\"}");
        } finally {
            exchange.close();
        }
    }

    private void token(HttpExchange exchange, String path, JsonNode body) throws IOException {
        tokenCalls.incrementAndGet();
        Map<String, String> parameters = query(exchange);
        if ("/cgi-bin/stable_token".equals(path)) {
            require("POST".equals(exchange.getRequestMethod()), "stable_token must use POST");
            require(
                    body.size() == 4
                            && body.path("force_refresh").isBoolean()
                            && !body.path("force_refresh").asBoolean(),
                    "Stable token must not force refresh");
            parameters = new HashMap<>();
            var fields = body.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                parameters.put(field.getKey(), field.getValue().asText());
            }
        } else {
            require("GET".equals(exchange.getRequestMethod()), "token must use GET");
        }
        String appId = parameters.get("appid");
        require("client_credential".equals(parameters.get("grant_type")), "Wrong token grant type");
        require(appId != null && appSecrets.containsKey(appId), "Unknown isolated app ID");
        require(
                appSecrets.get(appId).contains(parameters.get("secret")),
                "Wrong isolated app secret");
        String token = "q04-soter-access-" + UUID.randomUUID();
        tokenApps.put(token, appId);
        respond(exchange, 200, ViJson.write(Map.of("access_token", token, "expires_in", 7200)));
    }

    private void verify(HttpExchange exchange, JsonNode body) throws Exception {
        verifyCalls.incrementAndGet();
        require("POST".equals(exchange.getRequestMethod()), "verify_signature must use POST");
        require(body != null && body.isObject(), "Missing official JSON request");
        Set<String> fields = new java.util.HashSet<>();
        body.fieldNames().forEachRemaining(fields::add);
        require(
                fields.equals(Set.of("openid", "json_string", "json_signature")),
                "Unexpected verify fields");
        require(
                tokenApps.containsKey(query(exchange).get("access_token")),
                "Unknown or missing access token");
        require(body.path("openid").isTextual(), "openid must be a string");
        require(
                body.path("json_string").isTextual(),
                "json_string must remain the exact original string");
        require(body.path("json_signature").isTextual(), "json_signature must be a string");
        if (requestArrived != null) {
            requestArrived.countDown();
        }
        if (releaseResponse != null) {
            require(
                    releaseResponse.await(20, TimeUnit.SECONDS),
                    "Timed out waiting for test IO barrier");
        }
        switch (mode) {
            case HTTP_503 -> respond(exchange, 503, "{\"errcode\":-1}");
            case DROP_RESPONSE -> exchange.close();
            case MALFORMED_JSON -> respond(exchange, 200, "{not-json");
            case ERROR ->
                    respond(exchange, 200, "{\"errcode\":40001,\"errmsg\":\"invalid credential\"}");
            case ERROR_WITH_TRUE -> respond(exchange, 200, "{\"errcode\":40001,\"is_ok\":true}");
            case MISSING_RESULT -> respond(exchange, 200, "{\"errcode\":0,\"errmsg\":\"ok\"}");
            case STRING_TRUE -> respond(exchange, 200, "{\"is_ok\":\"true\"}");
            case OVERSIZED_RESPONSE ->
                    respond(
                            exchange,
                            200,
                            "{\"padding\":\"" + "x".repeat(262144) + "\",\"is_ok\":true}");
            case FALSE -> respond(exchange, 200, "{\"is_ok\":false}");
            case VERIFY_SIGNATURE ->
                    respond(
                            exchange,
                            200,
                            ViJson.write(
                                    Map.of(
                                            "is_ok",
                                            signatureValid(
                                                    body,
                                                    tokenApps.get(
                                                            query(exchange)
                                                                    .get("access_token"))))));
        }
    }

    private boolean signatureValid(JsonNode body, String tokenAppId) {
        try {
            KeyPair key = deviceKeys.get(body.path("openid").asText());
            if (key == null || !tokenAppId.equals(openIdApps.get(body.path("openid").asText()))) {
                return false;
            }
            Signature verifier = Signature.getInstance("RSASSA-PSS");
            verifier.setParameter(PSS);
            verifier.initVerify(key.getPublic());
            verifier.update(body.path("json_string").asText().getBytes(StandardCharsets.UTF_8));
            return verifier.verify(
                    Base64.getDecoder().decode(body.path("json_signature").asText()));
        } catch (Exception invalidSignature) {
            return false;
        }
    }

    private static Map<String, String> query(HttpExchange exchange) {
        Map<String, String> values = new HashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query != null) {
            for (String entry : query.split("&")) {
                String[] pair = entry.split("=", 2);
                values.put(
                        URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair.length == 1 ? "" : URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return values;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @Override
    public void close() {
        server.stop(0);
        executor.shutdownNow();
    }
}
