package com.xinyi.rongli.booking.service;

import com.xinyi.rongli.booking.dto.*;
import com.xinyi.rongli.booking.domain.ViQuote;
import com.xinyi.rongli.booking.mapper.QuoteMapper;
import com.xinyi.rongli.booking.route.RouteGateway;
import com.xinyi.rongli.address.mapper.ViAddressMapper;
import com.xinyi.rongli.address.service.AddressCrypto;
import com.xinyi.rongli.address.dto.GeoPoint;
import com.xinyi.rongli.vehicle.mapper.ViVehicleMapper;
import com.xinyi.rongli.station.mapper.ViStationMapper;
import com.xinyi.rongli.station.domain.ViStation;
import com.xinyi.rongli.capacity.domain.ViQuotaSlot;
import com.xinyi.rongli.config.mapper.ConfigMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.pricing.service.PricingService;
import com.xinyi.rongli.pricing.dto.PriceTrial;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;

@Service
public class QuoteService {
 private static final String ACTION="POST /api/app/owner/orders/quote";
 private final QuoteMapper mapper;private final ViVehicleMapper vehicles;private final ViAddressMapper addresses;
 private final ViStationMapper stations;private final ConfigMapper config;private final AddressCrypto crypto;
 private final RouteGateway routes;private final PricingService pricing;private final BusinessActorResolver actors;
 private final EmployeeDirectoryGuardMapper directory;private final ViAccountMapper accounts;
 private final IdempotencyService idem;private final AuditService audit;private final OutboxService outbox;private final TransactionTemplate tx;
 public QuoteService(QuoteMapper mapper,ViVehicleMapper vehicles,ViAddressMapper addresses,ViStationMapper stations,ConfigMapper config,AddressCrypto crypto,RouteGateway routes,PricingService pricing,BusinessActorResolver actors,EmployeeDirectoryGuardMapper directory,ViAccountMapper accounts,IdempotencyService idem,AuditService audit,OutboxService outbox,PlatformTransactionManager manager){
  this.mapper=mapper;this.vehicles=vehicles;this.addresses=addresses;this.stations=stations;this.config=config;this.crypto=crypto;this.routes=routes;this.pricing=pricing;this.actors=actors;this.directory=directory;this.accounts=accounts;this.idem=idem;this.audit=audit;this.outbox=outbox;tx=new TransactionTemplate(manager);tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
 }
 public record Candidate(String stationId,String name,String address,long routeDistanceMeter,long estimatedDurationSecond){}
 private record Inputs(Map<String,Object> vehicle,Map<String,Object> address,GeoPoint point,String vehicleType){}
 private record Policy(long version,long handover,long buffer,long lead,long ttl,String terms,String termsText,String privacy,String privacyText){}
 private record Plan(ViStation station,ViQuotaSlot pickup,ViQuotaSlot receive,RouteGateway.Route route,PriceTrial price,Policy policy,Inputs inputs,String fingerprint){}
 public List<Candidate> candidates(QuoteInput input){
  Actor actor=owner();try{
   Inputs data=inputs(actor.accountId(),input);Policy policy=policy();ViQuotaSlot anchor=anchor(input,policy);
   List<Candidate> result=new ArrayList<>();
   for(Long id:mapper.stations()){
    ViStation station=stations.selectById(id);
    if(!basic(station,data))continue;
    var route=routes.driving(new RouteGateway.Point(data.point.lng(),data.point.lat()),new RouteGateway.Point(station.getLng().doubleValue(),station.getLat().doubleValue()));
    try {Plan p=plan(input,data,policy,anchor,station,route);result.add(new Candidate(id.toString(),station.getName(),station.getAddress(),route.distanceMeter(),route.durationSecond()));}
    catch(ViException e){if(e.getErrorCode()!=ViErrorCode.VI_SERVICE_UNAVAILABLE)throw e;}
   }
   return result;
  }catch(RuntimeException e){failure(actor,e);throw e;}
 }
 public QuoteView quote(QuoteInput input,String key){
  Actor actor=owner();try{
   String body=ViJson.write(input);var replay=idem.replayIfPresent(IdempotencyService.subjectKey(actor),ACTION,key,body,QuoteView.class);
   if(replay.isPresent()){validateForOrder(actor.accountId(),replay.get().quoteId());return replay.get();}
   if(input.stationId()==null)invalid("请选择可用站点");
   Inputs data=inputs(actor.accountId(),input);Policy policy=policy();ViQuotaSlot anchor=anchor(input,policy);
   ViStation station=stations.selectById(id(input.stationId()));if(!basic(station,data))unavailable();
   var route=routes.driving(new RouteGateway.Point(data.point.lng(),data.point.lat()),new RouteGateway.Point(station.getLng().doubleValue(),station.getLat().doubleValue()));
   Plan prepared=plan(input,data,policy,anchor,station,route);
   QuoteView saved=tx.execute(status->{
    directory.ensureGuard();directory.lockGuard();mapper.lockConfig();if(accounts.selectByIdForUpdate(actor.accountId())==null)throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
    return idem.execute(IdempotencyService.subjectKey(actor),ACTION,key,body,true,QuoteView.class,()->{
     Inputs current=inputs(actor.accountId(),input);Policy now=policy();ViStation actual=stations.selectByIdForUpdate(station.getId());
     Plan p=plan(input,current,now,anchor(input,now),actual,route);
     if(!prepared.fingerprint.equals(p.fingerprint))throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"输入、价格或容量配置已变化，请重新报价");
     List<QuoteView.FeeItem> fees=List.of(new QuoteView.FeeItem("INSPECTION","检测费",p.price.inspectionFee(),"固定费用"),new QuoteView.FeeItem("DRIVER_BASE","代驾起步费",p.price.driverBaseFee(),"包含"+p.price.includedDistanceMeter()+"米"),new QuoteView.FeeItem("DISTANCE","超里程费",p.price.distanceFee(),p.price.distanceRounding()+" · "+p.price.chargedUnits()));
     Instant expires=Instant.now().plusSeconds(policy.ttl);if(expires.isAfter(p.pickup.getStartsAt()))expires=p.pickup.getStartsAt();
     ViQuote row=new ViQuote();row.setOwnerAccountId(actor.accountId());row.setStationId(station.getId());row.setVehicleSnapshot(ViJson.write(data.vehicle));row.setAddressSnapshotCipher(crypto.encrypt(ViJson.write(data.address)));row.setPickupSlotId(p.pickup.getId());row.setReceiveSlotId(p.receive.getId());row.setRuleId(id(p.price.ruleId()));row.setRouteSnapshot(ViJson.write(route));row.setAmountFen(p.price.totalFen());row.setExpiresAt(expires);
     row.setItemsJson(ViJson.write(Map.of("items",fees,"price",p.price,"policy",policy,"ruleContent",ViJson.tree(mapper.ruleContent(id(p.price.ruleId()))),"fingerprint",p.fingerprint,"inputCipher",Base64.getEncoder().encodeToString(crypto.encrypt(body)))));
     mapper.insert(row);audit.recordSuccess(actor,"VI_QUOTE",row.getId(),"QUOTE_CREATE",null,Map.of("stationId",row.getStationId(),"amountFen",row.getAmountFen()),null);outbox.publish("quote:"+row.getId(),"VI_QUOTE",row.getId(),"QUOTE_CREATED",Map.of("quoteId",row.getId()));
     return new QuoteView(row.getId().toString(),expires.toString(),station.getId().toString(),station.getName(),input.slotId(),route.distanceMeter(),p.price.routeBasis(),fees,p.price.totalFen(),p.price.revision(),policy.terms,policy.version);
    });
   });
   validateForOrder(actor.accountId(),saved.quoteId());return saved;
  }catch(RuntimeException e){failure(actor,e);throw e;}
 }
 /** B03须在锁定报价及容量的建单事务中再次调用；此方法只校验，不消费报价。 */
 public ViQuote validateForOrder(long ownerId,String quoteId){
  ViQuote q=mapper.find(id(quoteId));if(q==null||!Objects.equals(q.getOwnerAccountId(),ownerId))throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
  if(q.getUsedOrderId()!=null||!q.getExpiresAt().isAfter(Instant.now()))throw new ViException(ViErrorCode.VI_STATE_CONFLICT,"报价已使用或过期，请重新报价");
  try {
  var saved=ViJson.tree(q.getItemsJson());QuoteInput input=ViJson.read(crypto.decrypt(Base64.getDecoder().decode(saved.path("inputCipher").asText())),QuoteInput.class);
  var data=inputs(ownerId,input);var policy=policy();var p=plan(input,data,policy,anchor(input,policy),stations.selectById(q.getStationId()),ViJson.read(q.getRouteSnapshot(),RouteGateway.Route.class));
  if(!saved.path("fingerprint").asText().equals(p.fingerprint))throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"报价对应的信息或配置已变化，请重新报价");return q;
  }catch(ViException e){if(Set.of(ViErrorCode.VI_SERVICE_UNAVAILABLE,ViErrorCode.VI_CONFIG_INVALID,ViErrorCode.VI_REQUEST_INVALID).contains(e.getErrorCode()))throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"报价条件已变化，请重新报价");throw e;}
 }
 private Plan plan(QuoteInput input,Inputs data,Policy policy,ViQuotaSlot anchor,ViStation station,RouteGateway.Route route){
  if(!basic(station,data)||route.distanceMeter()>station.getServiceRadiusMeter())unavailable();
  List<ViQuotaSlot> slots=mapper.slots(station.getId());var pickup=slots.stream().filter(q->"PICKUP".equals(q.getQuotaType())&&q.getStartsAt().equals(anchor.getStartsAt())&&q.getEndsAt().equals(anchor.getEndsAt())).toList();
  long travel=Math.addExact(Math.addExact(policy.handover,policy.buffer),route.durationSecond());
  Instant arrivalStart=anchor.getStartsAt().plusSeconds(travel),arrivalEnd=anchor.getEndsAt().plusSeconds(travel);
  var receive=slots.stream().filter(q->"STATION_RECEIVE".equals(q.getQuotaType())&&!q.getStartsAt().isAfter(arrivalStart)&&!q.getEndsAt().isBefore(arrivalEnd)).toList();
  if(pickup.size()!=1||receive.size()!=1)unavailable();
  PriceTrial price=pricing.quoteForStation(station.getId(),data.vehicleType,input.inspectionTypeCode(),anchor.getStartsAt(),route.distanceMeter());
  String fingerprint=Base64.getEncoder().encodeToString(idem.mac(ViJson.write(List.of(data,policy,station.getId(),station.getVersion(),pickup.get(0).getId(),pickup.get(0).getVersion(),receive.get(0).getId(),receive.get(0).getVersion(),price))));
  return new Plan(station,pickup.get(0),receive.get(0),route,price,policy,data,fingerprint);
 }
 private boolean basic(ViStation station,Inputs data){
  if(station==null||!Integer.valueOf(1).equals(station.getEnabled())||mapper.activeDepartment(station.getDeptId())!=1)return false;
  return ViJson.read(station.getSupportedVehicleCodes(),List.class).contains(data.vehicleType);
 }
 private ViQuotaSlot anchor(QuoteInput input,Policy policy){
  var q=mapper.slot(id(input.slotId()));if(q==null||q.getStationId()==null||!q.isEnabled()||!"PICKUP".equals(q.getQuotaType())||!q.getStartsAt().isAfter(Instant.now().plusSeconds(policy.lead)))unavailable();return q;
 }
 private Policy policy(){
  String terms=config.selectValue("vi.biz.terms.version");if(!"true".equals(config.selectValue("vi.biz.booking.enabled"))||terms==null||terms.isBlank()||config.selectValue("vi.biz.privacy.version")==null)throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"预约及协议尚未发布");
  return new Policy(config.selectVersion(),setting("handover.seconds",0,86400),setting("route-buffer.seconds",0,86400),setting("min-lead.seconds",0,604800),setting("quote-ttl.seconds",1,3600),terms,requiredConfig("vi.biz.terms.text"),requiredConfig("vi.biz.privacy.version"),requiredConfig("vi.biz.privacy.text"));
 }
 private String requiredConfig(String key){String v=config.selectValue(key);if(v==null||v.isBlank())throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"协议尚未发布");return v;}
 private long setting(String key,long min,long max){try{long n=Long.parseLong(config.selectValue("vi.biz.booking."+key));if(n<min||n>max)throw new IllegalArgumentException();return n;}catch(RuntimeException e){throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"预约排期参数尚未配置："+key);}}
 private Inputs inputs(long owner,QuoteInput input){
  if((input.vehicleId()==null)==(input.vehicleDraft()==null)||(input.addressId()==null)==(input.addressDraft()==null))invalid("已有资料与临时资料必须二选一");
  Map<String,Object> vehicle=new LinkedHashMap<>(),address=new LinkedHashMap<>();String type;
  if(input.vehicleId()!=null){var v=vehicles.selectById(id(input.vehicleId()));if(v==null||!Objects.equals(v.getOwnerAccountId(),owner)||v.getDeletedAt()!=null)invisible();vehicle.put("sourceId",v.getId());vehicle.put("sourceVersion",v.getVersion());vehicle.put("plateNo",v.getPlateNo());type=v.getVehicleTypeCode();}
  else{var v=input.vehicleDraft();vehicle.put("draft",v);if(v.plateNo()==null||v.plateNo().isBlank()||v.plateNo().length()>20)invalid("车牌必填且不超过20字符");vehicle.put("plateNo",v.plateNo().trim().toUpperCase(Locale.ROOT));type=v.vehicleTypeCode();}
  String label=mapper.dictLabel("vi_vehicle_type",type);if(label==null||mapper.dictLabel("vi_inspection_type",input.inspectionTypeCode())==null)invalid("车型或检测类型尚未启用");vehicle.put("vehicleTypeCode",type);vehicle.put("vehicleTypeLabel",label);
  GeoPoint point;String text;
  if(input.addressId()!=null){var a=addresses.selectById(id(input.addressId()));if(a==null||!Objects.equals(a.getOwnerAccountId(),owner)||a.getDeletedAt()!=null)invisible();point=ViJson.read(crypto.decrypt(a.getLocationCipher()),GeoPoint.class);text=crypto.decrypt(a.getAddressCipher());address.put("sourceId",a.getId());address.put("sourceVersion",a.getVersion());}
  else{point=input.addressDraft().location();text=input.addressDraft().address();}
  if(text==null||text.isBlank()||text.length()>255||point==null||!Double.isFinite(point.lng())||!Double.isFinite(point.lat())||Math.abs(point.lng())>180||Math.abs(point.lat())>90||!"GCJ02".equals(point.coordinateSystem()))invalid("请填写有效地址及GCJ02坐标");
  address.put("address",text);address.put("location",point);return new Inputs(vehicle,address,point,type);
 }
 private Actor owner(){Actor a=actors.requireMini();if(!"OWNER".equals(a.actingRole()))throw new ViException(ViErrorCode.VI_FORBIDDEN);return a;}
 private void failure(Actor actor,RuntimeException e){audit.recordFailure(actor,"VI_QUOTE",0,"QUOTE_REJECTED",null,Map.of("errorCode",e instanceof ViException v?v.getErrorCode().name():"VI_INTERNAL_ERROR"),"报价未完成");}
 private static long id(String text){try{return com.xinyi.rongli.core.ViId.parse(text);}catch(RuntimeException e){invalid("ID无效");return 0;}}
 private static void invalid(String msg){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,msg);}
 private static void invisible(){throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);}
 private static void unavailable(){throw new ViException(ViErrorCode.VI_SERVICE_UNAVAILABLE,"该地址、车型或时段暂无可用站点，请重新选择");}
}
