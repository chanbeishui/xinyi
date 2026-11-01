package com.xinyi.rongli.pricing.domain;

import com.xinyi.rongli.core.ViRecord;
import java.time.Instant;

/** 持久化实体不接受HTTP批量绑定；已审核配置由Service状态机冻结。 */
public class ViPriceRule extends ViRecord {
    private Long ownerDeptId;
    private String companyName;
    private String environment;
    private String ruleKey;
    private int revision;
    private String vehicleTypeCode;
    private String inspectionTypeCode;
    private String status;
    private String configJson;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Long createdBy;
    private Long submittedBy;
    private Long approvedBy;
    private String configHash;
    private String approvedHash;
    private Instant publishedAt;
    private Long sourceRuleId;
    public Long getOwnerDeptId() { return ownerDeptId; }
    public void setOwnerDeptId(Long v) { ownerDeptId=v; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String v) { companyName=v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { environment=v; }
    public String getRuleKey() { return ruleKey; }
    public void setRuleKey(String v) { ruleKey=v; }
    public int getRevision() { return revision; }
    public void setRevision(int v) { revision=v; }
    public String getVehicleTypeCode() { return vehicleTypeCode; }
    public void setVehicleTypeCode(String v) { vehicleTypeCode=v; }
    public String getInspectionTypeCode() { return inspectionTypeCode; }
    public void setInspectionTypeCode(String v) { inspectionTypeCode=v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status=v; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String v) { configJson=v; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Instant v) { effectiveFrom=v; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Instant v) { effectiveTo=v; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long v) { createdBy=v; }
    public Long getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Long v) { submittedBy=v; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long v) { approvedBy=v; }
    public String getConfigHash() { return configHash; }
    public void setConfigHash(String v) { configHash=v; }
    public String getApprovedHash() { return approvedHash; }
    public void setApprovedHash(String v) { approvedHash=v; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant v) { publishedAt=v; }
    public Long getSourceRuleId() { return sourceRuleId; }
    public void setSourceRuleId(Long v) { sourceRuleId=v; }
}
