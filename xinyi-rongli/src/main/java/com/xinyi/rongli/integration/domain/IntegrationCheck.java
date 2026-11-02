package com.xinyi.rongli.integration.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class IntegrationCheck extends ViRecord {
 private String targetType;
 public String getTargetType(){return targetType;}
 public void setTargetType(String value){targetType=value;}
 private Long targetId;
 public Long getTargetId(){return targetId;}
 public void setTargetId(Long value){targetId=value;}
 private Long revisionId;
 public Long getRevisionId(){return revisionId;}
 public void setRevisionId(Long value){revisionId=value;}
 private String contentHash;
 public String getContentHash(){return contentHash;}
 public void setContentHash(String value){contentHash=value;}
 private String checkLevel;
 public String getCheckLevel(){return checkLevel;}
 public void setCheckLevel(String value){checkLevel=value;}
 private String status;
 public String getStatus(){return status;}
 public void setStatus(String value){status=value;}
 private String issuesJson;
 public String getIssuesJson(){return issuesJson;}
 public void setIssuesJson(String value){issuesJson=value;}
 private Instant checkedAt;
 public Instant getCheckedAt(){return checkedAt;}
 public void setCheckedAt(Instant value){checkedAt=value;}
 private Instant expiresAt;
 public Instant getExpiresAt(){return expiresAt;}
 public void setExpiresAt(Instant value){expiresAt=value;}
 private Long checkedBy;
 public Long getCheckedBy(){return checkedBy;}
 public void setCheckedBy(Long value){checkedBy=value;}
 private boolean realPaymentVerified;
 public boolean getRealPaymentVerified(){return realPaymentVerified;}
 public void setRealPaymentVerified(boolean value){realPaymentVerified=value;}
 private Long targetVersion;
 public Long getTargetVersion(){return targetVersion;}
 public void setTargetVersion(Long value){targetVersion=value;}
 @Override public String toString(){return "IntegrationCheck[id="+getId()+"]";}
}
