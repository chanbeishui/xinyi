package com.xinyi.web.config;

import com.xinyi.framework.security.filter.JwtAuthenticationTokenFilter;
import com.xinyi.rongli.identity.dto.ApiError;
import com.xinyi.rongli.identity.dto.ApiErrorDetails;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.json.ViJson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** 派单管理只接受真实ADMIN凭据，认证失败使用真实401；不改既有平台接口的响应约定。 */
@Configuration
public class DispatchAdminSecurityConfig {
    @Bean
    @Order(-12)
    SecurityFilterChain dispatchAdminChain(HttpSecurity http, JwtAuthenticationTokenFilter tokens)
            throws Exception {
        return http.securityMatcher(
                        request -> {
                            String uri = request.getRequestURI();
                            return uri.startsWith("/api/admin/dispatch/")
                                    && !uri.endsWith("/listening-config");
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
                                        DispatchAdminSecurityConfig::authenticationRequired))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(tokens, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static void authenticationRequired(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failure)
            throws IOException {
        var error =
                new ApiError(
                        401,
                        "VI_AUTH_REQUIRED",
                        ViErrorCode.VI_AUTH_REQUIRED.defaultMsg(),
                        UUID.randomUUID().toString(),
                        false,
                        null,
                        ApiErrorDetails.empty());
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("WWW-Authenticate", "Bearer realm=\"admin\"");
        response.getOutputStream().write(ViJson.write(error).getBytes(StandardCharsets.UTF_8));
    }
}
