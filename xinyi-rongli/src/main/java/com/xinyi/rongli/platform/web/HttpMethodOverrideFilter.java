package com.xinyi.rongli.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * X-HTTP-Method-Override 支持（修正 D-E04-1）。微信 wx.request 不支持 PATCH，
 * 小程序端以 POST + 头 X-HTTP-Method-Override: PATCH 表达（见 xinyi-miniapp/src/services/http.ts）。
 * 本过滤器仅在 /api/* 上、仅对 POST、仅接受安全子集 {PATCH,PUT,DELETE} 时改写方法，
 * 使 Spring MVC 的 @PatchMapping 等正常匹配；不触碰 /system 等框架端点，不放开任意方法。
 *
 * 注册顺序须早于 Spring Security 过滤链（见 ViWebConfig），以便安全匹配也看到改写后的方法。
 */
public class HttpMethodOverrideFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-HTTP-Method-Override";
    private static final Set<String> ALLOWED = Set.of("PATCH", "PUT", "DELETE");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String override = request.getHeader(HEADER);
        if (override == null) {
            chain.doFilter(request, response);
            return;
        }
        String method = override.trim().toUpperCase();
        if (!ALLOWED.contains(method)) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new MethodOverrideRequest(request, method), response);
    }

    private static final class MethodOverrideRequest extends HttpServletRequestWrapper {
        private final String method;

        MethodOverrideRequest(HttpServletRequest request, String method) {
            super(request);
            this.method = method;
        }

        @Override
        public String getMethod() {
            return method;
        }
    }
}
