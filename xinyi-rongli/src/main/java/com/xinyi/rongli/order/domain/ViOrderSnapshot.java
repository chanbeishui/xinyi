package com.xinyi.rongli.order.domain;
import java.time.Instant;
import com.xinyi.rongli.core.ViRecord;
public class ViOrderSnapshot extends ViRecord {
 private Long orderId;
 public Long getOrderId(){return orderId;}
 public void setOrderId(Long v){orderId=v;}
 private byte[] addressCipher;
 public byte[] getAddressCipher(){return addressCipher;}
 public void setAddressCipher(byte[] v){addressCipher=v;}
 private byte[] contactCipher;
 public byte[] getContactCipher(){return contactCipher;}
 public void setContactCipher(byte[] v){contactCipher=v;}
 private byte[] remarkCipher;
 public byte[] getRemarkCipher(){return remarkCipher;}
 public void setRemarkCipher(byte[] v){remarkCipher=v;}
 private String stationSnapshot;
 public String getStationSnapshot(){return stationSnapshot;}
 public void setStationSnapshot(String v){stationSnapshot=v;}
 private String agreementSnapshot;
 public String getAgreementSnapshot(){return agreementSnapshot;}
 public void setAgreementSnapshot(String v){agreementSnapshot=v;}
 private Instant acceptedAt;
 public Instant getAcceptedAt(){return acceptedAt;}
 public void setAcceptedAt(Instant v){acceptedAt=v;}
}
