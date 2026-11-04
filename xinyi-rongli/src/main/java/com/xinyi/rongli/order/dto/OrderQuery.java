package com.xinyi.rongli.order.dto;

import java.time.Instant;

/** 仅声明公开查询条件；内部数据权限范围由OrderReadScope单独构建。 */
public record OrderQuery(String status, Instant startAt, Instant endAt, int pageNum, int pageSize) {
    public int offset() {
        return Math.multiplyExact(pageNum - 1, pageSize);
    }
}
