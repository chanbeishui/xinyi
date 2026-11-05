package com.xinyi.rongli.aftersale.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;
import java.util.*;

/** 公开输入白名单；金额为整数分，部门、申请人、资金及审核状态一律由服务端确定。 */
public final class AftersaleRequests {
    private AftersaleRequests() {}

    public record Apply(
            String kind,
            String reasonCode,
            String reason,
            long requestedAmount,
            List<Long> voucherFileIds,
            long expectedOrderVersion) {}

    public record Note(long expectedVersion, String note, List<Long> voucherFileIds) {}

    public record Query(
            int pageNum,
            int pageSize,
            String keyword,
            String status,
            String kind,
            String returnState,
            String refundState) {}

    public static Apply apply(Map<String, Object> body) {
        allow(
                body,
                Set.of(
                        "kind",
                        "reasonCode",
                        "reason",
                        "requestedAmount",
                        "voucherFileIds",
                        "expectedOrderVersion"));
        String kind = text(body, "kind", 24);
        if (!Set.of("CANCEL_REFUND", "REFUND_ONLY").contains(kind)) invalid();
        return new Apply(
                kind,
                body.containsKey("reasonCode") ? text(body, "reasonCode", 64) : null,
                text(body, "reason", 1000),
                integer(body, "requestedAmount"),
                vouchers(body),
                integer(body, "expectedOrderVersion"));
    }

    public static Note note(Map<String, Object> body) {
        allow(body, Set.of("expectedVersion", "note", "voucherFileIds"));
        return new Note(integer(body, "expectedVersion"), text(body, "note", 1000), vouchers(body));
    }

    public static Query query(Map<String, String> input) {
        allow(
                input,
                Set.of(
                        "pageNum",
                        "pageSize",
                        "keyword",
                        "status",
                        "sort",
                        "kind",
                        "returnState",
                        "refundState"));
        try {
            int page = Integer.parseInt(input.getOrDefault("pageNum", "1"));
            int size = Integer.parseInt(input.getOrDefault("pageSize", "20"));
            if (page < 1 || page > 1000000 || size < 1 || size > 100) invalid();
            if (input.containsKey("sort") && !"createdAt:desc".equals(input.get("sort"))) invalid();
            String keyword = input.get("keyword");
            if (keyword != null && keyword.length() > 64) invalid();
            enumValue(input, "status", Set.of("PENDING_REVIEW", "EXECUTING", "CLOSED", "REJECTED"));
            enumValue(input, "kind", Set.of("CANCEL_REFUND", "REFUND_ONLY"));
            enumValue(
                    input,
                    "returnState",
                    Set.of("UNDETERMINED", "NOT_REQUIRED", "WAIT_RETURN", "RETURNED"));
            enumValue(
                    input,
                    "refundState",
                    Set.of(
                            "NONE",
                            "NOT_REQUIRED",
                            "WAIT_RETURN",
                            "READY",
                            "PROCESSING",
                            "SUCCESS",
                            "UNKNOWN",
                            "ABNORMAL",
                            "CLOSED"));
            return new Query(
                    page,
                    size,
                    keyword,
                    input.get("status"),
                    input.get("kind"),
                    input.get("returnState"),
                    input.get("refundState"));
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static void enumValue(Map<String, String> input, String key, Set<String> allowed) {
        if (input.containsKey(key) && !allowed.contains(input.get(key))) invalid();
    }

    public static String key(String value) {
        try {
            if (!UUID.fromString(value).toString().equalsIgnoreCase(value)) invalid();
        } catch (RuntimeException error) {
            invalid();
        }
        return value;
    }

    private static List<Long> vouchers(Map<String, Object> body) {
        if (!body.containsKey("voucherFileIds")) return List.of();
        if (!(body.get("voucherFileIds") instanceof List<?>)) invalid();
        List<?> values = (List<?>) body.get("voucherFileIds");
        if (values.size() > 9) invalid();
        List<Long> result = new ArrayList<>();
        try {
            for (Object value : values) {
                if (!(value instanceof String)) invalid();
                result.add(ViId.parse((String) value));
            }
        } catch (IllegalArgumentException error) {
            invalid();
        }
        if (new HashSet<>(result).size() != result.size()) invalid();
        // 全组按稳定次序加锁，避免多凭证同时追加时反向等待。
        return result.stream().sorted().toList();
    }

    private static long integer(Map<String, Object> body, String key) {
        try {
            if (!(body.get(key) instanceof Number)) invalid();
            long value = Long.parseLong(body.get(key).toString());
            if (value < 0 || value > 9007199254740991L) invalid();
            return value;
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String text(Map<String, Object> body, String key, int max) {
        if (!(body.get(key) instanceof String value) || value.isBlank() || value.length() > max)
            invalid();
        return (String) body.get(key);
    }

    private static void allow(Map<String, ?> body, Set<String> allowed) {
        if (!allowed.containsAll(body.keySet())) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
