package com.xinyi.common.security;

/**
 * 用户会话失效接口。
 */
public interface SessionInvalidator
{
    /**
     * 终止用户所有"授权快照已过期"的在线会话。
     *
     * <p>实现按 auth_version 与账号状态逐个比对：快照已与数据库一致的会话保留，
     * 否则删除。这样同一方法既能用于强制下线，也能与
     * {@link #refreshCurrentSessionAuthorization(Long)} 配合实现"只保留操作者当前会话"。</p>
     *
     * @param userId 用户ID
     */
    void forceLogoutByUserId(Long userId);

    /**
     * 把操作者本人的当前请求会话重载到最新授权快照（角色、权限、部门、authVersion），
     * 保留原 token 与剩余有效期。
     *
     * <p>用于"管理员修改自己的角色/部门"：他的权限确实变了，但他是这次变更的操作者，
     * 已经在一次已认证的请求里完成了校验，没有必要再让他重新登录一次。
     * 其他设备上的会话仍会在 {@link #forceLogoutByUserId(Long)} 中被淘汰。</p>
     *
     * @param userId 用户ID
     */
    void refreshCurrentSessionAuthorization(Long userId);

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
