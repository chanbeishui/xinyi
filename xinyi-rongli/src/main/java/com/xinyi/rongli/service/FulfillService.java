package com.xinyi.rongli.service;

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
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.DispatchMapper;
import com.xinyi.rongli.mapper.FulfillMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.StationMapper;
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
 * 履约执行：代驾节点动作、四次实物交接、车况证据、结果读取与归还释放。
 *
 * <p>核心不变量：归还交接成功即在同一事务内结束承接并精确释放占用；
 * 照片提交、结果登记、切换角色都不释放司机。</p>
 */
@Service
public class FulfillService
{
    /** 每个留证节点必须齐备的六类照片（PRD 3.1「节点必拍项」） */
    private static final List<String> REQUIRED_PHOTO_TYPES =
            List.of("FRONT", "BACK", "LEFT", "RIGHT", "DASHBOARD", "INTERIOR");

    private static final Map<String, String> PHOTO_TYPE_LABEL = Map.of(
            "FRONT", "车头", "BACK", "车尾", "LEFT", "左侧", "RIGHT", "右侧",
            "DASHBOARD", "仪表盘", "INTERIOR", "内饰", "EXTRA", "补充照");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DispatchMapper dispatchMapper;

    @Autowired
    private FulfillMapper fulfillMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private EventService eventService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private BookingService bookingService;

    /** 与售后服务互为依赖（售后审核要结束承接、还车要收尾售后），用延迟代理打破环 */
    @Autowired
    @org.springframework.context.annotation.Lazy
    private AfterSaleService afterSaleService;

    // ------------------------------------------------------------------ 代驾任务

