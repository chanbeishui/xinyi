package com.xinyi.web.controller.rongli.capacity;

import com.xinyi.rongli.capacity.dto.*;
import com.xinyi.rongli.capacity.service.CapacityService;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViRows;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/** 后台和员工容量入口共用应用服务；影响预览沿用列表查询，不产生业务写入。 */
@RestController
public class CapacityController {
    private final CapacityService service;
    public CapacityController(CapacityService service) { this.service = service; }

    @GetMapping("/api/admin/capacity")
    public ResponseEntity<?> adminList(@RequestParam Map<String,String> params) { return list(params, false); }
    @GetMapping("/api/app/station/capacity")
    public ResponseEntity<?> miniList(@RequestParam Map<String,String> params) { return list(params, true); }

    private ResponseEntity<?> list(Map<String,String> params, boolean mini) {
        if (!Set.of("pageNum", "pageSize", "stationId", "quotaType", "slotId", "proposedTotal").containsAll(params.keySet())) invalid();
        int page = number(params.getOrDefault("pageNum", "1"), 1, Integer.MAX_VALUE).intValue();
        int size = number(params.getOrDefault("pageSize", "20"), 1, 100).intValue();
        CapacityQuery query = new CapacityQuery(page, size, optionalId(params.get("stationId")), params.get("quotaType"),
                optionalId(params.get("slotId")), params.containsKey("proposedTotal") ? number(params.get("proposedTotal"), 0, Integer.MAX_VALUE).intValue() : null);
        CapacityService.Page result = service.list(query, mini);
        return noStore(ViRows.of(result.rows(), result.total()));
    }

    @PostMapping("/api/admin/capacity")
    public ResponseEntity<?> create(@RequestHeader("Idempotency-Key") String key, @RequestBody Map<String,Object> body) {
        fields(body, Set.of("stationId","quotaType","startAt","endAt","total","reason"));
        CapacityCreate input = new CapacityCreate(text(body,"stationId"), text(body,"quotaType"), text(body,"startAt"),
                text(body,"endAt"), bodyNumber(body.get("total"),0,Integer.MAX_VALUE).intValue(), text(body,"reason"));
        return noStore(ViSuccess.ok(service.create(input, key)));
    }

    @PutMapping("/api/admin/capacity/{slotId}")
    public ResponseEntity<?> adminChange(@PathVariable("slotId") String slotId, @RequestHeader("Idempotency-Key") String key,
                                        @RequestBody Map<String,Object> body) { return change(slotId,key,body,false); }
    @PutMapping("/api/app/station/capacity/{slotId}")
    public ResponseEntity<?> miniChange(@PathVariable("slotId") String slotId, @RequestHeader("Idempotency-Key") String key,
                                       @RequestBody Map<String,Object> body) { return change(slotId,key,body,true); }

    private ResponseEntity<?> change(String id, String key, Map<String,Object> body, boolean mini) {
        fields(body,Set.of("total","expectedVersion","reason","enabled"));
        if (body.containsKey("enabled") && !(body.get("enabled") instanceof Boolean)) invalid();
        CapacityChange input = new CapacityChange(bodyNumber(body.get("total"),0,Integer.MAX_VALUE).intValue(),
                bodyNumber(body.get("expectedVersion"),0,9007199254740991L),text(body,"reason"),(Boolean)body.get("enabled"));
        return noStore(ViSuccess.ok(service.change(requiredId(id),input,key,mini)));
    }

    private static void fields(Map<String,Object> body, Set<String> allowed) {
        if (body == null || !allowed.containsAll(body.keySet())) invalid();
    }
    private static String text(Map<String,Object> body, String key) {
        Object value = body.get(key);
        if (!(value instanceof String)) invalid();
        return (String)value;
    }
    private static Long bodyNumber(Object value, long min, long max) {
        if (!(value instanceof Number)) invalid();
        return number(value, min, max);
    }
    private static Long number(Object value, long min, long max) {
        try {
            if (!(value instanceof Number) && !(value instanceof String)) throw new IllegalArgumentException();
            long number = new BigDecimal(value.toString()).longValueExact();
            if (number < min || number > max) throw new IllegalArgumentException();
            return number;
        } catch (RuntimeException e) { throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"数值须为范围内整数"); }
    }
    private static Long optionalId(String value) { return value == null ? null : requiredId(value); }
    private static long requiredId(String value) {
        try { return ViId.parse(value); }
        catch (RuntimeException e) { throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"ID无效"); }
    }
    private static void invalid() { throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"字段缺失、类型错误或包含未允许字段"); }
    private static ResponseEntity<?> noStore(Object value) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value); }
}
