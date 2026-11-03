package com.xinyi.rongli.driver.domain;

import java.time.LocalDateTime;

/**
 * 代驾资格与听单偏好
 *
 * <p>对应表 vi_driver_profile，由数据库结构生成，字段与列一一对应。</p>
 */
public class DriverProfile
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 同一个基础账户 */
    private Long accountId;

    /** QualificationStatus: DRAFT,SUBMITTED,APPROVED… */
    private String qualificationStatus;

    /** 是否允许新增承接 */
    private Integer newAssignmentEnabled;

    /** 本人听单偏好 */
    private Integer listeningEnabled;

    /** 年度复审到期 */
    private LocalDateTime reviewDueAt;

    /** 实际准驾类别，不用字母排序比较 */
    private String licenseClass;

    /** 到期日 */
    private LocalDateTime licenseValidTo;

    /** 长期有效标记 */
    private Integer licenseLongTerm;

    /** 生效认证版本 */
    private Long qualificationRevision;

    /** 授权运营备注，变更留痕 */
    private String internalRemark;

    /** 归属部门；NULL=全公司共享 */
    private Long deptId;

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

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getQualificationStatus()
    {
        return qualificationStatus;
    }

    public void setQualificationStatus(String qualificationStatus)
    {
        this.qualificationStatus = qualificationStatus;
    }

    public Integer getNewAssignmentEnabled()
    {
        return newAssignmentEnabled;
    }

    public void setNewAssignmentEnabled(Integer newAssignmentEnabled)
    {
        this.newAssignmentEnabled = newAssignmentEnabled;
    }

    public Integer getListeningEnabled()
    {
        return listeningEnabled;
    }

    public void setListeningEnabled(Integer listeningEnabled)
    {
        this.listeningEnabled = listeningEnabled;
    }

    public LocalDateTime getReviewDueAt()
    {
        return reviewDueAt;
    }

    public void setReviewDueAt(LocalDateTime reviewDueAt)
    {
        this.reviewDueAt = reviewDueAt;
    }

    public String getLicenseClass()
    {
        return licenseClass;
    }

    public void setLicenseClass(String licenseClass)
    {
        this.licenseClass = licenseClass;
    }

    public LocalDateTime getLicenseValidTo()
    {
        return licenseValidTo;
    }

    public void setLicenseValidTo(LocalDateTime licenseValidTo)
    {
        this.licenseValidTo = licenseValidTo;
    }

    public Integer getLicenseLongTerm()
    {
        return licenseLongTerm;
    }

    public void setLicenseLongTerm(Integer licenseLongTerm)
    {
        this.licenseLongTerm = licenseLongTerm;
    }

    public Long getQualificationRevision()
    {
        return qualificationRevision;
    }

    public void setQualificationRevision(Long qualificationRevision)
    {
        this.qualificationRevision = qualificationRevision;
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
