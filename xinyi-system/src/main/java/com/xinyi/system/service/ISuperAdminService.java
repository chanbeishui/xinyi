package com.xinyi.system.service;

/**
 * 最高管理员判定。
 *
 * <p>口径：内置超管（{@code user_id = 1}）<b>或</b> 持有超级管理员角色（{@code role_id = 1}）。
 * 只认 userId=1 会让"最高管理员"退化成单点：唯一的最高管理员既不能改自己的角色/部门，
 * 又没有第二个账号能代劳，相关字段就会永久锁死。角色才是权限载体，持有超级管理员角色的账号
 * 本就应当拥有全部权限，承认它不扩大攻击面；而授予该角色的入口本身只对最高管理员开放并有审计。</p>
 */
public interface ISuperAdminService
{
    /**
     * 指定用户是否为最高管理员。
     *
     * @param userId 用户 ID，允许为空（返回 false）
     * @return true 表示内置超管或持有超级管理员角色
     */
    boolean isSuperAdmin(Long userId);

    /**
     * 当前登录用户是否为最高管理员。
     */
    boolean isCurrentUserSuperAdmin();
}
