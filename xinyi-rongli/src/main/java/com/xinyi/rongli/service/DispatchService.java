package com.xinyi.rongli.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.StationMapper;
import com.xinyi.rongli.mapper.VehicleMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 派单服务：计划派单、单邀约、全局轮转、人工兜底与司机占用。
 *
 * <p>不变量：同一司机最多一个待响应邀约或一个在手承接（vi_driver_occupancy 主键保证）；
 * 释放必须精确匹配 holderId，旧事件不能误释放新任务。</p>
 */
@Service
public class DispatchService
{
    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private static final String CURSOR_SCOPE = "GLOBAL";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private EventService eventService;

    @Autowired
    private LedgerService ledgerService;

    // ------------------------------------------------------------------ 计划派单

    /** 扫描到期的预约订单并启动派单 */
    public int dispatchDueOrders()
    {
        int count = 0;
        for (Map<String, Object> row : orderMapper.listDispatchDue(50))
        {
            try
            {
                dispatchOrder(Row.of(row).lng("id"));
                count++;
            }
            catch (Exception e)
            {
                log.warn("订单派单失败 orderId={}", Row.of(row).lng("id"), e);
            }
        }
        return count;
    }

    /**
     * 为订单发起一轮邀约。无可用司机时转待人工安排。
     */
    @Transactional
    public void dispatchOrder(Long orderId)
    {
        Row order = Row.of(orderMapper.findDetailById(orderId));
        if (order.isEmpty())
        {
            return;
        }
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.PENDING_DISPATCH.equals(status)
                && !Codes.Fulfill.SCHEDULED.equals(status)
                && !Codes.Fulfill.MANUAL_DISPATCH.equals(status))
        {
            return;
        }
        if (!Codes.Cancel.NONE.equals(order.str("cancel_status")))
        {
            return;
        }
        if (orderMapper.casStatusByState(orderId, status, Codes.Fulfill.DISPATCHING) == 0)
        {
            return;
        }

        // 已派单总时长超过上限：不再扩圈，直接转人工
        Integer elapsedMinutes = dispatchMapper.minutesSinceFirstInvite(orderId);
        if (elapsedMinutes != null && elapsedMinutes >= config.dispatchTotalMinutes())
        {
            toManual(orderId, "派单超时，已按上限转待人工安排");
            return;
        }

        Set<Long> invited = invitedDriverIds(orderId);
        List<Integer> steps = config.dispatchRadiusSteps();
        // 断点续跑：从"已经走到过的最大轮次"接着扩，而不是每次都从第 1 档重来。
        // 轮次取邀约与扩圈留痕的较大者——三轮都没候选的订单没有邀约，只看邀约会退化成 0。
        int startRound = Math.max(1, Math.min(dispatchMapper.maxReachedRound(orderId), steps.size()));