    public Map<String, Object> currentAssignment()
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfDriver(driver.lng("id")));
        if (assignment.isEmpty())
        {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("hasAssignment", false);
            return empty;
        }
        Map<String, Object> view = driverOrderView(Row.of(orderMapper.findDetailById(assignment.lng("order_id"))));
        view.put("hasAssignment", true);
        return view;
    }

    public Map<String, Object> driverOrders(String status, int pageNum, int pageSize)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        String filter = "ACTIVE".equalsIgnoreCase(status) || "ENDED".equalsIgnoreCase(status) ? status.toUpperCase() : null;
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : dispatchMapper.listAssignmentsOfDriver(driver.lng("id"), filter, offset, pageSize))
        {
            Row a = Row.of(r);
            JSONObject snapshot = Json.read(a.json("vehicle_snapshot"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", a.str("order_no"));
            item.put("fulfillmentStatus", a.str("fulfillment_status"));
            item.put("plateNo", snapshot.getString("plateNo"));
            item.put("serviceType", "检测代驾");
            item.put("acceptedAt", ViTime.iso(a.time("accepted_at")));
            item.put("endedAt", ViTime.iso(a.time("ended_at")));
            item.put("returnedAt", ViTime.iso(a.time("returned_at")));
            out.add(item);
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("rows", out);
        page.put("total", dispatchMapper.countAssignmentsOfDriver(driver.lng("id"), filter));
        page.put("pageNum", pageNum);
        page.put("pageSize", pageSize);
        return page;
    }

    public Map<String, Object> driverOrder(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        boolean mine = dispatchMapper.listAssignmentsOfDriver(driver.lng("id"), null, 0, 200).stream()
                .anyMatch(r -> orderNo.equals(Row.of(r).str("order_no")));
        if (!mine)
        {
            throw ApiException.forbidden("该订单不在您的服务记录中");
        }
        return driverOrderView(order);
    }

    /** 司机视角：不返回车主完整住址门牌、联系方式明文与客户金额明细 */
    public Map<String, Object> driverOrderView(Row order)
    {
        JSONObject snapshot = Json.read(order.json("vehicle_snapshot"));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", order.str("order_no"));
        view.put("fulfillmentStatus", order.str("fulfillment_status"));
        view.put("fulfillmentStatusLabel", bookingService.statusLabel(order.str("fulfillment_status")));
        view.put("custodyType", order.str("custody_type"));
        view.put("pickupStartsAt", ViTime.iso(order.time("pickup_starts_at")));
        view.put("pickupEndsAt", ViTime.iso(order.time("pickup_ends_at")));
        view.put("pickupWindowLabel", ViTime.shanghaiText(order.time("pickup_starts_at")) + " - "
                + ViTime.shanghaiText(order.time("pickup_ends_at")).substring(11));
        view.put("stationName", order.str("station_name"));
        view.put("stationAddress", order.str("station_address"));
        view.put("plateNo", snapshot.getString("plateNo"));
        view.put("vehicleTypeName", snapshot.getString("vehicleTypeName"));
        view.put("address", maskAddress(snapshot.getString("address")));
        view.put("contactName", snapshot.getString("contactName"));
        view.put("contactPhone", maskPhone(snapshot.getString("contactPhone")));
        view.put("version", order.lng("version"));
        view.put("allowedActions", driverActions(order.str("fulfillment_status")));
        view.put("nextHandoverType", nextHandover(order.str("fulfillment_status")));
        return view;
    }

    private List<String> driverActions(String status)
    {
        List<String> actions = new ArrayList<>();
        switch (status == null ? "" : status)
        {
            case Codes.Fulfill.ACCEPTED -> actions.add("DEPART");
            case Codes.Fulfill.GOING_PICKUP -> actions.add("ARRIVE_PICKUP");
            case Codes.Fulfill.ARRIVED_PICKUP -> actions.add("CREATE_HANDOVER");
            case Codes.Fulfill.PICKED_UP -> actions.add("ARRIVE_STATION");
            case Codes.Fulfill.ARRIVED_STATION, Codes.Fulfill.INSPECTING -> actions.add("WAIT_STATION");
            case Codes.Fulfill.INSPECTED -> actions.add("CREATE_HANDOVER");
            case Codes.Fulfill.CAR_TAKEN -> actions.add("CREATE_HANDOVER");
            default ->
            {
            }
        }
        if (Codes.Fulfill.ACCEPTED.equals(status) || Codes.Fulfill.GOING_PICKUP.equals(status)
                || Codes.Fulfill.ARRIVED_PICKUP.equals(status))
        {
            actions.add("CANCEL_ASSIGNMENT");
        }
        return actions;
    }

    private String nextHandover(String status)
    {
        if (status == null)
        {
            return null;
        }
        return switch (status)
        {
            case Codes.Fulfill.ARRIVED_PICKUP -> Codes.Handover.OWNER_TO_DRIVER;
            case Codes.Fulfill.ARRIVED_STATION -> Codes.Handover.DRIVER_TO_STATION;
            case Codes.Fulfill.INSPECTED -> Codes.Handover.STATION_TO_DRIVER;
            case Codes.Fulfill.CAR_TAKEN -> Codes.Handover.DRIVER_TO_OWNER;
            default -> null;
        };
    }

    @Transactional
    public Map<String, Object> depart(String orderNo)
    {
        return driverTransition(orderNo, Codes.Fulfill.ACCEPTED, Codes.Fulfill.GOING_PICKUP, "DRIVER_DEPARTED",
                "代驾已出发前往接车");
    }

    @Transactional
    public Map<String, Object> arrivePickup(String orderNo)
    {
        return driverTransition(orderNo, Codes.Fulfill.GOING_PICKUP, Codes.Fulfill.ARRIVED_PICKUP, "DRIVER_ARRIVED_PICKUP",
                "代驾已到达接车点，请与车主核对车辆并完成交接");
    }

    @Transactional
    public Map<String, Object> arriveStation(String orderNo)
    {
        Map<String, Object> view = driverTransition(orderNo, Codes.Fulfill.PICKED_UP, Codes.Fulfill.ARRIVED_STATION,
                "DRIVER_ARRIVED_STATION", "车辆已到达检测点，等待站点收车");
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        // 站点侧通过工作台待办看到待收车订单；此处只记录事件，不伪造站点接收
        eventService.publish("ORDER", order.lng("id"), "CAR_ARRIVED_STATION", Map.of("orderNo", orderNo));
        return view;
    }

    private Map<String, Object> driverTransition(String orderNo, String from, String to, String action, String message)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row order = requireDriverOrder(orderNo, driver.lng("id"));
        if (!from.equals(order.str("fulfillment_status")))
        {
            throw ApiException.stateConflict("当前状态不可执行该操作（需要 " + bookingService.statusLabel(from) + "）");
        }
        if (orderMapper.casStatusByState(order.lng("id"), from, to) == 0)
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "订单状态已变化，请刷新后重试");
        }
        eventService.record("ORDER", order.lng("id"), action, order.lng("id"), Map.of("status", from), Map.of("status", to), null);
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, order.lng("id"), action, message);
        return driverOrderView(Row.of(orderMapper.findDetailById(order.lng("id"))));
    }

    // ------------------------------------------------------------------ 交接

    @Transactional
    public Map<String, Object> createHandover(String orderNo, String handoverType)
    {
        MiniPrincipal p = MiniContext.require();
        if (handoverType == null || handoverType.isBlank())
        {
            throw ApiException.validation("缺少交接类型");
        }
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Long orderId = order.lng("id");
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(orderId));
        if (assignment.isEmpty())
        {
            throw ApiException.stateConflict("订单当前没有生效承接");
        }
        Long driverId = assignment.lng("driver_id");
        Long ownerAccountId = order.lng("owner_account_id");

        Long holder;
        Long verifier;
        String expected;
        switch (handoverType)
        {
            case Codes.Handover.OWNER_TO_DRIVER ->
            {
                expected = Codes.Fulfill.ARRIVED_PICKUP;
                holder = ownerAccountId;
                verifier = driverAccountId(driverId);
            }
            case Codes.Handover.DRIVER_TO_STATION ->
            {
                expected = Codes.Fulfill.ARRIVED_STATION;
                holder = driverAccountId(driverId);
                verifier = p.isStationRole() ? p.accountId : null;
                if (verifier == null)
                {
                    verifier = stationEmployeeAccount(p);
                }
            }
            case Codes.Handover.STATION_TO_DRIVER ->
            {
                expected = Codes.Fulfill.INSPECTED;
                holder = p.isStationRole() ? p.accountId : null;
                verifier = driverAccountId(driverId);
                if (holder == null)
                {
                    holder = stationEmployeeAccount(p);
                }
            }
            case Codes.Handover.DRIVER_TO_OWNER ->
            {
                expected = Codes.Fulfill.CAR_TAKEN;
                // PRD D22/D23：归还时由代驾出码、车主核验后确认收车
                holder = driverAccountId(driverId);
                verifier = ownerAccountId;
            }
            default -> throw ApiException.validation("未知交接类型");
        }
        if (verifier == null)
        {
            throw ApiException.forbidden("需要获授权检测员工完成站点侧交接");
        }
        String status = order.str("fulfillment_status");
        boolean cancelReturn = Codes.Cancel.APPROVED_WAIT_RETURN.equals(order.str("cancel_status"));
        if (!expected.equals(status) && !(cancelReturn
                && (Codes.Fulfill.INSPECTED.equals(status) || Codes.Fulfill.INSPECTING.equals(status)
                        || Codes.Fulfill.CAR_TAKEN.equals(status))))
        {
            throw ApiException.stateConflict("当前状态不可发起该交接（需要 " + bookingService.statusLabel(expected) + "）");
        }
        // 只有当事方可以发起
        if (!Objects.equals(p.accountId, holder) && !Objects.equals(p.accountId, verifier))
        {
            throw ApiException.forbidden("您不是本次交接的参与方");
        }
        // 同人自证防护：交付确认人与接收确认人不能是同一真实账号
        if (Objects.equals(holder, verifier))
        {
            throw ApiException.unprocessable(Codes.Error.STATE_CONFLICT, "同一账号不能同时作为交付方与接收方，请联系另一名同事确认");
        }

        Map<String, Object> existing = fulfillMapper.findLatestHandover(orderId, assignment.lng("id"), handoverType);
        if (existing != null && "PENDING".equals(Row.of(existing).str("status")))
        {
            return handoverView(Row.of(existing), p.accountId);
        }

        int generation = existing == null ? 1 : Row.of(existing).intg("generation_no", 0) + 1;
        String code = crypto.randomHandoverCode();
        String macContext = orderId + ":" + assignment.lng("id") + ":" + handoverType + ":" + generation + ":" + code;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", orderId);
        row.put("assignmentId", assignment.lng("id"));
        row.put("handoverType", handoverType);
        row.put("holderAccountId", holder);
        row.put("verifierAccountId", verifier);
        row.put("codeMac", crypto.hmac(macContext));
        row.put("codeCipher", crypto.encrypt(code));
        row.put("generationNo", generation);
        row.put("expiresAt", ViTime.now().plusMinutes(30));
        fulfillMapper.insertHandover(row);
        Long handoverId = ((Number) row.get("id")).longValue();
        eventService.record("ORDER", orderId, "HANDOVER_CREATED", orderId, null,
                Map.of("handoverId", String.valueOf(handoverId), "type", handoverType), null);
        return handoverView(Row.of(fulfillMapper.findHandover(handoverId)), p.accountId);
    }

    /** 持码方读取明文码：仅持码方本人、仅 PENDING 状态 */
    public Map<String, Object> handoverSecret(String orderNo, Long handoverId)
    {
        MiniPrincipal p = MiniContext.require();
        Row handover = requireHandover(orderNo, handoverId);
        if (!Objects.equals(p.accountId, handover.lng("holder_account_id")))
        {
            throw ApiException.forbidden("只有持码方可以查看交接码");
        }
        if (!"PENDING".equals(handover.str("status")))
        {
            throw ApiException.stateConflict("交接码已失效");
        }
        if (handover.time("expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.stateConflict("交接码已过期，请重新发起交接");
        }
        String code = crypto.decrypt((byte[]) handover.get("code_cipher"));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("handoverId", String.valueOf(handoverId));
        view.put("code", code);
        view.put("expiresAt", ViTime.iso(handover.time("expires_at")));
        view.put("displayHint", "请将 4 位数字口述或出示给对方完成核验，不要转发截图");
        return view;
    }

    public Map<String, Object> handoverDetail(String orderNo, Long handoverId)
    {
        MiniPrincipal p = MiniContext.require();
        Row handover = requireHandover(orderNo, handoverId);
        return handoverView(handover, p.accountId);
    }

    /**
     * 核验并完成实物交接：码校验与状态推进在同一事务，码只在此成功时被消费。
     */
    @Transactional
    public Map<String, Object> confirmHandover(String orderNo, Long handoverId, DriverDtos.HandoverConfirmReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row handover = requireHandover(orderNo, handoverId);
        if (!Objects.equals(p.accountId, handover.lng("verifier_account_id")))
        {
            throw ApiException.forbidden("只有核验方可以提交交接码");
        }
        if (!"PENDING".equals(handover.str("status")))
        {
            throw ApiException.stateConflict("该交接已处理或已失效");
        }
        if (handover.time("expires_at").isBefore(ViTime.now()))
        {
            fulfillMapper.expireHandover(handoverId);
            throw ApiException.stateConflict("交接码已过期，请重新发起");
        }
        String guardKey = handover.lng("order_id") + ":" + handover.lng("assignment_id") + ":" + handover.str("handover_type");
        Map<String, Object> guard = fulfillMapper.findAttemptGuard(guardKey);
        if (guard != null && Row.of(guard).time("locked_at") != null)
        {
            throw ApiException.conflict(Codes.Error.HANDOVER_LOCKED, "本阶段交接已连续失败 3 次并锁定，请联系运营人工核验");
        }
        if (req == null || req.code == null || !req.code.matches("\\d{4}"))
        {
            throw ApiException.validation("请输入 4 位交接码");
        }
        String macContext = handover.lng("order_id") + ":" + handover.lng("assignment_id") + ":"
                + handover.str("handover_type") + ":" + handover.intg("generation_no") + ":" + req.code;
        byte[] expected = crypto.hmac(macContext);
        if (!java.util.Arrays.equals(expected, (byte[]) handover.get("code_mac")))
        {
            fulfillMapper.bumpAttemptGuard(guardKey);
            Map<String, Object> after = fulfillMapper.findAttemptGuard(guardKey);
            int failed = after == null ? 1 : Row.of(after).intg("failed_attempts", 1);
            throw ApiException.conflict(Codes.Error.HANDOVER_CODE_INVALID,
                    "交接码不正确，本次失败已记录（" + failed + "/3）");
        }

        Long evidenceGroupId = null;
        if (req.evidence != null && req.evidence.nodeNo != null)
        {
            evidenceGroupId = createEvidenceInternal(handover.lng("order_id"), handover.lng("assignment_id"),
                    req.evidence, 2);
        }
        if (fulfillMapper.confirmHandover(handoverId, evidenceGroupId) == 0)
        {
            throw ApiException.stateConflict("交接已被处理，请刷新");
        }
        if (evidenceGroupId != null)
        {
            fulfillMapper.sealEvidence(evidenceGroupId);
        }

        Long orderId = handover.lng("order_id");
        Row order = Row.of(orderMapper.findDetailById(orderId));
        Long assignmentId = handover.lng("assignment_id");
        String type = handover.str("handover_type");
        switch (type)
        {
            case Codes.Handover.OWNER_TO_DRIVER ->
            {
                orderMapper.casStatusByState(orderId, Codes.Fulfill.ARRIVED_PICKUP, Codes.Fulfill.PICKED_UP);
                orderMapper.updateCustody(orderId, Codes.Custody.DRIVER, driverIdOfAssignment(assignmentId));
                eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "PICKED_UP",
                        "车辆已交接给代驾，正在送往检测点");
            }
            case Codes.Handover.DRIVER_TO_STATION ->
            {
                orderMapper.casStatusByState(orderId, Codes.Fulfill.ARRIVED_STATION, Codes.Fulfill.INSPECTING);
                orderMapper.updateCustody(orderId, Codes.Custody.STATION, order.lng("station_id"));
                // 站点实际接收即消耗容量
                for (Map<String, Object> r : orderMapper.listReservations(orderId))
                {
                    Row res = Row.of(r);
                    if (Codes.Reservation.CONFIRMED.equals(res.str("status"))
                            && orderMapper.casReservation(res.lng("id"), Codes.Reservation.CONFIRMED, Codes.Reservation.CONSUMED) > 0)
                    {
                        stationMapper.consumeQuota(res.lng("slot_id"), res.intg("quantity", 1));
                    }
                }
                eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "CAR_RECEIVED",
                        "检测点已收车，开始检测");
            }
            case Codes.Handover.STATION_TO_DRIVER ->
            {
                orderMapper.casStatusByState(orderId, Codes.Fulfill.INSPECTED, Codes.Fulfill.CAR_TAKEN);
                orderMapper.updateCustody(orderId, Codes.Custody.DRIVER, driverIdOfAssignment(assignmentId));
            }
            case Codes.Handover.DRIVER_TO_OWNER -> completeReturn(order, assignmentId);
            default ->
            {
            }
        }
        eventService.record("ORDER", orderId, "HANDOVER_CONFIRMED", orderId, null,
                Map.of("handoverId", String.valueOf(handoverId), "type", type), null);
        eventService.publish("ORDER", orderId, "HANDOVER_CONFIRMED", Map.of("orderNo", order.str("order_no"), "type", type));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("handoverId", String.valueOf(handoverId));
        result.put("confirmed", true);
        result.put("fulfillmentStatus", Row.of(orderMapper.findById(orderId)).str("fulfillment_status"));
        result.put("custodyType", Row.of(orderMapper.findById(orderId)).str("custody_type"));
        return result;
    }

    /**
     * 实际归还：订单进入待服务确认、司机立即释放、写入代驾费用台账；
     * 取消已批准的单在此正式办结取消，不等待退款到账。
     */
    private void completeReturn(Row order, Long assignmentId)
    {
        Long orderId = order.lng("id");
        LocalDateTime autoConfirmAt = ViTime.now().plusHours(config.autoConfirmHours());
        orderMapper.markReturned(orderId, autoConfirmAt);
        orderMapper.updateCustody(orderId, Codes.Custody.OWNER, order.lng("owner_account_id"));

        Row assignment = Row.of(dispatchMapper.findAssignment(assignmentId));
        dispatchMapper.endAssignment(assignmentId, Codes.EndReason.RETURNED);
        dispatchMapper.releaseOccupancy(assignment.lng("driver_id"), assignmentId);
        orderMapper.updateActiveAssignment(orderId, null);

        boolean cancelReturn = Codes.Cancel.APPROVED_WAIT_RETURN.equals(order.str("cancel_status"));
        if (cancelReturn)
        {
            orderMapper.casStatusByState(orderId, Codes.Fulfill.CAR_TAKEN, Codes.Fulfill.CANCELLED);
            orderMapper.updateCancelStatus(orderId, Codes.Cancel.COMPLETED);
            orderMapper.markCancelled(orderId);
        }
        else
        {
            orderMapper.casStatusByState(orderId, Codes.Fulfill.CAR_TAKEN, Codes.Fulfill.WAIT_CONFIRM);
        }

        ledgerService.recordReturned(orderId, assignmentId, assignment.lng("driver_id"), cancelReturn);

        // 售后单若在等实还，还车成功即在同一事务内完成取消与退款
        afterSaleService.onVehicleReturned(orderId);

        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "VEHICLE_RETURNED",
                cancelReturn ? "车辆已返还，本次订单已取消办结" : "车辆已归还，请在 24 小时内确认服务");
        eventService.notify(driverAccountId(assignment.lng("driver_id")), Codes.Role.DRIVER, orderId, "ASSIGNMENT_RELEASED",
                "车辆已实际归还，本单任务结束，可以接收新的邀约");
        eventService.record("ORDER", orderId, cancelReturn ? "CANCEL_RETURNED" : "VEHICLE_RETURNED", orderId,
                Map.of("custody", Codes.Custody.DRIVER), Map.of("custody", Codes.Custody.OWNER), null);
    }

    // ------------------------------------------------------------------ 证据

    @Transactional
    public Map<String, Object> createEvidence(String orderNo, Integer nodeNo, Integer mileageKm)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row order = requireDriverOrder(orderNo, driver.lng("id"));
        if (nodeNo == null || nodeNo < 1 || nodeNo > 4)
        {
            throw ApiException.validation("照片节点必须在 1-4 之间");
        }
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(order.lng("id")));
        if (assignment.isEmpty())
        {
            throw ApiException.stateConflict("订单当前没有生效承接，无法登记证据");
        }
        DriverDtos.EvidenceCreateReq req = new DriverDtos.EvidenceCreateReq();
        req.nodeNo = nodeNo;
        req.mileageKm = mileageKm == null ? null : mileageKm.longValue();
        Long groupId = createEvidenceInternal(order.lng("id"), assignment.lng("id"), req, null);
        return evidenceView(groupId);
    }

    private Long createEvidenceInternal(Long orderId, Long assignmentId, DriverDtos.EvidenceCreateReq req, Integer sealNode)
    {
        int nodeNo = req.nodeNo == null ? 1 : req.nodeNo;
        Map<String, Object> existing = fulfillMapper.findEvidenceGroup(assignmentId, nodeNo);
        if (existing != null)
        {
            return Row.of(existing).lng("id");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", orderId);
        row.put("assignmentId", assignmentId);
        row.put("nodeNo", nodeNo);
        fulfillMapper.insertEvidenceGroup(row);
        Long groupId = ((Number) row.get("id")).longValue();
        if (req.mileageKm != null)
        {
            fulfillMapper.updateMileage(groupId, java.math.BigDecimal.valueOf(req.mileageKm),
                    req.mileageSource == null ? "MANUAL" : req.mileageSource, req.mileageReason);
        }
        return groupId;
    }

    @Transactional
    public Map<String, Object> addPhoto(String orderNo, Long groupId, DriverDtos.PhotoReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row order = requireDriverOrder(orderNo, driver.lng("id"));
        Row group = Row.of(fulfillMapper.findGroupById(groupId));
        if (group.isEmpty() || !Objects.equals(group.lng("order_id"), order.lng("id")))
        {
            throw ApiException.notFound("证据组不存在");
        }
        if ("SEALED".equals(group.str("status")))
        {
            throw ApiException.stateConflict("证据组已封存，只能追加更正说明");
        }
        if (req == null || req.photoType == null || req.fileId == null)
        {
            throw ApiException.validation("缺少照片类型或文件");
        }
        Row file = Row.of(fulfillMapper.findFile(req.fileId));
        if (file.isEmpty() || !"READY".equals(file.str("status")))
        {
            throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "文件尚未通过服务端校验");
        }
        String type = req.photoType.toUpperCase();
        if (!List.of("FRONT", "BACK", "LEFT", "RIGHT", "DASHBOARD", "INTERIOR", "EXTRA").contains(type))
        {
            throw ApiException.validation("未知照片类型");
        }
        String captureId = req.clientCaptureId == null || req.clientCaptureId.isBlank()
                ? type + "-" + System.nanoTime() : req.clientCaptureId;
        int revision = fulfillMapper.listPhotos(groupId).size() + 1;
        if (!"EXTRA".equals(type))
        {
            fulfillMapper.supersedePhotos(groupId, type);
        }
        JSONObject location = new JSONObject();
        if (req.lng != null && req.lat != null)
        {
            location.put("lng", req.lng);
            location.put("lat", req.lat);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("groupId", groupId);
        row.put("photoType", type);
        row.put("fileId", req.fileId);
        row.put("captureId", captureId);
        row.put("revision", revision);
        row.put("capturedAt", req.capturedAt == null ? ViTime.now() : ViTime.parseFlexible(req.capturedAt));
        row.put("location", location.isEmpty() ? null : crypto.encrypt(location.toJSONString()));
        fulfillMapper.insertPhoto(row);
        return evidenceView(groupId);
    }

    @Transactional
    public Map<String, Object> updateMileage(String orderNo, Long groupId, DriverDtos.MileageReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row order = requireDriverOrder(orderNo, driver.lng("id"));
        Row group = Row.of(fulfillMapper.findGroupById(groupId));
        if (group.isEmpty() || !Objects.equals(group.lng("order_id"), order.lng("id")))
        {
            throw ApiException.notFound("证据组不存在");
        }
        if ("SEALED".equals(group.str("status")))
        {
            throw ApiException.stateConflict("证据组已封存，不能修改里程");
        }
        if (req == null || req.mileageKm == null || req.mileageKm.isBlank())
        {
            throw ApiException.validation("请填写仪表里程");
        }
        java.math.BigDecimal km;
        try
        {
            km = new java.math.BigDecimal(req.mileageKm);
        }
        catch (NumberFormatException e)
        {
            throw ApiException.validation("里程格式不正确");
        }
        if (km.signum() < 0 || km.scale() > 1)
        {
            throw ApiException.validation("里程必须为非负且最多一位小数");
        }
        fulfillMapper.updateMileage(groupId, km, req.source == null ? "MANUAL" : req.source, req.reason);
        eventService.record("ORDER", order.lng("id"), "MILEAGE_UPDATED", order.lng("id"),
                Map.of("groupId", String.valueOf(groupId)), Map.of("mileageKm", km.toPlainString()), req.reason);
        return evidenceView(groupId);
    }

    @Transactional
    public Map<String, Object> markEvidenceReady(String orderNo, Long groupId)
    {
        MiniPrincipal p = MiniContext.require();
        Row driver = requireDriver(p.accountId);
        Row order = requireDriverOrder(orderNo, driver.lng("id"));
        Row group = Row.of(fulfillMapper.findGroupById(groupId));
        if (group.isEmpty() || !Objects.equals(group.lng("order_id"), order.lng("id")))
        {
            throw ApiException.notFound("证据组不存在");
        }
        // PRD 3.1 / D12：每个节点六类照片（前、后、左、右、仪表盘、内饰）齐全方可提交
        java.util.Set<String> got = new java.util.HashSet<>(fulfillMapper.listActivePhotoTypes(groupId));
        List<String> missing = new ArrayList<>();
        for (String type : REQUIRED_PHOTO_TYPES)
        {
            if (!got.contains(type))
            {
                missing.add(PHOTO_TYPE_LABEL.get(type));
            }
        }
        if (!missing.isEmpty())
        {
            throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED,
                    "照片不完整，还缺：" + String.join("、", missing));
        }
        if (group.dec("mileage_km") == null)
        {
            throw ApiException.unprocessable(Codes.Error.VALIDATION_FAILED, "请先填写仪表里程");
        }
        fulfillMapper.markEvidenceReady(groupId);
        return evidenceView(groupId);
    }

    public Map<String, Object> evidenceList(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        boolean owner = Objects.equals(order.lng("owner_account_id"), p.accountId);
        boolean driver = false;
        Row driverProfile = Row.of(accountMapper.findDriverProfile(p.accountId));
        if (!driverProfile.isEmpty())
        {
            driver = dispatchMapper.listAssignmentsOfDriver(driverProfile.lng("id"), null, 0, 200).stream()
                    .anyMatch(r -> orderNo.equals(Row.of(r).str("order_no")));
        }
        if (!owner && !driver)
        {
            throw ApiException.forbidden("无权查看该订单证据");
        }
        List<Map<String, Object>> groups = new ArrayList<>();
        for (Map<String, Object> h : fulfillMapper.listHandoversOfOrder(order.lng("id")))
        {
            Row handover = Row.of(h);
            Long groupId = handover.lng("evidence_group_id");
            if (groupId != null)
            {
                groups.add(evidenceView(groupId));
            }
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("groups", groups);
        return view;
    }

    private Map<String, Object> evidenceView(Long groupId)
    {
        Row group = Row.of(fulfillMapper.findGroupById(groupId));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("groupId", String.valueOf(groupId));
        view.put("nodeNo", group.intg("node_no"));
        view.put("nodeLabel", nodeLabel(group.intg("node_no")));
        view.put("status", group.str("status"));
        view.put("mileageKm", group.dec("mileage_km") == null ? null : group.dec("mileage_km").toPlainString());
        view.put("mileageSource", group.str("mileage_source"));
        view.put("sealedAt", ViTime.iso(group.time("sealed_at")));
        List<Map<String, Object>> photos = new ArrayList<>();
        for (Map<String, Object> p : fulfillMapper.listPhotos(groupId))
        {
            Row photo = Row.of(p);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("photoId", String.valueOf(photo.lng("id")));
            item.put("photoType", photo.str("photo_type"));
            item.put("fileId", String.valueOf(photo.lng("file_id")));
            item.put("capturedAt", ViTime.iso(photo.time("captured_at")));
            item.put("receivedAt", ViTime.iso(photo.time("received_at")));
            photos.add(item);
        }
        view.put("photos", photos);
        view.put("photoCount", photos.size());
        return view;
    }

    private String nodeLabel(Integer nodeNo)
    {
        if (nodeNo == null)
        {
            return "";
        }
        return switch (nodeNo)
        {
            case 1 -> "接车";
            case 2 -> "到站";
            case 3 -> "取车";
            case 4 -> "归还";
            default -> "";
        };
    }

    // ------------------------------------------------------------------ 辅助

    public Row requireDriver(Long accountId)
    {
        Row driver = Row.of(accountMapper.findDriverProfile(accountId));
        if (driver.isEmpty())
        {
            throw ApiException.forbidden("尚未建立代驾档案");
        }
        return driver;
    }

    private Row requireDriverOrder(String orderNo, Long driverId)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(order.lng("id")));
        if (assignment.isEmpty() || !Objects.equals(assignment.lng("driver_id"), driverId))
        {
            throw ApiException.forbidden("该订单不在您的在手任务中");
        }
        return order;
    }

    private Row requireHandover(String orderNo, Long handoverId)
    {
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        Row handover = Row.of(fulfillMapper.findHandover(handoverId));
        if (handover.isEmpty() || !Objects.equals(handover.lng("order_id"), order.lng("id")))
        {
            throw ApiException.notFound("交接记录不存在");
        }
        return handover;
    }

    // ------------------------------------------------------------------ 联系通道

    /**
     * 履约期内按视角获取短期联系通道。
     *
     * <p>号码不在订单详情里常驻展示，只在履约窗口内按视角短期下发：
     * 车主看到代驾、代驾看到车主、检测员工看到双方；未进入履约窗口时一律脱敏。
     * 每次访问都写审计流水，便于回溯号码被谁在何时看过。</p>
     */
    public Map<String, Object> contactAccess(String orderNo, String purpose)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = Row.of(orderMapper.findDetailByNo(orderNo));
        if (order.isEmpty())
        {
            throw ApiException.notFound("订单不存在");
        }
        String status = order.str("fulfillment_status");
        Long ownerAccountId = order.lng("owner_account_id");
        Row assignment = Row.of(dispatchMapper.findActiveAssignmentOfOrder(order.lng("id")));
        Long driverId = assignment.isEmpty() ? null : assignment.lng("driver_id");
        Long driverAccountId = driverId == null ? null : driverAccountId(driverId);
        boolean isOwner = Objects.equals(p.accountId, ownerAccountId);
        boolean isDriver = driverAccountId != null && Objects.equals(p.accountId, driverAccountId);
        boolean isStation = p.stationId != null && Objects.equals(p.stationId, order.lng("station_id"));
        if (!isOwner && !isDriver && !isStation)
        {
            throw ApiException.forbidden("您不在本单履约范围内，无法获取联系方式");
        }
        boolean inWindow = List.of(Codes.Fulfill.ACCEPTED, Codes.Fulfill.GOING_PICKUP,
                Codes.Fulfill.ARRIVED_PICKUP, Codes.Fulfill.PICKED_UP, Codes.Fulfill.ARRIVED_STATION,
                Codes.Fulfill.INSPECTING, Codes.Fulfill.INSPECTED, Codes.Fulfill.CAR_TAKEN).contains(status);

        List<Map<String, Object>> contacts = new ArrayList<>();
        if (isOwner || isStation)
        {
            if (driverAccountId != null)
            {
                Row acc = Row.of(accountMapper.findAccount(driverAccountId));
                contacts.add(contact("DRIVER", acc.str("nickname"), phoneOf(acc), isStation || inWindow));
            }
        }
        if (isDriver || isStation)
        {
            Row acc = Row.of(accountMapper.findAccount(ownerAccountId));
            contacts.add(contact("OWNER", acc.str("nickname"), phoneOf(acc), isStation || inWindow));
        }
        eventService.record("ORDER", order.lng("id"), "CONTACT_ACCESS", order.lng("id"), null,
                Map.of("viewer", isOwner ? "OWNER" : (isDriver ? "DRIVER" : "STATION"),
                        "full", String.valueOf(isStation || inWindow)),
                purpose == null ? "履约联系" : purpose);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("purpose", purpose == null ? "履约联系" : purpose);
        view.put("contacts", contacts);
        view.put("expiresAt", ViTime.iso(ViTime.now().plusMinutes(10)));
        view.put("hint", inWindow || isStation
                ? "号码仅用于本次履约沟通，请勿保存或转发"
                : "尚未进入履约窗口，号码做了脱敏处理；接单后可查看完整号码");
        return view;
    }

    private Map<String, Object> contact(String role, String name, String phone, boolean full)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("role", role);
        item.put("roleLabel", "DRIVER".equals(role) ? "代驾" : "车主");
        item.put("name", name);
        item.put("phone", full ? phone : maskPhone(phone));
        item.put("masked", !full);
        return item;
    }

    private String phoneOf(Row account)
    {
        Object cipher = account.get("phone_cipher");
        if (cipher == null)
        {
            return null;
        }
        try
        {
            return crypto.decrypt((byte[]) cipher);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // ------------------------------------------------------------------ 交接视图

    /**
     * 交接单对外视图。交接码明文一律不在此返回：只有持码方经 {@link #handoverSecret} 单独读取。
     */
    private Map<String, Object> handoverView(Row handover, Long accountId)
    {
        String type = handover.str("handover_type");
        String status = handover.str("status");
        boolean holder = Objects.equals(accountId, handover.lng("holder_account_id"));
        boolean verifier = Objects.equals(accountId, handover.lng("verifier_account_id"));
        boolean expired = handover.time("expires_at") != null
                && handover.time("expires_at").isBefore(ViTime.now());

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("handoverId", String.valueOf(handover.lng("id")));
        view.put("orderId", String.valueOf(handover.lng("order_id")));
        view.put("assignmentId", String.valueOf(handover.lng("assignment_id")));
        view.put("handoverType", type);
        view.put("handoverTypeLabel", handoverTypeLabel(type));
        view.put("status", status);
        view.put("statusLabel", handoverStatusLabel(status));
        view.put("generationNo", handover.intg("generation_no"));
        view.put("expiresAt", ViTime.iso(handover.time("expires_at")));
        view.put("expired", expired);
        view.put("confirmedAt", ViTime.iso(handover.time("confirmed_at")));
        view.put("evidenceGroupId", handover.lng("evidence_group_id") == null ? null
                : String.valueOf(handover.lng("evidence_group_id")));
        view.put("mySide", holder ? "HOLDER" : (verifier ? "VERIFIER" : "OBSERVER"));
        // 权限口径与后端校验完全一致，避免前端出现"看得到按钮却必然失败"
        view.put("canViewCode", holder && "PENDING".equals(status) && !expired);
        view.put("canConfirm", verifier && "PENDING".equals(status) && !expired);
        if (holder && "PENDING".equals(status))
        {
            view.put("actionHint", "请把 4 位交接码告知对方，由对方在" + nextStepHint(type) + "核验");
        }
        else if (verifier && "PENDING".equals(status))
        {
            view.put("actionHint", "请向对方索要 4 位交接码后在" + nextStepHint(type) + "核验");
        }
        else if (expired)
        {
            view.put("actionHint", "交接码已过期，请重新发起交接");
        }
        else
        {
            view.put("actionHint", "");
        }
        return view;
    }

    private String handoverTypeLabel(String type)
    {
        if (type == null)
        {
            return "";
        }
        return switch (type)
        {
            case Codes.Handover.OWNER_TO_DRIVER -> "车主交车给代驾";
            case Codes.Handover.DRIVER_TO_STATION -> "代驾交车给检测站";
            case Codes.Handover.STATION_TO_DRIVER -> "检测站交车给代驾";
            case Codes.Handover.DRIVER_TO_OWNER -> "代驾还车给车主";
            default -> type;
        };
    }

    private String handoverStatusLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case "PENDING" -> "待核验";
            case "CONFIRMED" -> "已完成";
            case "LOCKED" -> "已锁定";
            case "EXPIRED" -> "已过期";
            default -> status;
        };
    }

    private String nextStepHint(String type)
    {
        return switch (type == null ? "" : type)
        {
            case Codes.Handover.OWNER_TO_DRIVER -> "取车环节";
            case Codes.Handover.DRIVER_TO_STATION -> "送站环节";
            case Codes.Handover.STATION_TO_DRIVER -> "取车离站环节";
            case Codes.Handover.DRIVER_TO_OWNER -> "还车环节";
            default -> "交接环节";
        };
    }

    private Long driverAccountId(Long driverId)
    {        Row driver = Row.of(accountMapper.findDriverById(driverId));
        return driver.isEmpty() ? null : driver.lng("account_id");
    }

    private Long driverIdOfAssignment(Long assignmentId)
    {
        Row assignment = Row.of(dispatchMapper.findAssignment(assignmentId));
        return assignment.isEmpty() ? null : assignment.lng("driver_id");
    }

    /** 站点员工账号：优先取当前会话账号；后台可指定 */
    private Long stationEmployeeAccount(MiniPrincipal p)
    {
        if (p.linkedSysUserId == null)
        {
            return null;
        }
        return p.accountId;
    }

    private String maskPhone(String phone)
    {
        if (phone == null || phone.length() < 7)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskAddress(String address)
    {
        if (address == null || address.length() <= 12)
        {
            return address;
        }
        return address.substring(0, address.length() - 6) + "******";
    }
}
