package com.xinyi.web.controller.rongli.identity;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.dto.EmployeeLinkAdmin;
import com.xinyi.rongli.identity.dto.EmployeeLinkRecheckResult;
import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.identity.service.EmployeeLinkAdminService;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台员工微信关联管理（I05，B36，ADMIN 域，框架 JWT 链）。功能权限+部门数据范围在服务层校验；
 * 暂停/撤销/允许重核需 expectedVersion+reason（CAS），不接收目标 openid 替换。响应 no-store。
 */
@RestController
@RequestMapping("/api/admin/employee-links")
public class EmployeeLinkAdminController {

    private final EmployeeLinkAdminService adminService;

    public EmployeeLinkAdminController(EmployeeLinkAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ViSuccess<List<EmployeeLinkAdmin>>> list() {
        return noStore(ViSuccess.ok(adminService.list()));
    }

    @GetMapping("/{bindingId}")
    public ResponseEntity<ViSuccess<EmployeeLinkAdmin>> detail(@PathVariable("bindingId") String bindingId) {
        return noStore(ViSuccess.ok(adminService.detail(parseId(bindingId))));
    }

    @PostMapping("/{bindingId}/suspend")
    public ResponseEntity<ViSuccess<EmployeeLinkAdmin>> suspend(@PathVariable("bindingId") String bindingId,
                                                                @RequestBody Map<String, Object> body) {
        Action a = parseAction(body);
        return noStore(ViSuccess.ok(adminService.suspend(parseId(bindingId), a.version, a.reason)));
    }

    @PostMapping("/{bindingId}/revoke")
    public ResponseEntity<ViSuccess<EmployeeLinkAdmin>> revoke(@PathVariable("bindingId") String bindingId,
                                                               @RequestBody Map<String, Object> body) {
        Action a = parseAction(body);
        return noStore(ViSuccess.ok(adminService.revoke(parseId(bindingId), a.version, a.reason)));
    }

    @PostMapping("/{bindingId}/allow-rematch")
    public ResponseEntity<ViSuccess<EmployeeLinkRecheckResult>> allowRematch(@PathVariable("bindingId") String bindingId,
                                                                            @RequestBody Map<String, Object> body) {
        Action a = parseAction(body);
        return noStore(ViSuccess.ok(adminService.allowRematch(parseId(bindingId), a.version, a.reason)));
    }

    private record Action(long version, String reason) {
    }

    private Action parseAction(Map<String, Object> body) {
        if (body == null || !body.keySet().equals(Set.of("expectedVersion", "reason"))) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "只接受 expectedVersion 与 reason");
        }
        if (!(body.get("expectedVersion") instanceof Number n) || n.longValue() < 0) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "expectedVersion 缺失或非法");
        }
        Object r = body.get("reason");
        if (!(r instanceof String reason) || reason.isBlank() || reason.length() > 500) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "reason 必填且不超过 500 字");
        }
        return new Action(n.longValue(), reason);
    }

    private long parseId(String value) {
        try {
            return ViId.parse(value);
        } catch (RuntimeException e) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "bindingId 非法");
        }
    }

    private static <T> ResponseEntity<ViSuccess<T>> noStore(ViSuccess<T> body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}