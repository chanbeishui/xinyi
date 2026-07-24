package com.xinyi.system.service.impl;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xinyi.common.constant.CacheConstants;
import com.xinyi.common.constant.UserConstants;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.redis.RedisCache;
import com.xinyi.common.exception.ServiceException;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.system.domain.vo.UserImportPreviewResult;
import com.xinyi.system.mapper.SysUserMapper;
import com.xinyi.system.service.IDeptScopeService;
import com.xinyi.system.service.ISysConfigService;
import com.xinyi.system.service.IUserAuthorizationMutationService;
import com.xinyi.system.service.IUserImportService;

/**
 * 通过文件摘要和一次性令牌绑定预检与正式执行。
 */
@Service
public class UserImportServiceImpl implements IUserImportService
{
    private static final int PREVIEW_MINUTES = 15;

    private final RedisCache redisCache;
    private final SysUserMapper userMapper;
    private final IDeptScopeService deptScopeService;
    private final ISysConfigService configService;
    private final IUserAuthorizationMutationService mutationService;

    public UserImportServiceImpl(RedisCache redisCache, SysUserMapper userMapper,
            IDeptScopeService deptScopeService, ISysConfigService configService,
            IUserAuthorizationMutationService mutationService)
    {
        this.redisCache = redisCache;
        this.userMapper = userMapper;
        this.deptScopeService = deptScopeService;
        this.configService = configService;
        this.mutationService = mutationService;
    }

    @Override
    public UserImportPreviewResult preview(byte[] fileBytes, List<SysUser> users, boolean updateSupport)
    {
        if (users == null || users.isEmpty())
        {
            throw new ServiceException("导入文件没有用户数据");
        }
        Set<String> names = new HashSet<>();
        int createCount = 0;
        int updateCount = 0;
        int platformCount = 0;
        for (SysUser row : users)
        {
            if (StringUtils.isBlank(row.getUserName()) || !names.add(row.getUserName()))
            {
                throw new ServiceException("导入文件存在空账号或重复账号: " + row.getUserName());
            }
            deptScopeService.checkDeptAccess(Collections.singleton(row.getDeptId()));
            SysUser existing = userMapper.selectUserByUserName(row.getUserName());
            if (existing == null)
            {
                createCount++;
                continue;
            }
            if (!updateSupport)
            {
                throw new ServiceException("账号已存在，必须选择覆盖更新: " + row.getUserName());
            }
            deptScopeService.checkUserFullAccess(existing.getUserId());
            updateCount++;
            if (UserConstants.MANAGEMENT_SCOPE_PLATFORM.equals(existing.getManagementScope()))
            {
                platformCount++;
                if (!SecurityUtils.isAdmin())
                {
                    throw new ServiceException("导入涉及PLATFORM账号，仅最高平台管理员可执行");
                }
            }
        }

        String token = UUID.randomUUID().toString();
        ImportPreviewTicket ticket = new ImportPreviewTicket();
        ticket.fileHash = hash(fileBytes);
        ticket.actorUserId = SecurityUtils.getUserId();
        ticket.updateSupport = updateSupport;
        ticket.rowCount = users.size();
        redisCache.setCacheObject(key(token), ticket, PREVIEW_MINUTES, TimeUnit.MINUTES);

        UserImportPreviewResult result = new UserImportPreviewResult();
        result.setPreviewToken(token);
        result.setRowCount(users.size());
        result.setCreateCount(createCount);
        result.setUpdateCount(updateCount);
        result.setPlatformAccountCount(platformCount);
        result.setExpiresInSeconds(PREVIEW_MINUTES * 60L);
        return result;
    }

    @Override
    @Transactional
    public int execute(byte[] fileBytes, List<SysUser> users, boolean updateSupport,
            String previewToken, boolean confirmed, String reason)
    {
        if (!confirmed)
        {
            throw new ServiceException("必须明确确认导入");
        }
        if (StringUtils.isBlank(reason))
        {
            throw new ServiceException("导入覆盖必须填写原因");
        }
        ImportPreviewTicket ticket = redisCache.getAndDeleteCacheObject(key(previewToken));
        if (ticket == null)
        {
            throw new ServiceException("预检令牌不存在或已过期");
        }
        if (!SecurityUtils.getUserId().equals(ticket.actorUserId)
                || updateSupport != ticket.updateSupport
                || users == null || users.size() != ticket.rowCount
                || !ticket.fileHash.equals(hash(fileBytes)))
        {
            throw new ServiceException("执行文件、操作者或参数与预检不一致");
        }
        String initialPassword = configService.selectConfigByKey("sys.user.initPassword");
        return mutationService.importUsers(users, updateSupport, reason, initialPassword);
    }

    private String key(String token)
    {
        return CacheConstants.USER_IMPORT_PREVIEW_KEY + token;
    }

    private String hash(byte[] bytes)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IllegalStateException("SHA-256不可用", ex);
        }
    }

    public static class ImportPreviewTicket implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private String fileHash;
        private Long actorUserId;
        private boolean updateSupport;
        private int rowCount;

        public String getFileHash()
        {
            return fileHash;
        }

        public void setFileHash(String fileHash)
        {
            this.fileHash = fileHash;
        }

        public Long getActorUserId()
        {
            return actorUserId;
        }

        public void setActorUserId(Long actorUserId)
        {
            this.actorUserId = actorUserId;
        }

        public boolean isUpdateSupport()
        {
            return updateSupport;
        }

        public void setUpdateSupport(boolean updateSupport)
        {
            this.updateSupport = updateSupport;
        }

        public int getRowCount()
        {
            return rowCount;
        }

        public void setRowCount(int rowCount)
        {
            this.rowCount = rowCount;
        }
    }
}
