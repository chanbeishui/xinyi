package com.xinyi.rongli.platform;

import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.service.SecretGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 边界“登录刷新不缓存明文”的单元证据：SecretGuard 按 JSON 字段名递归拒绝明文机密，
 * 命中即抛 VI_DATA_INVALID，不静默脱敏后继续。纯逻辑，无 DB/HTTP。
 */
class SecretGuardTest {

    private final SecretGuard guard = new SecretGuard();

    @Test
    void 放行不含机密的业务结果() {
        assertDoesNotThrow(() -> guard.assertNoSecret("{\"orderNo\":\"RO1\",\"amountFen\":6500}"));
        assertDoesNotThrow(() -> guard.assertNoSecret("{\"items\":[{\"id\":\"1\",\"name\":\"检测\"}]}"));
        assertDoesNotThrow(() -> guard.assertNoSecret(null));
        assertDoesNotThrow(() -> guard.assertNoSecret(""));
    }

    @Test
    void 拒绝顶层明文令牌字段() {
        ViException e = assertThrows(ViException.class,
                () -> guard.assertNoSecret("{\"token\":\"abc\"}"));
        assertEquals(ViErrorCode.VI_DATA_INVALID, e.getErrorCode());
        assertThrows(ViException.class, () -> guard.assertNoSecret("{\"refreshToken\":\"abc\"}"));
        assertThrows(ViException.class, () -> guard.assertNoSecret("{\"accessToken\":\"abc\"}"));
    }

    @Test
    void 拒绝嵌套与下划线命名的机密字段() {
        assertThrows(ViException.class,
                () -> guard.assertNoSecret("{\"data\":{\"launchUrl\":\"https://x\"}}"));
        assertThrows(ViException.class,
                () -> guard.assertNoSecret("{\"session\":{\"refresh_token\":\"abc\"}}"));
        assertThrows(ViException.class,
                () -> guard.assertNoSecret("{\"authorization\":\"Bearer x\"}"));
    }

    @Test
    void 拒绝数组元素中的机密字段() {
        assertThrows(ViException.class,
                () -> guard.assertNoSecret("{\"list\":[{\"ok\":1},{\"credential\":\"c\"}]}"));
    }

    @Test
    void 非JSON文本拒绝以免漏网() {
        assertThrows(ViException.class, () -> guard.assertNoSecret("token=abc&x=1"));
    }
}
