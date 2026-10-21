package com.xinyi.rongli.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.rongli.dto.MiniDtos;
import com.xinyi.rongli.mapper.AccountMapper;
import com.xinyi.rongli.mapper.StationMapper;
import com.xinyi.rongli.support.ApiException;
import com.xinyi.rongli.support.BizConfig;
import com.xinyi.rongli.support.Codes;
import com.xinyi.rongli.support.Crypto;
import com.xinyi.rongli.support.MiniContext;
import com.xinyi.rongli.support.MiniPrincipal;
import com.xinyi.rongli.support.Row;
import com.xinyi.rongli.support.ViTime;

/**
 * 小程序统一账户与会话服务。
 *
 * <p>MINI 认证域独立于框架 ADMIN：账户来自微信标识，员工工作能力必须经受控绑定获得，
 * 角色切换只改变视角，不复制账号、不授予权限、不释放已成立的承接任务。</p>
 */
@Service
public class AuthService
{
    private static final long ACCESS_TTL_MINUTES = 120;

    private static final long REFRESH_TTL_DAYS = 30;

    private static final long SESSION_TTL_DAYS = 30;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private Crypto crypto;

    @Autowired
    private BizConfig config;

    @Autowired
    private EventService eventService;

    @Value("${rongli.wechat.mock:true}")
    private boolean wechatMock;

    @Value("${rongli.wechat.appId:wxdev000000000000}")
    private String defaultAppId;

    // ------------------------------------------------------------------ 登录

    @Transactional
    public Map<String, Object> login(MiniDtos.LoginReq req)
    {
        if (req == null || req.code == null || req.code.isBlank())
        {
            throw ApiException.validation("缺少微信登录 code");
        }
        String appId = req.appId == null || req.appId.isBlank() ? defaultAppId : req.appId.trim();
        String openId = resolveOpenId(req.code);
        String nickname = req.nickname == null || req.nickname.isBlank() ? "车主" + openId.substring(Math.max(0, openId.length() - 4)) : req.nickname;

        Map<String, Object> existing = accountMapper.findAccountByOpenId(appId, openId);
        Long accountId;
        if (existing == null)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("appId", appId);
            row.put("openId", openId);
            row.put("unionId", null);
            row.put("nickname", nickname);
            accountMapper.insertAccount(row);
            accountId = ((Number) row.get("id")).longValue();
            accountMapper.insertSecurity(accountId);
            accountMapper.insertOwnerProfile(accountId, nickname);
        }
        else
        {
            Row r = Row.of(existing);
            accountId = r.lng("id");
            if (!"ACTIVE".equals(r.str("status", "ACTIVE")))
            {
                throw ApiException.forbidden("账户已被锁定，请联系客服");
            }
        }

        Map<String, Object> security = accountMapper.findSecurity(accountId);
        long epoch = security == null ? 0 : Row.of(security).lng("session_epoch", 0);

        // 员工绑定是长期关系：重新登录时必须恢复，否则检测点员工每次登录都会丢失工作视角
        Long linkedSysUserId = null;
        Map<String, Object> binding = accountMapper.findActiveBinding(accountId);
        if (binding != null)
        {
            Long boundUserId = Row.of(binding).lng("sys_user_id");
            Map<String, Object> user = boundUserId == null ? null : accountMapper.findSysUser(boundUserId);
            if (user != null && "0".equals(Row.of(user).str("status")))
            {
                linkedSysUserId = boundUserId;
            }
        }

        Map<String, Object> sessionRow = new LinkedHashMap<>();
        sessionRow.put("accountId", accountId);
        sessionRow.put("appId", appId);
        sessionRow.put("epoch", epoch);
        sessionRow.put("role", Codes.Role.OWNER);
        sessionRow.put("stationId", null);
        sessionRow.put("linkedSysUserId", linkedSysUserId);
        sessionRow.put("absoluteExpiresAt", ViTime.now().plusDays(SESSION_TTL_DAYS));
        sessionRow.put("deviceLabel", req.deviceLabel);
        accountMapper.insertSession(sessionRow);
        Long sessionId = ((Number) sessionRow.get("id")).longValue();

