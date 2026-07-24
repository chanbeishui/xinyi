package com.xinyi.system.domain.vo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 解析后的部门数据范围。
 */
public class DeptScopeResult
{
    private boolean allData;
    private boolean includeSelf;
    private Set<Long> deptIds = new LinkedHashSet<>();

    public boolean isAllData()
    {
        return allData;
    }

    public void setAllData(boolean allData)
    {
        this.allData = allData;
    }

    public boolean isIncludeSelf()
    {
        return includeSelf;
    }

    public void setIncludeSelf(boolean includeSelf)
    {
        this.includeSelf = includeSelf;
    }

    public Set<Long> getDeptIds()
    {
        return Collections.unmodifiableSet(deptIds);
    }

    public void addDeptIds(Iterable<Long> ids)
    {
        if (ids != null)
        {
            ids.forEach(id -> {
                if (id != null)
                {
                    deptIds.add(id);
                }
            });
        }
    }
}
