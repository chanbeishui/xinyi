package com.xinyi.rongli.dispatch.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.dispatch.service.ListeningDayPolicy;
import com.xinyi.rongli.platform.error.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/** 明确意图版本与来源观察值，不能用同为0的旧会话/旧公司请求漂移到新的设备上下文。 */
public final class DispatchRequests {
    private DispatchRequests() {}

    public record Source(
            long expectedCompanyRevision, long expectedSessionId, long expectedContextVersion) {}

    public record Listening(
            boolean enabled,
            long expectedVersion,
            Long expectedConfigVersion,
            String expectedBusinessDate,
            Source source) {}

    public record Position(
            BigDecimal lng,
            BigDecimal lat,
            Integer accuracyMeter,
            Instant capturedAt,
            long expectedVersion,
            Source source) {
        @Override
        public String toString() {
            return "Position[coordinates=REDACTED]";
        }
    }

    public record Presence(
            String clientState, Instant observedAt, long expectedVersion, Source source) {}

    public record Config(
            long expectedVersion,
            String closingTime,
            int locationMaxAgeSeconds,
            int reachabilityMaxAgeSeconds,
            int maxAccuracyMeter,
            int locationReportIntervalSeconds,
            String reason) {}

    public static Listening listening(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "enabled",
                        "expectedVersion",
                        "expectedCompanyRevision",
                        "expectedConfigVersion",
                        "expectedSessionId",
                        "expectedContextVersion",
                        "expectedBusinessDate"));
        if (!(body.get("enabled") instanceof Boolean)) invalid();
        boolean enabled = (Boolean) body.get("enabled");
        Long config =
                body.get("expectedConfigVersion") == null
                        ? null
                        : version(body.get("expectedConfigVersion"));
        if (enabled && config == null) invalid();
        return new Listening(
                enabled,
                version(body.get("expectedVersion")),
                config,
                businessDate(body.get("expectedBusinessDate")),
                source(body));
    }

    public static Position location(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "lng",
                        "lat",
                        "accuracyMeter",
                        "capturedAt",
                        "coordinateSystem",
                        "expectedVersion",
                        "expectedCompanyRevision",
                        "expectedSessionId",
                        "expectedContextVersion"));
        if (!"GCJ02".equals(body.get("coordinateSystem"))) invalid();
        BigDecimal lng = coordinate(body.get("lng"), 180), lat = coordinate(body.get("lat"), 90);
        Integer accuracy =
                body.get("accuracyMeter") == null
                        ? null
                        : integer(body.get("accuracyMeter"), 0, 100000);
        return new Position(
                lng,
                lat,
                accuracy,
                instant(body.get("capturedAt")),
                version(body.get("expectedVersion")),
                source(body));
    }

    public static Presence presence(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "clientState",
                        "observedAt",
                        "expectedVersion",
                        "expectedCompanyRevision",
                        "expectedSessionId",
                        "expectedContextVersion"));
        String state = text(body.get("clientState"), 16);
        if (!Set.of("FOREGROUND", "BACKGROUND").contains(state)) invalid();
        return new Presence(
                state,
                instant(body.get("observedAt")),
                version(body.get("expectedVersion")),
                source(body));
    }

    public static Config config(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "expectedVersion",
                        "closingTime",
                        "locationMaxAgeSeconds",
                        "reachabilityMaxAgeSeconds",
                        "maxAccuracyMeter",
                        "locationReportIntervalSeconds",
                        "reason"));
        if (!(body.get("closingTime") instanceof String)) invalid();
        String closing = (String) body.get("closingTime");
        ListeningDayPolicy.closingTime(closing);
        int location = integer(body.get("locationMaxAgeSeconds"), 31, 86400);
        int reachability = integer(body.get("reachabilityMaxAgeSeconds"), 31, 86400);
        int interval = integer(body.get("locationReportIntervalSeconds"), 30, 3600);
        if (interval >= location || interval >= reachability) data("上报间隔必须小于位置和近期交互的有效期");
        return new Config(
                version(body.get("expectedVersion")),
                closing,
                location,
                reachability,
                integer(body.get("maxAccuracyMeter"), 1, 100000),
                interval,
                text(body.get("reason"), 500));
    }

    public static Map<String, Object> tree(Listening input) {
        var map = sourceTree(input.source());
        map.put("enabled", input.enabled());
        map.put("expectedVersion", input.expectedVersion());
        map.put("expectedConfigVersion", input.expectedConfigVersion());
        map.put("expectedBusinessDate", input.expectedBusinessDate());
        return map;
    }

    public static Map<String, Object> tree(Position input) {
        var map = sourceTree(input.source());
        map.put("lng", input.lng());
        map.put("lat", input.lat());
        map.put("accuracyMeter", input.accuracyMeter());
        map.put("coordinateSystem", "GCJ02");
        map.put("capturedAt", input.capturedAt().toString());
        map.put("expectedVersion", input.expectedVersion());
        return map;
    }

    public static Map<String, Object> tree(Presence input) {
        var map = sourceTree(input.source());
        map.put("clientState", input.clientState());
        map.put("observedAt", input.observedAt().toString());
        map.put("expectedVersion", input.expectedVersion());
        return map;
    }

    private static Map<String, Object> sourceTree(Source input) {
        Map<String, Object> map = new TreeMap<>();
        map.put("expectedCompanyRevision", input.expectedCompanyRevision());
        map.put("expectedSessionId", Long.toString(input.expectedSessionId()));
        map.put("expectedContextVersion", input.expectedContextVersion());
        return map;
    }

    private static Source source(Map<String, Object> body) {
        if (!(body.get("expectedSessionId") instanceof String)) invalid();
        return new Source(
                version(body.get("expectedCompanyRevision")),
                id((String) body.get("expectedSessionId")),
                version(body.get("expectedContextVersion")));
    }

    public static long id(String value) {
        try {
            return ViId.parse(value);
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String businessDate(Object input) {
        if (!(input instanceof String)) invalid();
        String value = (String) input;
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) invalid();
        try {
            java.time.LocalDate.parse(value);
            return value;
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static Instant instant(Object input) {
        String value = text(input, 64);
        if (!value.matches(
                "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,3})?Z"))
            invalid();
        try {
            return Instant.parse(value);
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    public static long version(Object value) {
        if (!(value instanceof Integer || value instanceof Long)) invalid();
        long number = ((Number) value).longValue();
        if (number < 0 || number > 9007199254740991L) invalid();
        return number;
    }

    private static int integer(Object value, int min, int max) {
        if (!(value instanceof Integer || value instanceof Long)) invalid();
        long number = ((Number) value).longValue();
        if (number < min || number > max) data("数值超出允许范围");
        return (int) number;
    }

    private static BigDecimal coordinate(Object value, int max) {
        if (!(value instanceof Number)) invalid();
        if (!Double.isFinite(((Number) value).doubleValue())) data("坐标必须是有限数值");
        BigDecimal result = new BigDecimal(value.toString()).stripTrailingZeros();
        if (result.abs().compareTo(BigDecimal.valueOf(max)) > 0) data("坐标超出允许范围");
        return result;
    }

    private static String text(Object value, int max) {
        if (!(value instanceof String)) invalid();
        String text = ((String) value).trim();
        if (text.isEmpty() || text.length() > max) invalid();
        return text;
    }

    private static void exact(Map<String, Object> body, Set<String> keys) {
        if (body == null || !body.keySet().equals(keys)) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void data(String text) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, text);
    }
}
