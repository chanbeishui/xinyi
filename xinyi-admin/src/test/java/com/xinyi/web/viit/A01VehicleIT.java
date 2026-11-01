package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.framework.web.service.TokenService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A01 车辆档案与PC车辆列表 真实集成测试（HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 覆盖：CRUD+版本+生成列不入DTO、同车主车牌冲突409+软删重建、伪造owner/猜车牌越权404、设常用并发单默认、
 * CAS409、ADMIN列表/详情脱敏/维护CAS/关联订单/结果、MINI不获ADMIN。
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
class A01VehicleIT {

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
        registry.add("vi.crypto.data-key", () -> "a01-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "a01-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("a01-sim-", ".sqlite");
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
        for (String t : List.of("vi_inspection_result", "vi_order", "vi_vehicle", "vi_session_token", "vi_session",
                "vi_account_security", "vi_account")) {
            jdbc.execute("DELETE FROM " + t);
        }
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
        h.set("Idempotency-Key", UUID.randomUUID().toString());
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
        return new HttpEntity<>(body, h);
    }

    private JsonNode login(String persona) {
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code = (String) simPost("/control/codes", Map.of("runId", run, "persona", persona, "kind", "wechat")).get("code");
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", code), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private JsonNode create(String token, String plate) {
        ResponseEntity<String> r = rest.exchange(url("/api/app/owner/vehicles"), HttpMethod.POST,
                mini(Map.of("plateNo", plate, "vehicleTypeCode", "SMALL_CAR", "brandModel", "品牌", "vinLast4", "1234"), token), String.class);
        assertEquals(200, r.getStatusCode().value(), "创建应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private JsonNode list(String token) {
        return ViJson.tree(rest.exchange(url("/api/app/owner/vehicles"), HttpMethod.GET, mini(null, token), String.class)
                .getBody()).get("data");
    }

    private long seedOrder(long vehicleId, long ownerAccountId) {
        jdbc.update("INSERT INTO vi_order (order_no, owner_account_id, station_id, dept_id, quote_id, vehicle_snapshot, "
                + "fulfillment_status, custody_type, custody_ref_id, pickup_starts_at, pickup_ends_at, pay_expires_at, total_fen, terms_version, vehicle_id) "
                + "VALUES (?, ?, 1, 100, 1, '{}', 'SCHEDULED', 'OWNER', 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 100, 'v1', ?)",
                "A01" + System.nanoTime(), ownerAccountId, vehicleId);
        return jdbc.queryForObject("SELECT id FROM vi_order WHERE vehicle_id = ? ORDER BY id DESC LIMIT 1", Long.class, vehicleId);
    }

    private void seedResult(long orderId) {
        jdbc.update("INSERT INTO vi_inspection_result (order_id, revision, conclusion, items_json, inspector_user_id, submitted_by, source, inspected_at, submitted_at) "
                + "VALUES (?, 1, 'PASS', '[]', 1, 1, 'STATION', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))", orderId);
    }

    // ---------- 用例 ----------

    @Test
    void CRUD版本与生成列不入DTO() {
        JsonNode s = login("a01-crud-" + System.nanoTime());
        String token = s.get("token").asText();
        JsonNode v = create(token, "浙A12345");
        assertEquals(0, v.get("version").asLong());
        assertFalse(v.has("activePlate"), "生成列不入DTO");
        assertFalse(v.has("ownerAccountId"), "owner不入MINI DTO");
        String id = v.get("vehicleId").asText();
        assertEquals(1, list(token).size());
        ResponseEntity<String> up = rest.exchange(url("/api/app/owner/vehicles/" + id), HttpMethod.PUT,
                mini(Map.of("plateNo", "浙A12345", "vehicleTypeCode", "SUV", "expectedVersion", 0), token), String.class);
        assertEquals(200, up.getStatusCode().value(), up.getBody());
        assertEquals(1, ViJson.tree(up.getBody()).get("data").get("version").asLong());
        ResponseEntity<String> del = rest.exchange(url("/api/app/owner/vehicles/" + id), HttpMethod.DELETE,
                mini(Map.of("expectedVersion", 1, "reason", "卖车"), token), String.class);
        assertEquals(200, del.getStatusCode().value());
        assertEquals(0, list(token).size());
    }

    @Test
    void 同车主车牌冲突409软删后重建() {
        JsonNode s = login("a01-plate-" + System.nanoTime());
        String token = s.get("token").asText();
        JsonNode v = create(token, "浙B11111");
        assertEquals(409, rest.exchange(url("/api/app/owner/vehicles"), HttpMethod.POST,
                mini(Map.of("plateNo", "浙B11111", "vehicleTypeCode", "SMALL_CAR"), token), String.class).getStatusCode().value());
        String id = v.get("vehicleId").asText();
        rest.exchange(url("/api/app/owner/vehicles/" + id), HttpMethod.DELETE,
                mini(Map.of("expectedVersion", 0, "reason", "删"), token), String.class);
        assertEquals(200, rest.exchange(url("/api/app/owner/vehicles"), HttpMethod.POST,
                mini(Map.of("plateNo", "浙B11111", "vehicleTypeCode", "SMALL_CAR"), token), String.class).getStatusCode().value(),
                "软删释放车牌可重建");
    }

    @Test
    void 伪造owner与猜车牌越权404() {
        JsonNode s1 = login("a01-own-" + System.nanoTime());
        JsonNode s2 = login("a01-other-" + System.nanoTime());
        String id = create(s1.get("token").asText(), "浙C22222").get("vehicleId").asText();
        String t2 = s2.get("token").asText();
        assertEquals(404, rest.exchange(url("/api/app/owner/vehicles/" + id), HttpMethod.PUT,
                mini(Map.of("plateNo", "浙C22222", "vehicleTypeCode", "SUV", "expectedVersion", 0), t2), String.class).getStatusCode().value());
        assertEquals(404, rest.exchange(url("/api/app/owner/vehicles/" + id + "/default"), HttpMethod.POST,
                mini(Map.of("expectedVersion", 0, "reason", "x"), t2), String.class).getStatusCode().value());
        // 不同车主同车牌允许（per-owner 唯一，不泄露他人）
        assertEquals(200, rest.exchange(url("/api/app/owner/vehicles"), HttpMethod.POST,
                mini(Map.of("plateNo", "浙C22222", "vehicleTypeCode", "SMALL_CAR"), t2), String.class).getStatusCode().value());
        assertEquals(1, list(t2).size());
    }

    @Test
    void 设常用并发单默认() throws Exception {
        JsonNode s = login("a01-conc-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        String idA = create(token, "浙D10001").get("vehicleId").asText();
        String idB = create(token, "浙D10002").get("vehicleId").asText();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Integer>> fs = new java.util.ArrayList<>();
        for (String id : List.of(idA, idB)) {
            fs.add(pool.submit(() -> {
                gate.await();
                long v = jdbc.queryForObject("SELECT version FROM vi_vehicle WHERE id = ?", Long.class, Long.parseLong(id));
                return rest.exchange(url("/api/app/owner/vehicles/" + id + "/default"), HttpMethod.POST,
                        mini(Map.of("expectedVersion", v, "reason", "常用"), token), String.class).getStatusCode().value();
            }));
        }
        gate.countDown();
        fs.get(0).get(30, TimeUnit.SECONDS);
        fs.get(1).get(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_vehicle WHERE owner_account_id = ? AND is_default = 1 AND deleted_at IS NULL",
                Long.class, accountId), "并发设常用仅一个默认");
    }

    @Test
    void CAS冲突409() {
        JsonNode s = login("a01-cas-" + System.nanoTime());
        String token = s.get("token").asText();
        String id = create(token, "浙E30001").get("vehicleId").asText();
        assertEquals(200, rest.exchange(url("/api/app/owner/vehicles/" + id), HttpMethod.PUT,
                mini(Map.of("plateNo", "浙E30001", "vehicleTypeCode", "SUV", "expectedVersion", 0), token), String.class).getStatusCode().value());
        assertEquals(409, rest.exchange(url("/api/app/owner/vehicles/" + id), HttpMethod.PUT,
                mini(Map.of("plateNo", "浙E30001", "vehicleTypeCode", "SUV", "expectedVersion", 0), token), String.class).getStatusCode().value());
    }

    @Test
    void ADMIN列表详情脱敏维护CAS与关联记录() {
        JsonNode s = login("a01-adm-" + System.nanoTime());
        long accountId = Long.parseLong(s.get("accountId").asText());
        JsonNode v = create(s.get("token").asText(), "浙F40001");
        long id = Long.parseLong(v.get("vehicleId").asText());
        long orderId = seedOrder(id, accountId);
        seedResult(orderId);
        String admin = adminToken();
        ResponseEntity<String> lr = rest.exchange(url("/api/admin/vehicles?pageNum=1&pageSize=50"), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, lr.getStatusCode().value());
        assertTrue(ViJson.tree(lr.getBody()).get("data").toString().contains("浙F40001"));
        ResponseEntity<String> dr = rest.exchange(url("/api/admin/vehicles/" + id), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, dr.getStatusCode().value());
        JsonNode d = ViJson.tree(dr.getBody()).get("data");
        assertEquals("****1234", d.get("vinMask").asText(), "VIN脱敏");
        assertTrue(d.has("ownerRelation"), "owner关系投影");
        assertEquals("PASS", d.get("lastResult").asText());
        ResponseEntity<String> mr = rest.exchange(url("/api/admin/vehicles/" + id), HttpMethod.PATCH,
                admin(Map.of("expectedVersion", d.get("version").asLong(), "brandModel", "新型号", "reason", "维护"), admin), String.class);
        assertEquals(200, mr.getStatusCode().value(), mr.getBody());
        assertEquals("新型号", ViJson.tree(mr.getBody()).get("data").get("brandModel").asText());
        assertEquals(409, rest.exchange(url("/api/admin/vehicles/" + id), HttpMethod.PATCH,
                admin(Map.of("expectedVersion", d.get("version").asLong(), "reason", "旧版本"), admin), String.class).getStatusCode().value());
        ResponseEntity<String> or = rest.exchange(url("/api/admin/vehicles/" + id + "/orders"), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(1, ViJson.tree(or.getBody()).get("data").size());
        ResponseEntity<String> rr = rest.exchange(url("/api/admin/vehicles/" + id + "/inspection-results"), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(1, ViJson.tree(rr.getBody()).get("data").size());
        assertEquals("PASS", ViJson.tree(rr.getBody()).get("data").get(0).get("conclusion").asText());
    }

    @Test
    void MINI令牌不获ADMIN() {
        JsonNode s = login("a01-noadm-" + System.nanoTime());
        ResponseEntity<String> r = rest.exchange(url("/api/admin/vehicles"), HttpMethod.GET, mini(null, s.get("token").asText()), String.class);
        assertTrue(r.getStatusCode().value() == 401 || ViJson.tree(r.getBody()).path("code").asInt() == 401, "MINI不获ADMIN");
    }
}