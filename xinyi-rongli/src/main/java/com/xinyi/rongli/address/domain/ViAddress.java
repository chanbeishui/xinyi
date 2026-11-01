package com.xinyi.rongli.address.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;

/**
 * vi_address 车主地址簿（A02）。地址文本/位置加密存储（address_cipher/location_cipher）；
 * 软删除 deleted_at；is_default 常用标记（同事务串行保证单默认）；version CAS。
 */
public class ViAddress extends ViRecord {
    private Long ownerAccountId;
    private String label;
    private byte[] addressCipher;
    private byte[] locationCipher;
    private Integer isDefault;
    private Instant deletedAt;

    public Long getOwnerAccountId() { return ownerAccountId; }
    public void setOwnerAccountId(Long v) { this.ownerAccountId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public byte[] getAddressCipher() { return addressCipher; }
    public void setAddressCipher(byte[] v) { this.addressCipher = v; }
    public byte[] getLocationCipher() { return locationCipher; }
    public void setLocationCipher(byte[] v) { this.locationCipher = v; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer v) { this.isDefault = v; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant v) { this.deletedAt = v; }
}