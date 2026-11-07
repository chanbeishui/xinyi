package com.xinyi.rongli.payment.service;

import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.*;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.order.service.OrderReadService;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.service.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** 所有可信资金结论经同一事务落库；网络请求在事务外，不以客户端状态推进资金。 */
@Service
public class PaymentLifecycleService {
    private final com.xinyi.rongli.jobs.service.DispatchDeadlineService dispatch;
    private final PaymentMapper payments;
    private final com.xinyi.rongli.refund.service.RefundSettlementService refundSettlement;
    private final com.xinyi.rongli.refund.service.ReconciliationTracker reconciliation;
    private final PaymentLifecycleMapper mapper;
    private final PaymentService intents;
    private final PaymentHistoryGateway gateway;
    private final PaymentConfiguration configuration;
    private final BusinessActorResolver actors;
    private final EmployeeDirectoryGuardMapper directory;
    private final AuditService audit;
    private final OutboxService outbox;
    private final IdempotencyService idem;
    private final OrderReadService orders;
    private final TransactionTemplate tx;

    public PaymentLifecycleService(
            com.xinyi.rongli.jobs.service.DispatchDeadlineService dispatch,
            PaymentMapper payments,
            com.xinyi.rongli.refund.service.RefundSettlementService refundSettlement,
            com.xinyi.rongli.refund.service.ReconciliationTracker reconciliation,
            PaymentLifecycleMapper mapper,
            PaymentService intents,
            PaymentHistoryGateway gateway,
            PaymentConfiguration configuration,
            BusinessActorResolver actors,
            EmployeeDirectoryGuardMapper directory,
            AuditService audit,
            OutboxService outbox,
            IdempotencyService idem,
            OrderReadService orders,
            PlatformTransactionManager manager) {
        this.dispatch = dispatch;
        this.payments = payments;
        this.refundSettlement = refundSettlement;
        this.reconciliation = reconciliation;
        this.mapper = mapper;
        this.intents = intents;
        this.gateway = gateway;
        this.configuration = configuration;
        this.actors = actors;
        this.directory = directory;
        this.audit = audit;
        this.outbox = outbox;
        this.idem = idem;
        this.orders = orders;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    // 与身份变更、预支付共用锁顺序：目录守卫 → 订单 → 支付单 → 额度，避免并发反向取锁。
    private void lock() {
        directory.ensureGuard();
        directory.lockGuard();
    }

    private Actor owner() {
        var a = actors.requireMini();
        if (!"OWNER".equals(a.actingRole())) throw new ViException(ViErrorCode.VI_FORBIDDEN);
        return a;
    }

    private ViOrder owned(Actor a, String no) {
        intents.guard(a);
        var o = payments.lockOwnedOrder(no, a.accountId());
        if (o == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return o;
    }

    /** 查询可能补记渠道结果；网络返回后再次校验会话与订单归属，再向本人返回最新状态。 */
    public Map<String, Object> read(String no) {
        var a = owner();
        try {
            long id = tx.execute(s -> owned(a, no).getId());
            reconcile(id, a);
            return tx.execute(
                    s -> {
                        var o = owned(a, no);
                        var p = payments.lockPayment(o.getId());
                        audit.recordSuccess(
                                a,
                                "VI_PAYMENT_ORDER",
                                p.getId(),
                                "PAYMENT_RESULT_READ",
                                null,
                                Map.of("orderId", id),
                                null);
                        return view(p);
                    });
        } catch (RuntimeException e) {
            reject(a, "PAYMENT_READ_REJECTED", e);
            throw e;
        }
    }

    public record CloseReceipt(long orderId) {}

    /** 先持久化关闭意图，再查渠道；超时仍保留意图和额度，重试复用原幂等请求。 */
    public Object close(String no, long version, String reason, String key) {
        var a = owner();
        try {
            long id = tx.execute(status -> requestCloseLocked(a, no, version, reason, key));
            reconcile(id, a);
            return orders.detail("OWNER", no);
        } catch (RuntimeException e) {
            reject(a, "PAYMENT_CLOSE_REJECTED", e);
            throw e;
        }
    }

    private long requestCloseLocked(
            Actor actor, String orderNo, long version, String reason, String key) {
        var order = owned(actor, orderNo);
        var payment = payments.lockPayment(order.getId());
        return idem.execute(
                        IdempotencyService.subjectKey(actor),
                        "POST /api/app/owner/orders/" + orderNo + "/close-unpaid",
                        key,
                        ViJson.write(Map.of("expectedVersion", version, "reason", reason)),
                        true,
                        CloseReceipt.class,
                        () -> recordCloseIntent(actor, order, payment, version, reason))
                .orderId();
    }

    // 必须在持有订单/支付锁的同一事务内调用；关闭意图、成功审计、待处理事件一起提交。
    private CloseReceipt recordCloseIntent(
            Actor actor, ViOrder order, PaymentTransaction payment, long version, String reason) {
        if (order.getVersion() != version) {
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        }
        if (!"PENDING_PAY".equals(order.getFulfillmentStatus())
                || !Set.of("PENDING", "UNKNOWN").contains(payment.getStatus())
                || payment.getPaidFen() != 0) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "当前不能按未付订单关闭");
        }
        one(mapper.requestClose(payment.getId()));
        audit.recordSuccess(
                actor,
                "VI_PAYMENT_ORDER",
                payment.getId(),
                "PAYMENT_CLOSE_REQUESTED",
                null,
                Map.of("orderId", order.getId()),
                reason,
                order.getId());
        outbox.publish(
                "PAYMENT_CLOSE_REQUESTED:" + payment.getId(),
                "VI_PAYMENT_ORDER",
                payment.getId(),
                "PAYMENT_CLOSE_REQUESTED",
                Map.of("orderId", order.getId()));
        return new CloseReceipt(order.getId());
    }

