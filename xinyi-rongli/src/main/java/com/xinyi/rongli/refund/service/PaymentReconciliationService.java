package com.xinyi.rongli.refund.service;

import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.payment.service.PaymentLifecycleService;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.refund.domain.*;
import com.xinyi.rongli.refund.dto.RefundRequests.*;
import com.xinyi.rongli.refund.mapper.RefundMapper;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 对账异常是永久业务对象；查询可信渠道和登记一致性结案分离，文字理由不能改账或覆盖未知资金。 */
@Service
public class PaymentReconciliationService {
    public record Receipt(long caseId) {}

    private record Context(
            ReconciliationRecord row,
            ViOrder order,
            PaymentTransaction payment,
            RefundRecord refund) {}

    private final IntegrationAuthority authority;
    private final RefundExecutionService execution;
    private final RefundMapper mapper;
    private final PaymentMapper payments;
    private final PaymentLifecycleService lifecycle;
    private final ReconciliationTracker tracker;
    private final AuditService audit;
    private final PaymentOperationReason operationReason;
    private final IdempotencyService idem;
    private final TransactionTemplate tx;

    public PaymentReconciliationService(
            IntegrationAuthority authority,
            RefundExecutionService execution,
            RefundMapper mapper,
            PaymentMapper payments,
            PaymentLifecycleService lifecycle,
            ReconciliationTracker tracker,
            AuditService audit,
            PaymentOperationReason operationReason,
            IdempotencyService idem,
            PlatformTransactionManager manager) {
        this.authority = authority;
        this.execution = execution;
        this.mapper = mapper;
        this.payments = payments;
        this.lifecycle = lifecycle;
        this.tracker = tracker;
        this.audit = audit;
        this.operationReason = operationReason;
        this.idem = idem;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    private Context context(long id) {
        ReconciliationRecord snapshot = mapper.reconciliation(id);
        if (snapshot == null) invisible();
        ViOrder order = payments.lockOrder(snapshot.getOrderId());
        PaymentTransaction payment = payments.lockPayment(snapshot.getOrderId());
        if (order == null
                || payment == null
                || !Objects.equals(payment.getId(), snapshot.getPaymentId())) invisible();
        RefundRecord refund = null;
        if ("REFUND".equals(snapshot.getKind())) {
            var linked = execution.context(snapshot.getSubjectId());
            refund = linked.refund();
            if (!Objects.equals(refund.getPaymentId(), payment.getId())) invisible();
        } else if (!"PAYMENT".equals(snapshot.getKind())
                || !Objects.equals(snapshot.getSubjectId(), payment.getId())) invisible();
        return new Context(mapper.lockReconciliation(id), order, payment, refund);
    }

    public Map<String, Object> list(Query query) {
        Actor actor = authority.actor();
        return audited(
                actor,
                0,
                "RECONCILIATION_LIST",
                () ->
                        tx.execute(
                                status -> {
                                    authority.lockAuthority();
                                    Set<Long> scope =
                                            authority.scope("biz:paymentreconciliation:list");
                                    List<Map<String, Object>> rows = new ArrayList<>();
                                    for (var row :
                                            mapper.list(
                                                    scope,
                                                    (long) (query.pageNum() - 1) * query.pageSize(),
                                                    query.pageSize(),
                                                    query)) rows.add(view(context(row.getId())));
                                    return Map.of(
                                            "code",
                                            200,
                                            "msg",
                                            "操作成功",
                                            "rows",
                                            rows,
                                            "total",
                                            mapper.count(scope, query));
                                }));
    }

    public Map<String, Object> query(long id, Action request, String key) {
        Actor actor = authority.actor();
        return audited(
                actor, id, "RECONCILIATION_QUERY", () -> queryAction(id, request, key, actor));
    }

    private Map<String, Object> queryAction(long id, Action request, String key, Actor actor) {
        AtomicLong version = new AtomicLong(-1);
        tx.executeWithoutResult(status -> recordQueryIntent(id, request, key, actor, version));
        if (version.get() >= 0) queryChannel(id, actor, version.get());
        return tx.execute(status -> authorizedView(id, "biz:paymentreconciliation:query"));
    }

    private void recordQueryIntent(
            long id, Action request, String key, Actor actor, AtomicLong version) {
        authority.lockAuthority();
        Context context = context(id);
        execution.scope(context.payment(), "biz:paymentreconciliation:query");
        idem.execute(
                "ADMIN:" + actor.sysUserId(),
                "RECONCILIATION_QUERY:" + id,
                key,
                ViJson.write(request),
                true,
                Receipt.class,
                () -> acceptQuery(context, request, actor, version));
    }

    /** 首次查询意图、原因密文与幂等凭证同事务；渠道IO失败不能伪造可信核验时刻。 */
    private Receipt acceptQuery(Context context, Action request, Actor actor, AtomicLong version) {
        ReconciliationRecord row = context.row();
        if (row.getVersion() != request.expectedVersion()) conflict();
        if (!"WECHAT".equals(context.payment().getPaymentMethod()))
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "线下资金请通过实际凭证核实，不调用微信");
        row.setLocalStatus(local(context));
        row.setChannelStatus(null);
        row.setVerifiedAt(null);
        if (mapper.checked(row) != 1) conflict();
        version.set(row.getVersion() + 1);
        String envelope =
                operationReason.envelope(
                        actor, "RECONCILIATION_QUERY", row.getId(), request.reason());
        audit.recordSuccess(
                actor,
                "PAYMENT_RECONCILIATION",
                row.getId(),
                "RECONCILIATION_QUERY_INTENT",
                null,
                Map.of("reasonEnvelope", envelope),
                "人工操作说明已加密留存",
                row.getOrderId());
        return new Receipt(row.getId());
    }

