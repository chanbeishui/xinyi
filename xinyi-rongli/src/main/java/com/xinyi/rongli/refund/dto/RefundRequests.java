package com.xinyi.rongli.refund.dto;

import com.xinyi.rongli.aftersale.dto.AftersaleRequests;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;
import java.util.*;

/** 只接收版本及操作说明；金额、原渠道、权威部门、核验结论均不能从请求绑定。 */
public final class RefundRequests {
    private RefundRequests() {}

    public record Action(long expectedVersion, String reason) {}

    public record Query(
            int pageNum, int pageSize, Long caseId, String orderNo, String type, String status) {}

    public static String key(String value) {
        return AftersaleRequests.key(value);
    }

    public static Action action(Map<String, Object> body) {
        if (!body.keySet().equals(Set.of("expectedVersion", "reason"))
                || !(body.get("expectedVersion") instanceof Number)
                || !(body.get("reason") instanceof String reason)
                || reason.isBlank()
                || reason.length() > 500) invalid();
        try {
            long version = Long.parseLong(body.get("expectedVersion").toString());
            if (version < 0 || version > 9007199254740991L) invalid();
            return new Action(version, (String) body.get("reason"));
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    public static Query query(Map<String, String> body) {
        if (!Set.of("pageNum", "pageSize", "caseId", "orderNo", "type", "status")
                .containsAll(body.keySet())) invalid();
        try {
            int page = Integer.parseInt(body.getOrDefault("pageNum", "1")),
                    size = Integer.parseInt(body.getOrDefault("pageSize", "10"));
            if (page < 1 || page > 1000000 || size < 1 || size > 100) invalid();
            if (body.containsKey("type") && !Set.of("PAYMENT", "REFUND").contains(body.get("type")))
                invalid();
            if (body.containsKey("status")
                    && !Set.of("OPEN", "CHECKING", "RESOLVED").contains(body.get("status")))
                invalid();
            String no = body.get("orderNo");
            if (no != null && !no.matches("[A-Za-z0-9_-]{1,64}")) invalid();
            return new Query(
                    page,
                    size,
                    body.containsKey("caseId") ? ViId.parse(body.get("caseId")) : null,
                    no,
                    body.get("type"),
                    body.get("status"));
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
