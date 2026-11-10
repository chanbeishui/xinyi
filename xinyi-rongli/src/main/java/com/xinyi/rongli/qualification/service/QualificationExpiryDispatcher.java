package com.xinyi.rongli.qualification.service;

import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.service.OutboxDispatcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 只消费资格版本到期事件，不接收客户端到期或资格结论。 */
@Component
public class QualificationExpiryDispatcher implements OutboxDispatcher {
    private final QualificationExpiryService service;
    private final boolean enabled;

    public QualificationExpiryDispatcher(
            QualificationExpiryService service,
            @Value("${vi.jobs.qualification-enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @Override
    public boolean supports(String eventType) {
        return enabled && QualificationExpiryService.TIMER.equals(eventType);
    }

    @Override
    public void dispatch(ViOutbox event) {
        service.execute(event);
    }
}
