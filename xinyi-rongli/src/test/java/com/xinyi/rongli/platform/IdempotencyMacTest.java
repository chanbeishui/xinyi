package com.xinyi.rongli.platform;

import com.xinyi.rongli.platform.config.ViPlatformProperties;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.mapper.ViIdempotencyMapper;
import com.xinyi.rongli.platform.service.IdempotencyService;
import com.xinyi.rongli.platform.service.SecretGuard;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * 幂等请求摘要单元证据：HMAC-SHA256 确定性、32 字节匹配 BINARY(32)、不同体不同摘要、
 * 空密钥拒绝计算（不以空密钥静默生成摘要）。纯逻辑，无 DB。
 */
class IdempotencyMacTest {

    private static IdempotencyService serviceWithKey(String key) {
        ViPlatformProperties props = new ViPlatformProperties(key, 24, 100, 60, 8, false);
        return new IdempotencyService(mock(ViIdempotencyMapper.class), new SecretGuard(), props);
    }

    @Test
    void 同体同摘要_32字节() {
        IdempotencyService svc = serviceWithKey("unit-test-lookup-key-0123456789");
        byte[] a = svc.mac("{\"amountFen\":6500}");
        byte[] b = svc.mac("{\"amountFen\":6500}");
        assertEquals(32, a.length);
        assertArrayEquals(a, b);
    }

    @Test
    void 不同体不同摘要() {
        IdempotencyService svc = serviceWithKey("unit-test-lookup-key-0123456789");
        byte[] a = svc.mac("{\"amountFen\":6500}");
        byte[] b = svc.mac("{\"amountFen\":6000}");
        assertFalse(MessageDigest.isEqual(a, b));
    }

    @Test
    void 不同密钥不同摘要() {
        byte[] a = serviceWithKey("key-alpha-0123456789").mac("{\"x\":1}");
        byte[] b = serviceWithKey("key-bravo-0123456789").mac("{\"x\":1}");
        assertFalse(MessageDigest.isEqual(a, b));
    }

    @Test
    void 空密钥拒绝计算() {
        IdempotencyService svc = serviceWithKey("");
        ViException e = assertThrows(ViException.class, () -> svc.mac("{\"x\":1}"));
        assertEquals(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, e.getErrorCode());
    }

    @Test
    void 主体与动作键格式() {
        assertEquals("MINI:1001", IdempotencyService.subjectKey(
                com.xinyi.rongli.platform.actor.Actor.mini(1001L, "OWNER", null, null, null, null)));
        assertEquals("ADMIN:2002", IdempotencyService.subjectKey(
                com.xinyi.rongli.platform.actor.Actor.admin(2002L, "ADMIN")));
        assertEquals("POST /api/owner/orders",
                IdempotencyService.actionKey("post", "/api/owner/orders"));
    }
}
