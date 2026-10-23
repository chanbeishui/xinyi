package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViSessionToken;
import org.apache.ibatis.annotations.Param;

/** vi_session_token Mapper。只存摘要；uk_token_hash 唯一。按 (token_hash, token_type) 解析访问令牌。 */
public interface ViSessionTokenMapper {
    int insert(ViSessionToken token);

    ViSessionToken selectByHash(@Param("tokenHash") byte[] tokenHash, @Param("tokenType") String tokenType);
}
