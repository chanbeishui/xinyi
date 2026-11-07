package com.xinyi.rongli.refund.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.aftersale.domain.AftersaleCase;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.payment.service.PaymentHistoryGateway;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.refund.domain.RefundRecord;
import com.xinyi.rongli.refund.dto.RefundRequests.Action;
import com.xinyi.rongli.refund.gateway.WechatRefundGateway;
import com.xinyi.rongli.refund.mapper.RefundMapper;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 租约短事务→官方SDK IO→比对当前租约后落账；进程未知只查原号，原号受理不存在须可信签名证明。 */
@Service
public class RefundExecutionService {
    public record Receipt(long refundId) {}

    record Context(
            ViOrder order,
            PaymentTransaction payment,
            AftersaleCase aftersale,
            RefundRecord refund) {}

    private record Work(
            long id,
            String token,
            long version,
            boolean submit,
            boolean allowResubmit,
            Actor actor) {}

    private final IntegrationAuthority authority;
    private final com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper directory;
    private final RefundMapper mapper;
    private final PaymentMapper payments;
    private final RefundSettlementService settlement;
    private final WechatRefundGateway gateway;
    private final ReconciliationTracker reconciliation;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final PaymentOperationReason operationReason;
    private final TransactionTemplate tx;

    public RefundExecutionService(
            IntegrationAuthority authority,
            com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper directory,
            RefundMapper mapper,
            PaymentMapper payments,
            RefundSettlementService settlement,
            WechatRefundGateway gateway,
            ReconciliationTracker reconciliation,
            IdempotencyService idem,
            AuditService audit,
            PaymentOperationReason operationReason,
            PlatformTransactionManager manager) {
        this.authority = authority;
        this.directory = directory;
        this.mapper = mapper;
        this.payments = payments;
        this.settlement = settlement;
        this.gateway = gateway;
        this.reconciliation = reconciliation;
        this.idem = idem;
        this.audit = audit;
        this.operationReason = operationReason;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    void guard(Actor actor) {
        if (actor.isAdmin()) authority.lockAuthority();
        else {
            directory.ensureGuard();
            directory.lockGuard();
        }
    }

    void scope(PaymentTransaction payment, String permission) {
        Set<Long> scope = authority.scope(permission);
        if (scope != null && !scope.contains(payment.getReceiverDeptId()))
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    boolean can(PaymentTransaction payment, String permission) {
        if (!SecurityUtils.hasPermi(permission)) return false;
        Set<Long> scope = authority.scope(permission);
        return scope == null || scope.contains(payment.getReceiverDeptId());
    }

    Context context(long id) {
        RefundRecord snapshot = mapper.find(id);
        if (snapshot == null) invisible();
        Long orderId = mapper.orderId(snapshot.getPaymentId());
        if (orderId == null) invisible();
        ViOrder order = payments.lockOrder(orderId);
        PaymentTransaction payment = payments.lockPayment(orderId);
        if (order == null || payment == null) invisible();
        AftersaleCase aftersale = settlement.approved(order, payment, snapshot);
        return new Context(order, payment, aftersale, mapper.lock(id));
    }

    public Map<String, Object> get(String no) {
        Actor actor = authority.actor();
        return audited(
                actor,
                0,
                "REFUND_READ",
                () ->
                        tx.execute(
                                status -> {
                                    guard(actor);
                                    RefundRecord row = mapper.byNo(no);
                                    if (row == null) invisible();
                                    Context context = context(row.getId());
                                    scope(context.payment(), "biz:refund:query");
                                    return view(context);
                                }));
    }

    public Map<String, Object> action(String no, Action request, String key, boolean retry) {
        Actor actor = authority.actor();
        String action = retry ? "REFUND_RETRY" : "REFUND_QUERY";
        return audited(actor, 0, action, () -> performAction(no, request, key, retry, actor));
    }

    private Map<String, Object> performAction(
            String no, Action request, String key, boolean retry, Actor actor) {
        AtomicReference<Work> work = new AtomicReference<>();
        long id = tx.execute(status -> recordAction(no, request, key, retry, actor, work));
        if (work.get() != null) perform(work.get());
        return tx.execute(status -> actionResult(id, retry, actor));
    }

    private long recordAction(
            String no,
            Action request,
            String key,
            boolean retry,
            Actor actor,
            AtomicReference<Work> work) {
        guard(actor);
        RefundRecord row = mapper.byNo(no);
        if (row == null) invisible();
        Context context = context(row.getId());
        scope(context.payment(), retry ? "biz:refund:retry" : "biz:refund:query");
        String action = retry ? "REFUND_RETRY" : "REFUND_QUERY";
        Receipt receipt =
                idem.execute(
                        "ADMIN:" + actor.sysUserId(),
                        action + ":" + row.getId(),
                        key,
                        ViJson.write(request),
                        true,
                        Receipt.class,
                        () -> acceptAction(context, request, retry, actor, work));
        return receipt.refundId();
    }

    /** 幂等首次接受才留存人工说明；本地查询与终态读取也属于需要追溯的人工资金操作。 */
    private Receipt acceptAction(
            Context context,
            Action request,
            boolean retry,
            Actor actor,
            AtomicReference<Work> work) {
        RefundRecord row = context.refund();
        if (row.getVersion() != request.expectedVersion()) conflict();
        boolean offlineQuery = !retry && "OFFLINE".equals(row.getPaymentMethod());
        if (!offlineQuery) {
            wechat(row);
            boolean waitForReturn =
                    !retry
                            && row.getExecutionStartedAt() == null
                            && "WAIT_RETURN".equals(context.aftersale().getReturnState());
            if ("CLOSED".equals(row.getStatus()) && retry)
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "原退款已明确关闭，须独立修订批准，禁止原号重提");
            if (!waitForReturn && !Set.of("SUCCESS", "CLOSED").contains(row.getStatus()))
                work.set(claim(context, actor, retry, retry));
        }
        String action = retry ? "REFUND_RETRY" : "REFUND_QUERY";
        String envelope = operationReason.envelope(actor, action, row.getId(), request.reason());
        audit.recordSuccess(
                actor,
                "VI_REFUND_ORDER",
                row.getId(),
                action + "_ACCEPTED",
                null,
                Map.of("reasonEnvelope", envelope),
                "人工操作说明已加密留存",
                context.order().getId());
        return new Receipt(row.getId());
    }

    private Map<String, Object> actionResult(long id, boolean retry, Actor actor) {
        guard(actor);
        Context context = context(id);
        scope(context.payment(), retry ? "biz:refund:retry" : "biz:refund:query");
        return view(context);
    }

    private Work claim(Context context, Actor actor, boolean submitFirst, boolean allowResubmit) {
        RefundRecord row = context.refund();
        wechat(row);
        settlement.executable(context.order(), context.aftersale());
        if (context.aftersale().getReviewRevision() != row.getReviewRevision())
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "审批已修订，旧退款单不能继续执行");
        boolean submit =
                submitFirst
                        && row.getExecutionStartedAt() == null
                        && "CREATED".equals(row.getStatus());
        String token = UUID.randomUUID().toString();
        if (mapper.claim(row.getId(), row.getVersion(), token, submit) != 1)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "退款正在核查，请稍后刷新");
        audit.recordSuccess(
                actor,
                "VI_REFUND_ORDER",
                row.getId(),
                submit ? "REFUND_SUBMIT_INTENT" : "REFUND_QUERY_INTENT",
                null,
                Map.of(
                        "refundId",
                        row.getId().toString(),
                        "attempt",
                        row.getExecutionAttempts() + 1),
                null,
                context.order().getId());
        return new Work(row.getId(), token, row.getVersion() + 1, submit, allowResubmit, actor);
    }

    /** 内部调用仍验证原审核及实物条件；HTTP只暴露经过当前后台权限校验的action入口。 */
    public void execute(long id) {
        Actor actor = Actor.system();
        Work work =
                tx.execute(
                        status -> {
                            guard(actor);
                            Context context = context(id);
                            if (Set.of("SUCCESS", "CLOSED").contains(context.refund().getStatus()))
                                return null;
                            Instant now = Instant.now();
                            // 重复outbox/并发扫描是正常投递，不将仍持租约或退避中的工作当业务错误。
                            if (context.refund().getExecutionLeaseUntil() != null
                                            && context.refund()
                                                    .getExecutionLeaseUntil()
                                                    .isAfter(now)
                                    || context.refund().getNextQueryAt() != null
                                            && context.refund().getNextQueryAt().isAfter(now))
                                return null;
                            return claim(context, actor, true, false);
                        });
        if (work != null) perform(work);
    }

    private void perform(Work work) {
        AtomicReference<Work> current = new AtomicReference<>(work);
        try {
            RefundSettlementService.Fact fact;
            try {
                fact = work.submit() ? submit(work) : gateway.query(work.id());
            } catch (PaymentHistoryGateway.RefundNotFound missing) {
                if (!work.allowResubmit()) {
                    unknown(work, "REFUND_NOT_FOUND");
                    return;
                }
                // 只有当前同一租约仍有效且原审批/实物条件未变，可信404后才允许原号原参提交。
                tx.executeWithoutResult(
                        status -> {
                            guard(work.actor());
                            Context context = context(work.id());
                            requireLease(context.refund(), work);
                            settlement.executable(context.order(), context.aftersale());
                            if (mapper.markSubmission(work.id(), work.version(), work.token()) != 1)
                                conflict();
                            current.set(
                                    new Work(
                                            work.id(),
                                            work.token(),
                                            work.version() + 1,
                                            true,
                                            work.allowResubmit(),
                                            work.actor()));
                        });
                fact = submit(current.get());
            }
            var verified = fact;
            tx.executeWithoutResult(
                    status -> {
                        // 渠道事实无论操作者会话是否刚撤销都应落账；不再授予新的网络动作或读权限。
                        directory.ensureGuard();
                        directory.lockGuard();
                        Context context = context(work.id());
                        if (!lease(context.refund(), current.get())) return;
                        settlement.applyLocked(
                                context.order(),
                                context.payment(),
                                work.id(),
                                verified,
                                work.actor());
                    });
        } catch (RuntimeException error) {
            unknown(current.get(), "RESULT_UNKNOWN");
        }
    }

    private RefundSettlementService.Fact submit(Work work) {
        gateway.preflight(work.id());
        tx.executeWithoutResult(
                status -> {
                    guard(work.actor());
                    Context context = context(work.id());
                    if (work.actor().isAdmin()) scope(context.payment(), "biz:refund:retry");
                    requireLease(context.refund(), work);
                    settlement.executable(context.order(), context.aftersale());
                    if (context.aftersale().getReviewRevision()
                            != context.refund().getReviewRevision())
                        throw new ViException(
                                ViErrorCode.VI_STATE_CONFLICT, "审批已修订，旧退款单不能发起新的渠道请求");
                });
        // 支付预查可能等待数秒，撤权/改审批/交接必须在POST前重新检查；网络始终不持数据库事务。
        return gateway.submitVerified(work.id());
    }

    private void unknown(Work work, String code) {
        tx.executeWithoutResult(
                status -> {
                    directory.ensureGuard();
                    directory.lockGuard();
                    Context context = context(work.id());
                    if (!lease(context.refund(), work)) return;
                    RefundRecord row = context.refund();
                    row.setStatus("UNKNOWN");
                    row.setLastErrorCode(code);
                    if (mapper.finish(row, work.token()) != 1) conflict();
                    mapper.settleCase(row.getCaseId(), "UNKNOWN", false);
                    reconciliation.open(
                            "REFUND",
                            row.getId(),
                            context.order().getId(),
                            context.payment().getId(),
                            "UNKNOWN",
                            null,
                            code,
                            work.actor());
                    audit.recordSuccess(
                            work.actor(),
                            "VI_REFUND_ORDER",
                            row.getId(),
                            "REFUND_RESULT_UNKNOWN",
                            null,
                            Map.of("reasonCode", code),
                            null,
                            context.order().getId());
                });
    }

    /** 对账重查只GET，不会在404时发POST；可核验已经终态的退款以取得结案证据。 */
    String queryTrusted(long id, Actor actor) {
        RefundSettlementService.Fact fact = gateway.query(id);
        tx.executeWithoutResult(
                status -> {
                    directory.ensureGuard();
                    directory.lockGuard();
                    Context context = context(id);
                    settlement.applyLocked(context.order(), context.payment(), id, fact, actor);
                });
        return fact.status();
    }

    public int runDue() {
        int count = 0;
        for (long id : mapper.due()) {
            try {
                execute(id);
                count++;
            } catch (RuntimeException failure) {
                audit.recordFailure(
                        Actor.system(),
                        "VI_REFUND_ORDER",
                        id,
                        "REFUND_EXECUTION_DEFERRED",
                        null,
                        null,
                        "STATE_OR_AUTHORITY_UNAVAILABLE");
            }
        }
        return count;
    }

    Map<String, Object> view(Context context) {
        RefundRecord row = context.refund();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("refundNo", row.getOutRefundNo());
        result.put("orderNo", context.order().getOrderNo());
        result.put("amount", row.getAmountFen());
        result.put("status", row.getStatus());
        result.put("channelRefundId", row.getChannelRefundId());
        result.put("successAt", text(row.getSuccessAt()));
        result.put("version", row.getVersion());
        result.put("caseId", row.getCaseId().toString());
        result.put("reviewRevision", row.getReviewRevision());
        result.put("paymentMethod", row.getPaymentMethod());
        result.put("executionMode", row.getExecutionMode());
        result.put("manualProofState", null);
        result.put("returnState", context.aftersale().getReturnState());
        result.put("executionStartedAt", text(row.getExecutionStartedAt()));
        result.put("lastErrorCode", row.getLastErrorCode());
        result.put("lastVerifiedAt", text(row.getLastVerifiedAt()));
        result.put(
                "queryRequired",
                !Set.of("SUCCESS", "CLOSED").contains(row.getStatus())
                        && "WECHAT".equals(row.getPaymentMethod()));
        var balance = mapperBalance(context.payment());
        result.putAll(balance);
        List<String> actions = new ArrayList<>();
        if ("WECHAT".equals(row.getPaymentMethod())) {
            if (can(context.payment(), "biz:refund:query")) actions.add("QUERY_REFUND");
            if (!Set.of("SUCCESS", "CLOSED").contains(row.getStatus())
                    && can(context.payment(), "biz:refund:retry")) {
                try {
                    settlement.executable(context.order(), context.aftersale());
                    actions.add("RETRY_REFUND");
                } catch (ViException ignored) {
                }
            }
        }
        result.put("allowedActions", actions);
        return result;
    }

    private Map<String, Object> mapperBalance(PaymentTransaction payment) {
        // 原Payment实体只投影paid_fen，另外两项必须从当前已持锁的支付原记录读取。
        return balanceMapper(payment.getId());
    }

    private Map<String, Object> balanceMapper(long paymentId) {
        var value = mapper.balance(paymentId);
        return Map.of(
                "paidAmount",
                ((Number) value.get("paid_fen")).longValue(),
                "refundedAmount",
                ((Number) value.get("refunded_fen")).longValue(),
                "reservedAmount",
                ((Number) value.get("refund_reserved_fen")).longValue());
    }

    static String text(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private static boolean lease(RefundRecord row, Work work) {
        return Objects.equals(row.getExecutionLeaseToken(), work.token())
                && row.getVersion() == work.version();
    }

    private static void requireLease(RefundRecord row, Work work) {
        if (!lease(row, work)) conflict();
    }

    private static void wechat(RefundRecord row) {
        if (!"WECHAT".equals(row.getPaymentMethod())
                || !"WECHAT_API".equals(row.getExecutionMode()))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "线下退款请查看人工凭证核实进度，不调用微信");
    }

    private static void invisible() {
        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    private static void conflict() {
        throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
    }

    private <T> T audited(Actor actor, long id, String action, Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "VI_REFUND_ORDER",
                    id,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException failure
                            ? failure.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }
}
