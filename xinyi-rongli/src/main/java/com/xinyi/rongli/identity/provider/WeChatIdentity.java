package com.xinyi.rongli.identity.provider;

/**
 * 服务端可信换取的微信身份。appId/openId 一律来自 provider 换取结果，绝不接收客户端指定；
 * unionId 仅在供应商可信返回时记录。不含 session_key 等凭据（SecretGuard 约束）。
 */
public record WeChatIdentity(String appId, String openId, String unionId) {
}
