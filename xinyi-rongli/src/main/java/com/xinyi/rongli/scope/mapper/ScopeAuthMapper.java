package com.xinyi.rongli.scope.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 受控授权写（I06，V6.4 §5）：站点集合变更与框架 auth_version 同事务递增（控制路径，伴随审计/outbox）。
 * 不写 sys_user_dept/sys_user_role；仅 auth_version 小字段，供会话/权限实时失效。
 */
public interface ScopeAuthMapper {

    int bumpAuthVersion(@Param("sysUserId") long sysUserId);
}