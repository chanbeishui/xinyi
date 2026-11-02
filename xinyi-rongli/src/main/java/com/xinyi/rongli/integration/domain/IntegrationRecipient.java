package com.xinyi.rongli.integration.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class IntegrationRecipient extends ViRecord {
 private String name;
 public String getName(){return name;}
 public void setName(String value){name=value;}
 private String kind;
 public String getKind(){return kind;}
 public void setKind(String value){kind=value;}
 private Long ownerDeptId;
 public Long getOwnerDeptId(){return ownerDeptId;}
 public void setOwnerDeptId(Long value){ownerDeptId=value;}
 private String environment;
 public String getEnvironment(){return environment;}
 public void setEnvironment(String value){environment=value;}
 private byte[] phoneCipher;
 public byte[] getPhoneCipher(){return phoneCipher;}
 public void setPhoneCipher(byte[] value){phoneCipher=value;}
 private Long accountId;
 public Long getAccountId(){return accountId;}
 public void setAccountId(Long value){accountId=value;}
 private Long appProfileId;
 public Long getAppProfileId(){return appProfileId;}
 public void setAppProfileId(Long value){appProfileId=value;}
 private boolean enabled;
 public boolean getEnabled(){return enabled;}
 public void setEnabled(boolean value){enabled=value;}
 private Long createdBy;
 public Long getCreatedBy(){return createdBy;}
 public void setCreatedBy(Long value){createdBy=value;}
 private String reason;
 public String getReason(){return reason;}
 public void setReason(String value){reason=value;}
 @Override public String toString(){return "IntegrationRecipient[id="+getId()+"]";}
}
