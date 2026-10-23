package com.xinyi.rongli.support;

import com.xinyi.common.utils.SecurityUtils;

/**
 * 后台数据范围（部门维度）。
 *
 * 规范要求后台查询对接部门数据权限：超管或持全量权限的账号不限制；其余账号只看
 * 本人部门及下级部门。这里只负责给出「作用于哪个部门根节点」，具体过滤交给 SQL
 * 用 sys_dept.ancestors 展开，避免在内存里过滤破坏分页。
 */
public final class DeptScope
{
    private DeptScope() {}

    /**
     * @return null 表示不限制（超管）；否则为当前账号所在部门 id。
     * @throws ApiException 账号未分配部门时拒绝访问，避免退化成全量可见。
     */
    public static Long scopedDeptId()
    {
        if (SecurityUtils.isAdmin())
        {
            return null;
        }
        Long deptId;
        try
        {
            deptId = SecurityUtils.getDeptId();
        }
        catch (Exception ex)
        {
            deptId = null;
        }
        if (deptId == null)
        {
            throw ApiException.forbidden("当前账号未分配部门，无法查看业务数据");
        }
        return deptId;
    }
}
