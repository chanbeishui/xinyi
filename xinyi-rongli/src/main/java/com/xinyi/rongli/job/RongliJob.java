package com.xinyi.rongli.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.xinyi.rongli.service.BookingService;
import com.xinyi.rongli.service.DispatchService;
import com.xinyi.rongli.service.OutboxConsumer;

/**
 * 预约业务定时收尾任务。
 *
 * <p>全部任务使用 fixedDelay（上一轮结束后再计时），避免长事务叠加；
 * 每轮批量有上限，单条失败只记录日志不中断整批。周期可通过 sys_config 覆盖。</p>
 */
@Component
public class RongliJob
{
    private static final Logger log = LoggerFactory.getLogger(RongliJob.class);

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private OutboxConsumer outboxConsumer;

    @Autowired
    private com.xinyi.rongli.service.DriverQualificationService qualificationService;

    /** 年度复审到期扫描：置 EXPIRED 并停新承接（默认每 30 分钟，命中量极小） */
    @Scheduled(initialDelay = 90_000, fixedDelayString = "${rongli.job.qualificationExpiry:1800000}")
    public void expireQualifications()
    {
        try
        {
            int n = qualificationService.sweepExpiredQualifications();
            if (n > 0)
            {
                log.info("资格年审到期：本轮失效 {} 名代驾", n);
            }
        }
        catch (Exception e)
        {
            log.error("资格年审到期扫描异常", e);
        }
    }

    /** 到期订单启动派单（默认每 60 秒） */
    @Scheduled(initialDelay = 15_000, fixedDelayString = "${rongli.job.dispatch:60000}")
    public void dispatchDue()
    {
        try
        {
            int n = dispatchService.dispatchDueOrders();
            if (n > 0)
            {
                log.info("定时派单：本轮启动 {} 单", n);
            }
        }
        catch (Exception e)
        {
            log.error("定时派单异常", e);
        }
    }

    /** 邀约超时回收并轮转下一位司机（默认每 30 秒） */
    @Scheduled(initialDelay = 25_000, fixedDelayString = "${rongli.job.invite:30000}")
    public void expireInvites()
    {
        try
        {
            int n = dispatchService.expireInvites();
            if (n > 0)
            {
                log.info("邀约过期：本轮回收 {} 条", n);
            }
        }
        catch (Exception e)
        {
            log.error("邀约过期回收异常", e);
        }
    }

    /** 支付超时关单并释放时段名额（默认每 5 分钟） */
    @Scheduled(initialDelay = 40_000, fixedDelayString = "${rongli.job.payExpire:300000}")
    public void closeExpiredPayments()
    {
        try
        {
            int n = bookingService.closeExpiredPayments();
            if (n > 0)
            {
                log.info("支付超时关单：本轮关闭 {} 单", n);
            }
        }
        catch (Exception e)
        {
            log.error("支付超时关单异常", e);
        }
    }

    /** 车辆已归还且无争议的订单自动确认完成（默认每 10 分钟） */
    @Scheduled(initialDelay = 60_000, fixedDelayString = "${rongli.job.autoComplete:600000}")
    public void autoCompleteDue()
    {
        try
        {
            int n = bookingService.autoCompleteDue();
            if (n > 0)
            {
                log.info("自动确认完成：本轮完成 {} 单", n);
            }
        }
        catch (Exception e)
        {
            log.error("自动确认完成异常", e);
        }
    }

    /** outbox 事件投递（默认每 20 秒） */
    @Scheduled(initialDelay = 30_000, fixedDelayString = "${rongli.job.outbox:20000}")
    public void consumeOutbox()
    {
        try
        {
            outboxConsumer.consume();
        }
        catch (Exception e)
        {
            log.error("outbox 消费异常", e);
        }
    }
}
