package com.xinyi.rongli.booking.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.json.ViJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

/** 腾讯驾车路线：外部调用由报价事务外发起，异常不记录含密钥和坐标的URI。 */
@Component
public class TencentRouteGateway implements RouteGateway {
    private final String key;
    private final URI endpoint;
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
    public TencentRouteGateway(@Value("${vi.maps.tencent.key:}") String key,
            @Value("${vi.maps.tencent.endpoint:https://apis.map.qq.com/ws/direction/v1/driving/}") String endpoint,
            @Value("${vi.environment:local}") String environment) {
        this.key=key;this.endpoint=URI.create(endpoint);
        boolean official="https://apis.map.qq.com/ws/direction/v1/driving/".equals(endpoint);
        boolean local=Set.of("local","integration","test").contains(environment)
                && "http".equals(this.endpoint.getScheme()) && Set.of("127.0.0.1","localhost").contains(this.endpoint.getHost())
                && this.endpoint.getRawQuery()==null && this.endpoint.getUserInfo()==null && this.endpoint.getFragment()==null;
        if(!official&&!local)throw new IllegalArgumentException("地图端点必须为腾讯正式HTTPS地址；仅隔离测试允许本机HTTP替身");
    }
    @Override public Route driving(Point from,Point to) {
        valid(from);valid(to);
        if(key==null||key.isBlank())throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED,"尚未配置腾讯路线服务");
        String query="?key="+URLEncoder.encode(key,StandardCharsets.UTF_8)+"&from="+from.lat()+","+from.lng()+"&to="+to.lat()+","+to.lng()+"&policy=LEAST_TIME";
        try {
            var request=HttpRequest.newBuilder(URI.create(endpoint+query)).timeout(Duration.ofSeconds(6)).GET().build();
            var response=http.send(request,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if(response.statusCode()!=200)throw unavailable();
            return parse(response.body());
        }catch(InterruptedException e){Thread.currentThread().interrupt();throw unavailable();}
        catch(ViException e){throw e;}
        catch(Exception e){throw unavailable();}
    }
    static Route parse(String json) {
        try {
            JsonNode root=ViJson.tree(json);
            if(!root.path("status").isIntegralNumber()||root.path("status").asInt()!=0)throw unavailable();
            JsonNode route=root.path("result").path("routes").path(0);
            if(!route.path("distance").isNumber()||!route.path("duration").isNumber())throw unavailable();
            long distance=new BigDecimal(route.get("distance").asText()).longValueExact();
            long seconds=new BigDecimal(route.get("duration").asText()).multiply(BigDecimal.valueOf(60)).setScale(0,RoundingMode.CEILING).longValueExact();
            if(distance<=0||distance>Integer.MAX_VALUE||seconds<=0||seconds>604800)throw unavailable();
            return new Route(distance,seconds,"TENCENT","LEAST_TIME");
        }catch(RuntimeException e){throw unavailable();}
    }
    private static void valid(Point p) {
        if(p==null||!Double.isFinite(p.lng())||!Double.isFinite(p.lat())||Math.abs(p.lng())>180||Math.abs(p.lat())>90)
            throw new ViException(ViErrorCode.VI_DATA_INVALID,"路线坐标无效");
    }
    private static ViException unavailable(){return new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE,"路线暂不可用，请稍后重试；未产生报价");}
}
