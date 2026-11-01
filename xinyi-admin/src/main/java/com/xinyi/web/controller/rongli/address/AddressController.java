package com.xinyi.web.controller.rongli.address;

import com.xinyi.rongli.address.dto.AddressView;
import com.xinyi.rongli.address.dto.GeoPoint;
import com.xinyi.rongli.address.service.AddressService;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.Ack;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
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
 * 车主地址簿（A02，MINI 域）。新增/更新/删除/列表/设常用；归属本人；version CAS；
 * 设常用并发单默认；地址/位置加密；临时地址不强制先保存。写接口需 Idempotency-Key；X-Client-Type 仅审计。no-store。
 */
@RestController
@RequestMapping("/api/app/owner/addresses")
public class AddressController {

    private static final Set<String> CLIENT_TYPES = Set.of("owner", "driver", "station", "admin");
    private static final Set<String> CREATE_FIELDS = Set.of("label", "address", "location", "isDefault");
    private static final Set<String> UPDATE_FIELDS = Set.of("label", "address", "location", "isDefault", "expectedVersion");
    private static final Set<String> VERSION_FIELDS = Set.of("expectedVersion", "reason");

    private final AddressService addressService;
    private final BusinessActorResolver actorResolver;

    public AddressController(AddressService addressService, BusinessActorResolver actorResolver) {
        this.addressService = addressService;
        this.actorResolver = actorResolver;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<List<AddressView>>> list(
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        requireClientType(clientType);
        Actor actor = actorResolver.requireMini();
        return noStore(ViSuccess.ok(addressService.list(actor.accountId())));
    }

    @PostMapping
    public ResponseEntity<ViSuccess<AddressView>> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, CREATE_FIELDS);
        String address = str(body.get("address"));
        GeoPoint loc = geo(body.get("location"));
        boolean isDefault = Boolean.TRUE.equals(body.get("isDefault"));
        return noStore(ViSuccess.ok(addressService.create(actor.accountId(), str(body.get("label")), address, loc, isDefault)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViSuccess<AddressView>> update(
            @PathVariable("id") String id, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, UPDATE_FIELDS);
        long expectedVersion = version(body);
        Boolean isDefault = body.get("isDefault") == null ? null : Boolean.TRUE.equals(body.get("isDefault"));
        return noStore(ViSuccess.ok(addressService.update(actor.accountId(), parseId(id), str(body.get("label")),
                str(body.get("address")), geo(body.get("location")), isDefault, expectedVersion)));
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
        addressService.delete(actor.accountId(), parseId(id), version(body), str(body.get("reason")));
        return noStore(ViSuccess.ok(Ack.ok()));
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<ViSuccess<AddressView>> setDefault(
            @PathVariable("id") String id, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        requireClientType(clientType);
        requireIdem(idem);
        Actor actor = actorResolver.requireMini();
        requireOnly(body, VERSION_FIELDS);
        return noStore(ViSuccess.ok(addressService.setDefault(actor.accountId(), parseId(id), version(body), str(body.get("reason")))));
    }

    private GeoPoint geo(Object o) {
        if (!(o instanceof Map<?, ?> m) || !(m.get("lng") instanceof Number lng) || !(m.get("lat") instanceof Number lat)) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "location 须含 lng/lat");
        }
        Object cs = m.get("coordinateSystem");
        return new GeoPoint(lng.doubleValue(), lat.doubleValue(), cs == null ? null : String.valueOf(cs));
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