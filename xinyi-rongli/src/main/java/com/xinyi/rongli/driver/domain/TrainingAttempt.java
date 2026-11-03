package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 本人考试尝试及不可变题卷快照
 *
 * <p>对应表 vi_training_attempt，由数据库结构生成，字段与列一一对应。</p>
 */
public class TrainingAttempt
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 资格申请 */
    private Long applicationId;

    /** 本人 */
    private Long driverId;

    /** 确切课程版本 */
    private Long trainingId;

    /** 本课程尝试序号 */
    private Integer attemptNo;

    /** AttemptStatus: OPEN,SUBMITTED,PASSED,FAILED… */
    private String status;

    /** 确切题干/选项；无答案的对外投影单独构造 */
    private String questionSnapshot;

    /** 本人作答 */
    private String answerSnapshot;

    /** 服务器阅卷0至100 */
    private Integer score;

    /** UTC开始 */
    private LocalDateTime startedAt;

    /** UTC失效 */
    private LocalDateTime expiresAt;

    /** UTC提交 */
    private LocalDateTime submittedAt;

    /** 同申请同课程仅一个开放尝试 */
    private String activeAttempt;

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

    public Integer getAttemptNo()
    {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo)
    {
        this.attemptNo = attemptNo;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getQuestionSnapshot()
    {
        return questionSnapshot;
    }

    public void setQuestionSnapshot(String questionSnapshot)
    {
        this.questionSnapshot = questionSnapshot;
    }

    public String getAnswerSnapshot()
    {
        return answerSnapshot;
    }

    public void setAnswerSnapshot(String answerSnapshot)
    {
        this.answerSnapshot = answerSnapshot;
    }

    public Integer getScore()
    {
        return score;
    }

    public void setScore(Integer score)
    {
        this.score = score;
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

    public LocalDateTime getSubmittedAt()
    {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt)
    {
        this.submittedAt = submittedAt;
    }

    public String getActiveAttempt()
    {
        return activeAttempt;
    }

    public void setActiveAttempt(String activeAttempt)
    {
        this.activeAttempt = activeAttempt;
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
