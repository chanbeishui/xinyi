package com.xinyi.rongli.verification.dto;

import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.training.dto.LearningRequests;
import java.util.Map;
import java.util.Set;

/** 人工确认无需凭证或标准发布；只收结论和可选备注，不收公司、审核人或证明ID。 */
public final class ManualVerificationRequests {
    private ManualVerificationRequests() {}

    public record Create(
            long applicationId,
            long expectedApplicationVersion,
            long expectedCompanyRevision,
            long expectedMaterialRevision,
            String type,
            String conclusion,
            String note) {}

    public record Review(long expectedVersion, String decision, String note) {}

    public record Query(
            Long ownerDeptId,
            Long applicationId,
            String type,
            String status,
            int pageNum,
            int pageSize) {
        public int offset() {
            return (pageNum - 1) * pageSize;
        }
    }

    public static Create create(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "applicationId",
                        "expectedApplicationVersion",
                        "expectedCompanyRevision",
                        "expectedMaterialRevision",
                        "type",
                        "conclusion",
                        "note"));
        return new Create(
                id(body.get("applicationId")),
                version(body.get("expectedApplicationVersion")),
                version(body.get("expectedCompanyRevision")),
                version(body.get("expectedMaterialRevision")),
                choice(body.get("type"), Set.of("BACKGROUND", "LICENSE")),
                choice(body.get("conclusion"), Set.of("PASSED", "FAILED")),
                note(body.get("note")));
    }

    public static Review review(Map<String, Object> body) {
        exact(body, Set.of("expectedVersion", "decision", "note"));
        return new Review(
                version(body.get("expectedVersion")),
                choice(body.get("decision"), Set.of("CONFIRM", "RETURN")),
                note(body.get("note")));
    }

    public static Query query(Map<String, String> query, boolean applications) {
        Set<String> allowed =
                applications
                        ? Set.of("ownerDeptId", "pageNum", "pageSize")
                        : Set.of(
                                "ownerDeptId",
                                "applicationId",
                                "type",
                                "status",
                                "pageNum",
                                "pageSize");
        if (!allowed.containsAll(query.keySet())) invalid();
        String type = query.get("type"), status = query.get("status");
        if (type != null) choice(type, Set.of("BACKGROUND", "LICENSE"));
        if (status != null) choice(status, Set.of("PENDING_REVIEW", "CONFIRMED", "RETURNED"));
        return new Query(
                query.containsKey("ownerDeptId") ? id(query.get("ownerDeptId")) : null,
                query.containsKey("applicationId") ? id(query.get("applicationId")) : null,
                type,
                status,
                page(query.getOrDefault("pageNum", "1"), 100000),
                page(query.getOrDefault("pageSize", "20"), 100));
    }

    public static long id(Object value) {
        return LearningRequests.id(value);
    }

    private static long version(Object value) {
        if (!(value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long)) invalid();
        long result = ((Number) value).longValue();
        if (result < 0 || result > 9007199254740991L) invalid();
        return result;
    }

    private static int page(String value, int max) {
        try {
            if (!value.matches("[1-9][0-9]{0,5}")) invalid();
            int result = Integer.parseInt(value);
            if (result > max) invalid();
            return result;
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String note(Object value) {
        if (!(value instanceof String text) || text.length() > 500) invalid();
        return ((String) value).trim();
    }

    private static String choice(Object value, Set<String> allowed) {
        if (!(value instanceof String text) || !allowed.contains(text)) invalid();
        return (String) value;
    }

    private static void exact(Map<String, Object> body, Set<String> fields) {
        if (body == null || !body.keySet().equals(fields)) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
