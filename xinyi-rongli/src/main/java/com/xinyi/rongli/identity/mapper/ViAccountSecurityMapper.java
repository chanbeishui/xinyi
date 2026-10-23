package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViAccountSecurity;
import org.apache.ibatis.annotations.Param;

/** vi_account_security Mapper。uk_account_security(account_id) 唯一；登录时读取 session_epoch 写入新会话。 */
public interface ViAccountSecurityMapper {
    int insert(ViAccountSecurity security);

    ViAccountSecurity selectByAccountId(@Param("accountId") long accountId);
}
