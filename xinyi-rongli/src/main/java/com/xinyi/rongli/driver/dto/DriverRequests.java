package com.xinyi.rongli.driver.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.refund.dto.RefundRequests;
import java.util.*;

/** 后台公开输入白名单；客户端不得绑定资格、听单偏好、任务占用或内部权限参数。 */
public final class DriverRequests {
    private DriverRequests() {}

    public record Query(
            int pageNum,
            int pageSize,
            String keyword,
            String status,
            String sort,
            Long ownerDeptId) {}

    public record Maintenance(
            long expectedVersion,
            String reason,
            boolean ownerPresent,
            Long ownerDeptId,
            boolean remarkPresent,
            String internalRemark) {}

    public record Search(Query query, byte[] phoneLookup) {}

    public static Query query(Map<String, String> values, boolean assignments) {
        Set<String> allowed =
                assignments
                        ? Set.of("pageNum", "pageSize", "keyword", "status", "sort")
                        : Set.of("pageNum", "pageSize", "keyword", "status", "sort", "ownerDeptId");
        if (!allowed.containsAll(values.keySet())) invalid();
        try {
            int page = Integer.parseInt(values.getOrDefault("pageNum", "1")),
                    size = Integer.parseInt(values.getOrDefault("pageSize", "20"));
            if (page < 1 || page > 1000000 || size < 1 || size > 100) invalid();
            String keyword = values.get("keyword");
            if (keyword != null && keyword.length() > 100) invalid();
            String status = values.get("status");
            Set<String> statuses =
                    assignments
                            ? Set.of("ACTIVE", "ENDED", "RETURNED")
                            : Set.of(
                                    "DRAFT",
                                    "SUBMITTED",
                                    "APPROVED",
                                    "REJECTED",
                                    "EXPIRED",
                                    "SUSPENDED");
            if (status != null && !statuses.contains(status)) invalid();
            String sort = values.getOrDefault("sort", assignments ? "acceptedAtDesc" : "idDesc");
            if (!(assignments ? Set.of("acceptedAtDesc") : Set.of("idDesc", "nameAsc"))
                    .contains(sort)) invalid();
            return new Query(
                    page,
                    size,
                    keyword,
                    status,
                    sort,
                    values.containsKey("ownerDeptId")
                            ? ViId.parse(values.get("ownerDeptId"))
                            : null);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    public static Maintenance maintenance(Map<String, Object> body) {
        if (!Set.of("expectedVersion", "reason", "ownerDeptId", "internalRemark")
                        .containsAll(body.keySet())
                || !body.containsKey("ownerDeptId") && !body.containsKey("internalRemark"))
            invalid();
        Map<String, Object> action = new HashMap<>();
        action.put("expectedVersion", body.get("expectedVersion"));
        action.put("reason", body.get("reason"));
        var common = RefundRequests.action(action);
        Long owner = null;
        try {
            if (body.containsKey("ownerDeptId")) {
                if (!(body.get("ownerDeptId") instanceof String)) invalid();
                owner = ViId.parse((String) body.get("ownerDeptId"));
            }
        } catch (IllegalArgumentException error) {
            invalid();
        }
        if (body.containsKey("internalRemark")
                && (!(body.get("internalRemark") instanceof String remark)
                        || remark.length() > 1000)) invalid();
        return new Maintenance(
                common.expectedVersion(),
                common.reason(),
                body.containsKey("ownerDeptId"),
                owner,
                body.containsKey("internalRemark"),
                (String) body.get("internalRemark"));
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
