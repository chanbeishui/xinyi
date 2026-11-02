package com.xinyi.rongli.order.controller;
import com.xinyi.rongli.order.dto.OrderQuery;
import com.xinyi.rongli.order.service.OrderReadService;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import java.time.Instant;
import java.util.*;
@RestController
public class OrderReadController {
 private final OrderReadService service;public OrderReadController(OrderReadService service){this.service=service;}
 private ResponseEntity<?> ok(Object result){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result instanceof OrderReadService.Page p?com.xinyi.rongli.identity.dto.ViRows.of(p.rows(),p.total()):ViSuccess.ok(result));}
 private static void invalid(){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"请检查订单编号、状态、时间及分页参数");}
 private static String order(String value){if(value==null||!value.matches("[A-Za-z0-9_-]{1,64}"))invalid();return value;}
 private static OrderQuery query(MultiValueMap<String,String> values,boolean owner){try{
  Set<String> allowed=owner?Set.of("status","startAt","endAt","pageNum","pageSize"):Set.of("pageNum","pageSize");if(!allowed.containsAll(values.keySet())||values.values().stream().anyMatch(v->v.size()!=1))invalid();
  int page=Integer.parseInt(values.getFirst("pageNum")==null?"1":values.getFirst("pageNum")),size=Integer.parseInt(values.getFirst("pageSize")==null?"20":values.getFirst("pageSize"));if(page<1||size<1||size>100)invalid();Math.multiplyExact(page-1,size);
  String status=values.getFirst("status");if(status!=null&&!Set.of("PENDING_PAY","SCHEDULED","PENDING_DISPATCH","DISPATCHING","MANUAL_DISPATCH","ACCEPTED","GOING_PICKUP","ARRIVED_PICKUP","PICKED_UP","ARRIVED_STATION","INSPECTING","INSPECTED","CAR_TAKEN","WAIT_CONFIRM","COMPLETED","CANCELLED","CLOSED").contains(status))invalid();
  Instant start=values.getFirst("startAt")==null?null:Instant.parse(values.getFirst("startAt")),end=values.getFirst("endAt")==null?null:Instant.parse(values.getFirst("endAt"));if(start!=null&&end!=null&&!start.isBefore(end))invalid();return new OrderQuery(status,start,end,page,size);
 }catch(RuntimeException e){invalid();return null;}}
 @GetMapping("/api/app/owner/orders") public ResponseEntity<?> ownerList(@RequestParam MultiValueMap<String,String> q){return ok(service.list("OWNER",query(q,true)));}
 @GetMapping("/api/app/owner/orders/{orderNo}") public ResponseEntity<?> ownerDetail(@PathVariable String orderNo,@RequestParam MultiValueMap<String,String> q){if(!q.isEmpty())invalid();return ok(service.detail("OWNER",order(orderNo)));}
 @GetMapping("/api/app/driver/orders") public ResponseEntity<?> driverList(@RequestParam MultiValueMap<String,String> q){return ok(service.list("DRIVER",query(q,false)));}
 @GetMapping("/api/app/driver/orders/{orderNo}") public ResponseEntity<?> driverDetail(@PathVariable String orderNo,@RequestParam MultiValueMap<String,String> q){if(!q.isEmpty())invalid();return ok(service.detail("DRIVER",order(orderNo)));}
 @GetMapping("/api/app/station/orders") public ResponseEntity<?> stationList(@RequestParam MultiValueMap<String,String> q){return ok(service.list("STATION",query(q,false)));}
 @GetMapping("/api/app/station/orders/{orderNo}") public ResponseEntity<?> stationDetail(@PathVariable String orderNo,@RequestParam MultiValueMap<String,String> q){if(!q.isEmpty())invalid();return ok(service.detail("STATION",order(orderNo)));}
 @GetMapping("/api/admin/orders") public ResponseEntity<?> adminList(@RequestParam MultiValueMap<String,String> q){return ok(service.list("ADMIN",query(q,false)));}
 @GetMapping("/api/admin/orders/{orderNo}") public ResponseEntity<?> adminDetail(@PathVariable String orderNo,@RequestParam MultiValueMap<String,String> q){if(!q.isEmpty())invalid();return ok(service.detail("ADMIN",order(orderNo)));}
 @GetMapping("/api/app/owner/orders/{orderNo}/timeline") public ResponseEntity<?> timeline(@PathVariable String orderNo,@RequestParam MultiValueMap<String,String> q){return ok(service.timeline(order(orderNo),query(q,false)));}
}
