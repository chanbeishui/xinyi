package com.xinyi.rongli.capacity.dto;

/** 站点额度公开投影；接车与接收类型分别维护，版本用于防止覆盖并发修改。 */
public record CapacityView(
        String slotId,
        String stationId,
        String quotaType,
        String startAt,
        String endAt,
        int total,
        int held,
        int confirmed,
        int consumed,
        int available,
        boolean enabled,
        long version,
        Integer proposedTotal,
        Boolean canApply) {}
