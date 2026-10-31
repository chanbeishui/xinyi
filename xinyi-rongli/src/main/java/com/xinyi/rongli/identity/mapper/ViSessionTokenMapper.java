package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViSessionToken;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

/** vi_session_token Mapper。只存摘要；uk_token_hash 唯一。按 (token_hash, token_type) 解析访问令牌。 */
public interface ViSessionTokenMapper {
    int insert(ViSessionToken token);

    ViSessionToken selectByHash(@Param("tokenHash") byte[] tokenHash, @Param("tokenType") String tokenType);

    ViSessionToken selectByIdForUpdate(@Param("id") long id);

    /** 原子消费刷新令牌（CAS，consumed_at 仅置一次）；受影响行数=1 表示本次轮换独占成功。 */
    int consumeRefresh(@Param("id") long id, @Param("consumedAt") Instant consumedAt,
                       @Param("replacedByTokenId") Long replacedByTokenId, @Param("expectedVersion") long expectedVersion);

    /** 撤销某会话的全部令牌（刷新重用检测→撤会话族，或退出）。 */
    int revokeBySession(@Param("sessionId") long sessionId, @Param("revokedAt") Instant revokedAt);
}
