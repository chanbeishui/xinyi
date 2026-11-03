package com.xinyi.rongli.vehicle.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 授权客户-车辆关联档案，供独立车辆列表
 *
 * <p>对应表 vi_vehicle，由数据库结构生成，字段与列一一对应。</p>
 */
public class Vehicle
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 基础账户 */
    private Long ownerAccountId;

    /** 归一化车牌 */
    private String plateNo;

    /** 数据字典编码 */
    private String vehicleTypeCode;

    /** 可选 */
    private String brandModel;

    /** 只读核验摘要 */
    private String vinLast4;

    /** 登记日期，按需核验 */
    private LocalDate registeredOn;

    /** 使用性质 */
    private String useType;

    /** VehicleVerificationStatus: PENDING,VERIFIED… */
    private String verificationStatus;

    /** 运营资料备注，不回写历史订单 */
    private String internalRemark;

    /** 归属部门；NULL=全公司共享 */
    private Long deptId;

    /** 服务层保证本人至多一辆默认 */
    private Integer isDefault;

    /** 软删除 */
    private LocalDateTime deletedAt;

    /** 软删除后不占用本人车牌约束 */
    private String activePlate;

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

    public String getPlateNo()
    {
        return plateNo;
    }

    public void setPlateNo(String plateNo)
    {
        this.plateNo = plateNo;
    }

    public String getVehicleTypeCode()
    {
        return vehicleTypeCode;
    }

    public void setVehicleTypeCode(String vehicleTypeCode)
    {
        this.vehicleTypeCode = vehicleTypeCode;
    }

    public String getBrandModel()
    {
        return brandModel;
    }

    public void setBrandModel(String brandModel)
    {
        this.brandModel = brandModel;
    }

    public String getVinLast4()
    {
        return vinLast4;
    }

    public void setVinLast4(String vinLast4)
    {
        this.vinLast4 = vinLast4;
    }

    public LocalDate getRegisteredOn()
    {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDate registeredOn)
    {
        this.registeredOn = registeredOn;
    }

    public String getUseType()
    {
        return useType;
    }

    public void setUseType(String useType)
    {
        this.useType = useType;
    }

    public String getVerificationStatus()
    {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus)
    {
        this.verificationStatus = verificationStatus;
    }

    public String getInternalRemark()
    {
        return internalRemark;
    }

    public void setInternalRemark(String internalRemark)
    {
        this.internalRemark = internalRemark;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
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

    public String getActivePlate()
    {
        return activePlate;
    }

    public void setActivePlate(String activePlate)
    {
        this.activePlate = activePlate;
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
