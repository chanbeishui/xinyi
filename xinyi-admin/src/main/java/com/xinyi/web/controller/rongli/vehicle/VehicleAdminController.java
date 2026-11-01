package com.xinyi.web.controller.rongli.vehicle;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.vehicle.dto.VehicleAdminView;
import com.xinyi.rongli.vehicle.service.VehicleAdminService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台车辆管理（A01，B09，ADMIN）。列表/详情/维护PATCH/关联订单/关联结果；功能权限服务层校验；
 * 维护 CAS 不静默覆盖、不追改订单快照；VIN/手机号脱敏。no-store。
 */
@RestController
@RequestMapping("/api/admin/vehicles")
public class VehicleAdminController {

    private static final Set<String> MAINT_FIELDS = Set.of("expectedVersion", "reason", "brandModel", "vehicleTypeCode", "useType", "registeredAt");

    private final VehicleAdminService adminService;

    public VehicleAdminController(VehicleAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<List<VehicleAdminView>>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return noStore(ViSuccess.ok(adminService.list(pageNum, pageSize)));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<ViSuccess<VehicleAdminView>> detail(@PathVariable("vehicleId") String vehicleId) {
        return noStore(ViSuccess.ok(adminService.detail(parseId(vehicleId))));
    }

    @PatchMapping("/{vehicleId}")
    public ResponseEntity<ViSuccess<VehicleAdminView>> maintenance(
            @PathVariable("vehicleId") String vehicleId, @RequestBody Map<String, Object> body) {
        long id = parseId(vehicleId);
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String k : body.keySet()) {
            if (!MAINT_FIELDS.contains(k)) {
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "未知字段: " + k);
            }
        }
        if (!(body.get("expectedVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 缺失或非法");
        }
        Object r = body.get("reason");
        if (!(r instanceof String reason) || reason.isBlank() || reason.length() > 500) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "reason 必填且不超过 500 字");
        }
        LocalDate registeredOn = null;
        if (body.get("registeredAt") != null) {
            try {
                registeredOn = LocalDate.parse(String.valueOf(body.get("registeredAt")));
            } catch (DateTimeParseException e) {
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "registeredAt 须为 ISO 日期");
            }
        }
        return noStore(ViSuccess.ok(adminService.maintenance(id, n.longValue(), str(body.get("brandModel")),
                str(body.get("vehicleTypeCode")), str(body.get("useType")), registeredOn, reason)));
    }

    @GetMapping("/{vehicleId}/orders")
    public ResponseEntity<ViSuccess<List<Map<String, Object>>>> orders(@PathVariable("vehicleId") String vehicleId) {
        return noStore(ViSuccess.ok(adminService.orders(parseId(vehicleId))));
    }

    @GetMapping("/{vehicleId}/inspection-results")
    public ResponseEntity<ViSuccess<List<Map<String, Object>>>> results(@PathVariable("vehicleId") String vehicleId) {
        return noStore(ViSuccess.ok(adminService.results(parseId(vehicleId))));
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