package com.xinyi.rongli.vehicle.dto;

/** 本人车辆投影（A01，MINI Vehicle）。不含生成列 active_plate/owner/vin；version 供 CAS。 */
public record VehicleView(
        String vehicleId,
        String plateNo,
        String vehicleTypeCode,
        String vehicleTypeLabel,
        String brandModel,
        long version,
        boolean isDefault) {
}