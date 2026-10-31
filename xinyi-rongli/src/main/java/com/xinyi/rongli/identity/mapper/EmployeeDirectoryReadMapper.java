package com.xinyi.rongli.identity.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 框架人员目录只读（sys_user）。自动关联按服务端规范化号码查询有效员工，DISTINCT user_id 计数
 * （不因多部门 join 重复算多名）；仅读，不写 sys_user（其写入由框架统一授权服务/钩子纳管）。
 */
public interface EmployeeDirectoryReadMapper {

    /** 有效（status='0' 且 del_flag='0'）且手机号匹配的员工 user_id 列表（去重）。 */
    List<Long> selectActiveUserIdsByPhone(@Param("phone") String phone);

    /** 员工当前框架授权版本（建立绑定时快照）。 */
    Long selectUserAuthVersion(@Param("sysUserId") long sysUserId);

    /** 员工是否在职有效（status='0' 且 del_flag='0'）；工作权限实时 fail-closed 校验。 */
    int countActiveUserById(@Param("sysUserId") long sysUserId);

    /** 员工当前目录手机号（目录钩子换号比对用）。 */
    String selectUserPhone(@Param("sysUserId") long sysUserId);
}
