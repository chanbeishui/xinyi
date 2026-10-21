package com.xinyi.rongli.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.service.AdminService;
import com.xinyi.rongli.service.LedgerService;

/**
 * 管理端代驾费用：费用规则、台账与对账单、行内核价、整单复核、付款登记与核实、调差。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminLedgerController
{
    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AdminService adminService;

    // ------------------------------------------------------------------ 费用规则

    @GetMapping("/driver-fee-rules")
    @PreAuthorize("@ss.hasPermi('biz:driverfeerule:list')")
    public Map<String, Object> feeRules(@RequestParam(required = false) String status)
    {
        List<Map<String, Object>> rows = ledgerService.feeRules(status);
        return Map.of("total", rows.size(), "rows", rows);
    }

    @PostMapping("/driver-fee-rules")
    @PreAuthorize("@ss.hasPermi('biz:driverfeerule:create')")
    public Map<String, Object> createFeeRule(@RequestBody Map<String, Object> body)
    {
        return ledgerService.saveFeeRule(null, str(body.get("payorEntityId")), str(body.get("ruleCode")),
                str(body.get("scenario")), jsonStr(body.get("scope")), jsonStr(body.get("pricing")));
    }

    @GetMapping("/driver-fee-rules/{ruleId}")
    @PreAuthorize("@ss.hasPermi('biz:driverfeerule:query')")
    public Map<String, Object> feeRule(@PathVariable Long ruleId)
    {
        for (Map<String, Object> r : ledgerService.feeRules(null))
        {
            Object id = r.get("ruleId") == null ? r.get("id") : r.get("ruleId");
            if (id != null && String.valueOf(ruleId).equals(String.valueOf(id)))
            {
                return r;
            }
        }
        throw com.xinyi.rongli.support.ApiException.notFound("费用规则不存在");
    }

    @PutMapping("/driver-fee-rules/{ruleId}")
    @PreAuthorize("@ss.hasPermi('biz:driverfeerule:edit')")
    public Map<String, Object> updateFeeRule(@PathVariable Long ruleId, @RequestBody Map<String, Object> body)
    {
        return ledgerService.saveFeeRule(ruleId, str(body.get("payorEntityId")), str(body.get("ruleCode")),
                str(body.get("scenario")), jsonStr(body.get("scope")), jsonStr(body.get("pricing")));
    }

    @PostMapping("/driver-fee-rules/{ruleId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:driverfeerule:publish')")
    public Map<String, Object> publishFeeRule(@PathVariable Long ruleId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        return ledgerService.publishFeeRule(ruleId, body == null ? null : str(body.get("opinion")));
    }

    @PostMapping("/driver-fee-rules/trial")
    @PreAuthorize("@ss.hasPermi('biz:driverfeerule:trial')")
    public Map<String, Object> trialFee(@RequestBody Map<String, Object> body)
    {
        String scenario = str(body.get("scenario"));
        int distance = body.get("distanceMeters") == null ? 0
                : Integer.parseInt(String.valueOf(body.get("distanceMeters")));
        return ledgerService.trialFee(scenario, distance);
    }

    // ------------------------------------------------------------------ 对账单

    @PostMapping("/driver-statements/preview")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:preview')")
    public Map<String, Object> previewStatement(@RequestBody Map<String, Object> body)
    {
        return ledgerService.previewStatement(requireDriverId(body), entryIds(body));
    }

    @PostMapping("/driver-statements")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:create')")
    public Map<String, Object> createStatement(@RequestBody Map<String, Object> body)
    {
        return ledgerService.createStatement(requireDriverId(body), entryIds(body),
                SecurityUtils.getUsername());
    }

    @GetMapping("/driver-statements")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:list')")
    public Map<String, Object> statements(@RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.statements(driverId, status, pageNum, pageSize);
    }

    @GetMapping("/driver-statements/{statementId}")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:query')")
    public Map<String, Object> statement(@PathVariable Long statementId)
    {
        return ledgerService.statementView(statementId);
    }

    @GetMapping("/driver-statements/{statementId}/revisions/{revision}")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:query')")
    public Map<String, Object> statementRevision(@PathVariable Long statementId, @PathVariable int revision)
    {
        return adminService.statementRevision(statementId, revision);
    }

    @PostMapping("/driver-statements/{statementId}/lines/add")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:add')")
    public Map<String, Object> addLines(@PathVariable Long statementId, @RequestBody Map<String, Object> body)
    {
        return ledgerService.addEntries(statementId, entryIds(body), str(body.get("reason")));
    }

    @PostMapping("/driver-statements/{statementId}/lines/{lineId}/remove")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:remove')")
    public Map<String, Object> removeLine(@PathVariable Long statementId, @PathVariable Long lineId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        return ledgerService.removeLine(statementId, lineId, body == null ? null : str(body.get("reason")));
    }

    @PutMapping("/driver-statements/{statementId}/lines/{lineId}/price")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:price')")
    public Map<String, Object> priceLine(@PathVariable Long statementId, @PathVariable Long lineId,
            @RequestBody Map<String, Object> body)
    {
        Long amountFen = body.get("amountFen") == null ? null
                : Long.valueOf(String.valueOf(body.get("amountFen")));
        return ledgerService.priceLine(statementId, lineId, amountFen, str(body.get("reason")),
                str(body.get("idempotencyKey")));
    }

    @GetMapping("/driver-statements/{statementId}/lines/{lineId}/price-history")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:list')")
    public Map<String, Object> priceHistory(@PathVariable Long statementId, @PathVariable Long lineId)
    {
        Map<String, Object> view = ledgerService.statementView(statementId);
        Object lines = view.get("lines");
        List<Object> history = new ArrayList<>();
        if (lines instanceof List<?> list)
        {
            for (Object item : list)
            {
                if (item instanceof Map<?, ?> line && String.valueOf(lineId).equals(String.valueOf(line.get("lineId"))))
                {
                    Object h = line.get("priceHistory");
                    if (h instanceof List<?> hl)
                    {
                        history.addAll(hl);
                    }
                }
            }
        }
        return Map.of("lineId", String.valueOf(lineId), "history", history);
    }

    @PostMapping("/driver-statements/{statementId}/submit-review")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:submitreview')")
    public Map<String, Object> submitReview(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        return ledgerService.submitReview(statementId, body == null ? null : str(body.get("reason")));
    }

    @PostMapping("/driver-statements/{statementId}/review")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:review')")
    public Map<String, Object> reviewStatement(@PathVariable Long statementId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body.get("approve") == null || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        return ledgerService.reviewStatement(statementId, approve, str(body.get("opinion")));
    }

    @GetMapping("/driver-statements/{statementId}/reviews")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:list')")
    public Map<String, Object> reviews(@PathVariable Long statementId)
    {
        Map<String, Object> view = ledgerService.statementView(statementId);
        return Map.of("statementId", String.valueOf(statementId),
                "reviews", view.get("reviews") == null ? List.of() : view.get("reviews"));
    }

    @PostMapping("/driver-statements/{statementId}/withdraw")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:withdraw')")
    public Map<String, Object> withdraw(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        return ledgerService.withdrawStatement(statementId, body == null ? null : str(body.get("reason")));
    }

    @PostMapping("/driver-statements/{statementId}/send")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:send')")
    public Map<String, Object> send(@PathVariable Long statementId)
    {
        return ledgerService.sendStatement(statementId);
    }

    @PostMapping("/driver-statements/{statementId}/void")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:void')")
    public Map<String, Object> voidStatement(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        return ledgerService.voidStatement(statementId, body == null ? null : str(body.get("reason")));
    }

    @GetMapping("/driver-statements/{statementId}/membership-history")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:list')")
    public Map<String, Object> membershipHistory(@PathVariable Long statementId)
    {
        Map<String, Object> view = ledgerService.statementView(statementId);
        return Map.of("statementId", String.valueOf(statementId),
                "membership", view.get("membershipHistory") == null ? List.of() : view.get("membershipHistory"));
    }

    // ------------------------------------------------------------------ 差异与调整

    @GetMapping("/statement-differences")
    @PreAuthorize("@ss.hasPermi('biz:statementdifference:list')")
    public Map<String, Object> differences(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.statementDifferences(status, pageNum, pageSize);
    }

    @PostMapping("/statement-differences/{differenceId}/resolve")
    @PreAuthorize("@ss.hasPermi('biz:statementdifference:resolve')")
    public Map<String, Object> resolveDifference(@PathVariable Long differenceId,
            @RequestBody Map<String, Object> body)
    {
        boolean resend = body.get("resend") != null && Boolean.parseBoolean(String.valueOf(body.get("resend")));
        return adminService.resolveDifference(differenceId, str(body.get("resolution")), resend,
                SecurityUtils.getUserId());
    }

    @GetMapping("/driver-fee-adjustments")
    @PreAuthorize("@ss.hasPermi('biz:driveradjust:list')")
    public Map<String, Object> adjustments(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.feeAdjustments(status, pageNum, pageSize);
    }

    @PostMapping("/driver-fee-adjustments")
    @PreAuthorize("@ss.hasPermi('biz:driveradjust:create')")
    public Map<String, Object> applyAdjustment(@RequestBody Map<String, Object> body)
    {
        Long sourceEntryId = body.get("sourceEntryId") == null ? null
                : Long.valueOf(String.valueOf(body.get("sourceEntryId")));
        Long amountFen = body.get("amountFen") == null ? null
                : Long.valueOf(String.valueOf(body.get("amountFen")));
        if (sourceEntryId == null || amountFen == null)
        {
            throw com.xinyi.rongli.support.ApiException.validation("请指定来源明细与调整金额");
        }
        return ledgerService.applyAdjustment(sourceEntryId, amountFen, str(body.get("reason")),
                str(body.get("evidenceIds")));
    }

    @PostMapping("/driver-fee-adjustments/{adjustmentId}/audit")
    @PreAuthorize("@ss.hasPermi('biz:driveradjust:audit')")
    public Map<String, Object> auditAdjustment(@PathVariable Long adjustmentId,
            @RequestBody Map<String, Object> body)
    {
        boolean approve = body.get("approve") == null || Boolean.parseBoolean(String.valueOf(body.get("approve")));
        return ledgerService.auditAdjustment(adjustmentId, approve, str(body.get("opinion")));
    }

    // ------------------------------------------------------------------ 付款凭证

    @GetMapping("/driver-payment-records")
    @PreAuthorize("@ss.hasPermi('biz:driverpayment:list')")
    public Map<String, Object> payments(@RequestParam(required = false) String status,
            @RequestParam(required = false) Long statementId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.paymentRecords(status, statementId, pageNum, pageSize);
    }

    @GetMapping("/driver-payment-records/{recordId}")
    @PreAuthorize("@ss.hasPermi('biz:driverpayment:query')")
    public Map<String, Object> payment(@PathVariable Long recordId)
    {
        return adminService.paymentRecordDetail(recordId);
    }

    @PostMapping("/driver-payment-records")
    @PreAuthorize("@ss.hasPermi('biz:driverpayment:create')")
    public Map<String, Object> registerPayment(@RequestBody Map<String, Object> body)
    {
        Long statementId = body.get("statementId") == null ? null
                : Long.valueOf(String.valueOf(body.get("statementId")));
        Long amountFen = body.get("amountFen") == null ? null
                : Long.valueOf(String.valueOf(body.get("amountFen")));
        if (statementId == null || amountFen == null)
        {
            throw com.xinyi.rongli.support.ApiException.validation("请指定对账单与付款金额");
        }
        return ledgerService.registerPayment(statementId, amountFen, str(body.get("paidAt")),
                str(body.get("reference")), str(body.get("evidenceIds")));
    }

    @PostMapping("/driver-payment-records/{recordId}/verify")
    @PreAuthorize("@ss.hasPermi('biz:driverpayment:verify')")
    public Map<String, Object> verifyPayment(@PathVariable Long recordId,
            @RequestBody Map<String, Object> body)
    {
        boolean verified = body.get("verified") == null || Boolean.parseBoolean(String.valueOf(body.get("verified")));
        return ledgerService.verifyPayment(recordId, verified, str(body.get("reason")));
    }

    // ------------------------------------------------------------------ 个人对账视图

    @GetMapping("/drivers/{driverId}/reconciliation-overview")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:list')")
    public Map<String, Object> overview(@PathVariable Long driverId)
    {
        return adminService.driverReconciliationOverview(driverId);
    }

    @GetMapping("/drivers/{driverId}/reconciliation-entries")
    @PreAuthorize("@ss.hasPermi('biz:driverstatement:list')")
    public Map<String, Object> entries(@PathVariable Long driverId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean onlySelectable)
    {
        return adminService.driverReconciliationEntries(driverId, from, to, keyword, onlySelectable);
    }

    private Long requireDriverId(Map<String, Object> body)
    {
        if (body == null || body.get("driverId") == null)
        {
            throw com.xinyi.rongli.support.ApiException.validation("请指定代驾");
        }
        return Long.valueOf(String.valueOf(body.get("driverId")));
    }

    @SuppressWarnings("unchecked")
    private List<Long> entryIds(Map<String, Object> body)
    {
        List<Long> ids = new ArrayList<>();
        if (body == null)
        {
            return ids;
        }
        Object raw = body.get("entryIds");
        if (raw instanceof List<?> list)
        {
            for (Object o : list)
            {
                if (o != null && !String.valueOf(o).isBlank())
                {
                    ids.add(Long.valueOf(String.valueOf(o)));
                }
            }
        }
        return ids;
    }

    private String str(Object v)
    {
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).trim();
    }

    /** 结构体参数统一转成 JSON 文本，避免把 Map.toString 当成 JSON 落库 */
    private String jsonStr(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof CharSequence s)
        {
            return s.toString();
        }
        return com.alibaba.fastjson2.JSON.toJSONString(v);
    }
}
