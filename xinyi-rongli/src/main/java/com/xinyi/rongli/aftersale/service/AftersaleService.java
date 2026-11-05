package com.xinyi.rongli.aftersale.service;

import com.xinyi.rongli.aftersale.domain.AftersaleCase;
import com.xinyi.rongli.aftersale.dto.AftersaleRequests.*;
import com.xinyi.rongli.aftersale.mapper.AftersaleMapper;
import com.xinyi.rongli.file.service.FileAuthority;
import com.xinyi.rongli.file.service.PrivateFileService;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 统一售后申请只登记人工审核意图；资金预留和实际退款由后续独立审核流程决定。 */
@Service
public class AftersaleService {
    public record Receipt(long caseId) {}

    private final BusinessActorResolver actors;
    private final FileAuthority authority;
    private final PrivateFileService files;
    private final PaymentMapper payments;
    private final AftersaleMapper cases;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;

    public AftersaleService(
            BusinessActorResolver actors,
            FileAuthority authority,
            PrivateFileService files,
            PaymentMapper payments,
            AftersaleMapper cases,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            PlatformTransactionManager manager) {
        this.actors = actors;
        this.authority = authority;
        this.files = files;
        this.payments = payments;
        this.cases = cases;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    private Actor owner() {
        Actor actor = actors.requireMini();
        if (!"OWNER".equals(actor.actingRole())) throw new ViException(ViErrorCode.VI_FORBIDDEN);
        return actor;
    }

    /** 与私有文件共用实时会话校验，所有动作遵循目录→会话→原订单→支付→申请→文件的锁顺序。 */
    private void guard(Actor actor) {
        authority.guard(actor, "biz:aftersale:query");
    }

    private ViOrder order(Actor actor, String orderNo) {
        ViOrder order = payments.lockOwnedOrder(orderNo, actor.accountId());
        if (order == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return order;
    }

    public Map<String, Object> apply(String orderNo, Apply request, String key) {
        Actor actor = owner();
        return audited(
                actor,
                0,
                "AFTERSALE_APPLY",
                () ->
                        tx.execute(
                                status -> {
                                    guard(actor);
                                    ViOrder order = order(actor, orderNo);
                                    PaymentTransaction payment =
                                            payments.lockPayment(order.getId());
                                    Receipt receipt =
                                            idem.execute(
                                                    "MINI:" + actor.accountId(),
                                                    "AFTERSALE_APPLY:" + order.getId(),
                                                    key,
                                                    ViJson.write(request),
                                                    true,
                                                    Receipt.class,
                                                    () -> create(actor, order, payment, request));
                                    return view(cases.lock(receipt.caseId()), order);
                                }));
    }

    private Receipt create(Actor actor, ViOrder order, PaymentTransaction payment, Apply request) {
        // 同订单原申请优先返回；重复入口不因客户端停留在旧订单版本而创建第二条或改写原说明。
        AftersaleCase existing = cases.active(order.getId());
        if (existing != null) return new Receipt(existing.getId());
        if (order.getVersion() != request.expectedOrderVersion())
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        if (payment == null || !"SUCCESS".equals(payment.getStatus()) || payment.getPaidFen() <= 0)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "未确认收款的订单请使用未支付取消入口");
        if ("CANCEL_REFUND".equals(request.kind())
                && (!"NONE".equals(order.getCancelStatus())
                        || Set.of("CLOSED", "CANCELLED", "COMPLETED")
                                .contains(order.getFulfillmentStatus())))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "当前订单不能申请取消，请选择仅退款或查看原申请");
        var balance = cases.balance(payment.getId());
        long available =
                amount(balance, "paid_fen")
                        - amount(balance, "refund_reserved_fen")
                        - amount(balance, "refunded_fen");
        // 使用已持锁的支付资金余额；申请不占款，也不允许越过已有退款及预留。
        if (request.requestedAmount() > available)
            throw new ViException(ViErrorCode.VI_REFUND_LIMIT);
        if ("REFUND_ONLY".equals(request.kind()) && request.requestedAmount() == 0)
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "仅退款的申请金额必须大于0");
        files.validateVouchers(actor, order.getId(), request.voucherFileIds());
        AftersaleCase row = new AftersaleCase();
        row.setCaseNo("AS" + UUID.randomUUID().toString().replace("-", ""));
        row.setOrderId(order.getId());
        row.setPaymentId(payment.getId());
        row.setKind(request.kind());
        row.setRequestedFen(request.requestedAmount());
        row.setApplicantId(actor.accountId());
        row.setReason(request.reason());
        row.setReasonCode(request.reasonCode());
        row.setEvidenceIds(ViJson.write(externalIds(request.voucherFileIds())));
        cases.insert(row);
        if ("CANCEL_REFUND".equals(request.kind())
                && cases.markCancellation(order.getId(), order.getVersion()) != 1)
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        // 不改履约、实际保管、代驾占用或支付状态；取消申请阻止新派单，审核再判断返还条件。
        audit.recordSuccess(
                actor,
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_APPLY",
                null,
                Map.of(
                        "kind",
                        row.getKind(),
                        "requestedAmount",
                        row.getRequestedFen(),
                        "voucherCount",
                        request.voucherFileIds().size()),
                null,
                order.getId());
        outbox.publish(
                "AFTERSALE_APPLIED:" + row.getId(),
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_APPLIED",
                Map.of("caseId", row.getId().toString(), "orderId", order.getId().toString()));
        return new Receipt(row.getId());
    }

    public Map<String, Object> note(long id, Note request, String key) {
        Actor actor = owner();
        return audited(
                actor,
                id,
                "AFTERSALE_NOTE",
                () ->
                        tx.execute(
                                status -> {
                                    guard(actor);
                                    AftersaleCase row = cases.find(id);
                                    if (row == null)
                                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                                    ViOrder order = payments.lockOrder(row.getOrderId());
                                    if (order == null
                                            || !Objects.equals(
                                                    order.getOwnerAccountId(), actor.accountId()))
                                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                                    payments.lockPayment(order.getId());
                                    AftersaleCase locked = cases.lock(id);
                                    Receipt receipt =
                                            idem.execute(
                                                    "MINI:" + actor.accountId(),
                                                    "AFTERSALE_NOTE:" + id,
                                                    key,
                                                    ViJson.write(request),
                                                    true,
                                                    Receipt.class,
                                                    () -> append(actor, order, locked, request));
                                    return view(cases.lock(receipt.caseId()), order);
                                }));
    }

    private Receipt append(Actor actor, ViOrder order, AftersaleCase row, Note request) {
        if (row.getVersion() != request.expectedVersion())
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        if (!Set.of("PENDING_REVIEW", "EXECUTING").contains(row.getCaseStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "已办结申请不能继续补充资料");
        files.validateVouchers(actor, order.getId(), request.voucherFileIds());
        if (cases.increment(row.getId(), row.getVersion()) != 1)
            throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
        cases.insertNote(
                row.getId(),
                actor.accountId(),
                request.note(),
                ViJson.write(externalIds(request.voucherFileIds())));
        audit.recordSuccess(
                actor,
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_NOTE",
                null,
                Map.of(
                        "voucherCount",
                        request.voucherFileIds().size(),
                        "version",
                        row.getVersion() + 1),
                null,
                order.getId());
        outbox.publish(
                "AFTERSALE_NOTE:" + row.getId() + ":" + (row.getVersion() + 1),
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_NOTE_ADDED",
                Map.of("caseId", row.getId().toString()));
        return new Receipt(row.getId());
    }

    public Map<String, Object> get(long id) {
        Actor actor = owner();
        return audited(
                actor,
                id,
                "AFTERSALE_VIEW",
                () ->
                        tx.execute(
                                status -> {
                                    guard(actor);
                                    AftersaleCase row = cases.find(id);
                                    if (row == null)
                                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                                    ViOrder order = payments.lockOrder(row.getOrderId());
                                    if (order == null
                                            || !Objects.equals(
                                                    order.getOwnerAccountId(), actor.accountId()))
                                        throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                                    return view(cases.lock(id), order);
                                }));
    }

    public Map<String, Object> list(String orderNo, Query query) {
        Actor actor = owner();
        return audited(
                actor,
                0,
                "AFTERSALE_LIST",
                () ->
                        tx.execute(
                                status -> {
                                    guard(actor);
                                    Long orderId =
                                            orderNo == null ? null : order(actor, orderNo).getId();
                                    var rows =
                                            cases.list(
                                                    actor.accountId(),
                                                    orderId,
                                                    query,
                                                    (long) (query.pageNum() - 1)
                                                            * query.pageSize());
                                    List<Map<String, Object>> result = new ArrayList<>();
                                    // 列表SQL已按原订单车主过滤；投影再次使用数据库订单，不采信客户端部门或申请人。
                                    for (AftersaleCase row : rows)
                                        result.add(view(row, payments.lockOrder(row.getOrderId())));
                                    return Map.of(
                                            "code",
                                            200,
                                            "msg",
                                            "操作成功",
                                            "rows",
                                            result,
                                            "total",
                                            cases.count(actor.accountId(), orderId, query));
                                }));
    }

    private Map<String, Object> view(AftersaleCase row, ViOrder order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("caseId", row.getId().toString());
        result.put("orderNo", order.getOrderNo());
        result.put("kind", row.getKind());
        result.put("caseStatus", row.getCaseStatus());
        result.put("reviewStatus", row.getReviewStatus());
        result.put("requestedAmount", row.getRequestedFen());
        result.put("approvedAmount", row.getApprovedFen());
        result.put("returnState", row.getReturnState());
        result.put("refundState", row.getRefundState());
        result.put("custody", order.getCustodyType());
        result.put("fulfillmentStatus", order.getFulfillmentStatus());
        String refund = cases.refundNo(row.getId());
        if (refund != null) result.put("refundNo", refund);
        result.put("reason", row.getReason());
        result.put("reasonCode", row.getReasonCode());
        result.put("voucherFileIds", jsonIds(row.getEvidenceIds()));
        result.put(
                "notes",
                cases.notes(row.getId()).stream()
                        .map(
                                note ->
                                        Map.of(
                                                "noteId",
                                                note.get("id").toString(),
                                                "note",
                                                note.get("note"),
                                                "voucherFileIds",
                                                jsonIds((String) note.get("evidence_ids")),
                                                "createdAt",
                                                note.get("created_iso")))
                        .toList());
        // 内部审核意见没有可公开字段，不能把 opinion 原文当公开说明。
        result.put("publicOpinion", null);
        result.put("version", row.getVersion());
        result.put("createdAt", row.getCreatedAt().toString());
        result.put("updatedAt", row.getUpdatedAt().toString());
        result.put(
                "allowedActions",
                Set.of("PENDING_REVIEW", "EXECUTING").contains(row.getCaseStatus())
                        ? List.of("VIEW_DETAIL", "ADD_NOTE")
                        : List.of("VIEW_DETAIL"));
        return result;
    }

    private static List<String> externalIds(List<Long> ids) {
        return ids.stream().map(Object::toString).toList();
    }

    private static List<String> jsonIds(String json) {
        if (json == null) return List.of();
        List<String> values = new ArrayList<>();
        ViJson.tree(json).forEach(value -> values.add(value.asText()));
        return values;
    }

    private static long amount(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).longValue();
    }

    private <T> T audited(Actor actor, long id, String action, Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "AFTERSALE",
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
