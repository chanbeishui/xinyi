package com.xinyi.rongli.config.service;

import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.config.dto.ConfigView;
import com.xinyi.rongli.config.dto.DictItemView;
import com.xinyi.rongli.config.dto.PublicServiceConfigView;
import com.xinyi.rongli.config.mapper.ConfigMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典/公开条款/参数配置（C01）。业务配置存 sys_config（vi.biz.* 白名单）+vi.config.version CAS；
 * 固定安全项（支付上限/权限/水印）不可由普通配置绕过；公开配置仅白名单发布值，缺已发布协议不开放下单、
 * 第三方未配置不宣称可支付、不泄商户密钥；新车型无已发布价格不能下单（价格守卫）。
 */
@Service
public class ConfigService {

    private static final Set<String> DICT_TYPES = Set.of("vi_vehicle_type", "vi_inspection_type");
    private static final Set<String> PROTECTED_KEYS = Set.of(
            "vi.biz.payment.cap.minutes", "vi.biz.permission.registry", "vi.biz.watermark");

    private final ConfigMapper configMapper;
    private final BusinessActorResolver actorResolver;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;
    private final String environment;

    public ConfigService(ConfigMapper configMapper, BusinessActorResolver actorResolver,
                         AuditService auditService, OutboxService outboxService,
                         PlatformTransactionManager txManager, @Value("${vi.environment:local}") String environment) {
        this.configMapper = configMapper;
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
            out.add(new DictItemView((String) r.get("dict_value"), (String) r.get("dict_label"),
                    ((Number) r.get("dict_sort")).intValue(), true, 0));
        }
        return out;
    }

    public ConfigView getConfig() {
        actorResolver.requireAdmin();
        requirePerm("biz:config:query");
        configMapper.ensureVersion();
        List<ConfigView.Item> items = new ArrayList<>();
        for (Map<String, Object> r : configMapper.selectBusiness()) {
            items.add(new ConfigView.Item((String) r.get("config_key"), (String) r.get("config_value")));
        }
        return new ConfigView(items, configMapper.selectVersion());
    }

    public long putConfig(List<ConfigView.Item> items, long expectedVersion, String reason) {
        Actor actor = actorResolver.requireAdmin();
        requirePerm("biz:config:edit");
        for (ConfigView.Item it : items) {
            if (it.key() == null || !it.key().startsWith("vi.biz.") || PROTECTED_KEYS.contains(it.key())) {
                throw new ViException(ViErrorCode.VI_FORBIDDEN, "固定安全项/非白名单键不可由普通配置修改: " + it.key());
            }
        }
        return tx.execute(s -> {
            configMapper.ensureVersion();
            long current = configMapper.selectVersion();
            if (current != expectedVersion) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "配置已变化，请刷新后重试");
            }
            for (ConfigView.Item it : items) {
                configMapper.upsert(it.key(), it.value());
            }
            if (configMapper.casVersion(Long.toString(expectedVersion), Long.toString(expectedVersion + 1)) != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "配置已变化，请刷新后重试");
            }
            auditService.recordSuccess(actor, "VI_CONFIG", 0, "CONFIG_UPDATE", null,
                    Map.of("keys", items.size(), "reason", reason), null);
            outboxService.publish("config-update:" + (expectedVersion + 1), "VI_CONFIG", 0,
                    "CONFIG_UPDATED", Map.of("version", expectedVersion + 1));
            return expectedVersion + 1;
        });
    }

    public PublicServiceConfigView publicConfig() {
        configMapper.ensureVersion();
        String termsVersion = configMapper.selectValue("vi.biz.terms.version");
        String privacyVersion = configMapper.selectValue("vi.biz.privacy.version");
        boolean published = termsVersion != null && !termsVersion.isBlank()
                && privacyVersion != null && !privacyVersion.isBlank();
        boolean bookingEnabled = published && "true".equals(configMapper.selectValue("vi.biz.booking.enabled"));
        List<String> methods = new ArrayList<>();
        // 第三方未配置不宣称可支付；生产不采用测试主体（仅真实配置标志开启才暴露 WECHAT）
        if ("true".equals(configMapper.selectValue("vi.payment.wechat.configured"))) {
            methods.add("WECHAT");
        }
        if ("true".equals(configMapper.selectValue("vi.biz.payment.offline"))) {
            methods.add("OFFLINE");
        }
        List<String> cities = parseList(configMapper.selectValue("vi.biz.service.cities"));
        return new PublicServiceConfigView(
                configMapper.selectVersion(),
                nvl(configMapper.selectValue("vi.biz.company.name")),
                nvl(configMapper.selectValue("vi.biz.support.phone")),
                nvl(termsVersion), nvl(configMapper.selectValue("vi.biz.terms.text")),
                nvl(privacyVersion), nvl(configMapper.selectValue("vi.biz.privacy.text")),
                cities, bookingEnabled, methods,
                nvl(configMapper.selectValue("vi.biz.payment.notice")));
    }

    /** 价格守卫：新车型无已发布价格不能下单（防0元）。供报价/下单调用。 */
    public void assertPriced(String vehicleTypeCode) {
        if (configMapper.countPublishedPrice(vehicleTypeCode) <= 0) {
            throw new ViException(ViErrorCode.VI_SERVICE_UNAVAILABLE, "该车型暂无已发布价格，不能下单");
        }
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
        if (!SecurityUtils.isAdmin() && (lu.getPermissions() == null || !lu.getPermissions().contains(perm))) {
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "没有执行此操作的权限");
        }
    }
}