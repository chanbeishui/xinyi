package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.dispatch.domain.DispatchPolicy;
import com.xinyi.rongli.dispatch.dto.InviteRequests;
import com.xinyi.rongli.dispatch.mapper.DispatchInviteMapper;
import com.xinyi.rongli.driver.mapper.DriverMapper;
import com.xinyi.rongli.integration.service.*;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.MessageDigest;
import java.util.*;

/** 公司明确维护策略，建单冻结完整认证内容；不追用后来参数，更不把历史收款公司猜成服务公司。 */
@Service
public class DispatchPolicyService {
    public static final String QUERY = "biz:dispatchpolicy:query", EDIT = "biz:dispatchpolicy:edit";
    private final DispatchInviteMapper mapper;
    private final DriverMapper drivers;
    private final IntegrationAuthority authority;
    private final IntegrationCrypto crypto;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    public DispatchPolicyService(
            DispatchInviteMapper mapper,
            DriverMapper drivers,
            IntegrationAuthority authority,
            IntegrationCrypto crypto,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.mapper = mapper;
        this.drivers = drivers;
        this.authority = authority;
        this.crypto = crypto;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public record Receipt(long version) {}

    public Map<String, Object> get(long owner) {
        return guarded(
                "DISPATCH_POLICY_QUERY",
                () -> {
                    company(owner, QUERY);
                    return view(owner, mapper.policy(owner));
                });
    }

    public Map<String, Object> put(long owner, InviteRequests.Policy input, String key) {
        return guarded(
                "DISPATCH_POLICY_CHANGED",
                () -> {
                    company(owner, QUERY);
                    company(owner, EDIT);
                    idem.execute(
                            IdempotencyService.subjectKey(authority.actor()),
                            "DISPATCH_POLICY:" + owner,
                            key,
                            ViJson.write(input),
                            true,
                            Receipt.class,
                            () -> save(owner, input));
                    return view(owner, mapper.policy(owner));
                });
    }

    private Receipt save(long owner, InviteRequests.Policy input) {
        DispatchPolicy old = mapper.policy(owner);
        long version = old == null ? 0 : old.getVersion();
        if (input.expectedVersion() != version) version();
        for (Map<String, Object> item : input.licenseCompatibility())
            if (mapper.validVehicle(item.get("vehicleTypeCode").toString()) != 1)
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "车型必须引用当前启用的业务字典");
        DispatchPolicy row = new DispatchPolicy();
        row.setOwnerDeptId(owner);
        row.setVersion(version + 1);
        row.setInviteResponseSeconds(input.inviteResponseSeconds());
        row.setRadiusMeter(input.radiusMeter());
        row.setExpandedRadiusMeter(input.expandedRadiusMeter());
        row.setOverallDeadlineSeconds(input.overallDeadlineSeconds());
        row.setLicenseCompatibility(ViJson.write(input.licenseCompatibility()));
        row.setUpdatedByUserId(authority.actor().sysUserId());
        row.setConfigHash(hash(row));
        if (old == null) mapper.insertPolicy(row);
        else if (mapper.updatePolicy(row, version) != 1) version();
        String aad =
                "D02:POLICY:"
                        + owner
                        + ":VERSION:"
                        + row.getVersion()
                        + ":ADMIN:"
                        + authority.actor().sysUserId();
        Map<String, Object> after =
                Map.of(
                        "ownerDeptId",
                        Long.toString(owner),
                        "version",
                        row.getVersion(),
                        "reasonEnvelope",
                        Base64.getEncoder().encodeToString(crypto.envelope(input.reason(), aad)));
        audit.recordSuccess(
                authority.actor(),
                "VI_DISPATCH_POLICY",
                owner,
                "DISPATCH_POLICY_CHANGED",
                null,
                after,
                "变更原因已加密留存");
        outbox.publish(
                "DISPATCH_POLICY_CHANGED:" + owner + ":" + row.getVersion(),
                "VI_DISPATCH_POLICY",
                owner,
                "DISPATCH_POLICY_CHANGED",
                Map.of("ownerDeptId", Long.toString(owner), "version", row.getVersion()));
        return new Receipt(row.getVersion());
    }

