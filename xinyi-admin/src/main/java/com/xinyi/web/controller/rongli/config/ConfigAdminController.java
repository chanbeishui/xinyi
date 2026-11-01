package com.xinyi.web.controller.rongli.config;

import com.xinyi.rongli.config.dto.ConfigView;
import com.xinyi.rongli.config.service.ConfigService;
import com.xinyi.rongli.identity.dto.Ack;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 业务配置读/保存（C01，API_155/156，ADMIN）。CAS 版本；固定安全项不可绕过；功能权限服务层校验。no-store。 */
@RestController
@RequestMapping("/api/admin/config")
public class ConfigAdminController {

    private static final Set<String> PUT_FIELDS = Set.of("items", "expectedVersion", "reason");

    private final ConfigService configService;

    public ConfigAdminController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<ConfigView>> get() {
        return noStore(ViSuccess.ok(configService.getConfig()));
    }

    @PutMapping
    public ResponseEntity<ViSuccess<Ack>> put(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {
        if (idem == null || idem.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String k : body.keySet()) {
            if (!PUT_FIELDS.contains(k)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "未知字段: " + k);
            }
        }
        if (!(body.get("items") instanceof List<?> rawItems)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "items 必填");
        }
        List<ConfigView.Item> items = new ArrayList<>();
        for (Object o : rawItems) {
            if (!(o instanceof Map<?, ?> m) || !(m.get("key") instanceof String key) || !(m.get("value") instanceof String value)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "items 须含 key/value 字符串");
            }
            items.add(new ConfigView.Item(key, value));
        }
        if (!(body.get("expectedVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 缺失或非法");
        }
        Object r = body.get("reason");
        if (!(r instanceof String reason) || reason.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "reason 必填");
        }
        long version = configService.putConfig(items, n.longValue(), reason);
        return noStore(ViSuccess.ok(new Ack(true, null, version)));
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}