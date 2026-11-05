package com.xinyi.rongli.order.service;

import com.xinyi.rongli.order.domain.*;
import com.xinyi.rongli.order.dto.*;
import com.xinyi.rongli.order.mapper.*;
import com.xinyi.rongli.booking.mapper.QuoteMapper;
import com.xinyi.rongli.booking.dto.QuoteView;
import com.xinyi.rongli.address.dto.GeoPoint;
import com.xinyi.rongli.address.service.AddressCrypto;
import com.xinyi.rongli.identity.service.PhoneCrypto;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.error.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderProjection {
    private final OrderMapper orders;
    private final OrderReadMapper reads;
    private final QuoteMapper quotes;
    private final AddressCrypto crypto;

    public OrderProjection(
            OrderMapper orders, OrderReadMapper reads, QuoteMapper quotes, AddressCrypto crypto) {
        this.orders = orders;
        this.reads = reads;
        this.quotes = quotes;
        this.crypto = crypto;
    }

    private static String str(Instant at) {
        return at == null ? null : at.toString();
    }

    private static String value(Map<String, Object> m, String k) {
        return m == null || m.get(k) == null ? null : m.get(k).toString();
    }

    private static String text(com.fasterxml.jackson.databind.JsonNode n, String k) {
        return n.hasNonNull(k) ? n.get(k).asText() : null;
    }

    private static String display(ViOrder row) {
        if ("APPROVED_WAIT_RETURN".equals(row.getCancelStatus())) return "取消中，待还车";
        if (!"NONE".equals(row.getCancelStatus()))
            return switch (row.getCancelStatus()) {
                case "COMPLETED" -> "已取消";
                case "APPLIED" -> "取消审核中";
                case "REJECTED" -> "取消申请未通过";
                default -> row.getCancelStatus();
            };
        return switch (row.getFulfillmentStatus()) {
            case "PENDING_PAY" -> "待付款";
            case "DISPATCHING" -> "待接单";
            case "SCHEDULED" -> "预约成功";
            case "PENDING_DISPATCH" -> "待派单";
            case "MANUAL_DISPATCH" -> "人工安排中";
            case "ACCEPTED" -> "代驾已接单";
            case "GOING_PICKUP" -> "代驾前往接车";
            case "ARRIVED_PICKUP" -> "代驾已到达";
            case "PICKED_UP" -> "已接车";
            case "ARRIVED_STATION" -> "已到检测站";
            case "INSPECTING" -> "检测中";
            case "INSPECTED" -> "检测完成";
            case "CAR_TAKEN" -> "还车途中";
            case "WAIT_CONFIRM" -> "待确认完成";
            case "CANCELLED" -> "已取消";
            case "COMPLETED" -> "已完成";
            case "CLOSED" -> "已关闭";
            default -> row.getFulfillmentStatus();
        };
    }

    private OwnerOrderView.Vehicle vehicle(ViOrder row) {
        var v = ViJson.tree(row.getVehicleSnapshot());
        var draft = v.path("draft");
        return new OwnerOrderView.Vehicle(
                row.getVehicleId() == null ? null : row.getVehicleId().toString(),
                v.path("plateNo").asText(),
                v.path("vehicleTypeCode").asText(),
                v.path("vehicleTypeLabel").asText(),
                text(v.has("brandModel") ? v : draft, "brandModel"),
                text(v.has("vinLast4") ? v : draft, "vinLast4"));
    }

    /** 使用建单时的站点快照展示原约定；快照缺失时拒绝，不拼接当前配置猜测旧订单。 */
    private OwnerOrderView.Station station(ViOrder row) {
        String json = reads.stationSnapshot(row.getId());
        if (json == null) throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "订单站点快照缺失，请联系管理员核查");
        return ViJson.read(json, OwnerOrderView.Station.class);
    }

    private ViPaymentOrder payment(ViOrder row) {
        var p = orders.payment(row.getId());
        if (p == null) throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "订单支付记录缺失，请联系管理员核查");
        return p;
    }

    private OwnerOrderView.Money money(ViOrder row) {
        var p = payment(row);
        var q = quotes.find(row.getQuoteId());
        if (q == null) throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        return new OwnerOrderView.Money(
                Arrays.asList(
                        ViJson.read(
                                ViJson.tree(q.getItemsJson()).path("items").toString(),
                                QuoteView.FeeItem[].class)),
                row.getTotalFen(),
                p.getPaidFen(),
                p.getRefundedFen());
    }

    /** 联系人取建单加密快照并脱敏；不向普通订单投影返回完整手机号。 */
    private OwnerOrderView.Contact contact(ViOrder row) {
        var snap = reads.contactCipher(row.getId());
        if (snap == null || snap.getContactCipher() == null)
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        var c = ViJson.tree(crypto.decrypt(snap.getContactCipher()));
        return new OwnerOrderView.Contact(
                c.path("displayName").asText(), PhoneCrypto.mask(c.path("phone").asText()), false);
    }

    private OwnerOrderView.Address address(ViOrder row) {
        var snap = orders.snapshot(row.getId());
        if (snap == null) throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
        var a = ViJson.tree(crypto.decrypt(snap.getAddressCipher()));
        return new OwnerOrderView.Address(
                a.path("address").asText(),
                ViJson.read(a.path("location").toString(), GeoPoint.class),
                contact(row));
    }

    private OrderViews.DriverIdentity driver(ViOrder row) {
        var a = reads.activeAssignment(row.getId(), row.getActiveAssignmentId());
        if (a == null) return null;
        var d = reads.driverPublic(a.driverId());
        String name = value(d, "nickname");
        if (name == null || name.isBlank()) name = "代驾";
        return new OrderViews.DriverIdentity(
                name, "D" + a.driverId(), null, new OwnerOrderView.Contact(name, null, false));
    }

    private OrderViews.AfterSale afterSale(ViOrder row, boolean finance) {
        var a = reads.afterSale(row.getId());
        if (a == null) return null;
        return new OrderViews.AfterSale(
                value(a, "id"),
                value(a, "kind"),
                value(a, "case_status"),
                finance && a.get("approved_fen") != null
                        ? ((Number) a.get("approved_fen")).longValue()
                        : null,
                value(a, "return_state"),
                value(a, "refund_state"),
                value(a, "public_opinion"),
                ((Number) a.get("version")).longValue());
    }

    private String refund(ViOrder row) {
        var a = reads.afterSale(row.getId());
        return a == null ? "NONE" : value(a, "refund_state");
    }

    private String dispute(ViOrder row) {
        String s = reads.dispute(row.getId());
        return s == null ? "NONE" : s;
    }

    private OwnerOrderView.Mileage mileage(ViOrder row) {
        BigDecimal pickup = null, returned = null;
        for (var m : reads.mileage(row.getId())) {
            if (m.get("mileage_km") == null) continue;
            var km = new BigDecimal(m.get("mileage_km").toString());
            if ("OWNER_TO_DRIVER".equals(value(m, "handover_type"))) pickup = km;
            else returned = km;
        }
        var distance = pickup != null && returned != null ? returned.subtract(pickup) : null;
        boolean abnormal = distance != null && distance.signum() < 0;
        return new OwnerOrderView.Mileage(
                pickup == null ? null : pickup.toPlainString(),
                returned == null ? null : returned.toPlainString(),
                distance == null || abnormal ? null : distance.toPlainString(),
                abnormal,
                abnormal ? "仪表公里数异常，待核查" : null);
    }

    private OrderViews.Cancellation cancellation(ViOrder row) {
        boolean needed = "APPROVED_WAIT_RETURN".equals(row.getCancelStatus());
        return new OrderViews.Cancellation(needed, display(row), needed ? "请按原流程完成车辆交接" : null);
    }

    /** 司机只有当前未结束的有效任务才保留履约视角，历史接单关系不能继续授予操作能力。 */
    private boolean active(ViOrder row, OrderAssignment a) {
        return a != null
                && Objects.equals(row.getActiveAssignmentId(), a.id())
                && a.endedAt() == null
                && row.getReturnedAt() == null
                && !Set.of("CLOSED", "COMPLETED", "CANCELLED").contains(row.getFulfillmentStatus())
                && !"COMPLETED".equals(row.getCancelStatus());
    }

    private static boolean allows(Set<Long> ids, Long dept) {
        return ids == null || ids.contains(dept);
    }

    public OwnerOrderView owner(ViOrder row) {
        var a = address(row);
        return new OwnerOrderView(
                row.getOrderNo(),
                row.getFulfillmentStatus(),
                display(row),
                row.getCancelStatus(),
                row.getCustodyType(),
                vehicle(row),
                station(row),
                str(row.getPickupStartsAt()),
                str(row.getPickupEndsAt()),
                str(row.getReturnedAt()),
                row.getVersion(),
                "OWNER",
                payment(row).getStatus(),
                refund(row),
                dispute(row),
                a,
                a,
                driver(row),
                str(row.getDispatchAt()),
                str(row.getPayExpiresAt()),
                str(row.getAutoConfirmAt()),
                money(row),
                mileage(row),
                afterSale(row, true),
                ownerActions(row));
    }

    /** 操作入口按真实收款与原申请判断；申请本身仍由售后事务重新校验金额、版本及凭证。 */
    private List<String> ownerActions(ViOrder row) {
        List<String> actions = new ArrayList<>();
        actions.add("VIEW_DETAIL");
        var existing = reads.afterSale(row.getId());
        if (existing != null) actions.add("VIEW_AFTERSALE");
        var paid = payment(row);
        boolean activeCase =
                existing != null
                        && Set.of("PENDING_REVIEW", "EXECUTING")
                                .contains(value(existing, "case_status"));
        if (!activeCase
                && "SUCCESS".equals(paid.getStatus())
                && paid.getPaidFen() > paid.getRefundedFen()) {
            actions.add("APPLY_REFUND_ONLY");
            if ("NONE".equals(row.getCancelStatus())
                    && !Set.of("CLOSED", "CANCELLED", "COMPLETED")
                            .contains(row.getFulfillmentStatus()))
                actions.add("APPLY_CANCEL_REFUND");
        }
        return List.copyOf(actions);
    }

    /** 分角色生成字段白名单；财务、地址和详情操作各自取权限交集，不能仅隐藏前端按钮。 */
    public Object project(ViOrder row, OrderReadScope scope, boolean detail) {
        var vehicle = vehicle(row);
        var station = station(row);
        var actions =
                scope.canDetail() && allows(scope.detailDepartments(), row.getDeptId())
                        ? List.of("VIEW_DETAIL")
                        : List.<String>of();
        var own =
                "DRIVER".equals(scope.view())
                        ? reads.ownAssignment(row.getId(), scope.driverId())
                        : null;
        boolean active = active(row, own),
                finance = allows(scope.financeDepartments(), row.getDeptId()),
                addressAllowed = allows(scope.addressDepartments(), row.getDeptId());
        String paymentStatus = ("DRIVER".equals(scope.view()) ? null : payment(row).getStatus());
        if (detail && "OWNER".equals(scope.view())) return owner(row);
        if (own == null && "DRIVER".equals(scope.view()))
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        if (!detail) {
            if ("OWNER".equals(scope.view()))
                return new OrderViews.OwnerSummary(
                        scope.view(),
                        row.getOrderNo(),
                        row.getFulfillmentStatus(),
                        display(row),
                        row.getCancelStatus(),
                        row.getCustodyType(),
                        vehicle,
                        station,
                        str(row.getPickupStartsAt()),
                        str(row.getPickupEndsAt()),
                        str(row.getReturnedAt()),
                        row.getVersion(),
                        actions,
                        paymentStatus,
                        money(row));
            if ("DRIVER".equals(scope.view()))
                return new OrderViews.DriverSummary(
                        scope.view(),
                        row.getOrderNo(),
                        row.getFulfillmentStatus(),
                        display(row),
                        row.getCancelStatus(),
                        row.getCustodyType(),
                        vehicle,
                        station,
                        str(row.getPickupStartsAt()),
                        str(row.getPickupEndsAt()),
                        str(row.getReturnedAt()),
                        row.getVersion(),
                        actions,
                        own.id().toString(),
                        str(own.endedAt()),
                        active ? "ACTIVE_ASSIGNMENT" : "HISTORY",
                        !active);
            if ("STATION".equals(scope.view()))
                return new OrderViews.StationSummary(
                        scope.view(),
                        row.getOrderNo(),
                        row.getFulfillmentStatus(),
                        display(row),
                        row.getCancelStatus(),
                        row.getCustodyType(),
                        vehicle,
                        station,
                        str(row.getPickupStartsAt()),
                        str(row.getPickupEndsAt()),
                        str(row.getReturnedAt()),
                        row.getVersion(),
                        actions,
                        paymentStatus);
            if ("ADMIN".equals(scope.view()))
                return new OrderViews.AdminSummary(
                        scope.view(),
                        row.getOrderNo(),
                        row.getFulfillmentStatus(),
                        display(row),
                        row.getCancelStatus(),
                        row.getCustodyType(),
                        vehicle,
                        station,
                        str(row.getPickupStartsAt()),
                        str(row.getPickupEndsAt()),
                        str(row.getReturnedAt()),
                        row.getVersion(),
                        actions,
                        paymentStatus);
        }
        if ("DRIVER".equals(scope.view())) {
            var address = active ? address(row) : null;
            return new OrderViews.DriverDetail(
                    row.getOrderNo(),
                    row.getFulfillmentStatus(),
                    display(row),
                    row.getCancelStatus(),
                    row.getCustodyType(),
                    vehicle,
                    station,
                    str(row.getPickupStartsAt()),
                    str(row.getPickupEndsAt()),
                    str(row.getReturnedAt()),
                    row.getVersion(),
                    scope.view(),
                    own.id().toString(),
                    str(own.endedAt()),
                    active ? "ACTIVE_ASSIGNMENT" : "HISTORY",
                    "服务区域未单独记录",
                    address,
                    address,
                    active ? address.contact() : null,
                    mileage(row),
                    cancellation(row),
                    !active,
                    actions);
        }
        if ("STATION".equals(scope.view())) {
            String license = reads.license(row.getId());
            if (license == null) license = "MISSING";
            return new OrderViews.StationDetail(
                    row.getOrderNo(),
                    row.getFulfillmentStatus(),
                    display(row),
                    row.getCancelStatus(),
                    row.getCustodyType(),
                    vehicle,
                    station,
                    str(row.getPickupStartsAt()),
                    str(row.getPickupEndsAt()),
                    str(row.getReturnedAt()),
                    row.getVersion(),
                    scope.view(),
                    paymentStatus,
                    driver(row),
                    row.getActiveAssignmentId() == null
                            ? null
                            : row.getActiveAssignmentId().toString(),
                    license,
                    reads.result(row.getId()),
                    cancellation(row),
                    actions);
        }
        var address = addressAllowed ? address(row) : null;
        return new OrderViews.AdminDetail(
                row.getOrderNo(),
                row.getFulfillmentStatus(),
                display(row),
                row.getCancelStatus(),
                row.getCustodyType(),
                vehicle,
                station,
                str(row.getPickupStartsAt()),
                str(row.getPickupEndsAt()),
                str(row.getReturnedAt()),
                row.getVersion(),
                scope.view(),
                row.getOwnerAccountId().toString(),
                contact(row),
                driver(row),
                row.getActiveAssignmentId() == null ? null : row.getActiveAssignmentId().toString(),
                row.getDeptId().toString(),
                paymentStatus,
                refund(row),
                dispute(row),
                finance ? money(row) : null,
                address,
                address,
                mileage(row),
                afterSale(row, finance),
                str(row.getPayExpiresAt()),
                str(row.getDispatchAt()),
                str(row.getAutoConfirmAt()),
                row.getActiveAssignmentId() == null
                        || reads.activeAssignment(row.getId(), row.getActiveAssignmentId()) == null,
                actions);
    }
}
