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
import java.security.MessageDigest;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I02 手机号验证回执与自动关联 真实集成测试（HTTP+SpringSecurity(MINI链)+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 仅微信渠道为 SIMULATOR 替身（L01 另验）。覆盖 I02 直接可测用例：
 * TC-R62-008（裸字段拒绝）、014（回执查询/安全重试）、021（无效凭据失败不假成功）、025（手机号与员工结果分开/重号）、
 * 031（同 requestId 并发一次消费）、TC-R63-012（未核验不开 STATION）；唯一匹配 LINKED+STATION；SMS 未配置拒绝；档案 PATCH CAS。
 * TC-R62-029（订单联系人）属订单任务、TC-R62-030（SMS 成功路径）依赖 X01–X07 配置，本 IT 不伪造（仅验证未配置拒绝与不关联原则）。
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
class I02IdentityIT {

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
        // 手机号加密/摘要密钥（测试值，非生产凭据）
        registry.add("vi.crypto.data-key", () -> "i02-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "i02-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("i02-sim-", ".sqlite");
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
        for (String t : List.of("vi_phone_verification_receipt", "vi_employee_binding", "vi_session_token",
                "vi_session", "vi_account_security", "vi_account", "vi_station_grant", "vi_owner_profile")) {
            jdbc.execute("DELETE FROM " + t);
        }
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'i02emp\\_%'");
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

    /** 复算模拟器 /phone/exchange 的 verifiedPhone：'199' + (sha256(persona)[:10] hex % 1e8) 8位。 */
    private static String expectedPhone(String persona) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(persona.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : h) {
                hex.append(String.format("%02x", b));
            }
            long v = Long.parseLong(hex.substring(0, 10), 16) % 100000000L;
            return "199" + String.format("%08d", v);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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

    private JsonNode login(String persona) {
        String run = newRun();
        String code = newCode(run, persona, "wechat");
        ResponseEntity<String> resp = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                entity(Map.of("code", code), null, null), String.class);
        assertEquals(200, resp.getStatusCode().value(), "登录应200 " + resp.getBody());
        return ViJson.tree(resp.getBody()).get("data");
    }

