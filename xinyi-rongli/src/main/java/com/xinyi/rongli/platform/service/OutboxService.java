package com.xinyi.rongli.platform.service;

import com.xinyi.rongli.platform.config.ViPlatformProperties;
import com.xinyi.rongli.platform.domain.OutboxStatus;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.mapper.ViOutboxMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * outbox 发布（ADR-05/08）。必须在业务 @Transactional 内调用，与业务写同事务提交：
 * 业务回滚则事件不发出，杜绝“成功事件先于业务落库”。
 * event_key 唯一约束防重（自然源事件）：重复发布同一 event_key 视为已登记，幂等忽略。
 * payload 为最小必要载荷，经 SecretGuard 校验不含明文机密。
 */
@Service
public class OutboxService {

    private final ViOutboxMapper mapper;
    private final SecretGuard secretGuard;
    private final ViPlatformProperties properties;

    public OutboxService(ViOutboxMapper mapper, SecretGuard secretGuard, ViPlatformProperties properties) {
        this.mapper = mapper;
        this.secretGuard = secretGuard;
        this.properties = properties;
    }

    /**
     * 在当前事务登记一条 READY 事件，立即可被中继领取（next_attempt_at=now）。
     *
     * @return true=新登记；false=event_key 已存在（幂等忽略）
     */
    @Transactional
    public boolean publish(String eventKey, String aggregateType, long aggregateId,
                           String eventType, Object payload) {
        if (isBlank(eventKey) || isBlank(aggregateType) || isBlank(eventType)) {
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "outbox 事件键/聚合/类型不可为空");
        }
        String json = writeJson(payload);
        secretGuard.assertNoSecret(json);

        ViOutbox record = new ViOutbox();
        record.setEventKey(eventKey);
        record.setAggregateType(aggregateType);
        record.setAggregateId(aggregateId);
        record.setEventType(eventType);
        record.setPayload(json);
        record.setStatus(OutboxStatus.READY);
        record.setAttempts(0);
        record.setNextAttemptAt(Instant.now());
        record.setLeaseUntil(null);
        try {
            mapper.insert(record);
            return true;
        } catch (DuplicateKeyException dup) {
            // 自然源事件唯一键防重：已登记则幂等忽略，不重复分发。
            return false;
        }
    }

    public int getBatchSize() {
        return properties.getOutboxBatchSize();
    }

    private String writeJson(Object payload) {
        return ViJson.write(payload == null ? java.util.Map.of() : payload);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
