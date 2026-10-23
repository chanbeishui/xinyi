package com.xinyi.rongli.support;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 荣利业务接口统一错误体：{@code {code,message,msg}}，HTTP 状态按端点语义返回。
 * 不输出 SQL、对象键或敏感字段。
 *
 * <p>必须优先于框架的 {@code GlobalExceptionHandler} 生效：后者是把任何异常都包成
 * HTTP 200 + {@code {code:500,msg}}，会让小程序端拿不到 401/403/409/422，也无法
 * 依据状态码触发令牌续期。这里用最高优先级把荣利控制器包内的异常收回来。</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.xinyi.rongli.controller")
public class ViExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(ViExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex)
    {
        return body(ex.status(), ex.code(), ex.getMessage());
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<Map<String, Object>> handleValidation(Exception ex)
    {
        String message = "请求参数不合法";
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null)
        {
            message = manv.getBindingResult().getFieldError().getDefaultMessage();
        }
        else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null)
        {
            message = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return body(HttpStatus.BAD_REQUEST, Codes.Error.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleHeader(MissingRequestHeaderException ex)
    {
        return body(HttpStatus.BAD_REQUEST, Codes.Error.VALIDATION_FAILED, "缺少请求头：" + ex.getHeaderName());
    }

    /**
     * 方法级鉴权失败必须是 403，不能落进兜底分支变成 500。
     * 否则前端会把"没有权限"显示为"服务暂时不可用"，误导运维排查方向。
     */
    @ExceptionHandler({ org.springframework.security.authorization.AuthorizationDeniedException.class,
            org.springframework.security.access.AccessDeniedException.class })
    public ResponseEntity<Map<String, Object>> handleDenied(Exception ex)
    {
        return body(HttpStatus.FORBIDDEN, Codes.Error.FORBIDDEN, "无权访问该资源");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex)
    {
        log.error("荣利业务接口异常", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, Codes.Error.INTERNAL, "服务暂时不可用，请稍后重试");
    }

    /**
     * 同时带 {@code message} 与 {@code msg}：小程序端读 message，后台管理端读 msg，
     * 双写可以避免两侧因字段名不同而显示"系统未知错误"。
     */
    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String message)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("msg", message);
        return ResponseEntity.status(status).body(body);
    }
}
