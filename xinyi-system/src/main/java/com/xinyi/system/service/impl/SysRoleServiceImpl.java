package com.xinyi.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.common.annotation.DataScope;
import com.xinyi.common.constant.UserConstants;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.spring.SpringUtils;
import com.xinyi.system.domain.SysRoleDept;
import com.xinyi.system.domain.SysRoleMenu;
import com.xinyi.system.domain.SysUserRole;
import com.xinyi.system.mapper.SysRoleDeptMapper;
import com.xinyi.system.mapper.SysRoleMapper;
import com.xinyi.system.mapper.SysRoleMenuMapper;
import com.xinyi.system.mapper.SysUserRoleMapper;
import com.xinyi.system.service.ISysRoleService;
import com.xinyi.system.service.ISecurityAuditService;
import com.xinyi.system.service.IUserAuthorizationMutationService;

/**
 * 角色 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    @Autowired
    private IUserAuthorizationMutationService authorizationMutationService;

    @Autowired
    private ISecurityAuditService securityAuditService;

    /**
     * 根据条件分页查询角色数据
     * 
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根据用户ID查询角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * 根据用户ID获取角色选择框列表
     * 
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     * 
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     * 
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     * 
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (!SecurityUtils.isAdmin())
        {
            for (Long roleId : roleIds)
            {
                SysRole role = new SysRole();
                role.setRoleId(roleId);
                List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
                if (StringUtils.isEmpty(roles))
                {
                    ServiceException ex = new ServiceException("没有权限访问角色数据！");
                    securityAuditService.recordFailure("ROLE_ACCESS_DENIED", null,
                            "角色数据范围拒绝", null, Map.of("roleId", roleId),
                            "DATA_SCOPE_DENIED", ex.getMessage());
                    throw ex;
                }
            }
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRole(SysRole role, String reason)
    {
        requireReason(reason);
        try
        {
            roleMapper.insertRole(role);
            int rows = insertRoleMenu(role);
            securityAuditService.recordSuccess("ROLE_CREATE", null, reason,
                    null, roleSnapshot(role), null, null);
            return rows;
        }
        catch (RuntimeException ex)
        {
            securityAuditService.recordFailure("ROLE_CREATE", null, reason,
                    null, roleSnapshot(role), "ROLE_MUTATION_FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 修改保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRole(SysRole role, String reason)
    {
        requireReason(reason);
        Map<String, Object> before = roleSnapshot(roleMapper.selectRoleById(role.getRoleId()));
        try
        {
            roleMapper.updateRole(roleEditUpdate(role));
            roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
            int rows = insertRoleMenu(role);
            authorizationMutationService.invalidateUsersByRole(role.getRoleId(),
                    "ROLE_PERMISSION_UPDATE", reason);
            SysRole persisted = roleMapper.selectRoleById(role.getRoleId());
            persisted.setMenuIds(role.getMenuIds());
            securityAuditService.recordSuccess("ROLE_PERMISSION_CONFIG_UPDATE", null, reason,
                    before, roleSnapshot(persisted), null, null);
            return rows;
        }
        catch (RuntimeException ex)
        {
            securityAuditService.recordFailure("ROLE_PERMISSION_CONFIG_UPDATE", null, reason,
                    before, roleSnapshot(role), "ROLE_MUTATION_FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 修改角色状态
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRoleStatus(SysRole role, String reason)
    {
        requireReason(reason);
        Map<String, Object> before = roleSnapshot(roleMapper.selectRoleById(role.getRoleId()));
        try
        {
            SysRole statusUpdate = new SysRole();
            statusUpdate.setRoleId(role.getRoleId());
            statusUpdate.setStatus(role.getStatus());
            statusUpdate.setUpdateBy(role.getUpdateBy());
            int rows = roleMapper.updateRole(statusUpdate);
            if (rows > 0)
            {
                authorizationMutationService.invalidateUsersByRole(role.getRoleId(),
                        "ROLE_STATUS_UPDATE", reason);
                securityAuditService.recordSuccess("ROLE_STATUS_CONFIG_UPDATE", null, reason,
                        before, roleSnapshot(roleMapper.selectRoleById(role.getRoleId())), null, null);
            }
            return rows;
        }
        catch (RuntimeException ex)
        {
            securityAuditService.recordFailure("ROLE_STATUS_CONFIG_UPDATE", null, reason,
                    before, roleSnapshot(role), "ROLE_MUTATION_FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 修改数据权限信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role, String reason)
    {
        requireReason(reason);
        Map<String, Object> before = roleSnapshot(roleMapper.selectRoleById(role.getRoleId()));
        try
        {
            SysRole scopeUpdate = new SysRole();
            scopeUpdate.setRoleId(role.getRoleId());
            scopeUpdate.setDataScope(role.getDataScope());
            scopeUpdate.setDeptCheckStrictly(role.isDeptCheckStrictly());
            scopeUpdate.setUpdateBy(role.getUpdateBy());
            roleMapper.updateRole(scopeUpdate);
            roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
            int rows = insertRoleDept(role);
            authorizationMutationService.invalidateUsersByRole(role.getRoleId(),
                    "ROLE_DATA_SCOPE_UPDATE", reason);
            SysRole persisted = roleMapper.selectRoleById(role.getRoleId());
            persisted.setDeptIds(role.getDeptIds());
            securityAuditService.recordSuccess("ROLE_DATA_SCOPE_CONFIG_UPDATE", null, reason,
                    before, roleSnapshot(persisted), null, null);
            return rows;
        }
        catch (RuntimeException ex)
        {
            securityAuditService.recordFailure("ROLE_DATA_SCOPE_CONFIG_UPDATE", null, reason,
                    before, roleSnapshot(role), "ROLE_MUTATION_FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 新增角色菜单信息
     * 
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleById(Long roleId, String reason)
    {
        return deleteRoleByIds(new Long[] { roleId }, reason);
    }

    /**
     * 批量删除角色信息
     * 
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds, String reason)
    {
        requireReason(reason);
        List<Map<String, Object>> before = new ArrayList<>();
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
            before.add(roleSnapshot(role));
        }
        try
        {
            roleMenuMapper.deleteRoleMenu(roleIds);
            roleDeptMapper.deleteRoleDept(roleIds);
            int rows = roleMapper.deleteRoleByIds(roleIds);
            securityAuditService.recordSuccess("ROLE_DELETE", null, reason,
                    before, null, null, null);
            return rows;
        }
        catch (RuntimeException ex)
        {
            securityAuditService.recordFailure("ROLE_DELETE", null, reason,
                    before, roleIds, "ROLE_MUTATION_FAILED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取消授权用户角色
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        throw new ServiceException("角色撤销必须调用统一授权服务并填写原因");
    }

    /**
     * 批量取消授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        throw new ServiceException("批量角色撤销必须调用统一授权服务并填写原因");
    }

    /**
     * 批量选择授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        throw new ServiceException("批量角色授予必须调用统一授权服务并填写原因");
    }

    private void requireReason(String reason)
    {
        if (StringUtils.isBlank(reason))
        {
            throw new ServiceException("角色权限相关操作必须填写原因");
        }
    }

    private Map<String, Object> roleSnapshot(SysRole role)
    {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        if (role == null)
        {
            return snapshot;
        }
        snapshot.put("roleId", role.getRoleId());
        snapshot.put("roleName", role.getRoleName());
        snapshot.put("roleKey", role.getRoleKey());
        snapshot.put("dataScope", role.getDataScope());
        snapshot.put("status", role.getStatus());
        snapshot.put("menuIds", role.getMenuIds());
        snapshot.put("deptIds", role.getDeptIds());
        return snapshot;
    }

    private SysRole roleEditUpdate(SysRole role)
    {
        SysRole update = new SysRole();
        update.setRoleId(role.getRoleId());
        update.setRoleName(role.getRoleName());
        update.setRoleKey(role.getRoleKey());
        update.setRoleSort(role.getRoleSort());
        update.setMenuCheckStrictly(role.isMenuCheckStrictly());
        update.setRemark(role.getRemark());
        update.setUpdateBy(role.getUpdateBy());
        return update;
    }
}
