package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.framework.web.service.TokenService;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.pricing.service.PricingService;
import com.xinyi.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

/** 仅测试夹具：真实数据库人员、框架令牌、HTTP安全链和业务Service，不替换业务鉴权。 */
abstract class PricingHttpFixture {
    static final long COMPANY_A=994001, COMPANY_B=994002, SITE_DEPT=994003;
    static final String[] PERMS={"biz:price:list","biz:price:create","biz:price:edit","biz:price:submit","biz:price:audit","biz:price:publish"};
    private static final String TEST_LOOKUP_KEY=UUID.randomUUID().toString()+UUID.randomUUID();
    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) { registry.add("vi.lookup-key",()->TEST_LOOKUP_KEY); }
    static final AtomicLong SEQ=new AtomicLong(System.nanoTime());
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired TokenService tokens;
    @Autowired ISysUserService users;
    @Autowired PricingService pricing;
    final RestTemplate http=new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory()) {{
        setErrorHandler(new DefaultResponseErrorHandler() { @Override public boolean hasError(ClientHttpResponse r) {return false;} });
    }};
    @BeforeEach void fixtures() {
        assertEquals("xinyi_vi_it_e07",jdbc.queryForObject("SELECT DATABASE()",String.class));
        assertEquals("XINYI_VEHICLE_ISOLATED",jdbc.queryForObject("SELECT kind FROM test_environment_guard WHERE id=1",String.class));
        jdbc.execute("DELETE FROM vi_price_rule");
        jdbc.execute("DELETE FROM vi_idempotency");
        jdbc.execute("DELETE FROM vi_event_log");
        jdbc.execute("DELETE FROM vi_outbox");
        jdbc.execute("DELETE FROM vi_station WHERE name LIKE '__C04__%'");
        jdbc.execute("DELETE FROM sys_user_dept WHERE user_id IN (SELECT user_id FROM sys_user WHERE user_name LIKE 'c04\\_%')");
        jdbc.execute("DELETE FROM sys_user WHERE user_name LIKE 'c04\\_%'");
        jdbc.execute("DELETE FROM sys_dept WHERE dept_id IN (994001,994002,994003)");
        jdbc.update("INSERT INTO sys_dept (dept_id,parent_id,ancestors,dept_name,order_num,status,del_flag) VALUES (?,0,'0','C04公司甲',0,'0','0'), (?,0,'0','C04公司乙',0,'0','0'), (?,?,'0,994001','C04站点部门',0,'0','0')",COMPANY_A,COMPANY_B,SITE_DEPT,COMPANY_A);
        jdbc.execute("DELETE FROM sys_dict_data WHERE dict_type='vi_vehicle_type' AND dict_value='C04_CAR'");
        jdbc.execute("DELETE FROM sys_dict_data WHERE dict_type='vi_inspection_type' AND dict_value='C04_ANNUAL'");
        jdbc.execute("INSERT INTO sys_dict_data (dict_sort,dict_label,dict_value,dict_type,status) VALUES (1,'C04测试车型','C04_CAR','vi_vehicle_type','0'),(1,'C04年检','C04_ANNUAL','vi_inspection_type','0')");
    }
    String rootToken() {
        SysUser user=users.selectUserByUserName("admin");
        LoginUser login=new LoginUser(user,Set.of("*:*:*"));login.setUserId(user.getUserId());
        login.setAuthVersion(jdbc.queryForObject("SELECT auth_version FROM sys_user WHERE user_id=?",Long.class,user.getUserId()));
        return mint(login);
    }
    String scoped(long dept,String... permissions) {
        return scopedWithScope(dept,"3",permissions);
    }
    String scopedWithScope(long dept,String dataScope,String... permissions) {
        String name="c04_"+SEQ.incrementAndGet();
        jdbc.update("INSERT INTO sys_user (user_name,nick_name,status,del_flag,auth_version,password) VALUES (?,?,'0','0',0,'x')",name,"C04夹具");
        long id=jdbc.queryForObject("SELECT user_id FROM sys_user WHERE user_name=?",Long.class,name);
        jdbc.update("INSERT INTO sys_user_dept (user_id,dept_id) VALUES (?,?)",id,dept);
        SysUser user=users.selectUserById(id);
        SysRole role=new SysRole();role.setRoleId(SEQ.incrementAndGet());role.setRoleKey("C04");role.setStatus("0");role.setDataScope(dataScope);role.setPermissions(new HashSet<>(List.of(permissions)));user.setRoles(List.of(role));
        LoginUser login=new LoginUser(user,new HashSet<>(List.of(permissions)));login.setUserId(id);login.setAuthVersion(0L);
        return mint(login);
    }
    private String mint(LoginUser login) {
        var request=new org.springframework.mock.web.MockHttpServletRequest();request.addHeader("User-Agent","c04-it");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
        try {return tokens.createToken(login);}finally {org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();}
    }
    long station(long dept) {
        long id=SEQ.incrementAndGet();
        jdbc.update("INSERT INTO vi_station (id,name,dept_id,address,lng,lat,service_radius_meter,supported_vehicle_codes,business_hours,enabled) VALUES (?, '__C04__测试站',?,'测试地址',110,21,10000,'[\"C04_CAR\"]','\"全天\"',1)",id,dept);
        return id;
    }
    Map<String,Object> body(long owner,String env) {
        var values=new LinkedHashMap<String,Object>();
        values.put("ownerDeptId",Long.toString(owner));values.put("environment",env);values.put("name","C04公司方案");
        values.put("vehicleTypeCode","C04_CAR");values.put("inspectionTypeCode","C04_ANNUAL");
        values.put("inspectionFee",20000);values.put("driverBaseFee",3000);values.put("includedDistanceMeter",1000);
        values.put("distanceUnitMeter",1000);values.put("distanceUnitFee",100);values.put("distanceRounding","CEIL_UNIT");values.put("routeBasis","ONE_WAY");
        values.put("effectiveFrom",Instant.now().minusSeconds(60).truncatedTo(ChronoUnit.MILLIS).toString());
        return values;
    }
    HttpEntity<Object> request(Object body,String token,String key) {
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.APPLICATION_JSON);headers.setBearerAuth(token);headers.set("X-Client-Type","admin");
        if(key!=null)headers.set("Idempotency-Key",key);return new HttpEntity<>(body,headers);
    }
    ResponseEntity<String> call(String method,String path,Object body,String token) {return callKey(method,path,body,token,UUID.randomUUID().toString());}
    ResponseEntity<String> callKey(String method,String path,Object body,String token,String key) {return http.exchange("http://127.0.0.1:"+port+path,HttpMethod.valueOf(method),request(body,token,key),String.class);}
    JsonNode data(ResponseEntity<String> response) {assertEquals(200,response.getStatusCode().value(),response.getBody());return ViJson.tree(response.getBody()).get("data");}
    JsonNode create(Map<String,Object> body,String token) {return data(call("POST","/api/admin/prices",body,token));}
    Map<String,Object> action(JsonNode row) {return new LinkedHashMap<>(Map.of("expectedVersion",row.get("version").asLong(),"reason","测试操作"));}
    JsonNode submit(JsonNode row,String token) {return data(call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/submit",action(row),token));}
    JsonNode approve(JsonNode row,String reviewer) {return data(call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/audit",Map.of("expectedVersion",row.get("version").asLong(),"decision","APPROVE","opinion","独立核实通过"),reviewer));}
    JsonNode publish(JsonNode row,String token) {return data(call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/publish",action(row),token));}
    Map<String,Object> match(long station,long meters) {return new LinkedHashMap<>(Map.of("stationId",Long.toString(station),"vehicleTypeCode","C04_CAR","inspectionTypeCode","C04_ANNUAL","distanceMeter",meters));}
}
