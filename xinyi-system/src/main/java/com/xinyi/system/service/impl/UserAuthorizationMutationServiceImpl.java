package com.xinyi.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.xinyi.common.constant.UserConstants;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.security.SessionInvalidator;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.system.domain.SysSecurityOutbox;
import com.xinyi.system.domain.SysUserAuthState;
import com.xinyi.system.domain.SysUserDept;
import com.xinyi.system.domain.SysUserPost;
import com.xinyi.system.domain.SysUserRole;
import com.xinyi.system.domain.dto.SysUserCreateRequest;
import com.xinyi.system.domain.dto.SysUserDeptUpdateRequest;
import com.xinyi.system.domain.dto.SysUserManagementScopeRequest;
import com.xinyi.system.domain.dto.SysUserPasswordResetRequest;
import com.xinyi.system.domain.dto.SysUserRoleUpdateRequest;
import com.xinyi.system.domain.dto.SysUserStatusUpdateRequest;
import com.xinyi.system.domain.dto.SysUserUpdateRequest;
import com.xinyi.system.mapper.SysRoleMapper;
import com.xinyi.system.mapper.SysDeptMapper;
import com.xinyi.system.mapper.SysSecurityOutboxMapper;
import com.xinyi.system.mapper.SysUserDeptMapper;
import com.xinyi.system.mapper.SysUserMapper;
import com.xinyi.system.mapper.SysUserPostMapper;
import com.xinyi.system.mapper.SysUserRoleMapper;
import com.xinyi.system.service.IDeptScopeService;
import com.xinyi.system.service.ISecurityAuditService;
import com.xinyi.system.service.ISysConfigService;
import com.xinyi.system.service.ISysRoleService;
import com.xinyi.system.service.IUserAuthorizationMutationService;

/**
 * 对用户、任职和角色变更执行行锁、全量校验、版本递增、审计和会话撤销。
 */
@Service
public class UserAuthorizationMutationServiceImpl implements IUserAuthorizationMutationService
{
    private static final Logger log = LoggerFactory.getLogger(UserAuthorizationMutationServiceImpl.class);

    private final SysUserMapper userMapper;
    private final SysUserDeptMapper userDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final SysSecurityOutboxMapper outboxMapper;
    private final IDeptScopeService deptScopeService;
    private final ISysConfigService configService;
    private final ObjectProvider<ISysRoleService> roleServiceProvider;
    private final ISecurityAuditService auditService;
    private final ObjectProvider<SessionInvalidator> sessionInvalidatorProvider;

    @Value("${security.multi-dept-write-enabled:false}")
    private boolean multiDeptWriteEnabled;

    public UserAuthorizationMutationServiceImpl(SysUserMapper userMapper,
            SysUserDeptMapper userDeptMapper, SysUserRoleMapper userRoleMapper,
            SysUserPostMapper userPostMapper, SysRoleMapper roleMapper, SysDeptMapper deptMapper,
            SysSecurityOutboxMapper outboxMapper, IDeptScopeService deptScopeService,
            ISysConfigService configService, ObjectProvider<ISysRoleService> roleServiceProvider,
            ISecurityAuditService auditService,
            ObjectProvider<SessionInvalidator> sessionInvalidatorProvider)
    {
        this.userMapper = userMapper;
        this.userDeptMapper = userDeptMapper;
        this.userRoleMapper = userRoleMapper;
        this.userPostMapper = userPostMapper;
        this.roleMapper = roleMapper;
        this.deptMapper = deptMapper;
        this.outboxMapper = outboxMapper;
        this.deptScopeService = deptScopeService;
        this.configService = configService;
        this.roleServiceProvider = roleServiceProvider;
        this.auditService = auditService;
        this.sessionInvalidatorProvider = sessionInvalidatorProvider;
    }

