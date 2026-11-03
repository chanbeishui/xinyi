package com.xinyi.rongli.ledger.domain;

import java.time.LocalDateTime;

/**
 * 对账成员变更不可变历史
 *
 * <p>对应表 vi_driver_statement_membership_change，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverStatementMembershipChange
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 账单 */
    private Long statementId;

    /** 源条目；整单作废时可为空 */
    private Long ledgerId;

    /** 可重复激活的工作行 */
    private Long lineId;

    /** CREATE/ADD/REMOVE/REACTIVATE/VOID */
    private String action;

    /** 变更前内容版本 */
    private Integer revisionBefore;

    /** 变更后内容版本 */
    private Integer revisionAfter;

    /** 真实授权后台操作者 */
    private Long actorUserId;

    /** 操作依据 */
    private String reason;

    /** 集合变化及涉及行完整快照；不存凭据 */
    private String snapshotJson;

    /** UTC */
    private LocalDateTime createdAt;

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

    public Long getLedgerId()
    {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId)
    {
        this.ledgerId = ledgerId;
    }

    public Long getLineId()
    {
        return lineId;
    }

    public void setLineId(Long lineId)
    {
        this.lineId = lineId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Integer getRevisionBefore()
    {
        return revisionBefore;
    }

    public void setRevisionBefore(Integer revisionBefore)
    {
        this.revisionBefore = revisionBefore;
    }

    public Integer getRevisionAfter()
    {
        return revisionAfter;
    }

    public void setRevisionAfter(Integer revisionAfter)
    {
        this.revisionAfter = revisionAfter;
    }

    public Long getActorUserId()
    {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId)
    {
        this.actorUserId = actorUserId;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getSnapshotJson()
    {
        return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson)
    {
        this.snapshotJson = snapshotJson;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }
}
