package com.xinyi.rongli.scope.dto;

import java.util.List;
import java.util.Map;

/**
 * 员工获授权站点公开投影（I06，API_094，Station）。仅启用站点；不暴露无权站点单量/司机/订单。
 */
public record AuthorizedStation(
        String stationId,
        String name,
        String address,
        Map<String, Object> location,
        String phoneMask,
        boolean enabled,
        List<String> supportedVehicleTypeCodes,
        String businessHours,
        int serviceRadiusMeter,
        long version) {
}