    private long seedEmployee(String phone, String suffix) {
        String userName = "i02emp_" + suffix;
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, phonenumber, status, del_flag, auth_version, password) "
                + "VALUES (?, ?, ?, '0', '0', 0, 'x')", userName, "员工" + suffix, phone);
        return jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, userName);
    }

    private void seedGrant(long sysUserId, long stationId) {
        jdbc.update("INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason) "
                + "VALUES (?, ?, 1, 1, 'I02 测试授权')", sysUserId, stationId);
    }

    private ResponseEntity<String> bind(String token, long accountVersion, String phoneCode,
                                        String requestId, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        body.put("phoneCode", phoneCode);
        body.put("expectedAccountVersion", accountVersion);
        body.put("verificationRequestId", requestId);
        if (extra != null) {
            body.putAll(extra);
        }
        return rest.exchange(url("/api/common/phone/bind"), HttpMethod.POST, entity(body, token, null), String.class);
    }

    // ---------- 用例 ----------

    @Test
    void bind混入裸手机号或假身份字段被400拒绝_TC_R62_008() {
        JsonNode session = login("p02-forged-" + System.nanoTime());
        String token = session.get("token").asText();
        long ver = session.get("accountVersion").asLong();
        String run = newRun();
        String persona = "p02-forged2-" + System.nanoTime();
        String code = newCode(run, persona, "phone");

        ResponseEntity<String> r1 = bind(token, ver, code, UUID.randomUUID().toString(), Map.of("phone", "13800000000"));
        assertEquals(400, r1.getStatusCode().value(), "混入裸 phone 应400 " + r1.getBody());
        ResponseEntity<String> r2 = bind(token, ver, newCode(run, persona, "phone"),
                UUID.randomUUID().toString(), Map.of("sysUserId", "1"));
        assertEquals(400, r2.getStatusCode().value(), "混入 sysUserId 应400");
        ResponseEntity<String> r3 = bind(token, ver, newCode(run, persona, "phone"),
                UUID.randomUUID().toString(), Map.of("openId", "fake"));
        assertEquals(400, r3.getStatusCode().value(), "混入 openId 应400");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_phone_verification_receipt", Integer.class),
                "被拒请求不得登记回执");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class),
                "不得建立员工关联");
    }

    @Test
    void 核验成功响应丢失可查回执并安全重试_TC_R62_014() {
        JsonNode session = login("p02-receipt-" + System.nanoTime());
        String token = session.get("token").asText();
        long accountId = Long.parseLong(session.get("accountId").asText());
        long ver = session.get("accountVersion").asLong();
        String run = newRun();
        String code = newCode(run, "p02-receipt2-" + System.nanoTime(), "phone");
        String requestId = UUID.randomUUID().toString();

        ResponseEntity<String> first = bind(token, ver, code, requestId, null);
        assertEquals(200, first.getStatusCode().value(), "首次绑定应200 " + first.getBody());
        JsonNode d1 = ViJson.tree(first.getBody()).get("data");
        assertEquals("VERIFIED", d1.get("verificationStatus").asText());
        assertTrue(d1.get("phoneBound").asBoolean());

        // 响应丢失场景：按原 requestId 查询回执（不重复消费凭据）
        ResponseEntity<String> q = rest.exchange(url("/api/common/phone/verifications/" + requestId),
                HttpMethod.GET, entity(null, token, null), String.class);
        assertEquals(200, q.getStatusCode().value());
        assertEquals("VERIFIED", ViJson.tree(q.getBody()).get("data").get("verificationStatus").asText());

        // 同 requestId 安全重试：幂等返回，不重复消费、不新建回执
        ResponseEntity<String> retry = bind(token, ver, code, requestId, null);
        assertEquals(200, retry.getStatusCode().value());
        assertEquals("VERIFIED", ViJson.tree(retry.getBody()).get("data").get("verificationStatus").asText());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_phone_verification_receipt WHERE account_id = ?",
                Integer.class, accountId), "同 requestId 只一条回执");
    }

    @Test
    void 无效phoneCode失败不假成功不开STATION_TC_R62_021_R63_012() {
        JsonNode session = login("p02-fail-" + System.nanoTime());
        String token = session.get("token").asText();
        long ver = session.get("accountVersion").asLong();
        ResponseEntity<String> resp = bind(token, ver, "not-a-provisioned-phone-code",
                UUID.randomUUID().toString(), null);
        assertEquals(200, resp.getStatusCode().value(), "失败也返回200+状态FAILED " + resp.getBody());
        JsonNode d = ViJson.tree(resp.getBody()).get("data");
        assertEquals("FAILED", d.get("verificationStatus").asText(), "无效凭据应FAILED不假成功");
        assertFalse(d.get("phoneBound").asBoolean());
        assertEquals("UNLINKED", d.get("employeeLink").get("status").asText());
        // 未核验不得开通 STATION
        ResponseEntity<String> profile = rest.exchange(url("/api/common/profile"), HttpMethod.GET,
                entity(null, token, null), String.class);
        JsonNode roles = ViJson.tree(profile.getBody()).get("data").get("availableRoles");
        assertFalse(roles.toString().contains("STATION"), "未核验手机号不得开通STATION");
    }

    @Test
    void 目录无匹配手机号验证与员工结果分开_TC_R62_025() {
        JsonNode session = login("p02-nomatch-" + System.nanoTime());
        String token = session.get("token").asText();
        long ver = session.get("accountVersion").asLong();
        String run = newRun();
        String code = newCode(run, "p02-nomatch2-" + System.nanoTime(), "phone");
        ResponseEntity<String> resp = bind(token, ver, code, UUID.randomUUID().toString(), null);
        assertEquals(200, resp.getStatusCode().value());
        JsonNode d = ViJson.tree(resp.getBody()).get("data");
        assertEquals("VERIFIED", d.get("verificationStatus").asText());
        assertTrue(d.get("phoneBound").asBoolean(), "手机号核验成功");
        assertEquals("UNLINKED", d.get("employeeLink").get("status").asText(), "无员工匹配不影响手机号验证");
        // OWNER 不受影响
        ResponseEntity<String> profile = rest.exchange(url("/api/common/profile"), HttpMethod.GET,
                entity(null, token, null), String.class);
        assertTrue(ViJson.tree(profile.getBody()).get("data").get("availableRoles").toString().contains("OWNER"));
    }

    @Test
    void 目录重号核验成功但关联需管理员_TC_R62_025() {
        String persona = "p02-dup-" + System.nanoTime();
        String phone = expectedPhone(persona);
        seedEmployee(phone, "a" + System.nanoTime());
        seedEmployee(phone, "b" + System.nanoTime());
        JsonNode session = login(persona);
        String token = session.get("token").asText();
        long ver = session.get("accountVersion").asLong();
        String run = newRun();
        String code = newCode(run, persona, "phone");
        ResponseEntity<String> resp = bind(token, ver, code, UUID.randomUUID().toString(), null);
        assertEquals(200, resp.getStatusCode().value(), resp.getBody());
        JsonNode d = ViJson.tree(resp.getBody()).get("data");
        assertEquals("VERIFIED", d.get("verificationStatus").asText(), "重号不推翻手机号验证");
        assertTrue(d.get("phoneBound").asBoolean());
        assertEquals("NEEDS_ADMIN", d.get("employeeLink").get("status").asText(), "重号粗粒度NEEDS_ADMIN不泄细节");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class),
                "重号不得自动建立绑定");
    }

    @Test
    void 唯一有效员工自动关联LINKED且可STATION() {
        String persona = "p02-link-" + System.nanoTime();
        String phone = expectedPhone(persona);
        long sysUserId = seedEmployee(phone, "link" + System.nanoTime());
        seedGrant(sysUserId, 800000000L + (sysUserId % 100000));
        JsonNode session = login(persona);
        String token = session.get("token").asText();
        long accountId = Long.parseLong(session.get("accountId").asText());
        long ver = session.get("accountVersion").asLong();
        String run = newRun();
        String code = newCode(run, persona, "phone");
        ResponseEntity<String> resp = bind(token, ver, code, UUID.randomUUID().toString(), null);
        assertEquals(200, resp.getStatusCode().value(), resp.getBody());
        JsonNode d = ViJson.tree(resp.getBody()).get("data");
        assertEquals("VERIFIED", d.get("verificationStatus").asText());
        assertEquals("LINKED", d.get("employeeLink").get("status").asText());
        assertTrue(d.get("requiresSessionRefresh").asBoolean(), "关联成功须刷新会话");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding WHERE account_id = ? AND sys_user_id = ?",
                Integer.class, accountId, sysUserId), "应建立唯一绑定");
        // 刷新会话后档案含 STATION（permission_version 已递增）
        ResponseEntity<String> profile = rest.exchange(url("/api/common/profile"), HttpMethod.GET,
                entity(null, token, null), String.class);
        assertTrue(ViJson.tree(profile.getBody()).get("data").get("availableRoles").toString().contains("STATION"),
                "有效员工+站点授权应可STATION");
    }

    @Test
    void 同requestId并发只一次外部消费_TC_R62_031() throws Exception {
        JsonNode session = login("p02-conc-" + System.nanoTime());
        String token = session.get("token").asText();
        long accountId = Long.parseLong(session.get("accountId").asText());
        long ver = session.get("accountVersion").asLong();
        String run = newRun();
        String code = newCode(run, "p02-conc2-" + System.nanoTime(), "phone");
        String requestId = UUID.randomUUID().toString();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            futures.add(pool.submit(() -> {
                gate.await();
                ResponseEntity<String> r = bind(token, ver, code, requestId, null);
                return r.getStatusCode().value();
            }));
        }
        gate.countDown();
        int c1 = futures.get(0).get(30, TimeUnit.SECONDS);
        int c2 = futures.get(1).get(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertEquals(200, c1);
        assertEquals(200, c2);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_phone_verification_receipt WHERE account_id = ?",
                Integer.class, accountId), "并发同 requestId 只一条回执（一次外部消费）");
        ResponseEntity<String> q = rest.exchange(url("/api/common/phone/verifications/" + requestId),
                HttpMethod.GET, entity(null, token, null), String.class);
        assertEquals("VERIFIED", ViJson.tree(q.getBody()).get("data").get("verificationStatus").asText());
    }

    @Test
    void 短信兜底未配置拒绝且不关联_TC_R62_030原则() {
        JsonNode session = login("p02-sms-" + System.nanoTime());
        String token = session.get("token").asText();
        ResponseEntity<String> resp = rest.exchange(url("/api/common/phone/sms/send"), HttpMethod.POST,
                entity(Map.of("phone", "13800000000", "scene", "BIND_PHONE"), token, UUID.randomUUID().toString()),
                String.class);
        assertEquals(422, resp.getStatusCode().value(), "未配置短信路由应422拒绝 " + resp.getBody());
        assertEquals("VI_CAPABILITY_NOT_CONFIGURED", ViJson.tree(resp.getBody()).get("errorCode").asText());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class),
                "短信路径绝不建立员工关联");
    }

    @Test
    void 档案PATCH_CAS更新本人昵称_API_012() {
        JsonNode session = login("p02-patch-" + System.nanoTime());
        String token = session.get("token").asText();
        long ver = session.get("accountVersion").asLong();
        ResponseEntity<String> ok = rest.exchange(url("/api/app/owner/profile"), HttpMethod.PATCH,
                entity(Map.of("nickname", "新昵称", "expectedVersion", ver), token, UUID.randomUUID().toString()),
                String.class);
        assertEquals(200, ok.getStatusCode().value(), "档案PATCH应200 " + ok.getBody());
        assertEquals("新昵称", ViJson.tree(ok.getBody()).get("data").get("nickname").asText());
        // 旧版本 CAS 冲突
        ResponseEntity<String> conflict = rest.exchange(url("/api/app/owner/profile"), HttpMethod.PATCH,
                entity(Map.of("nickname", "再次", "expectedVersion", ver), token, UUID.randomUUID().toString()),
                String.class);
        assertEquals(409, conflict.getStatusCode().value(), "旧版本应409冲突");
    }
}