    /** 单笔失败不阻断其他订单；下次任务按数据库中的重试时点继续，不依赖内存队列。 */
    public int runDue() {
        int n = 0;
        for (long id : mapper.due()) {
            try {
                reconcile(id, Actor.system());
                n++;
            } catch (RuntimeException e) {
                reject(Actor.system(), "PAYMENT_RECONCILE_RETRY", e);
            }
        }
        return n;
    }

    private record Work(long orderId, boolean close) {}

    /** 短事务领取查询租约 → 事务外访问渠道 → 新事务落账；查单与回调共用资金落账方法。 */
    private void reconcile(long id, Actor actor) {
        Work work =
                tx.execute(
                        s -> {
                            lock();
                            var o = payments.lockOrder(id);
                            if (o == null) return null;
                            var p = payments.lockPayment(id);
                            if (p == null || !Set.of("PENDING", "UNKNOWN").contains(p.getStatus()))
                                return null;
                            boolean closing =
                                    p.getCloseRequestedAt() != null
                                            || !p.getExpiresAt()
                                                    .isAfter(Instant.parse(payments.now()));
                            if (!"WECHAT".equals(p.getPaymentMethod())) {
                                if (closing) closeLocked(o, p, actor);
                                return null;
                            }
                            // 已经持久化的关闭意图阻止新预支付；在途预支付租约结束前不能据渠道暂时未付释放额度。
                            if (closing && p.getCloseRequestedAt() == null)
                                one(mapper.requestClose(p.getId()));
                            if ("REQUESTING".equals(p.getPrepayState())
                                    && p.getPrepayLeaseUntil() != null
                                    && p.getPrepayLeaseUntil()
                                            .isAfter(Instant.parse(payments.now()))) return null;
                            // 数据库短租约限制多实例重复查单；进程退出后到期即可重新领取。
                            if (mapper.claimQuery(p.getId()) == 0) return null;
                            return new Work(id, closing);
                        });
        if (work == null) return;
        try {
            var result = gateway.queryOrder(id);
            String state =
                    result.getTradeState() == null ? "UNKNOWN" : result.getTradeState().name();
            // 关单请求受理不等于已经关闭；再次查单可识别与付款同时发生的竞争。
            if (work.close() && "NOTPAY".equals(state)) {
                gateway.closeOrder(id);
                result = gateway.queryOrder(id);
            }
            final Transaction fact = result;
            tx.executeWithoutResult(
                    s -> {
                        lock();
                        var o = payments.lockOrder(id);
                        var p = payments.lockPayment(id);
                        String status =
                                fact.getTradeState() == null
                                        ? "UNKNOWN"
                                        : fact.getTradeState().name();
                        if ("SUCCESS".equals(status))
                            paidLocked(
                                    o,
                                    p,
                                    fact.getTransactionId(),
                                    instant(fact.getSuccessTime()),
                                    actor);
                        else if ("CLOSED".equals(status)) closeLocked(o, p, actor);
                        else if (!"SUCCESS".equals(p.getStatus())
                                && !"CLOSED".equals(p.getStatus())) {
                            // 明确未付与无法确定结果分开；关闭中的 NOTPAY 仍不能据此释放额度。
                            if ("NOTPAY".equals(status) && !work.close()) mapper.pending(p.getId());
                            else mapper.unknown(p.getId());
                            if (!"NOTPAY".equals(status) || work.close())
                                reconciliation.open(
                                        "PAYMENT",
                                        p.getId(),
                                        o.getId(),
                                        p.getId(),
                                        "UNKNOWN",
                                        status,
                                        "PAYMENT_UNKNOWN",
                                        actor);
                            audit.recordSuccess(
                                    actor,
                                    "VI_PAYMENT_ORDER",
                                    p.getId(),
                                    "PAYMENT_QUERY_PENDING",
                                    null,
                                    Map.of("providerState", status),
                                    null,
                                    o.getId());
                        }
                    });
        } catch (RuntimeException e) {
            tx.executeWithoutResult(
                    s -> {
                        lock();
                        payments.lockOrder(id);
                        var p = payments.lockPayment(id);
                        if (p != null && mapper.unknown(p.getId()) > 0) {
                            reconciliation.open(
                                    "PAYMENT",
                                    p.getId(),
                                    id,
                                    p.getId(),
                                    "UNKNOWN",
                                    null,
                                    "PAYMENT_UNKNOWN",
                                    actor);
                            audit.recordSuccess(
                                    actor,
                                    "VI_PAYMENT_ORDER",
                                    p.getId(),
                                    "PAYMENT_QUERY_UNKNOWN",
                                    null,
                                    Map.of("orderId", id),
                                    "渠道结论未确认",
                                    id);
                            outbox.publish(
                                    "PAYMENT_QUERY_UNKNOWN:" + p.getId(),
                                    "VI_PAYMENT_ORDER",
                                    p.getId(),
                                    "PAYMENT_QUERY_UNKNOWN",
                                    Map.of("orderId", id));
                        }
                    });
        }
    }

