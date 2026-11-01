package com.xinyi.web.controller.rongli.booking;
import com.xinyi.rongli.booking.dto.QuoteInput;
import com.xinyi.rongli.booking.service.QuoteService;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@RestController
@RequestMapping("/api/app/owner/orders")
public class QuoteController {
 private final QuoteService service;
 public QuoteController(QuoteService service){this.service=service;}
 @PostMapping("/stations") public ResponseEntity<?> stations(@RequestBody Map<String,Object> body,@RequestHeader(value="X-Client-Type",required=false) String client){
  return ok(ViSuccess.ok(service.candidates(input(body,client,false))));
 }
 @PostMapping("/quote") public ResponseEntity<?> quote(@RequestBody Map<String,Object> body,@RequestHeader(value="X-Client-Type",required=false) String client,@RequestHeader(value="Idempotency-Key",required=false) String key){
  try{if(key==null||!UUID.fromString(key).toString().equalsIgnoreCase(key))throw new IllegalArgumentException();}catch(RuntimeException e){invalid();}
  return ok(ViSuccess.ok(service.quote(input(body,client,true),key)));
 }
 private static QuoteInput input(Map<String,Object> body,String client,boolean quote){
  if(!"owner".equals(client))invalid();
  Set<String> allowed=new HashSet<>(Set.of("slotId","inspectionTypeCode","vehicleId","vehicleDraft","addressId","addressDraft"));if(quote)allowed.add("stationId");
  if(!allowed.containsAll(body.keySet()))invalid();
  for(String field:List.of("slotId","inspectionTypeCode"))if(!(body.get(field) instanceof String s)||s.isBlank())invalid();
  if(quote&&!(body.get("stationId") instanceof String))invalid();
  for(String field:List.of("vehicleId","addressId","stationId"))if(body.containsKey(field)&&!(body.get(field) instanceof String))invalid();
  nested(body,"vehicleDraft",Set.of("plateNo","vehicleTypeCode","brandModel","useType","vinLast4","isDefault"));
  nested(body,"addressDraft",Set.of("address","location","label","isDefault"));
  if(body.get("addressDraft") instanceof Map<?,?> a){Object p=a.get("location");if(!(p instanceof Map<?,?> loc)||!Set.of("lng","lat","coordinateSystem").containsAll(loc.keySet())||!(loc.get("lng") instanceof Number)||!(loc.get("lat") instanceof Number)||!"GCJ02".equals(loc.get("coordinateSystem")))invalid();}
  try{return ViJson.read(ViJson.write(body),QuoteInput.class);}catch(RuntimeException e){invalid();return null;}
 }
 private static void nested(Map<String,Object> body,String field,Set<String> fields){
  if(!body.containsKey(field))return;if(!(body.get(field) instanceof Map<?,?> value)||!fields.containsAll(value.keySet()))invalid();
  Map<?,?> value=(Map<?,?>)body.get(field);
  for(var entry:value.entrySet())if(!entry.getKey().equals("location")&&!entry.getKey().equals("isDefault")&&!(entry.getValue() instanceof String))invalid();
  if(value.containsKey("isDefault")&&!(value.get("isDefault") instanceof Boolean))invalid();
 }
 private static void invalid(){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"报价字段缺失、类型无效或包含未允许字段");}
 private static ResponseEntity<?> ok(Object value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value);}
}
