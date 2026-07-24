package com.xinyi.framework.aspectj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.xinyi.common.annotation.DataScope;
import com.xinyi.common.core.domain.BaseEntity;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.framework.security.context.PermissionContextHolder;
import com.xinyi.system.domain.vo.DeptScopeResult;
import com.xinyi.system.service.IDeptScopeService;

/**
 * 查询数据范围切面。写接口不得依赖本切面进行授权。
 */
@Aspect
@Component
public class DataScopeAspect
{
    public static final String DATA_SCOPE = "dataScope";

    private final IDeptScopeService deptScopeService;

    public DataScopeAspect(IDeptScopeService deptScopeService)
    {
        this.deptScopeService = deptScopeService;
    }

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope)
    {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    protected void handleDataScope(JoinPoint joinPoint, DataScope annotation)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().isAdmin())
        {
            return;
        }
        String permission = StringUtils.defaultIfEmpty(annotation.permission(),
                PermissionContextHolder.getContext());
        DeptScopeResult scope = deptScopeService.resolve(loginUser, permission);
        if (scope.isAllData())
        {
            return;
        }

        List<String> conditions = new ArrayList<>();
        if (!scope.getDeptIds().isEmpty() && validIdentifier(annotation.deptAlias())
                && validIdentifier(annotation.deptField()))
        {
            String ids = scope.getDeptIds().stream().map(String::valueOf).collect(Collectors.joining(","));
            conditions.add(annotation.deptAlias() + "." + annotation.deptField() + " IN (" + ids + ")");
        }
        if (scope.isIncludeSelf() && validIdentifier(annotation.userAlias())
                && validIdentifier(annotation.userField()))
        {
            conditions.add(annotation.userAlias() + "." + annotation.userField() + " = "
                    + loginUser.getUserId());
        }
        if (conditions.isEmpty())
        {
            conditions.add("1 = 0");
        }
        putDataScope(joinPoint, " AND (" + String.join(" OR ", conditions) + ")");
    }

    private boolean validIdentifier(String value)
    {
        return StringUtils.isNotBlank(value) && value.matches("[A-Za-z0-9_]+");
    }

    private void clearDataScope(JoinPoint joinPoint)
    {
        putDataScope(joinPoint, "");
    }

    private void putDataScope(JoinPoint joinPoint, String sql)
    {
        if (joinPoint.getArgs().length == 0 || !(joinPoint.getArgs()[0] instanceof BaseEntity))
        {
            return;
        }
        BaseEntity entity = (BaseEntity) joinPoint.getArgs()[0];
        entity.getParams().put(DATA_SCOPE, sql);
    }
}
