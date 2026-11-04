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
import static org.junit.jupiter.api.Assertions.*;

/** B04真实建单、并发占额、支付门禁与事务回滚；仅外部微信/地图使用协议替身。 */
@SpringBootTest(
        classes = RuoYiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
            "vi.environment=integration",
            "vi.providers.mode=SIMULATOR",
            "vi.outbox-relay-enabled=false",
            "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
/** 覆盖车主、司机、站点、后台四种订单投影；验证列表/详情边界以及财务、地址字段不越权。 */
class B04OrderIT {

    private static com.sun.net.httpserver.HttpServer routeServer;
    private static final java.util.concurrent.atomic.AtomicInteger routeCalls =
            new java.util.concurrent.atomic.AtomicInteger();
    private static volatile boolean routeFailure;
    private static Process simProcess;
    private static int simPort;
    private static String simSecret;
    private static Path simDb;
    private static final java.net.http.HttpClient SIM_HTTP =
            java.net.http.HttpClient.newHttpClient();

    static {
        try {
            routeServer =
                    com.sun.net.httpserver.HttpServer.create(
                            new java.net.InetSocketAddress("127.0.0.1", 0), 0);
            routeServer.createContext(
                    "/route",
                    e -> {
                        routeCalls.incrementAndGet();
                        byte[] data =
                                (routeFailure
                                                ? "{\"status\":120}"
                                                : "{\"status\":0,\"result\":{\"routes\":[{\"distance\":12345,\"duration\":20}]}}")
                                        .getBytes(StandardCharsets.UTF_8);
                        e.sendResponseHeaders(200, data.length);
                        e.getResponseBody().write(data);
                        e.close();
                    });
            routeServer.start();
            startSimulator();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void providerProps(DynamicPropertyRegistry registry) {
        registry.add("vi.providers.base-url", () -> "http://127.0.0.1:" + simPort);
        registry.add("vi.providers.secret", () -> simSecret);
        registry.add("vi.crypto.data-key", () -> "b04-test-data-key-0123456789abcdef");
        registry.add("vi.maps.tencent.key", () -> "test-only");
        registry.add(
                "vi.maps.tencent.endpoint",
                () -> "http://127.0.0.1:" + routeServer.getAddress().getPort() + "/route");
        registry.add("vi.lookup-key", () -> "b04-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("b04-sim-", ".sqlite");
        Files.deleteIfExists(simDb);
        String python = System.getenv().getOrDefault("VI_PYTHON", "python3");
        Path sim =
                repoRoot().resolve("docs/vehicle/V6.4/13_真实集成测试/provider_simulator/simulator.py");
        ProcessBuilder pb =
                new ProcessBuilder(
                        python,
                        sim.toString(),
                        "--host",
                        "127.0.0.1",
                        "--port",
                        "0",
                        "--db",
                        simDb.toString());
        pb.environment().put("VI_SIM_SECRET", simSecret);
        pb.redirectErrorStream(true);
        simProcess = pb.start();
        BufferedReader reader =
                new BufferedReader(
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
        if (routeServer != null) routeServer.stop(0);
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

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired TokenService tokenService;
    @Autowired ISysUserService userService;

    private final RestTemplate rest =
            new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory()) {
                {
                    setErrorHandler(
                            new DefaultResponseErrorHandler() {
                                @Override
                                public boolean hasError(ClientHttpResponse response) {
                                    return false;
                                }
                            });
                }
            };

    @BeforeEach
    void fixtures() {
        assertEquals("xinyi_vi_it_e07", jdbc.queryForObject("SELECT DATABASE()", String.class));
        assertEquals(
                "XINYI_VEHICLE_ISOLATED",
                jdbc.queryForObject(
                        "SELECT kind FROM test_environment_guard WHERE id=1", String.class));
        routeCalls.set(0);
        routeFailure = false;
        cleanOrders();
        seedPaymentPolicy();
        jdbc.execute("DELETE FROM vi_quote");
        jdbc.execute("DELETE FROM vi_price_rule");
        jdbc.execute("DELETE FROM vi_idempotency");
        jdbc.execute("DELETE FROM vi_quota_slot WHERE station_id IN (996401,996402)");
        jdbc.execute("DELETE FROM vi_station WHERE id IN (996401,996402)");
        jdbc.execute(
                "INSERT INTO vi_station"
                    + " (id,name,dept_id,address,lng,lat,service_radius_meter,supported_vehicle_codes,business_hours,enabled)"
                    + " VALUES"
                    + " (996401,'B04甲',100,'测试甲',110,21,100000,'[\"B04_CAR\"]','{}',1),(996402,'B04乙',100,'测试乙',"
                    + "110.01,21,100000,'[\"B04_CAR\"]','{}',1)");
        jdbc.execute("DELETE FROM sys_dict_data WHERE dict_value IN ('B04_CAR','B04_ANNUAL')");
        jdbc.execute(
                "INSERT INTO sys_dict_data(dict_label,dict_value,dict_type,status) VALUES"
                    + " ('测试车型','B04_CAR','vi_vehicle_type','0'),('测试检测','B04_ANNUAL','vi_inspection_type','0')");
        for (var entry :
                Map.of(
                                "vi.biz.booking.enabled",
                                "true",
                                "vi.biz.terms.version",
                                "b04v1",
                                "vi.biz.terms.text",
                                "测试协议",
                                "vi.biz.privacy.version",
                                "b04v1",
                                "vi.biz.privacy.text",
                                "测试隐私",
                                "vi.biz.booking.handover.seconds",
                                "600",
                                "vi.biz.booking.route-buffer.seconds",
                                "300",
                                "vi.biz.booking.min-lead.seconds",
                                "60",
                                "vi.biz.booking.quote-ttl.seconds",
                                "300",
                                "vi.config.version",
                                "1")
                        .entrySet()) {
            jdbc.update("DELETE FROM sys_config WHERE config_key=?", entry.getKey());
            jdbc.update(
                    "INSERT INTO sys_config(config_name,config_key,config_value,config_type) VALUES"
                            + " (?,?,?,'N')",
                    entry.getKey(),
                    entry.getKey(),
                    entry.getValue());
        }
        start =
                java.time.Instant.now()
                        .plusSeconds(172800)
                        .truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        for (long station : List.of(996401L, 996402L))
            for (String type : List.of("PICKUP", "STATION_RECEIVE"))
                jdbc.update(
                        "INSERT INTO"
                            + " vi_quota_slot(quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota,"
                            + "confirmed_quota,consumed_quota,enabled) VALUES (?,?,"
                            + " ?,?,?,5,0,0,0,1)",
                        type,
                        "station:" + station,
                        station,
                        java.sql.Timestamp.from(start),
                        java.sql.Timestamp.from(
                                start.plusSeconds(type.equals("PICKUP") ? 3600 : 7200)));
        slotId =
                jdbc.queryForObject(
                        "SELECT id FROM vi_quota_slot WHERE station_id=996401 AND"
                                + " quota_type='PICKUP'",
                        Long.class);
        publishPrice();
    }

    @org.junit.jupiter.api.AfterEach
    void removeOwnSupply() {
        assertEquals("xinyi_vi_it_e07", jdbc.queryForObject("SELECT DATABASE()", String.class));
        cleanOrders();
        cleanPolicy();
        jdbc.execute("DELETE FROM vi_quota_slot WHERE station_id IN (996401,996402)");
        jdbc.execute("DELETE FROM vi_station WHERE id IN (996401,996402)");
    }

    private Map<String, Object> simPost(String path, Map<String, Object> body) {
        try {
            String json = ViJson.write(body);
            java.net.http.HttpRequest req =
                    java.net.http.HttpRequest.newBuilder(
                                    java.net.URI.create("http://127.0.0.1:" + simPort + path))
                            .header("Content-Type", "application/json")
                            .header("X-Sim-Secret", simSecret)
                            .POST(
                                    java.net.http.HttpRequest.BodyPublishers.ofString(
                                            json, StandardCharsets.UTF_8))
                            .build();
            java.net.http.HttpResponse<String> resp =
                    SIM_HTTP.send(
                            req,
                            java.net.http.HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8));
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

    private String adminToken() {
        SysUser admin = userService.selectUserByUserName("admin");
        Long av =
                jdbc.queryForObject(
                        "SELECT auth_version FROM sys_user WHERE user_id = ?",
                        Long.class,
                        admin.getUserId());
        LoginUser lu = new LoginUser(admin, Set.of("*:*:*"));
        lu.setUserId(admin.getUserId());
        lu.setAuthVersion(av);
        org.springframework.mock.web.MockHttpServletRequest req =
                new org.springframework.mock.web.MockHttpServletRequest();
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
        return new HttpEntity<>(body, h);
    }

    private JsonNode login(String persona) {
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code =
                (String)
                        simPost(
                                        "/control/codes",
                                        Map.of("runId", run, "persona", persona, "kind", "wechat"))
                                .get("code");
        ResponseEntity<String> r =
                rest.exchange(
                        url("/api/common/login/wechat"),
                        HttpMethod.POST,
                        mini(Map.of("code", code), null),
                        String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }

    java.time.Instant start;
    long slotId;
    @Autowired com.xinyi.rongli.booking.service.QuoteService quotes;
    @Autowired com.xinyi.rongli.address.service.AddressCrypto crypto;

    String reviewer() {
        String name = "b04_" + System.nanoTime();
        jdbc.update(
                "INSERT INTO sys_user(user_name,nick_name,status,del_flag,auth_version,password)"
                        + " VALUES (?,?,'0','0',0,'x')",
                name,
                "测试审核");
        long id =
                jdbc.queryForObject(
                        "SELECT user_id FROM sys_user WHERE user_name=?", Long.class, name);
        jdbc.update("INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,100)", id);
        SysUser user = userService.selectUserById(id);
        var role = new com.xinyi.common.core.domain.entity.SysRole();
        role.setRoleId(id);
        role.setStatus("0");
        role.setDataScope("3");
        role.setPermissions(Set.of("biz:price:audit"));
        user.setRoles(List.of(role));
        LoginUser login = new LoginUser(user, Set.of("biz:price:audit"));
        login.setUserId(id);
        login.setAuthVersion(0L);
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("User-Agent", "b04-test");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));
        try {
            return tokenService.createToken(login);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    JsonNode adminPost(String path, Map<String, Object> body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        h.set("X-Client-Type", "admin");
        h.set("Idempotency-Key", UUID.randomUUID().toString());
        var r = rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, h), String.class);
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        var json = ViJson.tree(r.getBody());
        return json.has("rows") ? json : json.get("data");
    }

    void publishPrice() {
        String admin = adminToken();
        var input = new HashMap<String, Object>();
        input.putAll(
                Map.of(
                        "ownerDeptId",
                        "100",
                        "environment",
                        "TEST",
                        "name",
                        "B04计价",
                        "vehicleTypeCode",
                        "B04_CAR",
                        "inspectionTypeCode",
                        "B04_ANNUAL",
                        "inspectionFee",
                        20000,
                        "driverBaseFee",
                        3000,
                        "includedDistanceMeter",
                        1000,
                        "distanceUnitMeter",
                        1000,
                        "distanceUnitFee",
                        100));
        input.put("distanceRounding", "CEIL_UNIT");
        input.put("routeBasis", "ONE_WAY");
        input.put("effectiveFrom", start.minusSeconds(3600).toString());
        var row = adminPost("/api/admin/prices", input, admin);
        String path = "/api/admin/prices/" + row.get("ruleId").asText();
        row = adminPost(path + "/submit", Map.of("expectedVersion", 0, "reason", "测试"), admin);
        row =
                adminPost(
                        path + "/audit",
                        Map.of("expectedVersion", 1, "decision", "APPROVE", "opinion", "独立测试审核"),
                        reviewer());
        adminPost(path + "/publish", Map.of("expectedVersion", 2, "reason", "测试发布"), admin);
    }

    Map<String, Object> input() {
        return new HashMap<>(
                Map.of(
                        "slotId",
                        Long.toString(slotId),
                        "stationId",
                        "996402",
                        "inspectionTypeCode",
                        "B04_ANNUAL",
                        "vehicleDraft",
                        Map.of("plateNo", "粤A12345", "vehicleTypeCode", "B04_CAR"),
                        "addressDraft",
                        Map.of(
                                "address",
                                "测试接车地址",
                                "location",
                                Map.of("lng", 110, "lat", 21.01, "coordinateSystem", "GCJ02"))));
    }

    ResponseEntity<String> post(String suffix, Map<String, Object> body, String token, String key) {
        var r = mini(body, token);
        HttpHeaders h = new HttpHeaders();
        h.putAll(r.getHeaders());
        h.set("Idempotency-Key", key);
        return rest.exchange(
                url("/api/app/owner/orders/" + suffix),
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class);
    }

    JsonNode good(ResponseEntity<String> r) {
        assertEquals(200, r.getStatusCode().value(), r.getBody());
        var json = ViJson.tree(r.getBody());
        return json.has("rows") ? json : json.get("data");
    }

    void cleanOrders() {
        for (String table :
                List.of(
                        "vi_handover",
                        "vi_evidence_group",
                        "vi_driver_assignment",
                        "vi_aftersale_case",
                        "vi_dispute",
                        "vi_vehicle_license",
                        "vi_inspection_result",
                        "vi_quota_reservation",
                        "vi_payment_order",
                        "vi_order_snapshot"))
            jdbc.execute(
                    "DELETE FROM "
                            + table
                            + " WHERE order_id IN (SELECT id FROM vi_order WHERE station_id IN"
                            + " (996401,996402))");
        jdbc.execute("DELETE FROM vi_order WHERE station_id IN (996401,996402)");
    }

    void cleanPolicy() {
        jdbc.execute(
                "DELETE FROM vi_integration_publication WHERE target_id BETWEEN 996410 AND 996419");
        jdbc.execute("DELETE FROM vi_integration_check WHERE target_id BETWEEN 996410 AND 996419");
        jdbc.execute(
                "DELETE FROM vi_integration_revision WHERE profile_id BETWEEN 996410 AND 996419");
        jdbc.execute("DELETE FROM vi_integration_profile WHERE id BETWEEN 996410 AND 996419");
    }

    String policyJson() {
        return ViJson.write(
                Map.of(
                        "offlineEnabled",
                        true,
                        "wechatEnabled",
                        false,
                        "offlineCollectionMode",
                        "PREPAID_MANUAL_VERIFIED",
                        "offlineMethods",
                        List.of("CASH"),
                        "offlineReceiverName",
                        "测试公司",
                        "offlineInstructions",
                        "仅隔离测试，不实际收款",
                        "offlineContactName",
                        "测试财务",
                        "offlineContactPhone",
                        "00000000000"));
    }

    void seedPaymentPolicy() {
        cleanPolicy();
        jdbc.update(
                "INSERT INTO"
                    + " vi_integration_profile(id,kind,owner_dept_id,environment,config_key,display_name,lifecycle,active_revision_id,created_by)"
                    + " VALUES"
                    + " (996410,'PAYMENT_POLICY',100,'TEST','DEFAULT','B04测试策略','ACTIVE',996410,1)");
        jdbc.update(
                "INSERT INTO"
                    + " vi_integration_revision(id,profile_id,revision_no,config_json,secret_refs_json,content_hash,created_by,reason)"
                    + " VALUES(996410,996410,1,?,'{}',REPEAT('a',64),1,'B04测试发布夹具')",
                policyJson());
        jdbc.update(
                "INSERT INTO"
                    + " vi_integration_check(id,target_type,target_id,revision_id,content_hash,check_level,"
                    + "status,issues_json,checked_at,expires_at,checked_by)"
                    + " VALUES(996410,'PROFILE',996410,996410,REPEAT('a',64),'LOCAL','PASS','[]',UTC_TIMESTAMP(3)-INTERVAL"
                    + " 1 MINUTE,UTC_TIMESTAMP(3)+INTERVAL 1 HOUR,1)");
        jdbc.update(
                "INSERT INTO"
                    + " vi_integration_publication(id,target_type,target_id,revision_id,check_id,action,"
                    + "published_by,published_at,reason,dependency_versions,target_version)"
                    + " VALUES(996410,'PROFILE',996410,996410,996410,'PUBLISH',1,UTC_TIMESTAMP(3),'B04测试发布夹具','{}',1)");
    }

    JsonNode verifiedLogin(String persona) {
        var session = login(persona + System.nanoTime());
        String token = session.path("token").asText();
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code =
                (String)
                        simPost(
                                        "/control/codes",
                                        Map.of(
                                                "runId",
                                                run,
                                                "persona",
                                                persona + System.nanoTime(),
                                                "kind",
                                                "phone"))
                                .get("code");
        good(
                rest.exchange(
                        url("/api/common/phone/bind"),
                        HttpMethod.POST,
                        mini(
                                Map.of(
                                        "phoneCode",
                                        code,
                                        "expectedAccountVersion",
                                        session.path("accountVersion").asLong(),
                                        "verificationRequestId",
                                        UUID.randomUUID().toString()),
                                token),
                        String.class));
        return session;
    }

    JsonNode quote(String token) {
        return good(post("quote", input(), token, UUID.randomUUID().toString()));
    }

    Map<String, Object> orderInput(JsonNode quote) {
        return new HashMap<>(
                Map.of(
                        "quoteId",
                        quote.path("quoteId").asText(),
                        "termsVersion",
                        "b04v1",
                        "privacyVersion",
                        "b04v1",
                        "acceptedTerms",
                        true));
    }

    ResponseEntity<String> create(Map<String, Object> body, String token, String key) {
        var entity = mini(body, token);
        HttpHeaders h = new HttpHeaders();
        h.putAll(entity.getHeaders());
        h.set("Idempotency-Key", key);
        return rest.exchange(
                url("/api/app/owner/orders"),
                HttpMethod.POST,
                new HttpEntity<>(body, h),
                String.class);
    }

    void emptyOrders() {
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM vi_order WHERE station_id IN (996401,996402)",
                        Integer.class));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT SUM(held_quota) FROM vi_quota_slot WHERE station_id IN"
                                + " (996401,996402)",
                        Integer.class));
    }

    ResponseEntity<String> get(String path, String token, boolean admin) {
        var response =
                rest.exchange(
                        url(path),
                        HttpMethod.GET,
                        admin ? admin(null, token) : mini(null, token),
                        String.class);
        var body = ViJson.tree(response.getBody());
        if (response.getStatusCode().value() == 200 && body.path("code").asInt() == 200) {
            String endpoint = path.split("\\?")[0].replaceAll("/orders/[^/]+", "/orders/{orderNo}");
            capture("envelope-" + endpoint.replace('/', '_'), body);
        }
        return response;
    }

    JsonNode made(String token) {
        return good(create(orderInput(quote(token)), token, UUID.randomUUID().toString()));
    }

    long id(JsonNode order) {
        return jdbc.queryForObject(
                "SELECT id FROM vi_order WHERE order_no=?",
                Long.class,
                order.path("orderNo").asText());
    }

    String path(String role, JsonNode o) {
        return "/api/"
                + (role.equals("admin") ? "admin" : "app/" + role)
                + "/orders/"
                + o.path("orderNo").asText();
    }

    void noPrivate(JsonNode data) {
        for (String forbidden :
                List.of(
                        "pickupAddress",
                        "returnAddress",
                        "ownerContact",
                        "addressCipher",
                        "contactCipher",
                        "money",
                        "remark",
                        "reason",
                        "ownerAccountId"))
            assertFalse(data.has(forbidden), forbidden + " must be absent");
    }

    @Autowired com.xinyi.rongli.identity.service.SessionSwitchService switches;

    String driver(JsonNode session) {
        long account = session.path("accountId").asLong();
        jdbc.update(
                "INSERT INTO"
                    + " vi_driver_profile(account_id,qualification_status,new_assignment_enabled)"
                    + " VALUES (?,'APPROVED',1)",
                account);
        return switches.switchRole(
                        account,
                        session.path("sessionId").asLong(),
                        "DRIVER",
                        session.path("contextVersion").asLong())
                .token();
    }

    long assign(JsonNode order, JsonNode driver) {
        long d =
                jdbc.queryForObject(
                        "SELECT id FROM vi_driver_profile WHERE account_id=?",
                        Long.class,
                        driver.path("accountId").asLong());
        jdbc.update(
                "INSERT INTO vi_driver_assignment(order_id,driver_id,invite_id,accepted_at) VALUES"
                        + " (?,?,?,UTC_TIMESTAMP(3))",
                id(order),
                d,
                System.nanoTime());
        long a =
                jdbc.queryForObject(
                        "SELECT id FROM vi_driver_assignment WHERE order_id=? AND ended_at IS NULL",
                        Long.class,
                        id(order));
        jdbc.update(
                "UPDATE vi_order SET active_assignment_id=?,fulfillment_status='ACCEPTED' WHERE"
                        + " id=?",
                a,
                id(order));
        return a;
    }

    String scopedAdmin(Set<String> permissions, Map<String, Long> scopes) {
        String name = "b04_read_" + System.nanoTime();
        jdbc.update(
                "INSERT INTO sys_user(user_name,nick_name,status,del_flag,auth_version,password)"
                        + " VALUES (?,?,'0','0',0,'x')",
                name,
                name);
        long uid =
                jdbc.queryForObject(
                        "SELECT user_id FROM sys_user WHERE user_name=?", Long.class, name);
        jdbc.update("INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,100)", uid);
        var user = userService.selectUserById(uid);
        var roles = new java.util.ArrayList<com.xinyi.common.core.domain.entity.SysRole>();
        for (String p : permissions) {
            jdbc.update(
                    "INSERT INTO sys_role(role_name,role_key,role_sort,data_scope,status,del_flag)"
                            + " VALUES (?, ?,1,'2','0','0')",
                    "B04读取角色",
                    name + p);
            long rid =
                    jdbc.queryForObject(
                            "SELECT role_id FROM sys_role WHERE role_key=?", Long.class, name + p);
            jdbc.update(
                    "INSERT INTO sys_role_dept(role_id,dept_id) VALUES (?,?)",
                    rid,
                    scopes.getOrDefault(p, 100L));
            var r = new com.xinyi.common.core.domain.entity.SysRole();
            r.setRoleId(rid);
            r.setStatus("0");
            r.setDataScope("2");
            r.setPermissions(Set.of(p));
            roles.add(r);
        }
        user.setRoles(roles);
        var lu = new LoginUser(user, permissions);
        lu.setUserId(uid);
        lu.setAuthVersion(0L);
        var req = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(req));
        try {
            return tokenService.createToken(lu);
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void 车主列表详情时间轴白名单和他人订单拒绝() {
        var s = verifiedLogin("owner");
        String token = s.path("token").asText();
        var o = made(token);
        var detail = good(get(path("owner", o), token, false));
        capture("OwnerOrderView", detail);
        assertEquals(o.path("money"), detail.path("money"));
        assertEquals("测试接车地址", detail.path("pickupAddress").path("address").asText());
        assertFalse(detail.toString().contains("contactCipher"));
        var list = good(get("/api/app/owner/orders", token, false));
        assertEquals(1, list.path("total").asInt());
        capture("OwnerOrderSummary", list.path("rows").get(0));
        assertFalse(list.path("rows").get(0).has("pickupAddress"));
        jdbc.update(
                "INSERT INTO"
                    + " vi_event_log(aggregate_type,aggregate_id,order_id,action,acting_role,reason)"
                    + " VALUES ('VI_ORDER',?,?, 'INTERNAL_REVIEW','ADMIN','secret refund reason')",
                id(o),
                id(o));
        var line = good(get(path("owner", o) + "/timeline", token, false));
        capture("OrderTimelinePublic", line.path("rows").get(0));
        assertEquals(1, line.path("total").asInt());
        assertFalse(line.toString().contains("secret"));
        assertEquals("预约已创建", line.path("rows").get(0).path("displayText").asText());
        String other = verifiedLogin("other").path("token").asText();
        assertEquals(0, good(get("/api/app/owner/orders", other, false)).path("total").asInt());
        assertEquals(404, get(path("owner", o), other, false).getStatusCode().value());
        assertEquals(
                404, get(path("owner", o) + "/timeline", other, false).getStatusCode().value());
        assertTrue(
                jdbc.queryForObject(
                                "SELECT COUNT(*) FROM vi_event_log WHERE action='ORDER_DETAIL_READ'"
                                        + " AND aggregate_id=?",
                                Integer.class,
                                id(o))
                        > 0);
    }

    @Test
    void 分页状态日期过滤伪造内部字段与重复参数拒绝() {
        String token = verifiedLogin("page").path("token").asText();
        var a = made(token);
        var b = made(token);
        var first = good(get("/api/app/owner/orders?pageSize=1&pageNum=1", token, false));
        var second = good(get("/api/app/owner/orders?pageSize=1&pageNum=2", token, false));
        assertEquals(2, first.path("total").asInt());
        assertNotEquals(
                first.path("rows").get(0).path("orderNo"),
                second.path("rows").get(0).path("orderNo"));
        assertEquals(
                0,
                good(get("/api/app/owner/orders?status=COMPLETED", token, false))
                        .path("total")
                        .asInt());
        assertEquals(
                2,
                good(get(
                                "/api/app/owner/orders?startAt="
                                        + java.time.Instant.parse(a.path("slotStartAt").asText())
                                                .minusSeconds(1)
                                        + "&endAt="
                                        + java.time.Instant.parse(a.path("slotStartAt").asText())
                                                .plusSeconds(1),
                                token,
                                false))
                        .path("total")
                        .asInt());
        for (String query :
                List.of(
                        "deptId=100",
                        "visibleDeptIds=100",
                        "params.dataScope=1",
                        "pageNum=0",
                        "pageSize=101",
                        "pageNum=2147483647",
                        "status=INVALID",
                        "startAt=oops",
                        "pageNum=1&pageNum=2",
                        "startAt=2026-01-02T00:00:00Z&endAt=2026-01-01T00:00:00Z"))
            assertEquals(
                    400,
                    get("/api/app/owner/orders?" + query, token, false).getStatusCode().value(),
                    query);
    }

    @Test
    void 代驾仅本人承接活动视图与结束后密文不读取() {
        String owner = verifiedLogin("car").path("token").asText();
        var o = made(owner);
        var ds = verifiedLogin("driver");
        String dt = driver(ds);
        assertEquals(0, good(get("/api/app/driver/orders", dt, false)).path("total").asInt());
        assertEquals(404, get(path("driver", o), dt, false).getStatusCode().value());
        long aid = assign(o, ds);
        var current = good(get(path("driver", o), dt, false));
        capture("DriverOrderView", current);
        capture(
                "DriverOrderSummary",
                good(get("/api/app/driver/orders", dt, false)).path("rows").get(0));
        assertEquals("ACTIVE_ASSIGNMENT", current.path("accessPhase").asText());
        assertEquals("测试接车地址", current.path("pickupAddress").path("address").asText());
        assertFalse(current.has("money"));
        noPrivate(good(get("/api/app/driver/orders", dt, false)).path("rows").get(0));
        jdbc.update(
                "UPDATE vi_driver_assignment SET ended_at=UTC_TIMESTAMP(3),end_reason='RETURNED'"
                        + " WHERE id=?",
                aid);
        jdbc.update(
                "UPDATE vi_order SET"
                    + " active_assignment_id=NULL,returned_at=UTC_TIMESTAMP(3),fulfillment_status='COMPLETED'"
                    + " WHERE id=?",
                id(o));
        jdbc.update(
                "UPDATE vi_order_snapshot SET address_cipher=?,contact_cipher=? WHERE order_id=?",
                new byte[] {1},
                new byte[] {2},
                id(o));
        var history = good(get(path("driver", o), dt, false));
        assertEquals("HISTORY", history.path("accessPhase").asText());
        assertTrue(history.path("pickupAddress").isNull());
        assertTrue(history.path("ownerContact").isNull());
        assertTrue(history.path("driverReleased").asBoolean());
        noPrivate(good(get("/api/app/driver/orders", dt, false)).path("rows").get(0));
        var stranger = verifiedLogin("driver2");
        assertEquals(404, get(path("driver", o), driver(stranger), false).getStatusCode().value());
        assertEquals(403, get(path("owner", o), dt, false).getStatusCode().value());
    }

    @Test
    void 后台列表不能绕过详情及金额地址单独部门授权() {
        String owner = verifiedLogin("adminowner").path("token").asText();
        var o = made(owner);
        String onlyList = scopedAdmin(Set.of("biz:order:list"), Map.of());
        var list = good(get("/api/admin/orders", onlyList, true));
        capture("AdminOrderSummary", list.path("rows").get(0));
        noPrivate(list.path("rows").get(0));
        assertEquals(0, list.path("rows").get(0).path("allowedActions").size());
        assertEquals(403, get(path("admin", o), onlyList, true).getStatusCode().value());
        String reader =
                scopedAdmin(
                        Set.of(
                                "biz:order:list",
                                "biz:order:query",
                                "biz:order:finance",
                                "biz:order:address"),
                        Map.of("biz:order:finance", 101L, "biz:order:address", 101L));
        var restricted = good(get(path("admin", o), reader, true));
        assertTrue(restricted.path("money").isNull());
        assertTrue(restricted.path("pickupAddress").isNull());
        assertFalse(restricted.path("ownerContact").path("phoneMask").asText().matches("\\d{11}"));
        String full = adminToken();
        var detail = good(get(path("admin", o), full, true));
        capture("AdminOrderView", detail);
        assertTrue(detail.path("money").isObject());
        assertTrue(detail.path("pickupAddress").isObject());
        assertEquals("100", detail.path("authoritativeDeptId").asText());
        String wrong =
                scopedAdmin(
                        Set.of("biz:order:list", "biz:order:query"),
                        Map.of("biz:order:list", 101L, "biz:order:query", 101L));
        assertEquals(0, good(get("/api/admin/orders", wrong, true)).path("total").asInt());
        assertEquals(404, get(path("admin", o), wrong, true).getStatusCode().value());
        var crossRealm = get(path("admin", o), owner, false);
        assertEquals(401, ViJson.tree(crossRealm.getBody()).path("code").asInt());
        assertEquals(401, crossRealm.getStatusCode().value());
        assertTrue(ViJson.tree(crossRealm.getBody()).path("data").isObject());
    }

    @Test
    void 旧会话撤销后读取失败且审计不含敏感字段() {
        var s = verifiedLogin("revoked");
        String token = s.path("token").asText();
        var o = made(token);
        good(get(path("owner", o), token, false));
        jdbc.update(
                "UPDATE vi_account_security SET session_epoch=session_epoch+1 WHERE account_id=?",
                s.path("accountId").asLong());
        assertEquals(401, get(path("owner", o), token, false).getStatusCode().value());
        var logs =
                jdbc.queryForList(
                        "SELECT before_summary,after_summary,reason FROM vi_event_log WHERE"
                                + " aggregate_type='VI_ORDER' AND aggregate_id=?",
                        id(o));
        assertFalse(logs.toString().contains("测试接车地址"));
    }

    void capture(String name, JsonNode data) {
        try {
            Files.writeString(
                    repoRoot().resolve(".vi-local/b04-" + name + ".json"), data.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    record StationActor(String token, long userId, long roleId) {}

    StationActor stationActor(JsonNode session, long station) {
        String name = "b04_station_" + System.nanoTime();
        jdbc.update(
                "INSERT INTO sys_user(user_name,nick_name,status,del_flag,auth_version,password)"
                        + " VALUES (?,?,'0','0',0,'x')",
                name,
                "测试员工");
        long uid =
                jdbc.queryForObject(
                        "SELECT user_id FROM sys_user WHERE user_name=?", Long.class, name);
        jdbc.update("INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,100)", uid);
        jdbc.update(
                "INSERT INTO"
                    + " vi_employee_binding(account_id,sys_user_id,bound_at,app_id,phone_lookup,last_phone_verified_at,employee_auth_version)"
                    + " SELECT id,?,UTC_TIMESTAMP(3),app_id,phone_lookup,phone_verified_at,0 FROM"
                    + " vi_account WHERE id=?",
                uid,
                session.path("accountId").asLong());
        jdbc.update(
                "INSERT INTO vi_station_grant(sys_user_id,station_id,granted_by,reason) VALUES"
                        + " (?,?,1,'隔离测试')",
                uid,
                station);
        jdbc.update(
                "INSERT INTO sys_role(role_name,role_key,role_sort,data_scope,status,del_flag)"
                        + " VALUES ('B04工作测试',?,1,'2','0','0')",
                name);
        long rid =
                jdbc.queryForObject(
                        "SELECT role_id FROM sys_role WHERE role_key=?", Long.class, name);
        jdbc.update("INSERT INTO sys_role_dept(role_id,dept_id) VALUES (?,100)", rid);
        jdbc.update("INSERT INTO sys_user_role(user_id,role_id) VALUES (?,?)", uid, rid);
        for (long mid : List.of(2970400L, 2970401L))
            jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES (?,?)", rid, mid);
        return new StationActor(
                switches.switchStation(
                                session.path("accountId").asLong(),
                                session.path("sessionId").asLong(),
                                station,
                                session.path("contextVersion").asLong())
                        .token(),
                uid,
                rid);
    }

    @Test
    void 检测端当前站点部门功能交集及撤权立即生效() {
        String owner = verifiedLogin("stationowner").path("token").asText();
        var o = made(owner);
        var actor = stationActor(verifiedLogin("staff"), 996402);
        var detail = good(get(path("station", o), actor.token(), false));
        noPrivate(detail);
        capture("StationOrderView", detail);
        var list = good(get("/api/app/station/orders", actor.token(), false));
        assertEquals(1, list.path("total").asInt());
        capture("StationOrderSummary", list.path("rows").get(0));
        noPrivate(list.path("rows").get(0));
        var other = stationActor(verifiedLogin("staff2"), 996401);
        assertEquals(
                0,
                good(get("/api/app/station/orders", other.token(), false)).path("total").asInt());
        assertEquals(404, get(path("station", o), other.token(), false).getStatusCode().value());
        jdbc.update("UPDATE vi_order SET dept_id=101 WHERE id=?", id(o));
        assertEquals(
                0,
                good(get("/api/app/station/orders", actor.token(), false)).path("total").asInt());
        assertEquals(404, get(path("station", o), actor.token(), false).getStatusCode().value());
        jdbc.update("UPDATE vi_order SET dept_id=100 WHERE id=?", id(o));
        jdbc.update(
                "DELETE FROM sys_role_menu WHERE role_id=? AND menu_id=2970401", actor.roleId());
        assertEquals(403, get(path("station", o), actor.token(), false).getStatusCode().value());
        assertEquals(
                0,
                good(get("/api/app/station/orders", actor.token(), false))
                        .path("rows")
                        .get(0)
                        .path("allowedActions")
                        .size());
        jdbc.update("UPDATE vi_station_grant SET enabled=0 WHERE sys_user_id=?", actor.userId());
        assertTrue(
                get("/api/app/station/orders", actor.token(), false)
                        .getStatusCode()
                        .is4xxClientError());
    }

    @Test
    void 改派后原代驾仍可看历史但不能看到新接车地址() {
        String owner = verifiedLogin("reassignowner").path("token").asText();
        var o = made(owner);
        var first = verifiedLogin("first");
        String ft = driver(first);
        long a = assign(o, first);
        jdbc.update(
                "UPDATE vi_driver_assignment SET"
                        + " ended_at=UTC_TIMESTAMP(3),end_reason='DRIVER_CANCEL' WHERE id=?",
                a);
        var next = verifiedLogin("next");
        String nt = driver(next);
        assign(o, next);
        var history = good(get(path("driver", o), ft, false));
        assertEquals("HISTORY", history.path("accessPhase").asText());
        assertTrue(history.path("pickupAddress").isNull());
        assertTrue(history.path("ownerContact").isNull());
        assertEquals(
                "ACTIVE_ASSIGNMENT",
                good(get(path("driver", o), nt, false)).path("accessPhase").asText());
        capture("DriverOrderView-history", history);
    }

    @Test
    void 后台任职相交读取且字段权限撤销旧令牌失效() {
        String owner = verifiedLogin("revadminowner").path("token").asText();
        var o = made(owner);
        String token =
                scopedAdmin(
                        Set.of("biz:order:list", "biz:order:query", "biz:order:finance"), Map.of());
        var detail = good(get(path("admin", o), token, true));
        assertTrue(detail.path("money").isObject());
        assertTrue(detail.path("pickupAddress").isNull());
        long uid =
                jdbc.queryForObject(
                        "SELECT MAX(user_id) FROM sys_user WHERE user_name LIKE 'b04_read_%'",
                        Long.class);
        jdbc.update("INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,101)", uid);
        assertEquals(1, good(get("/api/admin/orders", token, true)).path("total").asInt());
        jdbc.update("UPDATE sys_user SET auth_version=auth_version+1 WHERE user_id=?", uid);
        assertEquals(401, get(path("admin", o), token, true).getStatusCode().value());
    }

    @Test
    void 读取最新支付售后里程争议及幂等重放而不暴露内部说明() {
        String token = verifiedLogin("liveprojection").path("token").asText();
        var input = orderInput(quote(token));
        String key = UUID.randomUUID().toString();
        var o = good(create(input, token, key));
        long id = id(o);
        var ds = verifiedLogin("mileagedriver");
        driver(ds);
        long aid = assign(o, ds);
        jdbc.update(
                "UPDATE vi_payment_order SET status='SUCCESS',paid_fen=total_fen,refunded_fen=1200"
                        + " WHERE order_id=?",
                id);
        jdbc.update("UPDATE vi_order SET cancel_status='APPROVED_WAIT_RETURN' WHERE id=?", id);
        jdbc.update(
                "INSERT INTO"
                    + " vi_aftersale_case(case_no,order_id,payment_id,kind,case_status,review_status,"
                    + "requested_fen,approved_fen,return_state,refund_state,applicant_type,applicant_id,reason)"
                    + " SELECT"
                    + " ?,?,id,'CANCEL_REFUND','EXECUTING','APPROVED',1200,1200,'WAIT_RETURN','WAIT_RETURN','ACCOUNT',?,'内部售后备注不外发'"
                    + " FROM vi_payment_order WHERE order_id=?",
                "B04" + System.nanoTime(),
                id,
                ds.path("accountId").asLong(),
                id);
        jdbc.update(
                "INSERT INTO vi_dispute(order_id,type,status,description) VALUES"
                        + " (?,'SERVICE','OPEN','内部争议说明不外发')",
                id);
        for (int node : List.of(1, 4)) {
            jdbc.update(
                    "INSERT INTO"
                        + " vi_evidence_group(order_id,assignment_id,node_no,status,mileage_km,mileage_reason)"
                        + " VALUES (?,?,?,'SEALED',?,'内部仪表说明')",
                    id,
                    aid,
                    node,
                    node == 1 ? 100.1 : 123.4);
            long group =
                    jdbc.queryForObject(
                            "SELECT id FROM vi_evidence_group WHERE assignment_id=? AND node_no=?",
                            Long.class,
                            aid,
                            node);
            jdbc.update(
                    "INSERT INTO"
                        + " vi_handover(order_id,assignment_id,handover_type,holder_account_id,verifier_account_id,"
                        + "code_mac,code_cipher,generation_no,status,expires_at,confirmed_at,evidence_group_id)"
                        + " VALUES"
                        + " (?,?,?,?,?,UNHEX(REPEAT('00',32)),UNHEX('01'),1,'CONFIRMED',UTC_TIMESTAMP(3),UTC_TIMESTAMP(3),?)",
                    id,
                    aid,
                    node == 1 ? "OWNER_TO_DRIVER" : "DRIVER_TO_OWNER",
                    ds.path("accountId").asLong(),
                    ds.path("accountId").asLong(),
                    group);
        }
        var detail = good(get(path("owner", o), token, false));
        assertEquals("SUCCESS", detail.path("paymentStatus").asText());
        assertEquals(1200, detail.path("money").path("refundedAmount").asLong());
        assertEquals("WAIT_RETURN", detail.path("refundStatus").asText());
        assertEquals("OPEN", detail.path("disputeStatus").asText());
        assertEquals("23.3", detail.path("mileage").path("distanceKm").asText());
        assertEquals(1200, detail.path("afterSale").path("approvedAmount").asLong());
        assertFalse(detail.toString().contains("内部"));
        assertEquals(detail, good(create(input, token, key)));
        capture("OwnerOrderView-live", detail);
        String limited = scopedAdmin(Set.of("biz:order:query"), Map.of());
        var masked = good(get(path("admin", o), limited, true));
        assertTrue(masked.path("money").isNull());
        assertTrue(masked.path("afterSale").path("approvedAmount").isNull());
    }
}
