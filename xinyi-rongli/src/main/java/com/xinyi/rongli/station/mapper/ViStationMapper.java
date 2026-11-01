package com.xinyi.rongli.station.mapper;

import com.xinyi.rongli.station.domain.ViStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/** 站点 Mapper（C02）。后台与员工共用；行锁+CAS；deptIds=null 全量、空集合服务层 fail-closed。 */
@Mapper
public interface ViStationMapper {

    ViStation selectByIdForUpdate(@Param("id") long id);

    ViStation selectById(@Param("id") long id);

    List<ViStation> selectList(@Param("deptIds") Set<Long> deptIds, @Param("enabledOnly") boolean enabledOnly,
                               @Param("offset") int offset, @Param("limit") int limit);

    long countList(@Param("deptIds") Set<Long> deptIds, @Param("enabledOnly") boolean enabledOnly);

    int insert(ViStation station);

    int update(@Param("s") ViStation station, @Param("expectedVersion") long expectedVersion);

    long countActiveOrders(@Param("stationId") long stationId);
}