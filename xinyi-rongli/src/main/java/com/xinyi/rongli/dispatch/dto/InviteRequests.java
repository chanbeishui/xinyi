package com.xinyi.rongli.dispatch.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;

import java.util.*;

/** 精确写键和查询白名单，客户端不能提供公司范围、候选可信会话或SQL片段。 */
public final class InviteRequests {
    private InviteRequests() {}

    public record Policy(
            long expectedVersion,
            int inviteResponseSeconds,
            int radiusMeter,
            int expandedRadiusMeter,
            int overallDeadlineSeconds,
            String roundPolicy,
            String distanceMode,
            List<Map<String, Object>> licenseCompatibility,
            String reason) {}

    public record Response(
            String decision,
            long expectedVersion,
            long expectedOrderVersion,
            long expectedSessionId,
            long expectedContextVersion,
            String reason) {}

    public record Manual(
            String orderNo,
            long driverId,
            long expectedOrderVersion,
            long expectedDriverVersion,
            String reason) {}

    public record Revoke(long expectedVersion, long expectedOrderVersion, String reason) {}

    public record Query(
            int pageNum,
            int pageSize,
            String orderNo,
            Long stationId,
            String status,
            Long serviceCompanyId,
            Long driverId,
            String sort) {
        public int offset() {
            return (pageNum - 1) * pageSize;
        }
    }

    public static Policy policy(Map<String, Object> b) {
        exact(
                b,
                "expectedVersion",
                "inviteResponseSeconds",
                "radiusMeter",
                "expandedRadiusMeter",
                "overallDeadlineSeconds",
                "roundPolicy",
                "distanceMode",
                "licenseCompatibility",
                "reason");
        int radius = integer(b.get("radiusMeter"), 1, 1000000);
        int expanded = integer(b.get("expandedRadiusMeter"), radius, 1000000);
        String round = text(b.get("roundPolicy"), 64, false),
                mode = text(b.get("distanceMode"), 64, false);
        if (!"COMPANY_CURSOR".equals(round) || !"GCJ02_GREAT_CIRCLE".equals(mode)) invalid();
        if (!(b.get("licenseCompatibility") instanceof List<?>)) invalid();
        List<?> rows = (List<?>) b.get("licenseCompatibility");
        if (rows.isEmpty() || rows.size() > 200) data();
        Set<String> codes = new HashSet<>();
        List<Map<String, Object>> compat = new ArrayList<>();
        for (Object item : rows) {
            if (!(item instanceof Map<?, ?>)) invalid();
            Map<String, Object> entry = (Map<String, Object>) item;
            exact(entry, "vehicleTypeCode", "licenseClasses");
            String code = text(entry.get("vehicleTypeCode"), 64, false);
            if (!codes.add(code) || !(entry.get("licenseClasses") instanceof List<?>)) data();
            List<?> classes = (List<?>) entry.get("licenseClasses");
            if (classes.isEmpty() || classes.size() > 32) data();
            TreeSet<String> allowed = new TreeSet<>();
            for (Object value : classes) if (!allowed.add(text(value, 32, false))) data();
            compat.add(Map.of("vehicleTypeCode", code, "licenseClasses", List.copyOf(allowed)));
        }
        compat.sort(Comparator.comparing(v -> v.get("vehicleTypeCode").toString()));
        return new Policy(
                version(b.get("expectedVersion")),
                integer(b.get("inviteResponseSeconds"), 1, 3600),
                radius,
                expanded,
                integer(b.get("overallDeadlineSeconds"), 1, 86400),
                round,
                mode,
                List.copyOf(compat),
                text(b.get("reason"), 500, false));
    }

    public static Response response(Map<String, Object> b) {
        exact(
                b,
                "decision",
                "expectedVersion",
                "expectedOrderVersion",
                "expectedSessionId",
                "expectedContextVersion",
                "reason");
        String decision = text(b.get("decision"), 16, false);
        if (!Set.of("ACCEPT", "REJECT").contains(decision)) invalid();
        return new Response(
                decision,
                version(b.get("expectedVersion")),
                version(b.get("expectedOrderVersion")),
                id(b.get("expectedSessionId")),
                version(b.get("expectedContextVersion")),
                text(b.get("reason"), 500, true));
    }

