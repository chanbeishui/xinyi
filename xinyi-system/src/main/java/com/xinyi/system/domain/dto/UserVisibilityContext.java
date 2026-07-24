package com.xinyi.system.domain.dto;

import java.util.Collections;
import java.util.List;

/**
 * 服务端生成的用户列表可见性上下文。
 */
public class UserVisibilityContext
{
    private boolean allData;
    private boolean includeSelf;
    private Long currentUserId;
    private List<Long> visibleDeptIds = Collections.emptyList();
    private List<Long> searchDeptIds = Collections.emptyList();

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

    public Long getCurrentUserId()
    {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId)
    {
        this.currentUserId = currentUserId;
    }

    public List<Long> getVisibleDeptIds()
    {
        return visibleDeptIds;
    }

    public void setVisibleDeptIds(List<Long> visibleDeptIds)
    {
        this.visibleDeptIds = visibleDeptIds;
    }

    public List<Long> getSearchDeptIds()
    {
        return searchDeptIds;
    }

    public void setSearchDeptIds(List<Long> searchDeptIds)
    {
        this.searchDeptIds = searchDeptIds;
    }
}
