package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 对账工作明细；历史发布版本另表保存
 *
 * <p>对应表 vi_driver_statement_line，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverStatementLine
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 对账单 */
    private Long statementId;

    /** 所属账单修订 */
    private Integer statementRevision;

    /** 费用源条目 */
    private Long ledgerId;

    /** 承接关联 */
    private Long assignmentId;

    /** 本工作版本拟金额，可空；已发送快照必须完整 */
    private Long amountFen;

    /** 车牌/订单/服务时间/类型/规则/金额 */
    private String snapshot;

    /** 内容摘要 */
    private byte[] lineHash;

    /** 规则/源台账金额，只读，NULL不伪装成0 */
    private Long referenceAmountFen;

    /** PricingSource: RULE,UNPRICED,MANUAL_FILLED… */
    private String pricingSource;

    /** 人工参与标记；改回原金额不擦除操作轨迹 */
    private Integer manualChanged;

    /** 当前拟价依据 */
    private String priceReason;

    /** 最近编辑人；复核排除需查本轮全部改价人 */
    private Long lastEditorUserId;

    /** 源台账快照版本 */
    private Long sourceVersion;

    /** 0为从草稿移出，保留价格与审计；只对有效行求合计和发布快照 */
    private Integer isActive;

    /** 最近移出时间 */
    private LocalDateTime removedAt;

    /** 移出操作者 */
    private Long removedBy;

    /** 移出原因 */
    private String removeReason;

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

    public Long getStatementId()
    {
        return statementId;
    }

    public void setStatementId(Long statementId)
    {
        this.statementId = statementId;
    }

    public Integer getStatementRevision()
    {
        return statementRevision;
    }

    public void setStatementRevision(Integer statementRevision)
    {
        this.statementRevision = statementRevision;
    }

    public Long getLedgerId()
    {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId)
    {
        this.ledgerId = ledgerId;
    }

    public Long getAssignmentId()
    {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId)
    {
        this.assignmentId = assignmentId;
    }

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public String getSnapshot()
    {
        return snapshot;
    }

    public void setSnapshot(String snapshot)
    {
        this.snapshot = snapshot;
    }

    public byte[] getLineHash()
    {
        return lineHash;
    }

    public void setLineHash(byte[] lineHash)
    {
        this.lineHash = lineHash;
    }

    public Long getReferenceAmountFen()
    {
        return referenceAmountFen;
    }

    public void setReferenceAmountFen(Long referenceAmountFen)
    {
        this.referenceAmountFen = referenceAmountFen;
    }

    public String getPricingSource()
    {
        return pricingSource;
    }

    public void setPricingSource(String pricingSource)
    {
        this.pricingSource = pricingSource;
    }

    public Integer getManualChanged()
    {
        return manualChanged;
    }

    public void setManualChanged(Integer manualChanged)
    {
        this.manualChanged = manualChanged;
    }

    public String getPriceReason()
    {
        return priceReason;
    }

    public void setPriceReason(String priceReason)
    {
        this.priceReason = priceReason;
    }

    public Long getLastEditorUserId()
    {
        return lastEditorUserId;
    }

    public void setLastEditorUserId(Long lastEditorUserId)
    {
        this.lastEditorUserId = lastEditorUserId;
    }

    public Long getSourceVersion()
    {
        return sourceVersion;
    }

    public void setSourceVersion(Long sourceVersion)
    {
        this.sourceVersion = sourceVersion;
    }

    public Integer getIsActive()
    {
        return isActive;
    }

    public void setIsActive(Integer isActive)
    {
        this.isActive = isActive;
    }

    public LocalDateTime getRemovedAt()
    {
        return removedAt;
    }

    public void setRemovedAt(LocalDateTime removedAt)
    {
        this.removedAt = removedAt;
    }

    public Long getRemovedBy()
    {
        return removedBy;
    }

    public void setRemovedBy(Long removedBy)
    {
        this.removedBy = removedBy;
    }

    public String getRemoveReason()
    {
        return removeReason;
    }

    public void setRemoveReason(String removeReason)
    {
        this.removeReason = removeReason;
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
