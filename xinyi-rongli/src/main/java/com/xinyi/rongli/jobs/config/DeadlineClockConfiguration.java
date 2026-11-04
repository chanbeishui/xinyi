package com.xinyi.rongli.jobs.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 时间任务统一使用可注入UTC时钟；测试推进时钟，不等待真实预约日期。 */
@Configuration
public class DeadlineClockConfiguration {
    @Bean("viDeadlineClock")
    public Clock deadlineClock() {
        return Clock.systemUTC();
    }
}
