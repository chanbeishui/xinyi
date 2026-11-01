package com.xinyi.rongli.platform.service;

import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.config.ViPlatformProperties;
import com.xinyi.rongli.platform.domain.IdempotencyStatus;
import com.xinyi.rongli.platform.domain.ViIdempotency;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.mapper.ViIdempotencyMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * 请求级幂等（ADR-05/06）。必须在业务 @Transactional 方法内调用，使
 * “幂等占位 + 业务写 + 审计 + outbox” 同事务提交/回滚——业务失败则占位一并回滚，不留成功痕迹。
 *
 * 边界：
 * - 重复键不同体 → 409 VI_IDEMPOTENCY_CONFLICT（request_mac 常量时间比较）。
 * - 重复键同体且已 DONE → 重放缓存结果，不重复执行业务。
 * - 含明文令牌的登录/刷新类动作 cacheResult=false，结果不落 result_json（SecretGuard 兜底），
 *   重复请求按已处理拒绝，不重放敏感体。
 */
@Service
public class IdempotencyService {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final int MAC_BYTES = 32;

    private final ViIdempotencyMapper mapper;
    private final SecretGuard secretGuard;
    private final ViPlatformProperties properties;

    public IdempotencyService(ViIdempotencyMapper mapper, SecretGuard secretGuard,
                              ViPlatformProperties properties) {
        this.mapper = mapper;
        this.secretGuard = secretGuard;
        this.properties = properties;
    }

    /** 统一主体命名空间：realm + 内部 ID；SYSTEM/CALLBACK 无 ID 时用固定占位。 */
    public static String subjectKey(Actor actor) {
        return switch (actor.realm()) {
            case MINI -> "MINI:" + actor.accountId();
            case ADMIN -> "ADMIN:" + actor.sysUserId();
            case SYSTEM -> "SYSTEM";
            case CALLBACK -> "CALLBACK";
        };
    }

    /** HTTP 动作 + 资源，例如 "POST /api/owner/orders"。 */
    public static String actionKey(String httpMethod, String resourcePath) {
        return (httpMethod == null ? "GET" : httpMethod.toUpperCase()) + " " + resourcePath;
    }

    /** 请求体摘要：HMAC-SHA256(lookupKey, body)，32 字节，匹配 BINARY(32)。 */
    public byte[] mac(String body) {
        if (!properties.hasLookupKey()) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED,
                    "幂等摘要密钥未配置（vi.lookup-key / VI_LOOKUP_KEY），拒绝以空密钥生成摘要");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(properties.getLookupKey().getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] out = mac.doFinal((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
            if (out.length != MAC_BYTES) {
                throw new IllegalStateException("HMAC 输出长度异常: " + out.length);
            }
            return out;
        } catch (ViException e) {
            throw e;
        } catch (Exception e) {
            throw new ViException(ViErrorCode.VI_INTERNAL_ERROR, "幂等摘要计算失败", e);
        }
    }

    /**
     * 在调用方事务内执行幂等包裹的业务。
     *
     * @param cacheResult true=可缓存并重放成功结果；false=含明文机密（登录/刷新），只去重不缓存体
     */
    @Transactional
    public <T> T execute(String subjectKey, String actionKey, String requestKey, String requestBody,
                         boolean cacheResult, Class<T> resultType, Supplier<T> business) {
        if (isBlank(subjectKey) || isBlank(actionKey) || isBlank(requestKey)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "缺少幂等键（Idempotency-Key）");
        }
        byte[] mac = mac(requestBody);

        ViIdempotency record = new ViIdempotency();
        record.setSubjectKey(subjectKey);
        record.setActionKey(actionKey);
        record.setRequestKey(requestKey);
        record.setRequestMac(mac);
        record.setStatus(IdempotencyStatus.PROCESSING);
        record.setExpiresAt(Instant.now().plus(properties.getIdempotencyTtlHours(), ChronoUnit.HOURS));

        try {
            mapper.insertProcessing(record);
        } catch (DuplicateKeyException dup) {
            return replayOrConflict(subjectKey, actionKey, requestKey, mac, cacheResult, resultType);
        }

        T result = business.get();

        if (cacheResult) {
            String json = writeJson(result);
            secretGuard.assertNoSecret(json);
            int affected = mapper.markDone(record.getId(), json, record.getVersion());
            requireOne(affected);
        } else {
            int affected = mapper.markDoneNoResult(record.getId(), record.getVersion());
            requireOne(affected);
        }
        return result;
    }

    /** 外部只读调用前检查已完成请求；无记录不占位，最终写入仍须execute同事务去重。 */
    public <T> java.util.Optional<T> replayIfPresent(String subject,String action,String key,String body,Class<T> type) {
        if(isBlank(subject)||isBlank(action)||isBlank(key))throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        if(mapper.selectByKey(subject,action,key)==null)return java.util.Optional.empty();
        return java.util.Optional.of(replayOrConflict(subject,action,key,mac(body),true,type));
    }

    private <T> T replayOrConflict(String subjectKey, String actionKey, String requestKey,
                                   byte[] mac, boolean cacheResult, Class<T> resultType) {
        ViIdempotency existing = mapper.selectByKey(subjectKey, actionKey, requestKey);
        if (existing == null) {
            // 并发下唯一键冲突但当前可见性读不到（极少见）：按在途冲突，可重试。
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "相同请求正在处理中，请稍后重试");
        }
        if (!MessageDigest.isEqual(nullToEmpty(existing.getRequestMac()), mac)) {
            throw new ViException(ViErrorCode.VI_IDEMPOTENCY_CONFLICT);
        }
        if (existing.getStatus() == IdempotencyStatus.DONE) {
            if (!cacheResult) {
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "该请求已处理，敏感结果不可重放");
            }
            return readJson(existing.getResultJson(), resultType);
        }
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "相同请求正在处理中，请稍后重试");
    }

    private void requireOne(int affected) {
        if (affected != 1) {
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "幂等状态推进失败（受影响行数=" + affected + "）");
        }
    }

    private String writeJson(Object value) {
        return ViJson.write(value);
    }

    private <T> T readJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "幂等结果缺失，无法重放");
        }
        return ViJson.read(json, type);
    }

    private static byte[] nullToEmpty(byte[] value) {
        return value == null ? new byte[0] : value;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
