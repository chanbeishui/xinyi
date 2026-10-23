package com.xinyi.rongli.identity.web;

import com.xinyi.rongli.identity.auth.MiniPrincipalImpl;
import com.xinyi.rongli.identity.service.MiniTokenService;
import com.xinyi.rongli.platform.error.ViException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * MINI 不透明令牌认证过滤器（仅挂在 /api/** 安全链内，不由 Boot 自动注册为全局 servlet 过滤器）。
 * 解析 Authorization: Bearer &lt;token&gt; → 校验 vi_session_token/vi_session/代际 → 构造 MiniPrincipal
 * 放入 SecurityContext（绝不伪装成框架 LoginUser，绝不与 ADMIN 令牌交叉）。
 * 无令牌则不认证（放行给 permitAll 端点或触发 401）；令牌无效同样不认证，由入口点统一渲染真实 401。
 * 明文令牌绝不写日志。
 */
public class MiniTokenAuthFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private static final List<SimpleGrantedAuthority> MINI_AUTHORITIES =
            List.of(new SimpleGrantedAuthority("MINI_AUTHENTICATED"));

    private final MiniTokenService tokenService;

    public MiniTokenAuthFilter(MiniTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER.length()).trim();
            try {
                MiniPrincipalImpl principal = tokenService.resolveAccess(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, MINI_AUTHORITIES);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ViException e) {
                // 令牌无效/过期/撤销/代际不符/跨域：保持未认证，由 AuthenticationEntryPoint 返回真实 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
