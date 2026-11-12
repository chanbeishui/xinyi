package com.xinyi.rongli.config.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.config.dto.ConfigView;
import com.xinyi.rongli.config.dto.DictItemView;
import com.xinyi.rongli.config.dto.PublicServiceConfigView;
import com.xinyi.rongli.config.mapper.ConfigMapper;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import com.xinyi.rongli.pricing.service.PricingService;
import com.xinyi.rongli.verification.service.VerificationConsentSnapshot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 字典/公开条款/参数配置（C01）。业务配置存 sys_config（vi.biz.* 白名单）+vi.config.version CAS；
 * 固定安全项（支付上限/权限/水印）不可由普通配置绕过；公开配置仅白名单发布值，缺已发布协议不开放下单、 第三方未配置不宣称可支付、不泄商户密钥；新车型无已发布价格不能下单（价格守卫）。
 */
@Service
public class ConfigService {

    private static final Set<String> DICT_TYPES = Set.of("vi_vehicle_type", "vi_inspection_type");
    private static final Set<String> PROTECTED_KEYS =
            Set.of("vi.biz.payment.cap.minutes", "vi.biz.permission.registry", "vi.biz.watermark");

    private final ConfigMapper configMapper;
    private final IntegrationAuthority authority;
    private final BusinessActorResolver actorResolver;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;
    private final String environment;
    private final PricingService pricingService;

    public ConfigService(
            ConfigMapper configMapper,
            IntegrationAuthority authority,
            BusinessActorResolver actorResolver,
            AuditService auditService,
            OutboxService outboxService,
            PlatformTransactionManager txManager,
            PricingService pricingService,
            @Value("${vi.environment:local}") String environment) {
        this.configMapper = configMapper;
        this.authority = authority;
        this.pricingService = pricingService;
        this.actorResolver = actorResolver;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.environment = environment;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public List<DictItemView> dict(String dictType) {
        if (!DICT_TYPES.contains(dictType)) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        List<DictItemView> out = new ArrayList<>();
        for (Map<String, Object> r : configMapper.selectDict(dictType)) {
            out.add(
                    new DictItemView(
                            (String) r.get("dict_value"),
                            (String) r.get("dict_label"),
                            ((Number) r.get("dict_sort")).intValue(),
                            true,
                            0));
        }
        return out;
    }

    public ConfigView getConfig() {
        actorResolver.requireAdmin();
        requirePerm("biz:config:query");
        List<ConfigView.Item> items = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (Map<String, Object> r : configMapper.selectBusiness()) {
            String key = (String) r.get("config_key");
            if (!keys.add(key))
                throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "配置键存在重复记录，请先核查处理：" + key);
            items.add(
                    new ConfigView.Item(
                            (String) r.get("config_key"), (String) r.get("config_value")));
        }
        return new ConfigView(items, configMapper.selectVersion());
    }

