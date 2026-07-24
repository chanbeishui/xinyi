package com.xinyi.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xinyi.system.domain.SysUserDept;

/**
 * 用户任职部门数据层。
 */
public interface SysUserDeptMapper
{
    List<Long> selectDeptIdsByUserId(Long userId);

    List<Long> selectActiveDeptIdsByUserId(Long userId);

    List<SysUserDept> selectByUserIds(@Param("userIds") Collection<Long> userIds);

    List<Long> selectUserIdsByDeptId(Long deptId);

    int countByDeptId(Long deptId);

    int deleteByUserId(Long userId);

    int deleteByUserIds(Long[] userIds);

    int batchInsert(@Param("items") Collection<SysUserDept> items);

    int countIntegrityIssues();
}
