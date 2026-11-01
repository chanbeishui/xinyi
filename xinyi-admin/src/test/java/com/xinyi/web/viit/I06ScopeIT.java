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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I06 后台员工站点预配置与组合权限 真实集成测试（HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 微信为 SIMULATOR 替身；ADMIN 令牌经 TokenService 铸造（测试夹具）。覆盖：未登录先授权不建微信身份、
 * 先关联后补站点刷新可用、可STATION不可DRIVER、空集合版本/CAS冲突、清空失权车主保留、越权站点拒绝、
 * 配置对象不串员工、不存在/停用站点批量拒绝、MINI 获授权站点投影、列表分页。
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
class I06ScopeIT {

    private static Process simProcess;
    private static int simPort;
    private static String simSecret;
    private static Path simDb;
    private static final java.net.http.HttpClient SIM_HTTP = java.net.http.HttpClient.newHttpClient();
    private static final long S1 = 810000001L;
    private static final long S2 = 810000002L;

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
        registry.add("vi.crypto.data-key", () -> "i06-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "i06-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("i06-sim-", ".sqlite");
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

    private final Map<String, String> personaRuns = new HashMap<>();

    @BeforeEach
    void clean() {
        for (String t : List.of("vi_driver_assignment", "vi_driver_profile", "vi_phone_verification_receipt",
                "vi_employee_binding", "vi_session_token", "vi_session", "vi_account_security", "vi_account",
                "vi_station_grant", "vi_station_grant_set", "vi_owner_profile")) {
            jdbc.execute("DELETE FROM " + t);
        }
        jdbc.execute("DELETE FROM vi_station WHERE id IN (" + S1 + "," + S2 + ",810000003)");
        jdbc.execute("DELETE FROM sys_user_dept WHERE user_id IN (SELECT user_id FROM sys_user WHERE user_name LIKE 'i06emp\\_%')");
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'i06emp\\_%'");
        seedStation(S1, 1);
        seedStation(S2, 1);
    }

    private void seedStation(long id, int enabled) {
        jdbc.update("INSERT INTO vi_station (id, name, dept_id, address, lng, lat, enabled, supported_vehicle_codes, "
                + "business_hours, service_radius_meter, version) VALUES (?, ?, 100, '地址', 120.0, 30.0, ?, '[]', '[]', 5000, 0)",
                id, "检测站" + id, enabled);
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

    private JsonNode login(String persona) {
        String run = personaRuns.computeIfAbsent(persona, k -> newRun());
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", newCode(run, persona, "wechat")), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private JsonNode bind(JsonNode s, String persona) {
        long accountId = Long.parseLong(s.get("accountId").asText());
        long ver = jdbc.queryForObject("SELECT version FROM vi_account WHERE id = ?", Long.class, accountId);
        ResponseEntity<String> r = rest.exchange(url("/api/common/phone/bind"), HttpMethod.POST,
                mini(Map.of("phoneCode", newCode(newRun(), persona, "phone"), "expectedAccountVersion", ver,
                        "verificationRequestId", UUID.randomUUID().toString()), s.get("token").asText()), String.class);
        assertEquals(200, r.getStatusCode().value(), "绑定应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private long seedEmployee(String phone, String suffix) {
        String un = "i06emp_" + suffix;
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, phonenumber, status, del_flag, auth_version, password) "
                + "VALUES (?, ?, ?, '0', '0', 0, 'x')", un, "员工" + suffix, phone);
        return jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, un);
    }

    private JsonNode getGrant(String admin, long uid) {
        ResponseEntity<String> r = rest.exchange(url("/api/admin/station-grants/" + uid), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private ResponseEntity<String> putGrant(String admin, long uid, List<Long> ids, long expectedVersion) {
        Map<String, Object> body = new HashMap<>();
        body.put("authorizedStationIds", ids.stream().map(String::valueOf).toList());
        body.put("expectedVersion", expectedVersion);
        body.put("reason", "预配置测试");
        return rest.exchange(url("/api/admin/station-grants/" + uid), HttpMethod.PUT, admin(body, admin), String.class);
    }

    private JsonNode session(String token) {
        return ViJson.tree(rest.exchange(url("/api/common/session"), HttpMethod.GET, mini(null, token), String.class)
                .getBody()).get("data");
    }

    // ---------- 用例 ----------

    @Test
    void 未登录员工先授权不建微信身份且首登读到_TC_R62_016_R63_006() {
        String persona = "p06-pre-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "pre" + System.nanoTime());
        String admin = adminToken();
        JsonNode g0 = getGrant(admin, emp);
        assertEquals(0, g0.get("version").asLong());
        assertEquals(0, g0.get("authorizedStationIds").size());
        assertEquals("NOT_LOGGED_IN_OR_UNLINKED", g0.get("wechatLinkStatus").asText());
        ResponseEntity<String> r = putGrant(admin, emp, List.of(S1), 0);
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        assertEquals(1, ViJson.tree(r.getBody()).get("data").get("version").asLong());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_account", Integer.class), "不建伪造微信身份");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_employee_binding", Integer.class));
        // 之后首次登录+绑定自动读到授权
        JsonNode s = login(persona);
        bind(s, persona);
        assertTrue(session(s.get("token").asText()).get("availableRoles").toString().contains("STATION"), "首登读到预配置授权");
    }

