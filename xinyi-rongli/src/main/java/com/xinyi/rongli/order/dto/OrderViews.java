package com.xinyi.rongli.order.dto;

import java.util.List;

/** 每个端和列表/详情各自白名单，不能共享一个全字段响应对象。 */
/** 各角色订单投影分别声明字段，防止把数据库实体或其他角色的财务/地址字段直接序列化。 */
public final class OrderViews {
    private OrderViews() {}

    public record OwnerSummary(
            String view,
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            List<String> allowedActions,
            String paymentStatus,
            OwnerOrderView.Money money) {}

    public record DriverSummary(
            String view,
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            List<String> allowedActions,
            String assignmentId,
            String assignmentEndedAt,
            String accessPhase,
            boolean driverReleased) {}

    public record StationSummary(
            String view,
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            List<String> allowedActions,
            String paymentStatus) {}

    public record AdminSummary(
            String view,
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            List<String> allowedActions,
            String paymentStatus) {}

    public record DriverDetail(
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            String view,
            String assignmentId,
            String assignmentEndedAt,
            String accessPhase,
            String pickupArea,
            OwnerOrderView.Address pickupAddress,
            OwnerOrderView.Address returnAddress,
            OwnerOrderView.Contact ownerContact,
            OwnerOrderView.Mileage mileage,
            Cancellation cancellation,
            boolean driverReleased,
            List<String> allowedActions) {}

    public record StationDetail(
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            String view,
            String paymentStatus,
            DriverIdentity driver,
            String assignmentId,
            String licenseReviewStatus,
            String result,
            Cancellation cancellation,
            List<String> allowedActions) {}

    public record AdminDetail(
            String orderNo,
            String fulfillmentStatus,
            String displayStatus,
            String cancelStatus,
            String custody,
            OwnerOrderView.Vehicle vehicle,
            OwnerOrderView.Station station,
            String slotStartAt,
            String slotEndAt,
            String returnedAt,
            long version,
            String view,
            String ownerAccountId,
            OwnerOrderView.Contact ownerContact,
            DriverIdentity driver,
            String assignmentId,
            String authoritativeDeptId,
            String paymentStatus,
            String refundStatus,
            String disputeStatus,
            OwnerOrderView.Money money,
            OwnerOrderView.Address pickupAddress,
            OwnerOrderView.Address returnAddress,
            OwnerOrderView.Mileage mileage,
            AfterSale afterSale,
            String payExpireAt,
            String dispatchAt,
            String autoConfirmAt,
            boolean driverReleased,
            List<String> allowedActions) {}

    public record DriverIdentity(
            String displayName, String driverCode, Double rating, OwnerOrderView.Contact contact) {}

    public record AfterSale(
            String caseId,
            String kind,
            String caseStatus,
            Long approvedAmount,
            String returnState,
            String refundState,
            String publicOpinion,
            long version) {}

    public record Cancellation(
            boolean returnRequired, String displayStatus, String publicInstruction) {}

    public record Timeline(
            String eventId,
            String orderNo,
            String action,
            String displayText,
            String actorLabel,
            String at) {}
}
