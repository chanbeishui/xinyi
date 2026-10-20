package com.xinyi.web.viit;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@TestConfiguration(proxyBeanMethods=false)
@Profile("vi-it")
@Import(ViIntegrationTestConfiguration.ControlApi.class)
public class ViIntegrationTestConfiguration {
 @Bean TestGuard viTestGuard(Environment env,JdbcTemplate jdbc){return new TestGuard(env,jdbc);}
 @Bean @Order(-100)
 SecurityFilterChain integrationOnly(HttpSecurity http,TestGuard guard) throws Exception {
  return http.securityMatcher("/__integration/**")
   .csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.anyRequest().access((authentication,context)->new AuthorizationDecision(guard.allowed(context.getRequest()))))
   .build();
 }
 static final class TestGuard {
  private final Environment env;private final JdbcTemplate jdbc;
  TestGuard(Environment e,JdbcTemplate j){env=e;jdbc=j;}
  boolean allowed(HttpServletRequest r){
   try {
    String key=env.getRequiredProperty("VI_TEST_CONTROL_KEY");
    if(key.length()<32 || !Set.of("127.0.0.1","::1","0:0:0:0:0:0:0:1").contains(r.getRemoteAddr()))return false;
    if(!"integration".equals(env.getProperty("vi.environment")) || !"SIMULATOR".equals(env.getProperty("vi.providers.mode")))return false;
    String supplied=r.getHeader("X-Test-Control-Key");
    if(supplied==null || !MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8)))return false;
    String db=jdbc.queryForObject("SELECT DATABASE()",String.class);
    if(db==null || !db.matches("xinyi_vi_(dev|it_[a-z0-9_]+)"))return false;
    Map<String,Object> row=jdbc.queryForMap("SELECT kind,key_hash FROM test_environment_guard WHERE id=1");
    String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)));
    return "XINYI_VEHICLE_ISOLATED".equals(row.get("kind")) && digest.equals(row.get("key_hash"));
   }catch(Exception e){return false;}
  }
 }
 @RestController @Profile("vi-it") @RequestMapping("/__integration")
 static class ControlApi {
  private final FixturePort fixtures;
  ControlApi(FixturePort f){fixtures=f;} // 缺实际测试Service绑定则启动失败，不能默默跳过。
  @GetMapping("/runtime") Map<String,Object> runtime(){return Map.of("environment","integration","databaseGuard","XINYI_VEHICLE_ISOLATED","providerMode","SIMULATOR","contractVersion","6.0.0");}
  @PostMapping("/fixtures") @ResponseStatus(HttpStatus.CREATED)
  Map<String,Object> base(@RequestBody Map<String,Object> body){fields(body,Set.of("fixture","providerRunId"));String kind=text(body,"fixture");if(!Set.of("IDENTITY","BOOKING").contains(kind))throw invalid();return fixtures.create(kind,text(body,"providerRunId"));}
  @PostMapping("/clock/advance") Map<String,Object> clock(@RequestBody Map<String,Object> body){fields(body,Set.of("runId","advanceSeconds"));Object n=body.get("advanceSeconds");if(!(n instanceof Number number) || number.doubleValue()!=number.longValue() || number.longValue()<0 || number.longValue()>604800)throw invalid();return fixtures.advanceClock(text(body,"runId"),number.longValue());}
  @PostMapping("/jobs/run-due") Map<String,Object> due(@RequestBody Map<String,Object> body){fields(body,Set.of("runId","jobKind"));String kind=text(body,"jobKind");if(!Set.of("PAY_CLOSE","DISPATCH","INVITE_EXPIRE","SERVICE_CONFIRM","OUTBOX","REFUND_QUERY").contains(kind))throw invalid();return fixtures.runDue(text(body,"runId"),kind);}
  @PostMapping("/provider-events") Map<String,Object> event(@RequestBody Map<String,Object> body){fields(body,Set.of("runId","rawBody","signature","algorithm"));if(!"TEST_HMAC_SHA256".equals(body.get("algorithm")))throw invalid();return fixtures.applyProviderEvent(text(body,"runId"),text(body,"rawBody"),text(body,"signature"));}
  static String text(Map<String,Object> b,String k){Object v=b.get(k);if(!(v instanceof String s)||s.isBlank())throw invalid();return s;}
  static void fields(Map<String,Object> b,Set<String> allowed){if(!allowed.equals(b.keySet()))throw invalid();}
  static ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid integration control input");}
 }
}
