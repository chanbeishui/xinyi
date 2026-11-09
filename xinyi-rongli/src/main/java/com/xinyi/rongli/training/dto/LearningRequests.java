package com.xinyi.rongli.training.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.*;

import java.util.*;

/** 稳定申请/运行目标与CAS分开；不接受客户端公司、核验、有效时长、成绩或答案摘要。 */
public final class LearningRequests {
    private LearningRequests() {}

    public record PolicyUpdate(
            long expectedVersion,
            List<Long> requiredTrainingIds,
            Integer maxAttempts,
            Integer retryIntervalSeconds,
            String reason) {}

    public record PolicyPublish(long expectedVersion, long draftRevisionId, String reason) {}

    public record Start(
            long applicationId,
            long expectedApplicationVersion,
            long expectedCompanyRevision,
            long expectedMaterialRevision,
            long expectedPolicyRevisionId,
            Long planId,
            Long runId,
            int courseVersion,
            long expectedVersion) {}

    public record Progress(
            long applicationId,
            long runId,
            int courseVersion,
            long expectedVersion,
            long playbackSessionId,
            int sequence,
            long positionMillis,
            boolean playing) {}

    public record AttemptCreate(
            long applicationId, long runId, int courseVersion, long expectedVersion) {}

    public record Answer(String questionId, List<String> answerCodes) {}

    public record Submit(
            long applicationId,
            long runId,
            long attemptId,
            long expectedVersion,
            List<Answer> answers) {}

    public static PolicyUpdate policy(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "expectedVersion",
                        "requiredTrainingIds",
                        "maxAttempts",
                        "retryIntervalSeconds",
                        "reason"));
        if (!(body.get("requiredTrainingIds") instanceof List<?> list) || list.size() > 100)
            invalid();
        List<Long> ids = new ArrayList<>();
        for (Object item : (List<?>) body.get("requiredTrainingIds")) ids.add(id(item));
        if (new HashSet<>(ids).size() != ids.size()) data("必修课程不能重复");
        ids.sort(Long::compareTo);
        return new PolicyUpdate(
                version(body.get("expectedVersion")),
                List.copyOf(ids),
                nullableInt(body.get("maxAttempts"), 1, 100),
                nullableInt(body.get("retryIntervalSeconds"), 0, 2592000),
                text(body.get("reason"), 500));
    }

    public static PolicyPublish publish(Map<String, Object> body) {
        exact(body, Set.of("expectedVersion", "draftRevisionId", "reason"));
        return new PolicyPublish(
                version(body.get("expectedVersion")),
                id(body.get("draftRevisionId")),
                text(body.get("reason"), 500));
    }

    public static Start start(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "applicationId",
                        "expectedApplicationVersion",
                        "expectedCompanyRevision",
                        "expectedMaterialRevision",
                        "expectedPolicyRevisionId",
                        "planId",
                        "runId",
                        "courseVersion",
                        "expectedVersion"));
        return new Start(
                id(body.get("applicationId")),
                version(body.get("expectedApplicationVersion")),
                version(body.get("expectedCompanyRevision")),
                version(body.get("expectedMaterialRevision")),
                id(body.get("expectedPolicyRevisionId")),
                nullableId(body.get("planId")),
                nullableId(body.get("runId")),
                positiveInt(body.get("courseVersion")),
                version(body.get("expectedVersion")));
    }

    public static Progress progress(Map<String, Object> body) {
        exact(
                body,
                Set.of(
                        "applicationId",
                        "runId",
                        "courseVersion",
                        "expectedVersion",
                        "playbackSessionId",
                        "sequence",
                        "positionMillis",
                        "playing"));
        if (!(body.get("playing") instanceof Boolean)) invalid();
        long position = version(body.get("positionMillis"));
        if (position > 86400000) data("播放位置超过支持范围");
        return new Progress(
                id(body.get("applicationId")),
                id(body.get("runId")),
                positiveInt(body.get("courseVersion")),
                version(body.get("expectedVersion")),
                id(body.get("playbackSessionId")),
                positiveInt(body.get("sequence")),
                position,
                (Boolean) body.get("playing"));
    }

    public static AttemptCreate attempt(Map<String, Object> body) {
        exact(body, Set.of("applicationId", "runId", "courseVersion", "expectedVersion"));
        return new AttemptCreate(
                id(body.get("applicationId")),
                id(body.get("runId")),
                positiveInt(body.get("courseVersion")),
                version(body.get("expectedVersion")));
    }

    public static Submit submit(Map<String, Object> body) {
        exact(body, Set.of("applicationId", "runId", "attemptId", "expectedVersion", "answers"));
        if (!(body.get("answers") instanceof List<?> list) || list.isEmpty() || list.size() > 200)
            invalid();
        List<Answer> answers = new ArrayList<>();
        Set<String> questions = new HashSet<>();
        for (Object item : (List<?>) body.get("answers")) {
            if (!(item instanceof Map<?, ?>)) invalid();
            @SuppressWarnings("unchecked")
            Map<String, Object> answer = (Map<String, Object>) item;
            exact(answer, Set.of("questionId", "answerCodes"));
            String q = Long.toString(id(answer.get("questionId")));
            if (!questions.add(q)) data("每道题只能作答一次");
            if (!(answer.get("answerCodes") instanceof List<?> codes) || codes.size() > 10)
                invalid();
            List<String> codes = new ArrayList<>();
            for (Object code : (List<?>) answer.get("answerCodes")) codes.add(text(code, 32));
            if (new HashSet<>(codes).size() != codes.size()) data("选项不能重复");
            codes.sort(String::compareTo);
            answers.add(new Answer(q, List.copyOf(codes)));
        }
        answers.sort(Comparator.comparing(Answer::questionId));
        return new Submit(
                id(body.get("applicationId")),
                id(body.get("runId")),
                id(body.get("attemptId")),
                version(body.get("expectedVersion")),
                List.copyOf(answers));
    }

    public static long id(Object value) {
        if (!(value instanceof String text)) invalid();
        try {
            return ViId.parse((String) value);
        } catch (IllegalArgumentException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static Long nullableId(Object value) {
        return value == null ? null : id(value);
    }

    public static long version(Object value) {
        try {
            if (!(value instanceof Number) || !value.toString().matches("[0-9]+")) invalid();
            long result = Long.parseLong(value.toString());
            if (result < 0 || result > 9007199254740991L) invalid();
            return result;
        } catch (NumberFormatException error) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        }
    }

    private static int positiveInt(Object value) {
        long number = version(value);
        if (number < 1 || number > Integer.MAX_VALUE) invalid();
        return (int) number;
    }

    private static Integer nullableInt(Object value, int min, int max) {
        if (value == null) return null;
        long number = version(value);
        if (number < min || number > max) data("政策数值超出允许范围");
        return (int) number;
    }

    private static String text(Object value, int max) {
        if (!(value instanceof String text) || text.isBlank() || text.length() > max) invalid();
        return ((String) value).trim();
    }

    public static void exact(Map<?, ?> body, Set<String> keys) {
        if (body == null || !body.keySet().equals(keys)) invalid();
    }

    public static void noQuery(Map<String, String> query) {
        if (!query.isEmpty()) invalid();
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }

    private static void data(String message) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, message);
    }
}
