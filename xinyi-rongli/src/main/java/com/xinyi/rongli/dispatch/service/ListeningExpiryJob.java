package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.platform.service.ViOutboxRelay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 默认不启动真实调度；实时视图总按固定截止判定，DB任务提供关闭持久化与重启补偿。 */
@Component
public class ListeningExpiryJob {
    private final ListeningDeadlineService service;
    private final ViOutboxRelay relay;
    private final boolean enabled;

    public ListeningExpiryJob(
            ListeningDeadlineService service,
            ViOutboxRelay relay,
            @Value("${vi.jobs.listening-enabled:false}") boolean enabled) {
        this.service = service;
        this.relay = relay;
        this.enabled = enabled;
    }

    @Scheduled(
            fixedDelayString = "${vi.jobs.listening-delay-ms:5000}",
            initialDelayString = "${vi.jobs.listening-delay-ms:5000}")
    public void scheduled() {
        if (enabled) runOnce();
    }

    public int runOnce() {
        service.repairMissing();
        return relay.runOnce(ListeningDeadlineService.TIMER);
    }
}
