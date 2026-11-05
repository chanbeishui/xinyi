package com.xinyi.framework.web.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.xinyi.common.constant.CacheConstants;
import com.xinyi.common.constant.Constants;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.security.SessionInvalidator;
import com.xinyi.common.utils.ServletUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.http.UserAgentUtils;
import com.xinyi.common.utils.ip.AddressUtils;
import com.xinyi.common.utils.ip.IpUtils;
import com.xinyi.common.utils.uuid.IdUtils;
import com.xinyi.system.domain.SysUserAuthState;
import com.xinyi.system.service.ISysUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Token验证、缓存和按用户强退服务。
 */
@Component
public class TokenService implements SessionInvalidator
{
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    /**
     * 会话重载时复用登录侧的快照构建逻辑，避免权限取数口径出现第二套实现。
     */
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public LoginUser getLoginUser(HttpServletRequest request)
    {
        String token = getToken(request);
        if (StringUtils.isEmpty(token))
        {
            return null;
        }
        try
        {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
            return redisCache.getCacheObject(getTokenKey(uuid));
        }
        catch (Exception e)
        {
            log.error("获取登录用户信息异常: {}", e.getMessage());
            return null;
        }
    }

    public void setLoginUser(LoginUser loginUser)
    {
        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    public void delLoginUser(String token)
    {
        if (StringUtils.isEmpty(token))
        {
            return;
        }
        String userKey = getTokenKey(token);
        LoginUser loginUser = redisCache.getCacheObject(userKey);
        redisCache.deleteObject(userKey);
        if (loginUser != null && loginUser.getUserId() != null)
        {
            redisCache.removeCacheSetValue(getUserTokenKey(loginUser.getUserId()), token);
        }
    }

    /**
     * 签发前后都校验数据库版本，避免认证并发变更恢复旧权限。
     */
    public String createToken(LoginUser loginUser)
    {
        ensureCurrentAuthorization(loginUser);
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);
        try
        {
            ensureCurrentAuthorization(loginUser);
        }
        catch (RuntimeException ex)
        {
            delLoginUser(token);
            throw ex;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createToken(claims);
    }

    public void verifyToken(LoginUser loginUser)
    {
        long currentTime = System.currentTimeMillis();
        if (loginUser.getExpireTime() - currentTime <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * Token与用户索引必须同时写成功，否则删除Token并使调用失败。
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        String userKey = getTokenKey(loginUser.getToken());
        String indexKey = getUserTokenKey(loginUser.getUserId());
        try
        {
            redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
            redisCache.addCacheSetValue(indexKey, loginUser.getToken());
            if (!redisCache.expire(indexKey, expireTime, TimeUnit.MINUTES))
            {
                throw new ServiceException("用户Token索引设置有效期失败");
            }
        }
        catch (RuntimeException ex)
        {
            try
            {
                redisCache.deleteObject(userKey);
                redisCache.removeCacheSetValue(indexKey, loginUser.getToken());
            }
            catch (RuntimeException cleanupEx)
            {
                log.warn("登录失败后的Token缓存清理异常: {}", cleanupEx.getMessage());
            }
            throw ex;
        }
    }

    public void setUserAgent(LoginUser loginUser)
    {
        String userAgent = ServletUtils.getRequest().getHeader("User-Agent");
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(UserAgentUtils.getBrowser(userAgent));
        loginUser.setOs(UserAgentUtils.getOperatingSystem(userAgent));
    }

    /**
     * 只淘汰授权快照已过期的会话。
     *
     * <p>旧实现无条件删除该用户的全部 token，连操作者自己刚刚完成变更的会话一起删掉，
     * 于是"管理员改了自己的部门"要重新登录。现在按 auth_version / 状态逐个比对：
     * 已经与数据库一致的会话（例如被 {@link #refreshCurrentSessionAuthorization(Long)}
     * 刚刚重载过的那一个）保留，其余删除。</p>
     *
     * <p>读取权威状态失败时退化为"全部删除"，宁可多踢一次也不留下旧权限会话。</p>
     */
    @Override
    public void forceLogoutByUserId(Long userId)
    {
        String indexKey = getUserTokenKey(userId);
        Set<String> tokens = redisCache.getCacheSet(indexKey);
        if (tokens == null || tokens.isEmpty())
        {
            redisCache.deleteObject(indexKey);
            return;
        }
        SysUserAuthState state = null;
        try
        {
            state = userService.selectAuthStateByUserId(userId);
        }
        catch (RuntimeException ex)
        {
            log.warn("用户[{}]授权状态读取失败，按全部失效处理: {}", userId, ex.getMessage());
        }
        boolean anyRetained = false;
        for (String token : tokens)
        {
            if (state != null && isSessionCurrent(token, state))
            {
                anyRetained = true;
                continue;
            }
            redisCache.deleteObject(getTokenKey(token));
            redisCache.removeCacheSetValue(indexKey, token);
        }
        if (!anyRetained)
        {
            redisCache.deleteObject(indexKey);
        }
    }

    /**
     * 用数据库中的最新数据重建操作者当前会话的登录快照。
     *
     * <p>与登录走同一条构建路径（{@code UserDetailsServiceImpl#createLoginUser}），
     * 保证角色、权限码、任职部门与 authVersion 的取数口径完全一致。
     * token 与剩余有效期都保留，只换内容。</p>
     */
    @Override
    public void refreshCurrentSessionAuthorization(Long userId)
    {
        String currentToken = currentSessionId();
        if (StringUtils.isEmpty(currentToken))
        {
            // 不在请求线程内（例如定时任务触发），或 token 无法解析，交给全量失效兜底
            log.warn("用户[{}]无法定位当前会话标识，本次不保留当前会话", userId);
            return;
        }
        String userKey = getTokenKey(currentToken);
        LoginUser cached = redisCache.getCacheObject(userKey);
        if (cached == null || userId == null || !userId.equals(cached.getUserId()))
        {
            return;
        }
        SysUserAuthState state;
        try
        {
            state = userService.selectAuthStateByUserId(userId);
        }
        catch (RuntimeException ex)
        {
            log.warn("用户[{}]授权状态读取失败，跳过会话重载: {}", userId, ex.getMessage());
            return;
        }
        // 停用、删除后的账号不允许续命，直接留给 forceLogout 清掉
        if (state == null || !"0".equals(state.getStatus()) || !"0".equals(state.getDelFlag()))
        {
            return;
        }
        SysUser fresh = userService.selectUserByUserName(cached.getUsername());
        if (fresh == null)
        {
            return;
        }
        long remainingSeconds = redisCache.getExpire(userKey);
        if (remainingSeconds <= 0)
        {
            return;
        }
        LoginUser rebuilt = (LoginUser) userDetailsService.createLoginUser(fresh);
        rebuilt.setToken(currentToken);
        rebuilt.setLoginTime(cached.getLoginTime());
        rebuilt.setExpireTime(cached.getExpireTime());
        rebuilt.setIpaddr(cached.getIpaddr());
        rebuilt.setLoginLocation(cached.getLoginLocation());
        rebuilt.setBrowser(cached.getBrowser());
        rebuilt.setOs(cached.getOs());
        rebuilt.setDeptId(fresh.getDeptId());
        // 用剩余 TTL 回写，不因为一次权限调整顺带把会话续期
        redisCache.setCacheObject(userKey, rebuilt, (int) remainingSeconds, TimeUnit.SECONDS);
    }

    private boolean isSessionCurrent(String token, SysUserAuthState state)
    {
        try
        {
            LoginUser cached = redisCache.getCacheObject(getTokenKey(token));
            return isCurrentAuthorization(cached, state);
        }
        catch (RuntimeException ex)
        {
            log.warn("会话快照读取失败，按失效处理: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * 取当前请求对应的会话标识。
     *
     * <p>注意是 JWT 里的 {@code login_user_key}（uuid），不是请求头里的 JWT 原文——
     * Redis 的 {@code login_tokens:} 键用的是 uuid，拿 JWT 去拼会永远取不到会话。</p>
     */
    private String currentSessionId()
    {
        try
        {
            HttpServletRequest request = ServletUtils.getRequest();
            if (request == null)
            {
                return null;
            }
            String token = getToken(request);
            if (StringUtils.isEmpty(token))
            {
                return null;
            }
            Claims claims = parseToken(token);
            return (String) claims.get(Constants.LOGIN_USER_KEY);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * 只替换展示型资料字段，权限快照、authVersion 和会话有效期都保持原样。
     *
     * <p>用剩余 TTL 回写而不是 30 分钟，避免"改个昵称顺手把别人会话续期"。
     * 剩余 TTL 已过期（-1/-2）时直接跳过，交给过滤器按失效处理。</p>
     */
    @Override
    public void refreshUserProfileSnapshot(Long userId)
    {
        Set<String> tokens = redisCache.getCacheSet(getUserTokenKey(userId));
        if (tokens == null || tokens.isEmpty())
        {
            return;
        }
        SysUser fresh = userService.selectUserById(userId);
        if (fresh == null)
        {
            return;
        }
        for (String token : tokens)
        {
            String userKey = getTokenKey(token);
            LoginUser loginUser = redisCache.getCacheObject(userKey);
            if (loginUser == null || loginUser.getUser() == null)
            {
                continue;
            }
            long remainingSeconds = redisCache.getExpire(userKey);
            if (remainingSeconds <= 0)
            {
                continue;
            }
            SysUser cached = loginUser.getUser();
            cached.setNickName(fresh.getNickName());
            cached.setEmail(fresh.getEmail());
            cached.setPhonenumber(fresh.getPhonenumber());
            cached.setSex(fresh.getSex());
            cached.setRemark(fresh.getRemark());
            cached.setAvatar(fresh.getAvatar());
            // token 有效期最多 expireTime 分钟，remainingSeconds 不会溢出 int
            redisCache.setCacheObject(userKey, loginUser, (int) remainingSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isCurrentAuthorization(LoginUser loginUser, SysUserAuthState state)
    {
        return loginUser != null && state != null && loginUser.getAuthVersion() != null
                && loginUser.getAuthVersion().equals(state.getAuthVersion())
                && "0".equals(state.getStatus()) && "0".equals(state.getDelFlag());
    }

    private void ensureCurrentAuthorization(LoginUser loginUser)
    {
        SysUserAuthState state = userService.selectAuthStateByUserId(loginUser.getUserId());
        if (!isCurrentAuthorization(loginUser, state))
        {
            throw new ServiceException("认证期间权限或账号状态发生变化，请重新登录");
        }
    }

    private String createToken(Map<String, Object> claims)
    {
        return Jwts.builder().setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    private Claims parseToken(String token)
    {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    private String getUserTokenKey(Long userId)
    {
        return CacheConstants.USER_TOKEN_KEY + userId;
    }
}