        return sessionView(sessionId, issueTokens(sessionId, 1, 1));
    }

    /** 真实环境调用 jscode2session 换取 openId；联调环境把 code 作为稳定标识 */
    private String resolveOpenId(String code)
    {
        if (wechatMock)
        {
            return code.startsWith("dev_") ? code : "dev_" + code;
        }
        throw new ApiException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                Codes.Error.CHANNEL_UNAVAILABLE, "微信登录通道未配置，请先在后台完成小程序与商户配置");
    }

    // ------------------------------------------------------------------ 会话

    public Map<String, Object> currentSession()
    {
        MiniPrincipal p = MiniContext.require();
        return sessionView(p.sessionId, null);
    }

    /** 当前登录账号的统一档案 */
    public Map<String, Object> profileOfCurrent()
    {
        MiniPrincipal p = MiniContext.require();
        return profileView(p.accountId, p.linkedSysUserId);
    }

    @Transactional
    public Map<String, Object> switchRole(MiniDtos.RoleSwitchReq req)
    {
        MiniPrincipal p = MiniContext.require();
        String role = req == null || req.role == null ? "" : req.role.toUpperCase();
        List<String> roles = rolesOf(p.accountId, p.linkedSysUserId);
        if (!roles.contains(role))
        {
            throw ApiException.forbidden("当前账户未开通该视角");
        }
        Long stationId = p.stationId;
        if (Codes.Role.STATION.equals(role))
        {
            List<Map<String, Object>> grants = stationMapper.grantsOfUser(p.linkedSysUserId);
            if (grants.isEmpty())
            {
                throw ApiException.forbidden("未获授权任何检测点");
            }
            final Long currentStation = stationId;
            if (currentStation == null || grants.stream()
                    .noneMatch(g -> Row.of(g).lng("station_id", -1L) == currentStation.longValue()))
            {
                stationId = Row.of(grants.get(0)).lng("station_id");
            }
        }
        accountMapper.updateSessionRole(p.sessionId, role);
        if (!java.util.Objects.equals(stationId, p.stationId))
        {
            accountMapper.updateSessionStation(p.sessionId, stationId);
        }
        long context = p.contextVersion + (java.util.Objects.equals(stationId, p.stationId) ? 1 : 2);
        accountMapper.revokeTokensBySession(p.sessionId);
        return sessionView(p.sessionId, issueTokens(p.sessionId, context, 1));
    }

    @Transactional
    public Map<String, Object> switchStation(MiniDtos.StationSwitchReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (!p.isStationRole())
        {
            throw ApiException.forbidden("仅检测员工视角可切换站点");
        }
        if (req == null || req.stationId == null)
        {
            throw ApiException.validation("缺少目标站点");
        }
        if (stationMapper.hasGrant(p.linkedSysUserId, req.stationId) <= 0)
        {
            throw ApiException.forbidden("未获授权该检测点");
        }
        accountMapper.updateSessionStation(p.sessionId, req.stationId);
        long context = p.contextVersion + 1;
        accountMapper.revokeTokensBySession(p.sessionId);
        Map<String, Object> tokens = issueTokens(p.sessionId, context, 1);
        return sessionView(p.sessionId, tokens);
    }

    @Transactional
    public Map<String, Object> logout(boolean all)
    {
        MiniPrincipal p = MiniContext.require();
        if (all)
        {
            accountMapper.revokeAllSessions(p.accountId);
            accountMapper.bumpEpoch(p.accountId);
        }
        else
        {
            accountMapper.revokeTokensBySession(p.sessionId);
            accountMapper.revokeSession(p.sessionId, "REVOKED");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("loggedOut", true);
        return result;
    }

    @Transactional
    public Map<String, Object> refresh(String refreshToken)
    {
        if (refreshToken == null || refreshToken.isBlank())
        {
            throw ApiException.unauthorized("缺少刷新令牌");
        }
        Map<String, Object> tokenRow = accountMapper.findToken(Crypto.sha256(refreshToken));
        if (tokenRow == null)
        {
            throw ApiException.unauthorized("刷新令牌无效");
        }
        Row t = Row.of(tokenRow);
        if (!"REFRESH".equals(t.str("token_type")) || t.time("consumed_at") != null || t.time("revoked_at") != null)
        {
            throw ApiException.unauthorized("刷新令牌已失效，请重新进入小程序");
        }
        if (t.time("expires_at") == null || t.time("expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.unauthorized("刷新令牌已过期，请重新进入小程序");
        }
        Map<String, Object> sessionRow = accountMapper.findSession(t.lng("session_id"));
        if (sessionRow == null)
        {
            throw ApiException.unauthorized("会话不存在");
        }
        Row s = Row.of(sessionRow);
        if (!"ACTIVE".equals(s.str("status")))
        {
            throw ApiException.unauthorized("会话已结束");
        }
        accountMapper.consumeToken(t.lng("id"));
        long context = s.lng("context_version", 1);
        accountMapper.revokeTokensBySession(s.lng("id"));
        return sessionView(s.lng("id"), issueTokens(s.lng("id"), context, t.lng("generation", 1) + 1));
    }

    private Map<String, Object> issueTokens(Long sessionId, long contextVersion, long generation)
    {
        LocalDateTime now = ViTime.now();
        String access = crypto.randomToken();
        String refresh = crypto.randomToken();

        Map<String, Object> accessRow = new LinkedHashMap<>();
        accessRow.put("sessionId", sessionId);
        accessRow.put("tokenHash", Crypto.sha256(access));
        accessRow.put("tokenType", "ACCESS");
        accessRow.put("generation", generation);
        accessRow.put("contextVersion", contextVersion);
        accessRow.put("parentTokenId", null);
        accessRow.put("expiresAt", now.plusMinutes(ACCESS_TTL_MINUTES));
        accountMapper.insertToken(accessRow);

        Map<String, Object> refreshRow = new LinkedHashMap<>();
        refreshRow.put("sessionId", sessionId);
        refreshRow.put("tokenHash", Crypto.sha256(refresh));
        refreshRow.put("tokenType", "REFRESH");
        refreshRow.put("generation", generation);
        refreshRow.put("contextVersion", contextVersion);
        refreshRow.put("parentTokenId", null);
        refreshRow.put("expiresAt", now.plusDays(REFRESH_TTL_DAYS));
        accountMapper.insertToken(refreshRow);

        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);
        tokens.put("accessExpiresAt", ViTime.iso(now.plusMinutes(ACCESS_TTL_MINUTES)));
        tokens.put("refreshExpiresAt", ViTime.iso(now.plusDays(REFRESH_TTL_DAYS)));
        return tokens;
    }

    public Map<String, Object> sessionView(Long sessionId, Map<String, Object> tokens)
    {
        Row s = Row.of(accountMapper.findSession(sessionId));
        Long accountId = s.lng("account_id");
        Long linkedSysUserId = s.lng("linked_sys_user_id");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("sessionId", String.valueOf(sessionId));
        view.put("activeRole", s.str("active_role", Codes.Role.OWNER));
        view.put("stationId", s.lng("station_id") == null ? null : String.valueOf(s.lng("station_id")));
        view.put("contextVersion", s.lng("context_version", 1));
        view.put("absoluteExpiresAt", ViTime.iso(s.time("absolute_expires_at")));
        view.put("profile", profileView(accountId, linkedSysUserId));
        view.put("stations", stationViews(linkedSysUserId));
        if (tokens != null)
        {
            view.putAll(tokens);
        }
        return view;
    }

    public Map<String, Object> profileView(Long accountId, Long linkedSysUserId)
    {
        Map<String, Object> account = accountMapper.findAccount(accountId);
        Row a = Row.of(account);
        Map<String, Object> owner = accountMapper.findOwnerProfile(accountId);
        Map<String, Object> driver = accountMapper.findDriverProfile(accountId);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("accountId", String.valueOf(accountId));
        view.put("nickname", a.str("nickname"));
        view.put("phoneBound", a.bool("has_phone"));
        view.put("status", a.str("status", "ACTIVE"));
        view.put("roles", rolesOf(accountId, linkedSysUserId));
        view.put("serviceName", owner == null ? null : Row.of(owner).str("service_name"));
        view.put("driverQualificationStatus", driver == null ? null : Row.of(driver).str("qualification_status"));
        view.put("driverListeningEnabled", driver != null && Row.of(driver).bool("listening_enabled"));
        view.put("employeeBound", linkedSysUserId != null);
        if (linkedSysUserId != null)
        {
            Map<String, Object> user = accountMapper.findSysUser(linkedSysUserId);
            if (user != null)
            {
                view.put("employeeName", Row.of(user).str("nick_name"));
                view.put("employeeDeptName", Row.of(user).str("dept_name"));
            }
        }
        return view;
    }

    public List<String> rolesOf(Long accountId, Long linkedSysUserId)
    {
        List<String> roles = new ArrayList<>();
        roles.add(Codes.Role.OWNER);
        Map<String, Object> driver = accountMapper.findDriverProfile(accountId);
        if (driver != null && "APPROVED".equals(Row.of(driver).str("qualification_status")))
        {
            roles.add(Codes.Role.DRIVER);
        }
        if (linkedSysUserId != null && !stationMapper.grantsOfUser(linkedSysUserId).isEmpty())
        {
            roles.add(Codes.Role.STATION);
        }
        return roles;
    }

    private List<Map<String, Object>> stationViews(Long linkedSysUserId)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        if (linkedSysUserId == null)
        {
            return out;
        }
        for (Map<String, Object> g : stationMapper.grantsOfUser(linkedSysUserId))
        {
            Row r = Row.of(g);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stationId", String.valueOf(r.lng("station_id")));
            item.put("name", r.str("station_name"));
            out.add(item);
        }
        return out;
    }

    // ------------------------------------------------------------------ 档案与手机号

    @Transactional
    public Map<String, Object> updateProfile(MiniDtos.ProfilePatch req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req.nickname != null && !req.nickname.isBlank())
        {
            accountMapper.updateNickname(p.accountId, req.nickname.trim());
            if (req.serviceName == null || req.serviceName.isBlank())
            {
                accountMapper.updateOwnerServiceName(p.accountId, req.nickname.trim());
            }
        }
        if (req.serviceName != null)
        {
            accountMapper.updateOwnerServiceName(p.accountId, req.serviceName.trim());
        }
        eventService.record("ACCOUNT", p.accountId, "PROFILE_UPDATED", null);
        return profileView(p.accountId, p.linkedSysUserId);
    }

    @Transactional
    public Map<String, Object> bindPhone(MiniDtos.PhoneReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null || req.phone == null || !req.phone.matches("\\d{11}"))
        {
            throw ApiException.validation("手机号格式不正确");
        }
        if (req.smsCode == null || !isSmsCodeValid(p.accountId, req.smsCode))
        {
            throw ApiException.validation("短信验证码不正确或已过期");
        }
        accountMapper.updatePhone(p.accountId, crypto.encrypt(req.phone), Crypto.sha256("phone:" + req.phone));
        eventService.record("ACCOUNT", p.accountId, "PHONE_BOUND", null);
        return profileView(p.accountId, p.linkedSysUserId);
    }

    private boolean isSmsCodeValid(Long accountId, String code)
    {
        String expected = smsCodes.get(accountId);
        if (expected != null && expected.equals(code))
        {
            smsCodes.remove(accountId);
            return true;
        }
        return false;
    }

    private final Map<Long, String> smsCodes = new java.util.concurrent.ConcurrentHashMap<>();

    public Map<String, Object> sendSmsCode(MiniDtos.SmsSendReq req)
    {
        MiniPrincipal p = MiniContext.require();
        String code = crypto.randomDigits(6);
        smsCodes.put(p.accountId, code);
        Map<String, Object> ack = new LinkedHashMap<>();
        ack.put("sent", true);
        ack.put("expiresInSeconds", 300);
        // 联调环境不接真实短信通道，验证码回显仅用于本地联调；生产必须接入供应商
        if (wechatMock)
        {
            ack.put("devCode", code);
        }
        return ack;
    }

    /** 短信兜底核验：只校验验证码是否有效并一次性消费，不写手机号 */
    public Map<String, Object> verifySmsCode(MiniDtos.SmsVerifyReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null || req.code == null || req.code.isBlank())
        {
            throw ApiException.validation("请输入短信验证码");
        }
        boolean ok = isSmsCodeValid(p.accountId, req.code.trim());
        if (!ok)
        {
            throw ApiException.validation("短信验证码不正确或已过期");
        }
        Map<String, Object> ack = new LinkedHashMap<>();
        ack.put("verified", true);
        return ack;
    }

    // ------------------------------------------------------------------ 员工受控绑定

    @Transactional
    public Map<String, Object> requestBinding(MiniDtos.BindingRequestCreate req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null || req.reason == null || req.reason.isBlank())
        {
            throw ApiException.validation("请填写申请理由");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("accountId", p.accountId);
        row.put("code", "BR" + System.currentTimeMillis() + crypto.randomDigits(3));
        row.put("reason", req.reason.trim());
        row.put("expiresAt", ViTime.now().plusDays(7));
        accountMapper.insertBindingRequest(row);
        eventService.record("BINDING_REQUEST", ((Number) row.get("id")).longValue(), "BINDING_REQUESTED", null);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", String.valueOf(row.get("id")));
        view.put("requestCode", row.get("code"));
        view.put("status", "REQUESTED");
        view.put("requestReason", req.reason);
        view.put("expiresAt", ViTime.iso(ViTime.now().plusDays(7)));
        return view;
    }

    public List<Map<String, Object>> myBindingRequests()
    {
        MiniPrincipal p = MiniContext.require();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : accountMapper.listBindingRequests(p.accountId))
        {
            Row row = Row.of(r);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(row.lng("id")));
            item.put("requestCode", row.str("request_code"));
            item.put("status", row.str("status"));
            item.put("requestReason", row.str("request_reason"));
            item.put("expiresAt", ViTime.iso(row.time("expires_at")));
            item.put("createdAt", ViTime.iso(row.time("created_at")));
            out.add(item);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> consumeBinding(MiniDtos.EmployeeBindingReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null || req.credentialToken == null || req.credentialToken.isBlank())
        {
            throw ApiException.validation("请输入绑定凭证");
        }
        Map<String, Object> credentialRow = accountMapper.findCredentialByHash(Crypto.sha256(req.credentialToken.trim()));
        if (credentialRow == null)
        {
            throw ApiException.notFound("绑定凭证不存在");
        }
        Row c = Row.of(credentialRow);
        if (!"ISSUED".equals(c.str("status")))
        {
            throw ApiException.stateConflict("绑定凭证已使用或已撤销");
        }
        if (c.time("expires_at") != null && c.time("expires_at").isBefore(ViTime.now()))
        {
            throw ApiException.stateConflict("绑定凭证已过期");
        }
        if (!java.util.Objects.equals(c.lng("target_account_id"), p.accountId))
        {
            throw ApiException.forbidden("绑定凭证不属于当前账户");
        }
        Long sysUserId = c.lng("target_sys_user_id");
        Map<String, Object> user = accountMapper.findSysUser(sysUserId);
        if (user == null || !"0".equals(Row.of(user).str("status")))
        {
            throw ApiException.forbidden("目标员工已停用");
        }
        accountMapper.consumeCredential(c.lng("id"));
        accountMapper.revokeBinding(p.accountId);
        accountMapper.insertBinding(p.accountId, sysUserId, c.lng("issued_by_user_id"));
        accountMapper.updateBindingRequestStatus(c.lng("request_id"), "BOUND");
        accountMapper.updateSessionBinding(p.sessionId, sysUserId);
        accountMapper.bumpEpoch(p.accountId);
        accountMapper.revokeTokensBySession(p.sessionId);
        Map<String, Object> tokens = issueTokens(p.sessionId, p.contextVersion + 1, 1);
        eventService.record("EMPLOYEE_BINDING", p.accountId, "EMPLOYEE_BOUND", null);
        return sessionView(p.sessionId, tokens);
    }
}
