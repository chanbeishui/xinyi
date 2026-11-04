package com.xinyi.rongli.integration.service;

import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.integration.domain.IntegrationProfile;
import com.xinyi.rongli.integration.mapper.IntegrationMapper;
import com.xinyi.rongli.identity.mapper.EmployeeDirectoryGuardMapper;
import com.xinyi.system.service.IDeptScopeService;
import com.xinyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class IntegrationAuthority {
    private final BusinessActorResolver actors;
    private final IDeptScopeService scopes;
    private final IntegrationMapper mapper;
    private final EmployeeDirectoryGuardMapper directory;
    public final String environment;

    public IntegrationAuthority(
            BusinessActorResolver actors,
            IDeptScopeService scopes,
            IntegrationMapper mapper,
            EmployeeDirectoryGuardMapper directory,
            @Value("${vi.environment:local}") String deployment,
            @Value("${vi.integration.environment:}") String explicit) {
        this.actors = actors;
        this.scopes = scopes;
        this.mapper = mapper;
        this.directory = directory;
        environment =
                com.xinyi.rongli.pricing.service.PriceEnvironment.resolve(deployment, explicit);
    }

    public Actor actor() {
        return actors.requireAdmin();
    }

    /** 功能权限和部门范围分别校验；null 仅代表服务端确认的全数据权限，空集合必须拒绝。 */
    public Set<Long> scope(String permission) {
        actor();
        if (!SecurityUtils.hasPermi(permission)) throw new ViException(ViErrorCode.VI_FORBIDDEN);
        var result = scopes.resolve(SecurityUtils.getLoginUser(), permission);
        return result.isAllData() ? null : new HashSet<>(result.getDeptIds());
    }

    /** 先持目录守卫再校验数据库账号版本；避免入口鉴权后撤权仍能发布敏感配置。 */
    public void lockAuthority() {
        var actor = actor();
        directory.ensureGuard();
        directory.lockGuard();
        var row = mapper.lockUser(actor.sysUserId());
        var user = SecurityUtils.getLoginUser();
        if (row == null
                || !"0".equals(row.get("status"))
                || !"0".equals(row.get("del_flag"))
                || !Objects.equals(
                        ((Number) row.get("auth_version")).longValue(), user.getAuthVersion()))
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
    }

    public long department(long id, Set<Long> allowed) {
        if (allowed != null && !allowed.contains(id))
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        var row = mapper.lockDept(id);
        if (row == null || !"0".equals(row.get("status")) || !"0".equals(row.get("del_flag")))
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "主体部门无效或已停用");
        return ((Number) row.get("dept_id")).longValue();
    }

    /** 数据归属以配置原记录的公司和部署环境为准，不使用请求传来的部门替代。 */
    public IntegrationProfile profile(long id, String permission, boolean lock) {
        var allowed = scope(permission);
        var row = lock ? mapper.lockProfile(id) : mapper.profile(id);
        if (row == null
                || !environment.equals(row.getEnvironment())
                || allowed != null && !allowed.contains(row.getOwnerDeptId()))
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        return row;
    }

    /** 发布敏感配置要求近期真实登录，刷新访问令牌不能重新计算认证时点。 */
    public void recentAuthentication() {
        Long at = SecurityUtils.getLoginUser().getAuthenticatedAt();
        long now = System.currentTimeMillis();
        if (at == null || at > now || now - at > 900000)
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED, "敏感配置发布需15分钟内真实登录，请重新登录后重试");
    }

    public void environment(String requested) {
        if (!environment.equals(requested))
            throw new ViException(ViErrorCode.VI_FORBIDDEN, "当前部署不能维护其他运行环境配置");
    }
}
