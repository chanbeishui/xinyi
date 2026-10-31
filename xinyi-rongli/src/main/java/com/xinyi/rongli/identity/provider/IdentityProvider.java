package com.xinyi.rongli.identity.provider;

/**
 * 微信身份 provider 端口（ADR-08 供应商适配层）。以 wx.login 临时 code 换取可信 AppID/OpenID。
 * 实现按 vi.providers.mode 显式选择：SIMULATOR 仅用于隔离/集成测试，WECHAT 为真实渠道（L01 门禁）。
 * 外部换取绝不置于数据库锁内；凭据/session_key 不落库、不进日志。
 */
public interface IdentityProvider {
    WeChatIdentity exchangeCode(String code);

    /**
     * 以用户主动授权的微信手机号临时 phoneCode 换取可信号码（不同于 wx.login code）。
     * 一次性消费；结果不明返回 UNKNOWN（不假成功）。外部调用绝不置于数据库锁/事务内。
     */
    PhoneExchangeResult exchangePhone(String phoneCode);
}
