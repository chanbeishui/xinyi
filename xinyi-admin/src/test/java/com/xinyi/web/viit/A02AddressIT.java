package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.rongli.platform.json.ViJson;
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
 * A02 地址簿与位置输入 真实集成测试（HTTP+MINI链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 覆盖：CRUD+版本返回、设常用并发唯一默认、创建即默认清旧、越权404、CAS冲突409、删除默认不残留、非法输入拒绝。
 * 临时地址不强制先保存属预约（B 系列）草稿语义，本任务仅管理已保存地址（list 空可用）。
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
class A02AddressIT {

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
        registry.add("vi.crypto.data-key", () -> "a02-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "a02-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("a02-sim-", ".sqlite");
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
        for (String t : List.of("vi_address", "vi_session_token", "vi_session", "vi_account_security", "vi_account")) {
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

    private JsonNode login(String persona) {
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code = (String) simPost("/control/codes", Map.of("runId", run, "persona", persona, "kind", "wechat")).get("code");
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", code), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private Map<String, Object> loc(double lng, double lat) {
        return Map.of("lng", lng, "lat", lat, "coordinateSystem", "GCJ02");
    }

    private JsonNode create(String token, String label, boolean isDefault) {
        ResponseEntity<String> r = rest.exchange(url("/api/app/owner/addresses"), HttpMethod.POST,
                mini(Map.of("label", label, "address", "工业大道12号", "location", loc(120.1, 30.2), "isDefault", isDefault), token), String.class);
        assertEquals(200, r.getStatusCode().value(), "创建应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private JsonNode list(String token) {
        return ViJson.tree(rest.exchange(url("/api/app/owner/addresses"), HttpMethod.GET, mini(null, token), String.class)
                .getBody()).get("data");
    }

    private long defaultCount(long accountId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM vi_address WHERE owner_account_id = ? AND is_default = 1 AND deleted_at IS NULL",
                Long.class, accountId);
    }

    // ---------- 用例 ----------

    @Test
    void CRUD与版本返回() {
        JsonNode s = login("a02-crud-" + System.nanoTime());
        String token = s.get("token").asText();
        JsonNode a = create(token, "家", false);
        assertEquals(0, a.get("version").asLong());
        assertFalse(a.get("isDefault").asBoolean());
        assertEquals("工业大道12号", a.get("address").asText());
        assertEquals(120.1, a.get("location").get("lng").asDouble(), 1e-6);
        String id = a.get("addressId").asText();
        assertEquals(1, list(token).size());
        ResponseEntity<String> up = rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.PUT,
                mini(Map.of("label", "公司", "address", "北环路26号", "location", loc(120.2, 30.3), "expectedVersion", 0), token), String.class);
        assertEquals(200, up.getStatusCode().value(), up.getBody());
        assertEquals(1, ViJson.tree(up.getBody()).get("data").get("version").asLong());
        ResponseEntity<String> del = rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.DELETE,
                mini(Map.of("expectedVersion", 1, "reason", "搬家"), token), String.class);
        assertEquals(200, del.getStatusCode().value(), del.getBody());
        assertTrue(ViJson.tree(del.getBody()).get("data").get("accepted").asBoolean());
        assertEquals(0, list(token).size());
    }

    @Test
    void 设常用并发不出现多个默认() throws Exception {
        JsonNode s = login("a02-conc-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        String idA = create(token, "A", false).get("addressId").asText();
        String idB = create(token, "B", false).get("addressId").asText();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Integer>> fs = new java.util.ArrayList<>();
        for (String id : List.of(idA, idB)) {
            fs.add(pool.submit(() -> {
                gate.await();
                long v = jdbc.queryForObject("SELECT version FROM vi_address WHERE id = ?", Long.class, Long.parseLong(id));
                return rest.exchange(url("/api/app/owner/addresses/" + id + "/default"), HttpMethod.POST,
                        mini(Map.of("expectedVersion", v, "reason", "常用"), token), String.class).getStatusCode().value();
            }));
        }
        gate.countDown();
        int c1 = fs.get(0).get(30, TimeUnit.SECONDS);
        int c2 = fs.get(1).get(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertTrue(c1 == 200 || c2 == 200, "至少一个设常用成功");
        assertEquals(1, defaultCount(accountId), "并发设常用仅一个默认");
    }

    @Test
    void 创建即默认清旧默认() {
        JsonNode s = login("a02-def-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        create(token, "A", true);
        assertEquals(1, defaultCount(accountId));
        create(token, "B", true);
        assertEquals(1, defaultCount(accountId), "新默认清旧默认");
        JsonNode l = list(token);
        assertTrue(l.get(0).get("isDefault").asBoolean(), "默认排首");
    }

    @Test
    void 越权他人地址404() {
        JsonNode s1 = login("a02-own-" + System.nanoTime());
        JsonNode s2 = login("a02-other-" + System.nanoTime());
        String id = create(s1.get("token").asText(), "A", false).get("addressId").asText();
        String t2 = s2.get("token").asText();
        assertEquals(404, rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.PUT,
                mini(Map.of("address", "x", "location", loc(1, 1), "expectedVersion", 0), t2), String.class).getStatusCode().value());
        assertEquals(404, rest.exchange(url("/api/app/owner/addresses/" + id + "/default"), HttpMethod.POST,
                mini(Map.of("expectedVersion", 0, "reason", "x"), t2), String.class).getStatusCode().value());
        assertEquals(404, rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.DELETE,
                mini(Map.of("expectedVersion", 0, "reason", "x"), t2), String.class).getStatusCode().value());
        assertEquals(0, list(t2).size());
    }

    @Test
    void CAS冲突409() {
        JsonNode s = login("a02-cas-" + System.nanoTime());
        String token = s.get("token").asText();
        String id = create(token, "A", false).get("addressId").asText();
        assertEquals(200, rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.PUT,
                mini(Map.of("address", "x", "location", loc(1, 1), "expectedVersion", 0), token), String.class).getStatusCode().value());
        assertEquals(409, rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.PUT,
                mini(Map.of("address", "y", "location", loc(1, 1), "expectedVersion", 0), token), String.class).getStatusCode().value());
    }

    @Test
    void 删除默认不残留() {
        JsonNode s = login("a02-deldef-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        String id = create(token, "A", true).get("addressId").asText();
        assertEquals(1, defaultCount(accountId));
        rest.exchange(url("/api/app/owner/addresses/" + id), HttpMethod.DELETE,
                mini(Map.of("expectedVersion", 0, "reason", "删"), token), String.class);
        assertEquals(0, defaultCount(accountId), "删除默认不残留");
    }

    @Test
    void 非法位置与未知字段拒绝() {
        JsonNode s = login("a02-bad-" + System.nanoTime());
        String token = s.get("token").asText();
        assertEquals(422, rest.exchange(url("/api/app/owner/addresses"), HttpMethod.POST,
                mini(Map.of("address", "x", "location", loc(1, 999)), token), String.class).getStatusCode().value());
        assertEquals(400, rest.exchange(url("/api/app/owner/addresses"), HttpMethod.POST,
                mini(Map.of("address", "x", "location", loc(1, 1), "ownerAccountId", "1"), token), String.class).getStatusCode().value());
        assertEquals(0, list(token).size(), "空列表可用（临时地址不强制先保存）");
    }
}