    private void paidLocked(
            ViOrder o, PaymentTransaction p, String transactionId, Instant paidAt, Actor actor) {
        if (transactionId == null
                || !transactionId.matches("[A-Za-z0-9_-]{1,64}")
                || paidAt == null
                || paidAt.isAfter(Instant.parse(payments.now()).plusSeconds(300))) invalid();
        if (p.getPaidFen() > 0) {
            if (!transactionId.equals(p.getTransactionId()) || p.getPaidFen() != p.getTotalFen())
                invalid();
            return;
        }
        // 已关单或额度不可用时仍记录真实到账，但不重新预约、不抢占已释放名额。
        boolean conflict =
                !"PENDING_PAY".equals(o.getFulfillmentStatus())
                        || !"HELD".equals(p.getCapacityResolution())
                        || !"NONE".equals(o.getCancelStatus())
                        || !o.getPickupStartsAt().isAfter(Instant.parse(payments.now()));
        if (!conflict) moveQuota(o.getId(), true);
        one(mapper.paid(p.getId(), transactionId, paidAt, conflict));
        if (!conflict) {
            one(mapper.orderState(o.getId(), "SCHEDULED"));
            dispatch.onPaid(o.getId());
        }
        if (conflict)
            reconciliation.open(
                    "PAYMENT",
                    p.getId(),
                    o.getId(),
                    p.getId(),
                    "SUCCESS",
                    "SUCCESS",
                    "CAPACITY_CONFLICT",
                    actor);
        String action = conflict ? "PAYMENT_LATE_SUCCESS_CONFLICT" : "PAYMENT_CONFIRMED";
        audit.recordSuccess(
                actor,
                "VI_PAYMENT_ORDER",
                p.getId(),
                action,
                null,
                Map.of(
                        "orderId",
                        o.getId(),
                        "paidAmount",
                        p.getTotalFen(),
                        "capacityConflict",
                        conflict),
                null,
                o.getId());
        outbox.publish(
                action + ":" + p.getId(),
                "VI_PAYMENT_ORDER",
                p.getId(),
                action,
                Map.of("orderId", o.getId(), "paymentId", p.getId()));
    }

