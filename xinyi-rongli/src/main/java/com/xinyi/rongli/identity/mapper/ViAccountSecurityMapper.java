package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViAccountSecurity;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

/** vi_account_security Mapper。uk_account_security(account_id) 唯一；登录时读取 session_epoch 写入新会话。 */
public interface ViAccountSecurityMapper {
    int insert(ViAccountSecurity security);

    ViAccountSecurity selectByAccountId(@Param("accountId") long accountId);

    /** 角色/能力变化（如员工关联成功）递增 permission_version，客户端据此安全刷新会话。 */
    int bumpPermissionVersion(@Param("accountId") long accountId, @Param("now") Instant now);

    /** 全部设备退出/安全封禁：递增 session_epoch，使该账号所有旧会话代际不符而即时失效（DB 权威）。 */
    int bumpSessionEpoch(@Param("accountId") long accountId, @Param("now") Instant now);
}
