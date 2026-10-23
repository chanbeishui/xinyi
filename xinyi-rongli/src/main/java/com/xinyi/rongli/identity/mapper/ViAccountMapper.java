package com.xinyi.rongli.identity.mapper;

import com.xinyi.rongli.identity.domain.ViAccount;
import org.apache.ibatis.annotations.Param;

/**
 * vi_account Mapper。登录只经服务端可信 AppID/OpenID 创建/恢复账号；
 * uk_app_open 唯一键是"同 openid 并发只一账号"的第二道防线（插入冲突则重查）。
 */
public interface ViAccountMapper {
    int insert(ViAccount account);

    /**
     * 原子 get-or-create：依赖 uk_app_open 唯一键，插入冲突时经 ON DUPLICATE KEY UPDATE
     * id=LAST_INSERT_ID(id) 回填既有账号 id（不覆盖既有行）。保证"同 openid 并发只一账号"，
     * 无需捕获 DuplicateKeyException 后在只读事务里重查。调用后用 selectById 读取完整行。
     */
    int insertOrGetId(ViAccount account);

    ViAccount selectByAppOpen(@Param("appId") String appId, @Param("openId") String openId);

    ViAccount selectById(@Param("id") long id);

    /**
     * 锁定读（SELECT ... FOR UPDATE）：get-or-create 后读取账号。REPEATABLE_READ 下并发输家线程的
     * 一致性快照早于赢家提交，普通 selectById 读不到刚被 ON DUPLICATE 解析出的既有行；锁定读总读最新已提交，
     * 保证"同 openid 并发只一账号"后能可靠取回该账号。
     */
    ViAccount selectByIdForUpdate(@Param("id") long id);
}
