package com.xinyi.rongli.payment;
import com.xinyi.rongli.payment.gateway.WechatPrepayGateway;
import com.xinyi.rongli.platform.error.ViException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PaymentEnvironmentTest {
 @Test void simulatorCannotBeConfiguredInProductionOrPointOutsideLoopback(){
  assertThrows(IllegalArgumentException.class,()->new WechatPrepayGateway("production","http://127.0.0.1:1234","https://example.com"));
  for(String endpoint:new String[]{"https://example.com","http://localhost:1234","http://127.0.0.1:1234/path","http://user@127.0.0.1:1234"})assertThrows(IllegalArgumentException.class,()->new WechatPrepayGateway("integration",endpoint,"https://example.com"));
 }
 @Test void nonLiveEnvironmentCannotFallThroughToRealProviderAndCallbackIsFixed(){
  assertThrows(ViException.class,()->new WechatPrepayGateway("local","","https://example.com").available());
  assertThrows(ViException.class,()->new WechatPrepayGateway("production","","http://example.com").available());
  assertEquals("https://example.com/api/notify/wechat/pay",new WechatPrepayGateway("production","","https://example.com/").notifyUrl());
 }
}
