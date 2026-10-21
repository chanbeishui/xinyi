package com.xinyi.rongli.support;

import org.springframework.http.HttpStatus;

/**
 * 业务异常：携带 HTTP 状态与稳定错误码，由 {@link ViExceptionHandler} 统一转为 ApiError。
 */
public class ApiException extends RuntimeException
{
    private final HttpStatus status;

    private final String code;

    public ApiException(HttpStatus status, String code, String message)
    {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status()
    {
        return status;
    }

    public String code()
    {
        return code;
    }

    public static ApiException badRequest(String code, String message)
    {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static ApiException unauthorized(String message)
    {
        return new ApiException(HttpStatus.UNAUTHORIZED, Codes.Error.UNAUTHORIZED, message);
    }

    public static ApiException forbidden(String message)
    {
        return new ApiException(HttpStatus.FORBIDDEN, Codes.Error.FORBIDDEN, message);
    }

    public static ApiException notFound(String message)
    {
        return new ApiException(HttpStatus.NOT_FOUND, Codes.Error.NOT_FOUND, message);
    }

    public static ApiException conflict(String code, String message)
    {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    public static ApiException unprocessable(String code, String message)
    {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }

    public static ApiException stateConflict(String message)
    {
        return conflict(Codes.Error.STATE_CONFLICT, message);
    }

    public static ApiException validation(String message)
    {
        return new ApiException(HttpStatus.BAD_REQUEST, Codes.Error.VALIDATION_FAILED, message);
    }
}
