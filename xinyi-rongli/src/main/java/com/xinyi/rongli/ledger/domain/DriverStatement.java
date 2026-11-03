package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 单主体单司机单币种对账单
 *
 * <p>对应表 vi_driver_statement，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverStatement
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 对外账单编号 */
    private String statementNo;

    /** 代驾 */
    private Long driverId;

    /** 应付主体 */
    private Long payorEntityId;

    /** 币种 */
    private String currency;

    /** 当前有效明细最早服务结束日UTC；仅说明跨度，不用于组单；空草稿NULL */
    private LocalDateTime serviceRangeStart;

    /** 有效明细最晚结束日次日UTC；仅说明，不限定成员；空草稿NULL */
    private LocalDateTime serviceRangeEndExclusive;

    /** 本次数据截点 */
    private LocalDateTime cutoffAt;

    /** StatementStatus: DRAFT,INTERNAL_REVIEW,SENT… */
    private String status;

    /** 未确认修订版本 */
    private Integer revision;

    /** 草稿有未核价行时NULL；不是0；金额齐全时为净拟应付 */
    private Long amountFen;

    /** 当前工作副本规范化摘要，可包含NULL；发送前金额必须完整 */
    private byte[] snapshotHash;

    /** 平台制作人 */
    private Long issuedBy;

    /** 发出时冻结当前版本 */
    private LocalDateTime sentAt;

    /** 本人微信账户确认 */
    private Long confirmedAccountId;

    /** 实际确认时间 */
    private LocalDateTime confirmedAt;

    /** 确认的版本 */
    private Integer confirmationRevision;

    /** 仅未确认账单可作废 */
    private LocalDateTime voidedAt;

    /** 已填写金额小计；不能冒充完整应付 */
    private Long knownAmountFen;

    /** 待填写行数，发送和确认要求为0 */
    private Integer unpricedCount;

    /** 本次有人工填价/改价的行数 */
    private Integer manualPriceCount;

    /** StatementReviewStatus: NOT_REQUIRED,REQUIRED… */
    private String reviewStatus;

    /** 仅对匹配revision和hash的整单复核生效 */
    private Integer reviewRevision;

    /** 当前发送给司机的版本；撤回后失效 */
    private Integer publishedRevision;

    /** 当前有效公开快照SHA256，与public_snapshot_hash一致… */
    private byte[] publishedHash;

    /** 复核提交人，需与复核人分离 */
    private Long submittedBy;

    /** SelectionMode: EXPLICIT */
    private String selectionMode;

    /** 查询条件留痕，不是组单成员定义；不能据此重跑批量选择 */
    private String filterSnapshot;

    /** 当前有效工作行数；0草稿可保存不可发出 */
    private Integer activeEntryCount;

    /** UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getStatementNo()
    {
        return statementNo;
    }

    public void setStatementNo(String statementNo)
    {
        this.statementNo = statementNo;
    }

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public Long getPayorEntityId()
    {
        return payorEntityId;
    }

    public void setPayorEntityId(Long payorEntityId)
    {
        this.payorEntityId = payorEntityId;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public LocalDateTime getServiceRangeStart()
    {
        return serviceRangeStart;
    }

    public void setServiceRangeStart(LocalDateTime serviceRangeStart)
    {
        this.serviceRangeStart = serviceRangeStart;
    }

    public LocalDateTime getServiceRangeEndExclusive()
    {
        return serviceRangeEndExclusive;
    }

    public void setServiceRangeEndExclusive(LocalDateTime serviceRangeEndExclusive)
    {
        this.serviceRangeEndExclusive = serviceRangeEndExclusive;
    }

    public LocalDateTime getCutoffAt()
    {
        return cutoffAt;
    }

    public void setCutoffAt(LocalDateTime cutoffAt)
    {
        this.cutoffAt = cutoffAt;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public byte[] getSnapshotHash()
    {
        return snapshotHash;
    }

    public void setSnapshotHash(byte[] snapshotHash)
    {
        this.snapshotHash = snapshotHash;
    }

    public Long getIssuedBy()
    {
        return issuedBy;
    }

    public void setIssuedBy(Long issuedBy)
    {
        this.issuedBy = issuedBy;
    }

    public LocalDateTime getSentAt()
    {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt)
    {
        this.sentAt = sentAt;
    }

    public Long getConfirmedAccountId()
    {
        return confirmedAccountId;
    }

    public void setConfirmedAccountId(Long confirmedAccountId)
    {
        this.confirmedAccountId = confirmedAccountId;
    }

    public LocalDateTime getConfirmedAt()
    {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt)
    {
        this.confirmedAt = confirmedAt;
    }

    public Integer getConfirmationRevision()
    {
        return confirmationRevision;
    }

    public void setConfirmationRevision(Integer confirmationRevision)
    {
        this.confirmationRevision = confirmationRevision;
    }

    public LocalDateTime getVoidedAt()
    {
        return voidedAt;
    }

    public void setVoidedAt(LocalDateTime voidedAt)
    {
        this.voidedAt = voidedAt;
    }

    public Long getKnownAmountFen()
    {
        return knownAmountFen;
    }

    public void setKnownAmountFen(Long knownAmountFen)
    {
        this.knownAmountFen = knownAmountFen;
    }

    public Integer getUnpricedCount()
    {
        return unpricedCount;
    }

    public void setUnpricedCount(Integer unpricedCount)
    {
        this.unpricedCount = unpricedCount;
    }

    public Integer getManualPriceCount()
    {
        return manualPriceCount;
    }

    public void setManualPriceCount(Integer manualPriceCount)
    {
        this.manualPriceCount = manualPriceCount;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
    }

    public Integer getReviewRevision()
    {
        return reviewRevision;
    }

    public void setReviewRevision(Integer reviewRevision)
    {
        this.reviewRevision = reviewRevision;
    }

    public Integer getPublishedRevision()
    {
        return publishedRevision;
    }

    public void setPublishedRevision(Integer publishedRevision)
    {
        this.publishedRevision = publishedRevision;
    }

    public byte[] getPublishedHash()
    {
        return publishedHash;
    }

    public void setPublishedHash(byte[] publishedHash)
    {
        this.publishedHash = publishedHash;
    }

    public Long getSubmittedBy()
    {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy)
    {
        this.submittedBy = submittedBy;
    }

    public String getSelectionMode()
    {
        return selectionMode;
    }

    public void setSelectionMode(String selectionMode)
    {
        this.selectionMode = selectionMode;
    }

    public String getFilterSnapshot()
    {
        return filterSnapshot;
    }

    public void setFilterSnapshot(String filterSnapshot)
    {
        this.filterSnapshot = filterSnapshot;
    }

    public Integer getActiveEntryCount()
    {
        return activeEntryCount;
    }

    public void setActiveEntryCount(Integer activeEntryCount)
    {
        this.activeEntryCount = activeEntryCount;
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
