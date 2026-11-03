package com.xinyi.rongli.fulfill.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 照片节点分组
 *
 * <p>对应表 vi_evidence_group，由数据库结构生成，字段与列一一对应。</p>
 */
public class EvidenceGroup
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 承接 */
    private Long assignmentId;

    /** 1接车2到站3取车4归还 */
    private Integer nodeNo;

    /** EvidenceStatus: DRAFT,READY,SEALED */
    private String status;

    /** 仪表公里数 */
    private BigDecimal mileageKm;

    /** MANUAL/OCR_REVIEWED */
    private String mileageSource;

    /** 异常说明 */
    private String mileageReason;

    /** 交接时封存 */
    private LocalDateTime sealedAt;

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

    public Long getAssignmentId()
    {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId)
    {
        this.assignmentId = assignmentId;
    }

    public Integer getNodeNo()
    {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo)
    {
        this.nodeNo = nodeNo;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public BigDecimal getMileageKm()
    {
        return mileageKm;
    }

    public void setMileageKm(BigDecimal mileageKm)
    {
        this.mileageKm = mileageKm;
    }

    public String getMileageSource()
    {
        return mileageSource;
    }

    public void setMileageSource(String mileageSource)
    {
        this.mileageSource = mileageSource;
    }

    public String getMileageReason()
    {
        return mileageReason;
    }

    public void setMileageReason(String mileageReason)
    {
        this.mileageReason = mileageReason;
    }

    public LocalDateTime getSealedAt()
    {
        return sealedAt;
    }

    public void setSealedAt(LocalDateTime sealedAt)
    {
        this.sealedAt = sealedAt;
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
