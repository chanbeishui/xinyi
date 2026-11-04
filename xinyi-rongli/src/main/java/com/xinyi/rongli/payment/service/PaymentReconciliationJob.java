package com.xinyi.rongli.payment.service;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;

/** 由数据库时点和短租约领取；重启后继续，Redis不可用不丢失关闭意图。 */
@Component
public class PaymentReconciliationJob {
    private final PaymentLifecycleService service;
    private final boolean enabled;

    public PaymentReconciliationJob(
            PaymentLifecycleService service,
            @Value("${vi.payment.reconciliation-enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    // 默认关闭，环境确认后显式启用；定时入口仍调用统一 Service，不直接写 Mapper。
    @Scheduled(fixedDelayString = "${vi.payment.reconciliation-delay-ms:10000}")
    public void scheduled() {
        if (enabled) service.runDue();
    }
}