    public static Manual manual(Map<String, Object> b) {
        exact(b, "orderNo", "driverId", "expectedOrderVersion", "expectedDriverVersion", "reason");
        return new Manual(
                text(b.get("orderNo"), 64, false),
                id(b.get("driverId")),
                version(b.get("expectedOrderVersion")),
                version(b.get("expectedDriverVersion")),
                text(b.get("reason"), 500, false));
    }

    public static Revoke revoke(Map<String, Object> b) {
        exact(b, "expectedVersion", "expectedOrderVersion", "reason");
        return new Revoke(
                version(b.get("expectedVersion")),
                version(b.get("expectedOrderVersion")),
                text(b.get("reason"), 500, false));
    }

    public static Query query(Map<String, String> b, String type) {
        Set<String> keys =
                switch (type) {
                    case "QUEUE" ->
                            Set.of(
                                    "pageNum",
                                    "pageSize",
                                    "orderNo",
                                    "stationId",
                                    "status",
                                    "serviceCompanyId");
                    case "CANDIDATE" -> Set.of("pageNum", "pageSize", "orderNo", "sort");
                    case "ADMIN_INVITE" ->
                            Set.of("pageNum", "pageSize", "orderNo", "driverId", "status");
                    default -> Set.of("pageNum", "pageSize", "orderNo", "status");
                };
        if (!keys.containsAll(b.keySet())) invalid();
        int page = queryInt(b.get("pageNum"), 1, 1000000, 1),
                size = queryInt(b.get("pageSize"), 1, 100, 20);
        String order = b.containsKey("orderNo") ? text(b.get("orderNo"), 64, false) : null;
        if ("CANDIDATE".equals(type) && order == null) invalid();
        String status = b.get("status"), sort = b.getOrDefault("sort", "cursor");
        if (status != null) {
            Set<String> valid =
                    "QUEUE".equals(type)
                            ? Set.of(
                                    "SCHEDULED",
                                    "PENDING_DISPATCH",
                                    "DISPATCHING",
                                    "MANUAL_DISPATCH")
                            : Set.of("WAIT_RESPONSE", "ACCEPTED", "REJECTED", "EXPIRED", "REVOKED");
            if (!valid.contains(status)) invalid();
        }
        if (!Set.of("cursor", "distanceAsc").contains(sort)) invalid();
        return new Query(
                page,
                size,
                order,
                b.containsKey("stationId") ? id(b.get("stationId")) : null,
                status,
                b.containsKey("serviceCompanyId") ? id(b.get("serviceCompanyId")) : null,
                b.containsKey("driverId") ? id(b.get("driverId")) : null,
                sort);
    }

    public static long id(Object v) {
        if (!(v instanceof String)) invalid();
        try {
            return ViId.parse((String) v);
        } catch (IllegalArgumentException e) {
            invalid();
            return 0;
        }
    }

    public static void none(Map<String, String> b) {
        if (!b.isEmpty()) invalid();
    }

    private static long version(Object v) {
        if (!(v instanceof Number)
                || v instanceof Double
                || v instanceof Float
                || v instanceof java.math.BigDecimal) invalid();
        try {
            long n = Long.parseLong(v.toString());
            if (n < 0 || n > 9007199254740991L) invalid();
            return n;
        } catch (NumberFormatException e) {
            invalid();
            return 0;
        }
    }

    private static int integer(Object v, int min, int max) {
        if (!(v instanceof Number)
                || v instanceof Double
                || v instanceof Float
                || v instanceof java.math.BigDecimal) invalid();
        try {
            long n = Long.parseLong(v.toString());
            if (n < min || n > max) data();
            return (int) n;
        } catch (NumberFormatException failure) {
            invalid();
            return 0;
        }
    }

    private static int queryInt(String s, int min, int max, int def) {
        if (s == null) return def;
        try {
            if (!s.matches("[1-9][0-9]*")) invalid();
            int n = Integer.parseInt(s);
            if (n < min || n > max) invalid();
            return n;
        } catch (NumberFormatException e) {
            invalid();
            return 0;
        }
    }

    private static String text(Object v, int max, boolean nullable) {
        if (v == null && nullable) return null;
        if (!(v instanceof String)) invalid();
        String s = ((String) v).trim();
        if (s.length() > max || (!nullable && s.isEmpty())) invalid();
        return s.isEmpty() ? null : s;
    }

    private static void exact(Map<String, Object> b, String... keys) {
        if (b == null || !b.keySet().equals(Set.of(keys))) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void data() {
        throw new ViException(ViErrorCode.VI_DATA_INVALID);
    }
}