    /** 与建单同事务。公司来自已验证站点公司收款关系，并独立核对站点真实部门树，不接受客户端归属。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void freeze(ViOrder order, long receiverCompany) {
        if (mapper.stationCompany(order.getStationId(), receiverCompany) != 1)
            throw new ViException(ViErrorCode.VI_CONFIG_CHANGED, "站点所属服务公司与原收款公司不一致");
        order.setServiceCompanyId(receiverCompany);
        DispatchPolicy row = mapper.policy(receiverCompany);
        // 缺业务策略不阻断原建单链；付款后由引擎明确转人工，禁止默补运营值。
        if (row == null) return;
        if (!MessageDigest.isEqual(hash(row), row.getConfigHash()))
            throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "公司派单策略认证失败");
        Map<String, Object> snapshot = content(row);
        snapshot.put(
                "authentication",
                Base64.getEncoder()
                        .encodeToString(
                                crypto.envelope(
                                        ViJson.write(snapshot),
                                        aad(row.getOwnerDeptId(), row.getVersion()))));
        order.setDispatchPolicyVersion(row.getVersion());
        order.setDispatchPolicySnapshot(ViJson.write(snapshot));
    }

    /** 原单快照带密钥版本认证；后续公司配置变更或密钥轮换不能替换原单政策。 */
    public Map<String, Object> frozen(ViOrder order) {
        if (order.getServiceCompanyId() == null
                || order.getDispatchPolicyVersion() == null
                || order.getDispatchPolicySnapshot() == null) return null;
        try {
            Map<String, Object> snapshot =
                    new TreeMap<>(ViJson.read(order.getDispatchPolicySnapshot(), Map.class));
            Object encoded = snapshot.remove("authentication");
            if (!(encoded instanceof String)
                    || !Objects.equals(
                            snapshot.get("ownerDeptId"), order.getServiceCompanyId().toString())
                    || ((Number) snapshot.get("version")).longValue()
                            != order.getDispatchPolicyVersion()) return null;
            String authenticated =
                    crypto.unenvelope(
                            Base64.getDecoder().decode((String) encoded),
                            aad(order.getServiceCompanyId(), order.getDispatchPolicyVersion()));
            if (!ViJson.tree(authenticated).equals(ViJson.tree(ViJson.write(snapshot))))
                return null;
            return snapshot;
        } catch (ViException e) {
            if (e.getErrorCode() == ViErrorCode.VI_SECRET_UNAVAILABLE) throw e;
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private byte[] hash(DispatchPolicy row) {
        // 当前策略的认证使用独立稳定lookup-key，并绑定公司、版本和规范化内容。
        // 加密根密钥轮换不应令仍有效策略无法用于新建单；旧订单快照另由携带密钥版本的envelope认证。
        // lookup-key缺失时mac本身fail-closed，不能降级裸哈希或省略认证。
        return idem.mac(
                aad(row.getOwnerDeptId(), row.getVersion()) + ":" + ViJson.write(content(row)));
    }

    private static String aad(long owner, long version) {
        return "D02:POLICY:" + owner + ":VERSION:" + version;
    }

    private Map<String, Object> content(DispatchPolicy row) {
        Map<String, Object> result = new TreeMap<>();
        result.put("ownerDeptId", row.getOwnerDeptId().toString());
        result.put("version", row.getVersion());
        result.put("inviteResponseSeconds", row.getInviteResponseSeconds());
        result.put("radiusMeter", row.getRadiusMeter());
        result.put("expandedRadiusMeter", row.getExpandedRadiusMeter());
        result.put("overallDeadlineSeconds", row.getOverallDeadlineSeconds());
        result.put("roundPolicy", "COMPANY_CURSOR");
        result.put("distanceMode", "GCJ02_GREAT_CIRCLE");
        result.put(
                "licenseCompatibility",
                canonical(ViJson.read(row.getLicenseCompatibility(), List.class)));
        return result;
    }

    /** MySQL JSON会重排对象键；认证前递归规范化，不能把数据库合法归一化当作内容变更。 */
    private static Object canonical(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> ordered = new TreeMap<>();
            map.forEach((key, item) -> ordered.put(key.toString(), canonical(item)));
            return ordered;
        }
        if (value instanceof List<?> list)
            return list.stream().map(DispatchPolicyService::canonical).toList();
        return value;
    }

    private void company(long owner, String permission) {
        Set<Long> scope = authority.scope(permission);
        if (scope != null && !scope.contains(owner))
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        Map<String, Object> company = drivers.company(owner);
        if (company == null
                || !"0".equals(company.get("status"))
                || !"0".equals(company.get("del_flag")))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "请选择真实有效的公司部门");
    }

    private Map<String, Object> view(long owner, DispatchPolicy row) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ownerDeptId", Long.toString(owner));
        result.put("companyName", Objects.toString(drivers.company(owner).get("dept_name"), ""));
        result.put("configured", row != null);
        result.put("version", row == null ? 0 : row.getVersion());
        for (String field :
                List.of(
                        "inviteResponseSeconds",
                        "radiusMeter",
                        "expandedRadiusMeter",
                        "overallDeadlineSeconds",
                        "roundPolicy",
                        "distanceMode",
                        "licenseCompatibility"))
            result.put(field, row == null ? null : content(row).get(field));
        Set<Long> edit = authority.scope(QUERY);
        boolean permitted = com.xinyi.common.utils.SecurityUtils.hasPermi(EDIT);
        if (permitted) {
            edit = authority.scope(EDIT);
            permitted = edit == null || edit.contains(owner);
        }
        result.put("allowedActions", permitted ? List.of("EDIT_DISPATCH_POLICY") : List.of());
        return result;
    }

    private <T> T guarded(String action, java.util.function.Supplier<T> work) {
        try {
            return tx.execute(
                    status -> {
                        authority.lockAuthority();
                        return work.get();
                    });
        } catch (RuntimeException e) {
            audit.recordFailure(
                    authority.actor(),
                    "VI_DISPATCH_POLICY",
                    0,
                    action + "_REJECTED",
                    null,
                    Map.of(
                            "code",
                            e instanceof ViException v
                                    ? v.getErrorCode().name()
                                    : "VI_INTERNAL_ERROR"),
                    "派单策略操作未完成");
            throw e;
        }
    }

    private static void version() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }
}
