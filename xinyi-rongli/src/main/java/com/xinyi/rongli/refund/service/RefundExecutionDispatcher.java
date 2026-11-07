package com.xinyi.rongli.refund.service;

import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.OutboxDispatcher;
import com.xinyi.rongli.refund.mapper.RefundMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 载荷只定位申请/版本，退款金额和权限仍从数据库真实父记录读取。 */
@Component
public class RefundExecutionDispatcher implements OutboxDispatcher {
    private final RefundExecutionService service;
    private final RefundMapper mapper;
    private final boolean enabled;

    public RefundExecutionDispatcher(
            RefundExecutionService service,
            RefundMapper mapper,
            @Value("${vi.jobs.refund-enabled:false}") boolean enabled) {
        this.service = service;
        this.mapper = mapper;
        this.enabled = enabled;
    }

    @Override
    public boolean supports(String type) {
        return enabled && "REFUND_EXECUTION_REQUESTED".equals(type);
    }

    @Override
    public void dispatch(ViOutbox event) {
        var payload = ViJson.tree(event.getPayload());
        var row =
                mapper.byCase(
                        Long.parseLong(payload.path("caseId").asText()),
                        payload.path("reviewRevision").asInt());
        if (row != null) service.execute(row.getId());
    }
}
