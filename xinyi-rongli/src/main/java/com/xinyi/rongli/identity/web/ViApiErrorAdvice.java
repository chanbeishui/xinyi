package com.xinyi.rongli.identity.web;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.xinyi.rongli.identity.dto.ApiError;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * vi 域错误信封渲染（V6.4 留给"首个 vi Controller 任务"= I01）。仅作用于 com.xinyi.web.controller.rongli
 * 下的控制器（basePackages 限定），@Order(0) 优先于框架未排序的 GlobalExceptionHandler，
 * 确保 vi 端点用【真实 HTTP 状态 + ApiError】而非框架 sys 的"恒 200 + body.code"。
 *
 * 直接写响应（fastjson2 + 显式 Content-Length），与 MiniAuthenticationEntryPoint 一致：
 * 经 ResponseEntity 返回 401 时响应体在安全链回卷中可能被剥离，直写可稳定输出 ApiError 信封。
 * 身份/交易响应 no-store；429/503 带 Retry-After；未预期异常统一 500，不泄露堆栈/SQL/令牌。
 */
@RestControllerAdvice(basePackages = "com.xinyi.web.controller.rongli")
@Order(0)
public class ViApiErrorAdvice {

    @ExceptionHandler(ViException.class)
    public void handleViException(ViException ex, HttpServletResponse response) throws IOException {
        write(response, ApiErrors.from(ex, UUID.randomUUID().toString()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleUnreadable(HttpMessageNotReadableException ex, HttpServletResponse response) throws IOException {
        write(response, ApiErrors.of(ViErrorCode.VI_REQUEST_INVALID,
                ViErrorCode.VI_REQUEST_INVALID.defaultMsg(), UUID.randomUUID().toString()));
    }

    @ExceptionHandler(Exception.class)
    public void handleOther(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(response, ApiErrors.of(ViErrorCode.VI_INTERNAL_ERROR,
                ViErrorCode.VI_INTERNAL_ERROR.defaultMsg(), UUID.randomUUID().toString()));
    }

    private void write(HttpServletResponse response, ApiError body) throws IOException {
        byte[] out = JSON.toJSONString(body, JSONWriter.Feature.WriteNulls).getBytes(StandardCharsets.UTF_8);
        response.reset();
        response.setStatus(body.code());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        if (body.retryAfterSeconds() != null) {
            response.setHeader("Retry-After", String.valueOf(body.retryAfterSeconds()));
        }
        response.setContentLength(out.length);
        response.getOutputStream().write(out);
        response.getOutputStream().flush();
    }
}
