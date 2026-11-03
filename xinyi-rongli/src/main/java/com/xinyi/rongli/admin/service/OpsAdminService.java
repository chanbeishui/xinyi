package com.xinyi.rongli.admin.service;

import com.xinyi.rongli.common.service.EventService;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.station.service.StationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.auth.mapper.AccountMapper;
import com.xinyi.rongli.admin.mapper.AdminMapper;
import com.xinyi.rongli.aftersale.mapper.AfterSaleMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.ledger.mapper.LedgerMapper;
import com.xinyi.rongli.common.mapper.MiscMapper;
import com.xinyi.rongli.order.mapper.OrderMapper;
import com.xinyi.rongli.order.mapper.PriceMapper;
import com.xinyi.rongli.station.mapper.StationMapper;
import com.xinyi.rongli.vehicle.mapper.VehicleMapper;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.BizConfig;
import com.xinyi.rongli.common.support.DeptScope;
import com.xinyi.rongli.common.support.Codes;
import com.xinyi.rongli.common.support.Crypto;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.ViTime;

/**
 * 管理端 admin 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class OpsAdminService
{
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private EventService eventService;
    @Autowired
    private BizConfig config;
    @Autowired
    private Crypto crypto;
    @Autowired
    private DeptScope deptScope;
    @Autowired
    private com.xinyi.system.service.ISysConfigService sysConfigService;

    public Map<String, Object> dashboard()
    {
        List<Long> deptIds = deptScope.visibleDeptIds();
        Row base = Row.of(orderMapper.dashboard(deptIds));
        Row extra = Row.of(adminMapper.statisticsOverview(ViTime.shanghaiDate(ViTime.now()).toString(), deptIds));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("activeOrders", base.lng("active_orders", 0L));
        view.put("ordersToday", extra.lng("orders_today", 0L));
        view.put("dispatching", base.lng("dispatching", 0L));
        view.put("manualDispatch", extra.lng("manual_dispatch", base.lng("manual_dispatch", 0L)));
        view.put("waitConfirm", base.lng("wait_confirm", 0L));
        view.put("cancelApplied", base.lng("cancel_applied", 0L));
        view.put("aftersalePending", extra.lng("aftersale_pending", 0L));
        view.put("disputeOpen", extra.lng("dispute_open", 0L));
        view.put("exceptionOpen", extra.lng("exception_open", 0L));
        view.put("correctionPending", extra.lng("correction_pending", 0L));
        view.put("appealPending", extra.lng("appeal_pending", 0L));
        view.put("qualificationPending", extra.lng("qualification_pending", 0L));
        view.putAll(todoHints(view));
        return view;
    }
    public Map<String, Object> exports(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> g : adminMapper.listGovernance("EXPORT", status, null, offset, limit))
        {
            rows.add(governanceView(Row.of(g), true));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countGovernance("EXPORT", status, null));
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> requestExport(Map<String, Object> req, Long userId)
    {
        if (req == null)
        {
            throw ApiException.validation("缺少导出条件");
        }
        JSONObject payload = new JSONObject();
        payload.put("from", str(req.get("from")));
        payload.put("to", str(req.get("to")));
        payload.put("status", str(req.get("status")));
        payload.put("stationId", str(req.get("stationId")));
        payload.put("reason", str(req.get("reason")));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("caseType", "EXPORT");
        row.put("orderId", null);
        row.put("requesterAccountId", null);
        row.put("requesterUserId", userId);
        row.put("status", "APPLIED");
        row.put("payloadCipher", crypto.encrypt(payload.toJSONString()));
        row.put("reason", str(req.get("reason")));
        row.put("expiresAt", ViTime.now().plusHours(24));
        adminMapper.insertGovernance(row);
        Long caseId = ((Number) row.get("id")).longValue();
        eventService.record("EXPORT", caseId, "EXPORT_REQUESTED", null, null,
                Map.of("from", String.valueOf(payload.get("from"))), str(req.get("reason")));
        return governanceView(Row.of(adminMapper.findGovernance(caseId)), true);
    }
    @Transactional
    public Map<String, Object> auditExport(Long caseId, boolean approve, String opinion, Long userId)
    {
        Row g = Row.of(adminMapper.findGovernance(caseId));
        if (g.isEmpty() || !"EXPORT".equals(g.str("case_type")))
        {
            throw ApiException.notFound("导出任务不存在");
        }
        if (!"APPLIED".equals(g.str("status")))
        {
            throw ApiException.stateConflict("该导出任务已处理");
        }
        if (Objects.equals(g.lng("requester_user_id"), userId))
        {
            throw ApiException.forbidden("审批人不能是申请人");
        }
        if (!approve && (opinion == null || opinion.isBlank()))
        {
            throw ApiException.validation("驳回必须写明理由");
        }
        adminMapper.updateGovernance(caseId, approve ? "APPROVED" : "REJECTED", userId, opinion, null,
                approve ? ViTime.now().plusHours(24) : null);
        eventService.record("EXPORT", caseId, approve ? "EXPORT_APPROVED" : "EXPORT_REJECTED", null, null, null,
                opinion);
        return governanceView(Row.of(adminMapper.findGovernance(caseId)), true);
    }
    public Map<String, Object> exportTask(Long caseId)
    {
        Row g = Row.of(adminMapper.findGovernance(caseId));
        if (g.isEmpty() || !"EXPORT".equals(g.str("case_type")))
        {
            throw ApiException.notFound("导出任务不存在");
        }
        Map<String, Object> view = governanceView(g, true);
        if (!"APPROVED".equals(g.str("status")))
        {
            view.put("rows", new ArrayList<>());
            view.put("hint", "审批通过后才会生成数据");
            return view;
        }
        JSONObject payload = Json.read(crypto.decrypt((byte[]) g.get("payload_cipher")));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> o : orderMapper.adminList(payload.getString("status"), null,
                payload.getString("stationId") == null ? null : Long.valueOf(payload.getString("stationId")),
                null, null, null, deptScope.visibleDeptIds(), 0, 500))
        {
            Row order = Row.of(o);
            JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", order.str("order_no"));
            item.put("plateNo", maskPlate(snapshot.getString("plateNo")));
            item.put("ownerName", mask(snapshot.getString("contactName"), 1, 0));
            item.put("fulfillmentStatus", order.str("fulfillment_status"));
            item.put("pickupStartsAt", ViTime.iso(order.time("pickup_starts_at")));
            item.put("totalFen", order.lng("total_fen", 0L));
            rows.add(item);
        }
        view.put("rows", rows);
        view.put("rowCount", rows.size());
        view.put("expiresAt", ViTime.iso(g.time("expires_at")));
        view.put("maskingRule", "车牌保留前后各 2 位，联系人只保留姓氏");
        return view;
    }
    public Map<String, Object> auditEvents(Long orderId, String aggregateType, String action, int pageNum,
            int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 200);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> e : adminMapper.listEvents(orderId, aggregateType, action, offset, limit))
        {
            Row ev = Row.of(e);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("eventId", String.valueOf(ev.lng("id")));
            item.put("orderNo", ev.str("order_no"));
            item.put("aggregateType", ev.str("aggregate_type"));
            item.put("aggregateId", ev.lng("aggregate_id") == null ? null
                    : String.valueOf(ev.lng("aggregate_id")));
            item.put("action", ev.str("action"));
            item.put("actorRealm", ev.str("actor_realm"));
            item.put("actingRole", ev.str("acting_role"));
            item.put("actorAccountId", ev.lng("actor_account_id") == null ? null
                    : String.valueOf(ev.lng("actor_account_id")));
            item.put("actorUserId", ev.lng("actor_user_id"));
            item.put("beforeSummary", Json.read(ev.json("before_summary")));
            item.put("afterSummary", Json.read(ev.json("after_summary")));
            item.put("reason", ev.str("reason"));
            item.put("createdAt", ViTime.iso(ev.time("created_at")));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countEvents(orderId, aggregateType, action));
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> statistics(int days)
    {
        return statistics(days, null);
    }
    public Map<String, Object> statistics(int days, String range)
    {
        String scope = range == null || range.isBlank() ? null : range.trim().toUpperCase();
        int span = switch (scope == null ? "" : scope)
        {
            case "DAY" -> 7;
            case "WEEK" -> 30;
            case "MONTH" -> 90;
            default -> Math.min(Math.max(days, 1), 90);
        };
        java.time.LocalDateTime from = ViTime.now().minusDays(span);
        List<Long> deptIds = deptScope.visibleDeptIds();
        List<Map<String, Object>> daily = orderMapper.dailyStats(from, deptIds);
        Row overview = Row.of(adminMapper.statisticsOverview(ViTime.shanghaiDate(ViTime.now()).toString(), deptIds));
        Row dash = Row.of(orderMapper.dashboard(deptIds));
        Row orderKpi = Row.of(adminMapper.statisticsOrderKpi(deptIds));
        Row payKpi = Row.of(adminMapper.statisticsPaymentKpi(deptIds));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rangeDays", span);
        view.put("range", scope == null ? "DAY" : scope);
        view.put("from", ViTime.iso(from));
        view.put("overview", overview.map());
        view.put("dashboard", dash.map());
        view.put("daily", daily);
        view.put("definitions", List.of(
                "下单口径：按订单创建时间归属自然日（Asia/Shanghai）",
                "完成口径：以实际归还交接时间为准，非支付时间",
                "取消口径：含取消已办结与未支付关闭",
                "金额口径：一律为整数分，均为订单应收金额，不含代驾费用"));

        // ---- 统计卡：与设计稿四张卡顺序一致
        long sampleOrders = orderKpi.lng("sample_orders", 0L);
        long returnedAssignments = orderKpi.lng("returned_assignments", 0L);
        long pendingPayOrders = orderKpi.lng("pending_pay_orders", 0L);
        long paidFen = payKpi.lng("paid_fen", 0L);
        long refundedFen = payKpi.lng("refunded_fen", 0L);
        long paidOrders = payKpi.lng("paid_orders", 0L);
        long netPaidFen = paidFen - refundedFen;
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("sampleOrders", sampleOrders);
        kpi.put("paidFen", paidFen);
        kpi.put("paidOrders", paidOrders);
        kpi.put("refundedFen", refundedFen);
        kpi.put("netPaidFen", netPaidFen);
        kpi.put("returnedAssignments", returnedAssignments);
        kpi.put("pendingPayOrders", pendingPayOrders);
        view.put("kpi", kpi);
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(statCard("sampleOrders", "样本服务订单", String.valueOf(sampleOrders), "演示数据"));
        cards.add(statCard("paidAmount", "已支付金额", yuanText(paidFen), paidOrders + "条已支付演示订单"));
        cards.add(statCard("returnedAssignments", "已归还司机任务", String.valueOf(returnedAssignments),
                "司机已释放，等待服务确认"));
        cards.add(statCard("pendingPayOrders", "待支付订单", String.valueOf(pendingPayOrders), "不计入成功收款"));
        view.put("cards", cards);

        // ---- 近7日订单量：固定 7 天，缺数据的自然日补 0
        Map<String, Long> dailyMap = new LinkedHashMap<>();
        for (Map<String, Object> d : daily)
        {
            Row row = Row.of(d);
            dailyMap.put(row.str("day"), row.lng("orders", 0L));
        }
        List<Map<String, Object>> last7 = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        for (int i = 6; i >= 0; i--)
        {
            String day = today.minusDays(i).toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", day);
            item.put("label", (today.minusDays(i).getMonthValue()) + "/" + today.minusDays(i).getDayOfMonth());
            item.put("orders", dailyMap.getOrDefault(day, 0L));
            last7.add(item);
        }
        view.put("last7", last7);
        view.put("chartNote", "按订单创建时间（Asia/Shanghai）归属自然日，用于核对趋势与筛选条件；不对应当前明细样本。");

        // ---- 指标口径：设计稿右侧清单
        List<Map<String, Object>> metrics = new ArrayList<>();
        metrics.add(metricRow("司机任务占用起点", "接受邀约"));
        metrics.add(metricRow("司机任务占用终点", "实际归还 / 合法解除"));
        metrics.add(metricRow("上门准时", "与约定上门区间比较"));
        metrics.add(metricRow("净收款", "成功收款减成功退款"));
        metrics.add(metricRow("取消返还", "单独观察车辆是否交还"));
        view.put("metricDefinitions", metrics);

        // ---- 按站点服务概况
        List<Map<String, Object>> stationStats = new ArrayList<>();
        for (Map<String, Object> s : adminMapper.statisticsByStation(deptIds))
        {
            Row row = Row.of(s);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stationId", String.valueOf(row.lng("station_id")));
            item.put("stationName", row.str("station_name"));
            item.put("orders", row.lng("orders", 0L));
            item.put("paid", row.lng("paid", 0L));
            item.put("unpaid", row.lng("unpaid", 0L));
            item.put("inHand", row.lng("in_hand", 0L));
            item.put("returned", row.lng("returned", 0L));
            stationStats.add(item);
        }
        view.put("stationStats", stationStats);
        view.put("stationTotal", stationStats.size());
        return view;
    }
    public Map<String, Object> jobs(int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> o : adminMapper.listOutbox(offset, limit))
        {
            Row job = Row.of(o);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskId", String.valueOf(job.lng("id")));
            item.put("eventKey", job.str("event_key"));
            item.put("eventType", job.str("event_type"));
            item.put("aggregateType", job.str("aggregate_type"));
            item.put("status", job.str("status"));
            item.put("attempts", job.intg("attempts", 0));
            item.put("nextAttemptAt", ViTime.iso(job.time("next_attempt_at")));
            item.put("createdAt", ViTime.iso(job.time("created_at")));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countOutbox());
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> retryJob(Long taskId)
    {
        Row job = Row.of(adminMapper.findOutbox(taskId));
        if (job.isEmpty())
        {
            throw ApiException.notFound("补偿任务不存在");
        }
        if ("DONE".equals(job.str("status")))
        {
            throw ApiException.stateConflict("该任务已完成，无需重试");
        }
        adminMapper.retryOutbox(taskId);
        eventService.record("JOB", taskId, "JOB_RETRIED", null, null,
                Map.of("status", "READY"), "人工受控重试");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("taskId", String.valueOf(taskId));
        view.put("status", "READY");
        view.put("hint", "已重新入队，下一次补偿轮询会尝试投递");
        return view;
    }
    public Map<String, Object> businessConfig()
    {
        Map<String, Object> view = new LinkedHashMap<>();
        Map<String, Object> effective = new LinkedHashMap<>();
        effective.put("rongli.dispatch.leadMinutes", config.dispatchLeadMinutes());
        effective.put("rongli.dispatch.radiusMeter", config.dispatchRadiusMeter());
        effective.put("rongli.dispatch.radiusSteps",
                config.dispatchRadiusSteps().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
        effective.put("rongli.dispatch.totalMinutes", config.dispatchTotalMinutes());
        effective.put("rongli.invite.expireSeconds", config.inviteExpireSeconds());
        effective.put("rongli.pay.expireMinutes", config.payExpireMinutes());
        effective.put("rongli.confirm.autoHours", config.autoConfirmHours());
        effective.put("rongli.driver.locationFreshSeconds", config.locationFreshSeconds());
        effective.put("rongli.service.termsVersion", config.termsVersion());
        effective.put("rongli.content.intro",
                config.get("rongli.content.intro", "预约上门接车与检测结果登记服务。"));
        effective.put("rongli.content.support",
                config.get("rongli.content.support", "工作时段由运营确认后公布。"));
        effective.put("rongli.payment.channel", config.mockPayment() ? "MOCK" : "WECHAT");
        effective.put("rongli.sms.enabled", config.getBool("rongli.sms.enabled", false));
        effective.put("rongli.sms.provider", config.get("rongli.sms.provider", "ALIYUN"));
        effective.put("rongli.subscribe.enabled", config.getBool("rongli.subscribe.enabled", true));
        effective.put("rongli.handover.mutualConfirmTypes",
                String.join(",", config.handoverMutualConfirmTypes()));
        view.put("effective", effective);
        view.put("stored", adminMapper.listRongliConfigs());
        view.put("paymentChannelLocked", false);
        view.put("hint", "支付通道与商户参数在「系统设置 → 微信支付」维护；短信通道、订阅消息各有独立配置页，保存后即时生效。");
        return view;
    }
    public Map<String, Object> notificationTemplates()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, String> e : defaultTemplates().entrySet())
        {
            Row stored = Row.of(adminMapper.findConfigByKey("rongli.notify.template." + e.getKey()));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("templateId", e.getKey());
            item.put("scene", e.getKey());
            item.put("content", stored.isEmpty() ? e.getValue() : stored.str("config_value", e.getValue()));
            item.put("customized", !stored.isEmpty());
            item.put("variables", templateVars(e.getKey()));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> saveNotificationTemplate(String templateId, String content, Long userId)
    {
        if (templateId == null || !defaultTemplates().containsKey(templateId))
        {
            throw ApiException.notFound("通知模板不存在");
        }
        if (content == null || content.isBlank())
        {
            throw ApiException.validation("模板内容不能为空");
        }
        if (content.length() > 300)
        {
            throw ApiException.validation("模板内容不能超过 300 字");
        }
        String key = "rongli.notify.template." + templateId;
        Map<String, Object> existing = adminMapper.findConfigByKey(key);
        if (existing == null)
        {
            com.xinyi.system.domain.SysConfig entity = new com.xinyi.system.domain.SysConfig();
            entity.setConfigKey(key);
            entity.setConfigName(templateId);
            entity.setConfigValue(content);
            entity.setConfigType("N");
            sysConfigService.insertConfig(entity);
        }
        else
        {
            adminMapper.updateConfigValue(key, content);
        }
        eventService.record("NOTIFY_TEMPLATE", null, "TEMPLATE_UPDATED", null,
                Map.of("templateId", templateId), Map.of("length", String.valueOf(content.length())), null);
        return notificationTemplates();
    }
    private Map<String, String> defaultTemplates()
    {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("PAYMENT_SUCCEEDED", "预约已支付成功，上门时段 {pickupTime}，正在安排代驾");
        map.put("ASSIGNED", "代驾 {driverName} 已接单，将在 {pickupTime} 上门取车");
        map.put("VEHICLE_RETURNED", "车辆已归还，请在 {autoConfirmHours} 小时内确认服务");
        map.put("RESULT_REGISTERED", "检测结果已登记，请在订单内查看");
        map.put("AFTERSALE_REJECTED", "您的售后申请未通过：{opinion}");
        map.put("REFUND_SUCCEEDED", "退款 ¥{amount} 已原路退回");
        map.put("QUALIFICATION_APPROVED", "您的代驾资格已通过审核，可以开始听单");
        return map;
    }
    private Map<String, Object> governanceView(Row g, boolean admin)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("caseId", String.valueOf(g.lng("id")));
        view.put("caseType", g.str("case_type"));
        view.put("status", g.str("status"));
        view.put("statusLabel", switch (g.str("status", ""))
        {
            case "APPLIED" -> "已申请";
            case "APPROVED" -> "已批准";
            case "REJECTED" -> "已驳回";
            case "OPEN" -> "待处理";
            case "CHECKING" -> "核查中";
            case "RESOLVED" -> "已解决";
            case "PROCESSING" -> "处理中";
            case "COMPLETED" -> "已完成";
            case "EXPIRED" -> "已过期";
            default -> g.str("status");
        });
        view.put("reason", g.str("reason"));
        view.put("orderNo", g.str("order_no"));
        view.put("createdAt", ViTime.iso(g.time("created_at")));
        view.put("expiresAt", ViTime.iso(g.time("expires_at")));
        if (admin)
        {
            view.put("requesterUserId", g.lng("requester_user_id"));
            view.put("reviewerId", g.lng("reviewer_id"));
        }
        // B31 支付渠道核对所需列：业务订单、关联交易、问题类型、业务/渠道、金额
        view.put("amountFen", g.lng("total_fen", 0L));
        String refundNo = g.str("out_refund_no");
        String paymentNo = g.str("out_trade_no");
        view.put("relatedTradeNo", refundNo != null ? refundNo : paymentNo);
        view.put("paymentTradeNo", paymentNo);
        view.put("refundTradeNo", refundNo);
        view.put("problemType", g.str("reason"));
        view.put("conclusion", g.str("conclusion"));
        view.put("bizStatusLabel", reconcileBizLabel(g));
        view.put("channelStatus", reconcileChannelLabel(g));
        view.put("relatedTradeKind", refundNo != null ? "REFUND" : "PAYMENT");
        return view;
    }
    private String mask(String text, int keepLeft, int keepRight)
    {
        if (text == null || text.length() <= keepLeft + keepRight)
        {
            return text == null ? null : "***";
        }
        return text.substring(0, keepLeft) + "*".repeat(Math.max(1, text.length() - keepLeft - keepRight))
                + text.substring(text.length() - keepRight);
    }
    private String maskPlate(String plate)
    {
        if (plate == null || plate.length() < 5)
        {
            return plate;
        }
        return plate.substring(0, 2) + "***" + plate.substring(plate.length() - 2);
    }
    private Map<String, Object> metricRow(String label, String value)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("value", value);
        return row;
    }
    private long num(Object v)
    {
        if (v instanceof Number n)
        {
            return n.longValue();
        }
        return 0L;
    }
    private String reconcileBizLabel(Row g)
    {
        long reserved = g.lng("refund_reserved_fen", 0L);
        long refunded = g.lng("refunded_fen", 0L);
        String pay = g.str("payment_status");
        if (reserved > 0)
        {
            return "已预留";
        }
        if (pay != null)
        {
            return switch (pay)
            {
                case "CLOSED" -> "已关闭";
                case "SUCCESS" -> refunded > 0 ? "已退款" : "已支付";
                case "PENDING" -> "待支付";
                case "UNKNOWN" -> "渠道未知";
                default -> pay;
            };
        }
        String fs = g.str("fulfillment_status");
        return switch (fs == null ? "" : fs)
        {
            case "CLOSED" -> "已关闭";
            case "CANCELLED" -> "已取消";
            case "COMPLETED" -> "已完成";
            case "PENDING_PAY" -> "待支付";
            case "" -> "-";
            default -> "进行中";
        };
    }
    private String reconcileChannelLabel(Row g)
    {
        String refund = g.str("refund_status");
        if (refund != null)
        {
            return refund;
        }
        String pay = g.str("payment_status");
        if (pay == null)
        {
            return "待核实";
        }
        return g.str("transaction_id") == null ? "待核实" : pay;
    }
    private Map<String, Object> statCard(String key, String label, String value, String hint)
    {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("key", key);
        card.put("label", label);
        card.put("value", value);
        card.put("hint", hint);
        return card;
    }
    private String str(Object v)
    {
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).trim();
    }
    private List<String> templateVars(String scene)
    {
        return switch (scene)
        {
            case "PAYMENT_SUCCEEDED" -> List.of("{pickupTime}");
            case "ASSIGNED" -> List.of("{driverName}", "{pickupTime}");
            case "VEHICLE_RETURNED" -> List.of("{autoConfirmHours}");
            case "AFTERSALE_REJECTED" -> List.of("{opinion}");
            case "REFUND_SUCCEEDED" -> List.of("{amount}");
            default -> List.of();
        };
    }
    private Map<String, Object> todo(String type, String text, String path)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", type);
        item.put("text", text);
        item.put("path", path);
        return item;
    }
    private Map<String, Object> todoHints(Map<String, Object> m)
    {
        List<Map<String, Object>> todos = new ArrayList<>();
        long aftersale = num(m.get("aftersalePending"));
        if (aftersale > 0)
        {
            todos.add(todo("AFTERSALE", "售后待审核 " + aftersale + " 单", "/rongli/afterSale"));
        }
        long manual = num(m.get("manualDispatch"));
        if (manual > 0)
        {
            todos.add(todo("DISPATCH", "人工派单队列 " + manual + " 单", "/rongli/dispatch"));
        }
        long qual = num(m.get("qualificationPending"));
        if (qual > 0)
        {
            todos.add(todo("QUALIFICATION", "代驾资格待审核 " + qual + " 人", "/rongli/driverQualification"));
        }
        long appeal = num(m.get("appealPending"));
        if (appeal > 0)
        {
            todos.add(todo("APPEAL", "停单申诉待处理 " + appeal + " 条", "/rongli/penalty"));
        }
        long correction = num(m.get("correctionPending"));
        if (correction > 0)
        {
            todos.add(todo("CORRECTION", "检测结果更正待审核 " + correction + " 条", "/rongli/resultCorrection"));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("todos", todos);
        return view;
    }
    private String yuanText(long fen)
    {
        return "¥" + java.math.BigDecimal.valueOf(fen).movePointLeft(2)
                .setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
