package com.xinyi.rongli.identity.dto;

/**
 * vi 域成功响应信封 {code,msg,data}。与框架 sys 的 AjaxResult 不同：vi 端点用真实 HTTP 状态码，
 * 成功为 200/201，错误经 ViApiErrorAdvice 渲染为 ApiError（真实状态 + errorCode）。
 */
public record ViSuccess<T>(int code, String msg, T data) {

    public static <T> ViSuccess<T> ok(T data) {
        return new ViSuccess<>(200, "操作成功", data);
    }

    public static <T> ViSuccess<T> ok(String msg, T data) {
        return new ViSuccess<>(200, msg, data);
    }
}
