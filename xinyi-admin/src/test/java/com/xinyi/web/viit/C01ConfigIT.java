package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.framework.web.service.TokenService;
import com.xinyi.rongli.config.service.ConfigService;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.system.service.ISysUserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C01 字典/公开条款/参数配置 真实集成测试（HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 覆盖：公开配置匿名+白名单+缺协议不开放下单+不宣称可支付、ADMIN配置CAS+固定安全项拒绝、
 * 发布协议后开放下单、字典车型/检测类型、新车型无价格不能0元（价格守卫）、生产不采用测试主体。
 */
@SpringBootTest(classes = RuoYiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
                "vi.environment=integration",
                "vi.providers.mode=SIMULATOR",
                "vi.outbox-relay-enabled=false",
                "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class C01ConfigIT {

    private static Process simProcess;
    private static int simPort;
    private static String simSecret;
    private static Path simDb;
    private static final java.net.http.HttpClient SIM_HTTP = java.net.http.HttpClient.newHttpClient();

    static {
        try {
            startSimulator();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void providerProps(DynamicPropertyRegistry registry) {
        registry.add("vi.providers.base-url", () -> "http://127.0.0.1:" + simPort);
        registry.add("vi.providers.secret", () -> simSecret);
        registry.add("vi.crypto.data-key", () -> "c01-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "c01-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("c01-sim-", ".sqlite");
        Files.deleteIfExists(simDb);
        String python = System.getenv().getOrDefault("VI_PYTHON", "python3");
        Path sim = repoRoot().resolve("docs/vehicle/V6.4/13_真实集成测试/provider_simulator/simulator.py");
        ProcessBuilder pb = new ProcessBuilder(python, sim.toString(),
                "--host", "127.0.0.1", "--port", "0", "--db", simDb.toString());
        pb.environment().put("VI_SIM_SECRET", simSecret);
        pb.redirectErrorStream(true);
        simProcess = pb.start();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(simProcess.getInputStream(), StandardCharsets.UTF_8));
        String line = reader.readLine();
        if (line == null) {
            throw new IllegalStateException("模拟器无启动输出");
        }
        simPort = ViJson.tree(line).get("port").asInt();
    }

    private static Path repoRoot() {
        String env = System.getenv("VI_REPO_ROOT");
        if (env != null && !env.isBlank()) {
            return Path.of(env).toAbsolutePath().normalize();
        }
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path marker = Path.of("docs/vehicle/V6.4/13_真实集成测试/provider_simulator/simulator.py");
        return Files.exists(cwd.resolve(marker)) ? cwd : cwd.getParent();
    }

    @AfterAll
    static void stopSimulator() {
        if (simProcess != null) {
            simProcess.destroyForcibly();
        }
        try {
            if (simDb != null) {
                Files.deleteIfExists(simDb);
            }
        } catch (Exception ignored) {
        }
    }

    @LocalServerPort
    int port;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    TokenService tokenService;
    @Autowired
    ISysUserService userService;
    @Autowired
    ConfigService configService;

    private final RestTemplate rest = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory()) {{
        setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }};

    @BeforeEach
    void clean() {
        jdbc.execute("DELETE FROM sys_config WHERE config_key LIKE 'vi.%'");
        jdbc.execute("DELETE FROM sys_dict_data WHERE dict_type IN ('vi_vehicle_type','vi_inspection_type')");
        jdbc.execute("DELETE FROM vi_price_rule");
        jdbc.execute("DELETE FROM vi_session_token");
        jdbc.execute("DELETE FROM vi_session");
        jdbc.execute("DELETE FROM vi_account_security");
        jdbc.execute("DELETE FROM vi_account");
    }

    private Map<String, Object> simPost(String path, Map<String, Object> body) {
        try {
            String json = ViJson.write(body);
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(
                            java.net.URI.create("http://127.0.0.1:" + simPort + path))
                    .header("Content-Type", "application/json").header("X-Sim-Secret", simSecret)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
            java.net.http.HttpResponse<String> resp =
                    SIM_HTTP.send(req, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, resp.statusCode(), "模拟器 " + path + " 应200 " + resp.body());
            return ViJson.read(resp.body(), Map.class);
        } catch (java.io.IOException | InterruptedException e) {
            throw new IllegalStateException("模拟器调用失败 " + path, e);
        }
    }

    private String url(String p) {
        return "http://127.0.0.1:" + port + p;
    }

    private HttpEntity<Object> mini(Object body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("X-Client-Type", "owner");
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        return new HttpEntity<>(body, h);
    }

    private String adminToken() {
        SysUser admin = userService.selectUserByUserName("admin");
        Long av = jdbc.queryForObject("SELECT auth_version FROM sys_user WHERE user_id = ?", Long.class, admin.getUserId());
        LoginUser lu = new LoginUser(admin, Set.of("*:*:*"));
        lu.setUserId(admin.getUserId());
        lu.setAuthVersion(av);
        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.addHeader("User-Agent", "vi-it");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(req));
        try {
            return tokenService.createToken(lu);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    private HttpEntity<Object> admin(Object body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("Authorization", "Bearer " + token);
        h.set("X-Client-Type", "admin");
        h.set("Idempotency-Key", UUID.randomUUID().toString());
        return new HttpEntity<>(body, h);
    }

    private JsonNode publicConfig() {
        ResponseEntity<String> r = rest.exchange(url("/api/public/service-config"), HttpMethod.GET, mini(null, null), String.class);
        assertEquals(200, r.getStatusCode().value(), "公开配置应匿名200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private ResponseEntity<String> putConfig(String admin, List<Map<String, String>> items, long expectedVersion) {
        return rest.exchange(url("/api/admin/config"), HttpMethod.PUT,
                admin(Map.of("items", items, "expectedVersion", expectedVersion, "reason", "配置测试"), admin), String.class);
    }

    private String loginToken() {
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code = (String) simPost("/control/codes", Map.of("runId", run, "persona", "c01-" + System.nanoTime(), "kind", "wechat")).get("code");
        return ViJson.tree(rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", code), null), String.class).getBody()).get("data").get("token").asText();
    }

    // ---------- 用例 ----------

    @Test
    void 公开配置匿名白名单缺协议不开放下单不宣称可支付() {
        JsonNode pc = publicConfig();
        assertFalse(pc.get("bookingEnabled").asBoolean(), "缺已发布协议不开放下单");
        assertFalse(pc.get("availablePaymentMethods").toString().contains("WECHAT"), "第三方未配置不宣称可支付");
        assertFalse(pc.toString().contains("merchant"), "不泄商户密钥");
        assertTrue(pc.has("companyName") && pc.has("termsVersion") && pc.has("privacyVersion"));
    }

    @Test
    void ADMIN配置CAS与固定安全项拒绝() {
        String admin = adminToken();
        ResponseEntity<String> g = rest.exchange(url("/api/admin/config"), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, g.getStatusCode().value());
        long v0 = ViJson.tree(g.getBody()).get("data").get("version").asLong();
        assertEquals(200, putConfig(admin, List.of(Map.of("key", "vi.biz.company.name", "value", "荣利检测")), v0).getStatusCode().value());
        assertEquals(409, putConfig(admin, List.of(Map.of("key", "vi.biz.company.name", "value", "X")), v0).getStatusCode().value(), "旧版本CAS409");
        assertEquals(403, putConfig(admin, List.of(Map.of("key", "vi.biz.payment.cap.minutes", "value", "99")), v0 + 1).getStatusCode().value(), "固定安全项不可绕过");
        assertEquals(403, putConfig(admin, List.of(Map.of("key", "vi.internal.secret", "value", "x")), v0 + 1).getStatusCode().value(), "非白名单拒绝");
    }

    @Test
    void 发布协议后开放下单且公开配置反映() {
        String admin = adminToken();
        long v = ViJson.tree(rest.exchange(url("/api/admin/config"), HttpMethod.GET, admin(null, admin), String.class)
                .getBody()).get("data").get("version").asLong();
        assertEquals(200, putConfig(admin, List.of(
                Map.of("key", "vi.biz.terms.version", "value", "t1"),
                Map.of("key", "vi.biz.terms.text", "value", "服务条款"),
                Map.of("key", "vi.biz.privacy.version", "value", "p1"),
                Map.of("key", "vi.biz.privacy.text", "value", "隐私政策"),
                Map.of("key", "vi.biz.booking.enabled", "value", "true"),
                Map.of("key", "vi.biz.service.cities", "value", "[\"杭州\"]")), v).getStatusCode().value());
        JsonNode pc = publicConfig();
        assertTrue(pc.get("bookingEnabled").asBoolean(), "发布协议后开放下单");
        assertEquals("t1", pc.get("termsVersion").asText());
        assertTrue(pc.get("serviceCities").toString().contains("杭州"));
    }

    @Test
    void 字典车型与检测类型() {
        jdbc.update("INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status) VALUES (1, '小型车', 'SMALL_CAR', 'vi_vehicle_type', '0')");
        jdbc.update("INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status) VALUES (1, '年检', 'ANNUAL', 'vi_inspection_type', '0')");
        String token = loginToken();
        ResponseEntity<String> r = rest.exchange(url("/api/common/dict/vi_vehicle_type"), HttpMethod.GET, mini(null, token), String.class);
        assertEquals(200, r.getStatusCode().value());
        JsonNode body = ViJson.tree(r.getBody());
        JsonNode list = body.get("rows");
        assertEquals(1, body.get("total").asLong(), "契约 total 为全集计数");
        assertEquals(1, list.size());
        assertEquals("SMALL_CAR", list.get(0).get("code").asText());
        assertEquals("小型车", list.get(0).get("label").asText());
        assertEquals(404, rest.exchange(url("/api/common/dict/unknown_type"), HttpMethod.GET, mini(null, token), String.class).getStatusCode().value());
    }

    @Test
    void 新车型无价格不能0元下单() {
        // 字典新增车型但无已发布价格 → 价格守卫拒绝（防0元）
        jdbc.update("INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status) VALUES (1, '新车', 'NEW_TYPE', 'vi_vehicle_type', '0')");
        ViException ex = assertThrows(ViException.class, () -> configService.assertPriced("NEW_TYPE"));
        assertEquals("VI_SERVICE_UNAVAILABLE", ex.getErrorCode().name());
        // 有已发布价格 → 放行
        jdbc.update("INSERT INTO vi_price_rule (rule_key, revision, vehicle_type_code, inspection_type_code, status, config_json) "
                + "VALUES ('k1', 1, 'SMALL_CAR', 'ANNUAL', 'PUBLISHED', '{}')");
        configService.assertPriced("SMALL_CAR");
    }

    @Test
    void 生产不采用测试主体与金额() {
        // integration 环境默认不暴露 WECHAT（未配置真实支付），公开配置无测试商户/金额
        JsonNode pc = publicConfig();
        assertFalse(pc.get("availablePaymentMethods").toString().contains("WECHAT"));
        assertFalse(pc.toString().contains("SIM_"), "不暴露测试主体");
        assertFalse(pc.toString().contains("test"), "不暴露测试金额/主体");
    }
}