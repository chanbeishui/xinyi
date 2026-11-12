package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.service.OutboxDispatcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 仅处理服务端真实开启/收紧后的到期任务，不接收客户端到期或次日自动开启指令。 */
@Component
public class ListeningExpiryDispatcher implements OutboxDispatcher {
    private final ListeningDeadlineService service;
    private final boolean enabled;

    public ListeningExpiryDispatcher(
            ListeningDeadlineService service,
            @Value("${vi.jobs.listening-enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @Override
    public boolean supports(String type) {
        return enabled && ListeningDeadlineService.TIMER.equals(type);
    }

    @Override
    public void dispatch(ViOutbox event) {
        service.execute(event);
    }
}
