package com.xinyi.rongli.training.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.refund.dto.RefundRequests;
import java.util.*;

/** 查询和写入严格白名单；公司、族版本、发布状态和摘要均不可批量绑定实体。 */
public final class TrainingRequests {
    private TrainingRequests() {}

    public record Query(
            int pageNum,
            int pageSize,
            Long ownerDeptId,
            String keyword,
            String status,
            String sort) {}

    public record Create(
            Long ownerDeptId, String title, Long sourceTrainingId, Long expectedSourceVersion) {}

    public record Option(String code, String text) {}

    public record Question(
            String questionCode,
            String type,
            String prompt,
            List<Option> options,
            List<String> correctOptionCodes) {}

    public record Update(
            String title,
            String description,
            Long mediaFileId,
            Integer requiredDurationSeconds,
            Integer passScore,
            Integer attemptTimeLimitSeconds,
            List<Question> questions,
            long expectedVersion,
            String reason) {}

    public static Query query(Map<String, String> input) {
        fields(input, Set.of("pageNum", "pageSize", "ownerDeptId", "keyword", "status", "sort"));
        try {
            int page = Integer.parseInt(input.getOrDefault("pageNum", "1"));
            int size = Integer.parseInt(input.getOrDefault("pageSize", "20"));
            if (page < 1 || page > 1000000 || size < 1 || size > 100) invalid();
            String keyword = input.get("keyword");
            if (keyword != null && keyword.length() > 200) invalid();
            String status = input.get("status"), sort = input.getOrDefault("sort", "idDesc");
            if (status != null && !Set.of("DRAFT", "PUBLISHED", "DISABLED").contains(status))
                invalid();
            if (!Set.of("idDesc", "titleAsc").contains(sort)) invalid();
            return new Query(
                    page,
                    size,
                    input.containsKey("ownerDeptId") ? ViId.parse(input.get("ownerDeptId")) : null,
                    keyword,
                    status,
                    sort);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    public static Create create(Map<String, Object> input) {
        if (input.containsKey("sourceTrainingId")) {
            exact(input, Set.of("sourceTrainingId", "expectedSourceVersion"));
            return new Create(
                    null,
                    null,
                    id(input.get("sourceTrainingId")),
                    version(input.get("expectedSourceVersion")));
        }
        exact(input, Set.of("ownerDeptId", "title"));
        return new Create(id(input.get("ownerDeptId")), text(input.get("title"), 200), null, null);
    }

    public static Update update(Map<String, Object> input) {
        exact(
                input,
                Set.of(
                        "title",
                        "description",
                        "mediaFileId",
                        "requiredDurationSeconds",
                        "passScore",
                        "attemptTimeLimitSeconds",
                        "questions",
                        "expectedVersion",
                        "reason"));
        Map<String, Object> action = new HashMap<>();
        action.put("expectedVersion", input.get("expectedVersion"));
        action.put("reason", input.get("reason"));
        var common = RefundRequests.action(action);
        return new Update(
                text(input.get("title"), 200),
                optionalText(input.get("description"), 5000),
                input.get("mediaFileId") == null ? null : id(input.get("mediaFileId")),
                number(input.get("requiredDurationSeconds"), 1, 86400),
                number(input.get("passScore"), 0, 100),
                number(input.get("attemptTimeLimitSeconds"), 60, 86400),
                questions(input.get("questions")),
                common.expectedVersion(),
                common.reason());
    }

    @SuppressWarnings("unchecked")
    private static List<Question> questions(Object value) {
        if (!(value instanceof List<?> raw) || raw.size() > 200) invalid();
        List<Question> result = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        for (Object item : (List<?>) value) {
            if (!(item instanceof Map<?, ?>)) invalid();
            Map<String, Object> q = (Map<String, Object>) item;
            exact(q, Set.of("questionCode", "type", "prompt", "options", "correctOptionCodes"));
            String code = text(q.get("questionCode"), 64), type = text(q.get("type"), 24);
            if (!code.matches("[A-Za-z0-9_-]{1,64}") || !codes.add(code)) data("题目编码须合法且课程内唯一");
            if (!Set.of("SINGLE", "MULTIPLE", "TRUE_FALSE").contains(type)) data("题目类型无效");
            if (!(q.get("options") instanceof List<?> options)
                    || options.size() < 2
                    || options.size() > 10) data("每题须有2至10个选项");
            List<Option> opts = new ArrayList<>();
            Set<String> optionCodes = new HashSet<>();
            for (Object entry : (List<?>) q.get("options")) {
                if (!(entry instanceof Map<?, ?>)) invalid();
                Map<String, Object> option = (Map<String, Object>) entry;
                exact(option, Set.of("code", "text"));
                String optionCode = text(option.get("code"), 32);
                if (!optionCode.matches("[A-Za-z0-9_-]{1,32}") || !optionCodes.add(optionCode))
                    data("选项编码须合法且题内唯一");
                opts.add(new Option(optionCode, text(option.get("text"), 500)));
            }
            if (!(q.get("correctOptionCodes") instanceof List<?> answers)
                    || answers.isEmpty()
                    || answers.size() > 10) data("请为题目设置有效标准答案");
            List<String> correct = new ArrayList<>();
            for (Object answer : (List<?>) q.get("correctOptionCodes")) {
                String option = text(answer, 32);
                if (!optionCodes.contains(option) || correct.contains(option))
                    data("标准答案必须为不重复的现有选项编码");
                correct.add(option);
            }
            if (!"MULTIPLE".equals(type) && correct.size() != 1) data("单选或判断题必须恰有一个正确选项");
            if ("TRUE_FALSE".equals(type) && opts.size() != 2) data("判断题必须恰有两个选项");
            result.add(
                    new Question(
                            code,
                            type,
                            text(q.get("prompt"), 2000),
                            List.copyOf(opts),
                            List.copyOf(correct)));
        }
        return List.copyOf(result);
    }

    public static long id(Object value) {
        try {
            return ViId.parse(text(value, 19));
        } catch (IllegalArgumentException error) {
            invalid();
            return 0;
        }
    }

    private static long version(Object value) {
        if (!(value instanceof Number) || !value.toString().matches("[0-9]+")) invalid();
        try {
            long n = Long.parseLong(value.toString());
            if (n > 9007199254740991L) invalid();
            return n;
        } catch (NumberFormatException error) {
            invalid();
            return 0;
        }
    }

    private static Integer number(Object value, int min, int max) {
        if (value == null) return null;
        long n = version(value);
        if (n < min || n > max) data("课程数值超出允许范围");
        return (int) n;
    }

    private static String text(Object value, int max) {
        if (!(value instanceof String s) || s.isBlank() || s.trim().length() > max) invalid();
        return ((String) value).trim();
    }

    private static String optionalText(Object value, int max) {
        if (value == null) return null;
        if (!(value instanceof String s) || s.length() > max) invalid();
        return (String) value;
    }

    private static void fields(Map<?, ?> input, Set<String> allowed) {
        if (input == null || !allowed.containsAll(input.keySet())) invalid();
    }

    private static void exact(Map<?, ?> input, Set<String> expected) {
        fields(input, expected);
        if (!input.keySet().equals(expected)) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void data(String message) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, message);
    }
}
