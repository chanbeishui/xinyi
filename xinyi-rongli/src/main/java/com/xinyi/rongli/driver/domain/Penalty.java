package com.xinyi.rongli.driver.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 普通取消计次与轮转处罚
 *
 * <p>对应表 vi_penalty，由数据库结构生成，字段与列一一对应。</p>
 */
public class Penalty
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 司机 */
    private Long driverId;

    /** 取消承接 */
    private Long assignmentId;

    /** 北京时间自然日 */
    private LocalDate businessDate;

    /** 审核后是否计入 */
    private Integer counted;

    /** 恢复轮次：P01评审 */
    private String roundKey;

    /** PenaltyType: SKIP_ROUND,STOP_TODAY */
    private String penaltyType;

    /** PenaltyStatus: ACTIVE,EXPIRED,REVOKED */
    private String status;

    /** 处罚依据，对本人可读 */
    private String reason;

    /** 实际适用轮转及计次规则版本 */
    private String ruleVersion;

    /** 明确时刻；轮转型另用round_key */
    private LocalDateTime effectiveUntil;

    /** 通过申诉撤销时刻 */
    private LocalDateTime revokedAt;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getDriverId()
    {
        return driverId;
    }

    public void setDriverId(Long driverId)
    {
        this.driverId = driverId;
    }

    public Long getAssignmentId()
    {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId)
    {
        this.assignmentId = assignmentId;
    }

    public LocalDate getBusinessDate()
    {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate)
    {
        this.businessDate = businessDate;
    }

    public Integer getCounted()
    {
        return counted;
    }

    public void setCounted(Integer counted)
    {
        this.counted = counted;
    }

    public String getRoundKey()
    {
        return roundKey;
    }

    public void setRoundKey(String roundKey)
    {
        this.roundKey = roundKey;
    }

    public String getPenaltyType()
    {
        return penaltyType;
    }

    public void setPenaltyType(String penaltyType)
    {
        this.penaltyType = penaltyType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getRuleVersion()
    {
        return ruleVersion;
    }

    public void setRuleVersion(String ruleVersion)
    {
        this.ruleVersion = ruleVersion;
    }

    public LocalDateTime getEffectiveUntil()
    {
        return effectiveUntil;
    }

    public void setEffectiveUntil(LocalDateTime effectiveUntil)
    {
        this.effectiveUntil = effectiveUntil;
    }

    public LocalDateTime getRevokedAt()
    {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt)
    {
        this.revokedAt = revokedAt;
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
