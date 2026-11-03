package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 短期受控学习会话
 *
 * <p>对应表 vi_training_playback，由数据库结构生成，字段与列一一对应。</p>
 */
public class TrainingPlayback
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 本人进度 */
    private Long progressId;

    /** 服务端签发的学习会话串 */
    private String sessionId;

    /** 服务端开始时间 */
    private LocalDateTime startedAt;

    /** 服务端结束边界 */
    private LocalDateTime expiresAt;

    /** 最近服务器接收时刻 */
    private LocalDateTime lastReportedAt;

    /** 乱序和重复不增加时长 */
    private Integer lastSequence;

    /** 最近一次上报的播放位置，用于位置单调与跳变校验 */
    private Integer lastPositionSeconds;

    /** 创建时刻UTC */
    private LocalDateTime createdAt;

    /** 更新时间UTC；更新必须显式设置UTC_TIMESTAMP(3) */
    private LocalDateTime updatedAt;

    /** 手写CAS版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getProgressId()
    {
        return progressId;
    }

    public void setProgressId(Long progressId)
    {
        this.progressId = progressId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public LocalDateTime getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt)
    {
        this.startedAt = startedAt;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastReportedAt()
    {
        return lastReportedAt;
    }

    public void setLastReportedAt(LocalDateTime lastReportedAt)
    {
        this.lastReportedAt = lastReportedAt;
    }

    public Integer getLastSequence()
    {
        return lastSequence;
    }

    public void setLastSequence(Integer lastSequence)
    {
        this.lastSequence = lastSequence;
    }

    public Integer getLastPositionSeconds()
    {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds)
    {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
}
