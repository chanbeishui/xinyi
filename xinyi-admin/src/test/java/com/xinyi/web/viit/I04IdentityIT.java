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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * I04 角色与站点上下文切换 真实集成测试（HTTP+Security(MINI链)+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 仅微信渠道为 SIMULATOR 替身（L01 另验）。覆盖：
 * OWNER→DRIVER 切换成功+旧上下文令牌失效、待审切代驾拒绝且刷新不审批（TC-R63-010/016）、已批准我的入口（TC-R63-009）、
 * 员工站点同源一致切 STATION（TC-R63-001，真实手机号绑定）、越权站点/0授权拒绝（S22）、切换不释放 assignment（TC-R62-024）、
 * 旧检测权限失效仍可切回 OWNER、上下文版本冲突 409、非法字段/角色拒绝、切换仅影响本设备。
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
class I04IdentityIT {

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
        registry.add("vi.crypto.data-key", () -> "i04-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "i04-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("i04-sim-", ".sqlite");
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
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'i04emp\\_%'");
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

    private HttpEntity<Object> entity(Object body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("X-Client-Type", "owner");
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        return new HttpEntity<>(body, h);
    }

    private JsonNode login(String runId, String persona) {
        String code = newCode(runId, persona, "wechat");
        ResponseEntity<String> resp = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                entity(Map.of("code", code), null), String.class);
        assertEquals(200, resp.getStatusCode().value(), "登录应200 " + resp.getBody());
        return ViJson.tree(resp.getBody()).get("data");
    }

    private JsonNode login(String persona) {
        return login(newRun(), persona);
    }

    private ResponseEntity<String> session(String token) {
        return rest.exchange(url("/api/common/session"), HttpMethod.GET, entity(null, token), String.class);
    }

    private ResponseEntity<String> profile(String token) {
        return rest.exchange(url("/api/common/profile"), HttpMethod.GET, entity(null, token), String.class);
    }

    private ResponseEntity<String> switchRole(String token, String role, long ctx, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        body.put("role", role);
        body.put("expectedContextVersion", ctx);
        if (extra != null) {
            body.putAll(extra);
        }
        return rest.exchange(url("/api/common/session/role"), HttpMethod.POST, entity(body, token), String.class);
    }

    private ResponseEntity<String> switchStation(String token, String stationId, long ctx) {
        return rest.exchange(url("/api/common/session/station"), HttpMethod.POST,
                entity(Map.of("stationId", stationId, "expectedContextVersion", ctx), token), String.class);
    }

    private void seedDriverProfile(long accountId, String status) {
        jdbc.update("INSERT INTO vi_driver_profile (account_id, qualification_status, license_long_term, "
                + "new_assignment_enabled, listening_enabled) VALUES (?, ?, 1, 1, 1)", accountId, status);
    }

    private long seedActiveAssignment(long accountId) {
        long synthetic = 900000000L + (accountId % 1000000);
        jdbc.update("INSERT INTO vi_driver_assignment (order_id, driver_id, invite_id, accepted_at) "
                + "VALUES (?, ?, ?, UTC_TIMESTAMP(3))", synthetic, accountId, synthetic);
        return jdbc.queryForObject("SELECT id FROM vi_driver_assignment WHERE driver_id = ? AND ended_at IS NULL",
                Long.class, accountId);
    }

