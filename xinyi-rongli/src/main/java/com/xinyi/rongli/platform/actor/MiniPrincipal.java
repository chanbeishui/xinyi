package com.xinyi.rongli.platform.actor;

/**
 * 小程序可信主体视图（ADR-01）。E06 仅定义只读契约，供 BusinessActorResolver 解析双主体审计；
 * 具体认证实现（不透明令牌签发/刷新轮换/会话持久化 vi_session*）由 I01/I03 提供并实现本接口。
 * 本接口不可由 HTTP 反序列化构造；只能由服务端在可信会话校验后填充。
 * 明文 token/credential/launchUrl 不属于本视图，禁止经此进入审计或幂等缓存。
 */
public interface MiniPrincipal {
    ActorRealm realm();          // 恒为 MINI
    long accountId();            // vi_account 内部 Long
    Long linkedSysUserId();      // 经 vi_employee_binding 可信绑定的 sys_user，可空
    Long sessionId();            // vi_session 内部 Long，可空
    String activeRole();         // OWNER/DRIVER/STATION 当前视角
    Long stationId();            // STATION 视角下的站点，可空
    Long contextVersion();       // 视角上下文版本，可空
}
