package com.xinyi.rongli.jobs.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DispatchDeadlineMapper {
    List<Long> missingTimers();

    int stationAvailable(@Param("stationId") long stationId, @Param("deptId") long deptId);

    int confirmedReservations(long orderId);

    int pendingRefund(long orderId);

    String quotePolicy(long quoteId);

    int transition(
            @Param("id") long id, @Param("version") long version, @Param("state") String state);
}
