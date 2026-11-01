package com.xinyi.rongli.pricing.service;

import java.util.Locale;

public final class PriceEnvironment {
    private PriceEnvironment() { }
    public static String resolve(String deployment, String explicit) {
        String mode=deployment.toLowerCase(Locale.ROOT);
        boolean live=java.util.Set.of("prod","production","live").contains(mode);
        if (explicit != null && !explicit.isBlank()) {
            if (!java.util.Set.of("TEST","LIVE").contains(explicit) || (live && !"LIVE".equals(explicit)))
                throw new IllegalArgumentException("计价环境无效或生产错误引用TEST配置");
            return explicit;
        }
        if (live) return "LIVE";
        if (java.util.Set.of("test","local","dev","development","integration","staging","stage").contains(mode)) return "TEST";
        throw new IllegalArgumentException("未知部署环境，须明确配置vi.pricing.environment");
    }
}
