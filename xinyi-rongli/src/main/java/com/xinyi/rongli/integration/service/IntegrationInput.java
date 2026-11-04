package com.xinyi.rongli.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.integration.dto.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class IntegrationInput {
    private final JsonNode schemas;

    public IntegrationInput() {
        try (var in = getClass().getResourceAsStream("/integration/input-schemas.json")) {
            if (in == null) throw new IllegalStateException("配置输入契约缺失");
            schemas =
                    ViJson.tree(
                            new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("配置输入契约不可读");
        }
    }

    /** 按内置契约拒绝非法输入，避免把未知字段批量绑定到配置或权限实体。 */
    public JsonNode validate(String schema, Object value) {
        if (!schemas.has(schema)) throw new IllegalStateException("配置输入契约不存在");
        JsonNode data = ViJson.tree(ViJson.write(value));
        if (!valid(schemas.path(schema), data))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "配置请求含未知字段、错误类型或不符合字段范围的值");
        return data;
    }

    private boolean valid(JsonNode schema, JsonNode value) {
        if (schema.has("$ref"))
            return valid(
                    schemas.path(schema.path("$ref").asText().replace("#/components/schemas/", "")),
                    value);
        if (schema.has("oneOf")) {
            int matches = 0;
            for (var branch : schema.get("oneOf")) if (valid(branch, value)) matches++;
            if (matches != 1) return false;
        }
        if (schema.has("not") && valid(schema.get("not"), value)) return false;
        if (schema.has("enum")) {
            boolean found = false;
            for (var item : schema.get("enum")) if (item.equals(value)) found = true;
            if (!found) return false;
        }
        String type = schema.path("type").asText();
        if (!type.isEmpty()
                && !switch (type) {
                    case "object" -> value.isObject();
                    case "array" -> value.isArray();
                    case "string" -> value.isTextual();
                    case "integer" -> value.isIntegralNumber() && value.canConvertToLong();
                    case "boolean" -> value.isBoolean();
                    default -> false;
                }) return false;
        if (value.isObject()) {
            for (var key : schema.path("required")) if (!value.has(key.asText())) return false;
            var properties = schema.path("properties");
            var names = value.fieldNames();
            while (names.hasNext()) {
                String key = names.next();
                if (properties.has(key)) {
                    if (!valid(properties.get(key), value.get(key))) return false;
                } else if (schema.has("additionalProperties")
                        && !schema.get("additionalProperties").asBoolean()) return false;
            }
        }
        if (value.isTextual()) {
            String s = value.asText();
            int n = s.codePointCount(0, s.length());
            if (n < schema.path("minLength").asInt(0) || n > schema.path("maxLength").asInt(65536))
                return false;
            if (schema.has("pattern") && !s.matches(schema.get("pattern").asText())) return false;
        }
        if (value.isIntegralNumber()
                && (value.asLong() < schema.path("minimum").asLong(Long.MIN_VALUE)
                        || value.asLong() > schema.path("maximum").asLong(Long.MAX_VALUE)))
            return false;
        if (value.isArray()) {
            if (value.size() > schema.path("maxItems").asInt(100)) return false;
            Set<JsonNode> seen = new HashSet<>();
            for (var item : value) {
                if (schema.has("items") && !valid(schema.get("items"), item)) return false;
                if (schema.path("uniqueItems").asBoolean() && !seen.add(item)) return false;
            }
        }
        return true;
    }

    public IntegrationRequests.Draft draft(Object body) {
        var n = validate("IntegrationDraftRequest", body);
        String kind = n.path("kind").asText();
        Class<? extends IntegrationSettings.Config> type =
                switch (kind) {
                    case "PAYMENT_POLICY" -> IntegrationSettings.PaymentPolicy.class;
                    case "WECHAT_PAY" -> IntegrationSettings.WechatPay.class;
                    case "TENCENT_SMS" -> IntegrationSettings.TencentSms.class;
                    case "ALIYUN_SMS" -> IntegrationSettings.AliyunSms.class;
                    case "WECHAT_APP" -> IntegrationSettings.WechatApp.class;
                    default -> throw new ViException(ViErrorCode.VI_DATA_INVALID);
                };
        var mutations = new LinkedHashMap<String, IntegrationSettings.SecretMutation>();
        n.path("credentials")
                .fields()
                .forEachRemaining(
                        e ->
                                mutations.put(
                                        e.getKey(),
                                        ViJson.read(
                                                e.getValue().toString(),
                                                IntegrationSettings.SecretMutation.class)));
        return new IntegrationRequests.Draft(
                kind,
                n.path("expectedVersion").asLong(),
                ViJson.read(n.get("config").toString(), type),
                mutations,
                n.path("reason").asText(),
                n.get("config"));
    }

    /** 对象字段按键排序生成稳定内容摘要，字段排列差异不应产生不同修订。 */
    public static String canonical(JsonNode value) {
        if (value.isObject()) {
            var sorted = new TreeMap<String, Object>();
            value.fields()
                    .forEachRemaining(
                            e -> sorted.put(e.getKey(), ViJson.tree(canonical(e.getValue()))));
            return ViJson.write(sorted);
        }
        if (value.isArray()) {
            var rows = new ArrayList<JsonNode>();
            for (var item : value) rows.add(ViJson.tree(canonical(item)));
            return ViJson.write(rows);
        }
        return value.toString();
    }
}
