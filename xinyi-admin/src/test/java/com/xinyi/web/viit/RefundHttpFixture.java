package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.platform.json.ViJson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/** 独立退款回环渠道：真实验证官方SDK请求签名并签署返回，不修改其他支付套件的共享端口。 */
abstract class RefundHttpFixture extends PaymentHttpFixture {
    static final HttpServer REFUND_SERVER = startRefundServer();
    static final AtomicInteger refundPosts = new AtomicInteger(),
            refundQueries = new AtomicInteger(),
            paymentQueries = new AtomicInteger();
    static final Queue<String> postedBodies = new ConcurrentLinkedQueue<>(),
            signedSerials = new ConcurrentLinkedQueue<>();
    static volatile String expectedRefund,
            expectedTrade,
            postState,
            refundState,
            paymentState,
            providerFailure;
    static volatile long expectedAmount;
    static volatile boolean refundBadSignature,
            wrongAmount,
            wrongTotal,
            wrongTrade,
            wrongNo,
            wrongChannel,
            wrongMerchant,
            queryMissing,
            queryError,
            lostResponse,
            holdResponse;
    static volatile boolean missingUnsigned, missingBadSignature, holdRefundOnly;
    static volatile CountDownLatch requestStarted = new CountDownLatch(1),
            releaseResponse = new CountDownLatch(1);
    static volatile PublicKey requestPublicKey;

