package com.xinyi.rongli.station.dto;

import java.util.List;

/** 站点创建/更新输入（服务端解析校验后构造，不由客户端直接反序列化）。deptId 为 ADMIN 创建必填及受控迁移字段，员工端拒绝。 */
public record StationInput(
        String name,
        String address,
        GeoPoint location,
        String contactPhone,
        Boolean enabled,
        List<String> supportedVehicleTypeCodes,
        String businessHours,
        Integer serviceRadiusMeter,
        List<String> qualificationFileIds,
        Long expectedVersion,
        Long deptId) {
}