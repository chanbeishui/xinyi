package com.xinyi.rongli.file.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.file.service.PrivateFileStorage;
import com.xinyi.rongli.platform.error.*;
import java.util.*;

/** 请求白名单与实体隔离；不接收路径、部门、上传者、状态或客户端摘要。 */
public final class FileRequests {
    private FileRequests() {}

    public record Upload(
            String purpose,
            String fileName,
            String mimeType,
            long size,
            String orderNo,
            Long applicationId,
            Long stationId) {}

    public record Access(String purpose, String orderNo) {}

    public static Upload upload(Map<String, Object> body) {
        try {
            if (!Set.of(
                            "purpose",
                            "fileName",
                            "mimeType",
                            "size",
                            "orderNo",
                            "applicationId",
                            "stationId")
                    .containsAll(body.keySet())) invalid();
            String purpose = string(body, "purpose", 1, 64);
            String name = string(body, "fileName", 1, 255);
            String mime = string(body, "mimeType", 1, 64);
            if (!(body.get("size") instanceof Number)) invalid();
            long size = Long.parseLong(body.get("size").toString());
            if (size <= 0
                    || size > PrivateFileStorage.MAX_SIZE
                    || !Set.of("image/jpeg", "image/png").contains(mime)) invalid();
            if (!Set.of(
                            "VEHICLE_PHOTO",
                            "ID_CARD",
                            "DRIVER_LICENSE",
                            "VEHICLE_LICENSE",
                            "RESULT_EVIDENCE",
                            "DISPUTE",
                            "STATION_QUALIFICATION",
                            "AVATAR")
                    .contains(purpose)) invalid();
            String order = body.containsKey("orderNo") ? string(body, "orderNo", 1, 64) : null;
            Long application =
                    body.containsKey("applicationId")
                            ? ViId.parse(string(body, "applicationId", 1, 19))
                            : null;
            Long station =
                    body.containsKey("stationId")
                            ? ViId.parse(string(body, "stationId", 1, 19))
                            : null;
            int parents =
                    (order == null ? 0 : 1)
                            + (application == null ? 0 : 1)
                            + (station == null ? 0 : 1);
            if ("AVATAR".equals(purpose) ? parents != 0 : parents != 1) invalid();
            if (Set.of("ID_CARD", "DRIVER_LICENSE").contains(purpose) && application == null
                    || "STATION_QUALIFICATION".equals(purpose) && station == null
                    || Set.of("VEHICLE_PHOTO", "VEHICLE_LICENSE", "RESULT_EVIDENCE", "DISPUTE")
                                    .contains(purpose)
                            && order == null) invalid();
            return new Upload(purpose, name, mime, size, order, application, station);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    public static Access access(Map<String, Object> body) {
        if (!Set.of("purpose", "orderNo", "fields").containsAll(body.keySet())) invalid();
        String reason = string(body, "purpose", 1, 500);
        if (body.containsKey("fields")
                && (!(body.get("fields") instanceof List<?> fields)
                        || fields.size() > 1
                        || !fields.stream().allMatch("original"::equals))) invalid();
        return new Access(
                reason, body.containsKey("orderNo") ? string(body, "orderNo", 1, 64) : null);
    }

    public static String key(String key) {
        try {
            if (!UUID.fromString(key).toString().equalsIgnoreCase(key)) invalid();
        } catch (RuntimeException error) {
            invalid();
        }
        return key;
    }

    private static String string(Map<String, Object> body, String key, int min, int max) {
        if (!(body.get(key) instanceof String value)
                || value.isBlank()
                || value.length() < min
                || value.length() > max) invalid();
        return (String) body.get(key);
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
