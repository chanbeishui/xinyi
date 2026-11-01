package com.xinyi.rongli.scope.dto;

import java.util.List;

/**
 * 员工站点授权集合投影（I06，StationGrantView）。按 sys_user 返回，无 OpenID/vi_account 也能查改；
 * version 来自 vi_station_grant_set（含空集合）；wechatLinkStatus 仅粗粒度，不泄微信标识。
 */
public record StationGrantView(
        String employeeUserId,
        String ownStationId,
        List<String> authorizedStationIds,
        long version,
        String wechatLinkStatus,
        String updatedAt) {
}