package com.xinyi.rongli.integration.service;

import com.xinyi.rongli.integration.domain.*;
import com.xinyi.rongli.integration.dto.IntegrationSettings.SecretMutation;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class IntegrationSecrets {
    private final IntegrationMapper mapper;
    private final IntegrationCrypto crypto;

    public IntegrationSecrets(IntegrationMapper mapper, IntegrationCrypto crypto) {
        this.mapper = mapper;
        this.crypto = crypto;
    }

    public static Map<String, String> slots(String kind) {
        return switch (kind) {
            case "PAYMENT_POLICY" -> Map.of("offlineBankAccount", "BANK_ACCOUNT");
            case "WECHAT_PAY" ->
                    Map.of("merchantPrivateKey", "PRIVATE_KEY", "apiV3Key", "API_V3_KEY");
            case "TENCENT_SMS" -> Map.of("secretKey", "CLOUD_SECRET");
            case "ALIYUN_SMS" -> Map.of("accessKeySecret", "CLOUD_SECRET");
            case "WECHAT_APP" -> Map.of("appSecret", "APP_SECRET");
            default -> Map.of();
        };
    }

    /** 把公司、环境、配置及凭据槽绑定到认证上下文，阻止密文跨配置挪用。 */
    public static String aad(IntegrationProfile p, String slot) {
        return "VI_INTEGRATION:"
                + p.getId()
                + ":"
                + p.getOwnerDeptId()
                + ":"
                + p.getEnvironment()
                + ":"
                + p.getKind()
                + ":"
                + slot;
    }

    public Map<String, Long> refs(IntegrationRevision revision) {
        var map = new TreeMap<String, Long>();
        if (revision != null)
            ViJson.tree(revision.getSecretRefsJson())
                    .fields()
                    .forEachRemaining(
                            e -> map.put(e.getKey(), Long.parseLong(e.getValue().asText())));
        return map;
    }

    public boolean usable(IntegrationSecret s) {
        return s != null
                && "ACTIVE".equals(s.getLifecycle())
                && (s.getExpiresAt() == null || s.getExpiresAt().isAfter(Instant.now()));
    }

    /** 依据已保存的修订引用读取秘密，同时验证有效性；调用方负责及时清理明文字节。 */
    public byte[] read(IntegrationProfile p, IntegrationRevision r, String slot) {
        Long id = refs(r).get(slot);
        var s = id == null ? null : mapper.secret(p.getId(), id, slot);
        if (!usable(s)
                || !Objects.equals(slots(p.getKind()).get(slot), s.getCredentialKind())
                || !aad(p, slot).equals(s.getAadContext()))
            throw new ViException(ViErrorCode.VI_SECRET_UNAVAILABLE);
        return crypto.open(s.getCiphertext(), s.getNonce(), s.getKeyVersion(), s.getAadContext());
    }

    /** KEEP 继承引用、REPLACE 创建新密文、CLEAR 清除当前引用；历史发布需要的凭据保留。 */
    public Map<String, Long> mutate(
            IntegrationProfile p,
            IntegrationRevision previous,
            Map<String, SecretMutation> mutations,
            long userId) {
        Map<String, Long> result = refs(previous);
        for (var entry : mutations.entrySet()) {
            String slot = entry.getKey();
            var mutation = entry.getValue();
            if (!slots(p.getKind()).containsKey(slot))
                throw new ViException(ViErrorCode.VI_DATA_INVALID);
            if ("KEEP".equals(mutation.action())) continue;
            if ("CLEAR".equals(mutation.action())) {
                Long old = result.get(slot);
                if (old == null) continue;
                // 已发布引用保留供历史操作与审计；CLEAR只清除未曾发布的草稿秘密。
                for (var revision : mapper.publishedRevisions(p.getId()))
                    if (Objects.equals(refs(revision).get(slot), old))
                        throw new ViException(
                                ViErrorCode.VI_STATE_CONFLICT, "凭据曾发布，须保留历史维护引用；请使用替换轮换");
                mapper.clearSecret(p.getId(), old, userId);
                result.remove(slot);
                continue;
            }
            String value = mutation.value();
            if (value == null
                    || value.isBlank()
                    || value.matches("[*•]+")
                    || value.contains("****")
                    || value.contains("••••")
                    || value.equalsIgnoreCase("REDACTED"))
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "替换凭据不能使用空值或掩码");
            var encrypted = crypto.seal(value, aad(p, slot));
            var secret = new IntegrationSecret();
            secret.setProfileId(p.getId());
            secret.setSlotName(slot);
            secret.setCredentialKind(slots(p.getKind()).get(slot));
            secret.setCiphertext(encrypted.ciphertext);
            secret.setNonce(encrypted.nonce);
            secret.setKeyVersion(encrypted.keyVersion);
            secret.setAadContext(aad(p, slot));
            secret.setFingerprint(encrypted.fingerprint);
            secret.setCreatedBy(userId);
            mapper.insertSecret(secret);
            result.put(slot, secret.getId());
        }
        return result;
    }

    /** 管理端仅返回配置状态和指纹等元数据，禁止在查询响应中回显秘密明文。 */
    public Map<String, Object> states(IntegrationProfile p, IntegrationRevision r) {
        var states = new TreeMap<String, Object>();
        var refs = refs(r);
        for (String slot : slots(p.getKind()).keySet()) {
            Long id = refs.get(slot);
            var secret = id == null ? null : mapper.secret(p.getId(), id, slot);
            var state = new LinkedHashMap<String, Object>();
            state.put("configured", usable(secret));
            if (secret != null) {
                state.put("fingerprint", secret.getFingerprint());
                state.put("updatedAt", secret.getUpdatedAt().toString());
                state.put(
                        "expiresAt",
                        secret.getExpiresAt() == null ? null : secret.getExpiresAt().toString());
            }
            states.put(slot, state);
        }
        return states;
    }
}
