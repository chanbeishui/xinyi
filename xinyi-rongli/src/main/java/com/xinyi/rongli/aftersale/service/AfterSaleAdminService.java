package com.xinyi.rongli.aftersale.service;

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
import com.xinyi.rongli.aftersale.mapper.AdminAfterSaleMapper;

/**
 * 管理端 aftersale 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class AfterSaleAdminService
{
    @Autowired
    private AdminAfterSaleMapper adminAfterSaleMapper;
    @Autowired
    private MiscMapper miscMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private EventService eventService;

    public Map<String, Object> disputes(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> d : miscMapper.listDisputes(status, offset, limit))
        {
            rows.add(disputeView(Row.of(d)));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminAfterSaleMapper.countDisputes(status));
        view.put("rows", rows);
        return view;
    }
    public Map<String, Object> disputeDetail(Long disputeId)
    {
        Row d = Row.of(adminAfterSaleMapper.findDisputeDetail(disputeId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("争议不存在");
        }
        return disputeView(d);
    }
    @Transactional
    public Map<String, Object> resolveDispute(Long disputeId, String conclusion, Long userId)
    {
        Row d = Row.of(adminAfterSaleMapper.findDisputeDetail(disputeId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("争议不存在");
        }
        if ("RESOLVED".equals(d.str("status")))
        {
            throw ApiException.stateConflict("该争议已处理");
        }
        if (conclusion == null || conclusion.isBlank())
        {
            throw ApiException.validation("请填写处理结论");
        }
        miscMapper.resolveDispute(disputeId, conclusion, userId);
        eventService.record("DISPUTE", disputeId, "DISPUTE_RESOLVED", d.lng("order_id"), null,
                Map.of("status", "RESOLVED"), conclusion);
        Row order = Row.of(orderMapper.findDetailById(d.lng("order_id")));
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, order.lng("order_id"),
                "DISPUTE_RESOLVED", "您的服务争议已处理：" + conclusion);
        return disputeView(Row.of(adminAfterSaleMapper.findDisputeDetail(disputeId)));
    }
    public Map<String, Object> exceptions(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> e : miscMapper.listExceptions(status, offset, limit))
        {
            Row ex = Row.of(e);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("exceptionId", String.valueOf(ex.lng("id")));
            item.put("orderNo", ex.str("order_no"));
            item.put("type", ex.str("type"));
            item.put("status", ex.str("status"));
            item.put("statusLabel", statusLabelOf(ex.str("status")));
            item.put("description", ex.str("description"));
            item.put("resolution", ex.str("resolution"));
            item.put("createdAt", ViTime.iso(ex.time("created_at")));
            item.put("plateNo", plateOf(ex.str("vehicle_snapshot")));
            item.put("custodyType", ex.str("custody_type"));
            item.put("changedAt", ViTime.iso(ex.time("changed_at")));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminAfterSaleMapper.countExceptions(status));
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> resolveException(Long exceptionId, String resolution, Long userId)
    {
        Row e = Row.of(adminAfterSaleMapper.findExceptionDetail(exceptionId));
        if (e.isEmpty())
        {
            throw ApiException.notFound("异常不存在");
        }
        if ("RESOLVED".equals(e.str("status")))
        {
            throw ApiException.stateConflict("该异常已处理");
        }
        if (resolution == null || resolution.isBlank())
        {
            throw ApiException.validation("请填写处理结论");
        }
        miscMapper.resolveException(exceptionId, resolution, userId);
        eventService.record("EXCEPTION", exceptionId, "EXCEPTION_RESOLVED", e.lng("order_id"), null,
                Map.of("status", "RESOLVED"), resolution);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("exceptionId", String.valueOf(exceptionId));
        view.put("status", "RESOLVED");
        view.put("resolution", resolution);
        return view;
    }
    private Map<String, Object> disputeView(Row d)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("disputeId", String.valueOf(d.lng("id")));
        item.put("orderNo", d.str("order_no"));
        item.put("type", d.str("type"));
        item.put("status", d.str("status"));
        item.put("statusLabel", statusLabelOf(d.str("status")));
        item.put("description", d.str("description"));
        item.put("fileIds", d.json("file_ids"));
        item.put("conclusion", d.str("conclusion"));
        item.put("resolvedAt", ViTime.iso(d.time("resolved_at")));
        item.put("createdAt", ViTime.iso(d.time("created_at")));
        item.put("plateNo", plateOf(d.str("vehicle_snapshot")));
        item.put("custodyType", d.str("custody_type"));
        item.put("changedAt", ViTime.iso(d.time("changed_at")));
        return item;
    }
    private String plateOf(String snapshot)
    {
        if (snapshot == null || snapshot.isBlank())
        {
            return null;
        }
        try
        {
            return Json.read(snapshot).getString("plateNo");
        }
        catch (RuntimeException ex)
        {
            return null;
        }
    }
    private String statusLabelOf(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case "OPEN" -> "待处理";
            case "PROCESSING" -> "处理中";
            case "RESOLVED" -> "已处理";
            default -> status;
        };
    }
}
