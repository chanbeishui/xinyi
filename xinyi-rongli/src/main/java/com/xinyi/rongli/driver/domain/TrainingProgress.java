package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 本人每个课程版本的学习进度
 *
 * <p>对应表 vi_training_progress，由数据库结构生成，字段与列一一对应。</p>
 */
public class TrainingProgress
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 资格申请 */
    private Long applicationId;

    /** 真正司机档案 */
    private Long driverId;

    /** 课程版本主键 */
    private Long trainingId;

    /** 服务端按会话时间限幅的有效学习时长 */
    private Integer verifiedDurationSeconds;

    /** TrainingStatus: NOT_STARTED,IN_PROGRESS… */
    private String status;

    /** 仅恢复位置，不等于有效学习时长 */
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

    public Long getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId(Long applicationId)
    {
        this.applicationId = applicationId;
    }

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public Long getTrainingId()
    {
        return trainingId;
    }

    public void setTrainingId(Long trainingId)
    {
        this.trainingId = trainingId;
    }

    public Integer getVerifiedDurationSeconds()
    {
        return verifiedDurationSeconds;
    }

    public void setVerifiedDurationSeconds(Integer verifiedDurationSeconds)
    {
        this.verifiedDurationSeconds = verifiedDurationSeconds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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
