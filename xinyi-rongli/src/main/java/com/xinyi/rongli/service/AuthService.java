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
import com.xinyi.rongli.support.SmsSettings;
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
    /** 访问令牌有效期（分钟）。调短可在联调时快速复现过期与续期路径 */
    @Value("${rongli.auth.access-ttl-minutes:120}")
    private long accessTtlMinutes;

    /** 刷新令牌有效期（天） */
    @Value("${rongli.auth.refresh-ttl-days:30}")
    private long refreshTtlDays;

    /** 会话绝对有效期（天），到期后即使刷新令牌有效也必须重新登录 */
    @Value("${rongli.auth.session-ttl-days:30}")
    private long sessionTtlDays;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private Crypto crypto;

    @Autowired
    private BizConfig config;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EventService eventService;

    @Autowired
    private com.xinyi.rongli.support.WechatMiniClient wechatClient;

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
        com.xinyi.rongli.support.WechatMiniClient.WechatSession wechat =
                wechatClient.resolveOpenId(appId, req.code);
        String openId = wechat.openId();
        String unionId = wechat.unionId();
        String nickname = req.nickname == null || req.nickname.isBlank() ? "车主" + openId.substring(Math.max(0, openId.length() - 4)) : req.nickname;

        Map<String, Object> existing = accountMapper.findAccountByOpenId(appId, openId);
        Long accountId;
        boolean freshAccount = existing == null;
        if (freshAccount)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("appId", appId);
            row.put("openId", openId);
            row.put("unionId", unionId);
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

        /*
         * 手机号授权与登录一次完成：小程序把 getPhoneNumber 的 code 一起带上来，
         * 服务端换取真实手机号后立即写档，并按手机号匹配框架内的员工账号。
         * 新账号默认只有车主身份，员工与代驾能力仍须分别由绑定/审核开通。
         */
        String phone = wechatClient.resolvePhone(appId, req.phoneCode);
        if (phone != null)
        {
            accountMapper.updatePhone(accountId, crypto.encrypt(phone), Crypto.sha256("phone:" + phone));
            autoBindEmployeeByPhone(accountId, phone);
        }

        Map<String, Object> security = accountMapper.findSecurity(accountId);
        long epoch = security == null ? 0 : Row.of(security).lng("session_epoch", 0);

        Long linkedSysUserId = linkedSysUserIdOf(accountId);

        Map<String, Object> sessionRow = new LinkedHashMap<>();
        sessionRow.put("accountId", accountId);
        sessionRow.put("appId", appId);
        sessionRow.put("epoch", epoch);
        sessionRow.put("role", Codes.Role.OWNER);
        sessionRow.put("stationId", null);
        sessionRow.put("linkedSysUserId", linkedSysUserId);
        sessionRow.put("absoluteExpiresAt", ViTime.now().plusDays(sessionTtlDays));
        sessionRow.put("deviceLabel", req.deviceLabel);
        accountMapper.insertSession(sessionRow);
        Long sessionId = ((Number) sessionRow.get("id")).longValue();

        Map<String, Object> view = sessionView(sessionId, issueTokens(sessionId, 1, 1));
        view.put("firstLogin", freshAccount);
        return view;
    }

    /** 员工绑定是长期关系：重新登录时必须恢复，否则检测点员工每次登录都会丢失工作视角 */
    private Long linkedSysUserIdOf(Long accountId)
    {
        Map<String, Object> binding = accountMapper.findActiveBinding(accountId);
        if (binding == null)
        {
            return null;
        }
        Long boundUserId = Row.of(binding).lng("sys_user_id");
        Map<String, Object> user = boundUserId == null ? null : accountMapper.findSysUser(boundUserId);
        if (user != null && "0".equals(Row.of(user).str("status")))
        {
            return boundUserId;
        }
        return null;
    }

    /**
     * 手机号与框架员工账号一致时，把当前微信账号直接绑定到该员工。
     *
     * <p>这是"由后台维护人员关系、小程序只做身份识别"的落地：员工先在公司账号体系里登记手机号，
     * 后台再给他的账号配置检测点，本人用同一个手机号授权登录即可获得工作视角，
     * 不需要在小程序端提交任何申请或凭证。</p>
     */
    /**
     * 微信手机号授权后按手机号自动关联员工（V6.4 唯一关联路径）。
     *
     * <p>安全边界：手机号必须来自微信核验；匹配必须唯一；同一员工在同一应用下
     * 已有关联其他微信账号时**拒绝并留审计**，不做静默迁移——旧实现在这里会
     * 直接撤销别人的绑定把工作权限挪给新微信号，等于凭一次手机号授权就能接管
     * 他人的检测工作身份。</p>
     */
    private void autoBindEmployeeByPhone(Long accountId, String phone)
    {
        List<Map<String, Object>> candidates = accountMapper.listSysUserByPhone(phone);
        if (candidates.isEmpty())
        {
            return;
        }
        if (candidates.size() > 1)
        {
            // 手机号在 sys_user 上不唯一，自动绑定会绑错人；留给后台人工处理
            eventService.record("EMPLOYEE_BINDING", accountId, "EMPLOYEE_BIND_AMBIGUOUS", null,
                    null, Map.of("matched", String.valueOf(candidates.size())),
                    "同一手机号匹配到多个员工账号，已跳过自动绑定");
            return;
        }
        Long sysUserId = Row.of(candidates.get(0)).lng("user_id");

        Map<String, Object> account = accountMapper.findAccount(accountId);
        if (account == null)
        {
            return;
        }
        String appId = Row.of(account).str("app_id");
        if (Row.of(account).lng("employee_auto_link_blocked", 0L) != 0L)
        {
            eventService.record("EMPLOYEE_BINDING", accountId, "EMPLOYEE_AUTO_LINK_BLOCKED", null, null,
                    Map.of("sysUserId", String.valueOf(sysUserId)),
                    "管理员已禁止该账号自动关联员工，跳过本次关联");
            return;
        }

        Map<String, Object> current = accountMapper.findActiveBinding(accountId);
        if (current != null && sysUserId.equals(Row.of(current).lng("sys_user_id")))
        {
            return;
        }

        Map<String, Object> occupied = accountMapper.findActiveBindingByUserInApp(appId, sysUserId);
        if (occupied != null && !accountId.equals(Row.of(occupied).lng("account_id")))
        {
            // V6.4：跨账号身份转移不属于普通操作。同应用下该员工已关联其他微信账号，
            // 只能拒绝并交后台核实；绝不因一次手机号授权就把关联迁移过来。
            eventService.record("EMPLOYEE_BINDING", accountId, "EMPLOYEE_LINK_CONFLICT", null,
                    Map.of("occupiedAccountId", String.valueOf(Row.of(occupied).lng("account_id"))),
                    Map.of("sysUserId", String.valueOf(sysUserId)),
                    "该员工已关联其他微信账号，已拒绝自动关联，需后台核实后处理");
            return;
        }

        if (accountMapper.findRebindBlocked(appId, accountId, sysUserId) != null)
        {
            eventService.record("EMPLOYEE_BINDING", accountId, "EMPLOYEE_REBIND_BLOCKED", null, null,
                    Map.of("sysUserId", String.valueOf(sysUserId)),
                    "该员工或账号已被禁止自动重绑，需后台 ALLOW_REMATCH 后由本人重新核验");
            return;
        }

        Map<String, Object> employee = accountMapper.findSysUser(sysUserId);
        long authVersion = employee == null ? 0L : Row.of(employee).lng("auth_version", 0L);

        // 同一账号换绑：先把本账号旧关联置为 REVOKED，不触碰其它账号
        accountMapper.revokeBinding(accountId);
        accountMapper.insertBinding(accountId, sysUserId, appId, Crypto.sha256("phone:" + phone), authVersion);
        eventService.record("EMPLOYEE_BINDING", accountId, "EMPLOYEE_BOUND_AUTO_PHONE", null, null,
                Map.of("sysUserId", String.valueOf(sysUserId), "employeeAuthVersion", String.valueOf(authVersion)),
                "微信核验手机号与员工账号唯一匹配，自动完成绑定");
    }

    /**
     * 已登录账号补充手机号授权。
     *
     * <p>绑定关系变化会改变可用视角，因此这里同样撤销旧令牌并签发新令牌，
     * 前端只需按返回的会话视图覆盖本地状态。</p>
     */
    @Transactional
    public Map<String, Object> bindWechatPhone(MiniDtos.WechatPhoneReq req)
    {
        MiniPrincipal p = MiniContext.require();
        if (req == null || req.phoneCode == null || req.phoneCode.isBlank())
        {
            throw ApiException.validation("缺少手机号授权码");
        }
        String phone = wechatClient.resolvePhone(null, req.phoneCode);
        if (phone == null)
        {
            throw ApiException.validation("未能获取到有效手机号，请重新授权");
        }
        accountMapper.updatePhone(p.accountId, crypto.encrypt(phone), Crypto.sha256("phone:" + phone));
        autoBindEmployeeByPhone(p.accountId, phone);
        Long linkedSysUserId = linkedSysUserIdOf(p.accountId);
        boolean changed = !java.util.Objects.equals(linkedSysUserId, p.linkedSysUserId);
        eventService.record("ACCOUNT", p.accountId, "PHONE_BOUND_WECHAT", null, null,
                Map.of("employeeBound", String.valueOf(linkedSysUserId != null)), "微信授权手机号");
        if (changed)
        {
            // updateSessionBinding 会 context_version + 1，因此必须与换发令牌成对出现：
            // 只改会话不换令牌，前端手里的令牌立刻失效却拿不到新的，等于绑定即被踢下线。
            accountMapper.updateSessionBinding(p.sessionId, linkedSysUserId);
            accountMapper.revokeTokensBySession(p.sessionId);
            return sessionView(p.sessionId, issueTokens(p.sessionId, p.contextVersion + 1, 1));
        }
        // 绑定关系没变（重复授权同一手机号）：会话无需变更，令牌继续有效
        return sessionView(p.sessionId, null);
    }

    // openId 解析已统一收敛到 WechatMiniClient.resolveOpenId（联调用稳定标识、真实环境调 jscode2session）

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
        if (!"REFRESH".equals(t.str("token_type")))
        {
            throw ApiException.unauthorized("刷新令牌无效");
        }
        // 顺序有讲究：先判"已被使用"，再判"已失效"。
        // 并发刷新的失败方到达时，成功方可能已经撤销了整个会话的令牌（revoked_at 已写），
        // 若把两个条件并在一起先判 revoked，失败方就会退化成笼统的"已失效"，
        // 丢掉"这张令牌是单次使用的，重试没用"这个关键提示——而且这个提示会随事务提交时序时有时无。
        if (t.time("consumed_at") != null)
        {
            throw ApiException.unauthorized("刷新令牌已被使用，请重新进入小程序");
        }
        if (t.time("revoked_at") != null)
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
        // 刷新令牌单次消费：并发刷新时只有第一个能真正占用，否则后到的请求会执行
        // 下面的整链撤销，把前一个请求刚签发的新令牌一并作废，客户端随即陷入令牌失效循环。
        if (accountMapper.consumeToken(t.lng("id")) == 0)
        {
            throw ApiException.unauthorized("刷新令牌已被使用，请重新进入小程序");
        }
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
        accessRow.put("expiresAt", now.plusMinutes(accessTtlMinutes));
        accountMapper.insertToken(accessRow);

        Map<String, Object> refreshRow = new LinkedHashMap<>();
        refreshRow.put("sessionId", sessionId);
        refreshRow.put("tokenHash", Crypto.sha256(refresh));
        refreshRow.put("tokenType", "REFRESH");
        refreshRow.put("generation", generation);
        refreshRow.put("contextVersion", contextVersion);
        refreshRow.put("parentTokenId", null);
        refreshRow.put("expiresAt", now.plusDays(refreshTtlDays));
        accountMapper.insertToken(refreshRow);

        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);
        tokens.put("accessExpiresAt", ViTime.iso(now.plusMinutes(accessTtlMinutes)));
        tokens.put("refreshExpiresAt", ViTime.iso(now.plusDays(refreshTtlDays)));
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
            int stationCount = stationMapper.grantsOfUser(linkedSysUserId).size();
            view.put("employeeStationCount", stationCount);
            // 绑定来源按关联的 source 判定：V6.4 起只有「微信手机号唯一匹配」会新建关联，
            // MANUAL_LEGACY 是旧凭证流程遗留（已不可新增），对外仍报 CREDENTIAL 保持契约稳定。
            Map<String, Object> binding = accountMapper.findActiveBinding(accountId);
            String linkSource = binding == null ? null : Row.of(binding).str("source");
            view.put("employeeBindSource", "MANUAL_LEGACY".equals(linkSource) ? "CREDENTIAL" : "PHONE");
        }
        else
        {
            view.put("employeeStationCount", 0);
            view.put("employeeBindSource", null);
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
        // V6.4 §2.5：短信验证码属于「短信兜底」，不能作为新员工自动关联的依据。
        // 只有微信手机号授权（bindWechatPhone）才触发自动关联；这里仅完成普通手机号绑定。
        Long linkedSysUserId = linkedSysUserIdOf(p.accountId);
        eventService.record("ACCOUNT", p.accountId, "PHONE_BOUND", null);
        if (!java.util.Objects.equals(linkedSysUserId, p.linkedSysUserId))
        {
            // 绑定关系变化会新增视角，旧令牌的上下文已过期，换发新令牌并让前端整包覆盖
            accountMapper.updateSessionBinding(p.sessionId, linkedSysUserId);
            accountMapper.revokeTokensBySession(p.sessionId);
            return sessionView(p.sessionId, issueTokens(p.sessionId, p.contextVersion + 1, 1));
        }
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
        if (req != null && req.phone != null && !req.phone.isBlank() && !req.phone.matches("\\d{11}"))
        {
            throw ApiException.validation("手机号格式不正确");
        }
        SmsSettings sms = SmsSettings.load(config, crypto);
        String code = crypto.randomDigits(6);
        // 真实通道优先按本次提交的号码发送；未指定时用账号已绑定的手机号
        String phone = req != null && req.phone != null && !req.phone.isBlank() ? req.phone.trim()
                : phoneOfAccount(p.accountId);
        if (sms.enabled() && (phone == null || phone.isBlank()))
        {
            throw ApiException.validation("请先授权手机号或填写手机号后再获取验证码");
        }
        Map<String, Object> sendResult = smsService.sendCode(p.accountId, phone == null ? "" : phone, code,
                SmsService.SCENE_LOGIN);
        // 只有供应商受理后才登记验证码，避免发送失败却让用户拿着一个"有效"的码
        smsCodes.put(p.accountId, code);
        Map<String, Object> ack = new LinkedHashMap<>();
        ack.put("sent", true);
        ack.put("delivered", sendResult.get("delivered"));
        ack.put("provider", sendResult.get("provider"));
        ack.put("expiresInSeconds", sms.effectiveExpire());
        // 只有未启用真实通道时才回显验证码，真实通道下绝不返回
        if (!Boolean.TRUE.equals(sendResult.get("delivered")))
        {
            ack.put("devCode", code);
            ack.put("hint", "当前为联调模式，验证码直接回显；启用短信通道后改为真实下发");
        }
        return ack;
    }

    /** 账号已绑定的手机号；未绑定返回 null（此时真实通道会拒绝发送并提示先授权） */
    private String phoneOfAccount(Long accountId)
    {
        byte[] cipher = accountMapper.findPhoneCipher(accountId);
        return cipher == null ? null : crypto.decrypt(cipher);
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

    // ------------------------------------------------------------------ 员工绑定
    // V6.4 已退役「员工自助申请 + 后台签发一次性凭证 + 本人兑换」整条流程，
    // 改为微信手机号授权后按唯一匹配自动关联（见 autoBindEmployeeByPhone）。
    // 相关表 vi_employee_binding_request / vi_employee_binding_credential 已删除。

}
