package com.xinyi.rongli.verification.dto;

import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.training.dto.LearningRequests;

import java.util.*;

/** 设备认证请求不接收姓名身份证/机主结论/客户端passed，稳定目标及明确同意必须逐项校验。 */
public final class VerificationRequests {
    private VerificationRequests() {}

    public record Create(
            long applicationId,
            long expectedApplicationVersion,
            long expectedCompanyRevision,
            long expectedMaterialRevision,
            String consentVersion,
            String consentTextHash,
            boolean acceptedConsent) {}

    public record Close(long expectedVersion, String reason) {}

    public record Confirm(
            long expectedVersion, String resultJSON, String resultJSONSignature, boolean recover) {
        @Override
        public String toString() {
            return "DeviceAuthenticationConfirm[REDACTED]";
        }
    }

    public static Confirm confirm(Map<String, Object> body) {
        if (body.containsKey("recover")) {
            LearningRequests.exact(body, Set.of("expectedVersion", "recover"));
            if (!Boolean.TRUE.equals(body.get("recover"))) invalid();
            return new Confirm(
                    LearningRequests.version(body.get("expectedVersion")), null, null, true);
        }
        LearningRequests.exact(
                body, Set.of("expectedVersion", "resultJSON", "resultJSONSignature"));
        if (!(body.get("resultJSON") instanceof String raw)
                || raw.isEmpty()
                || raw.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 16384) invalid();
        if (!(body.get("resultJSONSignature") instanceof String signature)
                || signature.isEmpty()
                || signature.length() > 4096) invalid();
        String signature = (String) body.get("resultJSONSignature");
        try {
            if (java.util.Base64.getDecoder().decode(signature).length == 0) invalid();
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
        return new Confirm(
                LearningRequests.version(body.get("expectedVersion")),
                (String) body.get("resultJSON"),
                signature,
                false);
    }

    public record Query(
            int pageNum, int pageSize, Long ownerDeptId, String status, String keyword) {}

    public static Create create(Map<String, Object> body) {
        LearningRequests.exact(
                body,
                Set.of(
                        "applicationId",
                        "expectedApplicationVersion",
                        "expectedCompanyRevision",
                        "expectedMaterialRevision",
                        "consentVersion",
                        "consentTextHash",
                        "acceptedConsent"));
        if (!Boolean.TRUE.equals(body.get("acceptedConsent"))) invalid();
        String hash = text(body.get("consentTextHash"), 64);
        if (!hash.matches("[0-9a-f]{64}")) invalid();
        return new Create(
                LearningRequests.id(body.get("applicationId")),
                LearningRequests.version(body.get("expectedApplicationVersion")),
                LearningRequests.version(body.get("expectedCompanyRevision")),
                LearningRequests.version(body.get("expectedMaterialRevision")),
                text(body.get("consentVersion"), 64),
                hash,
                true);
    }

    public static Close close(Map<String, Object> body) {
        LearningRequests.exact(body, Set.of("expectedVersion", "reason"));
        return new Close(
                LearningRequests.version(body.get("expectedVersion")),
                text(body.get("reason"), 500));
    }

    public static Query query(Map<String, String> body) {
        if (!Set.of("pageNum", "pageSize", "ownerDeptId", "status", "keyword")
                .containsAll(body.keySet())) invalid();
        try {
            int page = Integer.parseInt(body.getOrDefault("pageNum", "1")),
                    size = Integer.parseInt(body.getOrDefault("pageSize", "20"));
            if (page < 1 || page > 1000000 || size < 1 || size > 100) invalid();
            String status = body.get("status"), keyword = body.get("keyword");
            if (status != null
                    && !Set.of(
                                    "CREATED",
                                    "PROCESSING",
                                    "UNKNOWN",
                                    "PASSED",
                                    "FAILED",
                                    "EXPIRED",
                                    "CLOSED")
                            .contains(status)) invalid();
            if (keyword != null && keyword.length() > 100) invalid();
            return new Query(
                    page,
                    size,
                    body.containsKey("ownerDeptId")
                            ? LearningRequests.id(body.get("ownerDeptId"))
                            : null,
                    status,
                    keyword);
        } catch (NumberFormatException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String text(Object raw, int max) {
        if (!(raw instanceof String value) || value.isBlank() || value.length() > max) invalid();
        return ((String) raw).trim();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
