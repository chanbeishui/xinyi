package com.xinyi.rongli.scope.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** vi_station_grant 子授权（增减以 enabled 标记，保留历史；uk_user_station 唯一）。 */
public interface StationGrantMapper {

    List<Map<String, Object>> selectBySysUser(@Param("sysUserId") long sysUserId);

    List<Long> selectEnabledStationIds(@Param("sysUserId") long sysUserId);

    int insert(@Param("sysUserId") long sysUserId, @Param("stationId") long stationId,
               @Param("grantedBy") long grantedBy, @Param("reason") String reason);

    int setEnabled(@Param("sysUserId") long sysUserId, @Param("stationId") long stationId,
                   @Param("enabled") int enabled, @Param("grantedBy") long grantedBy, @Param("reason") String reason);
}