    private void queryChannel(long id, Actor actor, long version) {
        String channel = null;
        try {
            Context snapshot =
                    tx.execute(status -> authorizedContext(id, "biz:paymentreconciliation:query"));
            channel =
                    snapshot.refund() == null
                            ? lifecycle.queryTrusted(snapshot.order().getId(), actor)
                            : execution.queryTrusted(snapshot.refund().getId(), actor);
        } catch (RuntimeException ignored) {
            // 未获可信结论时只保留异常，不能把失败当作未付或已退。
        }
        String verified = channel;
        tx.executeWithoutResult(status -> recordQueryResult(id, actor, version, verified));
    }

    private void recordQueryResult(long id, Actor actor, long version, String verified) {
        Context context = authorizedContext(id, "biz:paymentreconciliation:query");
        if (context.row().getVersion() != version) return;
        ReconciliationRecord row = context.row();
        row.setLocalStatus(local(context));
        row.setChannelStatus(verified);
        if (verified == null) {
            row.setReasonCode("RESULT_UNKNOWN");
            if (mapper.reopen(row) != 1) conflict();
        } else {
            row.setVerifiedAt(Instant.now());
            if (mapper.checked(row) != 1) conflict();
        }
        audit.recordSuccess(
                actor,
                "PAYMENT_RECONCILIATION",
                id,
                verified == null ? "RECONCILIATION_QUERY_UNKNOWN" : "RECONCILIATION_CHECKED",
                null,
                Map.of("channelStatus", verified == null ? "UNKNOWN" : verified),
                null,
                row.getOrderId());
    }

