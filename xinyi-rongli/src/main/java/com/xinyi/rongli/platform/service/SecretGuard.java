package com.xinyi.rongli.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * 明文机密落库防线（ADR-01 红线）：token/credential/launchUrl/password/secret/authorization 等
 * 不得写入通用幂等 result_json、审计 before/after_summary 或 outbox payload。
 * 按 JSON 字段名递归检测（只看键名，不看值内容，避免误伤合法值）；命中即拒绝，不静默脱敏后继续，
 * 以便在开发期就暴露“把敏感结果塞进可重放缓存”的错误用法。
 */
@Component
public class SecretGuard {

    private static final Set<String> FORBIDDEN_KEYS = Set.of(
            "token", "accesstoken", "access_token", "refreshtoken", "refresh_token",
            "idtoken", "id_token", "sessiontoken", "session_token",
            "credential", "credentials", "launchurl", "launch_url",
            "password", "passwd", "secret", "authorization", "authtoken", "auth_token");

    /** 校验一段将要持久化的 JSON 文本不含机密字段名；含则抛 VI_DATA_INVALID（不写库）。 */
    public void assertNoSecret(String json) {
        if (json == null || json.isBlank()) {
            return;
        }
        // 非 JSON 文本无法可靠检测字段名，ViJson.tree 会抛 VI_DATA_INVALID 拒绝以免漏网。
        JsonNode root = ViJson.tree(json);
        String hit = findForbiddenKey(root);
        if (hit != null) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID,
                    "禁止把明文机密写入幂等结果/审计/事件载荷，命中字段: " + hit);
        }
    }

    private String findForbiddenKey(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                String normalized = name.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
                for (String forbidden : FORBIDDEN_KEYS) {
                    if (normalized.equals(forbidden.replace("_", ""))) {
                        return name;
                    }
                }
                String nested = findForbiddenKey(node.get(name));
                if (nested != null) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String nested = findForbiddenKey(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }
}
