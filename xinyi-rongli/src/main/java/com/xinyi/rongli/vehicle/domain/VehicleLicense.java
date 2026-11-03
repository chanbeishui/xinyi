package com.xinyi.rongli.vehicle.domain;

import java.time.LocalDateTime;

/**
 * 行驶证登记与人工复核
 *
 * <p>对应表 vi_vehicle_license，由数据库结构生成，字段与列一一对应。</p>
 */
public class VehicleLicense
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 车主/VIN等密文 */
    private byte[] sensitivePayloadCipher;

    /** 主页 */
    private Long mainFileId;

    /** 副页 */
    private Long subFileId;

    /** 服务端OCR任务 */
    private Long ocrTaskId;

    /** LicenseReviewStatus: MISSING,PENDING,VERIFIED */
    private String reviewStatus;

    /** 框架核验人 */
    private Long reviewerUserId;

    /** 复核时间 */
    private LocalDateTime reviewedAt;

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

    public byte[] getSensitivePayloadCipher()
    {
        return sensitivePayloadCipher;
    }

    public void setSensitivePayloadCipher(byte[] sensitivePayloadCipher)
    {
        this.sensitivePayloadCipher = sensitivePayloadCipher;
    }

    public Long getMainFileId()
    {
        return mainFileId;
    }

    public void setMainFileId(Long mainFileId)
    {
        this.mainFileId = mainFileId;
    }

    public Long getSubFileId()
    {
        return subFileId;
    }

    public void setSubFileId(Long subFileId)
    {
        this.subFileId = subFileId;
    }

    public Long getOcrTaskId()
    {
        return ocrTaskId;
    }

    public void setOcrTaskId(Long ocrTaskId)
    {
        this.ocrTaskId = ocrTaskId;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
    }

    public Long getReviewerUserId()
    {
        return reviewerUserId;
    }

    public void setReviewerUserId(Long reviewerUserId)
    {
        this.reviewerUserId = reviewerUserId;
    }

    public LocalDateTime getReviewedAt()
    {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt)
    {
        this.reviewedAt = reviewedAt;
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
