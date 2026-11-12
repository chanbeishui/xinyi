package com.xinyi.rongli.aftersale.service;

import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.aftersale.domain.AftersaleCase;
import com.xinyi.rongli.aftersale.dto.AdminAftersaleRequests.*;
import com.xinyi.rongli.aftersale.dto.AftersaleRequests.*;
import com.xinyi.rongli.aftersale.mapper.*;
import com.xinyi.rongli.file.service.PrivateFileService;
import com.xinyi.rongli.integration.service.IntegrationAuthority;
import com.xinyi.rongli.order.domain.ViOrder;
import com.xinyi.rongli.payment.domain.PaymentTransaction;
import com.xinyi.rongli.payment.mapper.PaymentMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Supplier;

/** 后台统一审核：订单业务部门和历史收款主体的资金权限各自校验，一个决定原子提交所有业务及资金事实。 */
@Service
public class AdminAftersaleService {
    public record Receipt(long caseId) {}

    private record Context(ViOrder order, PaymentTransaction payment, AftersaleCase row) {}

    private final IntegrationAuthority authority;
    private final AftersaleMapper cases;
    private final AdminAftersaleMapper mapper;
    private final PaymentMapper payments;
    private final PrivateFileService files;
    private final AftersaleParticipants participants;
    private final AftersaleService publicViews;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final OutboxService outbox;
    private final TransactionTemplate tx;
    private final com.xinyi.rongli.dispatch.service.DispatchLifecyclePort dispatch;

    public AdminAftersaleService(
            IntegrationAuthority authority,
            AftersaleMapper cases,
            AdminAftersaleMapper mapper,
            PaymentMapper payments,
            PrivateFileService files,
            AftersaleParticipants participants,
            AftersaleService publicViews,
            IdempotencyService idem,
            AuditService audit,
            OutboxService outbox,
            com.xinyi.rongli.dispatch.service.DispatchLifecyclePort dispatch,
            PlatformTransactionManager manager) {
        this.dispatch = dispatch;
        this.authority = authority;
        this.cases = cases;
        this.mapper = mapper;
        this.payments = payments;
        this.files = files;
        this.participants = participants;
        this.publicViews = publicViews;
        this.idem = idem;
        this.audit = audit;
        this.outbox = outbox;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /** 所有事务先锁目录/当前后台账户，后锁订单→支付→申请；功能权限不代替原部门和收款主体权限。 */
    private void guard() {
        authority.lockAuthority();
    }

    private void department(long id, String permission) {
        Set<Long> scope = authority.scope(permission);
        if (scope != null && !scope.contains(id))
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
    }

    private boolean can(long id, String permission) {
        if (!SecurityUtils.hasPermi(permission)) return false;
        Set<Long> scope = authority.scope(permission);
        return scope == null || scope.contains(id);
    }

    private Context context(long caseId, String permission) {
        AftersaleCase snapshot = cases.find(caseId);
        if (snapshot == null) invisible();
        ViOrder order = payments.lockOrder(snapshot.getOrderId());
        if (order == null) invisible();
        department(order.getDeptId(), permission);
        PaymentTransaction payment = payments.lockPayment(order.getId());
        AftersaleCase row = cases.lock(caseId);
        if (payment == null || !Objects.equals(row.getPaymentId(), payment.getId()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "售后与原支付记录不匹配，请核查");
        return new Context(order, payment, row);
    }

    public Map<String, Object> get(long id) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "AFTERSALE_ADMIN_VIEW",
                () ->
                        tx.execute(
                                status -> {
                                    guard();
                                    Context context = context(id, "biz:aftersale:query");
                                    return view(context, actor);
                                }));
    }

