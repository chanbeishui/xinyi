package com.xinyi.rongli.owner.domain;

import java.time.LocalDateTime;

/**
 * 客户常用地址
 *
 * <p>对应表 vi_address，由数据库结构生成，字段与列一一对应。</p>
 */
public class Address
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 所属账户 */
    private Long ownerAccountId;

    /** 家/公司等 */
    private String label;

    /** 精确地址加密 */
    private byte[] addressCipher;

    /** 坐标敏感数据 */
    private byte[] locationCipher;

    /** 常用 */
    private Integer isDefault;

    /** 软删除 */
    private LocalDateTime deletedAt;

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

    public Long getOwnerAccountId()
    {
        return ownerAccountId;
    }

    public void setOwnerAccountId(Long ownerAccountId)
    {
        this.ownerAccountId = ownerAccountId;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public byte[] getAddressCipher()
    {
        return addressCipher;
    }

    public void setAddressCipher(byte[] addressCipher)
    {
        this.addressCipher = addressCipher;
    }

    public byte[] getLocationCipher()
    {
        return locationCipher;
    }

    public void setLocationCipher(byte[] locationCipher)
    {
        this.locationCipher = locationCipher;
    }

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }

    public LocalDateTime getDeletedAt()
    {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt)
    {
        this.deletedAt = deletedAt;
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
