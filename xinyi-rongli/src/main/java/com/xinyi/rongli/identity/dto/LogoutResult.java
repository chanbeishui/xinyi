package com.xinyi.rongli.identity.dto;

/**
 * 退出结果（LogoutResult）。loggedOut 恒 true；scope=CURRENT（仅当前会话）/ALL（全部设备，递增 session_epoch）。
 * 确认会话撤销；不返回 Token，不改变订单或司机任务（TC-R63-008）。
 */
public record LogoutResult(boolean loggedOut, String scope) {
    public static LogoutResult current() {
        return new LogoutResult(true, "CURRENT");
    }

    public static LogoutResult all() {
        return new LogoutResult(true, "ALL");
    }
}
