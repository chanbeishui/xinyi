package com.xinyi.rongli.identity.domain;

/** 小程序视角角色。新账号只有 OWNER；DRIVER 须审核通过；STATION 须有效员工关联+站点授权。 */
public enum MiniRole {
    OWNER, DRIVER, STATION
}
