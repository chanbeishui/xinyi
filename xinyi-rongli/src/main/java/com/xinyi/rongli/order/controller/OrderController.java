package com.xinyi.rongli.order.controller;
import com.xinyi.rongli.order.dto.OrderCreate;
import com.xinyi.rongli.order.service.OrderService;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@RestController
@RequestMapping("/api/app/owner/orders")
public class OrderController {
 private final OrderService service;
 public OrderController(OrderService service){this.service=service;}
 @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,Object> body,
  @RequestHeader(value="X-Client-Type",required=false)String client,@RequestHeader(value="Idempotency-Key",required=false)String key){
  if(!"owner".equals(client)||!Set.of("quoteId","termsVersion","privacyVersion","acceptedTerms","remark").containsAll(body.keySet())||!Boolean.TRUE.equals(body.get("acceptedTerms")))invalid();
  for(String field:List.of("quoteId","termsVersion","privacyVersion"))if(!(body.get(field) instanceof String s)||s.isBlank()||s.length()>64)invalid();
  if(body.containsKey("remark")&&(!(body.get("remark") instanceof String s)||s.length()>500))invalid();
  try{ViId.parse((String)body.get("quoteId"));if(key==null||!UUID.fromString(key).toString().equalsIgnoreCase(key))invalid();}catch(RuntimeException e){invalid();}
  var input=new OrderCreate((String)body.get("quoteId"),(String)body.get("termsVersion"),(String)body.get("privacyVersion"),true,(String)body.get("remark"));
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(service.create(input,key)));
 }
 private static void invalid(){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"请确认当前协议；建单只接受报价ID、协议版本及500字以内备注");}
}
