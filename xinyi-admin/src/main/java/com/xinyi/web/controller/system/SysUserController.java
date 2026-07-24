package com.xinyi.web.controller.system;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.common.annotation.Log;
import com.xinyi.common.core.controller.BaseController;
import com.xinyi.common.core.domain.AjaxResult;
import com.xinyi.common.core.domain.entity.SysDept;
import com.xinyi.common.core.domain.entity.SysRole;
import com.xinyi.common.core.domain.entity.SysUser;
import com.xinyi.common.core.page.TableDataInfo;
import com.xinyi.common.enums.BusinessType;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.poi.ExcelUtil;
import com.xinyi.system.domain.dto.SysUserCreateRequest;
import com.xinyi.system.domain.dto.SysUserDeptUpdateRequest;
import com.xinyi.system.domain.dto.SysUserManagementScopeRequest;
import com.xinyi.system.domain.dto.SysUserPasswordResetRequest;
import com.xinyi.system.domain.dto.SysUserQuery;
import com.xinyi.system.domain.dto.SysUserRoleUpdateRequest;
import com.xinyi.system.domain.dto.SysUserStatusUpdateRequest;
import com.xinyi.system.domain.dto.SysUserUpdateRequest;
import com.xinyi.system.service.ISysDeptService;
import com.xinyi.system.service.ISysPostService;
import com.xinyi.system.service.ISysRoleService;
import com.xinyi.system.service.ISysUserService;
import com.xinyi.system.service.IUserAuthorizationMutationService;
import com.xinyi.system.service.IUserImportService;

/**
 * 用户后台管理接口。实体、查询DTO和敏感写DTO相互分离。
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private IUserAuthorizationMutationService authorizationMutationService;

    @Autowired
    private IUserImportService userImportService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUserQuery query)
    {
        startPage();
        return getDataTable(userService.selectUserList(query));
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUserQuery query)
    {
        List<SysUser> list = userService.selectUserList(query);
        new ExcelUtil<SysUser>(SysUser.class).exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData()
    {
        return error("该入口已停用，请先调用 /import/preview，再调用 /import/execute");
    }

    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/import/preview")
    public AjaxResult importPreview(MultipartFile file, boolean updateSupport) throws Exception
    {
        byte[] bytes = file.getBytes();
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        List<SysUser> users = util.importExcel(new ByteArrayInputStream(bytes));
        return success(userImportService.preview(bytes, users, updateSupport));
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/import/execute")
    public AjaxResult importExecute(MultipartFile file, boolean updateSupport,
            String previewToken, boolean confirmed, String reason) throws Exception
    {
        byte[] bytes = file.getBytes();
        ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
        List<SysUser> users = util.importExcel(new ByteArrayInputStream(bytes));
        return toAjax(userImportService.execute(bytes, users, updateSupport,
                previewToken, confirmed, reason));
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        new ExcelUtil<SysUser>(SysUser.class).importTemplateExcel(response, "用户数据");
    }

    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = { "/", "/{userId}" })
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (userId != null)
        {
            userService.checkUserDataScope(userId);
            SysUser user = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, user);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", user.getRoles().stream().map(SysRole::getRoleId)
                    .collect(Collectors.toList()));
        }
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SecurityUtils.isAdmin(userId) ? roles
                : roles.stream().filter(role -> !role.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUserCreateRequest request)
    {
        return success(authorizationMutationService.createUser(request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户基本资料", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUserUpdateRequest request)
    {
        return toAjax(authorizationMutationService.updateUserBase(request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds, @RequestParam String reason)
    {
        return toAjax(authorizationMutationService.deleteUsers(userIds, reason));
    }

    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户密码重置", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@Validated @RequestBody SysUserPasswordResetRequest request)
    {
        return toAjax(authorizationMutationService.resetPassword(request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@Validated @RequestBody SysUserStatusUpdateRequest request)
    {
        return toAjax(authorizationMutationService.updateStatus(request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable Long userId)
    {
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SecurityUtils.isAdmin(userId) ? roles
                : roles.stream().filter(role -> !role.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 旧接口保留兼容，但仍委托统一授权服务。
     */
    @PreAuthorize("@ss.hasPermi('system:user:role:edit')")
    @Log(title = "用户角色", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds, String reason)
    {
        SysUserRoleUpdateRequest request = new SysUserRoleUpdateRequest();
        request.setRoleIds(roleIds);
        request.setReason(reason);
        authorizationMutationService.updateRoles(userId, request);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('system:user:dept:edit')")
    @Log(title = "用户任职部门", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/departments")
    public AjaxResult updateDepartments(@PathVariable Long userId,
            @Validated @RequestBody SysUserDeptUpdateRequest request)
    {
        return toAjax(authorizationMutationService.updateDepartments(userId, request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:role:edit')")
    @Log(title = "用户角色", businessType = BusinessType.GRANT)
    @PutMapping("/{userId}/roles")
    public AjaxResult updateRoles(@PathVariable Long userId,
            @Validated @RequestBody SysUserRoleUpdateRequest request)
    {
        return toAjax(authorizationMutationService.updateRoles(userId, request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:management:edit')")
    @Log(title = "用户管理范围", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/management-scope")
    public AjaxResult updateManagementScope(@PathVariable Long userId,
            @Validated @RequestBody SysUserManagementScopeRequest request)
    {
        return toAjax(authorizationMutationService.updateManagementScope(userId, request));
    }

    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept)
    {
        return success(deptService.selectDeptTreeList(dept));
    }
}