    public long putConfig(List<ConfigView.Item> items, long expectedVersion, String reason) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:config:edit");
        Set<String> keys = new HashSet<>();
        for (ConfigView.Item it : items) {
            if (!keys.add(it.key()))
                throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "同次配置请求不能重复提交同一个键");
            // 派单参数只影响新订单；缺失/非法值不能用示例30分钟静默兜底。
            if (com.xinyi.rongli.jobs.service.DispatchPlan.LEAD_KEY.equals(it.key())) {
                com.xinyi.rongli.jobs.service.DispatchPlan.minutes(it.value());
            }
            if (it.key() == null
                    || !it.key().startsWith("vi.biz.")
                    || PROTECTED_KEYS.contains(it.key())) {
                throw new ViException(
                        ViErrorCode.VI_FORBIDDEN, "固定安全项/非白名单键不可由普通配置修改: " + it.key());
            }
        }
        // 同意配置按相同键序加锁，避免资格提交锁正文/版本时与后台反序多键写入相互等待。
        List<ConfigView.Item> orderedItems =
                items.stream()
                        .sorted(java.util.Comparator.comparing(ConfigView.Item::key))
                        .toList();
        return tx.execute(
                s -> {
                    // 统一守卫先持目录并复核真实auth_version/status；等待期间撤权不能沿用入口旧权限写配置。
                    // 目录同时串行化首次建立版本行，不能仅靠无唯一键的NOT EXISTS防并发重复插入。
                    authority.lockAuthority();
                    requirePerm("biz:config:edit");
                    configMapper.ensureVersion();
                    List<Map<String, Object>> versions = configMapper.lockVersion();
                    ConfigMapper.requireUnique("vi.config.version", versions);
                    if (versions.isEmpty())
                        throw new ViException(ViErrorCode.VI_CONFIG_INVALID, "业务配置版本记录缺失");
                    Map<String, Object> version = versions.get(0);
                    long versionId = ((Number) version.get("config_id")).longValue();
                    long current = ConfigMapper.versionValue((String) version.get("config_value"));
                    if (current != expectedVersion) {
                        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "配置已变化，请刷新后重试");
                    }
                    for (ConfigView.Item it : orderedItems) {
                        saveItem(it);
                    }
                    if (configMapper.casVersion(
                                    versionId,
                                    Long.toString(expectedVersion),
                                    Long.toString(expectedVersion + 1))
                            != 1) {
                        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "配置已变化，请刷新后重试");
                    }
                    auditService.recordSuccess(
                            actor,
                            "VI_CONFIG",
                            0,
                            "CONFIG_UPDATE",
                            null,
                            Map.of("keys", items.size(), "reason", reason),
                            null);
                    outboxService.publish(
                            "config-update:" + (expectedVersion + 1),
                            "VI_CONFIG",
                            0,
                            "CONFIG_UPDATED",
                            Map.of("version", expectedVersion + 1));
                    return expectedVersion + 1;
                });
    }

    /** 版本行锁持有到整个批量提交结束；沿键序检查真实0/1/>1行，任何异常令整批和版本/成功事件回滚。 */
    private void saveItem(ConfigView.Item item) {
        List<Map<String, Object>> rows = configMapper.selectEntries(item.key(), true);
        ConfigMapper.requireUnique(item.key(), rows);
        if (rows.isEmpty()) {
            if (configMapper.insertValue(item.key(), item.value()) != 1)
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
            return;
        }
        Map<String, Object> before = rows.get(0);
        // 同值无须UPDATE，兼容JDBC按changed rows计数；更不能因为返回0而再INSERT造成重复键。
        if (Objects.equals(before.get("config_value"), item.value())) return;
        long id = ((Number) before.get("config_id")).longValue();
        if (configMapper.updateValue(id, item.key(), item.value()) != 1)
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    public PublicServiceConfigView publicConfig() {
        String termsVersion = configMapper.selectValue("vi.biz.terms.version");
        String privacyVersion = configMapper.selectValue("vi.biz.privacy.version");
        boolean published =
                termsVersion != null
                        && !termsVersion.isBlank()
                        && privacyVersion != null
                        && !privacyVersion.isBlank();
        boolean bookingEnabled =
                published && "true".equals(configMapper.selectValue("vi.biz.booking.enabled"));
        List<String> methods = new ArrayList<>();
        // 第三方未配置不宣称可支付；生产不采用测试主体（仅真实配置标志开启才暴露 WECHAT）
        if ("true".equals(configMapper.selectValue("vi.payment.wechat.configured"))) {
            methods.add("WECHAT");
        }
        if ("true".equals(configMapper.selectValue("vi.biz.payment.offline"))) {
            methods.add("OFFLINE");
        }
        List<String> cities = parseList(configMapper.selectValue("vi.biz.service.cities"));
        QualificationConsentSnapshot qualificationConsent =
                QualificationConsentSnapshot.read(configMapper, false);
        var verificationConsent = verificationConsent();
        return new PublicServiceConfigView(
                configMapper.selectVersion(),
                nvl(configMapper.selectValue("vi.biz.company.name")),
                nvl(configMapper.selectValue("vi.biz.support.phone")),
                nvl(termsVersion),
                nvl(configMapper.selectValue("vi.biz.terms.text")),
                nvl(privacyVersion),
                nvl(configMapper.selectValue("vi.biz.privacy.text")),
                cities,
                bookingEnabled,
                methods,
                nvl(configMapper.selectValue("vi.biz.payment.notice")),
                nvl(qualificationConsent.version()),
                nvl(qualificationConsent.text()),
                qualificationConsent.textHash(),
                verificationConsent.version(),
                verificationConsent.text(),
                verificationConsent.textHash());
    }

    /** 同意版本和正文必须来自同一次有序查询，避免把旧正文误记为已同意新版本。 */
    private VerificationConsentSnapshot verificationConsent() {
        String version = null, text = null;
        for (var row : configMapper.selectVerificationConsent(false)) {
            if ("vi.biz.verification.consent.version".equals(row.get("config_key")))
                version = (String) row.get("config_value");
            if ("vi.biz.verification.consent.text".equals(row.get("config_key")))
                text = (String) row.get("config_value");
        }
        return new VerificationConsentSnapshot(version, text);
    }

    /** 缺少真实站点和检测类型的旧入口不能证明可计价，保留签名并拒绝绕过C04。 */
    @Deprecated
    public void assertPriced(String vehicleTypeCode) {
        throw new ViException(ViErrorCode.VI_SERVICE_UNAVAILABLE, "计价必须提供真实站点、车型及检测类型");
    }

    public void assertPriced(long stationId, String vehicleTypeCode, String inspectionTypeCode) {
        pricingService.quoteForStation(
                stationId, vehicleTypeCode, inspectionTypeCode, java.time.Instant.now(), 0);
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return ViJson.read(json, List.class);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private void requirePerm(String perm) {
        LoginUser lu = SecurityUtils.getLoginUser();
        if (lu == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
        if (!SecurityUtils.isAdmin()
                && (lu.getPermissions() == null || !lu.getPermissions().contains(perm))) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "没有执行此操作的权限");
        }
    }
}
