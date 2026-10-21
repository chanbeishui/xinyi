package com.xinyi.rongli.platform.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * vi 平台 web 基座装配。当前仅注册 X-HTTP-Method-Override 过滤器（修正 D-E04-1）。
 * 顺序设为 -200，早于 Spring Security 过滤链（默认 -100），使安全匹配也看到改写后的方法；
 * urlPatterns 限定 /api/*，不影响 /system 等框架端点。
 *
 * 说明：本配置不注册任何业务 Controller，也不重建框架认证；vi 后台/小程序 Controller 由后续任务实现，
 * 届时复用本过滤器与 platform 服务。@RestControllerAdvice 错误信封渲染留待首个 vi Controller 任务，
 * 避免在无 Controller 时引入空切面（E06 服务层直接抛 ViException，由 IT 断言其 errorCode/状态）。
 */
@Configuration
public class ViWebConfig {

    @Bean
    public FilterRegistrationBean<HttpMethodOverrideFilter> viHttpMethodOverrideFilter() {
        FilterRegistrationBean<HttpMethodOverrideFilter> registration =
                new FilterRegistrationBean<>(new HttpMethodOverrideFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(-200);
        registration.setName("viHttpMethodOverrideFilter");
        return registration;
    }
}
