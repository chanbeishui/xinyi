package com.xinyi.rongli.aftersale.domain;

import java.time.LocalDateTime;

/**
 * 普通服务争议
 *
 * <p>对应表 vi_dispute，由数据库结构生成，字段与列一一对应。</p>
 */
public class Dispute
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 普通服务类型，不是事故 */
    private String type;

    /** ServiceCaseStatus: OPEN,PROCESSING,RESOLVED */
    private String status;

    /** 用户说明 */
    private String description;

    /** 凭证 */
    private String fileIds;

    /** 处理人 */
    private Long handlerId;

    /** 结论 */
    private String conclusion;

    /** 解决时间 */
    private LocalDateTime resolvedAt;

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

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getFileIds()
    {
        return fileIds;
    }

    public void setFileIds(String fileIds)
    {
        this.fileIds = fileIds;
    }

    public Long getHandlerId()
    {
        return handlerId;
    }

    public void setHandlerId(Long handlerId)
    {
        this.handlerId = handlerId;
    }

    public String getConclusion()
    {
        return conclusion;
    }

    public void setConclusion(String conclusion)
    {
        this.conclusion = conclusion;
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
