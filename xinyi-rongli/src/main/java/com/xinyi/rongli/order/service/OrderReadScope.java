package com.xinyi.rongli.order.service;

import java.util.Set;

/** 只由Service从真实身份、角色数据范围和站点关系计算，禁止客户端绑定。null部门集合仅表示已确认ALL。 */
public record OrderReadScope(
        String view,
        Long accountId,
        Long driverId,
        Long stationId,
        Set<Long> departments,
        Set<Long> financeDepartments,
        Set<Long> addressDepartments,
        boolean canDetail,
        Set<Long> detailDepartments) {}
