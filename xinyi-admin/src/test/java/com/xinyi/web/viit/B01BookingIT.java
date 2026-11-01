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
import static org.junit.jupiter.api.Assertions.assertTrue;

/** B01真实HTTP/MySQL候选时段验证，登录仅外部微信使用测试模拟器。 */
@SpringBootTest(classes = RuoYiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
                "vi.environment=integration",
                "vi.providers.mode=SIMULATOR",
                "vi.outbox-relay-enabled=false",
                "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class B01BookingIT {

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
        registry.add("vi.crypto.data-key", () -> "b01-test-data-key-0123456789abcdef");
        registry.add("vi.lookup-key", () -> "b01-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("b01-sim-", ".sqlite");
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

    @BeforeEach void clean() {
        assertEquals("xinyi_vi_it_e07",jdbc.queryForObject("SELECT DATABASE()",String.class));
        assertEquals("XINYI_VEHICLE_ISOLATED",jdbc.queryForObject("SELECT kind FROM test_environment_guard WHERE id=1",String.class));
        jdbc.execute("DELETE FROM vi_quota_slot WHERE scope_key LIKE 'b01:%'");
        jdbc.execute("DELETE FROM vi_station WHERE id IN (995001,995002)");
        jdbc.execute("INSERT INTO vi_station (id,name,dept_id,address,lng,lat,service_radius_meter,supported_vehicle_codes,business_hours,enabled) VALUES (995001,'B01测试',100,'测试',110,21,1000,'[]','{}',1),(995002,'B01测试',100,'测试',110,21,1000,'[]','{}',1)");
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
        h.set("Idempotency-Key", UUID.randomUUID().toString());
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
        return new HttpEntity<>(body, h);
    }

    private JsonNode login(String persona) {
        String run = (String) simPost("/control/runs", Map.of()).get("runId");
        String code = (String) simPost("/control/codes", Map.of("runId", run, "persona", persona, "kind", "wechat")).get("code");
        ResponseEntity<String> r = rest.exchange(url("/api/common/login/wechat"), HttpMethod.POST,
                mini(Map.of("code", code), null), String.class);
        assertEquals(200, r.getStatusCode().value(), "登录应200 " + r.getBody());
        return ViJson.tree(r.getBody()).get("data");
    }


    String day=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).plusDays(2).toString();
    void slot(long station,String type,int total,int held,boolean enabled) {
        jdbc.update("INSERT INTO vi_quota_slot (quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota,confirmed_quota,consumed_quota,enabled) VALUES (?,?,?,CONVERT_TZ(CONCAT(?,' 10:00:00'),'+08:00','+00:00'),CONVERT_TZ(CONCAT(?,' 11:00:00'),'+08:00','+00:00'),?,?,0,0,?)",type,"b01:"+UUID.randomUUID(),station,day,day,total,held,enabled);
    }
    ResponseEntity<String> get(String token,String extra){return rest.exchange(url("/api/app/owner/schedule/slots?date="+day+extra),HttpMethod.GET,mini(null,token),String.class);}
    @Test void 同时段合并且不暴露站点或扣减名额() {
        slot(995001,"PICKUP",5,1,true);slot(995002,"PICKUP",5,2,true);
        String token=login("OWNER_A").get("token").asText();var result=get(token,"");assertEquals(200,result.getStatusCode().value(),result.getBody());
        JsonNode data=ViJson.tree(result.getBody());assertEquals(1,data.get("total").asInt());JsonNode row=data.get("rows").get(0);
        assertTrue(row.get("candidateOnly").asBoolean());assertEquals("CANDIDATE",row.get("status").asText());assertFalse(row.has("stationId"));assertFalse(row.has("remainQuota"));
        assertTrue(row.get("startAt").asText().endsWith("02:00:00Z"));get(token,"");
        assertEquals(3,jdbc.queryForObject("SELECT SUM(held_quota) FROM vi_quota_slot WHERE scope_key LIKE 'b01:%'",Integer.class));
    }
    @Test void 停约满额停用站点和接收额度不充当候选() {
        String token=login("OWNER_A").get("token").asText();slot(995001,"STATION_RECEIVE",5,0,true);slot(995001,"PICKUP",5,5,true);slot(995001,"PICKUP",5,0,false);slot(995002,"PICKUP",5,0,true);
        jdbc.update("UPDATE vi_station SET enabled=0 WHERE id=995002");assertEquals(0,ViJson.tree(get(token,"").getBody()).get("total").asInt());
    }
    @Test void 非法筛选与跨域身份不能绕过() {
        String token=login("OWNER_A").get("token").asText();
        for(String q:List.of("&pageSize=0","&pageNum=-1","&serviceAreaCode=forged","&params.dataScope=1"))assertEquals(400,get(token,q).getStatusCode().value());
        assertEquals(401,get(null,"").getStatusCode().value());assertTrue(get(adminToken(),"").getStatusCode().value()!=200);
    }
}
