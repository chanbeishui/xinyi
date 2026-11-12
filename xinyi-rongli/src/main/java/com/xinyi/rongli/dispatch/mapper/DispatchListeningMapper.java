package com.xinyi.rongli.dispatch.mapper;

import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.driver.domain.DriverProfile;

import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.*;

/** 所有归属从真实档案读取；调用者先持目录、账号/会话和档案锁，不能使用客户端数据权限片段。 */
public interface DispatchListeningMapper {
    DriverProfile lockProfile(long accountId);

    Long account(long driverId);

    ListeningCompanyConfig config(long ownerDeptId);

    void insertConfig(ListeningCompanyConfig row);

    int updateConfig(@Param("row") ListeningCompanyConfig row, @Param("expected") long expected);

    List<Long> companyDrivers(long ownerDeptId);

    int saveListening(
            @Param("row") DriverProfile row,
            @Param("enabled") boolean enabled,
            @Param("until") Instant until,
            @Param("epoch") Long epoch,
            @Param("config") Long config);

    DriverLocation location(long driverId);

    DriverPresence presence(long driverId);

    void insertLocation(DriverLocation row);

    int updateLocation(@Param("row") DriverLocation row, @Param("expected") long expected);

    void insertPresence(DriverPresence row);

    int updatePresence(@Param("row") DriverPresence row, @Param("expected") long expected);

    Map<String, Object> liveSession(long sessionId);

    List<Long> penalties(@Param("driver") long driver, @Param("now") Instant now);

    Map<String, Object> occupation(long driverId);

    List<Long> dueDrivers(@Param("now") Instant now);

    List<Long> missingTimers();
}
