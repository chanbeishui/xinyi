package com.xinyi.web.viit;

import com.xinyi.framework.manager.ShutdownManager;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/** 验证上下文淘汰后全局异步池仍可工作，不能靠跳过真实调度或不关闭上下文来规避资源错误。 */
class ViTestResourceCustomizerFactoryTest {
    @Test
    void closingOneContextDoesNotStopSharedJvmScheduler() throws Exception {
        ScheduledThreadPoolExecutor first;
        try (var context = context()) {
            first = context.getBean("scheduledExecutorService", ScheduledThreadPoolExecutor.class);
            assertEquals(4, first.getCorePoolSize());
            assertEquals("首次执行", first.submit(() -> "首次执行").get(1, TimeUnit.SECONDS));
        }
        assertFalse(first.isShutdown());
        try (var next = context()) {
            var second =
                    next.getBean("scheduledExecutorService", ScheduledThreadPoolExecutor.class);
            assertSame(first, second);
            assertEquals("淘汰后执行", second.submit(() -> "淘汰后执行").get(1, TimeUnit.SECONDS));
        }
        assertFalse(first.isShutdown());
    }

    private GenericApplicationContext context() {
        var context = new GenericApplicationContext();
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.registerBean(
                "scheduledExecutorService",
                ScheduledThreadPoolExecutor.class,
                () -> new ScheduledThreadPoolExecutor(50));
        context.registerBean("shutdownManager", ShutdownManager.class);
        var customizer =
                new ViTestResourceCustomizerFactory()
                        .createContextCustomizer(getClass(), List.of());
        assertNotNull(customizer);
        customizer.customizeContext(context, null);
        context.refresh();
        return context;
    }
}
