package com.xinyi.rongli.platform.mapper;

import com.xinyi.rongli.platform.domain.ViIdempotency;
import org.apache.ibatis.annotations.Param;

/**
 * vi_idempotency 数据访问。纯 MyBatis，不引入 MyBatis-Plus（ADR-05）。
 * 唯一键 uk_request(subject_key,action_key,request_key) 由数据库保证；
 * 服务层捕获 DuplicateKeyException 后按 request_mac 判定重放或 409。
 */
public interface ViIdempotencyMapper {

    /** 插入 PROCESSING 占位；created_at/updated_at/version 用数据库默认（UTC）。返回受影响行数。 */
    int insertProcessing(ViIdempotency record);

    /** 按唯一键读取（含 request_mac、status、result_json、version）。 */
    ViIdempotency selectByKey(@Param("subjectKey") String subjectKey,
                              @Param("actionKey") String actionKey,
                              @Param("requestKey") String requestKey);

    /** CAS 置 DONE 并写入可重放结果；version 必须匹配，受影响行数须为 1。 */
    int markDone(@Param("id") long id,
                 @Param("resultJson") String resultJson,
                 @Param("expectedVersion") long expectedVersion);

    /** CAS 置 DONE 但不缓存结果（含明文令牌的登录/刷新类动作）；result_json 保持 NULL。 */
    int markDoneNoResult(@Param("id") long id,
                         @Param("expectedVersion") long expectedVersion);

    /** 清理过期占位（expires_at < now）；返回删除行数。仅供维护任务调用。 */
    int deleteExpired(@Param("now") java.time.Instant now);
}
