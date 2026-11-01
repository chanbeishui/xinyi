package com.xinyi.web.controller.rongli.station;

import com.xinyi.rongli.identity.dto.ViRows;
import com.xinyi.rongli.station.dto.PublicStationView;
import com.xinyi.rongli.station.service.StationService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公开营业站点列表（C02，API_011，MINI）。仅启用站点的公开营业信息；不含授权范围/人员/内部状态/版本。rows/total。no-store。 */
@RestController
@RequestMapping("/api/common/stations")
public class StationCommonController {

    private final StationService stationService;

    public StationCommonController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<ViRows<PublicStationView>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        int size = Math.min(100, Math.max(1, pageSize));
        int page = Math.max(1, pageNum);
        List<PublicStationView> rows = stationService.publicList(page, size);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ViRows.of(rows, stationService.publicCount()));
    }
}