package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.platform.service.ViOutboxRelay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 数据库绝对期限任务可重启修补；开关默认关闭，实时新单资格判断始终生效。 */
@Component
public class QualificationExpiryJob {
    private final QualificationExpiryService service;
    private final ViOutboxRelay relay;
    private final boolean enabled;

    public QualificationExpiryJob(
            QualificationExpiryService service,
            ViOutboxRelay relay,
            @Value("${vi.jobs.qualification-enabled:false}") boolean enabled) {
        this.service = service;
        this.relay = relay;
        this.enabled = enabled;
    }

    @Scheduled(
            fixedDelayString = "${vi.jobs.qualification-delay-ms:60000}",
            initialDelayString = "${vi.jobs.qualification-delay-ms:60000}")
    public void scheduled() {
        if (enabled) runOnce();
    }

    public int runOnce() {
        service.repairMissing();
        return relay.runOnce(QualificationExpiryService.TIMER);
    }
}
