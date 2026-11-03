package com.xinyi.rongli.ledger.service;

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
 * 管理端 ledger 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class LedgerAdminService
{
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private PriceMapper priceMapper;
    @Autowired
    private LedgerMapper ledgerMapper;
    @Autowired
    private EventService eventService;
    @Autowired
    private LedgerService ledgerService;

    public Map<String, Object> prices(String status, String vehicleTypeCode, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> r : priceMapper.adminList(status, vehicleTypeCode, offset, limit))
        {
            rows.add(priceView(Row.of(r)));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", priceMapper.adminCount(status, vehicleTypeCode));
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> createPrice(Map<String, Object> req, Long userId)
    {
        if (req == null)
        {
            throw ApiException.validation("缺少计价配置");
        }
        String vehicleType = str(req.get("vehicleTypeCode"));
        String inspectionType = str(req.get("inspectionTypeCode"));
        if (vehicleType == null || inspectionType == null)
        {
            throw ApiException.validation("请选择车型与检测类型");
        }
        Object configObj = req.get("config");
        if (configObj == null)
        {
            throw ApiException.validation("请填写计价配置");
        }
        validatePriceConfig(configObj);
        String ruleKey = vehicleType + ":" + inspectionType;
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ruleKey", ruleKey);
        row.put("revision", priceMapper.maxRevision(ruleKey) + 1);
        row.put("vehicleTypeCode", vehicleType);
        row.put("inspectionTypeCode", inspectionType);
        row.put("status", "DRAFT");
        row.put("configJson", configObj instanceof String s ? s : JSON.toJSONString(configObj));
        row.put("effectiveFrom", req.get("effectiveFrom") == null ? null
                : ViTime.parseFlexible(String.valueOf(req.get("effectiveFrom"))));
        row.put("submittedBy", userId);
        priceMapper.insertRule(row);
        Long id = ((Number) row.get("id")).longValue();
        eventService.record("PRICE", id, "PRICE_DRAFT_CREATED", null, null,
                Map.of("ruleKey", ruleKey, "revision", String.valueOf(row.get("revision"))), null);
        return priceView(Row.of(priceMapper.findRule(id)));
    }
    @Transactional
    public Map<String, Object> updatePrice(Long ruleId, Map<String, Object> req)
    {
        Row rule = Row.of(priceMapper.findRule(ruleId));
        if (rule.isEmpty())
        {
            throw ApiException.notFound("计价规则不存在");
        }
        if (!"DRAFT".equals(rule.str("status")))
        {
            throw ApiException.stateConflict("只有草稿可以修改，已提交审核的版本请新建草稿");
        }
        Object configObj = req == null ? null : req.get("config");
        if (configObj == null)
        {
            throw ApiException.validation("请填写计价配置");
        }
        validatePriceConfig(configObj);
        Object effective = req.get("effectiveFrom");
        priceMapper.updateDraft(ruleId, configObj instanceof String s ? s : JSON.toJSONString(configObj),
                effective == null ? rule.time("effective_from") : ViTime.parseFlexible(String.valueOf(effective)));
        return priceView(Row.of(priceMapper.findRule(ruleId)));
    }
    @Transactional
    public Map<String, Object> submitPrice(Long ruleId, Long userId)
    {
        Row rule = Row.of(priceMapper.findRule(ruleId));
        if (rule.isEmpty())
        {
            throw ApiException.notFound("计价规则不存在");
        }
        if (priceMapper.changeRuleStatus(ruleId, "DRAFT", "PENDING_REVIEW", userId) == 0)
        {
            throw ApiException.stateConflict("只有草稿可以提交审核");
        }
        eventService.record("PRICE", ruleId, "PRICE_SUBMITTED", null, null, Map.of("status", "PENDING_REVIEW"), null);
        return priceView(Row.of(priceMapper.findRule(ruleId)));
    }
    @Transactional
    public Map<String, Object> auditPrice(Long ruleId, boolean approve, String opinion, Long userId)
    {
        Row rule = Row.of(priceMapper.findRule(ruleId));
        if (rule.isEmpty())
        {
            throw ApiException.notFound("计价规则不存在");
        }
        if (!"PENDING_REVIEW".equals(rule.str("status")))
        {
            throw ApiException.stateConflict("该版本不在待审核状态");
        }
        if (Objects.equals(rule.lng("submitted_by"), userId))
        {
            throw ApiException.forbidden("审核人不能是提交人");
        }
        if (!approve && (opinion == null || opinion.isBlank()))
        {
            throw ApiException.validation("驳回必须写明理由");
        }
        String to = approve ? "APPROVED" : "DRAFT";
        if (priceMapper.changeRuleStatus(ruleId, "PENDING_REVIEW", to, userId) == 0)
        {
            throw ApiException.stateConflict("规则状态已变化，请刷新");
        }
        eventService.record("PRICE", ruleId, approve ? "PRICE_APPROVED" : "PRICE_REJECTED", null, null,
                Map.of("status", to), opinion);
        return priceView(Row.of(priceMapper.findRule(ruleId)));
    }
    @Transactional
    public Map<String, Object> publishPrice(Long ruleId, Long userId)
    {
        Row rule = Row.of(priceMapper.findRule(ruleId));
        if (rule.isEmpty())
        {
            throw ApiException.notFound("计价规则不存在");
        }
        if (Objects.equals(rule.lng("submitted_by"), userId))
        {
            throw ApiException.forbidden("发布人不能是提交人");
        }
        if (priceMapper.changeRuleStatus(ruleId, "APPROVED", "PUBLISHED", userId) == 0)
        {
            throw ApiException.stateConflict("只有审核通过的版本可以发布");
        }
        priceMapper.disableOtherPublished(rule.str("rule_key"), ruleId);
        eventService.record("PRICE", ruleId, "PRICE_PUBLISHED", null, null,
                Map.of("ruleKey", rule.str("rule_key"), "revision", String.valueOf(rule.intg("revision"))), null);
        return priceView(Row.of(priceMapper.findRule(ruleId)));
    }
    public Map<String, Object> reconciliation(String status, int pageNum, int pageSize)
    {
        return reconciliation(status, null, pageNum, pageSize);
    }
    public Map<String, Object> reconciliation(String status, String keyword, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> g : adminMapper.listGovernance("RECONCILE", status, keyword, offset, limit))
        {
            rows.add(governanceView(Row.of(g), true));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countGovernance("RECONCILE", status, keyword));
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> queryReconciliation(Long caseId)
    {
        Row g = Row.of(adminMapper.findGovernance(caseId));
        if (g.isEmpty() || !"RECONCILE".equals(g.str("case_type")))
        {
            throw ApiException.notFound("对账异常不存在");
        }
        adminMapper.updateGovernance(caseId, "CHECKING", null, null, null, null);
        eventService.record("RECONCILE", caseId, "RECONCILE_QUERY", g.lng("order_id"), null, null, "重查渠道状态");
        Map<String, Object> view = governanceView(Row.of(adminMapper.findGovernance(caseId)), true);
        view.put("hint", "已发起渠道重查，请以渠道流水为准登记结论");
        return view;
    }
    @Transactional
    public Map<String, Object> resolveReconciliation(Long caseId, String conclusion, Long userId)
    {
        Row g = Row.of(adminMapper.findGovernance(caseId));
        if (g.isEmpty() || !"RECONCILE".equals(g.str("case_type")))
        {
            throw ApiException.notFound("对账异常不存在");
        }
        if (conclusion == null || conclusion.isBlank())
        {
            throw ApiException.validation("请填写对账结论与依据");
        }
        adminMapper.concludeGovernance(caseId, "RESOLVED", userId, conclusion);
        eventService.record("RECONCILE", caseId, "RECONCILE_RESOLVED", g.lng("order_id"), null,
                Map.of("status", "RESOLVED"), conclusion);
        return governanceView(Row.of(adminMapper.findGovernance(caseId)), true);
    }
    public Map<String, Object> driverReconciliationOverview(Long driverId)
    {
        return ledgerService.reconciliationOverview(driverId);
    }
    public Map<String, Object> driverReconciliationEntries(Long driverId, String from, String to, String keyword,
            boolean onlySelectable)
    {
        List<Map<String, Object>> rows = ledgerService.entries(driverId, from, to, keyword, onlySelectable);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> driverFeeLedger(Long driverId, String status)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        String filter = status == null || status.isBlank() ? null : status.toUpperCase();
        for (Map<String, Object> r : ledgerMapper.listEntries(driverId, null, null, null, false))
        {
            Row e = Row.of(r);
            if (filter != null && !filter.equals(e.str("status")) && !filter.equals(e.str("entry_type")))
            {
                continue;
            }
            rows.add(r);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> statements(Long driverId, String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> s : ledgerMapper.listStatements(driverId, status, offset, limit))
        {
            Row st = Row.of(s);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("statementId", String.valueOf(st.lng("id")));
            item.put("statementNo", st.str("statement_no"));
            item.put("driverId", String.valueOf(st.lng("driver_id")));
            item.put("driverName", st.str("driver_name"));
            item.put("status", st.str("status"));
            item.put("revision", st.intg("revision"));
            item.put("amountFen", st.lng("amount_fen", 0L));
            item.put("unpricedCount", st.intg("unpriced_count", 0));
            item.put("manualPriceCount", st.intg("manual_price_count", 0));
            item.put("activeEntryCount", st.intg("active_entry_count", 0));
            item.put("periodStart", ViTime.iso(st.time("service_range_start")));
            item.put("periodEnd", ViTime.iso(st.time("service_range_end_exclusive")));
            item.put("createdAt", ViTime.iso(st.time("created_at")));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", ledgerMapper.countStatements(driverId, status));
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> statementRevision(Long statementId, int revision)
    {
        Map<String, Object> snapshot = ledgerMapper.findSnapshot(statementId, revision);
        if (snapshot == null)
        {
            throw ApiException.notFound("该版本快照不存在");
        }
        Row s = Row.of(snapshot);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("statementId", String.valueOf(statementId));
        view.put("revision", s.intg("revision"));
        view.put("amountFen", s.lng("amount_fen", 0L));
        view.put("publicHash", s.str("public_hash"));
        view.put("snapshot", Json.read(s.json("snapshot_json")));
        view.put("createdAt", ViTime.iso(s.time("created_at")));
        return view;
    }
    public Map<String, Object> paymentRecords(String status, Long statementId, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = ledgerMapper.listPayments(status, statementId, offset, limit);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", rows);
        view.put("total", ledgerMapper.countPayments(status, statementId));
        return view;
    }
    public Map<String, Object> paymentRecordDetail(Long recordId)
    {
        Map<String, Object> row = ledgerMapper.findExternalPayment(recordId);
        if (row == null)
        {
            throw ApiException.notFound("付款凭证不存在");
        }
        Row p = Row.of(row);
        Map<String, Object> view = new LinkedHashMap<>(p.map());
        view.put("statementId", String.valueOf(p.lng("statement_id")));
        view.put("amountFen", p.lng("amount_fen", 0L));
        view.put("verified", p.time("verified_at") != null);
        view.put("hint", "登记人与核实人必须不同人，核实前不计入已付");
        return view;
    }
    public Map<String, Object> feeAdjustments(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", ledgerMapper.listAdjustments(status, offset, limit));
        view.put("total", ledgerMapper.countAdjustments(status));
        return view;
    }
    public Map<String, Object> statementDifferences(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", ledgerMapper.listDifferencesAdmin(status, offset, limit));
        return view;
    }
    @Transactional
    public Map<String, Object> resolveDifference(Long differenceId, String resolution, boolean resend, Long userId)
    {
        Row d = Row.of(ledgerMapper.findDifference(differenceId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("差异记录不存在");
        }
        if (!"OPEN".equals(d.str("status")))
        {
            throw ApiException.stateConflict("该差异已处理");
        }
        if (resolution == null || resolution.isBlank())
        {
            throw ApiException.validation("请填写差异处理结论");
        }
        if (ledgerMapper.resolveDifference(differenceId, resolution, userId) == 0)
        {
            throw ApiException.stateConflict("差异状态已变化，请刷新");
        }
        eventService.record("STATEMENT", d.lng("statement_id"), "DIFFERENCE_RESOLVED", null, null,
                Map.of("differenceId", String.valueOf(differenceId)), resolution);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("differenceId", String.valueOf(differenceId));
        view.put("status", "RESOLVED");
        if (resend)
        {
            view.put("resent", ledgerService.sendStatement(d.lng("statement_id")));
        }
        return view;
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
    private Map<String, Object> priceView(Row r)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("ruleId", String.valueOf(r.lng("id")));
        item.put("ruleKey", r.str("rule_key"));
        item.put("revision", r.intg("revision"));
        item.put("vehicleTypeCode", r.str("vehicle_type_code"));
        item.put("vehicleTypeName", r.str("vehicle_type_name"));
        item.put("inspectionTypeCode", r.str("inspection_type_code"));
        item.put("status", r.str("status"));
        item.put("statusLabel", switch (r.str("status", ""))
        {
            case "DRAFT" -> "草稿";
            case "PENDING_REVIEW" -> "待审核";
            case "APPROVED" -> "已审核";
            case "PUBLISHED" -> "已发布";
            case "DISABLED" -> "已停用";
            default -> r.str("status");
        });
        item.put("config", Json.read(r.json("config_json")));
        item.put("effectiveFrom", ViTime.iso(r.time("effective_from")));
        item.put("createdAt", ViTime.iso(r.time("created_at")));
        return item;
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
    private String str(Object v)
    {
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).trim();
    }
    private void validatePriceConfig(Object configObj)
    {
        JSONObject json;
        try
        {
            json = configObj instanceof JSONObject o ? o : JSON.parseObject(JSON.toJSONString(configObj));
        }
        catch (Exception e)
        {
            throw ApiException.validation("计价配置必须是 JSON 对象");
        }
        Object base = json.get("baseFen");
        if (base == null)
        {
            throw ApiException.validation("计价配置必须包含 baseFen（起步价，单位分）");
        }
        long baseFen;
        try
        {
            baseFen = Long.parseLong(String.valueOf(base));
        }
        catch (NumberFormatException e)
        {
            throw ApiException.validation("baseFen 必须是整数分");
        }
        if (baseFen < 0)
        {
            throw ApiException.validation("金额不能为负");
        }
    }
}
