package com.xinyi.rongli.order.domain;

import java.time.LocalDateTime;

/**
 * 不可变生效计价版本
 *
 * <p>对应表 vi_price_rule，由数据库结构生成，字段与列一一对应。</p>
 */
public class PriceRule
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 稳定方案标识 */
    private String ruleKey;

    /** 新版本号 */
    private Integer revision;

    /** 车型字典编码 */
    private String vehicleTypeCode;

    /** 检测业务类型 */
    private String inspectionTypeCode;

    /** PriceRuleStatus: DRAFT,IN_REVIEW,APPROVED… */
    private String status;

    /** 按PriceInput约束的计价参数，分与米 */
    private String configJson;

    /** 生效时间 */
    private LocalDateTime effectiveFrom;

    /** 失效时间 */
    private LocalDateTime effectiveTo;

    /** 提交人 */
    private Long submittedBy;

    /** 审核人 */
    private Long approvedBy;

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

    public String getRuleKey()
    {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey)
    {
        this.ruleKey = ruleKey;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public String getVehicleTypeCode()
    {
        return vehicleTypeCode;
    }

    public void setVehicleTypeCode(String vehicleTypeCode)
    {
        this.vehicleTypeCode = vehicleTypeCode;
    }

    public String getInspectionTypeCode()
    {
        return inspectionTypeCode;
    }

    public void setInspectionTypeCode(String inspectionTypeCode)
    {
        this.inspectionTypeCode = inspectionTypeCode;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getConfigJson()
    {
        return configJson;
    }

    public void setConfigJson(String configJson)
    {
        this.configJson = configJson;
    }

    public LocalDateTime getEffectiveFrom()
    {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom)
    {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo()
    {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo)
    {
        this.effectiveTo = effectiveTo;
    }

    public Long getSubmittedBy()
    {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy)
    {
        this.submittedBy = submittedBy;
    }

    public Long getApprovedBy()
    {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy)
    {
        this.approvedBy = approvedBy;
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
