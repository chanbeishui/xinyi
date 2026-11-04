package com.xinyi.rongli.payment.service;
import com.xinyi.rongli.payment.domain.*;
import com.xinyi.rongli.payment.dto.*;
import com.xinyi.rongli.payment.mapper.*;
import com.xinyi.rongli.payment.gateway.*;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.identity.mapper.*;
import com.xinyi.rongli.integration.service.IntegrationCrypto;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.core.ViId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;
@Service
public class PaymentService {
 private final PaymentMapper mapper;private final OrderMapper orders;private final ViAccountMapper accounts;private final ViSessionMapper sessions;private final EmployeeDirectoryGuardMapper directory;
 private final BusinessActorResolver actors;private final PaymentConfiguration configuration;private final WechatPrepayGateway gateway;private final IntegrationCrypto crypto;private final IdempotencyService idem;private final AuditService audit;private final OutboxService outbox;private final TransactionTemplate tx;
 public PaymentService(PaymentMapper mapper,OrderMapper orders,ViAccountMapper accounts,ViSessionMapper sessions,EmployeeDirectoryGuardMapper directory,BusinessActorResolver actors,PaymentConfiguration configuration,WechatPrepayGateway gateway,IntegrationCrypto crypto,IdempotencyService idem,AuditService audit,OutboxService outbox,PlatformTransactionManager manager){this.mapper=mapper;this.orders=orders;this.accounts=accounts;this.sessions=sessions;this.directory=directory;this.actors=actors;this.configuration=configuration;this.gateway=gateway;this.crypto=crypto;this.idem=idem;this.audit=audit;this.outbox=outbox;tx=new TransactionTemplate(manager);tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);}
 public record Receipt(long paymentId){}
 private record Attempt(PaymentTransaction payment,String token){}
 private Actor actor(){var a=actors.requireMini();if(!"OWNER".equals(a.actingRole()))throw new ViException(ViErrorCode.VI_FORBIDDEN);return a;}
 private void guard(Actor a){directory.ensureGuard();directory.lockGuard();var account=accounts.selectByIdForUpdate(a.accountId());if(account==null||!"ACTIVE".equals(account.getStatus())||a.sessionId()==null)throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);var s=sessions.selectByIdForUpdate(a.sessionId());var security=orders.lockSecurity(a.accountId());if(s==null||security==null||!Objects.equals(s.getAccountId(),a.accountId())||!"ACTIVE".equals(s.getStatus())||s.getRevokedAt()!=null||!s.getAbsoluteExpiresAt().isAfter(Instant.now())||!"ACTIVE".equals(security.getSecurityStatus())||!Objects.equals(s.getSessionEpoch(),security.getSessionEpoch()))throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);if(!"OWNER".equals(s.getActiveRole())||!Objects.equals(s.getContextVersion(),a.contextVersion()))throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);}
 private ViOrder order(Actor a,String no){var o=mapper.lockOwnedOrder(no,a.accountId());if(o==null)throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);return o;}
 private boolean eligible(ViOrder o,PaymentTransaction p){return p!=null&&"PENDING_PAY".equals(o.getFulfillmentStatus())&&"NONE".equals(o.getCancelStatus())&&Set.of("PENDING","UNKNOWN").contains(p.getStatus())&&p.getPaidFen()==0&&"HELD".equals(p.getCapacityResolution())&&p.getExpiresAt().isAfter(Instant.parse(mapper.now()));}
 public Map<String,Object> options(String no){var a=actor();return tx.execute(s->{guard(a);var o=order(a,no);var p=mapper.lockPayment(o.getId());if(p==null)throw new ViException(ViErrorCode.VI_STATE_CONFLICT);var policy=configuration.policy(p);boolean allowed=eligible(o,p),wechat=allowed&&!"OFFLINE".equals(p.getPaymentMethod())&&(policy.config().path("wechatEnabled").asBoolean()||"READY".equals(p.getPrepayState()));var items=new ArrayList<Map<String,Object>>();items.add(Map.of("method","WECHAT","label","微信支付","available",wechat));items.add(configuration.offline(policy,allowed&&!"WECHAT".equals(p.getPaymentMethod())));return Map.of("orderNo",no,"paymentVersion",p.getVersion(),"policyRevisionId",policy.revision().getId().toString(),"options",items,"lockedMethod",p.getPaymentMethod(),"expiresAt",p.getExpiresAt().toString(),"newPaymentAllowed",allowed);});}
 public Map<String,Object> prepay(String no,PaymentIntent input,String key){var a=actor();try{
  Attempt attempt=tx.execute(s->{guard(a);var o=order(a,no);var p=mapper.lockPayment(o.getId());if(!eligible(o,p))throw new ViException(ViErrorCode.VI_STATE_CONFLICT,"订单当前不可发起付款，请刷新订单");var policy=configuration.policy(p);
   idem.execute(IdempotencyService.subjectKey(a),"POST /api/app/owner/orders/"+no+"/prepay",key,ViJson.write(input),true,Receipt.class,()->{
    if(p.getVersion()!=input.expectedVersion())throw new ViException(ViErrorCode.VI_STATE_CONFLICT);if(!Objects.equals(policy.revision().getId(),ViId.parse(input.expectedPolicyRevisionId())))throw new ViException(ViErrorCode.VI_CONFIG_CHANGED);
    if("OFFLINE".equals(p.getPaymentMethod()))throw new ViException(ViErrorCode.VI_STATE_CONFLICT,"订单已选择线下支付");
    if("UNSELECTED".equals(p.getPaymentMethod())){gateway.available();var account=accounts.selectByIdForUpdate(a.accountId());var merchant=configuration.newMerchant(policy,account.getAppId());p.setMerchantProfileId(merchant.getId());p.setMerchantRevisionId(merchant.getActiveRevisionId());p.setAppId(account.getAppId());p.setMchid(configuration.mchid(merchant));p.setPolicyRevisionId(policy.revision().getId());if(p.getOutTradeNo()==null||!p.getOutTradeNo().matches("[A-Za-z0-9_-]{1,32}")||p.getTotalFen()<1||p.getTotalFen()>Integer.MAX_VALUE)throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"订单收款信息需要核查");var request=new PrepaySnapshot(p.getAppId(),p.getMchid(),account.getOpenId(),p.getOutTradeNo(),p.getTotalFen(),p.getExpiresAt().toString(),gateway.notifyUrl());p.setPrepayRequestCipher(crypto.envelope(ViJson.write(request),aad(p,"request")));configuration.sdk(p);one(mapper.freeze(p));audit.recordSuccess(a,"VI_PAYMENT_ORDER",p.getId(),"PAYMENT_METHOD_LOCKED",null,Map.of("method","WECHAT","merchantProfileId",p.getMerchantProfileId(),"policyRevisionId",p.getPolicyRevisionId()),null);}
    return new Receipt(p.getId());
   });
   var refreshed=mapper.lockPayment(o.getId());if(!"WECHAT".equals(refreshed.getPaymentMethod()))throw new ViException(ViErrorCode.VI_STATE_CONFLICT);if("READY".equals(refreshed.getPrepayState()))return new Attempt(refreshed,null);
   if(!policy.config().path("wechatEnabled").asBoolean())throw new ViException(ViErrorCode.VI_PAYMENT_METHOD_DISABLED);gateway.available();configuration.sdk(refreshed);
   if("REQUESTING".equals(refreshed.getPrepayState())&&refreshed.getPrepayLeaseUntil()!=null&&refreshed.getPrepayLeaseUntil().isAfter(Instant.parse(mapper.now())))throw new ViException(ViErrorCode.VI_STATE_CONFLICT,"付款正在确认，请稍后刷新");
   String token=UUID.randomUUID().toString();one(mapper.claim(refreshed.getId(),refreshed.getVersion(),token));return new Attempt(mapper.lockPayment(o.getId()),token);
  });
  var p=attempt.payment();if(attempt.token()!=null){byte[] result=null;RuntimeException failure=null;try{var snapshot=ViJson.read(crypto.unenvelope(p.getPrepayRequestCipher(),aad(p,"request")),PrepaySnapshot.class);String id=gateway.prepay(configuration.sdk(p),snapshot);result=crypto.envelope(id,aad(p,"result"));}catch(RuntimeException e){failure=e;}final byte[] cipher=result;
   tx.executeWithoutResult(s->{directory.ensureGuard();directory.lockGuard();mapper.lockOrder(p.getOrderId());var current=mapper.lockPayment(p.getOrderId());if(mapper.finish(p.getId(),p.getVersion(),attempt.token(),cipher)==1){audit.recordSuccess(a,"VI_PAYMENT_ORDER",p.getId(),cipher==null?"PREPAY_UNKNOWN":"PREPAY_READY",null,Map.of("orderId",p.getOrderId()),null);outbox.publish("payment:"+p.getId(),"VI_PAYMENT_ORDER",p.getId(),cipher==null?"PREPAY_UNKNOWN":"PREPAY_READY",Map.of("orderId",p.getOrderId()));}});if(failure!=null)throw failure;
  }
  return tx.execute(s->{guard(a);var o=order(a,no);var current=mapper.lockPayment(o.getId());if(!eligible(o,current)||!"READY".equals(current.getPrepayState())||current.getPrepayResultCipher()==null)throw new ViException(ViErrorCode.VI_STATE_CONFLICT);return gateway.launch(configuration.sdk(current),current.getAppId(),crypto.unenvelope(current.getPrepayResultCipher(),aad(current,"result")),current.getExpiresAt().toString());});
 }catch(RuntimeException e){audit.recordFailure(a,"VI_PAYMENT_ORDER",0,"PREPAY_REJECTED",null,Map.of("errorCode",e instanceof ViException v?v.getErrorCode().name():"VI_INTERNAL_ERROR"),"预支付未完成");throw e;}}
 private static String aad(PaymentTransaction p,String slot){return "payment:"+p.getId()+":"+slot;}
 private static void one(int n){if(n!=1)throw new ViException(ViErrorCode.VI_STATE_CONFLICT);}
}
