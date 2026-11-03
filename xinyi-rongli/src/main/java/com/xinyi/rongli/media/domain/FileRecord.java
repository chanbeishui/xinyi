package com.xinyi.rongli.media.domain;

import java.time.LocalDateTime;

/**
 * 私有文件登记与校验
 *
 * <p>对应表 vi_file，由数据库结构生成，字段与列一一对应。</p>
 */
public class FileRecord
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 上传者 */
    private Long uploaderAccountId;

    /** 限定用途 */
    private String purpose;

    /** 订单归属 */
    private Long orderId;

    /** 认证等申请归属 */
    private Long applicationId;

    /** 服务端生成不可猜键 */
    private String objectKey;

    /** FileStatus: UPLOADING,VERIFYING,READY,REJECTED */
    private String status;

    /** 内容识别类型 */
    private String mimeType;

    /** 实际大小 */
    private Long serverSize;

    /** 服务器复核摘要 */
    private byte[] serverSha256;

    /** 按已审批保留政策 */
    private LocalDateTime retainUntil;

    /** 争议等必要留存 */
    private Integer legalHold;

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

    public Long getUploaderAccountId()
    {
        return uploaderAccountId;
    }

    public void setUploaderAccountId(Long uploaderAccountId)
    {
        this.uploaderAccountId = uploaderAccountId;
    }

    public String getPurpose()
    {
        return purpose;
    }

    public void setPurpose(String purpose)
    {
        this.purpose = purpose;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getApplicationId()
    {
        return applicationId;
    }

    public void setApplicationId(Long applicationId)
    {
        this.applicationId = applicationId;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public Long getServerSize()
    {
        return serverSize;
    }

    public void setServerSize(Long serverSize)
    {
        this.serverSize = serverSize;
    }

    public byte[] getServerSha256()
    {
        return serverSha256;
    }

    public void setServerSha256(byte[] serverSha256)
    {
        this.serverSha256 = serverSha256;
    }

    public LocalDateTime getRetainUntil()
    {
        return retainUntil;
    }

    public void setRetainUntil(LocalDateTime retainUntil)
    {
        this.retainUntil = retainUntil;
    }

    public Integer getLegalHold()
    {
        return legalHold;
    }

    public void setLegalHold(Integer legalHold)
    {
        this.legalHold = legalHold;
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
