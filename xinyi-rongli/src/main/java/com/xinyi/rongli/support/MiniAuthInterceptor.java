package com.xinyi.rongli.support;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.xinyi.rongli.mapper.AccountMapper;

/**
 * 小程序 MINI 认证域拦截器。
 *
 * <p>令牌为服务端签发的随机串，只在 vi_session_token 保存 SHA-256 摘要；
 * 每次请求校验令牌上下文版本、会话状态、账户安全状态与绝对有效期，
 * 切换角色/站点、全局退出、权限封禁都会立即让旧令牌失效。</p>
 */
@Component
public class MiniAuthInterceptor implements HandlerInterceptor
{
    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private Crypto crypto;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        String token = request.getHeader("X-Mini-Token");
        if (token == null || token.isBlank())
        {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer "))
            {
                token = authorization.substring(7).trim();
            }
        }
        if (token == null || token.isBlank())
        {
            throw ApiException.unauthorized("缺少登录令牌");
        }

        Map<String, Object> tokenRow = accountMapper.findToken(Crypto.sha256(token));
        if (tokenRow == null)
        {
            throw ApiException.unauthorized("登录令牌无效");
        }
        Row t = Row.of(tokenRow);
        if (!"ACCESS".equals(t.str("token_type")) || t.time("consumed_at") != null || t.time("revoked_at") != null)
        {
            throw ApiException.unauthorized("登录令牌已失效");
        }
        if (t.time("expires_at") == null || t.time("expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.unauthorized("登录令牌已过期");
        }

        Map<String, Object> sessionRow = accountMapper.findSession(t.lng("session_id"));
        if (sessionRow == null)
        {
            throw ApiException.unauthorized("会话不存在");
        }
        Row s = Row.of(sessionRow);
        if (!"ACTIVE".equals(s.str("status")) || s.time("revoked_at") != null)
        {
            throw ApiException.unauthorized("会话已结束，请重新进入小程序");
        }
        if (s.time("absolute_expires_at") != null && s.time("absolute_expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.unauthorized("会话已过期，请重新进入小程序");
        }
        if (!"ACTIVE".equals(s.str("account_status", "ACTIVE")))
        {
            throw ApiException.forbidden("账户已被锁定");
        }
        if (s.str("security_status") != null && !"ACTIVE".equals(s.str("security_status")))
        {
            throw ApiException.forbidden("账户安全状态异常");
        }
        long tokenContext = t.lng("context_version", 0);
        long sessionContext = s.lng("context_version", 1);
        if (tokenContext != sessionContext)
        {
            throw ApiException.unauthorized("登录上下文已变更（角色或站点切换），请刷新令牌");
        }

        MiniPrincipal principal = new MiniPrincipal();
        principal.accountId = s.lng("account_id");
        principal.sessionId = s.lng("id");
        principal.appId = s.str("app_id");
        principal.role = s.str("active_role", Codes.Role.OWNER);
        principal.stationId = s.lng("station_id");
        principal.linkedSysUserId = s.lng("linked_sys_user_id");
        principal.contextVersion = sessionContext;
        principal.nickname = s.str("nickname");
        MiniContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        MiniContext.clear();
    }
}
