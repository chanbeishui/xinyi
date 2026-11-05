package com.xinyi.rongli.aftersale.mapper;

import com.xinyi.rongli.aftersale.domain.AftersaleCase;
import com.xinyi.rongli.aftersale.dto.AftersaleRequests.Query;
import java.util.*;
import org.apache.ibatis.annotations.Param;

public interface AftersaleMapper {
    AftersaleCase find(long id);

    AftersaleCase lock(long id);

    AftersaleCase active(long orderId);

    void insert(AftersaleCase row);

    int markCancellation(@Param("id") long id, @Param("version") long version);

    Map<String, Object> balance(long paymentId);

    List<AftersaleCase> list(
            @Param("owner") long owner,
            @Param("orderId") Long orderId,
            @Param("query") Query query,
            @Param("offset") long offset);

    long count(
            @Param("owner") long owner,
            @Param("orderId") Long orderId,
            @Param("query") Query query);

    int increment(@Param("id") long id, @Param("version") long version);

    void insertNote(
            @Param("caseId") long caseId,
            @Param("accountId") long accountId,
            @Param("note") String note,
            @Param("evidence") String evidence);

    List<Map<String, Object>> notes(long caseId);

    String refundNo(long caseId);
}
