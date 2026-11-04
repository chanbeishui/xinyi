package com.xinyi.rongli.booking.dto;

import com.xinyi.rongli.address.dto.GeoPoint;

/** 已有车辆/地址ID与临时资料分别二选一；stationId由车主从服务端候选中选择。 */
public record QuoteInput(
        String slotId,
        String stationId,
        String inspectionTypeCode,
        String vehicleId,
        VehicleDraft vehicleDraft,
        String addressId,
        AddressDraft addressDraft) {
    public record VehicleDraft(
            String plateNo,
            String vehicleTypeCode,
            String brandModel,
            String useType,
            String vinLast4,
            Boolean isDefault) {}

    public record AddressDraft(
            String address, GeoPoint location, String label, Boolean isDefault) {}
}
