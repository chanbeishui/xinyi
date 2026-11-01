package com.xinyi.web.controller.rongli.owner;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViRows;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.owner.dto.OwnerView;
import com.xinyi.rongli.owner.service.OwnerAdminService;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 车主档案管理（A03，B27，ADMIN）。列表/详情/维护PATCH/关联车辆/关联订单；功能权限+部门数据范围服务层校验。
 * 列表与关联查询用 rows/total 信封；维护仅运营字段（拒绝手机/核验/微信标识），CAS 不静默覆盖。no-store。
 */
@RestController
@RequestMapping("/api/admin/owners")
public class OwnerAdminController {

    private static final Set<String> MAINT_FIELDS = Set.of("expectedVersion", "reason", "serviceName", "serviceFlag", "internalRemark");

    private final OwnerAdminService adminService;

    public OwnerAdminController(OwnerAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ViRows<OwnerView>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {
        int size = Math.min(100, Math.max(1, pageSize));
        int page = Math.max(1, pageNum);
        List<OwnerView> rows = adminService.list(page, size, keyword, status);
        return noStoreRows(ViRows.of(rows, adminService.count(keyword, status)));
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<ViSuccess<OwnerView>> detail(@PathVariable("ownerId") String ownerId) {
        return noStore(ViSuccess.ok(adminService.detail(parseId(ownerId))));
    }

    @PatchMapping("/{ownerId}")
    public ResponseEntity<ViSuccess<OwnerView>> maintenance(
            @PathVariable("ownerId") String ownerId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem,
            @RequestBody Map<String, Object> body) {
        if (idem == null || idem.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
        long id = parseId(ownerId);
        if (body == null) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "请求体不能为空");
        }
        for (String k : body.keySet()) {
            if (!MAINT_FIELDS.contains(k)) {
                // 手机号/核验结果/微信标识等不可由运营维护覆盖
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
        return noStore(ViSuccess.ok(adminService.maintenance(id, n.longValue(), str(body.get("serviceName")),
                str(body.get("serviceFlag")), str(body.get("internalRemark")), reason)));
    }

    @GetMapping("/{ownerId}/vehicles")
    public ResponseEntity<ViRows<Map<String, Object>>> vehicles(
            @PathVariable("ownerId") String ownerId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        long id = parseId(ownerId);
        int size = Math.min(100, Math.max(1, pageSize));
        int page = Math.max(1, pageNum);
        return noStoreRows(ViRows.of(adminService.vehicles(id, page, size), adminService.countVehicles(id)));
    }

    @GetMapping("/{ownerId}/orders")
    public ResponseEntity<ViRows<Map<String, Object>>> orders(
            @PathVariable("ownerId") String ownerId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        long id = parseId(ownerId);
        int size = Math.min(100, Math.max(1, pageSize));
        int page = Math.max(1, pageNum);
        return noStoreRows(ViRows.of(adminService.orders(id, page, size), adminService.countOrders(id)));
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

    private static <T> ResponseEntity<ViRows<T>> noStoreRows(ViRows<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}