package com.xinyi.rongli.vehicle.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 后台车辆只读（A01）。join vi_account 取脱敏 owner 投影；关联订单/结果只读，不追改快照。 */
public interface VehicleAdminReadMapper {

    List<Map<String, Object>> selectVehicles(@Param("offset") int offset, @Param("limit") int limit);

    Map<String, Object> selectVehicleById(@Param("id") long id);

    List<Map<String, Object>> selectOrdersByVehicle(@Param("vehicleId") long vehicleId);

    List<Map<String, Object>> selectResultsByVehicle(@Param("vehicleId") long vehicleId);
}