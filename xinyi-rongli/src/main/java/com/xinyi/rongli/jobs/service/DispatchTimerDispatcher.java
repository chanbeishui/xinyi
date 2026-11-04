package com.xinyi.rongli.jobs.service;

import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.service.OutboxDispatcher;
import org.springframework.stereotype.Component;

@Component
public class DispatchTimerDispatcher implements OutboxDispatcher {
    private final boolean enabled;
    private final DispatchDeadlineService service;

    public DispatchTimerDispatcher(
            DispatchDeadlineService service,
            @org.springframework.beans.factory.annotation.Value("${vi.jobs.dispatch-enabled:false}")
                    boolean enabled) {
        this.enabled = enabled;
        this.service = service;
    }

    @Override
    public boolean supports(String eventType) {
        return enabled && DispatchDeadlineService.TIMER.equals(eventType);
    }

    @Override
    public void dispatch(ViOutbox event) {
        service.execute(event);
    }
}
