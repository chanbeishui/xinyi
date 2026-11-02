package com.xinyi.rongli.integration.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.integration.domain.*;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.error.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.RSAKey;
import java.security.cert.*;
@Service
public class IntegrationValidation {
 public record Issue(String field,String code,String message,String severity,String recovery) {}
 public record Result(List<Issue> issues,List<Map<String,String>> dependencies) {public boolean passed(){return issues.stream().noneMatch(i->"ERROR".equals(i.severity()));}}
 private final IntegrationMapper mapper;private final IntegrationSecrets secrets;
 public IntegrationValidation(IntegrationMapper mapper,IntegrationSecrets secrets){this.mapper=mapper;this.secrets=secrets;}
 private static void error(List<Issue> issues,String field,String code,String message){issues.add(new Issue(field,code,message,"ERROR","修正字段后保存新草稿并重新校验"));}
 private static void need(JsonNode c,List<Issue> issues,String...fields){for(String field:fields)if(!c.path(field).isTextual()||c.path(field).asText().isBlank())error(issues,"config."+field,"REQUIRED","请填写"+field);}
 private static byte[] pem(String value,String label){String head="-----BEGIN "+label+"-----",tail="-----END "+label+"-----";if(!value.startsWith(head)||!value.trim().endsWith(tail))throw new IllegalArgumentException();return Base64.getDecoder().decode(value.replace(head,"").replace(tail,"").replaceAll("\\s", ""));}
 private IntegrationProfile dependency(IntegrationProfile parent,JsonNode ref,String kind,List<Issue> issues,List<Map<String,String>> dependencies,String field){
  try{long id=com.xinyi.rongli.core.ViId.parse(ref.path("profileId").asText()),revision=com.xinyi.rongli.core.ViId.parse(ref.path("revisionId").asText());var p=mapper.profile(id);if(p==null||!kind.equals(p.getKind())||!parent.getOwnerDeptId().equals(p.getOwnerDeptId())||!parent.getEnvironment().equals(p.getEnvironment())||!"ACTIVE".equals(p.getLifecycle())||!Objects.equals(p.getActiveRevisionId(),revision)||mapper.wasPublished(id,revision)==0)throw new IllegalArgumentException();dependencies.add(Map.of("profileId",Long.toString(id),"revisionId",Long.toString(revision)));var nested=validate(p,mapper.revision(id,revision));issues.addAll(nested.issues());dependencies.addAll(nested.dependencies());return p;}catch(ViException e){if(e.getErrorCode()==ViErrorCode.VI_SECRET_UNAVAILABLE)throw e;error(issues,field,"DEPENDENCY_INVALID","依赖配置不可用，请重新核对已发布版本");return null;}catch(RuntimeException e){error(issues,field,"DEPENDENCY_INVALID","依赖配置须为同公司、同环境、同用途的当前发布版本");return null;}
 }
 public Result validate(IntegrationProfile p,IntegrationRevision r){var issues=new ArrayList<Issue>();var dependencies=new ArrayList<Map<String,String>>();if(r==null){error(issues,"revisionId","MISSING","请先保存草稿");return new Result(issues,dependencies);}var c=ViJson.tree(r.getConfigJson());Map<String,byte[]> values=new HashMap<>();
  try{
   for(var entry:secrets.refs(r).entrySet()){var secret=mapper.secret(p.getId(),entry.getValue(),entry.getKey());if(!secrets.usable(secret)){error(issues,"credentials."+entry.getKey(),"SECRET_REVOKED","凭据已撤销、过期或不可用");continue;}values.put(entry.getKey(),secrets.read(p,r,entry.getKey()));}
   switch(p.getKind()){
    case "PAYMENT_POLICY"->{
     if(c.path("offlineEnabled").asBoolean()){need(c,issues,"offlineReceiverName","offlineInstructions","offlineContactName","offlineContactPhone");if(c.path("offlineMethods").isEmpty())error(issues,"config.offlineMethods","REQUIRED","开启线下支付须至少选择一种收款方式");for(var method:c.path("offlineMethods"))if("BANK_TRANSFER".equals(method.asText())){need(c,issues,"offlineBankName","offlineAccountName");requiredSecret(values,issues,"offlineBankAccount");}}
     if(c.path("wechatEnabled").asBoolean())dependency(p,c.path("wechatMerchant"),"WECHAT_PAY",issues,dependencies,"config.wechatMerchant");
    }
    case "WECHAT_APP"->{if(c.path("enabled").asBoolean()){need(c,issues,"appId","appName");requiredSecret(values,issues,"appSecret");}}
    case "TENCENT_SMS"->{if(c.path("enabled").asBoolean()){need(c,issues,"secretId","region","smsSdkAppId","signName");requiredSecret(values,issues,"secretKey");}}
    case "ALIYUN_SMS"->{if(c.path("enabled").asBoolean()){need(c,issues,"accessKeyId","region","signName");requiredSecret(values,issues,"accessKeySecret");}}
    case "WECHAT_PAY"->{
     need(c,issues,"merchantName","mchid","merchantCertificateSerial","verificationMode");requiredSecret(values,issues,"merchantPrivateKey");requiredSecret(values,issues,"apiV3Key");if(!c.path("bindingAcknowledged").asBoolean())error(issues,"config.bindingAcknowledged","REQUIRED","请确认小程序与商户的关联关系");
     var app=dependency(p,c.path("appProfile"),"WECHAT_APP",issues,dependencies,"config.appProfile");if(app!=null&&!ViJson.tree(mapper.revision(app.getId(),app.getActiveRevisionId()).getConfigJson()).path("enabled").asBoolean())error(issues,"config.appProfile","APP_DISABLED","引用的小程序已停用");
     if(values.containsKey("apiV3Key")&&values.get("apiV3Key").length!=32)error(issues,"credentials.apiV3Key","LENGTH","APIv3密钥须为32字节");
     PrivateKey privateKey=null;if(values.containsKey("merchantPrivateKey"))try{privateKey=KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pem(new String(values.get("merchantPrivateKey"),StandardCharsets.UTF_8),"PRIVATE KEY")));if(((RSAKey)privateKey).getModulus().bitLength()<2048)throw new IllegalArgumentException();}catch(Exception e){error(issues,"credentials.merchantPrivateKey","FORMAT","商户签名私钥须为至少2048位RSA PKCS8 PEM");}
     if("WECHATPAY_PUBLIC_KEY".equals(c.path("verificationMode").asText())){need(c,issues,"wechatPayPublicKeyId","wechatPayPublicKeyPem");try{var key=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pem(c.path("wechatPayPublicKeyPem").asText(),"PUBLIC KEY")));if(((RSAKey)key).getModulus().bitLength()<2048)throw new IllegalArgumentException();}catch(Exception e){error(issues,"config.wechatPayPublicKeyPem","FORMAT","微信支付验签公钥PEM格式不正确");}}
     else if("PLATFORM_CERT".equals(c.path("verificationMode").asText())){if(c.path("platformCertificates").isEmpty())error(issues,"config.platformCertificates","REQUIRED","请配置可信平台证书及轮换材料");for(var cert:c.path("platformCertificates"))certificate(cert.path("pem").asText(),cert.path("serial").asText(),null,issues,"config.platformCertificates");}
     if(c.hasNonNull("merchantCertificatePem"))certificate(c.get("merchantCertificatePem").asText(),c.path("merchantCertificateSerial").asText(),privateKey,issues,"config.merchantCertificatePem");
    }
    default->error(issues,"kind","UNSUPPORTED","配置类型不支持");
   }
   issues.add(new Issue("provider","NOT_VERIFIED","本地校验不证明供应商资格、真实发信或支付成功","WARNING","完成授权的供应商验证"));
   return new Result(issues,dependencies.stream().distinct().sorted(Comparator.comparing(m->m.get("profileId"))).toList());
  }finally{values.values().forEach(v->Arrays.fill(v,(byte)0));}
 }
 private static void requiredSecret(Map<String,byte[]> values,List<Issue> issues,String slot){if(!values.containsKey(slot)||values.get(slot).length==0)error(issues,"credentials."+slot,"REQUIRED","请配置"+slot);}
 private static void certificate(String pem,String serial,PrivateKey key,List<Issue> issues,String field){try{var cert=(X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(new java.io.ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));cert.checkValidity();if(!cert.getSerialNumber().toString(16).equalsIgnoreCase(serial))throw new IllegalArgumentException();if(key!=null){byte[] challenge="integration-key-pair-check".getBytes(StandardCharsets.UTF_8);var sig=Signature.getInstance("SHA256withRSA");sig.initSign(key);sig.update(challenge);byte[] signed=sig.sign();sig.initVerify(cert);sig.update(challenge);if(!sig.verify(signed))throw new IllegalArgumentException();}}catch(Exception e){error(issues,field,"CERTIFICATE_INVALID","证书格式、有效期、序列号或密钥配对不正确");}}
}
