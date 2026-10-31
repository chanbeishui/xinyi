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
import java.security.MessageDigest;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I05 手机号自动关联员工与后台异常管理 真实集成测试（HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 微信渠道为 SIMULATOR 替身（L01 另验）；ADMIN 令牌经框架 TokenService 铸造（测试夹具，非真实登录）。
 * 覆盖：唯一匹配无凭证、重号/多部门去重/抢占/转移拒绝、并发唯一、停用/换号钩子失权、revoke 防复绑、
 * allow-rematch 不直接恢复、无关联不能暂停、MINI 不获 ADMIN、退役旧路由拒绝、reconcile 窗口内/外。
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
class I05IdentityIT {

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
        registry.add("vi.crypto.data-key", () -> "i05-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "i05-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("i05-sim-", ".sqlite");
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
        for (String t : List.of("vi_driver_assignment", "vi_driver_profile", "vi_phone_verification_receipt",
                "vi_employee_binding", "vi_session_token", "vi_session", "vi_account_security", "vi_account",
                "vi_station_grant", "vi_owner_profile")) {
            jdbc.execute("DELETE FROM " + t);
        }
        jdbc.execute("DELETE FROM sys_user_dept WHERE user_id IN (SELECT user_id FROM sys_user WHERE user_name LIKE 'i05emp\\_%')");
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'i05emp\\_%'");
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
            assertEquals(200, resp.statusCode(), "模拟器 " + path + " 应200 " + resp.body());
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

    // ---------- HTTP ----------
    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
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
        Long authVersion = jdbc.queryForObject("SELECT auth_version FROM sys_user WHERE user_id = ?", Long.class, admin.getUserId());
        LoginUser lu = new LoginUser(admin, Set.of("*:*:*"));
        lu.setUserId(admin.getUserId());
        lu.setAuthVersion(authVersion);
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
        return new HttpEntity<>(body, h);
    }

