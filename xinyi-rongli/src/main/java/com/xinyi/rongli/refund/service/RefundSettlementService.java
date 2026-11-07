package com.xinyi.rongli.refund.service;

import com.xinyi.rongli.aftersale.domain.AftersaleCase;
import com.xinyi.rongli.aftersale.mapper.AftersaleMapper;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.refund.domain.RefundRecord;
import com.xinyi.rongli.refund.mapper.RefundMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.service.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

/** 通知、提交回执和查询的唯一资金落账点；调用者必须已持目录→订单→支付锁，且已验证渠道签名/身份。 */
@Service
public class RefundSettlementService {
    private final RefundMapper mapper;
    private final AftersaleMapper cases;
    private final AuditService audit;
    private final OutboxService outbox;
    private final ReconciliationTracker reconciliation;

    public RefundSettlementService(
            RefundMapper mapper,
            AftersaleMapper cases,
            AuditService audit,
            OutboxService outbox,
            ReconciliationTracker reconciliation) {
        this.mapper = mapper;
        this.cases = cases;
        this.audit = audit;
        this.outbox = outbox;
        this.reconciliation = reconciliation;
    }

    public record Fact(String status, String channelId, Instant successAt) {}

    public AftersaleCase approved(ViOrder order, PaymentTransaction payment, RefundRecord refund) {
        AftersaleCase row = cases.lock(refund.getCaseId());
        var review = mapper.review(refund.getCaseId(), refund.getReviewRevision());
        if (row == null
                || !Objects.equals(row.getOrderId(), order.getId())
                || !Objects.equals(payment.getOrderId(), order.getId())
                || !Objects.equals(row.getPaymentId(), payment.getId())
                || !Objects.equals(refund.getPaymentId(), payment.getId())
                || !"APPROVED".equals(row.getReviewStatus())
                || row.getApprovedFen() == null
                || row.getApprovedFen() != refund.getAmountFen()
                || refund.getAmountFen() <= 0
                || review == null
                || !"APPROVE".equals(review.get("decision"))
                || ((Number) review.get("approved_fen")).longValue() != refund.getAmountFen())
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "退款与原审核决定、金额或支付记录不一致");
        return row;
    }

    /** 实物归还与资金结果分开：此方法只检查能否发起，可信已发生退款即使提前也要如实入账。 */
    public void executable(ViOrder order, AftersaleCase row) {
        if ("CANCEL_REFUND".equals(row.getKind())) {
            boolean pendingReturn =
                    "WAIT_RETURN".equals(row.getReturnState())
                            || "APPROVED_WAIT_RETURN".equals(order.getCancelStatus())
                            || !"OWNER".equals(order.getCustodyType());
            if (pendingReturn
                    && (order.getReturnedAt() == null || !"OWNER".equals(order.getCustodyType())))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "必须完成真实归还后才能执行退款");
            if (!Set.of("COMPLETED", "APPROVED_WAIT_RETURN").contains(order.getCancelStatus()))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "取消决定尚未生效");
        }
    }

    public void applyLocked(
            ViOrder order, PaymentTransaction payment, long refundId, Fact fact, Actor actor) {
        var snapshot = mapper.find(refundId);
        if (snapshot == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        AftersaleCase row = approved(order, payment, snapshot);
        RefundRecord refund = mapper.lock(refundId);
        if (!Set.of("SUCCESS", "PROCESSING", "ABNORMAL", "CLOSED").contains(fact.status())
                || fact.channelId() == null
                || !fact.channelId().matches("[A-Za-z0-9_-]{1,64}")) invalid();
        if (refund.getChannelRefundId() != null
                && !refund.getChannelRefundId().equals(fact.channelId())) invalid();
        if ("SUCCESS".equals(refund.getStatus()) || "CLOSED".equals(refund.getStatus())) {
            if (!refund.getStatus().equals(fact.status())
                    || !Objects.equals(refund.getChannelRefundId(), fact.channelId())) invalid();
            return;
        }
        boolean success = "SUCCESS".equals(fact.status()), closed = "CLOSED".equals(fact.status());
        // 容忍双方时钟最多五分钟偏差；明显早于原付款或来自未来的成功时间不能作为退款事实。
        if (success
                && (fact.successAt() == null
                        || fact.successAt().isAfter(Instant.now().plusSeconds(300))
                        || payment.getPaidAt() != null
                                && fact.successAt()
                                        .isBefore(payment.getPaidAt().minusSeconds(300))))
            invalid();
        if (success || closed) {
            // 同一行锁内从预留迁移且退款状态随后终结；重复渠道事实不会再次消费预留。
            if (mapper.paidBalance(payment.getId(), refund.getAmountFen(), success) != 1)
                throw new ViException(ViErrorCode.VI_REFUND_LIMIT, "退款预留或实退余额不一致，需对账核查");
        }
        refund.setStatus(fact.status());
        refund.setChannelRefundId(fact.channelId());
        refund.setSuccessAt(success ? fact.successAt() : null);
        refund.setLastVerifiedStatus(fact.status());
        refund.setLastVerifiedAt(Instant.now());
        if (mapper.state(refund) != 1) throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        // 可信CLOSED沿P02释放旧预留，但标异常等待独立处理，绝不原号再次提交或自动换号。
        boolean physicalDone =
                "REFUND_ONLY".equals(row.getKind())
                        || "COMPLETED".equals(order.getCancelStatus())
                                && (!"WAIT_RETURN".equals(row.getReturnState())
                                        || order.getReturnedAt() != null
                                                && "OWNER".equals(order.getCustodyType()));
        mapper.settleCase(
                row.getId(), closed ? "ABNORMAL" : fact.status(), success && physicalDone);
        audit.recordSuccess(
                actor,
                "VI_REFUND_ORDER",
                refundId,
                "REFUND_" + fact.status(),
                null,
                Map.of("orderId", order.getId().toString(), "amount", refund.getAmountFen()),
                null,
                order.getId());
        outbox.publish(
                "refund:" + refundId + ":" + fact.status(),
                "VI_REFUND_ORDER",
                refundId,
                "REFUND_" + fact.status(),
                Map.of("orderId", order.getId().toString(), "refundId", Long.toString(refundId)));
        if (closed || "ABNORMAL".equals(fact.status()))
            reconciliation.open(
                    "REFUND",
                    refundId,
                    order.getId(),
                    payment.getId(),
                    fact.status(),
                    fact.status(),
                    "REFUND_" + fact.status(),
                    actor);
    }

    private static void invalid() {
        throw new ViException(ViErrorCode.VI_REQUEST_INVALID, "渠道退款事实与原退款记录不一致");
    }
}
