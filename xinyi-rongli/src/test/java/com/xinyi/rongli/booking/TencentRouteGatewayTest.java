package com.xinyi.rongli.booking;
import com.xinyi.rongli.booking.route.*;
import com.xinyi.rongli.platform.error.ViException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class TencentRouteGatewayTest {
 @Test void 真实HTTP协议校验坐标顺序及分钟转秒() throws Exception {
  HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/route", exchange->{
   String query=exchange.getRequestURI().getRawQuery();
   boolean valid=query.contains("from=21.0,110.0")&&query.contains("to=22.0,111.0");
   byte[] bytes=(valid?"{\"status\":0,\"result\":{\"routes\":[{\"distance\":12345,\"duration\":20.01}]}}":"{}").getBytes(StandardCharsets.UTF_8);
   exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
  });server.start();
  try {
   var gateway=new TencentRouteGateway("test-only","http://127.0.0.1:"+server.getAddress().getPort()+"/route","integration");
   var result=gateway.driving(new RouteGateway.Point(110,21),new RouteGateway.Point(111,22));
   assertEquals(12345,result.distanceMeter());assertEquals(1201,result.durationSecond());
  }finally{server.stop(0);}
 }
 @Test void 缺凭据非法坐标与生产替身关闭() {
  assertThrows(IllegalArgumentException.class,()->new TencentRouteGateway("x","http://127.0.0.1/route","production"));
  var gateway=new TencentRouteGateway("","https://apis.map.qq.com/ws/direction/v1/driving/","production");
  assertThrows(ViException.class,()->gateway.driving(new RouteGateway.Point(110,21),new RouteGateway.Point(111,22)));
  assertThrows(ViException.class,()->gateway.driving(new RouteGateway.Point(Double.NaN,21),new RouteGateway.Point(111,22)));
 }
}
