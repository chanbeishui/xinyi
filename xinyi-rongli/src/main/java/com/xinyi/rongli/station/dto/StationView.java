package com.xinyi.rongli.station.dto;

import java.util.List;

/** 站点视图（Station，ADMIN/员工域）。联系电话脱敏；含 enabled 与 version（CAS）。 */
public record StationView(
        String stationId,
        String name,
        String address,
        GeoPoint location,
        String phoneMask,
        String deptId,
        boolean enabled,
        List<String> supportedVehicleTypeCodes,
        String businessHours,
        int serviceRadiusMeter,
        long version) {
}