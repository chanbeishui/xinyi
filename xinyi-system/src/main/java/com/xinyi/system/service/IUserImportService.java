package com.xinyi.system.service;

import java.util.List;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.system.domain.vo.UserImportPreviewResult;

/**
 * 用户Excel导入预检与确认执行。
 */
public interface IUserImportService
{
    UserImportPreviewResult preview(byte[] fileBytes, List<SysUser> users, boolean updateSupport);

    int execute(byte[] fileBytes, List<SysUser> users, boolean updateSupport,
            String previewToken, boolean confirmed, String reason);
}
