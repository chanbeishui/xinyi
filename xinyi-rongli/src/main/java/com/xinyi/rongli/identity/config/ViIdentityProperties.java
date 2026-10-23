package com.xinyi.rongli.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * I01 身份域配置：provider 选择与会话令牌时效。
 * 时效为可审计安全配置（技术评审参数），非付款时间；生产真实值由部署环境/KMS 提供，不进 Git。
 * vi.providers.mode 缺省视为 WECHAT（生产安全默认，绝不缺省走 SIMULATOR）。
 */
@Component
public class ViIdentityProperties {

    private final String providersMode;
    private final String providersBaseUrl;
    private final String providersSecret;
    private final long accessTtlSeconds;
    private final long absoluteTtlSeconds;

    public ViIdentityProperties(
            @Value("${vi.providers.mode:WECHAT}") String providersMode,
            @Value("${vi.providers.base-url:}") String providersBaseUrl,
            @Value("${vi.providers.secret:}") String providersSecret,
            @Value("${vi.session.access-ttl-seconds:900}") long accessTtlSeconds,
            @Value("${vi.session.absolute-ttl-seconds:604800}") long absoluteTtlSeconds) {
        this.providersMode = providersMode == null ? "WECHAT" : providersMode.trim();
        this.providersBaseUrl = providersBaseUrl == null ? "" : providersBaseUrl.trim();
        this.providersSecret = providersSecret == null ? "" : providersSecret;
        this.accessTtlSeconds = accessTtlSeconds > 0 ? accessTtlSeconds : 900L;
        this.absoluteTtlSeconds = absoluteTtlSeconds > 0 ? absoluteTtlSeconds : 604800L;
    }

    public String getProvidersMode() { return providersMode; }
    public String getProvidersBaseUrl() { return providersBaseUrl; }
    public String getProvidersSecret() { return providersSecret; }
    public long getAccessTtlSeconds() { return accessTtlSeconds; }
    public long getAbsoluteTtlSeconds() { return absoluteTtlSeconds; }
    public boolean isSimulator() { return "SIMULATOR".equalsIgnoreCase(providersMode); }
}
