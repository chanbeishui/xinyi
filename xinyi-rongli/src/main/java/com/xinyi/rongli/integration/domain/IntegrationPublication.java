package com.xinyi.rongli.integration.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class IntegrationPublication extends ViRecord {
 private String targetType;
 public String getTargetType(){return targetType;}
 public void setTargetType(String value){targetType=value;}
 private Long targetId;
 public Long getTargetId(){return targetId;}
 public void setTargetId(Long value){targetId=value;}
 private Long revisionId;
 public Long getRevisionId(){return revisionId;}
 public void setRevisionId(Long value){revisionId=value;}
 private Long previousRevisionId;
 public Long getPreviousRevisionId(){return previousRevisionId;}
 public void setPreviousRevisionId(Long value){previousRevisionId=value;}
 private Long checkId;
 public Long getCheckId(){return checkId;}
 public void setCheckId(Long value){checkId=value;}
 private String action;
 public String getAction(){return action;}
 public void setAction(String value){action=value;}
 private Long publishedBy;
 public Long getPublishedBy(){return publishedBy;}
 public void setPublishedBy(Long value){publishedBy=value;}
 private Instant publishedAt;
 public Instant getPublishedAt(){return publishedAt;}
 public void setPublishedAt(Instant value){publishedAt=value;}
 private String reason;
 public String getReason(){return reason;}
 public void setReason(String value){reason=value;}
 private String dependencyVersions;
 public String getDependencyVersions(){return dependencyVersions;}
 public void setDependencyVersions(String value){dependencyVersions=value;}
 private long targetVersion;
 public long getTargetVersion(){return targetVersion;}
 public void setTargetVersion(long value){targetVersion=value;}
 @Override public String toString(){return "IntegrationPublication[id="+getId()+"]";}
}
