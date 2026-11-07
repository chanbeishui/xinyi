package com.xinyi.rongli.refund.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 自动执行默认关闭。数据库租约恢复扫描与outbox只消费同一执行服务，不在任务中直接改资金。 */
@Component
public class RefundExecutionJob {
    private final RefundExecutionService refunds;
    private final PaymentReconciliationService reconciliation;
    private final boolean enabled;

    public RefundExecutionJob(
            RefundExecutionService refunds,
            PaymentReconciliationService reconciliation,
            @Value("${vi.jobs.refund-enabled:false}") boolean enabled) {
        this.refunds = refunds;
        this.reconciliation = reconciliation;
        this.enabled = enabled;
    }

    @Scheduled(
            fixedDelayString = "${vi.jobs.refund-delay-ms:5000}",
            initialDelayString = "${vi.jobs.refund-delay-ms:5000}")
    public void scheduled() {
        if (enabled) runOnce();
    }

    public int runOnce() {
        if (!enabled) return 0;
        int count = refunds.runDue();
        reconciliation.scan();
        return count;
    }
}
