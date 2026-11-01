package com.xinyi.web.controller.rongli.vehicle;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.Ack;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.vehicle.dto.VehicleView;
import com.xinyi.rongli.vehicle.service.VehicleService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车主车辆（A01，MINI）。本人 CRUD/常用/软删；归属本人防伪造 owner/猜车牌；同车主车牌冲突409；
 * 生成列不入 DTO；写接口需 Idempotency-Key；X-Client-Type 仅审计。no-store。
 */
@RestController
@RequestMapping("/api/app/owner/vehicles")
public class VehicleController {

    private static final Set<String> CLIENT_TYPES = Set.of("owner", "driver", "station", "admin");
    private static final Set<String> CREATE_FIELDS = Set.of("plateNo", "vehicleTypeCode", "brandModel", "useType", "vinLast4", "isDefault");
    private static final Set<String> UPDATE_FIELDS = Set.of("plateNo", "vehicleTypeCode", "brandModel", "useType", "vinLast4", "isDefault", "expectedVersion");
    private static final Set<String> VERSION_FIELDS = Set.of("expectedVersion", "reason");

    private final VehicleService vehicleService;
    private final BusinessActorResolver actorResolver;

    public VehicleController(VehicleService vehicleService, BusinessActorResolver actorResolver) {
        this.vehicleService = vehicleService;
        this.actorResolver = actorResolver;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<List<VehicleView>>> list(
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        requireClientType(clientType);
        Actor actor = actorResolver.requireMini();
        return noStore(ViSuccess.ok(vehicleService.list(actor.accountId())));
    }

    @PostMapping
    public ResponseEntity<ViSuccess<VehicleView>> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, CREATE_FIELDS);
        return noStore(ViSuccess.ok(vehicleService.create(actor.accountId(), str(body.get("plateNo")),
                str(body.get("vehicleTypeCode")), str(body.get("brandModel")), str(body.get("useType")),
                str(body.get("vinLast4")), Boolean.TRUE.equals(body.get("isDefault")))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViSuccess<VehicleView>> update(
            @PathVariable("id") String id, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, UPDATE_FIELDS);
        Boolean isDefault = body.get("isDefault") == null ? null : Boolean.TRUE.equals(body.get("isDefault"));
        return noStore(ViSuccess.ok(vehicleService.update(actor.accountId(), parseId(id), str(body.get("plateNo")),
                str(body.get("vehicleTypeCode")), str(body.get("brandModel")), str(body.get("useType")),
                str(body.get("vinLast4")), isDefault, version(body))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ViSuccess<Ack>> delete(
            @PathVariable("id") String id, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, VERSION_FIELDS);
        vehicleService.delete(actor.accountId(), parseId(id), version(body), str(body.get("reason")));
        return noStore(ViSuccess.ok(Ack.ok()));
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<ViSuccess<VehicleView>> setDefault(
            @PathVariable("id") String id, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, VERSION_FIELDS);
        return noStore(ViSuccess.ok(vehicleService.setDefault(actor.accountId(), parseId(id), version(body), str(body.get("reason")))));
    }

    private long version(Map<String, Object> body) {
        if (!(body.get("expectedVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 缺失或非法");
        }
        return n.longValue();
    }

    private void requireOnly(Map<String, Object> body, Set<String> allowed) {
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String k : body.keySet()) {
            if (!allowed.contains(k)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "未知字段: " + k);
            }
        }
    }

    private void requireClientType(String t) {
        if (t == null || !CLIENT_TYPES.contains(t.trim().toLowerCase())) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "X-Client-Type 缺失或非法");
        }
    }

    private void requireIdem(String k) {
        if (k == null || k.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
    }

    private long parseId(String v) {
        try {
            return ViId.parse(v);
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "ID 非法");
        }
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}