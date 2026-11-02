package com.xinyi.rongli.integration.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class IntegrationRevision extends ViRecord {
 private Long profileId;
 public Long getProfileId(){return profileId;}
 public void setProfileId(Long value){profileId=value;}
 private long revisionNo;
 public long getRevisionNo(){return revisionNo;}
 public void setRevisionNo(long value){revisionNo=value;}
 private String configJson;
 public String getConfigJson(){return configJson;}
 public void setConfigJson(String value){configJson=value;}
 private String secretRefsJson;
 public String getSecretRefsJson(){return secretRefsJson;}
 public void setSecretRefsJson(String value){secretRefsJson=value;}
 private String contentHash;
 public String getContentHash(){return contentHash;}
 public void setContentHash(String value){contentHash=value;}
 private Long createdBy;
 public Long getCreatedBy(){return createdBy;}
 public void setCreatedBy(Long value){createdBy=value;}
 private String reason;
 public String getReason(){return reason;}
 public void setReason(String value){reason=value;}
 @Override public String toString(){return "IntegrationRevision[id="+getId()+"]";}
}
