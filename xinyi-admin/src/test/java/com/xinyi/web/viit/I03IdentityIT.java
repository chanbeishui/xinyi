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
import java.util.ArrayList;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I03 刷新、退出和多设备会话 真实集成测试（HTTP+SpringSecurity(MINI链)+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 仅微信渠道为 SIMULATOR 替身（L01 另验真实渠道）。覆盖：
 * 刷新单次轮换/新令牌可用、重用已消费刷新撤销会话族且持久化（事务提交后报错）、并发刷新仅一条成功、
 * 会话绝对到期不滑动、当前会话只读无令牌且实时重算角色、退出当前会话保留车辆任务重登恢复（TC-R63-008）、
 * 全部设备退出递增 session_epoch 撤全部会话、退出幂等键校验、MINI 鉴权数据库权威即时拒绝（不依赖 Redis）。
 * 模拟器 openId=sim_{run}_{persona}：重登同一账号须复用同一 runId+persona。
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
class I03IdentityIT {

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
        registry.add("vi.crypto.data-key", () -> "i03-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "i03-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("i03-sim-", ".sqlite");
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
            // 临时库清理失败不影响结论
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
        for (String t : List.of("vi_driver_assignment", "vi_driver_profile", "vi_phone_verification_receipt",
                "vi_employee_binding", "vi_session_token", "vi_session", "vi_account_security", "vi_account",
                "vi_station_grant", "vi_owner_profile")) {
            jdbc.execute("DELETE FROM " + t);
        }
    }

    // ---------- 模拟器 ----------
    private Map<String, Object> simPost(String path, Map<String, Object> body) {
        try {
            String json = ViJson.write(body);
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(
                            java.net.URI.create("http://127.0.0.1:" + simPort + path))
                    .header("Content-Type", "application/json").header("X-Sim-Secret", simSecret)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
            java.net.http.HttpResponse<String> resp =
                    SIM_HTTP.send(req, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, resp.statusCode(), "模拟器 " + path + " 应200，实际" + resp.statusCode() + " " + resp.body());
            return ViJson.read(resp.body(), Map.class);
        } catch (java.io.IOException | InterruptedException e) {
            throw new IllegalStateException("模拟器调用失败 " + path, e);
        }
    }

    private String newRun() {
        return (String) simPost("/control/runs", Map.of()).get("runId");
    }

    private String newCode(String runId, String persona, String kind) {
        Map<String, Object> req = new HashMap<>();
        req.put("runId", runId);
        req.put("persona", persona);
        req.put("kind", kind);
        return (String) simPost("/control/codes", req).get("code");
    }

