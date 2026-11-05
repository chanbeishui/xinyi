package com.xinyi.web.config;

import com.xinyi.framework.security.filter.JwtAuthenticationTokenFilter;
import com.xinyi.rongli.identity.dto.ApiError;
import com.xinyi.rongli.identity.dto.ApiErrorDetails;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.json.ViJson;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** 启动层装配框架 JWT 与文件共用协议；真实401不改变既有平台接口的响应约定。 */
@Configuration
public class PrivateFileAdminSecurityConfig {
    @Bean
    @Order(-1)
    SecurityFilterChain adminFileChain(HttpSecurity http, JwtAuthenticationTokenFilter tokens)
            throws Exception {
        return http.securityMatcher(
                        request -> {
                            String uri = request.getRequestURI();
                            return "admin".equals(request.getHeader("X-Client-Type"))
                                    && (uri.matches("^/api/common/(uploads|files)(/.*)?$")
                                            || uri.startsWith("/vi-private-transfer/"));
                        })
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .anonymous(anonymous -> anonymous.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                        (request, response, failure) -> {
                                            response.setStatus(401);
                                            response.setContentType("application/json");
                                            response.setCharacterEncoding(
                                                    StandardCharsets.UTF_8.name());
                                            response.setHeader("Cache-Control", "no-store");
                                            response.setHeader(
                                                    "WWW-Authenticate", "Bearer realm=\"admin\"");
                                            response.getOutputStream()
                                                    .write(
                                                            ViJson.write(
                                                                            new ApiError(
                                                                                    401,
                                                                                    "VI_AUTH_REQUIRED",
                                                                                    ViErrorCode
                                                                                            .VI_AUTH_REQUIRED
                                                                                            .defaultMsg(),
                                                                                    UUID.randomUUID()
                                                                                            .toString(),
                                                                                    false,
                                                                                    null,
                                                                                    ApiErrorDetails
                                                                                            .empty()))
                                                                    .getBytes(
                                                                            StandardCharsets
                                                                                    .UTF_8));
                                        }))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(tokens, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
