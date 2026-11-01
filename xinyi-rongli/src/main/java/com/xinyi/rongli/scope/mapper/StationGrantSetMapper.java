package com.xinyi.rongli.scope.mapper;

import com.xinyi.rongli.scope.domain.ViStationGrantSet;
import org.apache.ibatis.annotations.Param;

/** vi_station_grant_set 集合版本（含空集合，防 ABA）。uk_station_grant_employee 唯一。 */
public interface StationGrantSetMapper {

    ViStationGrantSet selectBySysUser(@Param("sysUserId") long sysUserId);

    ViStationGrantSet selectBySysUserForUpdate(@Param("sysUserId") long sysUserId);

    int insert(ViStationGrantSet set);

    /** CAS 更新集合（own_station_id/changed_by/reason，version+1）；受影响行数须为1。 */
    int casUpdate(@Param("id") long id, @Param("ownStationId") Long ownStationId,
                  @Param("changedBy") long changedBy, @Param("reason") String reason,
                  @Param("expectedVersion") long expectedVersion);
}