    // ---------- 应用 ----------
    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private HttpEntity<Object> entity(Object body, String token, String idem) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("X-Client-Type", "owner");
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        if (idem != null) {
            h.set("Idempotency-Key", idem);
        }
        return new HttpEntity<>(body, h);
    }

    /** 指定 run+persona 登录；同 run+persona 复登命中同一账号（openId=sim_run_persona），新建会话。 */
    private JsonNode login(String runId, String persona) {
        String code = newCode(runId, persona, "wechat");
        ResponseEntity<String> resp = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                entity(Map.of("code", code), null, null), String.class);
        assertEquals(200, resp.getStatusCode().value(), "登录应200 " + resp.getBody());
        return ViJson.tree(resp.getBody()).get("data");
    }

    private JsonNode login(String persona) {
        return login(newRun(), persona);
    }

    private ResponseEntity<String> refresh(String refreshToken, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        if (refreshToken != null) {
            body.put("refreshToken", refreshToken);
        }
        if (extra != null) {
            body.putAll(extra);
        }
        return rest.exchange(url("/api/common/token/refresh"), HttpMethod.POST, entity(body, null, null), String.class);
    }

    private ResponseEntity<String> session(String token) {
        return rest.exchange(url("/api/common/session"), HttpMethod.GET, entity(null, token, null), String.class);
    }

    private ResponseEntity<String> logout(String token, Boolean allDevices, String idem) {
        Map<String, Object> body = new HashMap<>();
        if (allDevices != null) {
            body.put("allDevices", allDevices);
        }
        return rest.exchange(url("/api/common/session/logout"), HttpMethod.POST, entity(body, token, idem), String.class);
    }

    private void seedDriverProfile(long accountId) {
        jdbc.update("INSERT INTO vi_driver_profile (account_id, qualification_status, license_long_term, "
                + "new_assignment_enabled, listening_enabled) VALUES (?, 'APPROVED', 1, 1, 1)", accountId);
    }

    private long seedActiveAssignment(long accountId) {
        long synthetic = 900000000L + (accountId % 1000000);
        jdbc.update("INSERT INTO vi_driver_assignment (order_id, driver_id, invite_id, accepted_at) "
                + "VALUES (?, ?, ?, UTC_TIMESTAMP(3))", synthetic, accountId, synthetic);
        return jdbc.queryForObject("SELECT id FROM vi_driver_assignment WHERE driver_id = ? AND ended_at IS NULL",
                Long.class, accountId);
    }

    // ---------- 用例 ----------

    @Test
    void 刷新轮换成功且新访问令牌可用() {
        JsonNode s = login("p03-rotate-" + System.nanoTime());
        String oldAccess = s.get("token").asText();
        String oldRefresh = s.get("refreshToken").asText();
        long sessionId = Long.parseLong(s.get("sessionId").asText());

        ResponseEntity<String> r = refresh(oldRefresh, null);
        assertEquals(200, r.getStatusCode().value(), "刷新应200 " + r.getBody());
        JsonNode d = ViJson.tree(r.getBody()).get("data");
        String newAccess = d.get("token").asText();
        String newRefresh = d.get("refreshToken").asText();
        assertNotEquals(oldAccess, newAccess, "访问令牌须轮换");
        assertNotEquals(oldRefresh, newRefresh, "刷新令牌须轮换");
        assertEquals(s.get("accountId").asText(), d.get("accountId").asText(), "同一账号");
        assertEquals(sessionId, Long.parseLong(d.get("sessionId").asText()), "同一会话");
        assertTrue(d.get("expiresIn").asLong() >= 1);
        assertTrue(d.get("refreshExpiresIn").asLong() >= 1);
        // 新访问令牌可用
        assertEquals(200, session(newAccess).getStatusCode().value(), "新访问令牌应可读取会话");
        // 代际递增、父令牌链接
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_session_token WHERE session_id = ? "
                + "AND token_type = 'REFRESH' AND generation = 1 AND parent_token_id IS NOT NULL", Integer.class, sessionId),
                "新刷新令牌 generation=1 且链接父令牌");
        assertNotNull(jdbc.queryForObject("SELECT consumed_at FROM vi_session_token WHERE session_id = ? "
                + "AND token_type = 'REFRESH' AND generation = 0", java.sql.Timestamp.class, sessionId),
                "旧刷新令牌已原子消费");
    }

    @Test
    void 重用已消费刷新撤销会话族且持久化() {
        JsonNode s = login("p03-reuse-" + System.nanoTime());
        String oldRefresh = s.get("refreshToken").asText();
        long sessionId = Long.parseLong(s.get("sessionId").asText());

        JsonNode rotated = ViJson.tree(refresh(oldRefresh, null).getBody()).get("data");
        String newAccess = rotated.get("token").asText();
        String newRefresh = rotated.get("refreshToken").asText();
        assertEquals(200, session(newAccess).getStatusCode().value(), "轮换后新令牌先可用");

        // 重用旧刷新 → 401 VI_REFRESH_REUSED，且撤销须持久化（事务提交后才报错）
        ResponseEntity<String> reuse = refresh(oldRefresh, null);
        assertEquals(401, reuse.getStatusCode().value(), "重用应401 " + reuse.getBody());
        assertEquals("VI_REFRESH_REUSED", ViJson.tree(reuse.getBody()).get("errorCode").asText());
        assertEquals("REVOKED", jdbc.queryForObject("SELECT status FROM vi_session WHERE id = ?", String.class, sessionId),
                "会话族撤销须持久化");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_session_token WHERE session_id = ? "
                + "AND revoked_at IS NULL", Integer.class, sessionId), "会话全部令牌须撤销");
        // 已轮换的新令牌亦被撤销（族撤销）
        assertEquals(401, session(newAccess).getStatusCode().value(), "新访问令牌随族撤销失效");
        assertEquals(401, refresh(newRefresh, null).getStatusCode().value(), "新刷新令牌随族撤销失效");
    }

    @Test
    void 并发刷新仅一条成功其余重用拒绝() throws Exception {
        JsonNode s = login("p03-conc-" + System.nanoTime());
        String oldRefresh = s.get("refreshToken").asText();
        long sessionId = Long.parseLong(s.get("sessionId").asText());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            futures.add(pool.submit(() -> {
                gate.await();
                return refresh(oldRefresh, null).getStatusCode().value();
            }));
        }
        gate.countDown();
        int c1 = futures.get(0).get(30, TimeUnit.SECONDS);
        int c2 = futures.get(1).get(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertTrue((c1 == 200 && c2 == 401) || (c1 == 401 && c2 == 200),
                "并发刷新仅一条成功，实际 " + c1 + "/" + c2);
        assertEquals("REVOKED", jdbc.queryForObject("SELECT status FROM vi_session WHERE id = ?", String.class, sessionId),
                "重用触发会话族撤销");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_session_token WHERE session_id = ? "
                + "AND token_type = 'REFRESH' AND consumed_at IS NOT NULL", Integer.class, sessionId),
                "旧刷新仅被消费一次");
    }

    @Test
    void 会话绝对到期刷新拒绝不滑动() {
        JsonNode s = login("p03-expiry-" + System.nanoTime());
        String oldRefresh = s.get("refreshToken").asText();
        long sessionId = Long.parseLong(s.get("sessionId").asText());
        // 模拟绝对到期（不等待7天）：会话与刷新令牌截止置过去
        jdbc.update("UPDATE vi_session SET absolute_expires_at = DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 HOUR) WHERE id = ?",
                sessionId);
        jdbc.update("UPDATE vi_session_token SET expires_at = DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 1 HOUR) "
                + "WHERE session_id = ? AND token_type = 'REFRESH'", sessionId);
        ResponseEntity<String> r = refresh(oldRefresh, null);
        assertEquals(401, r.getStatusCode().value(), "绝对到期应401 " + r.getBody());
        assertEquals("VI_AUTH_REQUIRED", ViJson.tree(r.getBody()).get("errorCode").asText());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_session_token WHERE session_id = ? "
                + "AND token_type = 'REFRESH' AND generation = 1", Integer.class, sessionId),
                "到期不得轮换出新令牌");
        assertTrue(jdbc.queryForObject("SELECT absolute_expires_at FROM vi_session WHERE id = ?",
                java.sql.Timestamp.class, sessionId).toInstant().isBefore(java.time.Instant.now()),
                "绝对截止不被滑动延长");
    }

    @Test
    void 当前会话视图只读无令牌且实时重算角色() {
        String persona = "p03-view-" + System.nanoTime();
        JsonNode s = login(persona);
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());

        ResponseEntity<String> r = session(token);
        assertEquals(200, r.getStatusCode().value(), "会话视图应200 " + r.getBody());
        JsonNode d = ViJson.tree(r.getBody()).get("data");
        assertEquals(s.get("sessionId").asText(), d.get("sessionId").asText());
        assertEquals("OWNER", d.get("activeRole").asText());
        assertTrue(d.has("availableRoles") && d.has("capabilities") && d.has("employeeLink"));
        assertFalse(d.has("token"), "会话视图不得返回访问令牌");
        assertFalse(d.has("refreshToken"), "会话视图不得返回刷新令牌");
        assertFalse(d.get("availableRoles").toString().contains("DRIVER"), "未批准资格不得有DRIVER");

        // 批准资格后无需重登，会话视图实时重算出 DRIVER
        seedDriverProfile(accountId);
        JsonNode d2 = ViJson.tree(session(token).getBody()).get("data");
        assertTrue(d2.get("availableRoles").toString().contains("DRIVER"), "权威状态变化应实时反映");
        assertEquals("APPROVED", d2.get("driverQualification").asText());
    }

    @Test
    void 退出当前会话旧令牌失效保留车辆任务重登恢复_TC_R63_008() {
        String run = newRun();
        String persona = "p03-tc008-" + System.nanoTime();
        JsonNode s = login(run, persona);
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        long sessionId = Long.parseLong(s.get("sessionId").asText());
        seedDriverProfile(accountId);
        long assignmentId = seedActiveAssignment(accountId);
        assertTrue(ViJson.tree(session(token).getBody()).get("data").get("availableRoles").toString().contains("DRIVER"),
                "退出前司机有有效资格与在手任务");

        ResponseEntity<String> out = logout(token, false, UUID.randomUUID().toString());
        assertEquals(200, out.getStatusCode().value(), "退出应200 " + out.getBody());
        JsonNode od = ViJson.tree(out.getBody()).get("data");
        assertTrue(od.get("loggedOut").asBoolean());
        assertEquals("CURRENT", od.get("scope").asText());
        assertFalse(od.has("token"), "退出结果不得返回令牌");

        // 旧令牌失效
        assertEquals(401, session(token).getStatusCode().value(), "退出后旧访问令牌须失效");
        assertEquals("REVOKED", jdbc.queryForObject("SELECT status FROM vi_session WHERE id = ?", String.class, sessionId));
        // 在手承接与历史资格保留（不动车辆任务）
        assertNull(jdbc.queryForObject("SELECT ended_at FROM vi_driver_assignment WHERE id = ?",
                java.sql.Timestamp.class, assignmentId), "退出不得结束在手承接");
        assertEquals("APPROVED", jdbc.queryForObject("SELECT qualification_status FROM vi_driver_profile WHERE account_id = ?",
                String.class, accountId), "退出不得撤销历史资格");

        // 重登恢复真实资格（同 run+persona 命中同一账号）
        JsonNode relogin = login(run, persona);
        assertEquals(s.get("accountId").asText(), relogin.get("accountId").asText(), "重登同一账号");
        String newToken = relogin.get("token").asText();
        JsonNode rd = ViJson.tree(session(newToken).getBody()).get("data");
        assertTrue(rd.get("availableRoles").toString().contains("DRIVER"), "重登恢复DRIVER资格");
        assertEquals("APPROVED", rd.get("driverQualification").asText());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_driver_assignment WHERE id = ? AND ended_at IS NULL",
                Integer.class, assignmentId), "在手承接始终保留");
    }

    @Test
    void 全部设备退出递增epoch撤销所有会话重登恢复() {
        String run = newRun();
        String persona = "p03-all-" + System.nanoTime();
        JsonNode a = login(run, persona);
        JsonNode b = login(run, persona);
        String tokenA = a.get("token").asText();
        String tokenB = b.get("token").asText();
        long accountId = Long.parseLong(a.get("accountId").asText());
        assertNotEquals(a.get("sessionId").asText(), b.get("sessionId").asText(), "两次登录产生两个会话");
        assertEquals(200, session(tokenB).getStatusCode().value(), "退出前两会话均可用");
        long epochBefore = jdbc.queryForObject("SELECT session_epoch FROM vi_account_security WHERE account_id = ?",
                Long.class, accountId);

        ResponseEntity<String> out = logout(tokenA, true, UUID.randomUUID().toString());
        assertEquals(200, out.getStatusCode().value(), "全部退出应200 " + out.getBody());
        assertEquals("ALL", ViJson.tree(out.getBody()).get("data").get("scope").asText());
        long epochAfter = jdbc.queryForObject("SELECT session_epoch FROM vi_account_security WHERE account_id = ?",
                Long.class, accountId);
        assertEquals(epochBefore + 1, epochAfter, "全部退出递增 session_epoch");
        assertEquals(401, session(tokenA).getStatusCode().value(), "会话A令牌失效");
        assertEquals(401, session(tokenB).getStatusCode().value(), "另一设备会话B令牌亦失效");

        // 重登以最新 epoch 建立新会话，恢复可用
        JsonNode c = login(run, persona);
        assertEquals(200, session(c.get("token").asText()).getStatusCode().value(), "重登恢复可用");
    }

    @Test
    void 退出幂等键缺失或过短及未知字段拒绝() {
        JsonNode s = login("p03-idem-" + System.nanoTime());
        String token = s.get("token").asText();
        assertEquals(400, logout(token, false, null).getStatusCode().value(), "缺 Idempotency-Key 应400");
        assertEquals(400, logout(token, false, "short").getStatusCode().value(), "过短幂等键应400");
        ResponseEntity<String> unknown = rest.exchange(url("/api/common/session/logout"), HttpMethod.POST,
                entity(Map.of("allDevices", false, "accountId", "1"), token, UUID.randomUUID().toString()), String.class);
        assertEquals(400, unknown.getStatusCode().value(), "退出请求未知字段应400");
        // 会话未被上述被拒请求撤销
        assertEquals(200, session(token).getStatusCode().value(), "被拒请求不得撤销会话");
    }

    @Test
    void MINI鉴权数据库权威撤销即时拒绝不依赖Redis() {
        // MINI 放行仅读 vi_session_token/vi_session/vi_account_security（resolveAccess 无任何 Redis 调用），
        // 故 Redis 故障不可能导致越权放行；本条以数据库权威撤销即时生效验证该保证（无缓存宽限）。
        JsonNode s = login("p03-dbauth-" + System.nanoTime());
        String token = s.get("token").asText();
        long sessionId = Long.parseLong(s.get("sessionId").asText());
        assertEquals(200, session(token).getStatusCode().value(), "撤销前令牌可用");
        // 直接在权威库撤销会话（模拟任何来源的权威撤权，不经缓存）
        jdbc.update("UPDATE vi_session SET status = 'REVOKED', revoked_at = UTC_TIMESTAMP(3), version = version + 1 "
                + "WHERE id = ?", sessionId);
        ResponseEntity<String> r = session(token);
        assertEquals(401, r.getStatusCode().value(), "数据库权威撤销须即时拒绝 " + r.getBody());
        assertEquals("VI_AUTH_REQUIRED", ViJson.tree(r.getBody()).get("errorCode").asText());
    }

    @Test
    void 刷新拒绝未知字段非法令牌与跨域令牌() {
        JsonNode s = login("p03-badrefresh-" + System.nanoTime());
        String access = s.get("token").asText();
        // 未知字段
        assertEquals(400, refresh(s.get("refreshToken").asText(), Map.of("role", "DRIVER")).getStatusCode().value(),
                "刷新混入未知字段应400");
        // 过短令牌
        assertEquals(400, refresh("too-short", null).getStatusCode().value(), "过短 refreshToken 应400");
        // 未知随机令牌（长度合法但无匹配摘要）
        String unknown = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(new SecureRandom().generateSeed(32));
        ResponseEntity<String> r = refresh(unknown, null);
        assertEquals(401, r.getStatusCode().value(), "未知刷新令牌应401");
        assertEquals("VI_AUTH_REQUIRED", ViJson.tree(r.getBody()).get("errorCode").asText());
        // 以 ACCESS 令牌冒充 REFRESH（域分离：按 REFRESH 摘要查无匹配）
        assertEquals(401, refresh(access, null).getStatusCode().value(), "ACCESS 令牌不得用于刷新");
    }
}