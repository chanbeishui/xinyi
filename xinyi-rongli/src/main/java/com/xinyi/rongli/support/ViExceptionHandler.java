package com.xinyi.rongli.support;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 荣利业务接口统一错误体：{@code {code,message}}，HTTP 状态按端点语义返回。
 * 不输出 SQL、对象键或敏感字段。
 */
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex)
    {
        log.error("荣利业务接口异常", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, Codes.Error.INTERNAL, "服务暂时不可用，请稍后重试");
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String message)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
