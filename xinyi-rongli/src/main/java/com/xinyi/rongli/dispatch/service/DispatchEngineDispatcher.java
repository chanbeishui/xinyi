package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.service.OutboxDispatcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 自动执行默认关闭；启用后消费永久事件，重复投递只重新检查库中真实批次，不重建原截止。 */
@Component
public class DispatchEngineDispatcher implements OutboxDispatcher {
    private final DispatchEngineService service;
    private final boolean enabled;

    public DispatchEngineDispatcher(
            DispatchEngineService service,
            @Value("${vi.jobs.invitation-enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @Override
    public boolean supports(String type) {
        return enabled && DispatchEngineService.EVENTS.contains(type);
    }

    @Override
    public void dispatch(ViOutbox event) {
        service.execute(event);
    }
}
