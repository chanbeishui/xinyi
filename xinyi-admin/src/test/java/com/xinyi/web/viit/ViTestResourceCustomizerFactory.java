package com.xinyi.web.viit;

import com.xinyi.framework.manager.ShutdownManager;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * 只在真实集成测试中管理框架线程池。框架AsyncManager是JVM静态单例，不能随某个Spring测试上下文提前销毁。
 * 业务服务、鉴权、数据库及审计实现保持真实；这里只统一测试基础资源的生命周期与线程预算。
 */
public final class ViTestResourceCustomizerFactory implements ContextCustomizerFactory {
    @Override
    public ContextCustomizer createContextCustomizer(
            Class<?> testClass, List<ContextConfigurationAttributes> configurationAttributes) {
        return testClass.getName().startsWith("com.xinyi.web.viit.")
                ? ResourceCustomizer.INSTANCE
                : null;
    }

    private enum ResourceCustomizer implements ContextCustomizer {
        INSTANCE;

        @Override
        public void customizeContext(
                ConfigurableApplicationContext context, MergedContextConfiguration configuration) {
            context.addBeanFactoryPostProcessor(
                    beanFactory -> {
                        if (beanFactory.containsBeanDefinition("scheduledExecutorService")) {
                            var definition =
                                    (AbstractBeanDefinition)
                                            beanFactory.getBeanDefinition(
                                                    "scheduledExecutorService");
                            definition.setBeanClass(ScheduledThreadPoolExecutor.class);
                            definition.setFactoryBeanName(null);
                            definition.setFactoryMethodName(null);
                            definition.setInstanceSupplier(() -> SharedScheduler.EXECUTOR);
                            // 该资源归JVM而非单个上下文。Spring仍负责取消本上下文注册的@Scheduled任务。
                            definition.setDestroyMethodName("");
                        }
                        if (beanFactory.containsBeanDefinition("shutdownManager")) {
                            var definition =
                                    (AbstractBeanDefinition)
                                            beanFactory.getBeanDefinition("shutdownManager");
                            definition.setBeanClass(TestShutdownManager.class);
                        }
                    });
        }
    }

    private static final class SharedScheduler {
        private static final AtomicInteger SEQUENCE = new AtomicInteger();
        private static final ScheduledThreadPoolExecutor EXECUTOR = create();

        private static ScheduledThreadPoolExecutor create() {
            var executor =
                    new ScheduledThreadPoolExecutor(
                            4,
                            task -> {
                                Thread thread =
                                        new Thread(
                                                task,
                                                "vi-test-scheduler-" + SEQUENCE.incrementAndGet());
                                thread.setDaemon(true);
                                return thread;
                            });
            executor.setRemoveOnCancelPolicy(true);
            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(executor::shutdownNow, "vi-test-scheduler-stop"));
            return executor;
        }
    }

    public static final class TestShutdownManager extends ShutdownManager {
        @Override
        public void destroy() {
            // 上下文淘汰不能关闭AsyncManager仍引用的全局池；测试JVM退出时统一清理。
        }
    }
}
