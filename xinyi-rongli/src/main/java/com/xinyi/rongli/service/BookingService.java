package com.xinyi.rongli.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xinyi.common.utils.DictUtils;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.OrderMapper;
import com.xinyi.rongli.mapper.PayMapper;
import com.xinyi.rongli.mapper.PriceMapper;
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
 * 车主预约主流程：候选时段、可服务校验与报价、建单预占、支付、关单与服务确认。
 *
 * <p>金额一律以整数分保存与计算；客户端只提交 quoteId 与业务输入，
 * 不接受金额、站点归属或操作者身份。</p>
 */
@Service
public class BookingService
{
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final int QUOTE_TTL_MINUTES = 15;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PriceMapper priceMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PayMapper payMapper;

    @Autowired
    private BizConfig config;

    @Autowired
    private Crypto crypto;

    @Autowired
    private EventService eventService;

    @Autowired
    private DispatchService dispatchService;

    // ------------------------------------------------------------------ 时段

    /**
     * 候选上门时段：按北京时间生成未来 7 天三段窗口，容量不足或未配置的不返回可售状态。
     */
    @Transactional
    public List<Map<String, Object>> slots()
    {
        ensurePickupSlots();
        LocalDateTime from = ViTime.now();
        LocalDateTime to = from.plusDays(8);
        List<Map<String, Object>> rows = stationMapper.listSlots("PICKUP", null, from, to);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows)
        {
            Row row = Row.of(r);
            int remaining = row.intg("total_quota", 0) - row.intg("held_quota", 0)
                    - row.intg("confirmed_quota", 0) - row.intg("consumed_quota", 0);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("slotId", String.valueOf(row.lng("id")));
            item.put("startsAt", ViTime.iso(row.time("starts_at")));
            item.put("endsAt", ViTime.iso(row.time("ends_at")));
            item.put("label", ViTime.shanghaiText(row.time("starts_at")) + " - "
                    + ViTime.shanghaiText(row.time("ends_at")).substring(11));
            item.put("remaining", Math.max(remaining, 0));
            item.put("selectable", row.bool("enabled") && remaining > 0
                    && row.time("starts_at").isAfter(ViTime.now().plusMinutes(30)));
            out.add(item);
        }
        return out;
    }

    /** 可见站点目录：登录后车主选站与员工切换站点共用，只返回启用站点 */
    public List<Map<String, Object>> visibleStations()
    {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> s : stationMapper.listEnabled())
        {
            Row st = Row.of(s);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stationId", String.valueOf(st.lng("id")));
            item.put("name", st.str("name"));
            item.put("address", st.str("address"));
            item.put("lng", st.dec("lng"));
            item.put("lat", st.dec("lat"));
            item.put("serviceRadiusMeter", st.intg("service_radius_meter"));
            item.put("supportedVehicleCodes", Json.read(st.json("supported_vehicle_codes")));
            item.put("businessHours", Json.read(st.json("business_hours")));
            out.add(item);
        }
        return out;
    }

    private void ensurePickupSlots()    {
        LocalDate today = ViTime.shanghaiDate(ViTime.now());
        int[][] windows = { { 9, 11 }, { 13, 15 }, { 15, 17 } };
        for (int day = 0; day < 8; day++)
        {
            LocalDate date = today.plusDays(day);
            for (int[] w : windows)
            {
                LocalDateTime startSh = date.atTime(w[0], 0);
                LocalDateTime endSh = date.atTime(w[1], 0);
                LocalDateTime start = ViTime.fromShanghai(startSh);
                LocalDateTime end = ViTime.fromShanghai(endSh);
                if (stationMapper.findSlotExact("PICKUP", "CITY", start, end) != null)
                {
                    continue;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("quotaType", "PICKUP");
                row.put("scopeKey", "CITY");
                row.put("stationId", null);
                row.put("startsAt", start);
                row.put("endsAt", end);
                row.put("totalQuota", 10);
                stationMapper.insertSlot(row);
            }
        }
    }

    // ------------------------------------------------------------------ 报价

    @Transactional
    public Map<String, Object> quote(MiniDtos.QuoteReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null)
        {
            throw ApiException.validation("缺少报价参数");
        }
        String vehicleType = req.vehicleTypeCode;
        String plateNo = req.plateNo;
        if (req.vehicleId != null)
        {
            Map<String, Object> vehicle = vehicleMapper.findOwned(req.vehicleId, p.accountId);
            if (vehicle == null)
            {
                throw ApiException.notFound("车辆不存在");
            }
            vehicleType = Row.of(vehicle).str("vehicle_type_code");
            plateNo = Row.of(vehicle).str("plate_no");
        }
        if (vehicleType == null || vehicleType.isBlank())
        {
            throw ApiException.validation("请先选择车型");
        }

        JSONObject address = resolveAddress(p.accountId, req.addressId, req.addressDraft);
        if (address == null)
        {
            throw ApiException.validation("请先填写上门接车地址");
        }
        double lng = address.getDoubleValue("lng");
        double lat = address.getDoubleValue("lat");

        Map<String, Object> station = matchStation(vehicleType, lng, lat);
        Row st = Row.of(station);
        Long stationId = st.lng("id");

        Map<String, Object> slot = req.pickupSlotId == null ? null : stationMapper.findSlot(req.pickupSlotId);
        if (slot == null)
        {
            throw ApiException.validation("请先选择上门接车时段");
        }
        Row slotRow = Row.of(slot);
        if (!"PICKUP".equals(slotRow.str("quota_type")) || !slotRow.bool("enabled"))
        {
            throw ApiException.unprocessable(Codes.Error.NOT_SERVICEABLE, "所选时段不可预约，请重新选择");
        }
        if (slotRow.time("starts_at").isBefore(ViTime.now().plusMinutes(30)))
        {
            throw ApiException.unprocessable(Codes.Error.NOT_SERVICEABLE, "所选时段过近，请选择更晚的时间");
        }

        String inspectionType = req.inspectionTypeCode == null || req.inspectionTypeCode.isBlank()
                ? "ANNUAL" : req.inspectionTypeCode.toUpperCase();
        Map<String, Object> rule = priceMapper.findPublished(vehicleType, inspectionType);
        if (rule == null)
        {
            throw ApiException.unprocessable(Codes.Error.NOT_SERVICEABLE, "该车型尚未配置生效计价方案，请稍后再试");
        }
        Row ruleRow = Row.of(rule);
        JSONObject ruleConfig = Json.read(ruleRow.json("config_json"));

        double distance = haversine(lng, lat, st.dec("lng").doubleValue(), st.dec("lat").doubleValue());
        int distanceMeters = (int) Math.round(distance);
        long amountFen = calculate(ruleConfig, distanceMeters);

        JSONObject items = new JSONObject();
        items.put("inspectionFeeFen", ruleConfig.getLongValue("inspectionFeeFen"));
        items.put("baseFeeFen", ruleConfig.getLongValue("baseFeeFen"));
        items.put("distanceMeters", distanceMeters);
        items.put("includedMeters", ruleConfig.getLongValue("includedMeters"));
        items.put("extraMeters", Math.max(0, distanceMeters - ruleConfig.getLongValue("includedMeters")));
        items.put("unitPriceFen", ruleConfig.getLongValue("unitPriceFen"));
        items.put("amountFen", amountFen);

        JSONObject vehicleSnapshot = new JSONObject();
        vehicleSnapshot.put("plateNo", plateNo);
        vehicleSnapshot.put("vehicleTypeCode", vehicleType);
        vehicleSnapshot.put("vehicleTypeName", dictLabel("vehicle_type", vehicleType));

        JSONObject route = new JSONObject();
        route.put("distanceMeters", distanceMeters);
        route.put("source", "LOCAL_HAVERSINE");
        route.put("direction", "ONE_WAY");

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ownerAccountId", p.accountId);
        row.put("stationId", stationId);
        row.put("vehicleSnapshot", vehicleSnapshot.toJSONString());
        row.put("addressCipher", crypto.encrypt(address.toJSONString()));
        row.put("pickupSlotId", slotRow.lng("id"));
        row.put("receiveSlotId", null);
        row.put("ruleId", ruleRow.lng("id"));
        row.put("routeSnapshot", route.toJSONString());
        row.put("amountFen", amountFen);
        row.put("itemsJson", items.toJSONString());
        row.put("expiresAt", ViTime.now().plusMinutes(QUOTE_TTL_MINUTES));
        priceMapper.insertQuote(row);
        Long quoteId = ((Number) row.get("id")).longValue();

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("quoteId", String.valueOf(quoteId));
        view.put("amountFen", amountFen);
        view.put("amountYuan", BigDecimal.valueOf(amountFen).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
        view.put("stationId", String.valueOf(stationId));
        view.put("stationName", st.str("name"));
        view.put("vehicleTypeCode", vehicleType);
        view.put("vehicleTypeName", dictLabel("vehicle_type", vehicleType));
        view.put("distanceMeters", distanceMeters);
        view.put("items", items);
        view.put("ruleId", String.valueOf(ruleRow.lng("id")));
        view.put("expiresAt", ViTime.iso(ViTime.now().plusMinutes(QUOTE_TTL_MINUTES)));
        view.put("pickupSlotId", String.valueOf(slotRow.lng("id")));
        return view;
    }

    /** 取最近且支持该车型、在服务半径内的已启用检测点 */
    private Map<String, Object> matchStation(String vehicleType, double lng, double lat)
    {
        Map<String, Object> best = null;
        double bestDistance = Double.MAX_VALUE;
        boolean typeSupported = false;
        for (Map<String, Object> s : stationMapper.listEnabled())
        {
            Row row = Row.of(s);
            JSONArray supported = JSON.parseArray(row.json("supported_vehicle_codes"));
            if (supported == null || !supported.contains(vehicleType))
            {
                continue;
            }
            typeSupported = true;
            double d = haversine(lng, lat, row.dec("lng").doubleValue(), row.dec("lat").doubleValue());
            if (d > row.intg("service_radius_meter", 30000))
            {
                continue;
            }
            if (d < bestDistance)
            {
                bestDistance = d;
                best = s;
            }
        }
        if (best == null)
        {
            String typeLabel = dictLabel("vehicle_type", vehicleType);
            throw ApiException.unprocessable(Codes.Error.NOT_SERVICEABLE,
                    typeSupported
                            ? "该地址暂不在任何检测点服务范围内"
                            : "暂无检测点支持「" + typeLabel + "」车型，请更换车型或联系客服");
        }
        return best;
    }

    private long calculate(JSONObject config, int distanceMeters)
    {
        long inspectionFee = config.getLongValue("inspectionFeeFen");
        long baseFee = config.getLongValue("baseFeeFen");
        long included = config.getLongValue("includedMeters");
        long unitMeters = Math.max(1, config.getLongValue("unitMeters"));
        long unitPrice = config.getLongValue("unitPriceFen");
        long extra = Math.max(0, distanceMeters - included);
        long units = (extra + unitMeters - 1) / unitMeters;
        return inspectionFee + baseFee + units * unitPrice;
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

    private JSONObject resolveAddress(Long accountId, Long addressId, MiniDtos.AddressReq draft)
    {
        if (addressId != null)
        {
            Map<String, Object> row = vehicleMapper.findAddress(addressId, accountId);
            if (row == null)
            {
                throw ApiException.notFound("地址不存在");
            }
            String plain = crypto.decrypt((byte[]) Row.of(row).get("address_cipher"));
            return Json.read(plain);
        }
        if (draft != null)
        {
            if (draft.address == null || draft.address.isBlank() || draft.lng == null || draft.lat == null)
            {
                throw ApiException.validation("请选择完整的上门接车地址与坐标");
            }
            JSONObject obj = new JSONObject();
            obj.put("address", draft.address);
            obj.put("lng", draft.lng);
            obj.put("lat", draft.lat);
            obj.put("label", draft.label);
            return obj;
        }
        return null;
    }

    // ------------------------------------------------------------------ 建单

    @Transactional
    public Map<String, Object> createOrder(MiniDtos.OrderCreateReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null || req.quoteId == null)
        {
            throw ApiException.validation("缺少报价标识");
        }
        Map<String, Object> quoteRow = priceMapper.findQuote(req.quoteId);
        if (quoteRow == null)
        {
            throw ApiException.notFound("报价不存在");
        }
        Row quote = Row.of(quoteRow);
        if (!java.util.Objects.equals(quote.lng("owner_account_id"), p.accountId))
        {
            throw ApiException.forbidden("报价不属于当前账户");
        }
        if (quote.lng("used_order_id") != null)
        {
            throw ApiException.stateConflict("该报价已用于建单");
        }
        if (quote.time("expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.conflict(Codes.Error.QUOTE_EXPIRED, "报价已过期，请重新核价");
        }

        Long stationId = quote.lng("station_id");
        Map<String, Object> station = stationMapper.findEnabled(stationId);
        if (station == null)
        {
            throw ApiException.unprocessable(Codes.Error.NOT_SERVICEABLE, "检测点当前不可预约");
        }
        Row st = Row.of(station);

        JSONObject address = resolveAddress(p.accountId, req.addressId, req.addressDraft);
        if (address == null)
        {
            String plain = crypto.decrypt((byte[]) quote.get("address_snapshot_cipher"));
            address = Json.read(plain);
        }

        Long pickupSlotId = quote.lng("pickup_slot_id");
        if (stationMapper.holdQuota(pickupSlotId, 1) <= 0)
        {
            throw ApiException.unprocessable(Codes.Error.QUOTA_EXHAUSTED, "该时段名额已满，请改约其他时段");
        }

        LocalDateTime now = ViTime.now();
        LocalDateTime payExpiresAt = now.plusMinutes(config.payExpireMinutes());
        LocalDateTime pickupStart = Row.of(stationMapper.findSlot(pickupSlotId)).time("starts_at");
        LocalDateTime pickupEnd = Row.of(stationMapper.findSlot(pickupSlotId)).time("ends_at");
        LocalDateTime dispatchAt = pickupStart.minusMinutes(config.dispatchLeadMinutes());

        JSONObject vehicleSnapshot = Json.read(quote.json("vehicle_snapshot"));
        if (req.contactName != null)
        {
            vehicleSnapshot.put("contactName", req.contactName);
        }
        if (req.contactPhone != null)
        {
            vehicleSnapshot.put("contactPhone", req.contactPhone);
        }
        vehicleSnapshot.put("address", address.getString("address"));
        vehicleSnapshot.put("addressLabel", address.getString("label"));
        vehicleSnapshot.put("lng", address.getDouble("lng"));
        vehicleSnapshot.put("lat", address.getDouble("lat"));

        Map<String, Object> orderRow = new LinkedHashMap<>();
        orderRow.put("orderNo", nextOrderNo());
        orderRow.put("ownerAccountId", p.accountId);
        orderRow.put("vehicleId", req.vehicleId);
        orderRow.put("stationId", stationId);
        orderRow.put("deptId", st.lng("dept_id"));
        orderRow.put("quoteId", quote.lng("id"));
        orderRow.put("vehicleSnapshot", vehicleSnapshot.toJSONString());
        orderRow.put("pickupStartsAt", pickupStart);
        orderRow.put("pickupEndsAt", pickupEnd);
        orderRow.put("payExpiresAt", payExpiresAt);
        orderRow.put("dispatchAt", dispatchAt);
        orderRow.put("dispatchConfigVersion", (long) config.dispatchLeadMinutes());
        orderRow.put("totalFen", quote.lng("amount_fen", 0L));
        orderRow.put("termsVersion", config.termsVersion());
        orderMapper.insert(orderRow);
        Long orderId = ((Number) orderRow.get("id")).longValue();
        String orderNo = String.valueOf(orderRow.get("orderNo"));

        Map<String, Object> reservation = new LinkedHashMap<>();
        reservation.put("orderId", orderId);
        reservation.put("slotId", pickupSlotId);
        reservation.put("quantity", 1);
        reservation.put("expiresAt", payExpiresAt);
        orderMapper.insertReservation(reservation);

        priceMapper.markQuoteUsed(quote.lng("id"), orderId);

        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("orderId", orderId);
        payment.put("outTradeNo", "RLP" + orderNo);
        payment.put("totalFen", orderRow.get("totalFen"));
        payment.put("expiresAt", payExpiresAt);
        payMapper.insertPayment(payment);

        eventService.record("ORDER", orderId, "ORDER_CREATED", orderId, null,
                Map.of("status", Codes.Fulfill.PENDING_PAY, "amountFen", orderRow.get("totalFen")), null);
        eventService.publish("ORDER", orderId, "ORDER_CREATED", Map.of("orderNo", orderNo));
        return ownerOrderView(orderNo, p.accountId);
    }

    private String nextOrderNo()
    {
        return "RL" + ViTime.now().atZone(ZoneOffset.UTC).withZoneSameInstant(ViTime.SHANGHAI).format(ORDER_NO_FMT)
                + crypto.randomDigits(4);
    }

    // ------------------------------------------------------------------ 支付

    @Transactional
    public Map<String, Object> prepay(String orderNo)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = requireOwnerOrder(orderNo, p.accountId);
        if (!Codes.Fulfill.PENDING_PAY.equals(order.str("fulfillment_status")))
        {
            throw ApiException.stateConflict("订单当前状态不可支付");
        }
        if (order.time("pay_expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.stateConflict("订单支付已超时，请重新预约");
        }
        Map<String, Object> payment = payMapper.findByOrder(order.lng("id"));
        if (payment == null)
        {
            throw ApiException.notFound("支付单不存在");
        }
        Row pay = Row.of(payment);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", orderNo);
        view.put("outTradeNo", pay.str("out_trade_no"));
        view.put("amountFen", pay.lng("total_fen"));
        view.put("expiresAt", ViTime.iso(pay.time("expires_at")));
        view.put("channel", config.mockPayment() ? "MOCK" : "WECHAT");
        if (config.mockPayment())
        {
            view.put("mock", true);
            view.put("hint", "当前为联调支付通道，可在小程序点击“模拟支付成功”完成支付");
            view.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            view.put("nonceStr", crypto.randomToken().substring(0, 16));
            view.put("package", "prepay_id=mock" + pay.str("out_trade_no"));
            view.put("signType", "RSA");
            view.put("paySign", "MOCK");
        }
        else
        {
            throw new ApiException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    Codes.Error.CHANNEL_UNAVAILABLE, "微信支付尚未配置商户凭据（L01），请改用联调通道");
        }
        return view;
    }

    /** 联调通道：模拟渠道支付成功回调，仍走同一套可信支付结果处理 */
    @Transactional
    public Map<String, Object> mockPay(String orderNo)
    {
        if (!config.mockPayment())
        {
            throw ApiException.forbidden("真实支付通道下不允许模拟支付");
        }
        MiniPrincipal p = MiniContext.require();
        Row order = requireOwnerOrder(orderNo, p.accountId);
        Map<String, Object> payment = payMapper.findByOrder(order.lng("id"));
        if (payment == null)
        {
            throw ApiException.notFound("支付单不存在");
        }
        applyPaymentSuccess(Row.of(payment), "MOCK" + orderNo);
        return ownerOrderView(orderNo, p.accountId);
    }

    /**
     * 可信支付成功处理：支付单幂等置成功、订单进入待安排或待派单、预占转正式占用。
     * 重复回调只返回原结果，不重复扣减名额。
     */
    @Transactional
    public void applyPaymentSuccess(Row payment, String transactionId)
    {
        int updated = payMapper.markPaid(payment.lng("id"), transactionId, ViTime.now());
        if (updated == 0)
        {
            return;
        }
        Long orderId = payment.lng("order_id");
        Row order = Row.of(orderMapper.findById(orderId));
        if (!Codes.Fulfill.PENDING_PAY.equals(order.str("fulfillment_status")))
        {
            return;
        }
        for (Map<String, Object> r : orderMapper.listReservations(orderId))
        {
            Row res = Row.of(r);
            if (!Codes.Reservation.HELD.equals(res.str("status")))
            {
                continue;
            }
            if (orderMapper.casReservation(res.lng("id"), Codes.Reservation.HELD, Codes.Reservation.CONFIRMED) > 0)
            {
                stationMapper.confirmQuota(res.lng("slot_id"), res.intg("quantity", 1));
            }
        }
        LocalDateTime now = ViTime.now();
        boolean due = order.time("dispatch_at") == null || !order.time("dispatch_at").isAfter(now);
        orderMapper.casStatusByState(orderId, Codes.Fulfill.PENDING_PAY,
                due ? Codes.Fulfill.PENDING_DISPATCH : Codes.Fulfill.SCHEDULED);

        eventService.record("ORDER", orderId, "PAYMENT_SUCCEEDED", orderId, null,
                Map.of("status", due ? Codes.Fulfill.PENDING_DISPATCH : Codes.Fulfill.SCHEDULED), null);
        eventService.publish("ORDER", orderId, "PAYMENT_SUCCEEDED", Map.of("orderNo", order.str("order_no")));
        eventService.notify(order.lng("owner_account_id"), Codes.Role.OWNER, orderId, "PAYMENT_SUCCEEDED",
                "预约已支付成功，上门时段 " + ViTime.shanghaiText(order.time("pickup_starts_at")) + "，正在安排代驾");
        if (due)
        {
            dispatchService.dispatchOrder(orderId);
        }
    }

    @Transactional
    public Map<String, Object> closeUnpaid(String orderNo, MiniDtos.VersionReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = requireOwnerOrder(orderNo, p.accountId);
        if (!Codes.Fulfill.PENDING_PAY.equals(order.str("fulfillment_status")))
        {
            throw ApiException.stateConflict("订单当前状态不可关闭");
        }
        Map<String, Object> payment = payMapper.findByOrder(order.lng("id"));
        if (payment != null && payMapper.markClosed(Row.of(payment).lng("id")) == 0)
        {
            Row pay = Row.of(payMapper.findById(Row.of(payment).lng("id")));
            if (Codes.Payment.SUCCESS.equals(pay.str("status")))
            {
                throw ApiException.stateConflict("支付已完成，不能关闭订单");
            }
        }
        orderMapper.casStatusByState(order.lng("id"), Codes.Fulfill.PENDING_PAY, Codes.Fulfill.CLOSED);
        releaseReservations(order.lng("id"), Codes.Reservation.HELD, false);
        eventService.record("ORDER", order.lng("id"), "ORDER_CLOSED_UNPAID", order.lng("id"),
                Map.of("status", Codes.Fulfill.PENDING_PAY), Map.of("status", Codes.Fulfill.CLOSED), req == null ? null : req.reason);
        return ownerOrderView(orderNo, p.accountId);
    }

    // ------------------------------------------------------------------ 定时收尾

    /**
     * 关闭超时未支付订单。逐单独立处理：单笔失败不影响其它订单，
     * 名额释放与支付单关闭在同一事务内完成，避免出现"名额占着但支付单已关"的中间态。
     */
    @Transactional
    public int closeExpiredPayments()
    {
        int closed = 0;
        for (Map<String, Object> r : orderMapper.listPayExpired(200))
        {
            Row order = Row.of(r);
            try
            {
                Long orderId = order.lng("id");
                Map<String, Object> payment = payMapper.findByOrder(orderId);
                if (payment != null)
                {
                    payMapper.markClosed(Row.of(payment).lng("id"));
                }
                if (orderMapper.casStatusByState(orderId, Codes.Fulfill.PENDING_PAY, Codes.Fulfill.CLOSED) > 0)
                {
                    releaseReservations(orderId, Codes.Reservation.HELD, false);
                    eventService.record("ORDER", orderId, "ORDER_CLOSED_UNPAID", orderId,
                            Map.of("status", Codes.Fulfill.PENDING_PAY), Map.of("status", Codes.Fulfill.CLOSED),
                            "支付超时自动关闭");
                    closed++;
                }
            }
            catch (RuntimeException e)
            {
                // 单笔异常留待下一轮重试，不阻塞批量收尾
            }
        }
        return closed;
    }

    /**
     * 实际归还后超过自动确认时长且无争议的订单置为已完成。
     */
    @Transactional
    public int autoCompleteDue()
    {
        int done = 0;
        for (Map<String, Object> r : orderMapper.listAutoConfirmDue(200))
        {
            Row order = Row.of(r);
            try
            {
                Long orderId = order.lng("id");
                if (orderMapper.casStatusByState(orderId, Codes.Fulfill.WAIT_CONFIRM, Codes.Fulfill.COMPLETED) > 0)
                {
                    orderMapper.markCompleted(orderId);
                    Row detail = Row.of(orderMapper.findDetailById(orderId));
                    eventService.record("ORDER", orderId, "ORDER_AUTO_COMPLETED", orderId,
                            Map.of("status", Codes.Fulfill.WAIT_CONFIRM),
                            Map.of("status", Codes.Fulfill.COMPLETED), "超过自动确认时长");
                    eventService.notify(detail.lng("owner_account_id"), Codes.Role.OWNER, orderId,
                            "ORDER_AUTO_COMPLETED", "订单已自动完成，如有问题可在售后入口发起申请");
                    done++;
                }
            }
            catch (RuntimeException e)
            {
                // 同上：跳过异常单，下一轮继续
            }
        }
        return done;
    }

    void releaseReservations(Long orderId, String fromStatus, boolean confirmed)    {
        for (Map<String, Object> r : orderMapper.listReservations(orderId))
        {
            Row res = Row.of(r);
            if (!fromStatus.equals(res.str("status")))
            {
                continue;
            }
            if (orderMapper.casReservation(res.lng("id"), fromStatus, Codes.Reservation.RELEASED) > 0)
            {
                if (confirmed)
                {
                    stationMapper.releaseConfirmedQuota(res.lng("slot_id"), res.intg("quantity", 1));
                }
                else
                {
                    stationMapper.releaseHeldQuota(res.lng("slot_id"), res.intg("quantity", 1));
                }
            }
        }
    }

    // ------------------------------------------------------------------ 查询

    public Row requireOwnerOrder(String orderNo, Long accountId)
    {
        Map<String, Object> row = orderMapper.findDetailByNo(orderNo);
        if (row == null)
        {
            throw ApiException.notFound("订单不存在");
        }
        Row order = Row.of(row);
        if (!java.util.Objects.equals(order.lng("owner_account_id"), accountId))
        {
            throw ApiException.forbidden("无权查看该订单");
        }
        return order;
    }

    public Map<String, Object> ownerOrderView(String orderNo, Long accountId)
    {
        Row o = requireOwnerOrder(orderNo, accountId);
        JSONObject snapshot = Json.read(o.json("vehicle_snapshot"));
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderNo", o.str("order_no"));
        view.put("fulfillmentStatus", o.str("fulfillment_status"));
        view.put("fulfillmentStatusLabel", statusLabel(o.str("fulfillment_status")));
        view.put("cancelStatus", o.str("cancel_status"));
        view.put("custodyType", o.str("custody_type"));
        view.put("custodyLabel", custodyLabel(o.str("custody_type")));
        view.put("stationId", String.valueOf(o.lng("station_id")));
        view.put("stationName", o.str("station_name"));
        view.put("stationAddress", o.str("station_address"));
        view.put("pickupStartsAt", ViTime.iso(o.time("pickup_starts_at")));
        view.put("pickupEndsAt", ViTime.iso(o.time("pickup_ends_at")));
        view.put("pickupWindowLabel", ViTime.shanghaiText(o.time("pickup_starts_at")) + " - "
                + ViTime.shanghaiText(o.time("pickup_ends_at")).substring(11));
        view.put("payExpiresAt", ViTime.iso(o.time("pay_expires_at")));
        view.put("dispatchAt", ViTime.iso(o.time("dispatch_at")));
        view.put("returnedAt", ViTime.iso(o.time("returned_at")));
        view.put("autoConfirmAt", ViTime.iso(o.time("auto_confirm_at")));
        view.put("completedAt", ViTime.iso(o.time("completed_at")));
        view.put("totalFen", o.lng("total_fen"));
        view.put("totalYuan", BigDecimal.valueOf(o.lng("total_fen", 0)).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
        view.put("vehicle", snapshot);
        view.put("plateNo", snapshot.getString("plateNo"));
        view.put("vehicleTypeName", snapshot.getString("vehicleTypeName"));
        view.put("address", snapshot.getString("address"));
        view.put("version", o.lng("version"));
        view.put("createdAt", ViTime.iso(o.time("created_at")));
        view.put("allowedActions", ownerActions(o.str("fulfillment_status"), o.str("cancel_status")));
        return view;
    }

    private List<String> ownerActions(String status, String cancelStatus)
    {
        List<String> actions = new ArrayList<>();
        if (Codes.Fulfill.PENDING_PAY.equals(status))
        {
            actions.add("PAY");
            actions.add("CLOSE_UNPAID");
            actions.add("APPLY_AFTER_SALE");
        }
        else if (!Codes.Fulfill.CANCELLED.equals(status) && !Codes.Fulfill.CLOSED.equals(status)
                && !Codes.Fulfill.COMPLETED.equals(status))
        {
            if (Codes.Cancel.NONE.equals(cancelStatus)) 
            {
                actions.add("APPLY_AFTER_SALE");
            }
        }
        if (Codes.Fulfill.WAIT_CONFIRM.equals(status))
        {
            actions.add("CONFIRM_SERVICE");
            actions.add("OPEN_DISPUTE");
        }
        if (Codes.Fulfill.COMPLETED.equals(status) || Codes.Fulfill.CANCELLED.equals(status))
        {
            actions.add("REVIEW");
        }
        actions.add("VIEW_TIMELINE");
        actions.add("VIEW_EVIDENCE");
        return actions;
    }

    public Map<String, Object> ownerOrderList(String status, int pageNum, int pageSize)
    {
        MiniPrincipal p = MiniContext.require();
        int offset = Math.max(0, (pageNum - 1) * pageSize);
        List<Map<String, Object>> rows = orderMapper.ownerList(p.accountId, status, offset, pageSize);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows)
        {
            Row o = Row.of(r);
            JSONObject snapshot = Json.read(o.json("vehicle_snapshot"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderNo", o.str("order_no"));
            item.put("fulfillmentStatus", o.str("fulfillment_status"));
            item.put("fulfillmentStatusLabel", statusLabel(o.str("fulfillment_status")));
            item.put("cancelStatus", o.str("cancel_status"));
            item.put("custodyType", o.str("custody_type"));
            item.put("pickupStartsAt", ViTime.iso(o.time("pickup_starts_at")));
            item.put("plateNo", snapshot.getString("plateNo"));
            item.put("vehicleTypeName", snapshot.getString("vehicleTypeName"));
            item.put("stationName", o.str("station_name"));
            item.put("totalFen", o.lng("total_fen"));
            item.put("totalYuan", BigDecimal.valueOf(o.lng("total_fen", 0)).movePointLeft(2).setScale(2, RoundingMode.HALF_UP));
            item.put("createdAt", ViTime.iso(o.time("created_at")));
            out.add(item);
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("rows", out);
        page.put("total", orderMapper.ownerCount(p.accountId, status));
        page.put("pageNum", pageNum);
        page.put("pageSize", pageSize);
        return page;
    }

    @Transactional
    public Map<String, Object> confirmService(String orderNo, MiniDtos.ServiceConfirmReq req)
    {
        MiniPrincipal p = MiniContext.require();
        Row order = requireOwnerOrder(orderNo, p.accountId);
        if (!Codes.Fulfill.WAIT_CONFIRM.equals(order.str("fulfillment_status")))
        {
            throw ApiException.stateConflict("订单当前状态不可确认服务");
        }
        if (orderMapper.casStatusByState(order.lng("id"), Codes.Fulfill.WAIT_CONFIRM, Codes.Fulfill.COMPLETED) == 0)
        {
            throw ApiException.conflict(Codes.Error.VERSION_CONFLICT, "订单状态已变化，请刷新后重试");
        }
        orderMapper.markCompleted(order.lng("id"));
        eventService.record("ORDER", order.lng("id"), "SERVICE_CONFIRMED", order.lng("id"),
                Map.of("status", Codes.Fulfill.WAIT_CONFIRM), Map.of("status", Codes.Fulfill.COMPLETED), null);
        return ownerOrderView(orderNo, p.accountId);
    }

    // ------------------------------------------------------------------ 展示辅助

    public String statusLabel(String status)
    {
        if (status == null)
        {
            return "";
        }
        return switch (status)
        {
            case Codes.Fulfill.PENDING_PAY -> "待支付";
            case Codes.Fulfill.SCHEDULED -> "预约成功，待安排";
            case Codes.Fulfill.PENDING_DISPATCH -> "待派单";
            case Codes.Fulfill.DISPATCHING -> "派单中";
            case Codes.Fulfill.MANUAL_DISPATCH -> "待人工安排";
            case Codes.Fulfill.ACCEPTED -> "代驾已接单";
            case Codes.Fulfill.GOING_PICKUP -> "代驾前往接车";
            case Codes.Fulfill.ARRIVED_PICKUP -> "代驾已到达";
            case Codes.Fulfill.PICKED_UP -> "已接车";
            case Codes.Fulfill.ARRIVED_STATION -> "已到检测点";
            case Codes.Fulfill.INSPECTING -> "检测中";
            case Codes.Fulfill.INSPECTED -> "检测完成，待取车";
            case Codes.Fulfill.CAR_TAKEN -> "返还中";
            case Codes.Fulfill.WAIT_CONFIRM -> "车辆已归还，待确认";
            case Codes.Fulfill.COMPLETED -> "已完成";
            case Codes.Fulfill.CANCELLED -> "已取消";
            case Codes.Fulfill.CLOSED -> "未支付已关闭";
            default -> status;
        };
    }

    public String custodyLabel(String custody)
    {
        if (custody == null)
        {
            return "";
        }
        return switch (custody)
        {
            case Codes.Custody.OWNER -> "车主保管";
            case Codes.Custody.DRIVER -> "代驾保管";
            case Codes.Custody.STATION -> "检测点保管";
            default -> custody;
        };
    }

    private String dictLabel(String dictType, String value)
    {
        if (value == null || value.isBlank())
        {
            return "";
        }
        String label = DictUtils.getDictLabel(dictType, value);
        return label == null || label.isBlank() ? value : label;
    }
}
