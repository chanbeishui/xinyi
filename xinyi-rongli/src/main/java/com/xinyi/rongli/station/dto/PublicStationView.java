package com.xinyi.rongli.station.dto;

import java.util.List;

/** 公开站点营业信息（PublicStationView，MINI /api/common/stations）。禁止包含授权范围/人员/内部状态/版本。 */
public record PublicStationView(
        String stationId,
        String name,
        String address,
        GeoPoint location,
        String phoneMask,
        List<String> supportedVehicleTypeCodes,
        String businessHours,
        int serviceRadiusMeter) {
}