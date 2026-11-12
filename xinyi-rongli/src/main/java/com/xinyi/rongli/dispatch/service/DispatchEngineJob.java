package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.platform.service.ViOutboxRelay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 扫描和outbox都通过同一引擎；重启可修补待派单、到期邀约和活动总截止。 */
@Component
public class DispatchEngineJob {
    private final DispatchEngineService service;
    private final ViOutboxRelay relay;
    private final boolean enabled;

    public DispatchEngineJob(
            DispatchEngineService service,
            ViOutboxRelay relay,
            @Value("${vi.jobs.invitation-enabled:false}") boolean enabled) {
        this.service = service;
        this.relay = relay;
        this.enabled = enabled;
    }

    @Scheduled(
            fixedDelayString = "${vi.jobs.invitation-delay-ms:1000}",
            initialDelayString = "${vi.jobs.invitation-delay-ms:1000}")
    public void scheduled() {
        if (enabled) runOnce();
    }

    public int runOnce() {
        int count = service.runOnce();
        for (String type : DispatchEngineService.EVENTS) count += relay.runOnce(type);
        return count;
    }
}
