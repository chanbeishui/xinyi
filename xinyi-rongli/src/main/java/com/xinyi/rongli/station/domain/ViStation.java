package com.xinyi.rongli.station.domain;

import java.math.BigDecimal;
import java.time.Instant;

/** 检测点与可服务设置（vi_station）。联系电话密文存储、对外仅脱敏；资质文件ID列表引用 vi_file；dept_id 为框架权威部门归属。 */
public class ViStation {
    private Long id;
    private String name;
    private Long deptId;
    private String address;
    private BigDecimal lng;
    private BigDecimal lat;
    private Integer serviceRadiusMeter;
    private String supportedVehicleCodes;
    private String businessHours;
    private Integer enabled;
    private byte[] contactPhoneCipher;
    private String qualificationFileIds;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long v) { this.deptId = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal v) { this.lng = v; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal v) { this.lat = v; }
    public Integer getServiceRadiusMeter() { return serviceRadiusMeter; }
    public void setServiceRadiusMeter(Integer v) { this.serviceRadiusMeter = v; }
    public String getSupportedVehicleCodes() { return supportedVehicleCodes; }
    public void setSupportedVehicleCodes(String v) { this.supportedVehicleCodes = v; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String v) { this.businessHours = v; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer v) { this.enabled = v; }
    public byte[] getContactPhoneCipher() { return contactPhoneCipher; }
    public void setContactPhoneCipher(byte[] v) { this.contactPhoneCipher = v; }
    public String getQualificationFileIds() { return qualificationFileIds; }
    public void setQualificationFileIds(String v) { this.qualificationFileIds = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }
}