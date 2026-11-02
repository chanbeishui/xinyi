package com.xinyi.framework.security.handle;

import java.io.IOException;
import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.xinyi.common.constant.HttpStatus;
import com.xinyi.common.core.domain.AjaxResult;
import com.xinyi.common.utils.ServletUtils;
import com.xinyi.common.utils.StringUtils;

/**
 * 认证失败处理类 返回未授权
 * 
 * @author ruoyi
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable
{
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException
    {
        if (request.getRequestURI().startsWith(request.getContextPath() + "/api/admin/"))
        {
            java.util.Map<String,Object> body = new java.util.LinkedHashMap<>();
            body.put("code", 401); body.put("errorCode", "VI_AUTH_REQUIRED");
            body.put("msg", "需要有效的后台登录态"); body.put("requestId", java.util.UUID.randomUUID().toString());
            body.put("retryable", false); body.put("retryAfterSeconds", null); body.put("data", java.util.Map.of());
            response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write(JSON.toJSONString(body, com.alibaba.fastjson2.JSONWriter.Feature.WriteNulls));
            return;
        }
        int code = HttpStatus.UNAUTHORIZED;
        String msg = StringUtils.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}
