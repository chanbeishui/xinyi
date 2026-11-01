package com.xinyi.web.controller.rongli.scope;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.scope.dto.StationGrantView;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.scope.service.StationGrantService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台员工站点预配置（I06，B26，ADMIN 域）。按 sys_user 查改（不要求 accountId/openid）；
 * 集合 CAS（含空集合）+部门数据范围+批量越权全拒绝；功能权限在服务层校验。响应 no-store。
 */
@RestController
@RequestMapping("/api/admin/station-grants")
public class StationGrantAdminController {

    private static final Set<String> PUT_FIELDS = Set.of("ownStationId", "authorizedStationIds", "expectedVersion", "reason");

    private final StationGrantService grantService;

    public StationGrantAdminController(StationGrantService grantService) {
        this.grantService = grantService;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<List<StationGrantView>>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return noStore(ViSuccess.ok(grantService.list(pageNum, pageSize)));
    }

    @GetMapping("/{employeeUserId}")
    public ResponseEntity<ViSuccess<StationGrantView>> get(@PathVariable("employeeUserId") String employeeUserId) {
        return noStore(ViSuccess.ok(grantService.getView(parseId(employeeUserId))));
    }

    @PutMapping("/{employeeUserId}")
    public ResponseEntity<ViSuccess<StationGrantView>> put(
            @PathVariable("employeeUserId") String employeeUserId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
        long sysUserId = parseId(employeeUserId);
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String key : body.keySet()) {
            if (!PUT_FIELDS.contains(key)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "未知字段: " + key);
            }
        }
        if (!(body.get("authorizedStationIds") instanceof List<?> rawIds)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "authorizedStationIds 必填");
        }
        List<Long> ids = new ArrayList<>();
        for (Object o : rawIds) {
            ids.add(parseId(String.valueOf(o)));
        }
        Long ownStationId = body.get("ownStationId") == null ? null : parseId(String.valueOf(body.get("ownStationId")));
        if (!(body.get("expectedVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 缺失或非法");
        }
        Object r = body.get("reason");
        if (!(r instanceof String reason) || reason.isBlank() || reason.length() > 500) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "reason 必填且不超过 500 字");
        }
        return noStore(ViSuccess.ok(grantService.put(sysUserId, ownStationId, ids, n.longValue(), reason)));
    }

    private long parseId(String value) {
        try {
            return ViId.parse(value);
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "ID 非法");
        }
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}