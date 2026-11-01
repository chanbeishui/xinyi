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
class B02BookingIT {

    private static com.sun.net.httpserver.HttpServer routeServer;
    private static final java.util.concurrent.atomic.AtomicInteger routeCalls=new java.util.concurrent.atomic.AtomicInteger();
    private static volatile boolean routeFailure;
    private static Process simProcess;
    private static int simPort;
    private static String simSecret;
    private static Path simDb;
    private static final java.net.http.HttpClient SIM_HTTP = java.net.http.HttpClient.newHttpClient();

    static {
        try {
            routeServer=com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
            routeServer.createContext("/route",e->{routeCalls.incrementAndGet();byte[] data=(routeFailure?"{\"status\":120}":"{\"status\":0,\"result\":{\"routes\":[{\"distance\":12345,\"duration\":20}]}}").getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(200,data.length);e.getResponseBody().write(data);e.close();});routeServer.start();
            startSimulator();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void providerProps(DynamicPropertyRegistry registry) {
        registry.add("vi.providers.base-url", () -> "http://127.0.0.1:" + simPort);
        registry.add("vi.providers.secret", () -> simSecret);
        registry.add("vi.crypto.data-key", () -> "b02-test-data-key-0123456789abcdef");
        registry.add("vi.maps.tencent.key",()->"test-only");
        registry.add("vi.maps.tencent.endpoint",()->"http://127.0.0.1:"+routeServer.getAddress().getPort()+"/route");
        registry.add("vi.lookup-key", () -> "b02-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("b02-sim-", ".sqlite");
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
        if(routeServer!=null)routeServer.stop(0);
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

    @BeforeEach void fixtures() {
        assertEquals("xinyi_vi_it_e07",jdbc.queryForObject("SELECT DATABASE()",String.class));
        assertEquals("XINYI_VEHICLE_ISOLATED",jdbc.queryForObject("SELECT kind FROM test_environment_guard WHERE id=1",String.class));
        routeCalls.set(0);routeFailure=false;
        jdbc.execute("DELETE FROM vi_quote");jdbc.execute("DELETE FROM vi_price_rule");jdbc.execute("DELETE FROM vi_idempotency");
        jdbc.execute("DELETE FROM vi_quota_slot WHERE station_id IN (996001,996002)");jdbc.execute("DELETE FROM vi_station WHERE id IN (996001,996002)");
        jdbc.execute("INSERT INTO vi_station (id,name,dept_id,address,lng,lat,service_radius_meter,supported_vehicle_codes,business_hours,enabled) VALUES (996001,'B02甲',100,'测试甲',110,21,100000,'[\"B02_CAR\"]','{}',1),(996002,'B02乙',100,'测试乙',110.01,21,100000,'[\"B02_CAR\"]','{}',1)");
        jdbc.execute("DELETE FROM sys_dict_data WHERE dict_value IN ('B02_CAR','B02_ANNUAL')");jdbc.execute("INSERT INTO sys_dict_data(dict_label,dict_value,dict_type,status) VALUES ('测试车型','B02_CAR','vi_vehicle_type','0'),('测试检测','B02_ANNUAL','vi_inspection_type','0')");
        for(var entry:Map.of("vi.biz.booking.enabled","true","vi.biz.terms.version","b02v1","vi.biz.terms.text","测试协议","vi.biz.privacy.version","b02v1","vi.biz.privacy.text","测试隐私","vi.biz.booking.handover.seconds","600","vi.biz.booking.route-buffer.seconds","300","vi.biz.booking.min-lead.seconds","60","vi.biz.booking.quote-ttl.seconds","300","vi.config.version","1").entrySet()){
          jdbc.update("DELETE FROM sys_config WHERE config_key=?",entry.getKey());jdbc.update("INSERT INTO sys_config(config_name,config_key,config_value,config_type) VALUES (?,?,?,'N')",entry.getKey(),entry.getKey(),entry.getValue());
        }
        start=java.time.Instant.now().plusSeconds(172800).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        for(long station:List.of(996001L,996002L))for(String type:List.of("PICKUP","STATION_RECEIVE"))jdbc.update("INSERT INTO vi_quota_slot(quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota,confirmed_quota,consumed_quota,enabled) VALUES (?,?, ?,?,?,5,0,0,0,1)",type,"station:"+station,station,java.sql.Timestamp.from(start),java.sql.Timestamp.from(start.plusSeconds(type.equals("PICKUP")?3600:7200)));
        slotId=jdbc.queryForObject("SELECT id FROM vi_quota_slot WHERE station_id=996001 AND quota_type='PICKUP'",Long.class);
        publishPrice();
    }
    @org.junit.jupiter.api.AfterEach void removeOwnSupply() {
        assertEquals("xinyi_vi_it_e07",jdbc.queryForObject("SELECT DATABASE()",String.class));
        jdbc.execute("DELETE FROM vi_quota_slot WHERE station_id IN (996001,996002)");
        jdbc.execute("DELETE FROM vi_station WHERE id IN (996001,996002)");
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



    java.time.Instant start;long slotId;
    @Autowired com.xinyi.rongli.booking.service.QuoteService quotes;
    @Autowired com.xinyi.rongli.address.service.AddressCrypto crypto;
    String reviewer(){
        String name="b02_"+System.nanoTime();jdbc.update("INSERT INTO sys_user(user_name,nick_name,status,del_flag,auth_version,password) VALUES (?,?,'0','0',0,'x')",name,"测试审核");
        long id=jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name=?",Long.class,name);jdbc.update("INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,100)",id);
        SysUser user=userService.selectUserById(id);var role=new com.xinyi.common.core.domain.entity.SysRole();role.setRoleId(id);role.setStatus("0");role.setDataScope("3");role.setPermissions(Set.of("biz:price:audit"));user.setRoles(List.of(role));
        LoginUser login=new LoginUser(user,Set.of("biz:price:audit"));login.setUserId(id);login.setAuthVersion(0L);
        var request=new org.springframework.mock.web.MockHttpServletRequest();request.addHeader("User-Agent","b02-test");org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));try{return tokenService.createToken(login);}finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
    }
    JsonNode adminPost(String path,Map<String,Object> body,String token){HttpHeaders h=new HttpHeaders();h.setBearerAuth(token);h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);h.set("X-Client-Type","admin");h.set("Idempotency-Key",UUID.randomUUID().toString());var r=rest.exchange(url(path),HttpMethod.POST,new HttpEntity<>(body,h),String.class);assertEquals(200,r.getStatusCode().value(),r.getBody());return ViJson.tree(r.getBody()).get("data");}
    void publishPrice(){
        String admin=adminToken();var input=new HashMap<String,Object>();input.putAll(Map.of("ownerDeptId","100","environment","TEST","name","B02计价","vehicleTypeCode","B02_CAR","inspectionTypeCode","B02_ANNUAL","inspectionFee",20000,"driverBaseFee",3000,"includedDistanceMeter",1000,"distanceUnitMeter",1000,"distanceUnitFee",100));input.put("distanceRounding","CEIL_UNIT");input.put("routeBasis","ONE_WAY");input.put("effectiveFrom",start.minusSeconds(3600).toString());
        var row=adminPost("/api/admin/prices",input,admin);String path="/api/admin/prices/"+row.get("ruleId").asText();row=adminPost(path+"/submit",Map.of("expectedVersion",0,"reason","测试"),admin);row=adminPost(path+"/audit",Map.of("expectedVersion",1,"decision","APPROVE","opinion","独立测试审核"),reviewer());adminPost(path+"/publish",Map.of("expectedVersion",2,"reason","测试发布"),admin);
    }
    Map<String,Object> input(){return new HashMap<>(Map.of("slotId",Long.toString(slotId),"stationId","996002","inspectionTypeCode","B02_ANNUAL","vehicleDraft",Map.of("plateNo","粤A12345","vehicleTypeCode","B02_CAR"),"addressDraft",Map.of("address","测试接车地址","location",Map.of("lng",110,"lat",21.01,"coordinateSystem","GCJ02"))));}
    ResponseEntity<String> post(String suffix,Map<String,Object> body,String token,String key){var r=mini(body,token);HttpHeaders h=new HttpHeaders();h.putAll(r.getHeaders());h.set("Idempotency-Key",key);return rest.exchange(url("/api/app/owner/orders/"+suffix),HttpMethod.POST,new HttpEntity<>(body,h),String.class);}
    JsonNode good(ResponseEntity<String> r){assertEquals(200,r.getStatusCode().value(),r.getBody());return ViJson.tree(r.getBody()).get("data");}
    @Test void 车主选站真实报价快照且不保存草稿不扣额度(){
        var session=login("OWNER_A");String token=session.get("token").asText();var input=input();var selection=new HashMap<>(input);selection.remove("stationId");
        var candidates=good(post("stations",selection,token,UUID.randomUUID().toString()));assertTrue(candidates.size()>=2);
        int vehicles=jdbc.queryForObject("SELECT COUNT(*) FROM vi_vehicle",Integer.class),addresses=jdbc.queryForObject("SELECT COUNT(*) FROM vi_address",Integer.class);
        var quote=good(post("quote",input,token,UUID.randomUUID().toString()));assertEquals("996002",quote.get("stationId").asText());assertEquals(24200,quote.get("totalAmount").asLong());
        assertEquals(vehicles,jdbc.queryForObject("SELECT COUNT(*) FROM vi_vehicle",Integer.class));assertEquals(addresses,jdbc.queryForObject("SELECT COUNT(*) FROM vi_address",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT SUM(held_quota) FROM vi_quota_slot WHERE station_id IN (996001,996002)",Integer.class));
        byte[] address=jdbc.queryForObject("SELECT address_snapshot_cipher FROM vi_quote WHERE id=?",byte[].class,quote.get("quoteId").asText());assertTrue(crypto.decrypt(address).contains("测试接车地址"));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE aggregate_type='VI_QUOTE' AND aggregate_id=? AND action='QUOTE_CREATE'",Integer.class,quote.get("quoteId").asText()));
    }
    @Test void 幂等重放不再调用地图且过期和配置变化拒绝(){
        var session=login("OWNER_A");String token=session.get("token").asText(),key=UUID.randomUUID().toString();var input=input();var quote=good(post("quote",input,token,key));int calls=routeCalls.get();assertEquals(quote,good(post("quote",input,token,key)));assertEquals(calls,routeCalls.get());
        var changed=new HashMap<>(input);changed.put("stationId","996001");assertEquals(409,post("quote",changed,token,key).getStatusCode().value());
        jdbc.update("UPDATE vi_quote SET expires_at=UTC_TIMESTAMP(3)-INTERVAL 1 SECOND WHERE id=?",quote.get("quoteId").asText());assertEquals(409,post("quote",input,token,key).getStatusCode().value());
        key=UUID.randomUUID().toString();quote=good(post("quote",input,token,key));jdbc.update("UPDATE sys_config SET config_value='2' WHERE config_key='vi.config.version'");assertEquals(409,post("quote",input,token,key).getStatusCode().value());
    }
    @Test void 地图失败接收不足和伪造输入均不产生报价(){
        String token=login("OWNER_A").get("token").asText();var input=input();routeFailure=true;assertEquals(503,post("quote",input,token,UUID.randomUUID().toString()).getStatusCode().value());routeFailure=false;
        jdbc.update("UPDATE vi_quota_slot SET held_quota=5 WHERE station_id=996002 AND quota_type='STATION_RECEIVE'");assertEquals(422,post("quote",input,token,UUID.randomUUID().toString()).getStatusCode().value());
        input.put("totalAmount",0);assertEquals(400,post("quote",input,token,UUID.randomUUID().toString()).getStatusCode().value());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quote",Integer.class));
    }
    JsonNode createResource(String path,Map<String,Object> body,String token){var r=rest.exchange(url(path),HttpMethod.POST,mini(body,token),String.class);return good(r);}
    @Test void 四组合与本人归属及输入修改失效(){
        var session=login("OWNER_A");String token=session.get("token").asText();String other=login("OWNER_B").get("token").asText();
        var base=input();JsonNode v=createResource("/api/app/owner/vehicles",(Map<String,Object>)base.get("vehicleDraft"),token);
        JsonNode a=createResource("/api/app/owner/addresses",(Map<String,Object>)base.get("addressDraft"),token);
        String quoteId=null;
        for(boolean vehicleId:List.of(false,true))for(boolean addressId:List.of(false,true)){
            var body=new HashMap<>(base);if(vehicleId){body.remove("vehicleDraft");body.put("vehicleId",v.get("vehicleId").asText());}if(addressId){body.remove("addressDraft");body.put("addressId",a.get("addressId").asText());}
            var quoted=good(post("quote",body,token,UUID.randomUUID().toString()));quoteId=quoted.get("quoteId").asText();
            if(vehicleId||addressId)assertEquals(404,post("quote",body,other,UUID.randomUUID().toString()).getStatusCode().value());
        }
        long owner=jdbc.queryForObject("SELECT owner_account_id FROM vi_quote WHERE id=?",Long.class,quoteId);
        final String savedId=quoteId;assertNotNull(quotes.validateForOrder(owner,quoteId));
        jdbc.update("UPDATE vi_address SET version=version+1 WHERE id=?",a.get("addressId").asText());
        assertThrows(com.xinyi.rongli.platform.error.ViException.class,()->quotes.validateForOrder(owner,savedId));
        var both=new HashMap<>(base);both.put("vehicleId",v.get("vehicleId").asText());assertEquals(400,post("quote",both,token,UUID.randomUUID().toString()).getStatusCode().value());
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quote",Integer.class));
    }
    @Test void 并发相同意图只保存一份报价() throws Exception {
        String token=login("OWNER_A").get("token").asText(),key=UUID.randomUUID().toString();var body=input();var pool=java.util.concurrent.Executors.newFixedThreadPool(2);var gate=new CountDownLatch(1);
        try{var a=pool.submit(()->{gate.await();return post("quote",body,token,key);});var b=pool.submit(()->{gate.await();return post("quote",body,token,key);});gate.countDown();assertEquals(good(a.get(15,TimeUnit.SECONDS)),good(b.get(15,TimeUnit.SECONDS)));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quote",Integer.class));}finally{pool.shutdownNow();}
    }
    @Test void 审计和发件箱故障使报价与幂等整体回滚(){
        String token=login("OWNER_A").get("token").asText(),trigger="b02_fault_"+UUID.randomUUID().toString().replace("-","");
        int before=jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='QUOTE_CREATE'",Integer.class);
        jdbc.execute("CREATE TRIGGER "+trigger+" BEFORE INSERT ON vi_outbox FOR EACH ROW BEGIN IF NEW.event_type='QUOTE_CREATED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='B02_TEST'; END IF; END");
        String key=UUID.randomUUID().toString();
        try{assertEquals(500,post("quote",input(),token,key).getStatusCode().value());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quote",Integer.class));assertEquals(before,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='QUOTE_CREATE'",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM vi_idempotency WHERE request_key=?",Integer.class,key));}finally{jdbc.execute("DROP TRIGGER "+trigger);}
        good(post("quote",input(),token,key));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quote",Integer.class));
    }

}
