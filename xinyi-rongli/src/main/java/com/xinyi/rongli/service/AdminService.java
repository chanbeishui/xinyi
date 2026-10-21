package com.xinyi.rongli.service;

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
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.AdminMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.mapper.LedgerMapper;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.PriceMapper;
import com.xinyi.rongli.mapper.StationMapper;
import com.xinyi.rongli.mapper.VehicleMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 后台管理聚合服务：工作台、订单、车辆车主代驾档案、站点与计价、派单、售后之外的治理与运维。
 *
 * <p>管理端接口只做三件事：按权限取数、做一次明确的状态变更、把结果留痕。
 * 涉及履约状态的推进一律复用车主端/代驾端同一套服务方法，避免两套口径。</p>
 */
@Service
public class AdminService
{
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private PriceMapper priceMapper;

    @Autowired
    private LedgerMapper ledgerMapper;

    @Autowired
    private StationService stationService;

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

    // ------------------------------------------------------------------ 工作台

    public Map<String, Object> dashboard()
    {
        Row base = Row.of(orderMapper.dashboard());
        Row extra = Row.of(adminMapper.statisticsOverview(ViTime.shanghaiDate(ViTime.now()).toString()));
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

    private Map<String, Object> todo(String type, String text, String path)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", type);
        item.put("text", text);
        item.put("path", path);
        return item;
    }

    private long num(Object v)
    {
        if (v instanceof Number n)
        {
            return n.longValue();
        }
        return 0L;
    }

    // ------------------------------------------------------------------ 订单（复用站点服务同一套履约口径）

    public Map<String, Object> orders(String status, Long stationId, int pageNum, int pageSize)
    {
        return stationService.adminOrders(status, stationId, pageNum, pageSize);
    }

    public Map<String, Object> orderDetail(String orderNo)
    {
        Map<String, Object> view = new LinkedHashMap<>(stationService.adminOrderDetail(orderNo));
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        Long orderId = order.lng("id");
        view.put("ownerAccountId", order.lng("owner_account_id") == null ? null
                : String.valueOf(order.lng("owner_account_id")));
        view.put("stationId", order.lng("station_id") == null ? null : String.valueOf(order.lng("station_id")));
        view.put("totalFen", order.lng("total_fen", 0L));
        view.put("custodyRefId", order.lng("custody_ref_id") == null ? null
                : String.valueOf(order.lng("custody_ref_id")));
        view.put("returnedAt", ViTime.iso(order.time("returned_at")));
        view.put("autoConfirmAt", ViTime.iso(order.time("auto_confirm_at")));
        view.put("disputes", adminMapper.listDisputesOfOrder(orderId).size());
        view.put("corrections", adminMapper.listCorrectionsOfOrder(orderId).size());
        return view;
    }

    public Map<String, Object> license(String orderNo)
    {
        return stationService.adminLicense(orderNo);
    }

    @Transactional
    public Map<String, Object> saveLicense(String orderNo, com.xinyi.rongli.dto.DriverDtos.LicenseReviewReq req,
            Long userId)
    {
        return stationService.adminSaveLicense(orderNo, req, userId);
    }

    public Map<String, Object> results(String orderNo)
    {
        return stationService.adminResults(orderNo);
    }

    @Transactional
    public Map<String, Object> submitResult(String orderNo, com.xinyi.rongli.dto.DriverDtos.ResultReq req)
    {
        return stationService.submitResult(orderNo, req, false);
    }

    @Transactional
    public Map<String, Object> applyCorrection(String orderNo, com.xinyi.rongli.dto.DriverDtos.CorrectionReq req)
    {
        return stationService.applyCorrection(orderNo, req, false);
    }

    @Transactional
    public Map<String, Object> resendPickupNotification(String orderNo)
    {
        return stationService.resendPickupNotification(orderNo);
    }

    @Transactional
    public Map<String, Object> reportException(String orderNo, com.xinyi.rongli.dto.DriverDtos.ExceptionReq req)
    {
        return stationService.reportException(orderNo, req);
    }

    public Map<String, Object> capacity(Long stationId)
    {
        return stationService.adminCapacity(stationId);
    }

    @Transactional
    public Map<String, Object> updateCapacity(Long slotId, com.xinyi.rongli.dto.DriverDtos.CapacityChangeReq req)
    {
        return stationService.adminUpdateCapacity(slotId, req);
    }

    // ------------------------------------------------------------------ 派单

    public Map<String, Object> dispatchQueue()
    {
        List<Map<String, Object>> rows = dispatchService.queue();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }

    public Map<String, Object> dispatchCandidates(String keyword)
    {
        List<Map<String, Object>> rows = dispatchService.candidates(keyword);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("total", rows.size());
        view.put("rows", rows);
        return view;
    }

