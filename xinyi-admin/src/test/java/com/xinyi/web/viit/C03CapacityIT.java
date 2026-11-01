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
 * C03 每站双额度真实 HTTP/MySQL 回归；外部微信仅由显式模拟器提供。
 * 覆盖创建与幂等、占用下限、影响预览、停约、部门及站点越权、撤权、时间校验和并发CAS。
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
class C03CapacityIT {

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
        jdbc.execute("DELETE FROM sys_menu WHERE menu_id IN (990301, 990302, 990303, 990304)");
        jdbc.execute("INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag) VALUES (990201, 'C02测试', 'c02-work', 1, '3', '0', '0')");
        jdbc.execute("INSERT INTO sys_menu (menu_id, menu_name, menu_type, status, perms) VALUES (990301, 'C02读取', 'F', '0', 'biz:station:query'), (990302, 'C02编辑', 'F', '0', 'biz:station:edit'), (990303, 'C03读取', 'F', '0', 'biz:capacity:list'), (990304, 'C03修改', 'F', '0', 'biz:capacity:edit')");
        jdbc.execute("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (990201,990301),(990201,990302),(990201,990303),(990201,990304)");
        jdbc.execute("DELETE FROM vi_quota_slot");
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


    private Map<String, Object> slotInput(long stationId, String type) {
        java.time.Instant start = java.time.Instant.now().plusSeconds(7200).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        return new java.util.HashMap<>(Map.of("stationId", Long.toString(stationId), "quotaType", type,
                "startAt", start.toString(), "endAt", start.plusSeconds(3600).toString(), "total", 10, "reason", "测试排期"));
    }
    private JsonNode createSlot(Map<String, Object> body, String token) {
        ResponseEntity<String> r = rest.exchange(url("/api/admin/capacity"), HttpMethod.POST, admin(body, token), String.class);
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }
    private Map<String,Object> changeInput(int total, long version) {
        return new java.util.HashMap<>(Map.of("total", total, "expectedVersion", version, "reason", "调整测试"));
    }