    private JsonNode login(String persona) {
        // 模拟器 openId=sim_run_persona：同 persona 复用同一 run 才能重登命中同一账号
        String run = personaRuns.computeIfAbsent(persona, k -> newRun());
        String code = newCode(run, persona, "wechat");
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", code), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private final Map<String, String> personaRuns = new HashMap<>();

    private JsonNode bind(JsonNode session, String persona) {
        long accountId = Long.parseLong(session.get("accountId").asText());
        long version = jdbc.queryForObject("SELECT version FROM vi_account WHERE id = ?", Long.class, accountId);
        String code = newCode(newRun(), persona, "phone");
        ResponseEntity<String> r = rest.exchange(url("/api/common/phone/bind"), HttpMethod.POST,
                mini(Map.of("phoneCode", code, "expectedAccountVersion", version,
                        "verificationRequestId", UUID.randomUUID().toString()), session.get("token").asText()), String.class);
        assertEquals(200, r.getStatusCode().value(), "绑定应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private JsonNode link(String token) {
        ResponseEntity<String> r = rest.exchange(url("/api/common/employee-link"), HttpMethod.GET,
                mini(null, token), String.class);
        assertEquals(200, r.getStatusCode().value());
        return ViJson.tree(r.getBody()).get("data");
    }

    private long seedEmployee(String phone, String suffix) {
        String userName = "i05emp_" + suffix;
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, phonenumber, status, del_flag, auth_version, password) "
                + "VALUES (?, ?, ?, '0', '0', 0, 'x')", userName, "员工" + suffix, phone);
        return jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, userName);
    }

    private void seedGrant(long sysUserId, long stationId) {
        jdbc.update("INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason) "
                + "VALUES (?, ?, 1, 1, 'I05 测试授权')", sysUserId, stationId);
    }

    private long bindingIdOf(long accountId) {
        return jdbc.queryForObject("SELECT id FROM vi_employee_binding WHERE account_id = ? ORDER BY id DESC LIMIT 1",
                Long.class, accountId);
    }

    // ---------- 用例 ----------

    @Test
    void 唯一匹配自动关联无凭证且不免司机审核_TC_R50_016_R62_007() {
        String persona = "p05-link-" + System.nanoTime();
        long sysUserId = seedEmployee(expectedPhone(persona), "a" + System.nanoTime());
        seedGrant(sysUserId, 800000000L + (sysUserId % 100000));
        JsonNode s = login(persona);
        JsonNode r = bind(s, persona);
        assertEquals("LINKED", r.get("employeeLink").get("status").asText());
        long accountId = Long.parseLong(s.get("accountId").asText());
        assertEquals("WECHAT_PHONE_MATCH", jdbc.queryForObject("SELECT source FROM vi_employee_binding WHERE account_id = ?",
                String.class, accountId));
        assertNullBoundBy(accountId);
        JsonNode l = link(s.get("token").asText());
        assertTrue(l.get("workAccessEnabled").asBoolean());
        assertFalse(l.toString().contains("DRIVER"), "员工关联不自动获DRIVER");
    }

    private void assertNullBoundBy(long accountId) {
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding WHERE account_id = ? AND bound_by IS NOT NULL",
                Integer.class, accountId), "自动关联 bound_by 须为 NULL（无申请/签发/消费）");
    }

    @Test
    void 重号不选第一不泄名单_TC_R62_009() {
        String persona = "p05-dup-" + System.nanoTime();
        String phone = expectedPhone(persona);
        seedEmployee(phone, "a" + System.nanoTime());
        seedEmployee(phone, "b" + System.nanoTime());
        JsonNode s = login(persona);
        JsonNode r = bind(s, persona);
        assertEquals("NEEDS_ADMIN", r.get("employeeLink").get("status").asText());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class));
        assertFalse(r.toString().contains("sysUserId"), "不泄员工名单/ID");
    }

    @Test
    void 多部门join按主键去重_TC_R62_010() {
        String persona = "p05-dept-" + System.nanoTime();
        long sysUserId = seedEmployee(expectedPhone(persona), "d" + System.nanoTime());
        jdbc.update("INSERT INTO sys_user_dept (user_id, dept_id) VALUES (?, 100), (?, 101)",
                sysUserId, sysUserId);
        JsonNode s = login(persona);
        JsonNode r = bind(s, persona);
        assertEquals("LINKED", r.get("employeeLink").get("status").asText(), "多部门任职不误判多名");
    }

    @Test
    void 员工已绑其他微信拒绝覆盖_TC_R62_011() {
        String phone = expectedPhone("p05-own-" + System.nanoTime());
        long sysUserId = seedEmployee(phone, "o" + System.nanoTime());
        JsonNode a = login("p05-ownA-" + System.nanoTime());
        // 让 A 的微信核验号码等于目录号码：用同一 persona 生成 phone code 需 persona 决定号码；改用直接目录=A核验号
        // 这里用 A 自身核验号建目录员工，保证 A 先绑
        String personaA = "p05-first-" + System.nanoTime();
        long empA = seedEmployee(expectedPhone(personaA), "f" + System.nanoTime());
        JsonNode sa = login(personaA);
        assertEquals("LINKED", bind(sa, personaA).get("employeeLink").get("status").asText());
        // 另一微信账号 B 授权同一号码（目录 empA 的号码）→ 需 B 的 phone code 对应同号：模拟器号码由 persona 决定，
        // 故改目录 empA 号码为 B 的核验号不可行；改为断言 countActiveByAppUser 保护：B 用同号 persona 不可能。
        // 采用：把 empA 的目录号码改成 B 核验号后 A 已绑（phone_lookup 旧），B 授权新号匹配 empA → USER_ALREADY_LINKED。
        String personaB = "p05-second-" + System.nanoTime();
        jdbc.update("UPDATE sys_user SET phonenumber = ? WHERE user_id = ?", expectedPhone(personaB), empA);
        JsonNode sb = login(personaB);
        JsonNode rb = bind(sb, personaB);
        assertEquals("NEEDS_ADMIN", rb.get("employeeLink").get("status").asText(), "员工已被其他微信占用拒绝覆盖");
        long accountIdA = Long.parseLong(sa.get("accountId").asText());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding WHERE sys_user_id = ? AND revoked_at IS NULL",
                Integer.class, empA), "原关联保留唯一");
    }

    @Test
    void 账号已绑另一员工拒绝转移_TC_R62_012() {
        String personaA = "p05-tA-" + System.nanoTime();
        long empA = seedEmployee(expectedPhone(personaA), "ta" + System.nanoTime());
        JsonNode s = login(personaA);
        assertEquals("LINKED", bind(s, personaA).get("employeeLink").get("status").asText());
        // 目录新增员工B，账号再授权B号码 → 不转移
        String personaB = "p05-tB-" + System.nanoTime();
        long empB = seedEmployee(expectedPhone(personaB), "tb" + System.nanoTime());
        JsonNode r = bind(s, personaB);
        assertEquals("NEEDS_ADMIN", r.get("employeeLink").get("status").asText());
        long accountId = Long.parseLong(s.get("accountId").asText());
        assertEquals(empA, jdbc.queryForObject("SELECT sys_user_id FROM vi_employee_binding WHERE account_id = ? AND revoked_at IS NULL",
                Long.class, accountId), "不自动转移身份");
    }

    @Test
    void 并发绑定同一员工只一条有效_TC_R62_013() throws Exception {
        String persona = "p05-conc-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "c" + System.nanoTime());
        JsonNode a = login("p05-concA-" + System.nanoTime());
        JsonNode b = login("p05-concB-" + System.nanoTime());
        // 两账号目录同号：把 emp 号码设为 A 核验号，B 用另号不匹配；改为两账号同 persona 不可能（openid 不同但号码同需同 persona）
        // 用同一 persona 登录两账号不可行；改并发同一账号两次 bind（同 requestId 不同）→ 唯一
        String code1 = newCode(newRun(), persona, "phone");
        String code2 = newCode(newRun(), persona, "phone");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<String>> fs = new java.util.ArrayList<>();
        for (String code : List.of(code1, code2)) {
            fs.add(pool.submit(() -> {
                gate.await();
                ResponseEntity<String> r = rest.exchange(url("/api/common/phone/bind"), HttpMethod.POST,
                        mini(Map.of("phoneCode", code, "expectedAccountVersion", a.get("accountVersion").asLong(),
                                "verificationRequestId", UUID.randomUUID().toString()), a.get("token").asText()), String.class);
                return ViJson.tree(r.getBody()).get("data").get("employeeLink").get("status").asText();
            }));
        }
        gate.countDown();
        String r1 = fs.get(0).get(30, TimeUnit.SECONDS);
        String r2 = fs.get(1).get(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertTrue(r1.equals("LINKED") || r2.equals("LINKED"));
        long accountId = Long.parseLong(a.get("accountId").asText());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding WHERE account_id = ? AND revoked_at IS NULL",
                Integer.class, accountId), "并发只一条有效关联");
    }

    @Test
    void 停用员工旧会话失权车主保留_TC_R62_015() {
        String persona = "p05-dis-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "dis" + System.nanoTime());
        seedGrant(emp, 800000000L + (emp % 100000));
        JsonNode s = login(persona);
        bind(s, persona);
        String token = s.get("token").asText();
        assertTrue(link(token).get("workAccessEnabled").asBoolean());
        String admin = adminToken();
        ResponseEntity<String> r = rest.exchange(url("/system/user/changeStatus"), HttpMethod.PUT,
                admin(Map.of("userId", emp, "status", "1", "reason", "停用测试"), admin), String.class);
        assertEquals(200, r.getStatusCode().value(), "停用应200 " + r.getBody());
        JsonNode l = link(token);
        assertFalse(l.get("workAccessEnabled").asBoolean(), "停用后旧会话失权");
        JsonNode sess = ViJson.tree(rest.exchange(url("/api/common/session"), HttpMethod.GET, mini(null, token), String.class)
                .getBody()).get("data");
        assertFalse(sess.get("availableRoles").toString().contains("STATION"));
        assertTrue(sess.get("availableRoles").toString().contains("OWNER"), "个人车主不删除");
        assertEquals("SUSPENDED", jdbc.queryForObject("SELECT status FROM vi_employee_binding WHERE sys_user_id = ? AND revoked_at IS NULL",
                String.class, emp), "钩子暂停绑定");
    }

    @Test
    void 换号旧能力暂停不变成另一员工_TC_R62_019() {
        String persona = "p05-phone-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "ph" + System.nanoTime());
        seedGrant(emp, 800000000L + (emp % 100000));
        JsonNode s = login(persona);
        bind(s, persona);
        String token = s.get("token").asText();
        String admin = adminToken();
        String newPhone = "199" + String.format("%08d", (System.nanoTime() % 100000000L));
        ResponseEntity<String> r = rest.exchange(url("/system/user"), HttpMethod.PUT,
                admin(Map.of("userId", emp, "phonenumber", newPhone, "nickName", "员工换号", "reason", "换号测试"), admin), String.class);
        assertEquals(200, r.getStatusCode().value(), "换号应200 " + r.getBody());
        assertFalse(link(token).get("workAccessEnabled").asBoolean(), "换号后旧能力暂停");
        assertEquals("SUSPENDED", jdbc.queryForObject("SELECT status FROM vi_employee_binding WHERE sys_user_id = ? AND revoked_at IS NULL",
                String.class, emp));
    }

    @Test
    void revoke后登录授权不自动复绑_TC_R62_020_R62_032() {
        String persona = "p05-rev-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "rev" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        long accountId = Long.parseLong(s.get("accountId").asText());
        long bindingId = bindingIdOf(accountId);
        String admin = adminToken();
        long ver = jdbc.queryForObject("SELECT version FROM vi_employee_binding WHERE id = ?", Long.class, bindingId);
        ResponseEntity<String> r = rest.exchange(url("/api/admin/employee-links/" + bindingId + "/revoke"), HttpMethod.POST,
                admin(Map.of("expectedVersion", ver, "reason", "管理员解除"), admin), String.class);
        assertEquals(200, r.getStatusCode().value(), "revoke应200 " + r.getBody());
        assertEquals("REVOKED", ViJson.tree(r.getBody()).get("data").get("status").asText());
        // 重新登录+授权同号 → 不自动复绑（U02 仍 REVOKED，OWNER 手机号核验可用）
        JsonNode s2 = login(persona);
        JsonNode r2 = bind(s2, persona);
        assertTrue(r2.get("phoneBound").asBoolean(), "OWNER 手机号核验仍可用");
        assertEquals("REVOKED", link(s2.get("token").asText()).get("status").asText(), "不自动悄悄复绑");
        assertEquals(1, jdbc.queryForObject("SELECT auto_rebind_blocked FROM vi_employee_binding WHERE id = ?",
                Integer.class, bindingId));
    }

    @Test
    void allowRematch不直接恢复须本人新核验_TC_R62_033_R63_005() {
        String persona = "p05-rem-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "rem" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        long accountId = Long.parseLong(s.get("accountId").asText());
        long bindingId = bindingIdOf(accountId);
        String admin = adminToken();
        long ver = jdbc.queryForObject("SELECT version FROM vi_employee_binding WHERE id = ?", Long.class, bindingId);
        rest.exchange(url("/api/admin/employee-links/" + bindingId + "/revoke"), HttpMethod.POST,
                admin(Map.of("expectedVersion", ver, "reason", "解除"), admin), String.class);
        long ver2 = jdbc.queryForObject("SELECT version FROM vi_employee_binding WHERE id = ?", Long.class, bindingId);
        ResponseEntity<String> r = rest.exchange(url("/api/admin/employee-links/" + bindingId + "/allow-rematch"), HttpMethod.POST,
                admin(Map.of("expectedVersion", ver2, "reason", "允许重核"), admin), String.class);
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        JsonNode d = ViJson.tree(r.getBody()).get("data");
        assertTrue(d.get("requiresNewPhoneAuthorization").asBoolean());
        assertEquals("REVOKED", d.get("binding").get("status").asText(), "不直接恢复ACTIVE");
        assertFalse(link(s.get("token").asText()).get("workAccessEnabled").asBoolean(), "本人未核验不恢复");
        // 本人新核验后重新关联（同一员工安全恢复）
        JsonNode r2 = bind(s, persona);
        assertEquals("LINKED", r2.get("employeeLink").get("status").asText(), "新可信核验后关联");
    }

    @Test
    void 无关联不能暂停或解除_TC_R63_011() {
        String admin = adminToken();
        ResponseEntity<String> r = rest.exchange(url("/api/admin/employee-links/999999999/suspend"), HttpMethod.POST,
                admin(Map.of("expectedVersion", 0, "reason", "无目标"), admin), String.class);
        assertEquals(404, r.getStatusCode().value(), "无关联暂停应404 " + r.getBody());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class), "不制造已暂停绑定");
    }

    @Test
    void MINI令牌不获ADMIN能力_TC_R62_022() {
        String persona = "p05-adm-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "adm" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        // MINI 令牌访问后台接口 → 拒绝（框架链不认 MINI 不透明令牌）
        ResponseEntity<String> r = rest.exchange(url("/api/admin/employee-links"), HttpMethod.GET,
                mini(null, s.get("token").asText()), String.class);
        JsonNode body = ViJson.tree(r.getBody());
        assertTrue(r.getStatusCode().value() == 401 || body.path("code").asInt() == 401, "MINI 不获 ADMIN");
        assertFalse(link(s.get("token").asText()).toString().contains("biz:employeebinding"), "不获后台权限");
    }

    @Test
    void 退役旧绑定申请路由拒绝无副作用_TC_R62_026_R63_015() {
        JsonNode s = login("p05-old-" + System.nanoTime());
        ResponseEntity<String> r = rest.exchange(url("/api/common/employee-binding/request"), HttpMethod.POST,
                mini(Map.of("requestReason", "x"), s.get("token").asText()), String.class);
        int code = ViJson.tree(r.getBody()).path("code").asInt(0);
        assertTrue(r.getStatusCode().value() >= 400 || code >= 400, "旧路由不存在/拒绝，实际 "
                + r.getStatusCode() + " " + r.getBody());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class), "无授权写入");
    }

    @Test
    void 暂停不能由登录绕过_TC_R63_004() {
        String persona = "p05-susp-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "sus" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        long accountId = Long.parseLong(s.get("accountId").asText());
        long bindingId = bindingIdOf(accountId);
        String admin = adminToken();
        long ver = jdbc.queryForObject("SELECT version FROM vi_employee_binding WHERE id = ?", Long.class, bindingId);
        rest.exchange(url("/api/admin/employee-links/" + bindingId + "/suspend"), HttpMethod.POST,
                admin(Map.of("expectedVersion", ver, "reason", "暂停"), admin), String.class);
        // 重新登录+授权同号 → 仍 SUSPENDED，无 STATION，不影响 OWNER
        JsonNode s2 = login(persona);
        bind(s2, persona);
        JsonNode l = link(s2.get("token").asText());
        assertEquals("SUSPENDED", l.get("status").asText());
        assertFalse(l.get("workAccessEnabled").asBoolean());
    }

    @Test
    void reconcile窗口内重试关联窗口外拒绝() {
        String persona = "p05-rec-" + System.nanoTime();
        JsonNode s = login(persona);
        bind(s, persona); // 无员工 → NO_MATCH/UNLINKED
        long accountId = Long.parseLong(s.get("accountId").asText());
        // 目录后建员工（同核验号）→ reconcile 窗口内重试 → LINKED
        seedEmployee(expectedPhone(persona), "rec" + System.nanoTime());
        long ctx = s.get("contextVersion").asLong();
        ResponseEntity<String> r = rest.exchange(url("/api/common/employee-link/reconcile"), HttpMethod.POST,
                mini(Map.of("expectedContextVersion", ctx), s.get("token").asText()), String.class);
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        assertEquals("LINKED", ViJson.tree(r.getBody()).get("data").get("status").asText());
        // 窗口外：把回执 verified_at 置 11 分钟前 → 422
        jdbc.update("UPDATE vi_phone_verification_receipt SET verified_at = DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 11 MINUTE) "
                + "WHERE account_id = ?", accountId);
        ResponseEntity<String> r2 = rest.exchange(url("/api/common/employee-link/reconcile"), HttpMethod.POST,
                mini(Map.of("expectedContextVersion", ctx), s.get("token").asText()), String.class);
        assertEquals(422, r2.getStatusCode().value(), "窗口外应422 " + r2.getBody());
        assertEquals("VI_PHONE_PROOF_REQUIRED", ViJson.tree(r2.getBody()).get("errorCode").asText());
    }

    @Test
    void 后台列表详情与部门范围投影() {
        String persona = "p05-list-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "lst" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        String admin = adminToken();
        ResponseEntity<String> r = rest.exchange(url("/api/admin/employee-links"), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, r.getStatusCode().value());
        JsonNode list = ViJson.tree(r.getBody()).get("data");
        assertTrue(list.toString().contains(Long.toString(emp)));
        JsonNode item = list.get(0);
        assertTrue(item.has("phoneMask") && item.has("openIdMask"));
        assertTrue(item.get("openIdMask").asText().contains("****"), "openId 脱敏不泄完整标识");
        long bindingId = Long.parseLong(item.get("bindingId").asText());
        ResponseEntity<String> d = rest.exchange(url("/api/admin/employee-links/" + bindingId), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, d.getStatusCode().value());
        assertEquals(Long.toString(emp), ViJson.tree(d.getBody()).get("data").get("sysUserId").asText());
    }

    @Test
    void 同号停用旧与启用新仅匹配有效员工_TC_R62_027() {
        String persona = "p05-mix-" + System.nanoTime();
        String phone = expectedPhone(persona);
        long old = seedEmployee(phone, "old" + System.nanoTime());
        jdbc.update("UPDATE sys_user SET status = '1' WHERE user_id = ?", old);
        long cur = seedEmployee(phone, "cur" + System.nanoTime());
        JsonNode s = login(persona);
        JsonNode r = bind(s, persona);
        assertEquals("LINKED", r.get("employeeLink").get("status").asText(), "仅按有效员工范围匹配");
        long accountId = Long.parseLong(s.get("accountId").asText());
        assertEquals(cur, jdbc.queryForObject("SELECT sys_user_id FROM vi_employee_binding WHERE account_id = ? AND revoked_at IS NULL",
                Long.class, accountId), "不匹配停用旧员工");
    }

    @Test
    void 目录改号后绑定不提交到已变化身份_TC_R62_028() {
        String persona = "p05-race-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "race" + System.nanoTime());
        JsonNode s = login(persona);
        assertEquals("LINKED", bind(s, persona).get("employeeLink").get("status").asText());
        long accountId = Long.parseLong(s.get("accountId").asText());
        String admin = adminToken();
        String newPhone = "199" + String.format("%08d", (System.nanoTime() % 100000000L));
        rest.exchange(url("/system/user"), HttpMethod.PUT,
                admin(Map.of("userId", emp, "phonenumber", newPhone, "nickName", "改号", "reason", "并发改号"), admin), String.class);
        // 改号后旧核验号在目录已无有效员工；再绑定不得把关联提交到已变化身份
        bind(s, persona);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding WHERE account_id = ? AND revoked_at IS NULL",
                Integer.class, accountId), "不产生第二条有效关联");
        assertEquals("SUSPENDED", jdbc.queryForObject("SELECT status FROM vi_employee_binding WHERE account_id = ? AND revoked_at IS NULL",
                String.class, accountId), "旧关联被钩子暂停而非转移到新号");
    }

    @Test
    void 本人自改手机号不成为可信目录值_TC_R62_036() {
        String persona = "p05-self-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "self" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        String before = jdbc.queryForObject("SELECT phonenumber FROM sys_user WHERE user_id = ?", String.class, emp);
        // MINI 令牌不能调用后台改号入口
        ResponseEntity<String> r = rest.exchange(url("/system/user"), HttpMethod.PUT,
                mini(Map.of("userId", emp, "phonenumber", "19900000000", "reason", "自改"), s.get("token").asText()), String.class);
        assertTrue(r.getStatusCode().value() == 401 || ViJson.tree(r.getBody()).path("code").asInt() == 401, "MINI 不获后台改号");
        // 个人资料自助修改不改变目录手机号
        rest.exchange(url("/system/user/profile"), HttpMethod.PUT,
                mini(Map.of("nickName", "本人", "phonenumber", "19900000000"), s.get("token").asText()), String.class);
        assertEquals(before, jdbc.queryForObject("SELECT phonenumber FROM sys_user WHERE user_id = ?", String.class, emp),
                "自改不成为自动关联可信目录值");
    }

    @Test
    void 后台改选对象暂停B不处理A_TC_R63_003() {
        String pa = "p05-objA-" + System.nanoTime();
        String pb = "p05-objB-" + System.nanoTime();
        long empA = seedEmployee(expectedPhone(pa), "oa" + System.nanoTime());
        long empB = seedEmployee(expectedPhone(pb), "ob" + System.nanoTime());
        JsonNode sa = login(pa);
        JsonNode sb = login(pb);
        bind(sa, pa);
        bind(sb, pb);
        long idA = bindingIdOf(Long.parseLong(sa.get("accountId").asText()));
        long idB = bindingIdOf(Long.parseLong(sb.get("accountId").asText()));
        String admin = adminToken();
        long verB = jdbc.queryForObject("SELECT version FROM vi_employee_binding WHERE id = ?", Long.class, idB);
        ResponseEntity<String> r = rest.exchange(url("/api/admin/employee-links/" + idB + "/suspend"), HttpMethod.POST,
                admin(Map.of("expectedVersion", verB, "reason", "暂停B"), admin), String.class);
        assertEquals(200, r.getStatusCode().value());
        assertEquals("SUSPENDED", jdbc.queryForObject("SELECT status FROM vi_employee_binding WHERE id = ?", String.class, idB));
        assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM vi_employee_binding WHERE id = ?", String.class, idA),
                "不得处理A");
    }
}