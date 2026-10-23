package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.mapper.AfterSaleMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.PayMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.DeptScope;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 统一取消与退款售后。
 *
 * <p>一次审核同时给出三件事的结论：是否同意取消、车辆是否需要先实还、批准退款金额。
 * 审核通过后按 custody 决定推进路径：车辆仍在外则由代驾实还触发收尾，
 * 车辆已在车主手里则立即取消并退款。退款金额受"实付 - 已退 - 已预留"强约束，
 * 同一 case 的重试不会重复退款。</p>
 */
@Service
public class AfterSaleService
{
    @Autowired
    private AfterSaleMapper afterSaleMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventService eventService;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private PayConfigService payConfigService;

    @Autowired
    private com.xinyi.rongli.support.WechatPayClient wechatPayClient;

    // ------------------------------------------------------------------ 申请

    /** 车主侧：创建统一取消与退款申请 */
    @Transactional
    public Map<String, Object> createByOwner(String orderNo, MiniDtos.AfterSaleApply req)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        return create(order, req, "ACCOUNT", p.accountId, false);
    }

    /** 管理端：代客创建申请 */
    @Transactional
    public Map<String, Object> createByAdmin(String orderNo, MiniDtos.AfterSaleApply req, Long userId)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return create(order, req, "ADMIN", userId, true);
    }

    private Map<String, Object> create(Row order, MiniDtos.AfterSaleApply req, String applicantType, Long applicantId,
            boolean admin)
    {
        Long orderId = order.lng("id");
        String status = order.str("fulfillment_status");
        if (Codes.Fulfill.CANCELLED.equals(status) || Codes.Fulfill.CLOSED.equals(status)
                || Codes.Fulfill.COMPLETED.equals(status))
        {
            throw ApiException.stateConflict("该订单已结束，不能再次申请取消或退款");
        }
        if (afterSaleMapper.findOpenCaseOfOrder(orderId) != null)
        {
            throw ApiException.stateConflict("该订单已有处理中的售后申请");
        }
        String kind = req == null || req.kind == null ? Codes.AfterSale.CANCEL_REFUND : req.kind.toUpperCase();
        if (!Codes.AfterSale.CANCEL_REFUND.equals(kind) && !Codes.AfterSale.REFUND_ONLY.equals(kind))
        {
            throw ApiException.validation("售后类型不正确");
        }
        String reason = req == null || req.reason == null || req.reason.isBlank()
                ? "用户申请取消并退款" : req.reason.trim();
        if (reason.length() > 500)
        {
            throw ApiException.validation("申请原因不能超过 500 字");
        }
        Row pay = Row.of(payMapper.findByOrder(orderId));
        long requested = req != null && req.requestedFen != null ? req.requestedFen
                : (pay.isEmpty() ? order.lng("total_fen", 0L) : pay.lng("paid_fen", 0L));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("caseNo", nextCaseNo());
        row.put("orderId", orderId);
        row.put("paymentId", pay.isEmpty() ? null : pay.lng("id"));
        row.put("kind", kind);
        row.put("requestedFen", Math.max(0L, requested));
        row.put("applicantType", applicantType);
        row.put("applicantId", applicantId);
        row.put("reason", reason);
        row.put("evidenceIds", req == null ? null : req.evidenceIds);
        afterSaleMapper.insertCase(row);
        Long caseId = ((Number) row.get("id")).longValue();

        eventService.record("AFTERSALE", caseId, "AFTERSALE_APPLIED", orderId, null,
                Map.of("kind", kind, "requestedFen", String.valueOf(requested)), reason);
        eventService.publish("AFTERSALE", caseId, "AFTERSALE_APPLIED",
                Map.of("caseNo", row.get("caseNo"), "orderNo", order.str("order_no")));
        return detail(caseId, admin);
    }

    private String nextCaseNo()
    {
        return "AS" + ViTime.now().atZone(java.time.ZoneOffset.UTC)
                .withZoneSameInstant(ViTime.SHANGHAI).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + crypto.randomDigits(4);
    }

    // ------------------------------------------------------------------ 查询

    public Map<String, Object> listOfOrder(String orderNo, boolean admin)
    {
        Row order = admin ? Row.of(orderMapper.findDetailByNo(orderNo))
                : bookingService.requireOwnerOrder(orderNo, MiniContext.require().accountId);
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        List<Map<String, Object>> cases = new ArrayList<>();
        for (Map<String, Object> c : afterSaleMapper.listByOrder(order.lng("id")))
        {
            Map<String, Object> view = summary(Row.of(c));
            cases.add(view);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", order.str("order_no"));
        result.put("total", cases.size());
        result.put("cases", cases);
        return result;
    }

    public Map<String, Object> myCases(int pageNum, int pageSize)
    {
        MiniPrincipal p = MiniContext.require();
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> c : afterSaleMapper.listByApplicant(p.accountId, offset, limit))
        {
            list.add(summary(Row.of(c)));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("rows", list);
        return result;
    }

    public Map<String, Object> adminCases(String status, String kind, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> list = new ArrayList<>();
        Long deptId = DeptScope.scopedDeptId();
        for (Map<String, Object> c : afterSaleMapper.adminList(status, kind, deptId, offset, limit))
        {
            list.add(summary(Row.of(c)));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageNum", pageNum);
        result.put("pageSize", limit);
        result.put("total", afterSaleMapper.adminCount(status, kind, deptId));
        result.put("rows", list);
        return result;
    }

    /**
     * 二次授权：后台按 id 直达详情/审批/退款时，仍需校验该单归属部门，
     * 防止跨部门越权（列表过滤不能替代详情鉴权）。
     */
    private void assertAdminDept(Long caseId)
    {
        Long deptId = DeptScope.scopedDeptId();
        if (deptId != null && afterSaleMapper.countInDept(caseId, deptId) == 0)
        {
            throw ApiException.forbidden("该售后单不在您的部门数据范围内");
        }
    }

    private Map<String, Object> summary(Row c)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("caseId", String.valueOf(c.lng("id")));
        view.put("caseNo", c.str("case_no"));
        view.put("orderNo", c.str("order_no"));
        view.put("kind", c.str("kind"));
        view.put("kindLabel", Codes.AfterSale.CANCEL_REFUND.equals(c.str("kind")) ? "取消并退款" : "仅退款");
        view.put("caseStatus", c.str("case_status"));
        view.put("caseStatusLabel", caseStatusLabel(c.str("case_status")));
        view.put("reviewStatus", c.str("review_status"));
        view.put("requestedFen", c.lng("requested_fen", 0L));
        view.put("approvedFen", c.lng("approved_fen", 0L));
        view.put("returnState", c.str("return_state"));
        view.put("refundState", c.str("refund_state"));
        view.put("orderStatus", c.str("fulfillment_status"));
        view.put("reason", c.str("reason"));
        view.put("createdAt", ViTime.iso(c.time("created_at")));
        return view;
    }

    public Map<String, Object> detail(Long caseId, boolean admin)
    {
        Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("售后申请不存在");
        }
        if (!admin)
        {
            MiniPrincipal p = MiniContext.require();
            if (!"ACCOUNT".equals(c.str("applicant_type")) || !Objects.equals(p.accountId, c.lng("applicant_id")))
            {
                throw ApiException.forbidden("无权查看该售后申请");
            }
        }
        else
        {
            assertAdminDept(caseId);
        }
        Map<String, Object> view = summary(c);
        view.put("orderId", String.valueOf(c.lng("order_id")));
        view.put("custodyType", c.str("custody_type"));
        view.put("orderTotalFen", c.lng("order_total_fen", 0L));
        view.put("pickupStartsAt", ViTime.iso(c.time("pickup_starts_at")));
        view.put("evidenceIds", c.json("evidence_ids"));
        view.put("resolvedAt", ViTime.iso(c.time("resolved_at")));
        view.put("reviewRevision", c.intg("review_revision", 0));
        Row pay = Row.of(payMapper.findByOrder(c.lng("order_id")));
        if (!pay.isEmpty())
        {
            view.put("paidFen", pay.lng("paid_fen", 0L));
            view.put("refundedFen", pay.lng("refunded_fen", 0L));
            view.put("refundReservedFen", pay.lng("refund_reserved_fen", 0L));
            view.put("refundableFen", Math.max(0L, pay.lng("paid_fen", 0L) - pay.lng("refunded_fen", 0L)
                    - pay.lng("refund_reserved_fen", 0L)));
        }
        Row refund = Row.of(payMapper.findLatestRefundOfCase(caseId));
        if (!refund.isEmpty())
        {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("refundNo", refund.str("out_refund_no"));
            r.put("amountFen", refund.lng("amount_fen", 0L));
            r.put("status", refund.str("status"));
            r.put("statusLabel", refundStatusLabel(refund.str("status")));
            r.put("successAt", ViTime.iso(refund.time("success_at")));
            view.put("refund", r);
        }
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Map<String, Object> r : afterSaleMapper.listReviews(caseId))
        {
            Row rv = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("revision", rv.intg("revision"));
            item.put("decision", rv.str("decision"));
            item.put("approvedFen", rv.lng("approved_fen", 0L));
            item.put("returnRequired", rv.bool("return_required"));
            item.put("custodyAtReview", rv.str("custody_at_review"));
            item.put("opinion", rv.str("opinion"));
            item.put("reviewedAt", ViTime.iso(rv.time("reviewed_at")));
            reviews.add(item);
        }
        view.put("reviews", reviews);
        return view;
    }

    /** 补充售后说明：追加事件留痕，不改变既有审核结论 */
    @Transactional
    public Map<String, Object> addNote(Long caseId, String note, boolean admin)
    {
        Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("售后申请不存在");
        }
        if (!admin)
        {
            MiniPrincipal p = MiniContext.require();
            if (!Objects.equals(p.accountId, c.lng("applicant_id")))
            {
                throw ApiException.forbidden("无权补充该售后申请");
            }
        }
        if (note == null || note.isBlank())
        {
            throw ApiException.validation("请填写补充说明");
        }
        if (note.length() > 1000)
        {
            throw ApiException.validation("补充说明不能超过 1000 字");
        }
        eventService.record("AFTERSALE", caseId, admin ? "ADMIN_NOTE" : "APPLICANT_NOTE",
                c.lng("order_id"), null, Map.of("length", String.valueOf(note.length())), note);
        return detail(caseId, true);
    }

    // ------------------------------------------------------------------ 审核

    /** 审核前预览：把"同意后会怎样"提前算清楚，避免审核人凭印象决策 */
    public Map<String, Object> reviewPreview(Long caseId)
    {
        assertAdminDept(caseId);
        Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("售后申请不存在");
        }
        Row order = Row.of(orderMapper.findDetailById(c.lng("order_id")));
        Row pay = Row.of(payMapper.findByOrder(c.lng("order_id")));
        long paid = pay.isEmpty() ? 0L : pay.lng("paid_fen", 0L);
        long refunded = pay.isEmpty() ? 0L : pay.lng("refunded_fen", 0L);
        long reserved = pay.isEmpty() ? 0L : pay.lng("refund_reserved_fen", 0L);
        long refundable = Math.max(0L, paid - refunded - reserved);
        String custody = order.str("custody_type");
        boolean onTheWay = order.lng("active_assignment_id") != null
                && !Codes.Custody.OWNER.equals(custody);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("caseId", String.valueOf(caseId));
        view.put("orderStatus", order.str("fulfillment_status"));
        view.put("custodyType", custody);
        view.put("paidFen", paid);
        view.put("refundedFen", refunded);
        view.put("refundReservedFen", reserved);
        view.put("refundableFen", refundable);
        view.put("requestedFen", c.lng("requested_fen", 0L));
        view.put("suggestedFen", Math.min(c.lng("requested_fen", 0L), refundable));
        view.put("returnRequired", onTheWay);
        view.put("returnStateAfterApprove", onTheWay
                ? Codes.AfterSale.RETURN_WAIT : Codes.AfterSale.RETURN_NOT_REQUIRED);
        view.put("refundStateAfterApprove", refundable <= 0
                ? Codes.AfterSale.REFUND_NOT_REQUIRED
                : (onTheWay ? Codes.AfterSale.REFUND_WAIT_RETURN : Codes.AfterSale.REFUND_READY));
        List<String> effects = new ArrayList<>();
        if (Codes.Custody.OWNER.equals(custody))
        {
            effects.add("车辆已在车主手中，同意后立即取消订单并释放检测名额");
        }
        else
        {
            effects.add("车辆尚在" + ("DRIVER".equals(custody) ? "代驾" : "检测站")
                    + "手中，同意后订单进入待实还，需完成还车交接才会退款");
        }
        if (Codes.Fulfill.PENDING_DISPATCH.equals(order.str("fulfillment_status"))
                || Codes.Fulfill.DISPATCHING.equals(order.str("fulfillment_status"))
                || Codes.Fulfill.MANUAL_DISPATCH.equals(order.str("fulfillment_status")))
        {
            effects.add("撤销进行中的邀约并结束派单");
        }
        if (refundable <= 0)
        {
            effects.add("当前没有可退金额，将只做取消处理");
        }
        else
        {
            effects.add("退款 ¥" + String.format("%.2f", Math.min(c.lng("requested_fen", 0L), refundable) / 100.0)
                    + "，预留后由渠道退款，成功前不会重复退款");
        }
        view.put("effects", effects);
        return view;
    }

    /**
     * 一次统一审核。
     *
     * @param approve   true 同意，false 驳回
     * @param approvedFen 批准退款金额（分），null 时取 申请金额 与 可退金额 的较小值
     * @param returnRequired 是否要求先实还；null 时按 custody 自动判定
     */
    @Transactional
    public Map<String, Object> review(Long caseId, boolean approve, Long approvedFen, Boolean returnRequired,
            String opinion, Long reviewerUserId)
    {
        assertAdminDept(caseId);
        Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("售后申请不存在");
        }
        if (!Codes.AfterSale.REVIEW_PENDING.equals(c.str("review_status")))
        {
            throw ApiException.stateConflict("该售后申请已完成审核");
        }
        Long orderId = c.lng("order_id");
        Row order = Row.of(orderMapper.findDetailById(orderId));
        if (!approve)
        {
            if (afterSaleMapper.rejectCase(caseId) == 0)
            {
                throw ApiException.stateConflict("售后申请状态已变化，请刷新");
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("caseId", caseId);
            row.put("revision", c.intg("review_revision", 1));
            row.put("decision", Codes.AfterSale.REVIEW_REJECTED);
            row.put("approvedFen", 0L);
            row.put("observedOrderVersion", order.lng("version", 0L));
            row.put("custodyAtReview", order.str("custody_type"));
            row.put("returnRequired", false);
            row.put("reviewerUserId", reviewerUserId);
            row.put("opinion", opinion);
            afterSaleMapper.insertReview(row);
            eventService.record("AFTERSALE", caseId, "AFTERSALE_REJECTED", orderId, null, null, opinion);
            eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "AFTERSALE_REJECTED",
                    "您的售后申请未通过：" + (opinion == null ? "不满足取消退款条件" : opinion));
            return detail(caseId, true);
        }

        Row pay = Row.of(payMapper.findByOrder(orderId));
        long paid = pay.isEmpty() ? 0L : pay.lng("paid_fen", 0L);
        long refundable = pay.isEmpty() ? 0L
                : Math.max(0L, paid - pay.lng("refunded_fen", 0L) - pay.lng("refund_reserved_fen", 0L));
        long want = approvedFen == null ? Math.min(c.lng("requested_fen", 0L), refundable) : approvedFen;
        if (want < 0)
        {
            throw ApiException.validation("退款金额不能为负数");
        }
        if (want > refundable)
        {
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT,
                    "批准金额超过当前可退金额（可退 " + refundable + " 分）");
        }
        boolean onTheWay = order.lng("active_assignment_id") != null
                && !Codes.Custody.OWNER.equals(order.str("custody_type"));
        boolean needReturn = returnRequired == null ? onTheWay : returnRequired;
        if (needReturn && Codes.Custody.OWNER.equals(order.str("custody_type")))
        {
            // 车已在车主手中，"要求先归还"没有可执行对象，按不需要归还处理
            needReturn = false;
        }

        String returnState = needReturn ? Codes.AfterSale.RETURN_WAIT : Codes.AfterSale.RETURN_NOT_REQUIRED;
        String refundState = want <= 0 ? Codes.AfterSale.REFUND_NOT_REQUIRED
                : (needReturn ? Codes.AfterSale.REFUND_WAIT_RETURN : Codes.AfterSale.REFUND_READY);
        String caseStatus = needReturn ? Codes.AfterSale.EXECUTING : Codes.AfterSale.EXECUTING;

        if (afterSaleMapper.applyReview(caseId, caseStatus, Codes.AfterSale.REVIEW_APPROVED, want,
                returnState, refundState) == 0)
        {
            throw ApiException.stateConflict("售后申请状态已变化，请刷新");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("caseId", caseId);
        row.put("revision", c.intg("review_revision", 1));
        row.put("decision", Codes.AfterSale.REVIEW_APPROVED);
        row.put("approvedFen", want);
        row.put("observedOrderVersion", order.lng("version", 0L));
        row.put("custodyAtReview", order.str("custody_type"));
        row.put("returnRequired", needReturn);
        row.put("reviewerUserId", reviewerUserId);
        row.put("opinion", opinion);
        afterSaleMapper.insertReview(row);

        eventService.record("AFTERSALE", caseId, "AFTERSALE_APPROVED", orderId, null,
                Map.of("approvedFen", String.valueOf(want), "returnRequired", String.valueOf(needReturn)), opinion);

        if (!needReturn)
        {
            // 车已在车主手中：先撤销在途安排（若有），再取消订单、释放名额与台账，最后退款
            if (order.lng("active_assignment_id") != null)
            {
                dispatchService.releaseForCancellation(orderId, "售后审核通过取消");
            }
            applyCancellation(order, caseId);
            executeRefund(caseId);
        }
        else
        {
            // 需先实还：订单打上"取消已批准待实还"，还车交接成功时正式办结并退款
            orderMapper.updateCancelStatus(orderId, Codes.Cancel.APPROVED_WAIT_RETURN);
            eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "AFTERSALE_WAIT_RETURN",
                    "售后已通过，待车辆归还后自动退款");
        }
        return detail(caseId, true);
    }

    /** 审核通过且无需实还：取消订单并释放名额与承接 */
    private void applyCancellation(Row order, Long caseId)
    {
        Long orderId = order.lng("id");
        String status = order.str("fulfillment_status");
        if (Codes.Fulfill.CANCELLED.equals(status))
        {
            return;
        }
        if (order.lng("active_assignment_id") != null)
        {
            dispatchService.releaseForCancellation(orderId, "售后取消");
        }
        orderMapper.updateCancelStatus(orderId, Codes.Cancel.COMPLETED);
        orderMapper.casStatusByState(orderId, status, Codes.Fulfill.CANCELLED);
        orderMapper.markCancelled(orderId);
        bookingService.releaseReservations(orderId, Codes.Reservation.CONFIRMED, true);
        bookingService.releaseReservations(orderId, Codes.Reservation.HELD, false);
        eventService.record("ORDER", orderId, "ORDER_CANCELLED_BY_AFTERSALE", orderId,
                Map.of("status", status), Map.of("status", Codes.Fulfill.CANCELLED),
                "售后单 " + caseId + " 审核通过");
        eventService.publish("ORDER", orderId, "ORDER_CANCELLED", Map.of("orderNo", order.str("order_no")));
    }

    // ------------------------------------------------------------------ 退款执行

    /**
     * 执行渠道退款：预留金额 → 建立退款单 → 调用渠道。
     * 联调通道直接成功；真实通道未配置凭据时保持 PROCESSING 并转人工对账，不伪造成功。
     */
    @Transactional
    public Map<String, Object> executeRefund(Long caseId)
    {
        assertAdminDept(caseId);
        Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("售后申请不存在");
        }
        if (!Codes.AfterSale.REVIEW_APPROVED.equals(c.str("review_status")))
        {
            throw ApiException.stateConflict("售后尚未审核通过");
        }
        long approved = c.lng("approved_fen", 0L);
        if (approved <= 0)
        {
            afterSaleMapper.updateProgress(caseId, closeStatus(c), Codes.AfterSale.RETURN_DONE,
                    Codes.AfterSale.REFUND_NOT_REQUIRED, ViTime.now());
            eventService.record("AFTERSALE", caseId, "AFTERSALE_CLOSED_NO_REFUND", c.lng("order_id"), null, null, null);
            return detail(caseId, true);
        }
        Row existing = Row.of(payMapper.findLatestRefundOfCase(caseId));
        if (!existing.isEmpty())
        {
            if (Codes.AfterSale.REFUND_SUCCESS.equals(existing.str("status")))
            {
                afterSaleMapper.updateProgress(caseId, closeStatus(c), Codes.AfterSale.RETURN_DONE,
                        Codes.AfterSale.REFUND_SUCCESS, ViTime.now());
                return detail(caseId, true);
            }
            if (Codes.AfterSale.REFUND_PROCESSING.equals(existing.str("status"))
                    || Codes.AfterSale.REFUND_UNKNOWN.equals(existing.str("status")))
            {
                // 已有退款单在途：不重复建单，等价重试语义
                return detail(caseId, true);
            }
        }
        Long paymentId = c.lng("payment_id");
        if (paymentId == null)
        {
            Row pay = Row.of(payMapper.findByOrder(c.lng("order_id")));
            paymentId = pay.isEmpty() ? null : pay.lng("id");
        }
        if (paymentId == null)
        {
            throw ApiException.stateConflict("该订单没有支付单，无法退款");
        }
        if (payMapper.reserveRefund(paymentId, approved) == 0)
        {
            afterSaleMapper.updateProgress(caseId, Codes.AfterSale.EXECUTING, c.str("return_state"),
                    Codes.AfterSale.REFUND_ABNORMAL, null);
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "可退金额不足，退款未发起，请转人工对账");
        }
        Map<String, Object> refund = new LinkedHashMap<>();
        refund.put("caseId", caseId);
        refund.put("reviewRevision", c.intg("review_revision", 1));
        refund.put("paymentId", paymentId);
        refund.put("outRefundNo", "RF" + c.str("case_no"));
        refund.put("amountFen", approved);
        refund.put("status", Codes.AfterSale.REFUND_PROCESSING);
        payMapper.insertRefund(refund);
        Long refundId = ((Number) refund.get("id")).longValue();

        if (config.mockPayment())
        {
            payMapper.settleRefund(paymentId, approved);
            payMapper.updateRefundStatus(refundId, Codes.AfterSale.REFUND_SUCCESS, "MOCK" + refund.get("outRefundNo"));
            afterSaleMapper.updateProgress(caseId, closeStatus(c), Codes.AfterSale.RETURN_DONE,
                    Codes.AfterSale.REFUND_SUCCESS, ViTime.now());
            eventService.record("AFTERSALE", caseId, "REFUND_SUCCEEDED", c.lng("order_id"), null,
                    Map.of("amountFen", String.valueOf(approved)), "联调通道退款");
            eventService.notify(Row.of(orderMapper.findById(c.lng("order_id"))).lng("owner_account_id"),
                    Codes.Role.OWNER, c.lng("order_id"), "REFUND_SUCCEEDED",
                    "退款 ¥" + String.format("%.2f", approved / 100.0) + " 已原路退回");
        }
        else
        {
            afterSaleMapper.updateProgress(caseId, Codes.AfterSale.EXECUTING, c.str("return_state"),
                    Codes.AfterSale.REFUND_PROCESSING, null);
            // 真实通道必须真的向微信发起退款；提交失败要释放预留，否则这笔可退金额会被永久占用
            Row payRow = Row.of(payMapper.findByOrder(c.lng("order_id")));
            try
            {
                var result = wechatPayClient.refund(payConfigService.settings(), payRow.str("out_trade_no"),
                        String.valueOf(refund.get("outRefundNo")), c.str("reason"), approved,
                        payRow.lng("total_fen", approved));
                payMapper.updateRefundStatus(refundId, Codes.AfterSale.REFUND_PROCESSING,
                        (String) result.get("refundId"));
                eventService.record("AFTERSALE", caseId, "REFUND_SUBMITTED", c.lng("order_id"), null,
                        Map.of("amountFen", String.valueOf(approved),
                                "refundId", String.valueOf(result.get("refundId"))),
                        "已提交渠道退款，等待回调");
            }
            catch (RuntimeException ex)
            {
                payMapper.releaseRefundReserve(paymentId, approved);
                payMapper.updateRefundStatus(refundId, Codes.AfterSale.REFUND_ABNORMAL, null);
                afterSaleMapper.updateProgress(caseId, Codes.AfterSale.EXECUTING, c.str("return_state"),
                        Codes.AfterSale.REFUND_ABNORMAL, null);
                eventService.record("AFTERSALE", caseId, "REFUND_SUBMIT_FAILED", c.lng("order_id"), null,
                        Map.of("amountFen", String.valueOf(approved)), ex.getMessage());
                throw ex;
            }
        }
        return detail(caseId, true);
    }

    private String closeStatus(Row c)
    {
        return Codes.AfterSale.CANCEL_REFUND.equals(c.str("kind"))
                ? Codes.AfterSale.CLOSED : Codes.AfterSale.CLOSED;
    }

    /**
     * 车辆完成实还后的售后收尾。由还车交接成功事务内调用：
     * 待实还的售后单在此取消订单并触发退款。
     */
    @Transactional
    public void onVehicleReturned(Long orderId)
    {
        Map<String, Object> open = afterSaleMapper.findOpenCaseOfOrder(orderId);
        if (open == null)
        {
            return;
        }
        Row c = Row.of(open);
        if (!Codes.AfterSale.REVIEW_APPROVED.equals(c.str("review_status")))
        {
            return;
        }
        afterSaleMapper.updateProgress(c.lng("id"), Codes.AfterSale.EXECUTING,
                Codes.AfterSale.RETURN_DONE, Codes.AfterSale.REFUND_READY, null);
        Row order = Row.of(orderMapper.findDetailById(orderId));
        applyCancellation(order, c.lng("id"));
        executeRefund(c.lng("id"));
    }

    /** 渠道退款回调处理（幂等） */
    @Transactional
    public void applyRefundResult(String outRefundNo, boolean success, String channelRefundId)
    {
        Row refund = Row.of(payMapper.findByRefundNo(outRefundNo));
        if (refund.isEmpty())
        {
            return;
        }
        if (Codes.AfterSale.REFUND_SUCCESS.equals(refund.str("status")))
        {
            return;
        }
        Long caseId = refund.lng("case_id");
        Long paymentId = refund.lng("payment_id");
        long amount = refund.lng("amount_fen", 0L);
        if (success)
        {
            payMapper.settleRefund(paymentId, amount);
            payMapper.updateRefundStatus(refund.lng("id"), Codes.AfterSale.REFUND_SUCCESS, channelRefundId);
            Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
            afterSaleMapper.updateProgress(caseId, Codes.AfterSale.CLOSED, Codes.AfterSale.RETURN_DONE,
                    Codes.AfterSale.REFUND_SUCCESS, ViTime.now());
            Row order = Row.of(orderMapper.findDetailById(c.lng("order_id")));
            eventService.record("AFTERSALE", caseId, "REFUND_SUCCEEDED", c.lng("order_id"), null,
                    Map.of("amountFen", String.valueOf(amount)), "渠道退款回调");
            eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, c.lng("order_id"),
                    "REFUND_SUCCEEDED", "退款 ¥" + String.format("%.2f", amount / 100.0) + " 已原路退回");
        }
        else
        {
            payMapper.releaseRefundReserve(paymentId, amount);
            payMapper.updateRefundStatus(refund.lng("id"), "FAILED", channelRefundId);
            Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
            afterSaleMapper.updateProgress(caseId, Codes.AfterSale.CLOSED, c.str("return_state"),
                    Codes.AfterSale.REFUND_ABNORMAL, null);
            eventService.record("AFTERSALE", caseId, "REFUND_FAILED", c.lng("order_id"), null,
                    Map.of("amountFen", String.valueOf(amount)), "渠道退款失败，转人工对账");
        }
    }

    // ------------------------------------------------------------------ 退款渠道运维

    public Map<String, Object> refundDetail(String refundNo)
    {
        Row refund = Row.of(payMapper.findByRefundNo(refundNo));
        if (refund.isEmpty())
        {
            throw ApiException.notFound("退款单不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("refundNo", refund.str("out_refund_no"));
        view.put("channelRefundId", refund.str("channel_refund_id"));
        view.put("amountFen", refund.lng("amount_fen", 0L));
        view.put("status", refund.str("status"));
        view.put("statusLabel", refundStatusLabel(refund.str("status")));
        view.put("successAt", ViTime.iso(refund.time("success_at")));
        view.put("lastQueryAt", ViTime.iso(refund.time("last_query_at")));
        view.put("caseId", String.valueOf(refund.lng("case_id")));
        return view;
    }

    /** 主动查退款：联调通道按已预留即视为成功；真实通道未配置时保持原状并提示 */
    @Transactional
    public Map<String, Object> queryRefund(String refundNo)
    {
        Row refund = Row.of(payMapper.findByRefundNo(refundNo));
        if (refund.isEmpty())
        {
            throw ApiException.notFound("退款单不存在");
        }
        if (!config.mockPayment())
        {
            // 以渠道结果为准：查询成功即按渠道状态推进，查询失败保持原状态并记录查询时间
            var result = wechatPayClient.queryRefund(payConfigService.settings(), refundNo);
            String channelStatus = String.valueOf(result.get("status"));
            if ("SUCCESS".equalsIgnoreCase(channelStatus))
            {
                applyRefundResult(refundNo, true, (String) result.get("refundId"));
            }
            else if ("ABNORMAL".equalsIgnoreCase(channelStatus) || "CLOSED".equalsIgnoreCase(channelStatus))
            {
                applyRefundResult(refundNo, false, (String) result.get("refundId"));
            }
            else
            {
                payMapper.updateRefundStatus(refund.lng("id"), Codes.AfterSale.REFUND_PROCESSING,
                        (String) result.get("refundId"));
            }
            Map<String, Object> view = refundDetail(refundNo);
            view.put("channelStatus", channelStatus);
            view.put("hint", "已按微信渠道返回状态同步");
            return view;
        }
        if (Codes.AfterSale.REFUND_PROCESSING.equals(refund.str("status"))
                || Codes.AfterSale.REFUND_UNKNOWN.equals(refund.str("status")))
        {
            applyRefundResult(refundNo, true, "MOCK" + refundNo);
        }
        return refundDetail(refundNo);
    }

    /** 安全重试：只在失败态重试，在途态不重复提交，避免重复退款 */
    @Transactional
    public Map<String, Object> retryRefund(String refundNo)
    {
        Row refund = Row.of(payMapper.findByRefundNo(refundNo));
        if (refund.isEmpty())
        {
            throw ApiException.notFound("退款单不存在");
        }
        String status = refund.str("status");
        if (Codes.AfterSale.REFUND_SUCCESS.equals(status))
        {
            throw ApiException.stateConflict("该退款已成功，不能重试");
        }
        if (Codes.AfterSale.REFUND_PROCESSING.equals(status) || Codes.AfterSale.REFUND_UNKNOWN.equals(status))
        {
            Map<String, Object> view = refundDetail(refundNo);
            view.put("hint", "退款在途，已拒绝重复提交以防重复退款；请先主动查询渠道状态");
            return view;
        }
        Long caseId = refund.lng("case_id");
        Row c = Row.of(afterSaleMapper.findCaseDetail(caseId));
        afterSaleMapper.updateProgress(caseId, Codes.AfterSale.EXECUTING, c.str("return_state"),
                Codes.AfterSale.REFUND_READY, null);
        executeRefund(caseId);
        eventService.record("AFTERSALE", caseId, "REFUND_RETRIED", c.lng("order_id"), null,
                Map.of("refundNo", refundNo), "人工受控重试");
        return detail(caseId, true);
    }

    // ------------------------------------------------------------------ 文案

    public String caseStatusLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.AfterSale.PENDING_REVIEW -> "待审核";
            case Codes.AfterSale.EXECUTING -> "执行中";
            case Codes.AfterSale.CLOSED -> "已完成";
            case Codes.AfterSale.CASE_REJECTED -> "已驳回";
            default -> status;
        };
    }

    public String refundStatusLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.AfterSale.REFUND_NONE -> "未发起";
            case Codes.AfterSale.REFUND_NOT_REQUIRED -> "无需退款";
            case Codes.AfterSale.REFUND_WAIT_RETURN -> "待实还";
            case Codes.AfterSale.REFUND_READY -> "待退款";
            case Codes.AfterSale.REFUND_PROCESSING -> "退款中";
            case Codes.AfterSale.REFUND_SUCCESS -> "退款成功";
            case Codes.AfterSale.REFUND_UNKNOWN -> "结果未知";
            case Codes.AfterSale.REFUND_ABNORMAL -> "退款异常";
            case "FAILED" -> "退款失败";
            default -> status;
        };
    }

    /** 已结清订单不允许继续发起售后，供 Controller 复用 */
    public void requireRefundable(Long orderId)
    {
        Row pay = Row.of(payMapper.findByOrder(orderId));
        if (pay.isEmpty() || !Codes.Payment.SUCCESS.equals(pay.str("status")))
        {
            throw ApiException.stateConflict("该订单没有成功支付，无需退款");
        }
    }

    /** 供还车链路判断是否需要等退款 */
    public boolean hasWaitingRefund(Long orderId)
    {
        Map<String, Object> open = afterSaleMapper.findOpenCaseOfOrder(orderId);
        if (open == null)
        {
            return false;
        }
        Row c = Row.of(open);
        return Codes.AfterSale.REVIEW_APPROVED.equals(c.str("review_status"))
                && Codes.AfterSale.REFUND_WAIT_RETURN.equals(c.str("refund_state"));
    }

    /** 台账汇总用：返回订单关联的售后单号列表 */
    public List<String> caseNosOfOrder(Long orderId)
    {
        List<String> list = new ArrayList<>();
        for (Map<String, Object> c : afterSaleMapper.listByOrder(orderId))
        {
            list.add(Row.of(c).str("case_no"));
        }
        return list;
    }
}
