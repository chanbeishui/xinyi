package com.xinyi.rongli.pricing.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.pricing.domain.ViPriceRule;
import com.xinyi.rongli.pricing.dto.*;
import com.xinyi.rongli.pricing.mapper.ViPriceRuleMapper;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

/** 公司统一计价，版本审核及正式匹配共用持久化规则，不创建订单或调用支付。 */
@Service
public class PricingService {
    private static final Set<String> STATUSES=Set.of("DRAFT","IN_REVIEW","APPROVED","PUBLISHED","DISABLED");
    private final ViPriceRuleMapper mapper;
    private final BusinessActorResolver actors;
    private final IDeptScopeService scopeService;
    private final EmployeeDirectoryGuardMapper directory;
    private final IdempotencyService idempotency;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;
    private final String environment;

    public PricingService(ViPriceRuleMapper mapper, BusinessActorResolver actors, IDeptScopeService scopeService,
            EmployeeDirectoryGuardMapper directory, IdempotencyService idempotency, AuditService audit,
            OutboxService outbox, PlatformTransactionManager manager,
            @Value("${vi.environment:local}") String deployment,
            @Value("${vi.pricing.environment:}") String explicitEnvironment) {
        this.mapper=mapper; this.actors=actors; this.scopeService=scopeService; this.directory=directory;
        this.idempotency=idempotency; this.audit=audit; this.outbox=outbox;
        this.environment=PriceEnvironment.resolve(deployment,explicitEnvironment);
        this.tx=new TransactionTemplate(manager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Page(List<PriceView> rows,long total,String environment) { }
    public Page list(PriceQuery query) {
        actors.requireAdmin();
        if (query.environment()!=null) requireEnvironment(query.environment());
        if (query.status()!=null && !STATUSES.contains(query.status())) invalid("规则状态无效");
        Set<Long> scope=scope("biz:price:list");
        int offset=Math.toIntExact(Math.min(Integer.MAX_VALUE,(long)(query.pageNum()-1)*query.pageSize()));
        return new Page(mapper.selectList(query,environment,scope,offset).stream().map(this::view).toList(),
                mapper.countList(query,environment,scope),environment);
    }

    public PriceView create(PriceWrite input,String key) {
        Actor actor=actors.requireAdmin();
        try {
            PriceContent config=canonical(input.config());
            requireEnvironment(input.environment());
            return tx.execute(status->{
                lockDirectory();
                Long owner=company(id(input.ownerDeptId()),scope("biz:price:create"));
                Long source=input.sourceRuleId()==null?null:id(input.sourceRuleId());
                String ruleKey=UUID.randomUUID().toString();
                int revision=1;
                if (source!=null) {
                    ViPriceRule old=accessible(source,scope("biz:price:create"),true);
                    if (!Set.of("PUBLISHED","DISABLED").contains(old.getStatus())) state("仅可从已发布或停用版本派生草稿");
                    if (!Objects.equals(old.getOwnerDeptId(),owner) || !old.getEnvironment().equals(input.environment())
                            || !old.getVehicleTypeCode().equals(config.vehicleTypeCode())
                            || !old.getInspectionTypeCode().equals(config.inspectionTypeCode())) state("新修订不能改变公司、环境或方案匹配条件");
                    ruleKey=old.getRuleKey();
                    revision=Math.addExact(mapper.maxRevision(ruleKey),1);
                }
                String stableKey=ruleKey; int nextRevision=revision;
                return idempotency.execute(IdempotencyService.subjectKey(actor),"POST /api/admin/prices",key,
                        ViJson.write(input),true,PriceView.class,()->{
                    validateDictionary(config);
                    ViPriceRule row=new ViPriceRule();row.setOwnerDeptId(owner);row.setEnvironment(environment);
                    row.setRuleKey(stableKey);row.setRevision(nextRevision);row.setCreatedBy(actor.sysUserId());
                    row.setSourceRuleId(source);row.setVehicleTypeCode(config.vehicleTypeCode());
                    row.setInspectionTypeCode(config.inspectionTypeCode());row.setStatus("DRAFT");
                    setConfig(row,config);mapper.insert(row);
                    success(actor,row,"PRICE_CREATE",null,"创建公司计价草稿");
                    return view(mapper.selectById(row.getId()));
                });
            });
        } catch (RuntimeException error) { rejected(actor,0,error);throw error; }
    }

    public PriceView update(long id,PriceWrite input,String key) {
        if (input.expectedVersion()==null) invalid("expectedVersion必填");
        return mutate(id,"biz:price:edit","PUT /api/admin/prices/"+id,input,input.expectedVersion(),key,(row,actor)->{
            PriceContent config=canonical(input.config());requireEnvironment(input.environment());
            company(PricingService.id(input.ownerDeptId()),scope("biz:price:edit"));
            if (!row.getOwnerDeptId().equals(PricingService.id(input.ownerDeptId()))
                    || !row.getEnvironment().equals(input.environment())
                    || !row.getVehicleTypeCode().equals(config.vehicleTypeCode())
                    || !row.getInspectionTypeCode().equals(config.inspectionTypeCode()) || input.sourceRuleId()!=null)
                state("公司、环境与方案匹配条件固定，需要变更请新建方案");
            requireState(row,"DRAFT");
            validateDictionary(config);
            Map<String,Object> before=summary(row);
            setConfig(row,config);
            row.setApprovedBy(null);row.setApprovedHash(null);row.setPublishedAt(null);
            one(mapper.updateDraft(row,input.expectedVersion()));
            success(actor,row,"PRICE_UPDATE",before,"修改计价草稿");
        });
    }

    public PriceView submit(long id,PriceAction input,String key) {
        reason(input.reason());
        return mutate(id,"biz:price:submit","POST /api/admin/prices/"+id+"/submit",input,input.expectedVersion(),key,(row,actor)->{
            requireState(row,"DRAFT");PriceContent config=integrity(row);validateDictionary(config);requireInterval(row);
            Map<String,Object> before=summary(row);row.setStatus("IN_REVIEW");row.setSubmittedBy(actor.sysUserId());
            row.setApprovedBy(null);row.setApprovedHash(null);row.setPublishedAt(null);
            one(mapper.transition(row,input.expectedVersion()));success(actor,row,"PRICE_SUBMIT",before,input.reason());
        });
    }

    public PriceView review(long id,PriceAction input,String key) {
        reason(input.reason());
        if (!Set.of("APPROVE","REJECT").contains(input.decision()==null?"":input.decision())) invalid("审核决定无效");
        return mutate(id,"biz:price:audit","POST /api/admin/prices/"+id+"/audit",input,input.expectedVersion(),key,(row,actor)->{
            requireState(row,"IN_REVIEW");integrity(row);
            if (Objects.equals(actor.sysUserId(),row.getSubmittedBy()) || Objects.equals(actor.sysUserId(),row.getCreatedBy())
                    || mapper.countContribution(id,actor.sysUserId())>0)
                throw new ViException(ViErrorCode.VI_SELF_REVIEW_DENIED,"提交人及草稿编辑参与人不能审核该版本");
            Map<String,Object> before=summary(row);
            if ("APPROVE".equals(input.decision())) {
                validateDictionary(integrity(row));requireInterval(row);row.setStatus("APPROVED");
                row.setApprovedBy(actor.sysUserId());row.setApprovedHash(row.getConfigHash());
            } else {row.setStatus("DRAFT");row.setApprovedBy(null);row.setApprovedHash(null);}
            one(mapper.transition(row,input.expectedVersion()));
            success(actor,row,"APPROVE".equals(input.decision())?"PRICE_AUDIT_APPROVE":"PRICE_AUDIT_REJECT",before,input.reason());
        });
    }

    public PriceView publish(long id,PriceAction input,String key) {
        reason(input.reason());
        if ((input.replacesRuleId()==null)!=(input.replacesExpectedVersion()==null)) invalid("替换规则ID和版本必须同时填写");
        return mutate(id,"biz:price:publish","POST /api/admin/prices/"+id+"/publish",input,input.expectedVersion(),key,(row,actor)->{
            requireState(row,"APPROVED");validateDictionary(integrity(row));requireInterval(row);approved(row);
            Long replaces=input.replacesRuleId()==null?null:PricingService.id(input.replacesRuleId());
            ViPriceRule old=null;
            if (replaces!=null) {
                old=accessible(replaces,scope("biz:price:publish"),true);requireState(old,"PUBLISHED");
                if (replaces==id || !Objects.equals(old.getOwnerDeptId(),row.getOwnerDeptId())
                        || !old.getEnvironment().equals(row.getEnvironment())
                        || !old.getVehicleTypeCode().equals(row.getVehicleTypeCode())
                        || !old.getInspectionTypeCode().equals(row.getInspectionTypeCode())) state("只能显式替换同公司同条件的价格");
                if (old.getVersion()!=input.replacesExpectedVersion()) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
                if (row.getEffectiveFrom().isAfter(Instant.now())) state("未来版本不能提前停用当前价格；请配置不重叠时段或到生效时替换");
            }
            if (mapper.countOverlap(row,replaces)>0) state("存在相同条件的重叠生效版本，请核实冲突或明确替换对象");
            if (old!=null) {
                Map<String,Object> before=summary(old);old.setStatus("DISABLED");
                one(mapper.transition(old,input.replacesExpectedVersion()));
                success(actor,old,"PRICE_DISABLE",before,"被新版本显式替换："+input.reason());
            }
            Map<String,Object> before=summary(row);row.setStatus("PUBLISHED");row.setPublishedAt(Instant.now());
            one(mapper.transition(row,input.expectedVersion()));success(actor,row,"PRICE_PUBLISH",before,input.reason());
        });
    }

    public PriceView disable(long id,PriceAction input,String key) {
        reason(input.reason());
        return mutate(id,"biz:price:publish","POST /api/admin/prices/"+id+"/disable",input,input.expectedVersion(),key,(row,actor)->{
            if (!Set.of("PUBLISHED","APPROVED").contains(row.getStatus())) state("仅已发布或已批准版本可停用");
            Map<String,Object> before=summary(row);row.setStatus("DISABLED");
            one(mapper.transition(row,input.expectedVersion()));success(actor,row,"PRICE_DISABLE",before,input.reason());
        });
    }

    public PriceTrial trial(long id,long distance) {
        Actor actor=actors.requireAdmin();
        try {
            ViPriceRule row=accessible(id,scope("biz:price:list"),false);
            return trialResult(row,integrity(row),distance,false);
        } catch (RuntimeException error) {rejected(actor,id,error);throw error;}
    }

    public PriceTrial matchTrial(long stationId,String vehicle,String inspection,Instant at,long distance) {
        Actor actor=actors.requireAdmin();
        try {
            if (mapper.countStationScope(stationId,scope("biz:price:list"))==0) invisible();
            PriceTrial result=quoteForStation(stationId,vehicle,inspection,at,distance);
            accessible(PricingService.id(result.ruleId()),scope("biz:price:list"),false);
            return result;
        } catch (ViException error) {rejected(actor,0,error);throw error;}
    }

    /** 报价/建单调用的唯一正式计价入口；站点ID必须由上游从真实记录解析，不能用车型计数替代。 */
    public PriceTrial quoteForStation(long stationId,String vehicle,String inspection,Instant at,long distance) {
        if (at==null || mapper.countStationScope(stationId,null)==0) unavailable();
        if (mapper.countDict("vi_vehicle_type",vehicle)!=1 || mapper.countDict("vi_inspection_type",inspection)!=1) unavailable();
        List<ViPriceRule> candidates=mapper.findPublished(stationId,vehicle,inspection,environment,at);
        if (candidates.isEmpty()) unavailable();
        if (candidates.size()!=1) state("命中多个价格版本，暂停报价并联系管理员处理");
        ViPriceRule row=candidates.get(0);PriceContent config=integrity(row);approved(row);
        return trialResult(row,config,distance,true);
    }

    private PriceView mutate(long id,String permission,String action,Object input,long expected,String key,
                             BiConsumer<ViPriceRule,Actor> change) {
        Actor actor=actors.requireAdmin();
        try {
            if (expected<0) invalid("版本无效");
            return tx.execute(status->{
                lockDirectory();
                Set<Long> scope=scope(permission);
                ViPriceRule initial=accessible(id,scope,false);
                company(initial.getOwnerDeptId(),scope);
                ViPriceRule row=accessible(id,scope,true);
                return idempotency.execute(IdempotencyService.subjectKey(actor),action,key,ViJson.write(input),true,
                        PriceView.class,()->{
                    if (row.getVersion()!=expected) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
                    change.accept(row,actor);
                    return view(mapper.selectById(id));
                });
            });
        } catch (RuntimeException error) {rejected(actor,id,error);throw error;}
    }

    private PriceTrial trialResult(ViPriceRule row,PriceContent config,long distance,boolean matched) {
        PriceCalculator.Amount amount=PriceCalculator.calculate(config,distance);
        return new PriceTrial(ViId.external(row.getId()),row.getRevision(),row.getVersion(),ViId.external(row.getOwnerDeptId()),
                row.getEnvironment(),matched,config.routeBasis(),config.distanceRounding(),distance,amount.distance(),
                config.includedDistanceMeter(),amount.excess(),amount.units(),config.inspectionFee(),config.driverBaseFee(),
                amount.distanceFee(),amount.total(),row.getConfigHash());
    }

    private ViPriceRule accessible(long id,Set<Long> scope,boolean lock) {
        ViPriceRule row=lock?mapper.lockById(id):mapper.selectById(id);
        if (row==null || row.getOwnerDeptId()==null || row.getCreatedBy()==null || row.getConfigHash()==null
                || !environment.equals(row.getEnvironment()) || (scope!=null && !scope.contains(row.getOwnerDeptId()))) invisible();
        return row;
    }
    private Long company(long id,Set<Long> scope) {
        if (scope!=null && !scope.contains(id)) throw new ViException(ViErrorCode.VI_FORBIDDEN,"无权维护该公司计价");
        Map<String,Object> dept=mapper.lockCompany(id);
        if (dept==null || !"0".equals(dept.get("status")) || !"0".equals(dept.get("del_flag")))
            throw new ViException(ViErrorCode.VI_FORBIDDEN,"公司部门不存在或已停用");
        return ((Number)dept.get("dept_id")).longValue();
    }
    private Set<Long> scope(String permission) {
        LoginUser user=SecurityUtils.getLoginUser();
        if (user==null || (!SecurityUtils.isAdmin() && (user.getPermissions()==null || !user.getPermissions().contains(permission))))
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        DeptScopeResult scope=scopeService.resolve(user,permission);return scope.isAllData()?null:scope.getDeptIds();
    }
    private void lockDirectory() {directory.ensureGuard();directory.lockGuard();}
    private void validateDictionary(PriceContent config) {
        if (mapper.countDict("vi_vehicle_type",config.vehicleTypeCode())!=1
                || mapper.countDict("vi_inspection_type",config.inspectionTypeCode())!=1)
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"车型或检测类型不存在、重复或已停用");
    }
    private void requireEnvironment(String value) {
        if (!environment.equals(value)) throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"当前部署只允许维护"+environment+"计价规则");
    }
    private static void requireState(ViPriceRule row,String expected) {
        if (!expected.equals(row.getStatus())) state("当前规则状态不允许此操作");
    }
    private static void requireInterval(ViPriceRule row) {
        if (row.getEffectiveFrom()==null || (row.getEffectiveTo()!=null && !row.getEffectiveTo().isAfter(Instant.now())))
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"请配置有效的生效区间，结束时间必须晚于当前时刻");
    }
    private void approved(ViPriceRule row) {
        if (row.getApprovedBy()==null || row.getSubmittedBy()==null || row.getCreatedBy()==null
                || !Objects.equals(row.getConfigHash(),row.getApprovedHash())
                || Objects.equals(row.getApprovedBy(),row.getCreatedBy()) || Objects.equals(row.getApprovedBy(),row.getSubmittedBy())
                || mapper.countContribution(row.getId(),row.getApprovedBy())>0)
            throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"审核绑定缺失或已失效，请重新独立审核");
    }
    private PriceContent integrity(ViPriceRule row) {
        try {
            PriceContent content=canonical(ViJson.read(row.getConfigJson(),PriceContent.class));
            if (row.getCreatedBy()==null || !Objects.equals(row.getConfigHash(),hash(row.getOwnerDeptId(),row.getEnvironment(),content))
                    || !Objects.equals(row.getVehicleTypeCode(),content.vehicleTypeCode())
                    || !Objects.equals(row.getInspectionTypeCode(),content.inspectionTypeCode())
                    || !Objects.equals(row.getEffectiveFrom(),instant(content.effectiveFrom()))
                    || !Objects.equals(row.getEffectiveTo(),instant(content.effectiveTo()))) throw new IllegalArgumentException();
            return content;
        } catch (RuntimeException error) {
            throw new ViException(ViErrorCode.VI_CONFIG_CHANGED,"已保存计价内容与审核摘要不一致，请核实；不能继续试算或发布");
        }
    }
    private PriceView view(ViPriceRule row) {
        PriceContent config=null;
        try {config=integrity(row);} catch (ViException ignored) { /* 后台保留异常记录入口，停用异常版本仍可返回真实状态。 */ }
        return new PriceView(ViId.external(row.getId()),ViId.external(row.getOwnerDeptId()),row.getCompanyName(),row.getEnvironment(),
                row.getRuleKey(),row.getRevision(),row.getStatus(),config,row.getVersion(),external(row.getSourceRuleId()),
                external(row.getCreatedBy()),external(row.getSubmittedBy()),external(row.getApprovedBy()),row.getConfigHash(),
                row.getApprovedHash(),row.getPublishedAt()==null?null:row.getPublishedAt().toString());
    }
    private static String external(Long id) {return id==null?null:ViId.external(id);}
    private static void setConfig(ViPriceRule row,PriceContent config) {
        row.setConfigJson(ViJson.write(config));row.setConfigHash(hash(row.getOwnerDeptId(),row.getEnvironment(),config));
        row.setEffectiveFrom(instant(config.effectiveFrom()));row.setEffectiveTo(instant(config.effectiveTo()));
    }
    private static String hash(Long owner,String environment,PriceContent config) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
                    ViJson.write(List.of(owner,environment,config)).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    private static PriceContent canonical(PriceContent c) {
        if (c==null || c.name()==null || c.name().isBlank() || c.name().trim().length()>100
                || c.vehicleTypeCode()==null || c.vehicleTypeCode().isBlank() || c.vehicleTypeCode().length()>64
                || c.inspectionTypeCode()==null || c.inspectionTypeCode().isBlank() || c.inspectionTypeCode().length()>64)
            invalid("名称、车型和检测类型必填且不能超长");
        for (long value:new long[]{c.inspectionFee(),c.driverBaseFee(),c.distanceUnitFee(),c.waitingUnitFee()})
            if (value<0 || value>PriceCalculator.MAX_SAFE_FEN) invalid("金额须为安全范围内的整数分");
        if (c.includedDistanceMeter()<0 || c.includedDistanceMeter()>Integer.MAX_VALUE || c.distanceUnitMeter()<1
                || c.distanceUnitMeter()>Integer.MAX_VALUE) invalid("包含里程和计费单位无效");
        if (c.waitingFeeEnabled() || c.waitingFreeSeconds()!=0 || c.waitingUnitSeconds()!=0 || c.waitingUnitFee()!=0)
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID,"等待费尚未通过P02评审，不能启用或设置非零等待参数");
        if (!Set.of("CEIL_UNIT","EXACT_PRORATA").contains(c.distanceRounding()==null?"":c.distanceRounding())
                || !Set.of("ONE_WAY","ROUND_TRIP").contains(c.routeBasis()==null?"":c.routeBasis())) invalid("计价进位或路线口径无效");
        Instant from=instant(c.effectiveFrom()),to=instant(c.effectiveTo());
        if (to!=null && (from==null || !from.isBefore(to))) invalid("生效开始时间必须早于结束时间");
        return new PriceContent(c.name().trim(),c.vehicleTypeCode(),c.inspectionTypeCode(),c.inspectionFee(),c.driverBaseFee(),
                c.includedDistanceMeter(),c.distanceUnitMeter(),c.distanceUnitFee(),c.distanceRounding(),c.routeBasis(),false,
                0,0,0,from==null?null:from.toString(),to==null?null:to.toString());
    }
    public static Instant instant(String text) {
        if (text==null) return null;
        try {
            if (!text.endsWith("Z")) throw new IllegalArgumentException();
            Instant value=Instant.parse(text);
            if (value.getNano()%1_000_000!=0 || value.isBefore(Instant.parse("1000-01-01T00:00:00Z"))
                    || value.isAfter(Instant.parse("9999-12-31T23:59:59.999Z"))) throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException e) {invalid("生效时间须为UTC，且最多毫秒精度");return null;}
    }
    private void success(Actor actor,ViPriceRule row,String action,Object before,String reason) {
        audit.recordSuccess(actor,"VI_PRICE_RULE",row.getId(),action,before,summary(row),reason);
        outbox.publish("price:"+row.getId()+":"+action+":"+(row.getVersion()+1),"VI_PRICE_RULE",row.getId(),action,
                Map.of("ruleId",row.getId(),"revision",row.getRevision()));
    }
    private static Map<String,Object> summary(ViPriceRule row) {
        return Map.of("status",row.getStatus(),"revision",row.getRevision(),"contentHash",row.getConfigHash());
    }
    private void rejected(Actor actor,long id,RuntimeException error) {
        String code=error instanceof ViException business?business.getErrorCode().name():ViErrorCode.VI_INTERNAL_ERROR.name();
        audit.recordFailure(actor,"VI_PRICE_RULE",id,"PRICE_REJECTED",null,Map.of("errorCode",code),"计价操作未成功");
    }
    private static void reason(String text) {if (text==null || text.isBlank() || text.length()>500) invalid("请填写500字以内的原因或审核意见");}
    private static long id(String text) {try {return ViId.parse(text);} catch (RuntimeException e) {invalid("ID无效");return 0;}}
    private static void one(int count) {if (count!=1) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);}
    private static void invalid(String text) {throw new ViException(ViErrorCode.VI_REQUEST_INVALID,text);}
    private static void state(String text) {throw new ViException(ViErrorCode.VI_STATE_CONFLICT,text);}
    private static void invisible() {throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);}
    private static void unavailable() {throw new ViException(ViErrorCode.VI_SERVICE_UNAVAILABLE,"该站点、车型或检测类型暂无唯一有效的已审核价格");}
}
