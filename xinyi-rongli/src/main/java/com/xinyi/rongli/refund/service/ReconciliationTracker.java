package com.xinyi.rongli.refund.service;

import com.xinyi.rongli.refund.domain.ReconciliationRecord;
import com.xinyi.rongli.refund.mapper.RefundMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.service.AuditService;
import java.util.*;
import org.springframework.stereotype.Service;

/** 在调用者已持目录与原订单/支付锁的事务内维护永久异常，同一subject重开原case，不删除旧审计。 */
@Service
public class ReconciliationTracker {
    private final RefundMapper mapper;
    private final AuditService audit;

    public ReconciliationTracker(RefundMapper mapper, AuditService audit) {
        this.mapper = mapper;
        this.audit = audit;
    }

    public void open(
            String kind,
            long subject,
            long order,
            long payment,
            String local,
            String channel,
            String reason,
            Actor actor) {
        var row = mapper.reconciliationSubject(kind, subject);
        if (row == null) {
            row = new ReconciliationRecord();
            row.setKind(kind);
            row.setSubjectId(subject);
            row.setOrderId(order);
            row.setPaymentId(payment);
            row.setLocalStatus(local);
            row.setChannelStatus(channel);
            row.setReasonCode(reason);
            mapper.insertReconciliation(row);
            audit.recordSuccess(
                    actor,
                    "PAYMENT_RECONCILIATION",
                    row.getId(),
                    "RECONCILIATION_OPENED",
                    null,
                    Map.of("kind", kind, "subjectId", Long.toString(subject), "reasonCode", reason),
                    null,
                    order);
        } else if ("RESOLVED".equals(row.getStatus())
                || !Objects.equals(row.getLocalStatus(), local)
                || !Objects.equals(row.getReasonCode(), reason)) {
            row.setLocalStatus(local);
            row.setChannelStatus(channel);
            row.setReasonCode(reason);
            mapper.reopen(row);
            audit.recordSuccess(
                    actor,
                    "PAYMENT_RECONCILIATION",
                    row.getId(),
                    "RECONCILIATION_REOPENED",
                    null,
                    Map.of("kind", kind, "reasonCode", reason),
                    null,
                    order);
        }
    }
}
