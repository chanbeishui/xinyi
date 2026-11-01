package com.xinyi.web.controller.rongli.station;

import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.station.dto.StationInput;
import com.xinyi.rongli.station.dto.StationView;
import com.xinyi.rongli.station.service.StationService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 员工本站点信息（C02，API_095/096，MINI）。仅本人切换到的站点；员工端不接受 deptId 迁移。no-store。 */
@RestController
@RequestMapping("/api/app/station/info")
public class StationInfoController {

    private final StationService stationService;

    public StationInfoController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<StationView>> get() {
        return noStore(ViSuccess.ok(stationService.getOwnStation()));
    }

    @PutMapping
    public ResponseEntity<ViSuccess<StationView>> put(
            @RequestHeader(value = "Idempotency-Key", required = false) String idem,
            @RequestBody Map<String, Object> body) {
        requireIdem(idem);
        StationInput in = StationWebSupport.parse(body, true, false);
        return noStore(ViSuccess.ok(stationService.updateOwnStation(in)));
    }

    private static void requireIdem(String idem) {
        if (idem == null || idem.isBlank()) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少 Idempotency-Key");
        }
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}