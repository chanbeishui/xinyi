package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

/** 公司培训课程的确切内容版本；发布后不修改材料和题库，行CAS另行维护。 */
public class TrainingCourse extends ViRecord {
    private Long ownerDeptId;
    private String companyName;
    private String courseKey;
    private int courseVersion;
    private Long sourceTrainingId;
    private Long inheritedMediaFileId;
    private String title;
    private String description;
    private Long mediaFileId;
    private Integer requiredDurationSeconds;
    private Integer passScore;
    private Integer attemptTimeLimitSeconds;
    private String status;
    private java.time.Instant publishedAt;
    private Long publishedByUserId;
    private Long createdByUserId;
    private Long updatedByUserId;
    private byte[] contentHash;

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String value) {
        companyName = value;
    }

    public String getCourseKey() {
        return courseKey;
    }

    public void setCourseKey(String value) {
        courseKey = value;
    }

    public int getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(int value) {
        courseVersion = value;
    }

    public Long getSourceTrainingId() {
        return sourceTrainingId;
    }

    public void setSourceTrainingId(Long value) {
        sourceTrainingId = value;
    }

    public Long getInheritedMediaFileId() {
        return inheritedMediaFileId;
    }

    public void setInheritedMediaFileId(Long value) {
        inheritedMediaFileId = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    public Long getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(Long value) {
        mediaFileId = value;
    }

    public Integer getRequiredDurationSeconds() {
        return requiredDurationSeconds;
    }

    public void setRequiredDurationSeconds(Integer value) {
        requiredDurationSeconds = value;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer value) {
        passScore = value;
    }

    public Integer getAttemptTimeLimitSeconds() {
        return attemptTimeLimitSeconds;
    }

    public void setAttemptTimeLimitSeconds(Integer value) {
        attemptTimeLimitSeconds = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public java.time.Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(java.time.Instant value) {
        publishedAt = value;
    }

    public Long getPublishedByUserId() {
        return publishedByUserId;
    }

    public void setPublishedByUserId(Long value) {
        publishedByUserId = value;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long value) {
        createdByUserId = value;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long value) {
        updatedByUserId = value;
    }

    public byte[] getContentHash() {
        return contentHash;
    }

    public void setContentHash(byte[] value) {
        contentHash = value;
    }
}
