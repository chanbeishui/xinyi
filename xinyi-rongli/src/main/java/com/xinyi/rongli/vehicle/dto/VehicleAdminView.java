package com.xinyi.rongli.vehicle.dto;

import java.util.List;

/**
 * 后台车辆记录（A01，VehicleRecord）。一行=已授权客户-车辆关系，不凭同车牌跨账户合并；
 * VIN/手机号脱敏；不含生成列；allowedActions 服务端计算；维护不追改订单快照。
 */
public record VehicleAdminView(
        String vehicleId,
        String plateNo,
        String vehicleTypeCode,
        String vehicleTypeLabel,
        String brandModel,
        String vinMask,
        String registeredAt,
        String useType,
        OwnerRef ownerRelation,
        String verificationStatus,
        String lastResult,
        String lastInspectAt,
        long version,
        List<String> allowedActions) {

    public record OwnerRef(String ownerId, String phoneMask) {
    }
}