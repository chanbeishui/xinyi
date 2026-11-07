package com.xinyi.rongli.qualification.dto;

import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 只接收本人资料白名单；材料摘要、资格、公司和渠道结论始终由服务端生成。 */
public final class QualificationRequests {
    public static final Set<String> MATERIAL_FIELDS =
            Set.of(
                    "realName",
                    "idCardNo",
                    "idCardFrontFileId",
                    "idCardBackFileId",
                    "licenseMainFileId",
                    "licenseSubFileId",
                    "licenseClass",
                    "firstIssueDate",
                    "validFrom",
                    "validTo",
                    "longTermValid");
    public static final Set<String> FILE_FIELDS =
            Set.of(
                    "idCardFrontFileId",
                    "idCardBackFileId",
                    "licenseMainFileId",
                    "licenseSubFileId");
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private QualificationRequests() {}

    public record Create(String consentVersion, String consentTextHash, boolean acceptedConsent) {}

    public record Draft(long applicationId, long expectedVersion, Map<String, Object> values) {}

    public record Submit(
            long applicationId, long expectedVersion, long expectedProfileVersion, String reason) {}

    public static Create create(Map<String, Object> body) {
        fields(body, Set.of("consentVersion", "consentTextHash", "acceptedConsent"));
        if (body.size() != 3 || !Boolean.TRUE.equals(body.get("acceptedConsent"))) invalid();
        return new Create(
                text(body.get("consentVersion"), 1, 64), hash(body.get("consentTextHash")), true);
    }

    public static Draft draft(Map<String, Object> body) {
        Set<String> allowed = new java.util.HashSet<>(MATERIAL_FIELDS);
        allowed.addAll(
                Set.of(
                        "applicationId",
                        "expectedVersion",
                        "consentVersion",
                        "consentTextHash",
                        "acceptedConsent"));
        fields(body, allowed);
        long version = version(body.get("expectedVersion"));
        Map<String, Object> values = new LinkedHashMap<>(body);
        values.remove("expectedVersion");
        values.remove("applicationId");
        if (values.isEmpty()) invalid();
        boolean consent = values.containsKey("consentVersion");
        if (consent != values.containsKey("acceptedConsent")
                || consent != values.containsKey("consentTextHash")
                || consent && !Boolean.TRUE.equals(values.get("acceptedConsent"))) invalid();
        if (consent) {
            values.put("consentVersion", text(values.get("consentVersion"), 1, 64));
            values.put("consentTextHash", hash(values.get("consentTextHash")));
        }
        for (String field : MATERIAL_FIELDS) {
            if (!values.containsKey(field)) continue;
            Object value = values.get(field);
            if ("longTermValid".equals(field)) {
                if (!(value instanceof Boolean)) invalid();
            } else if (FILE_FIELDS.contains(field)) {
                if (value != null) {
                    try {
                        values.put(field, Long.toString(ViId.parse(text(value, 1, 19))));
                    } catch (IllegalArgumentException error) {
                        invalid();
                    }
                }
            } else if (value != null) {
                int max = "realName".equals(field) ? 64 : 32;
                String text = text(value, 1, max);
                if (Set.of("firstIssueDate", "validFrom", "validTo").contains(field)) {
                    try {
                        text = LocalDate.parse(text, DATE).toString();
                    } catch (RuntimeException error) {
                        data("请填写有效日期，格式为yyyy-MM-dd");
                    }
                }
                if ("idCardNo".equals(field)) text = idCard(text);
                values.put(field, text);
            }
        }
        return new Draft(id(body.get("applicationId")), version, values);
    }

    public static Submit submit(Map<String, Object> body) {
        fields(
                body,
                Set.of("applicationId", "expectedVersion", "expectedProfileVersion", "reason"));
        if (body.size() != 4) invalid();
        return new Submit(
                id(body.get("applicationId")),
                version(body.get("expectedVersion")),
                version(body.get("expectedProfileVersion")),
                text(body.get("reason"), 1, 500));
    }

    private static String idCard(String value) {
        String normalized = value.toUpperCase(java.util.Locale.ROOT);
        if (!normalized.matches("[0-9]{17}[0-9X]")) data("身份证号码格式不正确，请重新填写完整号码");
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        int sum = 0;
        for (int index = 0; index < weights.length; index++) {
            sum += (normalized.charAt(index) - '0') * weights[index];
        }
        if ("10X98765432".charAt(sum % 11) != normalized.charAt(17)) data("身份证号码校验不通过");
        try {
            LocalDate.parse(normalized.substring(6, 14), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (RuntimeException error) {
            data("身份证出生日期不正确");
        }
        return normalized;
    }

    private static void fields(Map<String, Object> body, Set<String> allowed) {
        if (body == null || !allowed.containsAll(body.keySet())) invalid();
    }

    private static String text(Object value, int minimum, int maximum) {
        if (!(value instanceof String text)
                || text.isBlank()
                || text.trim().length() < minimum
                || text.trim().length() > maximum) invalid();
        return ((String) value).trim();
    }

    private static String hash(Object value) {
        String hash = text(value, 64, 64);
        if (!hash.matches("[a-f0-9]{64}")) invalid();
        return hash;
    }

    private static long id(Object value) {
        try {
            return ViId.parse(text(value, 1, 19));
        } catch (IllegalArgumentException error) {
            invalid();
            return 0;
        }
    }

    private static long version(Object value) {
        if (!(value instanceof Number) || !value.toString().matches("[0-9]+")) invalid();
        try {
            long result = Long.parseLong(value.toString());
            if (result > 9007199254740991L) invalid();
            return result;
        } catch (NumberFormatException error) {
            invalid();
            return 0;
        }
    }

    private static void data(String message) {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, message);
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
    }
}