    public Map<String, Object> list(String orderNo, Long ownerId, Query query) {
        Actor actor = authority.actor();
        return audited(
                actor,
                0,
                "AFTERSALE_ADMIN_LIST",
                () ->
                        tx.execute(
                                status -> {
                                    guard();
                                    Set<Long> scope = authority.scope("biz:aftersale:list");
                                    Long orderId = null;
                                    if (orderNo != null) {
                                        orderId = mapper.orderId(orderNo);
                                        if (orderId == null) invisible();
                                        ViOrder order = payments.lockOrder(orderId);
                                        if (scope != null && !scope.contains(order.getDeptId()))
                                            invisible();
                                    }
                                    var rows =
                                            mapper.list(
                                                    scope,
                                                    orderId,
                                                    ownerId,
                                                    query,
                                                    (long) (query.pageNum() - 1)
                                                            * query.pageSize());
                                    List<Map<String, Object>> projected = new ArrayList<>();
                                    for (var row : rows) {
                                        ViOrder order = payments.lockOrder(row.getOrderId());
                                        PaymentTransaction payment =
                                                payments.lockPayment(order.getId());
                                        if (payment == null)
                                            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
                                        projected.add(
                                                view(new Context(order, payment, row), actor));
                                    }
                                    return Map.of(
                                            "code",
                                            200,
                                            "msg",
                                            "操作成功",
                                            "rows",
                                            projected,
                                            "total",
                                            mapper.count(scope, orderId, ownerId, query));
                                }));
    }

    public Map<String, Object> apply(String orderNo, Apply request, String key) {
        Actor actor = authority.actor();
        return audited(
                actor,
                0,
                "AFTERSALE_ADMIN_APPLY",
                () ->
                        tx.execute(
                                status -> {
                                    guard();
                                    Long id = mapper.orderId(orderNo);
                                    if (id == null) invisible();
                                    ViOrder order = payments.lockOrder(id);
                                    department(order.getDeptId(), "biz:aftersale:create");
                                    PaymentTransaction payment = payments.lockPayment(id);
                                    if (payment == null)
                                        throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
                                    // 代办也会展示/选择真实可退额，资金可见范围以该笔历史收款主体为准。
                                    department(payment.getReceiverDeptId(), "biz:refund:query");
                                    Receipt receipt =
                                            idem.execute(
                                                    "ADMIN:" + actor.sysUserId(),
                                                    "AFTERSALE_ADMIN_APPLY:" + id,
                                                    key,
                                                    ViJson.write(request),
                                                    true,
                                                    Receipt.class,
                                                    () -> create(actor, order, payment, request));
                                    return view(
                                            new Context(
                                                    payments.lockOrder(id),
                                                    payment,
                                                    cases.lock(receipt.caseId())),
                                            actor);
                                }));
    }

