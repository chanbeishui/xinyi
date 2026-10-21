package com.xinyi.rongli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.xinyi.rongli.support.MiniAuthInterceptor;

/**
 * 小程序接口拦截配置。仅 /api/app/** 与 /api/common/** 进入 MINI 认证域；
 * 登录、令牌刷新、支付回调与公开配置单独放行。
 */
@Configuration
public class RongliWebConfig implements WebMvcConfigurer
{
    @Autowired
    private MiniAuthInterceptor miniAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(miniAuthInterceptor)
                .addPathPatterns("/api/app/**", "/api/common/**")
                .excludePathPatterns(
                        "/api/common/login/wechat",
                        "/api/common/token/refresh",
                        "/api/public/**",
                        "/api/notify/**")
                .order(1);
    }
}
