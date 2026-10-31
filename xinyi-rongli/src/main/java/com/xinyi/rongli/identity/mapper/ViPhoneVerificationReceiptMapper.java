package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViPhoneVerificationReceipt;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

/**
 * vi_phone_verification_receipt Mapper（CONTROLLED_LIFECYCLE）。
 * uk_phone_request(account_id,request_id) 与 uk_phone_code(app_id,code_hash) 是去重/防重放的第二道防线。
 * 状态推进用条件 CAS（受影响行数须为1），租约领取避免并发重复消费供应商凭据。
 */
public interface ViPhoneVerificationReceiptMapper {

    int insert(ViPhoneVerificationReceipt receipt);

    ViPhoneVerificationReceipt selectByRequest(@Param("accountId") long accountId,
                                               @Param("requestId") String requestId);

    ViPhoneVerificationReceipt selectByCode(@Param("appId") String appId, @Param("codeHash") byte[] codeHash);

    ViPhoneVerificationReceipt selectByIdForUpdate(@Param("id") long id);

    /** 领取处理租约：仅当当前无有效租约时置 lease_until，受影响行数=1 表示领取成功（独占一次外部消费）。 */
    int claimLease(@Param("id") long id, @Param("now") Instant now,
                   @Param("leaseUntil") Instant leaseUntil, @Param("expectedVersion") long expectedVersion);

    /** 核验成功：原子保存手机号事实并置 VERIFIED（CAS）。 */
    int markVerified(@Param("id") long id, @Param("phoneLookup") byte[] phoneLookup,
                     @Param("phoneCipher") byte[] phoneCipher, @Param("verifiedAt") Instant verifiedAt,
                     @Param("employeeLinkOutcome") String employeeLinkOutcome,
                     @Param("finishedAt") Instant finishedAt, @Param("expectedVersion") long expectedVersion);

    /** 置终态 UNKNOWN/FAILED 并记录内部 outcome（CAS），释放租约。 */
    int markTerminal(@Param("id") long id, @Param("status") String status,
                     @Param("employeeLinkOutcome") String employeeLinkOutcome,
                     @Param("finishedAt") Instant finishedAt, @Param("expectedVersion") long expectedVersion);

    /** 安全重试关联后仅更新 outcome（CAS），不改手机号验证事实。 */
    int updateOutcome(@Param("id") long id, @Param("employeeLinkOutcome") String employeeLinkOutcome,
                      @Param("expectedVersion") long expectedVersion);
}
