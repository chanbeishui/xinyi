package com.xinyi.rongli.qualification.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;

import java.time.Instant;
import java.util.*;

/** 明确九键审核意图；版本分别绑定申请、档案、提交快照及专用公司epoch，禁止隐含目标漂移。 */
public final class QualificationAdminRequests {
    private QualificationAdminRequests() {}

    public record Query(
            int pageNum,
            int pageSize,
            Long ownerDeptId,
            String status,
            String keyword,
            String sort) {}

    public record Audit(
            long expectedVersion,
            long expectedProfileVersion,
            long expectedSubmissionRevision,
            long expectedCompanyRevision,
            String decision,
            String opinion,
            String publicOpinion,
            List<String> rejectFields,
            Instant reviewDueAt) {}

    public static Query query(Map<String, String> input) {
        if (!Set.of("pageNum", "pageSize", "ownerDeptId", "status", "keyword", "sort")
                .containsAll(input.keySet())) invalid();
        String status = nullable(input.get("status"));
        if (status != null
                && !Set.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "EXPIRED", "SUSPENDED")
                        .contains(status)) invalid();
        String keyword = nullable(input.get("keyword"));
        if (keyword != null && keyword.length() > 100) invalid();
        String sort = input.getOrDefault("sort", "submittedAtDesc");
        if (!Set.of("submittedAtDesc", "idDesc").contains(sort)) invalid();
        Long owner = input.containsKey("ownerDeptId") ? id(input.get("ownerDeptId")) : null;
        return new Query(
                page(input.getOrDefault("pageNum", "1"), 100000),
                page(input.getOrDefault("pageSize", "10"), 100),
                owner,
                status,
                keyword,
                sort);
    }

    public static Audit audit(Map<String, Object> body) {
        Set<String> keys =
                Set.of(
                        "expectedVersion",
                        "expectedProfileVersion",
                        "expectedSubmissionRevision",
                        "expectedCompanyRevision",
                        "decision",
                        "opinion",
                        "publicOpinion",
                        "rejectFields",
                        "reviewDueAt");
        if (body == null || !body.keySet().equals(keys)) invalid();
        String decision = text(body.get("decision"), 32, false);
        if (!Set.of("APPROVE", "REJECT", "REPAIR_REJECTION").contains(decision)) invalid();
        String opinion = text(body.get("opinion"), 1000, false);
        String publicOpinion =
                body.get("publicOpinion") == null
                        ? null
                        : text(body.get("publicOpinion"), 1000, true);
        if (publicOpinion != null && publicOpinion.isBlank()) publicOpinion = null;
        if (!(body.get("rejectFields") instanceof List<?>)) invalid();
        List<?> fields = (List<?>) body.get("rejectFields");
        if (!fields.stream()
                .allMatch(
                        value ->
                                value instanceof String
                                        && QualificationRequests.MATERIAL_FIELDS.contains(value)))
            invalid();
        List<String> rejected = fields.stream().map(String::valueOf).sorted().toList();
        if (new HashSet<>(rejected).size() != rejected.size()) invalid();
        Instant due = null;
        if (body.get("reviewDueAt") != null) {
            String value = text(body.get("reviewDueAt"), 64, false);
            // DATETIME(3)只能保存四位年份及毫秒精度；拒绝而非舍入到另一时刻或数据库越界。
            if (!value.matches(
                    "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,3})?Z"))
                invalid();
            try {
                due = Instant.parse(value);
                if (due.isAfter(Instant.parse("9999-12-31T23:59:59.999Z"))) invalid();
            } catch (RuntimeException failure) {
                invalid();
            }
        }
        if ("APPROVE".equals(decision)) {
            if (!rejected.isEmpty() || due == null) data("批准必须指定复审截止且不携带补正字段");
        } else if (rejected.isEmpty() || publicOpinion == null || due != null) {
            data("驳回或历史补正修复必须指定公开意见和补正字段，不能指定新资格期限");
        }
        return new Audit(
                version(body.get("expectedVersion")),
                version(body.get("expectedProfileVersion")),
                version(body.get("expectedSubmissionRevision")),
                version(body.get("expectedCompanyRevision")),
                decision,
                opinion,
                publicOpinion,
                rejected,
                due);
    }

    public static long id(String value) {
        try {
            return ViId.parse(value);
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static long version(Object value) {
        if (!(value instanceof Integer || value instanceof Long)) invalid();
        long number = ((Number) value).longValue();
        if (number < 0 || number > 9007199254740991L) invalid();
        return number;
    }

    private static String text(Object value, int max, boolean empty) {
        if (!(value instanceof String)) invalid();
        String text = ((String) value).trim();
        if (text.length() > max || (!empty && text.isEmpty())) invalid();
        return text;
    }

    private static int page(String value, int max) {
        try {
            if (!value.matches("[1-9][0-9]{0,5}")) invalid();
            int number = Integer.parseInt(value);
            if (number > max) invalid();
            return number;
        } catch (RuntimeException failure) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void data(String message) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, message);
    }
}
