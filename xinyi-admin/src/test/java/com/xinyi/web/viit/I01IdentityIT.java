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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * I01 统一微信登录与默认车主 的真实集成测试（真实 HTTP + SpringSecurity(MINI 链) + MyBatis + 隔离库
 * xinyi_vi_it_e07 + 真实 provider 模拟器 simulator.py）。仅微信渠道为 SIMULATOR 替身（L01 真实渠道另行门禁），
 * 其余全链路真实。绝不伪造 LoginUser、绝不以 accountId=1 触发框架超管。
 *
 * 覆盖 I01 直接可测用例：TC-R62-001/002、TC-001/002、TC-R63-014、TC-R50-007/032、TC-R62-018/035，
 * 及三条 negative_checks（同 openid 并发只一账号、不按 role 授高权、accountId=1 不超管）。
 * TC-003..018（派单/切站/退款/统计鉴权）与 TC-R50-008/012 依赖尚未实现的派单/订单/站点/契约检查任务，
 * 由其归属任务覆盖，本 IT 不伪造。
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
class I01IdentityIT {

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
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("i01-sim-", ".sqlite");
        Files.deleteIfExists(simDb);
        String python = System.getenv().getOrDefault("VI_PYTHON", "python3");
        Path sim = repoRoot().resolve("docs/vehicle/V6.4/13_真实集成测试/provider_simulator/simulator.py");
        if (!Files.isRegularFile(sim)) {
            throw new IllegalStateException("找不到 provider 模拟器: " + sim);
        }
        ProcessBuilder pb = new ProcessBuilder(python, sim.toString(),
                "--host", "127.0.0.1", "--port", "0", "--db", simDb.toString());
        pb.environment().put("VI_SIM_SECRET", simSecret);
        pb.redirectErrorStream(true);
        simProcess = pb.start();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(simProcess.getInputStream(), StandardCharsets.UTF_8));
        String line = reader.readLine();
        if (line == null) {
            throw new IllegalStateException("模拟器无启动输出（可能缺少 python3 或 VI_SIM_SECRET）");
        }
        JsonNode node = ViJson.tree(line);
        simPort = node.get("port").asInt();
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
            // 临时库清理失败不影响测试结论
        }
    }

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    /**
     * 不抛 4xx/5xx 的 RestTemplate，便于断言真实状态码（Spring Boot 4 测试模块无 TestRestTemplate）。
     * 用 JdkClientHttpRequestFactory（java.net.http）而非默认 HttpURLConnection：后者对 401 响应体
     * 有读取怪癖（getBody 返回 null，尽管服务端已发 Content-Length 与 ApiError），真实客户端 wx.request/axios 无此问题。
     */
    private final RestTemplate rest = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory()) {{
        setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }};

    @BeforeEach
    void cleanIdentityTables() {
        for (String t : List.of("vi_session_token", "vi_session", "vi_account_security", "vi_account",
                "vi_driver_profile", "vi_employee_binding", "vi_station_grant")) {
            jdbc.execute("DELETE FROM " + t);
        }
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'i01rest\\_%'");
    }

    // ---------- 模拟器交互 ----------

    private String simUrl(String path) {
        return "http://127.0.0.1:" + simPort + path;
    }

    /**
     * 与模拟器交互用 java.net.http.HttpClient（ofString 显式设置 Content-Length）。
     * 模拟器 do_POST 要求 Content-Length>0，否则 422；RestTemplate 对空 Map 可能用 chunked 无 Content-Length。
     */
    private Map<String, Object> simPost(String path, Map<String, Object> body) {
        try {
            String json = ViJson.write(body);
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(simUrl(path)))
                    .header("Content-Type", "application/json")
                    .header("X-Sim-Secret", simSecret)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            java.net.http.HttpResponse<String> resp =
                    SIM_HTTP.send(req, java.net.http.HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, resp.statusCode(),
                    "模拟器 " + path + " 应 200，实际 " + resp.statusCode() + " 体=" + resp.body());
            return ViJson.read(resp.body(), Map.class);
        } catch (java.io.IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("模拟器调用失败: " + path, e);
        }
    }

    private String newRun() {
        return (String) simPost("/control/runs", Map.of()).get("runId");
    }

    private String newCode(String runId, String persona) {
        Map<String, Object> req = new HashMap<>();
        req.put("runId", runId);
        req.put("persona", persona);
        req.put("kind", "wechat");
        return (String) simPost("/control/codes", req).get("code");
    }

    // ---------- 应用交互 ----------

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private ResponseEntity<String> postJson(String path, Object body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Content-Type", "application/json");
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, h), String.class);
    }

    private ResponseEntity<String> getJson(String path, String token, String clientType) {
        HttpHeaders h = new HttpHeaders();
        if (token != null) {
            h.set("Authorization", "Bearer " + token);
        }
        if (clientType != null) {
            h.set("X-Client-Type", clientType);
        }
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(h), String.class);
    }

    private JsonNode login(String code) {
        ResponseEntity<String> resp = postJson("/api/common/login/wechat", Map.of("code", code), null);
        assertEquals(200, resp.getStatusCode().value(), "登录应 200，实际 " + resp.getStatusCode() + " 体=" + resp.getBody());
        JsonNode root = ViJson.tree(resp.getBody());
        assertEquals(200, root.get("code").asInt());
        return root.get("data");
    }

    // ---------- 用例 ----------

    @Test
    void 新微信用户默认OWNER且不建sys_user_TC_R62_001() {
        long sysUserBefore = countSysUser();
        String run = newRun();
        String persona = "p-new-" + System.nanoTime();
        JsonNode data = login(newCode(run, persona));

        assertEquals("OWNER", data.get("activeRole").asText());
        assertTrue(data.get("availableRoles").isArray() && data.get("availableRoles").size() == 1
                        && "OWNER".equals(data.get("availableRoles").get(0).asText()),
                "新账号 availableRoles 只应含 OWNER，实际=" + data.get("availableRoles"));
        assertEquals("UNLINKED", data.get("employeeLink").get("status").asText());
        assertTrue(data.get("token").asText().length() >= 32, "应返回不透明访问令牌");
        assertTrue(data.get("refreshToken").asText().length() >= 32, "应返回刷新令牌");
        assertTrue(data.get("accountId").asText().matches("[1-9][0-9]*"), "accountId 为十进制字符串");
        assertFalse(data.get("phoneBound").asBoolean(), "新号未绑定手机号");

        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_account", Integer.class), "应创建唯一账号");
        assertEquals(sysUserBefore, countSysUser(), "登录绝不创建 sys_user");
    }

    @Test
    void 同身份再次登录恢复同账号_TC_001() {
        String run = newRun();
        String persona = "p-restore-" + System.nanoTime();
        JsonNode first = login(newCode(run, persona));
        JsonNode second = login(newCode(run, persona));
        assertEquals(first.get("accountId").asText(), second.get("accountId").asText(), "同 openid 必须恢复同账号");
        assertEquals("OWNER", second.get("activeRole").asText(), "新会话仍默认 OWNER");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_account WHERE open_id = ?",
                Integer.class, "sim_" + run + "_" + persona), "同 openid 只一账号");
    }

    @Test
    void 伪造role或openid字段被400拒绝不提权_TC_002_R62_002() {
        String run = newRun();
        String persona = "p-forge-" + System.nanoTime();
        String code = newCode(run, persona);

        Map<String, Object> forged = new HashMap<>();
        forged.put("code", code);
        forged.put("role", "STATION");
        ResponseEntity<String> r1 = postJson("/api/common/login/wechat", forged, null);
        assertEquals(400, r1.getStatusCode().value(), "含未知字段 role 应 400");
        assertEquals("VI_REQUEST_INVALID", ViJson.tree(r1.getBody()).get("errorCode").asText());

        Map<String, Object> forged2 = new HashMap<>();
        forged2.put("code", newCode(run, persona));
        forged2.put("openid", "attacker-chosen");
        ResponseEntity<String> r2 = postJson("/api/common/login/wechat", forged2, null);
        assertEquals(400, r2.getStatusCode().value(), "含未知字段 openid 应 400");

        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM vi_account", Integer.class),
                "被拒请求不得创建账号");
    }

    @Test
    void 同openid并发登录只产生一个账号_negative() throws Exception {
        String run = newRun();
        String persona = "p-conc-" + System.nanoTime();
        String c1 = newCode(run, persona);
        String c2 = newCode(run, persona);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (String code : List.of(c1, c2)) {
            futures.add(pool.submit(() -> {
                gate.await();
                ResponseEntity<String> resp = postJson("/api/common/login/wechat", Map.of("code", code), null);
                assertEquals(200, resp.getStatusCode().value(), "并发登录都应成功，体=" + resp.getBody());
                return ViJson.tree(resp.getBody()).get("data").get("accountId").asText();
            }));
        }
        gate.countDown();
        String a1 = futures.get(0).get(30, TimeUnit.SECONDS);
        String a2 = futures.get(1).get(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertEquals(a1, a2, "并发同 openid 必须落到同一账号");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_account WHERE open_id = ?",
                Integer.class, "sim_" + run + "_" + persona), "uk_app_open 保证只一账号");
    }

    @Test
    void 无效code被拒401() {
        ResponseEntity<String> resp = postJson("/api/common/login/wechat",
                Map.of("code", "not-a-provisioned-code"), null);
        assertEquals(401, resp.getStatusCode().value(),
                "无效 code 应真实 401，实际=" + resp.getStatusCode() + " 体=" + resp.getBody());
        assertNotNull(resp.getBody(), "401 应带 ApiError 响应体");
        JsonNode err = ViJson.tree(resp.getBody());
        assertEquals("VI_AUTH_REQUIRED", err.get("errorCode").asText());
        assertEquals(401, err.get("code").asInt(), "ApiError.code 等于真实 HTTP 状态");
    }

    @Test
    void 档案_带令牌200_无令牌真实401_API_005() {
        String run = newRun();
        JsonNode data = login(newCode(run, "p-profile-" + System.nanoTime()));
        String token = data.get("token").asText();
        String accountId = data.get("accountId").asText();

        ResponseEntity<String> ok = getJson("/api/common/profile", token, "owner");
        assertEquals(200, ok.getStatusCode().value(), "带令牌读档案应 200，体=" + ok.getBody());
        JsonNode profile = ViJson.tree(ok.getBody()).get("data");
        assertEquals(accountId, profile.get("accountId").asText());
        assertEquals("OWNER", profile.get("availableRoles").get(0).asText());
        assertEquals("no-store", ok.getHeaders().getFirst("Cache-Control"), "身份响应 no-store");

        ResponseEntity<String> noAuth = getJson("/api/common/profile", null, "owner");
        assertEquals(401, noAuth.getStatusCode().value(), "无令牌应真实 401（vi 域非恒200）");
        assertEquals("VI_AUTH_REQUIRED", ViJson.tree(noAuth.getBody()).get("errorCode").asText());
    }

    @Test
    void 旧common_logout无业务映射不撤销会话_TC_R50_007() {
        String run = newRun();
        JsonNode data = login(newCode(run, "p-logout-" + System.nanoTime()));
        String token = data.get("token").asText();
        ResponseEntity<String> resp = postJson("/api/common/logout", Map.of(), token);
        // 无业务映射：HTTP 404，或框架对未映射资源返回非成功信封(body.code!=200)；绝不成功执行登出
        boolean notFound = resp.getStatusCode().value() == 404
                || (resp.getBody() != null && ViJson.tree(resp.getBody()).path("code").asInt(200) != 200);
        assertTrue(notFound, "退役 /api/common/logout 应无业务映射(404/非成功)，实际 status="
                + resp.getStatusCode() + " body=" + resp.getBody());
        // 关键安全属性：不创建兼容清理——原 MINI 会话未被撤销，仍可正常读档案
        ResponseEntity<String> after = getJson("/api/common/profile", token, "owner");
        assertEquals(200, after.getStatusCode().value(),
                "logout 无映射不得撤销会话；profile 仍应 200，实际=" + after.getStatusCode());
    }

    @Test
    void MINI令牌不能访问ADMIN入口_accountId不触发超管_TC_R50_032() {
        String run = newRun();
        JsonNode data = login(newCode(run, "p-crossdomain-" + System.nanoTime()));
        String miniToken = data.get("token").asText();
        // 用 MINI 不透明令牌打框架 ADMIN 端点：框架 JWT 过滤器无法解析 → 未认证 → 拒绝（不命中 sys_user=1 超管分支）
        ResponseEntity<String> resp = getJson("/system/user/list", miniToken, null);
        JsonNode body = ViJson.tree(resp.getBody());
        boolean denied = resp.getStatusCode().value() == 401 || resp.getStatusCode().value() == 403
                || (body.has("code") && (body.get("code").asInt() == 401 || body.get("code").asInt() == 403));
        assertTrue(denied, "MINI 令牌访问 ADMIN 端点必须被拒，实际 status=" + resp.getStatusCode()
                + " body=" + resp.getBody());
        assertFalse(body.has("rows"), "绝不返回 ADMIN 用户列表数据");
    }

    @Test
    void 已审核DRIVER与有效STATION再登录保持资格_默认OWNER_TC_R62_018_035() {
        String run = newRun();
        String persona = "p-qualified-" + System.nanoTime();
        String openId = "sim_" + run + "_" + persona;
        JsonNode first = login(newCode(run, persona));
        long accountId = Long.parseLong(first.get("accountId").asText());

        // 直接种子权威资格/关联/授权（这些表的写入分属代驾资格、手机号自动关联、后台站点预授权任务；
        // 此处仅构造"老账号已有真实资格"的既有状态，验证 I01 登录读取并恢复，不重置）
        jdbc.update("INSERT INTO vi_driver_profile (account_id, qualification_status, new_assignment_enabled, "
                + "listening_enabled, license_long_term) VALUES (?, 'APPROVED', 1, 1, 1)", accountId);
        long sysUserId = 900000000L + (accountId % 100000);
        long stationId = 700000000L + (accountId % 100000);
        // STATION 须链接真实在职员工（fail-closed 实时校验）；构造既有有效员工目录行
        jdbc.update("INSERT INTO sys_user (user_id, user_name, nick_name, status, del_flag, auth_version, password) "
                + "VALUES (?, ?, '恢复员工', '0', '0', 0, 'x')", sysUserId, "i01rest_" + accountId);
        jdbc.update("INSERT INTO vi_employee_binding (account_id, sys_user_id, bound_at, app_id, phone_lookup, "
                + "last_phone_verified_at, employee_auth_version, status) VALUES (?, ?, UTC_TIMESTAMP(3), 'SIM_APP', "
                + "?, UTC_TIMESTAMP(3), 0, 'ACTIVE')", accountId, sysUserId, new byte[32]);
        jdbc.update("INSERT INTO vi_station_grant (sys_user_id, station_id, enabled, granted_by, reason) "
                + "VALUES (?, ?, 1, 1, 'I01 恢复测试')", sysUserId, stationId);

        JsonNode second = login(newCode(run, persona));
        assertEquals(first.get("accountId").asText(), second.get("accountId").asText(), "恢复同账号");
        assertEquals("OWNER", second.get("activeRole").asText(), "新会话仍默认 OWNER（不自动切工作视角）");
        List<String> roles = new ArrayList<>();
        second.get("availableRoles").forEach(n -> roles.add(n.asText()));
        assertTrue(roles.contains("OWNER") && roles.contains("DRIVER") && roles.contains("STATION"),
                "已审核资格须恢复，availableRoles 应含 OWNER/DRIVER/STATION，实际=" + roles);
        assertEquals("APPROVED", second.get("driverQualification").asText(), "代驾资格不被重置");
        assertEquals("LINKED", second.get("employeeLink").get("status").asText());
        assertTrue(second.get("employeeLink").get("workAccessEnabled").asBoolean(), "有站点授权应可工作");
        assertTrue(second.get("stationIds").size() >= 1, "应返回授权站点");
        // 承接占用不因重新登录清空：此处仅验证账号/资格恢复；占用释放语义属派单任务
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM vi_account WHERE open_id = ?",
                Integer.class, openId), "仍只一账号");
    }

    private long countSysUser() {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
        return n == null ? 0L : n;
    }
}
