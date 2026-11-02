package com.xinyi.rongli.integration.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class IntegrationSecret extends ViRecord {
 private Long profileId;
 public Long getProfileId(){return profileId;}
 public void setProfileId(Long value){profileId=value;}
 private String slotName;
 public String getSlotName(){return slotName;}
 public void setSlotName(String value){slotName=value;}
 private String credentialKind;
 public String getCredentialKind(){return credentialKind;}
 public void setCredentialKind(String value){credentialKind=value;}
 private byte[] ciphertext;
 public byte[] getCiphertext(){return ciphertext;}
 public void setCiphertext(byte[] value){ciphertext=value;}
 private byte[] nonce;
 public byte[] getNonce(){return nonce;}
 public void setNonce(byte[] value){nonce=value;}
 private String keyVersion;
 public String getKeyVersion(){return keyVersion;}
 public void setKeyVersion(String value){keyVersion=value;}
 private String aadContext;
 public String getAadContext(){return aadContext;}
 public void setAadContext(String value){aadContext=value;}
 private String fingerprint;
 public String getFingerprint(){return fingerprint;}
 public void setFingerprint(String value){fingerprint=value;}
 private String lifecycle;
 public String getLifecycle(){return lifecycle;}
 public void setLifecycle(String value){lifecycle=value;}
 private Instant expiresAt;
 public Instant getExpiresAt(){return expiresAt;}
 public void setExpiresAt(Instant value){expiresAt=value;}
 private Long createdBy;
 public Long getCreatedBy(){return createdBy;}
 public void setCreatedBy(Long value){createdBy=value;}
 @Override public String toString(){return "IntegrationSecret[id="+getId()+"]";}
}
