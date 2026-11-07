package com.xinyi.rongli.refund.mapper;

import com.xinyi.rongli.refund.domain.*;
import java.time.Instant;
import java.util.*;
import org.apache.ibatis.annotations.Param;

public interface RefundMapper {
    Map<String, Object> balance(long paymentId);

    Map<String, Object> ledger(long paymentId);

    RefundRecord find(long id);

    RefundRecord lock(long id);

    RefundRecord byNo(String no);

    RefundRecord byCase(@Param("caseId") long caseId, @Param("revision") int revision);

    Long orderId(long paymentId);

    Map<String, Object> review(@Param("caseId") long caseId, @Param("revision") int revision);

    int claim(
            @Param("id") long id,
            @Param("version") long version,
            @Param("token") String token,
            @Param("submit") boolean submit);

    int markSubmission(
            @Param("id") long id, @Param("version") long version, @Param("token") String token);

    int finish(@Param("row") RefundRecord row, @Param("token") String token);

    int state(RefundRecord row);

    int settleCase(
            @Param("caseId") long caseId,
            @Param("refundState") String refundState,
            @Param("close") boolean close);

    int paidBalance(
            @Param("paymentId") long paymentId,
            @Param("amount") long amount,
            @Param("success") boolean success);

    List<Long> due();

    List<Map<String, Object>> problems();

    ReconciliationRecord reconciliation(long id);

    ReconciliationRecord lockReconciliation(long id);

    ReconciliationRecord reconciliationSubject(
            @Param("kind") String kind, @Param("subjectId") long subjectId);

    void insertReconciliation(ReconciliationRecord row);

    int reopen(ReconciliationRecord row);

    int checked(ReconciliationRecord row);

    int resolve(@Param("id") long id, @Param("version") long version, @Param("userId") long userId);

    List<ReconciliationRecord> list(
            @Param("departments") Set<Long> departments,
            @Param("offset") long offset,
            @Param("size") int size,
            @Param("query") com.xinyi.rongli.refund.dto.RefundRequests.Query query);

    long count(
            @Param("departments") Set<Long> departments,
            @Param("query") com.xinyi.rongli.refund.dto.RefundRequests.Query query);
}
