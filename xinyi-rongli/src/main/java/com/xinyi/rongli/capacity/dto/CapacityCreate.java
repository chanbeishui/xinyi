package com.xinyi.rongli.capacity.dto;

public record CapacityCreate(String stationId, String quotaType, String startAt, String endAt, int total, String reason) {
}
