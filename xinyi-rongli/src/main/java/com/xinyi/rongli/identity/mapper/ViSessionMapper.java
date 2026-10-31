package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViSession;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

/** vi_session Mapper。每设备一条会话；令牌解析时按 id 读取并校验状态/代际/绝对截止。 */
public interface ViSessionMapper {
    int insert(ViSession session);

    ViSession selectById(@Param("id") long id);

    ViSession selectByIdForUpdate(@Param("id") long id);

    /** CAS 撤销当前会话（status=REVOKED，revoked_at）；DB 权威撤权，不依赖 Redis。 */
    int revokeSession(@Param("id") long id, @Param("revokedAt") Instant revokedAt,
                      @Param("expectedVersion") long expectedVersion);

    /** CAS 切换角色/站点：仅当 context_version 匹配且会话 ACTIVE 时原子递增上下文（I04，T3.4）。 */
    int switchContext(@Param("id") long id, @Param("activeRole") String activeRole,
                      @Param("stationId") Long stationId, @Param("expectedContextVersion") long expectedContextVersion);
}
