package com.xinyi.web.controller.rongli.config;

import com.xinyi.rongli.config.dto.DictItemView;
import com.xinyi.rongli.config.service.ConfigService;
import com.xinyi.rongli.identity.dto.ViRows;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 业务字典查询（C01，API_010，MINI）。车型/检测类型白名单字典；契约 rows/total 分页信封。no-store。 */
@RestController
@RequestMapping("/api/common/dict")
public class DictController {

    private final ConfigService configService;

    public DictController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/{dictType}")
    public ResponseEntity<ViRows<DictItemView>> dict(
            @PathVariable("dictType") String dictType,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<DictItemView> all = configService.dict(dictType);
        int size = Math.min(100, Math.max(1, pageSize));
        int from = Math.min((Math.max(1, pageNum) - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ViRows.of(all.subList(from, to), all.size()));
    }
}