package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 整单内部复核记录
 *
 * <p>对应表 vi_driver_statement_review，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverStatementReview
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 账单 */
    private Long statementId;

    /** 提交内容版本 */
    private Integer revision;

    /** 待审全部明细摘要 */
    private byte[] snapshotHash;

    /** 冻结的明细与变更，不能只查当前值 */
    private String snapshotJson;

    /** 服务端查出的本轮所有实际改价人 */
    private String contributorIds;

    /** 提交人 */
    private Long submittedBy;

    /** 独立复核人 */
    private Long reviewedBy;

    /** InternalReviewRecordStatus: PENDING,APPROVED… */
    private String status;

    /** 复核或撤回意见 */
    private String opinion;

    /** 决定时间 */
    private LocalDateTime decidedAt;

    /** 每账单至多一份待审 */
    private Long activeStatement;

    /** UTC */
    private LocalDateTime createdAt;

    /** 并发控制；不可变行不更新内容 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getStatementId()
    {
        return statementId;
    }

    public void setStatementId(Long statementId)
    {
        this.statementId = statementId;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public byte[] getSnapshotHash()
    {
        return snapshotHash;
    }

    public void setSnapshotHash(byte[] snapshotHash)
    {
        this.snapshotHash = snapshotHash;
    }

    public String getSnapshotJson()
    {
        return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson)
    {
        this.snapshotJson = snapshotJson;
    }

    public String getContributorIds()
    {
        return contributorIds;
    }

    public void setContributorIds(String contributorIds)
    {
        this.contributorIds = contributorIds;
    }

    public Long getSubmittedBy()
    {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy)
    {
        this.submittedBy = submittedBy;
    }

    public Long getReviewedBy()
    {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy)
    {
        this.reviewedBy = reviewedBy;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }

    public LocalDateTime getDecidedAt()
    {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt)
    {
        this.decidedAt = decidedAt;
    }

    public Long getActiveStatement()
    {
        return activeStatement;
    }

    public void setActiveStatement(Long activeStatement)
    {
        this.activeStatement = activeStatement;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
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
