package com.xinyi.rongli.integration.service;
import com.xinyi.rongli.integration.config.IntegrationKeyProperties;
import com.xinyi.rongli.platform.error.*;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
@Service
public class IntegrationCrypto {
 private final IntegrationKeyProperties keys;private static final SecureRandom RANDOM=new SecureRandom();
 public IntegrationCrypto(IntegrationKeyProperties keys){this.keys=keys;}
 public static final class Sealed {
  public final byte[] ciphertext,nonce;public final String keyVersion,fingerprint;
  private Sealed(byte[] c,byte[] n,String k,String f){ciphertext=c;nonce=n;keyVersion=k;fingerprint=f;}
  @Override public String toString(){return "Sealed[REDACTED]";}
 }
 private byte[] key(String version){try{if(version==null||version.isBlank())throw unavailable();String encoded=keys.getVersions().get(version);if(encoded==null)throw unavailable();byte[] key=Base64.getDecoder().decode(encoded);if(key.length!=32)throw unavailable();return key;}catch(RuntimeException e){throw unavailable();}}
 public Sealed seal(String value,String aad){String version=keys.getCurrentVersion();byte[] key=key(version),plain=value.getBytes(StandardCharsets.UTF_8);try{byte[] nonce=new byte[12];RANDOM.nextBytes(nonce);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,nonce));cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(key,"HmacSHA256"));mac.update(("fingerprint:"+aad+":").getBytes(StandardCharsets.UTF_8));return new Sealed(cipher.doFinal(plain),nonce,version,HexFormat.of().formatHex(mac.doFinal(plain)));}catch(Exception e){throw unavailable();}finally{Arrays.fill(key,(byte)0);Arrays.fill(plain,(byte)0);}}
 public byte[] open(byte[] encrypted,byte[] nonce,String version,String aad){byte[] key=key(version);try{if(nonce==null||nonce.length!=12||encrypted==null||encrypted.length<17)throw unavailable();Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,nonce));cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));return cipher.doFinal(encrypted);}catch(Exception e){throw unavailable();}finally{Arrays.fill(key,(byte)0);}}
 public byte[] envelope(String value,String aad){var s=seal(value,aad);String json=com.xinyi.rongli.platform.json.ViJson.write(Map.of("v",s.keyVersion,"n",Base64.getEncoder().encodeToString(s.nonce),"c",Base64.getEncoder().encodeToString(s.ciphertext)));return json.getBytes(StandardCharsets.UTF_8);}
 public String unenvelope(byte[] envelope,String aad){try{var s=com.xinyi.rongli.platform.json.ViJson.tree(new String(envelope,StandardCharsets.UTF_8));byte[] plain=open(Base64.getDecoder().decode(s.path("c").asText()),Base64.getDecoder().decode(s.path("n").asText()),s.path("v").asText(),aad);try{return new String(plain,StandardCharsets.UTF_8);}finally{Arrays.fill(plain,(byte)0);}}catch(RuntimeException e){throw unavailable();}}
 private static ViException unavailable(){return new ViException(ViErrorCode.VI_SECRET_UNAVAILABLE);}
}
