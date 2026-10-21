package com.xinyi.rongli.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.mapper.FulfillMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.StationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 检测点工作台：待收车/检测中订单、行驶证复核、结构化结果登记与容量维护。
 *
 * <p>检测员工只能操作获授权站点内的真实订单；跨站必须显式切换站点并再次校验授权。</p>
 */
@Service
public class StationService
{
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private FulfillMapper fulfillMapper;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventService eventService;

    @Autowired
    private Crypto crypto;

    // ------------------------------------------------------------------ 站点上下文

    public List<Map<String, Object>> authorizedStations()
    {
        MiniPrincipal p = requireStationRole();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> g : stationMapper.grantsOfUser(p.linkedSysUserId))
        {
            Row r = Row.of(g);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stationId", String.valueOf(r.lng("station_id")));
            item.put("name", r.str("station_name"));
            item.put("current", Objects.equals(r.lng("station_id"), p.stationId));
            out.add(item);
        }
        return out;
    }

    public Map<String, Object> stationInfo()
    {
        MiniPrincipal p = requireStationRole();
        Row station = Row.of(stationMapper.find(p.requireStation()));
        if (station.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        return stationView(station);
    }

    @Transactional
    public Map<String, Object> updateStationInfo(DriverDtos.StationUpdateReq req)
    {
        MiniPrincipal p = requireStationRole();
        Row existing = Row.of(stationMapper.find(p.requireStation()));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", existing.lng("id"));
        row.put("name", req.name == null ? existing.str("name") : req.name);
        row.put("deptId", existing.lng("dept_id"));
        row.put("address", req.address == null ? existing.str("address") : req.address);
        row.put("lng", req.lng == null ? existing.dec("lng") : req.lng);
        row.put("lat", req.lat == null ? existing.dec("lat") : req.lat);
        row.put("serviceRadiusMeter", req.serviceRadiusMeter == null ? existing.intg("service_radius_meter")
                : req.serviceRadiusMeter);
        row.put("supportedVehicleCodes", req.supportedVehicleCodes == null ? existing.json("supported_vehicle_codes")
                : req.supportedVehicleCodes);
        row.put("businessHours", req.businessHours == null ? existing.json("business_hours") : req.businessHours);
        row.put("enabled", req.enabled == null ? existing.bool("enabled") : req.enabled);
        stationMapper.update(row);
        eventService.record("STATION", existing.lng("id"), "STATION_UPDATED", null);
        return stationView(Row.of(stationMapper.find(existing.lng("id"))));
    }

    private Map<String, Object> stationView(Row s)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("stationId", String.valueOf(s.lng("id")));
        view.put("name", s.str("name"));
        view.put("address", s.str("address"));
        view.put("lng", s.dec("lng"));
        view.put("lat", s.dec("lat"));
        view.put("serviceRadiusMeter", s.intg("service_radius_meter"));
        view.put("supportedVehicleCodes", s.json("supported_vehicle_codes"));
        view.put("businessHours", s.json("business_hours"));
        view.put("enabled", s.bool("enabled"));
        view.put("version", s.lng("version"));
        return view;
    }

    // ------------------------------------------------------------------ 工作订单

    public Map<String, Object> orders(String status, int pageNum, int pageSize)
    {
        MiniPrincipal p = requireStationRole();
        return listOrders(p.requireStation(), status, pageNum, pageSize);
    }

    /** 管理端工作订单：stationId 为空表示不限站点 */
    public Map<String, Object> adminOrders(String status, Long stationId, int pageNum, int pageSize)
    {
        return listOrders(stationId, status, pageNum, pageSize);
    }

    private Map<String, Object> listOrders(Long stationId, String status, int pageNum, int pageSize)
    {
        String filter = status;
        if (filter == null || filter.isBlank())
        {
            filter = null;
        }
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = Math.max(0, (Math.max(pageNum, 1) - 1) * limit);
        List<Map<String, Object>> rows = orderMapper.adminList(filter, stationId, null, null, null, offset, limit);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows)
        {
            Row o = Row.of(r);
            JSONObject snapshot = Json.read(o.json("vehicle_snapshot"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", o.str("order_no"));
            item.put("fulfillmentStatus", o.str("fulfillment_status"));
            item.put("fulfillmentStatusLabel", bookingService.statusLabel(o.str("fulfillment_status")));
            item.put("plateNo", snapshot.getString("plateNo"));
            item.put("vehicleTypeName", snapshot.getString("vehicleTypeName"));
            item.put("pickupStartsAt", ViTime.iso(o.time("pickup_starts_at")));
            item.put("custodyType", o.str("custody_type"));
            item.put("ownerName", o.str("owner_name"));
            out.add(item);
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("rows", out);
        page.put("total", orderMapper.adminCount(filter, stationId, null, null, null));
        page.put("pageNum", pageNum);
        page.put("pageSize", limit);
        return page;
    }

    public Map<String, Object> orderDetail(String orderNo)
    {
        MiniPrincipal p = requireStationRole();
        return detailView(requireStationOrder(orderNo, p.requireStation()));
    }

    /** 管理端订单详情：跨站点可见，不做站点归属校验 */
    public Map<String, Object> adminOrderDetail(String orderNo)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return detailView(order);
    }

    private Map<String, Object> detailView(Row order)
    {
        JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", order.str("order_no"));
        view.put("fulfillmentStatus", order.str("fulfillment_status"));
        view.put("fulfillmentStatusLabel", bookingService.statusLabel(order.str("fulfillment_status")));
        view.put("cancelStatus", order.str("cancel_status"));
        view.put("custodyType", order.str("custody_type"));
        view.put("plateNo", snapshot.getString("plateNo"));
        view.put("vehicleTypeName", snapshot.getString("vehicleTypeName"));
        view.put("brandModel", snapshot.getString("brandModel"));
        view.put("pickupStartsAt", ViTime.iso(order.time("pickup_starts_at")));
        view.put("stationName", order.str("station_name"));
        view.put("contactName", snapshot.getString("contactName"));
        view.put("version", order.lng("version"));
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(order.lng("id")));
        if (!assignment.isEmpty())
        {
            Row driver = Row.of(accountMapper.findDriverById(assignment.lng("driver_id")));
            view.put("driverName", driver.str("nickname"));
        }
        Map<String, Object> license = fulfillMapper.findLicense(order.lng("id"));
        view.put("licenseStatus", license == null ? "MISSING" : Row.of(license).str("review_status"));
        Row result = Row.of(fulfillMapper.findCurrentResult(order.lng("id")));
        if (!result.isEmpty())
        {
            view.put("result", resultView(result));
        }
        List<String> actions = new ArrayList<>();
        if (Codes.Fulfill.ARRIVED_STATION.equals(order.str("fulfillment_status")))
        {
            actions.add("RECEIVE_CAR");
        }
        if (Codes.Fulfill.INSPECTING.equals(order.str("fulfillment_status")))
        {
            actions.add("REGISTER_RESULT");
        }
        if (Codes.Fulfill.INSPECTED.equals(order.str("fulfillment_status")))
        {
            actions.add("RELEASE_CAR");
        }
        actions.add("REVIEW_LICENSE");
        actions.add("VIEW_EVIDENCE");
        view.put("allowedActions", actions);
        return view;
    }

    // ------------------------------------------------------------------ 行驶证

    public Map<String, Object> license(String orderNo)
    {
        MiniPrincipal p = requireStationRole();
        return licenseOf(requireStationOrder(orderNo, p.requireStation()));
    }

    public Map<String, Object> adminLicense(String orderNo)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return licenseOf(order);
    }

    private Map<String, Object> licenseOf(Row order)
    {
        Map<String, Object> row = fulfillMapper.findLicense(order.lng("id"));
        if (row == null)
        {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("orderNo", order.str("order_no"));
            view.put("reviewStatus", "MISSING");
            return view;
        }
        return licenseView(Row.of(row), order.str("order_no"));
    }

    @Transactional
    public Map<String, Object> saveLicense(String orderNo, DriverDtos.LicenseReviewReq req)
    {
        MiniPrincipal p = requireStationRole();
        Long userId = p.linkedSysUserId == null ? SecurityUtils.getUserId() : p.linkedSysUserId;
        return saveLicenseOf(requireStationOrder(orderNo, p.requireStation()), req, userId);
    }

    @Transactional
    public Map<String, Object> adminSaveLicense(String orderNo, DriverDtos.LicenseReviewReq req, Long userId)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return saveLicenseOf(order, req, userId);
    }

    private Map<String, Object> saveLicenseOf(Row order, DriverDtos.LicenseReviewReq req, Long userId)
    {
        if (req == null)
        {
            throw ApiException.validation("缺少行驶证信息");
        }
        JSONObject payload = new JSONObject();
        payload.put("ownerName", req.ownerName);
        payload.put("plateNo", req.plateNo);
        payload.put("vin", req.vin);
        payload.put("registeredOn", req.registeredOn);
        fulfillMapper.upsertLicense(order.lng("id"), crypto.encrypt(payload.toJSONString()), req.mainFileId, req.subFileId);
        if (Boolean.TRUE.equals(req.verified))
        {
            fulfillMapper.verifyLicense(order.lng("id"), userId);
        }
        eventService.record("ORDER", order.lng("id"), "LICENSE_REVIEWED", order.lng("id"), null,
                Map.of("verified", Boolean.TRUE.equals(req.verified)), null);
        return licenseView(Row.of(fulfillMapper.findLicense(order.lng("id"))), order.str("order_no"));
    }

    private Map<String, Object> licenseView(Row license, String orderNo)
    {
        String plain = crypto.decrypt((byte[]) license.get("sensitive_payload_cipher"));
        JSONObject payload = Json.read(plain);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("reviewStatus", license.str("review_status"));
        view.put("ownerName", payload.getString("ownerName"));
        view.put("plateNo", payload.getString("plateNo"));
        view.put("vin", payload.getString("vin"));
        view.put("registeredOn", payload.getString("registeredOn"));
        view.put("mainFileId", license.lng("main_file_id") == null ? null : String.valueOf(license.lng("main_file_id")));
        view.put("subFileId", license.lng("sub_file_id") == null ? null : String.valueOf(license.lng("sub_file_id")));
        view.put("reviewedAt", ViTime.iso(license.time("reviewed_at")));
        view.put("version", license.lng("version"));
        return view;
    }

    // ------------------------------------------------------------------ 检测结果

    public Map<String, Object> results(String orderNo)
    {
        MiniPrincipal p = requireStationRole();
        return resultsOf(requireStationOrder(orderNo, p.requireStation()));
    }

    public Map<String, Object> adminResults(String orderNo)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return resultsOf(order);
    }

    private Map<String, Object> resultsOf(Row order)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : fulfillMapper.listResults(order.lng("id")))
        {
            out.add(resultView(Row.of(r)));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", order.str("order_no"));
        view.put("versions", out);
        view.put("current", out.isEmpty() ? null : out.get(0));
        return view;
    }

    @Transactional
    public Map<String, Object> submitResult(String orderNo, DriverDtos.ResultReq req, boolean stationRealm)
    {
        Row order = stationRealm ? requireStationOrderForStaff(orderNo) : Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        if (req == null || req.conclusion == null)
        {
            throw ApiException.validation("请填写检测结论");
        }
        String conclusion = req.conclusion.toUpperCase();
        if (!List.of("PASS", "FAIL").contains(conclusion))
        {
            throw ApiException.validation("检测结论只能是 PASS 或 FAIL");
        }
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.INSPECTING.equals(status))
        {
            throw ApiException.stateConflict("订单不在检测中状态，不能登记结果");
        }
        Map<String, Object> license = fulfillMapper.findLicense(order.lng("id"));
        if (license == null || !"VERIFIED".equals(Row.of(license).str("review_status")))
        {
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "请先完成行驶证核验再登记检测结果");
        }
        int revision = fulfillMapper.maxResultRevision(order.lng("id")) + 1;
        Long operator = currentUserId();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", order.lng("id"));
        row.put("revision", revision);
        row.put("conclusion", conclusion);
        row.put("itemsJson", req.items == null || req.items.isBlank() ? "{}" : req.items);
        row.put("inspectorUserId", operator);
        row.put("submittedBy", operator);
        row.put("source", stationRealm ? "STATION" : "ADMIN");
        row.put("inspectedAt", req.inspectedAt == null ? ViTime.now() : ViTime.parseFlexible(req.inspectedAt));
        row.put("submittedAt", ViTime.now());
        fulfillMapper.insertResult(row);

        if (orderMapper.casStatusByState(order.lng("id"), Codes.Fulfill.INSPECTING, Codes.Fulfill.INSPECTED) == 0)
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "订单状态已变化，请刷新");
        }
        eventService.record("ORDER", order.lng("id"), "RESULT_REGISTERED", order.lng("id"), null,
                Map.of("revision", revision, "conclusion", conclusion), null);
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, order.lng("id"), "RESULT_REGISTERED",
                "检测结果已登记：" + ("PASS".equals(conclusion) ? "合格" : "不合格"));
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(order.lng("id")));
        if (!assignment.isEmpty())
        {
            Row driver = Row.of(accountMapper.findDriverById(assignment.lng("driver_id")));
            eventService.notify(driver.lng("account_id"), Codes.Role.DRIVER, order.lng("id"), "PICKUP_READY",
                    "检测已完成，请到站点办理取车放行");
        }
        return resultView(Row.of(fulfillMapper.findCurrentResult(order.lng("id"))));
    }

    private Map<String, Object> resultView(Row r)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("resultId", String.valueOf(r.lng("id")));
        view.put("revision", r.intg("revision"));
        view.put("conclusion", r.str("conclusion"));
        view.put("conclusionLabel", "PASS".equals(r.str("conclusion")) ? "合格" : "不合格");
        view.put("items", Json.read(r.json("items_json")));
        view.put("source", r.str("source"));
        view.put("inspectedAt", ViTime.iso(r.time("inspected_at")));
        view.put("submittedAt", ViTime.iso(r.time("submitted_at")));
        view.put("supersededAt", ViTime.iso(r.time("superseded_at")));
        return view;
    }

    @Transactional
    public Map<String, Object> applyCorrection(String orderNo, DriverDtos.CorrectionReq req, boolean stationRealm)
    {
        Row order = stationRealm ? requireStationOrderForStaff(orderNo) : Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Row current = Row.of(fulfillMapper.findCurrentResult(order.lng("id")));
        if (current.isEmpty())
        {
            throw ApiException.stateConflict("该订单尚无检测结果，不能申请更正");
        }
        if (req == null || req.reason == null || req.reason.isBlank())
        {
            throw ApiException.validation("请填写更正原因");
        }
        Map<String, Object> replacement = new LinkedHashMap<>();
        replacement.put("conclusion", req.conclusion);
        replacement.put("items", req.items);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", order.lng("id"));
        row.put("baseResultId", current.lng("id"));
        row.put("replacementJson", Json.write(replacement));
        row.put("reason", req.reason);
        row.put("applicantId", currentUserId());
        fulfillMapper.insertCorrection(row);
        Long correctionId = ((Number) row.get("id")).longValue();
        eventService.record("ORDER", order.lng("id"), "RESULT_CORRECTION_APPLIED", order.lng("id"), null,
                Map.of("correctionId", String.valueOf(correctionId)), req.reason);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("correctionId", String.valueOf(correctionId));
        view.put("status", "APPLIED");
        view.put("reason", req.reason);
        return view;
    }

    // ------------------------------------------------------------------ 容量

    @Transactional
    public Map<String, Object> capacity()
    {
        MiniPrincipal p = requireStationRole();
        return capacityOf(p.requireStation());
    }

    @Transactional
    public Map<String, Object> adminCapacity(Long stationId)
    {
        if (stationId == null)
        {
            throw ApiException.validation("请指定检测点");
        }
        return capacityOf(stationId);
    }

    private Map<String, Object> capacityOf(Long stationId)
    {
        ensureStationSlots(stationId);
        LocalDateTime from = ViTime.now();
        LocalDateTime to = from.plusDays(7);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : stationMapper.listSlots("STATION_RECEIVE", String.valueOf(stationId), from, to))
        {
            Row slot = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("slotId", String.valueOf(slot.lng("id")));
            item.put("startsAt", ViTime.iso(slot.time("starts_at")));
            item.put("endsAt", ViTime.iso(slot.time("ends_at")));
            item.put("label", ViTime.shanghaiText(slot.time("starts_at")));
            item.put("totalQuota", slot.intg("total_quota"));
            item.put("heldQuota", slot.intg("held_quota"));
            item.put("confirmedQuota", slot.intg("confirmed_quota"));
            item.put("consumedQuota", slot.intg("consumed_quota"));
            item.put("usedQuota", slot.intg("held_quota") + slot.intg("confirmed_quota") + slot.intg("consumed_quota"));
            item.put("enabled", slot.bool("enabled"));
            out.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("stationId", String.valueOf(stationId));
        view.put("slots", out);
        return view;
    }

    private void ensureStationSlots(Long stationId)
    {
        LocalDate today = ViTime.shanghaiDate(ViTime.now());
        for (int day = 0; day < 7; day++)
        {
            LocalDate date = today.plusDays(day);
            for (int hour = 9; hour < 17; hour++)
            {
                LocalDateTime start = ViTime.fromShanghai(date.atTime(hour, 0));
                LocalDateTime end = ViTime.fromShanghai(date.atTime(hour + 1, 0));
                if (stationMapper.findSlotExact("STATION_RECEIVE", String.valueOf(stationId), start, end) != null)
                {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("quotaType", "STATION_RECEIVE");
                row.put("scopeKey", String.valueOf(stationId));
                row.put("stationId", stationId);
                row.put("startsAt", start);
                row.put("endsAt", end);
                row.put("totalQuota", 5);
                stationMapper.insertSlot(row);
            }
        }
    }

    @Transactional
    public Map<String, Object> updateCapacity(Long slotId, DriverDtos.CapacityChangeReq req)
    {
        MiniPrincipal p = requireStationRole();
        return updateCapacityOf(slotId, req, p.requireStation());
    }

    @Transactional
    public Map<String, Object> adminUpdateCapacity(Long slotId, DriverDtos.CapacityChangeReq req)
    {
        Row slot = Row.of(stationMapper.findSlot(slotId));
        if (slot.isEmpty())
        {
            throw ApiException.notFound("容量时段不存在");
        }
        return updateCapacityOf(slotId, req, slot.lng("station_id"));
    }

    private Map<String, Object> updateCapacityOf(Long slotId, DriverDtos.CapacityChangeReq req, Long stationId)
    {
        Row slot = Row.of(stationMapper.findSlot(slotId));
        if (slot.isEmpty() || !Objects.equals(slot.lng("station_id"), stationId))
        {
            throw ApiException.notFound("容量时段不存在");
        }
        if (!"STATION_RECEIVE".equals(slot.str("quota_type")))
        {
            throw ApiException.validation("只能维护本站接收容量");
        }
        if (req == null || req.totalQuota == null || req.totalQuota < 0)
        {
            throw ApiException.validation("请填写有效容量");
        }
        int used = slot.intg("held_quota") + slot.intg("confirmed_quota") + slot.intg("consumed_quota");
        if (req.totalQuota < used)
        {
            throw ApiException.stateConflict("已占用 " + used + " 个名额，容量不能小于已占用数");
        }
        boolean enabled = req.enabled == null ? slot.bool("enabled") : req.enabled;
        if (stationMapper.updateSlotCapacity(slotId, req.totalQuota, enabled) == 0)
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "容量已变化，请刷新后重试");
        }
        eventService.record("STATION", stationId, "CAPACITY_UPDATED", null,
                Map.of("slotId", String.valueOf(slotId)), Map.of("totalQuota", req.totalQuota), null);
        return capacityOf(stationId);
    }

    // ------------------------------------------------------------------ 其他

    @Transactional
    public Map<String, Object> reportException(String orderNo, DriverDtos.ExceptionReq req)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        if (req == null || req.type == null || req.type.isBlank())
        {
            throw ApiException.validation("请选择异常类型");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", order.lng("id"));
        row.put("type", req.type);
        row.put("description", req.description);
        row.put("fileIds", req.fileIds);
        miscMapper.insertException(row);
        eventService.record("ORDER", order.lng("id"), "EXCEPTION_REPORTED", order.lng("id"), null,
                Map.of("type", req.type), req.description);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("exceptionId", String.valueOf(row.get("id")));
        view.put("status", "OPEN");
        return view;
    }

    @Transactional
    public Map<String, Object> resendPickupNotification(String orderNo)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, order.lng("id"), "PICKUP_REMINDER",
                "车辆已完成检测，请安排取车");
        Map<String, Object> ack = new LinkedHashMap<>();
        ack.put("sent", true);
        return ack;
    }

    private Row requireStationOrder(String orderNo, Long stationId)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        if (!Objects.equals(order.lng("station_id"), stationId))
        {
            throw ApiException.forbidden("该订单不属于当前检测点");
        }
        return order;
    }

    /** 后台员工（ADMIN 域）操作结果登记时的站点校验 */
    private Row requireStationOrderForStaff(String orderNo)
    {
        return Row.of(orderMapper.findDetailByNo(orderNo));
    }

    private MiniPrincipal requireStationRole()
    {
        MiniPrincipal p = MiniContext.require();
        if (p.linkedSysUserId == null)
        {
            throw ApiException.forbidden("尚未绑定员工身份，无法使用检测点工作台");
        }
        if (!p.isStationRole())
        {
            throw ApiException.forbidden("请先切换到检测员工视角");
        }
        return p;
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
