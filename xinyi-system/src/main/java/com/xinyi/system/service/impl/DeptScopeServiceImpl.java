package com.xinyi.system.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import com.xinyi.common.constant.Constants;
import com.xinyi.common.constant.UserConstants;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.core.text.Convert;
import com.xinyi.system.domain.dto.SysUserQuery;
import com.xinyi.system.domain.dto.UserVisibilityContext;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.mapper.SysDeptMapper;
import com.xinyi.system.mapper.SysRoleDeptMapper;
import com.xinyi.system.mapper.SysUserDeptMapper;
import com.xinyi.system.service.IDeptScopeService;

/**
 * 以用户全部有效任职部门为基础解析角色数据范围。
 */
@Service
public class DeptScopeServiceImpl implements IDeptScopeService
{
    private final SysUserDeptMapper userDeptMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    @Value("${security.multi-dept-query-enabled:true}")
    private boolean multiDeptQueryEnabled;

    public DeptScopeServiceImpl(SysUserDeptMapper userDeptMapper, SysDeptMapper deptMapper,
            SysRoleDeptMapper roleDeptMapper)
    {
        this.userDeptMapper = userDeptMapper;
        this.deptMapper = deptMapper;
        this.roleDeptMapper = roleDeptMapper;
    }

    @Override
    public DeptScopeResult resolve(LoginUser loginUser, String permission)
    {
        DeptScopeResult result = new DeptScopeResult();
        if (loginUser == null || loginUser.getUser() == null)
        {
            return result;
        }
        if (loginUser.getUser().isAdmin())
        {
            result.setAllData(true);
            return result;
        }

        List<Long> memberships = selectActiveMembershipDeptIds(loginUser.getUserId());
        if (!multiDeptQueryEnabled && loginUser.getDeptId() != null)
        {
            memberships = Collections.singletonList(loginUser.getDeptId());
        }
        List<Long> customRoleIds = new ArrayList<>();
        List<SysRole> roles = loginUser.getUser().getRoles();
        if (roles == null)
        {
            roles = Collections.emptyList();
        }
        for (SysRole role : roles)
        {
            if (!roleApplies(role, permission))
            {
                continue;
            }
            String scope = role.getDataScope();
            if (Constants.Dept.DATA_SCOPE_ALL.equals(scope))
            {
                result.setAllData(true);
                return result;
            }
            if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(scope))
            {
                customRoleIds.add(role.getRoleId());
            }
            else if (Constants.Dept.DATA_SCOPE_DEPT.equals(scope))
            {
                result.addDeptIds(memberships);
            }
            else if (Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD.equals(scope))
            {
                result.addDeptIds(expandDeptIds(memberships));
            }
            else if (Constants.Dept.DATA_SCOPE_SELF.equals(scope))
            {
                result.setIncludeSelf(true);
            }
        }
        if (!customRoleIds.isEmpty())
        {
            result.addDeptIds(roleDeptMapper.selectDeptIdsByRoleIds(customRoleIds));
        }
        return result;
    }

    @Override
    public UserVisibilityContext buildUserVisibility(SysUserQuery query, String permission)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        DeptScopeResult scope = resolve(loginUser, permission);
        UserVisibilityContext visibility = new UserVisibilityContext();
        visibility.setAllData(scope.isAllData());
        visibility.setIncludeSelf(scope.isIncludeSelf());
        visibility.setCurrentUserId(loginUser.getUserId());
        visibility.setVisibleDeptIds(new ArrayList<>(scope.getDeptIds()));
        if (query != null && query.getDeptId() != null)
        {
            List<Long> searchIds = expandDeptIds(Collections.singletonList(query.getDeptId()));
            visibility.setSearchDeptIds(searchIds.isEmpty() ? Collections.singletonList(-1L) : searchIds);
        }
        return visibility;
    }

    @Override
    public List<Long> selectActiveMembershipDeptIds(Long userId)
    {
        List<Long> ids = userDeptMapper.selectActiveDeptIdsByUserId(userId);
        return ids == null ? Collections.emptyList() : ids;
    }

    @Override
    public List<Long> selectMembershipDeptIds(Long userId)
    {
        List<Long> ids = userDeptMapper.selectDeptIdsByUserId(userId);
        return ids == null ? Collections.emptyList() : ids;
    }

    @Override
    public void checkDeptAccess(Collection<Long> deptIds)
    {
        Set<Long> requested = normalize(deptIds);
        if (requested.isEmpty())
        {
            throw new ServiceException("部门范围不能为空");
        }
        List<Long> active = deptMapper.selectActiveDeptIds(new ArrayList<>(requested));
        if (active == null || active.size() != requested.size())
        {
            throw new ServiceException("任职部门不存在、已删除或已停用");
        }
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        DeptScopeResult scope = resolve(SecurityUtils.getLoginUser(), null);
        if (!scope.isAllData() && !scope.getDeptIds().containsAll(requested))
        {
            throw new ServiceException("没有权限操作所选部门");
        }
    }

    @Override
    public void checkUserFullAccess(Long userId)
    {
        if (SecurityUtils.isAdmin() || SecurityUtils.getUserId().equals(userId))
        {
            return;
        }
        List<Long> targetDeptIds = selectMembershipDeptIds(userId);
        if (targetDeptIds.isEmpty())
        {
            throw new ServiceException("目标用户没有有效的任职部门关系");
        }
        DeptScopeResult scope = resolve(SecurityUtils.getLoginUser(), null);
        if (!scope.isAllData() && !scope.getDeptIds().containsAll(targetDeptIds))
        {
            throw new ServiceException("没有权限访问目标用户的全部任职部门");
        }
    }

    private List<Long> expandDeptIds(Collection<Long> deptIds)
    {
        Set<Long> ids = normalize(deptIds);
        if (ids.isEmpty())
        {
            return Collections.emptyList();
        }
        List<Long> expanded = deptMapper.selectDeptAndChildIds(new ArrayList<>(ids));
        return expanded == null ? Collections.emptyList() : expanded;
    }

    private boolean roleApplies(SysRole role, String permission)
    {
        if (role == null || UserConstants.ROLE_DISABLE.equals(role.getStatus()))
        {
            return false;
        }
        return StringUtils.isEmpty(permission)
                || StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission));
    }

    private Set<Long> normalize(Collection<Long> ids)
    {
        if (CollectionUtils.isEmpty(ids))
        {
            return Collections.emptySet();
        }
        return ids.stream().filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
