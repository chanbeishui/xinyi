package com.xinyi.rongli.payment.dto;
/** 参数一旦发起即冻结；持久化前整体加密，禁止记录OpenID。 */
public record PrepaySnapshot(String appId,String mchid,String openId,String outTradeNo,long totalFen,String expiresAt,String notifyUrl) {
 @Override public String toString(){return "PrepaySnapshot[REDACTED]";}
}
