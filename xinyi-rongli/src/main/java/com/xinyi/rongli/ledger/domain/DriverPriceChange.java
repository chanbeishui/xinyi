package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 对账内填写与改价不可变记录
 *
 * <p>对应表 vi_driver_price_change，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverPriceChange
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 账单 */
    private Long statementId;

    /** 工作明细 */
    private Long statementLineId;

    /** 不可变费用源 */
    private Long ledgerId;

    /** 修改后的内容修订 */
    private Integer revision;

    /** 原拟价，未填写为NULL */
    private Long oldAmountFen;

    /** 新拟价，总额输入，不是重复加差额 */
    private Long newAmountFen;

    /** 修改原因或初次定价依据 */
    private String reason;

    /** 经权限及归属验证的文件引用 */
    private String evidenceFileIds;

    /** 从ADMIN会话读取 */
    private Long actorUserId;

    /** 与受控绑定一致的真实人员关联，可空 */
    private Long actorAccountId;

    /** 同主体动作资源请求键 */
    private String idempotencyKey;

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

    public Long getStatementLineId()
    {
        return statementLineId;
    }

    public void setStatementLineId(Long statementLineId)
    {
        this.statementLineId = statementLineId;
    }

    public Long getLedgerId()
    {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId)
    {
        this.ledgerId = ledgerId;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public Long getOldAmountFen()
    {
        return oldAmountFen;
    }

    public void setOldAmountFen(Long oldAmountFen)
    {
        this.oldAmountFen = oldAmountFen;
    }

    public Long getNewAmountFen()
    {
        return newAmountFen;
    }

    public void setNewAmountFen(Long newAmountFen)
    {
        this.newAmountFen = newAmountFen;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getEvidenceFileIds()
    {
        return evidenceFileIds;
    }

    public void setEvidenceFileIds(String evidenceFileIds)
    {
        this.evidenceFileIds = evidenceFileIds;
    }

    public Long getActorUserId()
    {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId)
    {
        this.actorUserId = actorUserId;
    }

    public Long getActorAccountId()
    {
        return actorAccountId;
    }

    public void setActorAccountId(Long actorAccountId)
    {
        this.actorAccountId = actorAccountId;
    }

    public String getIdempotencyKey()
    {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey)
    {
        this.idempotencyKey = idempotencyKey;
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
