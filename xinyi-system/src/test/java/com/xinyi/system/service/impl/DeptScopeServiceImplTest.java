package com.xinyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.xinyi.common.constant.Constants;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.mapper.SysDeptMapper;
import com.xinyi.system.mapper.SysRoleDeptMapper;
import com.xinyi.system.mapper.SysUserDeptMapper;

@ExtendWith(MockitoExtension.class)
class DeptScopeServiceImplTest
{
    @Mock
    private SysUserDeptMapper userDeptMapper;

    @Mock
    private SysDeptMapper deptMapper;

    @Mock
    private SysRoleDeptMapper roleDeptMapper;

    private DeptScopeServiceImpl service;

    @BeforeEach
    void setUp()
    {
        service = new DeptScopeServiceImpl(userDeptMapper, deptMapper, roleDeptMapper);
        ReflectionTestUtils.setField(service, "multiDeptQueryEnabled", true);
        when(userDeptMapper.selectActiveDeptIdsByUserId(9L)).thenReturn(List.of(10L, 20L));
    }

    @Test
    void scopeThreeIncludesAllMembershipDepartments()
    {
        LoginUser loginUser = loginUser(role(Constants.Dept.DATA_SCOPE_DEPT));

        DeptScopeResult result = service.resolve(loginUser, null);

        assertThat(result.getDeptIds()).containsExactlyInAnyOrder(10L, 20L);
        assertThat(result.isAllData()).isFalse();
    }

    @Test
    void scopeFourExpandsEveryMembershipDepartment()
    {
        when(deptMapper.selectDeptAndChildIds(anyList()))
                .thenReturn(List.of(10L, 11L, 20L, 21L));
        LoginUser loginUser = loginUser(role(Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD));

        DeptScopeResult result = service.resolve(loginUser, null);

        assertThat(result.getDeptIds()).containsExactlyInAnyOrder(10L, 11L, 20L, 21L);
    }

    @Test
    void multipleRolesUseUnionAndRetainSelfScope()
    {
        SysRole custom = role(Constants.Dept.DATA_SCOPE_CUSTOM);
        custom.setRoleId(7L);
        when(roleDeptMapper.selectDeptIdsByRoleIds(List.of(7L))).thenReturn(List.of(30L));
        LoginUser loginUser = loginUser(role(Constants.Dept.DATA_SCOPE_DEPT),
                custom, role(Constants.Dept.DATA_SCOPE_SELF));

        DeptScopeResult result = service.resolve(loginUser, null);

        assertThat(result.getDeptIds()).containsExactlyInAnyOrder(10L, 20L, 30L);
        assertThat(result.isIncludeSelf()).isTrue();
    }

    @Test
    void allDataRoleShortCircuitsDepartmentFiltering()
    {
        DeptScopeResult result = service.resolve(
                loginUser(role(Constants.Dept.DATA_SCOPE_ALL)), null);

        assertThat(result.isAllData()).isTrue();
        assertThat(result.getDeptIds()).isEmpty();
    }

    @Test
    void noApplicableRoleProducesEmptyFailClosedScope()
    {
        SysRole disabled = role(Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD);
        disabled.setStatus("1");

        DeptScopeResult result = service.resolve(loginUser(disabled), null);

        assertThat(result.isAllData()).isFalse();
        assertThat(result.isIncludeSelf()).isFalse();
        assertThat(result.getDeptIds()).isEmpty();
    }

    private LoginUser loginUser(SysRole... roles)
    {
        SysUser user = new SysUser();
        user.setUserId(9L);
        user.setDeptId(10L);
        user.setRoles(List.of(roles));
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(9L);
        loginUser.setDeptId(10L);
        loginUser.setUser(user);
        return loginUser;
    }

    private SysRole role(String dataScope)
    {
        SysRole role = new SysRole();
        role.setRoleId((long) dataScope.hashCode());
        role.setDataScope(dataScope);
        role.setStatus("0");
        return role;
    }
}