    @Override
    @Transactional
    public boolean registerUser(SysUser user)
    {
        String event = "USER_REGISTER";
        try
        {
            String configuredDeptId = configService.selectConfigByKey("sys.user.defaultDeptId");
            if (StringUtils.isBlank(configuredDeptId))
            {
                throw new ServiceException("未配置注册用户默认主部门，已拒绝创建无归属账号");
            }
            Long deptId;
            try
            {
                deptId = Long.valueOf(configuredDeptId);
            }
            catch (NumberFormatException ex)
            {
                throw new ServiceException("注册用户默认主部门配置无效");
            }
            if (deptMapper.selectActiveDeptIds(List.of(deptId)).size() != 1)
            {
                throw new ServiceException("注册用户默认主部门不存在或不可用");
            }
            if (userMapper.checkUserNameUnique(user.getUserName()) != null)
            {
                throw new ServiceException("注册账号已存在");
            }
            user.setDeptId(deptId);
            user.setManagementScope(UserConstants.MANAGEMENT_SCOPE_DEPT);
            int rows = userMapper.insertUser(user);
            userDeptMapper.batchInsert(List.of(new SysUserDept(
                    user.getUserId(), deptId, user.getUserName())));
            createOutbox(user.getUserId(), 0L, event);
            auditService.recordSuccess(event, user.getUserId(), "用户自助注册",
                    null, Map.of("primaryDeptId", deptId), null, 0L);
            return rows > 0;
        }
        catch (RuntimeException ex)
        {
            Map<String, Object> requested = new LinkedHashMap<>();
            requested.put("userName", user == null ? null : user.getUserName());
            auditService.recordFailure(event, null, "用户自助注册", null,
                    requested, failureCode(ex), ex.getMessage());
            throw translateUnique(ex);
        }
    }

    @Override
    @Transactional
    public Long createUser(SysUserCreateRequest request)
    {
        String event = "USER_CREATE";
        try
        {
            Set<Long> deptIds = normalize(request.getDeptIds());
            validatePrimaryDepartment(request.getDeptId(), deptIds);
            deptScopeService.checkDeptAccess(deptIds);
            if (deptIds.size() > 1 && !SecurityUtils.isAdmin())
            {
                throw new ServiceException("只有最高平台管理员可以创建多部门账号");
            }
            if (deptIds.size() > 1 && !multiDeptWriteEnabled)
            {
                throw new ServiceException("多部门写入功能尚未开启");
            }
            if (userMapper.checkUserNameUnique(request.getUserName()) != null)
            {
                throw new ServiceException("登录账号已存在");
            }
            if (StringUtils.isNotEmpty(request.getPhonenumber())
                    && userMapper.checkPhoneUnique(request.getPhonenumber()) != null)
            {
                throw new ServiceException("手机号码已存在");
            }
            if (StringUtils.isNotEmpty(request.getEmail())
                    && userMapper.checkEmailUnique(request.getEmail()) != null)
            {
                throw new ServiceException("邮箱已存在");
            }
            validateRoles(Collections.emptySet(), normalize(request.getRoleIds()));

            SysUser user = toUser(request);
            user.setManagementScope(deptIds.size() > 1
                    ? UserConstants.MANAGEMENT_SCOPE_PLATFORM : UserConstants.MANAGEMENT_SCOPE_DEPT);
            user.setCreateBy(SecurityUtils.getUsername());
            user.setPassword(SecurityUtils.encryptPassword(request.getPassword()));
            userMapper.insertUser(user);
            replaceDepartments(user.getUserId(), deptIds);
            replaceRoles(user.getUserId(), normalize(request.getRoleIds()));
            replacePosts(user.getUserId(), normalize(request.getPostIds()));
            createOutbox(user.getUserId(), 0L, event);
            auditService.recordSuccess(event, user.getUserId(), "后台新增用户", null,
                    authorizationSnapshot(user.getUserId()), null, 0L);
            scheduleForceLogout(user.getUserId());
            return user.getUserId();
        }
        catch (RuntimeException ex)
        {
            Map<String, Object> requested = new LinkedHashMap<>();
            requested.put("userName", request == null ? null : request.getUserName());
            requested.put("deptId", request == null ? null : request.getDeptId());
            requested.put("deptIds", request == null || request.getDeptIds() == null
                    ? new Long[0] : request.getDeptIds());
            auditService.recordFailure(event, null, "后台新增用户", null,
                    requested,
                    failureCode(ex), ex.getMessage());
            throw translateUnique(ex);
        }
    }

