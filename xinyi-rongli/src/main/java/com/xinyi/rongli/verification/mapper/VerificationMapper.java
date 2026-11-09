package com.xinyi.rongli.verification.mapper;

import com.xinyi.rongli.verification.domain.*;
import com.xinyi.rongli.verification.dto.VerificationRequests;

import org.apache.ibatis.annotations.Param;

import java.util.*;

/** 共用调用方持目录与当前申请锁；查询范围全部由真实权限解析而非客户端数据片段。 */
public interface VerificationMapper {
    List<Map<String, Object>> consent(@Param("lock") boolean lock);

    Long applicationAccount(long applicationId);

    DeviceAuthenticationSession find(long id);

    DeviceAuthenticationSession lock(long id);

    DeviceAuthenticationSession byRequest(String requestNo);

    DeviceAuthenticationSession active(
            @Param("application") long application,
            @Param("session") long session,
            @Param("context") long context);

    DeviceAuthenticationSession current(
            @Param("application") long application,
            @Param("company") long company,
            @Param("epoch") long epoch,
            @Param("material") long material,
            @Param("hash") byte[] hash,
            @Param("session") long session,
            @Param("context") long context);

    void insertSession(DeviceAuthenticationSession row);

    int saveSession(DeviceAuthenticationSession row);

    List<DeviceAuthenticationSession> list(
            @Param("scope") Set<Long> scope,
            @Param("query") VerificationRequests.Query query,
            @Param("offset") long offset);

    long count(@Param("scope") Set<Long> scope, @Param("query") VerificationRequests.Query query);

    List<VerificationProof> currentProofs(
            @Param("application") long application,
            @Param("company") long company,
            @Param("epoch") long epoch,
            @Param("material") long material,
            @Param("hash") byte[] hash);

    VerificationProof proof(long id);

    VerificationProof sourceProof(
            @Param("kind") String kind, @Param("source") long source, @Param("type") String type);

    void insertProof(VerificationProof proof);

    int sealProof(@Param("id") long id, @Param("cipher") byte[] cipher);
}
