package com.xinyi.rongli.platform.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_outbox 可靠异步事务消息。唯一键 uk_event(event_key) 防重（自然源事件）。
 * 与业务写同事务插入（status=READY）；中继按 DB 到期扫描/领取/租约恢复，Redis 过期通知不是唯一时钟（ADR-08）。
 * payload 为最小必要载荷，禁止塞敏感原文。
 */
public class ViOutbox extends ViRecord {
    private String eventKey;
    private String aggregateType;
    private Long aggregateId;
    private String eventType;
    private String payload;
    private OutboxStatus status;
    private int attempts;
    private Instant nextAttemptAt;
    private Instant leaseUntil;

    public String getEventKey() { return eventKey; }
    public void setEventKey(String v) { this.eventKey = v; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String v) { this.aggregateType = v; }
    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long v) { this.aggregateId = v; }
    public String getEventType() { return eventType; }
    public void setEventType(String v) { this.eventType = v; }
    public String getPayload() { return payload; }
    public void setPayload(String v) { this.payload = v; }
    public OutboxStatus getStatus() { return status; }
    public void setStatus(OutboxStatus v) { this.status = v; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int v) { this.attempts = v; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Instant v) { this.nextAttemptAt = v; }
    public Instant getLeaseUntil() { return leaseUntil; }
    public void setLeaseUntil(Instant v) { this.leaseUntil = v; }
}
