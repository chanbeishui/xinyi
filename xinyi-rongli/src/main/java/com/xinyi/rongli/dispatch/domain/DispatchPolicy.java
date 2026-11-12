package com.xinyi.rongli.dispatch.domain;

/** 派单内部持久事实；HTTP仅使用专用白名单投影，不批量绑定实体。 */
public class DispatchPolicy {
    private Long ownerDeptId;
    private int inviteResponseSeconds;
    private int radiusMeter;
    private int expandedRadiusMeter;
    private int overallDeadlineSeconds;
    private String licenseCompatibility;
    private byte[] configHash;
    private Long updatedByUserId;
    private long version;

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    public int getInviteResponseSeconds() {
        return inviteResponseSeconds;
    }

    public void setInviteResponseSeconds(int value) {
        inviteResponseSeconds = value;
    }

    public int getRadiusMeter() {
        return radiusMeter;
    }

    public void setRadiusMeter(int value) {
        radiusMeter = value;
    }

    public int getExpandedRadiusMeter() {
        return expandedRadiusMeter;
    }

    public void setExpandedRadiusMeter(int value) {
        expandedRadiusMeter = value;
    }

    public int getOverallDeadlineSeconds() {
        return overallDeadlineSeconds;
    }

    public void setOverallDeadlineSeconds(int value) {
        overallDeadlineSeconds = value;
    }

    public String getLicenseCompatibility() {
        return licenseCompatibility;
    }

    public void setLicenseCompatibility(String value) {
        licenseCompatibility = value;
    }

    public byte[] getConfigHash() {
        return configHash;
    }

    public void setConfigHash(byte[] value) {
        configHash = value;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long value) {
        updatedByUserId = value;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        version = value;
    }
}
