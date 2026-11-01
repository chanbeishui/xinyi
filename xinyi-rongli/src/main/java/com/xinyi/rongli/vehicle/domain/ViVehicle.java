package com.xinyi.rongli.vehicle.domain;

import com.xinyi.rongli.core.ViRecord;

import java.time.Instant;
import java.time.LocalDate;

/**
 * vi_vehicle 车主车辆（A01）。生成列 active_plate 不入实体/不写（软删释放车牌）；
 * uk_owner_plate(owner_account_id,active_plate) 保证同车主同车牌仅一条活跃；软删除 deleted_at；version CAS。
 * 历史订单快照不追改（维护仅改本行）。
 */
public class ViVehicle extends ViRecord {
    private Long ownerAccountId;
    private String plateNo;
    private String vehicleTypeCode;
    private String brandModel;
    private String vinLast4;
    private LocalDate registeredOn;
    private String useType;
    private String verificationStatus;
    private String internalRemark;
    private Integer isDefault;
    private Instant deletedAt;

    public Long getOwnerAccountId() { return ownerAccountId; }
    public void setOwnerAccountId(Long v) { this.ownerAccountId = v; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String v) { this.plateNo = v; }
    public String getVehicleTypeCode() { return vehicleTypeCode; }
    public void setVehicleTypeCode(String v) { this.vehicleTypeCode = v; }
    public String getBrandModel() { return brandModel; }
    public void setBrandModel(String v) { this.brandModel = v; }
    public String getVinLast4() { return vinLast4; }
    public void setVinLast4(String v) { this.vinLast4 = v; }
    public LocalDate getRegisteredOn() { return registeredOn; }
    public void setRegisteredOn(LocalDate v) { this.registeredOn = v; }
    public String getUseType() { return useType; }
    public void setUseType(String v) { this.useType = v; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String v) { this.verificationStatus = v; }
    public String getInternalRemark() { return internalRemark; }
    public void setInternalRemark(String v) { this.internalRemark = v; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer v) { this.isDefault = v; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant v) { this.deletedAt = v; }
}