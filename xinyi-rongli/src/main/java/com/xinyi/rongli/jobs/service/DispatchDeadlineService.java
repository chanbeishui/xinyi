package com.xinyi.rongli.jobs.service;

import com.xinyi.rongli.jobs.mapper.DispatchDeadlineMapper;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.domain.ViOutbox;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** 订单到期只进入待派单；D02负责选司机/邀约，P03不提前占用司机。 */
@Service
public class DispatchDeadlineService {
    public static final String TIMER = "ORDER_DISPATCH_DUE";
    private final PaymentMapper payments;
    private final DispatchDeadlineMapper mapper;
    private final EmployeeDirectoryGuardMapper directory;
    private final OutboxService outbox;
    private final AuditService audit;
    private final Clock clock;
    private final TransactionTemplate tx;

    public DispatchDeadlineService(
            PaymentMapper payments,
            DispatchDeadlineMapper mapper,
            EmployeeDirectoryGuardMapper directory,
            OutboxService outbox,
            AuditService audit,
            @Qualifier("viDeadlineClock") Clock clock,
            PlatformTransactionManager manager) {
        this.payments = payments;
        this.mapper = mapper;
        this.directory = directory;
        this.outbox = outbox;
        this.audit = audit;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /** 支付可信入账后在原事务登记：资金、正式额度、计划任务任一失败全部回滚。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void onPaid(long orderId) {
        register(payments.lockOrder(orderId), true);
    }

    /** 只补缺失任务，不用最新配置猜测旧单计划；永久事件键避免重启后重建已完成任务。 */
    public int repairMissing() {
        int count = 0;
        for (Long id : mapper.missingTimers()) {
            tx.executeWithoutResult(
                    status -> {
                        lockDirectory();
                        register(payments.lockOrder(id), false);
                    });
            count++;
        }
        return count;
    }

    private void register(ViOrder order, boolean fromPayment) {
        if (order == null || !"SCHEDULED".equals(order.getFulfillmentStatus())) return;
        var payment = payments.lockPayment(order.getId());
        if (payment == null || !"SUCCESS".equals(payment.getStatus())) return;
        if (order.getDispatchAt() == null || order.getDispatchConfigVersion() == null) {
            signal(order, "DISPATCH_PLAN_MISSING", "历史订单缺少原计划快照，须人工处理，禁止用当前参数回填");
            return;
        }
        outbox.publishAt(
                "DISPATCH_TIMER:" + order.getId(),
                "VI_ORDER",
                order.getId(),
                TIMER,
                Map.of("orderId", order.getId(), "planVersion", order.getDispatchConfigVersion()),
                order.getDispatchAt());
        // 付款已过计划时点时不等待下轮扫描；仍在支付事务内验证运营条件及最短提前量。
        if (fromPayment && !clock.instant().isBefore(order.getDispatchAt()))
            advanceLocked(order, true);
    }

    public void execute(ViOutbox event) {
        if (!TIMER.equals(event.getEventType()) || !"VI_ORDER".equals(event.getAggregateType()))
            throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
        tx.executeWithoutResult(
                status -> {
                    // 与支付、权限变更共用锁顺序；outbox领取事务已结束，不持任务锁反向等待订单锁。
                    lockDirectory();
                    ViOrder order = payments.lockOrder(event.getAggregateId());
                    if (order == null || !"SCHEDULED".equals(order.getFulfillmentStatus())) return;
                    long expectedPlan =
                            ViJson.tree(event.getPayload()).path("planVersion").asLong(-1);
                    if (order.getDispatchConfigVersion() == null
                            || expectedPlan != order.getDispatchConfigVersion()) {
                        signal(order, "DISPATCH_PLAN_CONFLICT", "任务与订单计划版本不同，禁止用旧任务推进新计划");
                        return;
                    }
                    advanceLocked(order, false);
                });
    }

    private void advanceLocked(ViOrder order, boolean fromPayment) {
        Instant now = clock.instant();
        if (order.getDispatchAt() == null || now.isBefore(order.getDispatchAt()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "尚未到计划派单时间");
        if (!"NONE".equals(order.getCancelStatus()) || order.getActiveAssignmentId() != null) {
            signal(order, "DISPATCH_BLOCKED", "取消流程或有效承接阻止自动派单，恢复须显式重新安排");
            return;
        }
        var payment = payments.lockPayment(order.getId());
        // 钱款与两份正式额度都以原订单真实记录为准；不信任任务payload中的权限、金额或站点。
        boolean funded =
                payment != null
                        && "SUCCESS".equals(payment.getStatus())
                        && payment.getPaidFen() == order.getTotalFen()
                        && !"MANUAL_CONFLICT".equals(payment.getCapacityResolution())
                        && !"RELEASED".equals(payment.getCapacityResolution())
                        && mapper.pendingRefund(order.getId()) == 0;
        boolean available =
                mapper.stationAvailable(order.getStationId(), order.getDeptId()) == 1
                        && mapper.confirmedReservations(order.getId()) == 2;
        boolean inTime = order.getPickupStartsAt().isAfter(now);
        if (fromPayment && inTime) {
            String json = mapper.quotePolicy(order.getQuoteId());
            var lead = json == null ? null : ViJson.tree(json).path("policy").path("lead");
            // 缺失旧报价最短提前量时转协调，不把未知当0；不影响已经真实到账的资金事实。
            inTime =
                    lead != null
                            && lead.isIntegralNumber()
                            && lead.asLong() >= 0
                            && lead.asLong() <= 604800
                            && order.getPickupStartsAt().isAfter(now.plusSeconds(lead.asLong()));
        }
        String next = funded && available && inTime ? "PENDING_DISPATCH" : "MANUAL_DISPATCH";
        if (mapper.transition(order.getId(), order.getVersion(), next) != 1)
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        String action =
                "PENDING_DISPATCH".equals(next) ? "DISPATCH_REQUESTED" : "DISPATCH_REVIEW_REQUIRED";
        signal(
                order,
                action,
                "PENDING_DISPATCH".equals(next)
                        ? "到点且资金、额度和站点有效，登记派单请求"
                        : "时效、资金或运营条件不满足，保留原资金与额度等待人工协调");
    }

    private void signal(ViOrder order, String action, String reason) {
        boolean inserted =
                outbox.publish(
                        action + ":" + order.getId(),
                        "VI_ORDER",
                        order.getId(),
                        action,
                        Map.of(
                                "orderId",
                                order.getId(),
                                "stationId",
                                order.getStationId(),
                                "deptId",
                                order.getDeptId()));
        if (inserted)
            audit.recordSuccess(
                    Actor.system(),
                    "VI_ORDER",
                    order.getId(),
                    action,
                    null,
                    Map.of("stationId", order.getStationId(), "deptId", order.getDeptId()),
                    reason,
                    order.getId());
    }

    private void lockDirectory() {
        directory.ensureGuard();
        directory.lockGuard();
    }
}
