package com.xinyi.rongli.common.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Component;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;

/**
 * 后台数据范围（部门维度）。
 *
 * <p>数据来源是框架的 {@link IDeptScopeService}：以 {@code sys_user_dept} 的<b>全部有效任职部门</b>为基础，
 * 再按角色 {@code data_scope} 解析（全部 / 本部门 / 本部门及以下 / 自定义 / 仅本人）。
 * 早期实现只取 {@code sys_user.dept_id} 主部门，多部门任职的用户只能看到主部门的数据，
 * 与《部门数据权限与安全审计开发规范》冲突。</p>
 *
 * <p>返回值约定：{@code null} 表示不受部门限制（超管或角色持全部数据权限）；空集合不会返回，
 * 权限集合为空时直接拒绝，避免退化成全量可见。</p>
 */
@Component
public class DeptScope
{
    private final IDeptScopeService deptScopeService;

    public DeptScope(IDeptScopeService deptScopeService)
    {
        this.deptScopeService = deptScopeService;
    }

    /**
     * 列表可见部门集合：按角色数据范围解析，“本部门及以下”已展开成具体部门 id，
     * 调用方直接用于 {@code dept_id IN (...)}，不在内存里过滤，避免破坏分页。
     *
     * @param permission 功能权限点，用于区分同名角色；传 null 表示不区分
     * @return null 表示不受限
     */
    public List<Long> visibleDeptIds(String permission)
    {
        DeptScopeResult scope = deptScopeService.resolve(loginUser(), permission);
        if (scope.isAllData())
        {
            return null;
        }
        List<Long> ids = new ArrayList<>(new LinkedHashSet<>(scope.getDeptIds()));
        if (ids.isEmpty())
        {
            throw ApiException.forbidden("当前账号没有可用的部门数据权限");
        }
        return ids;
    }

    public List<Long> visibleDeptIds()
    {
        return visibleDeptIds(null);
    }

    /**
     * 任职部门集合（不含下级），用于“这条记录的归属部门是否属于我”的精确判断。
     *
     * @return null 表示不受限
     */
    public List<Long> assignedDeptIds()
    {
        LoginUser loginUser = loginUser();
        SysUser user = loginUser == null ? null : loginUser.getUser();
        if (user != null && user.isAdmin())
        {
            return null;
        }
        DeptScopeResult scope = deptScopeService.resolve(loginUser, null);
        if (scope.isAllData())
        {
            return null;
        }
        List<Long> ids = deptScopeService.selectActiveMembershipDeptIds(user.getUserId());
        if (ids == null || ids.isEmpty())
        {
            throw ApiException.forbidden("当前账号没有有效的任职部门");
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    /** 记录归属部门是否落在任职范围内；不受限时恒为 true。 */
    public boolean coversAssigned(Long deptId)
    {
        List<Long> assigned = assignedDeptIds();
        return assigned == null || (deptId != null && assigned.contains(deptId));
    }

    /** 归属校验：记录部门必须在任职范围内，否则拒绝。 */
    public void requireAssigned(Long deptId, String message)
    {
        if (!coversAssigned(deptId))
        {
            throw ApiException.forbidden(message);
        }
    }

    /**
     * 单值形式的任职部门，供“一条记录只归属一个部门”的查询与写入使用。
     *
     * <p>优先返回主部门（{@code sys_user.dept_id}）；主部门不在任职范围内时退回任职部门中的第一个。</p>
     *
     * @return null 表示不受限
     */
    public Long primaryAssignedDeptId()
    {
        List<Long> assigned = assignedDeptIds();
        if (assigned == null)
        {
            return null;
        }
        Long primary = currentDeptId();
        if (primary != null && assigned.contains(primary))
        {
            return primary;
        }
        return assigned.get(0);
    }

    /**
     * 写入时的目标归属部门。
     *
     * <p>不受限时沿用请求值（请求为空则取当前账号主部门）；受限时请求值必须落在任职范围内，
     * 未指定则取 {@link #primaryAssignedDeptId()}，避免多部门账号把数据写到无权维护的部门。</p>
     */
    public Long writeDeptId(Long requested, String message)
    {
        List<Long> assigned = assignedDeptIds();
        if (assigned == null)
        {
            return requested == null ? currentDeptId() : requested;
        }
        if (requested != null)
        {
            if (!assigned.contains(requested))
            {
                throw ApiException.forbidden(message);
            }
            return requested;
        }
        return primaryAssignedDeptId();
    }

    /** 空集合安全包装，供调用方拼接 SQL 前判断。 */
    public static List<Long> orEmpty(List<Long> ids)
    {
        return ids == null ? Collections.emptyList() : ids;
    }

    private LoginUser loginUser()
    {
        try
        {
            return SecurityUtils.getLoginUser();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private Long currentDeptId()
    {
        try
        {
            return SecurityUtils.getDeptId();
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
