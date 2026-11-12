package com.xinyi.rongli.dispatch.controller;

import com.xinyi.rongli.dispatch.dto.DispatchRequests;
import com.xinyi.rongli.dispatch.service.CompanyListeningConfigService;
import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.error.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** D01公司听单参数独立于后续D02全局派单策略，不以客户端主部门推定目标公司。 */
@RestController
@RequestMapping("/api/admin/dispatch/companies/{companyId}/listening-config")
public class CompanyListeningConfigController {
    private final CompanyListeningConfigService service;

    public CompanyListeningConfigController(CompanyListeningConfigService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> get(
            @PathVariable String companyId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestParam Map<String, String> query) {
        check(client, query);
        return response(service.get(DispatchRequests.id(companyId)));
    }

    @PutMapping
    public ResponseEntity<?> put(
            @PathVariable String companyId,
            @RequestHeader(value = "X-Client-Type", required = false) String client,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestParam Map<String, String> query,
            @RequestBody Map<String, Object> body) {
        check(client, query);
        return response(
                service.put(
                        DispatchRequests.id(companyId),
                        DispatchRequests.config(body),
                        FileRequests.key(key)));
    }

    private static void check(String client, Map<String, String> query) {
        if (!"admin".equals(client) || !query.isEmpty())
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static ResponseEntity<?> response(Object data) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ViSuccess.ok(data));
    }
}
