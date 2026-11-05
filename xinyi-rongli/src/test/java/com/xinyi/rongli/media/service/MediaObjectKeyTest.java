package com.xinyi.rongli.media.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.xinyi.rongli.common.support.Crypto;

/**
 * 业务影像对象 key 的命名约束。
 *
 * <p>key 一旦写进 {@code vi_file.object_key} 就无法改名（对象存储没有重命名），
 * 所以格式必须靠测试钉住：订单段的位置决定能不能按单清理，日期格式决定清理脚本要不要写两套。</p>
 */
class MediaObjectKeyTest
{
    private MediaService service;

    private Method build;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new MediaService();
        ReflectionTestUtils.setField(service, "crypto", new Crypto("unit-test-key"));
        build = MediaService.class.getDeclaredMethod("buildObjectKey", String.class, String.class, Long.class,
                String.class);
        build.setAccessible(true);
    }

    @Test
    void orderSegmentShouldSitBeforeDate()
    {
        // 订单段必须在日期之前，否则无法用一次前缀删除清掉整单影像
        assertThat(key("EVIDENCE_PHOTO", "RL20261105001", 7L, ".jpg"))
                .matches("evidence_photo/ord-RL20261105001/\\d{4}/\\d{2}/\\d{2}/evidence_photo_[A-Za-z0-9_-]{12}\\.jpg");
    }

    @Test
    void dateSegmentShouldUseSlashSeparator()
    {
        // 与框架层 upload/avatar/download 统一成 yyyy/MM/dd，不能出现 2026-11-05
        assertThat(key("LICENSE_PHOTO", null, null, ".jpg"))
                .matches("license_photo/\\d{4}/\\d{2}/\\d{2}/license_photo_[A-Za-z0-9_-]{12}\\.jpg");
    }

    @Test
    void uploadsWithoutOrderShouldNotCarryOrderSegment()
    {
        assertThat(key("AVATAR", null, null, ".png")).doesNotContain("ord-");
    }

    @Test
    void illegalOrderNoShouldFallBackToId()
    {
        // 订单号全是清洗后为空的中文/符号，退回订单 id，仍保证可追溯
        assertThat(key("LICENSE_PHOTO", "订单/证", 42L, ".jpg")).startsWith("license_photo/ord-42/");
    }

    @Test
    void filenameShouldCarryReadablePurposePrefix()
    {
        String k = key("AFTERSALE_EVIDENCE", null, null, ".jpg");
        assertThat(k).endsWith(".jpg");
        assertThat(k.substring(k.lastIndexOf('/') + 1)).startsWith("aftersale_evidence_");
    }

    private String key(String purpose, String orderNo, Long orderId, String ext)
    {
        try
        {
            return (String) build.invoke(service, purpose, orderNo, orderId, ext);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
}
