package com.xinyi.rongli.payment.controller;
import com.xinyi.rongli.payment.service.PaymentService;
import com.xinyi.rongli.payment.dto.PaymentIntent;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@RestController
@RequestMapping("/api/app/owner/orders")
public class PaymentController {
 private final PaymentService service;public PaymentController(PaymentService service){this.service=service;}
 @GetMapping("/{no}/payment-options") public ResponseEntity<?> options(@PathVariable String no,@RequestHeader(value="X-Client-Type",required=false)String client){check(client,no);return response(service.options(no));}
 @PostMapping("/{no}/prepay") public ResponseEntity<?> prepay(@PathVariable String no,@RequestHeader(value="X-Client-Type",required=false)String client,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestBody Map<String,Object> body){check(client,no);long version=0;try{if(!body.keySet().equals(Set.of("expectedVersion","expectedPolicyRevisionId"))||!(body.get("expectedVersion") instanceof Number)||!(body.get("expectedPolicyRevisionId") instanceof String))invalid();version=Long.parseLong(body.get("expectedVersion").toString());if(version<0)invalid();ViId.parse((String)body.get("expectedPolicyRevisionId"));if(key==null||!UUID.fromString(key).toString().equalsIgnoreCase(key))invalid();}catch(RuntimeException e){invalid();}return response(service.prepay(no,new PaymentIntent(version,(String)body.get("expectedPolicyRevisionId")),key));}
 private static void check(String client,String no){if(!"owner".equals(client)||no==null||!no.matches("[A-Za-z0-9_-]{1,64}"))invalid();}
 private static ResponseEntity<?> response(Object value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(value));}
 private static void invalid(){throw new ViException(ViErrorCode.VI_REQUEST_INVALID);}
}
