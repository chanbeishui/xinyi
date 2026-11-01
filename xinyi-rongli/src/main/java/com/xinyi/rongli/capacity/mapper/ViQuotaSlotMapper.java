package com.xinyi.rongli.capacity.mapper;

import com.xinyi.rongli.capacity.domain.ViQuotaSlot;
import com.xinyi.rongli.capacity.dto.CapacityQuery;
import org.apache.ibatis.annotations.Param;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface ViQuotaSlotMapper {
    ViQuotaSlot selectById(@Param("id") long id);
    ViQuotaSlot lockById(@Param("id") long id);
    List<ViQuotaSlot> selectList(@Param("query") CapacityQuery query, @Param("deptIds") Set<Long> deptIds,
                               @Param("stationId") Long stationId, @Param("offset") int offset);
    long countList(@Param("query") CapacityQuery query, @Param("deptIds") Set<Long> deptIds, @Param("stationId") Long stationId);
    int countOverlap(@Param("row") ViQuotaSlot row);
    int insert(ViQuotaSlot row);
    int change(@Param("id") long id, @Param("total") int total, @Param("enabled") Boolean enabled,
               @Param("expectedVersion") long expectedVersion);
}
