package com.xinyi.rongli.platform.service;

import com.xinyi.rongli.platform.config.ViPlatformProperties;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.mapper.ViOutboxMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * vi 业务 outbox 中继（与框架 SecurityOutboxProcessor 各自独立 Bean，互不影响；
 * 不注册第二个 SessionInvalidator——ADR-02）。
 *
 * 时钟来自 DB 到期扫描，不依赖 Redis 过期通知（ADR-08）。三段式：
 * 1) 租约恢复：回收 lease 过期的滞留 PROCESSING；
 * 2) 领取（短事务）：FOR UPDATE SKIP LOCKED 选到期 READY，仅领取“已有分发器”的事件，CAS 置 PROCESSING + 租约；
 * 3) 分发（事务外）+ 结算（短事务）：成功置 DONE；失败 attempts+1 退避，达上限置 FAILED。
 *
 * 无分发器的事件类型保持 READY，待对应业务任务注册 OutboxDispatcher 后再投递（不计入 attempts）。
 */
@Component
public class ViOutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(ViOutboxRelay.class);

    private final ViOutboxMapper mapper;
    private final ObjectProvider<List<OutboxDispatcher>> dispatchers;
    private final ViPlatformProperties properties;
    private final TransactionTemplate tx;

    public ViOutboxRelay(ViOutboxMapper mapper,
                         ObjectProvider<List<OutboxDispatcher>> dispatchers,
                         ViPlatformProperties properties,
                         PlatformTransactionManager txManager) {
        this.mapper = mapper;
        this.dispatchers = dispatchers;
        this.properties = properties;
        this.tx = new TransactionTemplate(txManager);
    }

    @Scheduled(fixedDelayString = "${vi.outbox-fixed-delay:5000}")
    public void scheduled() {
        if (properties.isOutboxRelayEnabled()) {
            try {
                runOnce();
            } catch (RuntimeException e) {
                log.warn("vi outbox 中继本轮异常: {}", e.getMessage());
            }
        }
    }

    /** 执行一轮中继；返回本轮成功结算（DONE/FAILED）的事件数。测试可禁用调度后直接调用。 */
    public int runOnce() {
        Instant now = Instant.now();
        tx.executeWithoutResult(s -> mapper.recoverExpiredLeases(now));

        List<ViOutbox> claimed = tx.execute(s -> claimDue(now));
        if (claimed == null || claimed.isEmpty()) {
            return 0;
        }
        int settled = 0;
        for (ViOutbox event : claimed) {
            if (settle(event)) {
                settled++;
            }
        }
        return settled;
    }

    private List<ViOutbox> claimDue(Instant now) {
        List<ViOutbox> due = mapper.selectDueForUpdate(now, properties.getOutboxBatchSize());
        Instant leaseUntil = now.plusSeconds(properties.getOutboxLeaseSeconds());
        List<ViOutbox> claimed = new ArrayList<>();
        for (ViOutbox e : due) {
            if (findDispatcher(e.getEventType()) == null) {
                continue; // 无分发器，留 READY，不计 attempts
            }
            int n = mapper.claim(e.getId(), leaseUntil, e.getVersion());
            if (n == 1) {
                e.setVersion(e.getVersion() + 1);
                claimed.add(e);
            }
        }
        return claimed;
    }

    private boolean settle(ViOutbox event) {
        OutboxDispatcher dispatcher = findDispatcher(event.getEventType());
        try {
            dispatcher.dispatch(event);
            Integer n = tx.execute(s -> mapper.markDone(event.getId(), event.getVersion()));
            if (n != null && n == 1) {
                return true;
            }
            log.warn("vi outbox[{}] DONE 结算未命中（version={}），可能已被其他工作者结算", event.getId(), event.getVersion());
            return false;
        } catch (RuntimeException ex) {
            int attempts = event.getAttempts() + 1;
            boolean terminal = attempts >= properties.getOutboxMaxAttempts();
            Instant next = Instant.now().plusSeconds(backoffSeconds(attempts));
            Integer n = tx.execute(s -> mapper.markFailed(event.getId(), attempts, next, terminal, event.getVersion()));
            log.warn("vi outbox[{}] type={} 第{}次分发失败{}: {}", event.getId(), event.getEventType(),
                    attempts, terminal ? "（已达上限置 FAILED）" : "", ex.getMessage());
            return n != null && n == 1;
        }
    }

    private OutboxDispatcher findDispatcher(String eventType) {
        List<OutboxDispatcher> list = dispatchers.getIfAvailable();
        if (list == null) {
            return null;
        }
        for (OutboxDispatcher d : list) {
            if (d.supports(eventType)) {
                return d;
            }
        }
        return null;
    }

    /** 指数退避，封顶 1 小时（与框架安全 outbox 同量级）。 */
    static long backoffSeconds(int attempts) {
        int shift = Math.min(Math.max(attempts, 1), 12);
        return Math.min(3600L, 1L << shift);
    }
}
