package com.xinyi.rongli.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.LedgerMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.PriceMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 代驾费用与对账：费用规则、不可覆盖台账、明确勾选组单、行内核价、整单复核、付款核实与调差。
 *
 * <p>核心语义：NULL 金额表示"尚未核价"，不等于 0 元；只有本人确认的版本才可产生付款记录；
 * 确认或有付款记录后原账只读，后续差异走独立调差条目。</p>
 */
@Service
public class LedgerService
{
    private static final long DEFAULT_PAYOR = 1L;

    @Autowired
    private LedgerMapper ledgerMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PriceMapper priceMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private Crypto crypto;

    @Autowired
    private EventService eventService;

    // ------------------------------------------------------------------ 台账生成

    /** 实际归还（或取消返还）后生成费用台账，未知金额保留 NULL 待核价 */
    @Transactional
    public void recordReturned(Long orderId, Long assignmentId, Long driverId, boolean cancelReturn)
    {
        String sourceKey = (cancelReturn ? "CANCEL_RETURN:" : "NORMAL_RETURN:") + assignmentId;
        if (ledgerMapper.findLedgerBySource(DEFAULT_PAYOR, driverId, sourceKey) != null)
        {
            return;
        }
        Row order = Row.of(orderMapper.findById(orderId));
        String scenario = cancelReturn ? "CANCEL_RETURN" : "NORMAL_RETURN";
        Map<String, Object> rule = ledgerMapper.findPublishedFeeRule(DEFAULT_PAYOR, scenario);
        Long amountFen = null;
        String breakdown = null;
        Long ruleId = null;
        String ruleSnapshot = null;
        if (rule != null)
        {
            Row r = Row.of(rule);
            ruleId = r.lng("id");
            JSONObject pricing = Json.read(r.json("pricing_json"));
            amountFen = calculateFee(pricing, order);
            JSONObject bd = new JSONObject();
            bd.put("baseFen", pricing.getLongValue("baseFen"));
            bd.put("amountFen", amountFen);
            bd.put("scenario", scenario);
            breakdown = bd.toJSONString();
            ruleSnapshot = r.json("rule_snapshot") != null ? r.json("rule_snapshot")
                    : r.json("pricing_json");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("driverId", driverId);
        row.put("payorEntityId", DEFAULT_PAYOR);
        row.put("assignmentId", assignmentId);
        row.put("orderId", orderId);
        row.put("sourceEventKey", sourceKey);
        row.put("entryKind", cancelReturn ? Codes.Ledger.CANCEL_RETURN : Codes.Ledger.NORMAL_RETURN);
        row.put("serviceEndedAt", ViTime.now());
        row.put("pricingStatus", amountFen == null ? Codes.Ledger.PENDING_PRICING : Codes.Ledger.READY);
        row.put("feeRuleId", ruleId);
        row.put("ruleSnapshot", ruleSnapshot);
        row.put("amountFen", amountFen);
        row.put("feeBreakdown", breakdown);
        row.put("sourceEntryId", null);
        ledgerMapper.insertLedger(row);
        eventService.publish("LEDGER", ((Number) row.get("id")).longValue(), "FEE_LEDGER_CREATED",
                Map.of("driverId", String.valueOf(driverId), "orderId", String.valueOf(orderId)));
    }

    /** 接车前解除承接：只记录已发生的服务事实，不产生客户退款 */
    @Transactional
    public void recordCancelledBeforePickup(Long orderId, Long assignmentId, Long driverId, String reason)
    {
        String sourceKey = "CANCEL_BEFORE_PICKUP:" + assignmentId;
        if (ledgerMapper.findLedgerBySource(DEFAULT_PAYOR, driverId, sourceKey) != null)
        {
            return;
        }
        Map<String, Object> rule = ledgerMapper.findPublishedFeeRule(DEFAULT_PAYOR, "CANCEL_RETURN");
        Row order = Row.of(orderMapper.findById(orderId));
        Long amountFen = null;
        String breakdown = null;
        Long ruleId = null;
        if (rule != null)
        {
            ruleId = Row.of(rule).lng("id");
            JSONObject pricing = Json.read(Row.of(rule).json("pricing_json"));
            amountFen = calculateFee(pricing, order);
            JSONObject bd = new JSONObject();
            bd.put("baseFen", pricing.getLongValue("baseFen"));
            bd.put("amountFen", amountFen);
            bd.put("scenario", "CANCEL_BEFORE_PICKUP");
            breakdown = bd.toJSONString();
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("driverId", driverId);
        row.put("payorEntityId", DEFAULT_PAYOR);
        row.put("assignmentId", assignmentId);
        row.put("orderId", orderId);
        row.put("sourceEventKey", sourceKey);
        row.put("entryKind", Codes.Ledger.CANCEL_BEFORE_PICKUP);
        row.put("serviceEndedAt", ViTime.now());
        row.put("pricingStatus", Codes.Ledger.PENDING_PRICING);
        row.put("feeRuleId", ruleId);
        row.put("ruleSnapshot", null);
        row.put("amountFen", null);
        row.put("feeBreakdown", breakdown);
        row.put("sourceEntryId", null);
        ledgerMapper.insertLedger(row);
        eventService.record("LEDGER", ((Number) row.get("id")).longValue(), "FEE_LEDGER_PENDING", orderId, null, null, reason);
    }

    private long calculateFee(JSONObject pricing, Row order)
    {
        long base = pricing.getLongValue("baseFen");
        long included = pricing.getLongValue("includedMeters");
        long unitMeters = Math.max(1, pricing.getLongValue("unitMeters"));
        long unitPrice = pricing.getLongValue("unitPriceFen");
        int distance = 0;
        if (order.lng("quote_id") != null)
        {
            Map<String, Object> quote = priceMapper.findQuote(order.lng("quote_id"));
            if (quote != null)
            {
                JSONObject route = Json.read(Row.of(quote).json("route_snapshot"));
                distance = route.getIntValue("distanceMeters");
            }
        }
        long extra = Math.max(0, distance - included);
        long units = (extra + unitMeters - 1) / unitMeters;
        return base + units * unitPrice;
    }

    // ------------------------------------------------------------------ 个人对账视图

    public Map<String, Object> reconciliationOverview(Long driverId)
    {
        Row driver = Row.of(accountMapper.findDriverById(driverId));
        if (driver.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        Map<String, Object> metric = ledgerMapper.overview(driverId);
        Row m = Row.of(metric);
        long confirmedFen = m.lng("confirmed_fen", 0);
        long verifiedFen = 0;
        long pendingEvidenceFen = 0;
        for (Map<String, Object> s : ledgerMapper.listStatements(driverId, Codes.Statement.CONFIRMED, 0, 200))
        {
            Long statementId = Row.of(s).lng("id");
            verifiedFen += ledgerMapper.sumVerifiedPayment(statementId);
            pendingEvidenceFen += ledgerMapper.sumPendingPayment(statementId);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("driverId", String.valueOf(driverId));
        view.put("driverName", driver.str("driver_name", driver.str("nickname")));
        view.put("payorEntityId", String.valueOf(DEFAULT_PAYOR));
        view.put("pendingEntryCount", m.lng("pending_count", 0));
        view.put("unpricedCount", m.lng("unpriced_count", 0));
        view.put("confirmedUnpaidFen", Math.max(0, confirmedFen - verifiedFen));
        view.put("pendingEvidenceFen", pendingEvidenceFen);
        view.put("statementCount", m.lng("statement_count", 0));
        view.put("confirmedFen", confirmedFen);
        view.put("verifiedPaidFen", verifiedFen);
        return view;
    }

    public List<Map<String, Object>> entries(Long driverId, String from, String to, String keyword, boolean onlySelectable)
    {
        LocalDateTime fromTime = from == null || from.isBlank() ? null : ViTime.parseFlexible(from);
        LocalDateTime toTime = to == null || to.isBlank() ? null : ViTime.parseFlexible(to).plusDays(1);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : ledgerMapper.listEntries(driverId, fromTime, toTime, keyword, onlySelectable))
        {
            Row e = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("entryId", String.valueOf(e.lng("id")));
            item.put("assignmentId", String.valueOf(e.lng("assignment_id")));
            item.put("orderNo", e.str("order_no"));
            item.put("plateNo", e.str("plate_no"));
            item.put("serviceEndedAt", ViTime.iso(e.time("service_ended_at")));
            item.put("entryKind", e.str("entry_kind"));
            item.put("entryKindLabel", entryKindLabel(e.str("entry_kind")));
            item.put("pricingStatus", e.str("pricing_status"));
            item.put("amountFen", e.lng("amount_fen"));
            item.put("amountYuan", e.lng("amount_fen") == null ? null
                    : BigDecimal.valueOf(e.lng("amount_fen")).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
            item.put("statementId", e.lng("statement_id") == null ? null : String.valueOf(e.lng("statement_id")));
            item.put("statementNo", e.str("statement_no"));
            item.put("statementStatus", e.str("statement_status"));
            item.put("selectable", e.lng("statement_id") == null && e.lng("included_statement_id") == null);
            item.put("sourceVersion", e.lng("version"));
            out.add(item);
        }
        return out;
    }

    private String entryKindLabel(String kind)
    {
        if (kind == null)
        {
            return "";
        }
        return switch (kind)
        {
            case Codes.Ledger.NORMAL_RETURN -> "正常归还";
            case Codes.Ledger.CANCEL_RETURN -> "取消返还";
            case Codes.Ledger.CANCEL_BEFORE_PICKUP -> "接车前解除";
            case Codes.Ledger.INITIAL_VALUATION -> "初次核价";
            case Codes.Ledger.PRICE_DELTA -> "核价差额";
            case Codes.Ledger.ADJUSTMENT -> "费用调整";
            default -> kind;
        };
    }

    // ------------------------------------------------------------------ 组单

    public Map<String, Object> previewStatement(Long driverId, List<Long> entryIds)
    {
        validateDriver(driverId);
        if (entryIds == null || entryIds.isEmpty())
        {
            throw ApiException.validation("请至少选择一条未结清服务");
        }
        List<Map<String, Object>> lines = new ArrayList<>();
        long knownFen = 0;
        int unpriced = 0;
        for (Long entryId : entryIds)
        {
            Row entry = Row.of(ledgerMapper.findLedger(entryId));
            if (entry.isEmpty() || !Objects.equals(entry.lng("driver_id"), driverId))
            {
                throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "存在不属于该代驾的费用条目");
            }
            if (ledgerMapper.findActiveAllocation(entryId) != null)
            {
                throw ApiException.stateConflict("服务已被其他对账单占用，请刷新后重试");
            }
            Row order = Row.of(orderMapper.findById(entry.lng("order_id")));
            JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("entryId", String.valueOf(entryId));
            line.put("assignmentId", String.valueOf(entry.lng("assignment_id")));
            line.put("orderNo", order.str("order_no"));
            line.put("plateNo", snapshot.getString("plateNo"));
            line.put("serviceEndedAt", ViTime.iso(entry.time("service_ended_at")));
            line.put("entryKindLabel", entryKindLabel(entry.str("entry_kind")));
            line.put("amountFen", entry.lng("amount_fen"));
            line.put("sourceVersion", entry.lng("version"));
            lines.add(line);
            if (entry.lng("amount_fen") == null)
            {
                unpriced++;
            }
            else
            {
                knownFen += entry.lng("amount_fen");
            }
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("driverId", String.valueOf(driverId));
        view.put("count", lines.size());
        view.put("knownFen", knownFen);
        view.put("unpricedCount", unpriced);
        view.put("lines", lines);
        return view;
    }

    @Transactional
    public Map<String, Object> createStatement(Long driverId, List<Long> entryIds, String operator)
    {
        validateDriver(driverId);
        if (entryIds == null || entryIds.isEmpty())
        {
            throw ApiException.validation("请勾选需要组单的服务，空选择不生成账单");
        }
        Long userId = currentUserId();
        String statementNo = "DS" + ViTime.now().atZone(java.time.ZoneOffset.UTC)
                .withZoneSameInstant(ViTime.SHANGHAI).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + crypto.randomDigits(3);

        JSONObject filter = new JSONObject();
        filter.put("selectionMode", "EXPLICIT");
        filter.put("entryCount", entryIds.size());

        long known = 0;
        int unpriced = 0;
        int active = 0;
        for (Long entryId : entryIds)
        {
            Row entry = Row.of(ledgerMapper.findLedger(entryId));
            if (entry.lng("amount_fen") == null)
            {
                unpriced++;
            }
            else
            {
                known += entry.lng("amount_fen");
            }
            active++;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("statementNo", statementNo);
        row.put("driverId", driverId);
        row.put("payorEntityId", DEFAULT_PAYOR);
        row.put("amountFen", unpriced > 0 ? null : known);
        row.put("snapshotHash", Crypto.sha256(statementNo + ":" + entryIds));
        row.put("issuedBy", userId);
        row.put("knownAmountFen", known);
        row.put("unpricedCount", unpriced);
        row.put("filterSnapshot", filter.toJSONString());
        row.put("activeEntryCount", active);
        ledgerMapper.insertStatement(row);
        Long statementId = ((Number) row.get("id")).longValue();

        int revision = 1;
        for (Long entryId : entryIds)
        {
            Row entry = Row.of(ledgerMapper.findLedger(entryId));
            if (entry.isEmpty() || !Objects.equals(entry.lng("driver_id"), driverId))
            {
                throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "存在不属于该代驾的费用条目");
            }
            if (entry.lng("included_statement_id") != null)
            {
                throw ApiException.stateConflict("该服务已在确认结转中，不能再次组单");
            }
            try
            {
                ledgerMapper.allocate(entryId, statementId);
            }
            catch (org.springframework.dao.DuplicateKeyException e)
            {
                throw ApiException.stateConflict("服务已被其他对账单占用，本次组单已回滚");
            }
            Row order = Row.of(orderMapper.findById(entry.lng("order_id")));
            JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
            JSONObject lineSnapshot = new JSONObject();
            lineSnapshot.put("orderNo", order.str("order_no"));
            lineSnapshot.put("plateNo", snapshot.getString("plateNo"));
            lineSnapshot.put("serviceEndedAt", ViTime.iso(entry.time("service_ended_at")));
            lineSnapshot.put("entryKind", entry.str("entry_kind"));
            lineSnapshot.put("referenceAmountFen", entry.lng("amount_fen"));

            Map<String, Object> lineRow = new LinkedHashMap<>();
            lineRow.put("statementId", statementId);
            lineRow.put("revision", revision);
            lineRow.put("ledgerId", entryId);
            lineRow.put("assignmentId", entry.lng("assignment_id"));
            lineRow.put("amountFen", entry.lng("amount_fen"));
            lineRow.put("snapshot", lineSnapshot.toJSONString());
            lineRow.put("lineHash", Crypto.sha256(statementId + ":" + entryId + ":" + entry.lng("amount_fen")));
            lineRow.put("referenceAmountFen", entry.lng("amount_fen"));
            lineRow.put("pricingSource", entry.lng("amount_fen") == null ? "UNPRICED" : "RULE");
            lineRow.put("sourceVersion", entry.lng("version"));
            ledgerMapper.insertLine(lineRow);
        }
        refreshStatement(statementId);
        eventService.record("STATEMENT", statementId, "STATEMENT_CREATED", null, null,
                Map.of("entries", entryIds.size(), "driverId", String.valueOf(driverId)), operator);
        return statementView(statementId);
    }

    // ------------------------------------------------------------------ 账单视图与汇总

    public Map<String, Object> statementView(Long statementId)
    {
        Row s = Row.of(ledgerMapper.findStatementDetail(statementId));
        if (s.isEmpty())
        {
            throw ApiException.notFound("对账单不存在");
        }
        List<Map<String, Object>> lines = new ArrayList<>();
        for (Map<String, Object> r : ledgerMapper.listLines(statementId))
        {
            Row line = Row.of(r);
            if (!line.bool("is_active") && !"DRAFT".equals(s.str("status")))
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("lineId", String.valueOf(line.lng("id")));
            item.put("ledgerId", String.valueOf(line.lng("ledger_id")));
            item.put("amountFen", line.lng("amount_fen"));
            item.put("amountYuan", line.lng("amount_fen") == null ? null
                    : BigDecimal.valueOf(line.lng("amount_fen")).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
            item.put("referenceAmountFen", line.lng("reference_amount_fen"));
            item.put("pricingSource", line.str("pricing_source"));
            item.put("manualChanged", line.bool("manual_changed"));
            item.put("priceReason", line.str("price_reason"));
            item.put("isActive", line.bool("is_active"));
            item.put("removeReason", line.str("remove_reason"));
            item.put("snapshot", Json.read(line.json("snapshot")));
            item.put("version", line.lng("version"));
            lines.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("statementId", String.valueOf(statementId));
        view.put("statementNo", s.str("statement_no"));
        view.put("driverId", String.valueOf(s.lng("driver_id")));
        view.put("driverName", s.str("driver_name"));
        view.put("payorEntityId", String.valueOf(s.lng("payor_entity_id")));
        view.put("status", s.str("status"));
        view.put("statusLabel", statementStatusLabel(s.str("status")));
        view.put("revision", s.intg("revision"));
        view.put("amountFen", s.lng("amount_fen"));
        view.put("amountYuan", s.lng("amount_fen") == null ? null
                : BigDecimal.valueOf(s.lng("amount_fen")).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
        view.put("knownAmountFen", s.lng("known_amount_fen"));
        view.put("unpricedCount", s.intg("unpriced_count"));
        view.put("manualPriceCount", s.intg("manual_price_count"));
        view.put("activeEntryCount", s.intg("active_entry_count"));
        view.put("reviewStatus", s.str("review_status"));
        view.put("publishedRevision", s.intg("published_revision"));
        view.put("sentAt", ViTime.iso(s.time("sent_at")));
        view.put("confirmedAt", ViTime.iso(s.time("confirmed_at")));
        view.put("confirmationRevision", s.intg("confirmation_revision"));
        view.put("version", s.lng("version"));
        view.put("lines", lines);
        long verified = ledgerMapper.sumVerifiedPayment(statementId);
        long pending = ledgerMapper.sumPendingPayment(statementId);
        long net = s.lng("amount_fen", 0L);
        view.put("verifiedPaidFen", verified);
        view.put("pendingEvidenceFen", pending);
        view.put("remainingFen", Math.max(0, net - verified));
        view.put("paymentStatus", paymentStatus(s, verified));
        view.put("allowedActions", statementActions(s));
        return view;
    }

    private String paymentStatus(Row s, long verified)
    {
        if (!Codes.Statement.CONFIRMED.equals(s.str("status")))
        {
            return "NOT_READY";
        }
        long amount = s.lng("amount_fen", 0L);
        if (amount == 0)
        {
            return "NO_PAYMENT_REQUIRED";
        }
        if (amount < 0)
        {
            return "OFFSET_REVIEW";
        }
        if (verified <= 0)
        {
            return "UNPAID";
        }
        return verified >= amount ? "PAID" : "PARTIAL";
    }

    private List<String> statementActions(Row s)
    {
        List<String> actions = new ArrayList<>();
        String status = s.str("status");
        if (Codes.Statement.DRAFT.equals(status))
        {
            actions.add("PRICE_LINE");
            actions.add("ADD_ENTRIES");
            actions.add("REMOVE_LINE");
            if (s.intg("unpriced_count") == 0 && s.intg("active_entry_count") > 0)
            {
                actions.add(s.intg("manual_price_count") > 0 ? "SUBMIT_REVIEW" : "SEND");
            }
            actions.add("VOID");
        }
        else if (Codes.Statement.INTERNAL_REVIEW.equals(status))
        {
            actions.add("REVIEW");
            actions.add("WITHDRAW");
        }
        else if (Codes.Statement.SENT.equals(status))
        {
            actions.add("WITHDRAW");
        }
        else if (Codes.Statement.DISPUTED.equals(status))
        {
            actions.add("SEND");
            actions.add("WITHDRAW");
        }
        if (Codes.Statement.CONFIRMED.equals(status))
        {
            actions.add("REGISTER_PAYMENT");
            actions.add("APPLY_ADJUSTMENT");
        }
        return actions;
    }

    private String statementStatusLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.Statement.DRAFT -> "整理中";
            case Codes.Statement.INTERNAL_REVIEW -> "内部复核中";
            case Codes.Statement.SENT -> "待代驾确认";
            case Codes.Statement.DISPUTED -> "有差异";
            case Codes.Statement.CONFIRMED -> "已确认";
            case Codes.Statement.VOID -> "已作废";
            default -> status;
        };
    }

    /** 重算金额、待核价条数与内容版本 */
    private void refreshStatement(Long statementId)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        List<Map<String, Object>> activeLines = ledgerMapper.listActiveLines(statementId);
        long known = 0;
        int unpriced = 0;
        int manual = 0;
        LocalDateTime min = null;
        LocalDateTime max = null;
        JSONArray canonical = new JSONArray();
        for (Map<String, Object> r : activeLines)
        {
            Row line = Row.of(r);
            if (line.lng("amount_fen") == null)
            {
                unpriced++;
            }
            else
            {
                known += line.lng("amount_fen");
            }
            if (line.bool("manual_changed"))
            {
                manual++;
            }
            JSONObject snapshot = Json.read(line.json("snapshot"));
            LocalDateTime ended = ViTime.parse(snapshot.getString("serviceEndedAt"));
            if (ended != null)
            {
                if (min == null || ended.isBefore(min))
                {
                    min = ended;
                }
                if (max == null || ended.isAfter(max))
                {
                    max = ended;
                }
            }
            JSONObject item = new JSONObject();
            item.put("ledgerId", line.lng("ledger_id"));
            item.put("amountFen", line.lng("amount_fen"));
            canonical.add(item);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("statementId", statementId);
        row.put("amountFen", unpriced > 0 ? null : known);
        row.put("knownAmountFen", known);
        row.put("unpricedCount", unpriced);
        row.put("manualPriceCount", manual);
        row.put("activeEntryCount", activeLines.size());
        row.put("snapshotHash", Crypto.sha256(canonical.toJSONString()));
        row.put("reviewStatus", manual > 0 ? "REQUIRED" : "NOT_REQUIRED");
        row.put("rangeStart", min);
        row.put("rangeEnd", max == null ? null : max.plusDays(1));
        ledgerMapper.refreshStatement(row);
        // 内容变化让既有复核记录失效
        if (manual > 0)
        {
            ledgerMapper.setReviewStatus(statementId, "REQUIRED");
        }
    }

    // ------------------------------------------------------------------ 行内核价

    @Transactional
    public Map<String, Object> priceLine(Long statementId, Long lineId, Long amountFen, String reason,
            String idempotencyKey)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (s.isEmpty())
        {
            throw ApiException.notFound("对账单不存在");
        }
        if (!Codes.Statement.DRAFT.equals(s.str("status")))
        {
            throw ApiException.stateConflict("只有整理中的草稿可以改价，已发送或已确认账单请申请调差");
        }
        if (amountFen == null || amountFen < 0)
        {
            throw ApiException.validation("请填写有效的应付金额（元），不接受负数");
        }
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请填写修改原因或计价依据");
        }
        Row line = Row.of(ledgerMapper.findLine(lineId));
        if (line.isEmpty() || !Objects.equals(line.lng("statement_id"), statementId) || !line.bool("is_active"))
        {
            throw ApiException.notFound("账单明细不存在");
        }
        Long old = line.lng("amount_fen");
        if (Objects.equals(old, amountFen))
        {
            return statementView(statementId);
        }
        Long userId = currentUserId();
        int revision = s.intg("revision") + 1;
        JSONObject snapshot = Json.read(line.json("snapshot"));
        snapshot.put("referenceAmountFen", line.lng("reference_amount_fen"));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("lineId", lineId);
        row.put("amountFen", amountFen);
        row.put("pricingSource", old == null ? "MANUAL_FILLED" : "MANUAL_CHANGED");
        row.put("reason", reason);
        row.put("userId", userId);
        row.put("revision", revision);
        row.put("lineHash", Crypto.sha256(statementId + ":" + lineId + ":" + amountFen));
        ledgerMapper.priceLine(row);

        Map<String, Object> change = new LinkedHashMap<>();
        change.put("statementId", statementId);
        change.put("lineId", lineId);
        change.put("ledgerId", line.lng("ledger_id"));
        change.put("revision", revision);
        change.put("oldAmountFen", old);
        change.put("newAmountFen", amountFen);
        change.put("reason", reason);
        change.put("actorUserId", userId);
        change.put("idempotencyKey", idempotencyKey == null || idempotencyKey.isBlank()
                ? UUID.randomUUID().toString() : idempotencyKey);
        ledgerMapper.insertPriceChange(change);
        // 明细金额同步回工作副本台账，保持单一事实来源
        ledgerMapper.priceLedger(line.lng("ledger_id"), amountFen, null, userId);
        refreshStatement(statementId);
        eventService.record("STATEMENT", statementId, "LINE_PRICED", null,
                Map.of("amountFen", old == null ? "NULL" : old), Map.of("amountFen", amountFen), reason);
        return statementView(statementId);
    }

    @Transactional
    public Map<String, Object> addEntries(Long statementId, List<Long> entryIds, String reason)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (!Codes.Statement.DRAFT.equals(s.str("status")))
        {
            throw ApiException.stateConflict("只有草稿可以添加服务");
        }
        if (entryIds == null || entryIds.isEmpty())
        {
            throw ApiException.validation("请选择需要添加的服务");
        }
        for (Long entryId : entryIds)
        {
            Row entry = Row.of(ledgerMapper.findLedger(entryId));
            if (entry.isEmpty() || !Objects.equals(entry.lng("driver_id"), s.lng("driver_id")))
            {
                throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "存在不属于该代驾的服务");
            }
            Map<String, Object> existing = ledgerMapper.findLine(entryId);
            try
            {
                ledgerMapper.allocate(entryId, statementId);
            }
            catch (org.springframework.dao.DuplicateKeyException e)
            {
                throw ApiException.stateConflict("服务已被其他对账单占用");
            }
            Row order = Row.of(orderMapper.findById(entry.lng("order_id")));
            JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
            JSONObject lineSnapshot = new JSONObject();
            lineSnapshot.put("orderNo", order.str("order_no"));
            lineSnapshot.put("plateNo", snapshot.getString("plateNo"));
            lineSnapshot.put("serviceEndedAt", ViTime.iso(entry.time("service_ended_at")));
            lineSnapshot.put("entryKind", entry.str("entry_kind"));
            Map<String, Object> lineRow = new LinkedHashMap<>();
            lineRow.put("statementId", statementId);
            lineRow.put("revision", s.intg("revision") + 1);
            lineRow.put("ledgerId", entryId);
            lineRow.put("assignmentId", entry.lng("assignment_id"));
            lineRow.put("amountFen", entry.lng("amount_fen"));
            lineRow.put("snapshot", lineSnapshot.toJSONString());
            lineRow.put("lineHash", Crypto.sha256(statementId + ":" + entryId));
            lineRow.put("referenceAmountFen", entry.lng("amount_fen"));
            lineRow.put("pricingSource", entry.lng("amount_fen") == null ? "UNPRICED" : "RULE");
            lineRow.put("sourceVersion", entry.lng("version"));
            ledgerMapper.insertLine(lineRow);
        }
        refreshStatement(statementId);
        eventService.record("STATEMENT", statementId, "ENTRIES_ADDED", null, null, Map.of("count", entryIds.size()), reason);
        return statementView(statementId);
    }

    @Transactional
    public Map<String, Object> removeLine(Long statementId, Long lineId, String reason)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (!Codes.Statement.DRAFT.equals(s.str("status")))
        {
            throw ApiException.stateConflict("必须先撤回或退回，才能移出服务");
        }
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请填写移出原因");
        }
        Row line = Row.of(ledgerMapper.findLine(lineId));
        if (line.isEmpty() || !Objects.equals(line.lng("statement_id"), statementId))
        {
            throw ApiException.notFound("明细不存在");
        }
        Long userId = currentUserId();
        ledgerMapper.removeLine(lineId, userId, reason);
        ledgerMapper.releaseAllocation(line.lng("ledger_id"), "从草稿移出：" + reason, userId);
        refreshStatement(statementId);
        eventService.record("STATEMENT", statementId, "LINE_REMOVED", null,
                Map.of("lineId", String.valueOf(lineId)), null, reason);
        return statementView(statementId);
    }

    @Transactional
    public Map<String, Object> voidStatement(Long statementId, String reason)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (!Codes.Statement.DRAFT.equals(s.str("status")))
        {
            throw ApiException.stateConflict("只有未发出的草稿可以作废");
        }
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请填写作废原因");
        }
        Long userId = currentUserId();
        ledgerMapper.voidStatement(statementId);
        ledgerMapper.releaseAllocationsOfStatement(statementId, "作废：" + reason, userId);
        eventService.record("STATEMENT", statementId, "STATEMENT_VOIDED", null, null, null, reason);
        return statementView(statementId);
    }

    // ------------------------------------------------------------------ 复核与发送

    @Transactional
    public Map<String, Object> submitReview(Long statementId, String reason)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (!Codes.Statement.DRAFT.equals(s.str("status")))
        {
            throw ApiException.stateConflict("只有草稿可以提交复核");
        }
        if (s.intg("unpriced_count") > 0 || s.intg("active_entry_count") == 0)
        {
            throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "仍有待填写价格的明细，无法提交复核");
        }
        Long userId = currentUserId();
        JSONArray contributors = new JSONArray();
        for (Map<String, Object> r : ledgerMapper.listActiveLines(statementId))
        {
            Row line = Row.of(r);
            if (line.bool("manual_changed") && line.lng("last_editor_user_id") != null)
            {
                contributors.add(line.lng("last_editor_user_id"));
            }
        }
        JSONObject snapshot = new JSONObject();
        snapshot.put("statementId", statementId);
        snapshot.put("revision", s.intg("revision"));
        snapshot.put("amountFen", s.lng("amount_fen"));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("statementId", statementId);
        row.put("revision", s.intg("revision"));
        row.put("snapshotHash", Crypto.sha256(snapshot.toJSONString()));
        row.put("snapshotJson", snapshot.toJSONString());
        row.put("contributors", contributors.toJSONString());
        row.put("submittedBy", userId);
        try
        {
            ledgerMapper.insertReviewRecord(row);
        }
        catch (org.springframework.dao.DuplicateKeyException e)
        {
            throw ApiException.stateConflict("该账单已有待复核记录");
        }
        ledgerMapper.casStatementStatus(statusRow(statementId, Codes.Statement.DRAFT, Codes.Statement.INTERNAL_REVIEW,
                "PENDING", null, null, s.intg("revision"), userId));
        eventService.record("STATEMENT", statementId, "SUBMITTED_FOR_REVIEW", null, null,
                Map.of("revision", s.intg("revision")), reason);
        return statementView(statementId);
    }

    @Transactional
    public Map<String, Object> reviewStatement(Long statementId, boolean approve, String opinion)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (!Codes.Statement.INTERNAL_REVIEW.equals(s.str("status")))
        {
            throw ApiException.stateConflict("该账单不在复核中");
        }
        Long userId = currentUserId();
        Row review = ledgerMapper.listReviewRecords(statementId).stream()
                .filter(r -> "PENDING".equals(Row.of(r).str("status"))).findFirst().map(Row::of).orElse(null);
        if (review == null)
        {
            throw ApiException.stateConflict("没有待处理的复核记录");
        }
        if (Objects.equals(review.lng("submitted_by"), userId))
        {
            throw ApiException.forbidden("复核人不能是本次提交人");
        }
        JSONArray contributors = com.alibaba.fastjson2.JSON.parseArray(review.str("contributor_ids", "[]"));
        boolean contributed = contributors.stream().anyMatch(c -> c != null
                && String.valueOf(userId).equals(String.valueOf(c).trim()));
        if (contributed)
        {
            throw ApiException.forbidden("复核人不能是本轮参与改价的人员");
        }
        if (!approve)
        {
            ledgerMapper.decideReviewRecord(statementId, "REJECTED", userId, opinion);
            ledgerMapper.casStatementStatus(statusRow(statementId, Codes.Statement.INTERNAL_REVIEW,
                    Codes.Statement.DRAFT, "REJECTED", null, null, s.intg("revision"), null));
            eventService.record("STATEMENT", statementId, "REVIEW_REJECTED", null, null, null, opinion);
            return statementView(statementId);
        }
        ledgerMapper.decideReviewRecord(statementId, "APPROVED", userId, opinion);
        publish(statementId, s, userId);
        eventService.record("STATEMENT", statementId, "REVIEW_APPROVED_AND_SENT", null, null,
                Map.of("revision", s.intg("revision")), opinion);
        return statementView(statementId);
    }

    /** 无人工改价时直接发送；有改价必须经过整单复核 */
    @Transactional
    public Map<String, Object> sendStatement(Long statementId)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        String status = s.str("status");
        if (!Codes.Statement.DRAFT.equals(status) && !Codes.Statement.DISPUTED.equals(status))
        {
            throw ApiException.stateConflict("当前状态不可发送");
        }
        if (s.intg("unpriced_count") > 0 || s.intg("active_entry_count") == 0)
        {
            throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "仍有待填写价格或空账单，不能发送");
        }
        if (s.intg("manual_price_count") > 0 && Codes.Statement.DRAFT.equals(status))
        {
            throw ApiException.stateConflict("本账单存在人工改价，请先提交整单复核");
        }
        publish(statementId, s, currentUserId());
        return statementView(statementId);
    }

    private void publish(Long statementId, Row s, Long userId)
    {
        int revision = s.intg("revision");
        long amount = s.lng("amount_fen", 0L);
        JSONArray publicLines = new JSONArray();
        for (Map<String, Object> r : ledgerMapper.listActiveLines(statementId))
        {
            Row line = Row.of(r);
            JSONObject snapshot = Json.read(line.json("snapshot"));
            JSONObject item = new JSONObject();
            item.put("orderNo", snapshot.getString("orderNo"));
            item.put("plateNo", snapshot.getString("plateNo"));
            item.put("serviceEndedAt", snapshot.getString("serviceEndedAt"));
            item.put("amountFen", line.lng("amount_fen"));
            publicLines.add(item);
        }
        JSONObject publicSnapshot = new JSONObject();
        publicSnapshot.put("statementNo", s.str("statement_no"));
        publicSnapshot.put("driverId", String.valueOf(s.lng("driver_id")));
        publicSnapshot.put("amountFen", amount);
        publicSnapshot.put("lines", publicLines);
        byte[] publicHash = Crypto.sha256(publicSnapshot.toJSONString());

        Map<String, Object> snapshotRow = new LinkedHashMap<>();
        snapshotRow.put("statementId", statementId);
        snapshotRow.put("revision", revision);
        snapshotRow.put("snapshotHash", Crypto.sha256(statementId + ":" + revision + ":" + amount));
        snapshotRow.put("snapshotJson", publicSnapshot.toJSONString());
        snapshotRow.put("amountFen", amount);
        snapshotRow.put("publishedBy", userId);
        snapshotRow.put("publicJson", publicSnapshot.toJSONString());
        snapshotRow.put("publicHash", publicHash);
        ledgerMapper.insertSnapshot(snapshotRow);

        Map<String, Object> row = statusRow(statementId, s.str("status"), Codes.Statement.SENT, "APPROVED",
                revision, publicHash, revision, null);
        row.put("sentAt", ViTime.now());
        if (ledgerMapper.casStatementStatus(row) == 0)
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "账单内容已变化，请刷新后重新发送");
        }
        Row driver = Row.of(accountMapper.findDriverById(s.lng("driver_id")));
        eventService.notify(driver.lng("account_id"), Codes.Role.DRIVER, null, "STATEMENT_SENT",
                "新的代驾对账单已发布，请核对后确认");
    }

    private Map<String, Object> statusRow(Long statementId, String from, String to, String reviewStatus,
            Integer publishedRevision, byte[] publishedHash, Integer revision, Long actor)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("statementId", statementId);
        row.put("from", from);
        row.put("to", to);
        row.put("reviewStatus", reviewStatus);
        row.put("actor", actor);
        row.put("publishedRevision", publishedRevision);
        row.put("publishedHash", publishedHash);
        row.put("sentAt", null);
        row.put("revision", revision);
        return row;
    }

    @Transactional
    public Map<String, Object> withdrawStatement(Long statementId, String reason)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (!List.of(Codes.Statement.SENT, Codes.Statement.DISPUTED, Codes.Statement.INTERNAL_REVIEW)
                .contains(s.str("status")))
        {
            throw ApiException.stateConflict("当前状态不可撤回");
        }
        ledgerMapper.withdrawStatement(statementId);
        ledgerMapper.decideReviewRecord(statementId, "WITHDRAWN", currentUserId(), reason);
        eventService.record("STATEMENT", statementId, "STATEMENT_WITHDRAWN", null, null, null, reason);
        return statementView(statementId);
    }

    // ------------------------------------------------------------------ 代驾确认与差异

    public Map<String, Object> driverStatements()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("尚未建立代驾档案");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : ledgerMapper.listStatements(driver.lng("id"), null, 0, 100))
        {
            Row s = Row.of(r);
            if (List.of(Codes.Statement.DRAFT, Codes.Statement.INTERNAL_REVIEW, Codes.Statement.VOID)
                    .contains(s.str("status")))
            {
                continue;
            }
            out.add(publicStatementView(s));
        }
        return Map.of("rows", out, "total", out.size());
    }

    /** 代驾查看本人账单：先校验归属，再走公开快照视图，避免越权看到他人账单 */
    public Map<String, Object> myStatementDetail(Long statementId)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("当前账号还不是代驾员");
        }
        Row statement = Row.of(ledgerMapper.findStatement(statementId));
        if (statement.isEmpty() || !Objects.equals(driver.lng("id"), statement.lng("driver_id")))
        {
            throw ApiException.notFound("对账单不存在");
        }
        return publicStatementView(statement);
    }

    public Map<String, Object> publicStatementView(Row s)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("statementId", String.valueOf(s.lng("id")));        view.put("statementNo", s.str("statement_no"));
        view.put("status", s.str("status"));
        view.put("statusLabel", statementStatusLabel(s.str("status")));
        view.put("amountFen", s.lng("amount_fen"));
        view.put("amountYuan", s.lng("amount_fen") == null ? null
                : BigDecimal.valueOf(s.lng("amount_fen")).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
        view.put("revision", s.intg("revision"));
        view.put("snapshotHash", s.str("published_hash") == null ? null : Crypto.toBase64((byte[]) s.get("published_hash")));
        view.put("sentAt", ViTime.iso(s.time("sent_at")));
        view.put("confirmedAt", ViTime.iso(s.time("confirmed_at")));
        List<Map<String, Object>> lines = new ArrayList<>();
        Map<String, Object> snapshot = ledgerMapper.findSnapshot(s.lng("id"), s.intg("revision"));
        if (snapshot != null)
        {
            JSONObject json = Json.read(Row.of(snapshot).json("public_snapshot_json"));
            JSONArray arr = json.getJSONArray("lines");
            if (arr != null)
            {
                for (int i = 0; i < arr.size(); i++)
                {
                    lines.add(arr.getJSONObject(i));
                }
            }
        }
        view.put("lines", lines);
        view.put("allowedActions", List.of("SENT".equals(s.str("status")) ? "CONFIRM" : "VIEW",
                "DISPUTE"));
        return view;
    }

    @Transactional
    public Map<String, Object> confirmStatement(Long statementId, int expectedRevision, String snapshotHash)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (s.isEmpty() || !Objects.equals(s.lng("driver_id"), driver.lng("id")))
        {
            throw ApiException.notFound("对账单不存在");
        }
        if (!Codes.Statement.SENT.equals(s.str("status")))
        {
            throw ApiException.stateConflict("该账单当前不可确认");
        }
        if (expectedRevision != s.intg("revision"))
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "账单已更新，请刷新后重新核对");
        }
        if (ledgerMapper.confirmStatement(statementId, p.accountId, expectedRevision) == 0)
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "账单状态已变化，请刷新");
        }
        // 确认成功后把本轮核价写回台账，作为最终金额并锁定，不再进入后续账单
        for (Map<String, Object> r : ledgerMapper.listActiveLines(statementId))
        {
            Row line = Row.of(r);
            ledgerMapper.markLedgerIncluded(line.lng("ledger_id"), statementId);
        }
        eventService.record("STATEMENT", statementId, "STATEMENT_CONFIRMED", null, null,
                Map.of("revision", expectedRevision), null);
        return publicStatementView(Row.of(ledgerMapper.findStatementDetail(statementId)));
    }

    @Transactional
    public Map<String, Object> submitDifference(Long statementId, String description, String entryIds)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (s.isEmpty() || !Objects.equals(s.lng("driver_id"), driver.lng("id")))
        {
            throw ApiException.notFound("对账单不存在");
        }
        if (!List.of(Codes.Statement.SENT, Codes.Statement.CONFIRMED).contains(s.str("status")))
        {
            throw ApiException.stateConflict("当前状态不可提交差异");
        }
        if (description == null || description.isBlank())
        {
            throw ApiException.validation("请说明差异内容");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("statementId", statementId);
        row.put("revision", s.intg("revision"));
        row.put("accountId", p.accountId);
        row.put("entryIds", entryIds);
        row.put("description", description);
        row.put("evidenceIds", null);
        ledgerMapper.insertDifference(row);
        ledgerMapper.markDisputed(statementId);
        eventService.record("STATEMENT", statementId, "DIFFERENCE_SUBMITTED", null, null, null, description);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("differenceId", String.valueOf(row.get("id")));
        view.put("status", "OPEN");
        return view;
    }

    // ------------------------------------------------------------------ 付款与调差

    @Transactional
    public Map<String, Object> registerPayment(Long statementId, long amountFen, String paidAt, String reference,
            String evidenceIds)
    {
        Row s = Row.of(ledgerMapper.findStatement(statementId));
        if (s.isEmpty() || !Codes.Statement.CONFIRMED.equals(s.str("status")))
        {
            throw ApiException.stateConflict("只有已确认的对账单可以登记付款凭证");
        }
        if (amountFen <= 0)
        {
            throw ApiException.validation("付款金额必须大于 0");
        }
        long remaining = s.lng("amount_fen", 0L) - ledgerMapper.sumVerifiedPayment(statementId);
        long pending = ledgerMapper.sumPendingPayment(statementId);
        if (amountFen > remaining - pending)
        {
            throw ApiException.validation("登记金额超过可登记余额（剩余应付 " + remaining + " 分，待核实 " + pending + " 分）");
        }
        if (reference == null || reference.isBlank())
        {
            throw ApiException.validation("请填写外部流水号");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("statementId", statementId);
        row.put("driverId", s.lng("driver_id"));
        row.put("payorEntityId", s.lng("payor_entity_id"));
        row.put("amountFen", amountFen);
        row.put("referenceCipher", crypto.encrypt(reference));
        row.put("referenceLookup", crypto.hmac("REF:" + reference));
        row.put("paidAt", paidAt == null ? ViTime.now() : ViTime.parseFlexible(paidAt));
        row.put("evidenceIds", evidenceIds == null ? "[]" : evidenceIds);
        row.put("registeredBy", currentUserId());
        row.put("statementRevision", s.intg("revision"));
        try
        {
            ledgerMapper.insertExternalPayment(row);
        }
        catch (org.springframework.dao.DuplicateKeyException e)
        {
            throw ApiException.stateConflict("该流水号已登记");
        }
        eventService.record("STATEMENT", statementId, "PAYMENT_REGISTERED", null, null,
                Map.of("amountFen", amountFen), "待独立核实");
        return paymentView(Row.of(ledgerMapper.findExternalPayment(((Number) row.get("id")).longValue())));
    }

    @Transactional
    public Map<String, Object> verifyPayment(Long recordId, boolean verified, String reason)
    {
        Row record = Row.of(ledgerMapper.findExternalPayment(recordId));
        if (record.isEmpty())
        {
            throw ApiException.notFound("付款凭证不存在");
        }
        if (!"PENDING".equals(record.str("status")))
        {
            throw ApiException.stateConflict("该凭证已核实");
        }
        Long userId = currentUserId();
        if (Objects.equals(record.lng("registered_by"), userId))
        {
            throw ApiException.forbidden("核实人不能是登记人");
        }
        ledgerMapper.verifyPayment(recordId, verified ? "VERIFIED" : "REJECTED", userId, reason);
        eventService.record("STATEMENT", record.lng("statement_id"), verified ? "PAYMENT_VERIFIED" : "PAYMENT_REJECTED",
                null, null, null, reason);
        return paymentView(Row.of(ledgerMapper.findExternalPayment(recordId)));
    }

    private Map<String, Object> paymentView(Row p)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("recordId", String.valueOf(p.lng("id")));
        view.put("statementId", String.valueOf(p.lng("statement_id")));
        view.put("amountFen", p.lng("amount_fen"));
        view.put("paidAt", ViTime.iso(p.time("paid_at")));
        view.put("status", p.str("status"));
        view.put("verifiedAt", ViTime.iso(p.time("verified_at")));
        view.put("reason", p.str("reason"));
        return view;
    }

    @Transactional
    public Map<String, Object> applyAdjustment(Long sourceEntryId, long amountFen, String reason, String evidenceIds)
    {
        Row entry = Row.of(ledgerMapper.findLedger(sourceEntryId));
        if (entry.isEmpty())
        {
            throw ApiException.notFound("源费用条目不存在");
        }
        if (amountFen == 0)
        {
            throw ApiException.validation("调整金额不能为 0");
        }
        if (reason == null || reason.isBlank() || evidenceIds == null || evidenceIds.isBlank())
        {
            throw ApiException.validation("调整必须填写依据并附凭证");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sourceEntryId", sourceEntryId);
        row.put("driverId", entry.lng("driver_id"));
        row.put("payorEntityId", entry.lng("payor_entity_id"));
        row.put("amountFen", amountFen);
        row.put("reason", reason);
        row.put("evidenceIds", evidenceIds);
        row.put("createdBy", currentUserId());
        ledgerMapper.insertAdjustment(row);
        eventService.record("LEDGER", sourceEntryId, "ADJUSTMENT_APPLIED", entry.lng("order_id"), null,
                Map.of("amountFen", amountFen), reason);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("adjustmentId", String.valueOf(row.get("id")));
        view.put("status", "PENDING");
        return view;
    }

    @Transactional
    public Map<String, Object> auditAdjustment(Long adjustmentId, boolean approve, String opinion)
    {
        Row adjustment = Row.of(ledgerMapper.findAdjustment(adjustmentId));
        if (adjustment.isEmpty() || !"PENDING".equals(adjustment.str("status")))
        {
            throw ApiException.stateConflict("调整申请不存在或已处理");
        }
        Long userId = currentUserId();
        if (Objects.equals(adjustment.lng("created_by"), userId))
        {
            throw ApiException.forbidden("审核人不能是申请人");
        }
        Long ledgerId = null;
        if (approve)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("driverId", adjustment.lng("driver_id"));
            row.put("payorEntityId", adjustment.lng("payor_entity_id"));
            row.put("assignmentId", adjustment.lng("source_entry_id"));
            row.put("orderId", adjustment.lng("source_entry_id"));
            row.put("sourceEventKey", "ADJUSTMENT:" + adjustmentId);
            row.put("entryKind", Codes.Ledger.ADJUSTMENT);
            row.put("serviceEndedAt", ViTime.now());
            row.put("pricingStatus", Codes.Ledger.READY);
            row.put("feeRuleId", null);
            row.put("ruleSnapshot", null);
            row.put("amountFen", adjustment.lng("amount_fen"));
            row.put("feeBreakdown", null);
            row.put("sourceEntryId", adjustment.lng("source_entry_id"));
            ledgerMapper.insertLedger(row);
            ledgerId = ((Number) row.get("id")).longValue();
        }
        ledgerMapper.auditAdjustment(adjustmentId, approve ? "APPROVED" : "REJECTED", userId, opinion, ledgerId);
        eventService.record("LEDGER", adjustment.lng("source_entry_id"), approve ? "ADJUSTMENT_APPROVED" : "ADJUSTMENT_REJECTED",
                null, null, null, opinion);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("adjustmentId", String.valueOf(adjustmentId));
        view.put("status", approve ? "APPROVED" : "REJECTED");
        view.put("generatedLedgerId", ledgerId == null ? null : String.valueOf(ledgerId));
        return view;
    }

    // ------------------------------------------------------------------ 费用规则管理

    public List<Map<String, Object>> feeRules(String status)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : ledgerMapper.listFeeRules(status, 0, 100))
        {
            Row rule = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ruleId", String.valueOf(rule.lng("id")));
            item.put("payorEntityId", String.valueOf(rule.lng("payor_entity_id")));
            item.put("ruleCode", rule.str("rule_code"));
            item.put("revision", rule.intg("revision"));
            item.put("scenario", rule.str("scenario"));
            item.put("status", rule.str("status"));
            item.put("pricing", Json.read(rule.json("pricing_json")));
            item.put("scope", Json.read(rule.json("scope_json")));
            item.put("effectiveFrom", ViTime.iso(rule.time("effective_from")));
            item.put("approvedAt", ViTime.iso(rule.time("approved_at")));
            out.add(item);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> saveFeeRule(Long ruleId, String payorEntityId, String ruleCode, String scenario,
            String scopeJson, String pricingJson)
    {
        Long payor = payorEntityId == null || payorEntityId.isBlank() ? DEFAULT_PAYOR : Long.valueOf(payorEntityId);
        Long userId = currentUserId();
        if (ruleId == null)
        {
            int revision = ledgerMapper.maxFeeRuleRevision(payor, ruleCode) + 1;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("payorEntityId", payor);
            row.put("ruleCode", ruleCode);
            row.put("revision", revision);
            row.put("scenario", scenario);
            row.put("scopeJson", scopeJson == null ? "{}" : scopeJson);
            row.put("pricingJson", pricingJson == null ? "{}" : pricingJson);
            row.put("status", Codes.Statement.DRAFT);
            row.put("effectiveFrom", ViTime.now());
            row.put("effectiveTo", null);
            row.put("createdBy", userId);
            ledgerMapper.insertFeeRule(row);
            return Map.of("ruleId", String.valueOf(row.get("id")), "status", "DRAFT");
        }
        Row existing = Row.of(ledgerMapper.findFeeRule(ruleId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("费用规则不存在");
        }
        if (!"DRAFT".equals(existing.str("status")))
        {
            throw ApiException.stateConflict("只有草稿可以编辑，已发布版本不可修改");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ruleId", ruleId);
        row.put("scopeJson", scopeJson == null ? existing.json("scope_json") : scopeJson);
        row.put("pricingJson", pricingJson == null ? existing.json("pricing_json") : pricingJson);
        row.put("effectiveFrom", existing.time("effective_from"));
        row.put("effectiveTo", existing.time("effective_to"));
        ledgerMapper.updateFeeRuleDraft(row);
        return Map.of("ruleId", String.valueOf(ruleId), "status", "DRAFT");
    }

    @Transactional
    public Map<String, Object> publishFeeRule(Long ruleId, String opinion)
    {
        Row existing = Row.of(ledgerMapper.findFeeRule(ruleId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("费用规则不存在");
        }
        if (!"DRAFT".equals(existing.str("status")))
        {
            throw ApiException.stateConflict("只有草稿可以发布");
        }
        Long userId = currentUserId();
        if (Objects.equals(existing.lng("created_by"), userId))
        {
            throw ApiException.forbidden("发布审核人不能是规则创建人");
        }
        ledgerMapper.casFeeRuleStatus(ruleId, "DRAFT", "PUBLISHED", userId);
        eventService.record("FEE_RULE", ruleId, "FEE_RULE_PUBLISHED", null, null, null, opinion);
        return Map.of("ruleId", String.valueOf(ruleId), "status", "PUBLISHED");
    }

    /** 试算：使用当前规则对给定里程计算，不入账 */
    public Map<String, Object> trialFee(String scenario, int distanceMeters)
    {
        Map<String, Object> rule = ledgerMapper.findPublishedFeeRule(DEFAULT_PAYOR,
                scenario == null ? "NORMAL_RETURN" : scenario);
        if (rule == null)
        {
            throw ApiException.notFound("没有可用的已发布规则");
        }
        JSONObject pricing = Json.read(Row.of(rule).json("pricing_json"));
        long base = pricing.getLongValue("baseFen");
        long included = pricing.getLongValue("includedMeters");
        long unitMeters = Math.max(1, pricing.getLongValue("unitMeters"));
        long unitPrice = pricing.getLongValue("unitPriceFen");
        long extra = Math.max(0, distanceMeters - included);
        long units = (extra + unitMeters - 1) / unitMeters;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("baseFen", base);
        view.put("extraMeters", extra);
        view.put("units", units);
        view.put("unitPriceFen", unitPrice);
        view.put("amountFen", base + units * unitPrice);
        view.put("breakdown", Map.of("distanceMeters", distanceMeters, "includedMeters", included));
        return view;
    }

    private void validateDriver(Long driverId)
    {
        Row driver = Row.of(accountMapper.findDriverById(driverId));
        if (driver.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
    }

    private Long currentUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            MiniPrincipal p = MiniContext.get();
            return p == null ? null : p.linkedSysUserId;
        }
    }
}
