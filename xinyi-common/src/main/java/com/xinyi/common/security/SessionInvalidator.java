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
}