    static HttpServer startRefundServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(
                    Executors.newCachedThreadPool(
                            runnable -> {
                                Thread thread = new Thread(runnable, "p04-channel-fixture");
                                thread.setDaemon(true);
                                return thread;
                            }));
            server.createContext("/v3/", RefundHttpFixture::channel);
            server.start();
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> server.stop(0), "p04-fixture-stop"));
            return server;
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void paymentProperties(DynamicPropertyRegistry properties) {
        properties.add(
                "vi.payment.test-endpoint",
                () -> "http://127.0.0.1:" + REFUND_SERVER.getAddress().getPort());
        properties.add("vi.jobs.refund-enabled", () -> "true");
        properties.add("vi.jobs.refund-delay-ms", () -> "3600000");
    }

    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    static class EndpointIsolation {
        @org.springframework.context.annotation.Bean
        static org.springframework.beans.factory.config.BeanFactoryPostProcessor p04PrivateEndpoint(
                org.springframework.core.env.ConfigurableEnvironment environment) {
            // DynamicPropertySource继承合并的遍历顺序不是端口隔离依据；仅此测试上下文在普通Bean实例化前固定本地端口。
            return factory ->
                    environment
                            .getPropertySources()
                            .addFirst(
                                    new org.springframework.core.env.MapPropertySource(
                                            "p04-private-loopback",
                                            Map.of(
                                                    "vi.payment.test-endpoint",
                                                    "http://127.0.0.1:"
                                                            + REFUND_SERVER.getAddress().getPort(),
                                                    "vi.jobs.refund-enabled",
                                                    "true",
                                                    "vi.jobs.refund-delay-ms",
                                                    "3600000")));
        }
    }

    static void resetChannel() {
        refundPosts.set(0);
        refundQueries.set(0);
        paymentQueries.set(0);
        postedBodies.clear();
        signedSerials.clear();
        postState = "PROCESSING";
        paymentState = "SUCCESS";
        missingUnsigned = false;
        missingBadSignature = false;
        holdRefundOnly = false;
        refundState = "SUCCESS";
        providerFailure = null;
        expectedRefund = null;
        expectedTrade = null;
        expectedAmount = 1000;
        refundBadSignature = false;
        wrongAmount = false;
        wrongTotal = false;
        wrongTrade = false;
        wrongNo = false;
        wrongChannel = false;
        wrongMerchant = false;
        queryMissing = false;
        queryError = false;
        lostResponse = false;
        holdResponse = false;
        requestStarted = new CountDownLatch(1);
        releaseResponse = new CountDownLatch(1);
        requestPublicKey = merchantKey.getPublic();
    }

    static void channel(HttpExchange exchange) {
        try {
            String body =
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var attrs = new HashMap<String, String>();
            var matcher =
                    java.util.regex.Pattern.compile("(\\w+)=\"([^\"]*)\"")
                            .matcher(exchange.getRequestHeaders().getFirst("Authorization"));
            while (matcher.find()) attrs.put(matcher.group(1), matcher.group(2));
            Signature verify = Signature.getInstance("SHA256withRSA");
            verify.initVerify(requestPublicKey);
            verify.update(
                    (exchange.getRequestMethod()
                                    + "\n"
                                    + exchange.getRequestURI()
                                    + "\n"
                                    + attrs.get("timestamp")
                                    + "\n"
                                    + attrs.get("nonce_str")
                                    + "\n"
                                    + body
                                    + "\n")
                            .getBytes(StandardCharsets.UTF_8));
            if (!verify.verify(Base64.getDecoder().decode(attrs.get("signature"))))
                throw new IllegalStateException("SDK request signature mismatch");
            if (!"1234567890".equals(attrs.get("mchid")))
                throw new IllegalStateException("SDK merchant identity mismatch");
            signedSerials.add(attrs.get("serial_no"));
            String path = exchange.getRequestURI().getPath();
            boolean refund = path.startsWith("/v3/refund/domestic/refunds");
            boolean post = "POST".equals(exchange.getRequestMethod());
            if (refund && post) {
                refundPosts.incrementAndGet();
                postedBodies.add(body);
            } else if (refund) refundQueries.incrementAndGet();
            else paymentQueries.incrementAndGet();
            if (!holdRefundOnly || refund) requestStarted.countDown();
            if (holdResponse
                    && (!holdRefundOnly || refund)
                    && !releaseResponse.await(8, TimeUnit.SECONDS))
                throw new IllegalStateException("fixture release timed out");
            if (lostResponse && refund) return;
            if (refund && !post && queryMissing) {
                signedResponse(
                        exchange,
                        404,
                        Map.of("code", "RESOURCE_NOT_EXISTS", "message", "隔离渠道无此退款"));
                return;
            }
            if (refund && !post && queryError) {
                signedResponse(
                        exchange, 503, Map.of("code", "SYSTEM_ERROR", "message", "隔离渠道暂不可用"));
                return;
            }
            Map<String, Object> result =
                    refund ? refundFact(post ? postState : refundState) : paymentFact();
            signedResponse(exchange, 200, result);
        } catch (Exception error) {
            providerFailure = error.getClass().getSimpleName();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {
                /* 断连接场景无需伪造成功响应。 */
            }
        } finally {
            exchange.close();
        }
    }

    static Map<String, Object> refundFact(String status) {
        var result = new LinkedHashMap<String, Object>();
        result.put("mchid", wrongMerchant ? "0000000000" : "1234567890");
        result.put("out_trade_no", wrongTrade ? "WRONG_TRADE" : expectedTrade);
        result.put("transaction_id", "TX_P04_" + expectedTrade);
        result.put("out_refund_no", wrongNo ? "WRONG_REFUND" : expectedRefund);
        result.put(
                "refund_id",
                wrongChannel ? "50300000000000000000000000009" : "50300000000000000000000000001");
        result.put("status", status);
        result.put("refund_status", status);
        result.put("channel", "ORIGINAL");
        result.put("user_received_account", "用户原支付账户");
        if ("SUCCESS".equals(status)) result.put("success_time", Instant.now().toString());
        result.put(
                "amount",
                Map.of(
                        "total",
                        wrongTotal ? 12346 : 12345,
                        "refund",
                        wrongAmount ? expectedAmount + 1 : expectedAmount,
                        "payer_total",
                        12345,
                        "payer_refund",
                        expectedAmount,
                        "currency",
                        "CNY"));
        return result;
    }

    static Map<String, Object> paymentFact() {
        return Map.of(
                "appid",
                "wx0000000000000000",
                "mchid",
                "1234567890",
                "out_trade_no",
                expectedTrade,
                "transaction_id",
                "TX_P04_" + expectedTrade,
                "trade_state",
                paymentState,
                "success_time",
                Instant.now().toString(),
                "amount",
                Map.of("total", 12345, "payer_total", 12345, "currency", "CNY"));
    }

    static void signedResponse(HttpExchange exchange, int status, Object value) throws Exception {
        String body = ViJson.write(value),
                at = Long.toString(Instant.now().getEpochSecond()),
                nonce = "p04-provider-nonce";
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(providerKey.getPrivate());
        sign.update((at + "\n" + nonce + "\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
        if (!(status == 404 && missingUnsigned)) {
            exchange.getResponseHeaders().set("Wechatpay-Timestamp", at);
            exchange.getResponseHeaders().set("Wechatpay-Nonce", nonce);
            exchange.getResponseHeaders().set("Wechatpay-Serial", "PUB_KEY_ID_TEST");
            exchange.getResponseHeaders()
                    .set(
                            "Wechatpay-Signature",
                            Base64.getEncoder()
                                    .encodeToString(
                                            (refundBadSignature
                                                                    && exchange.getRequestURI()
                                                                            .getPath()
                                                                            .contains("/refund/")
                                                            || status == 404 && missingBadSignature)
                                                    ? new byte[256]
                                                    : sign.sign()));
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    String encryptedEnvelope(Map<String, Object> fact, String eventId, String type)
            throws Exception {
        String nonce = "123456789012", aad = "refund";
        var cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(
                        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "AES"),
                new javax.crypto.spec.GCMParameterSpec(
                        128, nonce.getBytes(StandardCharsets.UTF_8)));
        cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        return ViJson.write(
                Map.of(
                        "id",
                        eventId,
                        "create_time",
                        Instant.now().toString(),
                        "event_type",
                        type,
                        "resource_type",
                        "encrypt-resource",
                        "resource",
                        Map.of(
                                "algorithm",
                                "AEAD_AES_256_GCM",
                                "nonce",
                                nonce,
                                "associated_data",
                                aad,
                                "ciphertext",
                                Base64.getEncoder()
                                        .encodeToString(
                                                cipher.doFinal(
                                                        ViJson.write(fact)
                                                                .getBytes(
                                                                        StandardCharsets
                                                                                .UTF_8))))));
    }

    ResponseEntity<String> refundNotify(Map<String, Object> fact, String eventId, boolean forged)
            throws Exception {
        String body = encryptedEnvelope(fact, eventId, "REFUND." + fact.get("refund_status"));
        return refundNotifyBody(body, forged);
    }

    ResponseEntity<String> refundNotifyBody(String body, boolean forged) throws Exception {
        String at = Long.toString(Instant.now().getEpochSecond()), nonce = "p04-notify";
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(providerKey.getPrivate());
        sign.update((at + "\n" + nonce + "\n" + body + "\n").getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Wechatpay-Serial", "PUB_KEY_ID_TEST");
        headers.set("Wechatpay-Timestamp", at);
        headers.set("Wechatpay-Nonce", nonce);
        headers.set(
                "Wechatpay-Signature",
                Base64.getEncoder().encodeToString(forged ? new byte[256] : sign.sign()));
        return http.exchange(
                "http://127.0.0.1:" + port + "/api/notify/wechat/refund",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);
    }
}
