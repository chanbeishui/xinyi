package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingPlaybackSession extends ViRecord {
    private Long runId;

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long value) {
        runId = value;
    }

    private Long progressId;

    public Long getProgressId() {
        return progressId;
    }

    public void setProgressId(Long value) {
        progressId = value;
    }

    private long materialRevision;

    public long getMaterialRevision() {
        return materialRevision;
    }

    public void setMaterialRevision(long value) {
        materialRevision = value;
    }

    private byte[] materialHash;

    public byte[] getMaterialHash() {
        return materialHash;
    }

    public void setMaterialHash(byte[] value) {
        materialHash = value;
    }

    private Long sessionId;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long value) {
        sessionId = value;
    }

    private Long contextVersion;

    public Long getContextVersion() {
        return contextVersion;
    }

    public void setContextVersion(Long value) {
        contextVersion = value;
    }

    private Instant startedAt;

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant value) {
        startedAt = value;
    }

    private Instant expiresAt;

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant value) {
        expiresAt = value;
    }

    private Instant lastReportedAt;

    public Instant getLastReportedAt() {
        return lastReportedAt;
    }

    public void setLastReportedAt(Instant value) {
        lastReportedAt = value;
    }

    private int lastSequence;

    public int getLastSequence() {
        return lastSequence;
    }

    public void setLastSequence(int value) {
        lastSequence = value;
    }

    private Long lastPositionMillis;

    public Long getLastPositionMillis() {
        return lastPositionMillis;
    }

    public void setLastPositionMillis(Long value) {
        lastPositionMillis = value;
    }

    private boolean lastPlaying;

    public boolean getLastPlaying() {
        return lastPlaying;
    }

    public void setLastPlaying(boolean value) {
        lastPlaying = value;
    }
}
