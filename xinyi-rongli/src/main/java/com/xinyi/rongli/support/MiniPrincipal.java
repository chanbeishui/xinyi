package com.xinyi.rongli.support;

/**
 * 小程序侧可信身份上下文。MINI 认证域与后台 ADMIN 认证域完全隔离：
 * accountId 是 vi_account 主键，绝不是 sys_user 主键。
 */
public class MiniPrincipal
{
    public Long accountId;

    public Long sessionId;

    public String appId;

    public String role;

    public Long stationId;

    public Long linkedSysUserId;

    public long contextVersion;

    public String nickname;

    public String phone;

    public boolean isOwnerRole()
    {
        return Codes.Role.OWNER.equals(role);
    }

    public boolean isDriverRole()
    {
        return Codes.Role.DRIVER.equals(role);
    }

    public boolean isStationRole()
    {
        return Codes.Role.STATION.equals(role);
    }

    public Long requireStation()
    {
        if (stationId == null)
        {
            throw ApiException.forbidden("当前会话未选择检测点，请先切换站点");
        }
        return stationId;
    }
}
