package com.xinyi.rongli.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * vi 平台基座可调参数。集中读取，避免散落 @Value。
 * lookup-key 优先取 vi.lookup-key，回退环境变量 VI_LOOKUP_KEY，再回退空（空则幂等 MAC 拒绝计算，
 * 不允许用空密钥静默生成摘要）。无需修改框架 application.yml 即可经环境变量注入。
 */
@Component
public class ViPlatformProperties {

    private final String lookupKey;
    private final int idempotencyTtlHours;
    private final int outboxBatchSize;
    private final int outboxLeaseSeconds;
    private final int outboxMaxAttempts;
    private final boolean outboxRelayEnabled;

    public ViPlatformProperties(
            @Value("${vi.lookup-key:${VI_LOOKUP_KEY:}}") String lookupKey,
            @Value("${vi.idempotency-ttl-hours:24}") int idempotencyTtlHours,
            @Value("${vi.outbox-batch-size:100}") int outboxBatchSize,
            @Value("${vi.outbox-lease-seconds:60}") int outboxLeaseSeconds,
            @Value("${vi.outbox-max-attempts:8}") int outboxMaxAttempts,
            @Value("${vi.outbox-relay-enabled:true}") boolean outboxRelayEnabled) {
        this.lookupKey = lookupKey;
        // ADR-01：请求级幂等保存时限至少 24h，配置不得低于该下限。
        this.idempotencyTtlHours = Math.max(24, idempotencyTtlHours);
        this.outboxBatchSize = outboxBatchSize > 0 ? outboxBatchSize : 100;
        this.outboxLeaseSeconds = outboxLeaseSeconds > 0 ? outboxLeaseSeconds : 60;
        this.outboxMaxAttempts = outboxMaxAttempts > 0 ? outboxMaxAttempts : 8;
        this.outboxRelayEnabled = outboxRelayEnabled;
    }

    public String getLookupKey() { return lookupKey; }
    public boolean hasLookupKey() { return lookupKey != null && !lookupKey.isBlank(); }
    public int getIdempotencyTtlHours() { return idempotencyTtlHours; }
    public int getOutboxBatchSize() { return outboxBatchSize; }
    public int getOutboxLeaseSeconds() { return outboxLeaseSeconds; }
    public int getOutboxMaxAttempts() { return outboxMaxAttempts; }
    public boolean isOutboxRelayEnabled() { return outboxRelayEnabled; }
}