    private void closeLocked(ViOrder o, PaymentTransaction p, Actor actor) {
        if ("SUCCESS".equals(p.getStatus()) || "CLOSED".equals(p.getStatus())) return;
        if (!Set.of("PENDING", "UNKNOWN").contains(p.getStatus()) || p.getPaidFen() > 0)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        if (!"PENDING_PAY".equals(o.getFulfillmentStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        moveQuota(o.getId(), false);
        one(mapper.closed(p.getId()));
        one(mapper.orderState(o.getId(), "CLOSED"));
        audit.recordSuccess(
                actor,
                "VI_PAYMENT_ORDER",
                p.getId(),
                "PAYMENT_CLOSED",
                null,
                Map.of("orderId", o.getId()),
                null,
                o.getId());
        outbox.publish(
                "PAYMENT_CLOSED:" + p.getId(),
                "VI_PAYMENT_ORDER",
                p.getId(),
                "PAYMENT_CLOSED",
                Map.of("orderId", o.getId()));
    }

    /** 接车、接收两份额度凭证必须一起迁移；任一校验失败连同资金与事件一起回滚。 */
    private void moveQuota(long orderId, boolean confirmed) {
        var rows = mapper.reservations(orderId);
        if (rows.size() != 2) throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "订单额度凭证不完整，需核查");
        for (var r : rows) {
            if (!"HELD".equals(r.get("status")))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
            long slot = ((Number) r.get("slot_id")).longValue();
            int q = ((Number) r.get("quantity")).intValue();
            if (q < 1) throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
            one(confirmed ? mapper.confirmSlot(slot, q) : mapper.releaseSlot(slot, q));
            one(
                    mapper.reserveState(
                            ((Number) r.get("id")).longValue(),
                            confirmed ? "CONFIRMED" : "RELEASED"));
        }
    }

    /** 仅接受验签解密后的事实；渠道事件永久去重，与支付、额度、审计及 outbox 同事务。 */
    public void notification(String kind, String body, RequestParam request) {
        var actor = Actor.callback("WECHAT");
        try {
            long timestamp = timestamp(request), now = Instant.now().getEpochSecond();
            if (timestamp < now - 300 || timestamp > now + 300) invalid();
            tx.executeWithoutResult(
                    s -> {
                        lock();
                        Map<?, ?> decoded = null;
                        PaymentTransaction route = null;
                        // 只遍历数据库中已发生交易的可信商户身份，不采用未验签报文里的商户/部门选密钥。
                        for (var candidate : mapper.callbackRoutes()) {
                            try {
                                var value =
                                        configuration.parseNotification(
                                                candidate, request, Map.class);
                                String trade = Objects.toString(value.get("out_trade_no"), "");
                                Long id = mapper.orderByTrade(trade);
                                if (id == null) continue;
                                var p = payments.lockPayment(id);
                                if (!Objects.equals(
                                                p.getMerchantProfileId(),
                                                candidate.getMerchantProfileId())
                                        || !Objects.equals(p.getMchid(), candidate.getMchid())
                                        || !Objects.equals(p.getAppId(), candidate.getAppId()))
                                    continue;
                                decoded = value;
                                route = p;
                                break;
                            } catch (ViException ignored) {
                                // 当前可信商户不匹配时继续尝试；所有候选失败则统一拒绝，不泄露配置细节。
                            }
                        }
                        if (decoded == null || route == null) invalid();
                        var fact = ViJson.tree(ViJson.write(decoded));
                        var envelope = ViJson.tree(body);
                        String eventId = envelope.path("id").asText(),
                                type = envelope.path("event_type").asText();
                        if (!eventId.matches("[A-Za-z0-9_-]{1,100}")) invalid();
                        byte[] digest = hash(body);
                        String provider = "WX:" + route.getMchid();
                        // 相同事件号必须对应相同原文摘要；不同事件号的同一笔付款另由资金事实去重。
                        var existing = mapper.event(provider, eventId);
                        if (existing != null) {
                            if (!Objects.equals(existing.get("trade_no"), route.getOutTradeNo())
                                    || !Objects.equals(existing.get("event_type"), type)
                                    || !Arrays.equals(
                                            (byte[]) existing.get("verified_digest"), digest))
                                invalid();
                            return;
                        }
                        var o = payments.lockOrder(route.getOrderId());
                        var p = payments.lockPayment(o.getId());
                        if ("pay".equals(kind)) {
                            if (!"TRANSACTION.SUCCESS".equals(type)
                                    || !"SUCCESS".equals(fact.path("trade_state").asText())
                                    || !p.getMchid().equals(fact.path("mchid").asText())
                                    || !p.getAppId().equals(fact.path("appid").asText())) invalid();
                            if (amount(fact.path("amount").path("total")) != p.getTotalFen()
                                    || !"CNY".equals(fact.path("amount").path("currency").asText()))
                                throw new EvidenceConflict(o.getId());
                            paidLocked(
                                    o,
                                    p,
                                    fact.path("transaction_id").asText(),
                                    instant(fact.path("success_time").asText()),
                                    actor);
                        } else refundLocked(o, p, fact, type, actor);
                        one(mapper.eventInsert(provider, eventId, p.getOutTradeNo(), type, digest));
                    });
        } catch (RuntimeException e) {
            reject(actor, "PAYMENT_CALLBACK_REJECTED", e);
            // 业务事务已回滚，金额异常需另留核查事件；原始通知和签名材料不写审计。
            if (e instanceof EvidenceConflict conflict)
                tx.executeWithoutResult(
                        s ->
                                outbox.publish(
                                        "PAYMENT_AMOUNT_MISMATCH:" + conflict.orderId,
                                        "VI_ORDER",
                                        conflict.orderId,
                                        "PAYMENT_AMOUNT_MISMATCH",
                                        Map.of("orderId", conflict.orderId)));
            throw e;
        }
    }

    private static class EvidenceConflict extends ViException {
        final long orderId;

        EvidenceConflict(long orderId) {
            super(ViErrorCode.VI_REQUEST_INVALID, "渠道金额或币种不符，已登记资金核查事件");
            this.orderId = orderId;
        }
    }

    private void refundLocked(
            ViOrder o,
            PaymentTransaction p,
            com.fasterxml.jackson.databind.JsonNode fact,
            String type,
            Actor actor) {
        String state = fact.path("refund_status").asText();
        if (!Set.of("SUCCESS", "CLOSED", "ABNORMAL").contains(state)
                || !type.equals("REFUND." + state)
                || !p.getMchid().equals(fact.path("mchid").asText())
                || !Objects.equals(p.getTransactionId(), fact.path("transaction_id").asText()))
            invalid();
        var found = mapper.refundByNo(fact.path("out_refund_no").asText());
        if (found == null || ((Number) found.get("order_id")).longValue() != o.getId()) invalid();
        long id = ((Number) found.get("id")).longValue();
        var r = mapper.refundSnapshot(id);
        long amount = ((Number) r.get("amount_fen")).longValue();
        if (amount(fact.path("amount").path("refund")) != amount
                || amount(fact.path("amount").path("total")) != p.getTotalFen()
                || !"CNY".equals(fact.path("amount").path("currency").asText())) invalid();
        refundSettlement.applyLocked(
                o,
                p,
                id,
                new com.xinyi.rongli.refund.service.RefundSettlementService.Fact(
                        state,
                        fact.path("refund_id").asText(),
                        "SUCCESS".equals(state)
                                ? instant(fact.path("success_time").asText())
                                : null),
                actor);
    }

    /** 后台资金权限已由调用者校验；真实原交易重查仍沿同一支付落账点，绝不构造车主会话。 */
    public String queryTrusted(long orderId, Actor actor) {
        Transaction fact = gateway.queryOrder(orderId);
        String state = fact.getTradeState() == null ? "UNKNOWN" : fact.getTradeState().name();
        tx.executeWithoutResult(
                status -> {
                    lock();
                    ViOrder order = payments.lockOrder(orderId);
                    PaymentTransaction payment = payments.lockPayment(orderId);
                    if (order == null || payment == null)
                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                    if ("SUCCESS".equals(state))
                        paidLocked(
                                order,
                                payment,
                                fact.getTransactionId(),
                                instant(fact.getSuccessTime()),
                                actor);
                    else if ("CLOSED".equals(state)) closeLocked(order, payment, actor);
                    else if ("REFUND".equals(state)) {
                        if (payment.getTransactionId() == null
                                || !payment.getTransactionId().equals(fact.getTransactionId())
                                || payment.getPaidFen() != payment.getTotalFen()) invalid();
                    } else if (Set.of("PENDING", "UNKNOWN").contains(payment.getStatus()))
                        mapper.unknown(payment.getId());
                    audit.recordSuccess(
                            actor,
                            "VI_PAYMENT_ORDER",
                            payment.getId(),
                            "PAYMENT_RECONCILIATION_QUERY",
                            null,
                            Map.of("providerState", state),
                            null,
                            orderId);
                });
        return state;
    }

    private Map<String, Object> view(PaymentTransaction p) {
        var m = new LinkedHashMap<String, Object>();
        m.put("merchantTradeNo", p.getOutTradeNo());
        m.put("payStatus", p.getStatus());
        m.put("paidAmount", p.getPaidFen());
        if (p.getPaidAt() != null) m.put("paidAt", p.getPaidAt().toString());
        m.put(
                "queryRequired",
                "UNKNOWN".equals(p.getStatus())
                        || "MANUAL_CONFLICT".equals(p.getCapacityResolution()));
        m.put("paymentMethod", p.getPaymentMethod());
        m.put("paymentPolicyRevisionId", p.getPolicyRevisionId().toString());
        m.put(
                "merchantProfileRevisionId",
                p.getMerchantRevisionId() == null ? null : p.getMerchantRevisionId().toString());
        m.put("methodLocked", !"UNSELECTED".equals(p.getPaymentMethod()));
        m.put("capacityResolution", p.getCapacityResolution());
        m.put("offlineProofStatus", null);
        m.put("version", p.getVersion());
        return m;
    }

    private void reject(Actor a, String action, RuntimeException e) {
        audit.recordFailure(
                a,
                "VI_PAYMENT_ORDER",
                0,
                action,
                null,
                Map.of(
                        "code",
                        e instanceof ViException v ? v.getErrorCode().name() : "VI_INTERNAL_ERROR"),
                "支付操作或渠道证据未确认");
    }

    private static long amount(com.fasterxml.jackson.databind.JsonNode n) {
        try {
            return n.decimalValue().longValueExact();
        } catch (RuntimeException e) {
            invalid();
            return 0;
        }
    }

    private static Instant instant(String value) {
        try {
            return java.time.OffsetDateTime.parse(value).toInstant();
        } catch (RuntimeException e) {
            invalid();
            return null;
        }
    }

    private static long timestamp(RequestParam r) {
        try {
            return Long.parseLong(r.getMessage().split("\n", 2)[0]);
        } catch (RuntimeException e) {
            invalid();
            return 0;
        }
    }

    private static byte[] hash(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "渠道通知身份、金额或签名不匹配");
    }

    private static void one(int n) {
        if (n != 1) throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
    }
}
