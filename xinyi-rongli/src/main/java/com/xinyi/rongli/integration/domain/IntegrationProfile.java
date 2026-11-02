package com.xinyi.rongli.integration.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class IntegrationProfile extends ViRecord {
 private String kind;
 public String getKind(){return kind;}
 public void setKind(String value){kind=value;}
 private Long ownerDeptId;
 public Long getOwnerDeptId(){return ownerDeptId;}
 public void setOwnerDeptId(Long value){ownerDeptId=value;}
 private String environment;
 public String getEnvironment(){return environment;}
 public void setEnvironment(String value){environment=value;}
 private String configKey;
 public String getConfigKey(){return configKey;}
 public void setConfigKey(String value){configKey=value;}
 private String displayName;
 public String getDisplayName(){return displayName;}
 public void setDisplayName(String value){displayName=value;}
 private String lifecycle;
 public String getLifecycle(){return lifecycle;}
 public void setLifecycle(String value){lifecycle=value;}
 private Long activeRevisionId;
 public Long getActiveRevisionId(){return activeRevisionId;}
 public void setActiveRevisionId(Long value){activeRevisionId=value;}
 private Long draftRevisionId;
 public Long getDraftRevisionId(){return draftRevisionId;}
 public void setDraftRevisionId(Long value){draftRevisionId=value;}
 private long revisionSeq;
 public long getRevisionSeq(){return revisionSeq;}
 public void setRevisionSeq(long value){revisionSeq=value;}
 private Long createdBy;
 public Long getCreatedBy(){return createdBy;}
 public void setCreatedBy(Long value){createdBy=value;}
 private Long lastPublishedBy;
 public Long getLastPublishedBy(){return lastPublishedBy;}
 public void setLastPublishedBy(Long value){lastPublishedBy=value;}
 @Override public String toString(){return "IntegrationProfile[id="+getId()+"]";}
}
