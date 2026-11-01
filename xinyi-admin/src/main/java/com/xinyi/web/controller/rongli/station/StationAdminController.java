package com.xinyi.web.controller.rongli.station;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.ViRows;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.station.dto.StationInput;
import com.xinyi.rongli.station.dto.StationView;
import com.xinyi.rongli.station.service.StationService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 后台站点管理（C02，API_128/129/130，ADMIN）。列表 rows/total（数据范围按 dept_id）；创建/更新 data（CAS）。
 * 写操作强制 Idempotency-Key；deptId 为 ADMIN 受控迁移字段（迁部门有在手订单则拒绝）。no-store。
 */
@RestController
@RequestMapping("/api/admin/stations")
public class StationAdminController {

    private final StationService stationService;

    public StationAdminController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<ViRows<StationView>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        int size = Math.min(100, Math.max(1, pageSize));
        int page = Math.max(1, pageNum);
        List<StationView> rows = stationService.adminList(page, size);
        return noStoreRows(ViRows.of(rows, stationService.adminCount()));
    }

    @PostMapping
    public ResponseEntity<ViSuccess<StationView>> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idem,
            @RequestBody Map<String, Object> body) {
        requireIdem(idem);
        StationInput in = StationWebSupport.parse(body, false, true);
        return noStore(ViSuccess.ok(stationService.create(in)));
    }

    @PutMapping("/{stationId}")
    public ResponseEntity<ViSuccess<StationView>> update(
            @PathVariable("stationId") String stationId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idem,
            @RequestBody Map<String, Object> body) {
        requireIdem(idem);
        StationInput in = StationWebSupport.parse(body, true, true);
        return noStore(ViSuccess.ok(stationService.adminUpdate(parseId(stationId), in)));
    }

    private long parseId(String v) {
        try {
            return ViId.parse(v);
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "ID 非法");
        }
    }

    private static void requireIdem(String idem) {
        if (idem == null || idem.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }

    private static <T> ResponseEntity<ViRows<T>> noStoreRows(ViRows<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}