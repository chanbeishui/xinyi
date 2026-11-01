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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C02 检测点/营业/服务区域 真实集成测试（HTTP+双安全链+MyBatis+隔离库 MySQL+provider 模拟器）。
 * 覆盖：公开营业列表仅启用+脱敏、ADMIN 创建与契约校验、ADMIN 列表数据范围、更新 CAS+启停、
 * 迁部门在手订单受控拒绝、员工本站点读取/更新（真实绑定→授权→切站）、员工无站点上下文拒绝+MINI 不获 ADMIN。
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
class C02StationIT {

    private static Process simProcess;
    private static int simPort;
    private static String simSecret;
    private static Path simDb;
    private static final java.net.http.HttpClient SIM_HTTP = java.net.http.HttpClient.newHttpClient();
    private static final AtomicLong SEQ = new AtomicLong(System.nanoTime());
    private static final long DEPT_A = 990101L;
    private static final long DEPT_B = 990102L;

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
        registry.add("vi.crypto.data-key", () -> "c02-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "c02-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("c02-sim-", ".sqlite");
        Files.deleteIfExists(simDb);
        String python = System.getenv().getOrDefault("VI_PYTHON", "python3");
        Path sim = repoRoot().resolve("docs/vehicle/V6.4/13_真实集成测试/provider_simulator/simulator.py");
        ProcessBuilder pb = new ProcessBuilder(python, sim.toString(), "--host", "127.0.0.1", "--port", "0", "--db", simDb.toString());
        pb.environment().put("VI_SIM_SECRET", simSecret);
        pb.redirectErrorStream(true);
        simProcess = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(simProcess.getInputStream(), StandardCharsets.UTF_8));
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
        assertEquals("xinyi_vi_it_e07", jdbc.queryForObject("SELECT DATABASE()", String.class), "只允许在专属测试库清理夹具");
        jdbc.execute("DELETE FROM vi_idempotency");
        jdbc.execute("DELETE FROM vi_event_log");
        jdbc.execute("DELETE FROM vi_outbox");
        jdbc.execute("DELETE FROM sys_user_role WHERE role_id = 990201");
        jdbc.execute("DELETE FROM sys_role_menu WHERE role_id = 990201");
        jdbc.execute("DELETE FROM sys_role WHERE role_id = 990201");
        jdbc.execute("DELETE FROM sys_menu WHERE menu_id IN (990301, 990302)");
        jdbc.execute("INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag) VALUES (990201, 'C02测试', 'c02-work', 1, '3', '0', '0')");
        jdbc.execute("INSERT INTO sys_menu (menu_id, menu_name, menu_type, status, perms) VALUES (990301, 'C02读取', 'F', '0', 'biz:station:query'), (990302, 'C02编辑', 'F', '0', 'biz:station:edit')");
        jdbc.execute("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (990201,990301),(990201,990302)");
        jdbc.execute("DELETE FROM vi_order");
        jdbc.execute("DELETE FROM vi_station_grant");
        jdbc.execute("DELETE FROM vi_station");
        jdbc.execute("DELETE FROM vi_employee_binding");
        jdbc.execute("DELETE FROM vi_session_token");
        jdbc.execute("DELETE FROM vi_session");
        jdbc.execute("DELETE FROM vi_account_security");
        jdbc.execute("DELETE FROM vi_account");
        jdbc.execute("DELETE FROM sys_user_dept WHERE user_id IN (SELECT user_id FROM sys_user WHERE user_name LIKE 'c02emp\\_%' OR user_name LIKE 'c02scoped\\_%')");
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'c02emp\\_%' OR user_name LIKE 'c02scoped\\_%'");
        jdbc.execute("DELETE FROM sys_dept WHERE dept_id IN (990101,990102)");
        for (long d : List.of(DEPT_A, DEPT_B)) {
            jdbc.update("INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, status, del_flag) VALUES (?, 0, '0', ?, 0, '0', '0')", d, "公司" + d);
        }
    }

    // ---------- 模拟器 / HTTP ----------

    private Map<String, Object> simPost(String path, Map<String, Object> body) {
        try {
            String json = ViJson.write(body);
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + simPort + path))
                    .header("Content-Type", "application/json").header("X-Sim-Secret", simSecret)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
            java.net.http.HttpResponse<String> resp = SIM_HTTP.send(req, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, resp.statusCode(), "模拟器 " + path + " 应200 " + resp.body());
            return ViJson.read(resp.body(), Map.class);
        } catch (java.io.IOException | InterruptedException e) {
            throw new IllegalStateException("模拟器调用失败 " + path, e);
        }
    }

    private String newRun() {
        return (String) simPost("/control/runs", Map.of()).get("runId");
    }

    private String newCode(String run, String persona, String kind) {
        return (String) simPost("/control/codes", Map.of("runId", run, "persona", persona, "kind", kind)).get("code");
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

    private HttpEntity<Object> station(Object body, String token, String idem) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("X-Client-Type", "station");
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        if (idem != null) {
            h.set("Idempotency-Key", idem);
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

    private HttpEntity<Object> adminNoIdem(Object body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        h.set("Authorization", "Bearer " + token);
        h.set("X-Client-Type", "admin");
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

    private String scopedAdminToken(long deptId, String... perms) {
        String userName = "c02scoped_" + SEQ.incrementAndGet();
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, status, del_flag, auth_version, password) VALUES (?, ?, '0', '0', 0, 'x')", userName, "受限" + deptId);
        long userId = jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, userName);
        jdbc.update("INSERT INTO sys_user_dept (user_id, dept_id) VALUES (?, ?)", userId, deptId);
        SysUser u = userService.selectUserByUserName(userName);
        SysRole role = new SysRole();
        role.setRoleId(SEQ.incrementAndGet());
        role.setRoleKey("c02scoped");
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
        String code = newCode(newRun(), persona, "wechat");
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST, mini(Map.of("code", code), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    private String expectedPhone(String persona) {
        try {
            byte[] h = java.security.MessageDigest.getInstance("SHA-256").digest(persona.getBytes(StandardCharsets.UTF_8));
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

    private long seedEmployee(String phone, String suffix) {
        String userName = "c02emp_" + suffix;
        jdbc.update("INSERT INTO sys_user (user_name, nick_name, phonenumber, status, del_flag, auth_version, password) VALUES (?, ?, ?, '0', '0', 0, 'x')", userName, "员工" + suffix, phone);
        long id = jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name = ?", Long.class, userName);
        jdbc.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, 990201)", id);
        jdbc.update("INSERT INTO sys_user_dept (user_id, dept_id) VALUES (?, ?)", id, DEPT_A);
        return id;
    }

    private void seedGrant(long sysUserId, long stationId) {
        jdbc.update("INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason) VALUES (?, ?, 1, 1, 'C02 测试授权')", sysUserId, stationId);
    }

    private long seedStation(long deptId, boolean enabled, String name) {
        long id = SEQ.incrementAndGet();
        jdbc.update("INSERT INTO vi_station (id, name, dept_id, address, lng, lat, service_radius_meter, supported_vehicle_codes, "
                        + "business_hours, enabled, contact_phone_cipher, qualification_file_ids, version) "
                        + "VALUES (?, ?, ?, '公开地址', 120.0, 30.0, 5000, '[\"SMALL_CAR\"]', '\"09:00-18:00\"', ?, ?, '[\"9001\"]', 0)",
                id, name, deptId, enabled ? 1 : 0, crypto.encrypt("13800001111"));
        return id;
    }

    private void seedActiveOrder(long stationId, long deptId) {
        long q = SEQ.incrementAndGet();
        jdbc.update("INSERT INTO vi_order (order_no, owner_account_id, station_id, dept_id, quote_id, vehicle_snapshot, "
                        + "fulfillment_status, custody_type, custody_ref_id, pickup_starts_at, pickup_ends_at, pay_expires_at, total_fen, terms_version) "
                        + "VALUES (?, 1, ?, ?, ?, '{}', 'SCHEDULED', 'OWNER', 0, UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), UTC_TIMESTAMP(3), 100, 'v1')",
                "C02" + q, stationId, deptId, q);
    }

    private Map<String, Object> createBody(String name) {
        Map<String, Object> body = new java.util.HashMap<>(Map.of("name", name, "address", "地址X", "location", Map.of("lng", 120.1, "lat", 30.2, "coordinateSystem", "GCJ02"),
                "contactPhone", "13800002222", "enabled", true, "supportedVehicleTypeCodes", List.of("SMALL_CAR", "SUV"),
                "businessHours", "周一至周日 08:00-20:00", "serviceRadiusMeter", 8000, "qualificationFileIds", List.of("9100")));
        body.put("deptId", Long.toString(DEPT_A));
        return body;
    }

    private String employeeStationToken(String persona, long stationId) {
        JsonNode s = login(persona);
        String token = s.get("token").asText();
        String pcode = newCode(newRun(), persona, "phone");
        ResponseEntity<String> bind = rest.exchange(url("/api/common/phone/bind"), HttpMethod.POST,
                mini(Map.of("phoneCode", pcode, "expectedAccountVersion", s.get("accountVersion").asLong(),
                        "verificationRequestId", UUID.randomUUID().toString()), token), String.class);
        assertEquals(200, bind.getStatusCode().value(), bind.getBody());
        assertEquals("LINKED", ViJson.tree(bind.getBody()).get("data").get("employeeLink").get("status").asText());
        JsonNode sess = ViJson.tree(rest.exchange(url("/api/common/session"), HttpMethod.GET, mini(null, token), String.class).getBody()).get("data");
        ResponseEntity<String> sw = rest.exchange(url("/api/common/session/station"), HttpMethod.POST,
                mini(Map.of("stationId", Long.toString(stationId), "expectedContextVersion", sess.get("contextVersion").asLong()), token), String.class);
        assertEquals(200, sw.getStatusCode().value(), sw.getBody());
        JsonNode swd = ViJson.tree(sw.getBody()).get("data");
        assertEquals("STATION", swd.get("activeRole").asText());
        String stToken = swd.has("token") ? swd.get("token").asText() : token;
        return stToken;
    }

    private JsonNode rowsOf(ResponseEntity<String> r) {
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        return ViJson.tree(r.getBody()).get("rows");
    }

    // ---------- 用例 ----------

    @Test
    void 公开营业列表仅启用站点且脱敏() {
        long en = seedStation(DEPT_A, true, "C02启用站");
        seedStation(DEPT_A, false, "C02停用站");
        String token = login("c02-pub-" + System.nanoTime()).get("token").asText();
        ResponseEntity<String> r = rest.exchange(url("/api/common/stations"), HttpMethod.GET, mini(null, token), String.class);
        JsonNode rows = rowsOf(r);
        boolean foundEnabled = false;
        for (JsonNode n : rows) {
            assertFalse(n.has("version"), "公开视图不含版本");
            assertFalse(n.has("enabled"), "公开视图不含内部启用状态");
            assertFalse(n.toString().contains("13800001111"), "不泄完整电话");
            if (n.get("stationId").asText().equals(String.valueOf(en))) {
                foundEnabled = true;
                assertTrue(n.get("phoneMask").asText().contains("****"), "电话脱敏");
                assertEquals("GCJ02", n.get("location").get("coordinateSystem").asText());
                assertEquals("C02启用站", n.get("name").asText());
            }
        }
        assertTrue(foundEnabled, "启用站点在公开列表");
        for (JsonNode n : rows) {
            assertFalse(n.get("name").asText().equals("C02停用站"), "停用站点不公开");
        }
    }

    @Test
    void ADMIN创建站点与契约校验() {
        String admin = adminToken();
        ResponseEntity<String> ok = rest.exchange(url("/api/admin/stations"), HttpMethod.POST, admin(createBody("C02新站"), admin), String.class);
        assertEquals(200, ok.getStatusCode().value(), ok.getBody());
        JsonNode d = ViJson.tree(ok.getBody()).get("data");
        assertEquals(0, d.get("version").asLong());
        assertTrue(d.get("phoneMask").asText().contains("****"), "创建后电话脱敏");
        assertFalse(d.toString().contains("13800002222"), "不泄完整电话");
        assertEquals("GCJ02", d.get("location").get("coordinateSystem").asText());
        assertTrue(d.get("enabled").asBoolean());
        assertEquals(2, d.get("supportedVehicleTypeCodes").size());
        // 缺 Idempotency-Key
        assertEquals(400, rest.exchange(url("/api/admin/stations"), HttpMethod.POST, adminNoIdem(createBody("C02无幂等"), admin), String.class).getStatusCode().value());
        // 缺 name
        Map<String, Object> noName = createBody("x");
        noName = new java.util.HashMap<>(noName);
        noName.remove("name");
        assertEquals(400, rest.exchange(url("/api/admin/stations"), HttpMethod.POST, admin(noName, admin), String.class).getStatusCode().value());
        // 坐标系非法
        Map<String, Object> badLoc = new java.util.HashMap<>(createBody("x"));
        badLoc.put("location", Map.of("lng", 120.0, "lat", 30.0, "coordinateSystem", "WGS84"));
        assertEquals(400, rest.exchange(url("/api/admin/stations"), HttpMethod.POST, admin(badLoc, admin), String.class).getStatusCode().value());
    }

    @Test
    void ADMIN列表按部门数据范围() {
        long sa = seedStation(DEPT_A, true, "C02范围A");
        seedStation(DEPT_B, true, "C02范围B");
        String admin = adminToken();
        JsonNode all = rowsOf(rest.exchange(url("/api/admin/stations"), HttpMethod.GET, admin(null, admin), String.class));
        boolean seeA = false, seeB = false;
        for (JsonNode n : all) {
            if (n.get("name").asText().equals("C02范围A")) seeA = true;
            if (n.get("name").asText().equals("C02范围B")) seeB = true;
        }
        assertTrue(seeA && seeB, "超管全量可见");
        String scopedA = scopedAdminToken(DEPT_A, "biz:station:list");
        JsonNode onlyA = rowsOf(rest.exchange(url("/api/admin/stations"), HttpMethod.GET, admin(null, scopedA), String.class));
        boolean a = false, b = false;
        for (JsonNode n : onlyA) {
            if (n.get("name").asText().equals("C02范围A")) a = true;
            if (n.get("name").asText().equals("C02范围B")) b = true;
        }
        assertTrue(a, "受限管理员见本部门站点");
        assertFalse(b, "受限管理员不见他部门站点");
        assertTrue(onlyA.toString().contains(String.valueOf(sa)));
    }

    @Test
    void ADMIN更新CAS与启停() {
        long id = seedStation(DEPT_A, true, "C02更新站");
        String admin = adminToken();
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", "C02更新站");
        body.put("address", "新地址");
        body.put("location", Map.of("lng", 120.5, "lat", 30.5, "coordinateSystem", "GCJ02"));
        body.put("supportedVehicleTypeCodes", List.of("SMALL_CAR"));
        body.put("enabled", false);
        body.put("expectedVersion", 0);
        ResponseEntity<String> ok = rest.exchange(url("/api/admin/stations/" + id), HttpMethod.PUT, admin(body, admin), String.class);
        assertEquals(200, ok.getStatusCode().value(), ok.getBody());
        JsonNode d = ViJson.tree(ok.getBody()).get("data");
        assertEquals(1, d.get("version").asLong());
        assertFalse(d.get("enabled").asBoolean(), "停用生效（B17-disable）");
        assertEquals("新地址", d.get("address").asText());
        // 旧版本 CAS 409
        assertEquals(409, rest.exchange(url("/api/admin/stations/" + id), HttpMethod.PUT, admin(body, admin), String.class).getStatusCode().value());
    }

    @Test
    void 迁部门存在在手订单受控拒绝() {
        long id = seedStation(DEPT_A, true, "C02迁部门站");
        seedActiveOrder(id, DEPT_A);
        String admin = adminToken();
        Map<String, Object> move = new java.util.HashMap<>();
        move.put("name", "C02迁部门站");
        move.put("address", "公开地址");
        move.put("location", Map.of("lng", 120.0, "lat", 30.0, "coordinateSystem", "GCJ02"));
        move.put("supportedVehicleTypeCodes", List.of("SMALL_CAR"));
        move.put("expectedVersion", 0);
        move.put("deptId", DEPT_B);
        ResponseEntity<String> r = rest.exchange(url("/api/admin/stations/" + id), HttpMethod.PUT, admin(move, admin), String.class);
        assertEquals(409, r.getStatusCode().value(), "在手订单迁部门应受控拒绝 " + r.getBody());
        assertEquals("VI_STATE_CONFLICT", ViJson.tree(r.getBody()).get("errorCode").asText());
        // 订单办结后允许迁部门
        jdbc.execute("UPDATE vi_order SET fulfillment_status='COMPLETED' WHERE station_id=" + id);
        move.put("expectedVersion", 0);
        ResponseEntity<String> ok = rest.exchange(url("/api/admin/stations/" + id), HttpMethod.PUT, admin(move, admin), String.class);
        assertEquals(200, ok.getStatusCode().value(), ok.getBody());
        assertEquals(DEPT_B, jdbc.queryForObject("SELECT dept_id FROM vi_station WHERE id=" + id, Long.class).longValue());
    }

    @Test
    void 员工本站点读取与更新() {
        long stationId = seedStation(DEPT_A, true, "C02员工站");
        String persona = "c02-emp-" + System.nanoTime();
        long sysUserId = seedEmployee(expectedPhone(persona), "e" + System.nanoTime());
        seedGrant(sysUserId, stationId);
        String stToken = employeeStationToken(persona, stationId);
        // 读取本站点
        ResponseEntity<String> info = rest.exchange(url("/api/app/station/info"), HttpMethod.GET, station(null, stToken, null), String.class);
        assertEquals(200, info.getStatusCode().value(), info.getBody());
        JsonNode st = ViJson.tree(info.getBody()).get("data");
        assertEquals(Long.toString(stationId), st.get("stationId").asText());
        long v0 = st.get("version").asLong();
        // 更新本站点（不含 deptId）
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", "C02员工站改");
        body.put("address", "员工改地址");
        body.put("location", Map.of("lng", 120.0, "lat", 30.0, "coordinateSystem", "GCJ02"));
        body.put("supportedVehicleTypeCodes", List.of("SMALL_CAR"));
        body.put("expectedVersion", v0);
        ResponseEntity<String> put = rest.exchange(url("/api/app/station/info"), HttpMethod.PUT, station(body, stToken, UUID.randomUUID().toString()), String.class);
        assertEquals(200, put.getStatusCode().value(), put.getBody());
        assertEquals(v0 + 1, ViJson.tree(put.getBody()).get("data").get("version").asLong());
        assertEquals("C02员工站改", ViJson.tree(put.getBody()).get("data").get("name").asText());
        // 员工端不接受 deptId 迁移
        body.put("deptId", Long.toString(DEPT_B));
        body.put("expectedVersion", v0 + 1);
        assertEquals(400, rest.exchange(url("/api/app/station/info"), HttpMethod.PUT, station(body, stToken, UUID.randomUUID().toString()), String.class).getStatusCode().value());
    }

    @Test
    void 员工无站点上下文拒绝且MINI不获ADMIN() {
        String token = login("c02-noctx-" + System.nanoTime()).get("token").asText();
        assertEquals(403, rest.exchange(url("/api/app/station/info"), HttpMethod.GET, mini(null, token), String.class).getStatusCode().value(),
                "OWNER 无站点上下文不能读员工站点");
        ResponseEntity<String> r = rest.exchange(url("/api/admin/stations"), HttpMethod.GET, mini(null, token), String.class);
        JsonNode body = ViJson.tree(r.getBody());
        int sc = r.getStatusCode().value();
        int bc = body.path("code").asInt();
        assertFalse(sc == 200 && bc == 200 && body.has("rows"), "MINI 不得获得 ADMIN 站点列表");
        assertTrue(sc == 401 || sc == 403 || bc == 401 || bc == 403, "MINI 不获 ADMIN，status=" + sc + " body=" + r.getBody());
    }
    @Test
    void 同键创建重放且不同请求拒绝() {
        String admin = adminToken();
        String key = UUID.randomUUID().toString();
        Map<String, Object> body = createBody("C02幂等创建");
        HttpEntity<Object> request = admin(body, admin);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        headers.set("Idempotency-Key", key);
        ResponseEntity<String> first = rest.exchange(url("/api/admin/stations"), HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        ResponseEntity<String> repeat = rest.exchange(url("/api/admin/stations"), HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        assertEquals(200, first.getStatusCode().value(), first.getBody());
        assertEquals(ViJson.tree(first.getBody()), ViJson.tree(repeat.getBody()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_station WHERE name='C02幂等创建'", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='STATION_CREATE'", Integer.class));
        Map<String, Object> changed = new java.util.HashMap<>(body);
        changed.put("name", "另一个名字");
        ResponseEntity<String> conflict = rest.exchange(url("/api/admin/stations"), HttpMethod.POST, new HttpEntity<>(changed, headers), String.class);
        assertEquals(409, conflict.getStatusCode().value());
        assertTrue(conflict.getBody().contains("VI_IDEMPOTENCY_CONFLICT"));
    }

    @Test
    void 创建必须明确归属且目标部门越权拒绝() {
        String admin = adminToken();
        Map<String, Object> body = new java.util.HashMap<>(createBody("C02无归属"));
        body.remove("deptId");
        assertEquals(400, rest.exchange(url("/api/admin/stations"), HttpMethod.POST, admin(body, admin), String.class).getStatusCode().value());
        String limited = scopedAdminToken(DEPT_A, "biz:station:create");
        body.put("deptId", Long.toString(DEPT_B));
        assertEquals(403, rest.exchange(url("/api/admin/stations"), HttpMethod.POST, admin(body, limited), String.class).getStatusCode().value());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_station WHERE name='C02无归属'", Integer.class));
    }

    @Test
    void 站点授权或功能权限撤销旧工作令牌立即失权() {
        long id = seedStation(DEPT_A, true, "C02实时授权");
        String persona = "c02-revoke-" + System.nanoTime();
        long employee = seedEmployee(expectedPhone(persona), "revoke" + System.nanoTime());
        seedGrant(employee, id);
        String token = employeeStationToken(persona, id);
        assertEquals(200, rest.exchange(url("/api/app/station/info"), HttpMethod.GET, station(null, token, null), String.class).getStatusCode().value());
        jdbc.execute("DELETE FROM sys_role_menu WHERE role_id=990201 AND menu_id=990302");
        Map<String, Object> body = new java.util.HashMap<>(createBody("不应写入"));
        body.remove("deptId");
        body.put("expectedVersion", 0);
        assertEquals(403, rest.exchange(url("/api/app/station/info"), HttpMethod.PUT, station(body, token, UUID.randomUUID().toString()), String.class).getStatusCode().value());
        jdbc.update("UPDATE vi_station_grant SET enabled=0 WHERE sys_user_id=?", employee);
        assertEquals(403, rest.exchange(url("/api/app/station/info"), HttpMethod.GET, station(null, token, null), String.class).getStatusCode().value());
        assertEquals("C02实时授权", jdbc.queryForObject("SELECT name FROM vi_station WHERE id=?", String.class, id));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_type='VI_STATION'", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='STATION_ACCESS_DENIED'", Integer.class));
        assertEquals(200, rest.exchange(url("/api/common/session"), HttpMethod.GET, mini(null, token), String.class).getStatusCode().value(), "保留基础账号查询及刷新能力");
    }

    @Test
    void 员工停用或权威部门超范围拒绝旧令牌() {
        long id = seedStation(DEPT_B, true, "C02跨部门");
        String persona = "c02-dept-" + System.nanoTime();
        long employee = seedEmployee(expectedPhone(persona), "dept" + System.nanoTime());
        seedGrant(employee, id);
        String token = employeeStationToken(persona, id);
        assertEquals(403, rest.exchange(url("/api/app/station/info"), HttpMethod.GET, station(null, token, null), String.class).getStatusCode().value());
        jdbc.update("INSERT INTO sys_user_dept (user_id, dept_id) VALUES (?, ?)", employee, DEPT_B);
        assertEquals(200, rest.exchange(url("/api/app/station/info"), HttpMethod.GET, station(null, token, null), String.class).getStatusCode().value());
        jdbc.update("UPDATE sys_user SET status='1' WHERE user_id=?", employee);
        assertEquals(403, rest.exchange(url("/api/app/station/info"), HttpMethod.GET, station(null, token, null), String.class).getStatusCode().value());
    }

    @Test
    void 部门迁移必须覆盖原部门和目标部门() {
        long original = seedStation(DEPT_A, true, "C02迁移保护");
        Map<String, Object> body = new java.util.HashMap<>(createBody("C02迁移保护"));
        body.put("expectedVersion", 0);
        body.put("deptId", Long.toString(DEPT_B));
        String onlyOriginal = scopedAdminToken(DEPT_A, "biz:station:edit");
        assertEquals(403, rest.exchange(url("/api/admin/stations/" + original), HttpMethod.PUT, admin(body, onlyOriginal), String.class).getStatusCode().value());
        String onlyTarget = scopedAdminToken(DEPT_B, "biz:station:edit");
        assertEquals(404, rest.exchange(url("/api/admin/stations/" + original), HttpMethod.PUT, admin(body, onlyTarget), String.class).getStatusCode().value());
        assertEquals(DEPT_A, jdbc.queryForObject("SELECT dept_id FROM vi_station WHERE id=?", Long.class, original));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_idempotency", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_type='VI_STATION'", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='STATION_UPDATE_REJECTED'", Integer.class));
    }

    @Test
    void 并发重复创建只提交一次() throws Exception {
        String token = adminToken();
        HttpEntity<Object> request = admin(createBody("C02并发创建"), token);
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        try {
            java.util.concurrent.Callable<ResponseEntity<String>> call = () -> {
                start.await();
                return rest.exchange(url("/api/admin/stations"), HttpMethod.POST, request, String.class);
            };
            var a = pool.submit(call);
            var b = pool.submit(call);
            start.countDown();
            var first = a.get(10, java.util.concurrent.TimeUnit.SECONDS);
            var second = b.get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(200, first.getStatusCode().value(), first.getBody());
            assertEquals(200, second.getStatusCode().value(), second.getBody());
            assertEquals(ViJson.tree(first.getBody()), ViJson.tree(second.getBody()));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_station WHERE name='C02并发创建'", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='STATION_CREATE'", Integer.class));
        } finally {
            pool.shutdownNow();
        }
    }

}