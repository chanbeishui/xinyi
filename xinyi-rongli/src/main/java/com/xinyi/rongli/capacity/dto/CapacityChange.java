package com.xinyi.rongli.capacity.dto;

public record CapacityChange(int total, long expectedVersion, String reason, Boolean enabled) {
}