    public Map<String, Object> resolve(long id, Action request, String key) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "RECONCILIATION_RESOLVE",
                () -> tx.execute(status -> resolveAction(id, request, key, actor)));
    }

    private Map<String, Object> resolveAction(long id, Action request, String key, Actor actor) {
        Context context = authorizedContext(id, "biz:paymentreconciliation:resolve");
        idem.execute(
                "ADMIN:" + actor.sysUserId(),
                "RECONCILIATION_RESOLVE:" + id,
                key,
                ViJson.write(request),
                true,
                Receipt.class,
                () -> acceptResolution(context, request, actor));
        return view(context(id));
    }

    /** 理由只作为加密追溯资料；结案权限、可信终态、当前资金与容量一致性必须全部独立成立。 */
    private Receipt acceptResolution(Context context, Action request, Actor actor) {
        ReconciliationRecord row = context.row();
        if (row.getVersion() != request.expectedVersion()) conflict();
        consistent(context);
        if (mapper.resolve(row.getId(), row.getVersion(), actor.sysUserId()) != 1) conflict();
        String envelope =
                operationReason.envelope(
                        actor, "RECONCILIATION_RESOLVE", row.getId(), request.reason());
        audit.recordSuccess(
                actor,
                "PAYMENT_RECONCILIATION",
                row.getId(),
                "RECONCILIATION_RESOLVED",
                null,
                Map.of(
                        "kind",
                        row.getKind(),
                        "subjectId",
                        row.getSubjectId().toString(),
                        "reasonEnvelope",
                        envelope),
                "依据可信资金一致性结案，人工说明已加密留存",
                context.order().getId());
        return new Receipt(row.getId());
    }

    private Context authorizedContext(long id, String permission) {
        authority.lockAuthority();
        Context context = context(id);
        execution.scope(context.payment(), permission);
        return context;
    }

    private Map<String, Object> authorizedView(long id, String permission) {
        return view(authorizedContext(id, permission));
    }

    /** 后台工作器调用，原subject唯一键保证重复扫描只重开必要异常；不在GET里悄悄写对账记录。 */
    public int scan() {
        int count = 0;
        for (var problem : mapper.problems()) {
            tx.executeWithoutResult(
                    status -> {
                        execution.guard(Actor.system());
                        long order = ((Number) problem.get("order_id")).longValue();
                        payments.lockOrder(order);
                        PaymentTransaction payment = payments.lockPayment(order);
                        if (payment == null) return;
                        // 扫描结果只定位候选，领取目录/支付锁后重新确认异常仍存在，避免旧快照重开已处理记录。
                        String kind = problem.get("kind").toString();
                        if ("PAYMENT".equals(kind)) {
                            if (!"UNKNOWN".equals(payment.getStatus())
                                    && !"MANUAL_CONFLICT".equals(payment.getCapacityResolution()))
                                return;
                        } else {
                            RefundRecord current =
                                    mapper.find(((Number) problem.get("subject_id")).longValue());
                            if (current == null
                                    || !Objects.equals(current.getPaymentId(), payment.getId())
                                    || !Set.of("UNKNOWN", "ABNORMAL", "CLOSED")
                                            .contains(current.getStatus())
                                    || !Objects.equals(
                                            current.getStatus(), problem.get("local_status")))
                                return;
                        }
                        tracker.open(
                                problem.get("kind").toString(),
                                ((Number) problem.get("subject_id")).longValue(),
                                order,
                                payment.getId(),
                                problem.get("local_status").toString(),
                                null,
                                problem.get("reason_code").toString(),
                                Actor.system());
                    });
            count++;
        }
        return count;
    }

    private void consistent(Context context) {
        ReconciliationRecord row = context.row();
        PaymentTransaction payment = context.payment();
        if (!"CHECKING".equals(row.getStatus()) || row.getVerifiedAt() == null) unresolved();
        var balance = mapper.balance(payment.getId());
        var ledger = mapper.ledger(payment.getId());
        if (number(balance, "refund_reserved_fen") != number(ledger, "reserved")
                || number(balance, "refunded_fen") != number(ledger, "refunded")) unresolved();
        if (context.refund() != null) {
            RefundRecord refund = context.refund();
            if (!Set.of("SUCCESS", "CLOSED").contains(refund.getStatus())
                    || !refund.getStatus().equals(row.getChannelStatus())
                    || !refund.getStatus().equals(refund.getLastVerifiedStatus())) unresolved();
        } else {
            boolean paid =
                    "SUCCESS".equals(payment.getStatus())
                            && Set.of("SUCCESS", "REFUND").contains(row.getChannelStatus())
                            && payment.getPaidFen() == payment.getTotalFen()
                            && !"MANUAL_CONFLICT".equals(payment.getCapacityResolution());
            boolean closed =
                    "CLOSED".equals(payment.getStatus())
                            && "CLOSED".equals(row.getChannelStatus())
                            && payment.getPaidFen() == 0
                            && "RELEASED".equals(payment.getCapacityResolution());
            if (!paid && !closed) unresolved();
        }
    }

    private Map<String, Object> view(Context context) {
        var row = context.row();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("caseId", row.getId().toString());
        result.put("orderNo", context.order().getOrderNo());
        result.put("type", row.getKind());
        result.put("localStatus", local(context));
        result.put("channelStatus", row.getChannelStatus());
        result.put("status", row.getStatus());
        result.put("version", row.getVersion());
        result.put("subjectId", row.getSubjectId().toString());
        result.put("paymentMethod", context.payment().getPaymentMethod());
        result.put("refundNo", context.refund() == null ? null : context.refund().getOutRefundNo());
        result.put("reasonCode", row.getReasonCode());
        result.put("verifiedAt", RefundExecutionService.text(row.getVerifiedAt()));
        result.put("resolvedAt", RefundExecutionService.text(row.getResolvedAt()));
        List<String> actions = new ArrayList<>();
        if ("WECHAT".equals(context.payment().getPaymentMethod())
                && execution.can(context.payment(), "biz:paymentreconciliation:query"))
            actions.add("QUERY_RECONCILIATION");
        if (execution.can(context.payment(), "biz:paymentreconciliation:resolve")) {
            try {
                consistent(context);
                actions.add("RESOLVE_RECONCILIATION");
            } catch (ViException ignored) {
            }
        }
        result.put("allowedActions", actions);
        return result;
    }

    private static String local(Context context) {
        return context.refund() == null
                ? context.payment().getStatus()
                : context.refund().getStatus();
    }

    private static long number(Map<String, Object> values, String key) {
        return ((Number) values.get(key)).longValue();
    }

    private static void unresolved() {
        throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "可信渠道终态、资金或容量尚未核实一致，不能凭文字结案");
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
                    "PAYMENT_RECONCILIATION",
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
