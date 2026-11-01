package com.xinyi.rongli.identity.dto;

import java.util.List;

/** vi 域分页列表信封 {code,msg,rows,total}（契约 API_010 等列表查询；与 data 信封区分）。 */
public record ViRows<T>(int code, String msg, List<T> rows, long total) {

    public static <T> ViRows<T> of(List<T> rows, long total) {
        return new ViRows<>(200, "操作成功", rows, total);
    }
}