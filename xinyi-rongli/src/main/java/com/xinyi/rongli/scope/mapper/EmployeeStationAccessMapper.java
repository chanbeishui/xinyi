package com.xinyi.rongli.scope.mapper;

import org.apache.ibatis.annotations.Param;

/** 员工工作权限只读校验，不使用小程序会话中的旧授权，也不继承后台超级管理员特权。 */
public interface EmployeeStationAccessMapper {
    int countAllowed(@Param("accountId") long accountId,
                     @Param("stationId") long stationId, @Param("permission") String permission);
}