        Row candidate = null;
        int round = startRound;
        while (true)
        {
            candidate = pickCandidate(order, invited, steps.get(round - 1));
            if (candidate != null)
            {
                break;
            }
            if (round >= steps.size())
            {
                break;
            }
            round++;
            eventService.record("ORDER", orderId, "DISPATCH_EXPAND", orderId, null,
                    Map.of("round", String.valueOf(round), "radiusMeter", String.valueOf(steps.get(round - 1))),
                    "第 " + (round - 1) + " 轮半径内无可用代驾，按阶梯扩圈");
        }
        if (candidate == null)
        {
            toManual(orderId, "扩圈 " + steps.size() + " 轮后仍无可用代驾");
            return;
        }
        issueInvite(order, candidate, "AUTO", round);
    }

    /** 转待人工安排并留痕 */
    private void toManual(Long orderId, String reason)
    {
        orderMapper.casStatusByState(orderId, Codes.Fulfill.DISPATCHING, Codes.Fulfill.MANUAL_DISPATCH);
        eventService.record("ORDER", orderId, "NO_CANDIDATE", orderId, null,
                Map.of("status", Codes.Fulfill.MANUAL_DISPATCH), reason);
    }

    /** 人工发出邀约 */
    @Transactional
    public Map<String, Object> manualInvite(Long orderId, Long driverId, String reason)
    {
        Row order = Row.of(orderMapper.findDetailById(orderId));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.MANUAL_DISPATCH.equals(status) && !Codes.Fulfill.PENDING_DISPATCH.equals(status)
                && !Codes.Fulfill.DISPATCHING.equals(status))
        {
            throw ApiException.stateConflict("订单当前状态不可人工邀约");
        }
        Row driver = Row.of(accountMapper.findDriverById(driverId));
        if (driver.isEmpty())
        {
            throw ApiException.notFound("代驾不存在");
        }
        if (!"APPROVED".equals(driver.str("qualification_status")))
        {
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "该代驾资格未通过审核");
        }
        if (dispatchMapper.findOccupancy(driverId) != null)
        {
            throw ApiException.stateConflict("该代驾存在未结束的邀约或承接");
        }
        if (dispatchMapper.findActiveAssignmentOfDriver(driverId) != null)
        {
            throw ApiException.stateConflict("该代驾仍在执行其他任务");
        }
        if (!Codes.Fulfill.DISPATCHING.equals(status)
                && orderMapper.casStatusByState(orderId, status, Codes.Fulfill.DISPATCHING) == 0)
        {
            throw ApiException.stateConflict("订单状态已变化，请刷新");
        }
        issueInvite(order, driver, "MANUAL", Math.max(1, dispatchMapper.maxRoundNo(orderId)));
        eventService.record("ORDER", orderId, "MANUAL_INVITE", orderId, null,
                Map.of("driverId", String.valueOf(driverId)), reason);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", order.str("order_no"));
        view.put("driverId", String.valueOf(driverId));
        view.put("source", "MANUAL");
        view.put("expiresInSeconds", config.inviteExpireSeconds());
        return view;
    }

    private void issueInvite(Row order, Row driver, String source, int roundNo)
    {
        Long orderId = order.lng("id");
        Long driverId = driver.lng("driver_id", driver.lng("id", 0L));
        LocalDateTime expiresAt = ViTime.now().plusSeconds(config.inviteExpireSeconds());

        Map<String, Object> invite = new LinkedHashMap<>();
        invite.put("orderId", orderId);
        invite.put("driverId", driverId);
        invite.put("source", source);
        invite.put("roundNo", roundNo);
        invite.put("roundKey", roundNo + "-" + System.currentTimeMillis());
        invite.put("expiresAt", expiresAt);
        try
        {
            dispatchMapper.insertInvite(invite);
        }
        catch (DuplicateKeyException e)
        {
            // 同单已有待响应邀约
            return;
        }
        Long inviteId = ((Number) invite.get("id")).longValue();
        try
        {
            dispatchMapper.acquireOccupancy(driverId, "INVITE", inviteId, orderId, expiresAt);
        }
        catch (DuplicateKeyException e)
        {
            dispatchMapper.respondInvite(inviteId, Codes.Invite.REVOKED);
            throw ApiException.stateConflict("该代驾刚刚被其他订单占用，请换一位");
        }

        Long accountId = Row.of(accountMapper.findDriverById(driverId)).lng("account_id");
        eventService.notify(accountId, Codes.Role.DRIVER, orderId, "DISPATCH_INVITE",
                "您有新的接车邀约，请在 " + config.inviteExpireSeconds() + " 秒内响应");
        eventService.record("ORDER", orderId, "INVITE_ISSUED", orderId, null,
                Map.of("inviteId", String.valueOf(inviteId), "driverId", String.valueOf(driverId),
                        "round", String.valueOf(roundNo)), source);
    }

    /** 全局游标轮转：从上次指针之后找第一位可用司机，找不到再从头 */
    private Row pickCandidate(Row order, Set<Long> exclude, int radiusMeter)
    {
        List<Row> eligible = eligibleDrivers(order, radiusMeter);
        if (eligible.isEmpty())
        {
            return null;
        }
        Map<String, Object> cursor = orderMapper.findCursor(CURSOR_SCOPE);
        long lastDriverId = cursor == null ? 0 : Row.of(cursor).lng("last_driver_id", 0);
        Row next = null;
        for (Row d : eligible)
        {
            long id = d.lng("driver_id", 0);
            if (exclude.contains(id))
            {
                continue;
            }
            if (id > lastDriverId)
            {
                next = d;
                break;
            }
        }
        if (next == null)
        {
            for (Row d : eligible)
            {
                if (!exclude.contains(d.lng("driver_id", 0)))
                {
                    next = d;
                    break;
                }
            }
        }
        if (next != null)
        {
            orderMapper.saveCursor(CURSOR_SCOPE, next.lng("driver_id"));
        }
        return next;
    }

    /** 资格有效、允许新接单、开启听单、无占用，且位置在派单半径内 */
    private List<Row> eligibleDrivers(Row order, int radiusMeter)
    {
        JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
        double targetLng = snapshot.getDoubleValue("lng");
        double targetLat = snapshot.getDoubleValue("lat");
        int radius = radiusMeter;
        long now = System.currentTimeMillis();
        int freshSeconds = config.locationFreshSeconds();

        List<Row> out = new ArrayList<>();
        for (Map<String, Object> raw : dispatchMapper.listEligibleDrivers())
        {
            Row d = Row.of(raw);
            Object cipher = d.get("location_cipher");
            if (cipher instanceof byte[] bytes && bytes.length > 12 && targetLng != 0 && targetLat != 0)
            {
                LocalDateTime capturedAt = d.time("captured_at");
                if (capturedAt != null
                        && capturedAt.plusSeconds(freshSeconds).isBefore(ViTime.now()))
                {
                    continue;
                }
                JSONObject loc = Json.read(crypto.decrypt(bytes));
                double dLng = loc.getDoubleValue("lng");
                double dLat = loc.getDoubleValue("lat");
                if (haversine(targetLng, targetLat, dLng, dLat) > radius)
                {
                    continue;
                }
            }
            out.add(d);
        }
        return out;
    }

    private double haversine(double lng1, double lat1, double lng2, double lat2)
    {
        double r = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    private Set<Long> invitedDriverIds(Long orderId)
    {
        Set<Long> ids = new LinkedHashSet<>();
        for (Map<String, Object> r : dispatchMapper.adminListInvites(orderId, null, null, null, 0, 100))
        {
            ids.add(Row.of(r).lng("driver_id"));
        }
        return ids;
    }

    // ------------------------------------------------------------------ 邀约响应

    @Transactional
    public Map<String, Object> respondInvite(Long inviteId, boolean accept, String reason)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("尚未建立代驾档案");
        }
        Long driverId = driver.lng("id");
        Row invite = Row.of(dispatchMapper.findInviteForDriver(inviteId, driverId));
        if (invite.isEmpty())
        {
            throw ApiException.notFound("邀约不存在");
        }
        if (!Codes.Invite.WAIT_RESPONSE.equals(invite.str("status")))
        {
            throw ApiException.stateConflict("邀约已失效或已响应");
        }
        if (invite.time("expires_at").isBefore(ViTime.now()))
        {
            dispatchMapper.respondInvite(inviteId, Codes.Invite.EXPIRED);
            dispatchMapper.releaseOccupancy(driverId, inviteId);
            throw ApiException.stateConflict("邀约已超时");
        }

        if (!accept)
        {
            if (dispatchMapper.respondInvite(inviteId, Codes.Invite.REJECTED) == 0)
            {
                throw ApiException.stateConflict("邀约状态已变化");
            }
            dispatchMapper.releaseOccupancy(driverId, inviteId);
            Long orderId = invite.lng("order_id");
            eventService.record("ORDER", orderId, "INVITE_REJECTED", orderId, null,
                    Map.of("driverId", String.valueOf(driverId)), reason);
            orderMapper.casStatusByState(orderId, Codes.Fulfill.DISPATCHING, Codes.Fulfill.PENDING_DISPATCH);
            dispatchOrder(orderId);
            return Map.of("accepted", false);
        }

        if (!"APPROVED".equals(driver.str("qualification_status")) || !driver.bool("new_assignment_enabled")
                || !driver.bool("listening_enabled"))
        {
            throw ApiException.forbidden("当前状态不可接单，请检查听单与新单开关");
        }

        if (dispatchMapper.respondInvite(inviteId, Codes.Invite.ACCEPTED) == 0)
        {
            throw ApiException.stateConflict("邀约已被其他设备处理");
        }
        Long orderId = invite.lng("order_id");
        Map<String, Object> assignment = new LinkedHashMap<>();
        assignment.put("orderId", orderId);
        assignment.put("driverId", driverId);
        assignment.put("inviteId", inviteId);
        try
        {
            dispatchMapper.insertAssignment(assignment);
        }
        catch (DuplicateKeyException e)
        {
            throw ApiException.stateConflict("您已有在手任务，不能同时承接两单");
        }
        Long assignmentId = ((Number) assignment.get("id")).longValue();

        // 占用从邀约切换为承接
        dispatchMapper.releaseOccupancy(driverId, inviteId);
        try
        {
            dispatchMapper.acquireOccupancy(driverId, "ASSIGNMENT", assignmentId, orderId, null);
        }
        catch (DuplicateKeyException e)
        {
            throw ApiException.stateConflict("代驾占用状态冲突，请重试");
        }

        orderMapper.updateActiveAssignment(orderId, assignmentId);
        orderMapper.updateCustody(orderId, Codes.Custody.DRIVER, driverId);
        orderMapper.casStatusByState(orderId, Codes.Fulfill.DISPATCHING, Codes.Fulfill.ACCEPTED);

        Row order = Row.of(orderMapper.findDetailById(orderId));
        eventService.record("ORDER", orderId, "ASSIGNMENT_ACCEPTED", orderId, null,
                Map.of("driverId", String.valueOf(driverId), "assignmentId", String.valueOf(assignmentId)), null);
        eventService.publish("ORDER", orderId, "ASSIGNMENT_ACCEPTED", Map.of("orderNo", order.str("order_no")));
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "ASSIGNMENT_ACCEPTED",
                "代驾已接单，将在约定时段上门接车");

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("accepted", true);
        view.put("assignmentId", String.valueOf(assignmentId));
        view.put("orderNo", order.str("order_no"));
        return view;
    }

    /** 邀约到期回收：释放占用并把订单退回待派单，继续轮转 */
    public int expireInvites()
    {
        int expired = 0;
        for (Map<String, Object> r : orderMapper.listDispatchingInvoices(50))
        {
            Long orderId = Row.of(r).lng("id");
            Map<String, Object> invite = null;
            for (Map<String, Object> item : dispatchMapper.adminListInvites(orderId, null,
                    Codes.Invite.WAIT_RESPONSE, null, 0, 5))
            {
                invite = item;
                break;
            }
            if (invite == null)
            {
                continue;
            }
            Row inv = Row.of(invite);
            if (inv.time("expires_at").isAfter(ViTime.now()))
            {
                continue;
            }
            dispatchMapper.respondInvite(inv.lng("id"), Codes.Invite.EXPIRED);
            dispatchMapper.releaseOccupancy(inv.lng("driver_id"), inv.lng("id"));
            dispatchMapper.releaseExpiredInviteOccupancy();
            orderMapper.casStatusByState(orderId, Codes.Fulfill.DISPATCHING, Codes.Fulfill.PENDING_DISPATCH);
            eventService.record("ORDER", orderId, "INVITE_EXPIRED", orderId, null, null, null);
            dispatchOrder(orderId);
            expired++;
        }
        return expired;
    }

    // ------------------------------------------------------------------ 代驾取消承接

    @Transactional
    public Map<String, Object> cancelAssignment(String orderNo, String reason)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("尚未建立代驾档案");
        }
        Long driverId = driver.lng("id");
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Long orderId = order.lng("id");
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfDriver(driverId));
        if (assignment.isEmpty() || !java.util.Objects.equals(assignment.lng("order_id"), orderId))
        {
            throw ApiException.forbidden("该订单不在您的在手任务中");
        }
        String status = order.str("fulfillment_status");
        if (!Codes.Fulfill.ACCEPTED.equals(status) && !Codes.Fulfill.GOING_PICKUP.equals(status)
                && !Codes.Fulfill.ARRIVED_PICKUP.equals(status))
        {
            throw ApiException.stateConflict("已接车后不能自助解除承接，请联系运营");
        }

        dispatchMapper.endAssignment(assignment.lng("id"), Codes.EndReason.DRIVER_CANCEL);
        dispatchMapper.releaseOccupancy(driverId, assignment.lng("id"));
        orderMapper.updateActiveAssignment(orderId, null);
        orderMapper.updateCustody(orderId, Codes.Custody.OWNER, order.lng("owner_account_id"));
        orderMapper.casStatusByState(orderId, status, Codes.Fulfill.PENDING_DISPATCH);

        // 未接车解除承接：原车主订单保留，重新派给其他代驾；不计退款、不重新收费
        ledgerService.recordCancelledBeforePickup(orderId, assignment.lng("id"), driverId, reason);
        eventService.record("ORDER", orderId, "ASSIGNMENT_CANCELLED_BY_DRIVER", orderId,
                Map.of("status", status), Map.of("status", Codes.Fulfill.PENDING_DISPATCH), reason);
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "ASSIGNMENT_CANCELLED",
                "代驾因故无法履约，正在为您重新安排，原订单与支付不受影响");

        dispatchOrder(orderId);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("assignmentEnded", true);
        view.put("message", "已解除本次承接，订单将继续派给其他代驾");
        return view;
    }

    /** 订单取消时撤销在途邀约与承接 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void releaseForCancellation(Long orderId, String reason)
    {
        dispatchMapper.revokeInvitesOfOrder(orderId);
        Row order = Row.of(orderMapper.findById(orderId));
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(orderId));
        if (!assignment.isEmpty())
        {
            dispatchMapper.endAssignment(assignment.lng("id"), Codes.EndReason.CANCELLED);
            dispatchMapper.releaseOccupancy(assignment.lng("driver_id"), assignment.lng("id"));
            orderMapper.updateActiveAssignment(orderId, null);
        }
        if (Codes.Custody.DRIVER.equals(order.str("custody_type")) && order.lng("custody_ref_id") != null)
        {
            dispatchMapper.releaseOccupancy(order.lng("custody_ref_id"), order.lng("active_assignment_id", 0L));
        }
        eventService.record("ORDER", orderId, "DISPATCH_RELEASED", orderId, null, null, reason);
    }

    // ------------------------------------------------------------------ 管理端查询

    public List<Map<String, Object>> queue()
    {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : orderMapper.dispatchQueue(100))
        {
            Row o = Row.of(r);
            JSONObject snapshot = Json.read(o.json("vehicle_snapshot"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", o.str("order_no"));
            item.put("fulfillmentStatus", o.str("fulfillment_status"));
            item.put("plateNo", snapshot.getString("plateNo"));
            item.put("stationName", o.str("station_name"));
            item.put("pickupStartsAt", ViTime.iso(o.time("pickup_starts_at")));
            item.put("pickupEndsAt", ViTime.iso(o.time("pickup_ends_at")));
            item.put("pickupWindowLabel",
                    ViTime.windowLabel(o.time("pickup_starts_at"), o.time("pickup_ends_at")));
            item.put("dispatchAt", ViTime.iso(o.time("dispatch_at")));
            item.put("totalFen", o.lng("total_fen"));
            out.add(item);
        }
        return out;
    }

    public List<Map<String, Object>> candidates(String keyword)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : dispatchMapper.candidates(keyword))
        {
            Row d = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("driverId", String.valueOf(d.lng("driver_id")));
            item.put("nickname", d.str("nickname"));
            item.put("qualificationStatus", d.str("qualification_status"));
            item.put("listeningEnabled", d.bool("listening_enabled"));
            item.put("newAssignmentEnabled", d.bool("new_assignment_enabled"));
            item.put("busy", d.str("holder_type") != null);
            item.put("holderType", d.str("holder_type"));
            item.put("lastLocationAt", ViTime.iso(d.time("captured_at")));
            out.add(item);
        }
        return out;
    }

    public Map<String, Object> dispatchConfig()
    {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("leadMinutes", config.dispatchLeadMinutes());
        view.put("inviteExpireSeconds", config.inviteExpireSeconds());
        view.put("radiusMeter", config.dispatchRadiusMeter());
        view.put("radiusSteps", config.dispatchRadiusSteps());
        view.put("totalMinutes", config.dispatchTotalMinutes());
        view.put("requireApprovedQualification", true);
        return view;
    }
}
