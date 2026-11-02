package com.xinyi.rongli.integration.dto;
import java.util.List;
/** 五种配置的专用DTO；非秘密字段与只写秘密操作分离。 */
public final class IntegrationSettings {
 private IntegrationSettings() {}
 public record Reference(String profileId,String revisionId) {}
 public record Certificate(String serial,String pem) {}
 public sealed interface Config permits PaymentPolicy,WechatPay,TencentSms,AliyunSms,WechatApp {}
 public record PaymentPolicy(Boolean offlineEnabled,Boolean wechatEnabled,Reference wechatMerchant,String offlineInstructions,String offlineCollectionMode,String offlineReceiverName,String offlineBankName,List<String> offlineMethods,String offlineContactName,String offlineContactPhone,String offlineAccountName) implements Config {}
 public record WechatPay(String merchantName,String mchid,Reference appProfile,String merchantCertificateSerial,String verificationMode,String wechatPayPublicKeyId,String wechatPayPublicKeyPem,List<Certificate> platformCertificates,String merchantCertificatePem,Boolean bindingAcknowledged,String bindingEvidenceId) implements Config {}
 public record TencentSms(String secretId,String region,String smsSdkAppId,String apiVersion,String signName,Boolean enabled) implements Config {}
 public record AliyunSms(String accessKeyId,String region,String apiVersion,String signName,Boolean enabled) implements Config {}
 public record WechatApp(String appId,String appName,Boolean enabled) implements Config {}
 public record SecretMutation(String action,String value) {
  @Override public String toString(){return "SecretMutation["+action+", REDACTED]";}
 }
}
