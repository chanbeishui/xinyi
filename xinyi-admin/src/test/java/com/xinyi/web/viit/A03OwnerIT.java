package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.framework.web.service.TokenService;
import com.xinyi.rongli.identity.service.PhoneCrypto;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A03 车主档案管理 真实集成测试（HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 覆盖：列表/详情脱敏与档案投影、维护仅运营字段+CAS+手机不可改、跨公司历史不因一条订单暴露（部门数据范围）、
 * 无订单车主受限不可见 fail-closed、关联车辆/订单投影、MINI 不获 ADMIN、非超管无功能权限 403。
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
class A03OwnerIT {

    private static Process simProcess;
    private static int simPort;
    private static String simSecret;
    private static Path simDb;
    private static final java.net.http.HttpClient SIM_HTTP = java.net.http.HttpClient.newHttpClient();
    private static final AtomicLong SEQ = new AtomicLong(System.nanoTime());
    private static final long DEPT_A = 990001L;
    private static final long DEPT_B = 990002L;
    private static final long DEPT_C = 990003L;

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
        registry.add("vi.crypto.data-key", () -> "a03-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "a03-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("a03-sim-", ".sqlite");
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
    PhoneCrypto crypto;

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
        jdbc.execute("DELETE FROM vi_owner_profile");
        jdbc.execute("DELETE FROM vi_payment_order");
        jdbc.execute("DELETE FROM vi_order");
        jdbc.execute("DELETE FROM vi_vehicle");
        jdbc.execute("DELETE FROM vi_station WHERE dept_id IN (990001,990002,990003)");
        jdbc.execute("DELETE FROM vi_session_token");
        jdbc.execute("DELETE FROM vi_session");
        jdbc.execute("DELETE FROM vi_account_security");
        jdbc.execute("DELETE FROM vi_account");
        jdbc.execute("DELETE FROM sys_user_dept WHERE user_id IN (SELECT user_id FROM sys_user WHERE user_name LIKE 'a03scoped\\_%')");
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'a03scoped\\_%'");
        jdbc.execute("DELETE FROM sys_dept WHERE dept_id IN (990001,990002,990003)");
        for (long d : List.of(DEPT_A, DEPT_B, DEPT_C)) {
            jdbc.update("INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, status, del_flag) "
                    + "VALUES (?, 0, '0', ?, 0, '0', '0')", d, "公司" + d);
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
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        return new HttpEntity<>(body, h);
    }

    private HttpEntity<Object> admin(Object body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("Authorization", "Bearer " + token);
        h.set("X-Client-Type", "admin");
        h.set("Idempotency-Key", UUID.randomUUID().toString());
        return new HttpEntity<>(body, h);
    }

    private String adminToken() {
        SysUser admin = userService.selectUserByUserName("admin");
        Long av = jdbc.queryForObject("SELECT auth_version FROM sys_user WHERE user_id = ?", Long.class, admin.getUserId());
        LoginUser lu = new LoginUser(admin, Set.of("*:*:*"));
        lu.setUserId(admin.getUserId());
        lu.setAuthVersion(av);
        return mintToken(lu);
    }

    /** 非超管受限管理员：真实 sys_user + 任职部门 + 角色 dataScope=本部门（3）；perms 为功能权限集合。 */
    private String scopedAdminToken(long deptId, String... perms) {
        String userName = "a03scoped_" + SEQ.incrementAndGet();
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, status, del_flag, auth_version, password) "
                + "VALUES (?, ?, '0', '0', 0, 'x')", userName, "受限" + deptId);
        long userId = jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, userName);
        jdbc.update("INSERT INTO sys_user_dept (user_id, dept_id) VALUES (?, ?)", userId, deptId);
        SysUser u = userService.selectUserByUserName(userName);
        SysRole role = new SysRole();
        role.setRoleId(SEQ.incrementAndGet());
        role.setRoleKey("a03scoped");
        role.setDataScope("3");
        role.setStatus("0");
        role.setPermissions(new java.util.HashSet<>(List.of(perms)));
        u.setRoles(List.of(role));
        LoginUser lu = new LoginUser(u, new java.util.HashSet<>(List.of(perms)));
        lu.setUserId(userId);
        lu.setAuthVersion(0L);
        return mintToken(lu);
    }

    private String mintToken(LoginUser lu) {
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

    private JsonNode login(String persona) {
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code = (String) simPost("/control/codes", Map.of("runId", run, "persona", persona, "kind", "wechat")).get("code");
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", code), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private long seedStation(long deptId) {
        long id = SEQ.incrementAndGet();
        jdbc.update("INSERT INTO vi_station (id, name, dept_id, address, lng, lat, enabled, supported_vehicle_codes, "
                + "business_hours, service_radius_meter, version) VALUES (?, ?, ?, '公开地址', 120.0, 30.0, 1, '[]', '[]', 5000, 0)",
                id, "检测站" + id, deptId);
        return id;
    }

    private long seedProfile(long accountId, String remark) {
        jdbc.update("INSERT INTO vi_owner_profile (account_id, service_name, service_flag, internal_remark, version) "
                + "VALUES (?, '称呼', 'NORMAL', ?, 0)", accountId, remark);
        return jdbc.queryForObject("SELECT id FROM vi_owner_profile WHERE account_id = ?", Long.class, accountId);
    }

    /** 模拟认证服务已核验手机：写入密文与核验时刻（运营维护不可覆盖）。 */
    private void seedPhone(long accountId, String phone) {
        jdbc.update("UPDATE vi_account SET phone_cipher = ?, phone_verified_at = UTC_TIMESTAMP(3) WHERE id = ?",
                crypto.encrypt(phone), accountId);
    }

    private long seedVehicle(long accountId, String plate) {
        jdbc.update("INSERT INTO vi_vehicle (owner_account_id, plate_no, vehicle_type_code, brand_model, vin_last4, "
                + "verification_status, is_default, version) VALUES (?, ?, 'SMALL_CAR', '品牌', '1234', 'VERIFIED', 0, 0)",
                accountId, plate);
        return jdbc.queryForObject("SELECT id FROM vi_vehicle WHERE owner_account_id = ? AND plate_no = ?", Long.class, accountId, plate);
    }

    private long seedOrder(long accountId, long vehicleId, long stationId, long deptId, String plate) {
        long q = SEQ.incrementAndGet();
        String snapshot = "{\"vehicleId\":\"" + vehicleId + "\",\"plateNo\":\"" + plate + "\",\"vehicleTypeCode\":\"SMALL_CAR\","
                + "\"vehicleTypeLabel\":\"小型车\",\"brandModel\":\"品牌\",\"vinLast4\":\"1234\"}";
        jdbc.update("INSERT INTO vi_order (order_no, owner_account_id, station_id, dept_id, quote_id, vehicle_snapshot, "
                + "fulfillment_status, custody_type, custody_ref_id, pickup_starts_at, pickup_ends_at, pay_expires_at, total_fen, terms_version, vehicle_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'SCHEDULED', 'OWNER', 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 100, 'v1', ?)",
                "A03" + q, accountId, stationId, deptId, q, snapshot, vehicleId);
        return jdbc.queryForObject("SELECT id FROM vi_order WHERE quote_id = ?", Long.class, q);
    }

    private JsonNode rows(ResponseEntity<String> r) {
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        return ViJson.tree(r.getBody()).get("rows");
    }

    // ---------- 用例 ----------

    @Test
    void 列表详情脱敏与档案投影() {
        JsonNode s = login("a03-list-" + System.nanoTime());
        long accountId = s.get("accountId").asLong();
        seedPhone(accountId, "13812345678");
        long profileId = seedProfile(accountId, "内部备注X");
        long station = seedStation(DEPT_A);
        long vehicle = seedVehicle(accountId, "浙A12345");
        seedOrder(accountId, vehicle, station, DEPT_A, "浙A12345");
        String admin = adminToken();
        ResponseEntity<String> lr = rest.exchange(url("/api/admin/owners"), HttpMethod.GET, admin(null, admin), String.class);
        JsonNode list = rows(lr);
        assertTrue(list.size() >= 1);
        JsonNode o = null;
        for (JsonNode n : list) {
            if (n.get("ownerId").asText().equals(String.valueOf(profileId))) {
                o = n;
            }
        }
        assertNotNull(o, "列表含本车主");
        assertTrue(o.get("phoneMask").asText().contains("****"), "手机号脱敏");
        assertTrue(o.get("phoneVerified").asBoolean(), "已核验手机");
        assertFalse(o.toString().contains("13812345678"), "不泄完整手机号");
        assertFalse(o.toString().contains("phoneCipher"), "不泄密文");
        assertEquals(1, o.get("vehicleCount").asLong());
        assertEquals(1, o.get("orderCount").asLong());
        assertEquals("NORMAL", o.get("serviceFlag").asText());
        assertNotNull(o.get("lastServiceAt").asText());
        ResponseEntity<String> dr = rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, dr.getStatusCode().value());
        JsonNode d = ViJson.tree(dr.getBody()).get("data");
        assertEquals("内部备注X", d.get("internalRemark").asText());
        assertEquals(0, d.get("version").asLong());
        assertTrue(d.has("phoneVerified"));
    }

    @Test
    void 维护仅运营字段CAS与手机不可改() {
        JsonNode s = login("a03-maint-" + System.nanoTime());
        long accountId = s.get("accountId").asLong();
        seedPhone(accountId, "13900000001");
        long profileId = seedProfile(accountId, "原备注");
        long station = seedStation(DEPT_A);
        long vehicle = seedVehicle(accountId, "浙B00001");
        seedOrder(accountId, vehicle, station, DEPT_A, "浙B00001");
        String admin = adminToken();
        ResponseEntity<String> ok = rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.PATCH,
                admin(Map.of("expectedVersion", 0, "serviceName", "新称呼", "serviceFlag", "VERIFY_AGAIN",
                        "internalRemark", "新备注", "reason", "维护"), admin), String.class);
        assertEquals(200, ok.getStatusCode().value(), ok.getBody());
        JsonNode d = ViJson.tree(ok.getBody()).get("data");
        assertEquals("新称呼", d.get("serviceName").asText());
        assertEquals("VERIFY_AGAIN", d.get("serviceFlag").asText());
        assertEquals(1, d.get("version").asLong());
        // 手机号/核验结果不可由运营维护覆盖（未知字段拒绝）
        assertEquals(400, rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.PATCH,
                admin(Map.of("expectedVersion", 1, "phoneVerified", false, "reason", "改核验"), admin), String.class).getStatusCode().value());
        assertEquals(400, rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.PATCH,
                admin(Map.of("expectedVersion", 1, "phone", "13800000000", "reason", "改手机"), admin), String.class).getStatusCode().value());
        // 旧版本 CAS 409
        assertEquals(409, rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.PATCH,
                admin(Map.of("expectedVersion", 0, "serviceName", "X", "reason", "旧版本"), admin), String.class).getStatusCode().value());
        // 核验状态不因维护改变（登录账户手机核验时刻仍由认证服务维护）
        assertEquals(d.get("phoneVerified").asBoolean(),
                ViJson.tree(rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.GET, admin(null, admin), String.class)
                        .getBody()).get("data").get("phoneVerified").asBoolean());
    }

    @Test
    void 跨公司历史不因一条订单暴露() {
        JsonNode s = login("a03-cross-" + System.nanoTime());
        long accountId = s.get("accountId").asLong();
        long profileId = seedProfile(accountId, "跨公司");
        long stationA = seedStation(DEPT_A);
        long stationB = seedStation(DEPT_B);
        long vehicle = seedVehicle(accountId, "浙C00002");
        long orderA = seedOrder(accountId, vehicle, stationA, DEPT_A, "浙C00002");
        long orderB = seedOrder(accountId, vehicle, stationB, DEPT_B, "浙C00002");
        String adminA = scopedAdminToken(DEPT_A, "biz:owner:list", "biz:owner:query");
        ResponseEntity<String> ra = rest.exchange(url("/api/admin/owners/" + profileId + "/orders"), HttpMethod.GET, admin(null, adminA), String.class);
        JsonNode rowsA = rows(ra);
        assertEquals(1, rowsA.size(), "A公司只见本公司订单");
        assertEquals(orderNoOf(orderA), rowsA.get(0).get("orderNo").asText());
        assertEquals("ADMIN", rowsA.get(0).get("view").asText());
        assertEquals("浙C00002", rowsA.get(0).get("vehicle").get("plateNo").asText());
        assertTrue(rowsA.get(0).get("slotStartAt").asText().endsWith("Z"));
        assertNotNull(rowsA.get(0).get("station").get("stationId"));
        // A公司详情可见（有本公司订单），orderCount 仅本公司
        ResponseEntity<String> da = rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.GET, admin(null, adminA), String.class);
        assertEquals(200, da.getStatusCode().value());
        assertEquals(1, ViJson.tree(da.getBody()).get("data").get("orderCount").asLong());
        // B公司只见B订单
        String adminB = scopedAdminToken(DEPT_B, "biz:owner:list", "biz:owner:query");
        JsonNode rowsB = rows(rest.exchange(url("/api/admin/owners/" + profileId + "/orders"), HttpMethod.GET, admin(null, adminB), String.class));
        assertEquals(1, rowsB.size());
        assertEquals(orderNoOf(orderB), rowsB.get(0).get("orderNo").asText());
        // C公司无订单 → 详情不可见404，列表不含
        String adminC = scopedAdminToken(DEPT_C, "biz:owner:list", "biz:owner:query");
        assertEquals(404, rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.GET, admin(null, adminC), String.class).getStatusCode().value());
        // 超管全量见两条
        String admin = adminToken();
        JsonNode all = rows(rest.exchange(url("/api/admin/owners/" + profileId + "/orders"), HttpMethod.GET, admin(null, admin), String.class));
        assertEquals(2, all.size());
    }

    private String orderNoOf(long orderId) {
        return jdbc.queryForObject("SELECT order_no FROM vi_order WHERE id = ?", String.class, orderId);
    }

    @Test
    void 无订单车主受限不可见fail_closed() {
        JsonNode s = login("a03-noorder-" + System.nanoTime());
        long accountId = s.get("accountId").asLong();
        long profileId = seedProfile(accountId, "无订单");
        String adminA = scopedAdminToken(DEPT_A, "biz:owner:list", "biz:owner:query");
        assertEquals(404, rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.GET, admin(null, adminA), String.class).getStatusCode().value());
        JsonNode list = rows(rest.exchange(url("/api/admin/owners"), HttpMethod.GET, admin(null, adminA), String.class));
        for (JsonNode n : list) {
            assertFalse(n.get("ownerId").asText().equals(String.valueOf(profileId)), "受限列表不含无订单车主");
        }
        // 超管全量可见
        String admin = adminToken();
        assertEquals(200, rest.exchange(url("/api/admin/owners/" + profileId), HttpMethod.GET, admin(null, admin), String.class).getStatusCode().value());
    }

    @Test
    void 关联车辆投影与分页信封() {
        JsonNode s = login("a03-veh-" + System.nanoTime());
        long accountId = s.get("accountId").asLong();
        long profileId = seedProfile(accountId, "车辆");
        long station = seedStation(DEPT_A);
        long vehicle = seedVehicle(accountId, "浙D00003");
        seedOrder(accountId, vehicle, station, DEPT_A, "浙D00003");
        String admin = adminToken();
        ResponseEntity<String> vr = rest.exchange(url("/api/admin/owners/" + profileId + "/vehicles"), HttpMethod.GET, admin(null, admin), String.class);
        assertEquals(200, vr.getStatusCode().value());
        JsonNode body = ViJson.tree(vr.getBody());
        assertEquals(1, body.get("total").asLong());
        JsonNode v = body.get("rows").get(0);
        assertEquals("浙D00003", v.get("plateNo").asText());
        assertEquals("****1234", v.get("vinMask").asText());
        assertEquals(String.valueOf(vehicle), v.get("vehicleId").asText());
        assertTrue(v.has("version"));
    }

    @Test
    void MINI令牌不获ADMIN() {
        JsonNode s = login("a03-mini-" + System.nanoTime());
        String miniToken = s.get("token").asText();
        ResponseEntity<String> r = rest.exchange(url("/api/admin/owners"), HttpMethod.GET, mini(null, miniToken), String.class);
        JsonNode body = ViJson.tree(r.getBody());
        int sc = r.getStatusCode().value();
        int bc = body.path("code").asInt();
        assertFalse(sc == 200 && bc == 200 && body.has("rows"), "MINI 不得获得 ADMIN 车主列表: " + r.getBody());
        assertTrue(sc == 401 || sc == 403 || bc == 401 || bc == 403,
                "MINI 令牌不获 ADMIN，status=" + sc + " body=" + r.getBody());
    }

    @Test
    void 非超管无功能权限拒绝() {
        JsonNode s = login("a03-noperm-" + System.nanoTime());
        long accountId = s.get("accountId").asLong();
        long profileId = seedProfile(accountId, "无权限");
        long station = seedStation(DEPT_A);
        long vehicle = seedVehicle(accountId, "浙E00004");
        seedOrder(accountId, vehicle, station, DEPT_A, "浙E00004");
        // 有部门数据范围但无 biz:owner:list 功能权限
        String noPerm = scopedAdminToken(DEPT_A);
        assertEquals(403, rest.exchange(url("/api/admin/owners"), HttpMethod.GET, admin(null, noPerm), String.class).getStatusCode().value());
    }
}