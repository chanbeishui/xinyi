package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 每费用条目至多一个活跃账单的分配历史
 *
 * <p>对应表 vi_driver_ledger_allocation，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverLedgerAllocation
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 费用条目 */
    private Long ledgerId;

    /** 有效账单 */
    private Long statementId;

    /** 草稿合法移出或作废释放；已确认未付不能释放 */
    private LocalDateTime releasedAt;

    /** 每费用条目至多一个有效账单 */
    private Long activeLedger;

    /** 精确释放原因 */
    private String releaseReason;

    /** 释放者 */
    private Long releasedBy;

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

    public Long getLedgerId()
    {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId)
    {
        this.ledgerId = ledgerId;
    }

    public Long getStatementId()
    {
        return statementId;
    }

    public void setStatementId(Long statementId)
    {
        this.statementId = statementId;
    }

    public LocalDateTime getReleasedAt()
    {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime releasedAt)
    {
        this.releasedAt = releasedAt;
    }

    public Long getActiveLedger()
    {
        return activeLedger;
    }

    public void setActiveLedger(Long activeLedger)
    {
        this.activeLedger = activeLedger;
    }

    public String getReleaseReason()
    {
        return releaseReason;
    }

    public void setReleaseReason(String releaseReason)
    {
        this.releaseReason = releaseReason;
    }

    public Long getReleasedBy()
    {
        return releasedBy;
    }

    public void setReleasedBy(Long releasedBy)
    {
        this.releasedBy = releasedBy;
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
