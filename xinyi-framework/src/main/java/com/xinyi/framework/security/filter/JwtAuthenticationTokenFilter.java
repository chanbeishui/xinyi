package com.xinyi.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.alibaba.fastjson2.JSON;
import com.xinyi.common.constant.HttpStatus;
import com.xinyi.common.core.domain.AjaxResult;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.ServletUtils;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.framework.web.service.TokenService;
import com.xinyi.system.domain.SysUserAuthState;
import com.xinyi.system.service.ISysUserService;

/**
 * token过滤器 验证token有效性
 * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication()))
        {
            SysUserAuthState state;
            try
            {
                state = userService.selectAuthStateByUserId(loginUser.getUserId());
            }
            catch (RuntimeException ex)
            {
                ServletUtils.renderString(response,
                        JSON.toJSONString(AjaxResult.error(HttpStatus.SERVICE_UNAVAILABLE,
                                "认证权威状态暂时不可读取")),
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            if (!tokenService.isCurrentAuthorization(loginUser, state))
            {
                try
                {
                    tokenService.delLoginUser(loginUser.getToken());
                }
                catch (RuntimeException ignored)
                {
                    // 数据库版本已确保旧权限无法继续使用，缓存清理由outbox重试。
                }
                ServletUtils.renderString(response,
                        JSON.toJSONString(AjaxResult.error(HttpStatus.UNAUTHORIZED,
                                "登录权限已变更，请重新登录")),
                        HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);
    }
}
