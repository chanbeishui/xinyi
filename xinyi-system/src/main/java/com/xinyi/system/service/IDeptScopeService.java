package com.xinyi.system.service;

import java.util.Collection;
import java.util.List;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.system.domain.dto.SysUserQuery;
import com.xinyi.system.domain.dto.UserVisibilityContext;
import com.xinyi.system.domain.vo.DeptScopeResult;

/**
 * 部门数据范围统一解析服务。
 */
public interface IDeptScopeService
{
    DeptScopeResult resolve(LoginUser loginUser, String permission);

    UserVisibilityContext buildUserVisibility(SysUserQuery query, String permission);

    List<Long> selectActiveMembershipDeptIds(Long userId);

    List<Long> selectMembershipDeptIds(Long userId);

    void checkDeptAccess(Collection<Long> deptIds);

    void checkUserFullAccess(Long userId);
}