    @Override
    @Transactional
    public int updateUserBase(SysUserUpdateRequest request)
    {
        String event = "USER_BASE_UPDATE";
        SysUser target = null;
        try
        {
            requireReason(request.getReason());
            target = lockTarget(request.getUserId());
            checkTargetWritable(target, true);
            deptScopeService.checkUserFullAccess(target.getUserId());
            SysUser phoneOwner = StringUtils.isEmpty(request.getPhonenumber()) ? null
                    : userMapper.checkPhoneUnique(request.getPhonenumber());
            if (phoneOwner != null && !target.getUserId().equals(phoneOwner.getUserId()))
            {
                throw new ServiceException("手机号码已存在");
            }
            SysUser emailOwner = StringUtils.isEmpty(request.getEmail()) ? null
                    : userMapper.checkEmailUnique(request.getEmail());
            if (emailOwner != null && !target.getUserId().equals(emailOwner.getUserId()))
            {
                throw new ServiceException("邮箱已存在");
            }
            Object before = authorizationSnapshot(target.getUserId());

            SysUser update = new SysUser();
            update.setUserId(target.getUserId());
            update.setNickName(request.getNickName());
            update.setEmail(request.getEmail());
            update.setPhonenumber(request.getPhonenumber());
            update.setSex(request.getSex());
            update.setRemark(request.getRemark());
            update.setUpdateBy(SecurityUtils.getUsername());
            int rows = userMapper.updateUser(update);
            replacePosts(target.getUserId(), normalize(request.getPostIds()));
            long version = bumpVersion(target.getUserId(), event);
            auditService.recordSuccess(event, target.getUserId(), request.getReason(),
                    before, authorizationSnapshot(target.getUserId()), target.getAuthVersion(), version);
            return rows;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, request.getReason(), request, ex);
            throw translateUnique(ex);
        }
    }

    @Override
    @Transactional
    public int updateDepartments(Long userId, SysUserDeptUpdateRequest request)
    {
        String event = "USER_DEPARTMENTS_UPDATE";
        SysUser target = null;
        try
        {
            requireReason(request.getReason());
            target = lockTarget(userId);
            checkTargetWritable(target, true);
            // 目标保护在上一步已经把内置超管挡住了；这里补的是"任何人不得给自己加授权"，
            // 原来写成 !isAdmin() && 是给超管留的口子，而那个口子在第一步就进不来，属于虚假承诺。
            if (SecurityUtils.getUserId().equals(userId))
            {
                throw new ServiceException("不能修改自己的任职部门，请由其他管理员操作");
            }
            Set<Long> oldIds = normalize(userDeptMapper.selectDeptIdsByUserId(userId));
            Set<Long> newIds = normalize(request.getDeptIds());
            validatePrimaryDepartment(request.getPrimaryDeptId(), newIds);
            if (newIds.size() > 1 && !multiDeptWriteEnabled)
            {
                throw new ServiceException("多部门写入功能尚未开启");
            }
            Set<Long> union = new LinkedHashSet<>(oldIds);
            union.addAll(newIds);
            deptScopeService.checkDeptAccess(union);
            if (UserConstants.MANAGEMENT_SCOPE_PLATFORM.equals(target.getManagementScope())
                    || newIds.size() > 1)
            {
                requireHighestAdmin("只有最高平台管理员可以修改多部门或平台管理账号");
            }

            Map<String, Object> before = authorizationSnapshot(userId);
            replaceDepartments(userId, newIds);
            SysUser update = new SysUser();
            update.setUserId(userId);
            update.setDeptId(request.getPrimaryDeptId());
            update.setUpdateBy(SecurityUtils.getUsername());
            if (newIds.size() > 1)
            {
                update.setManagementScope(UserConstants.MANAGEMENT_SCOPE_PLATFORM);
            }
            int rows = userMapper.updateUser(update);
            long version = bumpVersion(userId, event);
            auditService.recordSuccess(event, userId, request.getReason(), before,
                    authorizationSnapshot(userId), target.getAuthVersion(), version);
            return rows;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, request.getReason(), request, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int updateRoles(Long userId, SysUserRoleUpdateRequest request)
    {
        String event = "USER_ROLES_UPDATE";
        SysUser target = null;
        try
        {
            requireReason(request.getReason());
            target = lockTarget(userId);
            checkTargetWritable(target, true);
            // 同上：不给任何人给自己改角色的机会，超管的口子本来就走不到。
            if (SecurityUtils.getUserId().equals(userId))
            {
                throw new ServiceException("不能修改自己的角色，请由其他管理员操作");
            }
            Set<Long> oldIds = normalize(userRoleMapper.selectRoleIdsByUserId(userId));
            Set<Long> newIds = normalize(request.getRoleIds());
            validateRoles(oldIds, newIds);
            Map<String, Object> before = authorizationSnapshot(userId);
            replaceRoles(userId, newIds);
            long version = bumpVersion(userId, event);
            auditService.recordSuccess(event, userId, request.getReason(), before,
                    authorizationSnapshot(userId), target.getAuthVersion(), version);
            return 1;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, request.getReason(), request, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int updateManagementScope(Long userId, SysUserManagementScopeRequest request)
    {
        String event = "USER_MANAGEMENT_SCOPE_UPDATE";
        SysUser target = null;
        try
        {
            requireReason(request.getReason());
            requireHighestAdmin("只有最高平台管理员可以修改管理范围");
            // 本接口的服务对象就是 PLATFORM 账号（把它降级），所以第二层判定必须关掉
            target = lockTarget(userId);
            checkTargetWritable(target, false);
            if (!UserConstants.MANAGEMENT_SCOPE_DEPT.equals(request.getManagementScope()))
            {
                throw new ServiceException("专用接口仅允许将 PLATFORM 显式降级为 DEPT");
            }
            if (!UserConstants.MANAGEMENT_SCOPE_PLATFORM.equals(target.getManagementScope()))
            {
                throw new ServiceException("目标账号当前不是 PLATFORM");
            }
            List<Long> deptIds = userDeptMapper.selectDeptIdsByUserId(userId);
            if (deptIds.size() != 1 || !deptIds.get(0).equals(target.getDeptId()))
            {
                throw new ServiceException("降级前必须仅保留主部门这一条任职关系");
            }
            Map<String, Object> before = authorizationSnapshot(userId);
            int rows = userMapper.updateManagementScope(userId,
                    UserConstants.MANAGEMENT_SCOPE_DEPT, SecurityUtils.getUsername());
            long version = bumpVersion(userId, event);
            auditService.recordSuccess(event, userId, request.getReason(), before,
                    authorizationSnapshot(userId), target.getAuthVersion(), version);
            return rows;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, request.getReason(), request, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int updateStatus(SysUserStatusUpdateRequest request)
    {
        String event = "USER_STATUS_UPDATE";
        SysUser target = null;
        try
        {
            requireReason(request.getReason());
            target = lockTarget(request.getUserId());
            checkTargetWritable(target, true);
            deptScopeService.checkUserFullAccess(target.getUserId());
            String before = target.getStatus();
            int rows = userMapper.updateUserStatus(target.getUserId(), request.getStatus());
            long version = bumpVersion(target.getUserId(), event);
            auditService.recordSuccess(event, target.getUserId(), request.getReason(), before,
                    request.getStatus(), target.getAuthVersion(), version);
            return rows;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, request.getReason(), request, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int resetPassword(SysUserPasswordResetRequest request)
    {
        String event = "USER_PASSWORD_RESET";
        SysUser target = null;
        try
        {
            requireReason(request.getReason());
            target = lockTarget(request.getUserId());
            checkTargetWritable(target, true);
            deptScopeService.checkUserFullAccess(target.getUserId());
            int rows = userMapper.resetUserPwd(target.getUserId(),
                    SecurityUtils.encryptPassword(request.getPassword()));
            long version = bumpVersion(target.getUserId(), event);
            auditService.recordSuccess(event, target.getUserId(), request.getReason(),
                    null, null, target.getAuthVersion(), version);
            return rows;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, request.getReason(), null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int changeOwnPassword(Long userId, String newPassword)
    {
        String event = "USER_PASSWORD_CHANGE_SELF";
        SysUser target = null;
        try
        {
            // 自助改密属于凭据维护，不是对账号的管理动作，因此不走 checkTargetWritable；
            // 否则内置超管会被自己的目标保护拦住，永远改不了密码。
            target = lockSelfTarget(userId);
            int rows = userMapper.resetUserPwd(userId, SecurityUtils.encryptPassword(newPassword));
            long version = bumpVersion(userId, event);
            auditService.recordSuccess(event, userId, "员工自助修改本人密码",
                    null, null, target.getAuthVersion(), version);
            return rows;
        }
        catch (RuntimeException ex)
        {
            auditFailure(event, target, "员工自助修改本人密码", null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int deleteUsers(Long[] userIds, String reason)
    {
        String event = "USER_DELETE";
        requireReason(reason);
        List<SysUser> targets = new ArrayList<>();
        // 记录当前正在处理的目标，拒绝发生时审计才有具体的 target 可还原
        Long currentTargetId = null;
        try
        {
            Set<Long> ids = normalize(userIds);
            if (ids.contains(SecurityUtils.getUserId()))
            {
                throw new ServiceException("当前用户不能删除自己");
            }
            for (Long userId : ids)
            {
                currentTargetId = userId;
                SysUser target = lockTarget(userId);
                checkTargetWritable(target, true);
                deptScopeService.checkUserFullAccess(userId);
                targets.add(target);
            }
            for (SysUser target : targets)
            {
                Map<String, Object> before = authorizationSnapshot(target.getUserId());
                userRoleMapper.deleteUserRoleByUserId(target.getUserId());
                userPostMapper.deleteUserPostByUserId(target.getUserId());
                userDeptMapper.deleteByUserId(target.getUserId());
                userMapper.deleteUserById(target.getUserId());
                long version = bumpVersion(target.getUserId(), event);
                auditService.recordSuccess(event, target.getUserId(), reason, before,
                        null, target.getAuthVersion(), version);
            }
            return targets.size();
        }
        catch (RuntimeException ex)
        {
            auditService.recordFailure(event, currentTargetId, reason,
                    targets.stream().map(SysUser::getUserId).collect(Collectors.toList()),
                    userIds, failureCode(ex), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public int grantRoleToUsers(Long roleId, Long[] userIds, String reason)
    {
        return changeRoleForUsers(roleId, userIds, reason, true);
    }

    @Override
    @Transactional
    public int revokeRoleFromUsers(Long roleId, Long[] userIds, String reason)
    {
        return changeRoleForUsers(roleId, userIds, reason, false);
    }

    @Override
    @Transactional
    public int importUsers(List<SysUser> users, boolean updateSupport, String reason,
            String initialPassword)
    {
        String event = "USER_IMPORT";
        requireReason(reason);
        if (users == null || users.isEmpty())
        {
            throw new ServiceException("导入数据不能为空");
        }
        List<SysUser> existingUsers = new ArrayList<>();
        Set<String> names = new LinkedHashSet<>();
        // 导入是批量场景，失败时至少要留下"卡在哪一行"，否则审计里只有行数
        String currentUserName = null;
        try
        {
            // 第一阶段只校验和加锁，任何一行失败都不会开始写入。
            for (SysUser row : users)
            {
                currentUserName = row.getUserName();
                if (StringUtils.isBlank(row.getUserName()) || !names.add(row.getUserName()))
                {
                    throw new ServiceException("导入文件存在空账号或重复账号: " + row.getUserName());
                }
                deptScopeService.checkDeptAccess(Collections.singleton(row.getDeptId()));
                SysUser existing = userMapper.selectUserByUserName(row.getUserName());
                if (existing != null)
                {
                    if (!updateSupport)
                    {
                        throw new ServiceException("账号已存在且未确认覆盖: " + row.getUserName());
                    }
                    existing = lockTarget(existing.getUserId());
                    checkTargetWritable(existing, true);
                    deptScopeService.checkUserFullAccess(existing.getUserId());
                    existingUsers.add(existing);
                }
            }

            int affected = 0;
            for (SysUser row : users)
            {
                SysUser existing = existingUsers.stream()
                        .filter(user -> user.getUserName().equals(row.getUserName()))
                        .findFirst().orElse(null);
                if (existing == null)
                {
                    SysUserCreateRequest create = new SysUserCreateRequest();
                    create.setUserName(row.getUserName());
                    create.setNickName(row.getNickName());
                    create.setPassword(initialPassword);
                    create.setEmail(row.getEmail());
                    create.setPhonenumber(row.getPhonenumber());
                    create.setSex(row.getSex());
                    create.setStatus(StringUtils.defaultIfEmpty(row.getStatus(), "0"));
                    create.setRemark(row.getRemark());
                    create.setDeptId(row.getDeptId());
                    create.setDeptIds(new Long[] { row.getDeptId() });
                    create.setRoleIds(new Long[0]);
                    create.setPostIds(new Long[0]);
                    createUser(create);
                }
                else
                {
                    Map<String, Object> before = authorizationSnapshot(existing.getUserId());
                    replaceDepartments(existing.getUserId(), Collections.singleton(row.getDeptId()));
                    SysUser update = new SysUser();
                    update.setUserId(existing.getUserId());
                    update.setDeptId(row.getDeptId());
                    update.setNickName(row.getNickName());
                    update.setEmail(row.getEmail());
                    update.setPhonenumber(row.getPhonenumber());
                    update.setSex(row.getSex());
                    update.setStatus(row.getStatus());
                    update.setRemark(row.getRemark());
                    update.setUpdateBy(SecurityUtils.getUsername());
                    userMapper.updateUser(update);
                    long version = bumpVersion(existing.getUserId(), "USER_IMPORT_OVERWRITE");
                    auditService.recordSuccess("USER_IMPORT_OVERWRITE", existing.getUserId(), reason,
                            before, authorizationSnapshot(existing.getUserId()),
                            existing.getAuthVersion(), version);
                }
                affected++;
            }
            auditService.recordSuccess(event, null, reason, null,
                    Map.of("rowCount", affected, "updateSupport", updateSupport), null, null);
            return affected;
        }
        catch (RuntimeException ex)
        {
            Map<String, Object> requested = new LinkedHashMap<>();
            requested.put("rowCount", users.size());
            requested.put("updateSupport", updateSupport);
            requested.put("failedUserName", currentUserName);
            auditService.recordFailure(event, null, reason, null, requested,
                    failureCode(ex), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public void invalidateUsersByRole(Long roleId, String eventType, String reason)
    {
        invalidateUsers(userRoleMapper.selectUserIdsByRoleId(roleId), eventType, reason);
    }

    @Override
    @Transactional
    public void invalidateUsersByDept(Long deptId, String eventType, String reason)
    {
        invalidateUsers(userDeptMapper.selectUserIdsByDeptId(deptId), eventType, reason);
    }

    @Override
    @Transactional
    public void invalidateUsers(Collection<Long> userIds, String eventType, String reason)
    {
        Set<Long> ids = normalize(userIds);
        for (Long userId : ids)
        {
            SysUser target = userMapper.selectUserByIdForUpdate(userId);
            if (target == null)
            {
                continue;
            }
            long version = bumpVersion(userId, eventType);
            auditService.recordSuccess(eventType, userId, reason, null, null,
                    target.getAuthVersion(), version);
        }
    }

    private int changeRoleForUsers(Long roleId, Long[] userIds, String reason, boolean grant)
    {
        String event = grant ? "ROLE_GRANT_USERS" : "ROLE_REVOKE_USERS";
        requireReason(reason);
        List<SysUser> targets = new ArrayList<>();
        Long currentTargetId = null;
        try
        {
            Set<Long> ids = normalize(userIds);
            for (Long userId : ids)
            {
                currentTargetId = userId;
                SysUser target = lockTarget(userId);
                checkTargetWritable(target, true);
                if (SecurityUtils.getUserId().equals(userId))
                {
                    throw new ServiceException("不能修改自己的角色，请由其他管理员操作");
                }
                deptScopeService.checkUserFullAccess(userId);
                Set<Long> oldRoles = normalize(userRoleMapper.selectRoleIdsByUserId(userId));
                Set<Long> newRoles = new LinkedHashSet<>(oldRoles);
                if (grant)
                {
                    newRoles.add(roleId);
                }
                else
                {
                    newRoles.remove(roleId);
                }
                validateRoles(oldRoles, newRoles);
                targets.add(target);
            }
            for (SysUser target : targets)
            {
                Set<Long> newRoles = normalize(userRoleMapper.selectRoleIdsByUserId(target.getUserId()));
                if (grant)
                {
                    newRoles.add(roleId);
                }
                else
                {
                    newRoles.remove(roleId);
                }
                Map<String, Object> before = authorizationSnapshot(target.getUserId());
                replaceRoles(target.getUserId(), newRoles);
                long version = bumpVersion(target.getUserId(), event);
                auditService.recordSuccess(event, target.getUserId(), reason, before,
                        authorizationSnapshot(target.getUserId()), target.getAuthVersion(), version);
            }
            return targets.size();
        }
        catch (RuntimeException ex)
        {
            auditService.recordFailure(event, currentTargetId, reason,
                    targets.stream().map(SysUser::getUserId).collect(Collectors.toList()),
                    userIds, failureCode(ex), ex.getMessage());
            throw ex;
        }
    }

    /**
     * 锁定目标行并校验存在性，不含任何策略判定。
     *
     * <p>与 {@link #checkTargetWritable} 拆开是为了让策略拒绝发生在 target 已赋值之后：
     * 否则拒绝时 catch 块只能拿到 null，失败审计的 target 字段永远是空的，事后无法还原"试图改谁"。</p>
     */
    private SysUser lockTarget(Long userId)
    {
        SysUser target = userMapper.selectUserByIdForUpdate(userId);
        if (target == null || !"0".equals(target.getDelFlag()))
        {
            throw new ServiceException("目标用户不存在或已删除");
        }
        return target;
    }

    /**
     * 目标账号可写性判定。与调用者身份正交：先问"这个对象能不能被业务接口改"，再问"你有没有权限改"。
     *
     * <p>请求被拒时用 403 表达策略拒绝，而不是 500。策略拒绝是预期内的结果，不应混进系统异常，
     * 前端也能据此给出引导而不是"操作失败"。</p>
     *
     * @param protectPlatform 是否需要附加"平台管理账号仅最高平台管理员可操作"的第二层判定。
     *        管理范围降级接口本身就是作用于 PLATFORM 账号的，必须传 false。
     */
    private void checkTargetWritable(SysUser target, boolean protectPlatform)
    {
        if (target.isAdmin())
        {
            throw policyDenied("该账号是系统内置最高管理员，不通过用户管理修改；"
                    + "如需变更昵称、手机号或密码，请用该账号登录后在「个人中心」自助处理");
        }
        if (protectPlatform && UserConstants.MANAGEMENT_SCOPE_PLATFORM.equals(target.getManagementScope()))
        {
            requireHighestAdmin("平台管理账号仅最高平台管理员可操作");
        }
    }

    /**
     * 自助场景的目标加锁：只允许操作本人，且不受"内置超管禁用业务接口写入"约束。
     *
     * <p>内置超管的保护针对的是<b>管理属性</b>（昵称、手机号、状态、角色、部门、删除、代重置密码），
     * 改自己的密码属于凭据自助维护，和个人中心改资料同一条旁路；若一并封禁，
     * 超管将无法改密，只能绕过应用直接改库，反而削弱了审计。</p>
     */
    private SysUser lockSelfTarget(Long userId)
    {
        if (!SecurityUtils.getUserId().equals(userId))
        {
            throw policyDenied("自助操作仅允许作用于本人账号");
        }
        return lockTarget(userId);
    }

    private ServiceException policyDenied(String message)
    {
        return new ServiceException(message, HttpStatus.FORBIDDEN.value());
    }

    private void validatePrimaryDepartment(Long primaryDeptId, Set<Long> deptIds)
    {
        if (primaryDeptId == null || !deptIds.contains(primaryDeptId))
        {
            throw new ServiceException("主部门必须包含在完整任职部门集合中");
        }
    }

    private void validateRoles(Set<Long> oldRoleIds, Set<Long> newRoleIds)
    {
        Set<Long> union = new LinkedHashSet<>(oldRoleIds);
        union.addAll(newRoleIds);
        if (union.isEmpty())
        {
            return;
        }
        if (!SecurityUtils.isAdmin() && union.contains(1L))
        {
            throw new ServiceException("普通管理员不能授予或撤销超级管理员角色");
        }
        for (Long roleId : union)
        {
            SysRole role = roleMapper.selectRoleById(roleId);
            if (role == null || !"0".equals(role.getDelFlag()))
            {
                throw new ServiceException("角色不存在或已删除: " + roleId);
            }
        }
        ISysRoleService roleService = roleServiceProvider.getIfAvailable();
        if (roleService != null)
        {
            roleService.checkRoleDataScope(union.toArray(new Long[0]));
        }
    }

    private void replaceDepartments(Long userId, Set<Long> deptIds)
    {
        userDeptMapper.deleteByUserId(userId);
        if (!deptIds.isEmpty())
        {
            String actor = SecurityUtils.getUsername();
            List<SysUserDept> rows = deptIds.stream()
                    .map(id -> new SysUserDept(userId, id, actor)).collect(Collectors.toList());
            userDeptMapper.batchInsert(rows);
        }
    }

    private void replaceRoles(Long userId, Set<Long> roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        if (!roleIds.isEmpty())
        {
            List<SysUserRole> rows = roleIds.stream().map(roleId -> {
                SysUserRole row = new SysUserRole();
                row.setUserId(userId);
                row.setRoleId(roleId);
                return row;
            }).collect(Collectors.toList());
            userRoleMapper.batchUserRole(rows);
        }
    }

    private void replacePosts(Long userId, Set<Long> postIds)
    {
        userPostMapper.deleteUserPostByUserId(userId);
        if (!postIds.isEmpty())
        {
            List<SysUserPost> rows = postIds.stream().map(postId -> {
                SysUserPost row = new SysUserPost();
                row.setUserId(userId);
                row.setPostId(postId);
                return row;
            }).collect(Collectors.toList());
            userPostMapper.batchUserPost(rows);
        }
    }

    private long bumpVersion(Long userId, String eventType)
    {
        if (userMapper.incrementAuthVersion(userId) != 1)
        {
            throw new ServiceException("递增授权版本失败");
        }
        SysUserAuthState state = userMapper.selectAuthStateByUserId(userId);
        if (state == null)
        {
            throw new ServiceException("无法读取更新后的授权版本");
        }
        createOutbox(userId, state.getAuthVersion(), eventType);
        scheduleForceLogout(userId);
        return state.getAuthVersion();
    }

    private void createOutbox(Long userId, Long version, String eventType)
    {
        SysSecurityOutbox event = new SysSecurityOutbox();
        event.setUserId(userId);
        event.setTargetAuthVersion(version);
        event.setEventType(eventType);
        outboxMapper.insert(event);
    }

    private void scheduleForceLogout(Long userId)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                SessionInvalidator invalidator = sessionInvalidatorProvider.getIfAvailable();
                if (invalidator == null)
                {
                    return;
                }
                try
                {
                    invalidator.forceLogoutByUserId(userId);
                }
                catch (RuntimeException ex)
                {
                    log.warn("用户[{}]提交后立即强退失败，等待outbox重试: {}", userId, ex.getMessage());
                }
            }
        });
    }

    private Map<String, Object> authorizationSnapshot(Long userId)
    {
        SysUser user = userMapper.selectUserById(userId);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("primaryDeptId", user == null ? null : user.getDeptId());
        snapshot.put("deptIds", userDeptMapper.selectDeptIdsByUserId(userId));
        snapshot.put("roleIds", userRoleMapper.selectRoleIdsByUserId(userId));
        snapshot.put("managementScope", user == null ? null : user.getManagementScope());
        return snapshot;
    }

    private SysUser toUser(SysUserCreateRequest request)
    {
        SysUser user = new SysUser();
        user.setUserName(request.getUserName());
        user.setNickName(request.getNickName());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhonenumber());
        user.setSex(request.getSex());
        user.setStatus(StringUtils.defaultIfEmpty(request.getStatus(), "0"));
        user.setRemark(request.getRemark());
        user.setDeptId(request.getDeptId());
        return user;
    }

    private Set<Long> normalize(Long[] ids)
    {
        return ids == null ? Collections.emptySet() : normalize(Arrays.asList(ids));
    }

    private Set<Long> normalize(Collection<Long> ids)
    {
        if (ids == null)
        {
            return Collections.emptySet();
        }
        return ids.stream().filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void requireHighestAdmin(String message)
    {
        if (!SecurityUtils.isAdmin())
        {
            throw new ServiceException(message);
        }
    }

    private void requireReason(String reason)
    {
        if (StringUtils.isBlank(reason))
        {
            throw new ServiceException("敏感操作必须填写原因");
        }
    }

    private void auditFailure(String event, SysUser target, String reason, Object requested,
            RuntimeException ex)
    {
        auditService.recordFailure(event, target == null ? null : target.getUserId(), reason,
                target == null ? null : targetAuditSnapshot(target),
                requested, failureCode(ex), ex.getMessage());
    }

    private Map<String, Object> targetAuditSnapshot(SysUser target)
    {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("userId", target.getUserId());
        snapshot.put("primaryDeptId", target.getDeptId());
        snapshot.put("managementScope", target.getManagementScope());
        return snapshot;
    }

    private String failureCode(RuntimeException ex)
    {
        return ex instanceof ServiceException ? "AUTHORIZATION_OR_VALIDATION_DENIED" : "MUTATION_FAILED";
    }

    /**
     * 唯一约束冲突的可读化。
     *
     * <p>手机号与登录账号都在写入前做了应用层唯一校验，但并发提交时两次校验可以同时通过，
     * 最终由数据库唯一索引拦下。这里把索引冲突翻译成业务提示，避免把 SQL 异常直接抛给页面。</p>
     */
    private RuntimeException translateUnique(RuntimeException ex)
    {
        if (ex instanceof DuplicateKeyException)
        {
            String message = String.valueOf(ex.getMessage());
            if (message.contains("uk_sys_user_phone"))
            {
                return new ServiceException("该手机号已被其他账号使用，请核对是否重复录入；"
                        + "若为历史重复数据，请先修正原账号手机号");
            }
        }
        return ex;
    }
}
