package com.xinyi.rongli.fulfill.service;

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
import com.xinyi.rongli.fulfill.mapper.AdminFulfillMapper;

/**
 * 管理端 fulfill 域服务。由原 AdminService 按功能域拆分而来，方法体与 SQL 保持原样。
 */
@Service
public class FulfillAdminService
{
    @Autowired
    private AdminFulfillMapper adminFulfillMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StationService stationService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventService eventService;
    @Autowired
    private DeptScope deptScope;

    public Map<String, Object> license(String orderNo)
    {
        return stationService.adminLicense(orderNo);
    }
    @Transactional
    public Map<String, Object> saveLicense(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.LicenseReviewReq req,
            Long userId)
    {
        return stationService.adminSaveLicense(orderNo, req, userId);
    }
    public Map<String, Object> results(String orderNo)
    {
        return stationService.adminResults(orderNo);
    }
    @Transactional
    public Map<String, Object> submitResult(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.ResultReq req)
    {
        return stationService.submitResult(orderNo, req, false);
    }
    @Transactional
    public Map<String, Object> applyCorrection(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.CorrectionReq req)
    {
        return stationService.applyCorrection(orderNo, req, false);
    }
    @Transactional
    public Map<String, Object> resendPickupNotification(String orderNo)
    {
        return stationService.resendPickupNotification(orderNo);
    }
    @Transactional
    public Map<String, Object> reportException(String orderNo, com.xinyi.rongli.driver.dto.DriverDtos.ExceptionReq req)
    {
        return stationService.reportException(orderNo, req);
    }
    public Map<String, Object> receivingBoard(Long stationId, String keyword, int pageNum, int pageSize)
    {
        return fulfillBoard(List.of(Codes.Fulfill.ARRIVED_STATION), stationId, keyword, null, null,
                pageNum, pageSize);
    }
    public Map<String, Object> inspectionTodoBoard(Long stationId, String keyword, String bucket,
            int pageNum, int pageSize)
    {
        boolean licensePendingOnly = "LICENSE_PENDING".equalsIgnoreCase(bucket);
        Boolean licensePending = licensePendingOnly ? Boolean.TRUE : null;
        Boolean resultMissing = "RESULT_PENDING".equalsIgnoreCase(bucket) ? Boolean.TRUE : null;
        // 行驶证复核覆盖「已到站」起的全部在站订单：有的车在收车前就要先核证件
        List<String> statuses = licensePendingOnly
                ? List.of(Codes.Fulfill.ARRIVED_STATION, Codes.Fulfill.INSPECTING, Codes.Fulfill.INSPECTED)
                : List.of(Codes.Fulfill.INSPECTING, Codes.Fulfill.INSPECTED);
        return fulfillBoard(statuses, stationId, keyword, licensePending, resultMissing, pageNum, pageSize);
    }
    public Map<String, Object> corrections(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : adminFulfillMapper.listCorrections(status, offset, limit))
        {
            rows.add(correctionView(Row.of(c)));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminFulfillMapper.countCorrections(status));
        view.put("rows", rows);
        return view;
    }
    @Transactional
    public Map<String, Object> auditCorrection(Long correctionId, boolean approve, String opinion, Long userId)
    {
        Row c = Row.of(adminFulfillMapper.findCorrection(correctionId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("更正申请不存在");
        }
        if (!"APPLIED".equals(c.str("status")))
        {
            throw ApiException.stateConflict("该更正申请已处理");
        }
        if (!approve && (opinion == null || opinion.isBlank()))
        {
            throw ApiException.validation("驳回必须写明理由");
        }
        String status = approve ? "APPROVED" : "REJECTED";
        if (adminFulfillMapper.auditCorrection(correctionId, status, userId, opinion) == 0)
        {
            throw ApiException.stateConflict("更正申请状态已变化，请刷新");
        }
        eventService.record("ORDER", c.lng("order_id"), "RESULT_CORRECTION_" + status, c.lng("order_id"), null,
                Map.of("correctionId", String.valueOf(correctionId)), opinion);
        return correctionView(Row.of(adminFulfillMapper.findCorrection(correctionId)));
    }
    private Map<String, Object> correctionView(Row c)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("correctionId", String.valueOf(c.lng("id")));
        item.put("orderNo", c.str("order_no"));
        item.put("baseResultId", String.valueOf(c.lng("base_result_id")));
        item.put("status", c.str("status"));
        item.put("statusLabel", switch (c.str("status", ""))
        {
            case "APPLIED" -> "待审核";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            default -> c.str("status");
        });
        item.put("reason", c.str("reason"));
        item.put("replacement", Json.read(c.json("replacement_json")));
        item.put("opinion", c.str("opinion"));
        item.put("createdAt", ViTime.iso(c.time("created_at")));
        return item;
    }
    private Map<String, Object> fulfillBoard(List<String> statuses, Long stationId, String keyword,
            Boolean licensePending, Boolean resultMissing, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Long> deptIds = deptScope.visibleDeptIds();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : orderMapper.adminFulfillBoard(statuses, stationId, keyword, deptIds,
                licensePending, resultMissing, offset, limit))
        {
            Row o = Row.of(r);
            JSONObject snapshot = Json.read(o.json("vehicle_snapshot"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", o.str("order_no"));
            item.put("fulfillmentStatus", o.str("fulfillment_status"));
            item.put("fulfillmentStatusLabel", bookingService.statusLabel(o.str("fulfillment_status")));
            item.put("custodyType", o.str("custody_type"));
            item.put("plateNo", snapshot.getString("plateNo"));
            item.put("vehicleTypeName", snapshot.getString("vehicleTypeName"));
            item.put("brandModel", snapshot.getString("brandModel"));
            item.put("stationId", o.lng("station_id") == null ? null : String.valueOf(o.lng("station_id")));
            item.put("stationName", o.str("station_name"));
            item.put("ownerName", o.str("owner_name"));
            item.put("driverName", o.str("driver_name"));
            item.put("pickupStartsAt", ViTime.iso(o.time("pickup_starts_at")));
            item.put("pickupEndsAt", ViTime.iso(o.time("pickup_ends_at")));
            item.put("pickupWindowLabel",
                    ViTime.windowLabel(o.time("pickup_starts_at"), o.time("pickup_ends_at")));
            String licenseStatus = o.str("license_status");
            item.put("licenseStatus", licenseStatus == null ? "MISSING" : licenseStatus);
            item.put("licenseStatusLabel", licenseLabel(licenseStatus));
            int resultCount = o.intg("result_count", 0);
            item.put("resultCount", resultCount);
            item.put("resultRevision", o.intg("result_revision", 0));
            // 待办分档，前端直接按 bucket 分栏，不用重复判断口径
            item.put("bucket", !"VERIFIED".equals(licenseStatus) ? "LICENSE_PENDING"
                    : (resultCount <= 0 ? "RESULT_PENDING" : "DONE"));
            if (o.lng("handover_id") != null)
            {
                Map<String, Object> handover = new LinkedHashMap<>();
                handover.put("handoverId", String.valueOf(o.lng("handover_id")));
                handover.put("status", o.str("handover_status"));
                handover.put("generationNo", o.intg("handover_generation", 1));
                handover.put("expiresAt", ViTime.iso(o.time("handover_expires_at")));
                handover.put("expired", o.time("handover_expires_at") != null
                        && o.time("handover_expires_at").isBefore(ViTime.now()));
                item.put("handover", handover);
            }
            else
            {
                item.put("handover", null);
            }
            out.add(item);
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("rows", out);
        page.put("total", orderMapper.adminFulfillBoardCount(statuses, stationId, keyword, deptIds,
                licensePending, resultMissing));
        page.put("pageNum", pageNum);
        page.put("pageSize", limit);
        return page;
    }
    private String licenseLabel(String status)
    {
        if (status == null)
        {
            return "未登记";
        }
        return switch (status)
        {
            case "MISSING" -> "未登记";
            case "PENDING" -> "待复核";
            case "VERIFIED" -> "已核验";
            default -> status;
        };
    }
}
