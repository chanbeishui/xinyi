package com.xinyi.web.controller.rongli.booking;
import com.xinyi.rongli.booking.service.ScheduleService;
import com.xinyi.rongli.platform.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@RestController
@RequestMapping("/api/app/owner/schedule/slots")
public class ScheduleController {
 private final ScheduleService service;
 public ScheduleController(ScheduleService service){this.service=service;}
 @GetMapping public ResponseEntity<?> list(@RequestParam Map<String,String> q,@RequestHeader(value="X-Client-Type",required=false) String client){
  if(!"owner".equals(client)||!Set.of("date","serviceAreaCode","pageNum","pageSize").containsAll(q.keySet()))throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
  int page,size;try{page=Integer.parseInt(q.getOrDefault("pageNum","1"));size=Integer.parseInt(q.getOrDefault("pageSize","10"));}catch(RuntimeException e){throw new ViException(ViErrorCode.VI_REQUEST_INVALID);}
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.list(q.get("date"),q.get("serviceAreaCode"),page,size));
 }
}