    @Transactional
    public Map<String, Object> manualInvite(String orderNo, Long driverId, String reason)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        return dispatchService.manualInvite(order.lng("id"), driverId, reason);
    }

    /**
     * 立即派单：把订单的派单时点提前到当前时刻并按规则发起一轮自动邀约。
     *
     * <p>常规派单由定时任务在「接车时间 - 提前量」触发。运营在临时加急、司机已在附近、
     * 或客户临时改约等场景下需要跳过等待，此入口即用于该场景；仍走自动派单的候选筛选与
     * 轮转规则，不指定具体司机。</p>
     */
    @Transactional
    public Map<String, Object> dispatchNow(String orderNo)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.SCHEDULED.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status)
                && !Codes.Fulfill.MANUAL_DISPATCH.equals(status))
        {
            throw ApiException.stateConflict("订单当前状态不可派单");
        }
        if (!Codes.Cancel.NONE.equals(order.str("cancel_status", Codes.Cancel.NONE)))
        {
            throw ApiException.stateConflict("订单处于取消流程中，不可派单");
        }
        Long orderId = order.lng("id");
        orderMapper.updateDispatchAt(orderId, ViTime.now(), order.lng("dispatch_config_version", 0L));
        eventService.record("ORDER", orderId, "DISPATCH_TRIGGERED_NOW", orderId, null,
                Map.of("dispatchAt", ViTime.iso(ViTime.now())), "后台立即派单");
        dispatchService.dispatchOrder(orderId);

        Row after = Row.of(orderMapper.findDetailByNo(orderNo));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("fulfillmentStatus", after.str("fulfillment_status"));
        view.put("dispatchAt", ViTime.iso(after.time("dispatch_at")));
        view.put("hint", Codes.Fulfill.MANUAL_DISPATCH.equals(after.str("fulfillment_status"))
                ? "当前无可用代驾，订单已转待人工安排"
                : "已发起一轮自动邀约");
        return view;
    }

    public Map<String, Object> dispatchInvites(Long orderId, Long driverId, String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> i : dispatchMapper.adminListInvites(orderId, driverId, status, offset, limit))
        {
            Row inv = Row.of(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("inviteId", String.valueOf(inv.lng("id")));
            item.put("orderNo", inv.str("order_no"));
            item.put("driverId", String.valueOf(inv.lng("driver_id")));
            item.put("driverName", inv.str("driver_name"));
            item.put("source", inv.str("source"));
            item.put("status", inv.str("status"));
            item.put("statusLabel", inviteLabel(inv.str("status")));
            item.put("expiresAt", ViTime.iso(inv.time("expires_at")));
            item.put("respondedAt", ViTime.iso(inv.time("responded_at")));
            item.put("createdAt", ViTime.iso(inv.time("created_at")));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", rows);
        return view;
    }

    private String inviteLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.Invite.WAIT_RESPONSE -> "等待响应";
            case Codes.Invite.ACCEPTED -> "已接单";
            case Codes.Invite.REJECTED -> "已拒绝";
            case Codes.Invite.EXPIRED -> "已过期";
            case Codes.Invite.REVOKED -> "已撤销";
            default -> status;
        };
    }

    public Map<String, Object> dispatchConfig()
    {
        Map<String, Object> view = new LinkedHashMap<>(dispatchService.dispatchConfig());
        view.put("termsVersion", config.termsVersion());
        view.put("paymentChannel", config.mockPayment() ? "MOCK" : "WECHAT");
        view.put("locationFreshSeconds", config.locationFreshSeconds());
        return view;
    }

    @Autowired
    private com.xinyi.system.service.ISysConfigService sysConfigService;

    @Transactional
    public Map<String, Object> saveDispatchConfig(Map<String, Object> req)
    {
        if (req == null || req.isEmpty())
        {
            throw ApiException.validation("没有需要保存的配置项");
        }
        Map<String, String> allowed = new LinkedHashMap<>();
        allowed.put("leadMinutes", "rongli.dispatch.leadMinutes");
        allowed.put("radiusMeter", "rongli.dispatch.radiusMeter");
        allowed.put("totalMinutes", "rongli.dispatch.totalMinutes");
        allowed.put("inviteExpireSeconds", "rongli.invite.expireSeconds");
        allowed.put("payExpireMinutes", "rongli.pay.expireMinutes");
        allowed.put("autoConfirmHours", "rongli.confirm.autoHours");
        allowed.put("locationFreshSeconds", "rongli.driver.locationFreshSeconds");
        allowed.put("termsVersion", "rongli.service.termsVersion");
        for (Map.Entry<String, String> e : allowed.entrySet())
        {
            Object v = req.get(e.getKey());
            if (v == null)
            {
                continue;
            }
            String text = String.valueOf(v).trim();
            if (text.isEmpty())
            {
                throw ApiException.validation(e.getKey() + " 不能为空");
            }
            if (!"termsVersion".equals(e.getKey()))
            {
                int n;
                try
                {
                    n = Integer.parseInt(text);
                }
                catch (NumberFormatException ex)
                {
                    throw ApiException.validation(e.getKey() + " 必须是整数");
                }
                if (n < 0 || n > 100000)
                {
                    throw ApiException.validation(e.getKey() + " 超出允许范围");
                }
            }
            Map<String, Object> existing = adminMapper.findConfigByKey(e.getValue());
            if (existing == null)
            {
                com.xinyi.system.domain.SysConfig entity = new com.xinyi.system.domain.SysConfig();
                entity.setConfigKey(e.getValue());
                entity.setConfigName(e.getKey());
                entity.setConfigValue(text);
                entity.setConfigType("N");
                sysConfigService.insertConfig(entity);
            }
            else
            {
                adminMapper.updateConfigValue(e.getValue(), text);
            }
            eventService.record("CONFIG", null, "CONFIG_CHANGED", null,
                    Map.of("key", e.getValue()), Map.of("value", text), "管理端保存业务配置");
        }
        return dispatchConfig();
    }

    /** 计划变更影响预览：只算"未派单"的订单，不影响已承接订单 */
    public Map<String, Object> previewDispatchPlan(Integer leadMinutes)
    {
        int lead = leadMinutes == null ? config.dispatchLeadMinutes() : Math.max(0, leadMinutes);
        List<Map<String, Object>> affected = new ArrayList<>();
        for (Map<String, Object> r : orderMapper.listPlanCandidates(500))
        {
            Row o = Row.of(r);
            String status = o.str("fulfillment_status");
            if (!Codes.Fulfill.SCHEDULED.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status))
            {
                continue;
            }
            java.time.LocalDateTime pickup = o.time("pickup_starts_at");
            java.time.LocalDateTime next = pickup == null ? o.time("dispatch_at")
                    : pickup.minusMinutes(lead);
            if (next == null || Objects.equals(next, o.time("dispatch_at")))
            {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", o.str("order_no"));
            item.put("fulfillmentStatus", status);
            item.put("currentDispatchAt", ViTime.iso(o.time("dispatch_at")));
            item.put("nextDispatchAt", ViTime.iso(next));
            affected.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("planId", "PLAN" + crypto.randomDigits(8));
        view.put("leadMinutes", lead);
        view.put("affectedCount", affected.size());
        view.put("rows", affected);
        view.put("hint", "确认后仅调整未派单订单的派单时间，已承接订单不受影响");
        return view;
    }

    /** 确认更新未派单计划：按新提前量重算 dispatchAt，已派单订单跳过 */
    @Transactional
    public Map<String, Object> applyDispatchPlan(Integer leadMinutes)
    {
        int lead = leadMinutes == null ? config.dispatchLeadMinutes() : Math.max(0, leadMinutes);
        if (lead > 24 * 60)
        {
            throw ApiException.validation("提前量不能超过 1440 分钟");
        }
        int changed = 0;
        List<String> skipped = new ArrayList<>();
        List<Map<String, Object>> rows = orderMapper.listPlanCandidates(500);
        for (Map<String, Object> r : rows)
        {
            Row o = Row.of(r);
            String status = o.str("fulfillment_status");
            if (!Codes.Fulfill.SCHEDULED.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status))
            {
                skipped.add(o.str("order_no"));
                continue;
            }
            java.time.LocalDateTime pickup = o.time("pickup_starts_at");
            if (pickup == null)
            {
                skipped.add(o.str("order_no"));
                continue;
            }
            java.time.LocalDateTime next = pickup.minusMinutes(lead);
            if (orderMapper.updateDispatchAt(o.lng("id"), next, o.lng("dispatch_config_version", 0L)) > 0)
            {
                changed++;
                eventService.record("ORDER", o.lng("id"), "DISPATCH_PLAN_UPDATED", o.lng("id"),
                        Map.of("dispatchAt", ViTime.iso(o.time("dispatch_at"))),
                        Map.of("dispatchAt", ViTime.iso(next)), "计划变更确认");
            }
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("leadMinutes", lead);
        view.put("changedCount", changed);
        view.put("skippedCount", skipped.size());
        view.put("skippedOrders", skipped);
        return view;
    }

    // ------------------------------------------------------------------ 争议 / 异常 / 更正

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
        view.put("total", adminMapper.countDisputes(status));
        view.put("rows", rows);
        return view;
    }

    public Map<String, Object> disputeDetail(Long disputeId)
    {
        Row d = Row.of(adminMapper.findDisputeDetail(disputeId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("争议不存在");
        }
        return disputeView(d);
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
        return item;
    }

    @Transactional
    public Map<String, Object> resolveDispute(Long disputeId, String conclusion, Long userId)
    {
        Row d = Row.of(adminMapper.findDisputeDetail(disputeId));
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
        return disputeView(Row.of(adminMapper.findDisputeDetail(disputeId)));
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
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countExceptions(status));
        view.put("rows", rows);
        return view;
    }

    @Transactional
    public Map<String, Object> resolveException(Long exceptionId, String resolution, Long userId)
    {
        Row e = Row.of(adminMapper.findExceptionDetail(exceptionId));
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

    public Map<String, Object> corrections(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : adminMapper.listCorrections(status, offset, limit))
        {
            rows.add(correctionView(Row.of(c)));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countCorrections(status));
        view.put("rows", rows);
        return view;
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

    @Transactional
    public Map<String, Object> auditCorrection(Long correctionId, boolean approve, String opinion, Long userId)
    {
        Row c = Row.of(adminMapper.findCorrection(correctionId));
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
        if (adminMapper.auditCorrection(correctionId, status, userId, opinion) == 0)
        {
            throw ApiException.stateConflict("更正申请状态已变化，请刷新");
        }
        eventService.record("ORDER", c.lng("order_id"), "RESULT_CORRECTION_" + status, c.lng("order_id"), null,
                Map.of("correctionId", String.valueOf(correctionId)), opinion);
        return correctionView(Row.of(adminMapper.findCorrection(correctionId)));
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

    // ------------------------------------------------------------------ 代驾资格

    public Map<String, Object> qualifications(String status, int pageNum, int pageSize)
    {
        String filter = status == null || status.isBlank() ? "SUBMITTED" : status;
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> all = accountMapper.listApplicationsByStatus(filter, 500);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = offset; i < all.size() && rows.size() < limit; i++)
        {
            Row a = Row.of(all.get(i));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("applicationId", String.valueOf(a.lng("id")));
            item.put("driverId", String.valueOf(a.lng("driver_id")));
            item.put("accountId", String.valueOf(a.lng("account_id")));
            item.put("nickname", a.str("nickname"));
            item.put("status", a.str("status"));
            item.put("qualificationStatus", a.str("qualification_status"));
            item.put("licenseClass", a.str("license_class"));
            item.put("licenseValidTo", a.str("license_valid_to"));
            item.put("submittedAt", ViTime.iso(a.time("submitted_at")));
            item.put("reviewedAt", ViTime.iso(a.time("reviewed_at")));
            item.put("reviewOpinion", a.str("review_opinion"));
            rows.add(item);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", all.size());
        view.put("rows", rows);
        return view;
    }

    public Map<String, Object> qualificationDetail(Long applicationId)
    {
        Row a = Row.of(accountMapper.findApplication(applicationId));
        if (a.isEmpty())
        {
            throw ApiException.notFound("资格申请不存在");
        }
        Row driver = Row.of(accountMapper.findDriverById(a.lng("driver_id")));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("applicationId", String.valueOf(applicationId));
        view.put("driverId", String.valueOf(a.lng("driver_id")));
        view.put("status", a.str("status"));
        view.put("submittedAt", ViTime.iso(a.time("submitted_at")));
        view.put("reviewedAt", ViTime.iso(a.time("reviewed_at")));
        view.put("reviewOpinion", a.str("review_opinion"));
        view.put("evidenceFileIds", a.json("evidence_file_ids"));
        view.put("driverName", driver.str("nickname"));
        view.put("licenseClass", driver.str("license_class"));
        view.put("licenseValidTo", driver.str("license_valid_to"));
        view.put("qualificationStatus", driver.str("qualification_status"));
        String plain = crypto.decrypt((byte[]) a.get("sensitive_payload_cipher"));
        if (plain != null)
        {
            JSONObject payload = Json.read(plain);
            Map<String, Object> masked = new LinkedHashMap<>();
            masked.put("realName", mask(payload.getString("realName"), 1, 0));
            masked.put("idCardNo", mask(payload.getString("idCardNo"), 4, 4));
            masked.put("licenseClass", payload.getString("licenseClass"));
            view.put("sensitive", masked);
            view.put("sensitiveHint", "管理端只展示脱敏信息，原件在独立档案中受控访问");
        }
        List<Map<String, Object>> courses = new ArrayList<>();
        for (Map<String, Object> p : adminMapper.listProgressOfApplication(applicationId))
        {
            Row pr = Row.of(p);
            courses.add(Map.of("trainingId", String.valueOf(pr.lng("training_id")),
                    "verifiedSeconds", pr.intg("verified_duration_seconds", 0),
                    "status", pr.str("status")));
        }
        view.put("training", courses);
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

    @Transactional
    public Map<String, Object> auditQualification(Long applicationId, boolean approve, String opinion, Long userId)
    {
        Row a = Row.of(accountMapper.findApplication(applicationId));
        if (a.isEmpty())
        {
            throw ApiException.notFound("资格申请不存在");
        }
        if (!"SUBMITTED".equals(a.str("status")))
        {
            throw ApiException.stateConflict("该申请不在待审核状态");
        }
        if (!approve && (opinion == null || opinion.isBlank()))
        {
            throw ApiException.validation("驳回必须写明理由，代驾会看到该理由");
        }
        String status = approve ? "APPROVED" : "REJECTED";
        if (accountMapper.auditApplication(applicationId, status, userId, opinion) == 0)
        {
            throw ApiException.stateConflict("申请状态已变化，请刷新");
        }
        Long driverId = a.lng("driver_id");
        if (approve)
        {
            // 资格通过：递增资格版本并写入下次复审到期时间（12 个月）
            long revision = Row.of(accountMapper.findDriverById(driverId)).lng("qualification_revision", 0L) + 1;
            accountMapper.approveDriver(driverId, revision, ViTime.now().plusMonths(12));
        }
        else
        {
            accountMapper.updateDriverQualificationStatus(driverId, status);
        }
        Row driver = Row.of(accountMapper.findDriverById(driverId));
        eventService.record("DRIVER", driverId, "QUALIFICATION_" + status, null,
                Map.of("applicationId", String.valueOf(applicationId)), Map.of("status", status), opinion);
        eventService.notify(driver.lng("account_id"), Codes.Role.DRIVER, null, "QUALIFICATION_" + status,
                approve ? "您的代驾资格已通过审核，可以开始听单" : "您的代驾资格审核未通过：" + opinion);
        return qualificationDetail(applicationId);
    }

    // ------------------------------------------------------------------ 车主 / 代驾 / 车辆

    public Map<String, Object> owners(String keyword, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", vehicleMapper.adminOwnerCount(keyword));
        view.put("rows", vehicleMapper.adminOwnerList(keyword, offset, limit));
        return view;
    }

    public Map<String, Object> ownerDetail(Long ownerId)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>(o.map());
        view.put("vehicles", vehicleMapper.vehiclesByOwner(o.lng("account_id")));
        return view;
    }

    @Transactional
    public Map<String, Object> maintainOwner(Long ownerId, String serviceFlag, String remark, Long userId)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        if (serviceFlag != null && !List.of("NORMAL", "VIP", "BLOCKED").contains(serviceFlag.toUpperCase()))
        {
            throw ApiException.validation("客户标记只能是 NORMAL / VIP / BLOCKED");
        }
        accountMapper.updateOwnerMaintenance(o.lng("account_id"),
                serviceFlag == null ? o.str("service_flag") : serviceFlag.toUpperCase(),
                remark == null ? o.str("internal_remark") : remark, userId);
        eventService.record("OWNER", ownerId, "OWNER_MAINTAINED", null,
                Map.of("serviceFlag", String.valueOf(o.str("service_flag"))),
                Map.of("serviceFlag", String.valueOf(serviceFlag)), remark);
        return ownerDetail(ownerId);
    }

    public Map<String, Object> drivers(String keyword, String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", vehicleMapper.adminDriverCount(keyword, status));
        view.put("rows", vehicleMapper.adminDriverList(keyword, status, offset, limit));
        return view;
    }

    public Map<String, Object> driverDetail(Long driverId)
    {
        Row d = Row.of(vehicleMapper.adminDriverDetail(driverId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>(d.map());
        Row progress = Row.of(accountMapper.findLatestApplication(driverId));
        view.put("latestApplicationId", progress.isEmpty() ? null : String.valueOf(progress.lng("id")));
        view.put("latestApplicationStatus", progress.isEmpty() ? null : progress.str("status"));
        view.put("recentAssignments", dispatchMapper.listAssignmentsOfDriver(driverId, null, 0, 10));
        return view;
    }

    @Transactional
    public Map<String, Object> maintainDriver(Long driverId, String remark, Boolean newAssignmentEnabled,
            Long userId)
    {
        Row d = Row.of(vehicleMapper.adminDriverDetail(driverId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        if (remark != null)
        {
            accountMapper.updateDriverRemark(driverId, remark);
        }
        if (newAssignmentEnabled != null)
        {
            accountMapper.updateNewAssignmentEnabled(driverId, newAssignmentEnabled);
        }
        eventService.record("DRIVER", driverId, "DRIVER_MAINTAINED", null,
                Map.of("newAssignmentEnabled", String.valueOf(d.bool("new_assignment_enabled"))),
                Map.of("newAssignmentEnabled", String.valueOf(newAssignmentEnabled)), remark);
        return driverDetail(driverId);
    }

    @Transactional
    public Map<String, Object> disableNewAssignments(Long driverId, boolean disabled, String reason, Long userId)
    {
        Row d = Row.of(accountMapper.findDriverById(driverId));
        if (d.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        if (reason == null || reason.isBlank())
        {
            throw ApiException.validation("请填写原因，该原因会留痕");
        }
        accountMapper.updateNewAssignmentEnabled(driverId, !disabled);
        eventService.record("DRIVER", driverId, disabled ? "NEW_ASSIGNMENT_DISABLED" : "NEW_ASSIGNMENT_ENABLED",
                null, Map.of("enabled", String.valueOf(d.bool("new_assignment_enabled"))),
                Map.of("enabled", String.valueOf(!disabled)), reason);
        Row account = Row.of(accountMapper.findDriverById(driverId));
        eventService.notify(account.lng("account_id"), Codes.Role.DRIVER, null,
                disabled ? "NEW_ASSIGNMENT_DISABLED" : "NEW_ASSIGNMENT_ENABLED",
                disabled ? "运营已暂停您接收新邀约：" + reason : "运营已恢复您接收新邀约");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("driverId", String.valueOf(driverId));
        view.put("newAssignmentEnabled", !disabled);
        view.put("hint", disabled ? "该代驾不会再收到新邀约，在手任务不受影响" : "已恢复派单资格");
        return view;
    }

    public Map<String, Object> vehicles(String keyword, String vehicleTypeCode, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", vehicleMapper.adminCount(keyword, vehicleTypeCode));
        view.put("rows", vehicleMapper.adminList(keyword, vehicleTypeCode, offset, limit));
        return view;
    }

    public Map<String, Object> vehicleDetail(Long vehicleId)
    {
        Row v = Row.of(vehicleMapper.findById(vehicleId));
        if (v.isEmpty() || v.get("deleted_at") != null)
        {
            throw ApiException.notFound("车辆不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>(v.map());
        view.put("orders", vehicleMapper.ordersByVehicle(vehicleId));
        view.put("inspectionResults", vehicleMapper.resultsByVehicle(vehicleId));
        return view;
    }

    @Transactional
    public Map<String, Object> maintainVehicle(Long vehicleId, String remark, String verificationStatus, Long userId)
    {
        Row v = Row.of(vehicleMapper.findById(vehicleId));
        if (v.isEmpty())
        {
            throw ApiException.notFound("车辆不存在");
        }
        if (verificationStatus != null
                && !List.of("PENDING", "VERIFIED", "REJECTED").contains(verificationStatus.toUpperCase()))
        {
            throw ApiException.validation("核验状态只能是 PENDING / VERIFIED / REJECTED");
        }
        vehicleMapper.adminMaintain(vehicleId, remark == null ? v.str("internal_remark") : remark,
                verificationStatus == null ? v.str("verification_status") : verificationStatus.toUpperCase());
        eventService.record("VEHICLE", vehicleId, "VEHICLE_MAINTAINED", null,
                Map.of("verificationStatus", String.valueOf(v.str("verification_status"))),
                Map.of("verificationStatus", String.valueOf(verificationStatus)), remark);
        return vehicleDetail(vehicleId);
    }

    public Map<String, Object> vehicleOrders(Long vehicleId)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", vehicleMapper.ordersByVehicle(vehicleId));
        return view;
    }

    public Map<String, Object> vehicleResults(Long vehicleId)
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", vehicleMapper.resultsByVehicle(vehicleId));
        return view;
    }

    public Map<String, Object> ownerVehicles(Long ownerId)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", vehicleMapper.vehiclesByOwner(o.lng("account_id")));
        return view;
    }

    public Map<String, Object> ownerOrders(Long ownerId, int pageNum, int pageSize)
    {
        Row o = Row.of(vehicleMapper.adminOwnerDetail(ownerId));
        if (o.isEmpty())
        {
            throw ApiException.notFound("车主不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        List<Map<String, Object>> rows = orderMapper.ownerList(o.lng("account_id"), null,
                Math.max(0, (pageNum - 1) * pageSize), Math.min(Math.max(pageSize, 1), 100));
        view.put("rows", rows);
        view.put("total", orderMapper.ownerCount(o.lng("account_id"), null));
        return view;
    }

    public Map<String, Object> driverAssignments(Long driverId, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rows", dispatchMapper.listAssignmentsOfDriver(driverId, null, offset, limit));
        view.put("total", dispatchMapper.countAssignmentsOfDriver(driverId, null));
        return view;
    }

    // ------------------------------------------------------------------ 检测点与授权

    public Map<String, Object> stations(int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", stationMapper.adminCount());
        view.put("rows", stationMapper.adminList(offset, limit));
        return view;
    }

    @Transactional
    public Map<String, Object> createStation(com.xinyi.rongli.dto.DriverDtos.StationUpdateReq req)
    {
        if (req == null || req.name == null || req.name.isBlank())
        {
            throw ApiException.validation("请填写检测点名称");
        }
        if (req.lng == null || req.lat == null)
        {
            throw ApiException.validation("请填写检测点经纬度");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", req.name.trim());
        row.put("deptId", null);
        row.put("address", req.address);
        row.put("lng", req.lng);
        row.put("lat", req.lat);
        row.put("serviceRadiusMeter", req.serviceRadiusMeter == null ? 8000 : req.serviceRadiusMeter);
        row.put("supportedVehicleCodes", req.supportedVehicleCodes == null
                ? "[\"SMALL_CAR\"]" : req.supportedVehicleCodes);
        row.put("businessHours", req.businessHours == null ? "[]" : req.businessHours);
        row.put("enabled", req.enabled == null || req.enabled);
        stationMapper.insert(row);
        Long id = ((Number) row.get("id")).longValue();
        eventService.record("STATION", id, "STATION_CREATED", null, null, Map.of("name", req.name), null);
        return stationInfo(id);
    }

    @Transactional
    public Map<String, Object> updateStation(Long stationId, com.xinyi.rongli.dto.DriverDtos.StationUpdateReq req)
    {
        Row existing = Row.of(stationMapper.find(stationId));
        if (existing.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        if (req == null)
        {
            throw ApiException.validation("缺少站点信息");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", stationId);
        row.put("name", req.name == null ? existing.str("name") : req.name);
        row.put("deptId", existing.lng("dept_id"));
        row.put("address", req.address == null ? existing.str("address") : req.address);
        row.put("lng", req.lng == null ? existing.dec("lng") : req.lng);
        row.put("lat", req.lat == null ? existing.dec("lat") : req.lat);
        row.put("serviceRadiusMeter", req.serviceRadiusMeter == null
                ? existing.intg("service_radius_meter") : req.serviceRadiusMeter);
        row.put("supportedVehicleCodes", req.supportedVehicleCodes == null
                ? existing.json("supported_vehicle_codes") : req.supportedVehicleCodes);
        row.put("businessHours", req.businessHours == null ? existing.json("business_hours") : req.businessHours);
        row.put("enabled", req.enabled == null ? existing.bool("enabled") : req.enabled);
        stationMapper.update(row);
        eventService.record("STATION", stationId, "STATION_UPDATED", null,
                Map.of("enabled", String.valueOf(existing.bool("enabled"))),
                Map.of("enabled", String.valueOf(row.get("enabled"))), null);
        return stationInfo(stationId);
    }

    public Map<String, Object> stationInfo(Long stationId)
    {
        Row s = Row.of(stationMapper.find(stationId));
        if (s.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("stationId", String.valueOf(stationId));
        view.put("name", s.str("name"));
        view.put("deptId", s.lng("dept_id"));
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

    public Map<String, Object> stationGrants(int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 200);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("rows", stationMapper.listGrants(offset, limit));
        return view;
    }

    @Transactional
    public Map<String, Object> updateStationGrant(Long employeeUserId, Long stationId, boolean enabled,
            String reason, Long grantedBy)
    {
        if (employeeUserId == null || stationId == null)
        {
            throw ApiException.validation("缺少员工或站点");
        }
        Row user = Row.of(accountMapper.findSysUser(employeeUserId));
        if (user.isEmpty())
        {
            throw ApiException.notFound("员工不存在");
        }
        Row station = Row.of(stationMapper.find(stationId));
        if (station.isEmpty())
        {
            throw ApiException.notFound("检测点不存在");
        }
        stationMapper.upsertGrant(employeeUserId, stationId, enabled, grantedBy, reason);
        if (!enabled)
        {
            // 撤销授权后立即让该员工旧会话失效，避免继续用旧站点上下文操作
            accountMapper.revokeBySysUser(employeeUserId);
        }
        eventService.record("STATION_GRANT", stationId, enabled ? "GRANT_ENABLED" : "GRANT_DISABLED", null,
                null, Map.of("sysUserId", String.valueOf(employeeUserId)), reason);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("sysUserId", String.valueOf(employeeUserId));
        view.put("stationId", String.valueOf(stationId));
        view.put("enabled", enabled);
        view.put("hint", enabled ? "已授权，该员工切换站点后可见" : "已撤销授权，该员工旧会话已失效");
        return view;
    }

    // ------------------------------------------------------------------ 计价规则

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

    private String str(Object v)
    {
        return v == null || String.valueOf(v).isBlank() ? null : String.valueOf(v).trim();
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

    /** 价格审核：审核人不能是提交人，形成双人控制 */
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

    // ------------------------------------------------------------------ 对账异常（RECONCILE 治理）

    public Map<String, Object> reconciliation(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> g : adminMapper.listGovernance("RECONCILE", status, offset, limit))
        {
            rows.add(governanceView(Row.of(g), true));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countGovernance("RECONCILE", status));
        view.put("rows", rows);
        return view;
    }

    /** 重查可信渠道状态：只记录查询动作，不改动金额与单据状态 */
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
        adminMapper.updateGovernance(caseId, "RESOLVED", userId, conclusion, null, null);
        eventService.record("RECONCILE", caseId, "RECONCILE_RESOLVED", g.lng("order_id"), null,
                Map.of("status", "RESOLVED"), conclusion);
        return governanceView(Row.of(adminMapper.findGovernance(caseId)), true);
    }

    // ------------------------------------------------------------------ 脱敏导出（EXPORT 治理）

    /** 导出任务列表：审批人需要先看到待审清单，再决定批准或驳回 */
    public Map<String, Object> exports(String status, int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> g : adminMapper.listGovernance("EXPORT", status, offset, limit))
        {
            rows.add(governanceView(Row.of(g), true));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", adminMapper.countGovernance("EXPORT", status));
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

    /** 导出任务详情：通过审批后按条件生成脱敏行，链接短期有效 */
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
        for (Map<String, Object> o : orderMapper.adminList(payload.getString("status"),
                payload.getString("stationId") == null ? null : Long.valueOf(payload.getString("stationId")),
                null, null, null, 0, 500))
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

    private String maskPlate(String plate)
    {
        if (plate == null || plate.length() < 5)
        {
            return plate;
        }
        return plate.substring(0, 2) + "***" + plate.substring(plate.length() - 2);
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
        return view;
    }

    // ------------------------------------------------------------------ 审计 / 统计 / 补偿

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
        int span = Math.min(Math.max(days, 1), 90);
        java.time.LocalDateTime from = ViTime.now().minusDays(span);
        List<Map<String, Object>> daily = orderMapper.dailyStats(from);
        Row overview = Row.of(adminMapper.statisticsOverview(ViTime.shanghaiDate(ViTime.now()).toString()));
        Row dash = Row.of(orderMapper.dashboard());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rangeDays", span);
        view.put("from", ViTime.iso(from));
        view.put("overview", overview.map());
        view.put("dashboard", dash.map());
        view.put("daily", daily);
        view.put("definitions", List.of(
                "下单口径：按订单创建时间归属自然日（Asia/Shanghai）",
                "完成口径：以实际归还交接时间为准，非支付时间",
                "取消口径：含取消已办结与未支付关闭",
                "金额口径：一律为整数分，均为订单应收金额，不含代驾费用"));
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

    // ------------------------------------------------------------------ 业务配置 / 通知模板

    public Map<String, Object> businessConfig()
    {
        Map<String, Object> view = new LinkedHashMap<>();
        Map<String, Object> effective = new LinkedHashMap<>();
        effective.put("rongli.dispatch.leadMinutes", config.dispatchLeadMinutes());
        effective.put("rongli.dispatch.radiusMeter", config.dispatchRadiusMeter());
        effective.put("rongli.dispatch.totalMinutes", config.dispatchTotalMinutes());
        effective.put("rongli.invite.expireSeconds", config.inviteExpireSeconds());
        effective.put("rongli.pay.expireMinutes", config.payExpireMinutes());
        effective.put("rongli.confirm.autoHours", config.autoConfirmHours());
        effective.put("rongli.driver.locationFreshSeconds", config.locationFreshSeconds());
        effective.put("rongli.service.termsVersion", config.termsVersion());
        effective.put("rongli.payment.channel", config.mockPayment() ? "MOCK" : "WECHAT");
        view.put("effective", effective);
        view.put("stored", adminMapper.listRongliConfigs());
        view.put("paymentChannelLocked", true);
        view.put("hint", "支付通道需在配置文件中切换，管理端不可改，避免误把联调通道用于真实收款");
        return view;
    }

    /** 通知模板：无独立表，采用内置默认 + sys_config 覆盖，改动可审计 */
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

    // ------------------------------------------------------------------ 员工绑定申请

    public Map<String, Object> bindingRequests(int pageNum, int pageSize)
    {
        int limit = Math.min(Math.max(pageSize, 1), 100);
        int offset = (Math.max(pageNum, 1) - 1) * limit;
        List<Map<String, Object>> all = accountMapper.listPendingBindingRequests();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = offset; i < all.size() && rows.size() < limit; i++)
        {
            rows.add(all.get(i));
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("pageNum", pageNum);
        view.put("pageSize", limit);
        view.put("total", all.size());
        view.put("rows", rows);
        return view;
    }

    /** 签发绑定凭证：凭证只返回一次明文，之后只存哈希 */
    @Transactional
    public Map<String, Object> issueBindingCredential(Long requestId, Long employeeUserId, Long userId)
    {
        Row request = Row.of(accountMapper.findBindingRequest(requestId));
        if (request.isEmpty())
        {
            throw ApiException.notFound("绑定申请不存在");
        }
        if (!"PENDING".equals(request.str("status")))
        {
            throw ApiException.stateConflict("该申请已处理");
        }
        Row employee = Row.of(accountMapper.findSysUser(employeeUserId));
        if (employee.isEmpty())
        {
            throw ApiException.notFound("员工不存在");
        }
        String code = crypto.randomDigits(8);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("requestId", requestId);
        row.put("targetAccountId", request.lng("account_id"));
        row.put("targetSysUserId", employeeUserId);
        row.put("tokenHash", crypto.sha256(code));
        row.put("issuedBy", userId);
        row.put("expiresAt", ViTime.now().plusHours(24));
        row.put("reason", "管理端签发");
        accountMapper.insertBindingCredential(row);
        Long credentialId = ((Number) row.get("id")).longValue();
        accountMapper.updateBindingRequestStatus(requestId, "ISSUED");
        eventService.record("EMPLOYEE_BINDING", requestId, "BINDING_CREDENTIAL_ISSUED", null, null,
                Map.of("sysUserId", String.valueOf(employeeUserId)), "签发绑定凭证");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("credentialId", String.valueOf(credentialId));
        view.put("requestId", String.valueOf(requestId));
        view.put("sysUserId", String.valueOf(employeeUserId));
        view.put("employeeName", employee.str("nick_name"));
        view.put("code", code);
        view.put("expiresAt", ViTime.iso((java.time.LocalDateTime) row.get("expiresAt")));
        view.put("hint", "该凭证只显示一次，请线下告知员工本人在小程序内输入完成绑定");
        return view;
    }

    @Transactional
    public Map<String, Object> revokeBindingCredential(Long credentialId, Long userId)
    {
        Row c = Row.of(accountMapper.findCredential(credentialId));
        if (c.isEmpty())
        {
            throw ApiException.notFound("绑定凭证不存在");
        }
        if (accountMapper.revokeCredential(credentialId) == 0)
        {
            throw ApiException.stateConflict("凭证已被使用或已撤销");
        }
        eventService.record("EMPLOYEE_BINDING", credentialId, "BINDING_CREDENTIAL_REVOKED", null, null, null, null);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("credentialId", String.valueOf(credentialId));
        view.put("revoked", true);
        return view;
    }

    // ------------------------------------------------------------------ 代驾对账运维

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

    /** 处理账单差异：登记结论后按需重新发出账单 */
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

    @Autowired
    private LedgerService ledgerService;
}
