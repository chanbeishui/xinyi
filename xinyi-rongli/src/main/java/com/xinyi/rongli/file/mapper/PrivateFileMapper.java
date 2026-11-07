package com.xinyi.rongli.file.mapper;

import com.xinyi.rongli.file.domain.PrivateFile;
import com.xinyi.rongli.order.domain.ViOrder;

import org.apache.ibatis.annotations.Param;

import java.time.Instant;

public interface PrivateFileMapper {
    PrivateFile lock(long id);

    PrivateFile find(long id);

    void insert(PrivateFile row);

    int claim(
            @Param("id") long id,
            @Param("version") long version,
            @Param("token") String token,
            @Param("until") Instant until);

    int uploaded(@Param("id") long id, @Param("token") String token);

    int finish(PrivateFile row);

    ViOrder orderByNo(String no);

    Long applicationOwner(long id);

    java.util.Map<String, Object> lockApplication(long id);

    Long stationDept(long id);

    int activeDriver(@Param("orderId") long orderId, @Param("accountId") long accountId);

    int aftersaleEvidence(@Param("orderId") long orderId, @Param("fileId") long fileId);
}
