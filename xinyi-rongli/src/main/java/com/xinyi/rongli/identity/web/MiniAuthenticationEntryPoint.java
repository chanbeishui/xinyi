package com.xinyi.rongli.identity.web;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.xinyi.rongli.identity.dto.ApiError;
import com.xinyi.rongli.platform.error.ViErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MINI 域认证入口点：返回【真实 HTTP 401】+ ApiError 信封（VI_AUTH_REQUIRED），
 * 与框架 sys 的 AuthenticationEntryPointImpl（恒 200 + body.code）刻意区分——vi 契约要求真实状态码，
 * 小程序 http.ts 依赖 status===401 && errorCode==='VI_AUTH_REQUIRED' 触发刷新。
 * 不泄露令牌细节；WWW-Authenticate 仅标注 realm="mini"。
 */
public class MiniAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiError body = ApiErrors.of(ViErrorCode.VI_AUTH_REQUIRED,
                ViErrorCode.VI_AUTH_REQUIRED.defaultMsg(), UUID.randomUUID().toString());
        response.setStatus(ViErrorCode.VI_AUTH_REQUIRED.httpStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("WWW-Authenticate", "Bearer realm=\"mini\"");
        byte[] out = JSON.toJSONString(body, JSONWriter.Feature.WriteNulls)
                .getBytes(StandardCharsets.UTF_8);
        response.setContentLength(out.length);
        response.getOutputStream().write(out);
        response.getOutputStream().flush();
    }
}
