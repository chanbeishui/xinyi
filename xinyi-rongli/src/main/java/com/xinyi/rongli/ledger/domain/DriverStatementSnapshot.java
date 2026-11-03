package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 已发送与确认的不可变版本快照
 *
 * <p>对应表 vi_driver_statement_snapshot，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverStatementSnapshot
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 账单 */
    private Long statementId;

    /** 内容版本 */
    private Integer revision;

    /** 规范化SHA-256，包含主体司机币种和明细 */
    private byte[] snapshotHash;

    /** 完整明细、理由、总额与审核来源 */
    private String snapshotJson;

    /** 此版明确净应付 */
    private Long amountFen;

    /** 发送或复核人 */
    private Long publishedBy;

    /** 仅对本人发布的规范化字段，不含内部理由/改价者/复核者 */
    private String publicSnapshotJson;

    /** SHA256公开快照；MINI snapshotHash与此匹配… */
    private byte[] publicSnapshotHash;

    /** 版本发出时间 */
    private LocalDateTime publishedAt;

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

    public Long getAmountFen()
    {
        return amountFen;
    }

    public void setAmountFen(Long amountFen)
    {
        this.amountFen = amountFen;
    }

    public Long getPublishedBy()
    {
        return publishedBy;
    }

    public void setPublishedBy(Long publishedBy)
    {
        this.publishedBy = publishedBy;
    }

    public String getPublicSnapshotJson()
    {
        return publicSnapshotJson;
    }

    public void setPublicSnapshotJson(String publicSnapshotJson)
    {
        this.publicSnapshotJson = publicSnapshotJson;
    }

    public byte[] getPublicSnapshotHash()
    {
        return publicSnapshotHash;
    }

    public void setPublicSnapshotHash(byte[] publicSnapshotHash)
    {
        this.publicSnapshotHash = publicSnapshotHash;
    }

    public LocalDateTime getPublishedAt()
    {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt)
    {
        this.publishedAt = publishedAt;
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