    @Test
    void 每站接车与接收额度独立且创建幂等() {
        long a = seedStation(DEPT_A,true,"C03甲站"), b = seedStation(DEPT_B,true,"C03乙站");
        String admin = adminToken();
        Map<String,Object> body = slotInput(a,"PICKUP");
        HttpEntity<Object> request = admin(body,admin);
        ResponseEntity<String> first = rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,request,String.class);
        assertEquals(200, first.getStatusCode().value(),first.getBody());
        ResponseEntity<String> repeat = rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,request,String.class);
        assertEquals(ViJson.tree(first.getBody()),ViJson.tree(repeat.getBody()));
        Map<String,Object> receiving = new java.util.HashMap<>(body); receiving.put("quotaType","STATION_RECEIVE");
        createSlot(receiving,admin);
        Map<String,Object> other = new java.util.HashMap<>(body);other.put("stationId",Long.toString(b));
        createSlot(other,admin);
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quota_slot",Integer.class));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quota_slot WHERE station_id=?",Integer.class,a));
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='CAPACITY_CREATE'",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT SUM(held_quota+confirmed_quota+consumed_quota) FROM vi_quota_slot",Integer.class));
    }

    @Test
    void 影响预览不写库且不能缩容低于实际占用() {
        long station = seedStation(DEPT_A,true,"C03占用");String admin=adminToken();
        JsonNode created=createSlot(slotInput(station,"PICKUP"),admin);String id=created.get("slotId").asText();
        jdbc.update("UPDATE vi_quota_slot SET held_quota=2,confirmed_quota=2,consumed_quota=1 WHERE id=?",id);
        ResponseEntity<String> preview=rest.exchange(url("/api/admin/capacity?slotId="+id+"&proposedTotal=4"),HttpMethod.GET,admin(null,admin),String.class);
        JsonNode row=rowsOf(preview).get(0);assertFalse(row.get("canApply").asBoolean());
        assertEquals(0,row.get("version").asLong());assertEquals(5,row.get("available").asInt());
        assertEquals(409,rest.exchange(url("/api/admin/capacity/"+id),HttpMethod.PUT,admin(changeInput(4,0),admin),String.class).getStatusCode().value());
        assertEquals(10,jdbc.queryForObject("SELECT total_quota FROM vi_quota_slot WHERE id=?",Integer.class,id));
        Map<String,Object> valid=changeInput(5,0);valid.put("enabled",false);
        HttpEntity<Object> request=admin(valid,admin);
        ResponseEntity<String> changed=rest.exchange(url("/api/admin/capacity/"+id),HttpMethod.PUT,request,String.class);
        assertEquals(200,changed.getStatusCode().value(),changed.getBody());
        JsonNode result=ViJson.tree(changed.getBody()).get("data");
        assertEquals(0,result.get("available").asInt());assertEquals(2,result.get("held").asInt());assertEquals(1,result.get("consumed").asInt());
        assertEquals(ViJson.tree(changed.getBody()),ViJson.tree(rest.exchange(url("/api/admin/capacity/"+id),HttpMethod.PUT,request,String.class).getBody()));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='CAPACITY_CHANGE'",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='CAPACITY_REJECTED'",Integer.class));
    }

    @Test
    void 伪造部门归属或修改越权时段整体拒绝() {
        long a=seedStation(DEPT_A,true,"C03范围甲"), b=seedStation(DEPT_B,true,"C03范围乙");String admin=adminToken();
        createSlot(slotInput(a,"PICKUP"),admin);JsonNode hidden=createSlot(slotInput(b,"PICKUP"),admin);
        String limited=scopedAdminToken(DEPT_A,"biz:capacity:list","biz:capacity:edit");
        JsonNode rows=rowsOf(rest.exchange(url("/api/admin/capacity"),HttpMethod.GET,admin(null,limited),String.class));
        assertEquals(1,rows.size());assertEquals(Long.toString(a),rows.get(0).get("stationId").asText());
        String id=hidden.get("slotId").asText();
        assertEquals(404,rest.exchange(url("/api/admin/capacity/"+id),HttpMethod.PUT,admin(changeInput(20,0),limited),String.class).getStatusCode().value());
        assertEquals(404,rest.exchange(url("/api/admin/capacity?slotId="+id+"&proposedTotal=20"),HttpMethod.GET,admin(null,limited),String.class).getStatusCode().value());
        assertEquals(404,rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,admin(slotInput(b,"STATION_RECEIVE"),limited),String.class).getStatusCode().value());
        assertEquals(400,rest.exchange(url("/api/admin/capacity?params.dataScope=1"),HttpMethod.GET,admin(null,limited),String.class).getStatusCode().value());
        assertEquals(10,jdbc.queryForObject("SELECT total_quota FROM vi_quota_slot WHERE id=?",Integer.class,id));
    }

    @Test
    void 员工只管理当前授权站点且撤权即时生效() {
        long a=seedStation(DEPT_A,true,"C03员工甲"), b=seedStation(DEPT_A,true,"C03员工乙");String admin=adminToken();
        JsonNode mine=createSlot(slotInput(a,"PICKUP"),admin), other=createSlot(slotInput(b,"PICKUP"),admin);
        String persona="c03-employee-"+System.nanoTime();long employee=seedEmployee(expectedPhone(persona),"capacity"+System.nanoTime());
        seedGrant(employee,a);String token=employeeStationToken(persona,a);
        assertEquals(1,rowsOf(rest.exchange(url("/api/app/station/capacity"),HttpMethod.GET,station(null,token,null),String.class)).size());
        String path="/api/app/station/capacity/";
        assertEquals(404,rest.exchange(url(path+other.get("slotId").asText()),HttpMethod.PUT,station(changeInput(20,0),token,UUID.randomUUID().toString()),String.class).getStatusCode().value());
        assertEquals(200,rest.exchange(url(path+mine.get("slotId").asText()),HttpMethod.PUT,station(changeInput(20,0),token,UUID.randomUUID().toString()),String.class).getStatusCode().value());
        jdbc.update("UPDATE vi_station_grant SET enabled=0 WHERE sys_user_id=?",employee);
        assertEquals(403,rest.exchange(url(path+mine.get("slotId").asText()),HttpMethod.PUT,station(changeInput(30,1),token,UUID.randomUUID().toString()),String.class).getStatusCode().value());
        assertEquals(20,jdbc.queryForObject("SELECT total_quota FROM vi_quota_slot WHERE id=?",Integer.class,mine.get("slotId").asText()));
    }

    @Test
    void 无效时间重叠时段停用站点及伪造占用数拒绝() {
        long a=seedStation(DEPT_A,true,"C03校验");String admin=adminToken();Map<String,Object> body=slotInput(a,"PICKUP");
        JsonNode existing=createSlot(body,admin);
        assertEquals(409,rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,admin(body,admin),String.class).getStatusCode().value());
        Map<String,Object> invalid=new java.util.HashMap<>(body);invalid.put("startAt","2020-01-01T00:00:00Z");invalid.put("endAt","2019-12-31T23:00:00Z");
        assertEquals(400,rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,admin(invalid,admin),String.class).getStatusCode().value());
        invalid=new java.util.HashMap<>(body);invalid.put("total",1.5);
        assertEquals(400,rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,admin(invalid,admin),String.class).getStatusCode().value());
        Map<String,Object> forged=changeInput(10,0);forged.put("held",0);
        assertEquals(400,rest.exchange(url("/api/admin/capacity/"+existing.get("slotId").asText()),HttpMethod.PUT,admin(forged,admin),String.class).getStatusCode().value());
        long disabled=seedStation(DEPT_A,false,"C03停用");
        assertEquals(409,rest.exchange(url("/api/admin/capacity"),HttpMethod.POST,admin(slotInput(disabled,"PICKUP"),admin),String.class).getStatusCode().value());
        jdbc.update("UPDATE vi_quota_slot SET starts_at=UTC_TIMESTAMP(3)-INTERVAL 1 MINUTE WHERE id=?",existing.get("slotId").asText());
        assertEquals(409,rest.exchange(url("/api/admin/capacity/"+existing.get("slotId").asText()),HttpMethod.PUT,admin(changeInput(9,0),admin),String.class).getStatusCode().value());
    }

    @Test
    void 并发调容同版本只能提交一次且失败事务无成功事件() throws Exception {
        long a=seedStation(DEPT_A,true,"C03并发");String admin=adminToken();String id=createSlot(slotInput(a,"PICKUP"),admin).get("slotId").asText();
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);var start=new java.util.concurrent.CountDownLatch(1);
        try {
            var first=pool.submit(() -> {start.await();return rest.exchange(url("/api/admin/capacity/"+id),HttpMethod.PUT,admin(changeInput(8,0),admin),String.class);});
            var second=pool.submit(() -> {start.await();return rest.exchange(url("/api/admin/capacity/"+id),HttpMethod.PUT,admin(changeInput(9,0),admin),String.class);});
            start.countDown();List<Integer> codes=List.of(first.get(10,java.util.concurrent.TimeUnit.SECONDS).getStatusCode().value(),second.get(10,java.util.concurrent.TimeUnit.SECONDS).getStatusCode().value());
            assertTrue(codes.contains(200)&&codes.contains(409),codes.toString());
            assertEquals(1,jdbc.queryForObject("SELECT version FROM vi_quota_slot WHERE id=?",Integer.class,id));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='CAPACITY_CHANGE'",Integer.class));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE event_type='CAPACITY_CHANGED'",Integer.class));
        } finally {pool.shutdownNow();}
    }
}
