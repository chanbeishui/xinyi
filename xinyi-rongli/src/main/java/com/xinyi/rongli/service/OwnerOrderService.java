package com.xinyi.rongli.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.mapper.FulfillMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.PayMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 车主侧订单延伸能力：时间轴、可信支付结果、结构化检测结果、服务争议与评价。
 *
 * <p>这些能力只读或只追加，不推进履约状态；状态推进仍由履约服务统一处理。</p>
 */
@Service
public class OwnerOrderService
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private FulfillMapper fulfillMapper;

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventService eventService;

    // ------------------------------------------------------------------ 时间轴

    public Map<String, Object> timeline(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Map<String, Object> e : miscMapper.timelineEvents(order.lng("id")))
        {
            Row ev = Row.of(e);
            Map<String, Object> node = new LinkedHashMap<>();
            String action = ev.str("action");
            node.put("action", action);
            node.put("label", timelineLabel(ev, action));
            node.put("realm", ev.str("actor_realm"));
            node.put("role", ev.str("acting_role"));
            node.put("reason", ev.str("reason"));
            node.put("at", ViTime.iso(ev.time("created_at")));
            nodes.add(node);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("total", nodes.size());
        view.put("nodes", nodes);
        return view;
    }

    /** 交接类事件按交接类型区分，避免时间线上出现多个同名的「发起交接」 */
    private String timelineLabel(Row ev, String action)
    {
        String label = actionLabel(action);
        if (action != null && action.startsWith("HANDOVER"))
        {
            String type = Json.read(ev.json("after_summary")).getString("type");
            String suffix = switch (type == null ? "" : type)
            {
                case "OWNER_TO_DRIVER" -> "车主交车给代驾";
                case "DRIVER_TO_STATION" -> "代驾交车给检测点";
                case "STATION_TO_DRIVER" -> "检测点交车给代驾";
                case "DRIVER_TO_OWNER" -> "代驾归还车主";
                default -> "";
            };
            if (!suffix.isEmpty())
            {
                label = label + "（" + suffix + "）";
            }
        }
        return label;
    }

    private String actionLabel(String action)
    {
        if (action == null)
        {
            return "";
        }
        return switch (action)
        {
            case "ORDER_CREATED" -> "提交预约";
            case "PAYMENT_SUCCEEDED" -> "支付成功";
            case "ORDER_CLOSED_UNPAID" -> "超时未支付关闭";
            case "DISPATCH_STARTED" -> "开始派单";
            case "INVITE_ISSUED" -> "发送邀约";
            case "INVITE_ACCEPTED" -> "代驾接单";
            case "INVITE_REJECTED" -> "代驾拒单";
            case "INVITE_EXPIRED" -> "邀约超时";
            case "MANUAL_DISPATCH" -> "转人工派单";
            case "DRIVER_DEPARTED" -> "代驾出发";
            case "ARRIVED_PICKUP" -> "代驾到达接车点";
            case "ARRIVED_STATION" -> "车辆到达检测站";
            case "HANDOVER_CREATED" -> "发起交接";
            case "HANDOVER_CONFIRMED" -> "交接完成";
            case "LICENSE_REVIEWED" -> "行驶证核验";
            case "RESULT_REGISTERED" -> "检测结果登记";
            case "RESULT_CORRECTION_APPLIED" -> "申请结果更正";
            case "VEHICLE_RETURNED" -> "车辆已归还";
            case "SERVICE_CONFIRMED" -> "车主确认服务";
            case "ORDER_AUTO_COMPLETED" -> "超时自动完成";
            case "AFTERSALE_APPLIED" -> "提交售后申请";
            case "AFTERSALE_APPROVED" -> "售后审核通过";
            case "AFTERSALE_REJECTED" -> "售后审核驳回";
            case "REFUND_SUCCEEDED" -> "退款到账";
            case "DISPUTE_CREATED" -> "发起服务争议";
            case "DISPUTE_RESOLVED" -> "争议处理完成";
            case "ASSIGNMENT_CANCELLED" -> "取消承接";
            case "ORDER_CANCELLED_BY_AFTERSALE" -> "取消并退款";
            case "CONTACT_ACCESS" -> "查看联系方式";
            case "REVIEW_SUBMITTED" -> "提交评价";
            case "ASSIGNMENT_ACCEPTED" -> "代驾接单";
            case "DRIVER_ARRIVED_PICKUP" -> "代驾到达接车点";
            case "CAR_ARRIVED_STATION" -> "车辆到达检测站";
            case "MILEAGE_UPDATED" -> "登记里程";
            case "MANUAL_INVITE" -> "人工邀约";
            case "NO_CANDIDATE" -> "暂无可用代驾";
            case "DISPATCH_RELEASED" -> "释放派单";
            case "DISPATCH_PLAN_UPDATED" -> "派单计划变更";
            case "DISPATCH_TRIGGERED_NOW" -> "后台立即派单";
            case "ASSIGNMENT_CANCELLED_BY_DRIVER" -> "代驾取消承接";
            case "ORDER_CANCELLED" -> "订单已取消";
            case "AFTERSALE_CLOSED_NO_REFUND" -> "售后办结（无需退款）";
            case "AFTERSALE_WAIT_RETURN" -> "售后待车辆归还";
            case "REFUND_SUBMITTED" -> "发起退款";
            case "REFUND_FAILED" -> "退款失败";
            case "REFUND_RETRIED" -> "退款重试";
            case "EXCEPTION_REPORTED" -> "上报异常";
            case "EXCEPTION_RESOLVED" -> "异常处理完成";
            case "CAPACITY_UPDATED" -> "调整运力";
            case "ADJUSTMENT_APPLIED" -> "改价生效";
            case "SUBMITTED_FOR_REVIEW" -> "提交复核";
            case "REVIEW_APPROVED_AND_SENT" -> "复核通过并发出";
            case "REVIEW_REJECTED" -> "复核驳回";
            case "STATEMENT_CREATED" -> "生成对账单";
            case "STATEMENT_CONFIRMED" -> "对账单确认";
            case "STATEMENT_VOIDED" -> "对账单作废";
            case "STATEMENT_WITHDRAWN" -> "撤回对账单";
            case "ENTRIES_ADDED" -> "新增对账明细";
            case "LINE_PRICED" -> "明细核价";
            case "LINE_REMOVED" -> "移除对账明细";
            case "DIFFERENCE_SUBMITTED" -> "提交对账差异";
            case "DIFFERENCE_RESOLVED" -> "对账差异处理完成";
            case "PAYMENT_REGISTERED" -> "登记付款";
            case "RECONCILE_QUERY" -> "对账查询";
            case "RECONCILE_RESOLVED" -> "对账处理完成";
            case "FEE_LEDGER_CREATED" -> "生成费用台账";
            case "FEE_LEDGER_PENDING" -> "费用待计价";
            case "PENALTY_REVOKED_BY_APPEAL" -> "申诉成立撤销处罚";
            case "APPEAL_SUBMITTED" -> "提交申诉";
            case "PRICE_DRAFT_CREATED" -> "创建计价草稿";
            case "PRICE_SUBMITTED" -> "提交计价审核";
            case "PRICE_PUBLISHED" -> "发布计价规则";
            case "CONFIG_CHANGED" -> "修改业务配置";
            case "STATION_CREATED" -> "新增检测点";
            case "STATION_UPDATED" -> "修改检测点";
            case "COURSE_PUBLISHED" -> "发布培训课程";
            case "TRAINING_COMPLETED" -> "完成培训";
            case "EXAM_PASSED" -> "培训考核通过";
            case "QUALIFICATION_DRAFT_CREATED" -> "创建资格申请";
            case "QUALIFICATION_SUBMITTED" -> "提交资格审核";
            case "FACE_SESSION_CREATED" -> "发起人脸核验";
            case "FACE_VERIFIED" -> "人脸核验通过";
            case "VEHICLE_CREATED" -> "新增车辆";
            case "VEHICLE_UPDATED" -> "更新车辆";
            case "VEHICLE_DELETED" -> "删除车辆";
            case "VEHICLE_MAINTAINED" -> "车辆档案维护";
            case "OWNER_MAINTAINED" -> "车主档案维护";
            case "DRIVER_MAINTAINED" -> "代驾档案维护";
            case "PROFILE_UPDATED" -> "更新资料";
            case "PHONE_BOUND" -> "绑定手机号";
            case "EXPORT_REQUESTED" -> "发起数据导出";
            case "JOB_RETRIED" -> "后台任务重试";
            default -> action;
        };
    }

    // ------------------------------------------------------------------ 支付结果

    public Map<String, Object> payment(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        Row pay = Row.of(payMapper.findByOrderNo(orderNo));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        if (pay.isEmpty())
        {
            view.put("status", "NONE");
            view.put("statusLabel", "未生成支付单");
            return view;
        }
        view.put("outTradeNo", pay.str("out_trade_no"));
        view.put("status", pay.str("status"));
        view.put("statusLabel", payStatusLabel(pay.str("status")));
        view.put("totalFen", pay.lng("total_fen", 0L));
        view.put("paidFen", pay.lng("paid_fen", 0L));
        view.put("refundedFen", pay.lng("refunded_fen", 0L));
        view.put("refundReservedFen", pay.lng("refund_reserved_fen", 0L));
        view.put("paidAt", ViTime.iso(pay.time("paid_at")));
        view.put("expiresAt", ViTime.iso(pay.time("expires_at")));
        view.put("orderStatus", order.str("fulfillment_status"));
        view.put("trustedSource", "SERVER");
        view.put("hint", "支付结果以服务器记录为准，请勿以前端回调作为凭据");
        return view;
    }

    private String payStatusLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.Payment.PENDING -> "待支付";
            case Codes.Payment.SUCCESS -> "已支付";
            case Codes.Payment.CLOSED -> "已关闭";
            case Codes.Payment.UNKNOWN -> "结果未知";
            default -> status;
        };
    }

    // ------------------------------------------------------------------ 检测结果

    public Map<String, Object> result(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        Row current = Row.of(fulfillMapper.findCurrentResult(order.lng("id")));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        if (current.isEmpty())
        {
            view.put("available", false);
            view.put("hint", "检测结果尚未登记");
            return view;
        }
        JSONObject items = Json.read(current.json("items_json"));
        view.put("available", true);
        view.put("revision", current.intg("revision"));
        view.put("conclusion", current.str("conclusion"));
        view.put("conclusionLabel", "PASS".equals(current.str("conclusion")) ? "合格" : "不合格");
        view.put("items", items);
        view.put("inspectedAt", ViTime.iso(current.time("inspected_at")));
        view.put("stationName", order.str("station_name"));
        boolean corrected = !adminMapper.listCorrectionsOfOrder(order.lng("id")).isEmpty();
        view.put("hasCorrectionRequest", corrected);
        view.put("hint", "如对检测结论有异议，可在订单内发起服务争议");
        return view;
    }

    // ------------------------------------------------------------------ 争议

    @Transactional
    public Map<String, Object> createDispute(String orderNo, MiniDtos.DisputeCreateReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        if (req == null || req.description == null || req.description.isBlank())
        {
            throw ApiException.validation("请描述遇到的问题");
        }
        if (req.description.length() > 1000)
        {
            throw ApiException.validation("问题描述不能超过 1000 字");
        }
        Map<String, Object> existing = miscMapper.findLatestDispute(order.lng("id"));
        if (existing != null && "OPEN".equals(Row.of(existing).str("status")))
        {
            throw ApiException.stateConflict("本单已有处理中的争议，请等待处理结果");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", order.lng("id"));
        row.put("type", req.type == null ? "SERVICE" : req.type.toUpperCase());
        row.put("description", req.description.trim());
        row.put("fileIds", req.fileIds);
        miscMapper.insertDispute(row);
        Long disputeId = ((Number) row.get("id")).longValue();
        eventService.record("ORDER", order.lng("id"), "DISPUTE_CREATED", order.lng("id"), null,
                Map.of("disputeId", String.valueOf(disputeId)), req.description);
        eventService.publish("DISPUTE", disputeId, "DISPUTE_CREATED", Map.of("orderNo", orderNo));
        return disputeView(Row.of(miscMapper.findDispute(disputeId)));
    }

    public Map<String, Object> listDisputes(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> d : adminMapper.listDisputesOfOrder(order.lng("id")))
        {
            rows.add(disputeView(Row.of(d)));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }

    private Map<String, Object> disputeView(Row d)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("disputeId", String.valueOf(d.lng("id")));
        view.put("type", d.str("type"));
        view.put("status", d.str("status"));
        view.put("statusLabel", switch (d.str("status", ""))
        {
            case "OPEN" -> "待处理";
            case "PROCESSING" -> "处理中";
            case "RESOLVED" -> "已处理";
            default -> d.str("status");
        });
        view.put("description", d.str("description"));
        view.put("fileIds", d.json("file_ids"));
        view.put("conclusion", d.str("conclusion"));
        view.put("resolvedAt", ViTime.iso(d.time("resolved_at")));
        view.put("createdAt", ViTime.iso(d.time("created_at")));
        return view;
    }

    // ------------------------------------------------------------------ 评价

    @Transactional
    public Map<String, Object> submitReview(String orderNo, MiniDtos.ReviewCreateReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.WAIT_CONFIRM.equals(status) && !Codes.Fulfill.COMPLETED.equals(status))
        {
            throw ApiException.stateConflict("车辆归还后才能评价");
        }
        if (req == null || req.score == null || req.score < 1 || req.score > 5)
        {
            throw ApiException.validation("评分必须在 1-5 之间");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", order.lng("id"));
        row.put("accountId", p.accountId);
        row.put("score", req.score);
        row.put("tags", req.tags);
        row.put("content", req.content == null ? null
                : req.content.substring(0, Math.min(req.content.length(), 500)));
        miscMapper.insertReview(row);
        eventService.record("ORDER", order.lng("id"), "REVIEW_SUBMITTED", order.lng("id"), null,
                Map.of("score", String.valueOf(req.score)), null);
        Row saved = Row.of(miscMapper.findReview(order.lng("id")));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("score", saved.intg("score", req.score));
        view.put("tags", saved.str("tags"));
        view.put("content", saved.str("content"));
        view.put("createdAt", ViTime.iso(saved.time("created_at")));
        return view;
    }

    /** 关联当前订单的代驾，供车主在评价页展示 */
    public Map<String, Object> serviceSnapshot(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = bookingService.requireOwnerOrder(orderNo, p.accountId);
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(order.lng("id")));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        if (!assignment.isEmpty())
        {
            Row driver = Row.of(accountMapper.findDriverById(assignment.lng("driver_id")));
            view.put("driverName", driver.str("nickname"));
            view.put("acceptedAt", ViTime.iso(assignment.time("accepted_at")));
        }
        Row review = Row.of(miscMapper.findReview(order.lng("id")));
        view.put("reviewed", !review.isEmpty() && Objects.nonNull(review.get("score")));
        return view;
    }
}
