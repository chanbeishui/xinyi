package com.xinyi.common.security;

/**
 * 用户会话失效接口。
 */
public interface SessionInvalidator
{
    /**
     * 终止用户全部在线会话。
     *
     * @param userId 用户ID
     */
    void forceLogoutByUserId(Long userId);

    /**
     * 刷新用户在线会话中的展示型资料，不终止会话、不改动权限快照。
     *
     * <p>昵称、邮箱、手机号、性别、备注、头像不参与权限判定，改这些字段没有必要让用户重新登录；
     * 但登录时写入缓存的用户对象是副本，不刷新会让页面一直显示旧资料。</p>
     *
     * @param userId 用户ID
     */
    void refreshUserProfileSnapshot(Long userId);
}
