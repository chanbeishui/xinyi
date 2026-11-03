package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 培训课程版本的后台题库
 *
 * <p>对应表 vi_training_question，由数据库结构生成，字段与列一一对应。</p>
 */
public class TrainingQuestion
{
    /** 主键。数据库生成业务主键；插入不提供id */
    private Long id;

    /** 确切课程版本 */
    private Long trainingId;

    /** 课程内稳定编码 */
    private String questionCode;

    /** QuestionType: SINGLE,MULTIPLE,TRUE_FALSE */
    private String questionType;

    /** 题干 */
    private String prompt;

    /** code和text */
    private String optionsJson;

    /** 仅课程管理/服务端阅卷可见，禁止MINI输出 */
    private String correctOptionCodes;

    /** 排序 */
    private Integer sortNo;

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

    public Long getTrainingId()
    {
        return trainingId;
    }

    public void setTrainingId(Long trainingId)
    {
        this.trainingId = trainingId;
    }

    public String getQuestionCode()
    {
        return questionCode;
    }

    public void setQuestionCode(String questionCode)
    {
        this.questionCode = questionCode;
    }

    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    public String getPrompt()
    {
        return prompt;
    }

    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public String getOptionsJson()
    {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson)
    {
        this.optionsJson = optionsJson;
    }

    public String getCorrectOptionCodes()
    {
        return correctOptionCodes;
    }

    public void setCorrectOptionCodes(String correctOptionCodes)
    {
        this.correctOptionCodes = correctOptionCodes;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
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
