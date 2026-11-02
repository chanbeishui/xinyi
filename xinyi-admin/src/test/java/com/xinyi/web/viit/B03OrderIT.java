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

/** B03真实建单、并发占额、支付门禁与事务回滚；仅外部微信/地图使用协议替身。 */
@SpringBootTest(classes = RuoYiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
                "vi.environment=integration",
                "vi.providers.mode=SIMULATOR",
                "vi.outbox-relay-enabled=false",
                "server.address=127.0.0.1"
        })
@ActiveProfiles("druid")
class B03OrderIT {

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
        registry.add("vi.crypto.data-key", () -> "b03-test-data-key-0123456789abcdef");
        registry.add("vi.maps.tencent.key",()->"test-only");
        registry.add("vi.maps.tencent.endpoint",()->"http://127.0.0.1:"+routeServer.getAddress().getPort()+"/route");
        registry.add("vi.lookup-key", () -> "b03-test-lookup-key-0123456789abcdef");
    }

    private static void startSimulator() throws Exception {
        byte[] s = new byte[32];
        new SecureRandom().nextBytes(s);
        StringBuilder sb = new StringBuilder();
        for (byte b : s) {
            sb.append(String.format("%02x", b));
        }
        simSecret = sb.toString();
        simDb = Files.createTempFile("b03-sim-", ".sqlite");
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
        cleanOrders();
        seedPaymentPolicy();
        jdbc.execute("DELETE FROM vi_quote");jdbc.execute("DELETE FROM vi_price_rule");jdbc.execute("DELETE FROM vi_idempotency");
        jdbc.execute("DELETE FROM vi_quota_slot WHERE station_id IN (996301,996302)");jdbc.execute("DELETE FROM vi_station WHERE id IN (996301,996302)");
        jdbc.execute("INSERT INTO vi_station (id,name,dept_id,address,lng,lat,service_radius_meter,supported_vehicle_codes,business_hours,enabled) VALUES (996301,'B03甲',100,'测试甲',110,21,100000,'[\"B03_CAR\"]','{}',1),(996302,'B03乙',100,'测试乙',110.01,21,100000,'[\"B03_CAR\"]','{}',1)");
        jdbc.execute("DELETE FROM sys_dict_data WHERE dict_value IN ('B03_CAR','B03_ANNUAL')");jdbc.execute("INSERT INTO sys_dict_data(dict_label,dict_value,dict_type,status) VALUES ('测试车型','B03_CAR','vi_vehicle_type','0'),('测试检测','B03_ANNUAL','vi_inspection_type','0')");
        for(var entry:Map.of("vi.biz.booking.enabled","true","vi.biz.terms.version","b03v1","vi.biz.terms.text","测试协议","vi.biz.privacy.version","b03v1","vi.biz.privacy.text","测试隐私","vi.biz.booking.handover.seconds","600","vi.biz.booking.route-buffer.seconds","300","vi.biz.booking.min-lead.seconds","60","vi.biz.booking.quote-ttl.seconds","300","vi.config.version","1").entrySet()){
          jdbc.update("DELETE FROM sys_config WHERE config_key=?",entry.getKey());jdbc.update("INSERT INTO sys_config(config_name,config_key,config_value,config_type) VALUES (?,?,?,'N')",entry.getKey(),entry.getKey(),entry.getValue());
        }
        start=java.time.Instant.now().plusSeconds(172800).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        for(long station:List.of(996301L,996302L))for(String type:List.of("PICKUP","STATION_RECEIVE"))jdbc.update("INSERT INTO vi_quota_slot(quota_type,scope_key,station_id,starts_at,ends_at,total_quota,held_quota,confirmed_quota,consumed_quota,enabled) VALUES (?,?, ?,?,?,5,0,0,0,1)",type,"station:"+station,station,java.sql.Timestamp.from(start),java.sql.Timestamp.from(start.plusSeconds(type.equals("PICKUP")?3600:7200)));
        slotId=jdbc.queryForObject("SELECT id FROM vi_quota_slot WHERE station_id=996301 AND quota_type='PICKUP'",Long.class);
        publishPrice();
    }
    @org.junit.jupiter.api.AfterEach void removeOwnSupply() {
        assertEquals("xinyi_vi_it_e07",jdbc.queryForObject("SELECT DATABASE()",String.class));
        cleanOrders();
        cleanPolicy();
        jdbc.execute("DELETE FROM vi_quota_slot WHERE station_id IN (996301,996302)");
        jdbc.execute("DELETE FROM vi_station WHERE id IN (996301,996302)");
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
        String name="b03_"+System.nanoTime();jdbc.update("INSERT INTO sys_user(user_name,nick_name,status,del_flag,auth_version,password) VALUES (?,?,'0','0',0,'x')",name,"测试审核");
        long id=jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name=?",Long.class,name);jdbc.update("INSERT INTO sys_user_dept(user_id,dept_id) VALUES (?,100)",id);
        SysUser user=userService.selectUserById(id);var role=new com.xinyi.common.core.domain.entity.SysRole();role.setRoleId(id);role.setStatus("0");role.setDataScope("3");role.setPermissions(Set.of("biz:price:audit"));user.setRoles(List.of(role));
        LoginUser login=new LoginUser(user,Set.of("biz:price:audit"));login.setUserId(id);login.setAuthVersion(0L);
        var request=new org.springframework.mock.web.MockHttpServletRequest();request.addHeader("User-Agent","b03-test");org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));try{return tokenService.createToken(login);}finally{org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
    }
    JsonNode adminPost(String path,Map<String,Object> body,String token){HttpHeaders h=new HttpHeaders();h.setBearerAuth(token);h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);h.set("X-Client-Type","admin");h.set("Idempotency-Key",UUID.randomUUID().toString());var r=rest.exchange(url(path),HttpMethod.POST,new HttpEntity<>(body,h),String.class);assertEquals(200,r.getStatusCode().value(),r.getBody());return ViJson.tree(r.getBody()).get("data");}
    void publishPrice(){
        String admin=adminToken();var input=new HashMap<String,Object>();input.putAll(Map.of("ownerDeptId","100","environment","TEST","name","B03计价","vehicleTypeCode","B03_CAR","inspectionTypeCode","B03_ANNUAL","inspectionFee",20000,"driverBaseFee",3000,"includedDistanceMeter",1000,"distanceUnitMeter",1000,"distanceUnitFee",100));input.put("distanceRounding","CEIL_UNIT");input.put("routeBasis","ONE_WAY");input.put("effectiveFrom",start.minusSeconds(3600).toString());
        var row=adminPost("/api/admin/prices",input,admin);String path="/api/admin/prices/"+row.get("ruleId").asText();row=adminPost(path+"/submit",Map.of("expectedVersion",0,"reason","测试"),admin);row=adminPost(path+"/audit",Map.of("expectedVersion",1,"decision","APPROVE","opinion","独立测试审核"),reviewer());adminPost(path+"/publish",Map.of("expectedVersion",2,"reason","测试发布"),admin);
    }
    Map<String,Object> input(){return new HashMap<>(Map.of("slotId",Long.toString(slotId),"stationId","996302","inspectionTypeCode","B03_ANNUAL","vehicleDraft",Map.of("plateNo","粤A12345","vehicleTypeCode","B03_CAR"),"addressDraft",Map.of("address","测试接车地址","location",Map.of("lng",110,"lat",21.01,"coordinateSystem","GCJ02"))));}
    ResponseEntity<String> post(String suffix,Map<String,Object> body,String token,String key){var r=mini(body,token);HttpHeaders h=new HttpHeaders();h.putAll(r.getHeaders());h.set("Idempotency-Key",key);return rest.exchange(url("/api/app/owner/orders/"+suffix),HttpMethod.POST,new HttpEntity<>(body,h),String.class);}
    JsonNode good(ResponseEntity<String> r){assertEquals(200,r.getStatusCode().value(),r.getBody());return ViJson.tree(r.getBody()).get("data");}

    void cleanOrders(){
        for(String table:List.of("vi_quota_reservation","vi_payment_order","vi_order_snapshot"))jdbc.execute("DELETE FROM "+table+" WHERE order_id IN (SELECT id FROM vi_order WHERE station_id IN (996301,996302))");
        jdbc.execute("DELETE FROM vi_order WHERE station_id IN (996301,996302)");
    }
    void cleanPolicy(){
        jdbc.execute("DELETE FROM vi_integration_publication WHERE target_id BETWEEN 996310 AND 996319");
        jdbc.execute("DELETE FROM vi_integration_check WHERE target_id BETWEEN 996310 AND 996319");
        jdbc.execute("DELETE FROM vi_integration_revision WHERE profile_id BETWEEN 996310 AND 996319");
        jdbc.execute("DELETE FROM vi_integration_profile WHERE id BETWEEN 996310 AND 996319");
    }
    String policyJson(){return ViJson.write(Map.of("offlineEnabled",true,"wechatEnabled",false,"offlineCollectionMode","PREPAID_MANUAL_VERIFIED","offlineMethods",List.of("CASH"),"offlineReceiverName","测试公司","offlineInstructions","仅隔离测试，不实际收款","offlineContactName","测试财务","offlineContactPhone","00000000000"));}
    void seedPaymentPolicy(){
        cleanPolicy();
        jdbc.update("INSERT INTO vi_integration_profile(id,kind,owner_dept_id,environment,config_key,display_name,lifecycle,active_revision_id,created_by) VALUES (996310,'PAYMENT_POLICY',100,'TEST','DEFAULT','B03测试策略','ACTIVE',996310,1)");
        jdbc.update("INSERT INTO vi_integration_revision(id,profile_id,revision_no,config_json,secret_refs_json,content_hash,created_by,reason) VALUES(996310,996310,1,?,'{}',REPEAT('a',64),1,'B03测试发布夹具')",policyJson());
        jdbc.update("INSERT INTO vi_integration_check(id,target_type,target_id,revision_id,content_hash,check_level,status,issues_json,checked_at,expires_at,checked_by) VALUES(996310,'PROFILE',996310,996310,REPEAT('a',64),'LOCAL','PASS','[]',UTC_TIMESTAMP(3)-INTERVAL 1 MINUTE,UTC_TIMESTAMP(3)+INTERVAL 1 HOUR,1)");
        jdbc.update("INSERT INTO vi_integration_publication(id,target_type,target_id,revision_id,check_id,action,published_by,published_at,reason,dependency_versions,target_version) VALUES(996310,'PROFILE',996310,996310,996310,'PUBLISH',1,UTC_TIMESTAMP(3),'B03测试发布夹具','{}',1)");
    }
    JsonNode verifiedLogin(String persona){
        var session=login(persona+System.nanoTime());String token=session.path("token").asText();
        String run=(String)simPost("/control/runs",Map.of()).get("runId");String code=(String)simPost("/control/codes",Map.of("runId",run,"persona",persona+System.nanoTime(),"kind","phone")).get("code");
        good(rest.exchange(url("/api/common/phone/bind"),HttpMethod.POST,mini(Map.of("phoneCode",code,"expectedAccountVersion",session.path("accountVersion").asLong(),"verificationRequestId",UUID.randomUUID().toString()),token),String.class));return session;
    }
    JsonNode quote(String token){return good(post("quote",input(),token,UUID.randomUUID().toString()));}
    Map<String,Object> orderInput(JsonNode quote){return new HashMap<>(Map.of("quoteId",quote.path("quoteId").asText(),"termsVersion","b03v1","privacyVersion","b03v1","acceptedTerms",true));}
    ResponseEntity<String> create(Map<String,Object> body,String token,String key){var entity=mini(body,token);HttpHeaders h=new HttpHeaders();h.putAll(entity.getHeaders());h.set("Idempotency-Key",key);return rest.exchange(url("/api/app/owner/orders"),HttpMethod.POST,new HttpEntity<>(body,h),String.class);}
    void emptyOrders(){assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM vi_order WHERE station_id IN (996301,996302)",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT SUM(held_quota) FROM vi_quota_slot WHERE station_id IN (996301,996302)",Integer.class));}
    @Test void 建单快照付款截止双额度协议与最小视图同事务() throws Exception {
        String token=verifiedLogin("OWNER_A").path("token").asText();JsonNode quote=quote(token);var body=orderInput(quote);body.put("remark","敏感客户备注B03");String key=UUID.randomUUID().toString();
        int vc=jdbc.queryForObject("SELECT COUNT(*) FROM vi_vehicle",Integer.class),ac=jdbc.queryForObject("SELECT COUNT(*) FROM vi_address",Integer.class);
        JsonNode order=good(create(body,token,key));Files.writeString(repoRoot().resolve(".vi-local/b03-owner-response.json"),order.toString());assertEquals("PENDING_PAY",order.path("fulfillmentStatus").asText());assertEquals("PENDING",order.path("paymentStatus").asText());assertTrue(order.path("vehicle").path("vehicleId").isNull());assertEquals(24200,order.path("money").path("totalAmount").asLong());assertEquals(0,order.path("money").path("paidAmount").asLong());
        long id=jdbc.queryForObject("SELECT id FROM vi_order WHERE order_no=?",Long.class,order.path("orderNo").asText());assertEquals(100L,jdbc.queryForObject("SELECT dept_id FROM vi_order WHERE id=?",Long.class,id));
        assertEquals(1800,jdbc.queryForObject("SELECT TIMESTAMPDIFF(SECOND,s.accepted_at,o.pay_expires_at) FROM vi_order o JOIN vi_order_snapshot s ON s.order_id=o.id WHERE o.id=?",Integer.class,id));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM vi_quota_reservation WHERE order_id=? AND status='HELD'",Integer.class,id));assertEquals(2,jdbc.queryForObject("SELECT SUM(held_quota) FROM vi_quota_slot WHERE station_id=996302",Integer.class));
        assertEquals(id,jdbc.queryForObject("SELECT used_order_id FROM vi_quote WHERE id=?",Long.class,quote.path("quoteId").asText()));
        assertEquals("UNSELECTED",jdbc.queryForObject("SELECT payment_method FROM vi_payment_order WHERE order_id=?",String.class,id));assertEquals(100L,jdbc.queryForObject("SELECT receiver_dept_id FROM vi_payment_order WHERE order_id=?",Long.class,id));
        assertEquals("敏感客户备注B03",crypto.decrypt(jdbc.queryForObject("SELECT remark_cipher FROM vi_order_snapshot WHERE order_id=?",byte[].class,id)));
        String acceptance=jdbc.queryForObject("SELECT agreement_snapshot FROM vi_order_snapshot WHERE order_id=?",String.class,id);assertTrue(acceptance.contains("b03v1"));assertTrue(acceptance.contains("测试协议"));
        String cache=jdbc.queryForObject("SELECT result_json FROM vi_idempotency WHERE request_key=?",String.class,key);assertEquals(Set.of("orderId"),ViJson.read(cache,Map.class).keySet());
        assertFalse(order.toString().contains("Cipher"));assertFalse(order.toString().contains("敏感客户备注"));assertTrue(order.path("pickupAddress").path("contact").path("phoneMask").asText().contains("****"));
        assertEquals(vc,jdbc.queryForObject("SELECT COUNT(*) FROM vi_vehicle",Integer.class));assertEquals(ac,jdbc.queryForObject("SELECT COUNT(*) FROM vi_address",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE aggregate_type='VI_ORDER' AND aggregate_id=? AND action='ORDER_CREATE'",Integer.class,id));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_type='VI_ORDER' AND aggregate_id=? AND event_type='ORDER_CREATED'",Integer.class,id));
    }
    @Test void 同键复用不同体冲突已用报价不可换键重复建单(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var quote=quote(token);var body=orderInput(quote);String key=UUID.randomUUID().toString();var first=good(create(body,token,key));
        jdbc.update("UPDATE vi_quote SET expires_at=UTC_TIMESTAMP(3)-INTERVAL 1 SECOND WHERE id=?",quote.path("quoteId").asText());
        jdbc.update("UPDATE vi_integration_profile SET lifecycle='RETIRED' WHERE id=996310");assertEquals(first,good(create(body,token,key)));
        body.put("remark","变更");assertEquals(409,create(body,token,key).getStatusCode().value());body.remove("remark");assertEquals(409,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_order WHERE station_id=996302",Integer.class));
    }
    @Test void 四组合不自动保存且常用档案ID保留(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var input=input();
        var v=good(rest.exchange(url("/api/app/owner/vehicles"),HttpMethod.POST,mini(input.get("vehicleDraft"),token),String.class));var a=good(rest.exchange(url("/api/app/owner/addresses"),HttpMethod.POST,mini(input.get("addressDraft"),token),String.class));
        for(boolean vd:List.of(false,true))for(boolean ad:List.of(false,true)){
            var q=new HashMap<>(input);if(vd){q.remove("vehicleDraft");q.put("vehicleId",v.path("vehicleId").asText());}if(ad){q.remove("addressDraft");q.put("addressId",a.path("addressId").asText());}
            var quoted=good(post("quote",q,token,UUID.randomUUID().toString()));var order=good(create(orderInput(quoted),token,UUID.randomUUID().toString()));if(vd)assertEquals(v.path("vehicleId"),order.path("vehicle").path("vehicleId"));else assertTrue(order.path("vehicle").path("vehicleId").isNull());
        }
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM vi_order WHERE station_id=996302",Integer.class));
    }
    @Test void 越权伪造金额协议未同意过期与手机号未核验均无副作用(){
        String token=verifiedLogin("OWNER_A").path("token").asText(),other=verifiedLogin("OWNER_B").path("token").asText();var quote=quote(token);var body=orderInput(quote);
        assertEquals(404,create(body,other,UUID.randomUUID().toString()).getStatusCode().value());body.put("totalAmount",0);assertEquals(400,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());body.remove("totalAmount");body.put("acceptedTerms",false);assertEquals(400,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());body.put("acceptedTerms",true);body.put("termsVersion","old");assertEquals(409,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());body.put("termsVersion","b03v1");
        jdbc.update("UPDATE vi_quote SET expires_at=UTC_TIMESTAMP(3)-INTERVAL 1 SECOND WHERE id=?",quote.path("quoteId").asText());assertEquals(409,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());
        String unverified=login("unverified"+System.nanoTime()).path("token").asText();assertEquals(422,create(orderInput(quote(unverified)),unverified,UUID.randomUUID().toString()).getStatusCode().value());emptyOrders();
    }
    @Test void 支付双关草稿错误环境过期校验和伪造发布拒绝不占额(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var body=orderInput(quote(token));
        for(String sql:List.of("UPDATE vi_integration_revision SET config_json=JSON_SET(config_json,'$.offlineEnabled',false) WHERE id=996310","UPDATE vi_integration_profile SET lifecycle='DRAFT' WHERE id=996310","UPDATE vi_integration_profile SET environment='LIVE' WHERE id=996310","UPDATE vi_integration_check SET expires_at=UTC_TIMESTAMP(3)-INTERVAL 1 DAY WHERE id=996310","DELETE FROM vi_integration_publication WHERE id=996310")){
            seedPaymentPolicy();jdbc.execute(sql);assertEquals(422,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());emptyOrders();
        }
    }
    @Test void 微信开关缺商户拒绝而完整线下开关可独立建单(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var body=orderInput(quote(token));jdbc.execute("UPDATE vi_integration_revision SET config_json=JSON_SET(config_json,'$.wechatEnabled',true,'$.offlineEnabled',false) WHERE id=996310");assertEquals(422,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());emptyOrders();
        jdbc.execute("UPDATE vi_integration_revision SET config_json=JSON_SET(config_json,'$.offlineEnabled',true) WHERE id=996310");good(create(body,token,UUID.randomUUID().toString()));
    }
    @Test void 配置变更拒绝重新报价且30分钟窗口不足不建单(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var body=orderInput(quote(token));jdbc.execute("UPDATE sys_config SET config_value='2' WHERE config_key='vi.config.version'");assertEquals(409,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());
        jdbc.execute("UPDATE vi_quota_slot SET starts_at=UTC_TIMESTAMP(3)+INTERVAL 20 MINUTE,ends_at=UTC_TIMESTAMP(3)+INTERVAL 1 HOUR WHERE station_id=996301");
        jdbc.execute("UPDATE vi_quota_slot SET starts_at=UTC_TIMESTAMP(3)+INTERVAL 20 MINUTE,ends_at=UTC_TIMESTAMP(3)+INTERVAL 1 HOUR WHERE station_id=996302 AND quota_type='PICKUP'");jdbc.execute("UPDATE vi_quota_slot SET starts_at=UTC_TIMESTAMP(3)+INTERVAL 20 MINUTE,ends_at=UTC_TIMESTAMP(3)+INTERVAL 3 HOUR WHERE station_id=996302 AND quota_type='STATION_RECEIVE'");
        // SQL语句时钟略有差异，以锚点时刻对齐两个站点。
        jdbc.execute("UPDATE vi_quota_slot target JOIN vi_quota_slot anchor ON anchor.id="+slotId+" SET target.starts_at=anchor.starts_at,target.ends_at=anchor.ends_at WHERE target.station_id=996302 AND target.quota_type='PICKUP'");
        start=java.time.Instant.now().plusSeconds(1200).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);jdbc.execute("DELETE FROM vi_price_rule");publishPrice();
        assertEquals(422,create(orderInput(quote(token)),token,UUID.randomUUID().toString()).getStatusCode().value());emptyOrders();
    }
    @Test void 并发最后双额度仅一人成功并且同键并发只一单()throws Exception{
        String a=verifiedLogin("OWNER_A").path("token").asText(),b=verifiedLogin("OWNER_B").path("token").asText();jdbc.execute("UPDATE vi_quota_slot SET total_quota=1 WHERE station_id=996302");var qa=orderInput(quote(a));var qb=orderInput(quote(b));
        var pool=Executors.newFixedThreadPool(2);var gate=new CountDownLatch(1);try{
            var f1=pool.submit(()->{gate.await();return create(qa,a,UUID.randomUUID().toString());});var f2=pool.submit(()->{gate.await();return create(qb,b,UUID.randomUUID().toString());});gate.countDown();List<Integer> statuses=List.of(f1.get(20,TimeUnit.SECONDS).getStatusCode().value(),f2.get(20,TimeUnit.SECONDS).getStatusCode().value());assertTrue(statuses.contains(200));assertTrue(statuses.contains(409));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_order WHERE station_id=996302",Integer.class));assertEquals(2,jdbc.queryForObject("SELECT SUM(held_quota) FROM vi_quota_slot WHERE station_id=996302",Integer.class));
        }finally{pool.shutdownNow();}
    }
    @Test void 尚有余量时其他订单占额不使旧报价失效(){
        String a=verifiedLogin("OWNER_A").path("token").asText(),b=verifiedLogin("OWNER_B").path("token").asText();var qa=orderInput(quote(a));var qb=orderInput(quote(b));good(create(qa,a,UUID.randomUUID().toString()));good(create(qb,b,UUID.randomUUID().toString()));assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM vi_order WHERE station_id=996302",Integer.class));
    }
    @Test void 并发同键只创建一单()throws Exception{
        String token=verifiedLogin("OWNER_A").path("token").asText();var body=orderInput(quote(token));String key=UUID.randomUUID().toString();var pool=Executors.newFixedThreadPool(2);var gate=new CountDownLatch(1);try{var a=pool.submit(()->{gate.await();return create(body,token,key);});var b=pool.submit(()->{gate.await();return create(body,token,key);});gate.countDown();assertEquals(good(a.get(20,TimeUnit.SECONDS)),good(b.get(20,TimeUnit.SECONDS)));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_order WHERE station_id=996302",Integer.class));}finally{pool.shutdownNow();}
    }
    @Test void 付款或发件箱故障全部回滚且原请求可重试(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var quoted=quote(token);var body=orderInput(quoted);String key=UUID.randomUUID().toString();
        for(String table:List.of("vi_payment_order","vi_outbox")){
            String trigger="b03_fault_"+UUID.randomUUID().toString().replace("-","");String condition=table.equals("vi_outbox")?"NEW.event_type='ORDER_CREATED'":"NEW.payment_method='UNSELECTED'";
            jdbc.execute("CREATE TRIGGER "+trigger+" BEFORE INSERT ON "+table+" FOR EACH ROW BEGIN IF "+condition+" THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='B03_TEST'; END IF; END");
            int before=jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='ORDER_CREATE'",Integer.class);
            try{assertEquals(500,create(body,token,key).getStatusCode().value());emptyOrders();assertNull(jdbc.queryForObject("SELECT used_order_id FROM vi_quote WHERE id=?",Long.class,quoted.path("quoteId").asText()));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM vi_idempotency WHERE request_key=?",Integer.class,key));assertEquals(before,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='ORDER_CREATE'",Integer.class));}finally{jdbc.execute("DROP TRIGGER "+trigger);}
        }
        good(create(body,token,key));
    }
    @Test void 子部门订单归属真实站点而收款归属公司且多公司配置拒绝(){
        jdbc.execute("UPDATE vi_station SET dept_id=101 WHERE id=996302");String token=verifiedLogin("OWNER_A").path("token").asText();var body=orderInput(quote(token));
        jdbc.execute("INSERT INTO vi_integration_profile(id,kind,owner_dept_id,environment,config_key,display_name,lifecycle,created_by) VALUES(996311,'PAYMENT_POLICY',101,'TEST','DEFAULT','冲突测试','DRAFT',1)");
        assertEquals(422,create(body,token,UUID.randomUUID().toString()).getStatusCode().value());emptyOrders();jdbc.execute("UPDATE vi_integration_profile SET lifecycle='RETIRED' WHERE id=996311");
        var order=good(create(body,token,UUID.randomUUID().toString()));assertEquals(101,jdbc.queryForObject("SELECT dept_id FROM vi_order WHERE order_no=?",Integer.class,order.path("orderNo").asText()));assertEquals(100,jdbc.queryForObject("SELECT p.receiver_dept_id FROM vi_payment_order p JOIN vi_order o ON o.id=p.order_id WHERE o.order_no=?",Integer.class,order.path("orderNo").asText()));
    }
    @Test void 已有地址修改后不得沿用报价建单(){
        String token=verifiedLogin("OWNER_A").path("token").asText();var in=input();var address=good(rest.exchange(url("/api/app/owner/addresses"),HttpMethod.POST,mini(in.get("addressDraft"),token),String.class));in.remove("addressDraft");in.put("addressId",address.path("addressId").asText());var q=good(post("quote",in,token,UUID.randomUUID().toString()));jdbc.update("UPDATE vi_address SET version=version+1 WHERE id=?",address.path("addressId").asText());assertEquals(409,create(orderInput(q),token,UUID.randomUUID().toString()).getStatusCode().value());emptyOrders();
    }

    @Autowired com.xinyi.rongli.order.service.OrderService orders;
    @Autowired com.xinyi.rongli.identity.service.MiniTokenService tokens;
    @Autowired com.xinyi.rongli.identity.service.MiniSessionService sessions;
    @Test void 认证后等待业务锁期间退出的旧上下文写入仍被拒绝(){
        var session=verifiedLogin("OWNER_A");String token=session.path("token").asText();var q=quote(token);var principal=tokens.resolveAccess(token);
        sessions.logout(Long.parseLong(session.path("accountId").asText()),Long.parseLong(session.path("sessionId").asText()),true);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal,null,List.of()));
        try{var e=assertThrows(com.xinyi.rongli.platform.error.ViException.class,()->orders.create(new com.xinyi.rongli.order.dto.OrderCreate(q.path("quoteId").asText(),"b03v1","b03v1",true,null),UUID.randomUUID().toString()));assertEquals(com.xinyi.rongli.platform.error.ViErrorCode.VI_AUTH_REQUIRED,e.getErrorCode());}finally{org.springframework.security.core.context.SecurityContextHolder.clearContext();}
        emptyOrders();
    }

}