    @Test
    void 先关联后补站点刷新可用无需重绑_TC_R62_017() {
        String persona = "p06-late-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "late" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        assertFalse(session(s.get("token").asText()).get("availableRoles").toString().contains("STATION"), "无站点先无STATION");
        String admin = adminToken();
        assertEquals(200, putGrant(admin, emp, List.of(S1), 0).getStatusCode().value());
        assertTrue(session(s.get("token").asText()).get("availableRoles").toString().contains("STATION"), "补站点后刷新可用");
    }

    @Test
    void 未过代驾审核可STATION不可DRIVER_TC_R62_006() {
        String persona = "p06-st-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "st" + System.nanoTime());
        String admin = adminToken();
        putGrant(admin, emp, List.of(S1), 0);
        JsonNode s = login(persona);
        bind(s, persona);
        JsonNode sess = session(s.get("token").asText());
        assertTrue(sess.get("availableRoles").toString().contains("STATION"));
        assertFalse(sess.get("availableRoles").toString().contains("DRIVER"), "员工关联不自动获DRIVER");
    }

    @Test
    void 空集合版本与CAS冲突_TC_R62_034_R63_013() {
        String persona = "p06-cas-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "cas" + System.nanoTime());
        String admin = adminToken();
        assertEquals(0, getGrant(admin, emp).get("version").asLong());
        assertEquals(200, putGrant(admin, emp, List.of(S1, S2), 0).getStatusCode().value());
        assertEquals(1, getGrant(admin, emp).get("version").asLong());
        assertEquals(200, putGrant(admin, emp, List.of(), 1).getStatusCode().value(), "空集合可保存");
        assertEquals(2, getGrant(admin, emp).get("version").asLong(), "空集合版本持续存在");
        ResponseEntity<String> stale = putGrant(admin, emp, List.of(S1), 1);
        assertEquals(409, stale.getStatusCode().value(), "旧版本CAS拒绝 " + stale.getBody());
        assertEquals(0, getGrant(admin, emp).get("authorizedStationIds").size(), "不覆盖真实集合");
    }

    @Test
    void 清空站点旧会话失权车主保留_TC_R63_007() {
        String persona = "p06-clear-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "clr" + System.nanoTime());
        JsonNode s = login(persona);
        bind(s, persona);
        String admin = adminToken();
        putGrant(admin, emp, List.of(S1, S2), 0);
        String token = s.get("token").asText();
        assertTrue(session(token).get("availableRoles").toString().contains("STATION"));
        long v = getGrant(admin, emp).get("version").asLong();
        assertEquals(200, putGrant(admin, emp, List.of(), v).getStatusCode().value());
        JsonNode sess = session(token);
        assertFalse(sess.get("availableRoles").toString().contains("STATION"), "清空后失权");
        assertTrue(sess.get("availableRoles").toString().contains("OWNER"), "车主保留");
        long ctx = sess.get("contextVersion").asLong();
        ResponseEntity<String> sw = rest.exchange(url("/api/common/session/station"), HttpMethod.POST,
                mini(Map.of("stationId", Long.toString(S1), "expectedContextVersion", ctx), token), String.class);
        assertEquals(403, sw.getStatusCode().value(), "旧会话写入已撤权站点被拒");
        assertTrue(getGrant(admin, emp).get("version").asLong() > 0, "空集合版本存在");
    }

