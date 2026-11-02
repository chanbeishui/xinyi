package com.xinyi.rongli.order.service;

import com.xinyi.rongli.order.domain.*;
import com.xinyi.rongli.order.dto.*;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.booking.mapper.QuoteMapper;
import com.xinyi.rongli.booking.service.QuoteService;
import com.xinyi.rongli.booking.dto.QuoteView;
import com.xinyi.rongli.capacity.mapper.ViQuotaSlotMapper;
import com.xinyi.rongli.capacity.domain.ViQuotaSlot;
import com.xinyi.rongli.station.mapper.ViStationMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.address.service.AddressCrypto;
import com.xinyi.rongli.address.dto.GeoPoint;
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
public class OrderService {
 private static final String ACTION="POST /api/app/owner/orders";
 private final OrderProjection projection;private final OrderMapper mapper;private final QuoteMapper quoteMapper;private final QuoteService quotes;
 private final ViStationMapper stations;private final ViQuotaSlotMapper slots;private final PaymentPolicyGateway payments;
 private final EmployeeDirectoryGuardMapper directory;private final ViAccountMapper accounts;private final com.xinyi.rongli.identity.mapper.ViSessionMapper sessions;private final BusinessActorResolver actors;
 private final AddressCrypto crypto;private final PhoneCrypto phones;private final IdempotencyService idem;
 private final AuditService audit;private final OutboxService outbox;private final TransactionTemplate tx;
 public OrderService(OrderProjection projection,OrderMapper mapper,QuoteMapper quoteMapper,QuoteService quotes,ViStationMapper stations,ViQuotaSlotMapper slots,
 PaymentPolicyGateway payments,EmployeeDirectoryGuardMapper directory,ViAccountMapper accounts,com.xinyi.rongli.identity.mapper.ViSessionMapper sessions,BusinessActorResolver actors,
 AddressCrypto crypto,PhoneCrypto phones,IdempotencyService idem,AuditService audit,OutboxService outbox,PlatformTransactionManager manager){
  this.projection=projection;this.mapper=mapper;this.quoteMapper=quoteMapper;this.quotes=quotes;this.stations=stations;this.slots=slots;this.payments=payments;
  this.directory=directory;this.accounts=accounts;this.sessions=sessions;this.actors=actors;this.crypto=crypto;this.phones=phones;this.idem=idem;this.audit=audit;this.outbox=outbox;
  tx=new TransactionTemplate(manager);tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
 }
 public record Receipt(long orderId){}
 public OwnerOrderView create(OrderCreate input,String key){
  Actor actor=actors.requireMini();
  try {
   if(!"OWNER".equals(actor.actingRole()))throw new ViException(ViErrorCode.VI_FORBIDDEN);
   if(!input.acceptedTerms()||input.termsVersion()==null||input.privacyVersion()==null||input.termsVersion().length()>64||input.privacyVersion().length()>64||input.remark()!=null&&input.remark().length()>500)throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
   long quoteId=ViId.parse(input.quoteId());
   return tx.execute(status->{
    directory.ensureGuard();directory.lockGuard();quoteMapper.lockConfig();
    var account=accounts.selectByIdForUpdate(actor.accountId());
    if(account==null||!"ACTIVE".equals(account.getStatus()))throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
    // 等待业务锁期间身份可能已退出或切换，写入前以DB会话/安全代际再次线性化核验。
    if(actor.sessionId()==null)throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
    var session=sessions.selectByIdForUpdate(actor.sessionId());var security=mapper.lockSecurity(actor.accountId());
    if(session==null||security==null||!Objects.equals(session.getAccountId(),actor.accountId())||!"ACTIVE".equals(session.getStatus())||session.getRevokedAt()!=null||!session.getAbsoluteExpiresAt().isAfter(Instant.now())||!"ACTIVE".equals(security.getSecurityStatus())||!Objects.equals(session.getSessionEpoch(),security.getSessionEpoch()))throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
    if(!"OWNER".equals(session.getActiveRole())||!Objects.equals(session.getContextVersion(),actor.contextVersion()))throw new ViException(ViErrorCode.VI_CONTEXT_CHANGED);
    // 缓存只保存订单ID；完整地址、联系人、备注不得落入幂等结果明文。
    Receipt receipt=idem.execute(IdempotencyService.subjectKey(actor),ACTION,key,ViJson.write(input),true,Receipt.class,()->{
     var quote=mapper.lockQuote(quoteId);if(quote==null||!Objects.equals(quote.getOwnerAccountId(),actor.accountId()))throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
     var station=stations.selectByIdForUpdate(quote.getStationId());
     if(station==null)throw new ViException(ViErrorCode.VI_CONFIG_CHANGED);
     if(quote.getReceiveSlotId()==null)throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"报价缺少接收排期，请重新报价");
     List<Long> ids=OrderLocks.orderedIds(List.of(quote.getPickupSlotId(),quote.getReceiveSlotId()));
     if(ids.size()!=2)throw new ViException(ViErrorCode.VI_CONFIG_CHANGED);
     Map<Long,ViQuotaSlot> locked=new HashMap<>();for(Long id:ids){var slot=slots.lockById(id);if(slot==null||!Objects.equals(slot.getStationId(),station.getId()))throw new ViException(ViErrorCode.VI_CONFIG_CHANGED);locked.put(id,slot);}
     quotes.validateForOrder(actor.accountId(),input.quoteId());
     var detail=ViJson.tree(quote.getItemsJson());var policy=detail.path("policy");
     if(!input.termsVersion().equals(policy.path("terms").asText())||!input.privacyVersion().equals(policy.path("privacy").asText()))throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"协议版本已变化，请重新阅读并确认");
     if(account.getPhoneVerifiedAt()==null||account.getPhoneCipher()==null)throw new ViException(ViErrorCode.VI_PHONE_PROOF_REQUIRED);
     String phone=phones.decrypt(account.getPhoneCipher());if(phone==null||phone.isBlank())throw new ViException(ViErrorCode.VI_PHONE_PROOF_REQUIRED);
     PaymentPolicyGateway.Policy payment=payments.requireForStation(station.getId(),account.getAppId());
     Instant now=Instant.parse(mapper.now()),expires=now.plusSeconds(1800);var pickup=locked.get(quote.getPickupSlotId());
     if(!pickup.getStartsAt().isAfter(expires))throw new ViException(ViErrorCode.VI_SERVICE_UNAVAILABLE,"所选时段不足完整30分钟付款窗口，请重新选择");
     for(Long id:ids)one(mapper.hold(id,locked.get(id).getVersion()));
     var vehicle=ViJson.tree(quote.getVehicleSnapshot());
     ViOrder row=new ViOrder();row.setOrderNo("VI"+UUID.randomUUID().toString().replace("-","").toUpperCase(Locale.ROOT));
     row.setOwnerAccountId(actor.accountId());row.setVehicleId(vehicle.hasNonNull("sourceId")?vehicle.get("sourceId").asLong():null);
     row.setStationId(station.getId());row.setDeptId(station.getDeptId());row.setQuoteId(quoteId);row.setVehicleSnapshot(quote.getVehicleSnapshot());
     row.setFulfillmentStatus("PENDING_PAY");row.setCancelStatus("NONE");row.setCustodyType("OWNER");row.setCustodyRefId(actor.accountId());row.setPickupStartsAt(pickup.getStartsAt());row.setPickupEndsAt(pickup.getEndsAt());row.setPayExpiresAt(expires);row.setTotalFen(quote.getAmountFen());row.setTermsVersion(input.termsVersion());mapper.insert(row);
     var stationView=new OwnerOrderView.Station(station.getId().toString(),station.getName(),station.getAddress(),new GeoPoint(station.getLng().doubleValue(),station.getLat().doubleValue(),"GCJ02"),null);
     ViOrderSnapshot snapshot=new ViOrderSnapshot();snapshot.setOrderId(row.getId());snapshot.setAddressCipher(quote.getAddressSnapshotCipher());
     snapshot.setContactCipher(crypto.encrypt(ViJson.write(Map.of("displayName",account.getNickname()==null?"车主":account.getNickname(),"phone",phone,"verifiedAt",account.getPhoneVerifiedAt().toString()))));
     snapshot.setRemarkCipher(input.remark()==null?null:crypto.encrypt(input.remark()));snapshot.setStationSnapshot(ViJson.write(stationView));
     snapshot.setAgreementSnapshot(ViJson.write(Map.of("acceptedTerms",true,"termsVersion",input.termsVersion(),"privacyVersion",input.privacyVersion(),"termsText",policy.path("termsText").asText(),"privacyText",policy.path("privacyText").asText())));snapshot.setAcceptedAt(now);mapper.insertSnapshot(snapshot);
     ViPaymentOrder pay=new ViPaymentOrder();pay.setOrderId(row.getId());pay.setOutTradeNo("P"+row.getOrderNo());pay.setStatus("PENDING");pay.setTotalFen(row.getTotalFen());pay.setExpiresAt(expires);pay.setPaymentMethod("UNSELECTED");pay.setReceiverDeptId(payment.receiverDeptId());pay.setPolicyRevisionId(payment.revisionId());pay.setCapacityResolution("HELD");mapper.insertPayment(pay);
     for(Long id:ids)mapper.reserve(row.getId(),id,expires);one(mapper.consumeQuote(quoteId,row.getId(),quote.getVersion()));
     audit.recordSuccess(actor,"VI_ORDER",row.getId(),"ORDER_CREATE",null,Map.of("stationId",row.getStationId(),"deptId",row.getDeptId(),"quoteId",quoteId,"termsVersion",input.termsVersion(),"privacyVersion",input.privacyVersion(),"acceptedAt",now.toString()),null);
     outbox.publish("order:"+row.getId(),"VI_ORDER",row.getId(),"ORDER_CREATED",Map.of("orderId",row.getId(),"payExpiresAt",expires.toString()));return new Receipt(row.getId());
    });
    return view(actor.accountId(),receipt.orderId());
   });
  }catch(RuntimeException e){audit.recordFailure(actor,"VI_ORDER",0,"ORDER_CREATE_REJECTED",null,Map.of("errorCode",e instanceof ViException v?v.getErrorCode().name():"VI_INTERNAL_ERROR"),"建单未完成");throw e;}
 }
 private OwnerOrderView view(long owner,long id){
  ViOrder row=mapper.find(id);if(row==null||!Objects.equals(owner,row.getOwnerAccountId()))throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
  return projection.owner(row);
 }
 private static String nullable(com.fasterxml.jackson.databind.JsonNode node,String key){return node.hasNonNull(key)?node.get(key).asText():null;}
 private static void one(int n){if(n!=1)throw new ViException(ViErrorCode.VI_STATE_CONFLICT,"名额或报价已变化，请重新报价");}
}