    private long seedEmployee(String phone, String suffix) {
        String userName = "i04emp_" + suffix;
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, phonenumber, status, del_flag, auth_version, password) "
                + "VALUES (?, ?, ?, '0', '0', 0, 'x')", userName, "员工" + suffix, phone);
        return jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, userName);
    }

    private long seedGrant(long sysUserId, long stationId) {
        jdbc.update("INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason) "
                + "VALUES (?, ?, 1, 1, 'I04 测试授权')", sysUserId, stationId);
        return stationId;
    }

    /** 真实手机号绑定流程：seed 员工(+站点)→登录→phone/bind→LINKED。返回登录后会话。 */
    private JsonNode linkedEmployee(String persona, boolean withGrant) {
        String phone = expectedPhone(persona);
        long sysUserId = seedEmployee(phone, "link" + System.nanoTime());
        if (withGrant) {
            seedGrant(sysUserId, 800000000L + (sysUserId % 100000));
        }
        JsonNode s = login(persona);
        String token = s.get("token").asText();
        long ver = s.get("accountVersion").asLong();
        String code = newCode(newRun(), persona, "phone");
        ResponseEntity<String> bind = rest.exchange(url("/api/common/phone/bind"), HttpMethod.POST,
                entity(Map.of("phoneCode", code, "expectedAccountVersion", ver,
                        "verificationRequestId", UUID.randomUUID().toString()), token), String.class);
        assertEquals(200, bind.getStatusCode().value(), "绑定应200 " + bind.getBody());
        assertEquals("LINKED", ViJson.tree(bind.getBody()).get("data").get("employeeLink").get("status").asText());
        return s;
    }

    private long grantedStationOf(long accountId) {
        Long sysUserId = jdbc.queryForObject("SELECT sys_user_id FROM vi_employee_binding WHERE account_id = ? "
                + "AND revoked_at IS NULL LIMIT 1", Long.class, accountId);
        return jdbc.queryForObject("SELECT station_id FROM vi_station_grant WHERE sys_user_id = ? AND enabled = 1 "
                + "ORDER BY station_id LIMIT 1", Long.class, sysUserId);
    }

    // ---------- 用例 ----------

    @Test
    void 切换OWNER到DRIVER成功且旧上下文令牌失效() {
        JsonNode s = login("p04-role-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        long ctx = s.get("contextVersion").asLong();
        seedDriverProfile(accountId, "APPROVED");

        ResponseEntity<String> r = switchRole(token, "DRIVER", ctx, null);
        assertEquals(200, r.getStatusCode().value(), "切换应200 " + r.getBody());
        JsonNode d = ViJson.tree(r.getBody()).get("data");
        assertEquals("DRIVER", d.get("activeRole").asText());
        assertEquals(ctx + 1, d.get("contextVersion").asLong(), "上下文原子递增");
        assertTrue(d.get("availableRoles").toString().contains("DRIVER"));
        assertFalse(d.has("refreshToken"), "切换不返回刷新令牌");
        String newToken = d.get("token").asText();
        assertNotEquals(token, newToken, "签发新访问令牌");
        // 旧上下文令牌失效，新令牌可用
        assertEquals(401, session(token).getStatusCode().value(), "旧上下文访问令牌须失效");
        ResponseEntity<String> now = session(newToken);
        assertEquals(200, now.getStatusCode().value());
        assertEquals("DRIVER", ViJson.tree(now.getBody()).get("data").get("activeRole").asText());
    }

    @Test
    void 待审账号切代驾拒绝且刷新不审批_TC_R63_010_R63_016() {
        JsonNode s = login("p04-pending-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        long ctx = s.get("contextVersion").asLong();
        seedDriverProfile(accountId, "SUBMITTED");

        assertFalse(ViJson.tree(session(token).getBody()).get("data").get("availableRoles").toString().contains("DRIVER"),
                "待审无DRIVER");
        ResponseEntity<String> r = switchRole(token, "DRIVER", ctx, null);
        assertEquals(403, r.getStatusCode().value(), "待审切代驾应403拒绝 " + r.getBody());
        assertEquals("VI_FORBIDDEN", ViJson.tree(r.getBody()).get("errorCode").asText());
        // 刷新不会审批：仍无 DRIVER
        String refresh = s.get("refreshToken").asText();
        ResponseEntity<String> refreshed = rest.exchange(url("/api/common/token/refresh"), HttpMethod.POST,
                entity(Map.of("refreshToken", refresh), null), String.class);
        assertEquals(200, refreshed.getStatusCode().value());
        assertFalse(ViJson.tree(refreshed.getBody()).get("data").get("availableRoles").toString().contains("DRIVER"),
                "刷新不改变审核状态");
        assertEquals("SUBMITTED", jdbc.queryForObject("SELECT qualification_status FROM vi_driver_profile WHERE account_id = ?",
                String.class, accountId), "刷新不得审批资格");
    }

    @Test
    void 已批准代驾我的入口显示资格不重复引导_TC_R63_009() {
        JsonNode s = login("p04-approved-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        seedDriverProfile(accountId, "APPROVED");
        JsonNode d = ViJson.tree(session(token).getBody()).get("data");
        assertEquals("APPROVED", d.get("driverQualification").asText(), "显示已批准资格（非首次申请引导）");
        assertTrue(d.get("availableRoles").toString().contains("DRIVER"), "可切换代驾身份");
        assertEquals(200, switchRole(token, "DRIVER", d.get("contextVersion").asLong(), null).getStatusCode().value());
    }

    @Test
    void 员工站点同源一致切STATION_TC_R63_001() {
        String persona = "p04-staff-" + System.nanoTime();
        JsonNode s = linkedEmployee(persona, true);
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        long station = grantedStationOf(accountId);

        JsonNode sess = ViJson.tree(session(token).getBody()).get("data");
        JsonNode prof = ViJson.tree(profile(token).getBody()).get("data");
        assertTrue(sess.get("phoneBound").asBoolean(), "手机号已核验");
        assertFalse(prof.get("phoneMask").isNull(), "档案脱敏手机号非空（已核验同源）");
        assertEquals("LINKED", sess.get("employeeLink").get("status").asText());
        assertEquals("LINKED", prof.get("employeeLink").get("status").asText(), "关联页与会话同源");
        assertTrue(sess.get("availableRoles").toString().contains("STATION"));
        assertEquals(sess.get("availableRoles").toString(), prof.get("availableRoles").toString(), "会话与档案可用角色一致");
        assertTrue(sess.get("stationIds").toString().contains(Long.toString(station)), "可用站点一致");

        long ctx = sess.get("contextVersion").asLong();
        ResponseEntity<String> r = switchStation(token, Long.toString(station), ctx);
        assertEquals(200, r.getStatusCode().value(), "切换授权站点应200 " + r.getBody());
        JsonNode d = ViJson.tree(r.getBody()).get("data");
        assertEquals("STATION", d.get("activeRole").asText());
        assertEquals(Long.toString(station), d.get("stationId").asText());
        assertEquals(ctx + 1, d.get("contextVersion").asLong());
        assertEquals("LINKED", d.get("employeeLink").get("status").asText(), "切换后关联状态一致");
    }

    @Test
    void 越权站点与0授权拒绝_S22() {
        // 有授权但切到未授权站点 → 403
        String persona = "p04-cross-" + System.nanoTime();
        JsonNode s = linkedEmployee(persona, true);
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        long ctx = ViJson.tree(session(token).getBody()).get("data").get("contextVersion").asLong();
        long granted = grantedStationOf(accountId);
        ResponseEntity<String> cross = switchStation(token, Long.toString(granted + 1), ctx);
        assertEquals(403, cross.getStatusCode().value(), "未授权站点应403 " + cross.getBody());

        // 0 授权：LINKED 但无 STATION、stationIds 空，切站点/切STATION均拒绝
        String persona0 = "p04-nogrant-" + System.nanoTime();
        JsonNode s0 = linkedEmployee(persona0, false);
        String token0 = s0.get("token").asText();
        JsonNode d0 = ViJson.tree(session(token0).getBody()).get("data");
        assertFalse(d0.get("availableRoles").toString().contains("STATION"), "0授权无STATION");
        assertEquals(0, d0.get("stationIds").size(), "0授权站点集合为空");
        assertTrue(d0.get("workAccessDisabled").asBoolean());
        long ctx0 = d0.get("contextVersion").asLong();
        assertEquals(403, switchStation(token0, Long.toString(granted), ctx0).getStatusCode().value(), "0授权切站点拒绝");
        assertEquals(403, switchRole(token0, "STATION", ctx0, null).getStatusCode().value(), "0授权切STATION拒绝");
        // 仍可切回 OWNER（安全视角）
        assertEquals(200, switchRole(token0, "OWNER", ctx0, null).getStatusCode().value(), "0授权仍可OWNER");
    }

    @Test
    void 切换与退出均不释放assignment_TC_R62_024() {
        JsonNode s = login("p04-assign-" + System.nanoTime());
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        seedDriverProfile(accountId, "APPROVED");
        long assignmentId = seedActiveAssignment(accountId);
        long ctx = s.get("contextVersion").asLong();

        ResponseEntity<String> toDriver = switchRole(token, "DRIVER", ctx, null);
        assertEquals(200, toDriver.getStatusCode().value());
        String driverToken = ViJson.tree(toDriver.getBody()).get("data").get("token").asText();
        long ctx2 = ViJson.tree(toDriver.getBody()).get("data").get("contextVersion").asLong();
        assertNull(jdbc.queryForObject("SELECT ended_at FROM vi_driver_assignment WHERE id = ?",
                java.sql.Timestamp.class, assignmentId), "切DRIVER不释放承接");

        ResponseEntity<String> toOwner = switchRole(driverToken, "OWNER", ctx2, null);
        assertEquals(200, toOwner.getStatusCode().value());
        assertNull(jdbc.queryForObject("SELECT ended_at FROM vi_driver_assignment WHERE id = ?",
                java.sql.Timestamp.class, assignmentId), "切回OWNER不释放承接");
        assertEquals("APPROVED", jdbc.queryForObject("SELECT qualification_status FROM vi_driver_profile WHERE account_id = ?",
                String.class, accountId), "切换不改资格");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_driver_assignment WHERE id = ? AND ended_at IS NULL",
                Integer.class, assignmentId), "在手承接占用始终保留");
    }

    @Test
    void 旧检测权限失效仍可切回OWNER() {
        String persona = "p04-revoke-" + System.nanoTime();
        JsonNode s = linkedEmployee(persona, true);
        String token = s.get("token").asText();
        long accountId = Long.parseLong(s.get("accountId").asText());
        long station = grantedStationOf(accountId);
        long ctx = ViJson.tree(session(token).getBody()).get("data").get("contextVersion").asLong();
        ResponseEntity<String> toStation = switchStation(token, Long.toString(station), ctx);
        assertEquals(200, toStation.getStatusCode().value());
        String stationToken = ViJson.tree(toStation.getBody()).get("data").get("token").asText();
        long ctx2 = ViJson.tree(toStation.getBody()).get("data").get("contextVersion").asLong();

        // 后台撤销站点授权 → 旧检测权限失效
        jdbc.update("UPDATE vi_station_grant SET enabled = 0 WHERE station_id = ? AND sys_user_id = "
                + "(SELECT sys_user_id FROM vi_employee_binding WHERE account_id = ? LIMIT 1)", station, accountId);
        JsonNode now = ViJson.tree(session(stationToken).getBody()).get("data");
        assertFalse(now.get("availableRoles").toString().contains("STATION"), "撤权后无STATION");
        assertTrue(now.get("workAccessDisabled").asBoolean());
        // 基础会话仍可读，且可切回 OWNER
        ResponseEntity<String> back = switchRole(stationToken, "OWNER", ctx2, null);
        assertEquals(200, back.getStatusCode().value(), "失权仍可切回OWNER " + back.getBody());
        assertEquals("OWNER", ViJson.tree(back.getBody()).get("data").get("activeRole").asText());
        // 再切 STATION 已不可用
        long ctx3 = ViJson.tree(back.getBody()).get("data").get("contextVersion").asLong();
        String ownerToken = ViJson.tree(back.getBody()).get("data").get("token").asText();
        assertEquals(403, switchRole(ownerToken, "STATION", ctx3, null).getStatusCode().value(), "失权后不可再切STATION");
    }

    @Test
    void 上下文版本冲突返回409() {
        JsonNode s = login("p04-conflict-" + System.nanoTime());
        String token = s.get("token").asText();
        long ctx = s.get("contextVersion").asLong();
        ResponseEntity<String> bad = switchRole(token, "OWNER", ctx + 99, null);
        assertEquals(409, bad.getStatusCode().value(), "错误上下文版本应409 " + bad.getBody());
        assertEquals("VI_CONTEXT_CHANGED", ViJson.tree(bad.getBody()).get("errorCode").asText());
        // 成功切换后重发旧上下文 → 409
        ResponseEntity<String> ok = switchRole(token, "OWNER", ctx, null);
        assertEquals(200, ok.getStatusCode().value());
        String newToken = ViJson.tree(ok.getBody()).get("data").get("token").asText();
        ResponseEntity<String> stale = switchRole(newToken, "OWNER", ctx, null);
        assertEquals(409, stale.getStatusCode().value(), "重发旧上下文应409");
    }

    @Test
    void 切换请求非法字段角色与缺客户端类型拒绝() {
        JsonNode s = login("p04-bad-" + System.nanoTime());
        String token = s.get("token").asText();
        long ctx = s.get("contextVersion").asLong();
        assertEquals(400, switchRole(token, "OWNER", ctx, Map.of("accountId", "1")).getStatusCode().value(), "未知字段400");
        assertEquals(400, switchRole(token, "ADMIN", ctx, null).getStatusCode().value(), "非法角色400");
        assertEquals(400, switchStation(token, "abc", ctx).getStatusCode().value(), "非法stationId 400");
        assertEquals(400, switchRole(token, "OWNER", -1, null).getStatusCode().value(), "负上下文版本400");
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("Authorization", "Bearer " + token);
        ResponseEntity<String> noClient = rest.exchange(url("/api/common/session/role"), HttpMethod.POST,
                new HttpEntity<>(Map.of("role", "OWNER", "expectedContextVersion", ctx), h), String.class);
        assertEquals(400, noClient.getStatusCode().value(), "缺X-Client-Type 400");
    }

    @Test
    void 切换仅影响本设备其他会话不受影响() {
        String run = newRun();
        String persona = "p04-multi-" + System.nanoTime();
        JsonNode a = login(run, persona);
        JsonNode b = login(run, persona);
        String tokenA = a.get("token").asText();
        String tokenB = b.get("token").asText();
        long accountId = Long.parseLong(a.get("accountId").asText());
        seedDriverProfile(accountId, "APPROVED");
        long ctxA = a.get("contextVersion").asLong();

        ResponseEntity<String> r = switchRole(tokenA, "DRIVER", ctxA, null);
        assertEquals(200, r.getStatusCode().value());
        assertEquals(401, session(tokenA).getStatusCode().value(), "本设备旧令牌失效");
        // 另一设备会话不受影响
        ResponseEntity<String> other = session(tokenB);
        assertEquals(200, other.getStatusCode().value(), "其他设备会话不受影响");
        JsonNode od = ViJson.tree(other.getBody()).get("data");
        assertEquals("OWNER", od.get("activeRole").asText(), "其他设备视角不变");
        assertEquals(ctxA, od.get("contextVersion").asLong(), "其他设备上下文版本不变");
    }
}