package com.xinyi.rongli.pricing;

import com.xinyi.rongli.pricing.service.PriceEnvironment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PriceEnvironmentTest {
    @Test void 部署环境明确分离() {
        assertEquals("LIVE",PriceEnvironment.resolve("production",""));
        assertEquals("LIVE",PriceEnvironment.resolve("prod","LIVE"));
        assertEquals("TEST",PriceEnvironment.resolve("integration",""));
        assertEquals("LIVE",PriceEnvironment.resolve("integration","LIVE"));
    }
    @Test void 生产不能降为测试且未知环境不得猜测() {
        assertThrows(IllegalArgumentException.class,()->PriceEnvironment.resolve("production","TEST"));
        assertThrows(IllegalArgumentException.class,()->PriceEnvironment.resolve("unknown",""));
        assertThrows(IllegalArgumentException.class,()->PriceEnvironment.resolve("local","OTHER"));
    }
}
