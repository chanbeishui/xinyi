package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 本人对账差异与处理记录
 *
 * <p>对应表 vi_driver_statement_difference，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverStatementDifference
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 账单 */
    private Long statementId;

    /** 被质疑版本 */
    private Integer statementRevision;

    /** 本人 */
    private Long driverAccountId;

    /** 相关条目 */
    private String entryIds;

    /** 差异说明 */
    private String description;

    /** 凭证引用 */
    private String evidenceIds;

    /** StatementDifferenceStatus: OPEN,RESOLVED */
    private String status;

    /** 处理说明 */
    private String resolution;

    /** 金额变更引用已审核调整 */
    private String approvedAdjustmentIds;

    /** 处理人 */
    private Long resolvedBy;

    /** UTC */
    private LocalDateTime resolvedAt;

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

    public Long getDriverAccountId()
    {
        return driverAccountId;
    }

    public void setDriverAccountId(Long driverAccountId)
    {
        this.driverAccountId = driverAccountId;
    }

    public String getEntryIds()
    {
        return entryIds;
    }

    public void setEntryIds(String entryIds)
    {
        this.entryIds = entryIds;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getEvidenceIds()
    {
        return evidenceIds;
    }

    public void setEvidenceIds(String evidenceIds)
    {
        this.evidenceIds = evidenceIds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getResolution()
    {
        return resolution;
    }

    public void setResolution(String resolution)
    {
        this.resolution = resolution;
    }

    public String getApprovedAdjustmentIds()
    {
        return approvedAdjustmentIds;
    }

    public void setApprovedAdjustmentIds(String approvedAdjustmentIds)
    {
        this.approvedAdjustmentIds = approvedAdjustmentIds;
    }

    public Long getResolvedBy()
    {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy)
    {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt()
    {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt)
    {
        this.resolvedAt = resolvedAt;
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
