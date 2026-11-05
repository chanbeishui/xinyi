package com.xinyi.system.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.system.mapper.SysUserRoleMapper;
import com.xinyi.system.service.ISuperAdminService;

/**
 * 最高管理员判定的唯一实现，供权限计算、菜单查询与授权变更共用。
 */
@Service
public class SuperAdminServiceImpl implements ISuperAdminService
{
    /** 内置超级管理员角色 ID。 */
    private static final Long SUPER_ADMIN_ROLE_ID = 1L;

    private final SysUserRoleMapper userRoleMapper;

    public SuperAdminServiceImpl(SysUserRoleMapper userRoleMapper)
    {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public boolean isSuperAdmin(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        if (SecurityUtils.isAdmin(userId))
        {
            return true;
        }
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds != null && roleIds.contains(SUPER_ADMIN_ROLE_ID);
    }

    @Override
    public boolean isCurrentUserSuperAdmin()
    {
        return isSuperAdmin(SecurityUtils.getUserId());
    }
}
