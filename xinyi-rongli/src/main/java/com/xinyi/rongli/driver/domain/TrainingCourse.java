package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 代驾培训不可变课程版本
 *
 * <p>对应表 vi_training_course，由数据库结构生成，字段与列一一对应。</p>
 */
public class TrainingCourse
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 稳定课程编码 */
    private String courseKey;

    /** 课程内容版本 */
    private Integer courseVersion;

    /** 课程标题 */
    private String title;

    /** 培训说明 */
    private String description;

    /** 私有培训媒体 */
    private Long mediaFileId;

    /** 规定学习时长；发布时校验>0 */
    private Integer requiredDurationSeconds;

    /** 0至100的配置门槛 */
    private Integer passScore;

    /** 考试时间配置 */
    private Integer attemptTimeLimitSeconds;

    /** TrainingCourseStatus: DRAFT,PUBLISHED,DISABLED */
    private String status;

    /** UTC发布时间 */
    private LocalDateTime publishedAt;

    /** 后台发布人 */
    private Long publishedByUserId;

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

    public String getCourseKey()
    {
        return courseKey;
    }

    public void setCourseKey(String courseKey)
    {
        this.courseKey = courseKey;
    }

    public Integer getCourseVersion()
    {
        return courseVersion;
    }

    public void setCourseVersion(Integer courseVersion)
    {
        this.courseVersion = courseVersion;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getMediaFileId()
    {
        return mediaFileId;
    }

    public void setMediaFileId(Long mediaFileId)
    {
        this.mediaFileId = mediaFileId;
    }

    public Integer getRequiredDurationSeconds()
    {
        return requiredDurationSeconds;
    }

    public void setRequiredDurationSeconds(Integer requiredDurationSeconds)
    {
        this.requiredDurationSeconds = requiredDurationSeconds;
    }

    public Integer getPassScore()
    {
        return passScore;
    }

    public void setPassScore(Integer passScore)
    {
        this.passScore = passScore;
    }

    public Integer getAttemptTimeLimitSeconds()
    {
        return attemptTimeLimitSeconds;
    }

    public void setAttemptTimeLimitSeconds(Integer attemptTimeLimitSeconds)
    {
        this.attemptTimeLimitSeconds = attemptTimeLimitSeconds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getPublishedAt()
    {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt)
    {
        this.publishedAt = publishedAt;
    }

    public Long getPublishedByUserId()
    {
        return publishedByUserId;
    }

    public void setPublishedByUserId(Long publishedByUserId)
    {
        this.publishedByUserId = publishedByUserId;
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
