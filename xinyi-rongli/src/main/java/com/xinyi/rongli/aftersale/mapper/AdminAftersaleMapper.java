package com.xinyi.rongli.aftersale.mapper;

import com.xinyi.rongli.aftersale.domain.AftersaleCase;
import com.xinyi.rongli.aftersale.dto.AftersaleRequests.Query;
import java.util.*;
import org.apache.ibatis.annotations.Param;

public interface AdminAftersaleMapper {
    List<AftersaleCase> list(
            @Param("departments") Set<Long> departments,
            @Param("orderId") Long orderId,
            @Param("ownerId") Long ownerId,
            @Param("query") Query query,
            @Param("offset") long offset);

    long count(
            @Param("departments") Set<Long> departments,
            @Param("orderId") Long orderId,
            @Param("ownerId") Long ownerId,
            @Param("query") Query query);

    Long orderId(String no);

    void insertNote(
            @Param("caseId") long caseId,
            @Param("userId") long userId,
            @Param("note") String note,
            @Param("evidence") String evidence);

    List<Long> contributorAccounts(long caseId);

    List<Long> boundUsers(long accountId);

    Map<String, Object> accountPhone(long accountId);

    List<Map<String, Object>> usersWithPhone();

    String reviewerPhone(long userId);

    int contributor(@Param("caseId") long caseId, @Param("userId") long userId);

    void addParticipant(@Param("caseId") long caseId, @Param("userId") long userId);

    String ownerLabel(long accountId);

    int pickupHandover(long orderId);

    List<Map<String, Object>> reviews(long caseId);

    int reviewCase(AftersaleCase row);

    void insertReview(
            @Param("row") AftersaleCase row,
            @Param("decision") String decision,
            @Param("orderVersion") long orderVersion,
            @Param("custody") String custody,
            @Param("returnRequired") boolean returnRequired,
            @Param("reviewer") long reviewer,
            @Param("opinion") String opinion);

    int reserve(@Param("id") long id, @Param("amount") long amount);

    void createRefund(
            @Param("row") AftersaleCase row,
            @Param("refundNo") String refundNo,
            @Param("method") String method,
            @Param("mode") String mode);

    int cancelOrder(
            @Param("id") long id,
            @Param("version") long version,
            @Param("waitReturn") boolean waitReturn);

    int rejectCancellation(@Param("id") long id, @Param("version") long version);

    List<Map<String, Object>> invites(long orderId);

    List<Map<String, Object>> assignments(long orderId);

    int revokeInvite(@Param("id") long id, @Param("orderId") long orderId);

    int endAssignment(@Param("id") long id, @Param("orderId") long orderId);

    int releaseOccupancy(
            @Param("driverId") long driverId,
            @Param("orderId") long orderId,
            @Param("holderId") long holderId,
            @Param("holderType") String holderType);

    List<Map<String, Object>> reservations(long orderId);

    int releaseQuota(
            @Param("slotId") long slotId,
            @Param("quantity") int quantity,
            @Param("status") String status);

    int releaseReservation(@Param("id") long id, @Param("status") String status);
}
