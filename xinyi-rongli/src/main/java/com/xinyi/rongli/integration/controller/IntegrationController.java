package com.xinyi.rongli.integration.controller;
import com.xinyi.rongli.integration.service.IntegrationService;
import com.xinyi.rongli.integration.dto.IntegrationRequests.Query;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
@RestController
@RequestMapping("/api/admin/integrations")
public class IntegrationController {
 private final IntegrationService service;
 public IntegrationController(IntegrationService service){this.service=service;}
 @GetMapping("/profiles") public ResponseEntity<?> list(@RequestParam Map<String,String> q){fields(q,Set.of("kind","ownerDeptId","environment","pageNum","pageSize"));String kind=q.get("kind");if(kind!=null&&!Set.of("PAYMENT_POLICY","WECHAT_PAY","TENCENT_SMS","ALIYUN_SMS","WECHAT_APP").contains(kind))invalid();var query=new Query(kind,q.containsKey("ownerDeptId")?id(q.get("ownerDeptId")):null,q.get("environment"),page(q),size(q));offset(q);return ok(service.list(query));}
 @GetMapping("/profiles/{id}") public ResponseEntity<?> detail(@PathVariable String id){return ok(ViSuccess.ok(service.detail(id(id))));}
 @PostMapping("/profiles") public ResponseEntity<?> create(@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body){headers(key,client);return ok(ViSuccess.ok(service.create(body,key)));}
 @PutMapping("/profiles/{id}/draft") public ResponseEntity<?> draft(@PathVariable String id,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body,HttpServletRequest request){headers(key,client);transport(request);return ok(ViSuccess.ok(service.draft(id(id),body,key)));}
 @PostMapping("/profiles/{id}/checks") public ResponseEntity<?> check(@PathVariable String id,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body){headers(key,client);return ok(ViSuccess.ok(service.check(id(id),body,key)));}
 @PostMapping("/profiles/{id}/publish") public ResponseEntity<?> publish(@PathVariable String id,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body){headers(key,client);return ok(ViSuccess.ok(service.publish(id(id),body,key,false)));}
 @PostMapping("/profiles/{id}/rollback") public ResponseEntity<?> rollback(@PathVariable String id,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body){headers(key,client);return ok(ViSuccess.ok(service.publish(id(id),body,key,true)));}
 @GetMapping("/profiles/{id}/revisions") public ResponseEntity<?> revisions(@PathVariable String id,@RequestParam Map<String,String> q){fields(q,Set.of("pageNum","pageSize"));return ok(service.history(id(id),offset(q),size(q),false));}
 @GetMapping("/profiles/{id}/publications") public ResponseEntity<?> publications(@PathVariable String id,@RequestParam Map<String,String> q){fields(q,Set.of("pageNum","pageSize"));return ok(service.history(id(id),offset(q),size(q),true));}
 @GetMapping("/test-recipients") public ResponseEntity<?> recipients(@RequestParam Map<String,String> q){fields(q,Set.of("pageNum","pageSize"));return ok(service.recipients(offset(q),size(q)));}
 @PostMapping("/test-recipients") public ResponseEntity<?> recipientCreate(@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body,HttpServletRequest request){headers(key,client);transport(request);return ok(ViSuccess.ok(service.recipientCreate(body,key)));}
 @PostMapping("/test-recipients/{id}/revoke") public ResponseEntity<?> revoke(@PathVariable String id,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestBody Map<String,Object> body){headers(key,client);return ok(ViSuccess.ok(service.recipientRevoke(id(id),body,key)));}
 private static void transport(HttpServletRequest r){if(!r.isSecure()&&!Set.of("127.0.0.1","0:0:0:0:0:0:0:1","::1").contains(r.getRemoteAddr()))throw new ViException(ViErrorCode.VI_FORBIDDEN,"凭据维护需要安全传输");}
 private static void headers(String key,String client){try{if(key==null||!UUID.fromString(key).toString().equalsIgnoreCase(key)||!"admin".equals(client))invalid();}catch(IllegalArgumentException e){invalid();}}
 private static long id(String text){try{return com.xinyi.rongli.core.ViId.parse(text);}catch(RuntimeException e){invalid();return 0;}}
 private static int number(String s,int max){try{if(!s.matches("[0-9]+"))invalid();int v=Integer.parseInt(s);if(v<1||v>max)invalid();return v;}catch(NumberFormatException e){invalid();return 0;}}
 private static int page(Map<String,String> q){return number(q.getOrDefault("pageNum","1"),Integer.MAX_VALUE);}
 private static int size(Map<String,String> q){return number(q.getOrDefault("pageSize","20"),100);}
 private static int offset(Map<String,String> q){long n=(long)(page(q)-1)*size(q);if(n>Integer.MAX_VALUE)invalid();return (int)n;}
 private static void fields(Map<String,String> q,Set<String> allowed){if(!allowed.containsAll(q.keySet()))invalid();}
 private static void invalid(){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"请求参数、分页或请求头无效");}
 private static ResponseEntity<?> ok(Object body){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);}
}
