package com.xinyi.web.controller.rongli.pricing;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.pricing.dto.*;
import com.xinyi.rongli.pricing.service.PricingService;
import com.xinyi.rongli.pricing.service.PriceCalculator;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/** HTTP字段白名单与领域DTO分离；不接受审核人、状态、摘要或内部权限字段。 */
@RestController
@RequestMapping("/api/admin/prices")
public class PricingController {
    private static final Set<String> CONTENT=Set.of("name","vehicleTypeCode","inspectionTypeCode","inspectionFee",
            "driverBaseFee","includedDistanceMeter","distanceUnitMeter","distanceUnitFee","distanceRounding","routeBasis",
            "waitingFeeEnabled","waitingFreeSeconds","waitingUnitSeconds","waitingUnitFee","effectiveFrom","effectiveTo");
    private final PricingService service;
    public PricingController(PricingService service) {this.service=service;}
    private record ListResponse(int code,String msg,List<PriceView> rows,long total,String environment) { }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Map<String,String> query) {
        if (!Set.of("pageNum","pageSize","ownerDeptId","environment","status","vehicleTypeCode","inspectionTypeCode").containsAll(query.keySet())) invalid();
        var input=new PriceQuery(queryNumber(query.getOrDefault("pageNum","1"),1,Integer.MAX_VALUE).intValue(),
                queryNumber(query.getOrDefault("pageSize","20"),1,100).intValue(),
                query.containsKey("ownerDeptId")?id(query.get("ownerDeptId")):null,query.get("status"),
                query.get("vehicleTypeCode"),query.get("inspectionTypeCode"),query.get("environment"));
        PricingService.Page page=service.list(input);
        return noStore(new ListResponse(200,"操作成功",page.rows(),page.total(),page.environment()));
    }
    @PostMapping
    public ResponseEntity<?> create(@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody Map<String,Object> body) {
        requireKey(key);return noStore(ViSuccess.ok(service.create(write(body,false),key)));
    }
    @PutMapping("/{ruleId}")
    public ResponseEntity<?> update(@PathVariable("ruleId") String id,@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody Map<String,Object> body) {
        requireKey(key);return noStore(ViSuccess.ok(service.update(id(id),write(body,true),key)));
    }
    @PostMapping("/{ruleId}/submit")
    public ResponseEntity<?> submit(@PathVariable("ruleId") String id,@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody Map<String,Object> body) {
        requireKey(key);return noStore(ViSuccess.ok(service.submit(id(id),action(body,false,false),key)));
    }
    @PostMapping("/{ruleId}/audit")
    public ResponseEntity<?> audit(@PathVariable("ruleId") String id,@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody Map<String,Object> body) {
        requireKey(key);return noStore(ViSuccess.ok(service.review(id(id),action(body,true,false),key)));
    }
    @PostMapping("/{ruleId}/publish")
    public ResponseEntity<?> publish(@PathVariable("ruleId") String id,@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody Map<String,Object> body) {
        requireKey(key);return noStore(ViSuccess.ok(service.publish(id(id),action(body,false,true),key)));
    }
    @PostMapping("/{ruleId}/disable")
    public ResponseEntity<?> disable(@PathVariable("ruleId") String id,@RequestHeader(value="Idempotency-Key",required=false) String key,@RequestBody Map<String,Object> body) {
        requireKey(key);return noStore(ViSuccess.ok(service.disable(id(id),action(body,false,false),key)));
    }
    @PostMapping("/{ruleId}/trial")
    public ResponseEntity<?> trial(@PathVariable("ruleId") String id,@RequestBody Map<String,Object> body) {
        fields(body,Set.of("distanceMeter"));
        return noStore(ViSuccess.ok(service.trial(id(id),number(body,"distanceMeter",0,Integer.MAX_VALUE,false))));
    }
    @PostMapping("/trial")
    public ResponseEntity<?> matchTrial(@RequestBody Map<String,Object> body) {
        fields(body,Set.of("stationId","vehicleTypeCode","inspectionTypeCode","distanceMeter","serviceAt"));
        Instant at=body.containsKey("serviceAt")?PricingService.instant(text(body,"serviceAt",false)):Instant.now();
        return noStore(ViSuccess.ok(service.matchTrial(id(text(body,"stationId",false)),text(body,"vehicleTypeCode",false),
                text(body,"inspectionTypeCode",false),at,number(body,"distanceMeter",0,Integer.MAX_VALUE,false))));
    }
    private static PriceWrite write(Map<String,Object> body,boolean update) {
        Set<String> allowed=new HashSet<>(CONTENT);allowed.addAll(Set.of("ownerDeptId","environment"));
        allowed.add(update?"expectedVersion":"sourceRuleId");fields(body,allowed);
        PriceContent config=new PriceContent(text(body,"name",false),text(body,"vehicleTypeCode",false),text(body,"inspectionTypeCode",false),
                number(body,"inspectionFee",0,PriceCalculator.MAX_SAFE_FEN,false),number(body,"driverBaseFee",0,PriceCalculator.MAX_SAFE_FEN,false),
                number(body,"includedDistanceMeter",0,Integer.MAX_VALUE,false),number(body,"distanceUnitMeter",1,Integer.MAX_VALUE,false),
                number(body,"distanceUnitFee",0,PriceCalculator.MAX_SAFE_FEN,false),text(body,"distanceRounding",false),text(body,"routeBasis",false),
                bool(body,"waitingFeeEnabled"),number(body,"waitingFreeSeconds",0,Integer.MAX_VALUE,true),
                number(body,"waitingUnitSeconds",0,Integer.MAX_VALUE,true),number(body,"waitingUnitFee",0,PriceCalculator.MAX_SAFE_FEN,true),
                text(body,"effectiveFrom",true),text(body,"effectiveTo",true));
        return new PriceWrite(text(body,"ownerDeptId",false),text(body,"environment",false),
                update?null:text(body,"sourceRuleId",true),config,update?number(body,"expectedVersion",0,PriceCalculator.MAX_SAFE_FEN,false):null);
    }
    private static PriceAction action(Map<String,Object> body,boolean audit,boolean publish) {
        Set<String> allowed=new HashSet<>(Set.of("expectedVersion"));
        allowed.add(audit?"opinion":"reason");
        if (audit) allowed.add("decision");
        if (publish) allowed.addAll(Set.of("replacesRuleId","replacesExpectedVersion"));
        fields(body,allowed);
        return new PriceAction(number(body,"expectedVersion",0,PriceCalculator.MAX_SAFE_FEN,false),text(body,audit?"opinion":"reason",false),
                audit?text(body,"decision",false):null,publish?text(body,"replacesRuleId",true):null,
                publish&&body.containsKey("replacesExpectedVersion")?number(body,"replacesExpectedVersion",0,PriceCalculator.MAX_SAFE_FEN,false):null);
    }
    private static String text(Map<String,Object> body,String key,boolean optional) {
        if (!body.containsKey(key)&&optional) return null;
        if (!(body.get(key) instanceof String value)) {invalid();return null;}return value;
    }
    private static boolean bool(Map<String,Object> body,String key) {
        if (!body.containsKey(key)) return false;
        if (!(body.get(key) instanceof Boolean)) invalid();return Boolean.TRUE.equals(body.get(key));
    }
    private static Long number(Map<String,Object> body,String key,long min,long max,boolean optional) {
        if (!body.containsKey(key)&&optional) return 0L;
        if (!(body.get(key) instanceof Number)) invalid();return queryNumber(body.get(key).toString(),min,max);
    }
    private static Long queryNumber(String text,long min,long max) {
        try {long value=new BigDecimal(text).longValueExact();if(value<min||value>max) throw new IllegalArgumentException();return value;}
        catch(RuntimeException e){invalid();return null;}
    }
    private static void fields(Map<String,Object> body,Set<String> allowed) {if(body==null||!allowed.containsAll(body.keySet())) invalid();}
    private static long id(String text) {try{return ViId.parse(text);}catch(RuntimeException e){invalid();return 0;}}
    private static void requireKey(String value) {
        try {if(value==null||!UUID.fromString(value).toString().equalsIgnoreCase(value)) throw new IllegalArgumentException();}
        catch(RuntimeException e){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"请提供有效UUID格式的Idempotency-Key");}
    }
    private static void invalid() {throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"字段缺失、类型/数值范围不正确或包含未允许字段");}
    private static ResponseEntity<?> noStore(Object body) {return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);}
}
