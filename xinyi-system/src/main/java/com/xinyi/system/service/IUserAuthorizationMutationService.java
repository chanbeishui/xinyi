package com.xinyi.system.service;

import java.util.Collection;
import java.util.List;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.system.domain.dto.SysUserCreateRequest;
import com.xinyi.system.domain.dto.SysUserDeptUpdateRequest;
import com.xinyi.system.domain.dto.SysUserManagementScopeRequest;
import com.xinyi.system.domain.dto.SysUserPasswordResetRequest;
import com.xinyi.system.domain.dto.SysUserRoleUpdateRequest;
import com.xinyi.system.domain.dto.SysUserStatusUpdateRequest;
import com.xinyi.system.domain.dto.SysUserUpdateRequest;

/**
 * 用户授权与账户敏感信息唯一写入口。
 */
public interface IUserAuthorizationMutationService
{
    boolean registerUser(SysUser user);

    Long createUser(SysUserCreateRequest request);

    int updateUserBase(SysUserUpdateRequest request);

    int updateDepartments(Long userId, SysUserDeptUpdateRequest request);

    int updateRoles(Long userId, SysUserRoleUpdateRequest request);

    int updateManagementScope(Long userId, SysUserManagementScopeRequest request);

    int updateStatus(SysUserStatusUpdateRequest request);

    int resetPassword(SysUserPasswordResetRequest request);

    int changeOwnPassword(Long userId, String newPassword);

    int deleteUsers(Long[] userIds, String reason);

    int grantRoleToUsers(Long roleId, Long[] userIds, String reason);

    int revokeRoleFromUsers(Long roleId, Long[] userIds, String reason);

    int importUsers(List<SysUser> users, boolean updateSupport, String reason, String initialPassword);

    void invalidateUsersByRole(Long roleId, String eventType, String reason);

    void invalidateUsersByDept(Long deptId, String eventType, String reason);

    void invalidateUsers(Collection<Long> userIds, String eventType, String reason);
}
