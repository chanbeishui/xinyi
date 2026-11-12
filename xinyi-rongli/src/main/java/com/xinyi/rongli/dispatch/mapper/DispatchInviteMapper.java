package com.xinyi.rongli.dispatch.mapper;

import com.xinyi.rongli.dispatch.domain.*;
import com.xinyi.rongli.order.domain.ViOrder;

import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.*;

/** 仅统一派单事务使用。部门集合为服务端计算，null仅表示明确ALL，空集合必须返回空。 */
public interface DispatchInviteMapper {
    DispatchPolicy policy(long owner);

    void insertPolicy(DispatchPolicy row);

    int updatePolicy(@Param("row") DispatchPolicy row, @Param("expected") long expected);

    int validVehicle(String code);

    int stationCompany(@Param("station") long station, @Param("company") long company);

    Long orderId(String orderNo);

    Map<String, Object> metadata(long orderId);

    List<ViOrder> queue(
            @Param("scope") Set<Long> scope,
            @Param("q") com.xinyi.rongli.dispatch.dto.InviteRequests.Query q);

    long queueCount(
            @Param("scope") Set<Long> scope,
            @Param("q") com.xinyi.rongli.dispatch.dto.InviteRequests.Query q);

    List<DispatchInvite> invites(
            @Param("scope") Set<Long> scope,
            @Param("driver") Long driver,
            @Param("q") com.xinyi.rongli.dispatch.dto.InviteRequests.Query q,
            @Param("now") Instant now);

    long inviteCount(
            @Param("scope") Set<Long> scope,
            @Param("driver") Long driver,
            @Param("q") com.xinyi.rongli.dispatch.dto.InviteRequests.Query q,
            @Param("now") Instant now);

    DispatchInvite invite(long id);

    DispatchInvite findInvite(long id);

    int otherResponsibilities(@Param("driver") long driver, @Param("ownInvite") long ownInvite);

    Map<String, Object> lockLiveSession(long id);

    int issuedReason(@Param("id") long id, @Param("cipher") byte[] cipher);

    DispatchInvite activeInvite(long orderId);

    DispatchBatch batch(long orderId);

    DispatchBatch batchById(long id);

    int nextRun(long orderId);

    void insertBatch(DispatchBatch row);

    int updateBatch(@Param("row") DispatchBatch row, @Param("expected") long expected);

    List<Long> drivers(long owner);

    int alreadyInvited(@Param("batch") long batch, @Param("driver") long driver);

    Map<String, Object> cursor(String key);

    void ensureCursor(String key);

    int advanceCursor(
            @Param("key") String key, @Param("driver") long driver, @Param("version") long version);

    void insertInvite(DispatchInvite row);

    int finishInvite(@Param("row") DispatchInvite row, @Param("expected") long expected);

    void occupy(DispatchInvite row);

    int release(
            @Param("driver") long driver, @Param("order") long order, @Param("invite") long invite);

    int promote(
            @Param("driver") long driver,
            @Param("order") long order,
            @Param("invite") long invite,
            @Param("assignment") long assignment);

    void insertAssignment(Map<String, Object> row);

    Long assignment(long invite);

    Long currentOrder(long driver);

    int transition(
            @Param("row") ViOrder row,
            @Param("state") String state,
            @Param("assignment") Long assignment);

    List<Long> pendingOrders();

    List<Long> dueInvites(@Param("now") Instant now);

    List<Long> activeBatches();
}
