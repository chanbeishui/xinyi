package com.xinyi.rongli.fulfill.domain;

import java.time.LocalDateTime;

/**
 * 照片版本与服务端元数据
 *
 * <p>对应表 vi_photo，由数据库结构生成，字段与列一一对应。</p>
 */
public class Photo
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 证据组 */
    private Long groupId;

    /** PhotoType: FRONT,BACK,LEFT,RIGHT,DASHBOARD… */
    private String photoType;

    /** 私有文件 */
    private Long fileId;

    /** 同次重传去重 */
    private String clientCaptureId;

    /** 版本 */
    private Integer revision;

    /** 草稿重拍历史 */
    private LocalDateTime supersededAt;

    /** 客户端采集，仅作声明 */
    private LocalDateTime capturedAt;

    /** 服务端收件时间 */
    private LocalDateTime receivedAt;

    /** 节点位置 */
    private byte[] locationCipher;

    /** 服务端生成水印副本 */
    private Long watermarkFileId;

    /** 每类有效图唯一，EXTRA不覆盖 */
    private String activeType;

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

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public String getPhotoType()
    {
        return photoType;
    }

    public void setPhotoType(String photoType)
    {
        this.photoType = photoType;
    }

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public String getClientCaptureId()
    {
        return clientCaptureId;
    }

    public void setClientCaptureId(String clientCaptureId)
    {
        this.clientCaptureId = clientCaptureId;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public LocalDateTime getSupersededAt()
    {
        return supersededAt;
    }

    public void setSupersededAt(LocalDateTime supersededAt)
    {
        this.supersededAt = supersededAt;
    }

    public LocalDateTime getCapturedAt()
    {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt)
    {
        this.capturedAt = capturedAt;
    }

    public LocalDateTime getReceivedAt()
    {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt)
    {
        this.receivedAt = receivedAt;
    }

    public byte[] getLocationCipher()
    {
        return locationCipher;
    }

    public void setLocationCipher(byte[] locationCipher)
    {
        this.locationCipher = locationCipher;
    }

    public Long getWatermarkFileId()
    {
        return watermarkFileId;
    }

    public void setWatermarkFileId(Long watermarkFileId)
    {
        this.watermarkFileId = watermarkFileId;
    }

    public String getActiveType()
    {
        return activeType;
    }

    public void setActiveType(String activeType)
    {
        this.activeType = activeType;
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
