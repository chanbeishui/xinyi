package com.xinyi.rongli.capacity.dto;

public record CapacityView(String slotId, String stationId, String quotaType, String startAt, String endAt, int total, int held, int confirmed, int consumed, int available, boolean enabled, long version, Integer proposedTotal, Boolean canApply) {
}
