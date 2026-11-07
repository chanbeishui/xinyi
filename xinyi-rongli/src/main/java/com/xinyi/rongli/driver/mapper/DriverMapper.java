package com.xinyi.rongli.driver.mapper;

import com.xinyi.rongli.driver.domain.DriverProfile;
import com.xinyi.rongli.driver.dto.DriverRequests.*;
import java.util.*;
import org.apache.ibatis.annotations.Param;

public interface DriverMapper {
    DriverProfile find(long id);

    DriverProfile lock(long id);

    List<DriverProfile> list(
            @Param("scope") Set<Long> scope,
            @Param("search") Search search,
            @Param("offset") long offset);

    long count(@Param("scope") Set<Long> scope, @Param("search") Search search);

    Map<String, Object> company(long id);

    int busy(long id);

    Map<String, Object> occupancy(long id);

    List<Map<String, Object>> assignments(
            @Param("id") long id,
            @Param("scope") Set<Long> scope,
            @Param("query") Query query,
            @Param("offset") long offset);

    long assignmentCount(
            @Param("id") long id, @Param("scope") Set<Long> scope, @Param("query") Query query);

    Long completedCount(@Param("id") long id, @Param("scope") Set<Long> scope);

    Long visibleAssignment(@Param("id") long id, @Param("scope") Set<Long> scope);

    int maintain(
            @Param("row") DriverProfile row,
            @Param("ownerPresent") boolean ownerPresent,
            @Param("remarkPresent") boolean remarkPresent);

    int switchNew(
            @Param("id") long id,
            @Param("version") long version,
            @Param("enabled") boolean enabled);
}
