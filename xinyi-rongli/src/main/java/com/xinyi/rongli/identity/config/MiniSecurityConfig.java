package com.xinyi.rongli.identity.config;

import com.xinyi.rongli.identity.service.MiniTokenService;
import com.xinyi.rongli.identity.web.MiniAuthenticationEntryPoint;
import com.xinyi.rongli.identity.web.MiniTokenAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * MINI 认证域安全链（ADR-01 两认证域）。仅匹配 /api/** 且排除 /api/admin/**（后台 ADMIN 域交框架链 JWT+@PreAuthorize），
 * @Order(0) 优先于框架未排序链（框架 SecurityConfig 继续兜底 ADMIN /login、/system/**、/api/admin/** 等，互不干扰、令牌不可交叉）。
 * 精确放行登录/刷新端点，其余 MINI /api/** 一律 authenticated（严禁默认 permitAll）；无状态；
 * 认证失败由 MiniAuthenticationEntryPoint 返回真实 401 + ApiError（非框架"恒 200"）。
 * 先例：仓库 ViIntegrationTestConfiguration 的 @Order+securityMatcher 多链模式。
 */
@Configuration
public class MiniSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain miniApiFilterChain(HttpSecurity http, MiniTokenService tokenService) throws Exception {
        http
                .securityMatcher(request -> {
                    String uri = request.getRequestURI();
                    return uri.startsWith("/api/") && !uri.startsWith("/api/admin/");
                })
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .anonymous(anon -> anon.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new MiniAuthenticationEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/common/login/wechat", "/api/common/token/refresh", "/api/public/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new MiniTokenAuthFilter(tokenService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
