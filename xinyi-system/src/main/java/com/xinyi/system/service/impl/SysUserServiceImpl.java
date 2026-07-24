package com.xinyi.system.service.impl;

import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.xinyi.common.constant.UserConstants;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.spring.SpringUtils;
import com.xinyi.system.domain.SysPost;
import com.xinyi.system.domain.SysUserAuthState;
import com.xinyi.system.domain.SysUserDept;
import com.xinyi.system.domain.dto.SysUserQuery;
import com.xinyi.system.domain.dto.UserVisibilityContext;
import com.xinyi.system.mapper.SysPostMapper;
import com.xinyi.system.mapper.SysRoleMapper;
import com.xinyi.system.mapper.SysUserDeptMapper;
import com.xinyi.system.mapper.SysUserMapper;
import com.xinyi.system.service.IDeptScopeService;
import com.xinyi.system.service.ISecurityAuditService;
import com.xinyi.system.service.IUserAuthorizationMutationService;
import com.xinyi.system.service.ISysUserService;

/**
 * 用户 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysUserServiceImpl implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserDeptMapper userDeptMapper;

    @Autowired
    private IDeptScopeService deptScopeService;

    @Autowired
    private ISecurityAuditService securityAuditService;

    @Autowired
    private IUserAuthorizationMutationService authorizationMutationService;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectUserList(SysUserQuery query)
    {
        UserVisibilityContext visibility = deptScopeService.buildUserVisibility(query, "system:user:list");
        List<SysUser> users = userMapper.selectUserList(query, visibility);
        attachVisibleMemberships(users, visibility);
        return users;
    }

    /**
     * 仅供旧内部调用过渡，外部接口必须使用查询 DTO。
     */
    public List<SysUser> selectUserList(SysUser user)
    {
        SysUserQuery query = new SysUserQuery();
        query.setUserId(user.getUserId());
        query.setUserName(user.getUserName());
        query.setPhonenumber(user.getPhonenumber());
        query.setStatus(user.getStatus());
        query.setDeptId(user.getDeptId());
        query.setParams(user.getParams());
        return selectUserList(query);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectAllocatedList(SysUserQuery query)
    {
        UserVisibilityContext visibility = deptScopeService.buildUserVisibility(query, "system:role:list");
        List<SysUser> users = userMapper.selectAllocatedList(query, visibility);
        attachVisibleMemberships(users, visibility);
        return users;
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectUnallocatedList(SysUserQuery query)
    {
        UserVisibilityContext visibility = deptScopeService.buildUserVisibility(query, "system:role:list");
        List<SysUser> users = userMapper.selectUnallocatedList(query, visibility);
        attachVisibleMemberships(users, visibility);
        return users;
    }

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return attachMemberships(userMapper.selectUserByUserName(userName), true);
    }

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId)
    {
        return attachMemberships(userMapper.selectUserById(userId), false);
    }

    @Override
    public SysUserAuthState selectAuthStateByUserId(Long userId)
    {
        return userMapper.selectAuthStateByUserId(userId);
    }

    /**
     * 查询用户所属角色组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 校验用户名称是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     * 
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     * 
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId)
    {
        try
        {
            if (userId != null)
            {
                SysUser target = userMapper.selectUserById(userId);
                if (!SecurityUtils.isAdmin() && target != null
                        && UserConstants.MANAGEMENT_SCOPE_PLATFORM.equals(target.getManagementScope()))
                {
                    throw new ServiceException("平台管理账号仅最高平台管理员可查看完整后台资料");
                }
                deptScopeService.checkUserFullAccess(userId);
                return;
            }
            if (!SecurityUtils.isAdmin())
            {
                SysUser user = new SysUser();
                user.setUserId(userId);
                List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
                if (StringUtils.isEmpty(users))
                {
                    throw new ServiceException("没有权限访问用户数据！");
                }
            }
        }
        catch (RuntimeException ex)
        {
            securityAuditService.recordFailure("USER_DETAIL_ACCESS_DENIED", userId,
                    "用户详情数据范围拒绝", null, null, "DATA_SCOPE_DENIED", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 新增保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user)
    {
        throw new ServiceException("后台新增用户必须调用统一授权服务");
    }

    /**
     * 注册用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public boolean registerUser(SysUser user)
    {
        return authorizationMutationService.registerUser(user);
    }

    /**
     * 修改保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user)
    {
        throw new ServiceException("后台用户修改必须调用统一授权服务");
    }

    /**
     * 用户授权角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        throw new ServiceException("角色变更必须调用统一授权服务并填写原因");
    }

    /**
     * 修改用户状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user)
    {
        throw new ServiceException("状态变更必须调用统一授权服务并填写原因");
    }

    /**
     * 修改用户基本信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateUserProfile(user);
    }

    /**
     * 修改用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar)
    {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    /**
     * 更新用户登录信息（IP和登录时间）
     * 
     * @param userId 用户ID
     * @param loginIp 登录IP地址
     * @param loginDate 登录时间
     * @return 结果
     */
    public void updateLoginInfo(Long userId, String loginIp, Date loginDate)
    {
        userMapper.updateLoginInfo(userId, loginIp, loginDate);
    }

    /**
     * 重置用户密码
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user)
    {
        throw new ServiceException("管理员重置密码必须调用统一授权服务并填写原因");
    }

    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(Long userId, String password)
    {
        throw new ServiceException("密码修改必须调用统一授权服务");
    }

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId)
    {
        throw new ServiceException("用户删除必须调用统一授权服务并填写原因");
    }

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds)
    {
        throw new ServiceException("批量删除必须调用统一授权服务并填写原因");
    }

    /**
     * 导入用户数据
     * 
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        throw new ServiceException("旧导入入口已停用，必须使用预检令牌和统一授权服务执行");
    }

    private SysUser attachMemberships(SysUser user, boolean activeOnly)
    {
        if (user == null)
        {
            return null;
        }
        List<Long> deptIds = activeOnly
                ? userDeptMapper.selectActiveDeptIdsByUserId(user.getUserId())
                : userDeptMapper.selectDeptIdsByUserId(user.getUserId());
        if (CollectionUtils.isEmpty(deptIds) && user.getDeptId() != null)
        {
            deptIds = List.of(user.getDeptId());
            log.warn("用户[{}]缺少任职关系，临时回退主部门；请运行完整性迁移", user.getUserId());
        }
        user.setDeptIds(deptIds.toArray(new Long[0]));
        return user;
    }

    private void attachVisibleMemberships(List<SysUser> users, UserVisibilityContext visibility)
    {
        if (CollectionUtils.isEmpty(users))
        {
            return;
        }
        List<Long> userIds = users.stream().map(SysUser::getUserId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toList());
        Map<Long, List<Long>> memberships = userIds.isEmpty()
                ? Collections.emptyMap()
                : userDeptMapper.selectByUserIds(userIds).stream()
                        .collect(Collectors.groupingBy(SysUserDept::getUserId,
                                Collectors.mapping(SysUserDept::getDeptId, Collectors.toList())));
        List<Long> visibleDeptIds = visibility.getVisibleDeptIds();
        for (SysUser user : users)
        {
            List<Long> all = memberships.getOrDefault(user.getUserId(), Collections.emptyList());
            if (visibility.isAllData()
                    || visibility.isIncludeSelf()
                            && user.getUserId().equals(visibility.getCurrentUserId()))
            {
                user.setDeptIds(all.toArray(new Long[0]));
            }
            else
            {
                List<Long> visible = all.stream()
                        .filter(visibleDeptIds::contains)
                        .collect(Collectors.toList());
                user.setDeptIds(visible.toArray(new Long[0]));
            }
        }
    }
}
