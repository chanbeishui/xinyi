package com.xinyi.rongli.jobs.service;

import com.xinyi.rongli.config.mapper.ConfigMapper;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import org.springframework.stereotype.Service;

/** 使用C01全局业务参数；订单权威部门仍取实际站点，不从参数维护人推断。 */
@Service
public class DispatchPlan {
    public static final String LEAD_KEY = "vi.biz.dispatch.lead.minutes";
    private final ConfigMapper config;

    public DispatchPlan(ConfigMapper config) {
        this.config = config;
    }

    public static int minutes(String value) {
        if (value == null || !value.matches("[0-9]{1,5}")) invalid();
        int minutes = Integer.parseInt(value);
        if (minutes > 10080) invalid();
        return minutes;
    }

    private static void invalid() {
        throw new ViException(
                ViErrorCode.VI_CONFIG_CHANGED,
                "请在业务配置中设置派单提前分钟 vi.biz.dispatch.lead.minutes（0—10080整数）");
    }

    /** 调用方已持有vi.config.version锁；时间与版本在建单事务中冻结，不追改旧预约。 */
    public void snapshot(ViOrder order) {
        int lead = minutes(config.selectValue(LEAD_KEY));
        order.setDispatchAt(order.getPickupStartsAt().minusSeconds(lead * 60L));
        order.setDispatchConfigVersion(config.selectVersion());
    }
}