    private Receipt create(Actor actor, ViOrder order, PaymentTransaction payment, Apply request) {
        AftersaleCase existing = cases.active(order.getId());
        if (existing != null) return new Receipt(existing.getId());
        if (order.getVersion() != request.expectedOrderVersion()) conflict();
        if (!"SUCCESS".equals(payment.getStatus()) || payment.getPaidFen() <= 0)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "未支付订单不能申请退款");
        if ("CANCEL_REFUND".equals(request.kind())
                && (!"NONE".equals(order.getCancelStatus())
                        || Set.of("CLOSED", "CANCELLED", "COMPLETED")
                                .contains(order.getFulfillmentStatus())))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        requireAmount(payment, request.kind(), request.requestedAmount());
        files.validateAdminVouchers(actor, order.getId(), request.voucherFileIds());
        var row = new AftersaleCase();
        row.setCaseNo("AS" + UUID.randomUUID().toString().replace("-", ""));
        row.setOrderId(order.getId());
        row.setPaymentId(payment.getId());
        row.setKind(request.kind());
        row.setRequestedFen(request.requestedAmount());
        row.setApplicantType("SYS_USER");
        row.setApplicantId(actor.sysUserId());
        row.setReason(request.reason());
        row.setReasonCode(request.reasonCode());
        row.setEvidenceIds(
                ViJson.write(request.voucherFileIds().stream().map(Object::toString).toList()));
        cases.insert(row);
        participants.admin(row.getId(), actor.sysUserId());
        participants.account(row.getId(), order.getOwnerAccountId());
        if ("CANCEL_REFUND".equals(request.kind())
                && cases.markCancellation(order.getId(), order.getVersion()) != 1) conflict();
        audit.recordSuccess(
                actor,
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_ADMIN_APPLY",
                null,
                Map.of("kind", row.getKind(), "requestedAmount", row.getRequestedFen()),
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
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "AFTERSALE_ADMIN_NOTE",
                () ->
                        tx.execute(
                                status -> {
                                    guard();
                                    Context context = context(id, "biz:aftersale:note");
                                    Receipt receipt =
                                            idem.execute(
                                                    "ADMIN:" + actor.sysUserId(),
                                                    "AFTERSALE_ADMIN_NOTE:" + id,
                                                    key,
                                                    ViJson.write(request),
                                                    true,
                                                    Receipt.class,
                                                    () -> append(actor, context, request));
                                    return view(
                                            new Context(
                                                    context.order(),
                                                    context.payment(),
                                                    cases.lock(receipt.caseId())),
                                            actor);
                                }));
    }

    private Receipt append(Actor actor, Context context, Note request) {
        AftersaleCase row = context.row();
        if (row.getVersion() != request.expectedVersion()) conflict();
        if (!Set.of("PENDING_REVIEW", "EXECUTING").contains(row.getCaseStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        files.validateAdminVouchers(actor, row.getOrderId(), request.voucherFileIds());
        if (cases.increment(row.getId(), row.getVersion()) != 1) conflict();
        mapper.insertNote(
                row.getId(),
                actor.sysUserId(),
                request.note(),
                ViJson.write(request.voucherFileIds().stream().map(Object::toString).toList()));
        participants.admin(row.getId(), actor.sysUserId());
        audit.recordSuccess(
                actor,
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_ADMIN_NOTE",
                null,
                Map.of(
                        "version",
                        row.getVersion() + 1,
                        "voucherCount",
                        request.voucherFileIds().size()),
                null,
                row.getOrderId());
        outbox.publish(
                "AFTERSALE_NOTE:" + row.getId() + ":" + (row.getVersion() + 1),
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_NOTE_ADDED",
                Map.of("caseId", row.getId().toString()));
        return new Receipt(row.getId());
    }

    public Map<String, Object> preview(long id, Preview request, String key) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "AFTERSALE_REVIEW_PREVIEW",
                () ->
                        tx.execute(
                                status -> {
                                    guard();
                                    Context context = context(id, "biz:aftersale:reviewpreview");
                                    department(
                                            context.payment().getReceiverDeptId(),
                                            "biz:refund:query");
                                    pending(context.row());
                                    if (context.row().getVersion() != request.expectedVersion())
                                        conflict();
                                    participants.requireIndependent(id, actor.sysUserId());
                                    // 只冻结请求键与内容的关联，不缓存实时资金/保管快照；重放仍重新鉴权并读取事实。
                                    idem.execute(
                                            "ADMIN:" + actor.sysUserId(),
                                            "AFTERSALE_REVIEW_PREVIEW:" + id,
                                            key,
                                            ViJson.write(request),
                                            true,
                                            Receipt.class,
                                            () -> new Receipt(id));
                                    // 0元也可能是批准取消，不能用金额推断驳回；预览与正式决定按同一明确动作解释。
                                    boolean approved = "APPROVE".equals(request.decision());
                                    if (approved)
                                        requireAmount(
                                                context.payment(),
                                                context.row().getKind(),
                                                request.approvedAmount());
                                    boolean returns =
                                            approved
                                                    && returnRequired(
                                                            context.order(), context.row());
                                    var result = new LinkedHashMap<String, Object>();
                                    result.put("caseId", Long.toString(id));
                                    result.put("decision", request.decision());
                                    result.put("caseVersion", context.row().getVersion());
                                    result.put("orderVersion", context.order().getVersion());
                                    result.put("custody", context.order().getCustodyType());
                                    result.put(
                                            "assignmentId",
                                            external(context.order().getActiveAssignmentId()));
                                    result.putAll(balance(context.payment()));
                                    result.put("returnRequired", returns);
                                    result.put(
                                            "allowedActions",
                                            can(context.order().getDeptId(), "biz:aftersale:review")
                                                            && can(
                                                                    context.payment()
                                                                            .getReceiverDeptId(),
                                                                    "biz:refund:approve")
                                                    ? List.of("REVIEW_AFTERSALE")
                                                    : List.of());
                                    result.put(
                                            "warnings",
                                            !approved
                                                    ? List.of("驳回将保留当前履约与实际保管事实，不新增退款预留，也不释放司机")
                                                    : returns
                                                            ? List.of("须真实归还车辆后执行退款；批准0元仍需返还")
                                                            : List.of("提交后只登记退款决定，实际执行按原支付方式处理"));
                                    return result;
                                }));
    }

    public Map<String, Object> review(long id, Review request, String key) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "AFTERSALE_REVIEW",
                () ->
                        tx.execute(
                                status -> {
                                    guard();
                                    Context context = context(id, "biz:aftersale:review");
                                    // 拒绝申请也不能用仅有取消权限的账号访问资金决定入口。
                                    department(
                                            context.payment().getReceiverDeptId(),
                                            "biz:refund:approve");
                                    Receipt receipt =
                                            idem.execute(
                                                    "ADMIN:" + actor.sysUserId(),
                                                    "AFTERSALE_REVIEW:" + id,
                                                    key,
                                                    ViJson.write(request),
                                                    true,
                                                    Receipt.class,
                                                    () -> decide(actor, context, request));
                                    return view(
                                            new Context(
                                                    payments.lockOrder(context.order().getId()),
                                                    payments.lockPayment(context.order().getId()),
                                                    cases.lock(receipt.caseId())),
                                            actor);
                                }));
    }

    private Receipt decide(Actor actor, Context context, Review request) {
        AftersaleCase row = context.row();
        ViOrder order = context.order();
        PaymentTransaction payment = context.payment();
        pending(row);
        if (row.getVersion() != request.expectedVersion()
                || order.getVersion() != request.observedOrderVersion()) conflict();
        participants.requireIndependent(row.getId(), actor.sysUserId());
        boolean approved = "APPROVE".equals(request.decision());
        if (approved) requireAmount(payment, row.getKind(), request.approvedAmount());
        boolean returns = approved && returnRequired(order, row);
        row.setApprovedFen(request.approvedAmount());
        row.setPublicOpinion(request.publicOpinion());
        row.setReviewStatus(approved ? "APPROVED" : "REJECTED");
        row.setReturnState(returns ? "WAIT_RETURN" : "NOT_REQUIRED");
        row.setRefundState(
                approved && request.approvedAmount() > 0
                        ? (returns ? "WAIT_RETURN" : "READY")
                        : "NOT_REQUIRED");
        row.setCaseStatus(
                !approved
                        ? "REJECTED"
                        : request.approvedAmount() == 0 && !returns ? "CLOSED" : "EXECUTING");
        if ("CANCEL_REFUND".equals(row.getKind())) {
            if (approved) {
                if (!returns) releaseUnpicked(order);
                if (mapper.cancelOrder(order.getId(), order.getVersion(), returns) != 1) conflict();
            } else if (mapper.rejectCancellation(order.getId(), order.getVersion()) != 1)
                conflict();
        }
        if (approved && request.approvedAmount() > 0) {
            if (mapper.reserve(payment.getId(), request.approvedAmount()) != 1)
                throw new ViException(ViErrorCode.VI_REFUND_LIMIT);
            String method = payment.getPaymentMethod();
            if (!Set.of("WECHAT", "OFFLINE").contains(method))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "历史支付方式无效，禁止猜测退款通道");
            // 永久由申请+审核版本命名，不使用请求UUID；重试不能换号或改变原支付身份。
            String refundNo = "RF" + row.getId() + "R" + row.getReviewRevision();
            mapper.createRefund(
                    row,
                    refundNo,
                    method,
                    "WECHAT".equals(method) ? "WECHAT_API" : "MANUAL_OFFLINE");
        }
        mapper.insertReview(
                row,
                request.decision(),
                request.observedOrderVersion(),
                order.getCustodyType(),
                returns,
                actor.sysUserId(),
                request.opinion());
        if (mapper.reviewCase(row) != 1) conflict();
        audit.recordSuccess(
                actor,
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_REVIEW",
                null,
                Map.of(
                        "decision",
                        request.decision(),
                        "approvedAmount",
                        request.approvedAmount(),
                        "returnRequired",
                        returns,
                        "reviewRevision",
                        row.getReviewRevision()),
                null,
                order.getId());
        // P04消费前不调用渠道。待返还和可执行事件分开，避免0元或未还车凭证触发退款。
        outbox.publish(
                "AFTERSALE_REVIEWED:" + row.getId() + ":" + row.getReviewRevision(),
                "AFTERSALE",
                row.getId(),
                "AFTERSALE_REVIEWED",
                Map.of(
                        "caseId",
                        row.getId().toString(),
                        "approved",
                        approved,
                        "returnRequired",
                        returns));
        if (approved && request.approvedAmount() > 0 && !returns)
            outbox.publish(
                    "REFUND_EXECUTE:" + row.getId() + ":" + row.getReviewRevision(),
                    "AFTERSALE",
                    row.getId(),
                    "REFUND_EXECUTION_REQUESTED",
                    Map.of(
                            "caseId",
                            row.getId().toString(),
                            "reviewRevision",
                            row.getReviewRevision()));
        return new Receipt(row.getId());
    }

    /** 未接车只释放此订单的精确旧任务，绝不能按driverId清掉另一个订单当前占用。 */
    private void releaseUnpicked(ViOrder order) {
        dispatch.closeForCancellation(order.getId());
        for (var assignment : mapper.assignments(order.getId())) {
            long id = number(assignment, "id");
            if (!Objects.equals(order.getActiveAssignmentId(), id))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "承接指针与实际任务不一致，请人工核查");
            mapper.releaseOccupancy(
                    number(assignment, "driver_id"), order.getId(), id, "ASSIGNMENT");
            if (mapper.endAssignment(id, order.getId()) != 1) conflict();
            // 实际结束事实与取消事务同提交；R01只能据此生成费用，不能由审核金额推算代驾报酬。
            outbox.publish(
                    "ASSIGNMENT_ENDED:" + id,
                    "VI_DRIVER_ASSIGNMENT",
                    id,
                    "ASSIGNMENT_ENDED",
                    Map.of(
                            "assignmentId",
                            Long.toString(id),
                            "orderId",
                            order.getId().toString(),
                            "endReason",
                            "CANCELLED"));
        }
        // 只返还未使用额度；CONSUMED是真实使用事实，不能因为取消而减回可约数量。
        for (var reservation : mapper.reservations(order.getId())) {
            String state = reservation.get("status").toString();
            if (!Set.of("HELD", "CONFIRMED").contains(state)) continue;
            int quantity = ((Number) reservation.get("quantity")).intValue();
            if (quantity <= 0
                    || mapper.releaseQuota(number(reservation, "slot_id"), quantity, state) != 1
                    || mapper.releaseReservation(number(reservation, "id"), state) != 1)
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "额度凭证与实际库存不一致，请核查");
        }
    }

    private boolean returnRequired(ViOrder order, AftersaleCase row) {
        if (!"CANCEL_REFUND".equals(row.getKind())) return false;
        if (order.getReturnedAt() != null) {
            if (!"OWNER".equals(order.getCustodyType()))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "归还事实与实际保管方不一致");
            return false;
        }
        return !"OWNER".equals(order.getCustodyType())
                || mapper.pickupHandover(order.getId()) > 0
                || Set.of(
                                "PICKED_UP",
                                "ARRIVED_STATION",
                                "INSPECTING",
                                "INSPECTED",
                                "CAR_TAKEN",
                                "WAIT_CONFIRM",
                                "COMPLETED")
                        .contains(order.getFulfillmentStatus());
    }

    private void requireAmount(PaymentTransaction payment, String kind, long amount) {
        if (!"SUCCESS".equals(payment.getStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        if (amount < 0 || "REFUND_ONLY".equals(kind) && amount == 0)
            throw new ViException(ViErrorCode.VI_DATA_INVALID);
        var balance = cases.balance(payment.getId());
        long available =
                number(balance, "paid_fen")
                        - number(balance, "refunded_fen")
                        - number(balance, "refund_reserved_fen");
        if (amount > available) throw new ViException(ViErrorCode.VI_REFUND_LIMIT);
    }

    private Map<String, Object> balance(PaymentTransaction payment) {
        var values = cases.balance(payment.getId());
        long paid = number(values, "paid_fen"),
                refunded = number(values, "refunded_fen"),
                reserved = number(values, "refund_reserved_fen");
        return Map.of(
                "paidAmount",
                paid,
                "refundedAmount",
                refunded,
                "reservedAmount",
                reserved,
                "availableAmount",
                paid - refunded - reserved);
    }

    private Map<String, Object> view(Context context, Actor actor) {
        AftersaleCase row = context.row();
        ViOrder order = context.order();
        Map<String, Object> result = publicViews.view(row, order);
        boolean finance = can(context.payment().getReceiverDeptId(), "biz:refund:query");
        boolean self = participants.contributed(row.getId(), actor.sysUserId());
        result.put("financeVisible", finance);
        for (String key :
                List.of("paidAmount", "refundedAmount", "reservedAmount", "availableAmount"))
            result.put(key, null);
        if (finance) result.putAll(balance(context.payment()));
        else {
            result.put("requestedAmount", null);
            result.put("approvedAmount", null);
            result.remove("refundNo");
        }
        result.put("applicantType", row.getApplicantType());
        result.put("applicantId", row.getApplicantId().toString());
        result.put("orderVersion", order.getVersion());
        result.put("assignmentId", external(order.getActiveAssignmentId()));
        result.put("selfReviewDenied", self);
        result.put("ownerAccountId", order.getOwnerAccountId().toString());
        result.put(
                "ownerLabel",
                Objects.requireNonNullElse(mapper.ownerLabel(order.getOwnerAccountId()), "车主"));
        result.put(
                "orderPlateNo",
                ViJson.tree(order.getVehicleSnapshot()).path("plateNo").isTextual()
                        ? ViJson.tree(order.getVehicleSnapshot()).path("plateNo").asText()
                        : null);
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (var review : mapper.reviews(row.getId())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("decision", review.get("decision"));
            item.put("approvedAmount", finance ? review.get("approved_fen") : null);
            item.put("opinion", review.get("opinion"));
            item.put("publicOpinion", review.get("public_opinion"));
            item.put("reviewerUserId", review.get("reviewer_user_id").toString());
            item.put("reviewedAt", review.get("reviewed_iso"));
            reviews.add(item);
        }
        result.put("reviews", reviews);
        result.put(
                "opinion",
                reviews.isEmpty() ? null : reviews.get(reviews.size() - 1).get("opinion"));
        List<String> actions = new ArrayList<>(List.of("VIEW_DETAIL"));
        if (Set.of("PENDING_REVIEW", "EXECUTING").contains(row.getCaseStatus())
                && can(order.getDeptId(), "biz:aftersale:note")) actions.add("ADD_NOTE");
        if ("PENDING_REVIEW".equals(row.getCaseStatus())
                && "PENDING".equals(row.getReviewStatus())
                && !self
                && can(order.getDeptId(), "biz:aftersale:review")
                && can(context.payment().getReceiverDeptId(), "biz:refund:approve"))
            actions.add("REVIEW_AFTERSALE");
        result.put("allowedActions", actions);
        return result;
    }

    private static void pending(AftersaleCase row) {
        if (!"PENDING_REVIEW".equals(row.getCaseStatus())
                || !"PENDING".equals(row.getReviewStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "该申请已经审核，不能重复决定或直接改金额");
    }

    private static long number(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private static String external(Long id) {
        return id == null ? null : id.toString();
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
