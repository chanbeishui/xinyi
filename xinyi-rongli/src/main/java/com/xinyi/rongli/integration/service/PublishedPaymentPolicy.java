package com.xinyi.rongli.integration.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.integration.mapper.PublishedConfigMapper;
import com.xinyi.rongli.order.service.PaymentPolicyGateway;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.pricing.service.PriceEnvironment;
import com.xinyi.rongli.core.ViId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

/** B03只读消费发布指针；草稿、开关或测试成功都不能自行发布支付能力。 */
@Service
public class PublishedPaymentPolicy implements PaymentPolicyGateway {
 private final PublishedConfigMapper mapper;private final String environment;private final com.xinyi.rongli.integration.mapper.IntegrationMapper configs;private final IntegrationSecrets credentials;
 public PublishedPaymentPolicy(PublishedConfigMapper mapper,com.xinyi.rongli.integration.mapper.IntegrationMapper configs,IntegrationSecrets credentials,@Value("${vi.environment:local}")String deployment,@Value("${vi.integration.environment:}")String explicit){this.mapper=mapper;this.configs=configs;this.credentials=credentials;environment=PriceEnvironment.resolve(deployment,explicit);}
 private record Published(long id,long owner,long revision,JsonNode config,JsonNode secrets){}
 public Policy requireForStation(long stationId,String appId){
  var ids=mapper.companyPolicies(stationId,environment);
  if(ids.size()!=1)throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"站点缺少唯一公司收款配置，请联系管理员");
  Published policy=published(ids.get(0),"PAYMENT_POLICY",null);var config=policy.config;
  if(!config.path("offlineEnabled").isBoolean()||!config.path("wechatEnabled").isBoolean())invalid();
  boolean offline=config.path("offlineEnabled").asBoolean()&&offline(policy),wechat=false;
  if(config.path("wechatEnabled").asBoolean()){
   try{wechat=wechat(policy,appId);}catch(ViException e){if(!offline)throw e;}
  }
  if(!offline&&!wechat)throw new ViException(ViErrorCode.VI_PAYMENT_METHOD_DISABLED,"公司暂未提供可用付款方式，未占用名额");
  return new Policy(policy.owner,policy.revision);
 }
 private boolean offline(Published p){
  JsonNode c=p.config;
  if(!"PREPAID_MANUAL_VERIFIED".equals(c.path("offlineCollectionMode").asText())||!text(c,"offlineReceiverName")||!text(c,"offlineInstructions")||!text(c,"offlineContactName")||!text(c,"offlineContactPhone"))return false;
  JsonNode methods=c.path("offlineMethods");if(!methods.isArray()||methods.isEmpty()||methods.size()>2)return false;
  Set<String> used=new HashSet<>();
  for(JsonNode method:methods){String m=method.asText();if(!used.add(m)||!Set.of("CASH","BANK_TRANSFER").contains(m))return false;
   if("BANK_TRANSFER".equals(m)&&(!text(c,"offlineBankName")||!text(c,"offlineAccountName")||!secret(p,"offlineBankAccount","BANK_ACCOUNT")))return false;
  }
  return true;
 }
 private boolean wechat(Published p,String appId){
  Published merchant=reference(p.config.path("wechatMerchant"),"WECHAT_PAY",p.owner);JsonNode c=merchant.config;
  if(!c.path("mchid").asText().matches("[0-9]{8,32}")||!text(c,"merchantName")||!text(c,"merchantCertificateSerial")||!c.path("bindingAcknowledged").asBoolean()||!secret(merchant,"merchantPrivateKey","PRIVATE_KEY")||!secret(merchant,"apiV3Key","API_V3_KEY"))invalid();
  String mode=c.path("verificationMode").asText();
  if("WECHATPAY_PUBLIC_KEY".equals(mode)){if(!text(c,"wechatPayPublicKeyId")||!text(c,"wechatPayPublicKeyPem"))invalid();}
  else if(!"PLATFORM_CERT".equals(mode)||!c.path("platformCertificates").isArray()||c.path("platformCertificates").isEmpty())invalid();
  Published app=reference(c.path("appProfile"),"WECHAT_APP",p.owner);
  if(!app.config.path("enabled").asBoolean()||!Objects.equals(appId,app.config.path("appId").asText())||!secret(app,"appSecret","APP_SECRET"))invalid();
  return true;
 }
 private Published reference(JsonNode reference,String kind,long owner){
  try{Published p=published(ViId.parse(reference.path("profileId").asText()),kind,owner);if(p.revision!=ViId.parse(reference.path("revisionId").asText()))invalid();return p;}
  catch(ViException e){if(e.getErrorCode()==ViErrorCode.VI_SECRET_UNAVAILABLE)throw e;throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"支付依赖配置未发布或不匹配");}catch(RuntimeException e){throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"支付依赖配置未发布或不匹配");}
 }
 private Published published(long id,String kind,Long owner){
  Map<String,Object> row=mapper.lockPublished(id);if(row==null||!kind.equals(row.get("kind"))||!environment.equals(row.get("environment")))invalid();
  long dept=((Number)row.get("owner_dept_id")).longValue(),revision=((Number)row.get("active_revision_id")).longValue();
  if(owner!=null&&owner!=dept||mapper.publicationValid(id,revision,(String)row.get("content_hash"))<1)invalid();
  return new Published(id,dept,revision,ViJson.tree((String)row.get("config_json")),ViJson.tree((String)row.get("secret_refs_json")));
 }
 private boolean secret(Published p,String slot,String kind){try{if(mapper.usableSecret(p.id,ViId.parse(p.secrets.path(slot).asText()),slot,kind)!=1)return false;byte[] plain=credentials.read(configs.profile(p.id),configs.revision(p.id,p.revision),slot);try{return plain.length>0;}finally{Arrays.fill(plain,(byte)0);}}catch(ViException e){if(e.getErrorCode()==ViErrorCode.VI_SECRET_UNAVAILABLE)throw e;return false;}catch(RuntimeException e){return false;}}
 private static boolean text(JsonNode n,String f){return n.path(f).isTextual()&&!n.path(f).asText().isBlank();}
 private static void invalid(){throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"公司支付配置或已发布依赖未完成校验");}
}
