package com.xinyi.rongli.identity.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台员工微信关联读取（B36）。join sys_user/vi_account 取脱敏投影所需字段；
 * 列表按部门数据范围（任职相交）过滤，deptIds=null 表示全量（超级管理员）。只读，不写 sys_user。
 */
public interface EmployeeLinkAdminMapper {

    List<Map<String, Object>> selectBindings(@Param("deptIds") Set<Long> deptIds);

    Map<String, Object> selectAdminById(@Param("id") long id);
}