package com.xinyi.rongli.ledger.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.auth.mapper.AccountMapper;
import com.xinyi.rongli.dispatch.mapper.DispatchMapper;
import com.xinyi.rongli.payment.mapper.PayMapper;
import com.xinyi.rongli.dispatch.service.DispatchService;
import com.xinyi.rongli.driver.service.DriverQualificationService;
import com.xinyi.rongli.fulfill.service.FulfillService;
import com.xinyi.rongli.ledger.service.LedgerService;
import com.xinyi.rongli.driver.service.PenaltyService;
import com.xinyi.rongli.driver.service.TrainingService;
import com.xinyi.rongli.common.support.ApiException;
import com.xinyi.rongli.common.support.Json;
import com.xinyi.rongli.common.support.MiniContext;
import com.xinyi.rongli.common.support.MiniPrincipal;
import com.xinyi.rongli.common.support.Row;
import com.xinyi.rongli.common.support.ViTime;

/**
 * ledger 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/app/driver")
public class DriverLedgerController
{
    @Autowired
    private FulfillService fulfillService;
    @Autowired
    private com.xinyi.rongli.ledger.mapper.LedgerMapper ledgerMapperRef;
    @Autowired
    private LedgerService ledgerService;

    @GetMapping("/statements")
    public Map<String, Object> statements()
    {
        return ledgerService.driverStatements();
    }
    @GetMapping("/statements/{statementId}")
    public Map<String, Object> statement(@PathVariable Long statementId)
    {
        return ledgerService.myStatementDetail(statementId);
    }
    @PostMapping("/statements/{statementId}/confirm")
    public Map<String, Object> confirmStatement(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        Integer revision = body == null || body.get("revision") == null ? null
                : Integer.valueOf(String.valueOf(body.get("revision")));
        String hash = body == null || body.get("snapshotHash") == null ? null
                : String.valueOf(body.get("snapshotHash"));
        if (revision == null)
        {
            throw ApiException.validation("请提供账单版本号");
        }
        return ledgerService.confirmStatement(statementId, revision, hash);
    }
    @PostMapping("/statements/{statementId}/differences")
    public Map<String, Object> submitDifference(@PathVariable Long statementId,
            @RequestBody(required = false) Map<String, Object> body)
    {
        String description = body == null || body.get("description") == null ? null
                : String.valueOf(body.get("description"));
        String entryIds = body == null || body.get("entryIds") == null ? null
                : String.valueOf(body.get("entryIds"));
        return ledgerService.submitDifference(statementId, description, entryIds);
    }
    @GetMapping("/reconciliation-entries")
    public Map<String, Object> reconciliationEntries(@RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String keyword)
    {
        Row driver = fulfillService.requireDriver(MiniContext.require().accountId);
        List<Map<String, Object>> rows = ledgerService.entries(driver.lng("id"), from, to, keyword, true);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
    @GetMapping("/payment-records")
    public Map<String, Object> paymentRecords()
    {
        Row driver = fulfillService.requireDriver(MiniContext.require().accountId);
        List<Map<String, Object>> rows = ledgerMapperRef.listPaymentsOfDriver(driver.lng("id"));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }
}