    @Test
    void 越权站点切换拒绝_TC_R62_023() {
        String persona = "p06-cross-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "cr" + System.nanoTime());
        String admin = adminToken();
        putGrant(admin, emp, List.of(S1), 0);
        JsonNode s = login(persona);
        bind(s, persona);
        String token = s.get("token").asText();
        long ctx = session(token).get("contextVersion").asLong();
        ResponseEntity<String> bad = rest.exchange(url("/api/common/session/station"), HttpMethod.POST,
                mini(Map.of("stationId", Long.toString(S2), "expectedContextVersion", ctx), token), String.class);
        assertEquals(403, bad.getStatusCode().value(), "篡改无授权站点拒绝");
        ResponseEntity<String> ok = rest.exchange(url("/api/common/session/station"), HttpMethod.POST,
                mini(Map.of("stationId", Long.toString(S1), "expectedContextVersion", ctx), token), String.class);
        assertEquals(200, ok.getStatusCode().value(), "授权站点可切");
    }

    @Test
    void 配置对象不串员工_TC_R63_002() {
        long empA = seedEmployee(expectedPhone("p06-a-" + System.nanoTime()), "a" + System.nanoTime());
        long empB = seedEmployee(expectedPhone("p06-b-" + System.nanoTime()), "b" + System.nanoTime());
        String admin = adminToken();
        putGrant(admin, empA, List.of(S1), 0);
        putGrant(admin, empB, List.of(S2), 0);
        JsonNode ga = getGrant(admin, empA);
        JsonNode gb = getGrant(admin, empB);
        assertTrue(ga.get("authorizedStationIds").toString().contains(Long.toString(S1)));
        assertFalse(ga.get("authorizedStationIds").toString().contains(Long.toString(S2)), "A关系不变");
        assertTrue(gb.get("authorizedStationIds").toString().contains(Long.toString(S2)));
        assertEquals(1, ga.get("version").asLong());
        assertEquals(1, gb.get("version").asLong());
    }

    @Test
    void 不存在或停用站点批量拒绝无部分写入() {
        long emp = seedEmployee(expectedPhone("p06-bad-" + System.nanoTime()), "bad" + System.nanoTime());
        String admin = adminToken();
        ResponseEntity<String> r1 = putGrant(admin, emp, List.of(S1, 819999999L), 0);
        assertEquals(422, r1.getStatusCode().value(), "不存在站点整体拒绝");
        seedStation(810000003L, 0);
        ResponseEntity<String> r2 = putGrant(admin, emp, List.of(S1, 810000003L), 0);
        assertEquals(422, r2.getStatusCode().value(), "停用站点整体拒绝");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_station_grant WHERE sys_user_id = ?", Integer.class, emp),
                "无部分写入");
    }

    @Test
    void MINI获授权站点投影_API094() {
        String persona = "p06-auth-" + System.nanoTime();
        long emp = seedEmployee(expectedPhone(persona), "au" + System.nanoTime());
        String admin = adminToken();
        putGrant(admin, emp, List.of(S1), 0);
        JsonNode s = login(persona);
        bind(s, persona);
        ResponseEntity<String> r = rest.exchange(url("/api/app/station/authorized-stations"), HttpMethod.GET,
                mini(null, s.get("token").asText()), String.class);
        assertEquals(200, r.getStatusCode().value());
        JsonNode list = ViJson.tree(r.getBody()).get("data");
        assertEquals(1, list.size());
        assertEquals(Long.toString(S1), list.get(0).get("stationId").asText());
        assertTrue(list.get(0).has("name"));
        // 无绑定账号返回空
        JsonNode other = login("p06-nolink-" + System.nanoTime());
        ResponseEntity<String> r2 = rest.exchange(url("/api/app/station/authorized-stations"), HttpMethod.GET,
                mini(null, other.get("token").asText()), String.class);
        assertEquals(0, ViJson.tree(r2.getBody()).get("data").size());
    }

    @Test
    void 后台列表分页与微信状态投影_API131() {
        long emp = seedEmployee(expectedPhone("p06-list-" + System.nanoTime()), "lst" + System.nanoTime());
        String admin = adminToken();
        putGrant(admin, emp, List.of(S1), 0);
        ResponseEntity<String> r = rest.exchange(url("/api/admin/station-grants?pageNum=1&pageSize=50"), HttpMethod.GET,
                admin(null, admin), String.class);
        assertEquals(200, r.getStatusCode().value());
        JsonNode list = ViJson.tree(r.getBody()).get("data");
        boolean found = false;
        for (JsonNode n : list) {
            if (n.get("employeeUserId").asText().equals(Long.toString(emp))) {
                found = true;
                assertEquals("NOT_LOGGED_IN_OR_UNLINKED", n.get("wechatLinkStatus").asText());
            }
        }
        assertTrue(found, "列表含该员工");
    }
}