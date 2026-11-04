package com.xinyi.rongli.jobs.service;

import com.xinyi.rongli.platform.service.ViOutboxRelay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 可重复扫描数据库绝对到期任务，租约恢复沿用E06；不依赖进程内定时器或Redis过期消息。 */
@Component
public class DispatchDeadlineJob {
    private final DispatchDeadlineService service;
    private final ViOutboxRelay relay;
    private final boolean enabled;

    public DispatchDeadlineJob(
            DispatchDeadlineService service,
            ViOutboxRelay relay,
            @Value("${vi.jobs.dispatch-enabled:false}") boolean enabled) {
        this.service = service;
        this.relay = relay;
        this.enabled = enabled;
    }

    @Scheduled(
            fixedDelayString = "${vi.jobs.dispatch-delay-ms:5000}",
            initialDelayString = "${vi.jobs.dispatch-delay-ms:5000}")
    public void scheduled() {
        if (enabled) runOnce();
    }

    public int runOnce() {
        service.repairMissing();
        return relay.runOnce(DispatchDeadlineService.TIMER);
    }
}
